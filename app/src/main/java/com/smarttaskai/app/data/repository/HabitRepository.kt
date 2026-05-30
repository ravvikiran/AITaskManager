package com.smarttaskai.app.data.repository

import com.smarttaskai.app.data.local.dao.HabitDao
import com.smarttaskai.app.data.local.entity.HabitEntity
import com.smarttaskai.app.data.local.entity.HabitLogEntity
import com.smarttaskai.app.domain.model.Habit
import com.smarttaskai.app.domain.model.HabitFrequency
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import java.util.UUID

class HabitRepository(
    private val habitDao: HabitDao
) {

    fun getAllHabits(): Flow<List<Habit>> = habitDao.getAllHabits().map { entities ->
        entities.map { it.toDomain() }
    }

    suspend fun getHabitById(id: String): Habit? {
        return habitDao.getHabitById(id)?.toDomain()
    }

    suspend fun getHabitCount(): Int = habitDao.getHabitCount()

    suspend fun createHabit(habit: Habit) {
        val entity = HabitEntity(
            id = if (habit.id.isBlank()) UUID.randomUUID().toString() else habit.id,
            name = habit.name,
            frequency = habit.frequency.label,
            streakCount = habit.streakCount,
            createdAt = habit.createdAt,
            lastCompletedAt = habit.lastCompletedAt,
            color = habit.color
        )
        habitDao.insertHabit(entity)
    }

    suspend fun updateHabit(habit: Habit) {
        val entity = HabitEntity(
            id = habit.id,
            name = habit.name,
            frequency = habit.frequency.label,
            streakCount = habit.streakCount,
            createdAt = habit.createdAt,
            lastCompletedAt = habit.lastCompletedAt,
            color = habit.color
        )
        habitDao.updateHabit(entity)
    }

    suspend fun deleteHabit(habit: Habit) {
        habitDao.deleteHabit(
            HabitEntity(
                id = habit.id,
                name = habit.name,
                frequency = habit.frequency.label
            )
        )
    }

    suspend fun completeHabitToday(habitId: String) {
        val now = System.currentTimeMillis()
        habitDao.insertHabitLog(
            HabitLogEntity(
                habitId = habitId,
                completedAt = now
            )
        )

        // Update streak
        val habit = habitDao.getHabitById(habitId) ?: return
        val newStreak = habit.streakCount + 1
        habitDao.updateStreak(habitId, newStreak, now)
    }

    suspend fun isHabitCompletedToday(habitId: String): Boolean {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfDay = calendar.timeInMillis

        return habitDao.getLogForDate(habitId, startOfDay, endOfDay).isNotEmpty()
    }

    private fun HabitEntity.toDomain() = Habit(
        id = id,
        name = name,
        frequency = when (frequency) {
            "Weekly" -> HabitFrequency.WEEKLY
            else -> HabitFrequency.DAILY
        },
        streakCount = streakCount,
        createdAt = createdAt,
        lastCompletedAt = lastCompletedAt,
        color = color
    )
}
