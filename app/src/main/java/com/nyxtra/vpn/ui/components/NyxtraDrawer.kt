package com.nyxtra.vpn.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nyxtra.vpn.ui.navigation.Screen
import com.nyxtra.vpn.ui.theme.BorderSubtle
import com.nyxtra.vpn.ui.theme.CardSelectedBg
import com.nyxtra.vpn.ui.theme.NyxtraAccent
import com.nyxtra.vpn.ui.theme.NyxtraAccentSubtle
import com.nyxtra.vpn.ui.theme.SurfaceBg
import com.nyxtra.vpn.ui.theme.TextMuted
import com.nyxtra.vpn.ui.theme.TextPrimary
import com.nyxtra.vpn.ui.theme.TextSecondary

@Composable
fun NyxtraDrawer(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(
        modifier = modifier
            .width(280.dp)
            .fillMaxHeight(),
        drawerContainerColor = SurfaceBg
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .statusBarsPadding()
                .padding(vertical = 20.dp)
        ) {
            // Header: Clean workspace identity
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Nyxtra",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "LOW-LATENCY NETWORK ENGINE",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.8.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Divider(color = BorderSubtle, thickness = 1.dp, modifier = Modifier.padding(horizontal = 20.dp))

            Spacer(modifier = Modifier.height(16.dp))

            // Navigation Items
            DrawerItem(
                icon = Icons.Default.Dns,
                label = "Tunnels & Profiles",
                isSelected = currentRoute == Screen.Dashboard.route,
                onClick = { onNavigate(Screen.Dashboard.route) }
            )

            DrawerItem(
                icon = Icons.Default.FilterAlt,
                label = "Per-App Proxy",
                isSelected = currentRoute == Screen.PerApp.route,
                onClick = { onNavigate(Screen.PerApp.route) }
            )

            DrawerItem(
                icon = Icons.Default.ListAlt,
                label = "Live Logs",
                isSelected = currentRoute == Screen.Logs.route,
                onClick = { onNavigate(Screen.Logs.route) }
            )

            DrawerItem(
                icon = Icons.Default.Tune,
                label = "Engine Settings",
                isSelected = currentRoute == Screen.Settings.route,
                onClick = { onNavigate(Screen.Settings.route) }
            )

            Spacer(modifier = Modifier.weight(1f))

            Divider(color = BorderSubtle, thickness = 1.dp, modifier = Modifier.padding(horizontal = 20.dp))

            Spacer(modifier = Modifier.height(14.dp))

            // Footer / System Specs Metadata
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SING-BOX CORE 1.10.x",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Linux Direct FD • TCP_NODELAY",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun DrawerItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (isSelected) NyxtraAccentSubtle else SurfaceBg
    val textCol = if (isSelected) NyxtraAccent else TextSecondary
    val iconCol = if (isSelected) NyxtraAccent else TextMuted
    val borderMod = if (isSelected) Modifier.border(1.dp, NyxtraAccent, RoundedCornerShape(6.dp)) else Modifier

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .then(borderMod)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconCol,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                color = textCol,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                letterSpacing = (-0.1).sp
            )
        }
    }
}
