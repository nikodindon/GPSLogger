@echo off
setlocal enabledelayedexpansion

echo ============================================================
echo GPSLogger Android App - Build Verification Script
echo ============================================================
echo.

REM Check if we're in the right directory
if not exist "gradlew" (
    echo ERROR: gradlew not found. Please run from project root.
    pause
    exit /b 1
)

echo [1/5] Checking project structure...
if exist "app\src\main\AndroidManifest.xml" (
    echo   ✓ AndroidManifest.xml found
) else (
    echo   ✗ AndroidManifest.xml missing
    exit /b 1
)

if exist "app\src\main\java\com\example\gpslogger\data\NoteWithPhotos.kt" (
    echo   ✓ NoteWithPhotos.kt found
) else (
    echo   ✗ NoteWithPhotos.kt missing
)

if exist "app\src\main\java\com\example\gpslogger\ui\notes\NoteFragment.kt" (
    echo   ✓ NoteFragment.kt found
) else (
    echo   ✗ NoteFragment.kt missing
)

echo.
echo [2/5] Checking elevation statistics files...
if exist "app\src\main\java\com\example\gpslogger\data\ElevationCalculator.kt" (
    echo   ✓ ElevationCalculator.kt found
) else (
    echo   ✗ ElevationCalculator.kt missing
)

if exist "app\src\main\java\com\example\gpslogger\ui\stats\ElevationFragment.kt" (
    echo   ✓ ElevationFragment.kt found
) else (
    echo   ✗ ElevationFragment.kt missing
)

echo.
echo [3/5] Checking map provider files...
if exist "app\src\main\java\com\example\gpslogger\map\MapManager.kt" (
    echo   ✓ MapManager.kt found
) else (
    echo   ✗ MapManager.kt missing
)

if exist "app\src\main\res\layout\map_osm_fragment.xml" (
    echo   ✓ map_osm_fragment.xml found
) else (
    echo   ✗ map_osm_fragment.xml missing
)

echo.
echo [4/5] Checking layout files...
set count=0
for %%f in (
    "app\src\main\res\layout\fragment_note.xml"
    "app\src\main\res\layout\fragment_elevation.xml"
    "app\src\main\res\layout\fragment_statistics.xml"
) do (
    if exist %%f (
        set /a count+=1
    )
)
echo   Layout files found: !count!/3
if !count!==3 (
    echo   ✓ All layout files present
) else (
    echo   ⚠ Some layout files missing (this may be OK)
)

echo.
echo [5/5] Checking documentation...
set doc_count=0
for %%f in (
    "QWEN.md"
    "IMPROVEMENT_ROADMAP.md"
    "FINAL_SUMMARY.md"
    "VERIFICATION_CHECKLIST.md"
) do (
    if exist %%f (
        set /a doc_count+=1
    )
)
echo   Documentation files: !doc_count!/4
doc_count=0

for %%f in (
    "README.md"
    "app\src\main\res\values\strings.xml"
) do (
    if exist %%f (
        set /a doc_count+=1
    )
)
echo   Resource files: !doc_count!/2

echo.
echo ============================================================
echo Build Verification Checklist
echo ============================================================
echo.
echo CRITICAL CHECKS:
echo   [ ] AndroidManifest.xml has no duplicate activities
echo   [ ] All .kt files compile without errors
echo   [ ] Layout files are properly formatted
echo.
echo FEATURE CHECKS:
echo   [ ] Note capture with photos works
echo   [ ] Elevation statistics display correctly
echo   [ ] Map provider switching works
echo   [ ] UI follows Material 3 guidelines
echo.
echo ============================================================
echo To run actual build, execute:
echo   ./gradlew clean assembleDebug
echo ============================================================

pause