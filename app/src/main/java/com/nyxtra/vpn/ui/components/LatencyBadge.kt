package com.nyxtra.vpn.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.nyxtra.vpn.ui.theme.NyxtraAmber
import com.nyxtra.vpn.ui.theme.NyxtraCrimson
import com.nyxtra.vpn.ui.theme.NyxtraNeonGreen
import com.nyxtra.vpn.ui.theme.TextMuted

@Composable
fun LatencyBadge(
    pingMs: Long?,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val (color, text) = when {
        pingMs == null -> TextMuted to "TEST PING"
        pingMs < 0 -> NyxtraCrimson to "TIMEOUT"
        pingMs < 50 -> NyxtraNeonGreen to "$pingMs ms"
        pingMs < 100 -> NyxtraAmber to "$pingMs ms"
        else -> NyxtraCrimson to "$pingMs ms"
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}
