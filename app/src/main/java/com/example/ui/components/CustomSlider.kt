package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import kotlin.math.roundToInt

@Composable
fun CustomSliderRow(
    label: String,
    helperText: String,
    value: Float,
    unit: String,
    min: Float,
    max: Float,
    step: Float,
    enabled: Boolean = true,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val postureColors = PostureTheme.colors
    val stepsCount = ((max - min) / step).roundToInt() - 1
    val displayValue = if (step >= 1f) value.toInt().toString() else "%.1f".format(value)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .testTag("slider_row_${label.lowercase().replace(" ", "_")}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (enabled) postureColors.text else postureColors.textMuted
                )
                Text(
                    text = helperText,
                    fontSize = 12.sp,
                    color = postureColors.textMuted,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(postureColors.surfaceAlt)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "$displayValue $unit".trim(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (enabled) postureColors.primary else postureColors.textMuted
                )
            }
        }

        Slider(
            value = value.coerceIn(min, max),
            onValueChange = { newVal ->
                val snapped = (Math.round((newVal - min) / step) * step + min).coerceIn(min, max)
                onValueChange(snapped)
            },
            valueRange = min..max,
            steps = if (stepsCount > 0) stepsCount else 0,
            enabled = enabled,
            colors = SliderDefaults.colors(
                thumbColor = postureColors.primary,
                activeTrackColor = postureColors.primary,
                inactiveTrackColor = postureColors.surfaceAlt,
                disabledThumbColor = postureColors.textMuted,
                disabledActiveTrackColor = postureColors.textMuted.copy(alpha = 0.4f),
                disabledInactiveTrackColor = postureColors.surfaceAlt
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        )
    }
}
