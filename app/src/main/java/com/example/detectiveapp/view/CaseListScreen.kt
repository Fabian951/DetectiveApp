package com.example.detectiveapp.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.detectiveapp.model.entity.CaseEntity
import com.example.detectiveapp.viewmodel.CaseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaseListScreen(viewModel: CaseViewModel) {
    val listState by viewModel.listUiState.collectAsState()
    val detailState by viewModel.detailUiState.collectAsState()

    var showFormDialog by remember { mutableStateOf(false) }
    var caseToEdit by remember { mutableStateOf<CaseEntity?>(null) }

    // Control de navegación elástica a pantalla completa dedicado
    var showDetailScreen by remember { mutableStateOf(false) }

    val statusFilters = listOf("Todos", "Abierto", "En proceso", "Cerrado")

    if (showDetailScreen && detailState.selectedCase != null) {
        CaseDetailScreen(
            detailState = detailState,
            onBack = {
                showDetailScreen = false
                viewModel.selectCase(0) // Cambiado null por 0 para cumplir con el tipo Int
            },

            onEdit = { caseEntity ->
                caseToEdit = caseEntity
                showDetailScreen = false
                showFormDialog = true
            },
            onDelete = { caseEntity ->
                viewModel.deleteCase(caseEntity)
                showDetailScreen = false
            },
            onChangeStatus = { caseEntity, newStatus ->
                viewModel.updateCaseStatus(caseEntity, newStatus)
            },
            onAddFinding = { caseId, desc, date ->
                viewModel.addFinding(caseId, desc, date)
            },
            onAddEvidence = { caseId, name, desc, uri, witnessNotes, audioPath ->
                viewModel.addEvidence(caseId, name, description = desc, imageUri = uri, witnessNotes = witnessNotes, audioPath = audioPath)
            }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Casos de Investigación", fontWeight = FontWeight.Bold) }
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = {
                    caseToEdit = null
                    showFormDialog = true
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Nuevo Caso")
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = listState.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    label = { Text("Buscar caso por título o descripción") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(statusFilters) { filter ->
                        FilterChip(
                            selected = listState.selectedStatusFilter == filter,
                            onClick = { viewModel.onStatusFilterChange(filter) },
                            label = { Text(filter) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(listState.cases) { caseEntity ->
                        CaseCard(
                            caseEntity = caseEntity,
                            onClick = {
                                viewModel.selectCase(caseEntity.id)
                                showDetailScreen = true
                            }
                        )
                    }
                }
            }
        }
    }

    if (showFormDialog) {
        CaseFormDialog(
            initialCase = caseToEdit,
            onDismiss = { showFormDialog = false },
            onSave = { title, description, date, status ->
                if (caseToEdit == null) {
                    viewModel.createCase(title, description, date, status)
                } else {
                    val updated = caseToEdit!!.copy(
                        title = title,
                        description = description,
                        date = date,
                        status = status
                    )
                    viewModel.updateCase(updated)
                }
            }
        )
    }
}
@Composable
fun CaseCard(caseEntity: CaseEntity, onClick: () -> Unit) {
    val statusColor = when (caseEntity.status) {
        "Abierto" -> Color(0xFF2E7D32)
        "En proceso" -> Color(0xFFFBC02D)
        "Cerrado" -> Color(0xFFC62828)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = caseEntity.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = caseEntity.status, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = statusColor)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Fecha: ${caseEntity.date}", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = caseEntity.description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

