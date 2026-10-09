package com.nyxtra.vpn.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nyxtra.vpn.data.model.LogEntry
import com.nyxtra.vpn.data.model.LogLevel
import com.nyxtra.vpn.ui.theme.NyxtraRed
import com.nyxtra.vpn.ui.theme.NyxtraTeal
import com.nyxtra.vpn.ui.theme.PastelOrange
import com.nyxtra.vpn.ui.theme.TextGray
import com.nyxtra.vpn.ui.theme.TextWhite

@Composable
fun LogItemRow(
    entry: LogEntry,
    modifier: Modifier = Modifier
) {
    val levelColor = when (entry.level) {
        LogLevel.DEBUG -> TextGray
        LogLevel.INFO -> NyxtraTeal
        LogLevel.WARN -> PastelOrange
        LogLevel.ERROR -> NyxtraRed
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = entry.formattedTime(),
            color = TextGray,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = entry.level.name,
            color = levelColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = "[${entry.tag}] ${entry.message}",
            color = if (entry.level == LogLevel.ERROR) NyxtraRed else TextWhite,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.weight(1f)
        )
    }
}
