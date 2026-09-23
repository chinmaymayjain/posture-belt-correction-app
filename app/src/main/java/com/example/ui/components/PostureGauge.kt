package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PostureTheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun PostureGauge(
    angle: Float,
    thr: Float,
    isCalibrated: Boolean,
    isAlerting: Boolean,
    isOffline: Boolean,
    modifier: Modifier = Modifier
) {
    val postureColors = PostureTheme.colors
    val clampedAngle = angle.coerceIn(0f, 60f)

    // Smooth spring animation for the gauge progress
    val animatedAngle by animateFloatAsState(
        targetValue = clampedAngle,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 160f),
        label = "gaugeAngle"
    )

    // Determine posture color based on angle and threshold
    val targetColor = when {
        isOffline -> postureColors.textMuted
        !isCalibrated -> postureColors.warning
        animatedAngle > thr -> postureColors.bad
        animatedAngle >= (thr - 5f) -> postureColors.warning
        else -> postureColors.good
    }

    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(220),
        label = "gaugeColor"
    )

    // Pulse animation when alerting
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    val statusText = when {
        isOffline -> "Belt offline"
        !isCalibrated -> "Not calibrated yet"
        animatedAngle > thr -> "Sit up straight"
        animatedAngle >= (thr - 5f) -> "Careful, you're leaning"
        else -> "Great posture"
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(230.dp)
            .testTag("posture_gauge"),
        contentAlignment = Alignment.Center
    ) {
        // Alert pulsing ring behind the gauge
        if (isAlerting && !isOffline) {
            Box(
                modifier = Modifier
                    .size(210.dp * pulseScale)
                    .background(
                        color = postureColors.bad.copy(alpha = pulseAlpha),
                        shape = CircleShape
                    )
            )
            Box(
                modifier = Modifier
                    .size(175.dp * (1f + (pulseScale - 1f) * 0.7f))
                    .background(
                        color = postureColors.bad.copy(alpha = pulseAlpha * 0.8f),
                        shape = CircleShape
                    )
            )
        }

        // Canvas for Arc and Needle
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // Center of semi-circle arc sits near bottom center
            val arcDiameter = size.minDimension * 0.85f
            val arcRadius = arcDiameter / 2f
            val arcCenter = Offset(canvasWidth / 2f, canvasHeight * 0.76f)
            val strokeWidth = 24.dp.toPx()

            val arcTopLeft = Offset(arcCenter.x - arcRadius, arcCenter.y - arcRadius)
            val arcSize = Size(arcDiameter, arcDiameter)

            // Semi-circle from 180° to 360° (0° on left to 60° on right)
            val startAngle = 180f
            val sweepAngleTotal = 180f

            // 1. Background Arc Track
            drawArc(
                color = postureColors.surfaceAlt,
                startAngle = startAngle,
                sweepAngle = sweepAngleTotal,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // 2. Active Progress Arc
            val progressFraction = (animatedAngle / 60f).coerceIn(0.01f, 1f)
            val activeSweep = sweepAngleTotal * progressFraction

            drawArc(
                color = animatedColor,
                startAngle = startAngle,
                sweepAngle = activeSweep,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // 3. Threshold Tick Marker (thr)
            val thrFraction = (thr / 60f).coerceIn(0f, 1f)
            val thrAngleDeg = startAngle + (sweepAngleTotal * thrFraction)
            val thrRad = Math.toRadians(thrAngleDeg.toDouble())

            val tickInner = arcRadius - strokeWidth * 0.65f
            val tickOuter = arcRadius + strokeWidth * 0.65f

            val tickStart = Offset(
                (arcCenter.x + tickInner * cos(thrRad)).toFloat(),
                (arcCenter.y + tickInner * sin(thrRad)).toFloat()
            )
            val tickEnd = Offset(
                (arcCenter.x + tickOuter * cos(thrRad)).toFloat(),
                (arcCenter.y + tickOuter * sin(thrRad)).toFloat()
            )

            drawLine(
                color = postureColors.text,
                start = tickStart,
                end = tickEnd,
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )

            // 4. Subtle Needle Pointer Indicator at current angle
            val needleAngleDeg = startAngle + (sweepAngleTotal * progressFraction)
            val needleRad = Math.toRadians(needleAngleDeg.toDouble())
            val needleOuterRadius = arcRadius - strokeWidth * 0.7f
            val needlePoint = Offset(
                (arcCenter.x + needleOuterRadius * cos(needleRad)).toFloat(),
                (arcCenter.y + needleOuterRadius * sin(needleRad)).toFloat()
            )

            drawCircle(
                color = animatedColor,
                radius = 7.dp.toPx(),
                center = needlePoint
            )
            drawCircle(
                color = Color.White,
                radius = 3.5.dp.toPx(),
                center = needlePoint
            )
        }

        // Center Text: Angle & Status
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${animatedAngle.toInt()}°",
                fontSize = 54.sp,
                fontWeight = FontWeight.Bold,
                color = if (isOffline) postureColors.textMuted else postureColors.text,
                lineHeight = 56.sp
            )

            Text(
                text = statusText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = animatedColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )

            if (!isOffline && isCalibrated) {
                Text(
                    text = "Alert at ${thr.toInt()}°",
                    fontSize = 12.sp,
                    color = postureColors.textMuted,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}
