package com.rrajath.shopp.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rrajath.shopp.data.db.ItemEntity
import com.rrajath.shopp.data.db.LabelEntity
import com.rrajath.shopp.ui.UndoState
import com.rrajath.shopp.ui.components.EmptyState
import com.rrajath.shopp.ui.components.SectionHeader
import com.rrajath.shopp.ui.components.UndoToast
import com.rrajath.shopp.ui.theme.ShoppDimens
import com.rrajath.shopp.ui.theme.ShoppTheme
import com.rrajath.shopp.ui.theme.ShoppType
import kotlinx.coroutines.delay
import java.util.Calendar

// Mirrors ItemRow's COMPLETE_REMOVE_DELAY_MS: let the uncheck animation
// finish before the row actually leaves the list.
private const val READD_REMOVE_DELAY_MS = 200L

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RecentlyCompletedScreen(
    completedItems: List<ItemEntity>,
    labels: List<LabelEntity>,
    readdUndo: UndoState?,
    onReadd: (ItemEntity) -> Unit,
    onUndoReadd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ShoppTheme.colors

    Box(modifier = modifier.fillMaxSize().background(colors.background)) {
        if (completedItems.isEmpty()) {
            EmptyState(
                title = "Nothing here yet",
                body = "Items you tick off stay here for a week.",
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            val dayStartMillis = remember {
                Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }.timeInMillis
            }
            val (today, earlier) = completedItems.partition { (it.completedAt ?: 0) >= dayStartMillis }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 40.dp),
            ) {
                if (today.isNotEmpty()) {
                    item(key = "header-today") { SectionHeader(name = "Today", color = colors.foreground) }
                    items(today, key = { it.id }) {
                        CompletedRow(item = it, labels = labels, onReadd = onReadd, modifier = Modifier.animateItem())
                    }
                }
                if (earlier.isNotEmpty()) {
                    item(key = "header-earlier") { SectionHeader(name = "Earlier", color = colors.foreground) }
                    items(earlier, key = { it.id }) {
                        CompletedRow(item = it, labels = labels, onReadd = onReadd, modifier = Modifier.animateItem())
                    }
                }
            }
        }

        if (readdUndo != null) {
            UndoToast(
                text = readdUndo.text,
                onUndo = onUndoReadd,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = ShoppDimens.toastBottomOffsetNoFab),
            )
        }
    }
}

/**
 * Mirrors [com.rrajath.shopp.ui.components.ItemRow]: tap marks `readding`
 * immediately (drives the checkbox "un-checking" and the item's return to
 * normal weight/color), and [onReadd] -- the actual write -- fires after
 * [READD_REMOVE_DELAY_MS] so that animation finishes before the row fades
 * out of the list.
 */
@Composable
private fun CompletedRow(
    item: ItemEntity,
    labels: List<LabelEntity>,
    onReadd: (ItemEntity) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ShoppTheme.colors
    val label = labels.find { it.id == item.labelId }
    var readding by remember(item.id) { mutableStateOf(false) }

    LaunchedEffect(readding) {
        if (readding) {
            delay(READD_REMOVE_DELAY_MS)
            onReadd(item)
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = !readding) { readding = true }
            .padding(horizontal = ShoppDimens.rowPaddingHorizontal, vertical = ShoppDimens.rowPaddingVertical),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ShoppDimens.rowGap),
    ) {
        ReaddCheckbox(readding = readding)
        Text(
            text = item.title,
            style = ShoppType.itemTextDone.copy(
                color = if (readding) colors.foreground else colors.muted,
                textDecoration = if (readding) TextDecoration.None else TextDecoration.LineThrough,
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (label != null) {
            val color = colors.labelPalette[label.colorIndex % colors.labelPalette.size]
            Text(text = label.name, style = ShoppType.labelTag.copy(color = color))
        }
    }
}

@Composable
private fun ReaddCheckbox(readding: Boolean, modifier: Modifier = Modifier) {
    val colors = ShoppTheme.colors
    val borderColor by animateColorAsState(
        targetValue = if (readding) colors.checkboxBorder else Color.Transparent,
        animationSpec = tween(180),
        label = "readdCheckboxBorder",
    )
    val fillColor by animateColorAsState(
        targetValue = if (readding) Color.Transparent else colors.doneCheckboxFill,
        animationSpec = tween(180),
        label = "readdCheckboxFill",
    )
    val checkAlpha by animateFloatAsState(
        targetValue = if (readding) 0f else 1f,
        animationSpec = tween(140),
        label = "readdCheckAlpha",
    )

    Box(
        modifier = modifier
            .size(ShoppDimens.checkboxSize)
            .background(fillColor, CircleShape)
            .border(ShoppDimens.checkboxBorderWidth, borderColor, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Check,
            contentDescription = null,
            tint = colors.background.copy(alpha = checkAlpha),
            modifier = Modifier.size(12.dp),
        )
    }
}
