package com.rrajath.milk.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

// Every value here is transcribed verbatim from
// internal-docs/design/Shopp Prototype.dc.html's `renderVals()` — see
// docs/DESIGN_SYSTEM.md. Do not invent new colors; add a token there first.
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
    val press: Color,
    val shadow: Color,
    // color(null) in the prototype: the Inbox section header / unlabelled tint.
    val inboxTint: Color,
    val labelPalette: List<Color>,
)

private val LightLabelPalette = listOf(
    Color(0xFFB0442A),
    Color(0xFF3F6B44),
    Color(0xFF3C6382),
    Color(0xFF7A5C2E),
    Color(0xFF6B4A75),
    Color(0xFF2F6B6B),
)

private val DarkLabelPalette = listOf(
    Color(0xFFE58C6B),
    Color(0xFF86B889),
    Color(0xFF87ADCB),
    Color(0xFFC9A567),
    Color(0xFFBE9BC7),
    Color(0xFF7FBDBD),
)

val ShoppLightColors = ShoppColors(
    background = Color(0xFFFCFAF7),
    foreground = Color(0xFF2A2724),
    muted = Color(0xFF8A847C),
    line = Color(0xFFE4DED5),
    checkboxBorder = Color(0xFFB3AEA5),
    doneCheckboxFill = Color(0xFFCFC8BE),
    sheet = Color(0xFFFFFFFF),
    menu = Color(0xFFFFFFFF),
    chipBorder = Color(0xFFE2DBD1),
    scrim = Color(0x6B1E1A17), // rgba(30,26,23,.42)
    toastBackground = Color(0xFF2A2724),
    toastForeground = Color(0xFFFCFAF7),
    toastAction = Color(0xFFE9A98D),
    accent = Color(0xFF8C4A32),
    onAccent = Color(0xFFFFFFFF),
    chipSelectedText = Color(0xFFFFFFFF),
    press = Color(0x0A000000), // rgba(0,0,0,.04)
    shadow = Color(0x2E2A2724), // rgba(42,39,36,.18)
    inboxTint = Color(0xFF2A2724),
    labelPalette = LightLabelPalette,
)

val ShoppDarkColors = ShoppColors(
    background = Color(0xFF14110F),
    foreground = Color(0xFFF0EAE4),
    muted = Color(0xFF8E8579),
    line = Color(0xFF332C27),
    checkboxBorder = Color(0xFF564E48),
    doneCheckboxFill = Color(0xFF4A423C),
    sheet = Color(0xFF241D19),
    menu = Color(0xFF241D19),
    chipBorder = Color(0xFF4A3E37),
    scrim = Color(0x94000000), // rgba(0,0,0,.58)
    toastBackground = Color(0xFFEFE7E0),
    toastForeground = Color(0xFF241D19),
    toastAction = Color(0xFF8C4A32),
    accent = Color(0xFFE08A63),
    onAccent = Color(0xFF241811),
    chipSelectedText = Color(0xFFF3D9CD),
    press = Color(0x0DFFFFFF), // rgba(255,255,255,.05)
    shadow = Color(0x99000000), // rgba(0,0,0,.6)
    inboxTint = Color(0xFF8E8579),
    labelPalette = DarkLabelPalette,
)
