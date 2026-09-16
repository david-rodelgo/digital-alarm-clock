package com.clock.digitalalarm.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.clock.digitalalarm.model.ClockColorTheme
import com.clock.digitalalarm.model.ClockPreferences
import com.clock.digitalalarm.model.ClockSize
import com.clock.digitalalarm.model.SecondsStyle

@Composable
fun SettingsDialog(
    preferences: ClockPreferences,
    onSavePreferences: (ClockPreferences) -> Unit,
    onCheckUpdates: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTheme by remember { mutableStateOf(preferences.colorTheme) }
    var selectedSize by remember { mutableStateOf(preferences.clockSize) }
    var selectedSecondsStyle by remember { mutableStateOf(preferences.secondsStyle) }
    var is24H by remember { mutableStateOf(preferences.is24HourFormat) }
    var showDate by remember { mutableStateOf(preferences.showDate) }
    var blinkColon by remember { mutableStateOf(preferences.blinkColon) }
    var showWeather by remember { mutableStateOf(preferences.showWeatherWidget) }
    var showAlarm by remember { mutableStateOf(preferences.showAlarmWidget) }
    var dayBrightness by remember { mutableFloatStateOf(preferences.dayBrightness) }
    var nightDimLevel by remember { mutableFloatStateOf(preferences.nightDimLevel) }
    var isNightMode by remember { mutableStateOf(preferences.isNightModeActive) }
    var oledProtection by remember { mutableStateOf(preferences.oledBurnInProtection) }
    var cityName by remember { mutableStateOf(preferences.cityName) }
    var latitude by remember { mutableStateOf(preferences.latitude.toString()) }
    var longitude by remember { mutableStateOf(preferences.longitude.toString()) }
    var updateUrl by remember { mutableStateOf(preferences.updateCheckUrl) }
    var autoCheckUpdates by remember { mutableStateOf(preferences.autoCheckUpdates) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.94f)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF141414))
                .border(1.dp, Color(0xFF2C2C2C), RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    // Cabecera del Diálogo
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ajustes de Personalización",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        OutlinedButton(onClick = onDismiss) {
                            Text("Cerrar", color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 1. Color de los Dígitos LED
                    SettingSectionTitle("1. Color de los Dígitos LED")
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        ClockColorTheme.values().forEach { theme ->
                            val isSelected = theme == selectedTheme
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { selectedTheme = theme }
                                    .padding(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(theme.primaryColor)
                                        .border(
                                            width = if (isSelected) 3.dp else 0.dp,
                                            color = if (isSelected) Color.White else Color.Transparent,
                                            shape = CircleShape
                                        )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = theme.title,
                                    color = if (isSelected) Color.White else Color.Gray,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // 2. Tamaño de la Hora y Estilo del Segundero
                    SettingSectionTitle("2. Tamaño de la Hora y Segundero")
                    Spacer(modifier = Modifier.height(8.dp))

                    // Selector de Tamaño del Reloj
                    Text("Tamaño del Reloj en Pantalla:", color = Color.LightGray, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ClockSize.values().forEach { size ->
                            val isSelected = size == selectedSize
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) selectedTheme.primaryColor else Color(0xFF222222))
                                    .clickable { selectedSize = size }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = size.title,
                                    color = if (isSelected) Color.Black else Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Selector de Estilo de Segundero
                    Text("Disposición del Segundero:", color = Color.LightGray, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SecondsStyle.values().forEach { style ->
                            val isSelected = style == selectedSecondsStyle
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) selectedTheme.primaryColor else Color(0xFF222222))
                                    .clickable { selectedSecondsStyle = style }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = style.title,
                                    color = if (isSelected) Color.Black else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // 3. Atenuación Nocturna y Brillo
                    SettingSectionTitle("3. Iluminación y Modo Noche")
                    Spacer(modifier = Modifier.height(8.dp))

                    // Slider de Atenuación Nocturna (5% a 50%)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Atenuación en Modo Noche 🌙", color = Color.White, fontSize = 15.sp)
                            Text("Nivel tenue cuando se activa la noche (5% = casi apagado)", color = Color.Gray, fontSize = 12.sp)
                        }
                        Text(
                            text = "${(nightDimLevel * 100).toInt()}%",
                            color = selectedTheme.primaryColor,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Slider(
                        value = nightDimLevel,
                        onValueChange = { nightDimLevel = it },
                        valueRange = 0.05f..0.50f,
                        colors = SliderDefaults.colors(
                            thumbColor = selectedTheme.primaryColor,
                            activeTrackColor = selectedTheme.primaryColor,
                            inactiveTrackColor = Color(0xFF333333)
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Slider de Brillo Normal (30% a 100%)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Brillo en Modo Día ☀️", color = Color.White, fontSize = 15.sp)
                            Text("Iluminación habitual con la luz encendida", color = Color.Gray, fontSize = 12.sp)
                        }
                        Text(
                            text = "${(dayBrightness * 100).toInt()}%",
                            color = selectedTheme.primaryColor,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Slider(
                        value = dayBrightness,
                        onValueChange = { dayBrightness = it },
                        valueRange = 0.30f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = selectedTheme.primaryColor,
                            activeTrackColor = selectedTheme.primaryColor,
                            inactiveTrackColor = Color(0xFF333333)
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Switch activar noche
                    SettingSwitchRow(
                        title = "Activar Modo Noche Ahora",
                        subtitle = "Alterna al instante con el botón 🌙 de la barra superior",
                        checked = isNightMode,
                        onCheckedChange = { isNightMode = it },
                        primaryColor = selectedTheme.primaryColor
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // 4. Opciones de Visualización en Pantalla
                    SettingSectionTitle("4. Opciones de Pantalla")
                    Spacer(modifier = Modifier.height(8.dp))

                    SettingSwitchRow(
                        title = "Formato 24 Horas",
                        subtitle = "Desactiva para formato 12 Horas con indicador AM / PM",
                        checked = is24H,
                        onCheckedChange = { is24H = it },
                        primaryColor = selectedTheme.primaryColor
                    )

                    SettingSwitchRow(
                        title = "Mostrar Fecha Completa",
                        subtitle = "Muestra el día de la semana y mes centrado bajo la hora",
                        checked = showDate,
                        onCheckedChange = { showDate = it },
                        primaryColor = selectedTheme.primaryColor
                    )

                    SettingSwitchRow(
                        title = "Parpadeo de dos puntos (:)",
                        subtitle = "Efecto pulsante suave en el separador de horas y minutos",
                        checked = blinkColon,
                        onCheckedChange = { blinkColon = it },
                        primaryColor = selectedTheme.primaryColor
                    )

                    SettingSwitchRow(
                        title = "Mostrar Widget del Clima",
                        subtitle = "Muestra el chip de temperatura y tiempo en la esquina superior izquierda",
                        checked = showWeather,
                        onCheckedChange = { showWeather = it },
                        primaryColor = selectedTheme.primaryColor
                    )

                    SettingSwitchRow(
                        title = "Mostrar Widget de Alarma",
                        subtitle = "Muestra el chip de la próxima alarma en la esquina superior derecha",
                        checked = showAlarm,
                        onCheckedChange = { showAlarm = it },
                        primaryColor = selectedTheme.primaryColor
                    )

                    SettingSwitchRow(
                        title = "Protección Anti-Quemado OLED (*Burn-In*)",
                        subtitle = "Desplaza sutilmente la posición del reloj cada 3 minutos",
                        checked = oledProtection,
                        onCheckedChange = { oledProtection = it },
                        primaryColor = selectedTheme.primaryColor
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // 5. Configuración del Clima
                    SettingSectionTitle("5. Ubicación Meteorológica (Open-Meteo)")
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = cityName,
                            onValueChange = { cityName = it },
                            label = { Text("Ciudad") },
                            modifier = Modifier.weight(1.2f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = latitude,
                            onValueChange = { latitude = it },
                            label = { Text("Latitud") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = longitude,
                            onValueChange = { longitude = it },
                            label = { Text("Longitud") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // 6. Actualizaciones desde GitHub Releases
                    SettingSectionTitle("6. Actualizaciones desde GitHub Releases")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Introduce tu repositorio de GitHub (ej. usuario/repositorio o enlace a GitHub). La aplicación comprobará si has subido un nuevo APK a tus Releases:",
                        color = Color.LightGray,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = updateUrl,
                        onValueChange = { updateUrl = it },
                        label = { Text("Repositorio de GitHub o URL") },
                        placeholder = { Text("tu-usuario/tu-repositorio") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SettingSwitchRow(
                        title = "Comprobar Actualizaciones al Iniciar",
                        subtitle = "Avisa automáticamente con un diálogo si publicas un nuevo release con APK",
                        checked = autoCheckUpdates,
                        onCheckedChange = { autoCheckUpdates = it },
                        primaryColor = selectedTheme.primaryColor
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedButton(
                        onClick = { onCheckUpdates(updateUrl) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🔍 Comprobar Actualizaciones en GitHub", color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // 7. Activación NFC
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF202020))
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "📡", fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Activación por NFC",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Apoyando el móvil sobre cualquier pegatina o etiqueta NFC en tu mesilla de noche, el reloj se activa automáticamente en apaisado.",
                                color = Color.LightGray,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Reloj Despertador v3.2 (GitHub Releases & Pantalla Centrada)",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                }

                Spacer(modifier = Modifier.height(24.dp))

                // Botón Guardar Cambios
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            val lat = latitude.toDoubleOrNull() ?: preferences.latitude
                            val lon = longitude.toDoubleOrNull() ?: preferences.longitude
                            val newPrefs = preferences.copy(
                                colorTheme = selectedTheme,
                                clockSize = selectedSize,
                                secondsStyle = selectedSecondsStyle,
                                is24HourFormat = is24H,
                                showDate = showDate,
                                blinkColon = blinkColon,
                                showWeatherWidget = showWeather,
                                showAlarmWidget = showAlarm,
                                dayBrightness = dayBrightness,
                                nightDimLevel = nightDimLevel,
                                isNightModeActive = isNightMode,
                                oledBurnInProtection = oledProtection,
                                cityName = cityName.ifBlank { "Ciudad" },
                                latitude = lat,
                                longitude = lon,
                                updateCheckUrl = updateUrl.trim(),
                                autoCheckUpdates = autoCheckUpdates
                            )
                            onSavePreferences(newPrefs)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = selectedTheme.primaryColor),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(50.dp)
                    ) {
                        Text(
                            text = "Guardar y Aplicar Ajustes",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingSectionTitle(title: String) {
    Text(
        text = title,
        color = Color.White,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun SettingSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    primaryColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 15.sp)
            Text(subtitle, color = Color.Gray, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = primaryColor)
        )
    }
}
