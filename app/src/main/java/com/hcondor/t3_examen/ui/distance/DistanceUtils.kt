package com.hcondor.t3_examen.ui.distance

import kotlin.math.*

object DistanceUtils {

    /**
     * Calcula la distancia entre dos puntos geográficos usando la fórmula de Haversine.
     * @return distancia en metros.
     */
    fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371e3 // Radio de la Tierra en metros
        val phi1 = Math.toRadians(lat1)
        val phi2 = Math.toRadians(lat2)
        val deltaPhi = Math.toRadians(lat2 - lat1)
        val deltaLambda = Math.toRadians(lon2 - lon1)

        val a = sin(deltaPhi / 2).pow(2.0) +
                cos(phi1) * cos(phi2) *
                sin(deltaLambda / 2).pow(2.0)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return R * c
    }
}
