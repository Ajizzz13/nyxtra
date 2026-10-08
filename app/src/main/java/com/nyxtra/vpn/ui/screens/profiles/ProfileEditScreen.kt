package com.nyxtra.vpn.ui.screens.profiles

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nyxtra.vpn.data.model.ProtocolType
import com.nyxtra.vpn.data.model.TransportType
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NyxtraBackground)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
            Text(
                text = "PROFILE EDITOR",
                color = TextPrimary,
                fontSize = 16.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Protocol Selector
            Text(
                text = "PROTOCOL",
                color = TextMuted,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProtocolType.values().forEach { proto ->
                    val isSelected = protocol == proto
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) NyxtraNeonGreen.copy(alpha = 0.15f) else NyxtraSurface)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) NyxtraNeonGreen else NyxtraCardBorder,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { protocol = proto }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = proto.displayName,
                            color = if (isSelected) NyxtraNeonGreen else TextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Profile Name
            EditorTextField(
                label = "Profile Name",
                value = name,
                onValueChange = { name = it },
                placeholder = "e.g. SG Melbikomas Gaming"
            )

            // Server Address & Port
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(modifier = Modifier.weight(2.5f)) {
                    EditorTextField(
                        label = "Server Host / IP",
                        value = serverAddress,
                        onValueChange = { serverAddress = it },
                        placeholder = "e.g. 104.21.5.12 or domain"
                    )
                }

                Box(modifier = Modifier.weight(1f)) {
                    EditorTextField(
                        label = "Port",
                        value = serverPort.toString(),
                        onValueChange = { serverPort = it.toIntOrNull() ?: 443 },
                        placeholder = "443",
                        keyboardType = KeyboardType.Number
                    )
                }
            }

            // Bug Host (Injeksi Bug Host & SNI)
            EditorTextField(
                label = "Bug Host / Host Header",
                value = bugHost,
                onValueChange = { bugHost = it },
                placeholder = "e.g. graph.facebook.com or quiz.vidio.com"
            )

            // SNI
            EditorTextField(
                label = "Server Name Indication (SNI)",
                value = sni,
                onValueChange = { sni = it },
                placeholder = "Leave empty to use Bug Host"
            )

            // UUID or Password
            EditorTextField(
                label = if (protocol == ProtocolType.TROJAN) "Password" else "UUID",
                value = uuidOrPassword,
                onValueChange = { uuidOrPassword = it },
                placeholder = "e.g. 3a7b-45..."
            )

            // Transport Selector
            Text(
                text = "TRANSPORT NETWORK",
                color = TextMuted,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TransportType.values().forEach { trans ->
                    val isSelected = transport == trans
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) NyxtraCyberBlue.copy(alpha = 0.15f) else NyxtraSurface)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) NyxtraCyberBlue else NyxtraCardBorder,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { transport = trans }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = trans.displayName,
                            color = if (isSelected) NyxtraCyberBlue else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Path
            EditorTextField(
                label = "WebSocket / HTTPUpgrade Path",
                value = path,
                onValueChange = { path = it },
                placeholder = "e.g. /ws or /vless"
            )

            // Security toggles
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(NyxtraCard)
                    .border(1.dp, NyxtraCardBorder, RoundedCornerShape(10.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Enable TLS Encryption", color = TextPrimary, fontSize = 13.sp)
                    Text(text = "Required for port 443 & SNI bug handshake", color = TextMuted, fontSize = 11.sp)
                }
                Switch(
                    checked = isTls,
                    onCheckedChange = { isTls = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = NyxtraNeonGreen,
                        checkedTrackColor = NyxtraNeonGreen.copy(alpha = 0.3f)
                    )
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(NyxtraCard)
                    .border(1.dp, NyxtraCardBorder, RoundedCornerShape(10.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Allow Insecure Certificates", color = TextPrimary, fontSize = 13.sp)
                    Text(text = "Bypass self-signed SSL verification", color = TextMuted, fontSize = 11.sp)
                }
                Switch(
                    checked = allowInsecure,
                    onCheckedChange = { allowInsecure = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = NyxtraCyberBlue,
                        checkedTrackColor = NyxtraCyberBlue.copy(alpha = 0.3f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Save Button
            Button(
                onClick = {
                    if (name.isBlank() || serverAddress.isBlank() || uuidOrPassword.isBlank()) {
                        Toast.makeText(context, "Name, Server, and UUID/Password cannot be empty!", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(context, "Profile saved!", Toast.LENGTH_SHORT).show()
                    onNavigateBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NyxtraNeonGreen),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = TextOnAccent
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SAVE PROFILE CONFIG",
                    color = TextOnAccent,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun EditorTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column {
        Text(
            text = label.uppercase(),
            color = TextMuted,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = TextMuted, fontSize = 12.sp) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(8.dp),
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
}
