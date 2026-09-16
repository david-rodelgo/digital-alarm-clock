package com.clock.digitalalarm.service

import android.content.Context
import android.os.Build
import com.clock.digitalalarm.model.UpdateInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class UpdateChecker(private val context: Context) {

    fun getCurrentVersionCode(): Int {
        return try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pInfo.longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                pInfo.versionCode
            }
        } catch (e: Exception) {
            6
        }
    }

    fun getCurrentVersionName(): String {
        return try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName ?: "3.2.0"
        } catch (e: Exception) {
            "3.2.0"
        }
    }

    fun resolveUpdateUrl(input: String): String {
        val trimmed = input.trim()
        if (trimmed.isBlank()) return ""

        // Si ya es una URL de la API de GitHub
        if (trimmed.startsWith("https://api.github.com/repos/")) {
            return trimmed
        }

        // Si es una URL web de GitHub tipo https://github.com/owner/repo
        if (trimmed.startsWith("https://github.com/") || trimmed.startsWith("http://github.com/")) {
            val path = trimmed.removePrefix("https://github.com/").removePrefix("http://github.com/").trim('/')
            val parts = path.split("/")
            if (parts.size >= 2) {
                val owner = parts[0]
                val repo = parts[1]
                return "https://api.github.com/repos/$owner/$repo/releases/latest"
            }
        }

        // Formato corto "usuario/repositorio"
        if (!trimmed.contains("://") && trimmed.contains("/")) {
            val parts = trimmed.split("/")
            if (parts.size == 2 && parts[0].isNotBlank() && parts[1].isNotBlank()) {
                return "https://api.github.com/repos/${parts[0].trim()}/${parts[1].trim()}/releases/latest"
            }
        }

        // En cualquier otro caso, asumimos que es una URL directa (ej. version.json o endpoint propio)
        return trimmed
    }

    fun isNewerVersion(remoteVersion: String, localVersion: String): Boolean {
        val cleanRemote = remoteVersion.trim().removePrefix("v").removePrefix("V")
        val cleanLocal = localVersion.trim().removePrefix("v").removePrefix("V")
        val remoteParts = cleanRemote.split(".").mapNotNull { it.takeWhile { c -> c.isDigit() }.toIntOrNull() }
        val localParts = cleanLocal.split(".").mapNotNull { it.takeWhile { c -> c.isDigit() }.toIntOrNull() }

        val maxLength = maxOf(remoteParts.size, localParts.size)
        for (i in 0 until maxLength) {
            val r = remoteParts.getOrElse(i) { 0 }
            val l = localParts.getOrElse(i) { 0 }
            if (r > l) return true
            if (r < l) return false
        }
        return false
    }

    suspend fun checkUpdate(repoOrUrl: String): UpdateResult {
        val resolvedUrl = resolveUpdateUrl(repoOrUrl)
        if (resolvedUrl.isBlank()) {
            return UpdateResult.NoUrlConfigured
        }

        return withContext(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            try {
                val url = URL(resolvedUrl)
                connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 8000
                    readTimeout = 8000
                    setRequestProperty("Accept", "application/vnd.github.v3+json, application/json")
                    setRequestProperty("User-Agent", "DigitalAlarmClock-App/3.2")
                }

                val code = connection.responseCode
                if (code == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val response = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                    reader.close()

                    val json = JSONObject(response.toString())
                    val updateInfo = UpdateInfo.fromJson(json)

                    val localVersion = getCurrentVersionName()
                    val localCode = getCurrentVersionCode()

                    val isNewer = if (updateInfo.versionCode > 0) {
                        updateInfo.versionCode > localCode
                    } else {
                        isNewerVersion(updateInfo.versionName, localVersion)
                    }

                    if (isNewer && updateInfo.apkUrl.isNotBlank()) {
                        UpdateResult.UpdateAvailable(updateInfo, localVersionName = localVersion)
                    } else {
                        UpdateResult.UpToDate(localVersionName = localVersion)
                    }
                } else if (code == HttpURLConnection.HTTP_NOT_FOUND) {
                    UpdateResult.Error("No se encontraron releases en el repositorio (Error 404). Comprueba que el usuario/repositorio sea correcto y que hayas publicado al menos un release.")
                } else {
                    UpdateResult.Error("Error al contactar con el servidor (HTTP $code)")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                UpdateResult.Error("Error de conexión: ${e.localizedMessage ?: "Comprueba tu conexión a Internet"}")
            } finally {
                connection?.disconnect()
            }
        }
    }
}

sealed class UpdateResult {
    data class UpdateAvailable(val updateInfo: UpdateInfo, val localVersionName: String) : UpdateResult()
    data class UpToDate(val localVersionName: String) : UpdateResult()
    data class Error(val message: String) : UpdateResult()
    object NoUrlConfigured : UpdateResult()
}

