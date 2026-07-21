package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = QuranSecondary,
    onPrimary = Color(0xFF101917),
    secondary = QuranPrimaryLight,
    onSecondary = Color.White,
    tertiary = QuranTertiary,
    onTertiary = Color(0xFF101917),
    background = QuranBgDark,
    onBackground = Color(0xFFFAF6EE),
    surface = QuranSurfaceDark,
    onSurface = Color(0xFFFAF6EE),
    error = TajweedRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = QuranPrimary,
    onPrimary = Color.White,
    secondary = QuranSecondary,
    onSecondary = Color.White,
    tertiary = QuranTertiary,
    onTertiary = Color(0xFF101917),
    background = QuranBgLight,
    onBackground = Color(0xFF1B2321),
    surface = QuranSurfaceLight,
    onSurface = Color(0xFF1B2321),
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
