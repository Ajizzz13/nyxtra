package com.nyxtra.vpn.data.parser

import android.net.Uri
import android.util.Base64
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.nyxtra.vpn.data.model.ProtocolType
import com.nyxtra.vpn.data.model.TransportType
import com.nyxtra.vpn.data.model.VpnProfile
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object UriParser {

    private val gson = Gson()

    fun parse(rawUri: String): VpnProfile? {
        val trimmed = rawUri.trim()
        return try {
            when {
                trimmed.startsWith("vless://", ignoreCase = true) -> parseVless(trimmed)
                trimmed.startsWith("vmess://", ignoreCase = true) -> parseVmess(trimmed)
                trimmed.startsWith("trojan://", ignoreCase = true) -> parseTrojan(trimmed)
                else -> null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun parseVless(rawUri: String): VpnProfile {
        val uri = Uri.parse(rawUri)
        val userInfo = uri.userInfo ?: uri.authority?.substringBefore("@") ?: ""
        val host = uri.host ?: ""
        val port = if (uri.port != -1) uri.port else 443
        val fragment = uri.fragment?.let { URLDecoder.decode(it, "UTF-8") } ?: "VLESS Profile"

        val typeParam = uri.getQueryParameter("type")?.lowercase() ?: "ws"
        val transport = mapTransport(typeParam)

        val security = uri.getQueryParameter("security")?.lowercase()
        val isTls = security == "tls" || security == "reality" || port == 443

        val sni = uri.getQueryParameter("sni") ?: ""
        val hostHeader = uri.getQueryParameter("host") ?: ""
        val path = uri.getQueryParameter("path")?.let { URLDecoder.decode(it, "UTF-8") } ?: "/"

        return VpnProfile(
            name = fragment,
            protocol = ProtocolType.VLESS,
            serverAddress = host,
            serverPort = port,
            uuidOrPassword = userInfo,
            bugHost = hostHeader,
            sni = sni,
            path = path,
            transport = transport,
            isTls = isTls
        )
    }

    private fun parseTrojan(rawUri: String): VpnProfile {
        val uri = Uri.parse(rawUri)
        val password = uri.userInfo ?: uri.authority?.substringBefore("@") ?: ""
        val host = uri.host ?: ""
        val port = if (uri.port != -1) uri.port else 443
        val fragment = uri.fragment?.let { URLDecoder.decode(it, "UTF-8") } ?: "Trojan Profile"

        val typeParam = uri.getQueryParameter("type")?.lowercase() ?: "tcp"
        val transport = mapTransport(typeParam)

        val sni = uri.getQueryParameter("sni") ?: uri.getQueryParameter("peer") ?: ""
        val hostHeader = uri.getQueryParameter("host") ?: ""
        val path = uri.getQueryParameter("path")?.let { URLDecoder.decode(it, "UTF-8") } ?: "/"

        return VpnProfile(
            name = fragment,
            protocol = ProtocolType.TROJAN,
            serverAddress = host,
            serverPort = port,
            uuidOrPassword = password,
            bugHost = hostHeader,
            sni = sni,
            path = path,
            transport = transport,
            isTls = true
        )
    }

    private fun parseVmess(rawUri: String): VpnProfile {
        val b64 = rawUri.removePrefix("vmess://").removePrefix("VMESS://").trim()
        val decoded = String(Base64.decode(b64, Base64.DEFAULT), StandardCharsets.UTF_8)
        val json = gson.fromJson(decoded, JsonObject::class.java)

        val ps = json.get("ps")?.asString ?: "VMess Profile"
        val add = json.get("add")?.asString ?: ""
        val port = json.get("port")?.asInt ?: 443
        val id = json.get("id")?.asString ?: ""
        val net = json.get("net")?.asString?.lowercase() ?: "ws"
        val host = json.get("host")?.asString ?: ""
        val path = json.get("path")?.asString ?: "/"
        val tls = json.get("tls")?.asString?.lowercase() ?: ""
        val sni = json.get("sni")?.asString ?: ""

        return VpnProfile(
            name = ps,
            protocol = ProtocolType.VMESS,
            serverAddress = add,
            serverPort = port,
            uuidOrPassword = id,
            bugHost = host,
            sni = sni,
            path = path,
            transport = mapTransport(net),
            isTls = tls == "tls" || port == 443
        )
    }

    private fun mapTransport(raw: String): TransportType {
        return when (raw) {
            "ws", "websocket" -> TransportType.WS
            "httpupgrade", "http-upgrade" -> TransportType.HTTP_UPGRADE
            "grpc" -> TransportType.GRPC
            else -> TransportType.TCP
        }
    }

    fun exportToUri(profile: VpnProfile): String {
        val encodedName = URLEncoder.encode(profile.name, "UTF-8")
        return when (profile.protocol) {
            ProtocolType.VLESS -> {
                val sec = if (profile.isTls) "tls" else "none"
                val net = profile.transport.network
                val encodedPath = URLEncoder.encode(profile.path, "UTF-8")
                "vless://${profile.uuidOrPassword}@${profile.serverAddress}:${profile.serverPort}?security=$sec&type=$net&path=$encodedPath&host=${profile.effectiveHostHeader}&sni=${profile.effectiveSni}#$encodedName"
            }
            ProtocolType.TROJAN -> {
                val net = profile.transport.network
                val encodedPath = URLEncoder.encode(profile.path, "UTF-8")
                "trojan://${profile.uuidOrPassword}@${profile.serverAddress}:${profile.serverPort}?security=tls&type=$net&path=$encodedPath&host=${profile.effectiveHostHeader}&sni=${profile.effectiveSni}#$encodedName"
            }
            ProtocolType.VMESS -> {
                val obj = JsonObject().apply {
                    addProperty("v", "2")
                    addProperty("ps", profile.name)
                    addProperty("add", profile.serverAddress)
                    addProperty("port", profile.serverPort)
                    addProperty("id", profile.uuidOrPassword)
                    addProperty("aid", 0)
                    addProperty("scy", "auto")
                    addProperty("net", profile.transport.network)
                    addProperty("type", "none")
                    addProperty("host", profile.effectiveHostHeader)
                    addProperty("path", profile.path)
                    addProperty("tls", if (profile.isTls) "tls" else "")
                    addProperty("sni", profile.effectiveSni)
                }
                val b64 = Base64.encodeToString(obj.toString().toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP)
                "vmess://$b64"
            }
        }
    }
}
