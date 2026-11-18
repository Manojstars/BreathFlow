package com.breathflow.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BreathColors = darkColorScheme(
    primary = Color(0xFFF2B84B),      // warm gold, matches the glowing dot
    onPrimary = Color(0xFF1A1334),
    secondary = Color(0xFFC9A0F5),    // lavender, matches the lungs
    background = Color(0xFF0E0B26),
    onBackground = Color(0xFFF4F1FF),
    surface = Color(0xFF1E1A4D),
    onSurface = Color(0xFFF4F1FF),
    onSurfaceVariant = Color(0xFFB9B3D9)
)

@Composable
fun BreathFlowTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = BreathColors, content = content)
}
