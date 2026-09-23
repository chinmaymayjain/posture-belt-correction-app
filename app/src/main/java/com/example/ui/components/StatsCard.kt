package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PostureTheme

fun formatDurationSec(totalSeconds: Long): String {
    if (totalSeconds <= 0) return "0 min"
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    return when {
        hours > 0 && minutes > 0 -> "${hours} h ${minutes} min"
        hours > 0 -> "${hours} h"
        else -> "${maxOf(1, minutes)} min"
    }
}

@Composable
fun StatsCard(
    goodSec: Long,
    badSec: Long,
    alerts: Int,
    targetGoal: Int = 80,
    modifier: Modifier = Modifier
) {
    val postureColors = PostureTheme.colors
    val totalWearSec = goodSec + badSec
    val score = if (totalWearSec > 0) {
        ((goodSec.toDouble() / totalWearSec.toDouble()) * 100).toInt().coerceIn(0, 100)
    } else {
        100
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(postureColors.surface)
            .border(
                width = 1.dp,
                color = if (postureColors.isDark) postureColors.border else postureColors.surfaceAlt,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(18.dp)
            .testTag("today_stats_card")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(
                text = "Today's Activity",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = postureColors.text
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Good Time Tile
                StatTile(
                    label = "Good posture",
                    value = formatDurationSec(goodSec),
                    dotColor = postureColors.good,
                    modifier = Modifier.weight(1f)
                )

                // Slouch Time Tile
                StatTile(
                    label = "Slouch time",
                    value = formatDurationSec(badSec),
                    dotColor = postureColors.bad,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Alerts Tile
                StatTile(
                    label = "Reminders",
                    value = "$alerts",
                    dotColor = postureColors.warning,
                    modifier = Modifier.weight(1f)
                )

                // Score Tile with Ring
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(postureColors.surfaceAlt)
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Score",
                                fontSize = 12.sp,
                                color = postureColors.textMuted
                            )
                            Text(
                                text = "$score%",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = postureColors.text,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        ScoreRing(
                            score = score,
                            size = 42.dp,
                            strokeWidth = 4.dp,
                            fontSize = 11.sp,
                            showPercentSymbol = false,
                            targetGoal = targetGoal
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatTile(
    label: String,
    value: String,
    dotColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    val postureColors = PostureTheme.colors

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(postureColors.surfaceAlt)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 1.dp)
                        .background(dotColor, RoundedCornerShape(999.dp))
                        .padding(3.dp)
                )
                Text(
                    text = label,
                    fontSize = 12.sp,
                    color = postureColors.textMuted
                )
            }
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = postureColors.text,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
