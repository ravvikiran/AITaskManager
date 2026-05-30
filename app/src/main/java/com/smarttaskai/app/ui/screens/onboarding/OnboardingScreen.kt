package com.smarttaskai.app.ui.screens.onboarding

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlinx.coroutines.launch

data class OnboardingPage(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val tip: String
)

private val onboardingPages = listOf(
    OnboardingPage(
        icon = Icons.Default.CheckCircle,
        title = "Smart Task Management",
        description = "Create tasks with priorities, categories, and sub-task checklists. " +
            "Swipe to complete, tap to edit. Organize your day effortlessly.",
        tip = "💡 Try it: After this tutorial, tap the + button on the home screen to create your first task!"
    ),
    OnboardingPage(
        icon = Icons.Default.AutoAwesome,
        title = "AI-Powered Scheduling",
        description = "Our on-device AI learns from your habits. It predicts how long tasks " +
            "will take and builds an optimal daily schedule — no internet needed, fully private.",
        tip = "💡 Try it: When creating a task, tap the \"AI Suggest\" chip to see the magic wand in action!"
    ),
    OnboardingPage(
        icon = Icons.Default.Loop,
        title = "Build Lasting Habits",
        description = "Track daily or weekly habits with visual streaks. Watch your consistency " +
            "grow over time. The longer your streak, the higher your productivity score.",
        tip = "💡 Try it: Go to the Habits tab and create a habit like \"Drink water\" or \"Read 10 pages\"."
    ),
    OnboardingPage(
        icon = Icons.Default.Timer,
        title = "Deep Focus Mode",
        description = "A built-in Pomodoro timer helps you stay focused. Choose 15, 25, 45, or 60 " +
            "minute sessions. The timer runs even when you leave the app.",
        tip = "💡 Try it: Head to the Focus tab, pick a duration, and hit play. Put your phone down and focus!"
    ),
    OnboardingPage(
        icon = Icons.Default.Analytics,
        title = "Track Your Progress",
        description = "See detailed analytics about your productivity patterns. Discover your " +
            "peak hours, completion rates, and get AI-generated insights.",
        tip = "💡 Try it: Complete a few tasks first, then check the Analytics tab to see your stats build up."
    ),
    OnboardingPage(
        icon = Icons.Default.EmojiEvents,
        title = "Share Your Score",
        description = "Your productivity is scored from 0-100 based on task completion, consistency, " +
            "habits, and estimation accuracy. Share it with friends via email or social media!",
        tip = "💡 Try it: Tap the 🏆 trophy icon on the home screen to see your score and share it."
    )
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val scope = rememberCoroutineScope()
    val progress by animateFloatAsState(
        targetValue = (pagerState.currentPage + 1).toFloat() / onboardingPages.size,
        label = "progress"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
    ) {
        // Progress bar
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Page counter
        Text(
            text = "${pagerState.currentPage + 1} of ${onboardingPages.size}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Skip button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onFinish) {
                Text("Skip")
            }
        }

        // Pager content
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { pageIndex ->
            OnboardingPageContent(page = onboardingPages[pageIndex])
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Page indicators
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(onboardingPages.size) { index ->
                val isSelected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (isSelected) 10.dp else 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Navigation buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Back button
            if (pagerState.currentPage > 0) {
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Back")
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            // Next / Get Started button
            Button(
                onClick = {
                    if (pagerState.currentPage == onboardingPages.size - 1) {
                        onFinish()
                    } else {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    if (pagerState.currentPage == onboardingPages.size - 1) "Get Started!"
                    else "Next"
                )
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = page.icon,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Title
        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Description
        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Interactive tip card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.tertiaryContainer)
                .padding(16.dp)
        ) {
            Text(
                text = page.tip,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                textAlign = TextAlign.Center
            )
        }
    }
}
