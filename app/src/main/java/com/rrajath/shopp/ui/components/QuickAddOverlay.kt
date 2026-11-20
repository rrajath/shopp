package com.rrajath.shopp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rrajath.shopp.data.db.LabelEntity
import com.rrajath.shopp.designsystem.components.QuantityStepper
import com.rrajath.shopp.domain.MAX_QUANTITY
import com.rrajath.shopp.domain.MIN_QUANTITY
import com.rrajath.shopp.ui.QuickPicks
import com.rrajath.shopp.ui.SessionAddEntry
import com.rrajath.shopp.ui.TitleSuggestion
import com.rrajath.shopp.designsystem.theme.ShoppDimens
import com.rrajath.shopp.designsystem.theme.ShoppTheme
import com.rrajath.shopp.designsystem.theme.ShoppType

/**
 * Shared between the in-app FAB and the Quick
 * Settings tile's CaptureActivity -- same component, same behavior, since
 * both are native Compose in the same process.
 */
@Composable
fun QuickAddOverlay(
    draft: String,
    stickyLabelId: String?,
    labels: List<LabelEntity>,
    suggestions: List<LabelEntity>,
    titleSuggestions: List<TitleSuggestion>,
    quickPicks: QuickPicks,
    sessionAdds: List<SessionAddEntry>,
    quantity: Int,
    hint: String?,
    onDraftChange: (String) -> Unit,
    onQuantityChange: (Int) -> Unit,
    onSelectSticky: (String?) -> Unit,
    onAcceptSuggestion: (String) -> Unit,
    onAcceptTitleSuggestion: (TitleSuggestion) -> Unit,
    onAddQuickPick: (TitleSuggestion) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ShoppTheme.colors
    val focusRequester = remember { FocusRequester() }
    // Draft stays a String upstream, but the field needs its own selection so
    // that when the draft is replaced from outside (accepting a suggestion),
    // the caret jumps to the end instead of staying where typing left it.
    var fieldState by remember { mutableStateOf(TextFieldValue(draft, TextRange(draft.length))) }
    val fieldValue = if (fieldState.text == draft) fieldState else TextFieldValue(draft, TextRange(draft.length))

    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.scrim)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onDismiss,
                ),
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))
                .padding(
                    start = ShoppDimens.cardHorizontalMargin,
                    end = ShoppDimens.cardHorizontalMargin,
                    bottom = ShoppDimens.cardBottomMargin,
                )
                .fillMaxWidth()
                .widthIn(max = ShoppDimens.cardMaxWidth)
                .shadow(ShoppDimens.cardElevation, RoundedCornerShape(ShoppDimens.cardCornerRadius))
                .clip(RoundedCornerShape(ShoppDimens.cardCornerRadius))
                .background(colors.sheet)
                .padding(
                    horizontal = ShoppDimens.sheetPaddingHorizontal,
                    vertical = ShoppDimens.sheetPaddingTop,
                ),
        ) {
            if (suggestions.isNotEmpty()) {
                SuggestionsPopover(suggestions = suggestions, onAccept = onAcceptSuggestion)
            } else if (titleSuggestions.isNotEmpty()) {
                TitleSuggestionsPopover(
                    suggestions = titleSuggestions,
                    labels = labels,
                    onAccept = onAcceptTitleSuggestion,
                )
            } else if (draft.isEmpty() && !quickPicks.isEmpty) {
                QuickPicksPanel(quickPicks = quickPicks, labels = labels, onAdd = onAddQuickPick)
            }

            sessionAdds.forEach { entry ->
                SessionAddRow(entry = entry, labels = labels)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(ShoppDimens.quantityStepperGap),
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    if (draft.isEmpty()) {
                        Text(
                            text = hint ?: "Add an item",
                            style = ShoppType.quickAddInput.copy(color = colors.muted),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    BasicTextField(
                        value = fieldValue,
                        onValueChange = {
                            fieldState = it
                            if (it.text != draft) onDraftChange(it.text)
                        },
                        textStyle = ShoppType.quickAddInput.copy(color = colors.foreground),
                        cursorBrush = androidx.compose.ui.graphics.SolidColor(colors.accent),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { onSubmit() }),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .onPreviewKeyEvent { event ->
                                // Return commits; pasted text may still contain \n
                                // (that's the only way a newline enters the buffer)
                                // since paste bypasses key events.
                                if (event.type == androidx.compose.ui.input.key.KeyEventType.KeyDown &&
                                    (event.key == Key.Enter || event.key == Key.NumPadEnter)
                                ) {
                                    onSubmit()
                                    true
                                } else {
                                    false
                                }
                            },
                    )
                }
                QuantityStepper(
                    value = quantity,
                    onValueChange = onQuantityChange,
                    min = MIN_QUANTITY,
                    max = MAX_QUANTITY,
                )
            }

            LaunchedEffect(Unit) { focusRequester.requestFocus() }

            LabelChipRow(
                labels = labels,
                selectedLabelId = stickyLabelId,
                onSelect = onSelectSticky,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = ShoppDimens.chipRowPaddingTop),
            )
        }
    }
}

