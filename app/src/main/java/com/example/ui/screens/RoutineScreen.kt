package com.example.ui.screens

import android.app.TimePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.components.ScoreRing
import com.example.ui.components.StreakCard
import com.example.ui.components.TipsCarousel
import com.example.ui.theme.PostureTheme

@Composable
fun RoutineScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val postureColors = PostureTheme.colors
    val context = LocalContext.current
    val routineState by viewModel.routineState.collectAsState()
    val todayRecord by viewModel.todayRecord.collectAsState()
    val status by viewModel.status.collectAsState()
    val allRecords by viewModel.allDayRecords.collectAsState()

    val targetGoal = routineState.dailyGoal
    val goodSec = todayRecord?.goodSec ?: status.goodSec
    val badSec = todayRecord?.badSec ?: status.badSec
    val totalWearSec = goodSec + badSec
    val currentScore = if (totalWearSec > 0) {
        ((goodSec.toDouble() / totalWearSec.toDouble()) * 100).toInt().coerceIn(0, 100)
    } else 0

    val isGoalReached = currentScore >= targetGoal && totalWearSec >= 120
    val streakDays = calculateStreak(allRecords, targetGoal)

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(postureColors.background)
            .verticalScroll(scrollState)
            .padding(vertical = 12.dp)
            .testTag("routine_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                text = "Daily Routine",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = postureColors.text
            )
            Text(
                text = "Goals, reminders and habits to keep you upright.",
                fontSize = 13.sp,
                color = postureColors.textMuted,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        // Today's Goal Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(postureColors.surface)
                .border(
                    width = 1.dp,
                    color = if (postureColors.isDark) postureColors.border else postureColors.surfaceAlt,
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(20.dp)
                .testTag("goal_card")
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Daily Posture Target",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = postureColors.text
                        )
                        Text(
                            text = if (isGoalReached) {
                                "Goal reached! Excellent posture discipline today."
                            } else {
                                "$currentScore% of $targetGoal% goal. Keep going!"
                            },
                            fontSize = 13.sp,
                            color = if (isGoalReached) postureColors.good else postureColors.textMuted,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    ScoreRing(
                        score = currentScore,
                        size = 64.dp,
                        strokeWidth = 6.dp,
                        fontSize = 16.sp,
                        targetGoal = targetGoal
                    )
                }

                // Goal Chips Selector: 60%, 70%, 80%, 90%
                Column {
                    Text(
                        text = "Choose target percentage:",
                        fontSize = 12.sp,
                        color = postureColors.textMuted,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(60, 70, 80, 90).forEach { goalValue ->
                            val isSelected = targetGoal == goalValue
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) postureColors.primary else postureColors.surfaceAlt)
                                    .clickable { viewModel.setDailyGoal(goalValue) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$goalValue%",
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else postureColors.text
                                )
                            }
                        }
                    }
                }
            }
        }

        // Reminders Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Belt Notifications",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = postureColors.text
            )

            // 1. Wear reminder card
            ReminderCard(
                title = "Wear reminder",
                subtitle = "Gentle alert to put on your PostureBelt in the morning.",
                icon = Icons.Default.Alarm,
                enabled = routineState.wearReminderEnabled,
                hour = routineState.wearReminderHour,
                minute = routineState.wearReminderMinute,
                onToggle = { isChecked ->
                    viewModel.setWearReminder(isChecked, routineState.wearReminderHour, routineState.wearReminderMinute)
                    viewModel.showToast(if (isChecked) "Morning reminder on" else "Morning reminder off")
                },
                onPickTime = {
                    val dialog = TimePickerDialog(
                        context,
                        { _, h, m -> viewModel.setWearReminder(routineState.wearReminderEnabled, h, m) },
                        routineState.wearReminderHour,
                        routineState.wearReminderMinute,
                        false
                    )
                    dialog.show()
                }
            )

            // 2. Evening summary card
            ReminderCard(
                title = "Evening summary",
                subtitle = "Daily recap of your posture score and reminder counts.",
                icon = Icons.Default.Nightlight,
                enabled = routineState.eveningSummaryEnabled,
                hour = routineState.eveningSummaryHour,
                minute = routineState.eveningSummaryMinute,
                onToggle = { isChecked ->
                    viewModel.setEveningSummary(isChecked, routineState.eveningSummaryHour, routineState.eveningSummaryMinute)
                    viewModel.showToast(if (isChecked) "Evening summary on" else "Evening summary off")
                },
                onPickTime = {
                    val dialog = TimePickerDialog(
                        context,
                        { _, h, m -> viewModel.setEveningSummary(routineState.eveningSummaryEnabled, h, m) },
                        routineState.eveningSummaryHour,
                        routineState.eveningSummaryMinute,
                        false
                    )
                    dialog.show()
                }
            )
        }

        // Shared Streak Card
        Box(modifier = Modifier.padding(horizontal = 20.dp)) {
            StreakCard(
                streakDays = streakDays,
                targetGoal = targetGoal
            )
        }

        // Posture Tips Carousel
        TipsCarousel()

        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
fun ReminderCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean,
    hour: Int,
    minute: Int,
    onToggle: (Boolean) -> Unit,
    onPickTime: () -> Unit
) {
    val postureColors = PostureTheme.colors
    val timeFormatted = formatTime(hour, minute)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(postureColors.surface)
            .border(
                width = 1.dp,
                color = if (postureColors.isDark) postureColors.border else postureColors.surfaceAlt,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(postureColors.primary.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = postureColors.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = postureColors.text
                        )
                        Text(
                            text = subtitle,
                            fontSize = 12.sp,
                            color = postureColors.textMuted,
                            lineHeight = 16.sp
                        )
                    }
                }

                Switch(
                    checked = enabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = postureColors.primary,
                        uncheckedTrackColor = postureColors.surfaceAlt
                    )
                )
            }

            AnimatedVisibility(visible = enabled) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Scheduled time:",
                        fontSize = 13.sp,
                        color = postureColors.textMuted
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(postureColors.surfaceAlt)
                            .clickable(onClick = onPickTime)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = timeFormatted,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = postureColors.primary
                        )
                    }
                }
            }
        }
    }
}

fun formatTime(hour: Int, minute: Int): String {
    val h = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
    val amPm = if (hour >= 12) "PM" else "AM"
    val m = if (minute < 10) "0$minute" else "$minute"
    return "$h:$m $amPm"
}
