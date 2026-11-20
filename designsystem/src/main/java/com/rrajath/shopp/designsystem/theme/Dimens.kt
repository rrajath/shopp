package com.rrajath.shopp.designsystem.theme

import androidx.compose.ui.unit.dp

// Grouped by where they're used, not by "type" (see docs/DESIGN_SYSTEM.md),
// since most of these are one-off component measurements rather than a
// reusable spacing scale.
object ShoppDimens {
    val tapTarget = 44.dp

    // Header row (screen title / hamburger-or-back / trailing action).
    // The header keeps a 3-slot (lead/title/trail) layout so Recently
    // Completed's "Clear" action and the back-navigation affordance have a
    // slot; as a result the title isn't strictly left-aligned with a fixed gap.
    val headerHorizontalPadding = 20.dp
    val headerTopPadding = 14.dp
    val headerBottomPadding = 10.dp
    val headerLeadTrailSize = 44.dp // outer tap target (tapTarget)
    val headerIconVisualSize = 40.dp // the hamburger/back circle's own visual size

    // List screen. rowPaddingVertical keeps a single-Row structure (the 44dp
    // tapTarget minHeight already dominates row height).
    val listBottomPadding = 110.dp
    val sectionHeaderPaddingTop = 18.dp
    val sectionHeaderPaddingHorizontal = 22.dp
    val sectionHeaderPaddingBottom = 7.dp
    val rowPaddingVertical = 4.dp
    val rowPaddingHorizontal = 22.dp
    val rowGap = 14.dp
    val checkboxSize = 22.dp
    val checkboxBorderWidth = 1.5.dp

    // Empty state -- a single left-aligned primary line, with a secondary
    // hint line styled smaller and muted underneath it.
    val emptyStatePaddingTop = 34.dp
    val emptyStatePaddingHorizontal = 22.dp
    val emptyStateGap = 8.dp

    // FAB -- circular, icon-only.
    val fabSize = 58.dp
    val fabRightOffset = 22.dp
    val fabBottomOffset = 28.dp
    val fabIconSize = 26.dp

    // Undo toast -- a content-sized pill, not a full-width bar.
    val toastBottomOffset = 100.dp
    // Recently Completed's re-add toast: same component, but that screen has
    // no FAB to clear, so it sits closer to the edge.
    val toastBottomOffsetNoFab = 16.dp
    val toastPaddingStart = 18.dp
    val toastPaddingEnd = 10.dp
    val toastPaddingVertical = 11.dp
    val toastCornerRadius = 100.dp // pill -- see chipCornerRadius/themeSegmentContainerCornerRadius for the same convention
    val toastGap = 16.dp
    val toastMaxWidth = 280.dp // defensive cap for very long item titles

    // Quick Add card (compact floating overlay, not a full-width bottom
    // sheet).
    val sheetPaddingHorizontal = 14.dp
    val sheetPaddingTop = 14.dp
    val sheetPaddingBottom = 14.dp
    val cardBottomMargin = 20.dp
    val cardHorizontalMargin = 12.dp
    val cardMaxWidth = 400.dp
    val cardCornerRadius = 26.dp
    val cardElevation = 16.dp
    val suggestionsCornerRadius = 14.dp
    val suggestionsBottomMargin = 10.dp
    val suggestionRowPaddingHorizontal = 16.dp
    val suggestionRowPaddingVertical = 13.dp
    val chipRowGap = 7.dp
    val chipRowPaddingTop = 16.dp
    val chipPaddingHorizontal = 13.dp
    val chipPaddingVertical = 6.dp
    val chipMinHeight = 28.dp
    val chipCornerRadius = 100.dp
    val chipDotSize = 7.dp
    val chipDotGap = 6.dp
    val sessionAddGap = 10.dp
    val sessionAddPaddingBottom = 10.dp

    // Quantity stepper (Quick Add input row and ItemRow edit mode): 28dp
    // circles (same height as a label chip) inside 44dp tapTarget boxes, so
    // the visible gap between the circles is the boxes' own slack.
    val quantityStepperButtonSize = 28.dp
    val quantityStepperIconSize = 16.dp
    val quantityStepperCountMinWidth = 20.dp
    val quantityStepperGap = 6.dp // between the text field and the stepper

    // Drawer.
    val drawerWidth = 290.dp
    // Same elevation as the Quick Add card: `menu` equals `background` in
    // both themes, so the shadow is the only edge between drawer and page.
    val drawerElevation = 16.dp
    val drawerTopPadding = 26.dp
    val drawerTitlePaddingHorizontal = 24.dp
    val drawerTitlePaddingBottom = 22.dp
    val drawerMenuItemPaddingVertical = 13.dp
    val drawerMenuItemPaddingHorizontal = 24.dp
    val drawerMenuGap = 14.dp
    val drawerMenuDotSize = 5.dp
    val drawerFooterPaddingVertical = 16.dp

    // Labels screen -- the title/hint paddings are also shared with
    // Settings below since both screens use the same horizontal rhythm.
    val labelsTitlePaddingTop = 26.dp
    val labelsTitlePaddingHorizontal = 24.dp
    val labelsTitlePaddingBottom = 14.dp
    val labelRowPaddingVertical = 13.dp
    val labelRowGap = 16.dp
    val labelDotSize = 9.dp
    val labelHintPaddingTop = 22.dp

    // Merge picker (LabelManagementSheet MERGE mode) -- radio circle and
    // target-row metrics.
    val mergeRadioSize = 16.dp
    val mergeRadioBorderWidth = 1.5.dp
    val mergeRadioInsetRingWidth = 4.dp
    val mergeTargetRowGap = 11.dp
    val mergeTargetRowPaddingVertical = 9.dp

    // Label color picker (LabelManagementSheet RENAME/"Edit" mode):
    // swatches from the app's existing labelPalette, wrapped 5 per row.
    val colorSectionPaddingTop = 18.dp
    val colorSwatchesPerRow = 5
    val colorSwatchSize = 24.dp
    val colorSwatchRingSize = 32.dp
    val colorSwatchRingWidth = 2.dp
    val colorSwatchGap = 12.dp
    val colorSwatchRowGap = 10.dp

    // Sheet action buttons (Cancel/Save/Merge/Delete-confirm): actual
    // bordered/filled pill buttons, not bare adjacent text, which reads as
    // too close together.
    val sheetButtonGap = 14.dp
    val sheetButtonCornerRadius = 100.dp
    val sheetButtonBorderWidth = 1.5.dp
    val sheetButtonPaddingHorizontal = 18.dp
    val sheetButtonPaddingVertical = 9.dp

    // Settings -- single pill-shaped segmented control. The first section
    // label ("Theme") uses 12dp top padding, the second ("Labels") 30dp; the
    // bottom padding is shared across both rather than adding a third
    // padding token for one section.
    val settingsSectionLabelPaddingTop = 12.dp
    val settingsSectionLabelPaddingTopSecond = 30.dp
    val settingsSectionLabelPaddingBottom = 12.dp
    val themeSegmentPaddingVertical = 9.dp
    val themeSegmentContainerCornerRadius = 100.dp
    val toggleRowPaddingVertical = 14.dp
    val toggleRowGap = 18.dp

    // Widget section (Settings control for the home-screen widget's
    // background transparency).
    // Matches the toggle rows' horizontal rhythm/vertical padding.
    val sliderRowPaddingVertical = 14.dp
    val sliderRowGap = 4.dp
}
