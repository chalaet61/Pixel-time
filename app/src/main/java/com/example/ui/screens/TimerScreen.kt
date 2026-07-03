package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ClockViewModel
import com.example.ui.components.GlassmorphicCard
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TimerScreen(
    viewModel: ClockViewModel,
    modifier: Modifier = Modifier
) {
    val isRunning by viewModel.timerRunning.collectAsState()
    val isPaused by viewModel.timerIsPaused.collectAsState()
    val remainingMs by viewModel.timerRemainingMs.collectAsState()
    val totalMs by viewModel.timerTotalMs.collectAsState()
    val settings by viewModel.settingsState.collectAsState()

    val themeColor = Color(android.graphics.Color.parseColor(settings.themeColorHex))

    val presets = listOf(
        PresetTimer("1 min", 60),
        PresetTimer("5 min", 300),
        PresetTimer("10 min", 600),
        PresetTimer("15 min", 900),
        PresetTimer("30 min", 1800),
        PresetTimer("1 hr", 3600)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        if (isRunning || isPaused) {
            // Visual Countdown Mode with Circular Canvas progress
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .testTag("timer_progress_container"),
                contentAlignment = Alignment.Center
            ) {
                val progress = if (totalMs > 0) remainingMs.toFloat() / totalMs.toFloat() else 0f

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 10.dp.toPx()
                    val center = Offset(size.width / 2, size.height / 2)
                    val radius = (size.width - strokeWidth) / 2

                    // Track background
                    drawCircle(
                        color = Color.White.copy(alpha = 0.1f),
                        radius = radius,
                        center = center,
                        style = Stroke(width = strokeWidth)
                    )

                    // Active sweep arc
                    drawArc(
                        color = themeColor,
                        startAngle = -90f,
                        sweepAngle = progress * 360f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        size = Size(radius * 2, radius * 2),
                        topLeft = Offset(center.x - radius, center.y - radius)
                    )
                }

                // Center countdown text
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val totSec = (remainingMs + 999) / 1000 // ceil division so it hits 0 exactly at the finish
                    val h = totSec / 3600
                    val m = (totSec % 3600) / 60
                    val s = totSec % 60
                    val timeString = if (h > 0) {
                        String.format(Locale.getDefault(), "%02d:%02d:%02d", h, m, s)
                    } else {
                        String.format(Locale.getDefault(), "%02d:%02d", m, s)
                    }

                    Text(
                        text = timeString,
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = Color.White,
                        modifier = Modifier.testTag("timer_countdown_text")
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (isPaused) "PAUSED" else "COUNTING",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isPaused) Color.White.copy(alpha = 0.4f) else themeColor,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons for running timer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cancel
                OutlinedButton(
                    onClick = { viewModel.resetTimer() },
                    modifier = Modifier
                        .size(56.dp)
                        .testTag("timer_cancel_btn"),
                    shape = CircleShape,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Cancel Timer")
                }

                Spacer(modifier = Modifier.width(32.dp))

                // Play / Pause
                ElevatedButton(
                    onClick = {
                        if (isPaused) {
                            viewModel.startTimer()
                        } else {
                            viewModel.pauseTimer()
                        }
                    },
                    modifier = Modifier
                        .size(72.dp)
                        .testTag("timer_play_pause_btn"),
                    shape = CircleShape,
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = themeColor,
                        contentColor = Color.White
                    ),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = if (isPaused) "Resume" else "Pause",
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        } else {
            // Setup Mode with Number Selectors and presets
            GlassmorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("timer_setup_card"),
                intensity = settings.glassmorphismIntensity
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Set Timer Duration",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Hours
                        TimeColumnAdjuster(
                            label = "HOURS",
                            value = viewModel.timerInputHours,
                            onValueChange = { viewModel.timerInputHours = it },
                            maxValue = 23
                        )

                        Text(":", fontSize = 32.sp, color = Color.White.copy(alpha = 0.5f), modifier = Modifier.padding(bottom = 16.dp))

                        // Minutes
                        TimeColumnAdjuster(
                            label = "MINUTES",
                            value = viewModel.timerInputMinutes,
                            onValueChange = { viewModel.timerInputMinutes = it },
                            maxValue = 59
                        )

                        Text(":", fontSize = 32.sp, color = Color.White.copy(alpha = 0.5f), modifier = Modifier.padding(bottom = 16.dp))

                        // Seconds
                        TimeColumnAdjuster(
                            label = "SECONDS",
                            value = viewModel.timerInputSeconds,
                            onValueChange = { viewModel.timerInputSeconds = it },
                            maxValue = 59
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Preset chips
            Text(
                text = "PRESETS",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.4f),
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presets.forEach { preset ->
                    Card(
                        onClick = {
                            viewModel.timerInputHours = preset.seconds / 3600
                            viewModel.timerInputMinutes = (preset.seconds % 3600) / 60
                            viewModel.timerInputSeconds = preset.seconds % 60
                            viewModel.startTimer()
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.05f),
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .testTag("preset_${preset.name}")
                            .clickable {
                                viewModel.timerInputHours = preset.seconds / 3600
                                viewModel.timerInputMinutes = (preset.seconds % 3600) / 60
                                viewModel.timerInputSeconds = preset.seconds % 60
                                viewModel.startTimer()
                            }
                    ) {
                        Text(
                            text = preset.name,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Start Timer Action Button
            ElevatedButton(
                onClick = { viewModel.startTimer() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("start_timer_btn"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = themeColor,
                    contentColor = Color.White
                )
            ) {
                Icon(imageVector = Icons.Default.HourglassEmpty, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Start Timer", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun TimeColumnAdjuster(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    maxValue: Int
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f))
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { if (value > 0) onValueChange(value - 1) else onValueChange(maxValue) }) {
                Text("-", color = Color.White, fontSize = 24.sp)
            }
            Text(
                text = String.format(Locale.getDefault(), "%02d", value),
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 6.dp)
            )
            IconButton(onClick = { if (value < maxValue) onValueChange(value + 1) else onValueChange(0) }) {
                Text("+", color = Color.White, fontSize = 24.sp)
            }
        }
    }
}

data class PresetTimer(val name: String, val seconds: Int)
