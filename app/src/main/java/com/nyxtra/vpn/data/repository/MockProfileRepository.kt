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
import kotlinx.coroutines.withContext
import java.io.File
import java.net.InetSocketAddress
import java.net.Socket

object MockProfileRepository {

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

    suspend fun pingProfile(id: String): Long = withContext(Dispatchers.IO) {
        val target = _profiles.value.firstOrNull { it.id == id } ?: return@withContext -1L
        val host = if (target.serverAddress.isNotBlank()) target.serverAddress else "1.1.1.1"
        val port = if (target.serverPort > 0) target.serverPort else 443

        val pingResult = try {
            val start = System.currentTimeMillis()
            val socket = Socket()
            socket.connect(InetSocketAddress(host, port), 2500)
            val duration = System.currentTimeMillis() - start
            socket.close()
            duration
        } catch (e: Exception) {
            -1L
        }

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
            val deferredPings = currentProfiles.map { profile ->
                async {
                    val host = if (profile.serverAddress.isNotBlank()) profile.serverAddress else "1.1.1.1"
                    val port = if (profile.serverPort > 0) profile.serverPort else 443
                    val pingResult = try {
                        val start = System.currentTimeMillis()
                        val socket = Socket()
                        socket.connect(InetSocketAddress(host, port), 2500)
                        val duration = System.currentTimeMillis() - start
                        socket.close()
                        duration
                    } catch (e: Exception) {
                        -1L
                    }
                    profile.id to pingResult
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
