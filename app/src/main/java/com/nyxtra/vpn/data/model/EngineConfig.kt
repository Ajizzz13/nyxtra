package com.nyxtra.vpn.data.model

enum class TunStackMode(val displayName: String, val tag: String) {
    SYSTEM("System Linux Kernel Direct", "system"),
    MIXED("Mixed Kernel and Userspace Fallback", "mixed"),
    GVISOR("gVisor Userspace Netstack", "gvisor")
}

data class EngineConfig(
    val mtu: Int = 1280,
    val directFdHandover: Boolean = true,
    val tunStack: TunStackMode = TunStackMode.MIXED,
    val zeroRoutingSniffing: Boolean = true,
    val tcpNoDelay: Boolean = true,
    val keepaliveIntervalSeconds: Int = 15,
    val perAppEnabled: Boolean = false,
    val perAppWhitelistMode: Boolean = true,
    val selectedPackages: Set<String> = emptySet()
)
