package com.rrajath.shopp.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

// August 2026: literal transcription of the Organic design system shipped in
// internal-docs/website/design_handoff_shopp_site/design/_ds/organic-*/
// styles.css (tokens) and ShoppApp.dc.html (component usage), which now
// includes the light-mode stylesheet that earlier sessions couldn't find --
// see docs/DESIGN_SYSTEM.md. Both themes below are now literal, not
// reconstructed. Do not invent new colors; add a token there first.
@Immutable
data class ShoppColors(
    // The phone frame's own background in ShoppApp.dc.html is
    // var(--color-neutral-100), not var(--color-bg) -- there's no "page
    // behind the phone" in the real app, so the frame's background *is* the
    // app's background.
    val background: Color,
    val foreground: Color,
    // Not one single literal token -- the mockup uses several different
    // color-mix(text, N%) opacities contextually (45/50/62/70%). Anchored
    // instead on Organic's own general-purpose `.text-muted` /
    // `figcaption` utility: color-mix(text, 55%, transparent).
    val muted: Color,
    // var(--color-divider): color-mix(text, 16%/18%, transparent).
    val line: Color,
    // List row checkbox border, literal: color-mix(text, 28%, transparent).
    val checkboxBorder: Color,
    val doneCheckboxFill: Color,
    // Quick Add card / suggestions popovers: var(--color-neutral-100),
    // identical to `background` -- elevation reads via shadow only, not a
    // lighter/darker fill (see ShoppApp.dc.html). The Merge dialog's own
    // card literally uses var(--color-surface) instead; LabelManagementSheet
    // reuses this token for that case too (already a documented deviation --
    // it stays a bottom sheet, not the prototype's centered modal card).
    val sheet: Color,
    val menu: Color,
    // Fill (not a stroke) for an unselected label chip -- var(--color-neutral-200).
    val chipUnselectedFill: Color,
    // Text on an unselected chip -- var(--color-neutral-800), not `foreground`.
    val chipUnselectedText: Color,
    // Quick Add / Drawer scrim: color-mix(neutral-900, 32%, transparent).
    // The Merge dialog's own scrim is literally 46%; LabelManagementSheet
    // reuses this 32% value for both of its scrims rather than adding a
    // second token for one sub-mode.
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
    // The prototype's `color(null)` for the Inbox section: a literal
    // hardcoded hex in its script (not a CSS var), identical in both themes.
    val inboxTint: Color,
    val labelPalette: List<Color>,
)

// Flat 15-color pastel palette (August 2026, user request -- deviates from
// ShoppApp.dc.html's literal 10-color PALETTE constant), used identically
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

// Literal, from organic-*/styles.css's :root block.
val ShoppLightColors = ShoppColors(
    background = Color(0xFFF9F4ED), // --color-neutral-100
    foreground = Color(0xFF201E1D), // --color-text
    muted = Color(0x8C201E1D), // --color-text @ 55%
    line = Color(0x29201E1D), // --color-divider: --color-text @ 16%
    checkboxBorder = Color(0x47201E1D), // --color-text @ 28%
    doneCheckboxFill = Color(0xFFCFC8BE), // not depicted by the new prototype -- kept from the old one
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
    press = Color(0x0A000000), // rgba(0,0,0,.04) -- not specified by the prototype (no touch states), kept from before
    shadow = Color(0x382E2B25), // --shadow-lg: --color-neutral-900 @ 22%
    inboxTint = Color(0xFFC0B6A5), // literal hardcoded '#c0b6a5' in ShoppApp.dc.html's script
    labelPalette = LabelPalette,
)

// Literal, from ShoppApp.dc.html's [data-theme="dark"] block.
val ShoppDarkColors = ShoppColors(
    background = Color(0xFF2E2B25), // --color-neutral-100 (dark override)
    foreground = Color(0xFFF2E9DA), // --color-text
    muted = Color(0x8CF2E9DA), // --color-text @ 55%
    line = Color(0x2EF2E9DA), // --color-divider: --color-text @ 18%
    checkboxBorder = Color(0x47F2E9DA), // --color-text @ 28%
    doneCheckboxFill = Color(0xFF645C50), // not depicted by the new prototype -- kept from the old one
    sheet = Color(0xFF2E2B25), // --color-neutral-100
    menu = Color(0xFF2E2B25),
    chipUnselectedFill = Color(0xFF474238), // --color-neutral-200
    chipUnselectedText = Color(0xFFEEE7DB), // --color-neutral-800
    scrim = Color(0x52F9F4ED), // --color-neutral-900 (dark override) @ 32% -- a light veil, not a black dim
    toastBackground = Color(0xFFF9F4ED), // --color-neutral-900 (toast inverts: light bg in dark mode)
    toastForeground = Color(0xFF474238), // --color-neutral-200
    toastAction = Color(0xFFB2622D), // --color-accent-400
    accent = Color(0xFFF6A06B), // --color-accent
    onAccent = Color(0xFF211E19), // --color-bg
    chipSelectedText = Color(0xFF211E19),
    press = Color(0x0DFFFFFF), // rgba(255,255,255,.05) -- not specified by the prototype, kept from before
    shadow = Color(0x99000000), // --shadow-lg: rgba(0,0,0,.6)
    inboxTint = Color(0xFFC0B6A5), // same literal hardcoded hex as light -- the prototype doesn't theme this one
    labelPalette = LabelPalette,
)
