package com.nyxtra.vpn.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = NyxtraNeonGreen,
    onPrimary = TextOnAccent,
    primaryContainer = NyxtraSurfaceVariant,
    onPrimaryContainer = NyxtraNeonGreen,
    secondary = NyxtraCyberBlue,
    onSecondary = TextOnAccent,
    background = NyxtraBackground,
    onBackground = TextPrimary,
    surface = NyxtraSurface,
    onSurface = TextPrimary,
    surfaceVariant = NyxtraSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    error = NyxtraCrimson,
    onError = TextPrimary
)

@Composable
fun NyxtraTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
