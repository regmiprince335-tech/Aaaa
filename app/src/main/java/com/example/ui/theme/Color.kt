package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Premium Sci-Fi JARVIS Backgrounds
val JarvisVoid = Color(0xFF050811)
val JarvisBackground = Color(0xFF080C17)
val JarvisSurface = Color(0xFF0E1526)
val JarvisSurfaceVariant = Color(0xFF141E34)
val JarvisSurfaceElevated = Color(0xFF1A2744)

// Primary Neon Cyan
val NeonCyan = Color(0xFF00E5FF)
val NeonCyanBright = Color(0xFF70F3FF)
val NeonCyanDark = Color(0xFF00B0FF)
val NeonCyanGlow = Color(0x3300E5FF)
val NeonCyanSubtle = Color(0x1400E5FF)

// Secondary Electric Purple
val ElectricPurple = Color(0xFF7C4DFF)
val ElectricPurpleBright = Color(0xFFA67CFF)
val ElectricPurpleDark = Color(0xFF651FFF)
val ElectricPurpleGlow = Color(0x337C4DFF)

// Status colors
val JarvisMatrixGreen = Color(0xFF00E676)
val JarvisEmerald = Color(0xFF00FFA3)
val JarvisWarningAmber = Color(0xFFFFB300)
val JarvisAlertRed = Color(0xFFFF2D55)

// Typography and HUD borders
val JarvisTextPrimary = Color(0xFFF3F7FA)
val JarvisTextSecondary = Color(0xFF94A5BD)
val JarvisTextMuted = Color(0xFF556984)
val JarvisBorder = Color(0xFF1A2D48)
val JarvisBorderHighlight = Color(0xFF00E5FF)

// Premium Glassmorphism Gradients
val JarvisGlassBrush = Brush.verticalGradient(
    colors = listOf(Color(0xFF131D31), Color(0xFF0A101D))
)

val JarvisElevatedGlassBrush = Brush.verticalGradient(
    colors = listOf(Color(0xFF18253E), Color(0xFF0E1627))
)

val JarvisNeonBorderBrush = Brush.horizontalGradient(
    colors = listOf(
        Color(0x6600E5FF),
        Color(0x337C4DFF),
        Color(0x6600E5FF)
    )
)

val JarvisCyanPurpleBrush = Brush.linearGradient(
    colors = listOf(NeonCyan, ElectricPurple)
)

val JarvisAuroraGlowBrush = Brush.radialGradient(
    colors = listOf(
        Color(0x2200E5FF),
        Color(0x147C4DFF),
        Color.Transparent
    )
)
