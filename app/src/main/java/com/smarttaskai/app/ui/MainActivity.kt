package com.smarttaskai.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.smarttaskai.app.billing.BillingManager
import com.smarttaskai.app.ui.navigation.NavGraph
import com.smarttaskai.app.ui.navigation.Screen
import com.smarttaskai.app.ui.screens.onboarding.OnboardingScreen
import com.smarttaskai.app.ui.screens.onboarding.TutorialManager
import com.smarttaskai.app.ui.screens.onboarding.TutorialOverlay
import com.smarttaskai.app.ui.screens.onboarding.dashboardTutorialSteps
import com.smarttaskai.app.ui.theme.SmartTaskAITheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var billingManager: BillingManager

    @Inject
    lateinit var tutorialManager: TutorialManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartTaskAITheme {
                SmartTaskAIApp(
                    billingManager = billingManager,
                    tutorialManager = tutorialManager
                )
            }
        }
    }
}

@Composable
fun SmartTaskAIApp(
    billingManager: BillingManager,
    tutorialManager: TutorialManager
) {
    val showOnboarding by tutorialManager.showOnboarding.collectAsState()
    val showDashboardTutorial by tutorialManager.showDashboardTutorial.collectAsState()
    val currentTutorialStep by tutorialManager.currentTutorialStep.collectAsState()
    val scope = rememberCoroutineScope()

    // Check if first launch
    LaunchedEffect(Unit) {
        tutorialManager.checkFirstLaunch()
    }

    // Show onboarding pages on first launch
    if (showOnboarding) {
        OnboardingScreen(
            onFinish = {
                scope.launch { tutorialManager.completeOnboarding() }
            }
        )
        return
    }

    // Main app with optional tutorial overlay
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = currentDestination?.route in
        Screen.bottomNavItems.map { it.route }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar {
                        Screen.bottomNavItems.forEach { screen ->
                            val selected = currentDestination?.hierarchy
                                ?.any { it.route == screen.route } == true
                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        imageVector = if (selected)
                                            screen.selectedIcon!!
                                        else screen.unselectedIcon!!,
                                        contentDescription = screen.title
                                    )
                                },
                                label = { Text(screen.title) },
                                selected = selected,
                                onClick = {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph
                                            .findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                NavGraph(
                    navController = navController,
                    billingManager = billingManager,
                    onReplayTutorial = {
                        tutorialManager.replayTutorial()
                    }
                )
            }
        }

        // Interactive tutorial overlay on top of the real app
        if (showDashboardTutorial &&
            currentTutorialStep < dashboardTutorialSteps.size
        ) {
            TutorialOverlay(
                currentStep = currentTutorialStep,
                totalSteps = dashboardTutorialSteps.size,
                step = dashboardTutorialSteps[currentTutorialStep],
                onNext = { tutorialManager.nextTutorialStep() },
                onDismiss = { tutorialManager.dismissTutorial() }
            )
        }
    }
}
