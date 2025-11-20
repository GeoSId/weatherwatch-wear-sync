package com.weather.wearable.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Colors
import androidx.compose.ui.graphics.Color

private val DarkColorPalette = Colors(
    primary = Color(0xFF75E6DA),
    primaryVariant = Color(0xFF189AB4),
    secondary = Color(0xFF05445E),
    background = Color.Black,
    surface = Color(0xFF1A1A1A),
    error = Color(0xFFFF6B6B),
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    onError = Color.Black
)

@Composable
fun WearableWeatherTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = DarkColorPalette,
        content = content
    )
} 