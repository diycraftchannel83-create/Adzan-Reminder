package com.winsmediatama.adzanreminder.data

import android.content.Context
import java.time.ZoneId

data class PrayerSettings(
 val cityName:String="Tasikmalaya", val latitude:Double=-7.3274, val longitude:Double=108.2207,
 val zoneId:String="Asia/Jakarta", val useGps:Boolean=false,
 val method:String="KEMENAG_INDONESIA", val asrMadhab:String="SHAFI",
 val notificationsEnabled:Boolean=true, val imsakMinutes:Int=10
)

class PrayerPreferences(context:Context){
 private val prefs=context.applicationContext.getSharedPreferences("prayer_settings",Context.MODE_PRIVATE)
 fun read():PrayerSettings=PrayerSettings(
  cityName=prefs.getString("cityName","Tasikmalaya")?: "Tasikmalaya",
  latitude=prefs.getFloat("latitude",-7.3274f).toDouble(),
  longitude=prefs.getFloat("longitude",108.2207f).toDouble(),
  zoneId=prefs.getString("zoneId","Asia/Jakarta")?: "Asia/Jakarta",
  useGps=prefs.getBoolean("useGps",false),
  method=prefs.getString("method","KEMENAG_INDONESIA")?: "KEMENAG_INDONESIA",
  asrMadhab=prefs.getString("asrMadhab","SHAFI")?: "SHAFI",
  notificationsEnabled=prefs.getBoolean("notificationsEnabled",true),
  imsakMinutes=prefs.getInt("imsakMinutes",10)
 )
 fun save(s:PrayerSettings){
  prefs.edit().putString("cityName",s.cityName).putFloat("latitude",s.latitude.toFloat()).putFloat("longitude",s.longitude.toFloat())
   .putString("zoneId",s.zoneId).putBoolean("useGps",s.useGps).putString("method",s.method)
   .putString("asrMadhab",s.asrMadhab).putBoolean("notificationsEnabled",s.notificationsEnabled)
   .putInt("imsakMinutes",s.imsakMinutes).apply()
 }
 fun saveGps(lat:Double,lon:Double,zone:ZoneId,accuracy:Float?){
  prefs.edit().putFloat("latitude",lat.toFloat()).putFloat("longitude",lon.toFloat()).putString("zoneId",zone.id)
   .putString("cityName","Lokasi GPS").putBoolean("useGps",true).putFloat("gpsAccuracy",accuracy?:-1f).apply()
 }
 fun gpsAccuracy():Float?=prefs.getFloat("gpsAccuracy",-1f).takeIf{it>=0f}
}
