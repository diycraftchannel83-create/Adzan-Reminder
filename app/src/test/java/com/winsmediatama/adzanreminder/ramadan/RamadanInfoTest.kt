package com.winsmediatama.adzanreminder.ramadan

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.time.LocalDate
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField

class RamadanInfoTest {
    @Test
    fun startDateIsConsistentForDatesInsideHijriRamadan() {
        val start = HijrahDate.from(LocalDate.of(2026, 3, 1))
            .let { HijrahDate.of(it.get(ChronoField.YEAR), 9, 1) }
            .let { LocalDate.ofEpochDay(it.toEpochDay()) }
        assertEquals(start, RamadanInfo.upcomingRamadanStart(start.plusDays(28)))
        assertEquals(start, RamadanInfo.upcomingRamadanStart(start.plusDays(29)))
    }

    @Test
    fun nextStartMovesAfterHijriRamadanMonthEnds() {
        val start = HijrahDate.from(LocalDate.of(2026, 3, 1))
            .let { HijrahDate.of(it.get(ChronoField.YEAR), 9, 1) }
            .let { LocalDate.ofEpochDay(it.toEpochDay()) }
        val nextMonthDate = start.plusDays(30)
        val expectedNext = HijrahDate.from(nextMonthDate)
            .let { HijrahDate.of(it.get(ChronoField.YEAR), 9, 1) }
            .let { LocalDate.ofEpochDay(it.toEpochDay()) }
        assertEquals(expectedNext, RamadanInfo.upcomingRamadanStart(nextMonthDate))
        assertFalse(RamadanInfo.forDate(nextMonthDate, start, dummySchedule(), 10).isRamadan)
    }

    private fun dummySchedule(): com.winsmediatama.adzanreminder.prayer.PrayerSchedule {
        val zone = java.time.ZoneId.of("Asia/Jakarta")
        val location = com.winsmediatama.adzanreminder.prayer.PrayerLocation(-7.0, 110.0, 0.0, zone)
        val fajr = LocalDate.of(2026, 3, 1).atTime(4, 30).atZone(zone)
        return com.winsmediatama.adzanreminder.prayer.PrayerSchedule(
            fajr.toLocalDate(), location, fajr, fajr.plusHours(1), fajr.plusHours(7),
            fajr.plusHours(9), fajr.plusHours(13), fajr.plusHours(14), fajr.minusMinutes(10)
        )
    }
}
