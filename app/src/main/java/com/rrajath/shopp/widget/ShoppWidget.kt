package com.rrajath.shopp.widget

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import com.rrajath.shopp.ShoppApplication
import com.rrajath.shopp.data.db.ItemEntity
import com.rrajath.shopp.ui.ListSection
import com.rrajath.shopp.ui.buildSections
import com.rrajath.shopp.ui.theme.ShoppDarkColors
import com.rrajath.shopp.ui.theme.ShoppDimens
import com.rrajath.shopp.ui.theme.ShoppLightColors
import com.rrajath.shopp.ui.theme.ThemeMode

// Fixed chrome heights, derived from the app's own row/section metrics
// (ShoppDimens) rather than the prototype's px values -- see
// docs/DESIGN_SYSTEM.md, "Home screen widget". Used only to decide how many
// rows fit at the widget's current (resizable) size; the actual composables
// in ShoppWidgetContent.kt are the source of truth for what gets drawn.
private val HEADER_HEIGHT = 62.dp
private val DIVIDER_HEIGHT = 1.dp
private val SECTION_HEADER_HEIGHT = ShoppDimens.sectionHeaderPaddingTop + 14.dp + ShoppDimens.sectionHeaderPaddingBottom
private val ITEM_ROW_HEIGHT = ShoppDimens.tapTarget
private val MORE_LINE_HEIGHT = 28.dp
private val CARD_VERTICAL_PADDING = 12.dp

class ShoppWidget : GlanceAppWidget() {

    // Row/section count adapts to the widget's live size instead of a single
    // fixed layout -- see the "Resizable" decision in docs/DESIGN_SYSTEM.md.
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val container = (context.applicationContext as ShoppApplication).container

        provideContent {
            val items by container.itemRepository.observeActiveItems().collectAsState(initial = emptyList())
            val labels by container.labelRepository.observeLabels().collectAsState(initial = emptyList())
            val transparency by container.preferencesRepository.widgetTransparency.collectAsState(initial = 1f)
            val themeMode by container.preferencesRepository.themeMode.collectAsState(initial = ThemeMode.SYSTEM)

            val dark = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemDarkTheme(LocalContext.current)
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            val colors = if (dark) ShoppDarkColors else ShoppLightColors

            // Inbox is dropped when empty here (unlike the List screen, which
            // always pins it) -- matches the prototype's own behavior and
            // avoids wasting scarce widget space on an empty section.
            val sections = remember(items, labels) {
                buildSections(items, labels, groupByLabel = true, filterLabelId = null)
                    .filter { it.items.isNotEmpty() }
            }

            val availableHeight = LocalSize.current.height - HEADER_HEIGHT - DIVIDER_HEIGHT - CARD_VERTICAL_PADDING
            val visible = remember(sections, availableHeight) { capToHeight(sections, availableHeight) }

            ShoppWidgetContent(
                colors = colors,
                cardAlpha = transparency,
                sections = visible.sections,
                hiddenCount = visible.hidden,
            )
        }
    }
}

private fun isSystemDarkTheme(context: Context): Boolean {
    val nightMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    return nightMode == Configuration.UI_MODE_NIGHT_YES
}

private class VisibleSections(val sections: List<ListSection>, val hidden: Int)

// Ports the prototype's own rowCount/hasMore/moreText capping (a fixed item
// count) into a size-driven budget: walk sections in order, always show a
// section's header once its items start being shown, and stop once the
// available height runs out -- reserving room for the trailing "N more" line
// up front whenever not everything fits.
private fun capToHeight(sections: List<ListSection>, availableHeight: Dp): VisibleSections {
    val totalItems = sections.sumOf { it.items.size }
    val fullHeight = sections.fold(0.dp) { acc, s -> acc + SECTION_HEADER_HEIGHT + ITEM_ROW_HEIGHT * s.items.size }
    if (fullHeight <= availableHeight || totalItems == 0) {
        return VisibleSections(sections, 0)
    }

    var remaining = availableHeight - MORE_LINE_HEIGHT
    var shown = 0
    val result = mutableListOf<ListSection>()
    for (section in sections) {
        if (remaining < SECTION_HEADER_HEIGHT + ITEM_ROW_HEIGHT) break
        remaining -= SECTION_HEADER_HEIGHT
        val visibleItems = mutableListOf<ItemEntity>()
        for (item in section.items) {
            if (remaining < ITEM_ROW_HEIGHT) break
            visibleItems += item
            remaining -= ITEM_ROW_HEIGHT
        }
        if (visibleItems.isNotEmpty()) {
            result += section.copy(items = visibleItems)
            shown += visibleItems.size
        }
    }
    return VisibleSections(result, totalItems - shown)
}
