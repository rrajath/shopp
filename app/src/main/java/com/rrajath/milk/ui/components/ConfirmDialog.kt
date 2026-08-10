package com.rrajath.milk.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.rrajath.milk.ui.theme.ShoppDimens
import com.rrajath.milk.ui.theme.ShoppTheme
import com.rrajath.milk.ui.theme.ShoppType

// No prototype reference exists for this (it's a state flag the mockup
// never renders), so styled consistently with our own tokens rather than
// default Material chrome.
@Composable
fun ConfirmDialog(message: String, confirmLabel: String, onCancel: () -> Unit, onConfirm: () -> Unit) {
    val colors = ShoppTheme.colors
    Dialog(onDismissRequest = onCancel) {
        Column(
            modifier = Modifier
                .width(280.dp)
                .clip(RoundedCornerShape(ShoppDimens.suggestionsCornerRadius))
                .background(colors.sheet)
                .padding(20.dp),
        ) {
            Text(text = message, style = ShoppType.toggleName.copy(color = colors.foreground))
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                Text(
                    text = "Cancel",
                    style = ShoppType.settingsButtonLabel.copy(color = colors.muted),
                    modifier = Modifier.clickable(onClick = onCancel).padding(8.dp),
                )
                Text(
                    text = confirmLabel,
                    style = ShoppType.settingsButtonLabel.copy(color = colors.accent),
                    modifier = Modifier.clickable(onClick = onConfirm).padding(8.dp),
                )
            }
        }
    }
}
