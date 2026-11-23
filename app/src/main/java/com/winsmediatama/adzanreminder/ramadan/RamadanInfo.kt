package com.winsmediatama.adzanreminder.ramadan

import com.winsmediatama.adzanreminder.prayer.PrayerSchedule
import java.time.LocalDate
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoUnit

data class RamadanDailyInfo(val isRamadan:Boolean,val dayNumber:Int?,val daysUntilRamadan:Long?,val imsakTime:String?,val iftarTime:String?,val ramadanStart:LocalDate?)

object RamadanInfo{
 /** Uses Android's built-in Hijrah calendar data as a date estimate; local sighting may differ. */
 fun upcomingRamadanStart(date:LocalDate):LocalDate{
  val hijri=HijrahDate.from(date)
  val year=hijri.get(java.time.temporal.ChronoField.YEAR)
  val thisStart=HijrahDate.of(year,9,1).let{LocalDate.ofEpochDay(it.toEpochDay())}
  return if(date.isAfter(thisStart.plusDays(29))) {
   HijrahDate.of(year+1,9,1).let{LocalDate.ofEpochDay(it.toEpochDay())}
  } else thisStart
 }
 fun forDate(date:LocalDate,ramadanStart:LocalDate?,schedule:PrayerSchedule,imsakMinutes:Int=10):RamadanDailyInfo{
  val start=ramadanStart?:upcomingRamadanStart(date)
  val day=ChronoUnit.DAYS.between(start,date)
  val inRamadan=day in 0..29
  val daysUntil=if(date.isBefore(start))ChronoUnit.DAYS.between(date,start) else null
  return RamadanDailyInfo(inRamadan,if(inRamadan)day.toInt()+1 else null,daysUntil,
   schedule.fajr?.minusMinutes(imsakMinutes.toLong())?.toLocalTime()?.toString(),
   schedule.maghrib?.toLocalTime()?.toString(),start)
 }
}
