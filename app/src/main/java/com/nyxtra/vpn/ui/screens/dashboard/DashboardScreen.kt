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
import com.nyxtra.vpn.ui.theme.AccentCoral
import com.nyxtra.vpn.ui.theme.AccentCyan
import com.nyxtra.vpn.ui.theme.AccentGreen
import com.nyxtra.vpn.ui.theme.DarkBackground
import com.nyxtra.vpn.ui.theme.DarkBorder
import com.nyxtra.vpn.ui.theme.DarkCard
import com.nyxtra.vpn.ui.theme.DarkSurface
import com.nyxtra.vpn.ui.theme.DarkTab
import com.nyxtra.vpn.ui.theme.TextMuted
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Sleek Top App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .background(DarkSurface)
                    .padding(horizontal = 6.dp, vertical = 6.dp),
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
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Import URI / Add button
                    IconButton(onClick = { profilesViewModel.showImportDialog() }) {
                        Icon(
                            imageVector = Icons.Default.ContentPaste,
                            contentDescription = "Import URI",
                            tint = TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Scan QR button
                    IconButton(onClick = {
                        Toast.makeText(context, "Scan QR: Point camera at config QR code", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Scan QR",
                            tint = TextPrimary,
                            modifier = Modifier.size(20.dp)
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
                            tint = if (isPingingAll) AccentCyan else TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Overflow Menu
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More",
                                tint = TextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(DarkSurface)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Add Profile Manually", color = TextPrimary) },
                                onClick = {
                                    showMenu = false
                                    profilesViewModel.startCreateProfile()
                                    onNavigateToEdit()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Live Logs", color = TextPrimary) },
                                onClick = {
                                    showMenu = false
                                    onNavigateToLogs()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Engine Settings", color = TextPrimary) },
                                onClick = {
                                    showMenu = false
                                    onNavigateToSettings()
                                }
                            )
                        }
                    }
                }
            }

            // Sub-header Group Selector & Traffic
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Red square button with plus [+]
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(AccentCoral)
                        .clickable {
                            profilesViewModel.startCreateProfile()
                            onNavigateToEdit()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                        tint = TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Group Pill: Default
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (selectedGroup == "Default") DarkTab else DarkCard)
                        .border(1.dp, if (selectedGroup == "Default") AccentCyan.copy(alpha = 0.5f) else DarkBorder, RoundedCornerShape(16.dp))
                        .clickable { selectedGroup = "Default" }
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Default",
                        color = if (selectedGroup == "Default") AccentCyan else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Group Pill: Gaming
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (selectedGroup == "Gaming") DarkTab else DarkCard)
                        .border(1.dp, if (selectedGroup == "Gaming") AccentCyan.copy(alpha = 0.5f) else DarkBorder, RoundedCornerShape(16.dp))
                        .clickable { selectedGroup = "Gaming" }
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Gaming",
                        color = if (selectedGroup == "Gaming") AccentCyan else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Real-time speed readout when connected
                if (vpnState == VpnState.CONNECTED) {
                    Text(
                        text = "↓ ${trafficStats.formatDownloadSpeed()}",
                        color = AccentGreen,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Profile Cards List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
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
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }

        // Floating Connection Controller
        FloatingConnectionButton(
            state = vpnState,
            onClick = { dashboardViewModel.toggleConnection() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 24.dp)
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
private fun ImportUriModal(
    onDismiss: () -> Unit,
    onImport: (String) -> Unit
) {
    var rawText by remember { mutableStateOf("") }
    val clipboardManager = LocalClipboardManager.current

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text(
                text = "Import Config",
                color = TextPrimary,
                fontSize = 17.sp,
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
                    placeholder = { Text("vless://...", color = TextMuted, fontSize = 12.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkCard,
                        unfocusedContainerColor = DarkCard,
                        focusedBorderColor = AccentCoral,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        clipboardManager.getText()?.text?.let { rawText = it }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkCard),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Paste from Clipboard", color = TextPrimary, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onImport(rawText) },
                colors = ButtonDefaults.buttonColors(containerColor = AccentCoral)
            ) {
                Text(text = "Import", color = TextPrimary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel", color = TextMuted)
            }
        }
    )
}
