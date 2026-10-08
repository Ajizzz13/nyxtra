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
import androidx.compose.foundation.layout.width
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
import com.nyxtra.vpn.ui.theme.ActionPrimaryBg
import com.nyxtra.vpn.ui.theme.ActionPrimaryText
import com.nyxtra.vpn.ui.theme.BorderStrong
import com.nyxtra.vpn.ui.theme.BorderSubtle
import com.nyxtra.vpn.ui.theme.CanvasBg
import com.nyxtra.vpn.ui.theme.CardBg
import com.nyxtra.vpn.ui.theme.CardSelectedBg
import com.nyxtra.vpn.ui.theme.SurfaceBg
import com.nyxtra.vpn.ui.theme.TextMuted
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
            .background(CanvasBg)
    ) {
        // App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .background(SurfaceBg)
                .border(1.dp, BorderSubtle)
                .padding(horizontal = 6.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Edit Profile",
                color = TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.2).sp
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Protocol Tabs
            Text(
                text = "PROTOCOL",
                color = TextMuted,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
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
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) CardSelectedBg else CardBg)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) BorderStrong else BorderSubtle,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .clickable { protocol = proto }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = proto.displayName,
                            color = if (isSelected) TextPrimary else TextSecondary,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            // Name
            EditInput(
                label = "REMARKS / NAME",
                value = name,
                onValueChange = { name = it },
                placeholder = "e.g. SG Melbikomas 01"
            )

            // Host & Port
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(modifier = Modifier.weight(2.5f)) {
                    EditInput(
                        label = "SERVER / DESTINATION IP",
                        value = serverAddress,
                        onValueChange = { serverAddress = it },
                        placeholder = "104.18.41.141 or host"
                    )
                }

                Box(modifier = Modifier.weight(1f)) {
                    EditInput(
                        label = "PORT",
                        value = serverPort.toString(),
                        onValueChange = { serverPort = it.toIntOrNull() ?: 443 },
                        placeholder = "443",
                        keyboardType = KeyboardType.Number
                    )
                }
            }

            // Bug Host
            EditInput(
                label = "BUG HOST (HTTP HOST HEADER)",
                value = bugHost,
                onValueChange = { bugHost = it },
                placeholder = "e.g. quiz.vidio.com"
            )

            // SNI
            EditInput(
                label = "SNI (SERVER NAME INDICATION)",
                value = sni,
                onValueChange = { sni = it },
                placeholder = "Leave empty to fallback to Bug Host"
            )

            // UUID / Password
            EditInput(
                label = if (protocol == ProtocolType.TROJAN) "PASSWORD" else "UUID",
                value = uuidOrPassword,
                onValueChange = { uuidOrPassword = it },
                placeholder = "3a7b-..."
            )

            // Transport Selector
            Text(
                text = "TRANSPORT",
                color = TextMuted,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
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
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) CardSelectedBg else CardBg)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) BorderStrong else BorderSubtle,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .clickable { transport = trans }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = trans.displayName,
                            color = if (isSelected) TextPrimary else TextSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            // Path
            EditInput(
                label = "PATH",
                value = path,
                onValueChange = { path = it },
                placeholder = "/ws or /httpupgrade"
            )

            // TLS Switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(CardBg)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(6.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "TLS Encryption", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Text(text = "Enable transport security layer", color = TextMuted, fontSize = 11.sp)
                }
                Switch(
                    checked = isTls,
                    onCheckedChange = { isTls = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ActionPrimaryText,
                        checkedTrackColor = ActionPrimaryBg,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = SurfaceBg
                    )
                )
            }

            // Insecure Switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(CardBg)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(6.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Allow Insecure", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Text(text = "Skip TLS certificate verification", color = TextMuted, fontSize = 11.sp)
                }
                Switch(
                    checked = allowInsecure,
                    onCheckedChange = { allowInsecure = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ActionPrimaryText,
                        checkedTrackColor = ActionPrimaryBg,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = SurfaceBg
                    )
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Save Button
            Button(
                onClick = {
                    if (name.isBlank() || serverAddress.isBlank() || uuidOrPassword.isBlank()) {
                        Toast.makeText(context, "Fill required fields!", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(context, "Saved!", Toast.LENGTH_SHORT).show()
                    onNavigateBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ActionPrimaryBg),
                shape = RoundedCornerShape(6.dp)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = ActionPrimaryText, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "SAVE CONFIGURATION", color = ActionPrimaryText, fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = FontFamily.Monospace, letterSpacing = 0.5.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun EditInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column {
        Text(
            text = label,
            color = TextMuted,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = TextMuted, fontSize = 12.sp, fontFamily = FontFamily.Monospace) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(6.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = CardBg,
                unfocusedContainerColor = CardBg,
                focusedBorderColor = BorderStrong,
                unfocusedBorderColor = BorderSubtle,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )
    }
}
