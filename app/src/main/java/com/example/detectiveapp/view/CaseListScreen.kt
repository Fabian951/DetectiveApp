package com.example.detectiveapp.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
    var showDetailDialog by remember { mutableStateOf(false) }

    val statusFilters = listOf("Todos", "Abierto", "En Investigación", "Cerrado")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Casos de Investigación") }
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

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                statusFilters.forEach { filter ->
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
                            showDetailDialog = true
                        }
                    )
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

    if (showDetailDialog && detailState.selectedCase != null) {
        CaseDetailDialog(
            detailState = detailState,
            onDismiss = { showDetailDialog = false },
            onEdit = { caseEntity ->
                caseToEdit = caseEntity
                showDetailDialog = false
                showFormDialog = true
            },
            onDelete = { caseEntity ->
                viewModel.deleteCase(caseEntity)
            },
            onChangeStatus = { caseEntity, newStatus ->
                viewModel.updateCaseStatus(caseEntity, newStatus)
            },
            onAddFinding = { caseId, desc, date ->
                viewModel.addFinding(caseId, desc, date)
            },
            onAddEvidence = { caseId, name, desc ->
                viewModel.addEvidence(caseId, name, desc)
            }
        )
    }
}

@Composable
fun CaseCard(caseEntity: CaseEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = caseEntity.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = caseEntity.status,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Fecha: ${caseEntity.date}", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = caseEntity.description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}