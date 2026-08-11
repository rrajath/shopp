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

// August 2026: expanded from 6 to 15 (user request, for the Labels "Edit"
// color picker) -- muted/pleasant rather than bright, index-parallel across
// light/dark like the original 6 (same hue family per index, light mode
// deepened for contrast on white, dark mode lightened for contrast on
// charcoal). See docs/DESIGN_SYSTEM.md.
private val LightLabelPalette = listOf(
    Color(0xFFB15D66), // dusty rose
    Color(0xFFB35A38), // terracotta
    Color(0xFFA67C1E), // mustard
    Color(0xFF6E7A3D), // olive
    Color(0xFF4C7A4A), // sage
    Color(0xFF3D8A75), // seafoam
    Color(0xFF2E7A78), // teal
    Color(0xFF3E689A), // steel blue
    Color(0xFF5259AD), // periwinkle
    Color(0xFF7455A0), // lavender
    Color(0xFF8C4F80), // mauve
    Color(0xFFB36A46), // dusty peach
    Color(0xFF556072), // slate
    Color(0xFF71624F), // warm gray
    Color(0xFF99503B), // clay
)

private val DarkLabelPalette = listOf(
    Color(0xFFD98D96), // dusty rose
    Color(0xFFE0916A), // terracotta
    Color(0xFFD9AC4E), // mustard
    Color(0xFFA8B571), // olive
    Color(0xFF8FBF86), // sage
    Color(0xFF7FD1B9), // seafoam
    Color(0xFF6FBDBA), // teal
    Color(0xFF7FAAD6), // steel blue
    Color(0xFF9BA3E8), // periwinkle
    Color(0xFFB99CDB), // lavender
    Color(0xFFCB9AC0), // mauve
    Color(0xFFE3AE8B), // dusty peach
    Color(0xFF9FADC2), // slate
    Color(0xFFBBA98E), // warm gray
    Color(0xFFD68F73), // clay
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
    background = Color(0xFF0F1723),
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
    accent = Color(0xFFE0C069),
    onAccent = Color(0xFF212121),
    chipSelectedText = Color(0xFFFFFFFF),
    onForeground = Color(0xFF212121),
    press = Color(0x0DFFFFFF), // rgba(255,255,255,.05)
    shadow = Color(0x99000000), // rgba(0,0,0,.6)
    inboxTint = Color(0xFFA0A0A0),
    labelPalette = DarkLabelPalette,
)
