package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppThemeMode
import com.example.ui.MainViewModel
import com.example.ui.components.CalibrationDialog
import com.example.ui.components.OnboardingConnectDialog
import com.example.ui.screens.DevScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.RoutineScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PostureTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            val systemInDark = isSystemInDarkTheme()
            val isDark = when (themeMode) {
                AppThemeMode.SYSTEM -> systemInDark
                AppThemeMode.LIGHT -> false
                AppThemeMode.DARK -> true
            }

            MyApplicationTheme(darkTheme = isDark) {
                PostureBeltApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun PostureBeltApp(viewModel: MainViewModel) {
    val postureColors = PostureTheme.colors
    val selectedTab by viewModel.selectedTab.collectAsState()
    val isCalibrationOpen by viewModel.isCalibrationOpen.collectAsState()
    val isOnboardingOpen by viewModel.isOnboardingOpen.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val baseUrl by viewModel.baseUrl.collectAsState()
    val routineState by viewModel.routineState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(toastMessage) {
        toastMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearToast()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            PostureBottomNavigationBar(
                selectedTab = selectedTab,
                onSelectTab = { viewModel.setSelectedTab(it) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = selectedTab, label = "tabCrossfade") { tab ->
                when (tab) {
                    0 -> HomeScreen(viewModel = viewModel)
                    1 -> HistoryScreen(viewModel = viewModel)
                    2 -> RoutineScreen(viewModel = viewModel)
                    3 -> SettingsScreen(viewModel = viewModel)
                    4 -> DevScreen(viewModel = viewModel)
                    else -> HomeScreen(viewModel = viewModel)
                }
            }

            // Calibration Flow Dialog / Sheet
            CalibrationDialog(
                isOpen = isCalibrationOpen,
                onDismiss = { viewModel.setCalibrationOpen(false) },
                onCalibrateRequest = { viewModel.calibrate() }
            )

            // Onboarding & Connect Dialog
            OnboardingConnectDialog(
                isOpen = isOnboardingOpen,
                isFirstLaunch = !routineState.hasCompletedOnboarding,
                currentBaseUrl = baseUrl,
                onDismiss = {
                    viewModel.setOnboardingOpen(false)
                    viewModel.completeOnboarding()
                },
                onSaveBaseUrl = { url ->
                    viewModel.pairWithBelt(url)
                },
                onEnableSimulator = {
                    viewModel.toggleSimulator(true)
                    viewModel.completeOnboarding()
                },
                onSaveWifiCredentials = { ssid, pass ->
                    viewModel.configureBeltWifi(ssid, pass)
                },
                onTestConnection = { url ->
                    viewModel.testBeltConnection(url)
                }
            )
        }
    }
}

@Composable
fun PostureBottomNavigationBar(
    selectedTab: Int,
    onSelectTab: (Int) -> Unit
) {
    val postureColors = PostureTheme.colors

    val tabs = listOf(
        Triple("Home", Icons.Filled.Home, Icons.Outlined.Home),
        Triple("History", Icons.Filled.BarChart, Icons.Outlined.BarChart),
        Triple("Routine", Icons.Filled.Schedule, Icons.Outlined.Schedule),
        Triple("Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(postureColors.surface)
            .border(
                width = 1.dp,
                color = if (postureColors.isDark) postureColors.border else postureColors.surfaceAlt
            )
            .navigationBarsPadding()
            .testTag("bottom_nav_bar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEachIndexed { index, (label, activeIcon, inactiveIcon) ->
                val isSelected = selectedTab == index
                val animatedColor by animateColorAsState(
                    targetValue = if (isSelected) postureColors.primary else postureColors.textMuted,
                    label = "tabIconColor"
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onSelectTab(index) }
                        )
                        .padding(vertical = 4.dp)
                        .testTag("tab_$label"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (isSelected) activeIcon else inactiveIcon,
                        contentDescription = label,
                        tint = animatedColor,
                        modifier = Modifier.size(24.dp)
                    )

                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = animatedColor,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    // Small primary-colored dot underneath active tab as required by Prompt 1
                    Box(
                        modifier = Modifier
                            .padding(top = 3.dp)
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) postureColors.primary else Color.Transparent)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

