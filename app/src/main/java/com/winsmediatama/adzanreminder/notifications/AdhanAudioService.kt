package com.winsmediatama.adzanreminder.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.winsmediatama.adzanreminder.R

class AdhanAudioService : Service() {
    private var player: MediaPlayer? = null
    private val channelId = "adhan_audio_playback"
    private val notificationId = 7102

    override fun onCreate() {
        super.onCreate()
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopAudio()
            return START_NOT_STICKY
        }
        val prayer = intent?.getStringExtra(EXTRA_PRAYER) ?: "Zuhur"
        val volume = intent?.getFloatExtra(EXTRA_VOLUME, 1f)?.coerceIn(0f, 1f) ?: 1f
        startForeground(notificationId, buildNotification(prayer))
        stopPlayer()
        val resourceName = when (prayer.lowercase()) {
            "subuh", "fajr" -> "adhan_fajr"
            "zuhur", "dhuhr" -> "adhan_dhuhr"
            "asar", "asr" -> "adhan_asr"
            "magrib", "maghrib" -> "adhan_maghrib"
            "isya", "isha" -> "adhan_isha"
            else -> "adhan_dhuhr"
        }
        val resourceId = resources.getIdentifier(resourceName, "raw", packageName)
        if (resourceId == 0) {
            stopAudio()
            return START_NOT_STICKY
        }
        runCatching {
            player = MediaPlayer.create(this, resourceId)?.apply {
                setVolume(volume, volume)
                setOnCompletionListener { stopAudio() }
                start()
            }
        }.onFailure { stopAudio() }
        return START_NOT_STICKY
    }

    private fun buildNotification(prayer: String): Notification {
        val stopIntent = Intent(this, AdhanAudioService::class.java).setAction(ACTION_STOP)
        val stopPending = PendingIntent.getService(
            this, 7103, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Adzan sedang diputar")
            .setContentText("Suara adzan waktu $prayer")
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .addAction(android.R.drawable.ic_media_pause, "Hentikan", stopPending)
            .build()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(
                NotificationChannel(channelId, "Pemutaran Adzan", NotificationManager.IMPORTANCE_LOW)
            )
        }
    }

    private fun stopPlayer() {
        runCatching { player?.stop() }
        player?.release()
        player = null
    }

    private fun stopAudio() {
        stopPlayer()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        stopPlayer()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_STOP = "com.winsmediatama.adzanreminder.STOP_ADHAN"
        const val EXTRA_PRAYER = "prayer_name"
        const val EXTRA_VOLUME = "adhan_volume"
    }
}
