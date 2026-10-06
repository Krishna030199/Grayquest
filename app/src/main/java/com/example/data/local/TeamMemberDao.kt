package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.TeamMember
import kotlinx.coroutines.flow.Flow

@Dao
interface TeamMemberDao {
    @Query("SELECT * FROM team_members WHERE active = 1 ORDER BY name ASC")
    fun getAllTeamMembers(): Flow<List<TeamMember>>

    @Query("SELECT * FROM team_members WHERE id = :id LIMIT 1")
    suspend fun getMemberById(id: Long): TeamMember?

    @Query("SELECT * FROM team_members WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun getMemberByEmail(email: String): TeamMember?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: TeamMember): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(members: List<TeamMember>)

    @Update
    suspend fun updateMember(member: TeamMember)
}
