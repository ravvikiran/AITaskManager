package com.smarttaskai.app.ui.screens.onboarding

import com.smarttaskai.app.data.preferences.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages the tutorial/onboarding state across the app.
 * Controls both the initial onboarding flow and the in-app
 * interactive tutorial overlay.
 */
@Singleton
class TutorialManager @Inject constructor(
    private val userPreferences: UserPreferences
) {
    private val _showOnboarding = MutableStateFlow(false)
    val showOnboarding: StateFlow<Boolean> = _showOnboarding.asStateFlow()

    private val _showDashboardTutorial = MutableStateFlow(false)
    val showDashboardTutorial: StateFlow<Boolean> =
        _showDashboardTutorial.asStateFlow()

    private val _currentTutorialStep = MutableStateFlow(0)
    val currentTutorialStep: StateFlow<Int> =
        _currentTutorialStep.asStateFlow()

    /**
     * Check if onboarding should be shown (first launch).
     */
    suspend fun checkFirstLaunch() {
        val isComplete = userPreferences.onboardingComplete.first()
        _showOnboarding.value = !isComplete
    }

    /**
     * Called when the onboarding pages are finished.
     * Transitions to the interactive dashboard tutorial.
     */
    suspend fun completeOnboarding() {
        userPreferences.setOnboardingComplete()
        _showOnboarding.value = false
        _showDashboardTutorial.value = true
        _currentTutorialStep.value = 0
    }

    /**
     * Advance to the next tutorial step.
     */
    fun nextTutorialStep() {
        val current = _currentTutorialStep.value
        if (current < dashboardTutorialSteps.size - 1) {
            _currentTutorialStep.value = current + 1
        } else {
            dismissTutorial()
        }
    }

    /**
     * Dismiss the interactive tutorial overlay.
     */
    fun dismissTutorial() {
        _showDashboardTutorial.value = false
        _currentTutorialStep.value = 0
    }

    /**
     * Replay the tutorial from settings.
     */
    fun replayTutorial() {
        _showOnboarding.value = true
    }

    /**
     * Replay only the interactive overlay (skip onboarding pages).
     */
    fun replayDashboardTutorial() {
        _showDashboardTutorial.value = true
        _currentTutorialStep.value = 0
    }
}
