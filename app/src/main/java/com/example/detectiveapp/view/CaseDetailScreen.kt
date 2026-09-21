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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
fun CaseDetailScreen(
    detailState: CaseDetailUiState,
    onBack: () -> Unit,
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = caseEntity.title, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Volver atrás")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(text = "Detalles del Caso", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(text = "Fecha de inicio: ${caseEntity.date}")
                        Text(text = "Estado actual: ${caseEntity.status}", fontWeight = FontWeight.SemiBold)
                        Text(text = "Descripción: ${caseEntity.description}")

                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { onChangeStatus(caseEntity, "Abierto") }, modifier = Modifier.weight(1f)) { Text("Abrir") }
                            Button(onClick = { onChangeStatus(caseEntity, "Cerrado") }, modifier = Modifier.weight(1f)) { Text("Cerrar") }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                            OutlinedButton(onClick = { onEdit(caseEntity) }, modifier = Modifier.weight(1f)) { Text("Editar Caso") }
                            Button(onClick = { onDelete(caseEntity); onBack() }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red), modifier = Modifier.weight(1f)) { Text("Eliminar") }
                        }
                    }
                }
            }
            item {
                Text(text = "Testimonios de Testigos 👤", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = witnessTestimonyText, onValueChange = { witnessTestimonyText = it }, label = { Text("Declaración Escrita") }, modifier = Modifier.fillMaxWidth())

                        val permissionLauncher = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.RequestPermission()
                        ) { isGranted: Boolean ->
                            if (isGranted) {
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
                            }
                        }

                        Button(
                            onClick = {
                                if (!isRecording) {
                                    permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
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
                        ) { Text(if (isRecording) "🛑 Detener Grabación" else "🎙️ Grabar Audio de Testigo") }

                        if (tempAudioPath != null && !isRecording) {
                            Text("¡Audio grabado con éxito! ✓", color = Color(0xFF10B981), fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                if (witnessTestimonyText.isNotBlank() || tempAudioPath != null) {
                                    onAddEvidence(caseEntity.id, "Declaración Testigo", "Testimonio adjunto", null, witnessTestimonyText.ifBlank { null }, tempAudioPath)
                                    witnessTestimonyText = ""
                                    tempAudioPath = null
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Guardar Testimonio") }
                    }
                }
            }

            item {
                Text(text = "Hallazgos de la Investigación 📑", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = newFindingDesc, onValueChange = { newFindingDesc = it }, label = { Text("Descripción del Hallazgo") }, modifier = Modifier.fillMaxWidth())
                        Button(onClick = {
                            if (newFindingDesc.isNotBlank()) {
                                onAddFinding(caseEntity.id, newFindingDesc, "20/09/2026")
                                newFindingDesc = ""
                            }
                        }, modifier = Modifier.fillMaxWidth()) { Text("Agregar Hallazgo") }
                    }
                }
            }

            items(detailState.findings) { finding ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = finding.description)
                        Text(text = finding.date, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }
            item {
                Text(text = "Evidencias Fotográficas 📷", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = newEvidenceName, onValueChange = { newEvidenceName = it }, label = { Text("Nombre Evidencia") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = newEvidenceDesc, onValueChange = { newEvidenceDesc = it }, label = { Text("Descripción Evidencia") }, modifier = Modifier.fillMaxWidth())

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { cameraLauncher.launch() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)), modifier = Modifier.weight(1f)) { Text("Cámara 📷") }
                            Button(onClick = { galleryLauncher.launch("image/*") }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4B5563)), modifier = Modifier.weight(1f)) { Text("Galería 🖼️") }
                        }

                        if (currentEvidenceBitmap != null) {
                            Text("¡Imagen adjuntada! ✓", color = Color(0xFF10B981), fontSize = 12.sp)
                        }

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
                        ) { Text("Agregar Evidencia") }
                    }
                }
            }

            items(detailState.evidences) { evidence ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = evidence.name, fontWeight = FontWeight.Bold)
                            Text(text = evidence.description)

                            if (!evidence.witnessNotes.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "👤 Testigo: ${evidence.witnessNotes}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
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
                                Card(modifier = Modifier.size(60.dp).clickable { zoomedEvidence = evidence }, shape = RoundedCornerShape(8.dp)) {
                                    Image(bitmap = btm.asImageBitmap(), contentDescription = "Miniatura", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (zoomedEvidence != null) {
        val evidencia = zoomedEvidence!!
        AlertDialog(
            onDismissRequest = { zoomedEvidence = null },
            title = { Text(text = evidencia.name, fontWeight = FontWeight.Bold) },
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
                            Card(modifier = Modifier.fillMaxWidth().height(220.dp), shape = RoundedCornerShape(8.dp)) {
                                Image(bitmap = btm.asImageBitmap(), contentDescription = "Detalle", contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize())
                            }
                        }
                    }
                    Text(text = evidencia.description, fontSize = 15.sp, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(onClick = { zoomedEvidence = null }) {
                    Text("Cerrar")
                }
            }
        )
    }
}
