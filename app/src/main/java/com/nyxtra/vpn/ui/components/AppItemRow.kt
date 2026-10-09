package com.nyxtra.vpn.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
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
import com.nyxtra.vpn.data.model.AppInfo
import com.nyxtra.vpn.ui.theme.NyxtraAccent
import com.nyxtra.vpn.ui.theme.NyxtraAccentSubtle
import com.nyxtra.vpn.ui.theme.BorderSubtle
import com.nyxtra.vpn.ui.theme.CardBg
import com.nyxtra.vpn.ui.theme.TextMuted
import com.nyxtra.vpn.ui.theme.TextPrimary
import com.nyxtra.vpn.ui.theme.TextSecondary

@Composable
fun AppItemRow(
    app: AppInfo,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(CardBg)
            .border(1.dp, BorderSubtle, RoundedCornerShape(6.dp))
            .clickable(onClick = onToggle)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App icon container
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(BorderSubtle.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (app.isGame) Icons.Default.SportsEsports else Icons.Default.Widgets,
                contentDescription = null,
                tint = if (app.isGame) NyxtraAccent else TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = app.appName,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                if (app.isGame) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(NyxtraAccentSubtle)
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "GAME",
                            color = NyxtraAccent,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            Text(
                text = app.packageName,
                color = TextSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                maxLines = 1
            )
        }

        Checkbox(
            checked = app.isSelected,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(
                checkedColor = NyxtraAccent,
                checkmarkColor = Color.White,
                uncheckedColor = TextMuted
            )
        )
    }
}
