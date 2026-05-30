package com.smarttaskai.app.ui.screens.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarttaskai.app.data.repository.HabitRepository
import com.smarttaskai.app.domain.model.Habit
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HabitsUiState(
    val habits: List<Habit> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class HabitsViewModel @Inject constructor(
    private val habitRepository: HabitRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HabitsUiState())
    val uiState: StateFlow<HabitsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            habitRepository.getAllHabits().collect { habits ->
                val habitsWithStatus = habits.map { habit ->
                    habit.copy(isCompletedToday = habitRepository.isHabitCompletedToday(habit.id))
                }
                _uiState.update { it.copy(habits = habitsWithStatus, isLoading = false) }
            }
        }
    }

    fun completeHabit(habitId: String) {
        viewModelScope.launch {
            habitRepository.completeHabitToday(habitId)
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            habitRepository.deleteHabit(habit)
        }
    }
}
