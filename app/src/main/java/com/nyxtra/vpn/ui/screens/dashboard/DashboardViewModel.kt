package com.nyxtra.vpn.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nyxtra.vpn.data.model.LogLevel
import com.nyxtra.vpn.data.model.TrafficStats
import com.nyxtra.vpn.data.model.VpnProfile
import com.nyxtra.vpn.data.model.VpnState
import com.nyxtra.vpn.data.repository.MockLogsRepository
import com.nyxtra.vpn.data.repository.MockProfileRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

class DashboardViewModel : ViewModel() {

    private val _vpnState = MutableStateFlow(VpnState.DISCONNECTED)
    val vpnState: StateFlow<VpnState> = _vpnState.asStateFlow()

    private val _trafficStats = MutableStateFlow(TrafficStats())
    val trafficStats: StateFlow<TrafficStats> = _trafficStats.asStateFlow()

    val activeProfile: StateFlow<VpnProfile?> = MockProfileRepository.profiles
        .map { list -> list.firstOrNull { it.isSelected } ?: list.firstOrNull() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, MockProfileRepository.getSelectedProfile())

    private var trafficJob: Job? = null

    fun toggleConnection() {
        when (_vpnState.value) {
            VpnState.DISCONNECTED -> startConnection()
            VpnState.CONNECTED -> stopConnection()
            else -> {}
        }
    }

    private fun startConnection() {
        val profile = activeProfile.value
        if (profile == null) {
            MockLogsRepository.addLog(LogLevel.ERROR, "TUNNEL", "Cannot connect: No profile selected")
            return
        }

        viewModelScope.launch {
            _vpnState.value = VpnState.CONNECTING
            MockLogsRepository.addLog(LogLevel.INFO, "TUNNEL", "Initiating tunnel connection to ${profile.name}")
            MockLogsRepository.addLog(LogLevel.DEBUG, "CONFIG", "Outbound target: ${profile.serverAddress}:${profile.serverPort} via ${profile.transport.displayName}")
            MockLogsRepository.addLog(LogLevel.DEBUG, "ENGINE", "Acquiring TUN File Descriptor via VpnService.establish()...")

            delay(600)
            MockLogsRepository.addLog(LogLevel.INFO, "ENGINE", "Direct FD handover complete (fd=27, MTU=1280)")
            MockLogsRepository.addLog(LogLevel.INFO, "TUNNEL", "Handshake succeeded. Zero-sniffing gaming pipeline active.")

            _vpnState.value = VpnState.CONNECTED
            startTrafficSimulation()
        }
    }

    private fun stopConnection() {
        viewModelScope.launch {
            _vpnState.value = VpnState.DISCONNECTING
            MockLogsRepository.addLog(LogLevel.INFO, "TUNNEL", "Disconnect requested by user")
            stopTrafficSimulation()
            delay(300)
            _vpnState.value = VpnState.DISCONNECTED
            MockLogsRepository.addLog(LogLevel.INFO, "TUNNEL", "Tunnel closed. Virtual TUN interface released.")
        }
    }

    private fun startTrafficSimulation() {
        trafficJob?.cancel()
        trafficJob = viewModelScope.launch {
            var totalDown = _trafficStats.value.totalDownloadBytes
            var totalUp = _trafficStats.value.totalUploadBytes

            while (isActive) {
                delay(1000)
                val downSpeed = Random.nextLong(120_000, 1_450_000)
                val upSpeed = Random.nextLong(45_000, 380_000)
                totalDown += downSpeed
                totalUp += upSpeed

                _trafficStats.value = TrafficStats(
                    downloadBps = downSpeed,
                    uploadBps = upSpeed,
                    totalDownloadBytes = totalDown,
                    totalUploadBytes = totalUp
                )
            }
        }
    }

    private fun stopTrafficSimulation() {
        trafficJob?.cancel()
        trafficJob = null
        _trafficStats.value = _trafficStats.value.copy(downloadBps = 0L, uploadBps = 0L)
    }

    fun pingCurrentProfile() {
        activeProfile.value?.let { profile ->
            viewModelScope.launch {
                MockProfileRepository.pingProfile(profile.id)
            }
        }
    }
}
