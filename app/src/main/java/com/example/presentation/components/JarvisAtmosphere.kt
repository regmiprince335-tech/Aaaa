package com.example.presentation.components

import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ElectricPurpleGlow
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisVoid
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonCyanGlow

/**
 * Applies a premium sci-fi JARVIS holographic atmospheric background.
 * Combines a deep obsidian void base with a subtle overhead reactor aura
 * and ultra-fine holographic grid scan lines.
 */
fun Modifier.jarvisBackground(): Modifier = this
    .background(
        Brush.verticalGradient(
            colors = listOf(
                JarvisVoid,
                JarvisBackground,
                Color(0xFF070B16),
                JarvisVoid
            )
        )
    )
    .drawBehind {
        val w = size.width
        val h = size.height

        // 1. Upper holographic reactor aura (overhead ambient bloom)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    NeonCyanGlow.copy(alpha = 0.16f),
                    ElectricPurpleGlow.copy(alpha = 0.08f),
                    Color.Transparent
                ),
                center = Offset(w * 0.5f, h * 0.22f),
                radius = w * 0.75f
            ),
            radius = w * 0.75f,
            center = Offset(w * 0.5f, h * 0.22f)
        )

        // 2. Lower subtle ambient warmth
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    ElectricPurpleGlow.copy(alpha = 0.06f),
                    Color.Transparent
                ),
                center = Offset(w * 0.85f, h * 0.88f),
                radius = w * 0.6f
            ),
            radius = w * 0.6f,
            center = Offset(w * 0.85f, h * 0.88f)
        )

        // 3. Ultra-fine sci-fi telemetry grid lines (hairline 0.5dp, spaced 48dp)
        val gridStepPx = 48.dp.toPx()
        val gridColor = NeonCyan.copy(alpha = 0.025f)

        var y = gridStepPx
        while (y < h) {
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 0.5f
            )
            y += gridStepPx
        }
    }
