# SO Analyzer - Architecture Documentation

## Overview

SO Analyzer is a production-grade native Android application designed for filesystem browsing and ELF binary analysis. The architecture emphasizes direct filesystem access, efficient binary parsing, and extensible native code integration.

## System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                      Android Application Layer              │
├─────────────────────────────────────────────────────────────┤
│  MainActivity (File Browser) │ AnalyzerActivity (SO Analysis)│
├─────────────────────────────────────────────────────────────┤
│                      Kotlin/Java Business Logic             │
├─────────────────────────────────────────────────────────────┤
│  FileSystemManager  │  ELFParser  │  AdvancedELFAnalyzer   │
├─────────────────────────────────────────────────────────────┤
│                      JNI Bridge Layer                        │
├─────────────────────────────────────────────────────────────┤
│  NativeDisassembler (C++ via CMake)                         │
├─────────────────────────────────────────────────────────────┤
│  Native Libraries (Capstone, LLVM, Custom Parsers)          │
└─────────────────────────────────────────────────────────────┘
```

## Component Breakdown

### 1. UI Layer (Activities & Fragments)

#### MainActivity
- **Responsibility**: File system browsing and navigation
- **Key Features**:
  - Directory listing with RecyclerView
  - Breadcrumb navigation
  - File type detection and routing
  - Storage permission handling
- **Lifecycle**: Launched on app start, manages file browser state
- **Data Flow**: User interaction → FileSystemManager → UI update

```kotlin
MainActivity
├── setupUI()           // Initialize UI components
├── checkStoragePermission()  // Verify MANAGE_EXTERNAL_STORAGE
├── loadDirectory()     // Fetch and display files
└── onFileItemClicked() // Route to analyzer or open file
```

#### AnalyzerActivity
- **Responsibility**: SO file analysis and visualization
- **Key Features**:
  - Tab-based interface (Code, Sections, Symbols, Strings, Search)
  - Asynchronous binary parsing
  - Search with highlighting
  - Responsive scrolling for large files
- **Data Flow**: File path → ELFParser → Analysis results → UI tabs

```kotlin
AnalyzerActivity
├── setupUI()           // Initialize tabs and content containers
├── loadAnalysis()      // Parse binary asynchronously
├── showTabContent()    // Display selected tab
├── showCodeView()      // Render disassembly
├── showSectionsView()  // Display ELF sections
├── showSymbolsView()   // Display symbols/functions
├── showStringsView()   // Display extracted strings
└── showSearchView()    // Search interface
```

### 2. Business Logic Layer

#### FileSystemManager
**Purpose**: Abstraction for filesystem operations with permission handling

```kotlin
FileSystemManager
├── hasFullStorageAccess()    // Check MANAGE_EXTERNAL_STORAGE
├── listDirectory()           // Get files in directory
├── getFileSize()             // Format file size
├── getFormattedDate()        // Format timestamps
├── readFileAsBytes()         // Read file content
├── isSOFile()                // Detect .so files
└── getStorageRoots()         // Find available storage paths
```

**Key Responsibilities**:
- Direct `java.io.File` access (no SAF/scoped storage)
- Permission verification for Android 11+
- File metadata extraction
- Storage path discovery

#### ELFParser
**Purpose**: Basic ELF binary parsing with mock data

```kotlin
ELFParser
├── parse()              // Main parsing entry point
├── isValidELF()         // Validate ELF magic number
├── parseSections()      // Extract ELF sections
├── parseSymbols()       // Extract symbols/functions
├── extractStrings()     // Extract embedded strings
├── parseCode()          // Generate disassembly representation
└── search()             // Search in code/symbols/strings
```

**Current Implementation**: Mock data for demonstration
**Future**: Integration with native C++ parsers

#### AdvancedELFAnalyzer
**Purpose**: Deep ELF analysis with complete header parsing

```kotlin
AdvancedELFAnalyzer
├── parseCompleteHeader()      // Full ELF header parsing
├── parseProgramHeaders()      // Parse program headers
├── parseSectionHeaders()      // Parse section headers
├── extractDynamicSymbols()    // Extract .dynsym entries
├── analyzeRelocations()       // Parse relocation entries
└── [private helpers]
    ├── readHalfWord()         // Read 16-bit values
    └── readWord()             // Read 32-bit values
