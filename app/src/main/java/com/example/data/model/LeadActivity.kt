package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "lead_activities",
    indices = [Index(value = ["leadId"])]
)
data class LeadActivity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val leadId: Long,
    val authorName: String,
    val authorRole: String,
    val actionType: String, // CREATED, STAGE_CHANGE, NOTE_ADDED, RESCHEDULED, REASSIGNED, CALL_LOGGED, WHATSAPP_LOGGED
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)
