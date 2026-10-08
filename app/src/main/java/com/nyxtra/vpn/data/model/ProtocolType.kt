package com.nyxtra.vpn.data.model

enum class ProtocolType(val displayName: String, val scheme: String) {
    VLESS("VLESS", "vless"),
    VMESS("VMess", "vmess"),
    TROJAN("Trojan", "trojan")
}
