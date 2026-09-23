package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PostureTheme

@Composable
fun StreakCard(
    streakDays: Int,
    targetGoal: Int = 80,
    modifier: Modifier = Modifier
) {
    val postureColors = PostureTheme.colors

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
            .testTag("streak_card")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(Color(0xFFFF9500).copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = "Streak Flame",
                    tint = Color(0xFFFF9500),
                    modifier = Modifier.size(32.dp)
                )
            }

            Column {
                Text(
                    text = "$streakDays-Day Streak",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = postureColors.text
                )
                Text(
                    text = if (streakDays > 0) {
                        "Consecutive days hitting your $targetGoal% posture goal!"
                    } else {
                        "Reach your $targetGoal% goal today to start a streak!"
                    },
                    fontSize = 13.sp,
                    color = postureColors.textMuted,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}
