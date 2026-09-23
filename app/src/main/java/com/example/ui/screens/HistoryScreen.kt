package com.example.ui.screens

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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DayRecordEntity
import com.example.ui.MainViewModel
import com.example.ui.components.DayScoreBarChart
import com.example.ui.components.ScoreRing
import com.example.ui.components.StreakCard
import com.example.ui.components.formatDurationSec
import com.example.ui.theme.PostureTheme

@Composable
fun HistoryScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val postureColors = PostureTheme.colors
    val allRecords by viewModel.allDayRecords.collectAsState()
    val period by viewModel.historyPeriod.collectAsState()
    val selectedIndex by viewModel.selectedHistoryDayIndex.collectAsState()
    val routineState by viewModel.routineState.collectAsState()

    val targetGoal = routineState.dailyGoal
    val periodCount = if (period == "7d") 7 else 30

    // Take records up to period length, reversed so chronological order left-to-right
    val periodRecords = allRecords.take(periodCount).reversed()

    // Calculate average score for the period
    val averageScore = if (periodRecords.isNotEmpty()) {
        val totalGood = periodRecords.sumOf { it.goodSec }
        val totalBad = periodRecords.sumOf { it.badSec }
        val totalWear = totalGood + totalBad
        if (totalWear > 0) ((totalGood.toDouble() / totalWear) * 100).toInt().coerceIn(0, 100) else 0
    } else 0

    // Calculate streak: consecutive days meeting target goal
    val streakDays = calculateStreak(allRecords, targetGoal)

    // Selected day record (default to most recent)
    val safeSelectedIndex = selectedIndex.coerceIn(0, (periodRecords.size - 1).coerceAtLeast(0))
    val selectedRecord = if (periodRecords.isNotEmpty()) periodRecords[safeSelectedIndex] else null

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(postureColors.background)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .testTag("history_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Posture History",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = postureColors.text
            )

            // Period Segmented Control (7 days / 30 days)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(postureColors.surface)
                    .border(
                        width = 1.dp,
                        color = if (postureColors.isDark) postureColors.border else postureColors.surfaceAlt,
                        shape = RoundedCornerShape(999.dp)
                    )
                    .padding(3.dp)
            ) {
                PeriodChip(
                    label = "7 days",
                    isSelected = period == "7d",
                    onClick = { viewModel.setHistoryPeriod("7d") }
                )
                PeriodChip(
                    label = "30 days",
                    isSelected = period == "30d",
                    onClick = { viewModel.setHistoryPeriod("30d") }
                )
            }
        }

        if (periodRecords.isEmpty()) {
            // Empty State
            EmptyHistoryCard(
                onSeedDemo = { viewModel.seedDemoHistory() }
            )
        } else {
            // Hero Stat Card: Average Score
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
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Average score (${if (period == "7d") "Last 7 days" else "Last 30 days"})",
                            fontSize = 13.sp,
                            color = postureColors.textMuted
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                text = "$averageScore%",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = postureColors.text
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (averageScore >= targetGoal) postureColors.good.copy(alpha = 0.15f)
                                        else postureColors.warning.copy(alpha = 0.15f)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = if (averageScore >= targetGoal) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                    contentDescription = null,
                                    tint = if (averageScore >= targetGoal) postureColors.good else postureColors.warning,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = if (averageScore >= targetGoal) "Above goal" else "Near goal",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (averageScore >= targetGoal) postureColors.good else postureColors.warning
                                )
                            }
                        }
                    }

                    ScoreRing(
                        score = averageScore,
                        size = 54.dp,
                        strokeWidth = 5.dp,
                        targetGoal = targetGoal
                    )
                }
            }

            // Daily Score Bar Chart
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
                    .padding(vertical = 16.dp, horizontal = 12.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Daily Posture Score",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = postureColors.text
                        )
                        Text(
                            text = "Goal: $targetGoal%",
                            fontSize = 12.sp,
                            color = postureColors.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    DayScoreBarChart(
                        records = periodRecords,
                        targetGoal = targetGoal,
                        selectedIndex = safeSelectedIndex,
                        onSelectDay = { viewModel.selectHistoryDay(it) }
                    )
                }
            }

            // Selected Day Card
            if (selectedRecord != null) {
                SelectedDayCard(
                    record = selectedRecord,
                    targetGoal = targetGoal
                )
            }

            // Streak Card
            StreakCard(
                streakDays = streakDays,
                targetGoal = targetGoal
            )
        }

        // Action to seed or clear demo data
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            TextButton(
                onClick = { viewModel.seedDemoHistory() },
                modifier = Modifier.testTag("seed_history_button")
            ) {
                Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Seed 30-Day Demo Records", fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun SelectedDayCard(
    record: DayRecordEntity,
    targetGoal: Int
) {
    val postureColors = PostureTheme.colors
    val totalWearSec = record.goodSec + record.badSec
    val dayScore = if (totalWearSec > 0) {
        ((record.goodSec.toDouble() / totalWearSec) * 100).toInt().coerceIn(0, 100)
    } else 0

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
            .padding(18.dp)
            .testTag("selected_day_card")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = record.date,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = postureColors.text
                    )
                    Text(
                        text = "Total wear time: ${formatDurationSec(totalWearSec)}",
                        fontSize = 12.sp,
                        color = postureColors.textMuted
                    )
                }

                ScoreRing(
                    score = dayScore,
                    size = 46.dp,
                    strokeWidth = 4.dp,
                    targetGoal = targetGoal
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(postureColors.surfaceAlt)
                        .padding(10.dp)
                ) {
                    Column {
                        Text("Good posture", fontSize = 11.sp, color = postureColors.textMuted)
                        Text(formatDurationSec(record.goodSec), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = postureColors.good)
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(postureColors.surfaceAlt)
                        .padding(10.dp)
                ) {
                    Column {
                        Text("Slouch time", fontSize = 11.sp, color = postureColors.textMuted)
                        Text(formatDurationSec(record.badSec), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = postureColors.bad)
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(postureColors.surfaceAlt)
                        .padding(10.dp)
                ) {
                    Column {
                        Text("Reminders", fontSize = 11.sp, color = postureColors.textMuted)
                        Text("${record.alerts}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = postureColors.warning)
                    }
                }
            }
        }
    }
}

@Composable
fun PeriodChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val postureColors = PostureTheme.colors

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (isSelected) postureColors.primary else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) Color.White else postureColors.textMuted
        )
    }
}

@Composable
fun EmptyHistoryCard(
    onSeedDemo: () -> Unit
) {
    val postureColors = PostureTheme.colors

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(postureColors.surface)
            .border(1.dp, postureColors.surfaceAlt, RoundedCornerShape(20.dp))
            .padding(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(postureColors.surfaceAlt, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = postureColors.primary,
                    modifier = Modifier.size(32.dp)
                )
            }

            Text(
                text = "No history yet",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = postureColors.text
            )

            Text(
                text = "Wear your belt today to start your history tracking, or populate with realistic sample data.",
                fontSize = 14.sp,
                color = postureColors.textMuted,
                textAlign = TextAlign.Center
            )

            Button(
                onClick = onSeedDemo,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = postureColors.primary),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Seed 30-Day Sample Data", color = Color.White)
            }
        }
    }
}

fun calculateStreak(records: List<DayRecordEntity>, targetGoal: Int): Int {
    var streak = 0
    for (rec in records) {
        val total = rec.goodSec + rec.badSec
        if (total > 0) {
            val score = ((rec.goodSec.toDouble() / total) * 100).toInt()
            if (score >= targetGoal) {
                streak++
            } else {
                break
            }
        } else {
            break
        }
    }
    return streak
}
