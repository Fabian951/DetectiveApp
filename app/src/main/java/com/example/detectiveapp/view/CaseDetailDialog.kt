package com.example.detectiveapp.view

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.detectiveapp.model.entity.CaseEntity
import com.example.detectiveapp.model.entity.EvidenceEntity
import com.example.detectiveapp.viewmodel.CaseDetailUiState
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaseDetailDialog(
    detailState: CaseDetailUiState,
    onDismiss: () -> Unit,
    onEdit: (CaseEntity) -> Unit,
    onDelete: (CaseEntity) -> Unit,
    onChangeStatus: (CaseEntity, String) -> Unit,
    onAddFinding: (caseId: Int, description: String, date: String) -> Unit,
    onAddEvidence: (caseId: Int, name: String, description: String, imageUri: String?, witnessNotes: String?, audioPath: String?) -> Unit
) {
    val caseEntity = detailState.selectedCase ?: return
    val context = LocalContext.current

    var newFindingDesc by remember { mutableStateOf("") }
    var newEvidenceName by remember { mutableStateOf("") }
    var newEvidenceDesc by remember { mutableStateOf("") }

    var witnessTestimonyText by remember { mutableStateOf("") }
    var tempAudioPath by remember { mutableStateOf<String?>(null) }
    var isRecording by remember { mutableStateOf(false) }

    var currentEvidenceBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var zoomedEvidence by remember { mutableStateOf<EvidenceEntity?>(null) }

    var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }
    val mediaPlayer = remember { MediaPlayer() }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? -> if (bitmap != null) currentEvidenceBitmap = bitmap }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(it)
                currentEvidenceBitmap = BitmapFactory.decodeStream(inputStream)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = caseEntity.title,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth() // Ajuste dinámico de ancho para el título
            )
        },
        text = {
            // El alto usa fillMaxWidth y un height seguro adaptable con espaciado vertical elástico
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().height(320.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(text = "Fecha: ${caseEntity.date}", modifier = Modifier.fillMaxWidth())
                        Text(text = "Estado: ${caseEntity.status}", fontWeight = FontWeight.SemiBold, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Descripción: ${caseEntity.description}", modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Button(onClick = { onChangeStatus(caseEntity, "Abierto") }, modifier = Modifier.weight(1f)) { Text("Abierto", fontSize = 13.sp) }
                            Button(onClick = { onChangeStatus(caseEntity, "Cerrado") }, modifier = Modifier.weight(1f)) { Text("Cerrar", fontSize = 13.sp) }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "--- Testimonios de Testigos ---", fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())
                    }
                    item {
                        OutlinedTextField(
                            value = witnessTestimonyText,
                            onValueChange = { witnessTestimonyText = it },
                            label = { Text("Declaración Escrita del Testigo") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = {
                                if (!isRecording) {
                                    try {
                                        val audioFile = File.createTempFile("testimonio_", ".3gp", context.cacheDir)
                                        tempAudioPath = audioFile.absolutePath
                                        @Suppress("DEPRECATION")
                                        mediaRecorder = MediaRecorder().apply {
                                            setAudioSource(MediaRecorder.AudioSource.MIC)
                                            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                                            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                                            setOutputFile(tempAudioPath)
                                            prepare()
                                            start()
                                        }
                                        isRecording = true
                                    } catch (e: Exception) { e.printStackTrace() }
                                } else {
                                    try {
                                        mediaRecorder?.stop()
                                        mediaRecorder?.release()
                                        mediaRecorder = null
                                        isRecording = false
                                    } catch (e: Exception) { e.printStackTrace() }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = if (isRecording) Color.Red else Color(0xFF16A34A)),
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(if (isRecording) "🛑 Detener Grabación" else "🎙️ Grabar Audio de Testigo", fontSize = 13.sp) }

                        if (tempAudioPath != null && !isRecording) {
                            Text("¡Audio de voz grabado con éxito! ✓", color = Color(0xFF10B981), fontSize = 12.sp, modifier = Modifier.fillMaxWidth().padding(top = 2.dp))
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = {
                                if (witnessTestimonyText.isNotBlank() || tempAudioPath != null) {
                                    onAddEvidence(caseEntity.id, "Declaración Testigo", "Testimonio adjunto", null, witnessTestimonyText.ifBlank { null }, tempAudioPath)
                                    witnessTestimonyText = ""
                                    tempAudioPath = null
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Guardar Testimonio", fontSize = 13.sp) }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "--- Hallazgos ---", fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())
                    }

                    items(detailState.findings) { finding ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(10.dp).fillMaxWidth()) {
                                Text(text = finding.description, modifier = Modifier.fillMaxWidth())
                                Text(text = finding.date, style = MaterialTheme.typography.bodySmall, modifier = Modifier.fillMaxWidth())
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = newFindingDesc,
                            onValueChange = { newFindingDesc = it },
                            label = { Text("Nuevo Hallazgo") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(
                            onClick = {
                                if (newFindingDesc.isNotBlank()) {
                                    onAddFinding(caseEntity.id, newFindingDesc, "20/09/2026")
                                    newFindingDesc = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Agregar Hallazgo", fontSize = 13.sp) }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "--- Evidencias Fotográficas ---", fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())
                    }
                    items(detailState.evidences) { evidence ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.padding(10.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = evidence.name, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())
                                    Text(text = evidence.description, modifier = Modifier.fillMaxWidth())

                                    if (!evidence.witnessNotes.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(text = "📝 Nota: ${evidence.witnessNotes}", style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.fillMaxWidth())
                                    }

                                    if (!evidence.audioPath.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Button(
                                            onClick = {
                                                try {
                                                    mediaPlayer.reset()
                                                    mediaPlayer.setDataSource(evidence.audioPath)
                                                    mediaPlayer.prepare()
                                                    mediaPlayer.start()
                                                } catch (e: Exception) { e.printStackTrace() }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                            modifier = Modifier.height(30.dp)
                                        ) { Text("▶ Escuchar Audio", fontSize = 11.sp) }
                                    }
                                }

                                evidence.imageUri?.let { base64String ->
                                    val bitmap = remember(base64String) {
                                        try {
                                            val decodedString = Base64.decode(base64String, Base64.DEFAULT)
                                            BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                                        } catch (e: Exception) { null }
                                    }
                                    bitmap?.let { btm ->
                                        Card(
                                            modifier = Modifier.size(50.dp).clickable { zoomedEvidence = evidence },
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Image(
                                                bitmap = btm.asImageBitmap(),
                                                contentDescription = "Miniatura",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(value = newEvidenceName, onValueChange = { newEvidenceName = it }, label = { Text("Nombre Evidencia") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(value = newEvidenceDesc, onValueChange = { newEvidenceDesc = it }, label = { Text("Descripción Evidencia") }, modifier = Modifier.fillMaxWidth())

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Button(onClick = { cameraLauncher.launch() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)), modifier = Modifier.weight(1f)) { Text("Cámara 📷", fontSize = 12.sp) }
                            Button(onClick = { galleryLauncher.launch("image/*") }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4B5563)), modifier = Modifier.weight(1f)) { Text("Galería 🖼️", fontSize = 12.sp) }
                        }

                        if (currentEvidenceBitmap != null) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("¡Imagen seleccionada con éxito! ✓", color = Color(0xFF10B981), fontWeight = FontWeight.SemiBold, fontSize = 12.sp, modifier = Modifier.fillMaxWidth())
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Button(
                            onClick = {
                                if (newEvidenceName.isNotBlank()) {
                                    val base64String = currentEvidenceBitmap?.let { btm ->
                                        val outputStream = ByteArrayOutputStream()
                                        btm.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                                        Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
                                    }
                                    onAddEvidence(caseEntity.id, newEvidenceName, newEvidenceDesc, base64String, null, null)
                                    newEvidenceName = ""
                                    newEvidenceDesc = ""
                                    currentEvidenceBitmap = null
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Agregar Evidencia", fontSize = 13.sp) }
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Button(onClick = { onEdit(caseEntity) }) { Text("Editar", fontSize = 13.sp) }
                Button(onClick = { onDelete(caseEntity); onDismiss() }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("Eliminar", fontSize = 13.sp) }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar", fontSize = 13.sp) }
        }
    )

    if (zoomedEvidence != null) {
        val evidencia = zoomedEvidence!!
        AlertDialog(
            onDismissRequest = { zoomedEvidence = null },
            title = { Text(text = evidencia.name, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth()) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    evidencia.imageUri?.let { base64String ->
                        val bitmap = remember(base64String) {
                            try {
                                val decodedString = Base64.decode(base64String, Base64.DEFAULT)
                                BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                            } catch (e: Exception) { null }
                        }
                        bitmap?.let { btm ->
                            Card(modifier = Modifier.fillMaxWidth().height(200.dp), shape = RoundedCornerShape(8.dp)) {
                                Image(bitmap = btm.asImageBitmap(), contentDescription = "Detalle Evidencia", contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize())
                            }
                        }
                    }
                    Text(text = evidencia.description, fontSize = 15.sp, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = { Button(onClick = { zoomedEvidence = null }) { Text("Cerrar") } }
        )
    }
}
