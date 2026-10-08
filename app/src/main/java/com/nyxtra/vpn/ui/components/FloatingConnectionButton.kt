package com.nyxtra.vpn.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nyxtra.vpn.data.model.VpnState
import com.nyxtra.vpn.ui.theme.ActionPrimaryBg
import com.nyxtra.vpn.ui.theme.ActionPrimaryText
import com.nyxtra.vpn.ui.theme.BorderStrong
import com.nyxtra.vpn.ui.theme.PastelRed
import com.nyxtra.vpn.ui.theme.PastelRedSubtle
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun FloatingConnectionButton(
    state: VpnState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var elapsedSeconds by remember { mutableLongStateOf(0L) }

    LaunchedEffect(state) {
        if (state == VpnState.CONNECTED) {
            elapsedSeconds = 0L
            while (true) {
                delay(1000)
                elapsedSeconds++
            }
        } else {
            elapsedSeconds = 0L
        }
    }

    val isConnecting = state == VpnState.CONNECTING || state == VpnState.DISCONNECTING
    val isConnected = state == VpnState.CONNECTED

    Box(modifier = modifier) {
        if (isConnected) {
            // Connected State: Utilitarian tactile card with timer and stop indicator
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(PastelRedSubtle)
                    .border(1.dp, PastelRed.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .clickable(onClick = onClick)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(PastelRed, RoundedCornerShape(2.dp))
                )

                Spacer(modifier = Modifier.width(10.dp))

                val hours = elapsedSeconds / 3600
                val minutes = (elapsedSeconds % 3600) / 60
                val seconds = elapsedSeconds % 60
                val timerText = String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)

                Text(
                    text = timerText,
                    color = PastelRed,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "DISCONNECT",
                    color = PastelRed.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                )
            }
        } else {
            // Disconnected State: Solid flat button with 0dp shadow, crisp 8dp corner radius
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(ActionPrimaryBg)
                    .border(1.dp, BorderStrong, RoundedCornerShape(8.dp))
                    .clickable(onClick = onClick)
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (isConnecting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = ActionPrimaryText,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CONNECTING...",
                        color = ActionPrimaryText,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Connect",
                        tint = ActionPrimaryText,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CONNECT",
                        color = ActionPrimaryText,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}
