package com.example.database.dao

import androidx.room.*
import com.example.database.entities.ActiveThreat
import kotlinx.coroutines.flow.Flow

@Dao
interface ActiveThreatDao {
    @Query("SELECT * FROM active_threats ORDER BY timestamp DESC")
    fun getAllThreats(): Flow<List<ActiveThreat>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertThreat(threat: ActiveThreat)

    @Query("DELETE FROM active_threats WHERE id = :id")
    suspend fun deleteThreatById(id: Int)

    @Query("DELETE FROM active_threats")
    suspend fun clearAllThreats()
}
