package com.example.detectiveapp.view

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.sp
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
                    title = { Text("ARCHIVO DE CASOS", fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.primary
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        caseToEdit = null
                        showFormDialog = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
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
        "Abierto" -> Color(0xFF3B82F6)      // Azul de investigación abierta
        "En proceso" -> Color(0xFFFFB300)   // Ámbar / Dorado
        "Cerrado" -> Color(0xFFB91C1C)      // Rojo sangre / Expediente Cerrado
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp), // Esquinas más rectas tipo folder vintage
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            // Una barra lateral de color según el estado del caso, muy estilo expediente
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(statusColor)
            )
            
            Column(modifier = Modifier.padding(16.dp).weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = caseEntity.title.uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "● ${caseEntity.status.uppercase()}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "FECHA DEL SUCESO: ${caseEntity.date}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = caseEntity.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}


