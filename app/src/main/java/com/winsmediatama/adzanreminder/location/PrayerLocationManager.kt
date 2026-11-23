package com.winsmediatama.adzanreminder.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import java.time.ZoneId

data class ResolvedPrayerLocation(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float?,
    val displayName: String,
    val zoneId: ZoneId
)

class PrayerLocationManager(context: Context) {
    private val client = LocationServices.getFusedLocationProviderClient(context.applicationContext)

    @SuppressLint("MissingPermission")
    fun requestCurrentLocation(
        onResult: (ResolvedPrayerLocation?) -> Unit
    ) {
        val cancellation = CancellationTokenSource()
        client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellation.token)
            .addOnSuccessListener { location: Location? ->
                onResult(location?.let {
                    ResolvedPrayerLocation(
                        latitude = it.latitude,
                        longitude = it.longitude,
                        accuracyMeters = if (it.hasAccuracy()) it.accuracy else null,
                        displayName = "Lokasi GPS",
                        // Device timezone is a fallback. A true coordinate-to-timezone
                        // lookup should be added and validated for international travel.
                        zoneId = ZoneId.systemDefault()
                    )
                })
            }
            .addOnFailureListener { onResult(null) }
    }
}
