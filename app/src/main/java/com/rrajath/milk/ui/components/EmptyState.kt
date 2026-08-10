package com.rrajath.milk.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rrajath.milk.ui.theme.ShoppTheme
import com.rrajath.milk.ui.theme.ShoppType

@Composable
fun EmptyState(title: String, body: String, modifier: Modifier = Modifier) {
    val colors = ShoppTheme.colors
    Column(modifier = modifier.padding(top = 120.dp, start = 40.dp, end = 40.dp)) {
        Text(
            text = title,
            style = ShoppType.emptyTitle.copy(color = colors.foreground, textAlign = TextAlign.Center),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = body,
            style = ShoppType.emptyBody.copy(color = colors.muted, textAlign = TextAlign.Center),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
