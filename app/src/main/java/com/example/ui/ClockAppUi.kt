package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.screens.AlarmScreen
import com.example.ui.screens.ClockScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StopwatchScreen
import com.example.ui.screens.TimerScreen
import com.example.ui.screens.WorldClockScreen
import java.util.Locale

@Composable
fun ClockAppUi(
    viewModel: ClockViewModel,
    modifier: Modifier = Modifier
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val settings by viewModel.settingsState.collectAsState()
    val ringingAlarm by viewModel.ringingAlarm.collectAsState()
    val timerFinishedAlert by viewModel.timerFinishedAlert.collectAsState()

    val themeColor = Color(android.graphics.Color.parseColor(settings.themeColorHex))

    // Premium background gradient (dark elegant twilight space color)
    val appBackgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF090A0E),
            Color(0xFF11131A),
            Color(0xFF0A0C10)
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(appBackgroundGradient)
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            bottomBar = {
                CustomBottomNavigationBar(
                    currentTab = currentTab,
                    onTabSelected = { viewModel.selectTab(it) },
                    themeColor = themeColor,
                    intensity = settings.glassmorphismIntensity
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .statusBarsPadding()
            ) {
                // Smooth transition swapper for Premium Google Pixel look
                AnimatedContent(
                    targetState = currentTab,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(150))
                    },
                    label = "tab_navigation_content",
                    modifier = Modifier.fillMaxSize()
                ) { targetTab ->
                    when (targetTab) {
                        ClockViewModel.ClockTab.CLOCK -> ClockScreen(viewModel = viewModel)
                        ClockViewModel.ClockTab.WORLD_CLOCK -> WorldClockScreen(viewModel = viewModel)
                        ClockViewModel.ClockTab.ALARM -> AlarmScreen(viewModel = viewModel)
                        ClockViewModel.ClockTab.STOPWATCH -> StopwatchScreen(viewModel = viewModel)
                        ClockViewModel.ClockTab.TIMER -> TimerScreen(viewModel = viewModel)
                        ClockViewModel.ClockTab.SETTINGS -> SettingsScreen(viewModel = viewModel)
                    }
                }
            }
        }

        // --- OVERLAYS & DIALOG ALERTS ---

        // 1. Ringing Alarm Overlay Alert Screen
        AnimatedVisibility(
            visible = ringingAlarm != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            ringingAlarm?.let { alarm ->
                RingingAlarmOverlay(
                    alarm = alarm,
                    themeColor = themeColor,
                    onDismiss = { viewModel.dismissRingingAlarm() },
                    onSnooze = { viewModel.snoozeRingingAlarm() }
                )
            }
        }

        // 2. Finished Timer Alert Dialog
        if (timerFinishedAlert) {
            TimerFinishedDialog(
                themeColor = themeColor,
                onDismiss = { viewModel.dismissTimerAlert() }
            )
        }
    }
}

