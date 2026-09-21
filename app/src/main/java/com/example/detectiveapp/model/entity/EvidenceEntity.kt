package com.example.detectiveapp.model.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "evidences",
    foreignKeys = [ForeignKey(
        entity = CaseEntity::class,
        parentColumns = ["id"],
        childColumns = ["caseId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class EvidenceEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val caseId: Int,
    val name: String,
    val description: String,
    val imageUri: String? = null,
    // NUEVAS COLUMNAS REQUERIDAS PARA TESTIGOS
    val witnessNotes: String? = null,
    val audioPath: String? = null
)
