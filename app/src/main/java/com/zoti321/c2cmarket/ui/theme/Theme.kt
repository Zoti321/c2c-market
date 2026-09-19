package com.zoti321.c2cmarket.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    secondary = SecondaryBlue,
    tertiary = AccentGreen,
    onTertiary = Color.White,
    background = BackgroundLight,
    onBackground = ForegroundBlue,
    surface = CardWhite,
    onSurface = ForegroundBlue,
    surfaceVariant = Color(0xFFE9EFF8),
    onSurfaceVariant = MutedForeground,
    outline = BorderBlue,
    error = DestructiveRed,
    onError = Color.White,
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlueDark,
    onPrimary = Color.White,
    secondary = SecondaryBlue,
    tertiary = AccentGreen,
    onTertiary = Color.White,
    background = BackgroundDark,
    onBackground = Color(0xFFE2E8F0),
    surface = SurfaceDark,
    onSurface = Color(0xFFE2E8F0),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF475569),
    error = DestructiveRed,
    onError = Color.White,
)

@Composable
fun C2cmarketTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
