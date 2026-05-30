package com.smarttaskai.app.ui.screens.focus

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarttaskai.app.service.FocusTimerService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FocusUiState(
    val totalSeconds: Int = 25 * 60,
    val remainingSeconds: Int = 25 * 60,
    val isRunning: Boolean = false,
    val isBreak: Boolean = false,
    val sessionsCompleted: Int = 0,
    val focusDurationMinutes: Int = 25,
    val breakDurationMinutes: Int = 5
) {
    val progress: Float
        get() = if (totalSeconds > 0) remainingSeconds.toFloat() / totalSeconds else 0f

    val displayMinutes: Int get() = remainingSeconds / 60
    val displaySeconds: Int get() = remainingSeconds % 60

    val formattedTime: String
        get() = String.format("%02d:%02d", displayMinutes, displaySeconds)
}

@HiltViewModel
class FocusViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(FocusUiState())
    val uiState: StateFlow<FocusUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    fun startTimer() {
        if (_uiState.value.isRunning) return

        val state = _uiState.value
        _uiState.update { it.copy(isRunning = true) }

        // Start foreground service for persistence
        FocusTimerService.startTimer(context, state.remainingSeconds)

        // Local countdown for UI updates
        timerJob = viewModelScope.launch {
            while (_uiState.value.remainingSeconds > 0 && _uiState.value.isRunning) {
                delay(1000L)
                _uiState.update { it.copy(remainingSeconds = it.remainingSeconds - 1) }
            }

            if (_uiState.value.remainingSeconds <= 0) {
                onTimerComplete()
            }
        }
    }

    fun pauseTimer() {
        timerJob?.cancel()
        _uiState.update { it.copy(isRunning = false) }
        FocusTimerService.pauseTimer(context)
    }

    fun resetTimer() {
        timerJob?.cancel()
        FocusTimerService.stopTimer(context)

        val state = _uiState.value
        val totalSeconds = if (state.isBreak) state.breakDurationMinutes * 60
        else state.focusDurationMinutes * 60
        _uiState.update {
            it.copy(
                isRunning = false,
                remainingSeconds = totalSeconds,
                totalSeconds = totalSeconds
            )
        }
    }

    fun setFocusDuration(minutes: Int) {
        if (_uiState.value.isRunning) return
        val seconds = minutes * 60
        _uiState.update {
            it.copy(
                focusDurationMinutes = minutes,
                totalSeconds = seconds,
                remainingSeconds = seconds,
                isBreak = false
            )
        }
    }

    private fun onTimerComplete() {
        FocusTimerService.stopTimer(context)

        val state = _uiState.value
        if (state.isBreak) {
            // Break over, prepare next focus session
            val focusSeconds = state.focusDurationMinutes * 60
            _uiState.update {
                it.copy(
                    isRunning = false,
                    isBreak = false,
                    totalSeconds = focusSeconds,
                    remainingSeconds = focusSeconds
                )
            }
        } else {
            // Focus complete, start break
            val breakSeconds = state.breakDurationMinutes * 60
            _uiState.update {
                it.copy(
                    isRunning = false,
                    isBreak = true,
                    sessionsCompleted = it.sessionsCompleted + 1,
                    totalSeconds = breakSeconds,
                    remainingSeconds = breakSeconds
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
