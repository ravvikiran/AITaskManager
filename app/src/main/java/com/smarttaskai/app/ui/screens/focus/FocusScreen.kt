package com.smarttaskai.app.ui.screens.focus

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusScreen(
    viewModel: FocusViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Session label
        Text(
            text = if (uiState.isBreak) "Break Time" else "Focus Mode",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = if (uiState.isBreak) MaterialTheme.colorScheme.secondary
            else MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Circular progress timer
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(280.dp)
        ) {
            CircularProgressIndicator(
                progress = uiState.progress,
                modifier = Modifier.size(280.dp),
                strokeWidth = 12.dp,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                color = if (uiState.isBreak) MaterialTheme.colorScheme.secondary
                else MaterialTheme.colorScheme.primary
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = uiState.formattedTime,
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = if (uiState.isBreak) "Relax" else "Stay focused",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Controls
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reset
            IconButton(onClick = { viewModel.resetTimer() }) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "Reset",
                    modifier = Modifier.size(32.dp)
                )
            }

            // Play/Pause
            FilledIconButton(
                onClick = {
                    if (uiState.isRunning) viewModel.pauseTimer()
                    else viewModel.startTimer()
                },
                modifier = Modifier.size(72.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = if (uiState.isBreak) MaterialTheme.colorScheme.secondary
                    else MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = if (uiState.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (uiState.isRunning) "Pause" else "Start",
                    modifier = Modifier.size(36.dp)
                )
            }

            // Placeholder for symmetry
            Box(modifier = Modifier.size(48.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Sessions completed
        Text(
            text = "Sessions completed: ${uiState.sessionsCompleted}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Duration presets
        Spacer(modifier = Modifier.height(24.dp))
        if (!uiState.isRunning) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf(15, 25, 45, 60).forEach { minutes ->
                    androidx.compose.material3.FilterChip(
                        selected = uiState.focusDurationMinutes == minutes,
                        onClick = { viewModel.setFocusDuration(minutes) },
                        label = { Text("${minutes}m") }
                    )
                }
            }
        }
    }
}
