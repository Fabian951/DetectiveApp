package com.example.detectiveapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.detectiveapp.model.CaseRepository
import com.example.detectiveapp.model.entity.CaseEntity
import com.example.detectiveapp.model.entity.EvidenceEntity
import com.example.detectiveapp.model.entity.FindingEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CaseListUiState(
    val cases: List<CaseEntity> = emptyList(),
    val searchQuery: String = "",
    val selectedStatusFilter: String = "Todos"
)

data class CaseDetailUiState(
    val selectedCase: CaseEntity? = null,
    val findings: List<FindingEntity> = emptyList(),
    val evidences: List<EvidenceEntity> = emptyList()
)

class CaseViewModel(private val repository: CaseRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedStatusFilter = MutableStateFlow("Todos")
    val selectedStatusFilter: StateFlow<String> = _selectedStatusFilter.asStateFlow()

    private val _listUiState = MutableStateFlow(CaseListUiState())
    val listUiState: StateFlow<CaseListUiState> = _listUiState.asStateFlow()

    private val _detailUiState = MutableStateFlow(CaseDetailUiState())
    val detailUiState: StateFlow<CaseDetailUiState> = _detailUiState.asStateFlow()

    init {
        // Escucha reactiva combinada para Búsqueda, Filtro de Estado y Cambios en la Base de Datos
        viewModelScope.launch {
            combine(
                repository.allCases,
                _searchQuery,
                _selectedStatusFilter
            ) { cases, query, filter ->
                val cleanQuery = query.trim()
                var filtered = cases

                if (cleanQuery.isNotEmpty()) {
                    filtered = filtered.filter {
                        it.title.contains(cleanQuery, ignoreCase = true) ||
                                it.description.contains(cleanQuery, ignoreCase = true)
                    }
                }

                if (filter != "Todos") {
                    filtered = filtered.filter {
                        it.status.equals(filter, ignoreCase = true) ||
                                (filter == "En proceso" && it.status.equals("En Investigación", ignoreCase = true)) ||
                                (filter == "En Investigación" && it.status.equals("En proceso", ignoreCase = true))
                    }
                }

                CaseListUiState(
                    cases = filtered,
                    searchQuery = query,
                    selectedStatusFilter = filter
                )
            }.collect { newState ->
                _listUiState.value = newState
            }
        }
    }

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onStatusFilterChange(newFilter: String) {
        _selectedStatusFilter.value = newFilter
    }

    fun selectCase(caseId: Int) {
        viewModelScope.launch {
            repository.allCases.collect { cases ->
                val caso = cases.find { it.id == caseId }
                _detailUiState.update { it.copy(selectedCase = caso) }
                if (caso != null) {
                    loadCaseDetails(caso.id)
                }
            }
        }
    }

    private fun loadCaseDetails(caseId: Int) {
        viewModelScope.launch {
            repository.getFindingsByCase(caseId).collect { findingsList ->
                _detailUiState.update { it.copy(findings = findingsList) }
            }
        }
        viewModelScope.launch {
            repository.getEvidencesByCase(caseId).collect { evidencesList ->
                _detailUiState.update { it.copy(evidences = evidencesList) }
            }
        }
    }

    fun createCase(title: String, description: String, date: String, status: String) {
        viewModelScope.launch {
            val nuevoCaso = CaseEntity(title = title, description = description, date = date, status = status)
            repository.insertCase(nuevoCaso)
        }
    }

    fun updateCase(caseEntity: CaseEntity) {
        viewModelScope.launch {
            repository.updateCase(caseEntity)
            if (_detailUiState.value.selectedCase?.id == caseEntity.id) {
                _detailUiState.update { it.copy(selectedCase = caseEntity) }
            }
        }
    }

    fun deleteCase(caseEntity: CaseEntity) {
        viewModelScope.launch {
            repository.deleteCase(caseEntity)
            _detailUiState.update { CaseDetailUiState() }
        }
    }

    fun updateCaseStatus(caseEntity: CaseEntity, newStatus: String) {
        updateCase(caseEntity.copy(status = newStatus))
    }

    fun addFinding(caseId: Int, description: String, date: String) {
        viewModelScope.launch {
            val nuevoHallazgo = FindingEntity(caseId = caseId, description = description, date = date)
            repository.insertFinding(nuevoHallazgo)
        }
    }

    fun addEvidence(
        caseId: Int,
        name: String,
        description: String,
        imageUri: String? = null,
        witnessNotes: String? = null,
        audioPath: String? = null
    ) {
        viewModelScope.launch {
            val nuevaEvidencia = EvidenceEntity(
                caseId = caseId,
                name = name,
                description = description,
                imageUri = imageUri,
                witnessNotes = witnessNotes,
                audioPath = audioPath
            )
            repository.insertEvidence(nuevaEvidencia)
        }
    }
}

class CaseViewModelFactory(private val repository: CaseRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CaseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CaseViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}