# Adzan Reminder QA Checklist

## Build verification (deferred by request)
- [ ] Run GitHub Actions workflow manually after patch review.
- [ ] Verify downloaded MP3 files pass size and signature validation.
- [ ] Run `testDebugUnitTest`.
- [ ] Build `assembleDebug`.
- [ ] Confirm APK artifact is uploaded and installable.

## Device tests
- [ ] Android 12+ exact alarm access granted: schedule should use exact alarm API.
- [ ] Android 12+ exact alarm access denied: schedule should fall back to inexact alarm.
- [ ] Android 6–11: exact alarm path is used without special access.
- [ ] Android 13+: notification permission granted and notification appears.
- [ ] Android 13+: notification permission denied; settings show denied state and retry action.
- [ ] Toggle prayer notifications OFF: pending alarm schedule is canceled.
- [ ] Toggle prayer notifications ON: alarms are scheduled.
- [ ] Reboot: schedule is rebuilt.
- [ ] Change device date/time/timezone: schedule is rebuilt.
- [ ] Change city, calculation method, Asr madhab, timezone override: settings persist and alarms are refreshed.
- [ ] GPS works; test manual WIB/WITA/WIT override around timezone boundary.
- [ ] Widget displays Indonesian date, HH:mm prayer times, next prayer and countdown snapshot.
- [ ] Widget refresh after settings, alarm delivery, date/time/timezone changes.
- [ ] Test adhan button: notification transitions from preparation to playback; sound is audible at chosen volume.
- [ ] Test each prayer audio resource, including Fajr.
- [ ] Stop action stops playback and removes foreground notification.
- [ ] Audio focus loss stops playback safely.
- [ ] Test alarm audio while screen off and app backgrounded.
- [ ] Verify sound under alarm-volume, silent-mode, DND and Bluetooth conditions.

## Prayer schedule validation
- [ ] Compare Tasikmalaya dates against official Kemenag/SIHAT timetable.
- [ ] Compare Jakarta, Bandung, Makassar, Jayapura and a location near a timezone boundary.
- [ ] Compare at least 7 consecutive dates per location.
- [ ] Record differences for Fajr, sunrise, Dhuhr, Asr, Maghrib and Isha.
- [ ] Verify Ramadan/imsak date and offset against official Indonesian announcements.

## Release/privacy checks
- [ ] Review audio recording license and redistribution rights.
- [ ] Confirm location permissions are optional and explain their use in privacy policy.
- [ ] Test offline behavior after first install and after audio assets are bundled.
- [ ] Review Android 13–16 foreground-service and exact-alarm policy behavior.
- [ ] Run release build and Play Console pre-launch testing.

## Official Kemenag reference
- Official timetable portal: https://simbi.kemenag.go.id/jadwal-sholat
- SIHAT developer/API access information: https://sihat.kemenag.go.id/pengembang
- The official API requires application/approval and a private token; do not put a token in source code or a public repository.
