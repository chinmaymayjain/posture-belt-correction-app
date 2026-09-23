package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.PostureTheme
import kotlinx.coroutines.delay

enum class CalibrationStep {
    INSTRUCTIONS,
    COUNTDOWN,
    CALIBRATING,
    SUCCESS,
    ERROR
}

@Composable
fun CalibrationDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onCalibrateRequest: suspend () -> Result<Unit>
) {
    if (!isOpen) return

    val postureColors = PostureTheme.colors
    var currentStep by remember { mutableStateOf(CalibrationStep.INSTRUCTIONS) }
    var countdownValue by remember { mutableIntStateOf(3) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Reset when dialog opens
    LaunchedEffect(isOpen) {
        if (isOpen) {
            currentStep = CalibrationStep.INSTRUCTIONS
            countdownValue = 3
            errorMessage = null
        }
    }

    // Countdown and calibration trigger
    LaunchedEffect(currentStep) {
        if (currentStep == CalibrationStep.COUNTDOWN) {
            countdownValue = 3
            while (countdownValue > 0) {
                delay(1000)
                countdownValue--
            }
            currentStep = CalibrationStep.CALIBRATING
            val result = onCalibrateRequest()
            if (result.isSuccess) {
                currentStep = CalibrationStep.SUCCESS
                delay(1600)
                onDismiss()
            } else {
                errorMessage = result.exceptionOrNull()?.message ?: "Could not complete calibration"
                currentStep = CalibrationStep.ERROR
            }
        }
    }

    Dialog(onDismissRequest = {
        if (currentStep != CalibrationStep.CALIBRATING) {
            onDismiss()
        }
    }) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = postureColors.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("calibration_dialog")
        ) {
            Box(modifier = Modifier.padding(24.dp)) {
                if (currentStep != CalibrationStep.CALIBRATING) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = postureColors.textMuted
                        )
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    when (currentStep) {
                        CalibrationStep.INSTRUCTIONS -> {
                            Text(
                                text = "Calibrate Posture",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = postureColors.text
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Figure in upright posture (0 degrees)
                            PostureFigure(
                                angle = 0f,
                                thr = 20f,
                                modifier = Modifier.size(110.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Sit or stand up straight and relaxed, the way you want to hold yourself.",
                                fontSize = 15.sp,
                                color = postureColors.textMuted,
                                textAlign = TextAlign.Center,
                                lineHeight = 22.sp
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = { currentStep = CalibrationStep.COUNTDOWN },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = postureColors.primary),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("ready_calibrate_button")
                            ) {
                                Text(
                                    text = "I'm ready",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                        }

                        CalibrationStep.COUNTDOWN -> {
                            Text(
                                text = "Get Ready",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = postureColors.text
                            )

                            Spacer(modifier = Modifier.height(28.dp))

                            // Circular countdown ring
                            Box(
                                modifier = Modifier.size(120.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                val progress = countdownValue / 3f
                                Canvas(modifier = Modifier.size(120.dp)) {
                                    drawArc(
                                        color = postureColors.surfaceAlt,
                                        startAngle = -90f,
                                        sweepAngle = 360f,
                                        useCenter = false,
                                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                    drawArc(
                                        color = postureColors.primary,
                                        startAngle = -90f,
                                        sweepAngle = 360f * progress,
                                        useCenter = false,
                                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                }

                                Text(
                                    text = "$countdownValue",
                                    fontSize = 44.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = postureColors.primary
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                text = "Hold your natural straight posture...",
                                fontSize = 15.sp,
                                color = postureColors.textMuted,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        CalibrationStep.CALIBRATING -> {
                            Text(
                                text = "Calibrating",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = postureColors.text
                            )

                            Spacer(modifier = Modifier.height(32.dp))

                            CircularProgressIndicator(
                                modifier = Modifier.size(64.dp),
                                color = postureColors.primary,
                                strokeWidth = 5.dp
                            )

                            Spacer(modifier = Modifier.height(28.dp))

                            Text(
                                text = "Calibrating, hold still...",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = postureColors.text
                            )

                            Spacer(modifier = Modifier.height(20.dp))
                        }

                        CalibrationStep.SUCCESS -> {
                            Text(
                                text = "Calibrated",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = postureColors.text
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .background(postureColors.good.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Success",
                                    tint = postureColors.good,
                                    modifier = Modifier.size(44.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Text(
                                text = "Done. This is your new 0 degrees.",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = postureColors.good,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        CalibrationStep.ERROR -> {
                            Text(
                                text = "Calibration Failed",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = postureColors.bad
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = errorMessage ?: "Could not reach belt to calibrate. Make sure it's turned on.",
                                fontSize = 14.sp,
                                color = postureColors.textMuted,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = { currentStep = CalibrationStep.COUNTDOWN },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = postureColors.primary),
                                modifier = Modifier.fillMaxWidth().height(50.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.size(8.dp))
                                Text(
                                    text = "Try again",
                                    fontSize = 16.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
