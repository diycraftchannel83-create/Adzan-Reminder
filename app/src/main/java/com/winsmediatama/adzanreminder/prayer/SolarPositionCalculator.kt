package com.winsmediatama.adzanreminder.prayer

import kotlin.math.*

/**
 * Compact NOAA-style solar position approximation.
 * Designed for prayer-time calculations; official schedule parity must be validated.
 */
internal object SolarPositionCalculator {
    data class SolarData(val declination: Double, val equationOfTimeMinutes: Double)

    fun at(date: java.time.LocalDate, hourUtc: Double): SolarData {
        val dayOfYear = date.dayOfYear
        val gamma = 2.0 * PI / 365.0 * (dayOfYear - 1 + (hourUtc - 12.0) / 24.0)
        val eqTime = 229.18 * (0.000075 + 0.001868 * cos(gamma) - 0.032077 * sin(gamma) -
            0.014615 * cos(2 * gamma) - 0.040849 * sin(2 * gamma))
        val decl = 0.006918 - 0.399912 * cos(gamma) + 0.070257 * sin(gamma) -
            0.006758 * cos(2 * gamma) + 0.000907 * sin(2 * gamma) -
            0.002697 * cos(3 * gamma) + 0.00148 * sin(3 * gamma)
        return SolarData(decl, eqTime)
    }

    fun hourAngle(latitude: Double, declination: Double, altitudeDegrees: Double): Double? {
        val lat = Math.toRadians(latitude)
        val alt = Math.toRadians(altitudeDegrees)
        val cosH = (sin(alt) - sin(lat) * sin(declination)) / (cos(lat) * cos(declination))
        if (cosH !in -1.0..1.0) return null
        return Math.toDegrees(acos(cosH))
    }
}
