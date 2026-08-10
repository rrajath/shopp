package com.rrajath.milk.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rrajath.milk.data.db.LabelEntity
import com.rrajath.milk.ui.SessionAddEntry
import com.rrajath.milk.ui.theme.ShoppDimens
import com.rrajath.milk.ui.theme.ShoppTheme
import com.rrajath.milk.ui.theme.ShoppType

/**
 * TDD §6.1/§7.1-7.5: shared between the in-app FAB and (later) the Quick
 * Settings tile's CaptureActivity -- same component, same behavior, since
 * both are native Compose in the same process.
 */
@Composable
fun QuickAddOverlay(
    draft: String,
    stickyLabelId: String?,
    labels: List<LabelEntity>,
    suggestions: List<LabelEntity>,
    sessionAdds: List<SessionAddEntry>,
    onDraftChange: (String) -> Unit,
    onSelectSticky: (String?) -> Unit,
    onAcceptSuggestion: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ShoppTheme.colors
    val focusRequester = remember { FocusRequester() }

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
                .fillMaxWidth()
                .background(colors.sheet)
                .navigationBarsPadding()
                .imePadding()
                .padding(
                    horizontal = ShoppDimens.sheetPaddingHorizontal,
                    vertical = ShoppDimens.sheetPaddingTop,
                ),
        ) {
            if (suggestions.isNotEmpty()) {
                SuggestionsPopover(suggestions = suggestions, onAccept = onAcceptSuggestion)
            }

            sessionAdds.forEach { entry ->
                SessionAddRow(entry = entry, labels = labels)
            }

            Box(modifier = Modifier.fillMaxWidth()) {
                if (draft.isEmpty()) {
                    Text(text = "Add an item", style = ShoppType.quickAddInput.copy(color = colors.muted))
                }
                BasicTextField(
                    value = draft,
                    onValueChange = onDraftChange,
                    textStyle = ShoppType.quickAddInput.copy(color = colors.foreground),
                    cursorBrush = androidx.compose.ui.graphics.SolidColor(colors.accent),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { onSubmit() }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .onPreviewKeyEvent { event ->
                            // Return commits; pasted text may still contain \n
                            // (that's the only way a newline enters the buffer,
                            // per TDD §6.1 D-7) since paste bypasses key events.
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
