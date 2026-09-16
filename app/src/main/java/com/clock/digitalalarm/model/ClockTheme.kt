package com.clock.digitalalarm.model

import androidx.compose.ui.graphics.Color

enum class ClockColorTheme(
    val title: String,
    val primaryColor: Color,
    val glowColor: Color
) {
    GREEN_LED(
        title = "Verde Neón",
        primaryColor = Color(0xFF00FF66),
        glowColor = Color(0x3300FF66)
    ),
    NIGHT_RED(
        title = "Rojo Noche",
        primaryColor = Color(0xFFFF3333),
        glowColor = Color(0x33FF3333)
    ),
    CYAN_BLUE(
        title = "Azul Cian",
        primaryColor = Color(0xFF00E5FF),
        glowColor = Color(0x3300E5FF)
    ),
    AMBER_ORANGE(
        title = "Ámbar Cálido",
        primaryColor = Color(0xFFFFB300),
        glowColor = Color(0x33FFB300)
    ),
    WHITE_CLEAN(
        title = "Blanco Puro",
        primaryColor = Color(0xFFEEEEEE),
        glowColor = Color(0x33FFFFFF)
    ),
    PURPLE_NEON(
        title = "Púrpura",
        primaryColor = Color(0xFFD500F9),
        glowColor = Color(0x33D500F9)
    ),
    LIME_YELLOW(
        title = "Lima Eléctrico",
        primaryColor = Color(0xFFCCFF00),
        glowColor = Color(0x33CCFF00)
    ),
    HOT_PINK(
        title = "Rosa Neón",
        primaryColor = Color(0xFFFF2A85),
        glowColor = Color(0x33FF2A85)
    )
}

enum class SecondsStyle(val title: String) {
    BESIDE("Al lado (centrado)"),
    BELOW("Debajo con fecha"),
    SAME_LINE("Misma línea (HH:MM:SS)"),
    HIDDEN("Sin segundero")
}

enum class ClockSize(val title: String, val multiplier: Float) {
    MEDIUM("Medio (85%)", 0.85f),
    LARGE("Grande (100%)", 1.0f),
    MAXIMUM("Pantalla Completa (120%)", 1.2f)
}

data class ClockPreferences(
    val colorTheme: ClockColorTheme = ClockColorTheme.GREEN_LED,
    val is24HourFormat: Boolean = true,
    val secondsStyle: SecondsStyle = SecondsStyle.BESIDE,
    val clockSize: ClockSize = ClockSize.LARGE,
    val showDate: Boolean = true,
    val showWeatherWidget: Boolean = true,
    val showAlarmWidget: Boolean = true,
    val blinkColon: Boolean = true,
    val dayBrightness: Float = 1.0f,
    val nightDimLevel: Float = 0.15f,
    val isNightModeActive: Boolean = false,
    val oledBurnInProtection: Boolean = true,
    val autoDimNight: Boolean = false,
    val latitude: Double = 40.4168,
    val longitude: Double = -3.7038,
    val cityName: String = "Madrid",
    val updateCheckUrl: String = "david-rodelgo/digital-alarm-clock",
    val autoCheckUpdates: Boolean = true
) {
    val showSeconds: Boolean
        get() = secondsStyle != SecondsStyle.HIDDEN

    val effectiveBrightness: Float
        get() = if (isNightModeActive) nightDimLevel else dayBrightness
}
