package com.smarttaskai.app.ui.screens.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarttaskai.app.data.local.dao.ProductivityLogDao
import com.smarttaskai.app.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class AnalyticsUiState(
    val tasksCompletedThisWeek: Int = 0,
    val averageDuration: Int = 0,
    val focusSessions: Int = 0,
    val mostProductiveHour: String = "--",
    val completionRate: Int = 0,
    val aiInsight: String = "Complete more tasks to unlock AI-powered productivity insights."
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val productivityLogDao: ProductivityLogDao,
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init {
        loadAnalytics()
    }

    private fun loadAnalytics() {
        viewModelScope.launch {
            // Get logs from this week
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            val weekStart = calendar.timeInMillis

            val weekLogs = productivityLogDao.getLogsSince(weekStart)
            val tasksCompleted = weekLogs.size
            val avgDuration = if (weekLogs.isNotEmpty()) {
                weekLogs.map { it.completionTime }.average().toInt()
            } else 0

            // Most productive hour
            val productiveHours = productivityLogDao.getMostProductiveHours()
            val bestHour = if (productiveHours.isNotEmpty()) {
                formatHour(productiveHours.first().time_of_day)
            } else "--"

            // Completion rate
            val allTasks = taskRepository.getAllTasks().first()
            val completedCount = allTasks.count { it.isCompleted }
            val rate = if (allTasks.isNotEmpty()) {
                (completedCount * 100) / allTasks.size
            } else 0

            // AI Insight
            val insight = generateInsight(tasksCompleted, avgDuration, bestHour)

            _uiState.update {
                it.copy(
                    tasksCompletedThisWeek = tasksCompleted,
                    averageDuration = avgDuration,
                    mostProductiveHour = bestHour,
                    completionRate = rate,
                    aiInsight = insight
                )
            }
        }
    }

    private fun generateInsight(tasksCompleted: Int, avgDuration: Int, bestHour: String): String {
        if (tasksCompleted == 0) {
            return "Complete more tasks to unlock AI-powered productivity insights."
        }

        val insights = mutableListOf<String>()

        if (bestHour != "--") {
            insights.add("You're most productive around $bestHour.")
        }

        if (avgDuration > 0) {
            insights.add("Your average task takes $avgDuration minutes.")
        }

        if (tasksCompleted >= 5) {
            insights.add("Great momentum this week with $tasksCompleted tasks completed!")
        }

        return insights.joinToString(" ").ifEmpty {
            "Keep completing tasks to build your productivity profile."
        }
    }

    private fun formatHour(hour: Int): String {
        val period = if (hour >= 12) "PM" else "AM"
        val displayHour = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        return "$displayHour $period"
    }
}
