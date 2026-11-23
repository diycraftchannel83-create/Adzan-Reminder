# Adzan Reminder — QalbuVision Prayer Engine

Offline-first Android prayer times and reminder app.

## Implemented
- Android Kotlin + Jetpack Compose scaffold
- Dark-green prayer dashboard
- Daily prayer schedule: Fajr, sunrise, Dhuhr, Asr, Maghrib, Isha
- Live next-prayer countdown computed from target timestamp and current clock
- Countdown selects tomorrow's schedule after Isha
- Indonesian date display
- City selector presets: Tasikmalaya, Jakarta, Bandung, Yogyakarta, Surabaya, Medan, Makassar, Denpasar, Balikpapan, Jayapura, Kuala Lumpur
- Manual city selection persisted with SharedPreferences
- Location permission request UI and Fused Location Provider GPS action
- GPS accuracy display when available
- Prayer calculation method and Asr madhab selector
- Kemenag Indonesia configuration as initial default, Shafi'i Asr default

## Current accuracy status
- Local solar calculation is a NOAA-style approximation and is not yet validated against official Kemenag/SIHAT schedules.
- Kemenag parity is NOT claimed.
- The current GPS flow uses the selected city/device timezone as a provisional timezone. Coordinate-to-timezone resolution is still needed for users crossing time zones.
- Elevation and official ihtiyat calibration need validation.
- Imsak is currently estimated as 10 minutes before Fajr.
- GPS coordinate mode currently lasts in-memory; persistent GPS coordinate storage is a follow-up task.

## Build/CI
- Android CI workflow exists, but CI/build was intentionally not run per user request.

## Next roadmap
1. Persist calculation method, madhab, and GPS coordinates.
2. Coordinate-to-timezone resolver.
3. Robust permission rationale and denied-permission UX.
4. Prayer notifications and alarm scheduling.
5. Official reference validation across Indonesian regions and dates.
6. Widget, Ramadan/imsak settings, Qibla.
