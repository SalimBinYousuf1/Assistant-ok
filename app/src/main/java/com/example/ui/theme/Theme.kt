package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = FrostCyanDark,
    onPrimary = Color(0xFF032236),
    primaryContainer = Color(0xFF0C4A6E),
    onPrimaryContainer = Color(0xFFBAE6FD),
    secondary = FrostIrisAccent,
    onSecondary = Color.White,
    background = FrostDarkBackground,
    onBackground = FrostDarkTextPrimary,
    surface = FrostDarkSurface,
    onSurface = FrostDarkTextPrimary,
    surfaceVariant = FrostDarkSurfaceSubtle,
    onSurfaceVariant = FrostDarkTextSecondary,
    outline = FrostDarkBorder,
    error = FrostRoseError,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = FrostCyanPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF0369A1),
    secondary = FrostIrisAccent,
    onSecondary = Color.White,
    background = FrostLightBackground,
    onBackground = FrostLightTextPrimary,
    surface = FrostLightSurface,
    onSurface = FrostLightTextPrimary,
    surfaceVariant = FrostLightSurfaceSubtle,
    onSurfaceVariant = FrostLightTextSecondary,
    outline = FrostLightBorder,
    error = FrostRoseError,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent Liquid Glass Frost branding by default
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
