package com.smarttaskai.app.ui.screens.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarttaskai.app.data.repository.TaskRepository
import com.smarttaskai.app.domain.model.Priority
import com.smarttaskai.app.domain.model.Task
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditTaskUiState(
    val title: String = "",
    val description: String = "",
    val priority: Priority = Priority.MEDIUM,
    val categoryId: String? = null,
    val estimatedDuration: Int = 30,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val isDeleted: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class EditTaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditTaskUiState())
    val uiState: StateFlow<EditTaskUiState> = _uiState.asStateFlow()

    private var currentTask: Task? = null

    fun loadTask(taskId: String) {
        viewModelScope.launch {
            val task = taskRepository.getTaskById(taskId) ?: return@launch
            currentTask = task
            _uiState.update {
                it.copy(
                    title = task.title,
                    description = task.description ?: "",
                    priority = task.priority,
                    categoryId = task.categoryId,
                    estimatedDuration = task.estimatedDuration
                )
            }
        }
    }

    fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun updateDescription(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun updatePriority(priority: Priority) {
        _uiState.update { it.copy(priority = priority) }
    }

    fun updateEstimatedDuration(duration: Int) {
        _uiState.update { it.copy(estimatedDuration = duration) }
    }

    fun saveTask() {
        val task = currentTask ?: return
        val state = _uiState.value

        if (state.title.isBlank()) {
            _uiState.update { it.copy(error = "Title cannot be empty") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val updatedTask = task.copy(
                title = state.title,
                description = state.description.ifBlank { null },
                priority = state.priority,
                categoryId = state.categoryId,
                estimatedDuration = state.estimatedDuration
            )
            taskRepository.updateTask(updatedTask)
            _uiState.update { it.copy(isSaving = false, isSaved = true) }
        }
    }

    fun deleteTask() {
        val task = currentTask ?: return
        viewModelScope.launch {
            taskRepository.deleteTask(task)
            _uiState.update { it.copy(isDeleted = true) }
        }
    }
}
