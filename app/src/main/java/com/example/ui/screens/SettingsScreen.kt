package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLink
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ConnectionState
import com.example.data.model.SettingsConstants
import com.example.ui.MainViewModel
import com.example.ui.components.CustomSliderRow
import com.example.ui.components.ThemeModeSelector
import com.example.ui.components.formatDurationSec
import com.example.ui.theme.PostureTheme

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val postureColors = PostureTheme.colors
    val status by viewModel.status.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val baseUrl by viewModel.baseUrl.collectAsState()
    val beltName by viewModel.beltName.collectAsState()
    val useSimulator by viewModel.useSimulator.collectAsState()

    val draftSettings by viewModel.draftSettings.collectAsState()

    val hasPendingChanges by viewModel.hasPendingChanges.collectAsState()
    val changedCount by viewModel.changedCount.collectAsState()

    val isOffline = connectionState == ConnectionState.OFFLINE
    val isBusy by viewModel.isBusy.collectAsState()

    val currentPreset = SettingsConstants.matchesPreset(
        draftSettings.thr, draftSettings.hold, draftSettings.pulses, draftSettings.pulse, draftSettings.gap, draftSettings.cool
    ) ?: "Custom"

    var showResetWifiDialog by remember { mutableStateOf(false) }
    var showForgetBeltDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val themeMode by viewModel.themeMode.collectAsState()

    Box(modifier = modifier.fillMaxSize().testTag("settings_screen")) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(postureColors.background)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Text(
                text = "Settings",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = postureColors.text
            )

            // Theme Mode Selector Card (System / Light / Dark)
            ThemeModeSelector(
                selectedMode = themeMode,
                onSelectMode = { viewModel.setThemeMode(it) }
            )

            // Offline notice if belt not reachable
            AnimatedVisibility(visible = isOffline) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(postureColors.warning.copy(alpha = 0.12f))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Belt is offline. Slider settings cannot be sent until reconnected.",
                        fontSize = 12.sp,
                        color = postureColors.text
                    )
                }
            }

            // 1. Alert Behavior Card with Sliders & Presets
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
                    .testTag("alert_behaviour_card")
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Alert Behaviour",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = postureColors.text
                        )
                    }

                    // Presets selector row: Gentle, Normal, Strict, Custom
                    Text(
                        text = "Sensitivity presets:",
                        fontSize = 12.sp,
                        color = postureColors.textMuted,
                        modifier = Modifier.padding(top = 10.dp, bottom = 6.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Gentle", "Normal", "Strict", "Custom").forEach { presetName ->
                            val isSelected = currentPreset == presetName
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) postureColors.primary else postureColors.surfaceAlt)
                                    .clickable(enabled = !isOffline && presetName != "Custom") {
                                        viewModel.applyPreset(presetName)
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = presetName,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else postureColors.text
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 6 Sliders
                    CustomSliderRow(
                        label = "Alert angle",
                        helperText = "How far you can lean before the belt reminds you.",
                        value = draftSettings.thr,
                        unit = "°",
                        min = 10f,
                        max = 45f,
                        step = 1f,
                        enabled = !isOffline,
                        onValueChange = { viewModel.updateDraftThr(it) }
                    )

                    CustomSliderRow(
                        label = "Wait before buzzing",
                        helperText = "Short leans, like picking something up, are ignored.",
                        value = draftSettings.hold.toFloat(),
                        unit = "s",
                        min = 1f,
                        max = 10f,
                        step = 1f,
                        enabled = !isOffline,
                        onValueChange = { viewModel.updateDraftHold(it.toInt()) }
                    )

                    CustomSliderRow(
                        label = "Buzzes per alert",
                        helperText = "How many gentle buzzes each reminder has.",
                        value = draftSettings.pulses.toFloat(),
                        unit = "",
                        min = 1f,
                        max = 5f,
                        step = 1f,
                        enabled = !isOffline,
                        onValueChange = { viewModel.updateDraftPulses(it.toInt()) }
                    )

                    CustomSliderRow(
                        label = "Buzz length",
                        helperText = "How long each buzz lasts.",
                        value = draftSettings.pulse.toFloat(),
                        unit = "ms",
                        min = 100f,
                        max = 500f,
                        step = 50f,
                        enabled = !isOffline,
                        onValueChange = { viewModel.updateDraftPulse(it.toInt()) }
                    )

                    CustomSliderRow(
                        label = "Gap between buzzes",
                        helperText = "Pause between buzzes.",
                        value = draftSettings.gap.toFloat(),
                        unit = "ms",
                        min = 50f,
                        max = 500f,
                        step = 25f,
                        enabled = !isOffline,
                        onValueChange = { viewModel.updateDraftGap(it.toInt()) }
                    )

                    CustomSliderRow(
                        label = "Quiet time after alert",
                        helperText = "Rest time before the belt can remind you again.",
                        value = draftSettings.cool.toFloat(),
                        unit = "s",
                        min = 5f,
                        max = 60f,
                        step = 5f,
                        enabled = !isOffline,
                        onValueChange = { viewModel.updateDraftCool(it.toInt()) }
                    )
                }
            }

            // 2. Belt Hardware & Connection Card
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
                    .testTag("your_belt_card")
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "Your Belt",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = postureColors.text
                    )

                    // Simulator toggle row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Demo Simulator Mode", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = postureColors.text)
                            Text("Generates live posture data without physical belt", fontSize = 12.sp, color = postureColors.textMuted)
                        }
                        Switch(
                            checked = useSimulator,
                            onCheckedChange = { viewModel.toggleSimulator(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = postureColors.primary
                            )
                        )
                    }

                    // Details: Name, IP, RSSI, Uptime
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(postureColors.surfaceAlt)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        BeltInfoRow(label = "Belt Name", value = beltName)
                        BeltInfoRow(label = "Address", value = if (useSimulator) "Internal Simulator" else baseUrl)
                        BeltInfoRow(label = "Signal (RSSI)", value = "${status.rssi} dBm")
                        BeltInfoRow(label = "Belt Uptime", value = formatDurationSec(status.uptimeSec))
                    }

                    // Action Rows
                    ActionRow(
                        title = "Pair new belt or setup Wi-Fi",
                        icon = Icons.Default.AddLink,
                        color = postureColors.primary,
                        onClick = { viewModel.setOnboardingOpen(true) }
                    )

                    ActionRow(
                        title = "Recalibrate Posture",
                        icon = Icons.Default.Straighten,
                        onClick = { viewModel.setCalibrationOpen(true) }
                    )

                    ActionRow(
                        title = "Switch belt Wi-Fi",
                        icon = Icons.Default.Wifi,
                        onClick = { showResetWifiDialog = true }
                    )

                    ActionRow(
                        title = "Forget this belt",
                        icon = Icons.Default.Delete,
                        color = postureColors.bad,
                        onClick = { showForgetBeltDialog = true }
                    )
                }
            }

            // 3. App Details Card
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
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "About PostureBelt",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = postureColors.text
                    )
                    Text(
                        text = "PostureBelt Android companion v1.0.0. Communicates via Wi-Fi with ESP32-C3 wearable belt firmware for gentle, real-time spine posture correction.",
                        fontSize = 13.sp,
                        color = postureColors.textMuted,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(60.dp))
        }

        // Sticky "Save Changes" Bar sliding up from bottom
        AnimatedVisibility(
            visible = hasPendingChanges,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(postureColors.surface)
                    .border(1.dp, postureColors.border)
                    .padding(horizontal = 20.dp, vertical = 14.dp)
                    .testTag("save_changes_bar")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "$changedCount ${if (changedCount == 1) "change" else "changes"}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = postureColors.text
                        )
                        Text(
                            text = "Unsaved slider adjustments",
                            fontSize = 11.sp,
                            color = postureColors.textMuted
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { viewModel.resetDraftsToStatus() },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(40.dp)
                        ) {
                            Text("Discard", fontSize = 13.sp, color = postureColors.text)
                        }

                        Button(
                            onClick = { viewModel.saveDraftSettings() },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = postureColors.primary),
                            modifier = Modifier.height(40.dp)
                        ) {
                            Text("Save", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // Dialogs
        if (showResetWifiDialog) {
            AlertDialog(
                onDismissRequest = { showResetWifiDialog = false },
                title = { Text("Switch Belt Wi-Fi?") },
                text = {
                    Text("The belt will restart into setup hotspot mode (PostureBelt-Setup). You can then assign new home Wi-Fi credentials.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showResetWifiDialog = false
                            viewModel.resetBeltWifi()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = postureColors.primary)
                    ) {
                        Text("Restart Belt", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetWifiDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showForgetBeltDialog) {
            AlertDialog(
                onDismissRequest = { showForgetBeltDialog = false },
                title = { Text("Forget This Belt?") },
                text = {
                    Text("This removes the belt address from this phone. You will need to scan or enter it again to reconnect.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showForgetBeltDialog = false
                            viewModel.forgetBelt()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = postureColors.bad)
                    ) {
                        Text("Forget Belt", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showForgetBeltDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun BeltInfoRow(label: String, value: String) {
    val postureColors = PostureTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = postureColors.textMuted)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = postureColors.text)
    }
}

@Composable
fun ActionRow(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color? = null,
    onClick: () -> Unit
) {
    val postureColors = PostureTheme.colors
    val effectiveColor = color ?: postureColors.text

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = effectiveColor, modifier = Modifier.size(18.dp))
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = effectiveColor)
        }
        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = postureColors.textMuted, modifier = Modifier.size(18.dp))
    }
}
