package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "campus_visits")
data class CampusVisit(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val institutionName: String,
    val city: String,
    val visitDate: Long,
    val executiveName: String,
    val purpose: String, // Campus Loan Desk, Principal / Dean Meeting, Admission Info Session
    val outcomeNotes: String = "",
    val status: String = "Scheduled" // Scheduled, Completed, Cancelled
)
