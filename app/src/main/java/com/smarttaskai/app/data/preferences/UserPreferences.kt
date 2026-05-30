package com.smarttaskai.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.smarttaskai.app.ml.EnergyPreference
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private val KEY_IS_PREMIUM = booleanPreferencesKey("is_premium")
        private val KEY_DARK_MODE = stringPreferencesKey("dark_mode") // "system", "light", "dark"
        private val KEY_ENERGY_PREFERENCE = stringPreferencesKey("energy_preference")
        private val KEY_FOCUS_DURATION = intPreferencesKey("focus_duration_minutes")
        private val KEY_BREAK_DURATION = intPreferencesKey("break_duration_minutes")
        private val KEY_NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        private val KEY_ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
    }

    val isPremium: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_IS_PREMIUM] ?: false
    }

    val darkMode: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_DARK_MODE] ?: "system"
    }

    val energyPreference: Flow<EnergyPreference> = context.dataStore.data.map { prefs ->
        when (prefs[KEY_ENERGY_PREFERENCE]) {
            "night_owl" -> EnergyPreference.NIGHT_OWL
            "balanced" -> EnergyPreference.BALANCED
            else -> EnergyPreference.MORNING_PERSON
        }
    }

    val focusDuration: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_FOCUS_DURATION] ?: 25
    }

    val breakDuration: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_BREAK_DURATION] ?: 5
    }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_NOTIFICATIONS_ENABLED] ?: true
    }

    val onboardingComplete: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_ONBOARDING_COMPLETE] ?: false
    }

    suspend fun setPremium(isPremium: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_IS_PREMIUM] = isPremium
        }
    }

    suspend fun setDarkMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DARK_MODE] = mode
        }
    }

    suspend fun setEnergyPreference(preference: EnergyPreference) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ENERGY_PREFERENCE] = when (preference) {
                EnergyPreference.MORNING_PERSON -> "morning"
                EnergyPreference.NIGHT_OWL -> "night_owl"
                EnergyPreference.BALANCED -> "balanced"
            }
        }
    }

    suspend fun setFocusDuration(minutes: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_FOCUS_DURATION] = minutes
        }
    }

    suspend fun setBreakDuration(minutes: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_BREAK_DURATION] = minutes
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun setOnboardingComplete() {
        context.dataStore.edit { prefs ->
            prefs[KEY_ONBOARDING_COMPLETE] = true
        }
    }
}
