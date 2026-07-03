package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class Alarm(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val hour: Int,
    val minute: Int,
    val label: String = "Alarm",
    val isEnabled: Boolean = true,
    val isRepeating: Boolean = false,
    val repeatingDays: String = "", // "Mon,Tue,Wed,Thu,Fri,Sat,Sun"
    val soundName: String = "Breeze"
)

@Entity(tableName = "world_cities")
data class WorldCity(
    @PrimaryKey val cityId: String,
    val cityName: String,
    val timezoneId: String,
    val countryCode: String,
    val orderIndex: Int = 0
)

@Entity(tableName = "user_settings")
data class UserSettings(
    @PrimaryKey val id: Int = 1,
    val is24HourFormat: Boolean = false,
    val showSeconds: Boolean = true,
    val isAnalogEnabled: Boolean = true,
    val glassmorphismIntensity: Float = 0.6f,
    val themeColorHex: String = "#0A84FF", // Google Pixel Blue
    val alarmSound: String = "Breeze",
    val timerSound: String = "Digital beep"
)
