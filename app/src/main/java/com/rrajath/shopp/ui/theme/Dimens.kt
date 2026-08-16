package com.rrajath.shopp.ui.theme

import androidx.compose.ui.unit.dp

// Transcribed from the prototype's inline px values (1:1 as dp — see
// docs/DESIGN_SYSTEM.md). Grouped by where they're used, not by "type",
// since most of these are one-off component measurements rather than a
// reusable spacing scale.
object ShoppDimens {
    val tapTarget = 44.dp

    // Header row (screen title / hamburger-or-back / trailing action).
    // ShoppApp.dc.html's app bar: `padding: 14px 20px 10px`, `gap: 12px`,
    // a 40x40 icon circle. The header keeps a 3-slot (lead/title/trail)
    // layout the prototype doesn't need -- it never shows a back button or
    // trailing action -- so its title isn't strictly left-aligned with a
    // fixed gap the way the prototype's is; that structural difference is
    // kept deliberately (Recently Completed's "Clear" action and the
    // back-navigation affordance need the extra slot).
    val headerHorizontalPadding = 20.dp
    val headerTopPadding = 14.dp
    val headerBottomPadding = 10.dp
    val headerLeadTrailSize = 44.dp // outer tap target (tapTarget) -- not itself in the prototype
    val headerIconVisualSize = 40.dp // the hamburger/back circle's own visual size

    // List screen. Row literal padding is `0 22px` with a 44px min-height
    // and the row *text* getting its own `4px 0` -- rowPaddingVertical below
    // approximates that net effect while keeping the existing single-Row
    // structure (the 44dp tapTarget minHeight already dominates row height).
    val listBottomPadding = 110.dp
    val sectionHeaderPaddingTop = 18.dp
    val sectionHeaderPaddingHorizontal = 22.dp
    val sectionHeaderPaddingBottom = 7.dp
    val rowPaddingVertical = 4.dp
    val rowPaddingHorizontal = 22.dp
    val rowGap = 14.dp
    val checkboxSize = 22.dp
    val checkboxBorderWidth = 1.5.dp

    // Empty state -- ShoppApp.dc.html depicts this as a single left-aligned
    // 16px line (`padding: 34px 22px`), not the old prototype's centered
    // title+body treatment. The app still shows a secondary hint line (the
    // new prototype has no second line to source one from), styled smaller
    // and muted underneath the literal-sized primary line.
    val emptyStatePaddingTop = 34.dp
    val emptyStatePaddingHorizontal = 22.dp
    val emptyStateGap = 8.dp

    // FAB -- circular icon-only, per ShoppApp.dc.html (was a pill with an
    // "Add" text label)
    val fabSize = 58.dp
    val fabRightOffset = 22.dp
    val fabBottomOffset = 28.dp
    val fabIconSize = 26.dp

    // Undo toast -- a content-sized pill (`white-space: nowrap`), not a
    // full-width bar: `padding: 11px 10px 11px 18px`, `gap: 16px`, `bottom: 100px`.
    val toastBottomOffset = 100.dp
    // Recently Completed's re-add toast: same component, but that screen has
    // no FAB to clear, so it sits closer to the edge -- not in the prototype
    // (Recently Completed isn't depicted by it), kept from the old spacing.
    val toastBottomOffsetNoFab = 16.dp
    val toastPaddingStart = 18.dp
    val toastPaddingEnd = 10.dp
    val toastPaddingVertical = 11.dp
    val toastCornerRadius = 100.dp // pill -- see chipCornerRadius/themeSegmentContainerCornerRadius for the same convention
    val toastGap = 16.dp
    val toastMaxWidth = 280.dp // defensive cap for very long item titles -- not in the prototype, which never wraps

    // Quick Add card (compact floating overlay, not a full-width bottom
    // sheet) -- padding/margins/radius per ShoppApp.dc.html's overlay input card
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

    // Drawer -- not depicted by the new prototype (no drawer mode in
    // ShoppApp.dc.html); kept from the old prototype.
    val drawerWidth = 290.dp
    val drawerTopPadding = 26.dp
    val drawerTitlePaddingHorizontal = 24.dp
    val drawerTitlePaddingBottom = 22.dp
    val drawerMenuItemPaddingVertical = 13.dp
    val drawerMenuItemPaddingHorizontal = 24.dp
    val drawerMenuGap = 14.dp
    val drawerMenuDotSize = 5.dp
    val drawerFooterPaddingVertical = 16.dp

    // Labels screen -- its own row list isn't depicted by the new prototype
    // (kept from the old one); the title/hint paddings are also shared with
    // Settings below since both screens use the same horizontal rhythm.
    val labelsTitlePaddingTop = 26.dp
    val labelsTitlePaddingHorizontal = 24.dp
    val labelsTitlePaddingBottom = 14.dp
    val labelRowPaddingVertical = 13.dp
    val labelRowGap = 16.dp
    val labelDotSize = 9.dp
    val labelHintPaddingTop = 22.dp

    // Merge picker (LabelManagementSheet MERGE mode) -- radio circle and
    // target-row metrics per ShoppApp.dc.html's merge dialog.
    val mergeRadioSize = 16.dp
    val mergeRadioBorderWidth = 1.5.dp
    val mergeRadioInsetRingWidth = 4.dp
    val mergeTargetRowGap = 11.dp
    val mergeTargetRowPaddingVertical = 9.dp

    // Label color picker (LabelManagementSheet RENAME/"Edit" mode, August
    // 2026 user request): swatches from the app's existing labelPalette,
    // wrapped 5 per row.
    val colorSectionPaddingTop = 18.dp
    val colorSwatchesPerRow = 5
    val colorSwatchSize = 24.dp
    val colorSwatchRingSize = 32.dp
    val colorSwatchRingWidth = 2.dp
    val colorSwatchGap = 12.dp
    val colorSwatchRowGap = 10.dp

    // Sheet action buttons (Cancel/Save/Merge/Delete-confirm): actual
    // bordered/filled pill buttons, not bare adjacent text (August 2026,
    // user request -- they used to read as too close together).
    val sheetButtonGap = 14.dp
    val sheetButtonCornerRadius = 100.dp
    val sheetButtonBorderWidth = 1.5.dp
    val sheetButtonPaddingHorizontal = 18.dp
    val sheetButtonPaddingVertical = 9.dp

    // Settings -- single pill-shaped segmented control (was 3 separate
    // bordered buttons), per ShoppApp.dc.html. Section label padding:
    // `padding: 12px 0 12px` for the first ("Theme"), `30px 0 8px` for the
    // second ("Labels") -- the bottom value below is shared across both
    // (12px), a small (4px) deviation from the second section's 8px rather
    // than adding a third padding token for one section.
    val settingsSectionLabelPaddingTop = 12.dp
    val settingsSectionLabelPaddingTopSecond = 30.dp
    val settingsSectionLabelPaddingBottom = 12.dp
    val themeSegmentPaddingVertical = 9.dp
    val themeSegmentContainerCornerRadius = 100.dp
    val toggleRowPaddingVertical = 14.dp
    val toggleRowGap = 18.dp
}
