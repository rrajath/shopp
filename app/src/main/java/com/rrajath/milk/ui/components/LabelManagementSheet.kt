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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.rrajath.milk.data.db.LabelEntity
import com.rrajath.milk.ui.theme.ShoppDimens
import com.rrajath.milk.ui.theme.ShoppTheme
import com.rrajath.milk.ui.theme.ShoppType
import com.rrajath.milk.usecases.RenameLabel

private enum class SheetMode { MENU, RENAME, MERGE, DELETE }

@Composable
fun LabelManagementSheet(
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
                .padding(horizontal = ShoppDimens.sheetPaddingHorizontal, vertical = ShoppDimens.sheetPaddingTop),
        ) {
            Text(text = label.name, style = ShoppType.labelName.copy(color = colors.muted))
            Box(Modifier.size(1.dp, ShoppDimens.chipRowPaddingTop))

            when (mode) {
                SheetMode.MENU -> {
                    ManagementRow("Rename") { mode = SheetMode.RENAME }
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
                    Text(
                        text = "Merge \"${label.name}\" into:",
                        style = ShoppType.toggleName.copy(color = colors.foreground),
                    )
                    allLabels.filter { it.id != label.id }.forEach { target ->
                        val color = colors.labelPalette[target.colorIndex % colors.labelPalette.size]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onMerge(target.id) }
                                .padding(vertical = ShoppDimens.labelRowPaddingVertical),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(ShoppDimens.labelRowGap),
                        ) {
                            Box(Modifier.size(ShoppDimens.labelDotSize).background(color, CircleShape))
                            Text(text = target.name, style = ShoppType.labelName.copy(color = colors.foreground))
                        }
                    }
                    SheetActions(confirmLabel = null, onCancel = { mode = SheetMode.MENU }, onConfirm = {})
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

@Composable
private fun SheetActions(confirmLabel: String?, onCancel: () -> Unit, onConfirm: () -> Unit) {
    val colors = ShoppTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = ShoppDimens.chipRowPaddingTop),
        horizontalArrangement = Arrangement.spacedBy(ShoppDimens.settingsButtonGap),
    ) {
        Text(
            text = "Cancel",
            style = ShoppType.settingsButtonLabel.copy(color = colors.muted),
            modifier = Modifier.clickable(onClick = onCancel),
        )
        if (confirmLabel != null) {
            Text(
                text = confirmLabel,
                style = ShoppType.settingsButtonLabel.copy(color = colors.accent),
                modifier = Modifier.clickable(onClick = onConfirm),
            )
        }
    }
}
