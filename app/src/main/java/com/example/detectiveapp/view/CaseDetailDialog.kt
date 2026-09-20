package com.example.detectiveapp.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.detectiveapp.model.entity.CaseEntity
import com.example.detectiveapp.viewmodel.CaseDetailUiState

@Composable
fun CaseDetailDialog(
    detailState: CaseDetailUiState,
    onDismiss: () -> Unit,
    onEdit: (CaseEntity) -> Unit,
    onDelete: (CaseEntity) -> Unit,
    onChangeStatus: (CaseEntity, String) -> Unit,
    onAddFinding: (caseId: Int, description: String, date: String) -> Unit,
    onAddEvidence: (caseId: Int, name: String, description: String) -> Unit
) {
    val caseEntity = detailState.selectedCase ?: return

    var newFindingDesc by remember { mutableStateOf("") }
    var newEvidenceName by remember { mutableStateOf("") }
    var newEvidenceDesc by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = caseEntity.title, fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(modifier = Modifier.height(400.dp)) {
                item {
                    Text(text = "Fecha: ${caseEntity.date}")
                    Text(text = "Estado: ${caseEntity.status}", fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Descripción: ${caseEntity.description}")
                    Spacer(modifier = Modifier.height(16.dp))

                    Row {
                        Button(onClick = { onChangeStatus(caseEntity, "Abierto") }) {
                            Text("Abierto")
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Button(onClick = { onChangeStatus(caseEntity, "Cerrado") }) {
                            Text("Cerrar")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "--- Hallazgos ---", fontWeight = FontWeight.Bold)
                }

                items(detailState.findings) { finding ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Text(text = finding.description)
                            Text(text = finding.date, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                item {
                    OutlinedTextField(
                        value = newFindingDesc,
                        onValueChange = { newFindingDesc = it },
                        label = { Text("Nuevo Hallazgo") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            if (newFindingDesc.isNotBlank()) {
                                onAddFinding(caseEntity.id, newFindingDesc, "13/09/2026")
                                newFindingDesc = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Agregar Hallazgo")
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "--- Evidencias ---", fontWeight = FontWeight.Bold)
                }

                items(detailState.evidences) { evidence ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Text(text = evidence.name, fontWeight = FontWeight.Bold)
                            Text(text = evidence.description)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                item {
                    OutlinedTextField(
                        value = newEvidenceName,
                        onValueChange = { newEvidenceName = it },
                        label = { Text("Nombre Evidencia") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newEvidenceDesc,
                        onValueChange = { newEvidenceDesc = it },
                        label = { Text("Descripción Evidencia") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            if (newEvidenceName.isNotBlank()) {
                                onAddEvidence(caseEntity.id, newEvidenceName, newEvidenceDesc)
                                newEvidenceName = ""
                                newEvidenceDesc = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Agregar Evidencia")
                    }
                }
            }
        },
        confirmButton = {
            Row {
                Button(onClick = { onEdit(caseEntity) }) {
                    Text("Editar")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        onDelete(caseEntity)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Eliminar")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}