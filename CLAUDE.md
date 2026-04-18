# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

This is an Android project using Gradle with Kotlin DSL.

```bash
# Clean build
./gradlew clean

# Debug build (APK at app/build/outputs/apk/debug/app-debug.apk)
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Install to connected device
./gradlew installDebug

# Compile Kotlin only
./gradlew :app:compileDebugKotlin

# Validate resources
./gradlew :app:validateDebugResources
```

## Project Structure

**Language:** Kotlin (Java 11 target)
**Min SDK:** 30 (Android 11)
**Target/Compile SDK:** 35

Key source directories:
- `app/src/main/java/com/example/gpslogger/` - Main source
- `app/src/main/res/` - Android resources

## Architecture Overview

**Activity-Based Architecture:** The app uses multiple Activities for navigation rather than fragments:
- `WelcomeActivity` - Entry point (launcher)
- `MainActivity` - GPS tracking screen with OpenStreetMap
- `SummaryActivity` - Post-tracking statistics
- `HistoryActivity` - Previous tracks list
- `SettingsActivity` - App configuration
- `NoteActivity`/`MapNoteActivity` - Text/audio/photo notes linked to GPS points
- `DonateActivity` - Donation page

**Global State Pattern:** Static properties in `companion object` blocks are used for cross-Activity state:
- `MainActivity.pointDataList` - GPS points with timestamps
- `MainActivity.isRecordingGlobally` - Recording state
- Various `*Global` variables for stats (distance, speed, etc.)

**Data Flow:**
1. GPS tracking happens in `MainActivity` using `FusedLocationProviderClient`
2. Points saved to CSV/KML in external files directory
3. Notes attached to GPS points via timestamp/lat/long
4. Weather data fetched via Retrofit (OpenWeatherMap API)

## Key Dependencies

- `osmdroid-android` - OpenStreetMap maps
- `play-services-location` - GPS location
- `play-services-maps` - Google Maps (alternative provider)
- `MPAndroidChart` - Elevation charts
- `retrofit`/`converter-gson` - Weather API
- Material 3 components

## File Output

Tracks saved to `getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)`:
- `walk_DD-MM-YYYY.csv` - GPS coordinates
- `walk_DD-MM-YYYY.kml` - KML format for Google Earth

Notes include: text, audio (recorded), photos (captured or picked), weather data at location.

## Map Providers

Two map implementations exist:
1. **OpenStreetMap** (default) via `osmdroid` - offline capable
2. **Google Maps** via `play-services-maps` - requires API key

Provider switching handled in `MapProvider.kt` and `MapManager.kt`.

## Critical Implementation Notes

- Location updates run on main looper; permission checks required before accessing location
- MapView lifecycle tied to Activity (onResume/onPause/onDetach)
- FileProvider authority: `com.example.gpslogger.fileprovider`
- Repository uses JitPack for MPAndroidChart dependency
