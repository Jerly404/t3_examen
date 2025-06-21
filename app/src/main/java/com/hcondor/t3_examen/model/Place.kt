package com.hcondor.t3_examen.model

data class Place(
    val id: Int = 0,
    val name: String,
    val date: String,
    val note: String,
    val latitude: Double,
    val longitude: Double
)
