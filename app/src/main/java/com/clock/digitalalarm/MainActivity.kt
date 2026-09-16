package com.clock.digitalalarm

import android.app.Activity
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.clock.digitalalarm.data.AlarmRepository
import com.clock.digitalalarm.model.UpdateInfo
import com.clock.digitalalarm.model.WeatherData
import com.clock.digitalalarm.service.AlarmScheduler
import com.clock.digitalalarm.service.UpdateChecker
import com.clock.digitalalarm.service.UpdateResult
import com.clock.digitalalarm.service.WeatherService
import com.clock.digitalalarm.ui.AlarmHeaderChip
import com.clock.digitalalarm.ui.AlarmManagerDialog
import com.clock.digitalalarm.ui.DigitalClockDisplay
import com.clock.digitalalarm.ui.SettingsDialog
import com.clock.digitalalarm.ui.UpdateAvailableDialog
import com.clock.digitalalarm.ui.WeatherHeaderChip
import com.clock.digitalalarm.ui.theme.DigitalClockTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var repository: AlarmRepository
    private lateinit var alarmScheduler: AlarmScheduler
    private lateinit var updateChecker: UpdateChecker
    private val weatherService = WeatherService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        repository = AlarmRepository(this)
        alarmScheduler = AlarmScheduler(this)
        updateChecker = UpdateChecker(this)

        // Mantener la pantalla encendida siempre que la app esté visible
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Modo pantalla completa inmersivo
        enableImmersiveMode()

        // Comprobar si se abrió por etiqueta NFC
        handleNfcIntent(intent)

        setContent {
            DigitalClockTheme {
                MainClockScreen(
                    repository = repository,
                    alarmScheduler = alarmScheduler,
                    weatherService = weatherService,
                    updateChecker = updateChecker
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNfcIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        enableImmersiveMode()
    }

    private fun handleNfcIntent(intent: Intent) {
        val action = intent.action
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == action ||
            NfcAdapter.ACTION_TECH_DISCOVERED == action ||
            NfcAdapter.ACTION_TAG_DISCOVERED == action
        ) {
            val tag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            }
            val tagId = tag?.id?.joinToString(":") { "%02X".format(it) } ?: "Detectada"
            Toast.makeText(this, "Reloj despertador activado por NFC ($tagId)", Toast.LENGTH_SHORT).show()
        }
    }

    private fun enableImmersiveMode() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.systemBars())
    }
}

