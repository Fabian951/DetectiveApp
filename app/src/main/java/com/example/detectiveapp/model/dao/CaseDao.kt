package com.example.detectiveapp.model.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.detectiveapp.model.entity.CaseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CaseDao {
    @Query("SELECT * FROM cases ORDER BY id DESC")
    fun getAllCases(): Flow<List<CaseEntity>>

    @Query("SELECT * FROM cases WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' ORDER BY id DESC")
    fun searchCases(query: String): Flow<List<CaseEntity>>

    @Query("SELECT * FROM cases WHERE status = :status ORDER BY id DESC")
    fun getCasesByStatus(status: String): Flow<List<CaseEntity>>

    @Query("SELECT * FROM cases WHERE id = :caseId")
    fun getCaseById(caseId: Int): Flow<CaseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCase(caseEntity: CaseEntity): Long

    @Update
    suspend fun updateCase(caseEntity: CaseEntity)

    @Delete
    suspend fun deleteCase(caseEntity: CaseEntity)
}