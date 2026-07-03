package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.WorldCity
import com.example.ui.ClockViewModel
import com.example.ui.components.GlassmorphicCard
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun WorldClockScreen(
    viewModel: ClockViewModel,
    modifier: Modifier = Modifier
) {
    val currentTimeMs by viewModel.currentTime.collectAsState()
    val savedCities by viewModel.worldCitiesState.collectAsState()
    val settings by viewModel.settingsState.collectAsState()

    var showSearchDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        if (savedCities.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Public,
                    contentDescription = null,
                    modifier = Modifier.size(72.dp),
                    tint = Color.White.copy(alpha = 0.25f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No World Cities Added",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tap the '+' button below to add world clocks and compare timezone offsets.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.5f),
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

                items(savedCities, key = { it.cityId }) { city ->
                    WorldCityItem(
                        city = city,
                        currentTimeMs = currentTimeMs,
                        settings = settings,
                        onDelete = { viewModel.removeWorldCity(city.cityId) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(88.dp)) // margin for FAB
                }
            }
        }

        // FAB to add cities
        FloatingActionButton(
            onClick = { showSearchDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("add_city_fab"),
            containerColor = Color(android.graphics.Color.parseColor(settings.themeColorHex)),
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add World City",
                modifier = Modifier.size(28.dp)
            )
        }

        // Search and Add Dialog
        if (showSearchDialog) {
            WorldCitySearchDialog(
                viewModel = viewModel,
                themeColor = Color(android.graphics.Color.parseColor(settings.themeColorHex)),
                onDismiss = { showSearchDialog = false }
            )
        }
    }
}

@Composable
fun WorldCityItem(
    city: WorldCity,
    currentTimeMs: Long,
    settings: com.example.data.model.UserSettings,
    onDelete: () -> Unit
) {
    val localZoneId = ZoneId.systemDefault()
    val cityZoneId = try {
        ZoneId.of(city.timezoneId)
    } catch (e: Exception) {
        ZoneId.systemDefault()
    }

    val instant = Instant.ofEpochMilli(currentTimeMs)
    val localZoned = ZonedDateTime.ofInstant(instant, localZoneId)
    val cityZoned = ZonedDateTime.ofInstant(instant, cityZoneId)

    // Time Format
    val pattern = if (settings.is24HourFormat) {
        if (settings.showSeconds) "HH:mm:ss" else "HH:mm"
    } else {
        if (settings.showSeconds) "hh:mm:ss a" else "hh:mm a"
    }
    val formattedTime = cityZoned.format(DateTimeFormatter.ofPattern(pattern, Locale.getDefault()))

    // Calculate time difference
    val localOffset = localZoneId.rules.getOffset(instant).totalSeconds
    val cityOffset = cityZoneId.rules.getOffset(instant).totalSeconds
    val offsetDiffSec = cityOffset - localOffset
    val offsetDiffHours = offsetDiffSec / 3600f

    val offsetText = when {
        offsetDiffHours == 0f -> "Same time"
        offsetDiffHours > 0f -> "+${if (offsetDiffHours % 1 == 0f) offsetDiffHours.toInt().toString() else offsetDiffHours.toString()} hrs"
        else -> "${if (offsetDiffHours % 1 == 0f) offsetDiffHours.toInt().toString() else offsetDiffHours.toString()} hrs"
    }

    // Flag or initials placeholder for country
    val flagPlaceholder = getFlagEmoji(city.countryCode)

    GlassmorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("world_city_item_${city.cityId}"),
        intensity = settings.glassmorphismIntensity
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                // Circular country symbol
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.White.copy(alpha = 0.08f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = flagPlaceholder,
                        fontSize = 22.sp
                    )
                }
                Spacer(modifier = Modifier.size(12.dp))
                Column {
                    Text(
                        text = city.cityName,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = city.countryCode,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.size(6.dp))
                        Box(
                            modifier = Modifier
                                .size(3.dp)
                                .background(Color.White.copy(alpha = 0.5f), CircleShape)
                        )
                        Spacer(modifier = Modifier.size(6.dp))
                        Text(
                            text = offsetText,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(android.graphics.Color.parseColor(settings.themeColorHex)),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Clock / Time details
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = cityZoned.format(DateTimeFormatter.ofPattern("EEE, d MMM", Locale.getDefault())),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("delete_city_${city.cityId}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete World City",
                        tint = Color.White.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}

@Composable
fun WorldCitySearchDialog(
    viewModel: ClockViewModel,
    themeColor: Color,
    onDismiss: () -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val savedCities by viewModel.worldCitiesState.collectAsState()

    val filteredCities = remember(searchQuery, savedCities) {
        viewModel.availableSearchCities.filter { city ->
            city.cityName.contains(searchQuery, ignoreCase = true) &&
                    savedCities.none { it.cityId == city.cityId }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFF1C1B1F),
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Add World City",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = { Text("Search city name...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = themeColor,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        cursorColor = themeColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("city_search_input"),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Search Results
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (filteredCities.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No cities found or already added.",
                                    color = Color.White.copy(alpha = 0.5f),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    } else {
                        items(filteredCities) { city ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.addWorldCity(city)
                                        viewModel.updateSearchQuery("")
                                        onDismiss()
                                    }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = getFlagEmoji(city.countryCode),
                                    fontSize = 24.sp
                                )
                                Spacer(modifier = Modifier.size(16.dp))
                                Column {
                                    Text(
                                        text = city.cityName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = city.timezoneId,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = themeColor)
                    }
                }
            }
        }
    }
}

// Convert ISO country code to Country Flag Emoji helper
fun getFlagEmoji(countryCode: String): String {
    if (countryCode.length != 2) return "🌐"
    val firstChar = countryCode[0].uppercaseChar().code - 65 + 0x1F1E6
    val secondChar = countryCode[1].uppercaseChar().code - 65 + 0x1F1E6
    return String(Character.toChars(firstChar)) + String(Character.toChars(secondChar))
}
