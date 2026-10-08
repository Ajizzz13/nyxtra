package com.nyxtra.vpn.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Tune
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Dashboard : Screen("dashboard", "Tunnels & Profiles", Icons.Default.Dns)
    object ProfileEdit : Screen("profile_edit", "Edit Profile")
    object PerApp : Screen("per_app", "Per-App Proxy", Icons.Default.FilterAlt)
    object Logs : Screen("logs", "Live Logs", Icons.Default.ListAlt)
    object Settings : Screen("settings", "Engine Settings", Icons.Default.Tune)
}
