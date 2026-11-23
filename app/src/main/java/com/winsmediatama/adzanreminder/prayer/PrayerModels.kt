package com.winsmediatama.adzanreminder.prayer

import java.time.LocalDate
import java.time.ZoneId

enum class PrayerMethod(val fajrAngle: Double, val ishaAngle: Double) {
    KEMENAG_INDONESIA(20.0, 18.0),
    MUSLIM_WORLD_LEAGUE(18.0, 17.0),
    UMM_AL_QURA(18.5, 0.0),
    EGYPTIAN(19.5, 17.5)
}

enum class AsrMadhab(val shadowFactor: Double) {
    SHAFI(1.0),
    HANAFI(2.0)
}

data class PrayerCalculationConfig(
    val method: PrayerMethod = PrayerMethod.KEMENAG_INDONESIA,
    val asrMadhab: AsrMadhab = AsrMadhab.SHAFI,
    val fajrAdjustmentMinutes: Int = 0,
    val sunriseAdjustmentMinutes: Int = 0,
    val dhuhrAdjustmentMinutes: Int = 0,
    val asrAdjustmentMinutes: Int = 0,
    val maghribAdjustmentMinutes: Int = 0,
    val ishaAdjustmentMinutes: Int = 0,
    val ihtiyatMinutes: Int = 0,
    val imsakMinutes: Int = 10
)

data class PrayerLocation(
    val latitude: Double,
    val longitude: Double,
    val elevationMeters: Double = 0.0,
    val zoneId: ZoneId,
    val displayName: String = ""
) {
    init {
        require(latitude in -90.0..90.0)
        require(longitude in -180.0..180.0)
        require(elevationMeters >= -500.0)
    }
}

data class PrayerSchedule(
    val date: LocalDate,
    val location: PrayerLocation,
    val fajr: java.time.ZonedDateTime?,
    val sunrise: java.time.ZonedDateTime?,
    val dhuhr: java.time.ZonedDateTime?,
    val asr: java.time.ZonedDateTime?,
    val maghrib: java.time.ZonedDateTime?,
    val isha: java.time.ZonedDateTime?,
    val imsak: java.time.ZonedDateTime? = null
)
