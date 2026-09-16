package com.clock.digitalalarm

import android.app.KeyguardManager
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clock.digitalalarm.service.AlarmScheduler
import com.clock.digitalalarm.ui.theme.DigitalClockTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AlarmRingingActivity : ComponentActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Despertar la pantalla y mostrarse por encima de la pantalla de bloqueo
        turnScreenOnAndShowOverLockscreen()

        val alarmId = intent.getStringExtra("ALARM_ID") ?: ""
        val alarmLabel = intent.getStringExtra("ALARM_LABEL") ?: "¡Alarma!"

        startAlarmSoundAndVibration()

        setContent {
            DigitalClockTheme {
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val pulseScale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.08f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(600),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "alarmScale"
                )

                val currentTimeFormatted = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF0D0D0D)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Text(
                            text = "⏰",
                            fontSize = 64.sp,
                            modifier = Modifier.scale(pulseScale)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = currentTimeFormatted,
                            color = Color(0xFFFF3333),
                            fontSize = 90.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )

                        Text(
                            text = alarmLabel,
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(40.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(32.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Botón Posponer 10 min
                            OutlinedButton(
                                onClick = {
                                    AlarmScheduler(this@AlarmRingingActivity).scheduleSnooze(alarmId, alarmLabel, 10)
                                    stopAlarmAndFinish()
                                },
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .height(60.dp)
                                    .width(200.dp)
                            ) {
                                Text(
                                    text = "Posponer (10 min)",
                                    color = Color.White,
                                    fontSize = 18.sp
                                )
                            }

                            // Botón Descartar / Apagar
                            Button(
                                onClick = { stopAlarmAndFinish() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3333)),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .height(60.dp)
                                    .width(200.dp)
                            ) {
                                Text(
                                    text = "Descartar",
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun turnScreenOnAndShowOverLockscreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
    }

    private fun startAlarmSoundAndVibration() {
        try {
            var alertUri: Uri? = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            if (alertUri == null) {
                alertUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            }
            if (alertUri == null) {
                alertUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            }

            mediaPlayer = MediaPlayer().apply {
                setDataSource(applicationContext, alertUri!!)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Vibración continua
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibrator = vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

            val pattern = longArrayOf(0, 1000, 500, 1000, 500)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, 0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopAlarmAndFinish() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            vibrator?.cancel()
            vibrator = null
        } catch (e: Exception) {
            e.printStackTrace()
        }

        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAlarmAndFinish()
    }
}
