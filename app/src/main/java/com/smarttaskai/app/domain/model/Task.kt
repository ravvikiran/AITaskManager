package com.smarttaskai.app.domain.model

data class Task(
    val id: String = "",
    val title: String = "",
    val description: String? = null,
    val categoryId: String? = null,
    val categoryName: String? = null,
    val priority: Priority = Priority.LOW,
    val estimatedDuration: Int = 30,
    val actualDuration: Int? = null,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val scheduledAt: Long? = null,
    val dueDate: Long? = null,
    val subTasks: List<SubTask> = emptyList()
)

data class SubTask(
    val id: String = "",
    val taskId: String = "",
    val title: String = "",
    val isCompleted: Boolean = false,
    val sortOrder: Int = 0
)

enum class Priority(val value: Int, val label: String) {
    LOW(0, "Low"),
    MEDIUM(1, "Medium"),
    HIGH(2, "High");

    companion object {
        fun fromValue(value: Int): Priority = entries.firstOrNull { it.value == value } ?: LOW
    }
}

enum class TaskCategory(val displayName: String) {
    WORK("Work"),
    PERSONAL("Personal"),
    HEALTH("Health"),
    LEARNING("Learning"),
    ERRANDS("Errands"),
    OTHER("Other")
}
