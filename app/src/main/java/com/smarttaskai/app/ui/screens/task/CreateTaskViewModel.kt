package com.smarttaskai.app.ui.screens.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarttaskai.app.data.repository.TaskRepository
import com.smarttaskai.app.domain.model.Priority
import com.smarttaskai.app.domain.model.SubTask
import com.smarttaskai.app.domain.model.Task
import com.smarttaskai.app.domain.model.TaskCategory
import com.smarttaskai.app.ml.Confidence
import com.smarttaskai.app.ml.PredictionResult
import com.smarttaskai.app.ml.ProductivityMLService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateTaskUiState(
    val title: String = "",
    val description: String = "",
    val priority: Priority = Priority.MEDIUM,
    val categoryId: String? = null,
    val estimatedDuration: Int = 30,
    val subTasks: List<SubTask> = emptyList(),
    val aiPrediction: PredictionResult? = null,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CreateTaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val mlService: ProductivityMLService
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateTaskUiState())
    val uiState: StateFlow<CreateTaskUiState> = _uiState.asStateFlow()

    fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun updateDescription(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun updatePriority(priority: Priority) {
        _uiState.update { it.copy(priority = priority) }
    }

    fun updateCategory(categoryId: String?) {
        _uiState.update { it.copy(categoryId = categoryId) }
    }

    fun updateEstimatedDuration(duration: Int) {
        _uiState.update { it.copy(estimatedDuration = duration) }
    }

    fun addSubTask(title: String) {
        if (title.isBlank()) return
        val newSubTask = SubTask(
            title = title,
            sortOrder = _uiState.value.subTasks.size
        )
        _uiState.update { it.copy(subTasks = it.subTasks + newSubTask) }
    }

    fun removeSubTask(index: Int) {
        _uiState.update {
            it.copy(subTasks = it.subTasks.toMutableList().apply { removeAt(index) })
        }
    }

    /**
     * "Magic Wand" feature: Uses on-device ML to predict task duration.
     */
    fun predictDuration() {
        viewModelScope.launch {
            val state = _uiState.value
            val prediction = mlService.predictDuration(
                categoryId = state.categoryId,
                priority = state.priority
            )
            _uiState.update {
                it.copy(
                    aiPrediction = prediction,
                    estimatedDuration = prediction.predictedMinutes
                )
            }
        }
    }

    fun saveTask() {
        val state = _uiState.value
        if (state.title.isBlank()) {
            _uiState.update { it.copy(error = "Title cannot be empty") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                val task = Task(
                    title = state.title,
                    description = state.description.ifBlank { null },
                    categoryId = state.categoryId,
                    priority = state.priority,
                    estimatedDuration = state.estimatedDuration,
                    subTasks = state.subTasks
                )
                taskRepository.createTask(task)
                _uiState.update { it.copy(isSaving = false, isSaved = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }
}
