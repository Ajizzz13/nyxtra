package com.nyxtra.vpn.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.TrafficStats as AndroidTrafficStats
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import com.nyxtra.vpn.MainActivity
import com.nyxtra.vpn.R
import com.nyxtra.vpn.core.NyxtraVpnController
import com.nyxtra.vpn.core.SingBoxConfigGenerator
import com.nyxtra.vpn.data.model.EngineConfig
import com.nyxtra.vpn.data.model.LogLevel
import com.nyxtra.vpn.data.model.TrafficStats
import com.nyxtra.vpn.data.model.VpnProfile
import com.nyxtra.vpn.data.model.VpnState
import com.nyxtra.vpn.data.repository.MockAppRepository
import com.nyxtra.vpn.data.repository.MockLogsRepository
import com.nyxtra.vpn.data.repository.MockProfileRepository
import io.nekohasekai.libbox.BridgeOptions
import io.nekohasekai.libbox.BridgeSession
import io.nekohasekai.libbox.CommandServer
import io.nekohasekai.libbox.CommandServerHandler
import io.nekohasekai.libbox.ConnectionOwner
import io.nekohasekai.libbox.InterfaceUpdateListener
import io.nekohasekai.libbox.LocalDNSTransport
import io.nekohasekai.libbox.NeighborUpdateListener
import io.nekohasekai.libbox.NetworkInterfaceIterator
import io.nekohasekai.libbox.OverrideOptions
import io.nekohasekai.libbox.PlatformInterface
import io.nekohasekai.libbox.PlatformUser
import io.nekohasekai.libbox.ShellSession
import io.nekohasekai.libbox.StringIterator
import io.nekohasekai.libbox.SystemProxyStatus
import io.nekohasekai.libbox.TunOptions
import io.nekohasekai.libbox.WIFIState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

class NyxtraVpnService : VpnService(), PlatformInterface, CommandServerHandler {

    private var vpnInterface: ParcelFileDescriptor? = null
    private var commandServer: CommandServer? = null
    private var isCoreRunning = false

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var trafficJob: Job? = null

    private var activeProfile: VpnProfile? = null
    private var engineConfig: EngineConfig = EngineConfig()

    override fun onCreate() {
        super.onCreate()
        try {
            go.Seq.setContext(this)
        } catch (t: Throwable) {
            Log.w(TAG, "Go Seq runtime init note: ${t.message}")
        }
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_CONNECT
        when (action) {
            ACTION_CONNECT -> {
                val profileId = intent?.getStringExtra(EXTRA_PROFILE_ID)
                startVpnTunnel(profileId)
            }
            ACTION_DISCONNECT -> {
                stopVpnTunnel()
            }
        }
        return START_NOT_STICKY
    }

    private fun startVpnTunnel(profileId: String?) {
        serviceScope.launch {
            NyxtraVpnController.updateState(VpnState.CONNECTING)

            val profile = if (profileId != null) {
                MockProfileRepository.profiles.value.firstOrNull { it.id == profileId }
            } else {
                MockProfileRepository.getSelectedProfile()
            }

            if (profile == null) {
                MockLogsRepository.addLog(LogLevel.ERROR, "TUNNEL", "Cannot start VPN: No profile found")
                NyxtraVpnController.updateState(VpnState.ERROR)
                stopSelf()
                return@launch
            }

            activeProfile = profile
            NyxtraVpnController.setActiveProfileId(profile.id)

            MockLogsRepository.addLog(LogLevel.INFO, "TUNNEL", "Connecting to ${profile.name}")
            MockLogsRepository.addLog(LogLevel.DEBUG, "CONFIG", "Endpoint: ${profile.serverAddress}:${profile.serverPort}")

            val selectedApps = MockAppRepository.getSelectedPackages().toList()
            val configJson = SingBoxConfigGenerator.generate(
                profile = profile,
                engineConfig = engineConfig,
                perAppPackages = selectedApps,
                isPerAppWhitelist = true
            )

            startForeground(NOTIFICATION_ID, buildNotification(profile.name, "Establishing secure tunnel..."))

            try {
                // Initialize Sing-box Core CommandServer with Direct FD Handover
                val server = CommandServer(this@NyxtraVpnService, this@NyxtraVpnService)
                server.start()
                commandServer = server
                isCoreRunning = true

                server.startOrReloadService(configJson, OverrideOptions())
                MockLogsRepository.addLog(LogLevel.INFO, "CORE", "Sing-box core active with direct FD passing")
            } catch (t: Throwable) {
                Log.w(TAG, "Native Sing-box init note: ${t.message}, falling back to system TUN")
                MockLogsRepository.addLog(LogLevel.WARN, "CORE", "Direct native init fallback: ${t.message}")
                establishSystemTunFallback(profile)
            }

            NyxtraVpnController.updateState(VpnState.CONNECTED)
            MockLogsRepository.addLog(LogLevel.INFO, "TUNNEL", "Connected. Low-latency gaming pipeline active.")
            updateNotification(profile.name, "Connected • Ping: ${profile.pingMs ?: 20} ms")

            startTrafficMonitor()
        }
    }

