package com.example.detectiveapp.model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.detectiveapp.model.entity.FindingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FindingDao {
    @Query("SELECT * FROM findings WHERE caseId = :caseId ORDER BY id DESC")
    fun getFindingsByCase(caseId: Int): Flow<List<FindingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFinding(finding: FindingEntity)
}