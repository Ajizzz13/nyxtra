package com.nyxtra.vpn.ui.screens.dashboard

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nyxtra.vpn.data.model.VpnState
import com.nyxtra.vpn.ui.components.FloatingConnectionButton
import com.nyxtra.vpn.ui.components.TunnelProfileCard
import com.nyxtra.vpn.ui.screens.profiles.ProfilesViewModel
import com.nyxtra.vpn.ui.theme.NyxtraAccent
import com.nyxtra.vpn.ui.theme.NyxtraAccentSubtle
import com.nyxtra.vpn.ui.theme.NyxtraBorder
import com.nyxtra.vpn.ui.theme.NyxtraCard
import com.nyxtra.vpn.ui.theme.NyxtraDark
import com.nyxtra.vpn.ui.theme.NyxtraSurface
import com.nyxtra.vpn.ui.theme.PastelOrange
import com.nyxtra.vpn.ui.theme.TextMuted
import com.nyxtra.vpn.ui.theme.TextOnAccent
import com.nyxtra.vpn.ui.theme.TextPrimary
import com.nyxtra.vpn.ui.theme.TextSecondary

@Composable
fun DashboardScreen(
    dashboardViewModel: DashboardViewModel,
    profilesViewModel: ProfilesViewModel,
    onOpenDrawer: () -> Unit,
    onNavigateToEdit: () -> Unit,
    onNavigateToLogs: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val vpnState by dashboardViewModel.vpnState.collectAsState()
    val trafficStats by dashboardViewModel.trafficStats.collectAsState()
    val profiles by profilesViewModel.filteredProfiles.collectAsState()
    val isPingingAll by profilesViewModel.isPingingAll.collectAsState()
    val importDialogVisible by profilesViewModel.importDialogVisible.collectAsState()

    var showMenu by remember { mutableStateOf(false) }
    var selectedGroup by remember { mutableStateOf("Default") }

    val selectedProfile = profiles.firstOrNull { it.isSelected } ?: profiles.firstOrNull()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NyxtraDark)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .background(NyxtraSurface)
                    .border(1.dp, NyxtraBorder)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Drawer",
                            tint = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "Nyxtra",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Import URI button
                    IconButton(onClick = { profilesViewModel.showImportDialog() }) {
                        Icon(
                            imageVector = Icons.Default.ContentPaste,
                            contentDescription = "Import URI",
                            tint = TextSecondary,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    // Scan QR button
                    IconButton(onClick = {
                        Toast.makeText(context, "Scan QR: Point camera at config QR code", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Scan QR",
                            tint = TextSecondary,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    // Ping all
                    IconButton(
                        onClick = { profilesViewModel.pingAll() },
                        enabled = !isPingingAll
                    ) {
                        Icon(
                            imageVector = Icons.Default.NetworkCheck,
                            contentDescription = "Ping All",
                            tint = if (isPingingAll) NyxtraAccent else TextSecondary,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    // Overflow Menu
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More",
                                tint = TextSecondary,
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier
                                .background(NyxtraSurface)
                                .border(1.dp, NyxtraBorder, RoundedCornerShape(6.dp))
                        ) {
                            DropdownMenuItem(
                                text = { Text("Add Profile Manually", color = TextPrimary, fontSize = 13.sp) },
                                onClick = {
                                    showMenu = false
                                    profilesViewModel.startCreateProfile()
                                    onNavigateToEdit()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Live Logs", color = TextPrimary, fontSize = 13.sp) },
                                onClick = {
                                    showMenu = false
                                    onNavigateToLogs()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Engine Settings", color = TextPrimary, fontSize = 13.sp) },
                                onClick = {
                                    showMenu = false
                                    onNavigateToSettings()
                                }
                            )
                        }
                    }
                }
            }

            // 2D Solid Connection Status Banner
            if (vpnState == VpnState.CONNECTED || vpnState == VpnState.CONNECTING) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(NyxtraCard)
                        .border(1.dp, if (vpnState == VpnState.CONNECTED) NyxtraAccent else NyxtraBorder, RoundedCornerShape(6.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (vpnState == VpnState.CONNECTED) NyxtraAccent else PastelOrange)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (vpnState == VpnState.CONNECTED) "CONNECTED: ${selectedProfile?.name ?: "Nyxtra Core"}" else "CONNECTING...",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                    }

                    if (vpnState == VpnState.CONNECTED) {
                        Text(
                            text = "↓ ${trafficStats.formatDownloadSpeed()}  ↑ ${trafficStats.formatUploadSpeed()}",
                            color = NyxtraAccent,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Group Selector & Add Action
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quick create button
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(NyxtraCard)
                        .border(1.dp, NyxtraBorder, RoundedCornerShape(6.dp))
                        .clickable {
                            profilesViewModel.startCreateProfile()
                            onNavigateToEdit()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Profile",
                        tint = TextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Group Tab: DEFAULT
                GroupTab(
                    label = "DEFAULT",
                    count = profiles.size,
                    isSelected = selectedGroup == "Default",
                    onClick = { selectedGroup = "Default" }
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Group Tab: GAMING
                GroupTab(
                    label = "GAMING",
                    count = null,
                    isSelected = selectedGroup == "Gaming",
                    onClick = { selectedGroup = "Gaming" }
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "${profiles.size} NODES",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Profile Cards Feed or Empty State
            if (profiles.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(NyxtraCard)
                        .border(1.dp, NyxtraBorder, RoundedCornerShape(6.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "NO TUNNEL PROFILES",
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Import a configuration URI to start VPN tunneling",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { profilesViewModel.showImportDialog() },
                            colors = ButtonDefaults.buttonColors(containerColor = NyxtraAccent),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(text = "Import URI", color = TextOnAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(profiles, key = { it.id }) { profile ->
                        TunnelProfileCard(
                            profile = profile,
                            onSelect = { profilesViewModel.selectProfile(profile.id) },
                            onPing = { profilesViewModel.pingProfile(profile.id) },
                            onEdit = {
                                profilesViewModel.startEditProfile(profile)
                                onNavigateToEdit()
                            },
                            onDelete = { profilesViewModel.deleteProfile(profile.id) },
                            onShare = {
                                val uri = profilesViewModel.exportUri(profile)
                                clipboardManager.setText(AnnotatedString(uri))
                                Toast.makeText(context, "URI copied to clipboard", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(90.dp))
                    }
                }
            }
        }

        // 2D Solid Floating Action Button (NekoBox Style)
        FloatingConnectionButton(
            state = vpnState,
            onClick = { dashboardViewModel.toggleConnection() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 20.dp)
        )

        // Import URI Dialog
        if (importDialogVisible) {
            ImportUriModal(
                onDismiss = { profilesViewModel.hideImportDialog() },
                onImport = { rawUri ->
                    val ok = profilesViewModel.importUri(rawUri)
                    if (ok) {
                        Toast.makeText(context, "Profile imported!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Invalid URI (vless, vmess, trojan)", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }
}

@Composable
private fun GroupTab(
    label: String,
    count: Int?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (isSelected) NyxtraAccentSubtle else NyxtraCard
    val borderCol = if (isSelected) NyxtraAccent else NyxtraBorder
    val textCol = if (isSelected) NyxtraAccent else TextMuted

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .border(1.dp, borderCol, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                color = textCol,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            if (count != null) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$count",
                    color = textCol.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun ImportUriModal(
    onDismiss: () -> Unit,
    onImport: (String) -> Unit
) {
    var rawText by remember { mutableStateOf("") }
    val clipboardManager = LocalClipboardManager.current

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NyxtraSurface,
        shape = RoundedCornerShape(8.dp),
        title = {
            Text(
                text = "Import Configuration",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "Paste vless://, vmess://, or trojan:// URI:",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                OutlinedTextField(
                    value = rawText,
                    onValueChange = { rawText = it },
                    placeholder = { Text("vless://...", color = TextMuted, fontSize = 12.sp, fontFamily = FontFamily.Monospace) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = NyxtraCard,
                        unfocusedContainerColor = NyxtraCard,
                        focusedBorderColor = NyxtraAccent,
                        unfocusedBorderColor = NyxtraBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        clipboardManager.getText()?.text?.let { rawText = it }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NyxtraCard),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NyxtraBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Paste from Clipboard", color = TextPrimary, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onImport(rawText) },
                colors = ButtonDefaults.buttonColors(containerColor = NyxtraAccent),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(text = "Import", color = TextOnAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel", color = TextMuted, fontSize = 12.sp)
            }
        }
    )
}
