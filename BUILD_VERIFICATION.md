# Build Verification

## 📋 Build Process

### Quick Build
```bash
./gradlew clean assembleDebug
```

### Release Build
```bash
./gradlew clean assembleRelease
```

### Generate Signed Bundle (AAB for Play Store)
```bash
./gradlew bundleRelease
```

## ✅ Verification Steps

### Build Verification
- [ ] Build completes without errors
- [ ] `app-debug.apk` generated in `app/build/outputs/apk/debug/`
- [ ] `app-release.aab` generated in `app/build/outputs/bundle/release/`
- [ ] No Gradle dependency conflicts

### Testing Verification
- [ ] Unit tests pass
- [ ] Instrumentation tests pass
- [ ] All features functional
- [ ] No crashes on startup

### Quality Checks
- [ ] Lint passes without errors
- [ ] No unused resources
- [ ] All permissions justified
- [ ] ProGuard rules configured (if enabled)

## 🚨 Common Build Issues

### Fix: Duplicate Activity Entries
**Issue**: `AndroidManifest.xml` contains duplicate activity declarations
**Solution**: Removed duplicate entries in Phase 1

### Fix: Missing Dependencies
```bash
./gradlew --refresh-dependencies
./gradlew clean build
```

### Fix: Outdated Build Tools
```bash
# Update Android SDK Build-Tools to version 35.0.0
```

## 📊 Build Metrics

- **Build Time**: ~2-5 minutes (first build), ~30-60 seconds (incremental)
- **APK Size**: ~20-50 MB (debug), ~15-30 MB (release)
- **Tasks Executed**: ~150-200 (debug), ~120-180 (release)

## ✅ Build Success Criteria

### Must Pass
- ✅ No compilation errors
- ✅ No Gradle sync failures
- ✅ APK/AAB generated successfully
- ✅ All required resources present

### Should Pass
- ✅ Build completes under 3 minutes
- ✅ No deprecation warnings
- ✅ No resource conflicts
- ✅ ProGuard optimization successful

### Nice to Have
- ✅ Code coverage reports
- ✅ Performance benchmarks
- ✅ Lint zero-warnings

## 🔧 Troubleshooting

### Build Hangs
```bash
# Stop current build
Ctrl+C

# Clean and retry
./gradlew clean build --no-daemon
```

### Out of Memory
```bash
# Set JVM options
export GRADLE_OPTS="-Xmx4g -Dfile.encoding=UTF-8"
./gradlew assembleDebug
```

### Dependency Conflicts
```bash
# Show dependency tree
./gradlew :app:dependencies

# Resolve conflicts in build.gradle
```

## 📈 Build Performance

| Build Type | Time | Output Size | Tasks |
|------------|------|-------------|-------|
| Clean Debug | 2-5 min | ~40 MB | ~200 |
| Incremental Debug | 30-60 sec | ~5 MB | ~20 |
| Clean Release | 3-8 min | ~20 MB | ~180 |

## 🎯 Next Steps After Build

1. **Install on device**: `./gradlew installDebug`
2. **Run tests**: `./gradlew testDebug`
3. **Generate screenshots**: Run `generate_screenshots.ps1`
4. **Create release**: Generate signed APK/AAB
5. **Submit to Play Store**: Follow PLAY_STORE_CHECKLIST.md

## 📞 Support

If build fails:
1. Check `./gradlew --info assembleDebug` for detailed logs
2. Review `BUILD_GUIDE.md` for common issues
3. Verify all dependencies in `build.gradle`
4. Ensure Android SDK is properly configured