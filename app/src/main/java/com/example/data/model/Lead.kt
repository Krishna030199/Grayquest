package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "leads",
    indices = [
        Index(value = ["phone"], unique = true),
        Index(value = ["assignedToId"]),
        Index(value = ["stage"])
    ]
)
data class Lead(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val studentName: String,
    val parentName: String,
    val phone: String,
    val city: String,
    val institutionId: Long,
    val institutionName: String,
    val courseClass: String,
    val loanAmount: Double,
    val source: LeadSource,
    val assignedToId: Long,
    val assignedToName: String,
    val stage: LeadStage = LeadStage.NEW,
    val lostReason: String? = null,
    val nextFollowUpDate: Long = System.currentTimeMillis(), // epoch millis (start of day or time)
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
