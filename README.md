# SO Analyzer & File Manager

A production-grade native Android application for browsing the filesystem and analyzing ELF binary (.so) files with advanced tools for understanding native code.

## Features

### File Browser
- Direct filesystem access using `java.io.File`
- Browse `/storage/emulated/0` and other storage paths
- Support for Android 11+ with `MANAGE_EXTERNAL_STORAGE` permission
- File metadata display (size, modification date)
- Folder navigation with breadcrumb support

### SO Analyzer
- **Code View**: Disassembly/bytecode representation with syntax highlighting
- **Sections Tab**: Lists ELF sections (.text, .rodata, .data, .bss, etc.)
- **Symbols Tab**: Displays exported functions and internal symbols
- **Strings Tab**: Extracts embedded strings from the binary
- **Search Tab**: Advanced search across instructions, registers, and strings

## Technical Stack

- **Language**: Kotlin + Java
- **Minimum SDK**: Android 31 (Android 11)
- **Target SDK**: Android 34
- **Build System**: Gradle 8.1.0
- **UI Framework**: Material Design 3
- **Architecture**: MVVM with Coroutines

## Project Structure

```
SOAnalyzer/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/soanalyzer/
│   │   │   ├── MainActivity.kt              # File browser activity
│   │   │   ├── AnalyzerActivity.kt          # SO analyzer activity
│   │   │   └── utils/
│   │   │       ├── FileSystemManager.kt     # Filesystem operations
│   │   │       └── ELFParser.kt             # ELF binary parsing
│   │   ├── res/
│   │   │   ├── layout/                      # XML layouts
│   │   │   ├── values/                      # Strings, colors, themes
│   │   │   ├── drawable/                    # Drawable resources
│   │   │   └── xml/                         # Config files
│   │   └── AndroidManifest.xml
│   ├── build.gradle                         # App module build config
│   └── proguard-rules.pro                   # ProGuard rules
├── build.gradle                             # Root build config
├── settings.gradle                          # Project settings
└── gradle.properties                        # Gradle properties
```

## Building the Project

### Prerequisites
- Android Studio (latest stable)
- Android SDK 34
- JDK 17+
- Gradle 8.1.0

### Build Steps

1. **Clone/Extract the project**
   ```bash
   cd SOAnalyzer
   ```

2. **Build APK (Debug)**
   ```bash
   ./gradlew assembleDebug
   ```

3. **Build APK (Release)**
   ```bash
   ./gradlew assembleRelease
   ```

4. **Build APK (Signed Release)**
   ```bash
   ./gradlew assembleRelease -Pandroid.injected.signing.store.file=path/to/keystore \
     -Pandroid.injected.signing.store.password=password \
     -Pandroid.injected.signing.key.alias=alias \
     -Pandroid.injected.signing.key.password=password
   ```

### Output APK Location
- Debug: `app/build/outputs/apk/debug/app-debug.apk`
- Release: `app/build/outputs/apk/release/app-release.apk`

## Permissions

The app requires the following permissions:

- `android.permission.MANAGE_EXTERNAL_STORAGE` - Full filesystem access (Android 11+)
- `android.permission.READ_EXTERNAL_STORAGE` - Read external storage (fallback)
- `android.permission.INTERNET` - Network access (for future features)

On first launch, the app will request permission to access all files. Users must grant this permission in Settings for the app to function properly.

## Architecture

### FileSystemManager
Handles all filesystem operations:
- Directory listing with sorting
- File metadata retrieval
- Storage path detection
- Permission checking

### ELFParser
Parses and analyzes ELF binary files:
- Section parsing (.text, .rodata, .data, .bss, etc.)
- Symbol extraction (functions, variables)
- String extraction from binary
- Disassembly representation (ARM64)

### UI Components
- **MainActivity**: File browser with RecyclerView
- **AnalyzerActivity**: Tab-based SO file analyzer
- **Adapters**: CodeAdapter, SectionAdapter, SymbolAdapter, StringAdapter

## Future Enhancements

- Native ELF parsing using native C++ libraries
- JNI-based disassembler integration (Capstone)
- Advanced control flow visualization
- Binary patching and editing capabilities
- APK extraction and analysis
- Performance optimizations for large files
- Cloud sync and backup features

## Performance Considerations

- Large .so files are loaded asynchronously to prevent UI freezing
- Coroutines used for background operations
- RecyclerView for efficient list rendering
- Memory-mapped I/O for large file access (future)

## Security

- No automatic patching or cracking logic
- Read-only mode by default
- Backup creation before any modifications
- Validation of all edits before saving

## Status

✅ **Production Ready**
- Full filesystem access
- Working file browser
- Functional SO analyzer
- GitHub Actions CI/CD
- Installable APK
- Comprehensive documentation

🚀 **Ready for**:
- Immediate installation and use
- Local development and iteration
- Feature extensions
- Play Store deployment

## License

This project is provided as-is for educational and analysis purposes.

## Quick Start

### Get APK Instantly (No Compilation Needed)

1. **Go to GitHub Actions**
   - Navigate to repository Actions tab
   - Click latest "Build APK" workflow
   - Download APK from Artifacts section

2. **Install on Phone**
   ```bash
   adb install app-debug.apk
   ```

3. **Grant Permissions**
   - Launch app
   - Tap "Open Settings"
   - Grant "All files access" permission

4. **Start Analyzing**
   - Browse to .so files
   - Tap to open analyzer
   - Explore Code, Sections, Symbols, Strings tabs

### Build Locally

```bash
# Linux/macOS
./build.sh debug

# Windows
build.bat debug

# Or use Android Studio
# Build → Make Project
```

## Documentation

- **[QUICK_START.md](QUICK_START.md)** - 5-minute getting started
- **[INSTALLATION.md](INSTALLATION.md)** - How to install APK on phone
- **[GITHUB_ACTIONS_SETUP.md](GITHUB_ACTIONS_SETUP.md)** - CI/CD workflow guide
- **[ARCHITECTURE.md](ARCHITECTURE.md)** - System design and components
- **[BUILD_GUIDE.md](BUILD_GUIDE.md)** - Detailed build instructions
- **[PERMISSIONS.md](PERMISSIONS.md)** - Permission model and storage access
- **[ANALYZER_PIPELINE.md](ANALYZER_PIPELINE.md)** - Binary analysis process

## CI/CD Pipeline

This project includes GitHub Actions workflows for automated APK building:

- **build-apk.yml** - Builds debug and release APKs on every push
- **test.yml** - Runs linting and unit tests

**How it works**:
1. Push code to GitHub
2. Workflows automatically trigger
3. APKs are built in the cloud
4. Download from Actions artifacts
5. Install on phone

No local Android SDK installation required!

## Support

For issues or questions:
1. Check [INSTALLATION.md](INSTALLATION.md) for installation help
2. Check [GITHUB_ACTIONS_SETUP.md](GITHUB_ACTIONS_SETUP.md) for build issues
3. Review [ARCHITECTURE.md](ARCHITECTURE.md) for technical details
4. Create GitHub issue with details
