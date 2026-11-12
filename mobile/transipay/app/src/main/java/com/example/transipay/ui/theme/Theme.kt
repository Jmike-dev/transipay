package com.example.transipay.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Orange600,
    onPrimary = Color.White,
    primaryContainer = Orange100,
    onPrimaryContainer = Orange900,

    secondary = Slate700,
    onSecondary = Color.White,
    secondaryContainer = Slate200,
    onSecondaryContainer = Slate900,

    tertiary = Sky600,
    onTertiary = Color.White,
    tertiaryContainer = Sky100,
    onTertiaryContainer = Sky900,

    background = Slate50,
    onBackground = Slate900,
    surface = Color.White,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate500, // neutral text
    outline = Slate300,
    outlineVariant = Slate200,
)

// Light mode only: the system dark setting is ignored and
// dynamic (wallpaper) colour is off, so brand colours always apply.
@Composable
fun TransipayTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = Typography, // from your existing Type.kt
        content = content
    )
}