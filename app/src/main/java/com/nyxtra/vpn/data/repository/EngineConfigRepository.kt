package com.nyxtra.vpn.data.repository

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.nyxtra.vpn.data.model.EngineConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File

object EngineConfigRepository {

    private const val FILE_NAME = "nyxtra_engine.json"

    private val gson = Gson()
    private var storageFile: File? = null

    private val _config = MutableStateFlow(EngineConfig())
    val config: StateFlow<EngineConfig> = _config.asStateFlow()

    fun init(context: Context) {
        if (storageFile != null) return
        val file = File(context.filesDir, FILE_NAME)
        storageFile = file
        if (file.exists()) {
            try {
                val loaded = gson.fromJson(file.readText(), EngineConfig::class.java)
                if (loaded != null) {
                    _config.value = loaded.copy(
                        mtu = loaded.mtu.coerceIn(1280, 1340),
                        keepaliveIntervalSeconds = loaded.keepaliveIntervalSeconds.coerceIn(5, 60)
                    )
                }
            } catch (e: Exception) {
                Log.w("EngineConfig", "Failed to load engine config: ${e.message}")
            }
        }
    }

    fun current(): EngineConfig = _config.value

    private fun save(config: EngineConfig) {
        _config.value = config
        val file = storageFile ?: return
        try {
            file.writeText(gson.toJson(config))
        } catch (e: Exception) {
            Log.w("EngineConfig", "Failed to save engine config: ${e.message}")
        }
    }

    fun updateMtu(mtu: Int) {
        save(_config.value.copy(mtu = mtu.coerceIn(1280, 1340)))
    }

    fun updateTunStack(stack: com.nyxtra.vpn.data.model.TunStackMode) {
        save(_config.value.copy(tunStack = stack))
    }

    fun toggleZeroRouting(enabled: Boolean) {
        save(_config.value.copy(zeroRoutingSniffing = enabled))
    }

    fun toggleTcpNoDelay(enabled: Boolean) {
        save(_config.value.copy(tcpNoDelay = enabled))
    }

    fun updateKeepalive(seconds: Int) {
        save(_config.value.copy(keepaliveIntervalSeconds = seconds.coerceIn(5, 60)))
    }

    fun resetToDefaults() {
        save(EngineConfig())
    }
}
