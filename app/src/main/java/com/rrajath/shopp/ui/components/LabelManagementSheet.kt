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
@Composable
private fun SheetActions(confirmLabel: String?, onCancel: () -> Unit, onConfirm: () -> Unit) {
    val colors = ShoppTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = ShoppDimens.chipRowPaddingTop),
        horizontalArrangement = Arrangement.spacedBy(ShoppDimens.settingsButtonGap),
    ) {
        Text(
            text = "Cancel",
            style = ShoppType.dialogActionLabel.copy(color = colors.muted),
            modifier = Modifier.clickable(onClick = onCancel),
        )
        if (confirmLabel != null) {
            Text(
                text = confirmLabel,
                style = ShoppType.dialogActionLabel.copy(color = colors.accent),
                modifier = Modifier.clickable(onClick = onConfirm),
            )
        }
    }
}
