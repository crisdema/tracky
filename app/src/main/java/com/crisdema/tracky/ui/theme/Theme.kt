package com.crisdema.tracky.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun TrackyTheme(
    secondary: Color = AccentLilac,
    content: @Composable () -> Unit
) {
    val colorScheme = darkColorScheme(
        primary = TrackyPrimary,
        primaryContainer = TrackyPrimaryContainer,
        secondary = secondary,
        secondaryContainer = secondary,
        onSecondary = Color.Black,
        onSecondaryContainer = Color.Black,
        background = TrackyBackground,
        surface = TrackySurface,
        surfaceVariant = TrackySurfaceVariant,
        onSurface = TrackyOnSurface,
        onSurfaceVariant = TrackyOnSurfaceVariant,
        error = TrackyError
    )

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