@Composable
fun CustomBottomNavigationBar(
    currentTab: ClockViewModel.ClockTab,
    onTabSelected: (ClockViewModel.ClockTab) -> Unit,
    themeColor: Color,
    intensity: Float
) {
    val navBarBackground = Color(0xFF161822).copy(alpha = 0.82f * intensity)
    val navBarBorder = Brush.linearGradient(
        colors = listOf(
            Color(0xFFFFFFFF).copy(alpha = 0.12f * intensity),
            Color(0xFFFFFFFF).copy(alpha = 0.02f * intensity)
        )
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("custom_bottom_nav_bar"),
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent,
        border = androidx.compose.foundation.BorderStroke(1.dp, navBarBorder),
        shadowElevation = (12 * intensity).dp
    ) {
        Row(
            modifier = Modifier
                .background(navBarBackground)
                .padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavBarItem(
                tab = ClockViewModel.ClockTab.CLOCK,
                currentTab = currentTab,
                activeIcon = Icons.Filled.AccessTime,
                inactiveIcon = Icons.Outlined.AccessTime,
                label = "Clock",
                activeColor = themeColor,
                onSelected = onTabSelected
            )
            NavBarItem(
                tab = ClockViewModel.ClockTab.WORLD_CLOCK,
                currentTab = currentTab,
                activeIcon = Icons.Filled.Language,
                inactiveIcon = Icons.Outlined.Language,
                label = "World",
                activeColor = themeColor,
                onSelected = onTabSelected
            )
            NavBarItem(
                tab = ClockViewModel.ClockTab.ALARM,
                currentTab = currentTab,
                activeIcon = Icons.Filled.Alarm,
                inactiveIcon = Icons.Outlined.Alarm,
                label = "Alarm",
                activeColor = themeColor,
                onSelected = onTabSelected
            )
            NavBarItem(
                tab = ClockViewModel.ClockTab.STOPWATCH,
                currentTab = currentTab,
                activeIcon = Icons.Filled.Timer,
                inactiveIcon = Icons.Outlined.Timer,
                label = "Stopwatch",
                activeColor = themeColor,
                onSelected = onTabSelected
            )
            NavBarItem(
                tab = ClockViewModel.ClockTab.TIMER,
                currentTab = currentTab,
                activeIcon = Icons.Filled.Timer,
                inactiveIcon = Icons.Outlined.HourglassEmpty,
                label = "Timer",
                activeColor = themeColor,
                onSelected = onTabSelected
            )
            NavBarItem(
                tab = ClockViewModel.ClockTab.SETTINGS,
                currentTab = currentTab,
                activeIcon = Icons.Filled.Settings,
                inactiveIcon = Icons.Outlined.Settings,
                label = "Settings",
                activeColor = themeColor,
                onSelected = onTabSelected
            )
        }
    }
}

@Composable
fun NavBarItem(
    tab: ClockViewModel.ClockTab,
    currentTab: ClockViewModel.ClockTab,
    activeIcon: ImageVector,
    inactiveIcon: ImageVector,
    label: String,
    activeColor: Color,
    onSelected: (ClockViewModel.ClockTab) -> Unit
) {
    val isSelected = tab == currentTab

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onSelected(tab) }
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag("nav_item_${tab.name.lowercase()}"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (isSelected) activeIcon else inactiveIcon,
            contentDescription = label,
            tint = if (isSelected) activeColor else Color.White.copy(alpha = 0.5f),
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f),
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
fun RingingAlarmOverlay(
    alarm: com.example.data.model.Alarm,
    themeColor: Color,
    onDismiss: () -> Unit,
    onSnooze: () -> Unit
) {
    // Elegant pulsing animation representation
    val pulseTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xEC090A0E) // Very high opacity dark background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Pulsing Alarm Bell Ring
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .scale(pulseScale)
                    .background(themeColor.copy(alpha = 0.1f), CircleShape)
                    .border(1.dp, themeColor.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = themeColor,
                    modifier = Modifier.size(72.dp)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Time text
            val dispMin = String.format(Locale.getDefault(), "%02d", alarm.minute)
            val isPm = alarm.hour >= 12
            val h = alarm.hour % 12
            val dispHour = if (h == 0) 12 else h
            val amPm = if (isPm) "PM" else "AM"

            Text(
                text = "$dispHour:$dispMin $amPm",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 54.sp
                ),
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = alarm.label,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White.copy(alpha = 0.8f),
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(80.dp))

            // Dismiss and Snooze buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Snooze 5m
                OutlinedButton(
                    onClick = onSnooze,
                    modifier = Modifier
                        .height(56.dp)
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                        .testTag("alarm_overlay_snooze_btn"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Snooze (5m)", fontWeight = FontWeight.SemiBold)
                }

                // Dismiss
                ElevatedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .height(56.dp)
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                        .testTag("alarm_overlay_dismiss_btn"),
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = themeColor,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Dismiss", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun TimerFinishedDialog(
    themeColor: Color,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFF1C1B1F),
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = null,
                    tint = themeColor,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Timer Completed!",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Your countdown timer has finished successfully.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                ElevatedButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = themeColor,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("timer_dismiss_btn")
                ) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
