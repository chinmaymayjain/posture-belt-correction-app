package com.example.ui.components

import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.NetworkWifi
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsInputAntenna
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import com.example.data.model.BeltStatus
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.PostureTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class ConnectFlowStep {
    WELCOME,
    RADAR_SCAN,
    FOUND_BELT,
    MANUAL_ENTRY,
    WIFI_WIZARD_STEP1,
    WIFI_WIZARD_STEP2,
    WIFI_WIZARD_STEP3
}

@Composable
fun OnboardingConnectDialog(
    isOpen: Boolean,
    isFirstLaunch: Boolean,
    currentBaseUrl: String,
    onDismiss: () -> Unit,
    onSaveBaseUrl: (String) -> Unit,
    onEnableSimulator: () -> Unit,
    onSaveWifiCredentials: suspend (String, String) -> Boolean,
    onTestConnection: (suspend (String) -> Result<BeltStatus>)? = null
) {
    if (!isOpen) return

    val postureColors = PostureTheme.colors
    val scope = rememberCoroutineScope()

    var step by remember {
        mutableStateOf(if (isFirstLaunch) ConnectFlowStep.WELCOME else ConnectFlowStep.RADAR_SCAN)
    }

    var manualUrl by remember { mutableStateOf(currentBaseUrl.ifEmpty { "http://10.0.2.2:8080" }) }
    var wifiSsid by remember { mutableStateOf("") }
    var wifiPass by remember { mutableStateOf("") }
    var showWifiPass by remember { mutableStateOf(false) }
    var isSavingWifi by remember { mutableStateOf(false) }
    var wifiErrorMsg by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(postureColors.background)
                .testTag("onboarding_connect_dialog"),
            color = postureColors.background
        ) {
            Box(modifier = Modifier.fillMaxSize().padding(20.dp)) {
                // Top close button
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = postureColors.textMuted
                    )
                }

                when (step) {
                    ConnectFlowStep.WELCOME -> {
                        WelcomePager(
                            onDone = { step = ConnectFlowStep.RADAR_SCAN },
                            onSkip = {
                                onEnableSimulator()
                                onDismiss()
                            }
                        )
                    }

                    ConnectFlowStep.RADAR_SCAN -> {
                        RadarScanView(
                            onFound = { step = ConnectFlowStep.FOUND_BELT },
                            onManual = { step = ConnectFlowStep.MANUAL_ENTRY },
                            onWifiSetup = { step = ConnectFlowStep.WIFI_WIZARD_STEP1 },
                            onUseSimulator = {
                                onEnableSimulator()
                                onDismiss()
                            }
                        )
                    }

                    ConnectFlowStep.FOUND_BELT -> {
                        FoundBeltView(
                            onConnect = {
                                onSaveBaseUrl(manualUrl)
                                onDismiss()
                            },
                            onScanAgain = { step = ConnectFlowStep.RADAR_SCAN }
                        )
                    }

                    ConnectFlowStep.MANUAL_ENTRY -> {
                        ManualEntryView(
                            currentUrl = manualUrl,
                            onUrlChange = { manualUrl = it },
                            onConnect = {
                                onSaveBaseUrl(manualUrl)
                                onDismiss()
                            },
                            onTestConnection = onTestConnection,
                            onUseSimulator = {
                                onEnableSimulator()
                                onDismiss()
                            },
                            onBack = { step = ConnectFlowStep.RADAR_SCAN }
                        )
                    }

                    ConnectFlowStep.WIFI_WIZARD_STEP1 -> {
                        WifiWizardStep1(
                            onNext = { step = ConnectFlowStep.WIFI_WIZARD_STEP2 },
                            onBack = { step = ConnectFlowStep.RADAR_SCAN }
                        )
                    }

                    ConnectFlowStep.WIFI_WIZARD_STEP2 -> {
                        WifiWizardStep2(
                            ssid = wifiSsid,
                            onSsidChange = { wifiSsid = it },
                            pass = wifiPass,
                            onPassChange = { wifiPass = it },
                            showPass = showWifiPass,
                            onToggleShowPass = { showWifiPass = !showWifiPass },
                            isLoading = isSavingWifi,
                            errorMessage = wifiErrorMsg,
                            onSend = {
                                scope.launch {
                                    isSavingWifi = true
                                    wifiErrorMsg = null
                                    val success = onSaveWifiCredentials(wifiSsid, wifiPass)
                                    isSavingWifi = false
                                    if (success) {
                                        step = ConnectFlowStep.WIFI_WIZARD_STEP3
                                    } else {
                                        wifiErrorMsg = "Belt did not accept Wi-Fi. Make sure you joined PostureBelt-Setup."
                                    }
                                }
                            },
                            onBack = { step = ConnectFlowStep.WIFI_WIZARD_STEP1 }
                        )
                    }

                    ConnectFlowStep.WIFI_WIZARD_STEP3 -> {
                        WifiWizardStep3(
                            onFinish = {
                                step = ConnectFlowStep.RADAR_SCAN
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WelcomePager(
    onDone: () -> Unit,
    onSkip: () -> Unit
) {
    val postureColors = PostureTheme.colors
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()

    val pages = listOf(
        Triple(
            "Meet your posture coach",
            "The wearable belt monitors your spinal curvature and gently buzzes the moment you slouch.",
            0f
        ),
        Triple(
            "Calibrate to you",
            "Hold your own natural, healthy posture. One tap locks it in as your personal 0° reference.",
            15f
        ),
        Triple(
            "Build the habit",
            "Track daily good posture score, hit streaks, and build lifelong spinal health habits.",
            0f
        )
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onSkip) {
                Text("Skip", color = postureColors.textMuted, fontSize = 15.sp)
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { pageIndex ->
            val (title, desc, figAngle) = pages[pageIndex]
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                PostureFigure(
                    angle = figAngle,
                    thr = 20f,
                    modifier = Modifier.size(130.dp)
                )

                Spacer(modifier = Modifier.height(36.dp))

                Text(
                    text = title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = postureColors.text,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = desc,
                    fontSize = 15.sp,
                    color = postureColors.textMuted,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            }
        }

        // Dots indicator and bottom buttons
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                repeat(3) { idx ->
                    val isActive = pagerState.currentPage == idx
                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .width(if (isActive) 24.dp else 6.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(if (isActive) postureColors.primary else postureColors.surfaceAlt)
                    )
                }
            }

            Button(
                onClick = {
                    if (pagerState.currentPage < 2) {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    } else {
                        onDone()
                    }
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = postureColors.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("onboarding_next_button")
            ) {
                Text(
                    text = if (pagerState.currentPage == 2) "Get Started" else "Next",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun RadarScanView(
    onFound: () -> Unit,
    onManual: () -> Unit,
    onWifiSetup: () -> Unit,
    onUseSimulator: () -> Unit
) {
    val postureColors = PostureTheme.colors
    val transition = rememberInfiniteTransition(label = "radar")
    val pulse1 by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2200, easing = LinearEasing), RepeatMode.Restart),
        label = "radar1"
    )

    // Simulate 4s discovery then prompt
    LaunchedEffect(Unit) {
        delay(4000)
        onFound()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 28.dp)
        ) {
            Text(
                text = "Looking for PostureBelt",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = postureColors.text
            )
            Text(
                text = "Make sure your belt is turned on and connected to the same Wi-Fi.",
                fontSize = 14.sp,
                color = postureColors.textMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Radar Animation
        Box(
            modifier = Modifier.size(240.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(240.dp)) {
                val maxRadius = size.minDimension / 2f
                // 3 concentric rings
                drawCircle(
                    color = postureColors.surfaceAlt,
                    radius = maxRadius,
                    style = Stroke(width = 2.dp.toPx())
                )
                drawCircle(
                    color = postureColors.surfaceAlt,
                    radius = maxRadius * 0.66f,
                    style = Stroke(width = 2.dp.toPx())
                )
                drawCircle(
                    color = postureColors.surfaceAlt,
                    radius = maxRadius * 0.33f,
                    style = Stroke(width = 2.dp.toPx())
                )

                // Expanding wave
                val waveRadius = maxRadius * pulse1
                val waveAlpha = (1f - pulse1).coerceIn(0f, 0.8f)
                drawCircle(
                    color = postureColors.primary.copy(alpha = waveAlpha),
                    radius = waveRadius,
                    style = Stroke(width = 3.dp.toPx())
                )
            }

            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(postureColors.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Sensors,
                    contentDescription = "Radar",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onWifiSetup,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = postureColors.primary),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Icon(Icons.Default.Wifi, contentDescription = null, tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text("Set up New Belt (Wi-Fi Hotspot)", color = Color.White, fontWeight = FontWeight.SemiBold)
            }

            OutlinedButton(
                onClick = onManual,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Enter IP address manually / Direct", color = postureColors.text)
            }

            TextButton(
                onClick = onUseSimulator,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Or try built-in simulation mode", color = postureColors.textMuted)
            }
        }
    }
}

@Composable
fun FoundBeltView(
    onConnect: () -> Unit,
    onScanAgain: () -> Unit
) {
    val postureColors = PostureTheme.colors

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .background(postureColors.good.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Found",
                tint = postureColors.good,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "PostureBelt Found",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = postureColors.text
        )

        Text(
            text = "Ready to pair and calibrate your posture.",
            fontSize = 14.sp,
            color = postureColors.textMuted,
            modifier = Modifier.padding(top = 6.dp)
        )

        Spacer(modifier = Modifier.height(36.dp))

        Button(
            onClick = onConnect,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = postureColors.primary),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Connect & Calibrate", fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(10.dp))

        TextButton(onClick = onScanAgain) {
            Text("Scan again", color = postureColors.textMuted)
        }
    }
}

@Composable
fun ManualEntryView(
    currentUrl: String,
    onUrlChange: (String) -> Unit,
    onConnect: () -> Unit,
    onTestConnection: (suspend (String) -> Result<BeltStatus>)? = null,
    onUseSimulator: () -> Unit,
    onBack: () -> Unit
) {
    val postureColors = PostureTheme.colors
    val scope = rememberCoroutineScope()
    var isTesting by remember { mutableStateOf(false) }
    var testResultText by remember { mutableStateOf<String?>(null) }
    var testSuccess by remember { mutableStateOf<Boolean?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Belt IP Address",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = postureColors.text
            )
            Text(
                text = "Enter the local address of your belt or mock server.",
                fontSize = 14.sp,
                color = postureColors.textMuted,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            OutlinedTextField(
                value = currentUrl,
                onValueChange = {
                    onUrlChange(it)
                    testResultText = null
                    testSuccess = null
                },
                label = { Text("Belt Address (e.g. 192.168.1.42 or 10.0.2.2:8080)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )

            // Preset IP chips for quick filling
            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "http://10.0.2.2:8080" to "Emulator",
                    "http://192.168.4.1" to "Belt Hotspot",
                    "http://192.168.1.50" to "Local Wi-Fi"
                ).forEach { (preset, label) ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(postureColors.surfaceAlt)
                            .border(1.dp, postureColors.border, RoundedCornerShape(8.dp))
                            .clickable {
                                onUrlChange(preset)
                                testResultText = null
                                testSuccess = null
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(label, fontSize = 11.sp, color = postureColors.text)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Test Connection Button & Result indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        if (onTestConnection != null) {
                            scope.launch {
                                isTesting = true
                                testResultText = null
                                testSuccess = null
                                val result = onTestConnection(currentUrl)
                                isTesting = false
                                if (result.isSuccess) {
                                    val status = result.getOrNull()
                                    testSuccess = true
                                    testResultText = "Connected! Angle: ${status?.angle?.toInt() ?: 0}°"
                                } else {
                                    testSuccess = false
                                    testResultText = "Unreachable. Check belt power & Wi-Fi."
                                }
                            }
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(38.dp)
                ) {
                    if (isTesting) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = postureColors.primary)
                        Spacer(Modifier.width(6.dp))
                    }
                    Text("Test Connection", fontSize = 12.sp, color = postureColors.text)
                }

                testResultText?.let { msg ->
                    Text(
                        text = msg,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (testSuccess == true) postureColors.good else postureColors.bad
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = onConnect,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = postureColors.primary),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Connect & Save", color = Color.White, fontWeight = FontWeight.SemiBold)
            }

            OutlinedButton(
                onClick = onUseSimulator,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Use Built-in Simulator", color = postureColors.text)
            }

            TextButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Back to Scan", color = postureColors.textMuted)
            }
        }
    }
}

@Composable
fun WifiWizardStep1(onNext: () -> Unit, onBack: () -> Unit) {
    val postureColors = PostureTheme.colors
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text("Wi-Fi Setup • Step 1 of 3", fontSize = 13.sp, color = postureColors.primary, fontWeight = FontWeight.Bold)
            Text("Join the belt's hotspot", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = postureColors.text, modifier = Modifier.padding(top = 4.dp))
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(postureColors.surface)
                    .border(1.dp, postureColors.surfaceAlt, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text("1. Turn on your PostureBelt.", fontSize = 14.sp, color = postureColors.text)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("2. On your phone, connect to Wi-Fi network:", fontSize = 14.sp, color = postureColors.text)
                    Text("PostureBelt-Setup", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = postureColors.primary, modifier = Modifier.padding(vertical = 4.dp))
                    Text("Password: posture123", fontSize = 14.sp, color = postureColors.textMuted)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Tip: If Android warns there is no internet, tap 'Stay connected'.", fontSize = 12.sp, color = postureColors.textMuted)

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString("PostureBelt-Setup"))
                                Toast.makeText(context, "Copied 'PostureBelt-Setup' to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(38.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Copy SSID", fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                try {
                                    context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
                                } catch (_: Exception) {
                                    Toast.makeText(context, "Open your device Wi-Fi settings", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = postureColors.primary),
                            modifier = Modifier.weight(1f).height(38.dp)
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Open Wi-Fi", fontSize = 11.sp, color = Color.White)
                        }
                    }
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(14.dp)) {
                Text("Cancel", color = postureColors.text)
            }
            Button(onClick = onNext, modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = postureColors.primary)) {
                Text("I'm Connected →", color = Color.White)
            }
        }
    }
}

