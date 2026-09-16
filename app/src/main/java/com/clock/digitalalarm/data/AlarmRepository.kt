package com.clock.digitalalarm.data

import android.content.Context
import android.content.SharedPreferences
import com.clock.digitalalarm.model.AlarmItem
import com.clock.digitalalarm.model.ClockColorTheme
import com.clock.digitalalarm.model.ClockPreferences
import com.clock.digitalalarm.model.ClockSize
import com.clock.digitalalarm.model.SecondsStyle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class AlarmRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("digital_clock_prefs", Context.MODE_PRIVATE)

    private val _alarms = MutableStateFlow<List<AlarmItem>>(emptyList())
    val alarms: StateFlow<List<AlarmItem>> = _alarms.asStateFlow()

    private val _preferences = MutableStateFlow(loadPreferences())
    val preferences: StateFlow<ClockPreferences> = _preferences.asStateFlow()

    init {
        loadAlarms()
    }

    private fun loadAlarms() {
        val jsonString = prefs.getString(KEY_ALARMS, null)
        val list = mutableListOf<AlarmItem>()
        if (jsonString != null) {
            try {
                val jsonArray = JSONArray(jsonString)
                for (i in 0 until jsonArray.length()) {
                    list.add(AlarmItem.fromJson(jsonArray.getJSONObject(i)))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            // Alarmas de muestra iniciales
            list.add(
                AlarmItem(
                    hour = 7,
                    minute = 0,
                    isEnabled = true,
                    daysOfWeek = setOf(1, 2, 3, 4, 5),
                    label = "Despertador"
                )
            )
            saveAlarmsInternal(list)
        }
        _alarms.value = list
    }

    private fun saveAlarmsInternal(list: List<AlarmItem>) {
        val jsonArray = JSONArray()
        list.forEach { jsonArray.put(it.toJson()) }
        prefs.edit().putString(KEY_ALARMS, jsonArray.toString()).apply()
    }

    fun saveAlarm(alarm: AlarmItem) {
        val currentList = _alarms.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == alarm.id }
        if (index >= 0) {
            currentList[index] = alarm
        } else {
            currentList.add(alarm)
        }
        currentList.sortWith(compareBy({ it.hour }, { it.minute }))
        _alarms.value = currentList
        saveAlarmsInternal(currentList)
    }

    fun deleteAlarm(alarmId: String) {
        val updated = _alarms.value.filter { it.id != alarmId }
        _alarms.value = updated
        saveAlarmsInternal(updated)
    }

    fun toggleAlarm(alarmId: String, isEnabled: Boolean) {
        val currentList = _alarms.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == alarmId }
        if (index >= 0) {
            currentList[index] = currentList[index].copy(isEnabled = isEnabled)
            _alarms.value = currentList
            saveAlarmsInternal(currentList)
        }
    }

    fun getAlarm(alarmId: String): AlarmItem? {
        return _alarms.value.find { it.id == alarmId }
    }

    private fun loadPreferences(): ClockPreferences {
        val themeName = prefs.getString(KEY_THEME, ClockColorTheme.GREEN_LED.name)
        val theme = try {
            ClockColorTheme.valueOf(themeName ?: ClockColorTheme.GREEN_LED.name)
        } catch (e: Exception) {
            ClockColorTheme.GREEN_LED
        }

        val secondsStyleName = prefs.getString(KEY_SECONDS_STYLE, SecondsStyle.BESIDE.name)
        val secondsStyle = try {
            SecondsStyle.valueOf(secondsStyleName ?: SecondsStyle.BESIDE.name)
        } catch (e: Exception) {
            SecondsStyle.BESIDE
        }

        val clockSizeName = prefs.getString(KEY_CLOCK_SIZE, ClockSize.LARGE.name)
        val clockSize = try {
            ClockSize.valueOf(clockSizeName ?: ClockSize.LARGE.name)
        } catch (e: Exception) {
            ClockSize.LARGE
        }

        return ClockPreferences(
            colorTheme = theme,
            is24HourFormat = prefs.getBoolean(KEY_24H, true),
            secondsStyle = secondsStyle,
            clockSize = clockSize,
            showDate = prefs.getBoolean(KEY_SHOW_DATE, true),
            showWeatherWidget = prefs.getBoolean(KEY_SHOW_WEATHER, true),
            showAlarmWidget = prefs.getBoolean(KEY_SHOW_ALARM, true),
            blinkColon = prefs.getBoolean(KEY_BLINK_COLON, true),
            dayBrightness = prefs.getFloat(KEY_DAY_BRIGHTNESS, 1.0f),
            nightDimLevel = prefs.getFloat(KEY_NIGHT_DIM, 0.15f),
            isNightModeActive = prefs.getBoolean(KEY_IS_NIGHT_MODE, false),
            oledBurnInProtection = prefs.getBoolean(KEY_OLED, true),
            autoDimNight = prefs.getBoolean(KEY_AUTO_DIM, false),
            latitude = prefs.getString(KEY_LAT, "40.4168")?.toDoubleOrNull() ?: 40.4168,
            longitude = prefs.getString(KEY_LON, "-3.7038")?.toDoubleOrNull() ?: -3.7038,
            cityName = prefs.getString(KEY_CITY, "Madrid") ?: "Madrid",
            updateCheckUrl = prefs.getString(KEY_UPDATE_URL, "") ?: "",
            autoCheckUpdates = prefs.getBoolean(KEY_AUTO_UPDATE, true)
        )
    }

    fun updatePreferences(newPrefs: ClockPreferences) {
        _preferences.value = newPrefs
        prefs.edit()
            .putString(KEY_THEME, newPrefs.colorTheme.name)
            .putBoolean(KEY_24H, newPrefs.is24HourFormat)
            .putString(KEY_SECONDS_STYLE, newPrefs.secondsStyle.name)
            .putString(KEY_CLOCK_SIZE, newPrefs.clockSize.name)
            .putBoolean(KEY_SHOW_DATE, newPrefs.showDate)
            .putBoolean(KEY_SHOW_WEATHER, newPrefs.showWeatherWidget)
            .putBoolean(KEY_SHOW_ALARM, newPrefs.showAlarmWidget)
            .putBoolean(KEY_BLINK_COLON, newPrefs.blinkColon)
            .putFloat(KEY_DAY_BRIGHTNESS, newPrefs.dayBrightness)
            .putFloat(KEY_NIGHT_DIM, newPrefs.nightDimLevel)
            .putBoolean(KEY_IS_NIGHT_MODE, newPrefs.isNightModeActive)
            .putBoolean(KEY_OLED, newPrefs.oledBurnInProtection)
            .putBoolean(KEY_AUTO_DIM, newPrefs.autoDimNight)
            .putString(KEY_LAT, newPrefs.latitude.toString())
            .putString(KEY_LON, newPrefs.longitude.toString())
            .putString(KEY_CITY, newPrefs.cityName)
            .putString(KEY_UPDATE_URL, newPrefs.updateCheckUrl)
            .putBoolean(KEY_AUTO_UPDATE, newPrefs.autoCheckUpdates)
            .apply()
    }

    companion object {
        private const val KEY_ALARMS = "saved_alarms_json"
        private const val KEY_THEME = "pref_color_theme"
        private const val KEY_24H = "pref_24h_format"
        private const val KEY_SECONDS_STYLE = "pref_seconds_style"
        private const val KEY_CLOCK_SIZE = "pref_clock_size"
        private const val KEY_SHOW_DATE = "pref_show_date"
        private const val KEY_SHOW_WEATHER = "pref_show_weather"
        private const val KEY_SHOW_ALARM = "pref_show_alarm"
        private const val KEY_BLINK_COLON = "pref_blink_colon"
        private const val KEY_DAY_BRIGHTNESS = "pref_day_brightness"
        private const val KEY_NIGHT_DIM = "pref_night_dim"
        private const val KEY_IS_NIGHT_MODE = "pref_is_night_mode"
        private const val KEY_OLED = "pref_oled_burn_in"
        private const val KEY_AUTO_DIM = "pref_auto_dim_night"
        private const val KEY_LAT = "pref_latitude"
        private const val KEY_LON = "pref_longitude"
        private const val KEY_CITY = "pref_city_name"
        private const val KEY_UPDATE_URL = "pref_update_check_url"
        private const val KEY_AUTO_UPDATE = "pref_auto_check_updates"
    }
}
