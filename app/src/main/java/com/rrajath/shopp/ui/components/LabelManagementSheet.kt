package com.rrajath.shopp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.rrajath.shopp.data.db.LabelEntity
import com.rrajath.shopp.ui.theme.ShoppDimens
import com.rrajath.shopp.ui.theme.ShoppTheme
import com.rrajath.shopp.ui.theme.ShoppType
import com.rrajath.shopp.usecases.RenameLabel

private enum class SheetMode { MENU, RENAME, MERGE, DELETE }

// Matches DrawerMenu's slide/fade duration for consistency.
private const val SheetAnimationMillis = 220

@Composable
fun LabelManagementSheet(
    visibleState: MutableTransitionState<Boolean>,
    label: LabelEntity,
    allLabels: List<LabelEntity>,
    onDismiss: () -> Unit,
    onRename: (String, (RenameLabel.Result) -> Unit) -> Unit,
    onColorChange: (colorIndex: Int) -> Unit,
    onMerge: (targetLabelId: String) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ShoppTheme.colors
    var mode by remember(label.id) { mutableStateOf(SheetMode.MENU) }
    var renameText by remember(label.id) { mutableStateOf(label.name) }
    var nameTaken by remember(label.id) { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedVisibility(
            visibleState = visibleState,
            enter = fadeIn(tween(SheetAnimationMillis)),
            exit = fadeOut(tween(SheetAnimationMillis)),
        ) {
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
        }
        AnimatedVisibility(
            visibleState = visibleState,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(tween(SheetAnimationMillis)) { it },
            exit = slideOutVertically(tween(SheetAnimationMillis)) { it },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.sheet)
                    .navigationBarsPadding()
                    .padding(horizontal = ShoppDimens.sheetPaddingHorizontal, vertical = ShoppDimens.sheetPaddingTop),
            ) {
            Text(text = label.name, style = ShoppType.labelName.copy(color = colors.muted))
            Box(Modifier.size(1.dp, ShoppDimens.chipRowPaddingTop))

            when (mode) {
                SheetMode.MENU -> {
                    ManagementRow("Edit") { mode = SheetMode.RENAME }
                    ManagementRow("Merge into another label") { mode = SheetMode.MERGE }
                    ManagementRow("Delete", emphasize = true) { mode = SheetMode.DELETE }
                }

                SheetMode.RENAME -> {
                    BasicTextField(
                        value = renameText,
                        onValueChange = { renameText = it; nameTaken = false },
                        textStyle = ShoppType.quickAddInput.copy(color = colors.foreground),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            onRename(renameText) { result ->
                                if (result == RenameLabel.Result.NameTaken) nameTaken = true else onDismiss()
                            }
                        }),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (nameTaken) {
                        Text(text = "That name is already used", style = ShoppType.toggleHint.copy(color = colors.accent))
                    }
                    Box(Modifier.size(1.dp, ShoppDimens.colorSectionPaddingTop))
                    Text(text = "Color", style = ShoppType.toggleHint.copy(color = colors.muted))
                    Box(Modifier.size(1.dp, ShoppDimens.colorSwatchRowGap))
                    ColorSwatchGrid(selectedIndex = label.colorIndex, onSelect = onColorChange)
                    SheetActions(
                        confirmLabel = "Save",
                        onCancel = { mode = SheetMode.MENU },
                        onConfirm = {
                            onRename(renameText) { result ->
                                if (result == RenameLabel.Result.NameTaken) nameTaken = true else onDismiss()
                            }
                        },
                    )
                }

                SheetMode.MERGE -> {
                    var mergeTarget by remember(label.id) { mutableStateOf<String?>(null) }
                    Text(
                        text = "Merge \"${label.name}\" into:",
                        style = ShoppType.toggleName.copy(color = colors.foreground),
                    )
                    allLabels.filter { it.id != label.id }.forEach { target ->
                        val selected = mergeTarget == target.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { mergeTarget = target.id }
                                .padding(vertical = ShoppDimens.mergeTargetRowPaddingVertical),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(ShoppDimens.mergeTargetRowGap),
                        ) {
                            MergeRadio(selected = selected)
                            Text(text = target.name, style = ShoppType.mergeTargetLabel.copy(color = colors.foreground))
                        }
                    }
                    SheetActions(
                        confirmLabel = "Merge",
                        onCancel = { mode = SheetMode.MENU },
                        onConfirm = { mergeTarget?.let(onMerge) },
                    )
                }

                SheetMode.DELETE -> {
                    Text(
                        text = "Delete \"${label.name}\"? Its items move to Inbox.",
                        style = ShoppType.toggleName.copy(color = colors.foreground),
                    )
                    SheetActions(confirmLabel = "Delete", onCancel = { mode = SheetMode.MENU }, onConfirm = onDelete)
                }
            }
        }
        }
    }
}

