# SO Analyzer - Quick Start Guide

## What is SO Analyzer?

SO Analyzer is a production-grade native Android application for browsing the filesystem and analyzing ELF binary (.so) files. It provides professional-grade tools for understanding native code, similar to ZArchiver but with advanced binary analysis capabilities.

## Key Features

✅ **File Manager**
- Direct filesystem access (`java.io.File`)
- Browse `/storage/emulated/0` and system directories
- Full support for Android 11+ (`MANAGE_EXTERNAL_STORAGE`)

✅ **Binary Analysis**
- ELF section parsing (.text, .rodata, .data, .bss, etc.)
- Symbol extraction (functions, variables)
- String extraction from binaries
- ARM64/ARM32/x86 disassembly (Capstone-ready)

✅ **Interactive Exploration**
- Code view with syntax highlighting
- Tab-based interface (Code, Sections, Symbols, Strings, Search)
- Jump-to-symbol navigation
- Advanced search with filtering

✅ **Native Integration**
- JNI/C++ layer for performance
- CMake build system
- Ready for Capstone disassembler integration
- SHA-256 hashing for binary integrity

## Project Structure

```
SOAnalyzer/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/soanalyzer/
│   │   │   ├── MainActivity.kt              # File browser
│   │   │   ├── AnalyzerActivity.kt          # SO analyzer
│   │   │   └── utils/
│   │   │       ├── FileSystemManager.kt     # Filesystem ops
│   │   │       ├── ELFParser.kt             # ELF parsing
│   │   │       ├── AdvancedELFAnalyzer.kt   # Deep analysis
│   │   │       └── NativeDisassembler.kt    # JNI interface
│   │   ├── cpp/
│   │   │   ├── disassembler.cpp             # Native code
│   │   │   └── CMakeLists.txt               # Build config
│   │   ├── res/                             # UI resources
│   │   └── AndroidManifest.xml
│   ├── build.gradle                         # Build config
│   └── proguard-rules.pro                   # Obfuscation
├── build.gradle                             # Root config
├── settings.gradle                          # Project settings
├── gradle.properties                        # Gradle props
├── build.sh                                 # Linux/macOS build
├── build.bat                                # Windows build
├── README.md                                # Overview
├── QUICK_START.md                           # This file
├── ARCHITECTURE.md                          # Design details
├── BUILD_GUIDE.md                           # Build instructions
├── PERMISSIONS.md                           # Permission model
└── ANALYZER_PIPELINE.md                     # Analysis process
```

## Getting Started (5 Minutes)

### 1. Extract Project

```bash
tar -xzf SOAnalyzer.tar.gz
cd SOAnalyzer
```

### 2. Build Debug APK

**Linux/macOS**:
```bash
./build.sh debug
```

**Windows**:
```cmd
build.bat debug
```

**Or use Android Studio**:
1. Open project in Android Studio
2. Click Build → Make Project
3. APK generated at `app/build/outputs/apk/debug/app-debug.apk`

### 3. Install on Device/Emulator

```bash
# Via adb
adb install app/build/outputs/apk/debug/app-debug.apk

# Or via Android Studio
Run → Run 'app'
```

### 4. Grant Permissions

1. Launch app
2. Tap "Open Settings"
3. Navigate to All files access
4. Toggle permission on
5. Return to app

### 5. Start Analyzing

1. Browse to a .so file
2. Tap to open analyzer
3. Explore Code, Sections, Symbols, Strings tabs
4. Use Search to find instructions/strings

## Build Options

### Debug Build (Development)

```bash
./build.sh debug
# Output: app/build/outputs/apk/debug/app-debug.apk
```

- No optimization
- Debuggable
- Larger APK size
- Faster compilation

### Release Build (Unsigned)

```bash
./build.sh release
# Output: app/build/outputs/apk/release/app-release.apk
```

- Optimized (ProGuard)
- Unsigned
- Smaller APK size
- Not distributable

### Release Build (Signed)

```bash
# Create keystore (one-time)
keytool -genkey -v -keystore soanalyzer-release.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias soanalyzer-key

# Build and sign
export KEYSTORE_PATH="$(pwd)/soanalyzer-release.jks"
export KEYSTORE_PASS="your_password"
export KEY_ALIAS="soanalyzer-key"
export KEY_PASS="your_key_password"

./build.sh release true
# Output: app/build/outputs/apk/release/app-release.apk
```

- Optimized
- Signed
- Play Store ready
- Distributable

## System Requirements

### Minimum
- Android 11 (API 31)
- 100 MB free storage
- 2 GB RAM

### Recommended
- Android 12+ (API 32+)
- 500 MB free storage
- 4 GB RAM

### Development
- JDK 17+
- Android SDK 34
- Android NDK
- CMake 3.22.1+

## Permissions

### MANAGE_EXTERNAL_STORAGE (Required)
- Full filesystem access
- Android 11+
- Requested on first launch

### READ_EXTERNAL_STORAGE (Fallback)
- Read-only access
- Android 10 and below
- Automatic fallback

### INTERNET (Optional)
- Reserved for future features
- Not currently used

## Architecture Overview