@Composable
fun WifiWizardStep2(
    ssid: String,
    onSsidChange: (String) -> Unit,
    pass: String,
    onPassChange: (String) -> Unit,
    showPass: Boolean,
    onToggleShowPass: () -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
    onSend: () -> Unit,
    onBack: () -> Unit
) {
    val postureColors = PostureTheme.colors

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text("Wi-Fi Setup • Step 2 of 3", fontSize = 13.sp, color = postureColors.primary, fontWeight = FontWeight.Bold)
            Text("Your Home Wi-Fi", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = postureColors.text, modifier = Modifier.padding(top = 4.dp))
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = ssid,
                onValueChange = onSsidChange,
                label = { Text("Network Name (SSID)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = pass,
                onValueChange = onPassChange,
                label = { Text("Password") },
                visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = onToggleShowPass) {
                        Icon(
                            imageVector = if (showPass) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle password visibility"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))
            Text("Note: Belt supports 2.4 GHz Wi-Fi networks.", fontSize = 12.sp, color = postureColors.textMuted)

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = errorMessage, color = postureColors.bad, fontSize = 13.sp)
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(14.dp), enabled = !isLoading) {
                Text("Back", color = postureColors.text)
            }
            Button(
                onClick = onSend,
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = postureColors.primary),
                enabled = ssid.isNotBlank() && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Send to belt", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun WifiWizardStep3(onFinish: () -> Unit) {
    val postureColors = PostureTheme.colors

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(72.dp).background(postureColors.good.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = postureColors.good, modifier = Modifier.size(36.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text("Credentials Sent!", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = postureColors.text)
        Text(
            text = "Your belt is restarting and joining your home Wi-Fi.\nSwitch your phone back to your home Wi-Fi and find your belt.",
            fontSize = 14.sp,
            color = postureColors.textMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onFinish,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = postureColors.primary),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text("Find My Belt", color = Color.White)
        }
    }
}
