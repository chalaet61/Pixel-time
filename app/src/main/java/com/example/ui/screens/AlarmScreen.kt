package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AlarmOff
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Alarm
import com.example.ui.ClockViewModel
import com.example.ui.components.GlassmorphicCard
import java.util.Locale

@Composable
fun AlarmScreen(
    viewModel: ClockViewModel,
    modifier: Modifier = Modifier
) {
    val alarms by viewModel.alarmsState.collectAsState()
    val settings by viewModel.settingsState.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        if (alarms.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AlarmOff,
                    contentDescription = null,
                    modifier = Modifier.size(72.dp),
                    tint = Color.White.copy(alpha = 0.25f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No Alarms set",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tap the '+' button to schedule a premium alarm. It runs reliably offline with repeat configurations.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                items(alarms, key = { it.id }) { alarm ->
                    AlarmItem(
                        alarm = alarm,
                        settings = settings,
                        onToggle = { viewModel.toggleAlarm(alarm) },
                        onDelete = { viewModel.deleteAlarm(alarm) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(88.dp)) // padding for FAB
                }
            }
        }

        // FAB to add alarm
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("add_alarm_fab"),
            containerColor = Color(android.graphics.Color.parseColor(settings.themeColorHex)),
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Alarm",
                modifier = Modifier.size(28.dp)
            )
        }

        // Add Alarm dialog
        if (showAddDialog) {
            AddAlarmDialog(
                viewModel = viewModel,
                themeColor = Color(android.graphics.Color.parseColor(settings.themeColorHex)),
                onDismiss = { showAddDialog = false }
            )
        }
    }
}