@Composable
private fun SuggestionsPopover(suggestions: List<LabelEntity>, onAccept: (String) -> Unit) {
    val colors = ShoppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = ShoppDimens.suggestionsBottomMargin)
            .shadow(4.dp, RoundedCornerShape(ShoppDimens.suggestionsCornerRadius))
            .clip(RoundedCornerShape(ShoppDimens.suggestionsCornerRadius))
            .background(colors.menu),
    ) {
        suggestions.forEachIndexed { index, label ->
            val color = colors.labelPalette[label.colorIndex % colors.labelPalette.size]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAccept(label.name) }
                    .background(if (index == 0) colors.press else androidx.compose.ui.graphics.Color.Transparent)
                    .padding(
                        horizontal = ShoppDimens.suggestionRowPaddingHorizontal,
                        vertical = ShoppDimens.suggestionRowPaddingVertical,
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
                Text(text = label.name, style = ShoppType.suggestionText.copy(color = colors.foreground))
            }
        }
    }
}

// Autocomplete from purchase history (Recently Completed), see
// itemTitleSuggestions. Each row leads with a dot in the color of the label
// the item was last bought under (picking it files the item there), sized
// and spaced like the label-suggestion popover. Inbox has no palette color,
// so it gets a neutral `muted` dot, same as SessionAddRow.
@Composable
private fun TitleSuggestionsPopover(
    suggestions: List<TitleSuggestion>,
    labels: List<LabelEntity>,
    onAccept: (TitleSuggestion) -> Unit,
) {
    val colors = ShoppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = ShoppDimens.suggestionsBottomMargin)
            .shadow(4.dp, RoundedCornerShape(ShoppDimens.suggestionsCornerRadius))
            .clip(RoundedCornerShape(ShoppDimens.suggestionsCornerRadius))
            .background(colors.menu),
    ) {
        suggestions.forEachIndexed { index, suggestion ->
            val label = suggestion.labelId?.let { id -> labels.find { it.id == id } }
            val dotColor = if (label != null) colors.labelPalette[label.colorIndex % colors.labelPalette.size] else colors.muted
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAccept(suggestion) }
                    .background(if (index == 0) colors.press else androidx.compose.ui.graphics.Color.Transparent)
                    .padding(
                        horizontal = ShoppDimens.suggestionRowPaddingHorizontal,
                        vertical = ShoppDimens.suggestionRowPaddingVertical,
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(modifier = Modifier.size(8.dp).background(dotColor, CircleShape))
                Text(text = suggestion.title, style = ShoppType.suggestionText.copy(color = colors.foreground))
            }
        }
    }
}

private enum class QuickPickTab(val title: String) { Frequent("Frequent"), Recent("Recent") }

private const val QUICK_PICK_VISIBLE_ROWS = 5

