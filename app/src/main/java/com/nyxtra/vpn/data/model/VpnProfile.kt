package com.nyxtra.vpn.data.model

import java.util.UUID

data class VpnProfile(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val protocol: ProtocolType = ProtocolType.VLESS,
    val serverAddress: String,
    val serverPort: Int = 443,
    val uuidOrPassword: String,
    val bugHost: String = "",
    val sni: String = "",
    val path: String = "/",
    val transport: TransportType = TransportType.WS,
    val isTls: Boolean = true,
    val allowInsecure: Boolean = false,
    val pingMs: Long? = null,
    val isSelected: Boolean = false
) {
    val effectiveSni: String
        get() = bugHost.ifBlank { sni.ifBlank { serverAddress } }

    val effectiveHostHeader: String
        get() = bugHost.ifBlank { sni }
}
