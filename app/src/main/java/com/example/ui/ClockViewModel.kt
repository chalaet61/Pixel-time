package com.example.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.Alarm
import com.example.data.model.UserSettings
import com.example.data.model.WorldCity
import com.example.data.repository.ClockRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Calendar

class ClockViewModel(private val repository: ClockRepository) : ViewModel() {

    enum class ClockTab {
        CLOCK, WORLD_CLOCK, ALARM, STOPWATCH, TIMER, SETTINGS
    }

    // Navigation state
    private val _currentTab = MutableStateFlow(ClockTab.CLOCK)
    val currentTab: StateFlow<ClockTab> = _currentTab.asStateFlow()

    fun selectTab(tab: ClockTab) {
        _currentTab.value = tab
    }

    // Settings State
    val settingsState: StateFlow<UserSettings> = repository.settings
        .combine(MutableStateFlow(UserSettings())) { dbSettings, default ->
            dbSettings ?: default
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserSettings()
        )

    fun updateSettings(settings: UserSettings) {
        viewModelScope.launch {
            repository.updateSettings(settings)
        }
    }

    // Alarms State
    val alarmsState: StateFlow<List<Alarm>> = repository.alarms
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // World Clock state
    val worldCitiesState: StateFlow<List<WorldCity>> = repository.worldCities
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Current Time State (live clock ticker)
    private val _currentTime = MutableStateFlow(System.currentTimeMillis())
    val currentTime: StateFlow<Long> = _currentTime.asStateFlow()

    // Active/Triggering Alarm
    private val _ringingAlarm = MutableStateFlow<Alarm?>(null)
    val ringingAlarm: StateFlow<Alarm?> = _ringingAlarm.asStateFlow()

    // Alarm Check state (keeps track of what minute was last checked to prevent double triggering)
    private var lastCheckedMinute = -1

    init {
        viewModelScope.launch {
            repository.seedDatabaseIfEmpty()
        }

        // Start Clock ticker and Alarm detector coroutine
        viewModelScope.launch {
            while (true) {
                val now = System.currentTimeMillis()
                _currentTime.value = now
                checkAlarms(now)
                delay(1000)
            }
        }
    }

