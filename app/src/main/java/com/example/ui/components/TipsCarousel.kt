package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PostureTheme

data class PostureTip(
    val title: String,
    val description: String,
    val icon: ImageVector
)

val POSTURE_TIPS = listOf(
    PostureTip(
        title = "Screen at eye level",
        description = "Position monitor so the top third is at eye level, preventing forward neck strain.",
        icon = Icons.Default.Computer
    ),
    PostureTip(
        title = "Micro-breaks matter",
        description = "Stand up and stretch for 45 seconds every 30 minutes of seated desk work.",
        icon = Icons.Default.DirectionsWalk
    ),
    PostureTip(
        title = "Shoulders back & down",
        description = "Roll shoulders back gently; avoid hunching up toward your ears during deep focus.",
        icon = Icons.Default.AccessibilityNew
    ),
    PostureTip(
        title = "Feet flat on the floor",
        description = "Keep both feet grounded evenly with knees bent at a comfortable 90-degree angle.",
        icon = Icons.Default.Straighten
    ),
    PostureTip(
        title = "Engage your core",
        description = "A slight core engagement relieves lower lumbar compression and keeps the pelvis stable.",
        icon = Icons.Default.SelfImprovement
    )
)

@Composable
fun TipsCarousel(modifier: Modifier = Modifier) {
    val postureColors = PostureTheme.colors

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Healthy Habit Tips",
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = postureColors.text,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(POSTURE_TIPS) { tip ->
                Box(
                    modifier = Modifier
                        .size(width = 240.dp, height = 140.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(postureColors.surface)
                        .border(
                            width = 1.dp,
                            color = if (postureColors.isDark) postureColors.border else postureColors.surfaceAlt,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.SpaceBetween) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(postureColors.primary.copy(alpha = 0.12f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = tip.icon,
                                    contentDescription = null,
                                    tint = postureColors.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Text(
                                text = tip.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = postureColors.text
                            )
                        }

                        Text(
                            text = tip.description,
                            fontSize = 12.sp,
                            color = postureColors.textMuted,
                            lineHeight = 17.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
}
