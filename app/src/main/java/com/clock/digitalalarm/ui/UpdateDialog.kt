package com.clock.digitalalarm.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.clock.digitalalarm.model.UpdateInfo

@Composable
fun UpdateAvailableDialog(
    updateInfo: UpdateInfo,
    currentVersionName: String,
    primaryColor: Color,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF181818))
                .border(1.dp, Color(0xFF333333), RoundedCornerShape(20.dp))
                .padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "🚀", fontSize = 48.sp)
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "¡Nueva Actualización Disponible!",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Versión actual: $currentVersionName → Nueva: ${updateInfo.versionName}",
                    color = primaryColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

                if (updateInfo.releaseTitle.isNotBlank() && updateInfo.releaseTitle != updateInfo.versionName) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = updateInfo.releaseTitle,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Notas de la versión
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF242424))
                        .padding(14.dp)
                ) {
                    Column {
                        Text(
                            text = "Novedades:",
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = updateInfo.changelog,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Más tarde", color = Color.White)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateInfo.apkUrl)).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                    ) {
                        Text(
                            text = "⬇️ Descargar APK e Instalar",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

