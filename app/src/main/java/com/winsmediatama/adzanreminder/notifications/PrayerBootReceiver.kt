package com.winsmediatama.adzanreminder.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.winsmediatama.adzanreminder.data.PrayerPreferences
import com.winsmediatama.adzanreminder.prayer.*
import java.time.LocalDate
import java.time.ZoneId

class PrayerBootReceiver:BroadcastReceiver(){
 override fun onReceive(context:Context,intent:Intent){
  val supportedActions=setOf(Intent.ACTION_BOOT_COMPLETED,Intent.ACTION_TIME_CHANGED,Intent.ACTION_TIMEZONE_CHANGED,Intent.ACTION_DATE_CHANGED,Intent.ACTION_MY_PACKAGE_REPLACED)
  if(intent.action !in supportedActions)return
  val settings=PrayerPreferences(context).read()
  if(!settings.notificationsEnabled){PrayerAlarmScheduler(context).cancelAll();return}
  val zone=runCatching{ZoneId.of(settings.zoneId)}.getOrElse{ZoneId.systemDefault()}
  val location=PrayerLocation(settings.latitude,settings.longitude,0.0,zone,settings.cityName)
  val config=PrayerCalculationConfig(method=runCatching{PrayerMethod.valueOf(settings.method)}.getOrDefault(PrayerMethod.KEMENAG_INDONESIA),asrMadhab=runCatching{AsrMadhab.valueOf(settings.asrMadhab)}.getOrDefault(AsrMadhab.SHAFI))
  val engine=PrayerCalculationEngine()
  val today=LocalDate.now(zone)
  PrayerAlarmScheduler(context).schedule(listOf(engine.calculate(today,location,config),engine.calculate(today.plusDays(1),location,config)),true)
  val widgetManager=android.appwidget.AppWidgetManager.getInstance(context)
  val widgetIds=widgetManager.getAppWidgetIds(android.content.ComponentName(context,com.winsmediatama.adzanreminder.widget.PrayerWidgetProvider::class.java))
  if(widgetIds.isNotEmpty())com.winsmediatama.adzanreminder.widget.PrayerWidgetProvider().onUpdate(context,widgetManager,widgetIds)
 }
}
