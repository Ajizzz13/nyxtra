package com.nyxtra.vpn.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nyxtra.vpn.data.model.TransportType
import com.nyxtra.vpn.data.model.VpnProfile
import com.nyxtra.vpn.ui.theme.ActionPrimaryBg
import com.nyxtra.vpn.ui.theme.BorderStrong
import com.nyxtra.vpn.ui.theme.BorderSubtle
import com.nyxtra.vpn.ui.theme.CardBg
import com.nyxtra.vpn.ui.theme.CardSelectedBg
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
fun TunnelProfileCard(
    profile: VpnProfile,
    onSelect: () -> Unit,
    onPing: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSelected = profile.isSelected
    val borderColor = if (isSelected) BorderStrong else BorderSubtle
    val cardBackground = if (isSelected) CardSelectedBg else CardBg

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(cardBackground)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onSelect)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            // Subtle selection edge
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(if (isSelected) ActionPrimaryBg else Color.Transparent)
            )

            // Card Body
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                // Top Row: Title + Ping Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = profile.name,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = (-0.2).sp,
                        modifier = Modifier.weight(1f),
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    TunnelPingBadge(
                        pingMs = profile.pingMs,
                        onClick = onPing
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Middle Row: Monospace endpoint + Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val hostDisplay = if (profile.bugHost.isNotBlank()) {
                        "${profile.bugHost}:${profile.serverPort}"
                    } else {
                        "${profile.serverAddress}:${profile.serverPort}"
                    }

                    Text(
                        text = hostDisplay,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = (-0.2).sp,
                        modifier = Modifier.weight(1f),
                        maxLines = 1
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        IconButton(
                            onClick = onShare,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = TextMuted,
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        IconButton(
                            onClick = onEdit,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = TextMuted,
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete",
                                tint = TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Bottom Row: Clean technical tag
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val transportCode = when (profile.transport) {
                        TransportType.WS -> "WS"
                        TransportType.HTTP_UPGRADE -> "HTTP-UPGRADE"
                        TransportType.GRPC -> "GRPC"
                        TransportType.TCP -> "TCP"
                    }
                    val tlsCode = if (profile.isTls) " / TLS" else ""
                    val protocolTag = "${profile.protocol.displayName} • $transportCode$tlsCode"

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(BorderSubtle.copy(alpha = 0.5f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = protocolTag.uppercase(),
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TunnelPingBadge(
    pingMs: Long?,
    onClick: () -> Unit
) {
    val (textColor, bgColor, text) = when {
        pingMs == null -> Triple(TextMuted, BorderSubtle, "PING")
        pingMs < 0 -> Triple(PastelRed, PastelRedSubtle, "TIMEOUT")
        pingMs < 80 -> Triple(PastelGreen, PastelGreenSubtle, "${pingMs} MS")
        pingMs < 250 -> Triple(PastelOrange, PastelOrangeSubtle, "${pingMs} MS")
        else -> Triple(PastelRed, PastelRedSubtle, "${pingMs} MS")
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .border(1.dp, textColor.copy(alpha = 0.25f), RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.5.sp
        )
    }
}
