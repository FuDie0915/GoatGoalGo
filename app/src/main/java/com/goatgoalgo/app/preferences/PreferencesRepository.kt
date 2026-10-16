package com.goatgoalgo.app.preferences

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.goatGoalGoDataStore by preferencesDataStore(name = "goatgoalgo_preferences")

class PreferencesRepository(private val context: Context) {
    private object Keys {
        val themeMode = stringPreferencesKey("theme_mode")
        val globalPalette = stringPreferencesKey("global_palette")
        val expandedNodeIds = stringSetPreferencesKey("expanded_node_ids")
        val roleOverrides = ColorRole.entries.associateWith {
            stringPreferencesKey("palette_override_" + it.wireName.lowercase())
        }
    }

    val settings: Flow<AppPreferences> = context.goatGoalGoDataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { it.toAppPreferences() }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.goatGoalGoDataStore.edit { it[Keys.themeMode] = mode.wireName }
    }

    suspend fun setGlobalPalette(palette: PaletteId) {
        context.goatGoalGoDataStore.edit { it[Keys.globalPalette] = palette.wireName }
    }

    suspend fun setRolePalette(role: ColorRole, palette: PaletteId?) {
        context.goatGoalGoDataStore.edit { preferences ->
            val key = Keys.roleOverrides.getValue(role)
            if (palette == null) preferences.remove(key) else preferences[key] = palette.wireName
        }
    }

    suspend fun setExpandedNodeIds(ids: Set<String>) {
        context.goatGoalGoDataStore.edit { it[Keys.expandedNodeIds] = ids }
    }

    private fun Preferences.toAppPreferences(): AppPreferences {
        val overrides = ColorRole.entries.mapNotNull { role ->
            get(Keys.roleOverrides.getValue(role))?.let { role to PaletteId.fromWire(it) }
        }.toMap()
        return AppPreferences(
            themeMode = ThemeMode.fromWire(get(Keys.themeMode).orEmpty()),
            globalPalette = PaletteId.fromWire(get(Keys.globalPalette).orEmpty()),
            overrides = overrides,
            expandedNodeIds = get(Keys.expandedNodeIds).orEmpty(),
        )
    }
}
