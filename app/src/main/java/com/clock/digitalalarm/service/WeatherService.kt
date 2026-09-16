package com.clock.digitalalarm.service

import com.clock.digitalalarm.model.WeatherData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class WeatherService {

    suspend fun fetchWeather(latitude: Double, longitude: Double, cityName: String = "Ubicación"): WeatherData? {
        return withContext(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            try {
                val urlString = "https://api.open-meteo.com/v1/forecast?" +
                        "latitude=$latitude&longitude=$longitude" +
                        "&current=temperature_2m,relative_humidity_2m,weather_code,is_day" +
                        "&timezone=auto"
                val url = URL(urlString)
                connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 8000
                    readTimeout = 8000
                    setRequestProperty("User-Agent", "DigitalAlarmClock/1.0")
                }

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val response = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                    reader.close()

                    val json = JSONObject(response.toString())
                    val current = json.getJSONObject("current")
                    val temp = current.getDouble("temperature_2m")
                    val humidity = current.optInt("relative_humidity_2m", 50)
                    val code = current.optInt("weather_code", 0)
                    val isDay = current.optInt("is_day", 1) == 1

                    WeatherData(
                        temperature = temp,
                        humidity = humidity,
                        weatherCode = code,
                        isDay = isDay,
                        locationName = cityName
                    )
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            } finally {
                connection?.disconnect()
            }
        }
    }
}
