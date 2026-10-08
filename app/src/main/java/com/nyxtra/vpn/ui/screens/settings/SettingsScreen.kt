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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val config by viewModel.config.collectAsState()

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
                text = "Engine Settings",
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
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Direct FD
            SettingsToggleCard(
                title = "Direct FD Handover",
                desc = "Pass TUN file descriptor directly to Sing-box Core (No 127.0.0.1 proxy)",
                checked = config.directFdHandover,
                enabled = false,
                onCheckedChange = {}
            )

            // MTU
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(CardBg)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Virtual Interface MTU", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text(text = "Clamped (1280 - 1340) to prevent packet drop", color = TextMuted, fontSize = 11.sp)
                        }
                        Text(text = "${config.mtu}", color = TextPrimary, fontSize = 15.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Slider(
                        value = config.mtu.toFloat(),
                        onValueChange = { viewModel.updateMtu(it.toInt()) },
                        valueRange = 1280f..1340f,
                        steps = 6,
                        colors = SliderDefaults.colors(
                            thumbColor = ActionPrimaryBg,
                            activeTrackColor = ActionPrimaryBg,
                            inactiveTrackColor = BorderSubtle
                        )
                    )
                }
            }

            // TUN Stack
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(CardBg)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Text(text = "TUN Stack Mode", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Text(text = "System kernel mode reduces userspace memory copy latency", color = TextMuted, fontSize = 11.sp)

                    Spacer(modifier = Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        TunStackMode.values().forEach { mode ->
                            val isSelected = config.tunStack == mode
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) CardSelectedBg else SurfaceBg)
                                    .border(1.dp, if (isSelected) BorderStrong else BorderSubtle, RoundedCornerShape(6.dp))
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
                                        color = if (isSelected) TextPrimary else TextSecondary,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                    if (isSelected) {
                                        Text(text = "ACTIVE", color = TextPrimary, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Zero Routing & Zero Sniffing
            SettingsToggleCard(
                title = "Zero Routing & Zero Sniffing",
                desc = "Disable domain sniffing & regex rules for ultra low latency gaming",
                checked = config.zeroRoutingSniffing,
                enabled = true,
                onCheckedChange = { viewModel.toggleZeroRouting(it) }
            )

            // TCP NoDelay
            SettingsToggleCard(
                title = "TCP NoDelay & Keepalive",
                desc = "Aggressive socket transmission to prevent modem sleep jitter",
                checked = config.tcpNoDelay,
                enabled = true,
                onCheckedChange = { viewModel.toggleTcpNoDelay(it) }
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Reset Button
            Button(
                onClick = {
                    viewModel.resetToDefaults()
                    Toast.makeText(context, "Reset to gaming defaults!", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = CardBg),
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null, tint = TextMuted, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "RESET DEFAULT VALUES", color = TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace, letterSpacing = 0.5.sp)
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun SettingsToggleCard(
    title: String,
    desc: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(CardBg)
            .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = desc, color = TextMuted, fontSize = 11.sp)
        }

        Spacer(modifier = Modifier.width(10.dp))

        Switch(
            checked = checked,
            enabled = enabled,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = ActionPrimaryText,
                checkedTrackColor = ActionPrimaryBg,
                disabledCheckedThumbColor = ActionPrimaryText,
                disabledCheckedTrackColor = ActionPrimaryBg.copy(alpha = 0.6f),
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = SurfaceBg
            )
        )
    }
}
