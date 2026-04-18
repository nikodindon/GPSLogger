# GPSLogger Android Project

## Project Overview

**GPSLogger** is an Android application that records GPS positions into CSV/KML files, utilizing Open Street Map for position display. The application provides comprehensive tracking features including:

- Real-time GPS position recording
- CSV and KML export formats
- Open Street Map integration for map visualization
- Audio note recording during trips
- Text note capture during journeys
- Trip history management
- Journey summary with detailed statistics (distance, average speed, min/max speeds)

## Project Structure

```
GPSLogger/
├── build.gradle.kts              # Top-level Gradle build configuration
├── settings.gradle.kts            # Project settings and included modules
├── gradle.properties              # Gradle JVM and Android configuration
├── .gitignore                     # Git ignore rules for Android projects
├── README.md                      # Project documentation
├── app/
│   ├── build.gradle.kts          # App module build configuration
│   ├── proguard-rules.pro         # ProGuard/R8 obfuscation rules
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml  # App manifest with permissions
│       │   ├── java/com/          # Java/Kotlin source code
│       │   └── res/               # Resources (layouts, drawables, values)
│       ├── androidTest/           # Android instrumentation tests
│       └── test/                  # Unit tests
└── gradle/
    ├── libs.versions.toml         # Dependency version management
    └── wrapper/                   # Gradle wrapper files
```

## Technologies and Dependencies

### Core Technologies
- **Android SDK**: Target SDK 35 (Android 14), Min SDK 30
- **Kotlin**: JVM target 11, official code style
- **AndroidX**: Full AndroidX support enabled
- **OpenStreetMap**: osmdroid-android 6.1.18 for map display

### Key Dependencies
- `androidx.recyclerview:recyclerview:1.3.2` - RecyclerView for list display
- `com.google.android.gms:play-services-maps:18.2.0` - Google Maps integration
- `org.osmdroid:osmdroid-android:6.1.18` - OpenStreetMap implementation
- `com.google.android.gms:play-services-location:21.0.1` - Location services
- `androidx.core:core-ktx:1.12.0` - Kotlin extensions
- `com.google.android.material:material:1.12.0` - Material Design components
- `com.github.PhilJay:MPAndroidChart:v3.1.0` - Chart library for statistics
- `com.squareup.retrofit2:retrofit:2.9.0` - HTTP client
- `androidx.test` libraries - Testing frameworks

### Dependency Management
- Uses Gradle with `libs.versions.toml` for version catalog
- JitPack repository included for MPAndroidChart
- Google and Maven Central repositories configured

## Building and Running

### Prerequisites
- Java 11 or higher
- Android Studio or command-line tools
- Gradle 7.0+

### Build Commands
```bash
# Clean build
./gradlew clean build

# Build release APK
./gradlew assembleRelease

# Build debug APK
./gradlew assembleDebug

# Run tests
./gradlew testDebugUnitTest
./gradlew connectedDebugAndroidTest

# Install on device
./gradlew installDebug
```

### Android Studio Setup
1. Open the project in Android Studio
2. Ensure Gradle wrapper is used (recommended)
3. Sync project with Gradle files
4. Build and run on emulator or physical device

## Development Conventions

### Code Style
- **Language**: Kotlin (official style)
- **Java Version**: 11 compatibility
- **AndroidX**: Fully migrated, no legacy support library
- **Package Structure**: `com.example.gpslogger`

### Permissions Required
The app requires the following permissions (declared in AndroidManifest.xml):
- `ACCESS_FINE_LOCATION` - Precise location access
- `ACCESS_COARSE_LOCATION` - Approximate location access
- `INTERNET` - Network access for maps and APIs
- `RECORD_AUDIO` - Audio note recording

### Testing Strategy
- **Unit Tests**: Located in `app/src/test/`
- **Instrumentation Tests**: Located in `app/src/androidTest/`
- Uses AndroidJUnitRunner for instrumentation tests
- Espresso for UI testing
- JUnit for unit testing

### ProGuard Configuration
- ProGuard is disabled for release builds (`isMinifyEnabled = false`)
- Basic ProGuard rules provided in `proguard-rules.pro`
- FileProvider configuration for file sharing

## Key Features Implementation

### GPS Recording
- Continuous location updates using Google Play Services Location API
- Position data saved to CSV and KML formats
- Background location tracking support

### Map Integration
- OpenStreetMap via osmdroid for offline map capability
- Real-time position marker updates
- Route tracking visualization

### Note System
- Audio notes: Record voice memos during trips
- Text notes: Manual text entry with timestamps
- Notes linked to specific GPS positions

### Statistics and Summary
- Distance calculation using GPS coordinates
- Speed calculations (current, average, maximum)
- Journey duration tracking
- Visual charts using MPAndroidChart

## File Formats

### CSV Export
Standard GPS data format with columns:
- Timestamp
- Latitude
- Longitude
- Altitude
- Accuracy
- Speed

### KML Export
Google Earth compatible format for advanced mapping applications

## Known Issues and Limitations

- APK download link provided is from Dropbox (external hosting)
- Some activity classes have duplicate entries in manifest (e.g., `SummaryActivity` and `SummaryActivity.kt`)
- ProGuard rules are minimal and may need enhancement for release builds

## Future Enhancements

Potential improvements based on the codebase:
- Enhanced export formats (GPX, GeoJSON)
- Offline map tile caching
- Route planning and navigation
- Cloud synchronization
- Wear OS companion app
- More detailed analytics and statistics