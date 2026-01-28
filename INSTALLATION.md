# SO Analyzer - Installation Guide

## Prerequisites

- Android device or emulator running Android 11 (API 31) or higher
- USB cable (for device installation)
- 100 MB free storage on device
- APK file (download from GitHub Actions or provided)

## Installation Methods

### Method 1: Direct APK Installation (Easiest)

**For Android 12+**:
1. Download `app-debug.apk` from GitHub Actions
2. Transfer to phone via USB or email
3. Open file manager on phone
4. Navigate to Downloads folder
5. Tap `app-debug.apk`
6. Tap **Install** when prompted
7. App installs and launches

**For Android 11**:
Same as above, but may need to enable "Unknown sources" in Settings if installation is blocked.

### Method 2: ADB Installation (Recommended for Developers)

**Prerequisites**:
- Android SDK Platform Tools installed
- USB debugging enabled on phone
- Phone connected via USB

**Steps**:

1. **Enable USB Debugging** on phone:
   - Settings → About phone
   - Tap "Build number" 7 times
   - Go back to Settings → Developer options
   - Enable "USB Debugging"

2. **Connect phone via USB**:
   ```bash
   adb devices
   # Should show your device
   ```

3. **Install APK**:
   ```bash
   adb install app-debug.apk
   # Output: Success
   ```

4. **Launch app**:
   ```bash
   adb shell am start -n com.example.soanalyzer/.MainActivity
   ```

### Method 3: Android Studio Installation

**Prerequisites**:
- Android Studio installed
- Project open in Android Studio
- Phone connected or emulator running

**Steps**:

1. Download APK from GitHub Actions
2. Open Android Studio
3. Go to **Run** → **Edit Configurations**
4. Select **app** configuration
5. Go to **Installation Options**
6. Select **APK from app bundle**
7. Browse to downloaded APK
8. Click **Run** (Shift+F10)

### Method 4: Emulator Installation

**Prerequisites**:
- Android Emulator running (Android 11+)
- APK file

**Steps**:

```bash
# List running emulators
adb devices

# Install on emulator
adb -e install app-debug.apk

# Launch app
adb shell am start -n com.example.soanalyzer/.MainActivity
```

## Post-Installation Setup

### Grant Storage Permissions

1. **Launch SO Analyzer**
2. **Permission Dialog** appears
3. Tap **"Open Settings"**
4. Navigate to **Apps → SO Analyzer → Permissions**
5. Tap **"All files access"**
6. Toggle **"Allow access to manage all files"** ON
7. Return to app

**Note**: Without this permission, the app cannot browse files.

### Verify Installation

1. **App appears** in launcher
2. **Tap to open**
3. **File browser** loads
4. **Browse to** `/storage/emulated/0`
5. **See files and folders**
6. **Navigate** to a folder with .so files
7. **Tap a .so file** to open analyzer

## Troubleshooting Installation

### "App not installed"

**Cause**: Device security settings

**Solution**:
1. Settings → Security
2. Enable "Unknown sources" or "Install from unknown sources"
3. Try installation again

### "Cannot install - insufficient storage"

**Cause**: Not enough free space

**Solution**:
1. Free up at least 200 MB on device
2. Delete unnecessary files/apps
3. Try installation again

### "Installation failed - parse error"

**Cause**: Corrupted APK file

**Solution**:
1. Re-download APK from GitHub Actions
2. Verify file size matches (typically 15-25 MB)
3. Try installation again

### "USB debugging not working"

**Cause**: USB debugging not enabled

**Solution**:
1. Settings → About phone
2. Tap "Build number" 7 times
3. Go back to Settings → Developer options
4. Enable "USB Debugging"
5. Reconnect phone

### "Permission denied - cannot access files"

**Cause**: Storage permission not granted

**Solution**:
1. Launch app
2. Tap "Open Settings" in permission dialog
3. Grant "All files access" permission
4. Return to app

### "App crashes on launch"

**Cause**: Incompatible Android version or missing dependencies

**Solution**:
1. Verify Android version is 11 or higher
2. Uninstall app: `adb uninstall com.example.soanalyzer`
3. Re-download APK
4. Reinstall: `adb install app-debug.apk`

## Uninstallation

### Via Settings

1. Settings → Apps
2. Find "SO Analyzer"
3. Tap → **Uninstall**
4. Confirm

### Via ADB

```bash
adb uninstall com.example.soanalyzer
```

## APK Variants

### Debug APK (`app-debug.apk`)

- **Size**: ~15-20 MB
- **Signing**: Debug key
- **Features**: All features enabled
- **Performance**: Slightly slower (no optimization)
- **Use Case**: Testing and development

### Release APK (`app-release.apk`)

