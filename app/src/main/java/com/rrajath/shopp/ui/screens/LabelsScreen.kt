package com.rrajath.shopp.ui.screens

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import com.rrajath.shopp.data.db.LabelEntity
import com.rrajath.shopp.ui.components.EmptyState
import com.rrajath.shopp.ui.components.LabelManagementSheet
import com.rrajath.shopp.ui.theme.ShoppDimens
import com.rrajath.shopp.ui.theme.ShoppTheme
import com.rrajath.shopp.ui.theme.ShoppType
import com.rrajath.shopp.usecases.RenameLabel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LabelsScreen(
    labels: List<LabelEntity>,
    activeCounts: Map<String, Int>,
    onFilter: (String) -> Unit,
    onRename: (labelId: String, newName: String, onResult: (RenameLabel.Result) -> Unit) -> Unit,
    onColorChange: (labelId: String, colorIndex: Int) -> Unit,
    onMerge: (sourceLabelId: String, targetLabelId: String) -> Unit,
    onDelete: (labelId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ShoppTheme.colors
    val haptics = LocalHapticFeedback.current
    // Derived from the live `labels` list (not a snapshot captured at
    // long-press time) so a color change made in the sheet is reflected
    // back into it immediately -- see LabelManagementSheet's swatch picker.
    var managingLabelId by remember { mutableStateOf<String?>(null) }
    val liveManagingLabel = managingLabelId?.let { id -> labels.find { it.id == id } }

    // Keeps the last-known label around through the sheet's slide-down exit
    // (managingLabelId is already null by then, but the composable needs to
    // stay mounted -- with content -- until the animation finishes).
    var sheetLabel by remember { mutableStateOf<LabelEntity?>(null) }
    if (liveManagingLabel != null) sheetLabel = liveManagingLabel
    val sheetVisibleState = remember { MutableTransitionState(false) }
    sheetVisibleState.targetState = managingLabelId != null

    Box(modifier = modifier.fillMaxSize().background(colors.background)) {
        if (labels.isEmpty()) {
            EmptyState(
                title = "No labels yet",
                body = "Labels are created by typing @name in Quick Add.",
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Column(Modifier.fillMaxSize()) {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(labels, key = { it.id }) { label ->
                        LabelRow(
                            label = label,
                            count = activeCounts[label.id] ?: 0,
                            onTap = { onFilter(label.id) },
                            onLongPress = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                managingLabelId = label.id
                            },
                        )
                    }
                }
                Text(
                    text = "Labels are created by typing @name in Quick Add. Tap one to filter the list. Long-press to edit, merge, or delete.",
                    style = ShoppType.footerNote.copy(color = colors.muted),
                    textAlign = TextAlign.Start,
                    modifier = Modifier.padding(
                        horizontal = ShoppDimens.labelsTitlePaddingHorizontal,
                        vertical = ShoppDimens.labelHintPaddingTop,
                    ),
                )
            }
        }
    }

    sheetLabel?.let { label ->
        if (sheetVisibleState.targetState || !sheetVisibleState.isIdle) {
            LabelManagementSheet(
                visibleState = sheetVisibleState,
                label = label,
                allLabels = labels,
                onDismiss = { managingLabelId = null },
                onRename = { newName, onResult -> onRename(label.id, newName, onResult) },
                onColorChange = { colorIndex -> onColorChange(label.id, colorIndex) },
                onMerge = { targetId -> onMerge(label.id, targetId); managingLabelId = null },
                onDelete = { onDelete(label.id); managingLabelId = null },
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LabelRow(label: LabelEntity, count: Int, onTap: () -> Unit, onLongPress: () -> Unit) {
    val colors = ShoppTheme.colors
    val color = colors.labelPalette[label.colorIndex % colors.labelPalette.size]
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onTap, onLongClick = onLongPress)
            .padding(
                horizontal = ShoppDimens.labelsTitlePaddingHorizontal,
                vertical = ShoppDimens.labelRowPaddingVertical,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ShoppDimens.labelRowGap),
    ) {
        Box(Modifier.size(ShoppDimens.labelDotSize).background(color, CircleShape))
        Text(
            text = label.name,
            style = ShoppType.labelName.copy(color = colors.foreground),
            modifier = Modifier.weight(1f),
        )
        Text(text = count.toString(), style = ShoppType.labelCount.copy(color = colors.muted))
    }
}
