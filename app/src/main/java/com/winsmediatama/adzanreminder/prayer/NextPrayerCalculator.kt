package com.winsmediatama.adzanreminder.prayer

import java.time.ZonedDateTime

data class NextPrayer(
    val name: String,
    val time: ZonedDateTime,
    val remainingMillis: Long
)

object NextPrayerCalculator {
    fun findNext(schedule: PrayerSchedule, now: ZonedDateTime): NextPrayer? {
        val prayers = listOf(
            "Subuh" to schedule.fajr,
            "Zuhur" to schedule.dhuhr,
            "Asar" to schedule.asr,
            "Magrib" to schedule.maghrib,
            "Isya" to schedule.isha
        ).mapNotNull { (name, time) -> time?.let { name to it } }
        val next = prayers.firstOrNull { it.second.toInstant().isAfter(now.toInstant()) }
            ?: return null
        return NextPrayer(next.first, next.second, next.second.toInstant().toEpochMilli() - now.toInstant().toEpochMilli())
    }
}
