package com.winsmediatama.adzanreminder.location

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.ZoneId

class CoordinateTimeZoneResolverTest {
    @Test
    fun resolvesIndonesiaLongitudeBands() {
        assertEquals(ZoneId.of("Asia/Jakarta"), CoordinateTimeZoneResolver.resolveIndonesia(-7.0, 110.0))
        assertEquals(ZoneId.of("Asia/Makassar"), CoordinateTimeZoneResolver.resolveIndonesia(-5.0, 125.0))
        assertEquals(ZoneId.of("Asia/Jayapura"), CoordinateTimeZoneResolver.resolveIndonesia(-2.0, 140.0))
    }

    @Test
    fun rejectsCoordinatesOutsideApproximateIndonesiaBounds() {
        assertNull(CoordinateTimeZoneResolver.resolveIndonesia(40.0, -74.0))
        assertNull(CoordinateTimeZoneResolver.resolveIndonesia(10.0, 100.0))
    }

    @Test
    fun bandBoundariesAreDeterministic() {
        assertEquals(ZoneId.of("Asia/Jakarta"), CoordinateTimeZoneResolver.resolveIndonesia(0.0, 116.999))
        assertEquals(ZoneId.of("Asia/Makassar"), CoordinateTimeZoneResolver.resolveIndonesia(0.0, 117.0))
        assertEquals(ZoneId.of("Asia/Makassar"), CoordinateTimeZoneResolver.resolveIndonesia(0.0, 134.999))
        assertEquals(ZoneId.of("Asia/Jayapura"), CoordinateTimeZoneResolver.resolveIndonesia(0.0, 135.0))
    }
}
