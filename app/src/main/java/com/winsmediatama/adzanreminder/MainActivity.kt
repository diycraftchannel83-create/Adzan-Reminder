package com.winsmediatama.adzanreminder

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.winsmediatama.adzanreminder.data.PrayerPreferences
import com.winsmediatama.adzanreminder.data.PrayerSettings
import com.winsmediatama.adzanreminder.location.CoordinateTimeZoneResolver
import com.winsmediatama.adzanreminder.location.PrayerLocationManager
import com.winsmediatama.adzanreminder.notifications.PrayerAlarmScheduler
import com.winsmediatama.adzanreminder.notifications.AdhanAudioService
import com.winsmediatama.adzanreminder.prayer.*
import com.winsmediatama.adzanreminder.ramadan.RamadanInfo
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.Locale

private data class City(val name:String,val lat:Double,val lon:Double,val zone:String)
private val cities=listOf(
 City("Tasikmalaya",-7.3274,108.2207,"Asia/Jakarta"),City("Jakarta",-6.2088,106.8456,"Asia/Jakarta"),
 City("Bandung",-6.9175,107.6191,"Asia/Jakarta"),City("Yogyakarta",-7.7956,110.3695,"Asia/Jakarta"),
 City("Surabaya",-7.2575,112.7521,"Asia/Jakarta"),City("Medan",3.5952,98.6722,"Asia/Jakarta"),
 City("Makassar",-5.1477,119.4327,"Asia/Makassar"),City("Denpasar",-8.6705,115.2126,"Asia/Makassar"),
 City("Balikpapan",-1.2379,116.8529,"Asia/Makassar"),City("Jayapura",-2.5916,140.6690,"Asia/Jayapura"),
 City("Kuala Lumpur",3.1390,101.6869,"Asia/Kuala_Lumpur")
)

class MainActivity:ComponentActivity(){
 override fun onCreate(savedInstanceState:Bundle?){
  super.onCreate(savedInstanceState)
  val preferences=PrayerPreferences(this)
  val saved=preferences.read()
  val start=cities.firstOrNull{it.name==saved.cityName}?:City(saved.cityName,saved.latitude,saved.longitude,saved.zoneId)
  val locationManager=PrayerLocationManager(this)
  val alarmScheduler=PrayerAlarmScheduler(this)
  setContent {
   val permissionLauncher=rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){}
   var notificationPermissionGranted by remember{mutableStateOf(android.os.Build.VERSION.SDK_INT<33 || ContextCompat.checkSelfPermission(this,Manifest.permission.POST_NOTIFICATIONS)==PackageManager.PERMISSION_GRANTED)}
   val notificationPermissionLauncher=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){granted->notificationPermissionGranted=granted}
   MaterialTheme(colorScheme=darkColorScheme(primary=Color(0xFF6EE7B7),secondary=Color(0xFFD6B56D),background=Color(0xFF071B16),surface=Color(0xFF102820),onPrimary=Color(0xFF062018),onBackground=Color(0xFFE8F5EE),onSurface=Color(0xFFE8F5EE))){
    Dashboard(start,saved,locationManager,alarmScheduler,
     saveSettings=preferences::save,
     saveGps={lat,lon,zone,accuracy->preferences.saveGps(lat,lon,zone,accuracy)},
     requestPermission={permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION))},
     requestNotificationPermission={if(android.os.Build.VERSION.SDK_INT>=33)notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)},
     hasPermission={ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED||ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED},
     notificationPermissionGranted=notificationPermissionGranted)
   }
  }
 }
}

