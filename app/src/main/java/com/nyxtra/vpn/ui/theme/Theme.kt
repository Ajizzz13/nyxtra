package com.nyxtra.vpn.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val MinimalistColorScheme = darkColorScheme(
    primary = ActionPrimaryBg,
    onPrimary = ActionPrimaryText,
    primaryContainer = CardBg,
    onPrimaryContainer = TextPrimary,
    secondary = PastelCyan,
    onSecondary = TextPrimary,
    background = CanvasBg,
    onBackground = TextPrimary,
    surface = SurfaceBg,
    onSurface = TextPrimary,
    surfaceVariant = CardBg,
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle
)

@Composable
fun NyxtraTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MinimalistColorScheme,
        typography = Typography,
        content = content
    )
}
