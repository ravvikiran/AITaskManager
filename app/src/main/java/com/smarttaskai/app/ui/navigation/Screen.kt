package com.smarttaskai.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Loop
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector? = null,
    val unselectedIcon: ImageVector? = null
) {
    data object Dashboard : Screen(
        route = "dashboard",
        title = "Home",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    )

    data object Habits : Screen(
        route = "habits",
        title = "Habits",
        selectedIcon = Icons.Filled.Loop,
        unselectedIcon = Icons.Outlined.Loop
    )

    data object Focus : Screen(
        route = "focus",
        title = "Focus",
        selectedIcon = Icons.Filled.Timer,
        unselectedIcon = Icons.Outlined.Timer
    )

    data object Analytics : Screen(
        route = "analytics",
        title = "Analytics",
        selectedIcon = Icons.Filled.Analytics,
        unselectedIcon = Icons.Outlined.Analytics
    )

    data object CreateTask : Screen(route = "create_task", title = "New Task")
    data object EditTask : Screen(route = "edit_task/{taskId}", title = "Edit Task") {
        fun createRoute(taskId: String) = "edit_task/$taskId"
    }

    data object CreateHabit : Screen(route = "create_habit", title = "New Habit")
    data object Settings : Screen(route = "settings", title = "Settings")
    data object Premium : Screen(route = "premium", title = "Premium")

    companion object {
        val bottomNavItems = listOf(Dashboard, Habits, Focus, Analytics)
    }
}