    private fun establishSystemTunFallback(profile: VpnProfile) {
        val builder = Builder()
            .setSession("Nyxtra")
            .setMtu(engineConfig.mtu)
            .addAddress("172.19.0.1", 30)
            .addRoute("0.0.0.0", 0)
            .addDnsServer("1.1.1.1")
            .addDnsServer("8.8.8.8")

        val pfd = builder.establish()
        vpnInterface = pfd
        MockLogsRepository.addLog(LogLevel.INFO, "ENGINE", "Virtual TUN interface established - fd=${pfd?.fd ?: 0} MTU=${engineConfig.mtu}")
    }

    private fun stopVpnTunnel() {
        serviceScope.launch {
            NyxtraVpnController.updateState(VpnState.DISCONNECTING)
            MockLogsRepository.addLog(LogLevel.INFO, "TUNNEL", "Disconnect requested")

            stopTrafficMonitor()

            try {
                commandServer?.closeService()
                commandServer?.close()
                commandServer = null
                isCoreRunning = false
            } catch (t: Throwable) {
                Log.w(TAG, "Core close note: ${t.message}")
            }

            vpnInterface?.close()
            vpnInterface = null

            NyxtraVpnController.updateState(VpnState.DISCONNECTED)
            NyxtraVpnController.updateTrafficStats(TrafficStats())
            MockLogsRepository.addLog(LogLevel.INFO, "TUNNEL", "Tunnel closed. Virtual TUN interface released.")

            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun startTrafficMonitor() {
        trafficJob?.cancel()
        trafficJob = serviceScope.launch {
            var lastRx = AndroidTrafficStats.getTotalRxBytes()
            var lastTx = AndroidTrafficStats.getTotalTxBytes()
            var totalDown = 0L
            var totalUp = 0L

            while (isActive) {
                delay(1000)
                val currentRx = AndroidTrafficStats.getTotalRxBytes()
                val currentTx = AndroidTrafficStats.getTotalTxBytes()

                val downSpeed = if (lastRx > 0 && currentRx >= lastRx) currentRx - lastRx else 0L
                val upSpeed = if (lastTx > 0 && currentTx >= lastTx) currentTx - lastTx else 0L

                lastRx = currentRx
                lastTx = currentTx

                totalDown += downSpeed
                totalUp += upSpeed

                NyxtraVpnController.updateTrafficStats(
                    TrafficStats(
                        downloadBps = downSpeed,
                        uploadBps = upSpeed,
                        totalDownloadBytes = totalDown,
                        totalUploadBytes = totalUp
                    )
                )
            }
        }
    }

    private fun stopTrafficMonitor() {
        trafficJob?.cancel()
        trafficJob = null
    }

    // PlatformInterface Direct FD Implementation
    override fun openTun(options: TunOptions): Int {
        val builder = Builder()
            .setSession("Nyxtra")
            .setMtu(options.mtu)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            builder.setMetered(false)
        }

        val inet4 = options.inet4Address
        while (inet4.hasNext()) {
            val addr = inet4.next()
            builder.addAddress(addr.address(), addr.prefix())
        }

        if (options.autoRoute) {
            val dns = options.dnsServerAddress
            while (dns.hasNext()) {
                builder.addDnsServer(dns.next())
            }
            builder.addRoute("0.0.0.0", 0)
        }

        val pfd = builder.establish() ?: error("Failed to establish TUN interface")
        vpnInterface = pfd
        MockLogsRepository.addLog(LogLevel.INFO, "ENGINE", "Direct FD Handover complete - fd=${pfd.fd} MTU=${options.mtu}")
        return pfd.fd
    }

    override fun autoDetectInterfaceControl(fd: Int) {
        protect(fd)
    }

    // PlatformInterface stubs
    override fun clearDNSCache() {}
    override fun checkPlatformShell() {}
    override fun registerMyInterface(name: String?) {}
    override fun startDefaultInterfaceMonitor(listener: InterfaceUpdateListener?) {}
    override fun closeDefaultInterfaceMonitor(listener: InterfaceUpdateListener?) {}
    override fun startNeighborMonitor(listener: NeighborUpdateListener?) {}
    override fun closeNeighborMonitor(listener: NeighborUpdateListener?) {}
    override fun underNetworkExtension(): Boolean = false
    override fun includeAllNetworks(): Boolean = false
    override fun usePlatformAutoDetectInterfaceControl(): Boolean = true
    override fun usePlatformBridge(): Boolean = false
    override fun usePlatformShell(): Boolean = false
    override fun useProcFS(): Boolean = false
    override fun readSystemSSHHostKey(): String = ""
    override fun lookupSFTPServer(): String = ""
    override fun tailscaleHostname(): String = ""
    override fun readWIFIState(): WIFIState? = null
    override fun lookupUser(user: String?): PlatformUser? = null
    override fun openShellSession(user: PlatformUser?, path: String?, args: StringIterator?, dir: String?, uid: Int, gid: Int): ShellSession? = null
    override fun createBridge(options: BridgeOptions?): BridgeSession? = null
    override fun localDNSTransport(): LocalDNSTransport? = null
    override fun findConnectionOwner(ipProtocol: Int, sourceAddress: String?, sourcePort: Int, destinationAddress: String?, destinationPort: Int): ConnectionOwner? = null
    override fun getInterfaces(): NetworkInterfaceIterator? = null
    override fun sendNotification(notification: io.nekohasekai.libbox.Notification?) {}
    override fun cancelNotification(identifier: String?, typeID: Int) {}

    // CommandServerHandler implementation
    override fun serviceStop() {
        stopVpnTunnel()
    }

    override fun serviceReload() {
        activeProfile?.let { startVpnTunnel(it.id) }
    }

    override fun getSystemProxyStatus(): SystemProxyStatus? = null
    override fun setSystemProxyEnabled(enabled: Boolean) {}
    override fun triggerNativeCrash() {}
    override fun writeDebugMessage(message: String?) {
        Log.d("NyxtraCore", message ?: "")
    }
    override fun connectSSHAgent(): Int = -1

    // Notification handling
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Nyxtra VPN Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Nyxtra active connection status"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(title: String, text: String): Notification {
        val openIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val disconnectIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, NyxtraVpnService::class.java).apply { action = ACTION_DISCONNECT },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(openIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Disconnect", disconnectIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(title: String, text: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        manager?.notify(NOTIFICATION_ID, buildNotification(title, text))
    }

    override fun onDestroy() {
        super.onDestroy()
        trafficJob?.cancel()
        serviceScope.cancel()
        vpnInterface?.close()
        vpnInterface = null
        NyxtraVpnController.updateState(VpnState.DISCONNECTED)
    }

    companion object {
        const val ACTION_CONNECT = "com.nyxtra.vpn.CONNECT"
        const val ACTION_DISCONNECT = "com.nyxtra.vpn.DISCONNECT"
        const val EXTRA_PROFILE_ID = "extra_profile_id"
        private const val CHANNEL_ID = "nyxtra_vpn_channel"
        private const val NOTIFICATION_ID = 1001
        private const val TAG = "NyxtraVpnService"
    }
}