```

**Data Structures**:
- `ELFHeaderInfo`: Complete ELF header information
- `ProgramHeader`: Program segment information
- `SectionHeader`: Section information
- `DynamicSymbol`: Dynamic symbol entry
- `RelocationEntry`: Relocation information

### 3. Native Layer (JNI/C++)

#### NativeDisassembler (Kotlin Object)
**Purpose**: JNI interface to native C++ functions

```kotlin
NativeDisassembler
├── disassembleARM64()    // JNI: Capstone-based disassembly
├── parseELFHeader()      // JNI: Parse ELF header
├── extractStrings()      // JNI: Extract strings from binary
├── calculateHash()       // JNI: SHA-256 hashing
└── disassembleARM64Fallback()  // Kotlin fallback
```

**Library Loading**:
```kotlin
init {
    System.loadLibrary("soanalyzer")  // Load native library
}
```

#### disassembler.cpp (C++)
**Purpose**: Native implementation of binary analysis functions

```cpp
extern "C" {
    JNIEXPORT jobjectArray JNICALL
    Java_com_example_soanalyzer_utils_NativeDisassembler_disassembleARM64(...)
    
    JNIEXPORT jintArray JNICALL
    Java_com_example_soanalyzer_utils_NativeDisassembler_parseELFHeader(...)
    
    JNIEXPORT jobjectArray JNICALL
    Java_com_example_soanalyzer_utils_NativeDisassembler_extractStrings(...)
    
    JNIEXPORT jstring JNICALL
    Java_com_example_soanalyzer_utils_NativeDisassembler_calculateHash(...)
}
```

**Build Configuration**: CMake 3.22.1
- C++17 standard
- Android NDK integration
- Future: Capstone library linking

### 4. Data Flow Patterns

#### File Browsing Flow
```
User Action
    ↓
MainActivity.onFileItemClicked()
    ↓
FileSystemManager.listDirectory()
    ↓
java.io.File operations
    ↓
FileAdapter.submitList()
    ↓
RecyclerView update
```

#### Binary Analysis Flow
```
User selects .so file
    ↓
MainActivity routes to AnalyzerActivity
    ↓
AnalyzerActivity.loadAnalysis()
    ↓
ELFParser.parse() (async via Coroutine)
    ↓
AdvancedELFAnalyzer for deep analysis
    ↓
NativeDisassembler for native operations
    ↓
UI tabs populated with results
```

#### Search Flow
```
User enters search query
    ↓
AnalyzerActivity.showSearchView()
    ↓
ELFParser.search()
    ↓
Filter CodeInstruction list
    ↓
