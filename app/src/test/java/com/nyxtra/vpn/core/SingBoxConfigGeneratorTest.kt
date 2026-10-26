package com.nyxtra.vpn.core

import com.google.gson.JsonParser
import com.nyxtra.vpn.data.model.EngineConfig
import com.nyxtra.vpn.data.model.ProtocolType
import com.nyxtra.vpn.data.model.TransportType
import com.nyxtra.vpn.data.model.TunStackMode
import com.nyxtra.vpn.data.model.VpnProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SingBoxConfigGeneratorTest {

    private fun sampleProfile(protocol: ProtocolType = ProtocolType.VLESS): VpnProfile {
        return VpnProfile(
            name = "Test Tunnel",
            protocol = protocol,
            serverAddress = "203.0.113.10",
            serverPort = 443,
            uuidOrPassword = "11111111-2222-3333-4444-555555555555",
            bugHost = "bug.example.com",
            sni = "sni.example.com",
            path = "/nyxtra",
            transport = TransportType.WS,
            isTls = true
        )
    }

    private fun proxyOutbound(json: String) =
        JsonParser.parseString(json).asJsonObject
            .getAsJsonArray("outbounds")[0].asJsonObject

    @Test
    fun vlessOutboundHasRequiredFields() {
        val out = proxyOutbound(SingBoxConfigGenerator.generate(sampleProfile(), EngineConfig()))
        assertEquals("vless", out.get("type").asString)
        assertEquals("proxy", out.get("tag").asString)
        assertEquals("203.0.113.10", out.get("server").asString)
        assertEquals(443, out.get("server_port").asInt)
        assertEquals("11111111-2222-3333-4444-555555555555", out.get("uuid").asString)
        assertEquals("xudp", out.get("packet_encoding").asString)
        assertFalse("empty flow must be omitted", out.has("flow"))
    }

    @Test
    fun vmessOutboundUsesSecurityAuto() {
        val out = proxyOutbound(SingBoxConfigGenerator.generate(sampleProfile(ProtocolType.VMESS), EngineConfig()))
        assertEquals("vmess", out.get("type").asString)
        assertEquals("auto", out.get("security").asString)
        assertEquals(0, out.get("alter_id").asInt)
    }

    @Test
    fun trojanOutboundUsesPassword() {
        val out = proxyOutbound(SingBoxConfigGenerator.generate(sampleProfile(ProtocolType.TROJAN), EngineConfig()))
        assertEquals("trojan", out.get("type").asString)
        assertEquals("11111111-2222-3333-4444-555555555555", out.get("password").asString)
        assertFalse(out.has("uuid"))
    }

    @Test
    fun dnsHasProxyDetourAndDirectResolver() {
        val dns = JsonParser.parseString(SingBoxConfigGenerator.generate(sampleProfile(), EngineConfig()))
            .asJsonObject.getAsJsonObject("dns")
        val servers = dns.getAsJsonArray("servers")
        assertEquals("remote-dns", dns.get("final").asString)
        assertEquals("proxy", servers[0].asJsonObject.get("detour").asString)
        assertFalse("direct-dns must not target empty direct outbound", servers[1].asJsonObject.has("detour"))
    }

    @Test
    fun gameModeStillSniffsButOnlyDns() {
        // Regression guard for the removed inbound.sniff field: hijack-dns matches on
        // the sniffed protocol, so the sniff action must always be present.
        val route = JsonParser.parseString(
            SingBoxConfigGenerator.generate(sampleProfile(), EngineConfig(zeroRoutingSniffing = true))
        ).asJsonObject.getAsJsonObject("route")
        val rules = route.getAsJsonArray("rules").map { it.asJsonObject }
        val actions = rules.map { it.get("action").asString }
        assertTrue("sniff action must always be present", actions.contains("sniff"))
        assertTrue(actions.contains("hijack-dns"))
        assertEquals("proxy", route.get("final").asString)

        val sniffRule = rules.first { it.get("action").asString == "sniff" }
        val sniffers = sniffRule.getAsJsonArray("sniffer").map { it.asString }
        assertEquals("game mode must sniff DNS only", listOf("dns"), sniffers)
    }

    @Test
    fun fullModeSniffsAllProtocols() {
        val route = JsonParser.parseString(
            SingBoxConfigGenerator.generate(sampleProfile(), EngineConfig(zeroRoutingSniffing = false))
        ).asJsonObject.getAsJsonObject("route")
        val rules = route.getAsJsonArray("rules").map { it.asJsonObject }
        assertTrue(rules.map { it.get("action").asString }.contains("sniff"))

        val sniffRule = rules.first { it.get("action").asString == "sniff" }
        assertFalse(
            "full mode must not restrict the sniffer list",
            sniffRule.has("sniffer")
        )
    }

    @Test
    fun tunInboundHasNoLegacySniffField() {
        // inbound.sniff was removed in sing-box 1.13.0 and makes the whole config
        // fail to initialise, so it must never be emitted again.
        for (zeroRouting in listOf(true, false)) {
            val tun = JsonParser.parseString(
                SingBoxConfigGenerator.generate(
                    sampleProfile(), EngineConfig(zeroRoutingSniffing = zeroRouting)
                )
            ).asJsonObject.getAsJsonArray("inbounds")[0].asJsonObject
            assertFalse("legacy inbound.sniff must be absent", tun.has("sniff"))
        }
    }

    @Test
    fun whitelistPackagesUseInclude() {
        val tun = JsonParser.parseString(
            SingBoxConfigGenerator.generate(
                sampleProfile(), EngineConfig(),
                perAppPackages = listOf("com.example.game"), isPerAppWhitelist = true
            )
        ).asJsonObject.getAsJsonArray("inbounds")[0].asJsonObject
        assertTrue(tun.has("include_package"))
        assertFalse(tun.has("exclude_package"))
    }

    @Test
    fun blacklistPackagesUseExclude() {
        val tun = JsonParser.parseString(
            SingBoxConfigGenerator.generate(
                sampleProfile(), EngineConfig(),
                perAppPackages = listOf("com.example.app"), isPerAppWhitelist = false
            )
        ).asJsonObject.getAsJsonArray("inbounds")[0].asJsonObject
        assertTrue(tun.has("exclude_package"))
        assertFalse(tun.has("include_package"))
    }

    @Test
    fun tunInboundHasDualStackAddressAndEngineMtu() {
        val tun = JsonParser.parseString(
            SingBoxConfigGenerator.generate(sampleProfile(), EngineConfig(mtu = 1300, tunStack = TunStackMode.SYSTEM))
        ).asJsonObject.getAsJsonArray("inbounds")[0].asJsonObject
        assertEquals(1300, tun.get("mtu").asInt)
        assertEquals("system", tun.get("stack").asString)
        val addrs = tun.getAsJsonArray("address").map { it.asString }
        assertTrue(addrs.any { it.startsWith("172.19.") })
        assertTrue(addrs.any { it.startsWith("fdfe:") })
    }

    @Test
    fun keepaliveIntervalPresentWhenTcpTuningOn() {
        val out = proxyOutbound(SingBoxConfigGenerator.generate(sampleProfile(), EngineConfig(tcpNoDelay = true, keepaliveIntervalSeconds = 15)))
        assertEquals("15s", out.get("tcp_keep_alive_interval").asString)
        assertEquals(true, out.get("tcp_fast_open").asBoolean)
    }

    @Test
    fun keepaliveAbsentWhenTcpTuningOff() {
        val out = proxyOutbound(SingBoxConfigGenerator.generate(sampleProfile(), EngineConfig(tcpNoDelay = false)))
        assertFalse(out.has("tcp_keep_alive_interval"))
        assertFalse(out.has("tcp_fast_open"))
    }
}