@Composable
fun MainClockScreen(
    repository: AlarmRepository,
    alarmScheduler: AlarmScheduler,
    weatherService: WeatherService,
    updateChecker: UpdateChecker
) {
    val alarms by repository.alarms.collectAsState()
    val preferences by repository.preferences.collectAsState()

    var weatherData by remember { mutableStateOf<WeatherData?>(null) }
    var isWeatherLoading by remember { mutableStateOf(false) }

    var showAlarmDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    var availableUpdate by remember { mutableStateOf<UpdateInfo?>(null) }
    var currentVersionName by remember { mutableStateOf(updateChecker.getCurrentVersionName()) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Cargar clima periódicamente cada 30 minutos
    fun refreshWeather() {
        coroutineScope.launch {
            isWeatherLoading = true
            val data = weatherService.fetchWeather(
                latitude = preferences.latitude,
                longitude = preferences.longitude,
                cityName = preferences.cityName
            )
            weatherData = data
            isWeatherLoading = false
        }
    }

    LaunchedEffect(preferences.latitude, preferences.longitude) {
        refreshWeather()
        while (true) {
            delay(30 * 60 * 1000L)
            refreshWeather()
        }
    }

    // Comprobación automática de actualizaciones al iniciar
    LaunchedEffect(preferences.updateCheckUrl, preferences.autoCheckUpdates) {
        if (preferences.autoCheckUpdates && preferences.updateCheckUrl.isNotBlank()) {
            when (val res = updateChecker.checkUpdate(preferences.updateCheckUrl)) {
                is UpdateResult.UpdateAvailable -> {
                    availableUpdate = res.updateInfo
                    currentVersionName = res.localVersionName
                }
                else -> {}
            }
        }
    }

    fun performManualUpdateCheck(repoOrUrl: String) {
        if (repoOrUrl.isBlank()) {
            Toast.makeText(context, "Introduce tu repositorio de GitHub (ej. usuario/repo)", Toast.LENGTH_SHORT).show()
            return
        }
        Toast.makeText(context, "Buscando versiones en GitHub...", Toast.LENGTH_SHORT).show()
        coroutineScope.launch {
            when (val res = updateChecker.checkUpdate(repoOrUrl)) {
                is UpdateResult.UpdateAvailable -> {
                    availableUpdate = res.updateInfo
                    currentVersionName = res.localVersionName
                }
                is UpdateResult.UpToDate -> {
                    Toast.makeText(context, "¡Tienes la última versión instalada (${res.localVersionName})!", Toast.LENGTH_SHORT).show()
                }
                is UpdateResult.Error -> {
                    Toast.makeText(context, res.message, Toast.LENGTH_LONG).show()
                }
                is UpdateResult.NoUrlConfigured -> {
                    Toast.makeText(context, "Configura el repositorio de GitHub primero", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    // Próxima alarma activa
    val nextAlarm = remember(alarms) {
        alarms.filter { it.isEnabled }
            .minByOrNull { AlarmScheduler.calculateNextTriggerMillis(it) }
    }

    val primaryColor = preferences.colorTheme.primaryColor

    // Contenedor Maestro: Reloj 100% centrado con barra flotante superior que nunca colisiona
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // 1. RELOJ DIGITAL: 100% CENTRADO EN LA PANTALLA
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            DigitalClockDisplay(
                preferences = preferences,
                modifier = Modifier.fillMaxSize()
            )
        }

        // 2. BARRA SUPERIOR FLOTANTE
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Extremo Izquierdo: Clima compacto
            if (preferences.showWeatherWidget) {
                WeatherHeaderChip(
                    weatherData = weatherData,
                    isLoading = isWeatherLoading,
                    primaryColor = primaryColor,
                    onRefresh = { refreshWeather() }
                )
            } else {
                Spacer(modifier = Modifier.width(1.dp))
            }

            // Extremo Derecho: Alarma + Botón Noche + Botón Ajustes
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Widget de Alarma
                if (preferences.showAlarmWidget) {
                    AlarmHeaderChip(
                        nextAlarm = nextAlarm,
                        primaryColor = primaryColor,
                        onClick = { showAlarmDialog = true }
                    )
                }

                // Botón Modo Noche
                RoundIconButton(
                    icon = if (preferences.isNightModeActive) "🌙" else "☀️",
                    contentDescription = "Alternar Modo Noche",
                    primaryColor = if (preferences.isNightModeActive) primaryColor else Color.Transparent,
                    onClick = {
                        repository.updatePreferences(
                            preferences.copy(isNightModeActive = !preferences.isNightModeActive)
                        )
                    }
                )

                // Botón Ajustes
                RoundIconButton(
                    icon = "⚙️",
                    contentDescription = "Ajustes del Reloj",
                    primaryColor = Color.Transparent,
                    onClick = { showSettingsDialog = true }
                )
            }
        }

        // 3. Control de brillo a nivel de hardware y sistema
        LaunchedEffect(preferences.effectiveBrightness) {
            val window = (context as? Activity)?.window
            if (window != null) {
                val lp = window.attributes
                lp.screenBrightness = preferences.effectiveBrightness.coerceIn(0.01f, 1.0f)
                window.attributes = lp
            }
        }

        // 4. Capa de Atenuación de Brillo (Filtro OLED visual para oscuridad total)
        val currentBrightness = preferences.effectiveBrightness
        if (currentBrightness < 1.0f) {
            val alpha = (1.0f - currentBrightness).coerceIn(0f, 0.95f)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = alpha))
            )
        }

        // Diálogo de Alarmas
        if (showAlarmDialog) {
            AlarmManagerDialog(
                alarms = alarms,
                primaryColor = primaryColor,
                onDismiss = { showAlarmDialog = false },
                onSaveAlarm = { alarm ->
                    repository.saveAlarm(alarm)
                    alarmScheduler.scheduleAlarm(alarm)
                },
                onDeleteAlarm = { id ->
                    val alarm = repository.getAlarm(id)
                    if (alarm != null) alarmScheduler.cancelAlarm(alarm)
                    repository.deleteAlarm(id)
                },
                onToggleAlarm = { id, enabled ->
                    repository.toggleAlarm(id, enabled)
                    val alarm = repository.getAlarm(id)
                    if (alarm != null) {
                        alarmScheduler.scheduleAlarm(alarm.copy(isEnabled = enabled))
                    }
                }
            )
        }

        // Diálogo de Ajustes
        if (showSettingsDialog) {
            SettingsDialog(
                preferences = preferences,
                onSavePreferences = { newPrefs ->
                    repository.updatePreferences(newPrefs)
                },
                onCheckUpdates = { url ->
                    performManualUpdateCheck(url)
                },
                onDismiss = { showSettingsDialog = false }
            )
        }

        // Diálogo de Actualización Disponible
        availableUpdate?.let { update ->
            UpdateAvailableDialog(
                updateInfo = update,
                currentVersionName = currentVersionName,
                primaryColor = primaryColor,
                onDismiss = { availableUpdate = null }
            )
        }
    }
}

@Composable
fun RoundIconButton(
    icon: String,
    contentDescription: String,
    primaryColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(Color(0xFF222222).copy(alpha = 0.9f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = icon,
            fontSize = 19.sp
        )
    }
}
