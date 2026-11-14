package com.rrajath.shopp.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

// App color tokens. See docs/DESIGN_SYSTEM.md for what each one is for.
// Do not invent new colors; add a token there first.
@Immutable
data class ShoppColors(
    // The app's page background (--color-neutral-100, not --color-bg).
    val background: Color,
    val foreground: Color,
    // General-purpose muted text: text color at 55% opacity.
    val muted: Color,
    // var(--color-divider): color-mix(text, 16%/18%, transparent).
    val line: Color,
    // List row checkbox border, literal: color-mix(text, 28%, transparent).
    val checkboxBorder: Color,
    val doneCheckboxFill: Color,
    // Quick Add card / suggestions popovers: identical to `background` --
    // elevation reads via shadow only, not a lighter/darker fill.
    // LabelManagementSheet (a bottom sheet) reuses this token too.
    val sheet: Color,
    val menu: Color,
    // Fill (not a stroke) for an unselected label chip -- var(--color-neutral-200).
    val chipUnselectedFill: Color,
    // Text on an unselected chip -- var(--color-neutral-800), not `foreground`.
    val chipUnselectedText: Color,
    // Quick Add / Drawer scrim: neutral-900 at 32%. LabelManagementSheet
    // reuses this value for both of its scrims rather than adding a second
    // token for one sub-mode.
    val scrim: Color,
    val toastBackground: Color,
    val toastForeground: Color,
    val toastAction: Color,
    // var(--color-accent) itself (the base token, not an -N00 ramp rung).
    val accent: Color,
    // var(--color-bg) -- the text/icon color drawn on an accent-filled
    // surface (FAB, selected chip, selected segment, dialog primary button).
    // Distinct from `background`, which is var(--color-neutral-100).
    val onAccent: Color,
    val chipSelectedText: Color,
    val press: Color,
    val shadow: Color,
    // Section tint for Inbox, which has no palette color; identical in both
    // themes.
    val inboxTint: Color,
    val labelPalette: List<Color>,
)

// Flat 15-color pastel palette, used identically
// in both themes. Same list backs auto-allocation (LabelColorAllocator) and
// the manual picker (LabelManagementSheet's ColorSwatchGrid) -- one index,
// one palette, everywhere a label's color is drawn.
private val LabelPalette = listOf(
    Color(0xFFE8A0A0), // rose
    Color(0xFFE8B88A), // orange
    Color(0xFFE8CC8A), // amber
    Color(0xFFDCE08A), // yellow-green
    Color(0xFFB8E08A), // lime
    Color(0xFF8FDB9E), // green
    Color(0xFF8AD9C2), // teal
    Color(0xFF8AD0E0), // cyan
    Color(0xFF8AB8E0), // sky blue
    Color(0xFF8A9FE0), // blue
    Color(0xFFA08AE0), // indigo
    Color(0xFFC28AE0), // purple
    Color(0xFFE08AD0), // magenta
    Color(0xFFE08AB8), // pink
    Color(0xFFC2A98A), // taupe
)

// Light theme.
val ShoppLightColors = ShoppColors(
    background = Color(0xFFF9F4ED), // --color-neutral-100
    foreground = Color(0xFF201E1D), // --color-text
    muted = Color(0x8C201E1D), // --color-text @ 55%
    line = Color(0x29201E1D), // --color-divider: --color-text @ 16%
    checkboxBorder = Color(0x47201E1D), // --color-text @ 28%
    doneCheckboxFill = Color(0xFFCFC8BE),
    sheet = Color(0xFFF9F4ED), // --color-neutral-100
    menu = Color(0xFFF9F4ED),
    chipUnselectedFill = Color(0xFFEEE7DB), // --color-neutral-200
    chipUnselectedText = Color(0xFF474238), // --color-neutral-800
    scrim = Color(0x522E2B25), // --color-neutral-900 @ 32%
    toastBackground = Color(0xFF2E2B25), // --color-neutral-900
    toastForeground = Color(0xFFEEE7DB), // --color-neutral-200
    toastAction = Color(0xFFF6A06B), // --color-accent-400
    accent = Color(0xFFC67139), // --color-accent
    onAccent = Color(0xFFF5EAD8), // --color-bg
    chipSelectedText = Color(0xFFF5EAD8), // --color-bg
    press = Color(0x0A000000), // rgba(0,0,0,.04)
    shadow = Color(0x382E2B25), // --shadow-lg: --color-neutral-900 @ 22%
    inboxTint = Color(0xFFC0B6A5),
    labelPalette = LabelPalette,
)

// Dark theme.
val ShoppDarkColors = ShoppColors(
    background = Color(0xFF2E2B25), // --color-neutral-100 (dark override)
    foreground = Color(0xFFF2E9DA), // --color-text
    muted = Color(0x8CF2E9DA), // --color-text @ 55%
    line = Color(0x2EF2E9DA), // --color-divider: --color-text @ 18%
    checkboxBorder = Color(0x47F2E9DA), // --color-text @ 28%
    doneCheckboxFill = Color(0xFF645C50),
    sheet = Color(0xFF2E2B25), // --color-neutral-100
    menu = Color(0xFF2E2B25),
    chipUnselectedFill = Color(0xFF474238), // --color-neutral-200
    chipUnselectedText = Color(0xFFEEE7DB), // --color-neutral-800
    scrim = Color(0x66000000), // black @ 40%: neutral-900 @ 32% is the dark background's own color, so it didn't visibly dim anything
    toastBackground = Color(0xFFF9F4ED), // --color-neutral-900 (toast inverts: light bg in dark mode)
    toastForeground = Color(0xFF474238), // --color-neutral-200
    toastAction = Color(0xFFB2622D), // --color-accent-400
    accent = Color(0xFFF6A06B), // --color-accent
    onAccent = Color(0xFF211E19), // --color-bg
    chipSelectedText = Color(0xFF211E19),
    press = Color(0x0DFFFFFF), // rgba(255,255,255,.05)
    shadow = Color(0x99000000), // --shadow-lg: rgba(0,0,0,.6)
    inboxTint = Color(0xFFC0B6A5), // same as light -- not themed
    labelPalette = LabelPalette,
)
