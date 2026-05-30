package com.smarttaskai.app.di

import com.smarttaskai.app.data.local.dao.HabitDao
import com.smarttaskai.app.data.local.dao.ProductivityLogDao
import com.smarttaskai.app.data.local.dao.SubTaskDao
import com.smarttaskai.app.data.local.dao.TaskDao
import com.smarttaskai.app.data.repository.HabitRepository
import com.smarttaskai.app.data.repository.TaskRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideTaskRepository(
        taskDao: TaskDao,
        subTaskDao: SubTaskDao,
        productivityLogDao: ProductivityLogDao
    ): TaskRepository {
        return TaskRepository(taskDao, subTaskDao, productivityLogDao)
    }

    @Provides
    @Singleton
    fun provideHabitRepository(
        habitDao: HabitDao
    ): HabitRepository {
        return HabitRepository(habitDao)
    }
}
