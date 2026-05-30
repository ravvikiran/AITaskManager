package com.smarttaskai.app.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.smarttaskai.app.data.local.dao.TaskDao
import com.smarttaskai.app.service.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.Calendar

@HiltWorker
class TaskReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val taskDao: TaskDao,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
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

            val todayTasks = taskDao.getTodayTasks(startOfDay, endOfDay).first()
            val incompleteTasks = todayTasks.filter { !it.isCompleted }

            if (incompleteTasks.isNotEmpty()) {
                val highPriority = incompleteTasks.filter { it.priority == 2 }
                if (highPriority.isNotEmpty()) {
                    notificationHelper.showTaskReminder(
                        taskId = highPriority.first().id,
                        taskTitle = "High Priority Tasks",
                        message = "${highPriority.size} high-priority task(s) remaining today"
                    )
                } else {
                    notificationHelper.showTaskReminder(
                        taskId = incompleteTasks.first().id,
                        taskTitle = "Tasks Remaining",
                        message = "${incompleteTasks.size} task(s) left for today"
                    )
                }
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
