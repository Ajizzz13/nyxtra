package com.nyxtra.vpn.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nyxtra.vpn.data.model.TransportType
import com.nyxtra.vpn.data.model.VpnProfile
import com.nyxtra.vpn.ui.theme.NyxtraAccent
import com.nyxtra.vpn.ui.theme.NyxtraAccentSubtle
import com.nyxtra.vpn.ui.theme.NyxtraBorder
import com.nyxtra.vpn.ui.theme.NyxtraCard
import com.nyxtra.vpn.ui.theme.NyxtraCardSelected
import com.nyxtra.vpn.ui.theme.NyxtraRed
import com.nyxtra.vpn.ui.theme.NyxtraRedSubtle
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
    val borderColor = if (isSelected) NyxtraAccent else NyxtraBorder
    val cardBackground = if (isSelected) NyxtraCardSelected else NyxtraCard

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(cardBackground)
            .border(1.dp, borderColor, RoundedCornerShape(6.dp))
            .clickable(onClick = onSelect)
            .padding(horizontal = 10.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Radio button indicator
            RadioButton(
                selected = isSelected,
                onClick = onSelect,
                colors = RadioButtonDefaults.colors(
                    selectedColor = NyxtraAccent,
                    unselectedColor = TextMuted
                ),
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Profile info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = profile.name,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    // Protocol Tag
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(NyxtraAccentSubtle)
                            .border(1.dp, NyxtraAccent.copy(alpha = 0.3f), RoundedCornerShape(3.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        val transportCode = when (profile.transport) {
                            TransportType.WS -> "WS"
                            TransportType.HTTP_UPGRADE -> "HTTP"
                            TransportType.GRPC -> "GRPC"
                            TransportType.TCP -> "TCP"
                        }
                        Text(
                            text = "${profile.protocol.displayName} • $transportCode",
                            color = NyxtraAccent,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                val hostDisplay = if (profile.bugHost.isNotBlank()) {
                    "${profile.bugHost}:${profile.serverPort}"
                } else {
                    "${profile.serverAddress}:${profile.serverPort}"
                }

                Text(
                    text = hostDisplay,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Ping badge
            TunnelPingBadge(
                pingMs = profile.pingMs,
                onClick = onPing
            )

            Spacer(modifier = Modifier.width(4.dp))

            // Quick actions
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
}

@Composable
fun TunnelPingBadge(
    pingMs: Long?,
    onClick: () -> Unit
) {
    val (textColor, bgColor, text) = when {
        pingMs == null -> Triple(TextMuted, NyxtraBorder, "PING")
        pingMs < 0 -> Triple(NyxtraRed, NyxtraRedSubtle, "TIMEOUT")
        else -> Triple(NyxtraAccent, NyxtraAccentSubtle, "${pingMs}ms")
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(3.dp))
            .background(bgColor)
            .border(1.dp, textColor.copy(alpha = 0.3f), RoundedCornerShape(3.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold
        )
    }
}
