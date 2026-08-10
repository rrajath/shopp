package com.rrajath.milk.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.rrajath.milk.data.db.LabelEntity
import com.rrajath.milk.ui.theme.ShoppDimens
import com.rrajath.milk.ui.theme.ShoppTheme
import com.rrajath.milk.ui.theme.ShoppType

@Composable
fun LabelChipRow(
    labels: List<LabelEntity>,
    selectedLabelId: String?, // null = Inbox
    onSelect: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(ShoppDimens.chipRowGap),
    ) {
        val colors = ShoppTheme.colors
        // Inbox's selected fill is `foreground`, but it never shows a dot
        // (the prototype's `id && !on` -- only real labels get one).
        Chip(
            name = "Inbox",
            selected = selectedLabelId == null,
            selectedFill = colors.foreground,
            dotColor = null,
            onClick = { onSelect(null) },
        )
        labels.forEach { label ->
            val color = colors.labelPalette[label.colorIndex % colors.labelPalette.size]
            Chip(
                name = label.name,
                selected = selectedLabelId == label.id,
                selectedFill = color,
                dotColor = color,
                onClick = { onSelect(label.id) },
            )
        }
    }
}

@Composable
private fun Chip(name: String, selected: Boolean, selectedFill: Color, dotColor: Color?, onClick: () -> Unit) {
    val colors = ShoppTheme.colors
    Row(
        modifier = Modifier
            .defaultMinSize(minHeight = ShoppDimens.chipMinHeight)
            .clip(RoundedCornerShape(ShoppDimens.chipCornerRadius))
            .background(if (selected) selectedFill else Color.Transparent)
            .border(
                width = if (selected) 0.dp else 1.dp,
                color = if (selected) Color.Transparent else colors.chipBorder,
                shape = RoundedCornerShape(ShoppDimens.chipCornerRadius),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = ShoppDimens.chipPaddingHorizontal, vertical = ShoppDimens.chipPaddingVertical),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        if (!selected && dotColor != null) {
            Box(modifier = Modifier.size(7.dp).background(dotColor, CircleShape))
        }
        Text(
            text = name,
            style = (if (selected) ShoppType.chipSelected else ShoppType.chipUnselected)
                .copy(color = if (selected) colors.chipSelectedText else colors.muted),
        )
    }
}
