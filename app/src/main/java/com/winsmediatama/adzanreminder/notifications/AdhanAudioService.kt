package com.winsmediatama.adzanreminder.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.winsmediatama.adzanreminder.R

class AdhanAudioService : Service() {
    private var player: MediaPlayer? = null
    private var focusRequest: AudioFocusRequest? = null
    private val channelId = "adhan_audio_playback"
    private val notificationId = 7102
    private val audioManager by lazy { getSystemService(AudioManager::class.java) }

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
        val volume = (intent?.getFloatExtra(EXTRA_VOLUME, 1f) ?: 1f).coerceIn(0f, 1f)
        // Promote immediately to foreground before doing any media preparation.
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
            Log.e(TAG, "Missing audio resource: $resourceName")
            stopAudio()
            return START_NOT_STICKY
        }

        try {
            requestAudioFocus()
            val mp = MediaPlayer()
            player = mp
            mp.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            resources.openRawResourceFd(resourceId).use { afd ->
                mp.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            }
            mp.setVolume(volume, volume)
            mp.setOnPreparedListener { media ->
                Log.i(TAG, "Prepared and starting $resourceName")
                media.start()
            }
            mp.setOnCompletionListener {
                Log.i(TAG, "Completed $resourceName")
                stopAudio()
            }
            mp.setOnErrorListener { _, what, extra ->
                Log.e(TAG, "MediaPlayer error what=$what extra=$extra")
                stopAudio()
                true
            }
            mp.prepareAsync()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to prepare adhan audio ($resourceName)", e)
            stopAudio()
        }
        return START_NOT_STICKY
    }

    private fun requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= 26) {
            val attrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
            val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(attrs)
                .setOnAudioFocusChangeListener { change ->
                    if (change == AudioManager.AUDIOFOCUS_LOSS) stopAudio()
                }
                .build()
            focusRequest = request
            audioManager.requestAudioFocus(request)
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(null, AudioManager.STREAM_ALARM, AudioManager.AUDIOFOCUS_GAIN)
        }
    }

    private fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= 26) {
            focusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
            focusRequest = null
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(null)
        }
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
        try { player?.setOnCompletionListener(null); player?.setOnErrorListener(null) } catch (_: Exception) {}
        try { player?.stop() } catch (_: Exception) {}
        try { player?.release() } catch (_: Exception) {}
        player = null
    }

    private fun stopAudio() {
        stopPlayer()
        abandonAudioFocus()
        if (Build.VERSION.SDK_INT >= 24) stopForeground(STOP_FOREGROUND_REMOVE) else @Suppress("DEPRECATION") stopForeground(true)
        stopSelf()
    }

    override fun onDestroy() {
        stopPlayer()
        abandonAudioFocus()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val TAG = "AdhanAudioService"
        const val ACTION_STOP = "com.winsmediatama.adzanreminder.STOP_ADHAN"
        const val EXTRA_PRAYER = "prayer_name"
        const val EXTRA_VOLUME = "adhan_volume"
    }
}
