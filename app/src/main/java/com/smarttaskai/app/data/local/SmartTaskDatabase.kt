package com.smarttaskai.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.smarttaskai.app.data.local.dao.CategoryDao
import com.smarttaskai.app.data.local.dao.HabitDao
import com.smarttaskai.app.data.local.dao.ProductivityLogDao
import com.smarttaskai.app.data.local.dao.SubTaskDao
import com.smarttaskai.app.data.local.dao.TaskDao
import com.smarttaskai.app.data.local.entity.CategoryEntity
import com.smarttaskai.app.data.local.entity.HabitEntity
import com.smarttaskai.app.data.local.entity.HabitLogEntity
import com.smarttaskai.app.data.local.entity.ProductivityLogEntity
import com.smarttaskai.app.data.local.entity.SubTaskEntity
import com.smarttaskai.app.data.local.entity.TaskEntity

@Database(
    entities = [
        TaskEntity::class,
        SubTaskEntity::class,
        CategoryEntity::class,
        HabitEntity::class,
        HabitLogEntity::class,
        ProductivityLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SmartTaskDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun subTaskDao(): SubTaskDao
    abstract fun categoryDao(): CategoryDao
    abstract fun habitDao(): HabitDao
    abstract fun productivityLogDao(): ProductivityLogDao
}
