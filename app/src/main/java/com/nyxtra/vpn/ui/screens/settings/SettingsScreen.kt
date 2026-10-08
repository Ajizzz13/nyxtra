package com.nyxtra.vpn.ui.screens.settings

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nyxtra.vpn.data.model.TunStackMode
import com.nyxtra.vpn.ui.components.CommonTopBar
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
fun SettingsScreen(
    viewModel: SettingsViewModel
) {
    val context = LocalContext.current
    val config by viewModel.config.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NyxtraBackground)
    ) {
        CommonTopBar(
            title = "LOW-LATENCY ENGINE",
            subtitle = "TUNING"
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Engine Header note
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(NyxtraNeonGreen.copy(alpha = 0.08f))
                    .border(1.dp, NyxtraNeonGreen.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = NyxtraNeonGreen,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Engine parameters are fine-tuned according to PRD section 5 to eradicate jitter in mobile games (Free Fire, MLBB, PUBG).",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            // Direct FD Handover (Read only / enforced on)
            EngineSwitchCard(
                title = "Direct FD Handover",
                description = "File Descriptor passing from VpnService directly to Sing-box Core without 127.0.0.1 proxy overhead.",
                checked = config.directFdHandover,
                enabled = false,
                onCheckedChange = {}
            )

            // MTU Customizer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(NyxtraCard)
                    .border(1.dp, NyxtraCardBorder, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Virtual Interface MTU",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Clamped range 1280 - 1340 bytes to prevent BTS fragmentation",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }

                        Text(
                            text = "${config.mtu} B",
                            color = NyxtraNeonGreen,
                            fontSize = 16.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Slider(
                        value = config.mtu.toFloat(),
                        onValueChange = { viewModel.updateMtu(it.toInt()) },
                        valueRange = 1280f..1340f,
                        steps = 6,
                        colors = SliderDefaults.colors(
                            thumbColor = NyxtraNeonGreen,
                            activeTrackColor = NyxtraNeonGreen,
                            inactiveTrackColor = NyxtraSurfaceVariant
                        )
                    )
                }
            }

            // Stack TUN Mode Selection
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(NyxtraCard)
                    .border(1.dp, NyxtraCardBorder, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "TUN Stack Architecture",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "System kernel mode bypasses userspace gVisor memory copies",
                        color = TextMuted,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        TunStackMode.values().forEach { mode ->
                            val isSelected = config.tunStack == mode
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) NyxtraCyberBlue.copy(alpha = 0.15f) else NyxtraSurface)
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) NyxtraCyberBlue else NyxtraCardBorder,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { viewModel.updateTunStack(mode) }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = mode.displayName,
                                        color = if (isSelected) NyxtraCyberBlue else TextSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                    if (isSelected) {
                                        Text(
                                            text = "ACTIVE",
                                            color = NyxtraCyberBlue,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Zero Routing & Zero Sniffing (Game Mode)
            EngineSwitchCard(
                title = "Zero Routing & Zero Sniffing",
                description = "Completely disable domain sniffing and regex rule processing to eliminate packet delay.",
                checked = config.zeroRoutingSniffing,
                enabled = true,
                onCheckedChange = { viewModel.toggleZeroRouting(it) }
            )

            // TCP NoDelay & Aggressive Keepalive
            EngineSwitchCard(
                title = "TCP NoDelay & Modem High-Power",
                description = "Force socket outbound TCP_NODELAY and aggressive keepalives to prevent modem idle throttling.",
                checked = config.tcpNoDelay,
                enabled = true,
                onCheckedChange = { viewModel.toggleTcpNoDelay(it) }
            )

            // Build Variant & App Info
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(NyxtraCard)
                    .border(1.dp, NyxtraCardBorder, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "BUILD DISTRIBUTION (PRD SPEC)",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Modern Variant (arm64-v8a): Target Android 9.0+ (API 28+)\n• Legacy Variant (armeabi-v7a & arm64): Target Android 5.0+ (API 21+)",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            // Reset Button
            Button(
                onClick = {
                    viewModel.resetToDefaults()
                    Toast.makeText(context, "Engine reset to PRD gaming defaults", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = NyxtraSurfaceVariant),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = null,
                    tint = TextMuted
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Reset Engine Defaults", color = TextPrimary, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

@Composable
private fun EngineSwitchCard(
    title: String,
    description: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(NyxtraCard)
            .border(1.dp, NyxtraCardBorder, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                color = TextMuted,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Switch(
            checked = checked,
            enabled = enabled,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = NyxtraNeonGreen,
                checkedTrackColor = NyxtraNeonGreen.copy(alpha = 0.3f),
                disabledCheckedThumbColor = NyxtraNeonGreen,
                disabledCheckedTrackColor = NyxtraNeonGreen.copy(alpha = 0.3f)
            )
        )
    }
}
