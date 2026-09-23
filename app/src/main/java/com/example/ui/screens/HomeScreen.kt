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
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppThemeMode
import com.example.data.model.ConnectionState
import com.example.ui.MainViewModel
import com.example.ui.components.PostureFigure
import com.example.ui.components.PostureGauge
import com.example.ui.components.StatsCard
import com.example.ui.theme.PostureTheme
import java.util.Calendar

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val postureColors = PostureTheme.colors
    val status by viewModel.status.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val todayRecord by viewModel.todayRecord.collectAsState()
    val routineState by viewModel.routineState.collectAsState()
    val useSimulator by viewModel.useSimulator.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()

    val isOffline = connectionState == ConnectionState.OFFLINE

    // Determine greeting
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when (hour) {
        in 5..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        else -> "Good evening"
    }

    // Effective stats: use todayRecord if available; fallback to belt live seconds
    val goodSec = todayRecord?.goodSec ?: status.goodSec
    val badSec = todayRecord?.badSec ?: status.badSec
    val alerts = todayRecord?.alerts ?: status.alerts

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(postureColors.background)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .testTag("home_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header Row: Greeting & Theme Toggle & Connection Pill
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = greeting,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = postureColors.text
                )
                Text(
                    text = if (useSimulator) "PostureBelt • Simulator" else "PostureBelt Wearable",
                    fontSize = 13.sp,
                    color = postureColors.textMuted
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quick Theme Mode Toggle Button
                val themeIcon = when (themeMode) {
                    AppThemeMode.SYSTEM -> Icons.Default.BrightnessAuto
                    AppThemeMode.LIGHT -> Icons.Default.LightMode
                    AppThemeMode.DARK -> Icons.Default.DarkMode
                }

                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(postureColors.surface)
                        .border(
                            1.dp,
                            if (postureColors.isDark) postureColors.border else postureColors.surfaceAlt,
                            CircleShape
                        )
                        .clickable { viewModel.cycleThemeMode() }
                        .testTag("quick_theme_toggle_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = themeIcon,
                        contentDescription = "Theme: ${themeMode.title}",
                        tint = postureColors.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Connection status pill (tap to open pairing & setup)
                ConnectionPill(
                    connectionState = connectionState,
                    rssi = status.rssi,
                    onClick = { viewModel.setOnboardingOpen(true) }
                )
            }
        }

        // Setup & Pair Wi-Fi Belt banner
        AnimatedVisibility(visible = !routineState.hasCompletedOnboarding || isOffline) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(postureColors.primary.copy(alpha = 0.10f))
                    .border(1.dp, postureColors.primary.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                    .clickable { viewModel.setOnboardingOpen(true) }
                    .padding(14.dp)
                    .testTag("pair_belt_banner")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(postureColors.primary.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Wifi,
                            contentDescription = null,
                            tint = postureColors.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isOffline) "Belt Offline • Tap to Connect" else "Pair Belt or Wi-Fi Setup",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = postureColors.text
                        )
                        Text(
                            text = "Connect via belt hotspot, scan network, or enter IP.",
                            fontSize = 12.sp,
                            color = postureColors.textMuted
                        )
                    }
                    Button(
                        onClick = { viewModel.setOnboardingOpen(true) },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = postureColors.primary),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text("Setup", fontSize = 12.sp, color = Color.White)
                    }
                }
            }
        }

        // Uncalibrated banner
        AnimatedVisibility(visible = !status.calibrated && !isOffline) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(postureColors.warning.copy(alpha = 0.12f))
                    .border(1.dp, postureColors.warning.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .clickable { viewModel.setCalibrationOpen(true) }
                    .padding(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = postureColors.warning,
                        modifier = Modifier.size(24.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Calibration recommended",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = postureColors.text
                        )
                        Text(
                            text = "Tap to set your upright posture as 0 degrees.",
                            fontSize = 12.sp,
                            color = postureColors.textMuted
                        )
                    }
                }
            }
        }

        // 2. Posture Hero Gauge Card
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
                .padding(top = 20.dp, bottom = 12.dp, start = 16.dp, end = 16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Posture Gauge Arc & Animated needle
                PostureGauge(
                    angle = status.angle,
                    thr = status.thr,
                    isCalibrated = status.calibrated,
                    isAlerting = status.alerting,
                    isOffline = isOffline
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Posture Figure dynamic silhouette preview
                PostureFigure(
                    angle = status.angle,
                    thr = status.thr,
                    isOffline = isOffline
                )
            }
        }

        // Offline notice card if belt unreachable
        AnimatedVisibility(visible = isOffline) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(postureColors.surface)
                    .border(1.dp, postureColors.border, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Can't reach your belt",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = postureColors.text
                    )
                    Text(
                        text = "Make sure it's switched on and on the same Wi-Fi as your phone.",
                        fontSize = 13.sp,
                        color = postureColors.textMuted
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.beltRepo.startPolling() },
                            colors = ButtonDefaults.buttonColors(containerColor = postureColors.primary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(38.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Retry", fontSize = 13.sp)
                        }

                        OutlinedButton(
                            onClick = { viewModel.toggleSimulator(true) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(38.dp)
                        ) {
                            Text("Use Simulator Mode", fontSize = 13.sp, color = postureColors.text)
                        }
                    }
                }
            }
        }

        // 3. Action Row: Calibrate and Test Buzz
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { viewModel.setCalibrationOpen(true) },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = postureColors.primary),
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("calibrate_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Straighten,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Calibrate",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            OutlinedButton(
                onClick = { viewModel.triggerTestBuzz() },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("test_buzz_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Vibration,
                    contentDescription = null,
                    tint = postureColors.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Test buzz",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = postureColors.text
                )
            }
        }

        // 4. Today Activity 2x2 Stats Card
        StatsCard(
            goodSec = goodSec,
            badSec = badSec,
            alerts = alerts,
            targetGoal = routineState.dailyGoal
        )

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun ConnectionPill(
    connectionState: ConnectionState,
    rssi: Int,
    onClick: () -> Unit
) {
    val postureColors = PostureTheme.colors

    val (dotColor, text) = when (connectionState) {
        ConnectionState.CONNECTED -> postureColors.good to "Connected"
        ConnectionState.CONNECTING -> postureColors.warning to "Connecting"
        ConnectionState.OFFLINE -> postureColors.textMuted to "Offline"
    }

    val signalBars = when {
        rssi > -55 -> 4
        rssi > -65 -> 3
        rssi > -75 -> 2
        else -> 1
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(postureColors.surface)
            .border(
                width = 1.dp,
                color = if (postureColors.isDark) postureColors.border else postureColors.surfaceAlt,
                shape = RoundedCornerShape(999.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("connection_pill")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(dotColor, CircleShape)
            )
            Text(
                text = text,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = postureColors.text
            )
            if (connectionState == ConnectionState.CONNECTED) {
                Icon(
                    imageVector = Icons.Default.SignalCellularAlt,
                    contentDescription = "$signalBars signal bars",
                    tint = postureColors.good,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}
