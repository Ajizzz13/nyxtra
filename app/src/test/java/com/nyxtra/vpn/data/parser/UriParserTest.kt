package com.nyxtra.vpn.data.parser

import com.nyxtra.vpn.data.model.ProtocolType
import com.nyxtra.vpn.data.model.TransportType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * UriParser had no coverage at all, which is how the port-based TLS inference bug
 * survived: ?security=none on :443 silently became a TLS profile.
 *
 * Runs under Robolectric because android.net.Uri / android.util.Base64 are stubs
 * on the plain JVM.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class UriParserTest {

    @Test
    fun vlessExplicitSecurityNoneOnPort443KeepsTlsOff() {
        val uri = "vless://uuid-1234@1.2.3.4:443?security=none&type=ws&path=%2Fws#Test"
        val profile = UriParser.parse(uri)
        assertNotNull(profile)
        assertFalse("security=none must disable TLS even on port 443", profile!!.isTls)
    }

    @Test
    fun vlessExplicitSecurityTlsOnPort80KeepsTlsOn() {
        val uri = "vless://uuid-1234@1.2.3.4:80?security=tls&type=ws#Test"
        val profile = UriParser.parse(uri)!!
        assertTrue(profile.isTls)
    }

    @Test
    fun vlessWithoutSecurityParamFallsBackToPortHeuristic() {
        val onTlsPort = UriParser.parse("vless://uuid@1.2.3.4:443?type=ws#A")!!
        assertTrue(onTlsPort.isTls)
        val onPlainPort = UriParser.parse("vless://uuid@1.2.3.4:8080?type=ws#B")!!
        assertFalse(onPlainPort.isTls)
    }

    @Test
    fun vlessRealityParametersAreCaptured() {
        val uri = "vless://uuid-1234@1.2.3.4:443?security=reality&type=tcp" +
            "&sni=cdn.example.com&pbk=PUBKEY123&sid=abcd&fp=firefox#Reality"
        val profile = UriParser.parse(uri)!!
        assertEquals(ProtocolType.VLESS, profile.protocol)
        assertTrue(profile.isTls)
        assertTrue("pbk must be stored", profile.isReality)
        assertEquals("PUBKEY123", profile.realityPublicKey)
        assertEquals("abcd", profile.realityShortId)
        assertEquals("firefox", profile.realityFingerprint)
    }

    @Test
    fun realityProfileRoundTripsThroughExport() {
        val uri = "vless://uuid-1234@1.2.3.4:443?security=reality&type=tcp" +
            "&sni=cdn.example.com&pbk=PUBKEY123&sid=abcd&fp=firefox#Reality"
        val original = UriParser.parse(uri)!!
        val exported = UriParser.exportToUri(original)
        assertTrue("export must keep security=reality", exported.contains("security=reality"))
        assertTrue(exported.contains("pbk=PUBKEY123"))

        val reparsed = UriParser.parse(exported)!!
        assertEquals(original.realityPublicKey, reparsed.realityPublicKey)
        assertEquals(original.realityShortId, reparsed.realityShortId)
        assertEquals(original.isTls, reparsed.isTls)
    }

    @Test
    fun trojanHonoursExplicitSecurityNone() {
        val plain = UriParser.parse("trojan://pass@1.2.3.4:443?security=none&type=tcp#T")!!
        assertFalse(plain.isTls)

        val secure = UriParser.parse("trojan://pass@1.2.3.4:443?type=tcp#T")!!
        assertTrue("trojan defaults to TLS when unspecified", secure.isTls)
    }

    @Test
    fun vmessPlaintextWithEmptyTlsFieldStaysOff() {
        val json = """{"v":"2","ps":"T","add":"1.2.3.4","port":"443","id":"abc",""" +
            """"net":"ws","path":"/","tls":""}"""
        val encoded = java.util.Base64.getEncoder()
            .encodeToString(json.toByteArray(Charsets.UTF_8))
        val profile = UriParser.parse("vmess://$encoded")!!
        assertFalse("empty tls field must not be overridden by port 443", profile.isTls)
    }

    @Test
    fun vmessUrlSafeBase64WithoutPaddingIsAccepted() {
        val json = """{"v":"2","ps":"URLSafe","add":"1.2.3.4","port":"443",""" +
            """"id":"abc","net":"ws","path":"/","tls":"tls"}"""
        val encoded = java.util.Base64.getUrlEncoder().withoutPadding()
            .encodeToString(json.toByteArray(Charsets.UTF_8))
        assertTrue("URL-safe base64 must decode", encoded.contains("-") || encoded.contains("_") ||
            encoded.isNotEmpty())
        val profile = UriParser.parse("vmess://$encoded")
        assertNotNull("URL-safe vmess link must not be rejected", profile)
        assertEquals("URLSafe", profile!!.name)
        assertTrue(profile.isTls)
    }

    @Test
    fun transportMappingCoversAllVariants() {
        val cases = mapOf(
            "ws" to TransportType.WS,
            "websocket" to TransportType.WS,
            "httpupgrade" to TransportType.HTTP_UPGRADE,
            "http-upgrade" to TransportType.HTTP_UPGRADE,
            "grpc" to TransportType.GRPC,
            "tcp" to TransportType.TCP,
            "kcp" to TransportType.TCP
        )
        cases.forEach { (raw, expected) ->
            val profile = UriParser.parse("vless://uuid@1.2.3.4:443?security=tls&type=$raw#T")!!
            assertEquals("transport for '$raw'", expected, profile.transport)
        }
    }

    @Test
    fun malformedInputReturnsNullInsteadOfThrowing() {
        assertEquals(null, UriParser.parse("not-a-uri"))
        assertEquals(null, UriParser.parse(""))
        assertEquals(null, UriParser.parse("http://example.com"))
    }

    @Test
    fun effectiveHostHeaderNeverEmpty() {
        val profile = UriParser.parse("vless://uuid@1.2.3.4:443?security=tls&type=ws#T")!!
        assertEquals("falls back to server address", "1.2.3.4", profile.effectiveHostHeader)
        val exported = UriParser.exportToUri(profile)
        assertFalse("exported URI must not contain an empty host", exported.contains("host=#"))
    }
}
