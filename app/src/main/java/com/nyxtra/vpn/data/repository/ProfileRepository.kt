package com.nyxtra.vpn.data.repository

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nyxtra.vpn.data.model.VpnProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.io.File
import java.net.InetSocketAddress
import java.net.Socket

object ProfileRepository {

    private const val PING_TIMEOUT_MS = 2500
    private const val PING_CONCURRENCY = 6

    private val gson = Gson()
    private var storageFile: File? = null

    private val _profiles = MutableStateFlow<List<VpnProfile>>(emptyList())
    val profiles: StateFlow<List<VpnProfile>> = _profiles.asStateFlow()

    fun init(context: Context) {
        if (storageFile != null) return
        val file = File(context.filesDir, "nyxtra_profiles.json")
        storageFile = file
        if (file.exists()) {
            try {
                val json = file.readText()
                val type = object : TypeToken<List<VpnProfile>>() {}.type
                val loaded: List<VpnProfile>? = gson.fromJson(json, type)
                if (loaded != null) {
                    _profiles.value = loaded
                }
            } catch (e: Exception) {
                Log.w("ProfileRepository", "Failed to load profiles: ${e.message}")
            }
        }
    }

    private fun persist() {
        val file = storageFile ?: return
        try {
            val json = gson.toJson(_profiles.value)
            file.writeText(json)
        } catch (e: Exception) {
            Log.w("ProfileRepository", "Failed to save profiles: ${e.message}")
        }
    }

    fun selectProfile(id: String) {
        _profiles.update { list ->
            list.map { it.copy(isSelected = it.id == id) }
        }
        persist()
    }

    fun getSelectedProfile(): VpnProfile? {
        return _profiles.value.firstOrNull { it.isSelected } ?: _profiles.value.firstOrNull()
    }

    fun addProfile(profile: VpnProfile) {
        _profiles.update { list ->
            val hasSelected = list.any { it.isSelected }
            val newProfile = if (!hasSelected) profile.copy(isSelected = true) else profile
            list + newProfile
        }
        persist()
    }

    fun updateProfile(profile: VpnProfile) {
        _profiles.update { list ->
            list.map { if (it.id == profile.id) profile else it }
        }
        persist()
    }

    fun deleteProfile(id: String) {
        _profiles.update { list ->
            val filtered = list.filterNot { it.id == id }
            if (filtered.isNotEmpty() && filtered.none { it.isSelected }) {
                filtered.mapIndexed { index, item ->
                    if (index == 0) item.copy(isSelected = true) else item
                }
            } else {
                filtered
            }
        }
        persist()
    }

    /**
     * TCP connect latency to the profile endpoint. Not ICMP: Android denies raw
     * sockets to unprivileged apps, so this measures handshake time to the port.
     * Returns -1 on failure.
     */
    private fun measureTcpLatency(host: String, port: Int): Long {
        return try {
            val start = System.currentTimeMillis()
            // use{} guarantees close() even when connect() throws, which the
            // previous implementation leaked on every timeout.
            Socket().use { socket ->
                socket.connect(InetSocketAddress(host, port), PING_TIMEOUT_MS)
                System.currentTimeMillis() - start
            }
        } catch (_: Exception) {
            -1L
        }
    }

    suspend fun pingProfile(id: String): Long = withContext(Dispatchers.IO) {
        val target = _profiles.value.firstOrNull { it.id == id } ?: return@withContext -1L
        val host = target.serverAddress.ifBlank { "1.1.1.1" }
        val port = if (target.serverPort > 0) target.serverPort else 443

        val pingResult = measureTcpLatency(host, port)

        _profiles.update { list ->
            list.map {
                if (it.id == id) it.copy(pingMs = pingResult) else it
            }
        }
        persist()
        pingResult
    }

    suspend fun pingAll() = withContext(Dispatchers.IO) {
        coroutineScope {
            val currentProfiles = _profiles.value
            // Bound concurrency: previously every profile opened a socket at
            // once, which exhausts FDs on large lists.
            val semaphore = Semaphore(PING_CONCURRENCY)
            val deferredPings = currentProfiles.map { profile ->
                async {
                    semaphore.withPermit {
                        val host = profile.serverAddress.ifBlank { "1.1.1.1" }
                        val port = if (profile.serverPort > 0) profile.serverPort else 443
                        profile.id to measureTcpLatency(host, port)
                    }
                }
            }

            val results = deferredPings.awaitAll().toMap()
            _profiles.update { list ->
                list.map {
                    val updatedPing = results[it.id] ?: it.pingMs
                    it.copy(pingMs = updatedPing)
                }
            }
            persist()
        }
    }
}
