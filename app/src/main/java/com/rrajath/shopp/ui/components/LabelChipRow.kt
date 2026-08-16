package com.rrajath.shopp.ui.components

import androidx.compose.foundation.background
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
import com.rrajath.shopp.data.db.LabelEntity
import com.rrajath.shopp.ui.theme.ShoppDimens
import com.rrajath.shopp.ui.theme.ShoppTheme
import com.rrajath.shopp.ui.theme.ShoppType

// Per user request (August 2026, Quick Add overlay only -- the identical
// LabelChipRow used elsewhere in ShoppApp.dc.html keeps the old
// accent-vs-neutral treatment): unselected chips are a neutral filled pill
// (`chipUnselectedFill`/`chipUnselectedText`, as before) with a small leading
// dot in the label's own palette color -- Inbox never has a dot, it has no
// palette color. Selecting a chip moves that identity color from the dot
// into the whole pill's fill (dot disappears, text switches to a
// contrasting bold color) -- Inbox's selected fill is the inverted toast
// surface (dark in light mode, cream in dark mode) since it has no palette
// color of its own to promote.
@Composable
fun LabelChipRow(
    labels: List<LabelEntity>,
    selectedLabelId: String?, // null = Inbox
    onSelect: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ShoppTheme.colors
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(ShoppDimens.chipRowGap),
    ) {
        Chip(
            name = "Inbox",
            dotColor = null,
            selectedFillColor = colors.toastBackground,
            selectedTextColor = colors.toastForeground,
            selected = selectedLabelId == null,
            onClick = { onSelect(null) },
        )
        labels.forEach { label ->
            val labelColor = colors.labelPalette[label.colorIndex % colors.labelPalette.size]
            Chip(
                name = label.name,
                dotColor = labelColor,
                selectedFillColor = labelColor,
                selectedTextColor = colors.chipSelectedText,
                selected = selectedLabelId == label.id,
                onClick = { onSelect(label.id) },
            )
        }
    }
}

@Composable
private fun Chip(
    name: String,
    dotColor: Color?,
    selectedFillColor: Color,
    selectedTextColor: Color,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = ShoppTheme.colors
    Row(
        modifier = Modifier
            .defaultMinSize(minHeight = ShoppDimens.chipMinHeight)
            .clip(RoundedCornerShape(ShoppDimens.chipCornerRadius))
            .background(if (selected) selectedFillColor else colors.chipUnselectedFill)
            .clickable(onClick = onClick)
            .padding(horizontal = ShoppDimens.chipPaddingHorizontal, vertical = ShoppDimens.chipPaddingVertical),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ShoppDimens.chipDotGap),
    ) {
        if (!selected && dotColor != null) {
            Box(Modifier.size(ShoppDimens.chipDotSize).background(dotColor, CircleShape))
        }
        Text(
            text = name,
            style = (if (selected) ShoppType.chipSelected else ShoppType.chipUnselected)
                .copy(color = if (selected) selectedTextColor else colors.chipUnselectedText),
        )
    }
}
