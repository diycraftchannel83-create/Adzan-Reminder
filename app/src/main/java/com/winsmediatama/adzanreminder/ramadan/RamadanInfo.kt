package com.winsmediatama.adzanreminder.ramadan

import com.winsmediatama.adzanreminder.prayer.PrayerSchedule
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class RamadanDailyInfo(val isRamadan:Boolean,val dayNumber:Int?,val daysUntilRamadan:Long?,val imsakTime:String?,val iftarTime:String?)

object RamadanInfo{
 // Ramadan dates are intentionally supplied as a calendar table by a later verified data update.
 // This avoids presenting an unverified astronomical estimate as an official Hijri date.
 fun forDate(date:LocalDate,ramadanStart:LocalDate?,schedule:PrayerSchedule,imsakMinutes:Int=10):RamadanDailyInfo{
  if(ramadanStart==null)return RamadanDailyInfo(false,null,null,schedule.fajr?.minusMinutes(imsakMinutes.toLong())?.toLocalTime()?.toString(),schedule.maghrib?.toLocalTime()?.toString())
  val day=ChronoUnit.DAYS.between(ramadanStart,date)
  return RamadanDailyInfo(day in 0..29,if(day in 0..29)day.toInt()+1 else null,if(date.isBefore(ramadanStart))day else null,
   schedule.fajr?.minusMinutes(imsakMinutes.toLong())?.toLocalTime()?.toString(),schedule.maghrib?.toLocalTime()?.toString())
 }
}
