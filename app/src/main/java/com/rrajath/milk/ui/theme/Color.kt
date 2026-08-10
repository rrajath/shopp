package com.rrajath.milk.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

// August 2026 rebrand: dark mode is charcoal + bright yellow, light mode is
// white + red. These values are the source of truth going forward (they
// intentionally diverge from the original internal-docs/design/Shopp
// Prototype.dc.html palette) — see docs/DESIGN_SYSTEM.md. Do not invent new
// colors; add a token there first.
@Immutable
data class ShoppColors(
    val background: Color,
    val foreground: Color,
    val muted: Color,
    val line: Color,
    val checkboxBorder: Color,
    val doneCheckboxFill: Color,
    val sheet: Color,
    val menu: Color,
    val chipBorder: Color,
    val scrim: Color,
    val toastBackground: Color,
    val toastForeground: Color,
    val toastAction: Color,
    val accent: Color,
    val onAccent: Color,
    val chipSelectedText: Color,
    // Text color for content drawn on a `foreground`-colored surface (e.g.
    // the Inbox chip's selected fill, which is `foreground` itself) --
    // `chipSelectedText` alone isn't safe there since it's light in dark
    // mode, same as `foreground` is light in dark mode.
    val onForeground: Color,
    val press: Color,
    val shadow: Color,
    // color(null) in the prototype: the Inbox section header / unlabelled tint.
    val inboxTint: Color,
    val labelPalette: List<Color>,
)

private val LightLabelPalette = listOf(
    Color(0xFF1E5FA8),
    Color(0xFF2E7D32),
    Color(0xFF6A1B9A),
    Color(0xFF00695C),
    Color(0xFFB45300),
    Color(0xFFAD1457),
)

private val DarkLabelPalette = listOf(
    Color(0xFF3D7DD8),
    Color(0xFF4CAF50),
    Color(0xFF9C4DCC),
    Color(0xFF26A69A),
    Color(0xFFE07B39),
    Color(0xFFD6487D),
)

val ShoppLightColors = ShoppColors(
    background = Color(0xFFFFFFFF),
    foreground = Color(0xFF1E1E1E),
    muted = Color(0xFF767676),
    line = Color(0xFFE4E4E4),
    checkboxBorder = Color(0xFFC4C4C4),
    doneCheckboxFill = Color(0xFFD8D8D8),
    sheet = Color(0xFFFFFFFF),
    menu = Color(0xFFFFFFFF),
    chipBorder = Color(0xFFDDDDDD),
    scrim = Color(0x6B1E1E1E), // rgba(30,30,30,.42)
    toastBackground = Color(0xFF1E1E1E),
    toastForeground = Color(0xFFFFFFFF),
    toastAction = Color(0xFFFFD400), // dark theme's accent, for contrast on the dark toast surface
    accent = Color(0xFFE53935),
    onAccent = Color(0xFFFFFFFF),
    chipSelectedText = Color(0xFFFFFFFF),
    onForeground = Color(0xFFFFFFFF),
    press = Color(0x0A000000), // rgba(0,0,0,.04)
    shadow = Color(0x2E1E1E1E), // rgba(30,30,30,.18)
    inboxTint = Color(0xFF1E1E1E),
    labelPalette = LightLabelPalette,
)

val ShoppDarkColors = ShoppColors(
    background = Color(0xFF212121),
    foreground = Color(0xFFF2F2F2),
    muted = Color(0xFFA0A0A0),
    line = Color(0xFF3A3A3A),
    checkboxBorder = Color(0xFF5C5C5C),
    doneCheckboxFill = Color(0xFF454545),
    sheet = Color(0xFF2A2A2A),
    menu = Color(0xFF2A2A2A),
    chipBorder = Color(0xFF4A4A4A),
    scrim = Color(0x99000000), // rgba(0,0,0,.6)
    toastBackground = Color(0xFFF2F2F2),
    toastForeground = Color(0xFF212121),
    toastAction = Color(0xFFE53935), // light theme's accent, for contrast on the light toast surface
    accent = Color(0xFFFFD400),
    onAccent = Color(0xFF212121),
    chipSelectedText = Color(0xFFFFFFFF),
    onForeground = Color(0xFF212121),
    press = Color(0x0DFFFFFF), // rgba(255,255,255,.05)
    shadow = Color(0x99000000), // rgba(0,0,0,.6)
    inboxTint = Color(0xFFA0A0A0),
    labelPalette = DarkLabelPalette,
)
