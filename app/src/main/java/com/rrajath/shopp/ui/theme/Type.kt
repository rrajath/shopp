package com.rrajath.shopp.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.rrajath.shopp.R

// August 2026: swapped from Newsreader/Roboto to Caprasimo/Figtree, matching
// ShoppApp.dc.html's `var(--font-heading)` / `var(--font-body)` (see
// docs/DESIGN_SYSTEM.md). Both are self-hosted open-license (OFL) fonts, the
// same pattern as the Newsreader bundling they replace.
val CaprasimoFamily = FontFamily(
    Font(resId = R.font.caprasimo_regular, weight = FontWeight.Normal),
)

// Caprasimo only ships one weight (400) -- the prototype never asks for
// heading emphasis beyond that.
val FigtreeFamily = FontFamily(
    Font(resId = R.font.figtree_light, weight = FontWeight.Light),
    Font(resId = R.font.figtree_regular, weight = FontWeight.Normal),
    Font(resId = R.font.figtree_medium, weight = FontWeight.Medium),
    Font(resId = R.font.figtree_semibold, weight = FontWeight.SemiBold),
    Font(resId = R.font.figtree_bold, weight = FontWeight.Bold),
)

// Named text styles. Where ShoppApp.dc.html depicts the element literally,
// size/weight/spacing are transcribed 1:1 from its inline styles; where it
// doesn't (Drawer, Labels screen rows, Recently Completed, Settings toggle
// rows), only the font family changed and the old prototype's numbers are
// kept -- see docs/DESIGN_SYSTEM.md. Colors are applied by callers from
// ShoppColors, not baked in here.
object ShoppType {
    // App bar title ("Shopp" / "Settings"): `font-family: var(--font-heading);
    // font-size: 21px; letter-spacing: -0.02em`.
    val screenTitle = TextStyle(
        fontFamily = CaprasimoFamily, fontWeight = FontWeight.Normal,
        fontSize = 21.sp, lineHeight = 21.sp, letterSpacing = (-0.02).em(21.sp),
    )

    // List section header micro-label ("INBOX" / "@COSTCO"): `font-size:
    // 12px; font-weight: 700; letter-spacing: 0.08em; text-transform:
    // uppercase` -- shared with Settings' "THEME"/"LABELS" labels below,
    // which use the identical treatment.
    val sectionHeader = TextStyle(
        fontFamily = FigtreeFamily, fontWeight = FontWeight.Bold,
        fontSize = 12.sp, lineHeight = 12.sp, letterSpacing = 0.08.em(12.sp),
    )

    // Empty state primary line ("Nothing on the belt."): `font-size: 16px`,
    // no special weight -- regular. Left-aligned, not centered.
    val emptyTitle = TextStyle(
        fontFamily = FigtreeFamily, fontWeight = FontWeight.Normal,
        fontSize = 16.sp, lineHeight = 21.sp,
    )
    // Secondary hint line under the empty state -- not in the prototype
    // (which has only one line); kept smaller and muted.
    val emptyBody = TextStyle(
        fontFamily = FigtreeFamily, fontWeight = FontWeight.Light,
        fontSize = 14.sp, lineHeight = 21.sp,
    )

    // Drawer isn't depicted by the new prototype -- font family only.
    val drawerTitle = TextStyle(
        fontFamily = CaprasimoFamily, fontWeight = FontWeight.Normal,
        fontSize = 26.sp, lineHeight = 26.sp, letterSpacing = 0.02.em(26.sp),
    )

    // List row title: `font-size: 16px; line-height: 1.35`, no weight set -- regular.
    val itemText = TextStyle(
        fontFamily = FigtreeFamily, fontWeight = FontWeight.Normal,
        fontSize = 16.sp, lineHeight = 21.6.sp,
    )
    val itemTextDone = TextStyle(
        fontFamily = FigtreeFamily, fontWeight = FontWeight.Normal,
        fontSize = 16.sp, lineHeight = 21.6.sp,
    )

    val fabLabel = TextStyle(
        fontFamily = FigtreeFamily, fontWeight = FontWeight.Medium,
        fontSize = 15.sp, lineHeight = 15.sp,
    )

    // Undo toast: text `font-size: 14px` (no weight -- regular); action
    // `font-size: 12px; font-weight: 700; letter-spacing: 0.08em; uppercase`.
    val toastText = TextStyle(
        fontFamily = FigtreeFamily, fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 14.sp,
    )
    val toastAction = TextStyle(
        fontFamily = FigtreeFamily, fontWeight = FontWeight.Bold,
        fontSize = 12.sp, lineHeight = 12.sp, letterSpacing = 0.08.em(12.sp),
    )

