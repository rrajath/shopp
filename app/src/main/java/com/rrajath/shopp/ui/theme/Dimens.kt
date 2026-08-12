package com.rrajath.shopp.ui.theme

import androidx.compose.ui.unit.dp

// Transcribed from the prototype's inline px values (1:1 as dp — see
// docs/DESIGN_SYSTEM.md). Grouped by where they're used, not by "type",
// since most of these are one-off component measurements rather than a
// reusable spacing scale.
object ShoppDimens {
    val tapTarget = 44.dp

    // Header row (screen title / hamburger-or-back / trailing action)
    val headerHorizontalPadding = 14.dp
    val headerTopPadding = 10.dp
    val headerBottomPadding = 2.dp
    val headerLeadTrailSize = 44.dp
    val headerLeadTrailRadius = 22.dp

    // List screen
    val listBottomPadding = 110.dp
    val sectionHeaderPaddingTop = 26.dp
    val sectionHeaderPaddingHorizontal = 24.dp
    val sectionHeaderPaddingBottom = 10.dp
    val rowPaddingVertical = 12.dp
    val rowPaddingHorizontal = 24.dp
    val rowGap = 16.dp
    val checkboxSize = 19.dp
    val checkboxBorderWidth = 1.dp

    // FAB
    val fabHeight = 56.dp
    val fabPaddingHorizontal = 22.dp
    val fabIconTextGap = 10.dp
    val fabCornerRadius = 18.dp
    val fabOffset = 20.dp
    val fabIconSize = 16.dp

    // Undo toast
    val toastSideMargin = 16.dp
    val toastBottomOffset = 88.dp
    val toastPaddingHorizontal = 16.dp
    val toastPaddingVertical = 14.dp
    val toastCornerRadius = 14.dp
    val toastGap = 14.dp

    // Quick Add card (compact floating overlay, not a full-width bottom sheet)
    val sheetPaddingHorizontal = 20.dp
    val sheetPaddingTop = 18.dp
    val sheetPaddingBottom = 16.dp
    val cardBottomMargin = 20.dp
    val cardHorizontalMargin = 20.dp
    val cardMaxWidth = 400.dp
    val cardCornerRadius = 20.dp
    val cardElevation = 16.dp
    val suggestionsCornerRadius = 14.dp
    val suggestionsBottomMargin = 10.dp
    val suggestionRowPaddingHorizontal = 16.dp
    val suggestionRowPaddingVertical = 13.dp
    val chipRowGap = 8.dp
    val chipRowPaddingTop = 16.dp
    val chipPaddingHorizontal = 14.dp
    val chipPaddingVertical = 8.dp
    val chipMinHeight = 34.dp
    val chipCornerRadius = 100.dp
    val sessionAddGap = 10.dp
    val sessionAddPaddingBottom = 10.dp

    // Drawer
    val drawerWidth = 290.dp
    val drawerTopPadding = 26.dp
    val drawerTitlePaddingHorizontal = 24.dp
    val drawerTitlePaddingBottom = 22.dp
    val drawerMenuItemPaddingVertical = 13.dp
    val drawerMenuItemPaddingHorizontal = 24.dp
    val drawerMenuGap = 14.dp
    val drawerMenuDotSize = 5.dp

    // Labels screen
    val labelsTitlePaddingTop = 26.dp
    val labelsTitlePaddingHorizontal = 24.dp
    val labelsTitlePaddingBottom = 14.dp
    val labelRowPaddingVertical = 13.dp
    val labelRowGap = 16.dp
    val labelDotSize = 9.dp
    val labelHintPaddingTop = 22.dp
    val labelColorSwatchSize = 32.dp
    val labelColorSwatchGap = 12.dp
    val labelColorGridPaddingTop = 10.dp
    val labelColorSwatchSelectedBorderWidth = 2.dp

    // Settings
    val settingsSectionLabelPaddingTop = 22.dp
    val settingsSectionLabelPaddingTopSecond = 30.dp
    val settingsSectionLabelPaddingBottom = 10.dp
    val settingsButtonGap = 8.dp
    val settingsButtonPaddingVertical = 13.dp
    val settingsButtonCornerRadius = 12.dp
    val toggleRowPaddingVertical = 14.dp
    val toggleRowGap = 18.dp
    val toggleTrackWidth = 46.dp
    val toggleTrackHeight = 26.dp
    val toggleTrackCornerRadius = 13.dp
    val toggleTrackBorderWidth = 1.5.dp
    val toggleKnobSizeOn = 17.dp
    val toggleKnobSizeOff = 11.dp
    val footerPaddingTop = 28.dp
}
