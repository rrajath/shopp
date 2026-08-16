package com.rrajath.shopp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.rrajath.shopp.ui.theme.ShoppDimens
import com.rrajath.shopp.ui.theme.ShoppTheme
import com.rrajath.shopp.ui.theme.ShoppType

// A content-sized pill (`white-space: nowrap` in the prototype), not a
// full-width bar -- see ShoppDimens.toastMaxWidth for the one departure
// (a defensive cap so an unusually long item title can't run off-screen).
@Composable
fun UndoToast(text: String, onUndo: () -> Unit, modifier: Modifier = Modifier) {
    val colors = ShoppTheme.colors
    Row(
        modifier = modifier
            .background(colors.toastBackground, RoundedCornerShape(ShoppDimens.toastCornerRadius))
            .padding(
                start = ShoppDimens.toastPaddingStart,
                end = ShoppDimens.toastPaddingEnd,
                top = ShoppDimens.toastPaddingVertical,
                bottom = ShoppDimens.toastPaddingVertical,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ShoppDimens.toastGap),
    ) {
        Text(
            text = text,
            style = ShoppType.toastText.copy(color = colors.toastForeground),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = ShoppDimens.toastMaxWidth),
        )
        Text(
            text = "UNDO",
            style = ShoppType.toastAction.copy(color = colors.toastAction),
            modifier = Modifier.clickable(onClick = onUndo),
        )
    }
}