    private fun checkAlarms(timeMs: Long) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timeMs
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)
        val currentSecond = calendar.get(Calendar.SECOND)

        // Only check at the beginning of each minute (second == 0) or if the minute changed
        if (currentMinute != lastCheckedMinute && currentSecond == 0) {
            lastCheckedMinute = currentMinute
            
            val currentDayOfWeek = when (calendar.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> "Mon"
                Calendar.TUESDAY -> "Tue"
                Calendar.WEDNESDAY -> "Wed"
                Calendar.THURSDAY -> "Thu"
                Calendar.FRIDAY -> "Fri"
                Calendar.SATURDAY -> "Sat"
                Calendar.SUNDAY -> "Sun"
                else -> ""
            }

            viewModelScope.launch {
                val alarms = alarmsState.value
                val triggered = alarms.firstOrNull { alarm ->
                    if (!alarm.isEnabled) return@firstOrNull false
                    if (alarm.hour != currentHour || alarm.minute != currentMinute) return@firstOrNull false
                    
                    if (alarm.isRepeating) {
                        val days = alarm.repeatingDays.split(",")
                        days.contains(currentDayOfWeek)
                    } else {
                        true
                    }
                }

                if (triggered != null) {
                    _ringingAlarm.value = triggered
                }
            }
        }
    }

    fun dismissRingingAlarm() {
        val alarm = _ringingAlarm.value
        _ringingAlarm.value = null
        if (alarm != null && !alarm.isRepeating) {
            // Disable one-time alarm after it rings
            viewModelScope.launch {
                repository.updateAlarm(alarm.copy(isEnabled = false))
            }
        }
    }

    fun snoozeRingingAlarm() {
        val alarm = _ringingAlarm.value
        _ringingAlarm.value = null
        if (alarm != null) {
            // Postpone alarm by 5 minutes
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.MINUTE, 5)
            val snoozeHour = calendar.get(Calendar.HOUR_OF_DAY)
            val snoozeMinute = calendar.get(Calendar.MINUTE)

            viewModelScope.launch {
                repository.addAlarm(
                    Alarm(
                        hour = snoozeHour,
                        minute = snoozeMinute,
                        label = "Snoozed: ${alarm.label}",
                        isEnabled = true,
                        isRepeating = false,
                        soundName = alarm.soundName
                    )
                )
            }
        }
    }

    // Alarm Operations
    fun addAlarm(hour: Int, minute: Int, label: String, isRepeating: Boolean, days: List<String>, sound: String) {
        viewModelScope.launch {
            val repeatingDaysStr = days.joinToString(",")
            repository.addAlarm(
                Alarm(
                    hour = hour,
                    minute = minute,
                    label = label,
                    isEnabled = true,
                    isRepeating = isRepeating,
                    repeatingDays = repeatingDaysStr,
                    soundName = sound
                )
            )
        }
    }

    fun toggleAlarm(alarm: Alarm) {
        viewModelScope.launch {
            repository.updateAlarm(alarm.copy(isEnabled = !alarm.isEnabled))
        }
    }

    fun deleteAlarm(alarm: Alarm) {
        viewModelScope.launch {
            repository.deleteAlarm(alarm)
        }
    }

    // World Cities Search & Database of all searchables
    val availableSearchCities = listOf(
        WorldCity("london", "London", "Europe/London", "GB"),
        WorldCity("new_york", "New York", "America/New_York", "US"),
        WorldCity("los_angeles", "Los Angeles", "America/Los_Angeles", "US"),
        WorldCity("chicago", "Chicago", "America/Chicago", "US"),
        WorldCity("tokyo", "Tokyo", "Asia/Tokyo", "JP"),
        WorldCity("paris", "Paris", "Europe/Paris", "FR"),
        WorldCity("berlin", "Berlin", "Europe/Berlin", "DE"),
        WorldCity("moscow", "Moscow", "Europe/Moscow", "RU"),
        WorldCity("sydney", "Sydney", "Australia/Sydney", "AU"),
        WorldCity("dubai", "Dubai", "Asia/Dubai", "AE"),
        WorldCity("singapore", "Singapore", "Asia/Singapore", "SG"),
        WorldCity("shanghai", "Shanghai", "Asia/Shanghai", "CN"),
        WorldCity("mumbai", "Mumbai", "Asia/Kolkata", "IN"),
        WorldCity("cairo", "Cairo", "Africa/Cairo", "EG"),
        WorldCity("johannesburg", "Johannesburg", "Africa/Johannesburg", "ZA"),
        WorldCity("rio_de_janeiro", "Rio de Janeiro", "America/Sao_Paulo", "BR"),
        WorldCity("buenos_aires", "Buenos Aires", "America/Argentina/Buenos_Aires", "AR"),
        WorldCity("vancouver", "Vancouver", "America/Vancouver", "CA"),
        WorldCity("toronto", "Toronto", "America/Toronto", "CA"),
        WorldCity("seoul", "Seoul", "Asia/Seoul", "KR"),
        WorldCity("auckland", "Auckland", "Pacific/Auckland", "NZ")
    )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addWorldCity(city: WorldCity) {
        viewModelScope.launch {
            val cities = worldCitiesState.value
            if (cities.none { it.cityId == city.cityId }) {
                repository.addWorldCity(city.copy(orderIndex = cities.size))
            }
        }
    }

    fun removeWorldCity(cityId: String) {
        viewModelScope.launch {
            repository.deleteWorldCityById(cityId)
        }
    }

    // --- STOPWATCH STATE & TICKER ---
    data class Lap(val lapNumber: Int, val lapTimeMs: Long, val totalTimeMs: Long)

    private val _stopwatchRunning = MutableStateFlow(false)
    val stopwatchRunning: StateFlow<Boolean> = _stopwatchRunning.asStateFlow()

    private val _stopwatchTimeMs = MutableStateFlow(0L)
    val stopwatchTimeMs: StateFlow<Long> = _stopwatchTimeMs.asStateFlow()

    private val _stopwatchLaps = MutableStateFlow<List<Lap>>(emptyList())
    val stopwatchLaps: StateFlow<List<Lap>> = _stopwatchLaps.asStateFlow()

    private var stopwatchJob: Job? = null
    private var baseTime = 0L
    private var pausedAccumulatedTime = 0L

    fun startStopwatch() {
        if (_stopwatchRunning.value) return
        _stopwatchRunning.value = true
        baseTime = System.currentTimeMillis() - pausedAccumulatedTime

        stopwatchJob = viewModelScope.launch {
            while (_stopwatchRunning.value) {
                _stopwatchTimeMs.value = System.currentTimeMillis() - baseTime
                delay(30) // ~33 FPS for smooth millisecond UI updating
            }
        }
    }

    fun pauseStopwatch() {
        if (!_stopwatchRunning.value) return
        _stopwatchRunning.value = false
        stopwatchJob?.cancel()
        pausedAccumulatedTime = _stopwatchTimeMs.value
    }

    fun lapStopwatch() {
        val total = _stopwatchTimeMs.value
        val laps = _stopwatchLaps.value
        val lastTotal = laps.firstOrNull()?.totalTimeMs ?: 0L
        val lapDiff = total - lastTotal
        val newLap = Lap(laps.size + 1, lapDiff, total)
        _stopwatchLaps.value = listOf(newLap) + laps
    }

    fun resetStopwatch() {
        pauseStopwatch()
        _stopwatchTimeMs.value = 0L
        pausedAccumulatedTime = 0L
        _stopwatchLaps.value = emptyList()
    }

    // --- TIMER STATE & TICKER ---
    private val _timerRunning = MutableStateFlow(false)
    val timerRunning: StateFlow<Boolean> = _timerRunning.asStateFlow()

    private val _timerIsPaused = MutableStateFlow(false)
    val timerIsPaused: StateFlow<Boolean> = _timerIsPaused.asStateFlow()

    private val _timerRemainingMs = MutableStateFlow(0L)
    val timerRemainingMs: StateFlow<Long> = _timerRemainingMs.asStateFlow()

    private val _timerTotalMs = MutableStateFlow(0L)
    val timerTotalMs: StateFlow<Long> = _timerTotalMs.asStateFlow()

    private var timerJob: Job? = null

    // Pickers input state
    var timerInputHours by mutableStateOf(0)
    var timerInputMinutes by mutableStateOf(0)
    var timerInputSeconds by mutableStateOf(0)

    private val _timerFinishedAlert = MutableStateFlow(false)
    val timerFinishedAlert: StateFlow<Boolean> = _timerFinishedAlert.asStateFlow()

    fun startTimer() {
        if (_timerRunning.value && !_timerIsPaused.value) return

        if (!_timerIsPaused.value) {
            val totalMs = (timerInputHours * 3600 + timerInputMinutes * 60 + timerInputSeconds) * 1000L
            if (totalMs <= 0) return
            _timerTotalMs.value = totalMs
            _timerRemainingMs.value = totalMs
        }

        _timerRunning.value = true
        _timerIsPaused.value = false

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            val endTime = System.currentTimeMillis() + _timerRemainingMs.value
            while (_timerRemainingMs.value > 0 && !_timerIsPaused.value) {
                val remaining = endTime - System.currentTimeMillis()
                _timerRemainingMs.value = remaining.coerceAtLeast(0)
                if (remaining <= 0) {
                    _timerRunning.value = false
                    _timerFinishedAlert.value = true
                    break
                }
                delay(100)
            }
        }
    }

    fun pauseTimer() {
        if (!_timerRunning.value || _timerIsPaused.value) return
        _timerIsPaused.value = true
        timerJob?.cancel()
    }

    fun resetTimer() {
        timerJob?.cancel()
        _timerRunning.value = false
        _timerIsPaused.value = false
        _timerRemainingMs.value = 0
        _timerTotalMs.value = 0
    }

    fun dismissTimerAlert() {
        _timerFinishedAlert.value = false
    }
}

class ClockViewModelFactory(private val repository: ClockRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ClockViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ClockViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
