package com.smarttaskai.app.di

import android.content.Context
import androidx.room.Room
import com.smarttaskai.app.data.local.SmartTaskDatabase
import com.smarttaskai.app.data.local.dao.CategoryDao
import com.smarttaskai.app.data.local.dao.HabitDao
import com.smarttaskai.app.data.local.dao.ProductivityLogDao
import com.smarttaskai.app.data.local.dao.SubTaskDao
import com.smarttaskai.app.data.local.dao.TaskDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SmartTaskDatabase {
        return Room.databaseBuilder(
            context,
            SmartTaskDatabase::class.java,
            "smart_task_ai.db"
        )
            .addCallback(com.smarttaskai.app.data.local.DatabaseCallback())
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideTaskDao(database: SmartTaskDatabase): TaskDao = database.taskDao()

    @Provides
    fun provideSubTaskDao(database: SmartTaskDatabase): SubTaskDao = database.subTaskDao()

    @Provides
    fun provideCategoryDao(database: SmartTaskDatabase): CategoryDao = database.categoryDao()

    @Provides
    fun provideHabitDao(database: SmartTaskDatabase): HabitDao = database.habitDao()

    @Provides
    fun provideProductivityLogDao(database: SmartTaskDatabase): ProductivityLogDao = database.productivityLogDao()
}
