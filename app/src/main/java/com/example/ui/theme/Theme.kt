package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = QuranGoldTertiary,
    onPrimary = Color(0xFF18140C),
    primaryContainer = Color(0xFF382A05),
    onPrimaryContainer = Color(0xFFFFF0C2),
    secondary = QuranGoldPrimaryLight,
    onSecondary = Color(0xFF18140C),
    secondaryContainer = Color(0xFF13362A),
    onSecondaryContainer = Color(0xFFD0F0E4),
    tertiary = QuranEmeraldSecondary,
    onTertiary = Color.White,
    background = QuranBgDark,
    onBackground = Color(0xFFFAF6EA),
    surface = QuranSurfaceDark,
    onSurface = Color(0xFFFAF6EA),
    error = TajweedRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = QuranGoldPrimary,
    onPrimary = Color.White,
    primaryContainer = QuranGoldContainer,
    onPrimaryContainer = QuranOnGoldContainer,
    secondary = QuranEmeraldSecondary,
    onSecondary = Color.White,
    secondaryContainer = QuranEmeraldContainer,
    onSecondaryContainer = Color(0xFF042D20),
    tertiary = QuranGoldPrimaryLight,
    onTertiary = Color.White,
    background = QuranBgLight,
    onBackground = Color(0xFF1C180E),
    surface = QuranSurfaceLight,
    onSurface = Color(0xFF1C180E),
    error = TajweedRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

