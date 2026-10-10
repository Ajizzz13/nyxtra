package com.nyxtra.vpn.core

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.nyxtra.vpn.data.model.EngineConfig
import com.nyxtra.vpn.data.model.ProtocolType
import com.nyxtra.vpn.data.model.TransportType
import com.nyxtra.vpn.data.model.VpnProfile

object SingBoxConfigGenerator {

    private val gson = GsonBuilder().setPrettyPrinting().create()

    fun generate(
        profile: VpnProfile,
        engineConfig: EngineConfig,
        perAppPackages: List<String> = emptyList(),
        isPerAppWhitelist: Boolean = true
    ): String {
        val root = JsonObject()

        // 1. Log configuration
        val logObj = JsonObject().apply {
            addProperty("level", "info")
            addProperty("timestamp", true)
        }
        root.add("log", logObj)

        // 2. DNS configuration (sing-box 1.14.0+ schema)
        val dnsObj = JsonObject().apply {
            val servers = JsonArray().apply {
                // Remote DNS queried through proxy tunnel
                add(JsonObject().apply {
                    addProperty("type", "tcp")
                    addProperty("tag", "remote-dns")
                    addProperty("server", "1.1.1.1")
                    addProperty("detour", "proxy")
                })
                // Direct DNS for outbound bootstrap resolving
                add(JsonObject().apply {
                    addProperty("type", "udp")
                    addProperty("tag", "direct-dns")
                    addProperty("server", "1.1.1.1")
                })
            }
            add("servers", servers)
            addProperty("final", "remote-dns")
            addProperty("strategy", "prefer_ipv4")
        }
        root.add("dns", dnsObj)

        // 3. Inbound TUN configuration (sing-box 1.14.0+ schema)
        val inbounds = JsonArray().apply {
            val tunInbound = JsonObject().apply {
                addProperty("type", "tun")
                addProperty("tag", "tun-in")
                addProperty("interface_name", "nyxtra0")

                val addrArray = JsonArray().apply {
                    add("172.19.0.1/30")
                }
                add("address", addrArray)
                addProperty("mtu", engineConfig.mtu)
                addProperty("auto_route", true)
                addProperty("strict_route", false)
                addProperty("stack", engineConfig.tunStack.tag)
                addProperty("sniff", !engineConfig.zeroRoutingSniffing)

                if (perAppPackages.isNotEmpty()) {
                    val packageArray = JsonArray().apply {
                        perAppPackages.forEach { add(it) }
                    }
                    if (isPerAppWhitelist) {
                        add("include_package", packageArray)
                    } else {
                        add("exclude_package", packageArray)
                    }
                }
            }
            add(tunInbound)
        }
        root.add("inbounds", inbounds)

        // 4. Outbound configuration
        val outbounds = JsonArray().apply {
            val proxyOutbound = buildOutbound(profile, engineConfig)
            add(proxyOutbound)

            // Direct outbound
            add(JsonObject().apply {
                addProperty("type", "direct")
                addProperty("tag", "direct")
            })
        }
        root.add("outbounds", outbounds)

        // 5. Route configuration (sing-box 1.14.0+ schema with hijack-dns action)
        val routeObj = JsonObject().apply {
            addProperty("auto_detect_interface", true)
            addProperty("default_domain_resolver", "direct-dns")
            val rules = JsonArray().apply {
                add(JsonObject().apply {
                    addProperty("action", "sniff")
                })
                add(JsonObject().apply {
                    addProperty("protocol", "dns")
                    addProperty("action", "hijack-dns")
                })
            }
            add("rules", rules)
            addProperty("final", "proxy")
        }
        root.add("route", routeObj)

        return gson.toJson(root)
    }

    private fun buildOutbound(profile: VpnProfile, engineConfig: EngineConfig): JsonObject {
        val out = JsonObject()
        val protoTag = profile.protocol.name.lowercase()
        out.addProperty("type", protoTag)
        out.addProperty("tag", "proxy")
        out.addProperty("server", profile.serverAddress)
        out.addProperty("server_port", profile.serverPort)
        out.addProperty("domain_resolver", "direct-dns")

        when (profile.protocol) {
            ProtocolType.VLESS -> {
                out.addProperty("uuid", profile.uuidOrPassword)
                out.addProperty("flow", "")
                out.addProperty("packet_encoding", "xudp")
            }
            ProtocolType.VMESS -> {
                out.addProperty("uuid", profile.uuidOrPassword)
                out.addProperty("security", "auto")
                out.addProperty("alter_id", 0)
                out.addProperty("packet_encoding", "xudp")
            }
            ProtocolType.TROJAN -> {
                out.addProperty("password", profile.uuidOrPassword)
            }
        }

        // TLS
        if (profile.isTls) {
            val tlsObj = JsonObject().apply {
                addProperty("enabled", true)
                val sniHost = if (profile.sni.isNotBlank()) profile.sni else profile.serverAddress
                addProperty("server_name", sniHost)
                addProperty("insecure", profile.allowInsecure)
            }
            out.add("tls", tlsObj)
        }

        // Transport
        val hostHeader = if (profile.bugHost.isNotBlank()) {
            profile.bugHost
        } else if (profile.sni.isNotBlank()) {
            profile.sni
        } else {
            profile.serverAddress
        }

        when (profile.transport) {
            TransportType.WS -> {
                val wsObj = JsonObject().apply {
                    addProperty("type", "ws")
                    addProperty("path", if (profile.path.isNotBlank()) profile.path else "/")
                    val headers = JsonObject().apply {
                        addProperty("Host", hostHeader)
                    }
                    add("headers", headers)
                }
                out.add("transport", wsObj)
            }
            TransportType.HTTP_UPGRADE -> {
                val httpUpgradeObj = JsonObject().apply {
                    addProperty("type", "httpupgrade")
                    addProperty("path", if (profile.path.isNotBlank()) profile.path else "/")
                    addProperty("host", hostHeader)
                }
                out.add("transport", httpUpgradeObj)
            }
            TransportType.TCP -> {
                // Raw TCP transport
            }
            TransportType.GRPC -> {
                val grpcObj = JsonObject().apply {
                    addProperty("type", "grpc")
                    addProperty("service_name", if (profile.path.isNotBlank()) profile.path.trimStart('/') else "nyxtra-grpc")
                }
                out.add("transport", grpcObj)
            }
        }

        // Low latency gaming tuning
        if (engineConfig.tcpNoDelay) {
            out.addProperty("tcp_fast_open", true)
            out.addProperty("tcp_multi_path", false)
        }

        return out
    }
}
