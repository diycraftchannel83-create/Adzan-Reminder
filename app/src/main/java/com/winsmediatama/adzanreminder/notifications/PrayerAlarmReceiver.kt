package com.winsmediatama.adzanreminder.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.winsmediatama.adzanreminder.data.PrayerPreferences
import com.winsmediatama.adzanreminder.prayer.*
import java.time.LocalDate
import java.time.ZoneId

class PrayerAlarmReceiver:BroadcastReceiver(){
 override fun onReceive(context:Context,intent:Intent){
  val name=intent.getStringExtra("prayer_name")?: "Waktu salat"
  val channel="prayer_times_audio_v2"
  val manager=context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
  if(Build.VERSION.SDK_INT>=26)manager.createNotificationChannel(NotificationChannel(channel,"Pengingat Salat",NotificationManager.IMPORTANCE_LOW).apply{
   description="Pengingat waktu salat; suara adzan diputar oleh aplikasi"
   setSound(null,null)
   enableVibration(false)
  })
  val notification=NotificationCompat.Builder(context,channel)
   .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
   .setContentTitle("Waktu $name")
   .setContentText("Telah masuk waktu $name. Semoga Allah menerima ibadah kita.")
   .setPriority(NotificationCompat.PRIORITY_LOW)
   .setAutoCancel(true).build()
  manager.notify(name.hashCode(),notification)

  val settings=PrayerPreferences(context).read()
  val widgetManager=android.appwidget.AppWidgetManager.getInstance(context)
  val widgetIds=widgetManager.getAppWidgetIds(android.content.ComponentName(context,com.winsmediatama.adzanreminder.widget.PrayerWidgetProvider::class.java))
  if(widgetIds.isNotEmpty())com.winsmediatama.adzanreminder.widget.PrayerWidgetProvider().onUpdate(context,widgetManager,widgetIds)
  if(settings.adhanAudioEnabled){
   val audioIntent=Intent(context,AdhanAudioService::class.java)
    .putExtra(AdhanAudioService.EXTRA_PRAYER,name)
    .putExtra(AdhanAudioService.EXTRA_VOLUME,settings.adhanVolume)
   if(Build.VERSION.SDK_INT>=26)context.startForegroundService(audioIntent) else context.startService(audioIntent)
  }
  // Rolling reschedule: each delivered alarm refreshes the next two days.
  if(settings.notificationsEnabled){
   val zone=runCatching{ZoneId.of(settings.zoneId)}.getOrElse{ZoneId.systemDefault()}
   val location=PrayerLocation(settings.latitude,settings.longitude,0.0,zone,settings.cityName)
   val config=PrayerCalculationConfig(method=runCatching{PrayerMethod.valueOf(settings.method)}.getOrDefault(PrayerMethod.KEMENAG_INDONESIA),asrMadhab=runCatching{AsrMadhab.valueOf(settings.asrMadhab)}.getOrDefault(AsrMadhab.SHAFI))
   val engine=PrayerCalculationEngine()
   val date=LocalDate.now(zone)
   PrayerAlarmScheduler(context).schedule(listOf(engine.calculate(date,location,config),engine.calculate(date.plusDays(1),location,config)),true)
  }
 }
}
