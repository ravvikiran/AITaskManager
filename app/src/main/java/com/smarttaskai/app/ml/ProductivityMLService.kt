package com.smarttaskai.app.ml

import com.smarttaskai.app.data.local.dao.ProductivityLogDao
import com.smarttaskai.app.domain.model.Priority
import com.smarttaskai.app.domain.model.Task
import java.util.Calendar

/**
 * On-device ML service for productivity predictions.
 *
 * Uses a lightweight statistical approach (weighted moving average + linear regression)
 * to predict task durations and suggest optimal scheduling.
 * No external API calls — all inference happens locally.
 *
 * When insufficient data exists (cold start), sensible defaults are provided.
 */
class ProductivityMLService(
    private val productivityLogDao: ProductivityLogDao
) {

    companion object {
        // Default durations (minutes) when no historical data exists
        private val DEFAULT_DURATIONS = mapOf(
            Priority.LOW to 15,
            Priority.MEDIUM to 30,
            Priority.HIGH to 60
        )

        private const val MIN_DATA_POINTS = 3
    }

    /**
     * Predicts how long a task will take based on historical data.
     * Falls back to sensible defaults when insufficient data is available.
     */
    suspend fun predictDuration(
        categoryId: String?,
        priority: Priority,
        timeOfDay: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    ): PredictionResult {
        val logCount = productivityLogDao.getLogCount()

        if (logCount < MIN_DATA_POINTS) {
            return PredictionResult(
                predictedMinutes = DEFAULT_DURATIONS[priority] ?: 30,
                confidence = Confidence.LOW,
                basedOnDataPoints = 0
            )
        }

        // Get historical data for similar tasks
        val similarLogs = productivityLogDao.getLogsByCategoryAndPriority(categoryId, priority.value)

        if (similarLogs.isEmpty()) {
            // Fall back to priority-based average
            val avgByPriority = productivityLogDao.getAverageCompletionTimeByPriority(priority.value)
            return PredictionResult(
                predictedMinutes = avgByPriority?.toInt() ?: (DEFAULT_DURATIONS[priority] ?: 30),
                confidence = Confidence.MEDIUM,
                basedOnDataPoints = logCount
            )
        }

        // Weighted moving average: recent tasks weigh more
        val weightedSum = similarLogs.mapIndexed { index, log ->
            val weight = (similarLogs.size - index).toDouble() / similarLogs.size
            log.completionTime * weight
        }.sum()

        val totalWeight = similarLogs.mapIndexed { index, _ ->
            (similarLogs.size - index).toDouble() / similarLogs.size
        }.sum()

        val predicted = (weightedSum / totalWeight).toInt()

        // Apply time-of-day adjustment
        val timeAdjusted = applyTimeOfDayFactor(predicted, timeOfDay)

        val confidence = when {
            similarLogs.size >= 10 -> Confidence.HIGH
            similarLogs.size >= 5 -> Confidence.MEDIUM
            else -> Confidence.LOW
        }

        return PredictionResult(
            predictedMinutes = timeAdjusted.coerceIn(5, 480), // 5 min to 8 hours
            confidence = confidence,
            basedOnDataPoints = similarLogs.size
        )
    }

    /**
     * Generates an optimal daily schedule by fitting tasks into time blocks.
     * Uses a priority-weighted knapsack approach combined with energy level heuristics.
     */
    suspend fun generateDailySchedule(
        tasks: List<Task>,
        userPreference: EnergyPreference = EnergyPreference.MORNING_PERSON,
        availableHours: Int = 8
    ): List<ScheduledBlock> {
        if (tasks.isEmpty()) return emptyList()

        val sortedTasks = tasks.sortedByDescending { it.priority.value * 10 + (if (it.dueDate != null) 5 else 0) }

        val highEnergyWindow = when (userPreference) {
            EnergyPreference.MORNING_PERSON -> 8..12
            EnergyPreference.NIGHT_OWL -> 18..22
            EnergyPreference.BALANCED -> 10..14
        }

        val schedule = mutableListOf<ScheduledBlock>()
        var currentMinute = highEnergyWindow.first * 60
        val endMinute = currentMinute + (availableHours * 60)

        for (task in sortedTasks) {
            if (currentMinute + task.estimatedDuration > endMinute) break

            val isHighEnergy = (currentMinute / 60) in highEnergyWindow
            val block = ScheduledBlock(
                task = task,
                startMinute = currentMinute,
                endMinute = currentMinute + task.estimatedDuration,
                isHighEnergySlot = isHighEnergy
            )
            schedule.add(block)
            currentMinute += task.estimatedDuration + 10 // 10-min buffer between tasks
        }

        return schedule
    }

    /**
     * Adjusts predicted duration based on time of day.
     * People tend to be slower in early morning and late evening.
     */
    private fun applyTimeOfDayFactor(baseDuration: Int, hour: Int): Int {
        val factor = when (hour) {
            in 6..9 -> 1.0    // Morning: normal
            in 10..12 -> 0.9  // Late morning: slightly faster (peak focus)
            in 13..14 -> 1.1  // Post-lunch: slightly slower
            in 15..17 -> 0.95 // Afternoon: good focus
            in 18..21 -> 1.05 // Evening: slightly slower
            else -> 1.15      // Late night/early morning: slower
        }
        return (baseDuration * factor).toInt()
    }
}

data class PredictionResult(
    val predictedMinutes: Int,
    val confidence: Confidence,
    val basedOnDataPoints: Int
)

enum class Confidence(val label: String) {
    LOW("Low confidence - using defaults"),
    MEDIUM("Moderate confidence"),
    HIGH("High confidence - based on your history")
}

enum class EnergyPreference(val label: String) {
    MORNING_PERSON("Morning Person (8 AM - 12 PM peak)"),
    NIGHT_OWL("Night Owl (6 PM - 10 PM peak)"),
    BALANCED("Balanced (10 AM - 2 PM peak)")
}

data class ScheduledBlock(
    val task: Task,
    val startMinute: Int, // minutes from midnight
    val endMinute: Int,
    val isHighEnergySlot: Boolean
) {
    val startTimeFormatted: String
        get() {
            val hour = startMinute / 60
            val minute = startMinute % 60
            val period = if (hour >= 12) "PM" else "AM"
            val displayHour = if (hour > 12) hour - 12 else if (hour == 0) 12 else hour
            return String.format("%d:%02d %s", displayHour, minute, period)
        }

    val endTimeFormatted: String
        get() {
            val hour = endMinute / 60
            val minute = endMinute % 60
            val period = if (hour >= 12) "PM" else "AM"
            val displayHour = if (hour > 12) hour - 12 else if (hour == 0) 12 else hour
            return String.format("%d:%02d %s", displayHour, minute, period)
        }
}
