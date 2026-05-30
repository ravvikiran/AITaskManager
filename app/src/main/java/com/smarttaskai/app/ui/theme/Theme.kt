package com.smarttaskai.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = SecondaryLight,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = TertiaryLight,
    background = BackgroundLight,
    surface = SurfaceLight,
    onBackground = androidx.compose.ui.graphics.Color(0xFF1C1B1F),
    onSurface = androidx.compose.ui.graphics.Color(0xFF1C1B1F)
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = androidx.compose.ui.graphics.Color(0xFF2D0099),
    primaryContainer = androidx.compose.ui.graphics.Color(0xFF4A3AFF),
    onPrimaryContainer = androidx.compose.ui.graphics.Color(0xFFE8E0FF),
    secondary = SecondaryDark,
    secondaryContainer = androidx.compose.ui.graphics.Color(0xFF2E7D32),
    onSecondaryContainer = androidx.compose.ui.graphics.Color(0xFFD7F5D9),
    tertiary = TertiaryDark,
    background = BackgroundDark,
    surface = SurfaceDark,
    onBackground = androidx.compose.ui.graphics.Color(0xFFE6E1E5),
    onSurface = androidx.compose.ui.graphics.Color(0xFFE6E1E5)
)

@Composable
fun SmartTaskAITheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
