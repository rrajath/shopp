package com.rrajath.milk.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rrajath.milk.data.db.ItemEntity
import com.rrajath.milk.data.db.LabelEntity
import com.rrajath.milk.ui.components.EmptyState
import com.rrajath.milk.ui.components.SectionHeader
import com.rrajath.milk.ui.theme.ShoppDimens
import com.rrajath.milk.ui.theme.ShoppTheme
import com.rrajath.milk.ui.theme.ShoppType
import java.util.Calendar

@Composable
fun RecentlyCompletedScreen(
    completedItems: List<ItemEntity>,
    labels: List<LabelEntity>,
    onReadd: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ShoppTheme.colors

    if (completedItems.isEmpty()) {
        EmptyState(
            title = "Nothing here yet",
            body = "Items you tick off stay here for a week.",
            modifier = modifier.fillMaxSize().background(colors.background),
        )
        return
    }

    val dayStartMillis = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    val (today, earlier) = completedItems.partition { (it.completedAt ?: 0) >= dayStartMillis }

    LazyColumn(
        modifier = modifier.fillMaxSize().background(colors.background),
        contentPadding = PaddingValues(bottom = 40.dp),
    ) {
        if (today.isNotEmpty()) {
            item(key = "header-today") { SectionHeader(name = "Today", color = colors.foreground) }
            items(today, key = { it.id }) { CompletedRow(item = it, labels = labels, onReadd = onReadd) }
        }
        if (earlier.isNotEmpty()) {
            item(key = "header-earlier") { SectionHeader(name = "Earlier", color = colors.foreground) }
            items(earlier, key = { it.id }) { CompletedRow(item = it, labels = labels, onReadd = onReadd) }
        }
    }
}

@Composable
private fun CompletedRow(item: ItemEntity, labels: List<LabelEntity>, onReadd: (String) -> Unit) {
    val colors = ShoppTheme.colors
    val label = labels.find { it.id == item.labelId }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onReadd(item.id) }
            .padding(horizontal = ShoppDimens.rowPaddingHorizontal, vertical = ShoppDimens.rowPaddingVertical),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ShoppDimens.rowGap),
    ) {
        Box(
            modifier = Modifier.size(ShoppDimens.checkboxSize).background(colors.doneCheckboxFill, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = colors.background,
                modifier = Modifier.size(10.dp),
            )
        }
        Text(
            text = item.title,
            style = ShoppType.itemTextDone.copy(color = colors.muted, textDecoration = TextDecoration.LineThrough),
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
