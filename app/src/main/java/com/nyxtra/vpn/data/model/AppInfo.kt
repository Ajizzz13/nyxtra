package com.nyxtra.vpn.data.model

data class AppInfo(
    val packageName: String,
    val appName: String,
    val isSystemApp: Boolean = false,
    val isGame: Boolean = false,
    val isSelected: Boolean = false
)
