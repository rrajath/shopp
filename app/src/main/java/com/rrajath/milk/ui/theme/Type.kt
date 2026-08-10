package com.rrajath.milk.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.rrajath.milk.R

// Newsreader[opsz,wght].ttf is a variable font (see licenses/newsreader-OFL.txt);
// both weights point at the same resource with different variation settings.
@OptIn(ExperimentalTextApi::class)
val NewsreaderFamily = FontFamily(
    Font(
        resId = R.font.newsreader_variable,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(400),
            FontVariation.opticalSizing(20.sp),
        ),
    ),
    Font(
        resId = R.font.newsreader_variable,
        weight = FontWeight.Medium,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(500),
            FontVariation.opticalSizing(20.sp),
        ),
    ),
)

// Roboto is Android's system default, matching the prototype's body font
// (`Roboto, system-ui, sans-serif`) with no bundling needed.
private val SansFamily = FontFamily.Default

// Named text styles, transcribed 1:1 from the prototype's inline `font`
// shorthands (see docs/DESIGN_SYSTEM.md). Colors are applied by callers from
// ShoppColors, not baked in here.
object ShoppType {
    val screenTitle = TextStyle(
        fontFamily = NewsreaderFamily, fontWeight = FontWeight.Normal,
        fontSize = 18.sp, lineHeight = 18.sp, letterSpacing = 0.04.em(18.sp),
    )
    val sectionHeader = TextStyle(
        fontFamily = NewsreaderFamily, fontWeight = FontWeight.Normal,
        fontSize = 27.sp, lineHeight = 27.sp,
    )
    val emptyTitle = TextStyle(
        fontFamily = NewsreaderFamily, fontWeight = FontWeight.Normal,
        fontSize = 27.sp, lineHeight = 33.75.sp, textAlign = TextAlign.Center,
    )
    val emptyBody = TextStyle(
        fontFamily = SansFamily, fontWeight = FontWeight.Light,
        fontSize = 15.sp, lineHeight = 23.25.sp, textAlign = TextAlign.Center,
    )
    val drawerTitle = TextStyle(
        fontFamily = NewsreaderFamily, fontWeight = FontWeight.Normal,
        fontSize = 26.sp, lineHeight = 26.sp, letterSpacing = 0.02.em(26.sp),
    )
    val itemText = TextStyle(
        fontFamily = SansFamily, fontWeight = FontWeight.Light,
        fontSize = 18.sp, lineHeight = 23.4.sp,
    )
    val itemTextDone = TextStyle(
        fontFamily = SansFamily, fontWeight = FontWeight.Light,
        fontSize = 18.sp, lineHeight = 23.4.sp,
    )
    val fabLabel = TextStyle(
        fontFamily = SansFamily, fontWeight = FontWeight.Medium,
        fontSize = 15.sp, lineHeight = 15.sp,
    )
    val toastText = TextStyle(
        fontFamily = SansFamily, fontWeight = FontWeight.Light,
        fontSize = 15.sp, lineHeight = 19.5.sp,
    )
    val toastAction = TextStyle(
        fontFamily = SansFamily, fontWeight = FontWeight.Medium,
        fontSize = 14.sp, lineHeight = 14.sp, letterSpacing = 0.06.em(14.sp),
    )
    val chipSelected = TextStyle(
        fontFamily = SansFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 14.sp,
    )
    val chipUnselected = TextStyle(
        fontFamily = SansFamily, fontWeight = FontWeight.Light, fontSize = 14.sp, lineHeight = 14.sp,
    )
    val suggestionText = TextStyle(
        fontFamily = SansFamily, fontWeight = FontWeight.Light, fontSize = 16.sp, lineHeight = 16.sp,
    )
    val sessionAddText = TextStyle(
        fontFamily = SansFamily, fontWeight = FontWeight.Light, fontSize = 15.sp, lineHeight = 15.sp,
    )
    val quickAddInput = TextStyle(
        fontFamily = SansFamily, fontWeight = FontWeight.Light, fontSize = 18.sp, lineHeight = 27.sp,
    )
    val settingsSectionLabel = TextStyle(
        fontFamily = SansFamily, fontWeight = FontWeight.Normal,
        fontSize = 12.sp, lineHeight = 12.sp, letterSpacing = 0.14.em(12.sp),
    )
    val settingsButtonLabel = TextStyle(
        fontFamily = SansFamily, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 15.sp,
    )
    val toggleName = TextStyle(
        fontFamily = SansFamily, fontWeight = FontWeight.Light, fontSize = 17.sp, lineHeight = 22.1.sp,
    )
    val toggleHint = TextStyle(
        fontFamily = SansFamily, fontWeight = FontWeight.Light, fontSize = 13.sp, lineHeight = 18.2.sp,
    )
    val footerNote = TextStyle(
        fontFamily = SansFamily, fontWeight = FontWeight.Light, fontSize = 13.sp, lineHeight = 20.8.sp,
    )
    val drawerMenuItem = TextStyle(
        fontFamily = SansFamily, fontWeight = FontWeight.Light, fontSize = 17.sp, lineHeight = 22.1.sp,
    )
    val drawerMenuCount = TextStyle(
        fontFamily = SansFamily, fontWeight = FontWeight.Light, fontSize = 14.sp, lineHeight = 14.sp,
    )
    val labelName = TextStyle(
        fontFamily = SansFamily, fontWeight = FontWeight.Light, fontSize = 18.sp, lineHeight = 23.4.sp,
    )
    val labelCount = TextStyle(
        fontFamily = SansFamily, fontWeight = FontWeight.Light, fontSize = 15.sp, lineHeight = 15.sp,
    )
    val labelTag = TextStyle(
        fontFamily = SansFamily, fontWeight = FontWeight.Light, fontSize = 13.sp, lineHeight = 13.sp,
    )
    val clearAction = TextStyle(
        fontFamily = SansFamily, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 13.sp,
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
        fontFamily = SansFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
    )
)
