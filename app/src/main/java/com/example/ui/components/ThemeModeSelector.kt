package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppThemeMode
import com.example.ui.theme.PostureTheme

@Composable
fun ThemeModeSelector(
    selectedMode: AppThemeMode,
    onSelectMode: (AppThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val postureColors = PostureTheme.colors
    val modes = listOf(
        Triple(AppThemeMode.SYSTEM, "System", Icons.Default.BrightnessAuto),
        Triple(AppThemeMode.LIGHT, "Light", Icons.Default.LightMode),
        Triple(AppThemeMode.DARK, "Dark", Icons.Default.DarkMode)
    )

    val selectedIndex = modes.indexOfFirst { it.first == selectedMode }.coerceAtLeast(0)

    Column(
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
            .testTag("theme_mode_selector_card")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Appearance & Theme",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = postureColors.text
                )
                Text(
                    text = selectedMode.subtitle,
                    fontSize = 12.sp,
                    color = postureColors.textMuted,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3-Option Segmented Control with animated sliding pill
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(postureColors.surfaceAlt)
                .padding(4.dp)
        ) {
            val segmentWidth = maxWidth / 3
            val animatedOffset by animateDpAsState(
                targetValue = segmentWidth * selectedIndex,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                label = "themePillOffset"
            )

            // Animated sliding indicator
            Box(
                modifier = Modifier
                    .offset(x = animatedOffset)
                    .width(segmentWidth)
                    .fillMaxHeight()
                    .shadow(elevation = 3.dp, shape = RoundedCornerShape(10.dp))
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (postureColors.isDark) postureColors.surfaceElevated else Color.White
                    )
                    .border(
                        width = 1.dp,
                        color = if (postureColors.isDark) postureColors.border else postureColors.primary.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(10.dp)
                    )
            )

            Row(modifier = Modifier.fillMaxWidth()) {
                modes.forEach { (mode, label, icon) ->
                    val isSelected = mode == selectedMode
                    val textColor by animateColorAsState(
                        targetValue = if (isSelected) postureColors.primary else postureColors.textMuted,
                        label = "themeTextColor"
                    )

                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { onSelectMode(mode) }
                            )
                            .testTag("theme_tab_${mode.name.lowercase()}"),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = textColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = label,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = textColor
                        )
                    }
                }
            }
        }
    }
}
