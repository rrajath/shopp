package com.rrajath.shopp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.rrajath.shopp.ui.Screen
import com.rrajath.shopp.ui.theme.ShoppDimens
import com.rrajath.shopp.ui.theme.ShoppTheme
import com.rrajath.shopp.ui.theme.ShoppType
import kotlin.math.roundToInt

data class DrawerCounts(val activeCount: Int, val completedCount: Int, val labelCount: Int)

/**
 * Renders based on a continuous open-progress (0f closed .. 1f fully docked)
 * rather than a boolean, so it can track a drag 1:1 as the finger moves --
 * the caller (ShoppApp) owns the [Animatable] driving [progress], snapping it
 * during a drag and animating it to settle (tap-driven open/close, or a
 * drag released past/before the halfway point).
 */
@Composable
fun DrawerMenu(
    progress: Float,
    counts: DrawerCounts,
    currentScreen: Screen,
    isListFiltered: Boolean,
    onSelect: (Screen) -> Unit,
    onDismiss: () -> Unit,
    onDrawerDrag: (Float) -> Unit,
    onDrawerDragEnd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (progress <= 0f) return

    val colors = ShoppTheme.colors
    val drawerWidthPx = with(LocalDensity.current) { ShoppDimens.drawerWidth.toPx() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(onDrawerDrag, onDrawerDragEnd) {
                detectHorizontalDragGestures(
                    onDragEnd = onDrawerDragEnd,
                    onHorizontalDrag = { _, dragAmount -> onDrawerDrag(dragAmount) },
                )
            },
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.scrim.copy(alpha = colors.scrim.alpha * progress))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onDismiss,
                ),
        )
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxHeight()
                .width(ShoppDimens.drawerWidth)
                .offset { IntOffset((-(1f - progress) * drawerWidthPx).roundToInt(), 0) }
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
