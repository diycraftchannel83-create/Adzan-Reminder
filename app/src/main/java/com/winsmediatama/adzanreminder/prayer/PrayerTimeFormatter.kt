package com.winsmediatama.adzanreminder.prayer

import java.time.Duration
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object PrayerTimeFormatter {
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale("id", "ID"))
    fun format(time: ZonedDateTime?): String = time?.format(timeFormatter) ?: "--:--"

    fun countdown(target: ZonedDateTime, now: ZonedDateTime): String {
        val seconds = Duration.between(now.toInstant(), target.toInstant()).seconds.coerceAtLeast(0)
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val remainingSeconds = seconds % 60
        return "%02d:%02d:%02d".format(hours, minutes, remainingSeconds)
    }
}
