package com.winsmediatama.adzanreminder.prayer

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class PrayerCalculationEngineTest {
    private val engine = PrayerCalculationEngine()
    private val date = LocalDate.of(2026, 11, 23)
    private val tasik = PrayerLocation(-7.3274, 108.2207, 0.0, ZoneId.of("Asia/Jakarta"), "Tasikmalaya")

    @Test
    fun tasikScheduleHasChronologicalSolarEvents() {
        val schedule = engine.calculate(date, tasik, PrayerCalculationConfig(method = PrayerMethod.KEMENAG_INDONESIA))
        val events = listOf(schedule.fajr, schedule.sunrise, schedule.dhuhr, schedule.asr, schedule.maghrib, schedule.isha)
        events.forEach { assertNotNull("Expected prayer event to be calculated", it) }
        for (i in 0 until events.size - 1) {
            assertTrue("Prayer events should be chronological", events[i]!!.isBefore(events[i + 1]))
        }
    }

    @Test
    fun hanafiAsrShouldBeLaterThanShafiiAtSameLocation() {
        val shafii = engine.calculate(date, tasik, PrayerCalculationConfig(asrMadhab = AsrMadhab.SHAFI)).asr
        val hanafi = engine.calculate(date, tasik, PrayerCalculationConfig(asrMadhab = AsrMadhab.HANAFI)).asr
        assertNotNull(shafii)
        assertNotNull(hanafi)
        assertTrue("Hanafi Asr should occur later than Shafii Asr", hanafi!!.isAfter(shafii!!))
    }

    @Test
    fun calculationMethodsProduceDifferentFajrAndIsha() {
        val kemenag = engine.calculate(date, tasik, PrayerCalculationConfig(method = PrayerMethod.KEMENAG_INDONESIA))
        val mwl = engine.calculate(date, tasik, PrayerCalculationConfig(method = PrayerMethod.MUSLIM_WORLD_LEAGUE))
        assertNotNull(kemenag.fajr)
        assertNotNull(mwl.fajr)
        assertNotNull(kemenag.isha)
        assertNotNull(mwl.isha)
        assertTrue("Calculation methods should differ in at least one angle-based prayer", 
            kemenag.fajr != mwl.fajr || kemenag.isha != mwl.isha)
    }

    @Test
    fun nextPrayerFindsUpcomingEventAcrossTodayAndTomorrow() {
        val today = engine.calculate(date, tasik)
        val tomorrow = engine.calculate(date.plusDays(1), tasik)
        val now = date.atTime(23, 0).atZone(tasik.zoneId)
        val next = NextPrayerCalculator.findNext(today, tomorrow, now)
        assertNotNull(next)
        val upcoming = next ?: error("Expected next prayer to exist")
        assertTrue("Next event should be in the future", upcoming.time.isAfter(now))
        assertTrue("After late evening, next prayer should be tomorrow's Subuh", upcoming.name == "Subuh")
    }
    @Test
    fun elevationMovesSunriseEarlierAndMaghribLater() {
        val seaLevel = engine.calculate(date, tasik.copy(elevationMeters = 0.0))
        val elevated = engine.calculate(date, tasik.copy(elevationMeters = 1000.0))
        assertNotNull(seaLevel.sunrise)
        assertNotNull(elevated.sunrise)
        assertNotNull(seaLevel.maghrib)
        assertNotNull(elevated.maghrib)
        assertTrue("Higher elevation should advance sunrise", elevated.sunrise!!.isBefore(seaLevel.sunrise!!))
        assertTrue("Higher elevation should delay sunset/Maghrib", elevated.maghrib!!.isAfter(seaLevel.maghrib!!))
    }

}
