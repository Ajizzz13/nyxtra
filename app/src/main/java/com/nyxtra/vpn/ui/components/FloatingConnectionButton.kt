package com.nyxtra.vpn.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.nyxtra.vpn.data.model.VpnState
import com.nyxtra.vpn.ui.theme.NyxtraAccent
import com.nyxtra.vpn.ui.theme.NyxtraRed
import com.nyxtra.vpn.ui.theme.TextOnAccent

@Composable
fun FloatingConnectionButton(
    state: VpnState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isConnecting = state == VpnState.CONNECTING || state == VpnState.DISCONNECTING
    val isConnected = state == VpnState.CONNECTED

    val backgroundColor = when {
        isConnected -> NyxtraRed
        else -> NyxtraAccent
    }

    Box(
        modifier = modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        when {
            isConnecting -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = TextOnAccent,
                    strokeWidth = 2.5.dp
                )
            }
            isConnected -> {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = "Disconnect Nyxtra VPN",
                    tint = TextOnAccent,
                    modifier = Modifier.size(28.dp)
                )
            }
            else -> {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Connect Nyxtra VPN",
                    tint = TextOnAccent,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