CodeAdapter displays results
```

## Concurrency Model

### Coroutines for Async Operations
```kotlin
GlobalScope.launch {
    val analysis = withContext(Dispatchers.IO) {
        parser.parse()  // Heavy I/O operation
    }
    withContext(Dispatchers.Main) {
        // Update UI on main thread
    }
}
```

**Thread Safety**:
- File I/O: Dispatchers.IO (thread pool)
- UI updates: Dispatchers.Main (main thread)
- No blocking operations on main thread

## Permission Model

### Android 11+ (API 31+)
```kotlin
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
    if (!Environment.isExternalStorageManager()) {
        // Request MANAGE_EXTERNAL_STORAGE
        startActivity(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
    }
}
```

### Fallback (API < 31)
```kotlin
ActivityCompat.requestPermissions(
    this,
    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
    PERMISSION_REQUEST_CODE
)
```

## Storage Access Strategy

### Direct Filesystem Access
```kotlin
val file = File("/storage/emulated/0/example.so")
val bytes = file.readBytes()
```

### Supported Paths
- `/storage/emulated/0` (primary external storage)
- `/storage` (storage root)
- `/Android/data` (app-specific data)
- `/Android/obb` (app-specific OBB files)

### No SAF/Scoped Storage
- Direct `java.io.File` operations
- Full filesystem visibility
- Required for professional file manager functionality

## Extension Points

### Adding New Binary Formats
1. Create new parser class extending `BinaryParser`
2. Implement format-specific parsing logic
3. Register in `AnalyzerActivity.onFileItemClicked()`

### Integrating Capstone Disassembler
1. Add Capstone library via CMake
2. Implement disassembly in `disassembler.cpp`
3. Call via `NativeDisassembler.disassembleARM64()`

### Adding Custom Analysis Tools
1. Create analysis class in `utils/` package
2. Implement analysis logic
3. Add UI tab in `AnalyzerActivity`

## Performance Considerations

### Large File Handling
- Async parsing with Coroutines
- Lazy loading of sections/symbols
- Memory-efficient string extraction
- Future: Memory-mapped I/O for very large files

### UI Responsiveness
- RecyclerView for efficient list rendering
- ViewHolder pattern for item reuse
- No blocking operations on main thread
- Smooth scrolling with DiffUtil (future)

### Binary Analysis
- Native C++ for computationally intensive tasks
- Capstone for fast disassembly
- Caching of parsed results
- Incremental parsing for large binaries

## Security Architecture

### No Automated Patching
- Analysis-only by design
- No automatic code modification
- User-initiated actions only

### Data Integrity
- SHA-256 hashing via JNI
- Backup creation before modifications (future)
- Validation of all edits

### Permission Isolation
- MANAGE_EXTERNAL_STORAGE for filesystem access
- No network access by default
- No sensitive data storage

## Build System

### Gradle Configuration
```gradle
android {
    compileSdk 34
    minSdk 31
    targetSdk 34
    
    externalNativeBuild {
        cmake {
            path "src/main/cpp/CMakeLists.txt"
        }
    }
}
```

### CMake Build
```cmake
add_library(soanalyzer SHARED
    disassembler.cpp
)
target_link_libraries(soanalyzer ${log-lib})
```

### Output Artifacts
- Debug APK: `app/build/outputs/apk/debug/app-debug.apk`
- Release APK: `app/build/outputs/apk/release/app-release.apk`
- Native library: `app/build/intermediates/cmake/release/obj/arm64-v8a/libsoanalyzer.so`

## Testing Strategy

### Unit Tests
- FileSystemManager operations
- ELFParser logic
- String extraction algorithms

### Integration Tests
- Full file browsing workflow
- Binary analysis pipeline
- Permission handling

### Manual Testing
- Large file handling (100MB+ .so files)
- Permission scenarios (granted/denied)
- UI responsiveness on low-end devices

## Future Roadmap

### Phase 1: Core Enhancement
- [ ] Capstone integration for real disassembly
- [ ] Complete ELF header parsing
- [ ] Symbol demangling (C++ names)
- [ ] Control flow visualization

### Phase 2: Advanced Analysis
- [ ] Binary patching with diff view
- [ ] JNI method detection
- [ ] Vulnerability scanning
- [ ] Performance profiling

### Phase 3: Ecosystem
- [ ] Cloud sync and backup
- [ ] Collaborative analysis
- [ ] Plugin system
- [ ] Export to multiple formats

## Deployment

### Release Build Process
1. Update version in `build.gradle`
2. Create signing keystore
3. Run `./build.sh release true`
4. Sign APK with release key
5. Upload to Play Store or distribute directly

### CI/CD Integration
- GitHub Actions workflow
- Automated builds on push
- APK artifact generation
- Automated testing

## References

- [Android NDK Documentation](https://developer.android.com/ndk)
- [Capstone Disassembly Engine](http://www.capstone-engine.org/)
- [ELF Format Specification](https://refspecs.linuxbase.org/elf/gabi4+/)
- [ARM64 ISA Reference](https://developer.arm.com/documentation/ddi0602/latest/)
