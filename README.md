# Adzan Reminder — QalbuVision Prayer Engine

Offline-first Android prayer time and adhan reminder app.

## Implemented in repository
- Kotlin + Jetpack Compose starter app
- Prayer models and configurable method/asr settings
- NOAA-style solar declination/equation-of-time approximation
- Local calculation engine for Fajr, sunrise, Dhuhr, Asr, Maghrib, Isha
- Imsak estimate (10 minutes before Fajr; configurable policy still to be added)
- Timestamp-based countdown utilities
- Next-prayer calculation supports tomorrow after Isha
- Fused Location Provider current-location helper
- Manual-location data model
- Android CI workflow added, but user requested CI/build not be run at this stage

## Accuracy / limitations
- Calculation engine is an initial approximation and is NOT yet certified or verified to match official Kemenag schedules.
- Indonesia Kemenag alignment needs reference schedule comparisons and verified ihtiyat rules.
- GPS helper currently uses device timezone as fallback; coordinate-based timezone lookup is still needed for travel across time zones.
- Elevation correction is currently approximate.
- Prayer method and Asr madhab are configurable.
- Countdown should be derived from absolute timestamps and refreshed in UI, not decremented as a mutable counter.

## Next steps
1. Permission-aware location UI and manual city picker.
2. Coordinate-to-timezone provider with offline/online strategy.
3. UI dashboard showing location, next prayer, countdown and full daily schedule.
4. Persist settings and selected location.
5. Notification/alarm scheduling with Android exact alarm policy.
6. Official Kemenag reference validation suite across Indonesian cities/dates.
7. Widget and Ramadan features.
