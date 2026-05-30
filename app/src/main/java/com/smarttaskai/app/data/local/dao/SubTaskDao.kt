package com.smarttaskai.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.smarttaskai.app.data.local.entity.SubTaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SubTaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(subTask: SubTaskEntity)

    @Update
    suspend fun update(subTask: SubTaskEntity)

    @Delete
    suspend fun delete(subTask: SubTaskEntity)

    @Query("SELECT * FROM sub_tasks WHERE task_id = :taskId ORDER BY sort_order ASC")
    fun getSubTasksForTask(taskId: String): Flow<List<SubTaskEntity>>

    @Query("UPDATE sub_tasks SET is_completed = :isCompleted WHERE id = :subTaskId")
    suspend fun toggleCompletion(subTaskId: String, isCompleted: Boolean)

    @Query("DELETE FROM sub_tasks WHERE task_id = :taskId")
    suspend fun deleteAllForTask(taskId: String)
}
