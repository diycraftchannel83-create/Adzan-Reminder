package com.winsmediatama.adzanreminder.ramadan

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField

class RamadanInfoTest {
    @Test
    fun dateDuringRamadanReturnsCurrentRamadanStart() {
        val hijri = HijrahDate.from(LocalDate.of(2026, 3, 1))
        val year = hijri.get(ChronoField.YEAR)
        val start = HijrahDate.of(year, 9, 1).let { LocalDate.ofEpochDay(it.toEpochDay()) }
        val currentRamadanDay = start.plusDays(10)
        assertEquals(start, RamadanInfo.upcomingRamadanStart(currentRamadanDay))
    }

    @Test
    fun dateAfterRamadanReturnsNextRamadanStart() {
        val hijri = HijrahDate.from(LocalDate.of(2026, 3, 1))
        val year = hijri.get(ChronoField.YEAR)
        val start = HijrahDate.of(year, 9, 1).let { LocalDate.ofEpochDay(it.toEpochDay()) }
        val ramadanLength = HijrahDate.from(start).range(ChronoField.DAY_OF_MONTH).maximum.toLong()
        val afterRamadan = start.plusDays(ramadanLength)
        val dateHijri = HijrahDate.from(afterRamadan)
        val expectedYear = dateHijri.get(ChronoField.YEAR) + if (dateHijri.get(ChronoField.MONTH_OF_YEAR) >= 9) 1 else 0
        val expected = HijrahDate.of(expectedYear, 9, 1).let { LocalDate.ofEpochDay(it.toEpochDay()) }
        assertEquals(expected, RamadanInfo.upcomingRamadanStart(afterRamadan))
    }
}
