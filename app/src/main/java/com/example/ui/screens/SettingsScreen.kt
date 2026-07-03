package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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

@Composable
fun SettingsScreen(
    viewModel: ClockViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settingsState.collectAsState()
    val themeColor = Color(android.graphics.Color.parseColor(settings.themeColorHex))

    val colorThemes = listOf(
        ThemeColorOption("Pixel Blue", "#0A84FF"),
        ThemeColorOption("Emerald Green", "#30D158"),
        ThemeColorOption("Sunset Orange", "#FF453A"),
        ThemeColorOption("Neon Purple", "#BF5AF2"),
        ThemeColorOption("Amber Yellow", "#FFD60A")
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Clocks Settings Group
        Text(
            text = "CLOCK DISPLAY",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.4f),
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp
        )

        GlassmorphicCard(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("clocks_settings_card"),
            intensity = settings.glassmorphismIntensity
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // 24 Hour Format
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "24-Hour Format",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Display clock in 24-hour style (e.g., 13:00)",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                    Switch(
                        checked = settings.is24HourFormat,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(is24HourFormat = it)) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = themeColor
                        ),
                        modifier = Modifier.testTag("toggle_24hr_format")
                    )
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                // Show Seconds Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Show Seconds",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Render seconds ticking on digital clocks",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                    Switch(
                        checked = settings.showSeconds,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(showSeconds = it)) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = themeColor
                        ),
                        modifier = Modifier.testTag("toggle_show_seconds")
                    )
                }
            }
        }

        // Appearance Settings Group
        Text(
            text = "APPEARANCE & THEME",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.4f),
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(top = 8.dp)
        )

        GlassmorphicCard(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("theme_settings_card"),
            intensity = settings.glassmorphismIntensity
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Primary Accent color chooser
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ColorLens,
                            contentDescription = null,
                            tint = themeColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Theme Accent Color",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        colorThemes.forEach { themeOption ->
                            val isSelected = settings.themeColorHex == themeOption.hexColor
                            val optionColor = Color(android.graphics.Color.parseColor(themeOption.hexColor))

                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(optionColor, CircleShape)
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) Color.White else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        viewModel.updateSettings(settings.copy(themeColorHex = themeOption.hexColor))
                                    }
                                    .testTag("theme_chip_${themeOption.hexColor}")
                            )
                        }
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                // Glassmorphism intensity slider
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Opacity,
                            contentDescription = null,
                            tint = themeColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Glassmorphism Card Density",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Adjust the opacity of modern layout cards",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = settings.glassmorphismIntensity,
                        onValueChange = { viewModel.updateSettings(settings.copy(glassmorphismIntensity = it)) },
                        valueRange = 0.1f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = themeColor,
                            inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("glassmorphism_slider")
                    )
                }
            }
        }

        // About / Info Group
        Text(
            text = "SYSTEM INFO",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.4f),
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(top = 8.dp)
        )

        GlassmorphicCard(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("about_card"),
            intensity = settings.glassmorphismIntensity
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = themeColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Digital Clock App v1.0",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Designed in alignment with premium Google Pixel aesthetics.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

data class ThemeColorOption(val name: String, val hexColor: String)
