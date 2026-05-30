package com.smarttaskai.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.smarttaskai.app.data.local.entity.HabitEntity
import com.smarttaskai.app.data.local.entity.HabitLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity)

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Delete
    suspend fun deleteHabit(habit: HabitEntity)

    @Query("SELECT * FROM habits ORDER BY created_at DESC")
    fun getAllHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getHabitById(id: String): HabitEntity?

    @Query("SELECT COUNT(*) FROM habits")
    suspend fun getHabitCount(): Int

    // Habit Logs
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabitLog(log: HabitLogEntity)

    @Query("SELECT * FROM habit_logs WHERE habit_id = :habitId ORDER BY completed_at DESC")
    fun getLogsForHabit(habitId: String): Flow<List<HabitLogEntity>>

    @Query("""
        SELECT * FROM habit_logs 
        WHERE habit_id = :habitId 
        AND completed_at BETWEEN :startOfDay AND :endOfDay
    """)
    suspend fun getLogForDate(habitId: String, startOfDay: Long, endOfDay: Long): List<HabitLogEntity>

    @Query("UPDATE habits SET streak_count = :streak, last_completed_at = :lastCompleted WHERE id = :habitId")
    suspend fun updateStreak(habitId: String, streak: Int, lastCompleted: Long)
}