    // Chip row: selected `font-size: 12.5px; font-weight: 600`; unselected
    // `font-size: 12.5px`, no weight (regular).
    val chipSelected = TextStyle(
        fontFamily = FigtreeFamily, fontWeight = FontWeight.SemiBold, fontSize = 12.5.sp, lineHeight = 12.5.sp,
    )
    val chipUnselected = TextStyle(
        fontFamily = FigtreeFamily, fontWeight = FontWeight.Normal, fontSize = 12.5.sp, lineHeight = 12.5.sp,
    )

    // Label/title suggestion popover rows: `font-size: 15px`, no weight (regular).
    val suggestionText = TextStyle(
        fontFamily = FigtreeFamily, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 15.sp,
    )
    val sessionAddText = TextStyle(
        fontFamily = FigtreeFamily, fontWeight = FontWeight.Light, fontSize = 15.sp, lineHeight = 15.sp,
    )

    // Quick Add draft textarea (and reused for the label-rename field):
    // `font-size: 15px; line-height: 1.5`, no weight (regular).
    val quickAddInput = TextStyle(
        fontFamily = FigtreeFamily, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 22.5.sp,
    )

    // Settings section label ("THEME" / "LABELS"): identical treatment to
    // sectionHeader above (`font-size: 12px; font-weight: 700; letter-spacing:
    // 0.08em; uppercase`) -- kept as a separate name since the two screens
    // apply it to differently-shaped containers.
    val settingsSectionLabel = TextStyle(
        fontFamily = FigtreeFamily, fontWeight = FontWeight.Bold,
        fontSize = 12.sp, lineHeight = 12.sp, letterSpacing = 0.08.em(12.sp),
    )
    // Theme segmented control option: `font-size: 14px`; the selected
    // segment adds `font-weight: 600` at the call site (ThemeSegment), since
    // only that state needs the heavier weight.
    val settingsButtonLabel = TextStyle(
        fontFamily = FigtreeFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 14.sp,
    )
    // Merge dialog's Cancel/Merge (and reused for the rename sheet's
    // Cancel/Save): `font-family: var(--font-heading)`, matching the
    // Organic system's shared `.btn` class, `font-size: 14px`.
    val dialogActionLabel = TextStyle(
        fontFamily = CaprasimoFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 14.sp,
    )
    // Merge target row name: `font-size: 15px`, no weight (regular) -- distinct
    // from labelName (18px Light), which is the Labels screen's own row style.
    val mergeTargetLabel = TextStyle(
        fontFamily = FigtreeFamily, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 15.sp,
    )

    val toggleName = TextStyle(
        fontFamily = FigtreeFamily, fontWeight = FontWeight.Light, fontSize = 17.sp, lineHeight = 22.1.sp,
    )
    val toggleHint = TextStyle(
        fontFamily = FigtreeFamily, fontWeight = FontWeight.Light, fontSize = 13.sp, lineHeight = 18.2.sp,
    )
    val footerNote = TextStyle(
        fontFamily = FigtreeFamily, fontWeight = FontWeight.Light, fontSize = 13.sp, lineHeight = 20.8.sp,
    )
    val drawerMenuItem = TextStyle(
        fontFamily = FigtreeFamily, fontWeight = FontWeight.Light, fontSize = 17.sp, lineHeight = 22.1.sp,
    )
    val drawerMenuCount = TextStyle(
        fontFamily = FigtreeFamily, fontWeight = FontWeight.Light, fontSize = 14.sp, lineHeight = 14.sp,
    )
    val labelName = TextStyle(
        fontFamily = FigtreeFamily, fontWeight = FontWeight.Light, fontSize = 18.sp, lineHeight = 23.4.sp,
    )
    val labelCount = TextStyle(
        fontFamily = FigtreeFamily, fontWeight = FontWeight.Light, fontSize = 15.sp, lineHeight = 15.sp,
    )
    val labelTag = TextStyle(
        fontFamily = FigtreeFamily, fontWeight = FontWeight.Light, fontSize = 13.sp, lineHeight = 13.sp,
    )
    val clearAction = TextStyle(
        fontFamily = FigtreeFamily, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 13.sp,
    )
}

// Helper for the prototype's `em`-based letter-spacing at a given font size,
// since Compose's letterSpacing wants a concrete sp value, not a relative em.
private fun Double.em(fontSize: androidx.compose.ui.unit.TextUnit): androidx.compose.ui.unit.TextUnit =
    (fontSize.value * this).sp

// Material3 still wants a Typography for any incidental Material components;
// the app's own text uses ShoppType above, not this.
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FigtreeFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
    )
)
