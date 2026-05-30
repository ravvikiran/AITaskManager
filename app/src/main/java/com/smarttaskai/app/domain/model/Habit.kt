package com.smarttaskai.app.domain.model

data class Habit(
    val id: String = "",
    val name: String = "",
    val frequency: HabitFrequency = HabitFrequency.DAILY,
    val streakCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val lastCompletedAt: Long? = null,
    val color: Long = 0xFF4CAF50,
    val isCompletedToday: Boolean = false
)

enum class HabitFrequency(val label: String) {
    DAILY("Daily"),
    WEEKLY("Weekly")
}
