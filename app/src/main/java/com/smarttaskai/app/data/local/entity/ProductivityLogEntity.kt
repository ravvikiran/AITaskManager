package com.smarttaskai.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "productivity_logs",
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["task_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("task_id")]
)
data class ProductivityLogEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "task_id")
    val taskId: String,

    @ColumnInfo(name = "category_id")
    val categoryId: String? = null,

    val priority: Int = 0,

    @ColumnInfo(name = "time_of_day")
    val timeOfDay: Int = 0, // 0-23 hour

    @ColumnInfo(name = "day_of_week")
    val dayOfWeek: Int = 1, // 1=Monday, 7=Sunday

    @ColumnInfo(name = "estimated_duration")
    val estimatedDuration: Int = 0,

    @ColumnInfo(name = "completion_time")
    val completionTime: Int = 0, // actual minutes taken

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
