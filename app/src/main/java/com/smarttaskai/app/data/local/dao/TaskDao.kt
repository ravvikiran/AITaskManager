package com.smarttaskai.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.smarttaskai.app.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity)

    @Update
    suspend fun update(task: TaskEntity)

    @Delete
    suspend fun delete(task: TaskEntity)

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getById(id: String): TaskEntity?

    @Query("SELECT * FROM tasks ORDER BY priority DESC, created_at DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE is_completed = 0 ORDER BY priority DESC, created_at DESC")
    fun getActiveTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE is_completed = 1 ORDER BY completed_at DESC")
    fun getCompletedTasks(): Flow<List<TaskEntity>>

    @Query("""
        SELECT * FROM tasks 
        WHERE is_completed = 0 
        AND (scheduled_at BETWEEN :startOfDay AND :endOfDay 
             OR due_date BETWEEN :startOfDay AND :endOfDay
             OR (scheduled_at IS NULL AND due_date IS NULL))
        ORDER BY priority DESC, created_at ASC
    """)
    fun getTodayTasks(startOfDay: Long, endOfDay: Long): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE category_id = :categoryId ORDER BY priority DESC")
    fun getTasksByCategory(categoryId: String): Flow<List<TaskEntity>>

    @Query("UPDATE tasks SET is_completed = 1, completed_at = :completedAt, actual_duration = :actualDuration WHERE id = :taskId")
    suspend fun markCompleted(taskId: String, completedAt: Long, actualDuration: Int?)

    @Query("UPDATE tasks SET is_completed = 0, completed_at = NULL, actual_duration = NULL WHERE id = :taskId")
    suspend fun markIncomplete(taskId: String)

    @Query("DELETE FROM tasks WHERE is_completed = 1")
    suspend fun deleteAllCompleted()
}
