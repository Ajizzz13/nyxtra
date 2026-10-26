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
    // REALITY parameters. Empty means REALITY is not used.
    val realityPublicKey: String = "",
    val realityShortId: String = "",
    val realityFingerprint: String = "chrome",
    val pingMs: Long? = null,
    val isSelected: Boolean = false
) {
    val isReality: Boolean
        get() = realityPublicKey.isNotBlank()

    val effectiveSni: String
        get() = bugHost.ifBlank { sni.ifBlank { serverAddress } }

    // Previously returned an empty string when both bugHost and sni were blank,
    // which produced "host=" in exported URIs. Mirror the generator fallback.
    val effectiveHostHeader: String
        get() = bugHost.ifBlank { sni.ifBlank { serverAddress } }
}
