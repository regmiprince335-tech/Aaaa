package com.example.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ActionFeedback
import com.example.data.model.ActionStatus
import com.example.ui.theme.JarvisAlertRed
import com.example.ui.theme.JarvisMatrixGreen
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisWarningAmber
import com.example.ui.theme.NeonCyan

@Composable
fun ActionFeedbackBanner(
    feedback: ActionFeedback,
    modifier: Modifier = Modifier
) {
    val (color, icon) = when (feedback.status) {
        ActionStatus.SUCCESS -> Pair(JarvisMatrixGreen, Icons.Default.CheckCircle)
        ActionStatus.UNAVAILABLE -> Pair(JarvisAlertRed, Icons.Default.Warning)
        ActionStatus.PERMISSION_REQUIRED -> Pair(JarvisWarningAmber, Icons.Default.Warning)
        ActionStatus.SYSTEM_ACTION_REQUIRED -> Pair(NeonCyan, Icons.Default.Info)
    }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f)),
        modifier = modifier.fillMaxWidth().testTag("action_feedback_banner")
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = feedback.title,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = feedback.title.uppercase(),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = color,
                    letterSpacing = 1.sp
                )
                Text(
                    text = feedback.message,
                    fontSize = 12.sp,
                    color = JarvisTextPrimary
                )
            }
        }
    }
}
