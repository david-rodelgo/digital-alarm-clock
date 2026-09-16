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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.clock.digitalalarm.model.AlarmItem

@Composable
fun AlarmManagerDialog(
    alarms: List<AlarmItem>,
    primaryColor: Color,
    onDismiss: () -> Unit,
    onSaveAlarm: (AlarmItem) -> Unit,
    onDeleteAlarm: (String) -> Unit,
    onToggleAlarm: (String, Boolean) -> Unit
) {
    var editingAlarm by remember { mutableStateOf<AlarmItem?>(null) }
    var isCreatingNew by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF161616))
                .border(1.dp, Color(0xFF333333), RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            if (editingAlarm != null || isCreatingNew) {
                // Pantalla de edición / creación de alarma
                AlarmEditor(
                    initialAlarm = editingAlarm,
                    primaryColor = primaryColor,
                    onCancel = {
                        editingAlarm = null
                        isCreatingNew = false
                    },
                    onSave = { alarm ->
                        onSaveAlarm(alarm)
                        editingAlarm = null
                        isCreatingNew = false
                    }
                )
            } else {
                // Lista de alarmas
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Gestión de Alarmas",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = { isCreatingNew = true },
                                colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                            ) {
                                Text("+ Nueva Alarma", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(onClick = onDismiss) {
                                Text("Cerrar", color = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (alarms.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No hay alarmas configuradas.\nToca '+ Nueva Alarma' para añadir una.",
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(alarms, key = { it.id }) { alarm ->
                                AlarmRowItem(
                                    alarm = alarm,
                                    primaryColor = primaryColor,
                                    onEdit = { editingAlarm = alarm },
                                    onDelete = { onDeleteAlarm(alarm.id) },
                                    onToggle = { enabled -> onToggleAlarm(alarm.id, enabled) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AlarmRowItem(
    alarm: AlarmItem,
    primaryColor: Color,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF222222))
            .clickable { onEdit() }
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = alarm.formattedTime,
                    color = if (alarm.isEnabled) primaryColor else Color.Gray,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = alarm.label,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                text = alarm.daysSummary,
                color = Color(0xFFAAAAAA),
                fontSize = 13.sp
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(
                checked = alarm.isEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = primaryColor,
                    checkedTrackColor = primaryColor.copy(alpha = 0.3f),
                    uncheckedThumbColor = Color.DarkGray,
                    uncheckedTrackColor = Color.Black
                )
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "🗑️",
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onDelete() }
                    .padding(8.dp),
                fontSize = 20.sp
            )
        }
    }
}

@Composable
fun AlarmEditor(
    initialAlarm: AlarmItem?,
    primaryColor: Color,
    onCancel: () -> Unit,
    onSave: (AlarmItem) -> Unit
) {
    var hour by remember { mutableIntStateOf(initialAlarm?.hour ?: 7) }
    var minute by remember { mutableIntStateOf(initialAlarm?.minute ?: 0) }
    var label by remember { mutableStateOf(initialAlarm?.label ?: "Alarma") }
    var daysOfWeek by remember { mutableStateOf(initialAlarm?.daysOfWeek ?: emptySet()) }
    var vibrate by remember { mutableStateOf(initialAlarm?.vibrate ?: true) }

    val daysLabels = listOf(
        1 to "L", 2 to "M", 3 to "X", 4 to "J", 5 to "V", 6 to "S", 7 to "D"
    )

    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        // Columna Izquierda: Selector de Hora y Minuto
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Hora de la Alarma",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Selector de Horas
                NumberWheel(
                    value = hour,
                    range = 0..23,
                    onValueChange = { hour = it },
                    primaryColor = primaryColor
                )
                Text(
                    text = ":",
                    color = primaryColor,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                // Selector de Minutos
                NumberWheel(
                    value = minute,
                    range = 0..59,
                    onValueChange = { minute = it },
                    primaryColor = primaryColor
                )
            }
        }

        // Columna Derecha: Opciones y Días
        Column(
            modifier = Modifier
                .weight(1.2f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Etiqueta",
                    color = Color.LightGray,
                    fontSize = 14.sp
                )
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Repetir días",
                    color = Color.LightGray,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    daysLabels.forEach { (dayInt, dayText) ->
                        val isSelected = daysOfWeek.contains(dayInt)
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) primaryColor else Color(0xFF2A2A2A))
                                .clickable {
                                    daysOfWeek = if (isSelected) {
                                        daysOfWeek - dayInt
                                    } else {
                                        daysOfWeek + dayInt
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dayText,
                                color = if (isSelected) Color.Black else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Vibración al sonar", color = Color.White, fontSize = 16.sp)
                    Switch(
                        checked = vibrate,
                        onCheckedChange = { vibrate = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = primaryColor)
                    )
                }
            }

            // Botones Cancelar y Guardar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(onClick = onCancel) {
                    Text("Cancelar", color = Color.White)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = {
                        val alarm = AlarmItem(
                            id = initialAlarm?.id ?: java.util.UUID.randomUUID().toString(),
                            hour = hour,
                            minute = minute,
                            isEnabled = true,
                            daysOfWeek = daysOfWeek,
                            label = label.ifBlank { "Alarma" },
                            vibrate = vibrate
                        )
                        onSave(alarm)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    Text("Guardar Alarma", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun NumberWheel(
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit,
    primaryColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF222222))
            .padding(8.dp)
    ) {
        Text(
            text = "▲",
            color = Color.LightGray,
            modifier = Modifier
                .clickable {
                    val next = if (value + 1 > range.last) range.first else value + 1
                    onValueChange(next)
                }
                .padding(8.dp),
            fontSize = 18.sp
        )
        Text(
            text = String.format("%02d", value),
            color = primaryColor,
            fontSize = 42.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        Text(
            text = "▼",
            color = Color.LightGray,
            modifier = Modifier
                .clickable {
                    val prev = if (value - 1 < range.first) range.last else value - 1
                    onValueChange(prev)
                }
                .padding(8.dp),
            fontSize = 18.sp
        )
    }
}