- **Size**: ~8-12 MB (optimized)
- **Signing**: Debug key (for testing) or production key (for Play Store)
- **Features**: All features enabled
- **Performance**: Optimized
- **Use Case**: Distribution and Play Store

## Downloading APK from GitHub Actions

1. Go to GitHub repository
2. Click **Actions** tab
3. Click latest **"Build APK"** workflow run
4. Scroll down to **Artifacts** section
5. Click **app-debug** or **app-release** to download
6. Extract ZIP file
7. APK is ready to install

## Downloading APK from GitHub Releases

1. Go to GitHub repository
2. Click **Releases** tab
3. Find desired version (e.g., v1.0.0)
4. Under **Assets**, click APK to download
5. APK is ready to install

## Updating the App

### Method 1: Reinstall

```bash
adb uninstall com.example.soanalyzer
adb install app-debug-new.apk
```

### Method 2: Direct Update

```bash
# Installs over existing version
adb install -r app-debug-new.apk
```

### Method 3: Via Settings

1. Settings → Apps → SO Analyzer
2. Tap **Uninstall**
3. Install new APK

## System Requirements by Android Version

### Android 11 (API 31)
- ✅ Supported
- ✅ Full filesystem access via MANAGE_EXTERNAL_STORAGE
- ✅ All features working

### Android 12 (API 32)
- ✅ Supported
- ✅ Full filesystem access
- ✅ All features working

### Android 13 (API 33)
- ✅ Supported
- ✅ Full filesystem access
- ✅ All features working

### Android 14 (API 34)
- ✅ Supported
- ✅ Full filesystem access
- ✅ All features working

### Android 10 and Below
- ❌ Not supported
- ❌ Minimum API 31 required

## Storage Access

### Accessible Paths

After granting MANAGE_EXTERNAL_STORAGE permission:

- `/storage/emulated/0` - Primary storage
- `/storage/emulated/0/Download` - Downloads folder
- `/storage/emulated/0/Documents` - Documents folder
- `/storage/XXXX-XXXX` - SD cards
- `/Android/data` - App-specific data
- `/system/lib` - System libraries (read-only)

### Not Accessible

- `/data/` - Private app data
- `/system/` - System partition (except libraries)
- `/proc/` - Process information

## Performance Tips

### For Large Files

- Avoid opening very large .so files (>100 MB) on low-end devices
- Close other apps before analyzing large binaries
- Use Release APK for better performance

### For Smooth Scrolling

- Close unnecessary apps
- Clear app cache: Settings → Apps → SO Analyzer → Storage → Clear Cache
- Restart device if experiencing lag

## Security & Privacy

- **No data collection**: App doesn't send data anywhere
- **Offline operation**: Works completely offline
- **Read-only by default**: Doesn't modify files
- **Local analysis**: All processing happens on device

## Getting Help

### Check Logs

```bash
adb logcat | grep soanalyzer
```

### Common Issues

1. **File browser not loading**
   - Grant storage permission
   - Restart app

2. **SO analyzer crashes**
   - Try with smaller .so file
   - Restart device
   - Reinstall app

3. **Slow performance**
   - Close other apps
   - Clear cache
   - Use Release APK

### Report Issues

1. Go to GitHub repository
2. Click **Issues** tab
3. Click **New Issue**
4. Describe problem with:
   - Android version
   - Device model
   - APK version
   - Steps to reproduce
   - Logcat output

## Advanced Installation

### Sideloading

For installing without Google Play Store:

```bash
# Copy APK to device
adb push app-debug.apk /sdcard/Download/

# Install from device file manager
# Open file manager → Downloads → app-debug.apk → Install
```

### Batch Installation

For multiple devices:

```bash
for device in $(adb devices | grep -v "List" | awk '{print $1}'); do
  adb -s $device install app-debug.apk
done
```

### Installation with Options

```bash
# Install and replace existing
adb install -r app-debug.apk

# Install and grant permissions
adb install -g app-debug.apk

# Install on specific device
adb -s DEVICE_ID install app-debug.apk
```

## Verification

### Verify Installation

```bash
adb shell pm list packages | grep soanalyzer
# Output: package:com.example.soanalyzer
```

### Verify Permissions

```bash
adb shell pm list permissions | grep MANAGE_EXTERNAL_STORAGE
adb shell pm dump com.example.soanalyzer | grep MANAGE_EXTERNAL_STORAGE
```

### Verify APK Signature

```bash
jarsigner -verify -verbose app-debug.apk
# Output: jar verified.
```

## Next Steps

1. **Install APK** using preferred method
2. **Grant permissions** when prompted
3. **Browse files** to verify installation
4. **Open .so files** to test analyzer
5. **Explore features** (Code, Sections, Symbols, Strings, Search)
6. **Report issues** if any problems

---

**Installation complete!** You can now use SO Analyzer to browse files and analyze .so binaries on your Android device.
