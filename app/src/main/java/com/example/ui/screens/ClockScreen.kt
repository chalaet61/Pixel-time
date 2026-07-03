package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ClockViewModel
import com.example.ui.components.GlassmorphicCard
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ClockScreen(
    viewModel: ClockViewModel,
    modifier: Modifier = Modifier
) {
    val currentTimeMs by viewModel.currentTime.collectAsState()
    val settings by viewModel.settingsState.collectAsState()

    val date = Date(currentTimeMs)
    val timeZone = TimeZone.getDefault()
    val zoneId = ZoneId.systemDefault()

    // 12 vs 24 hour formats
    val timePattern = if (settings.is24HourFormat) {
        if (settings.showSeconds) "HH:mm:ss" else "HH:mm"
    } else {
        if (settings.showSeconds) "hh:mm:ss a" else "hh:mm a"
    }
    val timeFormatter = SimpleDateFormat(timePattern, Locale.getDefault())
    val formattedTime = timeFormatter.format(date)

    // Separate time and AM/PM if 12 hour to style AM/PM smaller
    val mainTimeText: String
    val amPmText: String
    if (!settings.is24HourFormat) {
        val parts = formattedTime.split(" ")
        mainTimeText = parts.getOrNull(0) ?: formattedTime
        amPmText = parts.getOrNull(1) ?: ""
    } else {
        mainTimeText = formattedTime
        amPmText = ""
    }

    val dateFormatter = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
    val formattedDate = dateFormatter.format(date)

    val utcOffset = getUtcOffsetString(timeZone)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Visual Layout Mode: Analog Clock
        if (settings.isAnalogEnabled) {
            GlassmorphicCard(
                modifier = Modifier
                    .size(240.dp)
                    .testTag("analog_clock_card"),
                intensity = settings.glassmorphismIntensity
            ) {
                AnalogClockCanvas(
                    timeMs = currentTimeMs,
                    accentColor = Color(android.graphics.Color.parseColor(settings.themeColorHex))
                )
            }
        }

        // Digital Clock Premium display Card
        GlassmorphicCard(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("digital_clock_card"),
            intensity = settings.glassmorphismIntensity
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Time HH:MM:SS
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = mainTimeText,
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = if (settings.showSeconds) 48.sp else 56.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = Color.White,
                        modifier = Modifier.testTag("live_digital_time")
                    )
                    if (amPmText.isNotEmpty()) {
                        Spacer(modifier = Modifier.size(6.dp))
                        Text(
                            text = amPmText,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 20.sp
                            ),
                            color = Color(android.graphics.Color.parseColor(settings.themeColorHex)).copy(alpha = 0.9f),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Date
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Date",
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.85f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Device Time Zone Card
        GlassmorphicCard(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("timezone_info_card"),
            intensity = settings.glassmorphismIntensity
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White.copy(alpha = 0.08f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "TimeZone",
                            tint = Color(android.graphics.Color.parseColor(settings.themeColorHex))
                        )
                    }
                    Spacer(modifier = Modifier.size(12.dp))
                    Column {
                        Text(
                            text = timeZone.getDisplayName(true, TimeZone.SHORT, Locale.getDefault()),
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = zoneId.id,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }

                Text(
                    text = utcOffset,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(android.graphics.Color.parseColor(settings.themeColorHex)),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
        }

        // Action Quick Toggle Button to switch Analog layout
        OutlinedButton(
            onClick = {
                viewModel.updateSettings(settings.copy(isAnalogEnabled = !settings.isAnalogEnabled))
            },
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color.White
            ),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                width = 1.dp
            ),
            modifier = Modifier
                .padding(top = 8.dp)
                .testTag("toggle_analog_button")
        ) {
            Icon(
                imageVector = Icons.Default.AccessTime,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = if (settings.isAnalogEnabled) "Hide Analog Clock" else "Show Analog Clock",
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun AnalogClockCanvas(
    timeMs: Long,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = timeMs

    val seconds = calendar.get(Calendar.SECOND) + (calendar.get(Calendar.MILLISECOND) / 1000f)
    val minutes = calendar.get(Calendar.MINUTE) + (seconds / 60f)
    val hours = (calendar.get(Calendar.HOUR) % 12) + (minutes / 60f)

    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = (size.width.coerceAtMost(size.height) / 2) * 0.85f

        // Draw Clock Ring Outer Boundary
        drawCircle(
            color = Color.White.copy(alpha = 0.15f),
            radius = radius,
            center = center,
            style = Stroke(width = 4.dp.toPx())
        )

        // Draw Hour Ticks (12, 3, 6, 9 ticks bold, others thin)
        for (i in 0 until 12) {
            val angle = i * 30.0
            val angleRad = Math.toRadians(angle)
            val startRadius = radius * if (i % 3 == 0) 0.8f else 0.88f
            val endRadius = radius * 0.95f

            val startX = center.x + startRadius * sin(angleRad).toFloat()
            val startY = center.y - startRadius * cos(angleRad).toFloat()
            val endX = center.x + endRadius * sin(angleRad).toFloat()
            val endY = center.y - endRadius * cos(angleRad).toFloat()

            drawLine(
                color = if (i % 3 == 0) Color.White.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.3f),
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = if (i % 3 == 0) 3.dp.toPx() else 1.5.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // 1. Hour Hand (shorter, thicker)
        val hourAngle = hours * 30.0
        val hourRad = Math.toRadians(hourAngle)
        val hourLength = radius * 0.5f
        val hourEndX = center.x + hourLength * sin(hourRad).toFloat()
        val hourEndY = center.y - hourLength * cos(hourRad).toFloat()
        drawLine(
            color = Color.White,
            start = center,
            end = Offset(hourEndX, hourEndY),
            strokeWidth = 6.dp.toPx(),
            cap = StrokeCap.Round
        )

        // 2. Minute Hand (longer, medium)
        val minuteAngle = minutes * 6.0
        val minuteRad = Math.toRadians(minuteAngle)
        val minuteLength = radius * 0.75f
        val minuteEndX = center.x + minuteLength * sin(minuteRad).toFloat()
        val minuteEndY = center.y - minuteLength * cos(minuteRad).toFloat()
        drawLine(
            color = Color.White.copy(alpha = 0.8f),
            start = center,
            end = Offset(minuteEndX, minuteEndY),
            strokeWidth = 4.dp.toPx(),
            cap = StrokeCap.Round
        )

        // 3. Second Hand (longest, thin, accentColor)
        val secondAngle = seconds * 6.0
        val secondRad = Math.toRadians(secondAngle)
        val secondLength = radius * 0.88f
        val secondEndX = center.x + secondLength * sin(secondRad).toFloat()
        val secondEndY = center.y - secondLength * cos(secondRad).toFloat()
        drawLine(
            color = accentColor,
            start = center,
            end = Offset(secondEndX, secondEndY),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Draw Center Pivot Pin
        drawCircle(
            color = accentColor,
            radius = 6.dp.toPx(),
            center = center
        )
        drawCircle(
            color = Color.White,
            radius = 3.dp.toPx(),
            center = center
        )
    }
}

fun getUtcOffsetString(timeZone: TimeZone): String {
    val offsetMinutes = timeZone.rawOffset / (1000 * 60)
    val hours = offsetMinutes / 60
    val minutes = kotlin.math.abs(offsetMinutes % 60)
    val sign = if (hours >= 0) "+" else "-"
    return String.format(Locale.getDefault(), "UTC %s%02d:%02d", sign, kotlin.math.abs(hours), minutes)
}
