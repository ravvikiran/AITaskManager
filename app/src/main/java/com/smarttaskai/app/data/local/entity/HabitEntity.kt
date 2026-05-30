package com.smarttaskai.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    val name: String,

    val frequency: String = "Daily", // Daily, Weekly

    @ColumnInfo(name = "streak_count")
    val streakCount: Int = 0,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "last_completed_at")
    val lastCompletedAt: Long? = null,

    val color: Long = 0xFF4CAF50 // Default green
)
