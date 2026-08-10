package com.rrajath.milk.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf

// Settings screen's Appearance choice (PRD §10). SYSTEM defers to the OS.
enum class ThemeMode { SYSTEM, LIGHT, DARK }

private val LocalShoppColors = compositionLocalOf { ShoppLightColors }

// Dual-purpose like Material3's own `MaterialTheme`: call `ShoppTheme(mode) { }`
// to install it, read `ShoppTheme.colors` anywhere below that point.
object ShoppTheme {
    val colors: ShoppColors
        @Composable
        get() = LocalShoppColors.current
}

@Composable
fun ShoppTheme(mode: ThemeMode = ThemeMode.SYSTEM, content: @Composable () -> Unit) {
    val dark = when (mode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val colors = if (dark) ShoppDarkColors else ShoppLightColors

    // A Material3 scheme is still needed for any incidental Material
    // components (ripples, text field defaults); the app's own surfaces
    // read ShoppTheme.colors directly rather than MaterialTheme.colorScheme.
    val materialScheme = if (dark) {
        darkColorScheme(
            primary = colors.accent,
            onPrimary = colors.onAccent,
            background = colors.background,
            surface = colors.background,
            onBackground = colors.foreground,
            onSurface = colors.foreground,
        )
    } else {
        lightColorScheme(
            primary = colors.accent,
            onPrimary = colors.onAccent,
            background = colors.background,
            surface = colors.background,
            onBackground = colors.foreground,
            onSurface = colors.foreground,
        )
    }

    CompositionLocalProvider(LocalShoppColors provides colors) {
        MaterialTheme(colorScheme = materialScheme, typography = Typography, content = content)
    }
}
