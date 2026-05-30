package com.smarttaskai.app.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Schedules periodic background work for reminders.
 * Uses WorkManager for battery-efficient scheduling.
 */
@Singleton
class WorkManagerScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val HABIT_REMINDER_WORK = "habit_reminder_work"
        private const val TASK_REMINDER_WORK = "task_reminder_work"
    }

    /**
     * Schedule all periodic reminders.
     * Call this once during app initialization.
     */
    fun scheduleAllReminders() {
        scheduleHabitReminders()
        scheduleTaskReminders()
    }

    /**
     * Schedules habit reminders to run every 8 hours.
     * This ensures users get nudged about incomplete habits
     * without being too aggressive on battery.
     */
    private fun scheduleHabitReminders() {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()

        val habitReminderRequest = PeriodicWorkRequestBuilder<HabitReminderWorker>(
            8, TimeUnit.HOURS,
            1, TimeUnit.HOURS // flex interval
        )
            .setConstraints(constraints)
            .setInitialDelay(calculateInitialDelay(9), TimeUnit.MILLISECONDS) // First run at 9 AM
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            HABIT_REMINDER_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            habitReminderRequest
        )
    }

    /**
     * Schedules task reminders to run every 4 hours during the day.
     */
    private fun scheduleTaskReminders() {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()

        val taskReminderRequest = PeriodicWorkRequestBuilder<TaskReminderWorker>(
            4, TimeUnit.HOURS,
            30, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setInitialDelay(calculateInitialDelay(8), TimeUnit.MILLISECONDS) // First run at 8 AM
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            TASK_REMINDER_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            taskReminderRequest
        )
    }

    /**
     * Cancels all scheduled reminders.
     */
    fun cancelAllReminders() {
        WorkManager.getInstance(context).cancelUniqueWork(HABIT_REMINDER_WORK)
        WorkManager.getInstance(context).cancelUniqueWork(TASK_REMINDER_WORK)
    }

    /**
     * Calculates the delay until the next occurrence of the target hour.
     */
    private fun calculateInitialDelay(targetHour: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, targetHour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (target.before(now)) {
            target.add(Calendar.DAY_OF_MONTH, 1)
        }

        return target.timeInMillis - now.timeInMillis
    }
}
