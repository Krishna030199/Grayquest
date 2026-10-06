package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Lead
import kotlinx.coroutines.flow.Flow

@Dao
interface LeadDao {
    @Query("SELECT * FROM leads ORDER BY nextFollowUpDate ASC, updatedAt DESC")
    fun getAllLeads(): Flow<List<Lead>>

    @Query("SELECT * FROM leads WHERE assignedToId = :assignedToId ORDER BY nextFollowUpDate ASC, updatedAt DESC")
    fun getLeadsForAssignee(assignedToId: Long): Flow<List<Lead>>

    @Query("SELECT * FROM leads WHERE id = :id LIMIT 1")
    suspend fun getLeadById(id: Long): Lead?

    @Query("SELECT * FROM leads WHERE phone = :phone LIMIT 1")
    suspend fun getLeadByPhone(phone: String): Lead?

    @Query("SELECT * FROM leads WHERE phone = :phone AND id != :excludeId LIMIT 1")
    suspend fun getLeadByPhoneExcluding(phone: String, excludeId: Long): Lead?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLead(lead: Lead): Long

    @Update
    suspend fun updateLead(lead: Lead)

    @Delete
    suspend fun deleteLead(lead: Lead)
}
