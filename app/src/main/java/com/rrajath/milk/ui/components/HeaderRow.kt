package com.rrajath.milk.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.rrajath.milk.ui.theme.ShoppDimens
import com.rrajath.milk.ui.theme.ShoppTheme
import com.rrajath.milk.ui.theme.ShoppType

/**
 * Shared header row across every screen (prototype's single `shell` header):
 * leading hamburger-or-back, centered title, trailing action (e.g. "Clear").
 */
@Composable
fun HeaderRow(
    title: String,
    showBack: Boolean,
    trailingLabel: String? = null,
    onLeadTap: () -> Unit,
    onTrailTap: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val colors = ShoppTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = ShoppDimens.headerHorizontalPadding, vertical = ShoppDimens.headerTopPadding),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(ShoppDimens.headerLeadTrailSize)
                .clip(CircleShape)
                .clickable(onClick = onLeadTap),
            contentAlignment = Alignment.Center,
        ) {
            if (showBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = colors.foreground)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.width(20.dp).height(1.5.dp).background(colors.foreground))
                    Box(Modifier.width(20.dp).height(1.5.dp).background(colors.foreground))
                }
            }
        }

        Text(text = title, style = ShoppType.screenTitle.copy(color = colors.foreground))

        Box(
            modifier = Modifier
                .size(ShoppDimens.headerLeadTrailSize)
                .clip(CircleShape)
                .clickable(enabled = trailingLabel != null, onClick = onTrailTap),
            contentAlignment = Alignment.Center,
        ) {
            if (trailingLabel != null) {
                Text(text = trailingLabel, style = ShoppType.clearAction.copy(color = colors.muted))
            }
        }
    }
}
