package com.clock.digitalalarm.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.clock.digitalalarm.model.AlarmItem
import java.util.Calendar

class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleAlarm(alarm: AlarmItem) {
        if (!alarm.isEnabled) {
            cancelAlarm(alarm)
            return
        }

        val triggerTime = calculateNextTriggerMillis(alarm)
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.clock.digitalalarm.ALARM_TRIGGER"
            putExtra("ALARM_ID", alarm.id)
            putExtra("ALARM_LABEL", alarm.label)
            putExtra("ALARM_HOUR", alarm.hour)
            putExtra("ALARM_MINUTE", alarm.minute)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
            Log.d("AlarmScheduler", "Alarma programada para: ${java.util.Date(triggerTime)}")
        } catch (e: SecurityException) {
            e.printStackTrace()
            // Fallback si no tiene permiso exacto
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }

    fun cancelAlarm(alarm: AlarmItem) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.clock.digitalalarm.ALARM_TRIGGER"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun scheduleSnooze(alarmId: String, label: String, minutes: Int = 10) {
        val triggerTime = System.currentTimeMillis() + (minutes * 60 * 1000)
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.clock.digitalalarm.ALARM_TRIGGER"
            putExtra("ALARM_ID", alarmId)
            putExtra("ALARM_LABEL", "$label (Pospuesta)")
            putExtra("IS_SNOOZE", true)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ("snooze_$alarmId").hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
    }

    companion object {
        fun calculateNextTriggerMillis(alarm: AlarmItem): Long {
            val now = Calendar.getInstance()
            val target = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, alarm.hour)
                set(Calendar.MINUTE, alarm.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            if (alarm.daysOfWeek.isEmpty()) {
                // Alarma de una sola vez
                if (target.before(now) || target.timeInMillis <= now.timeInMillis) {
                    target.add(Calendar.DAY_OF_YEAR, 1)
                }
                return target.timeInMillis
            }

            // Alarma repetitiva por días de la semana
            // Convertir de formato app (1=Lunes .. 7=Domingo) a Calendar (Calendar.MONDAY..Calendar.SUNDAY)
            val calendarDays = alarm.daysOfWeek.map { appDayToCalendarDay(it) }.toSet()

            for (i in 0..7) {
                val checkDay = (now.get(Calendar.DAY_OF_WEEK) - 1 + i) % 7 + 1
                if (calendarDays.contains(checkDay)) {
                    val candidate = Calendar.getInstance().apply {
                        add(Calendar.DAY_OF_YEAR, i)
                        set(Calendar.HOUR_OF_DAY, alarm.hour)
                        set(Calendar.MINUTE, alarm.minute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    if (candidate.after(now)) {
                        return candidate.timeInMillis
                    }
                }
            }

            // Fallback: siguiente semana mismo día
            target.add(Calendar.DAY_OF_YEAR, 7)
            return target.timeInMillis
        }

        private fun appDayToCalendarDay(day: Int): Int {
            return when (day) {
                1 -> Calendar.MONDAY
                2 -> Calendar.TUESDAY
                3 -> Calendar.WEDNESDAY
                4 -> Calendar.THURSDAY
                5 -> Calendar.FRIDAY
                6 -> Calendar.SATURDAY
                7 -> Calendar.SUNDAY
                else -> Calendar.MONDAY
            }
        }
    }
}
