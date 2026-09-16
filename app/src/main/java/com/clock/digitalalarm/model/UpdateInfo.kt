package com.clock.digitalalarm.model

import org.json.JSONObject

data class UpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val apkUrl: String,
    val changelog: String,
    val releaseTitle: String = ""
) {
    companion object {
        fun fromJson(json: JSONObject): UpdateInfo {
            // Caso 1: Formato GitHub Releases API (https://api.github.com/repos/{owner}/{repo}/releases/latest)
            if (json.has("tag_name")) {
                val tag = json.optString("tag_name", "")
                val cleanVersion = tag.removePrefix("v").removePrefix("V")
                val title = json.optString("name", tag)
                val body = json.optString("body", "Nueva versión publicada en GitHub.")
                val htmlUrl = json.optString("html_url", "")

                var apkDownloadUrl = htmlUrl
                val assets = json.optJSONArray("assets")
                if (assets != null) {
                    for (i in 0 until assets.length()) {
                        val asset = assets.optJSONObject(i) ?: continue
                        val assetName = asset.optString("name", "")
                        if (assetName.endsWith(".apk", ignoreCase = true)) {
                            apkDownloadUrl = asset.optString("browser_download_url", htmlUrl)
                            break
                        }
                    }
                }

                return UpdateInfo(
                    versionCode = 0,
                    versionName = cleanVersion,
                    apkUrl = apkDownloadUrl,
                    changelog = if (body.isBlank()) "Nueva versión $tag disponible en GitHub." else body,
                    releaseTitle = title
                )
            }

            // Caso 2: Formato version.json tradicional
            return UpdateInfo(
                versionCode = json.optInt("versionCode", 0),
                versionName = json.optString("versionName", "Desconocida"),
                apkUrl = json.optString("apkUrl", ""),
                changelog = json.optString("changelog", "Mejoras generales de rendimiento y correcciones."),
                releaseTitle = json.optString("title", "Actualización")
            )
        }
    }
}

