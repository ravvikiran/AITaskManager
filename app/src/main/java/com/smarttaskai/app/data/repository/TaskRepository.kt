package com.smarttaskai.app.data.repository

import com.smarttaskai.app.data.local.dao.ProductivityLogDao
import com.smarttaskai.app.data.local.dao.SubTaskDao
import com.smarttaskai.app.data.local.dao.TaskDao
import com.smarttaskai.app.data.local.entity.ProductivityLogEntity
import com.smarttaskai.app.data.local.entity.SubTaskEntity
import com.smarttaskai.app.data.local.entity.TaskEntity
import com.smarttaskai.app.domain.model.Priority
import com.smarttaskai.app.domain.model.SubTask
import com.smarttaskai.app.domain.model.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import java.util.UUID

class TaskRepository(
    private val taskDao: TaskDao,
    private val subTaskDao: SubTaskDao,
    private val productivityLogDao: ProductivityLogDao
) {

    fun getAllTasks(): Flow<List<Task>> = taskDao.getAllTasks().map { entities ->
        entities.map { it.toDomain() }
    }

    fun getActiveTasks(): Flow<List<Task>> = taskDao.getActiveTasks().map { entities ->
        entities.map { it.toDomain() }
    }

    fun getCompletedTasks(): Flow<List<Task>> = taskDao.getCompletedTasks().map { entities ->
        entities.map { it.toDomain() }
    }

    fun getTodayTasks(): Flow<List<Task>> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfDay = calendar.timeInMillis

        return taskDao.getTodayTasks(startOfDay, endOfDay).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun getTaskById(id: String): Task? {
        return taskDao.getById(id)?.toDomain()
    }

    suspend fun createTask(task: Task): String {
        val id = if (task.id.isBlank()) UUID.randomUUID().toString() else task.id
        val entity = TaskEntity(
            id = id,
            title = task.title,
            description = task.description,
            categoryId = task.categoryId,
            priority = task.priority.value,
            estimatedDuration = task.estimatedDuration,
            actualDuration = task.actualDuration,
            isCompleted = task.isCompleted,
            createdAt = task.createdAt,
            completedAt = task.completedAt,
            scheduledAt = task.scheduledAt,
            dueDate = task.dueDate
        )
        taskDao.insert(entity)

        // Insert sub-tasks
        task.subTasks.forEach { subTask ->
            subTaskDao.insert(
                SubTaskEntity(
                    id = if (subTask.id.isBlank()) UUID.randomUUID().toString() else subTask.id,
                    taskId = id,
                    title = subTask.title,
                    isCompleted = subTask.isCompleted,
                    sortOrder = subTask.sortOrder
                )
            )
        }
        return id
    }

    suspend fun updateTask(task: Task) {
        val entity = TaskEntity(
            id = task.id,
            title = task.title,
            description = task.description,
            categoryId = task.categoryId,
            priority = task.priority.value,
            estimatedDuration = task.estimatedDuration,
            actualDuration = task.actualDuration,
            isCompleted = task.isCompleted,
            createdAt = task.createdAt,
            completedAt = task.completedAt,
            scheduledAt = task.scheduledAt,
            dueDate = task.dueDate
        )
        taskDao.update(entity)
    }

    suspend fun deleteTask(task: Task) {
        taskDao.delete(
            TaskEntity(
                id = task.id,
                title = task.title,
                priority = task.priority.value,
                estimatedDuration = task.estimatedDuration
            )
        )
    }

    suspend fun completeTask(taskId: String, actualDuration: Int?) {
        val now = System.currentTimeMillis()
        taskDao.markCompleted(taskId, now, actualDuration)

        // Log productivity data for ML
        val task = taskDao.getById(taskId)
        if (task != null) {
            val calendar = Calendar.getInstance()
            productivityLogDao.insert(
                ProductivityLogEntity(
                    taskId = taskId,
                    categoryId = task.categoryId,
                    priority = task.priority,
                    timeOfDay = calendar.get(Calendar.HOUR_OF_DAY),
                    dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK),
                    estimatedDuration = task.estimatedDuration,
                    completionTime = actualDuration ?: task.estimatedDuration
                )
            )
        }
    }

    suspend fun uncompleteTask(taskId: String) {
        taskDao.markIncomplete(taskId)
    }

    suspend fun deleteAllCompleted() {
        taskDao.deleteAllCompleted()
    }

    private fun TaskEntity.toDomain() = Task(
        id = id,
        title = title,
        description = description,
        categoryId = categoryId,
        priority = Priority.fromValue(priority),
        estimatedDuration = estimatedDuration,
        actualDuration = actualDuration,
        isCompleted = isCompleted,
        createdAt = createdAt,
        completedAt = completedAt,
        scheduledAt = scheduledAt,
        dueDate = dueDate
    )
}
