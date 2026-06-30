package com.smarttaskai.app.data.export

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.smarttaskai.app.data.local.dao.HabitDao
import com.smarttaskai.app.data.local.dao.ProductivityLogDao
import com.smarttaskai.app.data.local.dao.TaskDao
import com.smarttaskai.app.data.preferences.UserPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages productivity scoring, local file persistence, and sharing via email/social.
 *
 * Scores are saved locally as JSON files with a unique user identifier.
 * No backend server required — sharing is done via Android's share intent (email, etc.).
 */
@Singleton
class ProductivityScoreManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val taskDao: TaskDao,
    private val habitDao: HabitDao,
    private val productivityLogDao: ProductivityLogDao,
    private val userPreferences: UserPreferences
) {

    companion object {
        private const val SCORES_DIR = "productivity_scores"
        private const val SCORE_FILE_PREFIX = "score_"
        private const val USER_ID_KEY = "user_unique_id"
    }

    /**
     * Generates a comprehensive productivity score based on user activity.
     */
    suspend fun calculateProductivityScore(): ProductivityScore {
        val calendar = Calendar.getInstance()

        // This week's data
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val weekStart = calendar.timeInMillis

        val weekLogs = productivityLogDao.getLogsSince(weekStart)
        val allTasks = taskDao.getAllTasks().first()
        val habits = habitDao.getAllHabits().first()

        val completedTasks = allTasks.count { it.isCompleted }
        val totalTasks = allTasks.size

        // Task completion rate (0-100)
        val taskCompletionRate = if (totalTasks > 0) {
            (completedTasks * 100) / totalTasks
        } else 0

        // Consistency score: how many days this week had completed tasks
        val daysActive = weekLogs.map { log ->
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(log.createdAt))
        }.distinct().size
        val consistencyScore = (daysActive * 100) / 7

        // Habit streak bonus
        val totalStreaks = habits.sumOf { it.streakCount }
        val habitScore = (totalStreaks.coerceAtMost(50) * 2) // Max 100 from habits

        // Estimation accuracy: how close estimates were to actual completion
        val accuracyScore = if (weekLogs.isNotEmpty()) {
            val accuracies = weekLogs.map { log ->
                if (log.estimatedDuration > 0) {
                    val ratio = log.completionTime.toDouble() / log.estimatedDuration
                    (1.0 - kotlin.math.abs(1.0 - ratio)).coerceIn(0.0, 1.0)
                } else 0.5
            }
            (accuracies.average() * 100).toInt()
        } else 50

        // Overall score (weighted average)
        val overallScore = (
            taskCompletionRate * 0.30 +
            consistencyScore * 0.25 +
            habitScore * 0.25 +
            accuracyScore * 0.20
        ).toInt().coerceIn(0, 100)

        val userId = getOrCreateUserId()

        return ProductivityScore(
            userId = userId,
            overallScore = overallScore,
            taskCompletionRate = taskCompletionRate,
            consistencyScore = consistencyScore,
            habitScore = habitScore,
            accuracyScore = accuracyScore,
            tasksCompleted = completedTasks,
            totalTasks = totalTasks,
            activeHabits = habits.size,
            totalStreakDays = totalStreaks,
            weeklyTasksCompleted = weekLogs.size,
            daysActiveThisWeek = daysActive,
            generatedAt = System.currentTimeMillis(),
            weekOf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(weekStart))
        )
    }

    /**
     * Saves the productivity score to a local JSON file.
     * Returns the file path.
     */
    suspend fun saveScoreToFile(score: ProductivityScore): File = withContext(Dispatchers.IO) {
        val scoresDir = File(context.filesDir, SCORES_DIR)
        if (!scoresDir.exists()) scoresDir.mkdirs()

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "${SCORE_FILE_PREFIX}${timestamp}.json"
        val file = File(scoresDir, fileName)

        val json = score.toJson()
        file.writeText(json.toString(2))

        file
    }

    /**
     * Loads all saved score files, returning them sorted by date (newest first).
     */
    suspend fun loadAllScores(): List<ProductivityScore> = withContext(Dispatchers.IO) {
        val scoresDir = File(context.filesDir, SCORES_DIR)
        if (!scoresDir.exists()) return@withContext emptyList()

        scoresDir.listFiles()
            ?.filter { it.name.startsWith(SCORE_FILE_PREFIX) && it.extension == "json" }
            ?.sortedByDescending { it.lastModified() }
            ?.mapNotNull { file ->
                try {
                    ProductivityScore.fromJson(JSONObject(file.readText()))
                } catch (e: Exception) {
                    null
                }
            } ?: emptyList()
    }

    /**
     * Creates a shareable intent for sending the score via email or social apps.
     * The JSON file is attached, and a human-readable summary is in the body.
     */
    suspend fun createShareIntent(score: ProductivityScore): Intent {
        val file = saveScoreToFile(score)

        // Copy to a shareable location using FileProvider
        val shareDir = File(context.cacheDir, "shared_scores")
        if (!shareDir.exists()) shareDir.mkdirs()

        val shareFile = File(shareDir, file.name)
        file.copyTo(shareFile, overwrite = true)

        val fileUri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            shareFile
        )

        val subject = "My SmartTask AI Productivity Score: ${score.overallScore}/100"
        val body = buildShareBody(score)

        return Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
            putExtra(Intent.EXTRA_STREAM, fileUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    /**
     * Creates an email-specific intent with the score data.
     */
    suspend fun createEmailIntent(score: ProductivityScore, recipientEmail: String? = null): Intent {
        val file = saveScoreToFile(score)

        val shareDir = File(context.cacheDir, "shared_scores")
        if (!shareDir.exists()) shareDir.mkdirs()

        val shareFile = File(shareDir, file.name)
        file.copyTo(shareFile, overwrite = true)

        val fileUri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            shareFile
        )

        val subject = "My SmartTask AI Productivity Score: ${score.overallScore}/100"
        val body = buildShareBody(score)

        return Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
            putExtra(Intent.EXTRA_STREAM, fileUri)
            if (recipientEmail != null) {
                putExtra(Intent.EXTRA_EMAIL, arrayOf(recipientEmail))
            }
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    /**
     * Exports all historical scores as a single JSON file for backup/sharing.
     */
    suspend fun exportAllScores(): File = withContext(Dispatchers.IO) {
        val scores = loadAllScores()
        val jsonArray = JSONArray()
        scores.forEach { jsonArray.put(it.toJson()) }

        val wrapper = JSONObject().apply {
            put("userId", getOrCreateUserId())
            put("exportedAt", System.currentTimeMillis())
            put("exportDate", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))
            put("totalScores", scores.size)
            put("scores", jsonArray)
        }

        val exportDir = File(context.cacheDir, "shared_scores")
        if (!exportDir.exists()) exportDir.mkdirs()

        val exportFile = File(exportDir, "productivity_history_${getOrCreateUserId().take(8)}.json")
        exportFile.writeText(wrapper.toString(2))

        exportFile
    }

    private fun buildShareBody(score: ProductivityScore): String {
        return """
            |📊 My SmartTask AI Productivity Report
            |━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            |
            |🏆 Overall Score: ${score.overallScore}/100
            |
            |📋 Tasks: ${score.tasksCompleted}/${score.totalTasks} completed (${score.taskCompletionRate}%)
            |📅 Consistency: ${score.daysActiveThisWeek}/7 days active this week
            |🔥 Habits: ${score.activeHabits} active, ${score.totalStreakDays} total streak days
            |🎯 Estimation Accuracy: ${score.accuracyScore}%
            |
            |Week of: ${score.weekOf}
            |User ID: ${score.userId.take(8)}...
            |
            |Generated by SmartTask AI
        """.trimMargin()
    }

    private fun getOrCreateUserId(): String {
        val prefs = context.getSharedPreferences("score_prefs", Context.MODE_PRIVATE)
        var userId = prefs.getString(USER_ID_KEY, null)
        if (userId == null) {
            userId = UUID.randomUUID().toString()
            prefs.edit().putString(USER_ID_KEY, userId).apply()
        }
        return userId
    }
}

