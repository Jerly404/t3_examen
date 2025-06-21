package com.hcondor.t3_examen.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PlaceHistoryDao {
    @Insert
    suspend fun insert(place: PlaceHistoryEntity)

    @Query("SELECT * FROM place_history")
    suspend fun getAll(): List<PlaceHistoryEntity>
}