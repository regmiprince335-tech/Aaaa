package com.example.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisMatrixGreen
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.NeonCyan
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Custom pulsing neon circle animation that visualizes real-time microphone input activity
 * when the user speaks to JARVIS.
 *
 * Features:
 * - Smooth RMS decibel amplitude smoothing
 * - Radiating concentric acoustic neon pulse waves
 * - Perimeter circular equalizer bars that react to voice volume
 * - Central glowing arc-reactor core with microphone icon
 * - Dual neon cyan (#00E5FF) and electric purple (#7C4DFF) glow
 */
@Composable
fun PulsingNeonCircle(
    isListening: Boolean,
    rmsLevel: Float,
    modifier: Modifier = Modifier,
    size: Dp = 140.dp,
    primaryColor: Color = NeonCyan,
    accentColor: Color = ElectricPurple,
    onClick: (() -> Unit)? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "PulsingNeonTransition")

    // Continuous smooth rotation for HUD telemetry rings
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "HudRotation"
    )

    // Base pulsing breathing cycle
    val idlePulse by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isListening) 650 else 1800,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "IdlePulse"
    )

    // Three staggered radiating ripple waves
    val wave1Progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Wave1"
    )

    val wave2Progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, delayMillis = 460, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Wave2"
    )

    val wave3Progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, delayMillis = 920, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Wave3"
    )

    // Smooth real-time RMS microphone amplitude
    val smoothedRms = remember { Animatable(0f) }
    LaunchedEffect(rmsLevel, isListening) {
        val target = if (isListening) rmsLevel.coerceIn(0f, 10f) else 0f
        smoothedRms.animateTo(
            targetValue = target,
            animationSpec = tween(durationMillis = 50, easing = LinearEasing)
        )
    }

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .size(size)
            .testTag("pulsing_neon_circle")
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val maxRadius = this.size.minDimension / 2f
            val coreBaseRadius = maxRadius * 0.42f

            // Voice activity amplification factor (0..1)
            val voiceEnergy = if (isListening) (smoothedRms.value / 10f).coerceIn(0f, 1f) else 0f
            val dynamicCoreRadius = coreBaseRadius * (if (isListening) (idlePulse + voiceEnergy * 0.4f) else idlePulse)

            // 1. Expanding Acoustic Sonar Ripple Waves (active when listening)
            if (isListening) {
                val waveProgresses = listOf(wave1Progress, wave2Progress, wave3Progress)
                for (progress in waveProgresses) {
                    val waveRadius = dynamicCoreRadius + (maxRadius - dynamicCoreRadius) * progress
                    val waveAlpha = (1f - progress) * (0.35f + voiceEnergy * 0.55f)
                    val waveStroke = (1.5f + (1f - progress) * 2f).dp.toPx()

                    drawCircle(
                        color = primaryColor.copy(alpha = waveAlpha.coerceIn(0f, 1f)),
                        radius = waveRadius,
                        center = center,
                        style = Stroke(width = waveStroke)
                    )
                }
            }

            // 2. Diffuse Holographic Glow Aura
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = if (isListening) 0.45f + voiceEnergy * 0.4f else 0.15f),
                        accentColor.copy(alpha = if (isListening) 0.25f + voiceEnergy * 0.2f else 0.08f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = dynamicCoreRadius * 1.6f
                ),
                radius = dynamicCoreRadius * 1.6f,
                center = center
            )

            // 3. Radial Circular Equalizer Ticks (32 audio bars around perimeter)
            val barCount = 32
            val barAngleStep = (2 * PI / barCount).toFloat()
            val rotRad = (rotationAngle * PI / 180f).toFloat()

            for (i in 0 until barCount) {
                val angle = i * barAngleStep + rotRad

                // Modulate individual bar heights using voice energy + harmonic wave
                val harmonicOffset = sin(i * 0.8f + voiceEnergy * 5f) * 0.5f + 0.5f
                val barLen = if (isListening) {
                    (3.dp.toPx() + (voiceEnergy * 18.dp.toPx() * harmonicOffset)).coerceAtLeast(3.dp.toPx())
                } else {
                    2.5.dp.toPx()
                }

                val innerR = dynamicCoreRadius + 6.dp.toPx()
                val outerR = (innerR + barLen).coerceAtMost(maxRadius - 2.dp.toPx())

                val startX = center.x + innerR * cos(angle)
                val startY = center.y + innerR * sin(angle)
                val endX = center.x + outerR * cos(angle)
                val endY = center.y + outerR * sin(angle)

                val barColor = if (i % 2 == 0) primaryColor else accentColor
                val barAlpha = if (isListening) (0.4f + voiceEnergy * 0.6f).coerceIn(0.2f, 1f) else 0.25f

                drawLine(
                    color = barColor.copy(alpha = barAlpha),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // 4. Outer Segmented Cyan HUD Ring
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 12f, 6f, 12f), 0f)
            drawCircle(
                color = primaryColor.copy(alpha = if (isListening) 0.6f else 0.3f),
                radius = dynamicCoreRadius + 4.dp.toPx(),
                center = center,
                style = Stroke(
                    width = 1.5.dp.toPx(),
                    pathEffect = dashEffect
                )
            )

            // 5. Central Reactor Disc Background & Gradient
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = if (isListening) 0.9f else 0.6f),
                        primaryColor.copy(alpha = if (isListening) 0.85f else 0.5f),
                        accentColor.copy(alpha = 0.5f),
                        JarvisBackground
                    ),
                    center = center,
                    radius = dynamicCoreRadius
                ),
                radius = dynamicCoreRadius,
                center = center
            )

            // 6. Glowing Neon Border around Central Core
            drawCircle(
                color = primaryColor,
                radius = dynamicCoreRadius,
                center = center,
                style = Stroke(width = if (isListening) 2.5.dp.toPx() else 1.5.dp.toPx())
            )
        }

        // 7. Centered Microphone Glyph with Dynamic Tint & Size
        val iconSize = (size.value * 0.28f).dp
        Icon(
            imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicOff,
            contentDescription = if (isListening) "Microphone active" else "Microphone muted",
            tint = if (isListening) JarvisBackground else JarvisTextPrimary.copy(alpha = 0.85f),
            modifier = Modifier.size(iconSize)
        )
    }
}