@Composable
private fun Dashboard(initial:City,initialSettings:PrayerSettings,locationManager:PrayerLocationManager,alarmScheduler:PrayerAlarmScheduler,
 saveSettings:(PrayerSettings)->Unit,saveGps:(Double,Double,ZoneId,Float?)->Unit,requestPermission:()->Unit,requestNotificationPermission:()->Unit,hasPermission:()->Boolean,notificationPermissionGranted:Boolean){
 var city by remember{mutableStateOf(initial)}
 var gpsPoint by remember{mutableStateOf(if(initialSettings.useGps) initialSettings.latitude to initialSettings.longitude else null)}
 var zone by remember{mutableStateOf(runCatching{ZoneId.of(initialSettings.zoneId)}.getOrElse{ZoneId.systemDefault()})}
 var picker by remember{mutableStateOf(false)}
 var settings by remember{mutableStateOf(false)}
 var ramadanPanel by remember{mutableStateOf(false)}
 var status by remember{mutableStateOf(if(initialSettings.useGps)"GPS tersimpan • ${zone.id}" else "Lokasi manual • ${initial.zone}")}
 var accuracy by remember{mutableStateOf<String?>(null)}
 var message by remember{mutableStateOf<String?>(null)}
 var method by remember{mutableStateOf(runCatching{PrayerMethod.valueOf(initialSettings.method)}.getOrDefault(PrayerMethod.KEMENAG_INDONESIA))}
 var asr by remember{mutableStateOf(runCatching{AsrMadhab.valueOf(initialSettings.asrMadhab)}.getOrDefault(AsrMadhab.SHAFI))}
 var notifications by remember{mutableStateOf(initialSettings.notificationsEnabled)}
 var imsakMinutes by remember{mutableIntStateOf(initialSettings.imsakMinutes)}
 var adhanAudioEnabled by remember{mutableStateOf(initialSettings.adhanAudioEnabled)}
 var adhanVolume by remember{mutableFloatStateOf(initialSettings.adhanVolume)}
 val context=LocalContext.current
 val alarmManager=context.getSystemService(android.app.AlarmManager::class.java)
 var exactAlarmAllowed by remember{mutableStateOf(android.os.Build.VERSION.SDK_INT<31 || alarmManager.canScheduleExactAlarms())}
 val exactAlarmAccessLauncher=rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()){exactAlarmAllowed=android.os.Build.VERSION.SDK_INT<31 || alarmManager.canScheduleExactAlarms()}
 var now by remember{mutableStateOf(Instant.now())}
 LaunchedEffect(Unit){while(true){now=Instant.now();kotlinx.coroutines.delay(1000)}}
 val date=now.atZone(zone).toLocalDate()
 val coords=gpsPoint?: (city.lat to city.lon)
 val location=PrayerLocation(coords.first,coords.second,0.0,zone,city.name)
 val config=PrayerCalculationConfig(method=method,asrMadhab=asr)
 val engine=remember{PrayerCalculationEngine()}
 val today=remember(date,location,config){engine.calculate(date,location,config)}
 val tomorrow=remember(date,location,config){engine.calculate(date.plusDays(1),location,config)}
 val localNow=now.atZone(zone)
 val next=NextPrayerCalculator.findNext(today,tomorrow,localNow)
 val rows=listOf("Subuh" to today.fajr,"Syuruq" to today.sunrise,"Zuhur" to today.dhuhr,"Asar" to today.asr,"Magrib" to today.maghrib,"Isya" to today.isha)
 LaunchedEffect(date,location,config,notifications,imsakMinutes,adhanAudioEnabled,adhanVolume,exactAlarmAllowed){
  alarmScheduler.schedule(listOf(today,tomorrow),notifications)
  saveSettings(PrayerSettings(cityName=if(gpsPoint!=null)"Lokasi GPS" else city.name,latitude=coords.first,longitude=coords.second,zoneId=zone.id,useGps=gpsPoint!=null,method=method.name,asrMadhab=asr.name,notificationsEnabled=notifications,imsakMinutes=imsakMinutes,adhanAudioEnabled=adhanAudioEnabled,adhanVolume=adhanVolume))
  val widgetManager=android.appwidget.AppWidgetManager.getInstance(context)
  val widgetIds=widgetManager.getAppWidgetIds(android.content.ComponentName(context,com.winsmediatama.adzanreminder.widget.PrayerWidgetProvider::class.java))
  if(widgetIds.isNotEmpty())com.winsmediatama.adzanreminder.widget.PrayerWidgetProvider().onUpdate(context,widgetManager,widgetIds)
 }
 Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).systemBarsPadding().padding(16.dp)){
  Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween){
   Column{Text("ADZAN REMINDER",color=MaterialTheme.colorScheme.primary,style=MaterialTheme.typography.labelLarge);Text("Waktu Salat",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold)}
   TextButton(onClick={settings=true}){Text("Pengaturan")}
  }
  Card(Modifier.fillMaxWidth().padding(top=12.dp),shape=RoundedCornerShape(22.dp)){
   Column(Modifier.padding(18.dp)){
    Row(verticalAlignment=Alignment.CenterVertically){
     Column(Modifier.weight(1f)){Text(if(gpsPoint!=null)"Lokasi GPS" else city.name,style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold);Text(status,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.secondary);accuracy?.let{Text(it,style=MaterialTheme.typography.labelSmall)}}
     TextButton(onClick={picker=true}){Text("Kota")}
    }
    Text(date.format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy",Locale("id","ID"))),modifier=Modifier.padding(top=8.dp))
    Text("Salat berikutnya",modifier=Modifier.padding(top=16.dp),color=MaterialTheme.colorScheme.secondary)
    Text(next?.name?: "—",style=MaterialTheme.typography.headlineLarge,fontWeight=FontWeight.Bold)
    Text(next?.let{PrayerTimeFormatter.format(it.time)}?:"--:--",style=MaterialTheme.typography.titleLarge)
    Text(next?.let{PrayerTimeFormatter.countdown(it.time,localNow)}?:"--:--:--",modifier=Modifier.padding(top=6.dp),style=MaterialTheme.typography.displaySmall,color=MaterialTheme.colorScheme.primary,fontWeight=FontWeight.Bold)
    Text("HH : MM : SS menuju waktu salat",style=MaterialTheme.typography.labelMedium)
    Button(onClick={
     if(!hasPermission())requestPermission()
     else locationManager.requestCurrentLocation{loc->
      if(loc!=null){
       val resolvedZone=CoordinateTimeZoneResolver.resolveIndonesia(loc.latitude,loc.longitude)?:ZoneId.systemDefault()
       gpsPoint=loc.latitude to loc.longitude;zone=resolvedZone;status="GPS aktif • zona ${resolvedZone.id}"
       accuracy=loc.accuracyMeters?.let{"Akurasi GPS ±${it.toInt()} m"}
       saveGps(loc.latitude,loc.longitude,resolvedZone,loc.accuracyMeters)
       message="GPS tersimpan. Resolver zona waktu Indonesia memakai perkiraan wilayah longitude; periksa bila berada dekat batas zona."
      }else message="Lokasi gagal diperoleh; periksa GPS atau pilih kota."
     }
    },modifier=Modifier.fillMaxWidth().padding(top=10.dp)){Text("Ambil lokasi GPS")}
    message?.let{Text(it,modifier=Modifier.padding(top=6.dp),style=MaterialTheme.typography.bodySmall)}
   }
  }
  Row(Modifier.fillMaxWidth().padding(top=10.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)){
   OutlinedButton(onClick={ramadanPanel=true},modifier=Modifier.weight(1f)){Text("Ramadan / Imsak")}
   OutlinedButton(onClick={settings=true},modifier=Modifier.weight(1f)){Text(if(notifications)"Notifikasi ON" else "Notifikasi OFF")}
  }
  Text("Jadwal hari ini",modifier=Modifier.padding(top=16.dp,bottom=8.dp),style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold)
  LazyColumn(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(6.dp)){
   items(rows){(name,time)->
    Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(13.dp),colors=CardDefaults.cardColors(containerColor=if(next?.name==name)Color(0xFF174D39) else MaterialTheme.colorScheme.surface)){
     Row(Modifier.fillMaxWidth().padding(14.dp),verticalAlignment=Alignment.CenterVertically){Text(name,modifier=Modifier.weight(1f),fontWeight=if(next?.name==name)FontWeight.Bold else FontWeight.Normal);Text(PrayerTimeFormatter.format(time),style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold)}
    }
   }
  }
 }
 if(picker)AlertDialog(onDismissRequest={picker=false},title={Text("Pilih kota")},text={LazyColumn(Modifier.heightIn(max=390.dp)){items(cities){c->TextButton(onClick={city=c;gpsPoint=null;zone=ZoneId.of(c.zone);status="Lokasi manual • ${c.zone}";accuracy=null;message=null;saveSettings(PrayerSettings(cityName=c.name,latitude=c.lat,longitude=c.lon,zoneId=c.zone,method=method.name,asrMadhab=asr.name,notificationsEnabled=notifications,imsakMinutes=imsakMinutes,adhanAudioEnabled=adhanAudioEnabled,adhanVolume=adhanVolume));picker=false},modifier=Modifier.fillMaxWidth()){Text(c.name,modifier=Modifier.fillMaxWidth())}}}},confirmButton={TextButton(onClick={picker=false}){Text("Tutup")}})
 if(settings)AlertDialog(onDismissRequest={settings=false},title={Text("Pengaturan salat")},text={Column(Modifier.heightIn(max=460.dp).verticalScroll(rememberScrollState())){
  Text("Metode perhitungan")
  PrayerMethod.entries.forEach{m->Row(verticalAlignment=Alignment.CenterVertically){RadioButton(method==m,onClick={method=m});Text(m.name.replace('_',' '))}}
  Divider();Text("Asar")
  AsrMadhab.entries.forEach{m->Row(verticalAlignment=Alignment.CenterVertically){RadioButton(asr==m,onClick={asr=m});Text(if(m==AsrMadhab.SHAFI)"Syafi'i" else "Hanafi")}}
  Divider()
  Text(if(exactAlarmAllowed)"Alarm tepat waktu: diizinkan" else "Alarm tepat waktu: belum diizinkan",color=if(exactAlarmAllowed)MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary)
  if(android.os.Build.VERSION.SDK_INT>=31 && !exactAlarmAllowed){OutlinedButton(onClick={exactAlarmAccessLauncher.launch(Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).setData(android.net.Uri.parse("package:${context.packageName}")))},modifier=Modifier.fillMaxWidth()){Text("Izinkan alarm tepat waktu")}}
  Text("Jika izin tidak diberikan, aplikasi tetap memakai alarm Android yang dapat terlambat karena optimasi baterai.",style=MaterialTheme.typography.bodySmall)
  Divider()
  Text("Zona waktu",style=MaterialTheme.typography.titleSmall)
  Text("Zona saat ini: ${zone.id}",style=MaterialTheme.typography.bodySmall)
  Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){
   OutlinedButton(onClick={zone=ZoneId.of("Asia/Jakarta");status=if(gpsPoint!=null)"GPS • Zona manual WIB" else "Lokasi manual • WIB"},modifier=Modifier.weight(1f)){Text("WIB")}
   OutlinedButton(onClick={zone=ZoneId.of("Asia/Makassar");status=if(gpsPoint!=null)"GPS • Zona manual WITA" else "Lokasi manual • WITA"},modifier=Modifier.weight(1f)){Text("WITA")}
   OutlinedButton(onClick={zone=ZoneId.of("Asia/Jayapura");status=if(gpsPoint!=null)"GPS • Zona manual WIT" else "Lokasi manual • WIT"},modifier=Modifier.weight(1f)){Text("WIT")}
  }
  Text("Untuk lokasi GPS di dekat batas zona waktu, pilih zona secara manual bila hasil otomatis kurang tepat.",style=MaterialTheme.typography.bodySmall)
  Divider()
  Row(verticalAlignment=Alignment.CenterVertically){Checkbox(notifications,onCheckedChange={notifications=it;if(it)requestNotificationPermission()});Text("Aktifkan notifikasi waktu salat")}
  Text(if(notificationPermissionGranted)"Izin notifikasi Android: aktif" else "Izin notifikasi Android: belum diberikan",style=MaterialTheme.typography.bodySmall,color=if(notificationPermissionGranted)MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary)
  if(android.os.Build.VERSION.SDK_INT>=33 && !notificationPermissionGranted){OutlinedButton(onClick=requestNotificationPermission,modifier=Modifier.fillMaxWidth()){Text("Izinkan notifikasi Android")}}
  Text("Imsak: ${imsakMinutes} menit sebelum Subuh")
  Slider(value=imsakMinutes.toFloat(),onValueChange={imsakMinutes=it.toInt()},valueRange=0f..30f,steps=29)
  Divider()
  Row(verticalAlignment=Alignment.CenterVertically){Checkbox(adhanAudioEnabled,onCheckedChange={adhanAudioEnabled=it});Text("Putar suara adzan otomatis")}
  Text("Volume adzan: ${(adhanVolume*100).toInt()}%")
  Slider(value=adhanVolume,onValueChange={adhanVolume=it},valueRange=0f..1f)
  OutlinedButton(onClick={
   val preview=Intent(context,AdhanAudioService::class.java).putExtra(AdhanAudioService.EXTRA_PRAYER,"Zuhur").putExtra(AdhanAudioService.EXTRA_VOLUME,adhanVolume)
   if(android.os.Build.VERSION.SDK_INT>=26)context.startForegroundService(preview) else context.startService(preview)
  },modifier=Modifier.fillMaxWidth()){Text("▶ Tes suara adzan")}
  Text("Audio adzan tersedia offline dan berjalan dari file yang dibundel dalam APK.",style=MaterialTheme.typography.bodySmall)
  Text("Pengaturan disimpan otomatis.")
 }},confirmButton={TextButton(onClick={settings=false}){Text("Selesai")}})
 if(ramadanPanel){
  val info=RamadanInfo.forDate(date,null,today,imsakMinutes)
  AlertDialog(onDismissRequest={ramadanPanel=false},title={Text("Ramadan & Imsak")},text={Column{
   Text("Ramadan diperkirakan mulai: ${info.ramadanStart}")
   info.dayNumber?.let{Text("Hari Ramadan ke-$it")}
   info.daysUntilRamadan?.let{Text("Perkiraan ${it} hari menuju Ramadan")}
   Text("Imsak hari ini: ${info.imsakTime?: "--"}")
   Text("Buka puasa / Magrib: ${info.iftarTime?: "--"}")
   Text("Pengaturan imsak: ${imsakMinutes} menit sebelum Subuh")
   Spacer(Modifier.height(8.dp))
   Text("Tanggal berdasarkan kalender Hijriah perangkat dan dapat berbeda dari keputusan rukyat/otoritas resmi Indonesia.")
  }},confirmButton={TextButton(onClick={ramadanPanel=false}){Text("Tutup")}})
}
}
