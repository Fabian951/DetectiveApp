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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Estado requerido para la pantalla de listado y filtros
data class CaseListUiState(
    val cases: List<CaseEntity> = emptyList(),
    val searchQuery: String = "",
    val selectedStatusFilter: String = "Todos"
)

// Estado requerido para la pantalla de detalles de un caso
data class CaseDetailUiState(
    val selectedCase: CaseEntity? = null,
    val findings: List<FindingEntity> = emptyList(),
    val evidences: List<EvidenceEntity> = emptyList()
)

class CaseViewModel(private val repository: CaseRepository) : ViewModel() {

    private val _listUiState = MutableStateFlow(CaseListUiState())
    val listUiState: StateFlow<CaseListUiState> = _listUiState.asStateFlow()

    private val _detailUiState = MutableStateFlow(CaseDetailUiState())
    val detailUiState: StateFlow<CaseDetailUiState> = _detailUiState.asStateFlow()

    private var allCasesLoaded: List<CaseEntity> = emptyList()

    init {
        refreshCasesList()
    }

    private fun refreshCasesList() {
        viewModelScope.launch {
            repository.allCases.collect { listaDeCasos ->
                allCasesLoaded = listaDeCasos
                applyFilters()
            }
        }
    }

    private fun applyFilters() {
        val query = _listUiState.value.searchQuery
        val filter = _listUiState.value.selectedStatusFilter

        var filtered = allCasesLoaded

        if (query.isNotBlank()) {
            filtered = filtered.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.description.contains(query, ignoreCase = true)
            }
        }

        if (filter != "Todos") {
            filtered = filtered.filter { it.status.equals(filter, ignoreCase = true) }
        }

        _listUiState.update { it.copy(cases = filtered) }
    }

    fun onSearchQueryChange(newQuery: String) {
        _listUiState.update { it.copy(searchQuery = newQuery) }
        applyFilters()
    }

    fun onStatusFilterChange(newFilter: String) {
        _listUiState.update { it.copy(selectedStatusFilter = newFilter) }
        applyFilters()
    }

    fun selectCase(caseId: Int) {
        val caso = allCasesLoaded.find { it.id == caseId }
        _detailUiState.update { it.copy(selectedCase = caso) }
        if (caso != null) {
            loadCaseDetails(caso.id)
        }
    }

    private fun loadCaseDetails(caseId: Int) {
        viewModelScope.launch {
            repository.getFindingsByCase(caseId).collect { findingsList ->
                repository.getEvidencesByCase(caseId).collect { evidencesList ->
                    _detailUiState.update {
                        it.copy(findings = findingsList, evidences = evidencesList)
                    }
                }
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

    fun addEvidence(caseId: Int, name: String, description: String, imageUri: String? = null, witnessNotes: String? = null, audioPath: String? = null) {
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

// Factoría oficial integrada fuera de la clase principal
class CaseViewModelFactory(private val repository: CaseRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CaseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CaseViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
