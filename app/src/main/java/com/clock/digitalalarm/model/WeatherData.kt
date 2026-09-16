package com.clock.digitalalarm.model

data class WeatherData(
    val temperature: Double,
    val humidity: Int,
    val weatherCode: Int,
    val isDay: Boolean = true,
    val locationName: String = "Ubicación actual"
) {
    val conditionDescription: String
        get() = when (weatherCode) {
            0 -> if (isDay) "Despejado" else "Noche despejada"
            1 -> "Mayormente despejado"
            2 -> "Parcialmente nublado"
            3 -> "Nublado"
            45, 48 -> "Niebla"
            51, 53, 55 -> "Llovizna leve"
            61 -> "Lluvia ligera"
            63 -> "Lluvia moderada"
            65 -> "Lluvia fuerte"
            71, 73, 75 -> "Nieve"
            77 -> "Granizo fino"
            80, 81, 82 -> "Chubascos"
            85, 86 -> "Chubascos de nieve"
            95 -> "Tormenta eléctrica"
            96, 99 -> "Tormenta con granizo"
            else -> "Tiempo variable"
        }

    val iconEmoji: String
        get() = when (weatherCode) {
            0 -> if (isDay) "☀️" else "🌙"
            1, 2 -> if (isDay) "⛅" else "☁️"
            3 -> "☁️"
            45, 48 -> "🌫️"
            51, 53, 55, 61, 63, 65, 80, 81, 82 -> "🌧️"
            71, 73, 75, 77, 85, 86 -> "❄️"
            95, 96, 99 -> "⛈️"
            else -> "🌡️"
        }
}
