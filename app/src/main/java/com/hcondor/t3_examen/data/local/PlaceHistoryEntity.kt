package com.hcondor.t3_examen.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "place_history")
data class PlaceHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val date: String,
    val note: String,
    val latitude: Double,
    val longitude: Double
)