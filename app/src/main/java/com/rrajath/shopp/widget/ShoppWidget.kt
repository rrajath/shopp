package com.rrajath.shopp.widget

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.glance.GlanceId
import androidx.glance.LocalContext
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import com.rrajath.shopp.ShoppApplication
import com.rrajath.shopp.ui.buildSections
import com.rrajath.shopp.designsystem.theme.ShoppDarkColors
import com.rrajath.shopp.designsystem.theme.ShoppLightColors
import com.rrajath.shopp.designsystem.theme.ThemeMode

class ShoppWidget : GlanceAppWidget() {

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

            ShoppWidgetContent(
                colors = colors,
                cardAlpha = transparency,
                sections = sections,
            )
        }
    }
}

private fun isSystemDarkTheme(context: Context): Boolean {
    val nightMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    return nightMode == Configuration.UI_MODE_NIGHT_YES
}
