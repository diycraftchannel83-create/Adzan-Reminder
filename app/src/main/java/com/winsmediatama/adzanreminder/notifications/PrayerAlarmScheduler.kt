package com.winsmediatama.adzanreminder.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.winsmediatama.adzanreminder.prayer.PrayerSchedule
import java.time.ZonedDateTime

class PrayerAlarmScheduler(private val context:Context){
 fun schedule(schedules:List<PrayerSchedule>,enabled:Boolean){
  cancelAll()
  if(!enabled)return
  val alarmManager=context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
  val events=schedules.flatMapIndexed{dayIndex,schedule->
   listOf("Subuh" to schedule.fajr,"Zuhur" to schedule.dhuhr,"Asar" to schedule.asr,"Magrib" to schedule.maghrib,"Isya" to schedule.isha)
    .mapIndexed{prayerIndex,pair->Triple(1000+dayIndex*5+prayerIndex,pair.first,pair.second)}
  }
  events.forEach{(requestCode,name,time)->
   if(time!=null&&time.isAfter(ZonedDateTime.now(time.zone))){
    val intent=Intent(context,PrayerAlarmReceiver::class.java).putExtra("prayer_name",name).putExtra("prayer_time",time.toString())
    val pending=PendingIntent.getBroadcast(context,requestCode,intent,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    val triggerAt=time.toInstant().toEpochMilli()
    val exactAllowed=Build.VERSION.SDK_INT<31 || runCatching{alarmManager.canScheduleExactAlarms()}.getOrDefault(false)
    val exactScheduled=if(exactAllowed && Build.VERSION.SDK_INT>=23){
     runCatching{alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,triggerAt,pending)}.isSuccess
    }else if(exactAllowed){
     runCatching{alarmManager.setExact(AlarmManager.RTC_WAKEUP,triggerAt,pending)}.isSuccess
    }else false
    if(!exactScheduled){
     // Exact alarm permission may be unavailable/revoked or an OEM may reject it.
     // Use a guarded inexact fallback rather than crashing the receiver/UI flow.
     val fallback=runCatching{
      if(Build.VERSION.SDK_INT>=23)alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,triggerAt,pending)
      else alarmManager.set(AlarmManager.RTC_WAKEUP,triggerAt,pending)
     }
     if(fallback.isFailure) android.util.Log.e("PrayerAlarmScheduler","Unable to schedule $name at $time",fallback.exceptionOrNull())
    }
   }
  }
 }
 fun cancelAll(){
  val alarmManager=context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
  (1000..1009).forEach{id->
   val intent=Intent(context,PrayerAlarmReceiver::class.java)
   val pending=PendingIntent.getBroadcast(context,id,intent,PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
   pending?.let{alarmManager.cancel(it);it.cancel()}
  }
 }
}
