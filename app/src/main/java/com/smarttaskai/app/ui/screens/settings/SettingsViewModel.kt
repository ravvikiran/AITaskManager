package com.smarttaskai.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarttaskai.app.data.preferences.UserPreferences
import com.smarttaskai.app.ml.EnergyPreference
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val isPremium: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val energyPreference: String = "morning",
    val darkMode: String = "system"
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userPreferences.isPremium.collect { isPremium ->
                _uiState.update { it.copy(isPremium = isPremium) }
            }
        }
        viewModelScope.launch {
            userPreferences.notificationsEnabled.collect { enabled ->
                _uiState.update { it.copy(notificationsEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            userPreferences.energyPreference.collect { pref ->
                val prefString = when (pref) {
                    EnergyPreference.MORNING_PERSON -> "morning"
                    EnergyPreference.NIGHT_OWL -> "night_owl"
                    EnergyPreference.BALANCED -> "balanced"
                }
                _uiState.update { it.copy(energyPreference = prefString) }
            }
        }
    }

    fun toggleNotifications() {
        viewModelScope.launch {
            val current = _uiState.value.notificationsEnabled
            userPreferences.setNotificationsEnabled(!current)
        }
    }

    fun setEnergyPreference(preference: String) {
        viewModelScope.launch {
            val energyPref = when (preference) {
                "night_owl" -> EnergyPreference.NIGHT_OWL
                "balanced" -> EnergyPreference.BALANCED
                else -> EnergyPreference.MORNING_PERSON
            }
            userPreferences.setEnergyPreference(energyPref)
        }
    }
}
