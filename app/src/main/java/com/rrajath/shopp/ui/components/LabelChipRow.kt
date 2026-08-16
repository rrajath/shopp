package com.rrajath.shopp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.rrajath.shopp.data.db.LabelEntity
import com.rrajath.shopp.ui.theme.ShoppDimens
import com.rrajath.shopp.ui.theme.ShoppTheme
import com.rrajath.shopp.ui.theme.ShoppType

// Per internal-docs/website/ShoppApp.dc.html: every chip (Inbox included) is
// uniformly accent-filled when selected and neutral-filled (no border, no
// colored dot) when unselected -- label identity color shows in section
// headers, the Labels screen, and the merge picker instead.
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
        Chip(name = "Inbox", selected = selectedLabelId == null, onClick = { onSelect(null) })
        labels.forEach { label ->
            Chip(name = label.name, selected = selectedLabelId == label.id, onClick = { onSelect(label.id) })
        }
    }
}

@Composable
private fun Chip(name: String, selected: Boolean, onClick: () -> Unit) {
    val colors = ShoppTheme.colors
    Row(
        modifier = Modifier
            .defaultMinSize(minHeight = ShoppDimens.chipMinHeight)
            .clip(RoundedCornerShape(ShoppDimens.chipCornerRadius))
            .background(if (selected) colors.accent else colors.chipUnselectedFill)
            .clickable(onClick = onClick)
            .padding(horizontal = ShoppDimens.chipPaddingHorizontal, vertical = ShoppDimens.chipPaddingVertical),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = name,
            style = (if (selected) ShoppType.chipSelected else ShoppType.chipUnselected)
                .copy(color = if (selected) colors.chipSelectedText else colors.chipUnselectedText),
        )
    }
}
