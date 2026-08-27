package com.fliker.shiftscheduler.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

enum class AppTheme { SYSTEM, LIGHT, DARK }

data class UserPreferences(
    val theme: AppTheme,
    val dimWeekends: Boolean,
    val showWeekNumbers: Boolean
)

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val THEME = stringPreferencesKey("app_theme")
        val DIM_WEEKENDS = booleanPreferencesKey("dim_weekends")
        val SHOW_WEEK_NUMBERS = booleanPreferencesKey("show_week_numbers")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .map { preferences ->
            val theme = AppTheme.valueOf(
                preferences[PreferencesKeys.THEME] ?: AppTheme.SYSTEM.name
            )
            val dimWeekends = preferences[PreferencesKeys.DIM_WEEKENDS] ?: false
            val showWeekNumbers = preferences[PreferencesKeys.SHOW_WEEK_NUMBERS] ?: false
            UserPreferences(theme, dimWeekends, showWeekNumbers)
        }

    suspend fun updateTheme(theme: AppTheme) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME] = theme.name
        }
    }

    suspend fun updateDimWeekends(dim: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DIM_WEEKENDS] = dim
        }
    }

    suspend fun updateShowWeekNumbers(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_WEEK_NUMBERS] = show
        }
    }
}
