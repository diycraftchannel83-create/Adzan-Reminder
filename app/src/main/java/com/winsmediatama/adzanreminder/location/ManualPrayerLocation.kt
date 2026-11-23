package com.winsmediatama.adzanreminder.location

import java.time.ZoneId

/** Manual location entry for when GPS permission or signal is unavailable. */
data class ManualPrayerLocation(
    val cityName: String,
    val latitude: Double,
    val longitude: Double,
    val zoneId: ZoneId
) {
    init {
        require(cityName.isNotBlank())
        require(latitude in -90.0..90.0)
        require(longitude in -180.0..180.0)
    }
}
