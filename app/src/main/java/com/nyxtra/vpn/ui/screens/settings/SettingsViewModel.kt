package com.nyxtra.vpn.ui.screens.settings

import androidx.lifecycle.ViewModel
import com.nyxtra.vpn.data.model.EngineConfig
import com.nyxtra.vpn.data.model.TunStackMode
import com.nyxtra.vpn.data.repository.EngineConfigRepository
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel : ViewModel() {

    val config: StateFlow<EngineConfig> = EngineConfigRepository.config

    fun updateMtu(mtu: Int) {
        EngineConfigRepository.updateMtu(mtu)
    }

    fun updateTunStack(stack: TunStackMode) {
        EngineConfigRepository.updateTunStack(stack)
    }

    fun toggleZeroRouting(enabled: Boolean) {
        EngineConfigRepository.toggleZeroRouting(enabled)
    }

    fun toggleTcpNoDelay(enabled: Boolean) {
        EngineConfigRepository.toggleTcpNoDelay(enabled)
    }

    fun toggleBlockQuic(enabled: Boolean) {
        EngineConfigRepository.toggleBlockQuic(enabled)
    }

    fun updateKeepalive(seconds: Int) {
        EngineConfigRepository.updateKeepalive(seconds)
    }

    fun resetToDefaults() {
        EngineConfigRepository.resetToDefaults()
    }
}
