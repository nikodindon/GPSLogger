@echo off
echo ==========================================
echo GPSLogger Project Check
echo ==========================================
echo.

REM Check for essential files
echo Checking project structure...
if exist "build.gradle.kts" (
    echo ✓ build.gradle.kts found
) else (
    echo ✗ build.gradle.kts missing
)

if exist "settings.gradle.kts" (
    echo ✓ settings.gradle.kts found
) else (
    echo ✗ settings.gradle.kts missing
)

if exist "app\\build.gradle.kts" (
    echo ✓ app/build.gradle.kts found
) else (
    echo ✗ app/build.gradle.kts missing
)

if exist "app\\src\\main\\AndroidManifest.xml" (
    echo ✓ AndroidManifest.xml found
) else (
    echo ✗ AndroidManifest.xml missing
)

if exist "gradlew" (
    echo ✓ gradlew found
) else (
    echo ✗ gradlew missing
)

echo.
echo Checking for duplicate activities in manifest...
findstr /i "activity" "app\src\main\AndroidManifest.xml" | findstr /i "name="

echo.
echo ==========================================
echo Check complete!
echo ==========================================