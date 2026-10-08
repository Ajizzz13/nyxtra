package com.nyxtra.vpn.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nyxtra.vpn.data.model.TrafficStats
import com.nyxtra.vpn.data.model.VpnProfile
import com.nyxtra.vpn.data.model.VpnState
import com.nyxtra.vpn.ui.theme.BorderSubtle
import com.nyxtra.vpn.ui.theme.CardBg
import com.nyxtra.vpn.ui.theme.PastelCyan
import com.nyxtra.vpn.ui.theme.PastelGreen
import com.nyxtra.vpn.ui.theme.PastelGreenSubtle
import com.nyxtra.vpn.ui.theme.PastelOrange
import com.nyxtra.vpn.ui.theme.PastelOrangeSubtle
import com.nyxtra.vpn.ui.theme.PastelRed
import com.nyxtra.vpn.ui.theme.PastelRedSubtle
import com.nyxtra.vpn.ui.theme.TextMuted
import com.nyxtra.vpn.ui.theme.TextPrimary
import com.nyxtra.vpn.ui.theme.TextSecondary

@Composable
fun BentoTelemetryWidget(
    vpnState: VpnState,
    trafficStats: TrafficStats,
    selectedProfile: VpnProfile?,
    modifier: Modifier = Modifier
) {
    // Perpetual Motion: Breathing Status Beacon
    val infiniteTransition = rememberInfiniteTransition(label = "beaconPulse")
    val beaconAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "beaconAlpha"
    )
    val beaconScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "beaconScale"
    )

    val (statusLabel, beaconColor, beaconBg) = when (vpnState) {
        VpnState.CONNECTED -> Triple("INGRESS ACTIVE // TUN DIRECT", PastelGreen, PastelGreenSubtle)
        VpnState.CONNECTING -> Triple("LINKING FD INTERFACE...", PastelOrange, PastelOrangeSubtle)
        VpnState.DISCONNECTING -> Triple("TEARING DOWN TUN...", PastelOrange, PastelOrangeSubtle)
        VpnState.DISCONNECTED -> Triple("STANDBY // READY", TextMuted, BorderSubtle)
        VpnState.ERROR -> Triple("TUNNEL ERROR // HALTED", PastelRed, PastelRedSubtle)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(CardBg)
            .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header: Status Beacon & Ping Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Micro-Interaction: Pulsing beacon ring & solid core
                    Box(
                        modifier = Modifier.size(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (vpnState == VpnState.CONNECTED || vpnState == VpnState.CONNECTING || vpnState == VpnState.ERROR) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .scale(beaconScale)
                                    .clip(CircleShape)
                                    .background(beaconColor.copy(alpha = beaconAlpha * 0.3f))
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(beaconColor)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = statusLabel,
                        color = beaconColor,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                // Ping Metric
                val ping = selectedProfile?.pingMs
                if (ping != null) {
                    val pingColor = if (ping < 80) PastelGreen else if (ping < 200) PastelOrange else PastelRed
                    val pingBg = if (ping < 80) PastelGreenSubtle else if (ping < 200) PastelOrangeSubtle else PastelRedSubtle
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(pingBg)
                            .border(1.dp, pingColor.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${ping} MS",
                            color = pingColor,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Primary Target Identity
            val profileName = selectedProfile?.name ?: "No Profile Configured"
            Text(
                text = profileName,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.3).sp,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(3.dp))

            val endpointSummary = if (selectedProfile != null) {
                val host = if (selectedProfile.bugHost.isNotBlank()) selectedProfile.bugHost else selectedProfile.serverAddress
                "${host}:${selectedProfile.serverPort} • ${selectedProfile.protocol.displayName}/${selectedProfile.transport.displayName}${if (selectedProfile.isTls) "+TLS" else ""}"
            } else {
                "Import URI or tap + to create tunnel profile"
            }

            Text(
                text = endpointSummary.uppercase(),
                color = TextSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = (-0.1).sp,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(12.dp))

            Divider(color = BorderSubtle, thickness = 1.dp)

            Spacer(modifier = Modifier.height(10.dp))

            // Asymmetric Bento Columns: Throughput & Kernel Engine Specs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Column 1: Live Throughput
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "LIVE THROUGHPUT",
                        color = TextMuted,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "↓ ${trafficStats.formatDownloadSpeed()}",
                            color = if (vpnState == VpnState.CONNECTED) PastelCyan else TextPrimary,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "↑ ${trafficStats.formatUploadSpeed()}",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Column 2: Kernel Pipeline Specs
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "KERNEL DIRECT-FD",
                        color = TextMuted,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(BorderSubtle.copy(alpha = 0.6f))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "MTU:1340",
                                color = TextSecondary,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(BorderSubtle.copy(alpha = 0.6f))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "TCP_NODELAY",
                                color = TextSecondary,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}
