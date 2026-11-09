package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = RevOrange,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF451A03),
    onPrimaryContainer = Color(0xFFFFD8A8),
    secondary = TurboCyan,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF083344),
    onSecondaryContainer = Color(0xFFA5F3FC),
    tertiary = NeonYellow,
    onTertiary = Color.Black,
    background = CarbonBlack,
    onBackground = TextPrimary,
    surface = CarbonDark,
    onSurface = TextPrimary,
    surfaceVariant = CarbonCard,
    onSurfaceVariant = TextSecondary,
    outline = CarbonBorder,
    error = ApexRed,
    onError = Color.White
)

@Composable
fun TunidexTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
