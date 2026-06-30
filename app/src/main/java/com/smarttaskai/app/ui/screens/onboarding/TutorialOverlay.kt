package com.smarttaskai.app.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Tutorial steps that overlay on top of the actual app screens.
 * Each step highlights a specific area and provides guidance.
 */
data class TutorialStep(
    val title: String,
    val message: String,
    val position: TooltipPosition = TooltipPosition.BOTTOM,
    val highlightArea: HighlightArea = HighlightArea.NONE
)

enum class TooltipPosition { TOP, CENTER, BOTTOM }
enum class HighlightArea { NONE, TOP_RIGHT, FAB, CENTER, TOP_LEFT }

val dashboardTutorialSteps = listOf(
    TutorialStep(
        title = "Welcome to Your Dashboard! 👋",
        message = "This is your home base. You'll see your AI-generated daily plan, " +
            "today's tasks, and habit streaks all in one place.",
        position = TooltipPosition.CENTER
    ),
    TutorialStep(
        title = "Create Your First Task ✏️",
        message = "Tap the + button in the bottom-right corner to create a new task. " +
            "Try adding something like \"Plan my week\" — go ahead, try it now!",
        position = TooltipPosition.CENTER,
        highlightArea = HighlightArea.FAB
    ),
    TutorialStep(
        title = "AI Daily Plan 🤖",
        message = "Once you have a few tasks, the AI will automatically generate an " +
            "optimal schedule based on your energy levels and priorities. " +
            "It learns from your history — the more you use it, the smarter it gets!",
        position = TooltipPosition.TOP
    ),
    TutorialStep(
        title = "Your Productivity Score 🏆",
        message = "Tap the trophy icon in the top-right to see your productivity score. " +
            "You can share it with friends via email or social media!",
        position = TooltipPosition.BOTTOM,
        highlightArea = HighlightArea.TOP_RIGHT
    ),
    TutorialStep(
        title = "You're All Set! 🎉",
        message = "Explore the app freely now. Check out Habits, Focus Mode, and Analytics " +
            "in the bottom navigation. You can replay this tutorial anytime from Settings.",
        position = TooltipPosition.CENTER
    )
)

/**
 * A semi-transparent overlay that shows tutorial tooltips on top of the real app UI.
 * Users can interact with the app underneath between steps.
 */
@Composable
fun TutorialOverlay(
    currentStep: Int,
    totalSteps: Int,
    step: TutorialStep,
    onNext: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Semi-transparent backdrop (allows taps through for interaction)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onNext() }
        )

        // Tooltip card positioned based on step
        val alignment = when (step.position) {
            TooltipPosition.TOP -> Alignment.TopCenter
            TooltipPosition.CENTER -> Alignment.Center
            TooltipPosition.BOTTOM -> Alignment.BottomCenter
        }

        val topPadding = when (step.position) {
            TooltipPosition.TOP -> 80.dp
            TooltipPosition.CENTER -> 0.dp
            TooltipPosition.BOTTOM -> 0.dp
        }

        val bottomPadding = when (step.position) {
            TooltipPosition.TOP -> 0.dp
            TooltipPosition.CENTER -> 0.dp
            TooltipPosition.BOTTOM -> 120.dp
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = topPadding, bottom = bottomPadding),
            contentAlignment = alignment
        ) {
            TutorialTooltipCard(
                step = step,
                currentStep = currentStep,
                totalSteps = totalSteps,
                onNext = onNext,
                onDismiss = onDismiss
            )
        }

        // Highlight pulse indicator
        if (step.highlightArea != HighlightArea.NONE) {
            val highlightAlignment = when (step.highlightArea) {
                HighlightArea.FAB -> Alignment.BottomEnd
                HighlightArea.TOP_RIGHT -> Alignment.TopEnd
                HighlightArea.TOP_LEFT -> Alignment.TopStart
                HighlightArea.CENTER -> Alignment.Center
                HighlightArea.NONE -> Alignment.Center
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = if (step.highlightArea == HighlightArea.TOP_RIGHT ||
                            step.highlightArea == HighlightArea.TOP_LEFT) 8.dp else 0.dp,
                        end = if (step.highlightArea == HighlightArea.TOP_RIGHT ||
                            step.highlightArea == HighlightArea.FAB) 16.dp else 0.dp,
                        bottom = if (step.highlightArea == HighlightArea.FAB) 80.dp else 0.dp
                    ),
                contentAlignment = highlightAlignment
            ) {
                // Pulsing indicator
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.TouchApp,
                        contentDescription = "Tap here",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TutorialTooltipCard(
    step: TutorialStep,
    currentStep: Int,
    totalSteps: Int,
    onNext: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header with close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = step.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close tutorial",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Message
            Text(
                text = step.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Step indicator + Next button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Dots
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(totalSteps) { index ->
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (index <= currentStep) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                        )
                    }
                }

                // Action button
                TextButton(onClick = onNext) {
                    Text(
                        text = if (currentStep == totalSteps - 1) "Got it!" else "Next →",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
