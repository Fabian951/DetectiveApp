package com.example.detectiveapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.detectiveapp.model.CaseRepository
import com.example.detectiveapp.model.entity.CaseEntity
import com.example.detectiveapp.model.entity.EvidenceEntity
import com.example.detectiveapp.model.entity.FindingEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class CaseViewModel(
    private val repository: CaseRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedStatusFilter = MutableStateFlow("Todos")
    val selectedStatusFilter: StateFlow<String> = _selectedStatusFilter

    private val _selectedCaseId = MutableStateFlow<Int?>(null)

    val listUiState: StateFlow<CaseListUiState> = combine(
        _searchQuery,
        _selectedStatusFilter
    ) { query, filter ->
        Pair(query, filter)
    }.flatMapLatest { (query, filter) ->
        when {
            query.isNotEmpty() -> repository.searchCases(query)
            filter != "Todos" -> repository.getCasesByStatus(filter)
            else -> repository.allCases
        }
    }.combine(_searchQuery) { cases, query ->
        CaseListUiState(
            cases = cases,
            searchQuery = query,
            selectedStatusFilter = _selectedStatusFilter.value
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CaseListUiState(isLoading = true)
    )

    val detailUiState: StateFlow<CaseDetailUiState> = _selectedCaseId.flatMapLatest { caseId ->
        if (caseId == null) {
            flowOf(CaseDetailUiState())
        } else {
            combine(
                repository.getCaseById(caseId),
                repository.getFindingsByCase(caseId),
                repository.getEvidencesByCase(caseId)
            ) { case, findings, evidences ->
                CaseDetailUiState(
                    selectedCase = case,
                    findings = findings,
                    evidences = evidences
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CaseDetailUiState()
    )

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onStatusFilterChange(newFilter: String) {
        _selectedStatusFilter.value = newFilter
    }

    fun selectCase(caseId: Int?) {
        _selectedCaseId.value = caseId
    }

    fun createCase(title: String, description: String, date: String, status: String) {
        viewModelScope.launch {
            val newCase = CaseEntity(
                title = title,
                description = description,
                date = date,
                status = status
            )
            repository.insertCase(newCase)
        }
    }

    fun updateCase(caseEntity: CaseEntity) {
        viewModelScope.launch {
            repository.updateCase(caseEntity)
        }
    }

    fun deleteCase(caseEntity: CaseEntity) {
        viewModelScope.launch {
            repository.deleteCase(caseEntity)
            if (_selectedCaseId.value == caseEntity.id) {
                _selectedCaseId.value = null
            }
        }
    }

    fun updateCaseStatus(caseEntity: CaseEntity, newStatus: String) {
        viewModelScope.launch {
            val updatedCase = caseEntity.copy(status = newStatus)
            repository.updateCase(updatedCase)
        }
    }

    fun addFinding(caseId: Int, description: String, date: String) {
        viewModelScope.launch {
            val finding = FindingEntity(
                caseId = caseId,
                description = description,
                date = date
            )
            repository.insertFinding(finding)
        }
    }

    fun addEvidence(caseId: Int, name: String, description: String, imageUri: String? = null) {
        viewModelScope.launch {
            val evidence = EvidenceEntity(
                caseId = caseId,
                name = name,
                description = description,
                imageUri = imageUri
            )
            repository.insertEvidence(evidence)
        }
    }
}

class CaseViewModelFactory(
    private val repository: CaseRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CaseViewModel::class.java)) {
            return CaseViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}