package com.nyxtra.vpn.ui.screens.profiles

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nyxtra.vpn.data.model.ProtocolType
import com.nyxtra.vpn.data.model.TransportType
import com.nyxtra.vpn.ui.theme.NyxtraDark
import com.nyxtra.vpn.ui.theme.NyxtraSurface
import com.nyxtra.vpn.ui.theme.NyxtraTeal
import com.nyxtra.vpn.ui.theme.TextGray
import com.nyxtra.vpn.ui.theme.TextWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditScreen(
    viewModel: ProfilesViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val currentProfile by viewModel.editingProfile.collectAsState()

    val profile = currentProfile ?: return

    var name by remember { mutableStateOf(profile.name) }
    var protocol by remember { mutableStateOf(profile.protocol) }
    var serverAddress by remember { mutableStateOf(profile.serverAddress) }
    var serverPort by remember { mutableIntStateOf(profile.serverPort) }
    var uuidOrPassword by remember { mutableStateOf(profile.uuidOrPassword) }
    var bugHost by remember { mutableStateOf(profile.bugHost) }
    var sni by remember { mutableStateOf(profile.sni) }
    var path by remember { mutableStateOf(profile.path) }
    var transport by remember { mutableStateOf(profile.transport) }
    var isTls by remember { mutableStateOf(profile.isTls) }
    var allowInsecure by remember { mutableStateOf(profile.allowInsecure) }

    Scaffold(
        containerColor = NyxtraDark,
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile", color = TextWhite) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NyxtraSurface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Protocol Selector
            Text("Protocol", color = TextGray, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProtocolType.values().forEach { proto ->
                    FilterChip(
                        selected = protocol == proto,
                        onClick = { protocol = proto },
                        label = { Text(proto.displayName) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NyxtraTeal,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Profile Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Profile Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Server Address & Port
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = serverAddress,
                    onValueChange = { serverAddress = it },
                    label = { Text("Server Address / IP") },
                    singleLine = true,
                    modifier = Modifier.weight(2.5f)
                )

                OutlinedTextField(
                    value = serverPort.toString(),
                    onValueChange = { serverPort = it.toIntOrNull() ?: 443 },
                    label = { Text("Port") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            // Bug Host
            OutlinedTextField(
                value = bugHost,
                onValueChange = { bugHost = it },
                label = { Text("Bug Host (Host Header)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // SNI
            OutlinedTextField(
                value = sni,
                onValueChange = { sni = it },
                label = { Text("SNI (Server Name Indication)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // UUID / Password
            OutlinedTextField(
                value = uuidOrPassword,
                onValueChange = { uuidOrPassword = it },
                label = { Text(if (protocol == ProtocolType.TROJAN) "Password" else "UUID") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Transport Selector
            Text("Transport", color = TextGray, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TransportType.values().forEach { trans ->
                    FilterChip(
                        selected = transport == trans,
                        onClick = { transport = trans },
                        label = { Text(trans.displayName) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NyxtraTeal,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Path
            OutlinedTextField(
                value = path,
                onValueChange = { path = it },
                label = { Text("Path") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // TLS Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("TLS", color = TextWhite, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    Text("Enable TLS encryption", color = TextGray, fontSize = 12.sp)
                }
                Switch(
                    checked = isTls,
                    onCheckedChange = { isTls = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = NyxtraTeal
                    )
                )
            }

            // Allow Insecure Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Allow Insecure", color = TextWhite, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    Text("Skip TLS certificate verification", color = TextGray, fontSize = 12.sp)
                }
                Switch(
                    checked = allowInsecure,
                    onCheckedChange = { allowInsecure = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = NyxtraTeal
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Save Button
            Button(
                onClick = {
                    if (name.isBlank() || serverAddress.isBlank() || uuidOrPassword.isBlank()) {
                        Toast.makeText(context, "Please fill required fields", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val updated = profile.copy(
                        name = name.trim(),
                        protocol = protocol,
                        serverAddress = serverAddress.trim(),
                        serverPort = serverPort,
                        uuidOrPassword = uuidOrPassword.trim(),
                        bugHost = bugHost.trim(),
                        sni = sni.trim(),
                        path = path.trim(),
                        transport = transport,
                        isTls = isTls,
                        allowInsecure = allowInsecure
                    )
                    viewModel.saveEditingProfile(updated)
                    Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()
                    onNavigateBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NyxtraTeal)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Save Configuration", color = Color.White, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
