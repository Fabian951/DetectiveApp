package com.example.detectiveapp.viewmodel

import com.example.detectiveapp.model.entity.CaseEntity
import com.example.detectiveapp.model.entity.EvidenceEntity
import com.example.detectiveapp.model.entity.FindingEntity

data class CaseListUiState(
    val cases: List<CaseEntity> = emptyList(),
    val searchQuery: String = "",
    val selectedStatusFilter: String = "Todos",
    val isLoading: Boolean = false
)

data class CaseDetailUiState(
    val selectedCase: CaseEntity? = null,
    val findings: List<FindingEntity> = emptyList(),
    val evidences: List<EvidenceEntity> = emptyList()
)