// One-tap re-adds while the draft is empty (see quickPicks): two chip-style
// tabs over a popover styled like the suggestion popovers, showing up to
// MAX_QUICK_PICKS rows with QUICK_PICK_VISIBLE_ROWS visible and the rest
// scrolled. Typing swaps it for the suggestion popovers, which use the
// same slot. No top-row highlight: Done doesn't pick from this list.
@Composable
private fun QuickPicksPanel(
    quickPicks: QuickPicks,
    labels: List<LabelEntity>,
    onAdd: (TitleSuggestion) -> Unit,
) {
    val colors = ShoppTheme.colors
    var tab by rememberSaveable { mutableStateOf(QuickPickTab.Frequent) }
    val picks = when (tab) {
        QuickPickTab.Frequent -> quickPicks.frequent
        QuickPickTab.Recent -> quickPicks.recent
    }
    // Rows are single-line text plus fixed padding, so their height is
    // known up front; capping the popover at five of them avoids a
    // measure-then-shrink flash on the first frame.
    val density = LocalDensity.current
    val rowHeight = with(density) { ShoppType.suggestionText.lineHeight.toDp() } +
        ShoppDimens.suggestionRowPaddingVertical * 2

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = ShoppDimens.quickPickTabsPaddingBottom)
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(ShoppDimens.quickPickTabGap),
    ) {
        QuickPickTab.entries.forEach { entry ->
            QuickPickTabChip(title = entry.title, selected = tab == entry, onClick = { tab = entry })
        }
    }

    // Fresh scroll position per tab, so switching always starts at the top.
    val scrollState = remember(tab) { ScrollState(0) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = ShoppDimens.suggestionsBottomMargin)
            .shadow(4.dp, RoundedCornerShape(ShoppDimens.suggestionsCornerRadius))
            .clip(RoundedCornerShape(ShoppDimens.suggestionsCornerRadius))
            .background(colors.menu)
            .heightIn(max = rowHeight * QUICK_PICK_VISIBLE_ROWS)
            .verticalScroll(scrollState)
            .testTag("quickPicksList"),
    ) {
        picks.forEach { pick ->
            val label = pick.labelId?.let { id -> labels.find { it.id == id } }
            val dotColor = if (label != null) colors.labelPalette[label.colorIndex % colors.labelPalette.size] else colors.muted
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAdd(pick) }
                    .padding(
                        horizontal = ShoppDimens.suggestionRowPaddingHorizontal,
                        vertical = ShoppDimens.suggestionRowPaddingVertical,
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(modifier = Modifier.size(8.dp).background(dotColor, CircleShape))
                Text(
                    text = pick.title,
                    style = ShoppType.suggestionText.copy(color = colors.foreground),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

// Styled like an Inbox chip in LabelChipRow: the selected tab takes the
// inverted toast surface, the other the neutral unselected fill.
@Composable
private fun QuickPickTabChip(title: String, selected: Boolean, onClick: () -> Unit) {
    val colors = ShoppTheme.colors
    Box(
        modifier = Modifier
            .defaultMinSize(minHeight = ShoppDimens.chipMinHeight)
            .clip(RoundedCornerShape(ShoppDimens.chipCornerRadius))
            .background(if (selected) colors.toastBackground else colors.chipUnselectedFill)
            .selectable(selected = selected, role = Role.Tab, onClick = onClick)
            .padding(horizontal = ShoppDimens.chipPaddingHorizontal, vertical = ShoppDimens.chipPaddingVertical),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = title,
            style = (if (selected) ShoppType.chipSelected else ShoppType.chipUnselected)
                .copy(color = if (selected) colors.toastForeground else colors.chipUnselectedText),
        )
    }
}

@Composable
private fun SessionAddRow(entry: SessionAddEntry, labels: List<LabelEntity>) {
    val colors = ShoppTheme.colors
    val label = labels.find { it.id == entry.labelId }
    val dotColor = if (label != null) colors.labelPalette[label.colorIndex % colors.labelPalette.size] else colors.muted
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = ShoppDimens.sessionAddPaddingBottom),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ShoppDimens.sessionAddGap),
    ) {
        Box(modifier = Modifier.size(6.dp).background(dotColor, CircleShape))
        Text(
            text = entry.text,
            style = ShoppType.sessionAddText.copy(color = colors.muted),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
