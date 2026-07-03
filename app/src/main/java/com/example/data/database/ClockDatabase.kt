package com.example.data.database

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import com.example.data.model.Alarm
import com.example.data.model.UserSettings
import com.example.data.model.WorldCity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmDao {
    @Query("SELECT * FROM alarms ORDER BY hour ASC, minute ASC")
    fun getAllAlarms(): Flow<List<Alarm>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarm: Alarm): Long

    @Update
    suspend fun updateAlarm(alarm: Alarm)

    @Delete
    suspend fun deleteAlarm(alarm: Alarm)

    @Query("SELECT * FROM alarms WHERE id = :id")
    suspend fun getAlarmById(id: Long): Alarm?
}

@Dao
interface WorldCityDao {
    @Query("SELECT * FROM world_cities ORDER BY orderIndex ASC")
    fun getAllCities(): Flow<List<WorldCity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCity(city: WorldCity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCities(cities: List<WorldCity>)

    @Delete
    suspend fun deleteCity(city: WorldCity)

    @Query("DELETE FROM world_cities WHERE cityId = :cityId")
    suspend fun deleteCityById(cityId: String)
}

@Dao
interface UserSettingsDao {
    @Query("SELECT * FROM user_settings WHERE id = 1")
    fun getSettingsFlow(): Flow<UserSettings?>

    @Query("SELECT * FROM user_settings WHERE id = 1")
    suspend fun getSettingsDirect(): UserSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: UserSettings)

    @Update
    suspend fun updateSettings(settings: UserSettings)
}

@Database(entities = [Alarm::class, WorldCity::class, UserSettings::class], version = 1, exportSchema = false)
abstract class ClockDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
    abstract fun worldCityDao(): WorldCityDao
    abstract fun userSettingsDao(): UserSettingsDao
}
