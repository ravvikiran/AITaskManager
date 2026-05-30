package com.smarttaskai.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.smarttaskai.app.data.local.entity.ProductivityLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductivityLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: ProductivityLogEntity)

    @Query("SELECT * FROM productivity_logs ORDER BY created_at DESC")
    fun getAllLogs(): Flow<List<ProductivityLogEntity>>

    @Query("""
        SELECT * FROM productivity_logs 
        WHERE (category_id = :categoryId OR (:categoryId IS NULL AND category_id IS NULL))
        AND priority = :priority
        ORDER BY created_at DESC
        LIMIT 20
    """)
    suspend fun getLogsByCategoryAndPriority(categoryId: String?, priority: Int): List<ProductivityLogEntity>

    @Query("""
        SELECT * FROM productivity_logs 
        WHERE priority = :priority
        ORDER BY created_at DESC
        LIMIT 20
    """)
    suspend fun getLogsByPriority(priority: Int): List<ProductivityLogEntity>

    @Query("""
        SELECT AVG(completion_time) FROM productivity_logs 
        WHERE priority = :priority
    """)
    suspend fun getAverageCompletionTimeByPriority(priority: Int): Double?

    @Query("""
        SELECT AVG(completion_time) FROM productivity_logs 
        WHERE time_of_day BETWEEN :startHour AND :endHour
    """)
    suspend fun getAverageCompletionTimeByTimeRange(startHour: Int, endHour: Int): Double?

    @Query("SELECT COUNT(*) FROM productivity_logs")
    suspend fun getLogCount(): Int

    @Query("""
        SELECT time_of_day, COUNT(*) as count 
        FROM productivity_logs 
        GROUP BY time_of_day 
        ORDER BY count DESC 
        LIMIT 3
    """)
    suspend fun getMostProductiveHours(): List<ProductiveHourResult>

    @Query("SELECT * FROM productivity_logs WHERE created_at >= :since ORDER BY created_at DESC")
    suspend fun getLogsSince(since: Long): List<ProductivityLogEntity>
}

data class ProductiveHourResult(
    val time_of_day: Int,
    val count: Int
)
