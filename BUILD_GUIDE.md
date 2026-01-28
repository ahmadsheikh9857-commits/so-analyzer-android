# SO Analyzer - Build & Development Guide

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Project Setup](#project-setup)
3. [Building with Android Studio](#building-with-android-studio)
4. [Building from Command Line](#building-from-command-line)
5. [Signing & Release](#signing--release)
6. [Troubleshooting](#troubleshooting)
7. [Development Workflow](#development-workflow)
8. [CI/CD Setup](#cicd-setup)

## Prerequisites

### Required Software

- **Java Development Kit (JDK)**: Version 17 or later
  - Download: https://www.oracle.com/java/technologies/downloads/
  - Verify: `java -version`

- **Android Studio**: Latest stable version (2023.1+)
  - Download: https://developer.android.com/studio
  - Includes Android SDK and NDK

- **Android SDK**: API 34 (minimum API 31)
  - Installed via Android Studio SDK Manager
  - Required components:
    - Android SDK Platform 34
    - Android SDK Build-Tools 34.x.x
    - Android NDK (for native compilation)
    - CMake 3.22.1+

- **Git**: For version control
  - Download: https://git-scm.com/

### System Requirements

- **Disk Space**: 10 GB minimum (SDK + build artifacts)
- **RAM**: 8 GB minimum (16 GB recommended)
- **OS**: Windows, macOS, or Linux

### Environment Variables

Set these for optimal build performance:

```bash
# Linux/macOS
export ANDROID_HOME=$HOME/Android/Sdk
export ANDROID_SDK_ROOT=$ANDROID_HOME
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin
export PATH=$PATH:$ANDROID_HOME/platform-tools
export JAVA_HOME=/path/to/jdk17

# Windows (Command Prompt)
set ANDROID_HOME=C:\Users\YourUsername\AppData\Local\Android\Sdk
set JAVA_HOME=C:\Program Files\Java\jdk-17
```

## Project Setup

### 1. Clone/Extract Project

```bash
# Extract the project
unzip SOAnalyzer.zip
cd SOAnalyzer

# Or clone from repository
git clone https://github.com/yourusername/SOAnalyzer.git
cd SOAnalyzer
```

### 2. Verify Project Structure

```
SOAnalyzer/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/soanalyzer/
│   │   ├── cpp/
│   │   ├── res/
│   │   └── AndroidManifest.xml
│   ├── build.gradle
│   └── proguard-rules.pro
├── build.gradle
├── settings.gradle
├── gradle.properties
├── build.sh
├── build.bat
├── README.md
├── ARCHITECTURE.md
└── BUILD_GUIDE.md
```

### 3. Initial Gradle Sync

```bash
# Linux/macOS
./gradlew --version
./gradlew tasks

# Windows
gradlew.bat --version
gradlew.bat tasks
```

This downloads dependencies and validates the build system.

## Building with Android Studio

### 1. Open Project

1. Launch Android Studio
2. Select **File → Open**
3. Navigate to `SOAnalyzer` directory
4. Click **OK**

### 2. Wait for Gradle Sync

- Android Studio will automatically sync Gradle
- Monitor progress in the **Gradle** panel
- Wait for "Gradle sync finished" message

### 3. Build Debug APK

```
Build → Make Project
```

Or use the toolbar button (green hammer icon).

**Output**: `app/build/outputs/apk/debug/app-debug.apk`

### 4. Run on Emulator/Device

1. Connect Android device or start emulator
2. Select **Run → Run 'app'** (or Shift+F10)
3. Choose target device
4. App launches automatically

### 5. Build Release APK

```
Build → Build Bundle(s) / APK(s) → Build APK(s)
```

**Output**: `app/build/outputs/apk/release/app-release.apk`

## Building from Command Line

### Linux/macOS

#### Debug Build

```bash
./gradlew assembleDebug
```

**Output**: `app/build/outputs/apk/debug/app-debug.apk`

#### Release Build (Unsigned)

```bash
./gradlew assembleRelease
```

**Output**: `app/build/outputs/apk/release/app-release.apk`

#### Clean Build

```bash
./gradlew clean assembleDebug
```

#### Build Script (Recommended)

```bash
# Build debug
./build.sh debug

# Build release (unsigned)
./build.sh release

# Build release (signed)
./build.sh release true
```

### Windows

#### Debug Build

```cmd
gradlew.bat assembleDebug
```

#### Release Build

```cmd
gradlew.bat assembleRelease
```

#### Build Script

```cmd
# Build debug
build.bat debug

# Build release
build.bat release
```

## Signing & Release

### Creating a Keystore

Generate a signing keystore for Play Store releases:

```bash
keytool -genkey -v -keystore soanalyzer-release.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias soanalyzer-key
```

**Prompts**:
- Keystore password: (create strong password)
- Key password: (same as keystore or different)
- Name: Your Name
- Organization: Your Organization
- City: Your City
- State: Your State
- Country: US
- Confirm: Yes

**Output**: `soanalyzer-release.jks`

### Signing Configuration File

Create `signing.sh` (Linux/macOS) or `signing.bat` (Windows):

**signing.sh**:
```bash
#!/bin/bash
export KEYSTORE_PATH="$(pwd)/soanalyzer-release.jks"
export KEYSTORE_PASS="your_keystore_password"
export KEY_ALIAS="soanalyzer-key"
export KEY_PASS="your_key_password"
```

**signing.bat**:
```batch
@echo off
set KEYSTORE_PATH=%CD%\soanalyzer-release.jks
set KEYSTORE_PASS=your_keystore_password
set KEY_ALIAS=soanalyzer-key
set KEY_PASS=your_key_password
```

### Building Signed Release

**Linux/macOS**:
```bash
source signing.sh
./build.sh release true
```

**Windows**:
```cmd
call signing.bat
build.bat release true
```

### Manual Signing (Alternative)

```bash
# Build unsigned release
./gradlew assembleRelease

# Sign with jarsigner
jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA-256 \
  -keystore soanalyzer-release.jks \
  app/build/outputs/apk/release/app-release-unsigned.apk \
  soanalyzer-key

# Verify signature
jarsigner -verify -verbose -certs \
  app/build/outputs/apk/release/app-release-unsigned.apk
```

### Optimizing Release APK

The build system automatically applies optimizations:

```gradle
buildTypes {
    release {
        minifyEnabled true      // Enable ProGuard
        shrinkResources true    // Remove unused resources
        proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
    }
}
```

## Troubleshooting

### Build Failures

#### Gradle Sync Failed

**Problem**: "Failed to sync Gradle"

**Solutions**:
1. Check internet connection
2. Update Gradle: `./gradlew wrapper --gradle-version 8.1.0`
3. Clear cache: `./gradlew clean`
4. Invalidate Android Studio cache: **File → Invalidate Caches**

#### Java Version Mismatch

**Problem**: "Unsupported class-file format"

**Solution**:
```bash
# Verify Java version
java -version

# Set JAVA_HOME if needed
export JAVA_HOME=/path/to/jdk17
```

#### NDK Not Found

**Problem**: "CMake Error: NDK not found"

**Solution**:
1. Open Android Studio
2. **Tools → SDK Manager → SDK Tools**
3. Install **NDK (Side by side)**
4. Set `ANDROID_NDK_HOME` environment variable

#### Out of Memory

**Problem**: "Java heap space" error

**Solution**:
```gradle
// In gradle.properties
org.gradle.jvmargs=-Xmx4096m
```

### Runtime Issues

#### App Crashes on Launch

**Problem**: "Unfortunately, SO Analyzer has stopped"

**Solutions**:
1. Check logcat: `adb logcat | grep soanalyzer`
2. Verify permissions granted
3. Check Android version (minimum API 31)
4. Rebuild: `./gradlew clean assembleDebug`

#### Permission Denied

**Problem**: "Permission denied" when accessing files

**Solutions**:
1. Grant `MANAGE_EXTERNAL_STORAGE` permission
2. On Android 11+: Settings → Apps → SO Analyzer → Permissions → All files
3. Verify `AndroidManifest.xml` includes permission

#### Native Library Not Loading

**Problem**: "java.lang.UnsatisfiedLinkError: libsoanalyzer.so"

**Solutions**:
1. Rebuild native library: `./gradlew clean assembleDebug`
2. Check CPU architecture (arm64-v8a vs armeabi-v7a)
3. Verify CMakeLists.txt configuration

## Development Workflow

### Setting Up Development Environment

1. **Clone repository**
   ```bash
   git clone https://github.com/yourusername/SOAnalyzer.git
   cd SOAnalyzer
   ```

2. **Create feature branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```

3. **Open in Android Studio**
   ```bash
   open -a "Android Studio" .
   ```

### Code Style & Standards

- **Language**: Kotlin (primary), Java (legacy)
- **Code Style**: Google Kotlin Style Guide
- **Formatting**: 4-space indentation
- **Naming**: camelCase for variables/functions, PascalCase for classes

### Building During Development

```bash
# Continuous build (watches for changes)
./gradlew build --continuous

# Build and install on connected device
./gradlew installDebug

# Run tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest
```

### Debugging

**Android Studio Debugger**:
1. Set breakpoint (click line number)
2. Run → Debug 'app'
3. App pauses at breakpoint
4. Inspect variables, step through code

**Logcat**:
```bash
# View all logs
adb logcat

# Filter by app
adb logcat | grep soanalyzer

# Filter by log level
adb logcat *:E  # Errors only
```

### Testing

```bash
# Unit tests
./gradlew test

# Instrumented tests (on device)
./gradlew connectedAndroidTest

# With coverage
./gradlew testDebugUnitTestCoverage
```

## CI/CD Setup

### GitHub Actions Workflow

Create `.github/workflows/build.yml`:

```yaml
name: Build APK

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Build APK
      run: |
        chmod +x gradlew
        ./gradlew assembleDebug
    
    - name: Upload APK
      uses: actions/upload-artifact@v3
      with:
        name: app-debug.apk
        path: app/build/outputs/apk/debug/app-debug.apk
```

### GitLab CI Configuration

Create `.gitlab-ci.yml`:

```yaml
image: android:latest

stages:
  - build
  - test

build_debug:
  stage: build
  script:
    - chmod +x gradlew
    - ./gradlew assembleDebug
  artifacts:
    paths:
      - app/build/outputs/apk/debug/app-debug.apk

test:
  stage: test
  script:
    - ./gradlew test
```

## Performance Optimization

### Build Time Optimization

```gradle
// gradle.properties
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.jvmargs=-Xmx4096m
android.useAndroidX=true
```

### APK Size Optimization

```gradle
buildTypes {
    release {
        minifyEnabled true
        shrinkResources true
        proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
    }
}
```

### Native Library Optimization

```cmake
# In CMakeLists.txt
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O3 -fvisibility=hidden")
```

## Deployment Checklist

Before releasing to Play Store:

- [ ] Version code incremented
- [ ] Version name updated
- [ ] Changelog written
- [ ] All tests passing
- [ ] ProGuard rules verified
- [ ] APK signed with release key
- [ ] APK tested on multiple devices
- [ ] Permissions documented
- [ ] Privacy policy prepared
- [ ] Screenshots prepared
- [ ] Description written

## Additional Resources

- [Android Developer Documentation](https://developer.android.com/docs)
- [Gradle Build System](https://gradle.org/)
- [Android NDK Guide](https://developer.android.com/ndk/guides)
- [Kotlin Documentation](https://kotlinlang.org/docs/)
- [Material Design 3](https://m3.material.io/)

## Getting Help

- **Issues**: Create GitHub issue with detailed description
- **Discussions**: Use GitHub Discussions for questions
- **Documentation**: Check ARCHITECTURE.md for design details
- **Community**: Android Developers subreddit, Stack Overflow
