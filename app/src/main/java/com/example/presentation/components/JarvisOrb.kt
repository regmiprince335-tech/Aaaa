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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.JarvisState
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.JarvisAlertRed
import com.example.ui.theme.JarvisMatrixGreen
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.ui.theme.NeonCyan
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun JarvisOrb(
    state: JarvisState,
    rmsLevel: Float,
    statusText: String,
    modifier: Modifier = Modifier,
    size: Dp = 220.dp,
    performanceMode: Boolean = false,
    onClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "OrbTransition")

    // Slow continuous rotation for HUD rings
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (state == JarvisState.THINKING) 3000 else 12000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "Rotation"
    )

    // Breathing pulse for core glow
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    JarvisState.LISTENING -> 800
                    JarvisState.SPEAKING -> 1000
                    JarvisState.ERROR -> 500
                    else -> 2400
                },
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    // Dynamic RMS smoothing
    val smoothRms = remember { Animatable(0f) }
    LaunchedEffect(rmsLevel) {
        smoothRms.animateTo(
            targetValue = rmsLevel.coerceIn(0f, 10f),
            animationSpec = tween(durationMillis = 60, easing = LinearEasing)
        )
    }

    val baseColor = when (state) {
        JarvisState.IDLE -> NeonCyan
        JarvisState.LISTENING -> NeonCyan
        JarvisState.THINKING -> ElectricPurple
        JarvisState.SPEAKING -> NeonCyan
        JarvisState.ERROR -> JarvisAlertRed
    }

    val accentColor = when (state) {
        JarvisState.IDLE -> ElectricPurple
        JarvisState.LISTENING -> JarvisMatrixGreen
        JarvisState.THINKING -> NeonCyan
        JarvisState.SPEAKING -> ElectricPurple
        JarvisState.ERROR -> Color(0xFFFF9100)
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .testTag("jarvis_orb")
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(size)) {
                val center = Offset(this.size.width / 2f, this.size.height / 2f)
                val baseRadius = this.size.minDimension / 2f * 0.72f

                // Audio reactive amplification factor
                val rmsFactor = if (state == JarvisState.LISTENING || state == JarvisState.SPEAKING) {
                    1f + (smoothRms.value / 10f) * 0.45f
                } else {
                    1f
                }

                val currentRadius = baseRadius * pulseScale * rmsFactor

                // 1. Outer Holographic Glow Aura
                if (!performanceMode) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                baseColor.copy(alpha = 0.35f),
                                accentColor.copy(alpha = 0.12f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = currentRadius * 1.35f
                        ),
                        radius = currentRadius * 1.35f,
                        center = center
                    )
                }

                // 2. Rotating Outer HUD Ring (segmented dashes)
                val dashEffect = PathEffect.dashPathEffect(floatArrayOf(24f, 16f, 8f, 16f), 0f)
                drawCircle(
                    color = baseColor.copy(alpha = 0.4f),
                    radius = baseRadius * 1.18f,
                    center = center,
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = dashEffect
                    )
                )

                // 3. Counter-rotating inner segmented HUD ring
                val counterDash = PathEffect.dashPathEffect(floatArrayOf(40f, 20f), 0f)
                drawCircle(
                    color = accentColor.copy(alpha = 0.35f),
                    radius = baseRadius * 0.95f,
                    center = center,
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = counterDash
                    )
                )

                // 4. Central Reactor Core Gradient
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.95f),
                            baseColor.copy(alpha = 0.85f),
                            accentColor.copy(alpha = 0.45f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = currentRadius * 0.75f
                    ),
                    radius = currentRadius * 0.75f,
                    center = center
                )

                // 5. Orbital nodes / ticks (rendered cleanly on canvas)
                val nodeCount = if (performanceMode) 4 else 8
                val angleStep = (2 * PI / nodeCount).toFloat()
                val rotRad = (rotationAngle * PI / 180f).toFloat()

                for (i in 0 until nodeCount) {
                    val theta = i * angleStep + rotRad
                    val nodeRadius = baseRadius * 1.08f
                    val nx = center.x + nodeRadius * cos(theta)
                    val ny = center.y + nodeRadius * sin(theta)

                    drawCircle(
                        color = baseColor.copy(alpha = 0.8f),
                        radius = if (i % 2 == 0) 3.5.dp.toPx() else 2.dp.toPx(),
                        center = Offset(nx, ny)
                    )
                }

                // 6. Thinking gyroscopic sweep arcs
                if (state == JarvisState.THINKING) {
                    drawArc(
                        color = NeonCyan,
                        startAngle = rotationAngle * 2f,
                        sweepAngle = 70f,
                        useCenter = false,
                        topLeft = Offset(center.x - baseRadius * 0.85f, center.y - baseRadius * 0.85f),
                        size = androidx.compose.ui.geometry.Size(baseRadius * 1.7f, baseRadius * 1.7f),
                        style = Stroke(width = 3.dp.toPx())
                    )
                }

                // 7. Concentric Pulsing Neon Waves when LISTENING to microphone activity
                if (state == JarvisState.LISTENING) {
                    val energyRatio = (smoothRms.value / 10f).coerceIn(0f, 1f)

                    // Wave 1: Immediate acoustic pulse ring
                    val wave1Radius = (currentRadius * (1.08f + energyRatio * 0.32f)).coerceAtMost(this.size.minDimension / 2f - 2.dp.toPx())
                    drawCircle(
                        color = baseColor.copy(alpha = (0.7f + energyRatio * 0.3f).coerceIn(0.2f, 0.95f)),
                        radius = wave1Radius,
                        center = center,
                        style = Stroke(width = (2.dp.toPx() + energyRatio * 2.5.dp.toPx()))
                    )

                    // Wave 2: Outer harmonic pulsing neon ring
                    val wave2Radius = (currentRadius * (1.22f + energyRatio * 0.22f)).coerceAtMost(this.size.minDimension / 2f)
                    drawCircle(
                        color = accentColor.copy(alpha = (0.4f + energyRatio * 0.45f).coerceIn(0.1f, 0.85f)),
                        radius = wave2Radius,
                        center = center,
                        style = Stroke(
                            width = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
                        )
                    )

                    // Wave 3: Radial voice energy rays
                    val rayCount = 16
                    val rayAngleStep = (2 * PI / rayCount).toFloat()
                    for (r in 0 until rayCount) {
                        val angle = r * rayAngleStep + (rotationAngle * PI / 180f).toFloat()
                        val rLen = energyRatio * 14.dp.toPx()
                        val rStartX = center.x + (currentRadius * 0.95f) * cos(angle)
                        val rStartY = center.y + (currentRadius * 0.95f) * sin(angle)
                        val rEndX = center.x + (currentRadius * 0.95f + rLen) * cos(angle)
                        val rEndY = center.y + (currentRadius * 0.95f + rLen) * sin(angle)

                        drawLine(
                            color = NeonCyan.copy(alpha = (0.45f + energyRatio * 0.55f)),
                            start = Offset(rStartX, rStartY),
                            end = Offset(rEndX, rEndY),
                            strokeWidth = 2.dp.toPx()
                        )
                    }
                } else if (state == JarvisState.SPEAKING) {
                    val waveAlpha = (0.6f - (smoothRms.value / 25f)).coerceIn(0.15f, 0.7f)
                    drawCircle(
                        color = baseColor.copy(alpha = waveAlpha),
                        radius = (currentRadius * 1.15f).coerceAtMost(this.size.minDimension / 2f),
                        center = center,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // State Text Banner with Sci-Fi HUD look
        Text(
            text = state.label,
            color = baseColor,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 2.sp,
            textAlign = TextAlign.Center
        )

        if (statusText.isNotBlank() && statusText != state.label) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = statusText,
                color = JarvisTextSecondary,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.5.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
