package com.nyxtra.vpn.ui.screens.dashboard

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nyxtra.vpn.core.NyxtraVpnController
import com.nyxtra.vpn.data.model.LogLevel
import com.nyxtra.vpn.data.model.TrafficStats
import com.nyxtra.vpn.data.model.VpnProfile
import com.nyxtra.vpn.data.model.VpnState
import com.nyxtra.vpn.data.repository.MockLogsRepository
import com.nyxtra.vpn.data.repository.MockProfileRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class DashboardViewModel : ViewModel() {

    val vpnState: StateFlow<VpnState> = NyxtraVpnController.vpnState
    val trafficStats: StateFlow<TrafficStats> = NyxtraVpnController.trafficStats

    val activeProfile: StateFlow<VpnProfile?> = MockProfileRepository.profiles
        .map { list -> list.firstOrNull { it.isSelected } ?: list.firstOrNull() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, MockProfileRepository.getSelectedProfile())

    fun toggleConnection(context: Context, onRequirePermission: (Intent) -> Unit) {
        when (vpnState.value) {
            VpnState.DISCONNECTED, VpnState.ERROR -> {
                val profile = activeProfile.value
                if (profile == null) {
                    MockLogsRepository.addLog(LogLevel.ERROR, "TUNNEL", "Cannot connect: No profile selected")
                    return
                }

                val prepareIntent = NyxtraVpnController.getPrepareIntent(context)
                if (prepareIntent != null) {
                    onRequirePermission(prepareIntent)
                } else {
                    NyxtraVpnController.startVpn(context, profile.id)
                }
            }
            VpnState.CONNECTED, VpnState.CONNECTING -> {
                NyxtraVpnController.stopVpn(context)
            }
            else -> {}
        }
    }

    fun onPermissionGranted(context: Context) {
        val profile = activeProfile.value ?: return
        NyxtraVpnController.startVpn(context, profile.id)
    }
}
