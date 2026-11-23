package com.winsmediatama.adzanreminder.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.winsmediatama.adzanreminder.R
import com.winsmediatama.adzanreminder.data.PrayerPreferences
import com.winsmediatama.adzanreminder.prayer.*
import java.time.LocalDate
import java.time.ZoneId

class PrayerWidgetProvider:AppWidgetProvider(){
 override fun onUpdate(context:Context,manager:AppWidgetManager,ids:IntArray){
  val settings=PrayerPreferences(context).read()
  val zone=runCatching{ZoneId.of(settings.zoneId)}.getOrElse{ZoneId.systemDefault()}
  val location=PrayerLocation(settings.latitude,settings.longitude,0.0,zone,settings.cityName)
  val config=PrayerCalculationConfig(method=runCatching{PrayerMethod.valueOf(settings.method)}.getOrDefault(PrayerMethod.KEMENAG_INDONESIA),asrMadhab=runCatching{AsrMadhab.valueOf(settings.asrMadhab)}.getOrDefault(AsrMadhab.SHAFI))
  val engine=PrayerCalculationEngine()
  val date=LocalDate.now(zone)
  val schedule=engine.calculate(date,location,config)
  val tomorrow=engine.calculate(date.plusDays(1),location,config)
  val now=java.time.ZonedDateTime.now(zone)
  val next=NextPrayerCalculator.findNext(schedule,tomorrow,now)
  val views=RemoteViews(context.packageName,R.layout.prayer_widget)
  views.setTextViewText(R.id.widget_location,settings.cityName)
  views.setTextViewText(R.id.widget_date,LocalDate.now(zone).toString())
  views.setTextViewText(R.id.widget_next,next?.let { "Salat berikutnya: ${it.name} ${PrayerTimeFormatter.format(it.time)} • ${PrayerTimeFormatter.countdown(it.time,now)}" } ?: "Salat berikutnya: --")
  views.setTextViewText(R.id.widget_prayers,"Subuh ${schedule.fajr?.toLocalTime()}  •  Zuhur ${schedule.dhuhr?.toLocalTime()}\nAsar ${schedule.asr?.toLocalTime()}  •  Magrib ${schedule.maghrib?.toLocalTime()}\nIsya ${schedule.isha?.toLocalTime()}")
  ids.forEach{manager.updateAppWidget(it,views)}
 }
}
