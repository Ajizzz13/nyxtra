package com.nyxtra.vpn.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nyxtra.vpn.data.model.LogEntry
import com.nyxtra.vpn.data.model.LogLevel
import com.nyxtra.vpn.ui.theme.PastelCyan
import com.nyxtra.vpn.ui.theme.PastelCyanSubtle
import com.nyxtra.vpn.ui.theme.PastelOrange
import com.nyxtra.vpn.ui.theme.PastelOrangeSubtle
import com.nyxtra.vpn.ui.theme.PastelRed
import com.nyxtra.vpn.ui.theme.PastelRedSubtle
import com.nyxtra.vpn.ui.theme.TextMuted
import com.nyxtra.vpn.ui.theme.TextPrimary

@Composable
fun LogItemRow(
    entry: LogEntry,
    modifier: Modifier = Modifier
) {
    val (textColor, bgColor) = when (entry.level) {
        LogLevel.DEBUG -> TextMuted to TextMuted.copy(alpha = 0.1f)
        LogLevel.INFO -> PastelCyan to PastelCyanSubtle
        LogLevel.WARN -> PastelOrange to PastelOrangeSubtle
        LogLevel.ERROR -> PastelRed to PastelRedSubtle
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = entry.formattedTime(),
            color = TextMuted,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(3.dp))
                .background(bgColor)
                .padding(horizontal = 4.dp, vertical = 1.dp)
        ) {
            Text(
                text = entry.level.name,
                color = textColor,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.5.sp
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "[${entry.tag}] ${entry.message}",
                color = if (entry.level == LogLevel.ERROR) PastelRed else TextPrimary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 15.sp
            )
        }
    }
}
