package com.clock.digitalalarm.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clock.digitalalarm.model.AlarmItem
import com.clock.digitalalarm.model.ClockPreferences
import com.clock.digitalalarm.model.SecondsStyle
import com.clock.digitalalarm.model.WeatherData
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun DigitalClockDisplay(
    preferences: ClockPreferences,
    modifier: Modifier = Modifier
) {
    var currentTime by remember { mutableStateOf(Date()) }

    // Actualizar cada segundo
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = Date()
            delay(1000L)
        }
    }

    // Algoritmo de protección OLED burn-in: microdesplazamiento cada 3 minutos
    var oledOffsetX by remember { mutableIntStateOf(0) }
    var oledOffsetY by remember { mutableIntStateOf(0) }

    LaunchedEffect(preferences.oledBurnInProtection) {
        if (preferences.oledBurnInProtection) {
            val offsets = listOf(0 to 0, 3 to 2, -3 to 1, 2 to -3, -2 to -2, 4 to 0, -4 to 3)
            var index = 0
            while (true) {
                delay(180_000L)
                index = (index + 1) % offsets.size
                oledOffsetX = offsets[index].first
                oledOffsetY = offsets[index].second
            }
        } else {
            oledOffsetX = 0
            oledOffsetY = 0
        }
    }

    val primaryColor = preferences.colorTheme.primaryColor

    val cal = Calendar.getInstance().apply { time = currentTime }
    val hour = if (preferences.is24HourFormat) {
        String.format("%02d", cal.get(Calendar.HOUR_OF_DAY))
    } else {
        val h = cal.get(Calendar.HOUR)
        String.format("%02d", if (h == 0) 12 else h)
    }
    val minute = String.format("%02d", cal.get(Calendar.MINUTE))
    val seconds = String.format("%02d", cal.get(Calendar.SECOND))
    val amPm = if (cal.get(Calendar.AM_PM) == Calendar.AM) "AM" else "PM"

    val dateSdf = remember { SimpleDateFormat("EEEE, d 'de' MMMM", Locale.forLanguageTag("es-ES")) }
    val formattedDate = dateSdf.format(currentTime).replaceFirstChar { it.uppercase() }

    // Parpadeo de dos puntos
    val infiniteTransition = rememberInfiniteTransition(label = "blink")
    val colonAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "colonBlink"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .offset(x = oledOffsetX.dp, y = oledOffsetY.dp),
        contentAlignment = Alignment.Center
    ) {
        val screenWidth = maxWidth
        val multiplier = preferences.clockSize.multiplier

        // Cálculo dinámico para escalar perfectamente en horizontal y mantener el reloj centrado
        val baseSize = when {
            screenWidth > 900.dp -> 160f * multiplier
            screenWidth > 750.dp -> 145f * multiplier
            screenWidth > 600.dp -> 130f * multiplier
            else -> 110f * multiplier
        }

        val mainDigitSize = baseSize.sp
        val colonDigitSize = (baseSize * 0.92f).sp
        val secondsSize = (baseSize * 0.34f).sp
        val amPmSize = (baseSize * 0.22f).sp
        val dateSize = (baseSize * 0.15f).coerceIn(15f, 26f).sp

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            when (preferences.secondsStyle) {
                SecondsStyle.BESIDE -> {
                    // Diseño perfectamente centrado con espaciadores simétricos a ambos lados
                    val sideWidth = (baseSize * 0.48f).dp
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Bloque izquierdo simétrico (contiene AM/PM o espacio vacío para centrar)
                        Box(
                            modifier = Modifier.width(sideWidth),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            if (!preferences.is24HourFormat) {
                                Text(
                                    text = amPm,
                                    color = primaryColor.copy(alpha = 0.85f),
                                    fontSize = amPmSize,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(end = 6.dp)
                                )
                            }
                        }

                        // Núcleo Horas : Minutos (100% centrado en el medio de la pantalla)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = hour,
                                color = primaryColor,
                                fontSize = mainDigitSize,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = (-2).sp
                            )
                            Text(
                                text = ":",
                                color = primaryColor,
                                modifier = Modifier
                                    .alpha(if (preferences.blinkColon) colonAlpha else 1f)
                                    .padding(horizontal = 4.dp),
                                fontSize = colonDigitSize,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = minute,
                                color = primaryColor,
                                fontSize = mainDigitSize,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = (-2).sp
                            )
                        }

                        // Bloque derecho simétrico con los segundos
                        Box(
                            modifier = Modifier.width(sideWidth),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = seconds,
                                color = primaryColor.copy(alpha = 0.75f),
                                fontSize = secondsSize,
                                fontWeight = FontWeight.Medium,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(start = 6.dp)
                            )
                        }
                    }
                }

                SecondsStyle.BELOW -> {
                    // HH:MM centrado arriba, :SS centrado abajo
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (!preferences.is24HourFormat) {
                            Text(
                                text = amPm,
                                color = primaryColor.copy(alpha = 0.85f),
                                fontSize = amPmSize,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(end = 10.dp)
                            )
                        }
                        Text(
                            text = hour,
                            color = primaryColor,
                            fontSize = mainDigitSize,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = (-2).sp
                        )
                        Text(
                            text = ":",
                            color = primaryColor,
                            modifier = Modifier
                                .alpha(if (preferences.blinkColon) colonAlpha else 1f)
                                .padding(horizontal = 4.dp),
                            fontSize = colonDigitSize,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = minute,
                            color = primaryColor,
                            fontSize = mainDigitSize,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = (-2).sp
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = ": $seconds",
                        color = primaryColor.copy(alpha = 0.75f),
                        fontSize = (baseSize * 0.28f).sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace
                    )
                }

                SecondsStyle.SAME_LINE -> {
                    // HH:MM:SS en una sola línea apaisada
                    val sameLineSize = (baseSize * 0.76f).sp
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (!preferences.is24HourFormat) {
                            Text(
                                text = amPm,
                                color = primaryColor.copy(alpha = 0.85f),
                                fontSize = (sameLineSize.value * 0.28f).sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                        Text(
                            text = hour,
                            color = primaryColor,
                            fontSize = sameLineSize,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = (-2).sp
                        )
                        Text(
                            text = ":",
                            color = primaryColor,
                            modifier = Modifier
                                .alpha(if (preferences.blinkColon) colonAlpha else 1f)
                                .padding(horizontal = 2.dp),
                            fontSize = (sameLineSize.value * 0.9f).sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = minute,
                            color = primaryColor,
                            fontSize = sameLineSize,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = (-2).sp
                        )
                        Text(
                            text = ":",
                            color = primaryColor,
                            modifier = Modifier
                                .alpha(if (preferences.blinkColon) colonAlpha else 1f)
                                .padding(horizontal = 2.dp),
                            fontSize = (sameLineSize.value * 0.9f).sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = seconds,
                            color = primaryColor.copy(alpha = 0.85f),
                            fontSize = sameLineSize,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = (-2).sp
                        )
                    }
                }

                SecondsStyle.HIDDEN -> {
                    // Solo horas y minutos centrado
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (!preferences.is24HourFormat) {
                            Text(
                                text = amPm,
                                color = primaryColor.copy(alpha = 0.85f),
                                fontSize = amPmSize,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                        }
                        Text(
                            text = hour,
                            color = primaryColor,
                            fontSize = mainDigitSize,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = (-2).sp
                        )
                        Text(
                            text = ":",
                            color = primaryColor,
                            modifier = Modifier
                                .alpha(if (preferences.blinkColon) colonAlpha else 1f)
                                .padding(horizontal = 6.dp),
                            fontSize = colonDigitSize,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = minute,
                            color = primaryColor,
                            fontSize = mainDigitSize,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = (-2).sp
                        )
                    }
                }
            }

            // Fecha en español
            if (preferences.showDate) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = formattedDate,
                    color = Color(0xFFD4D4D4),
                    fontSize = dateSize,
                    fontWeight = FontWeight.Normal,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
fun WeatherHeaderChip(
    weatherData: WeatherData?,
    isLoading: Boolean,
    primaryColor: Color,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF1C1C1C).copy(alpha = 0.9f))
            .clickable { onRefresh() }
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (weatherData != null) {
            Text(text = weatherData.iconEmoji, fontSize = 18.sp)
            Text(
                text = "${weatherData.temperature.toInt()}°C",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "· ${weatherData.conditionDescription}",
                color = primaryColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        } else {
            Text(text = "🌤️", fontSize = 16.sp)
            Text(
                text = if (isLoading) "Cargando..." else "Tocar para clima",
                color = Color.LightGray,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
fun AlarmHeaderChip(
    nextAlarm: AlarmItem?,
    primaryColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF1C1C1C).copy(alpha = 0.9f))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(text = "⏰", fontSize = 16.sp)
        if (nextAlarm != null) {
            Text(
                text = nextAlarm.formattedTime,
                color = primaryColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "· ${nextAlarm.label}",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        } else {
            Text(
                text = "Sin alarmas",
                color = Color.LightGray,
                fontSize = 13.sp
            )
        }
    }
}
