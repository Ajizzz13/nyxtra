package com.nyxtra.vpn.ui.screens.dashboard

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.nyxtra.vpn.core.NyxtraVpnController
import com.nyxtra.vpn.data.model.VpnState
import com.nyxtra.vpn.ui.components.FloatingConnectionButton
import com.nyxtra.vpn.ui.components.TunnelProfileCard
import com.nyxtra.vpn.ui.screens.profiles.ProfilesViewModel
import com.nyxtra.vpn.ui.theme.NyxtraDark
import com.nyxtra.vpn.ui.theme.NyxtraDivider
import com.nyxtra.vpn.ui.theme.NyxtraSurface
import com.nyxtra.vpn.ui.theme.NyxtraTeal
import com.nyxtra.vpn.ui.theme.TextGray
import com.nyxtra.vpn.ui.theme.TextWhite

@OptIn(ExperimentalMaterial3Api::class)
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
    val lastError by dashboardViewModel.lastError.collectAsState()
    val profiles by profilesViewModel.filteredProfiles.collectAsState()
    val isPingingAll by profilesViewModel.isPingingAll.collectAsState()
    val importDialogVisible by profilesViewModel.importDialogVisible.collectAsState()

    var showMenu by remember { mutableStateOf(false) }
    var notifAsked by remember { mutableStateOf(false) }

    val selectedProfile = profiles.firstOrNull { it.isSelected } ?: profiles.firstOrNull()
    val isTunnelActive = vpnState == VpnState.CONNECTED || vpnState == VpnState.CONNECTING

    fun stopTunnelForProfileChange(reason: String) {
        if (isTunnelActive) {
            NyxtraVpnController.stopVpn(context)
            Toast.makeText(context, reason, Toast.LENGTH_SHORT).show()
        }
    }

    val vpnPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            dashboardViewModel.onPermissionGranted(context)
        }
    }

    val scanLauncher = rememberLauncherForActivityResult(contract = ScanContract()) { result ->
        val contents = result.contents
        if (!contents.isNullOrBlank()) {
            val ok = profilesViewModel.importUri(contents)
            if (ok) {
                Toast.makeText(context, "Profile imported", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Invalid URI format", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun launchQrScanner() {
        scanLauncher.launch(
            ScanOptions()
                .setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                .setPrompt("Point camera at config QR code")
                .setBeepEnabled(false)
                .setBarcodeImageEnabled(false)
        )
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            launchQrScanner()
        } else {
            Toast.makeText(context, "Camera permission needed for QR scan", Toast.LENGTH_SHORT).show()
        }
    }

    val notifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !notifAsked) {
            notifAsked = true
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    Scaffold(
        containerColor = NyxtraDark,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Nyxtra",
                        color = TextWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Drawer",
                            tint = TextWhite
                        )
                    }
                },
                actions = {
                    // Ping all
                    IconButton(
                        onClick = { profilesViewModel.pingAll() },
                        enabled = !isPingingAll
                    ) {
                        Icon(
                            imageVector = Icons.Default.NetworkCheck,
                            contentDescription = "Ping All",
                            tint = if (isPingingAll) NyxtraTeal else TextWhite
                        )
                    }

                    // Scan QR
                    IconButton(onClick = {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            launchQrScanner()
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Scan QR",
                            tint = TextWhite
                        )
                    }

                    // Add / Import
                    IconButton(onClick = { profilesViewModel.showImportDialog() }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Import",
                            tint = TextWhite
                        )
                    }

                    // 3-dots Menu
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More",
                                tint = TextWhite
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Add Manually") },
                                onClick = {
                                    showMenu = false
                                    profilesViewModel.startCreateProfile()
                                    onNavigateToEdit()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Live Logs") },
                                onClick = {
                                    showMenu = false
                                    onNavigateToLogs()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Settings") },
                                onClick = {
                                    showMenu = false
                                    onNavigateToSettings()
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NyxtraSurface
                )
            )
        },
        floatingActionButton = {
            FloatingConnectionButton(
                state = vpnState,
                onClick = {
                    dashboardViewModel.toggleConnection(context) { intent ->
                        vpnPermissionLauncher.launch(intent)
                    }
                }
            )
        },
        bottomBar = {
            if (vpnState == VpnState.CONNECTED || vpnState == VpnState.CONNECTING) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NyxtraSurface)
                ) {
                    Divider(color = NyxtraDivider)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (vpnState == VpnState.CONNECTED) {
                                selectedProfile?.name ?: "Connected"
                            } else {
                                "Connecting..."
                            },
                            color = if (vpnState == VpnState.CONNECTED) NyxtraTeal else TextGray,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        if (vpnState == VpnState.CONNECTED) {
                            Text(
                                text = "↓ ${trafficStats.formatDownloadSpeed()}  ↑ ${trafficStats.formatUploadSpeed()}",
                                color = TextWhite,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Connection error banner with failure reason
            if (vpnState == VpnState.ERROR) {
                Card(
                    shape = RoundedCornerShape(4.dp),
                    colors = CardDefaults.cardColors(containerColor = NyxtraSurface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = lastError ?: "Connection failed",
                            color = TextWhite,
                            fontSize = 13.sp,
                            maxLines = 2,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = onNavigateToLogs) {
                            Text("Logs", color = NyxtraTeal, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Profile List directly without tabs
            if (profiles.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No Profiles",
                            color = TextGray,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap + to add or import a config",
                            color = TextGray,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { profilesViewModel.showImportDialog() },
                            colors = ButtonDefaults.buttonColors(containerColor = NyxtraTeal)
                        ) {
                            Text("Import Config", color = Color.White)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(profiles, key = { it.id }) { profile ->
                        TunnelProfileCard(
                            profile = profile,
                            onSelect = {
                                if (profile.id != selectedProfile?.id) {
                                    stopTunnelForProfileChange("Tunnel stopped, tap Connect to apply")
                                }
                                profilesViewModel.selectProfile(profile.id)
                            },
                            onPing = { profilesViewModel.pingProfile(profile.id) },
                            onEdit = {
                                profilesViewModel.startEditProfile(profile)
                                onNavigateToEdit()
                            },
                            onDelete = {
                                if (profile.id == selectedProfile?.id) {
                                    stopTunnelForProfileChange("Tunnel stopped, profile deleted")
                                }
                                profilesViewModel.deleteProfile(profile.id)
                            },
                            onShare = {
                                val uri = profilesViewModel.exportUri(profile)
                                clipboardManager.setText(AnnotatedString(uri))
                                Toast.makeText(context, "URI copied", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }

    // Import Dialog
    if (importDialogVisible) {
        ImportUriModal(
            onDismiss = { profilesViewModel.hideImportDialog() },
            onImport = { rawUri ->
                val ok = profilesViewModel.importUri(rawUri)
                if (ok) {
                    Toast.makeText(context, "Profile imported", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Invalid URI format", Toast.LENGTH_SHORT).show()
                }
            }
        )
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
        title = { Text("Import Config", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    text = "Paste vless, vmess, or trojan URI:",
                    color = TextGray,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = rawText,
                    onValueChange = { rawText = it },
                    placeholder = { Text("vless...", color = TextGray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        clipboardManager.getText()?.text?.let { rawText = it }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NyxtraSurface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Paste from Clipboard", color = TextWhite)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onImport(rawText) },
                colors = ButtonDefaults.buttonColors(containerColor = NyxtraTeal)
            ) {
                Text("Import", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextGray)
            }
        }
    )
}
