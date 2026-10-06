package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CampusVisit
import kotlinx.coroutines.flow.Flow

@Dao
interface CampusVisitDao {
    @Query("SELECT * FROM campus_visits ORDER BY visitDate ASC")
    fun getAllVisits(): Flow<List<CampusVisit>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVisit(visit: CampusVisit): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(visits: List<CampusVisit>)

    @Update
    suspend fun updateVisit(visit: CampusVisit)
}