// Radio circle for the merge target picker: outline when unselected,
// accent-filled with an inset ring (punched-out center in the sheet's own
// background) when selected -- matches internal-docs/website/
// ShoppApp.dc.html's merge dialog exactly (`box-shadow: inset 0 0 0 4px
// var(--color-surface)` there, done here with a nested inner circle).
@Composable
private fun MergeRadio(selected: Boolean) {
    val colors = ShoppTheme.colors
    if (selected) {
        Box(
            modifier = Modifier.size(ShoppDimens.mergeRadioSize).background(colors.accent, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(ShoppDimens.mergeRadioSize - ShoppDimens.mergeRadioInsetRingWidth * 2)
                    .background(colors.sheet, CircleShape),
            )
        }
    } else {
        Box(
            modifier = Modifier
                .size(ShoppDimens.mergeRadioSize)
                .border(ShoppDimens.mergeRadioBorderWidth, colors.line, CircleShape),
        )
    }
}

// The label color picker (August 2026, user request): offers the app's
// existing 15-color pastel palette (`colors.labelPalette`) as manual
// override swatches -- not a separate/new palette, since a label's color is
// a single `colorIndex` into that one palette everywhere else in the app
// (dots, section headers, chip fills). Wraps into rows of 5.
@Composable
private fun ColorSwatchGrid(selectedIndex: Int, onSelect: (Int) -> Unit) {
    val colors = ShoppTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(ShoppDimens.colorSwatchRowGap)) {
        colors.labelPalette.withIndex().chunked(ShoppDimens.colorSwatchesPerRow).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(ShoppDimens.colorSwatchGap)) {
                row.forEach { (index, color) ->
                    ColorSwatch(color = color, selected = index == selectedIndex) { onSelect(index) }
                }
            }
        }
    }
}

@Composable
private fun ColorSwatch(color: Color, selected: Boolean, onClick: () -> Unit) {
    val colors = ShoppTheme.colors
    Box(
        modifier = Modifier.size(ShoppDimens.colorSwatchRingSize).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .size(ShoppDimens.colorSwatchRingSize)
                    .border(ShoppDimens.colorSwatchRingWidth, colors.foreground, CircleShape),
            )
        }
        Box(modifier = Modifier.size(ShoppDimens.colorSwatchSize).background(color, CircleShape))
    }
}

@Composable
private fun ManagementRow(name: String, emphasize: Boolean = false, onClick: () -> Unit) {
    val colors = ShoppTheme.colors
    Text(
        text = name,
        style = ShoppType.toggleName.copy(color = if (emphasize) colors.accent else colors.foreground),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = ShoppDimens.labelRowPaddingVertical),
    )
}

// Cancel/Merge in ShoppApp.dc.html's merge dialog use `font-family:
// var(--font-heading)` (Caprasimo), like the shared `.btn` class -- not the
// body font, hence the dedicated dialogActionLabel style rather than
// settingsButtonLabel (which is Figtree, for the Theme segmented control).
// August 2026 (user request): rendered as actual bordered/filled pill
// buttons, not bare adjacent text -- they read as too close together and
// not obviously tappable as plain text.
@Composable
private fun SheetActions(confirmLabel: String?, onCancel: () -> Unit, onConfirm: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = ShoppDimens.chipRowPaddingTop),
        horizontalArrangement = Arrangement.spacedBy(ShoppDimens.sheetButtonGap),
    ) {
        SheetButton(text = "Cancel", filled = false, onClick = onCancel)
        if (confirmLabel != null) {
            SheetButton(text = confirmLabel, filled = true, onClick = onConfirm)
        }
    }
}

@Composable
private fun SheetButton(text: String, filled: Boolean, onClick: () -> Unit) {
    val colors = ShoppTheme.colors
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(ShoppDimens.sheetButtonCornerRadius))
            .then(
                if (filled) {
                    Modifier.background(colors.accent)
                } else {
                    Modifier.border(
                        ShoppDimens.sheetButtonBorderWidth,
                        colors.line,
                        RoundedCornerShape(ShoppDimens.sheetButtonCornerRadius),
                    )
                },
            )
            .clickable(onClick = onClick)
            .padding(
                horizontal = ShoppDimens.sheetButtonPaddingHorizontal,
                vertical = ShoppDimens.sheetButtonPaddingVertical,
            ),
    ) {
        Text(
            text = text,
            style = ShoppType.dialogActionLabel.copy(color = if (filled) colors.onAccent else colors.foreground),
        )
    }
}
