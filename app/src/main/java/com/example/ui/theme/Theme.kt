package com.example.ui.theme

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = CricketGreenPrimary,
    onPrimary = Color.Black,
    primaryContainer = CricketGreenContainer,
    onPrimaryContainer = CricketOnGreenContainer,
    secondary = CricketCyan,
    onSecondary = Color.Black,
    secondaryContainer = CricketCyanDark,
    onSecondaryContainer = CricketCyanLight,
    tertiary = CricketRedLive,
    onTertiary = Color.White,
    tertiaryContainer = CricketRedContainer,
    onTertiaryContainer = CricketOnRedContainer,
    background = ImmersiveBackground,
    onBackground = ImmersiveOnSurface,
    surface = ImmersiveSurface,
    onSurface = ImmersiveOnSurface,
    surfaceVariant = ImmersiveSurfaceVariant,
    onSurfaceVariant = ImmersiveOnSurfaceVariant,
    outline = ImmersiveOutline
)

private val LightColorScheme = darkColorScheme( // Keep Immersive Obsidian as base design system
    primary = CricketGreenPrimary,
    onPrimary = Color.Black,
    primaryContainer = CricketGreenContainer,
    onPrimaryContainer = CricketOnGreenContainer,
    secondary = CricketCyan,
    onSecondary = Color.Black,
    secondaryContainer = CricketCyanDark,
    onSecondaryContainer = CricketCyanLight,
    tertiary = CricketRedLive,
    onTertiary = Color.White,
    tertiaryContainer = CricketRedContainer,
    onTertiaryContainer = CricketOnRedContainer,
    background = ImmersiveBackground,
    onBackground = ImmersiveOnSurface,
    surface = ImmersiveSurface,
    onSurface = ImmersiveOnSurface,
    surfaceVariant = ImmersiveSurfaceVariant,
    onSurfaceVariant = ImmersiveOnSurfaceVariant,
    outline = ImmersiveOutline
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our tailored cricket palette by default
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
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.surface.toArgb()
                window.navigationBarColor = colorScheme.surface.toArgb()
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !darkTheme
                controller.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
