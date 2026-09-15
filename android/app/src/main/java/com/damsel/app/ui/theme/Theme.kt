package com.damsel.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Spec §56: deep blacks, soft whites, a restrained lavender accent — no neon, no clutter.
private val DamselLavender = Color(0xFFB79BFF)
private val DamselInk = Color(0xFF0E0B16)
private val DamselSurface = Color(0xFF171223)
private val DamselPaper = Color(0xFFF4F2FA)

private val DamselDarkColors = darkColorScheme(
    primary = DamselLavender,
    onPrimary = DamselInk,
    background = DamselInk,
    onBackground = DamselPaper,
    surface = DamselSurface,
    onSurface = DamselPaper,
    surfaceVariant = Color(0xFF221C33),
    onSurfaceVariant = Color(0xFFB6ADC9)
)

private val DamselLightColors = lightColorScheme(
    primary = Color(0xFF6C4FCE),
    onPrimary = Color.White,
    background = DamselPaper,
    onBackground = DamselInk,
    surface = Color.White,
    onSurface = DamselInk,
    surfaceVariant = Color(0xFFEAE5F5),
    onSurfaceVariant = Color(0xFF5B5470)
)

@Composable
fun DamselTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DamselDarkColors else DamselLightColors,
        typography = DamselTypography,
        content = content
    )
}
