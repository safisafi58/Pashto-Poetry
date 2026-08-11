package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = PashtoGold,
    onPrimary = PashtoDarkBackground,
    primaryContainer = PashtoEmeraldLight,
    onPrimaryContainer = PashtoParchment,
    secondary = PashtoGoldLight,
    onSecondary = PashtoDarkBackground,
    background = PashtoDarkBackground,
    onBackground = PashtoDarkTextPrimary,
    surface = PashtoDarkSurface,
    onSurface = PashtoDarkTextPrimary,
    surfaceVariant = PashtoDarkSurface,
    onSurfaceVariant = PashtoDarkTextSecondary
)

private val LightColorScheme = lightColorScheme(
    primary = PashtoEmerald,
    onPrimary = PashtoParchment,
    primaryContainer = PashtoEmeraldLight,
    onPrimaryContainer = PashtoParchment,
    secondary = PashtoGold,
    onSecondary = PashtoEmerald,
    background = PashtoParchment,
    onBackground = PashtoTextPrimary,
    surface = PashtoSurface,
    onSurface = PashtoTextPrimary,
    surfaceVariant = PashtoParchment,
    onSurfaceVariant = PashtoTextSecondary
)

@Composable
fun PashtoPoetryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
