package com.example.detectiveapp.model

import com.example.detectiveapp.model.dao.CaseDao
import com.example.detectiveapp.model.dao.EvidenceDao
import com.example.detectiveapp.model.dao.FindingDao
import com.example.detectiveapp.model.entity.CaseEntity
import com.example.detectiveapp.model.entity.EvidenceEntity
import com.example.detectiveapp.model.entity.FindingEntity
import kotlinx.coroutines.flow.Flow

class CaseRepository(
    private val caseDao: CaseDao,
    private val findingDao: FindingDao,
    private val evidenceDao: EvidenceDao
) {
    val allCases: Flow<List<CaseEntity>> = caseDao.getAllCases()

    fun searchCases(query: String): Flow<List<CaseEntity>> = caseDao.searchCases(query)

    fun getCasesByStatus(status: String): Flow<List<CaseEntity>> = caseDao.getCasesByStatus(status)

    fun getCaseById(caseId: Int): Flow<CaseEntity> = caseDao.getCaseById(caseId)

    fun getFindingsByCase(caseId: Int): Flow<List<FindingEntity>> = findingDao.getFindingsByCase(caseId)

    fun getEvidencesByCase(caseId: Int): Flow<List<EvidenceEntity>> = evidenceDao.getEvidencesByCase(caseId)

    suspend fun insertCase(caseEntity: CaseEntity): Long = caseDao.insertCase(caseEntity)

    suspend fun updateCase(caseEntity: CaseEntity) = caseDao.updateCase(caseEntity)

    suspend fun deleteCase(caseEntity: CaseEntity) = caseDao.deleteCase(caseEntity)

    suspend fun insertFinding(finding: FindingEntity) = findingDao.insertFinding(finding)

    suspend fun insertEvidence(evidence: EvidenceEntity) = evidenceDao.insertEvidence(evidence)
}