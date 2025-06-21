package com.hcondor.t3_examen.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(place: PlaceEntity): Long

    @Update
    suspend fun update(place: PlaceEntity)

    @Delete
    suspend fun delete(place: PlaceEntity)

    @Query("SELECT * FROM places ORDER BY id DESC")
    fun getAllPlaces(): Flow<List<PlaceEntity>>

    @Query("SELECT * FROM places ORDER BY id DESC")
    suspend fun getAllNow(): List<PlaceEntity>

    @Query("SELECT * FROM places WHERE id = :id")
    suspend fun getPlaceById(id: Int): PlaceEntity?

    @Query("DELETE FROM places")
    suspend fun deleteAll()
}
