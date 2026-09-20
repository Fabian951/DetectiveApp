package com.example.detectiveapp.view.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = DetectiveNavyPrimary,
    secondary = DetectiveGoldSecondary,
    background = Color(0xFF0F172A),
    surface = Color(0xFF1E293B)
)

private val LightColorScheme = lightColorScheme(
    primary = DetectiveNavyPrimary,
    secondary = DetectiveGoldSecondary,
    background = DetectiveBackground,
    surface = DetectiveSurface
)

@Composable
fun DetectiveAppTheme(
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