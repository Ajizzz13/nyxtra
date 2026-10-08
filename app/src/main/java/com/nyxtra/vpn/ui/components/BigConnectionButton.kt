package com.nyxtra.vpn.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nyxtra.vpn.data.model.VpnState
import com.nyxtra.vpn.ui.theme.NyxtraCyberBlue
import com.nyxtra.vpn.ui.theme.NyxtraNeonGreen
import com.nyxtra.vpn.ui.theme.NyxtraSurface
import com.nyxtra.vpn.ui.theme.NyxtraSurfaceVariant
import com.nyxtra.vpn.ui.theme.TextMuted
import com.nyxtra.vpn.ui.theme.TextPrimary

@Composable
fun BigConnectionButton(
    state: VpnState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isConnected = state == VpnState.CONNECTED
    val isConnecting = state == VpnState.CONNECTING || state == VpnState.DISCONNECTING

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isConnected) 1.14f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val activeColor = when (state) {
        VpnState.CONNECTED -> NyxtraNeonGreen
        VpnState.CONNECTING, VpnState.DISCONNECTING -> NyxtraCyberBlue
        else -> TextMuted
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(190.dp)
        ) {
            // Pulse outer glow when connected
            if (isConnected) {
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(activeColor.copy(alpha = 0.15f))
                )
            }

            // Outer decorative ring
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        brush = Brush.sweepGradient(
                            listOf(
                                activeColor.copy(alpha = 0.2f),
                                activeColor,
                                activeColor.copy(alpha = 0.2f)
                            )
                        ),
                        shape = CircleShape
                    )
                    .background(NyxtraSurface)
            )

            // Inner button
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                if (isConnected) activeColor.copy(alpha = 0.25f) else NyxtraSurfaceVariant,
                                NyxtraSurface
                            )
                        )
                    )
                    .border(
                        width = 1.5.dp,
                        color = if (isConnected) activeColor else NyxtraSurfaceVariant,
                        shape = CircleShape
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isConnecting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(64.dp),
                        color = NyxtraCyberBlue,
                        strokeWidth = 3.dp
                    )
                } else {
                    Icon(
                        imageVector = if (isConnected) Icons.Default.WifiTethering else Icons.Default.PowerSettingsNew,
                        contentDescription = "Toggle Connection",
                        tint = activeColor,
                        modifier = Modifier.size(54.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // State Text
        val stateText = when (state) {
            VpnState.CONNECTED -> "SECURE TUNNEL ACTIVE"
            VpnState.CONNECTING -> "ESTABLISHING TUN..."
            VpnState.DISCONNECTING -> "DISCONNECTING..."
            VpnState.DISCONNECTED -> "READY TO CONNECT"
            VpnState.ERROR -> "CONNECTION ERROR"
        }

        Text(
            text = stateText,
            color = activeColor,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            letterSpacing = 1.2.sp
        )
    }
}
