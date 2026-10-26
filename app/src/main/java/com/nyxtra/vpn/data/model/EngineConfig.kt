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
    // QUIC (HTTP/3 over UDP 443) is unreliable when carried through a TCP-based
    // proxy: small requests complete but media segments stall, which looks like
    // "web pages load, video and music will not play". Rejecting it makes clients
    // fall back to plain TCP, which streams reliably.
    val blockQuic: Boolean = true,
    val perAppEnabled: Boolean = false,
    val perAppWhitelistMode: Boolean = true,
    val selectedPackages: Set<String> = emptySet()
)
