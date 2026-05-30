package com.smarttaskai.app.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarttaskai.app.data.repository.HabitRepository
import com.smarttaskai.app.data.repository.TaskRepository
import com.smarttaskai.app.domain.model.Habit
import com.smarttaskai.app.domain.model.Task
import com.smarttaskai.app.ml.EnergyPreference
import com.smarttaskai.app.ml.ProductivityMLService
import com.smarttaskai.app.ml.ScheduledBlock
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val todayTasks: List<Task> = emptyList(),
    val habits: List<Habit> = emptyList(),
    val aiSchedule: List<ScheduledBlock> = emptyList(),
    val isLoading: Boolean = true,
    val greeting: String = ""
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val habitRepository: HabitRepository,
    private val mlService: ProductivityMLService
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    private fun loadDashboard() {
        viewModelScope.launch {
            _uiState.update { it.copy(greeting = getGreeting()) }

            // Observe today's tasks
            taskRepository.getTodayTasks().collect { tasks ->
                _uiState.update { it.copy(todayTasks = tasks, isLoading = false) }
                generateAISchedule(tasks)
            }
        }

        viewModelScope.launch {
            habitRepository.getAllHabits().collect { habits ->
                val habitsWithStatus = habits.map { habit ->
                    habit.copy(isCompletedToday = habitRepository.isHabitCompletedToday(habit.id))
                }
                _uiState.update { it.copy(habits = habitsWithStatus) }
            }
        }
    }

    private suspend fun generateAISchedule(tasks: List<Task>) {
        val activeTasks = tasks.filter { !it.isCompleted }
        if (activeTasks.isEmpty()) {
            _uiState.update { it.copy(aiSchedule = emptyList()) }
            return
        }

        val schedule = mlService.generateDailySchedule(
            tasks = activeTasks,
            userPreference = EnergyPreference.MORNING_PERSON
        )
        _uiState.update { it.copy(aiSchedule = schedule) }
    }

    fun completeTask(taskId: String) {
        viewModelScope.launch {
            taskRepository.completeTask(taskId, null)
        }
    }

    fun uncompleteTask(taskId: String) {
        viewModelScope.launch {
            taskRepository.uncompleteTask(taskId)
        }
    }

    fun completeHabit(habitId: String) {
        viewModelScope.launch {
            habitRepository.completeHabitToday(habitId)
        }
    }

    private fun getGreeting(): String {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            in 17..20 -> "Good Evening"
            else -> "Good Night"
        }
    }
}
