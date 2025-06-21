package com.hcondor.t3_examen.ui.area

import kotlin.math.abs

data class LatLng(val latitude: Double, val longitude: Double)

object AreaUtils {

    /**
     * Calcula el área de un polígono geográfico aproximado (en m²)
     * usando la fórmula de Shoelace.
     */
    fun calculateArea(points: List<LatLng>): Double {
        if (points.size < 3) return 0.0

        var sum = 0.0
        for (i in points.indices) {
            val j = (i + 1) % points.size
            sum += (points[i].longitude * points[j].latitude) -
                    (points[j].longitude * points[i].latitude)
        }

        // Conversión aproximada: 1 grado ≈ 111,319.9 m
        val areaDegrees = abs(sum) / 2.0
        val meterConversion = 111_319.9
        return areaDegrees * meterConversion * meterConversion
    }
}
