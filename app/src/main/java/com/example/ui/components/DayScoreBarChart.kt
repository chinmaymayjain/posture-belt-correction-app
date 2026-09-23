package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.local.DayRecordEntity
import com.example.ui.theme.PostureTheme

@Composable
fun DayScoreBarChart(
    records: List<DayRecordEntity>,
    targetGoal: Int = 80,
    selectedIndex: Int,
    onSelectDay: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val postureColors = PostureTheme.colors

    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(700),
        label = "barGrowth"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag("day_score_bar_chart")
    ) {
        Canvas(
            modifier = Modifier
                .matchParentSize()
                .pointerInput(records.size) {
                    detectTapGestures { offset ->
                        if (records.isNotEmpty()) {
                            val barWidthTotal = size.width / records.size
                            val tappedIdx = (offset.x / barWidthTotal).toInt().coerceIn(0, records.size - 1)
                            onSelectDay(tappedIdx)
                        }
                    }
                }
        ) {
            val chartWidth = size.width
            val chartHeight = size.height
            val bottomPadding = 24.dp.toPx()
            val usableHeight = chartHeight - bottomPadding

            if (records.isEmpty()) return@Canvas

            val barCount = records.size
            val slotWidth = chartWidth / barCount
            val barWidth = (slotWidth * 0.65f).coerceAtLeast(3.dp.toPx())

            // 1. Draw dashed line for targetGoal (e.g. 80%)
            val goalY = usableHeight * (1f - (targetGoal / 100f))
            drawLine(
                color = postureColors.primary.copy(alpha = 0.6f),
                start = Offset(0f, goalY),
                end = Offset(chartWidth, goalY),
                strokeWidth = 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )

            // 2. Draw each daily score bar
            records.forEachIndexed { index, record ->
                val totalSec = record.goodSec + record.badSec
                val score = if (totalSec > 0) {
                    ((record.goodSec.toDouble() / totalSec) * 100).toInt().coerceIn(0, 100)
                } else {
                    0
                }

                val barHeight = (usableHeight * (score / 100f) * animatedProgress).coerceAtLeast(4.dp.toPx())
                val left = (index * slotWidth) + (slotWidth - barWidth) / 2f
                val top = usableHeight - barHeight

                val isSelected = index == selectedIndex
                val barColor = when {
                    score == 0 -> postureColors.surfaceAlt
                    score >= targetGoal -> postureColors.good
                    score >= (targetGoal - 10) -> postureColors.warning
                    else -> postureColors.bad
                }

                // If selected, draw highlight halo
                if (isSelected) {
                    drawRoundRect(
                        color = postureColors.primary.copy(alpha = 0.25f),
                        topLeft = Offset(left - 3.dp.toPx(), top - 3.dp.toPx()),
                        size = Size(barWidth + 6.dp.toPx(), barHeight + 6.dp.toPx()),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )
                }

                // Draw Bar
                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(left, top),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )

                // Date label tick under the bar
                val tickY = chartHeight - 10.dp.toPx()
                val tickColor = if (isSelected) postureColors.primary else postureColors.textMuted.copy(alpha = 0.4f)
                drawCircle(
                    color = tickColor,
                    radius = if (isSelected) 3.dp.toPx() else 1.5.dp.toPx(),
                    center = Offset(left + barWidth / 2f, tickY)
                )
            }
        }
    }
}
