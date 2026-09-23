package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.components.CustomSliderRow
import com.example.ui.components.PostureFigure
import com.example.ui.components.PostureGauge
import com.example.ui.components.ScoreRing
import com.example.ui.theme.PostureTheme

@Composable
fun DevScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val postureColors = PostureTheme.colors
    val status by viewModel.status.collectAsState()
    val scrollState = rememberScrollState()

    var testAngle by remember { mutableFloatStateOf(status.angle) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(postureColors.background)
            .verticalScroll(scrollState)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Developer / Preview Tools", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = postureColors.text)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(postureColors.surface)
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Simulate Live Angle: ${testAngle.toInt()}°", fontWeight = FontWeight.Bold, color = postureColors.text)
                Slider(
                    value = testAngle,
                    onValueChange = {
                        testAngle = it
                        viewModel.beltRepo.simulator.userManualAngle = it
                    },
                    valueRange = 0f..60f
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            testAngle = 8f
                            viewModel.beltRepo.simulator.userManualAngle = 8f
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = postureColors.good)
                    ) {
                        Text("Good (8°)", color = Color.White)
                    }

                    Button(
                        onClick = {
                            testAngle = 18f
                            viewModel.beltRepo.simulator.userManualAngle = 18f
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = postureColors.warning)
                    ) {
                        Text("Warning (18°)", color = Color.White)
                    }

                    Button(
                        onClick = {
                            testAngle = 35f
                            viewModel.beltRepo.simulator.userManualAngle = 35f
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = postureColors.bad)
                    ) {
                        Text("Alert (35°)", color = Color.White)
                    }
                }

                Button(
                    onClick = {
                        viewModel.beltRepo.simulator.userManualAngle = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = postureColors.primary)
                ) {
                    Text("Resume Natural Wave", color = Color.White)
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(postureColors.surface)
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Database & Seed Actions", fontWeight = FontWeight.Bold, color = postureColors.text)
                Button(
                    onClick = { viewModel.seedDemoHistory() },
                    colors = ButtonDefaults.buttonColors(containerColor = postureColors.primary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Seed 30-Day History Records", color = Color.White)
                }

                OutlinedButton(
                    onClick = { viewModel.clearHistory() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Clear All History", color = postureColors.bad)
                }
            }
        }
    }
}
