package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ClockViewModel
import com.example.ui.components.GlassmorphicCard
import java.util.Locale

@Composable
fun StopwatchScreen(
    viewModel: ClockViewModel,
    modifier: Modifier = Modifier
) {
    val isRunning by viewModel.stopwatchRunning.collectAsState()
    val stopwatchTimeMs by viewModel.stopwatchTimeMs.collectAsState()
    val laps by viewModel.stopwatchLaps.collectAsState()
    val settings by viewModel.settingsState.collectAsState()

    val themeColor = Color(android.graphics.Color.parseColor(settings.themeColorHex))

    // Format primary display time
    val mins = (stopwatchTimeMs / 60000) % 60
    val secs = (stopwatchTimeMs / 1000) % 60
    val centis = (stopwatchTimeMs / 10) % 100

    val displayTime = String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
    val displayCenti = String.format(Locale.getDefault(), ".%02d", centis)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Counter Glass Circle
        GlassmorphicCard(
            modifier = Modifier
                .size(240.dp)
                .testTag("stopwatch_display_card"),
            intensity = settings.glassmorphismIntensity,
            cornerRadius = 120.dp // Makes card perfectly circular!
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = displayTime,
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = Color.White
                    )
                    Text(
                        text = displayCenti,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = themeColor,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "STOPWATCH",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.4f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            }
        }

        // Controls Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Lap / Reset Button
            OutlinedButton(
                onClick = {
                    if (isRunning) {
                        viewModel.lapStopwatch()
                    } else {
                        viewModel.resetStopwatch()
                    }
                },
                enabled = stopwatchTimeMs > 0,
                modifier = Modifier
                    .size(56.dp)
                    .testTag("stopwatch_lap_reset_btn"),
                shape = CircleShape,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White,
                    disabledContentColor = Color.White.copy(alpha = 0.2f)
                ),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.Flag else Icons.Default.Refresh,
                    contentDescription = if (isRunning) "Lap" else "Reset"
                )
            }

            Spacer(modifier = Modifier.width(32.dp))

            // Play / Pause Button
            ElevatedButton(
                onClick = {
                    if (isRunning) {
                        viewModel.pauseStopwatch()
                    } else {
                        viewModel.startStopwatch()
                    }
                },
                modifier = Modifier
                    .size(72.dp)
                    .testTag("stopwatch_play_pause_btn"),
                shape = CircleShape,
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = themeColor,
                    contentColor = Color.White
                ),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isRunning) "Pause" else "Start",
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        // Laps List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .testTag("stopwatch_laps_list"),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(laps) { lap ->
                val lapMins = (lap.lapTimeMs / 60000) % 60
                val lapSecs = (lap.lapTimeMs / 1000) % 60
                val lapCentis = (lap.lapTimeMs / 10) % 100
                val lapFormatted = String.format(Locale.getDefault(), "%02d:%02d.%02d", lapMins, lapSecs, lapCentis)

                val totalMins = (lap.totalTimeMs / 60000) % 60
                val totalSecs = (lap.totalTimeMs / 1000) % 60
                val totalCentis = (lap.totalTimeMs / 10) % 100
                val totalFormatted = String.format(Locale.getDefault(), "%02d:%02d.%02d", totalMins, totalSecs, totalCentis)

                GlassmorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    intensity = settings.glassmorphismIntensity * 0.7f
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(themeColor.copy(alpha = 0.12f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "#${lap.lapNumber}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = themeColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Split: $lapFormatted",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Text(
                            text = totalFormatted,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
