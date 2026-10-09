package com.nyxtra.vpn.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nyxtra.vpn.data.model.VpnState
import com.nyxtra.vpn.ui.theme.NyxtraRed
import com.nyxtra.vpn.ui.theme.NyxtraTeal

@Composable
fun FloatingConnectionButton(
    state: VpnState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isConnecting = state == VpnState.CONNECTING || state == VpnState.DISCONNECTING
    val isConnected = state == VpnState.CONNECTED

    FloatingActionButton(
        onClick = onClick,
        containerColor = if (isConnected) NyxtraRed else NyxtraTeal,
        contentColor = Color.White,
        modifier = modifier
    ) {
        when {
            isConnecting -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.5.dp
                )
            }
            isConnected -> {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = "Disconnect",
                    modifier = Modifier.size(28.dp)
                )
            }
            else -> {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Connect",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
