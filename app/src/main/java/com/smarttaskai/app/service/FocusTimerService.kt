package com.smarttaskai.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.smarttaskai.app.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FocusTimerService : Service() {

    companion object {
        const val CHANNEL_ID = "focus_timer_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_START = "com.smarttaskai.app.ACTION_START_TIMER"
        const val ACTION_PAUSE = "com.smarttaskai.app.ACTION_PAUSE_TIMER"
        const val ACTION_RESUME = "com.smarttaskai.app.ACTION_RESUME_TIMER"
        const val ACTION_STOP = "com.smarttaskai.app.ACTION_STOP_TIMER"
        const val EXTRA_DURATION_SECONDS = "extra_duration_seconds"

        private val _timerState = MutableStateFlow(TimerServiceState())
        val timerState: StateFlow<TimerServiceState> = _timerState.asStateFlow()

        fun startTimer(context: Context, durationSeconds: Int) {
            val intent = Intent(context, FocusTimerService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_DURATION_SECONDS, durationSeconds)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun pauseTimer(context: Context) {
            val intent = Intent(context, FocusTimerService::class.java).apply {
                action = ACTION_PAUSE
            }
            context.startService(intent)
        }

        fun resumeTimer(context: Context) {
            val intent = Intent(context, FocusTimerService::class.java).apply {
                action = ACTION_RESUME
            }
            context.startService(intent)
        }

        fun stopTimer(context: Context) {
            val intent = Intent(context, FocusTimerService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var timerJob: Job? = null
    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): FocusTimerService = this@FocusTimerService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val duration = intent.getIntExtra(EXTRA_DURATION_SECONDS, 25 * 60)
                startFocusTimer(duration)
            }
            ACTION_PAUSE -> pauseFocusTimer()
            ACTION_RESUME -> resumeFocusTimer()
            ACTION_STOP -> stopFocusTimer()
            else -> {
                // Service restarted by system without intent — stop gracefully
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun startFocusTimer(durationSeconds: Int) {
        _timerState.value = TimerServiceState(
            totalSeconds = durationSeconds,
            remainingSeconds = durationSeconds,
            isRunning = true,
            isActive = true
        )

        startForeground(NOTIFICATION_ID, buildNotification(durationSeconds))
        startCountdown()
    }

    private fun pauseFocusTimer() {
        timerJob?.cancel()
        _timerState.value = _timerState.value.copy(isRunning = false)
        updateNotification()
    }

    private fun resumeFocusTimer() {
        _timerState.value = _timerState.value.copy(isRunning = true)
        startCountdown()
        updateNotification()
    }

    private fun stopFocusTimer() {
        timerJob?.cancel()
        _timerState.value = TimerServiceState()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun startCountdown() {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (_timerState.value.remainingSeconds > 0 && _timerState.value.isRunning) {
                delay(1000L)
                val current = _timerState.value
                if (current.isRunning) {
                    val newRemaining = current.remainingSeconds - 1
                    _timerState.value = current.copy(remainingSeconds = newRemaining)

                    // Update notification every 15 seconds to save battery
                    if (newRemaining % 15 == 0) {
                        updateNotification()
                    }
                }
            }

            if (_timerState.value.remainingSeconds <= 0) {
                onTimerComplete()
            }
        }
    }

    private fun onTimerComplete() {
        val state = _timerState.value
        _timerState.value = state.copy(
            isRunning = false,
            isActive = false,
            isCompleted = true
        )

        // Show completion notification
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(
            NOTIFICATION_ID + 1,
            buildCompletionNotification()
        )

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Focus Timer",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows the focus timer progress"
                setShowBadge(false)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(remainingSeconds: Int = _timerState.value.remainingSeconds): Notification {
        val minutes = remainingSeconds / 60
        val seconds = remainingSeconds % 60
        val timeText = String.format("%02d:%02d", minutes, seconds)

        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val pauseResumeAction = if (_timerState.value.isRunning) {
            val pauseIntent = PendingIntent.getService(
                this, 1,
                Intent(this, FocusTimerService::class.java).apply { action = ACTION_PAUSE },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            NotificationCompat.Action(
                android.R.drawable.ic_media_pause, "Pause", pauseIntent
            )
        } else {
            val resumeIntent = PendingIntent.getService(
                this, 2,
                Intent(this, FocusTimerService::class.java).apply { action = ACTION_RESUME },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            NotificationCompat.Action(
                android.R.drawable.ic_media_play, "Resume", resumeIntent
            )
        }

        val stopIntent = PendingIntent.getService(
            this, 3,
            Intent(this, FocusTimerService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val stopAction = NotificationCompat.Action(
            android.R.drawable.ic_delete, "Stop", stopIntent
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Focus Mode Active")
            .setContentText("$timeText remaining")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentIntent(contentIntent)
            .setOngoing(true)
            .setSilent(true)
            .addAction(pauseResumeAction)
            .addAction(stopAction)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .build()
    }

    private fun buildCompletionNotification(): Notification {
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Focus Session Complete! 🎉")
            .setContentText("Great work! Time for a break.")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
    }

    private fun updateNotification() {
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, buildNotification())
    }

    override fun onDestroy() {
        super.onDestroy()
        timerJob?.cancel()
        serviceScope.cancel()
    }
}

data class TimerServiceState(
    val totalSeconds: Int = 0,
    val remainingSeconds: Int = 0,
    val isRunning: Boolean = false,
    val isActive: Boolean = false,
    val isCompleted: Boolean = false
) {
    val progress: Float
        get() = if (totalSeconds > 0) remainingSeconds.toFloat() / totalSeconds else 0f

    val formattedTime: String
        get() {
            val minutes = remainingSeconds / 60
            val seconds = remainingSeconds % 60
            return String.format("%02d:%02d", minutes, seconds)
        }
}
