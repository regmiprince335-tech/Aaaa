package com.example.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.JarvisBorder
import com.example.ui.theme.JarvisGlassBrush
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.NeonCyan

@Composable
fun HudCard(
    modifier: Modifier = Modifier,
    borderColor: Color = JarvisBorder,
    borderBrush: Brush? = null,
    borderWidth: Dp = 1.dp,
    backgroundColor: Color = JarvisSurface,
    backgroundBrush: Brush? = JarvisGlassBrush,
    cornerRadius: Dp = 14.dp,
    showSpecularHighlight: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val effectiveBorderStroke = if (borderBrush != null) {
        BorderStroke(borderWidth, borderBrush)
    } else {
        BorderStroke(borderWidth, borderColor)
    }

    Box(
        modifier = modifier
            .clip(shape)
            .then(
                if (backgroundBrush != null) Modifier.background(backgroundBrush, shape)
                else Modifier.background(backgroundColor, shape)
            )
            .border(effectiveBorderStroke, shape)
            .then(
                if (showSpecularHighlight) {
                    Modifier.drawBehind {
                        // Subtle specular top hairline glow (futuristic glass edge)
                        val strokePx = 1.2.dp.toPx()
                        drawLine(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    NeonCyan.copy(alpha = 0.35f),
                                    Color.White.copy(alpha = 0.25f),
                                    NeonCyan.copy(alpha = 0.35f),
                                    Color.Transparent
                                )
                            ),
                            start = Offset(cornerRadius.toPx(), strokePx / 2f),
                            end = Offset(size.width - cornerRadius.toPx(), strokePx / 2f),
                            strokeWidth = strokePx
                        )
                    }
                } else Modifier
            )
            .padding(14.dp),
        content = content
    )
}
