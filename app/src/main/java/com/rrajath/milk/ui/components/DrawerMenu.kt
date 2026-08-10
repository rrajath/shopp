package com.rrajath.milk.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.rrajath.milk.ui.Screen
import com.rrajath.milk.ui.theme.ShoppDimens
import com.rrajath.milk.ui.theme.ShoppTheme
import com.rrajath.milk.ui.theme.ShoppType

data class DrawerCounts(val activeCount: Int, val completedCount: Int, val labelCount: Int)

// Slide/fade duration for the drawer opening/closing -- fast enough to feel
// responsive to a hamburger tap, slow enough to actually read as a slide.
private const val DrawerAnimationMillis = 220

@Composable
fun DrawerMenu(
    visible: Boolean,
    counts: DrawerCounts,
    currentScreen: Screen,
    isListFiltered: Boolean,
    onSelect: (Screen) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ShoppTheme.colors
    Box(modifier = modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(DrawerAnimationMillis)),
            exit = fadeOut(tween(DrawerAnimationMillis)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.scrim)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = onDismiss,
                    ),
            )
        }
        AnimatedVisibility(
            visible = visible,
            modifier = Modifier.align(Alignment.CenterStart),
            enter = slideInHorizontally(tween(DrawerAnimationMillis), initialOffsetX = { -it }),
            exit = slideOutHorizontally(tween(DrawerAnimationMillis), targetOffsetX = { -it }),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(ShoppDimens.drawerWidth)
                    .background(colors.menu)
                    .padding(top = ShoppDimens.drawerTopPadding),
            ) {
                Text(
                    text = "Shopp",
                    style = ShoppType.drawerTitle.copy(color = colors.foreground),
                    modifier = Modifier.padding(
                        horizontal = ShoppDimens.drawerTitlePaddingHorizontal,
                        vertical = ShoppDimens.drawerTitlePaddingBottom,
                    ),
                )
                MenuItem(
                    name = "All items",
                    count = counts.activeCount,
                    active = currentScreen == Screen.LIST && !isListFiltered,
                    onClick = { onSelect(Screen.LIST) },
                )
                MenuItem(
                    name = "Recently completed",
                    count = counts.completedCount,
                    active = currentScreen == Screen.RECENTLY_COMPLETED,
                    onClick = { onSelect(Screen.RECENTLY_COMPLETED) },
                )
                MenuItem(
                    name = "Labels",
                    count = counts.labelCount,
                    active = currentScreen == Screen.LABELS,
                    onClick = { onSelect(Screen.LABELS) },
                )
                MenuItem(
                    name = "Settings",
                    count = null,
                    active = currentScreen == Screen.SETTINGS,
                    onClick = { onSelect(Screen.SETTINGS) },
                )
            }
        }
    }
}

@Composable
private fun MenuItem(name: String, count: Int?, active: Boolean, onClick: () -> Unit) {
    val colors = ShoppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                horizontal = ShoppDimens.drawerMenuItemPaddingHorizontal,
                vertical = ShoppDimens.drawerMenuItemPaddingVertical,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ShoppDimens.drawerMenuGap),
    ) {
        Box(
            modifier = Modifier
                .size(ShoppDimens.drawerMenuDotSize)
                .background(if (active) colors.accent else Color.Transparent, CircleShape),
        )
        Text(
            text = name,
            style = ShoppType.drawerMenuItem.copy(color = if (active) colors.foreground else colors.muted),
            modifier = Modifier.weight(1f),
        )
        if (count != null) {
            Text(text = count.toString(), style = ShoppType.drawerMenuCount.copy(color = colors.muted))
        }
    }
}
