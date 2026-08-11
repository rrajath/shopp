package com.rrajath.milk.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.rrajath.milk.data.db.ItemEntity
import com.rrajath.milk.ui.ListSection
import com.rrajath.milk.ui.UndoState
import com.rrajath.milk.ui.components.EmptyState
import com.rrajath.milk.ui.components.ItemRow
import com.rrajath.milk.ui.components.SectionHeader
import com.rrajath.milk.ui.components.UndoToast
import com.rrajath.milk.ui.theme.ShoppDimens
import com.rrajath.milk.ui.theme.ShoppTheme
import com.rrajath.milk.ui.theme.ShoppType

// Minimum rightward drag distance before a swipe-from-anywhere gesture opens
// the drawer -- large enough to not misfire during normal vertical scrolling
// or item taps.
private val SwipeOpenDrawerThreshold = 80.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ListScreen(
    sections: List<ListSection>,
    undo: UndoState?,
    emptyTitle: String,
    emptyBody: String,
    onCompleteItem: (ItemEntity) -> Unit,
    onCommitEdit: (itemId: String, newTitle: String) -> Unit,
    onUndo: () -> Unit,
    onAddClick: () -> Unit,
    onSwipeRightOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ShoppTheme.colors
    val hasAnyItems = sections.any { it.items.isNotEmpty() }
    val density = LocalDensity.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .pointerInput(onSwipeRightOpenDrawer) {
                val thresholdPx = with(density) { SwipeOpenDrawerThreshold.toPx() }
                var dragTotal = 0f
                detectHorizontalDragGestures(
                    onDragStart = { dragTotal = 0f },
                    onHorizontalDrag = { _, dragAmount -> dragTotal += dragAmount },
                    onDragEnd = { if (dragTotal > thresholdPx) onSwipeRightOpenDrawer() },
                )
            },
    ) {
        if (!hasAnyItems) {
            EmptyState(title = emptyTitle, body = emptyBody, modifier = Modifier.fillMaxSize())
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = ShoppDimens.listBottomPadding),
            ) {
                sections.forEach { section ->
                    val sectionColor = if (section.colorIndex == null) {
                        colors.inboxTint
                    } else {
                        colors.labelPalette[section.colorIndex % colors.labelPalette.size]
                    }
                    stickyHeader(key = "header-${section.labelId ?: "inbox"}") {
                        SectionHeader(name = section.name, color = sectionColor)
                    }
                    items(section.items, key = { it.id }) { item ->
                        ItemRow(
                            item = item,
                            onComplete = { onCompleteItem(item) },
                            onCommitEdit = { newTitle -> onCommitEdit(item.id, newTitle) },
                        )
                    }
                }
            }
        }

        AddFab(
            onClick = onAddClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(ShoppDimens.fabOffset),
        )

        if (undo != null) {
            UndoToast(
                text = undo.text,
                onUndo = onUndo,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(
                        horizontal = ShoppDimens.toastSideMargin,
                        vertical = ShoppDimens.toastBottomOffset,
                    ),
            )
        }
    }
}

@Composable
private fun AddFab(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = ShoppTheme.colors
    Row(
        modifier = modifier
            .height(ShoppDimens.fabHeight)
            .clip(RoundedCornerShape(ShoppDimens.fabCornerRadius))
            .background(colors.accent)
            .clickable(onClick = onClick)
            .padding(horizontal = ShoppDimens.fabPaddingHorizontal),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ShoppDimens.fabIconTextGap),
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = "Add",
            tint = colors.onAccent,
            modifier = Modifier.size(ShoppDimens.fabIconSize),
        )
        Text(text = "Add", style = ShoppType.fabLabel.copy(color = colors.onAccent))
    }
}
