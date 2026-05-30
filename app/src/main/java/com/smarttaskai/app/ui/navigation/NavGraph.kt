package com.smarttaskai.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.smarttaskai.app.billing.BillingManager
import com.smarttaskai.app.ui.screens.analytics.AnalyticsScreen
import com.smarttaskai.app.ui.screens.dashboard.DashboardScreen
import com.smarttaskai.app.ui.screens.focus.FocusScreen
import com.smarttaskai.app.ui.screens.habits.CreateHabitScreen
import com.smarttaskai.app.ui.screens.habits.HabitsScreen
import com.smarttaskai.app.ui.screens.settings.PremiumScreen
import com.smarttaskai.app.ui.screens.settings.SettingsScreen
import com.smarttaskai.app.ui.screens.task.CreateTaskScreen
import com.smarttaskai.app.ui.screens.task.EditTaskScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    billingManager: BillingManager
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onCreateTask = { navController.navigate(Screen.CreateTask.route) },
                onEditTask = { taskId -> navController.navigate(Screen.EditTask.createRoute(taskId)) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.Habits.route) {
            HabitsScreen(
                onCreateHabit = { navController.navigate(Screen.CreateHabit.route) }
            )
        }

        composable(Screen.Focus.route) {
            FocusScreen()
        }

        composable(Screen.Analytics.route) {
            AnalyticsScreen()
        }

        composable(Screen.CreateTask.route) {
            CreateTaskScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.EditTask.route,
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: return@composable
            EditTaskScreen(
                taskId = taskId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.CreateHabit.route) {
            CreateHabitScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPremium = { navController.navigate(Screen.Premium.route) }
            )
        }

        composable(Screen.Premium.route) {
            PremiumScreen(
                onNavigateBack = { navController.popBackStack() },
                billingManager = billingManager
            )
        }
    }
}
