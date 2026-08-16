package com.rrajath.shopp.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

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

    // `enableEdgeToEdge()` in the Activity only sets icon contrast once, at
    // launch, based on the *system* theme -- it doesn't know about this
    // screen's own ThemeMode override (Settings can force Light while the
    // system is in Dark, or vice versa), so status/nav bar icons went white
    // on a light background in that case. Re-derive contrast here every
    // time `dark` changes instead.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !dark
            controller.isAppearanceLightNavigationBars = !dark
        }
    }

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
