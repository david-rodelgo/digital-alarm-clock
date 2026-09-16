package com.clock.digitalalarm.model

import org.json.JSONArray
import org.json.JSONObject

data class AlarmItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val hour: Int,
    val minute: Int,
    val isEnabled: Boolean = true,
    val daysOfWeek: Set<Int> = emptySet(), // 1 = Lunes, ..., 7 = Domingo (Calendar.MONDAY..SUNDAY)
    val label: String = "Alarma",
    val vibrate: Boolean = true
) {
    val formattedTime: String
        get() = String.format("%02d:%02d", hour, minute)

    val daysSummary: String
        get() {
            if (daysOfWeek.isEmpty()) return "Una vez"
            if (daysOfWeek.size == 7) return "Todos los días"
            if (daysOfWeek == setOf(1, 2, 3, 4, 5)) return "Lunes a Viernes"
            if (daysOfWeek == setOf(6, 7)) return "Fines de semana"
            val names = mapOf(
                1 to "Lun", 2 to "Mar", 3 to "Mié", 4 to "Jue",
                5 to "Vie", 6 to "Sáb", 7 to "Dom"
            )
            return daysOfWeek.sorted().joinToString(", ") { names[it] ?: "" }
        }

    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("id", id)
        json.put("hour", hour)
        json.put("minute", minute)
        json.put("isEnabled", isEnabled)
        val daysArray = JSONArray()
        daysOfWeek.forEach { daysArray.put(it) }
        json.put("daysOfWeek", daysArray)
        json.put("label", label)
        json.put("vibrate", vibrate)
        return json
    }

    companion object {
        fun fromJson(json: JSONObject): AlarmItem {
            val daysSet = mutableSetOf<Int>()
            val daysArray = json.optJSONArray("daysOfWeek")
            if (daysArray != null) {
                for (i in 0 until daysArray.length()) {
                    daysSet.add(daysArray.getInt(i))
                }
            }
            return AlarmItem(
                id = json.optString("id", java.util.UUID.randomUUID().toString()),
                hour = json.getInt("hour"),
                minute = json.getInt("minute"),
                isEnabled = json.optBoolean("isEnabled", true),
                daysOfWeek = daysSet,
                label = json.optString("label", "Alarma"),
                vibrate = json.optBoolean("vibrate", true)
            )
        }
    }
}
