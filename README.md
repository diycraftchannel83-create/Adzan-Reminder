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
- Settings now include manual WIB/WITA/WIT override for GPS edge cases.
- IMPORTANT: automatic resolution remains a longitude-band approximation, not polygon/administrative timezone lookup. Locations near zone borders must be checked. Outside Indonesia falls back to device timezone.

### 8: Prayer notifications
- Android notification channel and prayer notifications.
- Runtime POST_NOTIFICATIONS permission request when reminders are enabled (Android 13+).
- AlarmManager uses exact scheduling on Android 6–11 without special access, and Android 12+ when special access is granted; otherwise it falls back to inexact `setAndAllowWhileIdle()` delivery. Battery and OS policy can still affect delivery.
- Two-day rolling schedule refreshed after delivered prayer notifications, app schedule changes, device reboot, date/time/timezone changes, and app replacement.

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
- AppWidget provider and layout showing selected location, date, daily prayer schedule, next prayer and a countdown snapshot.
- Refreshes after app settings/location changes, alarm delivery, system date/time/timezone changes, and periodic Android widget updates.

### 12: Ramadan & imsak
- Ramadan panel calculates an estimated Ramadan date and countdown from Android Hijrah calendar.
- Shows Ramadan day number if current date falls within the estimated 29/30-day month.
- Shows imsak offset (configurable 0–30 minutes before Fajr) and Maghrib/iftar time.
- Hijri calendar estimate may differ from Indonesia's official rukyat/authority decision. No official Ramadan date table has been integrated yet.

## QA and audit status

- Audit patches applied: settings dialog scrolling, persistence keys for audio/volume/imsak, GPS label persistence, audio-focus denial handling, system date/time/timezone/app-update alarm rescheduling, elevation correction for sunrise/Maghrib, manual WIB/WITA/WIT override, and exact alarms on supported API levels.
- Unit tests added for prayer-event chronology, Asr madhab ordering, method differences, next-prayer rollover, and Indonesia timezone resolver boundaries.
- CI validates all five downloaded MP3 files for count, size, and basic MP3 signature, then runs unit tests before building.
- Optional exact alarm access is requested from settings; if unavailable, scheduler falls back to Android inexact alarms.
- **Build/test intentionally not executed during this patch phase** per request. CI validation remains pending.

## Accuracy & limitations
- Solar events use a NOAA-style approximation; sanity unit tests cover event ordering, but results still need comparison against official schedules across many dates and locations.
- Kemenag parity is not verified.
- Imsak is an adjustable convenience offset, not a separate astronomical event.
- GPS elevation is not currently obtained; the engine accepts elevation input and applies a corrected sunrise/Maghrib horizon adjustment when elevation is supplied.
- Coordinate timezone lookup is approximate in Indonesia and fallback elsewhere.
- AlarmManager uses inexact `setAndAllowWhileIdle()` delivery and is subject to Android power management. Alarm schedules are now refreshed after boot, date/time/timezone changes, and app replacement.
- Notification permission must be granted by the user.
- Widget updates periodically and may be delayed by OS policy.

## Build/CI
- Android CI is temporarily **manual-only** (`workflow_dispatch`) during the requested patch-only phase. When manually run, it validates MP3 assets, runs unit tests, builds Debug APK, verifies APK output, and uploads artifact for 14 days.
- CI downloads five adhan MP3s from the listed Internet Archive source before building; the APK contains the audio for offline playback.

## Further refinement roadmap
- Official Kemenag/SIHAT data validation or a verified reference dataset.
- Geospatial timezone boundaries/polygon lookup.
- Verify exact-alarm access behavior and audio-focus/silent-mode policy on Android 12–16.
- Widget second-by-second live countdown (currently refreshed snapshot) and richer design.
- Hijri/Ramadan official Indonesia date source and local adjustment controls.
- Android 13–16 permission, alarm and widget behavior verification.
