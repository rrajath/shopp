package com.rrajath.shopp.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.rrajath.shopp.designsystem.R

// Heading and body fonts (see docs/DESIGN_SYSTEM.md). Both are self-hosted
// open-license (OFL) fonts.
val CaprasimoFamily = FontFamily(
    Font(resId = R.font.caprasimo_regular, weight = FontWeight.Normal),
)

// Caprasimo only ships one weight (400) -- headings never need more
// emphasis than that.
val FigtreeFamily = FontFamily(
    Font(resId = R.font.figtree_light, weight = FontWeight.Light),
    Font(resId = R.font.figtree_regular, weight = FontWeight.Normal),
    Font(resId = R.font.figtree_medium, weight = FontWeight.Medium),
    Font(resId = R.font.figtree_semibold, weight = FontWeight.SemiBold),
    Font(resId = R.font.figtree_bold, weight = FontWeight.Bold),
)

// Named text styles (see docs/DESIGN_SYSTEM.md). Colors are applied by
// callers from ShoppColors, not baked in here.
object ShoppType {
    // App bar title ("Shopp" / "Settings").
    val screenTitle = TextStyle(
        fontFamily = CaprasimoFamily, fontWeight = FontWeight.Normal,
        fontSize = 21.sp, lineHeight = 21.sp, letterSpacing = (-0.02).em(21.sp),
    )

    // List section header micro-label ("INBOX" / "@COSTCO"): bold, tracked,
    // uppercase -- shared with Settings' "THEME"/"LABELS" labels, which use
    // the identical treatment.
    val sectionHeader = TextStyle(
        fontFamily = FigtreeFamily, fontWeight = FontWeight.Bold,
        fontSize = 12.sp, lineHeight = 12.sp, letterSpacing = 0.08.em(12.sp),
    )

    // Empty state primary line ("Nothing on the belt."): regular weight,
    // left-aligned, not centered.
    val emptyTitle = TextStyle(
        fontFamily = FigtreeFamily, fontWeight = FontWeight.Normal,
        fontSize = 20.sp, lineHeight = 24.sp,
    )
    // Secondary hint line under the empty state; smaller and muted.
    val emptyBody = TextStyle(
        fontFamily = FigtreeFamily, fontWeight = FontWeight.Light,
        fontSize = 14.sp, lineHeight = 21.sp,
    )

    // Drawer title.
    val drawerTitle = TextStyle(
        fontFamily = CaprasimoFamily, fontWeight = FontWeight.Normal,
        fontSize = 26.sp, lineHeight = 26.sp, letterSpacing = 0.02.em(26.sp),
    )

    // List row title: 18sp with a 1.35 line-height ratio.
    val itemText = TextStyle(
        fontFamily = FigtreeFamily, fontWeight = FontWeight.Normal,
        fontSize = 18.sp, lineHeight = 24.3.sp,
    )
    val itemTextDone = TextStyle(
        fontFamily = FigtreeFamily, fontWeight = FontWeight.Normal,
        fontSize = 18.sp, lineHeight = 24.3.sp,
    )

    val fabLabel = TextStyle(
        fontFamily = FigtreeFamily, fontWeight = FontWeight.Medium,
        fontSize = 15.sp, lineHeight = 15.sp,
    )

    // Undo toast: regular-weight text; bold, tracked, uppercase action.
    val toastText = TextStyle(
        fontFamily = FigtreeFamily, fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 14.sp,
    )
    val toastAction = TextStyle(
        fontFamily = FigtreeFamily, fontWeight = FontWeight.Bold,
        fontSize = 12.sp, lineHeight = 12.sp, letterSpacing = 0.08.em(12.sp),
    )

    // Chip row: selected is semibold, unselected regular.
    val chipSelected = TextStyle(
        fontFamily = FigtreeFamily, fontWeight = FontWeight.SemiBold, fontSize = 12.5.sp, lineHeight = 12.5.sp,
    )
    val chipUnselected = TextStyle(
        fontFamily = FigtreeFamily, fontWeight = FontWeight.Normal, fontSize = 12.5.sp, lineHeight = 12.5.sp,
    )

    // Label/title suggestion popover rows.
    val suggestionText = TextStyle(
        fontFamily = FigtreeFamily, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 15.sp,
    )
    val sessionAddText = TextStyle(
        fontFamily = FigtreeFamily, fontWeight = FontWeight.Light, fontSize = 15.sp, lineHeight = 15.sp,
    )

    // Quick Add draft textarea (and reused for the label-rename field).
    val quickAddInput = TextStyle(
        fontFamily = FigtreeFamily, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 22.5.sp,
    )

    // Settings section label ("THEME" / "LABELS"): identical treatment to
    // sectionHeader above -- kept as a separate name since the two screens
    // apply it to differently-shaped containers.
    val settingsSectionLabel = TextStyle(
        fontFamily = FigtreeFamily, fontWeight = FontWeight.Bold,
        fontSize = 12.sp, lineHeight = 12.sp, letterSpacing = 0.08.em(12.sp),
    )
    // Theme segmented control option; the selected segment adds SemiBold at
    // the call site (ThemeSegment), since only that state needs the heavier
    // weight.
    val settingsButtonLabel = TextStyle(
        fontFamily = FigtreeFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 14.sp,
    )
    // Merge dialog's Cancel/Merge (and reused for the rename sheet's
    // Cancel/Save): the heading font, like other buttons.
    val dialogActionLabel = TextStyle(
        fontFamily = CaprasimoFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 14.sp,
    )
    // Merge target row name: regular weight -- distinct from labelName
    // (18sp Light), which is the Labels screen's own row style.
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

// Helper for `em`-based letter-spacing at a given font size,
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
