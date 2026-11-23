package com.winsmediatama.adzanreminder.prayer

import java.time.*
import kotlin.math.*

class PrayerCalculationEngine {
    fun calculate(
        date: LocalDate,
        location: PrayerLocation,
        config: PrayerCalculationConfig = PrayerCalculationConfig()
    ): PrayerSchedule {
        val zone = location.zoneId
        val solar = SolarPositionCalculator.at(date, 12.0)
        val decl = solar.declination
        val eq = solar.equationOfTimeMinutes
        val lat = Math.toRadians(location.latitude)

        fun solarEvent(altitude: Double, beforeNoon: Boolean): ZonedDateTime? {
            val h = SolarPositionCalculator.hourAngle(location.latitude, decl, altitude) ?: return null
            val utcMinutes = 720.0 - eq - location.longitude * 4.0 + (if (beforeNoon) -h * 4.0 else h * 4.0)
            return localTime(date, utcMinutes, zone)
        }

        val fajr = solarEvent(-config.method.fajrAngle, true)?.plusMinutes(config.fajrAdjustmentMinutes.toLong() - config.ihtiyatMinutes)
        val sunriseAlt = -0.833 - (0.0347 * sqrt(max(0.0, location.elevationMeters)) / 60.0)
        val sunrise = solarEvent(sunriseAlt, true)?.plusMinutes(config.sunriseAdjustmentMinutes.toLong())
        val dhuhrMinutes = 720.0 - eq - location.longitude * 4.0
        val dhuhr = localTime(date, dhuhrMinutes, zone)?.plusMinutes(config.dhuhrAdjustmentMinutes.toLong() + config.ihtiyatMinutes)
        val asrAlt = -Math.toDegrees(atan(1.0 / (config.asrMadhab.shadowFactor + tan(abs(lat - decl)))))
        val asr = solarEvent(asrAlt, false)?.plusMinutes(config.asrAdjustmentMinutes.toLong() + config.ihtiyatMinutes)
        val maghrib = solarEvent(sunriseAlt, false)?.plusMinutes(config.maghribAdjustmentMinutes.toLong() + config.ihtiyatMinutes)
        val isha = if (config.method == PrayerMethod.UMM_AL_QURA) {
            maghrib?.plusMinutes(90 + config.ishaAdjustmentMinutes)
        } else {
            solarEvent(-config.method.ishaAngle, false)?.plusMinutes(config.ishaAdjustmentMinutes.toLong() + config.ihtiyatMinutes)
        }
        val imsak = fajr?.minusMinutes(10)
        return PrayerSchedule(date, location, fajr, sunrise, dhuhr, asr, maghrib, isha, imsak)
    }

    private fun localTime(date: LocalDate, utcMinutes: Double, zone: ZoneId): ZonedDateTime? {
        if (!utcMinutes.isFinite()) return null
        val normalized = ((utcMinutes % 1440.0) + 1440.0) % 1440.0
        val hours = (normalized / 60.0).toInt()
        val minutes = (normalized - hours * 60).toInt()
        val seconds = ((normalized - hours * 60 - minutes) * 60).roundToInt().coerceIn(0, 59)
        return LocalDateTime.of(date, LocalTime.of(hours, minutes, seconds)).atZone(zone)
    }
}
