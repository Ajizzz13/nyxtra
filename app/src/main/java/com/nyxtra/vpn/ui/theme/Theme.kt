package com.nyxtra.vpn.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val NyxtraColorScheme = darkColorScheme(
    primary = NyxtraAccent,
    onPrimary = TextOnAccent,
    primaryContainer = NyxtraCardSelected,
    onPrimaryContainer = TextPrimary,
    secondary = NyxtraAccentVariant,
    onSecondary = TextOnAccent,
    background = NyxtraDark,
    onBackground = TextPrimary,
    surface = NyxtraSurface,
    onSurface = TextPrimary,
    surfaceVariant = NyxtraCard,
    onSurfaceVariant = TextSecondary,
    outline = NyxtraBorder
)

@Composable
fun NyxtraTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = NyxtraColorScheme,
        typography = Typography,
        content = content
    )
}