```
┌─────────────────────────────────────┐
│         Android UI Layer            │
│  MainActivity | AnalyzerActivity    │
├─────────────────────────────────────┤
│      Kotlin/Java Business Logic     │
│  FileSystemManager | ELFParser      │
├─────────────────────────────────────┤
│         JNI Bridge (C++)            │
│  NativeDisassembler                 │
├─────────────────────────────────────┤
│      Native Libraries (Future)      │
│  Capstone | LLVM | Custom Parsers   │
└─────────────────────────────────────┘
```

## Key Components

### FileSystemManager
- Direct `java.io.File` access
- Permission checking
- File metadata extraction
- Storage path discovery

### ELFParser
- Basic ELF parsing
- Section extraction
- Symbol detection
- String extraction

### AdvancedELFAnalyzer
- Complete ELF header parsing
- Program header analysis
- Dynamic symbol extraction
- Relocation parsing

### NativeDisassembler
- JNI interface to native code
- Capstone integration (ready)
- String extraction
- SHA-256 hashing

## Development Workflow

### 1. Clone/Extract
```bash
git clone https://github.com/yourusername/SOAnalyzer.git
cd SOAnalyzer
```

### 2. Open in Android Studio
```bash
open -a "Android Studio" .  # macOS
# or use File → Open in Android Studio
```

### 3. Make Changes
- Edit Kotlin files in `app/src/main/java/`
- Update layouts in `app/src/main/res/layout/`
- Modify native code in `app/src/main/cpp/`

### 4. Build & Test
```bash
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

### 5. Debug
- Set breakpoints in Android Studio
- Run → Debug 'app'
- Inspect variables and step through code

## Common Tasks

### Add New UI Tab

1. Create layout XML in `res/layout/`
2. Create adapter class in `java/`
3. Add tab to `AnalyzerActivity`
4. Implement tab content display

### Integrate Capstone Disassembler

1. Add Capstone to CMakeLists.txt
2. Implement disassembly in disassembler.cpp
3. Call via NativeDisassembler.disassembleARM64()
4. Update UI to display results

### Add New Analysis Feature

1. Create analyzer class in `utils/`
2. Implement analysis logic
3. Add results to ELFAnalysis data class
4. Display in new UI tab

### Build for Play Store

1. Create release keystore
2. Update version in build.gradle
3. Build signed release APK
4. Test on multiple devices
5. Upload to Play Store Console

## Troubleshooting

### "Permission Denied" Error
- Grant MANAGE_EXTERNAL_STORAGE in Settings
- Verify app has file access permission
- Check Android version (minimum 11)

### App Crashes on Launch
- Check logcat: `adb logcat | grep soanalyzer`
- Verify Android SDK installed
- Rebuild: `./gradlew clean assembleDebug`

### Build Fails
- Update Gradle: `./gradlew wrapper --gradle-version 8.1.0`
- Clear cache: `./gradlew clean`
- Invalidate Android Studio cache: File → Invalidate Caches

### Native Library Not Loading
- Rebuild: `./gradlew clean assembleDebug`
- Check CPU architecture (arm64-v8a)
- Verify CMakeLists.txt configuration

## Performance Tips

### For Large Binaries
- Use lazy loading for sections
- Disassemble visible range only
- Cache parsed results
- Use memory mapping for huge files

### For Smooth UI
- Async parsing with Coroutines
- RecyclerView for efficient rendering
- Background threads for heavy operations
- Smooth scrolling with predictive loading

## Security Considerations

- **No Automatic Patching**: Analysis-only by design
- **No Network Access**: Offline operation
- **No Data Collection**: User data stays on device
- **Read-Only by Default**: Safe exploration

## Next Steps

1. **Read Documentation**
   - ARCHITECTURE.md - System design
   - BUILD_GUIDE.md - Detailed build steps
   - PERMISSIONS.md - Permission model
   - ANALYZER_PIPELINE.md - Analysis process

2. **Explore Code**
   - MainActivity.kt - File browser
   - AnalyzerActivity.kt - Binary analyzer
   - ELFParser.kt - ELF parsing
   - NativeDisassembler.kt - JNI interface

3. **Extend Features**
   - Add Capstone integration
   - Implement new analysis tools
   - Create custom UI tabs
   - Build analysis plugins

4. **Test & Deploy**
   - Test on multiple Android versions
   - Test with various binary formats
   - Optimize for performance
   - Deploy to Play Store

## Resources

- [Android Developer Docs](https://developer.android.com/docs)
- [Capstone Disassembler](http://www.capstone-engine.org/)
- [ELF Format](https://refspecs.linuxbase.org/elf/gabi4+/)
- [ARM64 ISA](https://developer.arm.com/documentation/ddi0602/latest/)
- [Kotlin Documentation](https://kotlinlang.org/docs/)

## Support & Feedback

- **Issues**: Create GitHub issue with details
- **Questions**: Use GitHub Discussions
- **Feedback**: Submit feature requests
- **Contributions**: Pull requests welcome

## License

This project is provided as-is for educational and analysis purposes.

---

**Ready to build?** Start with `./build.sh debug` and explore the code!
