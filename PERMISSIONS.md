# SO Analyzer - Permissions & Storage Access Guide

## Overview

SO Analyzer requires specific permissions to function as a professional file manager and binary analyzer. This document explains the permission model, storage access strategy, and user-facing permission flows.

## Required Permissions

### MANAGE_EXTERNAL_STORAGE (Android 11+)

**Permission**: `android.permission.MANAGE_EXTERNAL_STORAGE`

**Purpose**: Full access to all files on the device

**Android Versions**:
- Android 11+ (API 31+): Required for full filesystem access
- Android 10 and below: Falls back to READ_EXTERNAL_STORAGE

**User Experience**:
1. App requests permission on first launch
2. User directed to Settings → Apps → SO Analyzer → Permissions → All files
3. User toggles "Allow access to manage all files"
4. App verifies access and enables full functionality

**Code Implementation**:
```kotlin
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
    if (!Environment.isExternalStorageManager()) {
        val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
        startActivity(intent)
    }
}
```

### READ_EXTERNAL_STORAGE (Fallback)

**Permission**: `android.permission.READ_EXTERNAL_STORAGE`

**Purpose**: Read access to external storage

**Used When**: MANAGE_EXTERNAL_STORAGE not available or not granted

**Android Versions**: API 19+

**Code Implementation**:
```kotlin
if (ContextCompat.checkSelfPermission(
    this,
    Manifest.permission.READ_EXTERNAL_STORAGE
) != PackageManager.PERMISSION_GRANTED) {
    ActivityCompat.requestPermissions(
        this,
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
        PERMISSION_REQUEST_CODE
    )
}
```

### INTERNET (Optional)

**Permission**: `android.permission.INTERNET`

**Purpose**: Network access for future cloud features

**Current Use**: None (reserved for future)

**Future Uses**:
- Cloud sync and backup
- Remote analysis
- Update checking

## Storage Access Strategy

### Direct Filesystem Access (No SAF)

SO Analyzer uses direct `java.io.File` access instead of Storage Access Framework (SAF) for several reasons:

**Advantages**:
- Full filesystem visibility (required for file manager)
- Fast file operations (no content provider overhead)
- Direct binary file access (no URI translation)
- Professional-grade file management
- Batch operations support

**Implementation**:
```kotlin
// Direct file access
val file = File("/storage/emulated/0/example.so")
val bytes = file.readBytes()

// No SAF/content URIs
// No DocumentFile wrapper
// Direct java.io.File operations
```

### Supported Storage Paths

#### Primary External Storage
```
/storage/emulated/0
/storage/emulated/0/DCIM
/storage/emulated/0/Download
/storage/emulated/0/Documents
```

#### Android System Directories
```
/Android/data              (App-specific data)
/Android/obb               (App-specific OBB files)
/Android/media             (Media files)
```

#### Storage Root
```
/storage                   (All storage devices)
/storage/emulated/0        (Primary emulated storage)
/storage/XXXX-XXXX         (SD cards, USB drives)
```

#### System Directories
```
/system/lib                (System libraries)
/system/lib64              (64-bit system libraries)
/vendor/lib                (Vendor libraries)
```

### Filesystem Hierarchy

```
/storage/
├── emulated/
│   ├── 0/                 (Primary user storage)
│   │   ├── DCIM/
│   │   ├── Download/
│   │   ├── Documents/
│   │   ├── Pictures/
│   │   ├── Movies/
│   │   ├── Music/
│   │   └── *.so files
│   └── legacy/            (Compatibility layer)
├── XXXX-XXXX/             (SD card)
└── ...
```

## Permission Flow

### First Launch Permission Check

```
App Launch
    ↓
MainActivity.onCreate()
    ↓
checkStoragePermission()
    ↓
Build.VERSION >= R (Android 11)?
    ├─ YES → Environment.isExternalStorageManager()?
    │   ├─ YES → Proceed to file browser
    │   └─ NO → Show permission dialog
    │       ↓
    │       User clicks "Open Settings"
    │       ↓
    │       Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
    │       ↓
    │       User grants permission
    │       ↓
    │       Return to app
    │       ↓
    │       Proceed to file browser
    │
    └─ NO → Request READ_EXTERNAL_STORAGE
        ↓
        ActivityCompat.requestPermissions()
        ↓
        User grants/denies
        ↓
        onRequestPermissionsResult()
```

### Permission Dialog UI

```kotlin
private fun showPermissionDialog() {
    val builder = AlertDialog.Builder(this)
    builder.setTitle(R.string.permission_required)
    builder.setMessage(R.string.permission_message)
    builder.setPositiveButton(R.string.open_settings) { _, _ ->
        val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
        startActivity(intent)
    }
    builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
    builder.show()
}
```

### Runtime Permission Handling

```kotlin
override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<String>,
    grantResults: IntArray
) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (requestCode == PERMISSION_REQUEST_CODE) {
        if (grantResults.isNotEmpty() && 
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadDirectory(currentPath)
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }
}
```

## Permission Manifest Declaration

### AndroidManifest.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- Storage permissions for Android 11+ -->
    <uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    
    <!-- Optional: Internet for future features -->
    <uses-permission android:name="android.permission.INTERNET" />

    <application>
        <!-- Activities and other components -->
    </application>

