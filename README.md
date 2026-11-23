# Adzan Reminder — QalbuVision Prayer Engine

Offline-first Android prayer time and adhan reminder app.

## Accuracy-first design
- GPS coordinates with manual location fallback
- Time zone resolved from location (never hard-coded)
- Indonesian default: Kemenag-aligned configuration, Syafi'i Asr
- Local astronomical calculation; network is optional
- Official schedule comparison/validation planned; do not claim exact Kemenag parity until verified against official reference data
- Countdown derives from absolute timestamps (not decrementing counters)

## Roadmap
1. Android project scaffold (Kotlin, Jetpack Compose)
2. Prayer calculation core and unit tests
3. Location provider and timezone handling
4. Daily schedule, next prayer, countdown
5. Notifications and exact-alarm policy handling
6. Widget
7. Indonesia official-reference validation suite
8. Ramadan/imsak and configuration UI

## Accuracy notes
Prayer times are convention-dependent. The Indonesian default will be calibrated and validated against official Kemenag/SIHAT reference schedules before parity is claimed. Calculation parameters and any minute adjustments must be visible in settings.

## Current repository status
This repository was empty at initialization. The project scaffold is being added incrementally; builds and CI are not yet verified.
