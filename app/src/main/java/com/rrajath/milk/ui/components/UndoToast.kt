package com.rrajath.milk.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.rrajath.milk.ui.theme.ShoppDimens
import com.rrajath.milk.ui.theme.ShoppTheme
import com.rrajath.milk.ui.theme.ShoppType

@Composable
fun UndoToast(text: String, onUndo: () -> Unit, modifier: Modifier = Modifier) {
    val colors = ShoppTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.toastBackground, RoundedCornerShape(ShoppDimens.toastCornerRadius))
            .padding(
                horizontal = ShoppDimens.toastPaddingHorizontal,
                vertical = ShoppDimens.toastPaddingVertical,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ShoppDimens.toastGap),
    ) {
        Text(
            text = text,
            style = ShoppType.toastText.copy(color = colors.toastForeground),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "UNDO",
            style = ShoppType.toastAction.copy(color = colors.toastAction),
            modifier = Modifier.clickable(onClick = onUndo),
        )
    }
}
