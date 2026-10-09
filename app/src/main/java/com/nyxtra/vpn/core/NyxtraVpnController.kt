package com.nyxtra.vpn.core

import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Build
import com.nyxtra.vpn.data.model.TrafficStats
import com.nyxtra.vpn.data.model.VpnState
import com.nyxtra.vpn.service.NyxtraVpnService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object NyxtraVpnController {

    private val _vpnState = MutableStateFlow(VpnState.DISCONNECTED)
    val vpnState: StateFlow<VpnState> = _vpnState.asStateFlow()

    private val _trafficStats = MutableStateFlow(TrafficStats())
    val trafficStats: StateFlow<TrafficStats> = _trafficStats.asStateFlow()

    private val _activeProfileId = MutableStateFlow<String?>(null)
    val activeProfileId: StateFlow<String?> = _activeProfileId.asStateFlow()

    fun updateState(state: VpnState) {
        _vpnState.value = state
    }

    fun updateTrafficStats(stats: TrafficStats) {
        _trafficStats.value = stats
    }

    fun setActiveProfileId(id: String?) {
        _activeProfileId.value = id
    }

    fun isVpnPrepared(context: Context): Boolean {
        return VpnService.prepare(context) == null
    }

    fun getPrepareIntent(context: Context): Intent? {
        return VpnService.prepare(context)
    }

    fun startVpn(context: Context, profileId: String) {
        val intent = Intent(context, NyxtraVpnService::class.java).apply {
            action = NyxtraVpnService.ACTION_CONNECT
            putExtra(NyxtraVpnService.EXTRA_PROFILE_ID, profileId)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun stopVpn(context: Context) {
        val intent = Intent(context, NyxtraVpnService::class.java).apply {
            action = NyxtraVpnService.ACTION_DISCONNECT
        }
        context.startService(intent)
    }
}
