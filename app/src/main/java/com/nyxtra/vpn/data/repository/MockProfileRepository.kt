package com.nyxtra.vpn.data.repository

import com.nyxtra.vpn.data.model.ProtocolType
import com.nyxtra.vpn.data.model.TransportType
import com.nyxtra.vpn.data.model.VpnProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

object MockProfileRepository {

    private val initialProfiles = listOf(
        VpnProfile(
            id = "sg-melbi-01",
            name = "SG Melbikomas Gaming [Low Latency]",
            protocol = ProtocolType.VLESS,
            serverAddress = "sg-node1.nyxtra.net",
            serverPort = 443,
            uuidOrPassword = "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d",
            bugHost = "graph.facebook.com",
            sni = "graph.facebook.com",
            path = "/nyxtra-vless-ws",
            transport = TransportType.WS,
            isTls = true,
            pingMs = 28L,
            isSelected = true
        ),
        VpnProfile(
            id = "id-biznet-02",
            name = "ID Biznet Gio Gaming [Direct Route]",
            protocol = ProtocolType.VMESS,
            serverAddress = "id-gio.nyxtra.net",
            serverPort = 443,
            uuidOrPassword = "f0e1d2c3-b4a5-6789-0123-456789abcdef",
            bugHost = "quiz.int.vidio.com",
            sni = "quiz.int.vidio.com",
            path = "/nyxtra-vmess",
            transport = TransportType.HTTP_UPGRADE,
            isTls = true,
            pingMs = 18L,
            isSelected = false
        ),
        VpnProfile(
            id = "sg-trojan-03",
            name = "SG DigitalOcean Trojan [HTTPUpgrade]",
            protocol = ProtocolType.TROJAN,
            serverAddress = "sg-trojan.nyxtra.net",
            serverPort = 443,
            uuidOrPassword = "nyxtra_super_secret_trojan_pass",
            bugHost = "support.zoom.us",
            sni = "support.zoom.us",
            path = "/trojan-upgrade",
            transport = TransportType.HTTP_UPGRADE,
            isTls = true,
            pingMs = 35L,
            isSelected = false
        )
    )

    private val _profiles = MutableStateFlow<List<VpnProfile>>(initialProfiles)
    val profiles: StateFlow<List<VpnProfile>> = _profiles.asStateFlow()

    fun selectProfile(id: String) {
        _profiles.update { list ->
            list.map { it.copy(isSelected = it.id == id) }
        }
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
    }

    fun updateProfile(profile: VpnProfile) {
        _profiles.update { list ->
            list.map { if (it.id == profile.id) profile else it }
        }
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
    }

    suspend fun pingProfile(id: String): Long {
        val simulatedPing = Random.nextLong(15, 65)
        _profiles.update { list ->
            list.map {
                if (it.id == id) it.copy(pingMs = simulatedPing) else it
            }
        }
        return simulatedPing
    }

    suspend fun pingAll() {
        _profiles.update { list ->
            list.map {
                it.copy(pingMs = Random.nextLong(15, 80))
            }
        }
    }
}
