package com.example.detectiveapp.view.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DetectiveNoirColorScheme = darkColorScheme(
    primary = DetectiveGold,
    onPrimary = DetectiveBlack,
    primaryContainer = DetectiveGray,
    onPrimaryContainer = DetectiveText,
    secondary = DetectiveAmber,
    onSecondary = DetectiveBlack,
    background = DetectiveBlack,
    onBackground = DetectiveText,
    surface = DetectiveCharcoal,
    onSurface = DetectiveText,
    surfaceVariant = DetectiveGray,
    onSurfaceVariant = Color(0xFF94A3B8),
    error = DetectiveRed,
    onError = Color.White
)

@Composable
fun DetectiveAppTheme(
    darkTheme: Boolean = true, // Forzamos el modo Noir Detective por defecto para cumplir con la petición
    content: @Composable () -> Unit
) {
    // Usamos el esquema de color Noir Detective para una experiencia inmersiva
    MaterialTheme(
        colorScheme = DetectiveNoirColorScheme,
        typography = Typography,
        content = content
    )
}
