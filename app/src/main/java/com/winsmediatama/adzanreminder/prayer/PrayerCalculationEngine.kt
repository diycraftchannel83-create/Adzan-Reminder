package com.winsmediatama.adzanreminder.prayer

import java.time.*
import kotlin.math.*

class PrayerCalculationEngine {
    fun calculate(
        date: LocalDate,
        location: PrayerLocation,
        config: PrayerCalculationConfig = PrayerCalculationConfig()
    ): PrayerSchedule {
        val solar = SolarPositionCalculator.at(date, 12.0)
        val decl = solar.declination
        val eq = solar.equationOfTimeMinutes
        val lat = Math.toRadians(location.latitude)

        fun solarEvent(altitude: Double, beforeNoon: Boolean): ZonedDateTime? {
            val h = SolarPositionCalculator.hourAngle(location.latitude, decl, altitude) ?: return null
            val utcMinutes = 720.0 - eq - location.longitude * 4.0 +
                (if (beforeNoon) -h * 4.0 else h * 4.0)
            return utcMinutesToLocal(date, utcMinutes, location.zoneId)
        }

        val fajr = solarEvent(-config.method.fajrAngle, true)
            ?.plusMinutes(config.fajrAdjustmentMinutes.toLong() - config.ihtiyatMinutes.toLong())
        val sunriseAltitude = -0.833 - (0.0347 * sqrt(max(0.0, location.elevationMeters)) / 60.0)
        val sunrise = solarEvent(sunriseAltitude, true)?.plusMinutes(config.sunriseAdjustmentMinutes.toLong())
        val dhuhrUtcMinutes = 720.0 - eq - location.longitude * 4.0
        val dhuhr = utcMinutesToLocal(date, dhuhrUtcMinutes, location.zoneId)
            ?.plusMinutes(config.dhuhrAdjustmentMinutes.toLong() + config.ihtiyatMinutes.toLong())
        val asrAltitude = Math.toDegrees(atan(1.0 / (config.asrMadhab.shadowFactor + tan(abs(lat - decl)))))
        val asr = solarEvent(asrAltitude, false)?.plusMinutes(config.asrAdjustmentMinutes.toLong() + config.ihtiyatMinutes.toLong())
        val maghrib = solarEvent(sunriseAltitude, false)?.plusMinutes(config.maghribAdjustmentMinutes.toLong() + config.ihtiyatMinutes.toLong())
        val isha = if (config.method == PrayerMethod.UMM_AL_QURA) {
            maghrib?.plusMinutes(90L + config.ishaAdjustmentMinutes.toLong())
        } else {
            solarEvent(-config.method.ishaAngle, false)
                ?.plusMinutes(config.ishaAdjustmentMinutes.toLong() + config.ihtiyatMinutes.toLong())
        }
        val imsak = fajr?.minusMinutes(10)
        return PrayerSchedule(date, location, fajr, sunrise, dhuhr, asr, maghrib, isha, imsak)
    }

    private fun utcMinutesToLocal(date: LocalDate, utcMinutes: Double, zone: ZoneId): ZonedDateTime? {
        if (!utcMinutes.isFinite()) return null
        val seconds = (utcMinutes * 60.0).roundToLong()
        return date.atStartOfDay(ZoneOffset.UTC).plusSeconds(seconds).withZoneSameInstant(zone)
    }
}
