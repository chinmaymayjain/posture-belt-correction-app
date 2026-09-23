package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PostureTheme

@Composable
fun ScoreRing(
    score: Int,
    modifier: Modifier = Modifier,
    size: Dp = 54.dp,
    strokeWidth: Dp = 5.dp,
    fontSize: TextUnit = 13.sp,
    showPercentSymbol: Boolean = true,
    targetGoal: Int = 80
) {
    val postureColors = PostureTheme.colors
    val clampedScore = score.coerceIn(0, 100)

    val animatedFraction by animateFloatAsState(
        targetValue = clampedScore / 100f,
        animationSpec = tween(durationMillis = 800),
        label = "scoreRingProgress"
    )

    val ringColor = when {
        clampedScore >= targetGoal -> postureColors.good
        clampedScore >= targetGoal - 10 -> postureColors.warning
        else -> postureColors.bad
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()
            val canvasSize = this.size.minDimension
            val radius = (canvasSize - strokePx) / 2f
            val topLeft = Offset((canvasSize - 2 * radius) / 2f, (canvasSize - 2 * radius) / 2f)
            val arcSize = Size(radius * 2, radius * 2)

            // Background circle track
            drawArc(
                color = postureColors.surfaceAlt,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Active progress arc
            drawArc(
                color = ringColor,
                startAngle = -90f,
                sweepAngle = 360f * animatedFraction,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
        }

        Text(
            text = if (showPercentSymbol) "$clampedScore%" else "$clampedScore",
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            color = postureColors.text
        )
    }
}
