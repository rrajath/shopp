package com.rrajath.shopp.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.rrajath.shopp.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Theme is the only persisted preference (TDD §5.2); these three behaviour
// toggles are prototype-scope additions (user decision: prototype wins for
// scope) and live alongside it in the same small DataStore file.
class PreferencesRepository(private val dataStore: DataStore<Preferences>) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val GROUP_BY_LABEL = booleanPreferencesKey("group_by_label")
        val KEEP_QUICK_ADD_OPEN = booleanPreferencesKey("keep_quick_add_open")
        val CONFIRM_BEFORE_CLEARING = booleanPreferencesKey("confirm_before_clearing")
        val WIDGET_TRANSPARENCY = floatPreferencesKey("widget_transparency")
    }

    val themeMode: Flow<ThemeMode> = dataStore.data.map { prefs ->
        prefs[Keys.THEME_MODE]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.SYSTEM
    }
    val groupByLabel: Flow<Boolean> = dataStore.data.map { it[Keys.GROUP_BY_LABEL] ?: true }
    val keepQuickAddOpen: Flow<Boolean> = dataStore.data.map { it[Keys.KEEP_QUICK_ADD_OPEN] ?: true }
    val confirmBeforeClearing: Flow<Boolean> = dataStore.data.map { it[Keys.CONFIRM_BEFORE_CLEARING] ?: true }
    // Widget card background alpha (1f = opaque, matching the prototype and
    // the app's own surfaces). Text/icons/checkboxes stay fully opaque --
    // only the card fill is affected.
    val widgetTransparency: Flow<Float> = dataStore.data.map { it[Keys.WIDGET_TRANSPARENCY] ?: 1f }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setGroupByLabel(value: Boolean) {
        dataStore.edit { it[Keys.GROUP_BY_LABEL] = value }
    }

    suspend fun setKeepQuickAddOpen(value: Boolean) {
        dataStore.edit { it[Keys.KEEP_QUICK_ADD_OPEN] = value }
    }

    suspend fun setConfirmBeforeClearing(value: Boolean) {
        dataStore.edit { it[Keys.CONFIRM_BEFORE_CLEARING] = value }
    }

    suspend fun setWidgetTransparency(value: Float) {
        dataStore.edit { it[Keys.WIDGET_TRANSPARENCY] = value.coerceIn(0f, 1f) }
    }
}
