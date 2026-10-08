package com.nyxtra.vpn.data.model

enum class TransportType(val displayName: String, val network: String) {
    TCP("TCP Direct", "tcp"),
    WS("WebSocket", "ws"),
    HTTP_UPGRADE("HTTPUpgrade", "httpupgrade"),
    GRPC("gRPC", "grpc")
}
