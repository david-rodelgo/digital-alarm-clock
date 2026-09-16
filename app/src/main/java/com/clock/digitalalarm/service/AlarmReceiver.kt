package com.clock.digitalalarm.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.clock.digitalalarm.AlarmRingingActivity
import com.clock.digitalalarm.R
import com.clock.digitalalarm.data.AlarmRepository

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getStringExtra("ALARM_ID") ?: return
        val alarmLabel = intent.getStringExtra("ALARM_LABEL") ?: "Alarma"
        val isSnooze = intent.getBooleanExtra("IS_SNOOZE", false)

        // Adquirir WakeLock para asegurar que la CPU no se duerma
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "digitalalarm:AlarmWakeLock"
        )
        wakeLock.acquire(60 * 1000L) // 60 segundos

        // Crear canal de notificación de alta prioridad
        createNotificationChannel(context)

        // Intent hacia AlarmRingingActivity
        val fullScreenIntent = Intent(context, AlarmRingingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("ALARM_ID", alarmId)
            putExtra("ALARM_LABEL", alarmLabel)
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            alarmId.hashCode(),
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("¡Alarma sonando!")
            .setContentText(alarmLabel)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(alarmId.hashCode(), notification)

        // Iniciar la actividad visual de alarma
        try {
            context.startActivity(fullScreenIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Si es una alarma de un solo uso, marcarla como deshabilitada en el repositorio
        if (!isSnooze) {
            val repo = AlarmRepository(context)
            val alarm = repo.getAlarm(alarmId)
            if (alarm != null) {
                if (alarm.daysOfWeek.isEmpty()) {
                    repo.toggleAlarm(alarmId, false)
                } else {
                    // Reprogramar para el siguiente ciclo
                    AlarmScheduler(context).scheduleAlarm(alarm)
                }
            }
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Alarmas de Reloj Despertador"
            val descriptionText = "Notificaciones críticas de alarma sonando"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setBypassDnd(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "alarm_clock_ringing_channel"
    }
}
