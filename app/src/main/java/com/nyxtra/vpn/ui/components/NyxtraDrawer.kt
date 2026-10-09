package com.nyxtra.vpn.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nyxtra.vpn.ui.navigation.Screen
import com.nyxtra.vpn.ui.theme.NyxtraDivider
import com.nyxtra.vpn.ui.theme.NyxtraSurface
import com.nyxtra.vpn.ui.theme.NyxtraTeal
import com.nyxtra.vpn.ui.theme.TextGray
import com.nyxtra.vpn.ui.theme.TextWhite

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
        drawerContainerColor = NyxtraSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .statusBarsPadding()
                .padding(vertical = 16.dp)
        ) {
            Text(
                text = "Nyxtra",
                color = TextWhite,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = NyxtraDivider)
            Spacer(modifier = Modifier.height(12.dp))

            DrawerMenuItem(
                icon = Icons.Default.Dns,
                label = "Profiles",
                selected = currentRoute == Screen.Dashboard.route,
                onClick = { onNavigate(Screen.Dashboard.route) }
            )

            DrawerMenuItem(
                icon = Icons.Default.FilterAlt,
                label = "Per-App Proxy",
                selected = currentRoute == Screen.PerApp.route,
                onClick = { onNavigate(Screen.PerApp.route) }
            )

            DrawerMenuItem(
                icon = Icons.Default.ListAlt,
                label = "Logs",
                selected = currentRoute == Screen.Logs.route,
                onClick = { onNavigate(Screen.Logs.route) }
            )

            DrawerMenuItem(
                icon = Icons.Default.Tune,
                label = "Settings",
                selected = currentRoute == Screen.Settings.route,
                onClick = { onNavigate(Screen.Settings.route) }
            )

            Spacer(modifier = Modifier.weight(1f))
            Divider(color = NyxtraDivider)
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Nyxtra Core v1.0",
                color = TextGray,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }
    }
}

@Composable
private fun DrawerMenuItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        icon = { Icon(icon, contentDescription = label) },
        label = { Text(label, fontSize = 14.sp) },
        selected = selected,
        onClick = onClick,
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = NyxtraTeal.copy(alpha = 0.15f),
            selectedIconColor = NyxtraTeal,
            selectedTextColor = NyxtraTeal,
            unselectedContainerColor = NyxtraSurface,
            unselectedIconColor = TextGray,
            unselectedTextColor = TextWhite
        ),
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
    )
}
