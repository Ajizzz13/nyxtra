package com.nyxtra.vpn.ui.screens.settings

import androidx.lifecycle.ViewModel
import com.nyxtra.vpn.data.model.EngineConfig
import com.nyxtra.vpn.data.model.TunStackMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SettingsViewModel : ViewModel() {

    private val _config = MutableStateFlow(EngineConfig())
    val config: StateFlow<EngineConfig> = _config.asStateFlow()

    fun updateMtu(mtu: Int) {
        _config.update { it.copy(mtu = mtu.coerceIn(1280, 1340)) }
    }

    fun updateTunStack(stack: TunStackMode) {
        _config.update { it.copy(tunStack = stack) }
    }

    fun toggleZeroRouting(enabled: Boolean) {
        _config.update { it.copy(zeroRoutingSniffing = enabled) }
    }

    fun toggleTcpNoDelay(enabled: Boolean) {
        _config.update { it.copy(tcpNoDelay = enabled) }
    }

    fun updateKeepalive(seconds: Int) {
        _config.update { it.copy(keepaliveIntervalSeconds = seconds.coerceIn(5, 60)) }
    }

    fun resetToDefaults() {
        _config.value = EngineConfig()
    }
}
