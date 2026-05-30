package com.smarttaskai.app.ui.screens.score

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarttaskai.app.data.export.ProductivityScore
import com.smarttaskai.app.data.export.ProductivityScoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ScoreUiState(
    val currentScore: ProductivityScore? = null,
    val scoreHistory: List<ProductivityScore> = emptyList(),
    val isLoading: Boolean = true,
    val shareIntent: Intent? = null,
    val error: String? = null
)

@HiltViewModel
class ScoreViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val scoreManager: ProductivityScoreManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScoreUiState())
    val uiState: StateFlow<ScoreUiState> = _uiState.asStateFlow()

    init {
        loadScore()
    }

    private fun loadScore() {
        viewModelScope.launch {
            try {
                val score = scoreManager.calculateProductivityScore()
                val history = scoreManager.loadAllScores()
                _uiState.update {
                    it.copy(
                        currentScore = score,
                        scoreHistory = history,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = "Failed to calculate score")
                }
            }
        }
    }

    fun refreshScore() {
        _uiState.update { it.copy(isLoading = true) }
        loadScore()
    }

    fun saveAndShareScore() {
        viewModelScope.launch {
            val score = _uiState.value.currentScore ?: return@launch
            try {
                val intent = scoreManager.createShareIntent(score)
                _uiState.update { it.copy(shareIntent = intent) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to share score") }
            }
        }
    }

    fun shareViaEmail(recipientEmail: String?) {
        viewModelScope.launch {
            val score = _uiState.value.currentScore ?: return@launch
            try {
                val intent = scoreManager.createEmailIntent(score, recipientEmail)
                _uiState.update { it.copy(shareIntent = intent) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to create email") }
            }
        }
    }

    fun exportAllHistory() {
        viewModelScope.launch {
            try {
                val file = scoreManager.exportAllScores()
                val fileUri = androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/json"
                    putExtra(Intent.EXTRA_SUBJECT, "SmartTask AI - Full Productivity History")
                    putExtra(Intent.EXTRA_TEXT, "Attached is my complete productivity score history from SmartTask AI.")
                    putExtra(Intent.EXTRA_STREAM, fileUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                _uiState.update { it.copy(shareIntent = intent) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to export history") }
            }
        }
    }

    fun clearShareIntent() {
        _uiState.update { it.copy(shareIntent = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
