package com.winsmediatama.adzanreminder.location

import java.time.ZoneId

/**
 * Offline coarse resolver for Indonesia only. It uses longitude bands as a fallback,
 * not geographic polygons; borders and islands near zone edges must be checked manually.
 */
object CoordinateTimeZoneResolver {
 fun resolveIndonesia(latitude:Double,longitude:Double):ZoneId?{
  if(latitude !in -11.0..6.5 || longitude !in 94.0..142.0) return null
  return when {
   longitude < 117.0 -> ZoneId.of("Asia/Jakarta")
   longitude < 135.0 -> ZoneId.of("Asia/Makassar")
   else -> ZoneId.of("Asia/Jayapura")
  }
 }
}
