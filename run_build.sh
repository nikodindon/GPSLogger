#!/bin/bash
# Build Verification Script for GPSLogger
# Run this script to verify your project builds correctly

echo "=========================================="
echo "GPSLogger Build Verification Script"
echo "=========================================="
echo ""

# Check if we're in the right directory
if [ ! -f "gradlew" ]; then
    echo "❌ Error: gradlew not found. Please run from project root."
    exit 1
fi

echo "✓ Found gradlew in project root"
echo ""

# Make gradlew executable
echo "Making gradlew executable..."
chmod +x gradlew
echo "✓ gradlew is now executable"
echo ""

# Clean previous builds
echo "Cleaning previous builds..."
./gradlew clean
echo "✓ Clean complete"
echo ""

# Build the project
echo "Building project..."
./gradlew assembleDebug
BUILD_STATUS=$?

echo ""
if [ $BUILD_STATUS -eq 0 ]; then
    echo "✅ BUILD SUCCESSFUL!"
    echo ""
    echo "APK location: app/build/outputs/apk/debug/app-debug.apk"
else
    echo "❌ BUILD FAILED!"
    echo ""
    echo "Please check:"
    echo "1. AndroidManifest.xml for errors"
    echo "2. Check that all activity .kt files exist"
    echo "3. Verify dependencies in build.gradle.kts"
    echo "4. Check Logcat for detailed error messages"
fi

echo ""
echo "=========================================="
echo "Build verification complete"
echo "=========================================="