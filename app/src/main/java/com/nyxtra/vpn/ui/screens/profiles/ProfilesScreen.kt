package com.nyxtra.vpn.ui.screens.profiles

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
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
import com.nyxtra.vpn.ui.components.CommonTopBar
import com.nyxtra.vpn.ui.components.ProfileCard
import com.nyxtra.vpn.ui.theme.NyxtraBackground
import com.nyxtra.vpn.ui.theme.NyxtraCard
import com.nyxtra.vpn.ui.theme.NyxtraCardBorder
import com.nyxtra.vpn.ui.theme.NyxtraCyberBlue
import com.nyxtra.vpn.ui.theme.NyxtraNeonGreen
import com.nyxtra.vpn.ui.theme.NyxtraSurface
import com.nyxtra.vpn.ui.theme.NyxtraSurfaceVariant
import com.nyxtra.vpn.ui.theme.TextMuted
import com.nyxtra.vpn.ui.theme.TextOnAccent
import com.nyxtra.vpn.ui.theme.TextPrimary
import com.nyxtra.vpn.ui.theme.TextSecondary

@Composable
fun ProfilesScreen(
    viewModel: ProfilesViewModel,
    onNavigateToEdit: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val profiles by viewModel.filteredProfiles.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val importDialogVisible by viewModel.importDialogVisible.collectAsState()
    val isPingingAll by viewModel.isPingingAll.collectAsState()

    var showActionMenu by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NyxtraBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            CommonTopBar(
                title = "PROFILES",
                subtitle = "MANAGEMENT",
                actions = {
                    // Ping all button
                    IconButton(
                        onClick = { viewModel.pingAll() },
                        enabled = !isPingingAll
                    ) {
                        Icon(
                            imageVector = Icons.Default.NetworkCheck,
                            contentDescription = "Test all ping",
                            tint = if (isPingingAll) TextMuted else NyxtraNeonGreen
                        )
                    }

                    // Import from clipboard
                    IconButton(
                        onClick = { viewModel.showImportDialog() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentPaste,
                            contentDescription = "Import URI",
                            tint = NyxtraCyberBlue
                        )
                    }
                }
            )

            // Search Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "Search server, bug host, protocol...",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = NyxtraSurface,
                        unfocusedContainerColor = NyxtraSurface,
                        focusedBorderColor = NyxtraNeonGreen,
                        unfocusedBorderColor = NyxtraCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            }

            // Profile List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${profiles.size} SERVERS AVAILABLE",
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )

                        TextButton(
                            onClick = { viewModel.pingAll() },
                            enabled = !isPingingAll
                        ) {
                            Text(
                                text = if (isPingingAll) "TESTING..." else "TEST ALL PING",
                                color = NyxtraCyberBlue,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                items(profiles, key = { it.id }) { profile ->
                    ProfileCard(
                        profile = profile,
                        onSelect = { viewModel.selectProfile(profile.id) },
                        onPing = { viewModel.pingProfile(profile.id) },
                        onEdit = {
                            viewModel.startEditProfile(profile)
                            onNavigateToEdit()
                        },
                        onDelete = { viewModel.deleteProfile(profile.id) },
                        onExport = {
                            val uri = viewModel.exportUri(profile)
                            clipboardManager.setText(AnnotatedString(uri))
                            Toast.makeText(context, "Copied URI to clipboard!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }

        // FAB to add profile manually
        FloatingActionButton(
            onClick = {
                viewModel.startCreateProfile()
                onNavigateToEdit()
            },
            containerColor = NyxtraNeonGreen,
            contentColor = TextOnAccent,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Profile")
        }

        // Import Dialog
        if (importDialogVisible) {
            ImportUriDialog(
                onDismiss = { viewModel.hideImportDialog() },
                onImport = { uri ->
                    val success = viewModel.importUri(uri)
                    if (success) {
                        Toast.makeText(context, "Profile imported successfully!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Invalid URI format. Expected vless://, vmess://, or trojan://", Toast.LENGTH_LONG).show()
                    }
                }
            )
        }
    }
}

@Composable
private fun ImportUriDialog(
    onDismiss: () -> Unit,
    onImport: (String) -> Unit
) {
    var rawText by remember { mutableStateOf("") }
    val clipboardManager = LocalClipboardManager.current

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NyxtraSurface,
        title = {
            Text(
                text = "IMPORT PROFILE URI",
                color = TextPrimary,
                fontSize = 16.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "Supported schemes: vless://, vmess://, trojan://",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = rawText,
                    onValueChange = { rawText = it },
                    placeholder = { Text("Paste configuration URI here...", color = TextMuted, fontSize = 12.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = NyxtraCard,
                        unfocusedContainerColor = NyxtraCard,
                        focusedBorderColor = NyxtraNeonGreen,
                        unfocusedBorderColor = NyxtraCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        clipboardManager.getText()?.text?.let { rawText = it }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NyxtraSurfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentPaste,
                        contentDescription = null,
                        tint = NyxtraCyberBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Paste from Clipboard", color = TextPrimary, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onImport(rawText) },
                colors = ButtonDefaults.buttonColors(containerColor = NyxtraNeonGreen)
            ) {
                Text(text = "Import", color = TextOnAccent, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel", color = TextMuted)
            }
        }
    )
}
