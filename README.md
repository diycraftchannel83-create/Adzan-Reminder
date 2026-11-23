# Adzan Reminder — QalbuVision Prayer Engine

Offline-first Android prayer schedule and reminder foundation.

## Roadmap implementation through item 12

### 1–5: Prayer engine and dashboard
- Kotlin/Jetpack Compose dashboard with prayer times and live countdown.
- Schedule supports tomorrow after Isha.
- City presets and GPS permission flow.
- Method and Asr madhab settings.
- Persisted settings/location/configuration with SharedPreferences.

### 6: Persistent configuration
- City/location, coordinates, timezone, GPS mode, calculation method, Asr madhab, notification toggle and imsak offset are persisted.
- GPS coordinates survive app restart.

### 7: Timezone resolver
- Coarse offline Indonesia resolver maps longitude bands to WIB/WITA/WIT zones.
- IMPORTANT: this is a longitude-band approximation, not polygon/administrative timezone lookup. Locations near zone borders must be checked. Outside Indonesia falls back to device timezone.

### 8: Prayer notifications
- Android notification channel and prayer notifications.
- Runtime POST_NOTIFICATIONS permission request when reminders are enabled (Android 13+).
- AlarmManager setAndAllowWhileIdle schedules reminders; exact delivery is not guaranteed under Android battery policies.
- Two-day rolling schedule refreshed after delivered prayer notifications, app schedule changes and device reboot.

### 9: Bundled offline Adhan audio
- Alarm can play a bundled MP3 recording for each prayer, including a dedicated Fajr recording.
- Foreground media-playback service keeps audio running and provides a Stop action notification.
- Settings include adhan playback enable/disable, volume slider, and test-play button.
- Audio files are downloaded by CI from the Internet Archive source during build and bundled into the APK; playback itself is offline.
- Source collection: Adhan Recordings from Doha, Qatar (Internet Archive), presented by the source reference as Public Domain Mark 1.0. License/rights metadata should be reviewed at source before release.
- Source: https://archive.org/details/adhan.recordings.from.doha.qatar
- MP3 files: Fajr, Dhuhr, Asr, Maghrib, Isha (five recordings).

### 10: Official Kemenag validation
- Calculation engine remains astronomical approximation.
- Kemenag parameter parity, ihtiyat, elevation and official schedule comparison are not certified. Do not claim official accuracy yet.

### 11: Android home widget
- AppWidget provider and layout showing selected location, date and daily prayer schedule.
- Refresh interval set to 30 minutes by Android widget scheduling. Widget currently displays schedule times, not a live countdown.

### 12: Ramadan & imsak
- Ramadan panel calculates an estimated Ramadan date and countdown from Android Hijrah calendar.
- Shows Ramadan day number if current date falls within the estimated 29/30-day month.
- Shows imsak offset (configurable 0–30 minutes before Fajr) and Maghrib/iftar time.
- Hijri calendar estimate may differ from Indonesia's official rukyat/authority decision. No official Ramadan date table has been integrated yet.

## Accuracy & limitations
- Solar events use a NOAA-style approximation; results need comparison against official schedules across many dates and locations.
- Kemenag parity is not verified.
- Imsak is an adjustable convenience offset, not a separate astronomical event.
- GPS elevation is not currently incorporated.
- Coordinate timezone lookup is approximate in Indonesia and fallback elsewhere.
- AlarmManager inexact delivery is subject to Android power management.
- Notification permission must be granted by the user.
- Widget updates periodically and may be delayed by OS policy.

## Build/CI
- Android CI builds Debug APK, verifies APK output, and uploads artifact for 14 days.
- CI downloads five adhan MP3s from the listed Internet Archive source before building; the APK contains the audio for offline playback.

## Further refinement roadmap
- Official Kemenag/SIHAT data validation or a verified reference dataset.
- Geospatial timezone boundaries/polygon lookup.
- Exact-alarm permission workflow and audio-focus/silent-mode policy verification.
- Widget countdown and richer design.
- Hijri/Ramadan official Indonesia date source and local adjustment controls.
- Android 13–16 permission, alarm and widget behavior verification.
