package com.rrajath.shopp.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import com.rrajath.shopp.data.db.ItemEntity
import com.rrajath.shopp.ui.ListSection
import com.rrajath.shopp.ui.UndoState
import com.rrajath.shopp.ui.components.EmptyState
import com.rrajath.shopp.ui.components.ItemRow
import com.rrajath.shopp.ui.components.SectionHeader
import com.rrajath.shopp.ui.components.UndoToast
import com.rrajath.shopp.ui.theme.ShoppDimens
import com.rrajath.shopp.ui.theme.ShoppTheme

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
    onDrawerDrag: (Float) -> Unit,
    onDrawerDragEnd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ShoppTheme.colors
    val hasAnyItems = sections.any { it.items.isNotEmpty() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .pointerInput(onDrawerDrag, onDrawerDragEnd) {
                detectHorizontalDragGestures(
                    onDragEnd = onDrawerDragEnd,
                    onHorizontalDrag = { _, dragAmount -> onDrawerDrag(dragAmount) },
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
                .padding(end = ShoppDimens.fabRightOffset, bottom = ShoppDimens.fabBottomOffset),
        )

        if (undo != null) {
            UndoToast(
                text = undo.text,
                onUndo = onUndo,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = ShoppDimens.toastBottomOffset),
            )
        }
    }
}

@Composable
private fun AddFab(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = ShoppTheme.colors
    Box(
        modifier = modifier
            .size(ShoppDimens.fabSize)
            .background(colors.accent, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = "Add",
            tint = colors.onAccent,
            modifier = Modifier.size(ShoppDimens.fabIconSize),
        )
    }
}