/**
 * Data class representing a user's productivity score at a point in time.
 */
data class ProductivityScore(
    val userId: String,
    val overallScore: Int,
    val taskCompletionRate: Int,
    val consistencyScore: Int,
    val habitScore: Int,
    val accuracyScore: Int,
    val tasksCompleted: Int,
    val totalTasks: Int,
    val activeHabits: Int,
    val totalStreakDays: Int,
    val weeklyTasksCompleted: Int,
    val daysActiveThisWeek: Int,
    val generatedAt: Long,
    val weekOf: String
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("userId", userId)
        put("overallScore", overallScore)
        put("taskCompletionRate", taskCompletionRate)
        put("consistencyScore", consistencyScore)
        put("habitScore", habitScore)
        put("accuracyScore", accuracyScore)
        put("tasksCompleted", tasksCompleted)
        put("totalTasks", totalTasks)
        put("activeHabits", activeHabits)
        put("totalStreakDays", totalStreakDays)
        put("weeklyTasksCompleted", weeklyTasksCompleted)
        put("daysActiveThisWeek", daysActiveThisWeek)
        put("generatedAt", generatedAt)
        put("generatedAtFormatted", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(generatedAt)))
        put("weekOf", weekOf)
    }

    companion object {
        fun fromJson(json: JSONObject): ProductivityScore = ProductivityScore(
            userId = json.getString("userId"),
            overallScore = json.getInt("overallScore"),
            taskCompletionRate = json.getInt("taskCompletionRate"),
            consistencyScore = json.getInt("consistencyScore"),
            habitScore = json.getInt("habitScore"),
            accuracyScore = json.getInt("accuracyScore"),
            tasksCompleted = json.getInt("tasksCompleted"),
            totalTasks = json.getInt("totalTasks"),
            activeHabits = json.getInt("activeHabits"),
            totalStreakDays = json.getInt("totalStreakDays"),
            weeklyTasksCompleted = json.getInt("weeklyTasksCompleted"),
            daysActiveThisWeek = json.getInt("daysActiveThisWeek"),
            generatedAt = json.getLong("generatedAt"),
            weekOf = json.getString("weekOf")
        )
    }
}
