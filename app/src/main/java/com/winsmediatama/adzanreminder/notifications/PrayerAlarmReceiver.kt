package com.winsmediatama.adzanreminder.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.winsmediatama.adzanreminder.R

class PrayerAlarmReceiver:BroadcastReceiver(){
 override fun onReceive(context:Context,intent:Intent){
  val name=intent.getStringExtra("prayer_name")?: "Waktu salat"
  val channel="prayer_times"
  val manager=context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
  if(Build.VERSION.SDK_INT>=26)manager.createNotificationChannel(NotificationChannel(channel,"Pengingat Salat",NotificationManager.IMPORTANCE_HIGH).apply{description="Pengingat waktu salat"})
  val notification=NotificationCompat.Builder(context,channel)
   .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
   .setContentTitle("Waktu $name")
   .setContentText("Telah masuk waktu $name. Semoga Allah menerima ibadah kita.")
   .setPriority(NotificationCompat.PRIORITY_HIGH)
   .setAutoCancel(true).build()
  manager.notify(name.hashCode(),notification)
 }
}
