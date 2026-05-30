package com.smarttaskai.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("category_id")]
)
data class TaskEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    val title: String,

    val description: String? = null,

    @ColumnInfo(name = "category_id")
    val categoryId: String? = null,

    val priority: Int = 0, // 0=Low, 1=Medium, 2=High

    @ColumnInfo(name = "estimated_duration")
    val estimatedDuration: Int = 30, // minutes

    @ColumnInfo(name = "actual_duration")
    val actualDuration: Int? = null, // minutes

    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean = false,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "completed_at")
    val completedAt: Long? = null,

    @ColumnInfo(name = "scheduled_at")
    val scheduledAt: Long? = null,

    @ColumnInfo(name = "due_date")
    val dueDate: Long? = null
)
