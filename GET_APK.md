# Getting Your APK - Complete Guide

## 🎯 Your Goal: Get an Installable APK on Your Phone

This guide walks you through getting SO Analyzer running on your Android device in the fastest way possible.

## Option A: Use Pre-Built APK (Fastest - 5 Minutes)

If someone has already built an APK for you, skip to [Installing the APK](#installing-the-apk).

## Option B: Build APK via GitHub Actions (Recommended - 10 Minutes)

This is the **recommended approach** - no local setup needed, cloud builds everything.

### Step 1: Create GitHub Account (If You Don't Have One)

1. Go to https://github.com
2. Click **Sign up**
3. Enter email, password, username
4. Verify email
5. Done!

### Step 2: Create Repository

1. Go to https://github.com/new
2. Enter **Repository name**: `SOAnalyzer`
3. Choose **Public** (so you can access artifacts)
4. Click **Create repository**
5. You'll see instructions for pushing code

### Step 3: Push Code to GitHub

**On your computer** (Windows, Mac, or Linux):

```bash
# Navigate to SOAnalyzer folder
cd SOAnalyzer

# Initialize git
git init
git add .
git commit -m "Initial commit: SO Analyzer"

# Add GitHub remote (replace YOUR_USERNAME)
git remote add origin https://github.com/YOUR_USERNAME/SOAnalyzer.git
git branch -M main
git push -u origin main
```

**If you don't have git installed**:
- **Windows**: Download from https://git-scm.com/
- **Mac**: `brew install git`
- **Linux**: `sudo apt-get install git`

### Step 4: Wait for Build

1. Go to your GitHub repository
2. Click **Actions** tab
3. You should see **"Build APK"** workflow running
4. Wait for it to complete (usually 5-10 minutes)
5. Green checkmark = Success ✅

### Step 5: Download APK

1. In **Actions** tab, click the latest **"Build APK"** run
2. Scroll down to **Artifacts** section
3. Click **app-debug** to download
4. Extract the ZIP file
5. You now have **app-debug.apk**

**That's it!** You have a real, compiled APK.

## Installing the APK

### Method 1: Direct Installation (Easiest for Most People)

**What you need**:
- APK file on your phone
- Phone with Android 11+

**Steps**:

1. **Transfer APK to phone**:
   - Email it to yourself and download
   - Or use cloud storage (Google Drive, Dropbox)
   - Or connect phone via USB and copy file

2. **Open file manager on phone**
3. **Navigate to Downloads folder**
4. **Tap app-debug.apk**
5. **Tap Install** when prompted
6. **Wait for installation** (usually 10-30 seconds)
7. **Tap Open** to launch app

### Method 2: ADB Installation (For Developers)

**What you need**:
- APK file on your computer
- Android SDK Platform Tools installed
- USB cable
- Phone with USB debugging enabled

**Steps**:

1. **Enable USB Debugging on phone**:
   - Settings → About phone
   - Tap "Build number" 7 times
   - Go back to Settings → Developer options
   - Enable "USB Debugging"

2. **Connect phone via USB**

3. **On your computer**, open terminal/command prompt:
   ```bash
   adb install app-debug.apk
   ```

4. **Wait for success message**

5. **Launch app**:
   ```bash
   adb shell am start -n com.example.soanalyzer/.MainActivity
   ```

## First Launch Setup

### Grant Storage Permission

1. **App launches**
2. **Permission dialog appears**
3. **Tap "Open Settings"**
4. **Navigate to All files access**
5. **Toggle permission ON**
6. **Return to app**
7. **App now works!**

### Verify It Works

1. **File browser loads**
2. **You can see files and folders**
3. **Navigate to a folder with .so files**
4. **Tap a .so file**
5. **Analyzer opens with tabs**
6. **Explore Code, Sections, Symbols, Strings tabs**

## Troubleshooting

### "Cannot install - App not installed"

**Problem**: Installation fails

**Solution**:
1. Check phone has 200 MB free space
2. Try again
3. If still fails, try Method 2 (ADB)

### "Permission denied - Cannot access files"

**Problem**: File browser shows "Permission denied"

**Solution**:
1. Tap "Open Settings" in app
2. Grant "All files access" permission
3. Return to app

### "App crashes on launch"

**Problem**: App closes immediately

**Solution**:
1. Uninstall: `adb uninstall com.example.soanalyzer`
2. Re-download APK from GitHub Actions
3. Reinstall

### "Build failed on GitHub"

**Problem**: GitHub Actions workflow shows red X

**Solution**:
1. Check workflow logs (click workflow run)
2. Look for error messages
3. Common issues:
   - Gradle cache issue: Try again
   - SDK not found: Workflow should auto-install
   - Syntax error in code: Fix and push again

## What If You Don't Have GitHub?

### Alternative: Use Existing Build

If someone has already built the APK:
1. Ask them for the APK file
2. Skip to [Installing the APK](#installing-the-apk)
3. Install on your phone

### Alternative: Use Cloud Build Service

1. **Codemagic**: https://codemagic.io/
   - Free tier available
   - Connect GitHub repo
   - Builds APK automatically

2. **EAS Build**: https://eas.expo.dev/
   - Expo-based builds
   - Free tier available

3. **Bitrise**: https://www.bitrise.io/
   - CI/CD platform
   - Free tier available

## Quick Reference

| Task | Time | Difficulty |
|------|------|------------|
| Create GitHub account | 2 min | Easy |
| Create repository | 2 min | Easy |
| Push code to GitHub | 3 min | Medium |
| Wait for build | 5-10 min | - |
| Download APK | 1 min | Easy |
| Install on phone | 2 min | Easy |
| Grant permissions | 1 min | Easy |
| **Total** | **~20 min** | **Easy** |

## Next Steps

1. **Choose your path** (GitHub Actions recommended)
2. **Follow the steps** for your chosen method
3. **Download APK**
4. **Install on phone**
5. **Grant permissions**
6. **Start using SO Analyzer!**

## Getting Help

- **Installation issues?** → See [INSTALLATION.md](INSTALLATION.md)
- **Build issues?** → See [GITHUB_ACTIONS_SETUP.md](GITHUB_ACTIONS_SETUP.md)
- **General questions?** → See [QUICK_START.md](QUICK_START.md)
- **Technical details?** → See [ARCHITECTURE.md](ARCHITECTURE.md)

## Success Checklist

- [ ] APK file downloaded
- [ ] APK installed on phone
- [ ] App launches
- [ ] Storage permission granted
- [ ] File browser works
- [ ] Can navigate folders
- [ ] Can open .so files
- [ ] Analyzer tabs visible
- [ ] Ready to use!

---

**You're almost there!** Follow the steps above and you'll have SO Analyzer running on your phone in about 20 minutes.