</manifest>
```

## Scoped Storage Considerations

### Why Not Scoped Storage?

Scoped Storage (introduced in Android 11) restricts app access to:
- App-specific directories
- Downloads folder
- Media collections

**Limitations for File Manager**:
- Cannot browse arbitrary directories
- Cannot access system libraries
- Cannot analyze .so files in app data
- Requires SAF for user-selected files
- Performance overhead

**SO Analyzer Decision**: Use MANAGE_EXTERNAL_STORAGE for full access

### Alternative: Hybrid Approach

For future versions supporting both scoped and unscoped:

```kotlin
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    // Android 12+: Use scoped storage with SAF fallback
    if (Environment.isExternalStorageManager()) {
        // Full access available
        useDirectFileAccess()
    } else {
        // Fallback to SAF
        useSAFAccess()
    }
} else {
    // Android 11 and below: Direct access
    useDirectFileAccess()
}
```

## User Privacy & Security

### Data Access Transparency

SO Analyzer:
- **Does NOT** collect user data
- **Does NOT** transmit files to external servers
- **Does NOT** modify files without user action
- **Does NOT** access sensitive app data
- **Does NOT** require login or accounts

### Minimal Permission Principle

Only requested permissions:
- `MANAGE_EXTERNAL_STORAGE` - Essential for file manager
- `READ_EXTERNAL_STORAGE` - Fallback for older Android
- `INTERNET` - Reserved for future, currently unused

### User Control

Users can:
- Revoke permissions anytime in Settings
- Restrict app to specific directories (via SAF)
- Clear app cache and data
- Uninstall app completely

## Testing Permission Scenarios

### Test Case 1: Permission Granted

```
1. Install app
2. Launch app
3. Grant MANAGE_EXTERNAL_STORAGE permission
4. Verify file browser loads
5. Verify can navigate directories
6. Verify can open .so files
```

### Test Case 2: Permission Denied

```
1. Install app
2. Launch app
3. Deny MANAGE_EXTERNAL_STORAGE permission
4. Verify permission dialog shown
5. Verify app handles gracefully
6. Verify can retry from settings
```

### Test Case 3: Permission Revoked

```
1. Install app
2. Grant permission
3. Use app normally
4. Go to Settings → Apps → SO Analyzer → Permissions
5. Revoke MANAGE_EXTERNAL_STORAGE
6. Return to app
7. Verify app detects revocation
8. Verify shows permission dialog again
```

### Test Case 4: Multiple Devices

```
Android 11 (API 31):
- Requires MANAGE_EXTERNAL_STORAGE
- Must handle Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION

Android 12 (API 32):
- Same as Android 11

Android 13+ (API 33+):
- Same permission model
- May add additional restrictions in future
```

## Troubleshooting Permission Issues

### Problem: "Permission Denied" When Accessing Files

**Causes**:
1. MANAGE_EXTERNAL_STORAGE not granted
2. READ_EXTERNAL_STORAGE not granted
3. Permission revoked after grant

**Solutions**:
```kotlin
// Check permission status
val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
    Environment.isExternalStorageManager()
} else {
    ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.READ_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED
}

if (!hasPermission) {
    showPermissionDialog()
}
```

### Problem: Cannot Access Specific Directory

**Causes**:
1. Directory doesn't exist
2. Permission not granted for that path
3. Directory is system-protected

**Solutions**:
```kotlin
// Verify directory exists
val dir = File(path)
if (!dir.exists() || !dir.isDirectory) {
    Log.e("FileManager", "Directory not accessible: $path")
    return emptyList()
}

// Check read permission
if (!dir.canRead()) {
    Log.e("FileManager", "No read permission: $path")
    return emptyList()
}
```

### Problem: App Crashes After Permission Grant

**Causes**:
1. Stale activity reference
2. Unhandled permission result
3. Race condition in async operations

**Solutions**:
```kotlin
// Handle permission result properly
override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<String>,
    grantResults: IntArray
) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    
    // Check if activity still valid
    if (isFinishing) return
    
    // Handle result
    if (requestCode == PERMISSION_REQUEST_CODE) {
        if (grantResults.isNotEmpty() && 
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Reload data
            loadDirectory(currentPath)
        }
    }
}
```

## Best Practices

### 1. Always Check Permissions

```kotlin
fun canAccessFile(path: String): Boolean {
    val file = File(path)
    return file.exists() && file.canRead()
}
```

### 2. Request Permissions Gracefully

```kotlin
fun requestStoragePermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        if (!Environment.isExternalStorageManager()) {
            showPermissionDialog()
        }
    } else {
        requestReadExternalStoragePermission()
    }
}
```

### 3. Handle Permission Denial

```kotlin
fun onPermissionDenied() {
    Toast.makeText(
        this,
        "Storage permission required to browse files",
        Toast.LENGTH_LONG
    ).show()
    // Disable file browser UI
    disableFileManager()
}
```

### 4. Verify Access Before Operations

```kotlin
fun listDirectory(path: String): List<File> {
    val dir = File(path)
    if (!dir.canRead()) {
        Log.w("FileManager", "Cannot read: $path")
        return emptyList()
    }
    return dir.listFiles()?.toList() ?: emptyList()
}
```

## Future Considerations

### Android 14+ Changes

- Possible additional storage restrictions
- Potential deprecation of MANAGE_EXTERNAL_STORAGE
- Enhanced privacy controls

### Mitigation Strategy

```kotlin
// Future-proof permission checking
fun getStorageAccessMethod(): StorageAccessMethod {
    return when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
            // Android 14+: Check new requirements
            StorageAccessMethod.FUTURE_API
        }
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
            // Android 11-13: Current approach
            StorageAccessMethod.MANAGE_EXTERNAL_STORAGE
        }
        else -> {
            // Android 10 and below: Legacy
            StorageAccessMethod.READ_EXTERNAL_STORAGE
        }
    }
}
```

## References

- [Android Storage Documentation](https://developer.android.com/training/data-storage)
- [Scoped Storage Guide](https://developer.android.com/training/data-storage/shared/media)
- [MANAGE_EXTERNAL_STORAGE Permission](https://developer.android.com/about/versions/11/privacy/permissions)
- [Android Security & Privacy](https://developer.android.com/privacy-and-security)
