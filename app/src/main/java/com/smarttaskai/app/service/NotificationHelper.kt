package com.smarttaskai.app.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.smarttaskai.app.ui.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        const val TASK_REMINDER_CHANNEL_ID = "task_reminders"
        const val HABIT_NUDGE_CHANNEL_ID = "habit_nudges"
        const val TASK_REMINDER_NOTIFICATION_BASE_ID = 2000
        const val HABIT_NUDGE_NOTIFICATION_BASE_ID = 3000
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(NotificationManager::class.java)

            val taskChannel = NotificationChannel(
                TASK_REMINDER_CHANNEL_ID,
                "Task Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders for upcoming and overdue tasks"
            }

            val habitChannel = NotificationChannel(
                HABIT_NUDGE_CHANNEL_ID,
                "Habit Nudges",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily reminders to complete your habits"
            }

            notificationManager.createNotificationChannel(taskChannel)
            notificationManager.createNotificationChannel(habitChannel)
        }
    }

    fun showTaskReminder(taskId: String, taskTitle: String, message: String) {
        if (!hasNotificationPermission()) return

        val contentIntent = PendingIntent.getActivity(
            context,
            taskId.hashCode(),
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, TASK_REMINDER_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(taskTitle)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(
            TASK_REMINDER_NOTIFICATION_BASE_ID + taskId.hashCode(),
            notification
        )
    }

    fun showHabitNudge(habitId: String, habitName: String) {
        if (!hasNotificationPermission()) return

        val contentIntent = PendingIntent.getActivity(
            context,
            habitId.hashCode(),
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, HABIT_NUDGE_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("Don't break your streak! 🔥")
            .setContentText("Time to complete: $habitName")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(
            HABIT_NUDGE_NOTIFICATION_BASE_ID + habitId.hashCode(),
            notification
        )
    }

    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
}
