package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Institution
import kotlinx.coroutines.flow.Flow

@Dao
interface InstitutionDao {
    @Query("SELECT * FROM institutions WHERE active = 1 ORDER BY name ASC")
    fun getAllInstitutions(): Flow<List<Institution>>

    @Query("SELECT * FROM institutions WHERE id = :id LIMIT 1")
    suspend fun getInstitutionById(id: Long): Institution?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInstitution(institution: Institution): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(institutions: List<Institution>)

    @Update
    suspend fun updateInstitution(institution: Institution)
}
