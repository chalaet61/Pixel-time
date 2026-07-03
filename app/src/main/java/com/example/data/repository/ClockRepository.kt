package com.example.data.repository

import com.example.data.database.AlarmDao
import com.example.data.database.UserSettingsDao
import com.example.data.database.WorldCityDao
import com.example.data.model.Alarm
import com.example.data.model.UserSettings
import com.example.data.model.WorldCity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.onStart

class ClockRepository(
    private val alarmDao: AlarmDao,
    private val worldCityDao: WorldCityDao,
    private val userSettingsDao: UserSettingsDao
) {
    // Expose Alarms
    val alarms: Flow<List<Alarm>> = alarmDao.getAllAlarms()

    // Expose World Cities with fallback/seeding
    val worldCities: Flow<List<WorldCity>> = worldCityDao.getAllCities()
        .onStart {
            // Check if we need to prepopulate cities
            // We can do this in a check, but let's let the ViewModel call a seed method on start
        }

    // Expose User Settings with a guaranteed non-null default
    val settings: Flow<UserSettings?> = userSettingsDao.getSettingsFlow()

    suspend fun getSettingsDirect(): UserSettings {
        return userSettingsDao.getSettingsDirect() ?: UserSettings().also {
            userSettingsDao.insertSettings(it)
        }
    }

    suspend fun updateSettings(settings: UserSettings) {
        userSettingsDao.insertSettings(settings)
    }

    // Alarm management
    suspend fun addAlarm(alarm: Alarm): Long {
        return alarmDao.insertAlarm(alarm)
    }

    suspend fun updateAlarm(alarm: Alarm) {
        alarmDao.updateAlarm(alarm)
    }

    suspend fun deleteAlarm(alarm: Alarm) {
        alarmDao.deleteAlarm(alarm)
    }

    // World cities management
    suspend fun addWorldCity(city: WorldCity) {
        worldCityDao.insertCity(city)
    }

    suspend fun deleteWorldCityById(cityId: String) {
        worldCityDao.deleteCityById(cityId)
    }

    suspend fun seedDatabaseIfEmpty() {
        // Seed default settings if empty
        val currentSettings = userSettingsDao.getSettingsDirect()
        if (currentSettings == null) {
            userSettingsDao.insertSettings(UserSettings())
        }

        // Seed default world cities if empty
        val currentCities = worldCityDao.getAllCities().firstOrNull()
        if (currentCities.isNullOrEmpty()) {
            val defaultCities = listOf(
                WorldCity("london", "London", "Europe/London", "GB", 0),
                WorldCity("new_york", "New York", "America/New_York", "US", 1),
                WorldCity("tokyo", "Tokyo", "Asia/Tokyo", "JP", 2),
                WorldCity("paris", "Paris", "Europe/Paris", "FR", 3),
                WorldCity("sydney", "Sydney", "Australia/Sydney", "AU", 4),
                WorldCity("cairo", "Cairo", "Africa/Cairo", "EG", 5)
            )
            worldCityDao.insertCities(defaultCities)
        }

        // Seed some default sample alarms if empty to make UI look amazing on first launch
        val currentAlarms = alarmDao.getAllAlarms().firstOrNull()
        if (currentAlarms.isNullOrEmpty()) {
            alarmDao.insertAlarm(Alarm(hour = 7, minute = 30, label = "Morning Workout", isEnabled = true, repeatingDays = "Mon,Tue,Wed,Thu,Fri"))
            alarmDao.insertAlarm(Alarm(hour = 9, minute = 0, label = "Weekly Team Sync", isEnabled = false, repeatingDays = "Mon"))
            alarmDao.insertAlarm(Alarm(hour = 22, minute = 0, label = "Bedtime Reminder", isEnabled = true, repeatingDays = "Mon,Tue,Wed,Thu,Fri,Sat,Sun"))
        }
    }
}