@Composable
fun AlarmItem(
    alarm: Alarm,
    settings: com.example.data.model.UserSettings,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val displayHour: String
    val displayMin = String.format(Locale.getDefault(), "%02d", alarm.minute)
    val amPm: String

    if (settings.is24HourFormat) {
        displayHour = String.format(Locale.getDefault(), "%02d", alarm.hour)
        amPm = ""
    } else {
        val h = alarm.hour % 12
        displayHour = (if (h == 0) 12 else h).toString()
        amPm = if (alarm.hour >= 12) "PM" else "AM"
    }

    val themeColor = Color(android.graphics.Color.parseColor(settings.themeColorHex))

    GlassmorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("alarm_item_${alarm.id}"),
        intensity = settings.glassmorphismIntensity
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                if (alarm.isEnabled) themeColor.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.08f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (alarm.isEnabled) Icons.Default.NotificationsActive else Icons.Default.AlarmOff,
                            contentDescription = null,
                            tint = if (alarm.isEnabled) themeColor else Color.White.copy(alpha = 0.4f)
                        )
                    }
                    Spacer(modifier = Modifier.size(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "$displayHour:$displayMin",
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = if (alarm.isEnabled) Color.White else Color.White.copy(alpha = 0.5f)
                            )
                            if (amPm.isNotEmpty()) {
                                Spacer(modifier = Modifier.size(4.dp))
                                Text(
                                    text = amPm,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (alarm.isEnabled) themeColor else Color.White.copy(alpha = 0.4f),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                        }
                        Text(
                            text = alarm.label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (alarm.isEnabled) Color.White.copy(alpha = 0.85f) else Color.White.copy(alpha = 0.4f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = alarm.isEnabled,
                        onCheckedChange = { onToggle() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = themeColor,
                            uncheckedThumbColor = Color.White.copy(alpha = 0.6f),
                            uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                        ),
                        modifier = Modifier.testTag("alarm_switch_${alarm.id}")
                    )

                    Spacer(modifier = Modifier.size(8.dp))

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.testTag("alarm_delete_${alarm.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete Alarm",
                            tint = Color.White.copy(alpha = 0.4f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Repeating Days representation
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Alarm,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = if (alarm.isEnabled) themeColor else Color.White.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.size(6.dp))
                Text(
                    text = if (alarm.isRepeating) "Repeats: ${alarm.repeatingDays}" else "One-time alarm",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (alarm.isEnabled) Color.White.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.3f),
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.weight(1f))

                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = if (alarm.isEnabled) themeColor else Color.White.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = alarm.soundName,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (alarm.isEnabled) Color.White.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.3f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddAlarmDialog(
    viewModel: ClockViewModel,
    themeColor: Color,
    onDismiss: () -> Unit
) {
    var hour by remember { mutableStateOf(7) }
    var minute by remember { mutableStateOf(0) }
    var label by remember { mutableStateOf("Alarm") }
    var isRepeating by remember { mutableStateOf(false) }
    var repeatingDays = remember { mutableStateOf(mutableListOf("Mon", "Tue", "Wed", "Thu", "Fri")) }
    var soundSelected by remember { mutableStateOf("Breeze") }

    val weekdays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val soundOptions = listOf("Breeze", "Digital beep", "Forest morning", "Cosmic wave")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFF1C1B1F),
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Add Alarm",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Modern visual Dial Time picker selectors
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Hour selector column
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("HOUR", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { if (hour > 0) hour-- else hour = 23 }) {
                                Text("-", color = Color.White, fontSize = 24.sp)
                            }
                            Text(
                                text = String.format(Locale.getDefault(), "%02d", hour),
                                fontSize = 42.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            IconButton(onClick = { if (hour < 23) hour++ else hour = 0 }) {
                                Text("+", color = Color.White, fontSize = 24.sp)
                            }
                        }
                    }

                    Text(":", fontSize = 42.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(bottom = 12.dp))

                    // Minute selector column
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("MINUTE", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { if (minute > 0) minute-- else minute = 59 }) {
                                Text("-", color = Color.White, fontSize = 24.sp)
                            }
                            Text(
                                text = String.format(Locale.getDefault(), "%02d", minute),
                                fontSize = 42.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            IconButton(onClick = { if (minute < 59) minute++ else minute = 0 }) {
                                Text("+", color = Color.White, fontSize = 24.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Label Input
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Label") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Label, contentDescription = null) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = themeColor,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        cursorColor = themeColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("alarm_label_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Repeat toggles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Repeat Alarm", color = Color.White, fontWeight = FontWeight.SemiBold)
                    Switch(
                        checked = isRepeating,
                        onCheckedChange = { isRepeating = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = themeColor
                        ),
                        modifier = Modifier.testTag("alarm_repeat_switch")
                    )
                }

                if (isRepeating) {
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        weekdays.forEach { day ->
                            val isSelected = repeatingDays.value.contains(day)
                            OutlinedButton(
                                onClick = {
                                    val list = repeatingDays.value.toMutableList()
                                    if (isSelected) list.remove(day) else list.add(day)
                                    repeatingDays.value = list
                                },
                                shape = CircleShape,
                                modifier = Modifier.size(42.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isSelected) themeColor else Color.Transparent,
                                    contentColor = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f)
                                ),
                                border = if (isSelected) null else BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                            ) {
                                Text(day.substring(0, 1), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sound Selector Chips
                Text("Alarm Sound", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    soundOptions.forEach { sound ->
                        val isSel = soundSelected == sound
                        Card(
                            onClick = { soundSelected = sound },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSel) themeColor.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f),
                                contentColor = if (isSel) themeColor else Color.White.copy(alpha = 0.8f)
                            ),
                            border = BorderStroke(1.dp, if (isSel) themeColor else Color.White.copy(alpha = 0.08f)),
                            modifier = Modifier.clickable { soundSelected = sound }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(sound, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Cancel / Add buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = themeColor)
                    }
                    Spacer(modifier = Modifier.size(12.dp))
                    TextButton(
                        onClick = {
                            viewModel.addAlarm(hour, minute, label, isRepeating, repeatingDays.value, soundSelected)
                            onDismiss()
                        },
                        modifier = Modifier.testTag("alarm_save_button")
                    ) {
                        Text("Save", color = themeColor, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
