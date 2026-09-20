package com.example.detectiveapp.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "evidences")
data class EvidenceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val caseId: Int,
    val name: String,
    val description: String,
    val imageUri: String? = null
)