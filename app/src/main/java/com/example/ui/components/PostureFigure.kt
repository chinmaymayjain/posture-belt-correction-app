package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.example.ui.theme.PostureTheme
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun PostureFigure(
    angle: Float,
    thr: Float,
    isOffline: Boolean = false,
    modifier: Modifier = Modifier
) {
    val postureColors = PostureTheme.colors
    val clamped = angle.coerceIn(0f, 60f)

    // Spring animation tracking the posture angle tilt
    val animatedAngle by animateFloatAsState(
        targetValue = clamped,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 160f),
        label = "figureAngle"
    )

    val targetColor = when {
        isOffline -> postureColors.textMuted
        animatedAngle > thr -> postureColors.bad
        animatedAngle >= (thr - 5f) -> postureColors.warning
        else -> postureColors.good
    }

    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(220),
        label = "figureColor"
    )

    Box(
        modifier = modifier.size(110.dp, 120.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(100.dp, 110.dp)) {
            val strokeWidth = 5.dp.toPx()
            val baseColor = animatedColor

            // Anchor pelvis / hips at fixed location
            val pelvisX = size.width * 0.45f
            val pelvisY = size.height * 0.62f

            // 1. Lower body (thigh and shin - seated/support posture)
            val kneeX = pelvisX + size.width * 0.28f
            val kneeY = pelvisY + size.height * 0.04f
            val footX = kneeX
            val footY = pelvisY + size.height * 0.32f

            val legPath = Path().apply {
                moveTo(pelvisX, pelvisY)
                lineTo(kneeX, kneeY)
                lineTo(footX, footY)
            }
            drawPath(
                path = legPath,
                color = postureColors.textMuted.copy(alpha = 0.5f),
                style = Stroke(width = strokeWidth * 0.85f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Chair / base line indicator
            drawLine(
                color = postureColors.surfaceAlt,
                start = Offset(pelvisX - size.width * 0.25f, pelvisY + size.height * 0.06f),
                end = Offset(kneeX + size.width * 0.05f, pelvisY + size.height * 0.06f),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )

            // 2. Torso / Spine line pivoting forward at the pelvis by animatedAngle
            // Lean forward: angle tilts clockwise
            val leanAngleRad = Math.toRadians(animatedAngle.toDouble())
            val spineLength = size.height * 0.40f

            val neckX = (pelvisX + spineLength * sin(leanAngleRad)).toFloat()
            val neckY = (pelvisY - spineLength * cos(leanAngleRad)).toFloat()

            // Spine stroke
            drawLine(
                color = baseColor,
                start = Offset(pelvisX, pelvisY),
                end = Offset(neckX, neckY),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )

            // Head circle positioned slightly above neck aligned with spine lean
            val headDistance = 14.dp.toPx()
            val headRadius = 9.dp.toPx()
            val headCenterX = (neckX + headDistance * sin(leanAngleRad)).toFloat()
            val headCenterY = (neckY - headDistance * cos(leanAngleRad)).toFloat()

            drawCircle(
                color = baseColor,
                radius = headRadius,
                center = Offset(headCenterX, headCenterY)
            )

            // Subtle posture belt marker at lower thoracic / waist
            val beltRatio = 0.32f
            val beltX = pelvisX + (neckX - pelvisX) * beltRatio
            val beltY = pelvisY + (neckY - pelvisY) * beltRatio

            drawCircle(
                color = postureColors.primary,
                radius = 4.5.dp.toPx(),
                center = Offset(beltX, beltY)
            )
        }
    }
}
