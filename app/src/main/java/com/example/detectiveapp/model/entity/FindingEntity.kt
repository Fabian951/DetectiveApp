package com.example.detectiveapp.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "findings")
data class FindingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val caseId: Int,
    val description: String,
    val date: String
)