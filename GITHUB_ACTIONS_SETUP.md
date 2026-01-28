# GitHub Actions Setup Guide

## Overview

This project includes automated CI/CD workflows that build, test, and sign APKs on every push. No local setup required—just push code to GitHub and get instant APK builds.

## Quick Start (5 Minutes)

### 1. Create GitHub Repository

```bash
# Initialize git (if not already done)
git init
git add .
git commit -m "Initial commit: SO Analyzer"

# Create repository on GitHub (via web UI)
# https://github.com/new

# Add remote and push
git remote add origin https://github.com/YOUR_USERNAME/SOAnalyzer.git
git branch -M main
git push -u origin main
```

### 2. Workflows Automatically Trigger

Once code is pushed:
1. **build-apk.yml** - Builds debug and release APKs
2. **test.yml** - Runs linting and unit tests

### 3. Download APK

1. Go to GitHub repository
2. Click **Actions** tab
3. Click latest workflow run
4. Scroll to **Artifacts** section
5. Download `app-debug` or `app-release` ZIP
6. Extract APK and install: `adb install app-debug.apk`

## Workflow Details

### build-apk.yml (Main Build Workflow)

**Triggers**:
- Push to `main` or `develop` branches
- Pull requests to `main` or `develop`
- Manual trigger (workflow_dispatch)
- Git tags starting with `v` (creates GitHub Release)

**Steps**:
1. Checkout code
2. Setup JDK 17
3. Setup Android SDK 34 + NDK 26.1 + CMake 3.22.1
4. Build Debug APK
5. Build Release APK (unsigned)
6. Create debug keystore
7. Sign Release APK with debug key
8. Verify signatures
9. Upload artifacts
10. Create GitHub Release (on tags)

**Output**:
- `app-debug.apk` - Debuggable, installable via adb
- `app-release.apk` - Optimized, signed, Play Store ready

**Artifacts Retention**: 30 days

### test.yml (Testing Workflow)

**Triggers**:
- Push to `main` or `develop`
- Pull requests
- Manual trigger

**Jobs**:
1. **lint** - Runs Android Lint checks
2. **unit-tests** - Runs Kotlin/Java unit tests
3. **build-check** - Runs full build checks

**Output**:
- Lint reports
- Test reports
- Build validation

## Installation Methods

### Method 1: Direct APK Install (Easiest)

```bash
# Download app-debug.apk from GitHub Actions artifacts

# Install via adb
adb install app-debug.apk

# Or install via Android Studio
# Drag and drop APK into Android Studio emulator
```

### Method 2: Using adb over Network

```bash
# Enable developer options on phone
# Enable USB debugging

# Connect phone via USB
adb devices

# Install APK
adb install app-debug.apk

# Launch app
adb shell am start -n com.example.soanalyzer/.MainActivity
```

### Method 3: Direct File Installation

```bash
# Copy APK to phone storage
adb push app-debug.apk /sdcard/Download/

# Open file manager on phone
# Navigate to Downloads
# Tap app-debug.apk to install
```

## Triggering Builds

### Automatic Triggers

Builds run automatically when:
- You push code to `main` or `develop`
- You create a pull request
- You push a git tag starting with `v` (e.g., `v1.0.0`)

### Manual Trigger

1. Go to **Actions** tab
2. Click **Build APK** workflow
3. Click **Run workflow** button
4. Select branch
5. Click **Run workflow**

### Tag-Based Release

```bash
# Create and push tag
git tag v1.0.0
git push origin v1.0.0

# This automatically:
# - Builds debug and release APKs
# - Creates GitHub Release
# - Uploads APKs as release assets
# - Makes APKs downloadable from Releases page
```

## Customization

### Change Build Branches

Edit `.github/workflows/build-apk.yml`:

```yaml
on:
  push:
    branches: [ main, develop, staging ]  # Add more branches
  pull_request:
    branches: [ main, develop, staging ]
```

### Change Android SDK Version

Edit `.github/workflows/build-apk.yml`:

```yaml
- name: Setup Android SDK
  uses: android-actions/setup-android@v3
  with:
    api-levels: 35  # Change from 34 to 35
    ndk-version: 27.0.12077973  # Update NDK version
    cmake-version: 3.28.0  # Update CMake version
```

### Add Custom Build Steps

Add steps before/after APK build:

```yaml
- name: Run Custom Script
  run: |
    echo "Custom build step"
    ./scripts/custom-build.sh
```

### Change APK Retention

Edit `.github/workflows/build-apk.yml`:

```yaml
- name: Upload Debug APK
  uses: actions/upload-artifact@v4
  with:
    name: app-debug
    path: app/build/outputs/apk/debug/app-debug.apk
    retention-days: 60  # Change from 30 to 60
```

## Troubleshooting

### Build Fails with "SDK not found"

**Problem**: Workflow can't find Android SDK

**Solution**: The workflow automatically sets up SDK. If it fails:
1. Check workflow logs: **Actions** → workflow run → **Logs**
2. Look for setup-android step errors
3. Verify API levels and NDK version are available

### APK Not Generated

**Problem**: Build completes but no APK artifact

**Solution**:
1. Check build logs for errors
2. Verify `build.gradle` is correct
3. Check for compilation errors in logs
4. Manually run: `./gradlew assembleDebug`

### Signing Fails

**Problem**: "jarsigner: command not found"

**Solution**: This shouldn't happen in GitHub Actions (keytool is pre-installed). If it does:
1. Check Java setup step
2. Verify JDK 17 is installed
3. Check workflow logs

### Artifact Download Fails

**Problem**: Can't download APK from Actions

**Solution**:
1. Workflow must complete successfully
2. Check artifacts section exists
3. Try downloading again (may be temporary)
4. Use GitHub CLI: `gh run download <run-id>`

## Advanced Configuration

### Using GitHub CLI

```bash
# Install GitHub CLI
# https://cli.github.com/

# List recent workflow runs
gh run list --workflow build-apk.yml

# Download artifacts from latest run
gh run download --name app-debug

# View workflow logs
gh run view <run-id> --log
```

### Setting Up Secrets (For Production)

For production releases with your own keystore:

1. Go to **Settings** → **Secrets and variables** → **Actions**
2. Click **New repository secret**
3. Add secrets:
   - `KEYSTORE_BASE64` - Base64 encoded keystore file
   - `KEYSTORE_PASSWORD` - Keystore password
   - `KEY_ALIAS` - Key alias
   - `KEY_PASSWORD` - Key password

4. Update workflow to use secrets:

```yaml
- name: Sign Release APK
  run: |
    echo "${{ secrets.KEYSTORE_BASE64 }}" | base64 -d > release.keystore
    jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA-256 \
      -keystore release.keystore \
      -storepass "${{ secrets.KEYSTORE_PASSWORD }}" \
      -keypass "${{ secrets.KEY_PASSWORD }}" \
      app/build/outputs/apk/release/app-release-unsigned.apk \
      "${{ secrets.KEY_ALIAS }}"
```

### Slack Notifications (Optional)

Add Slack notifications on build completion:

```yaml
- name: Notify Slack
  if: always()
  uses: slackapi/slack-github-action@v1
  with:
    webhook-url: ${{ secrets.SLACK_WEBHOOK }}
    payload: |
      {
        "text": "Build ${{ job.status }}: ${{ github.repository }}",
        "blocks": [
          {
            "type": "section",
            "text": {
              "type": "mrkdwn",
              "text": "Build Status: *${{ job.status }}*\nBranch: ${{ github.ref }}"
            }
          }
        ]
      }
```

## Performance Tips

### Reduce Build Time

1. **Use Gradle cache**:
   ```yaml
   - uses: actions/setup-java@v4
     with:
       cache: gradle  # Caches gradle dependencies
   ```

2. **Parallel builds**:
   ```yaml
   - name: Build Debug APK
     run: ./gradlew assembleDebug -x test --parallel
   ```

3. **Skip unnecessary tasks**:
   ```yaml
   - name: Build Debug APK
     run: ./gradlew assembleDebug -x lint -x test
   ```

### Reduce Artifact Size

1. **Enable ProGuard in release builds** (already configured)
2. **Remove unused resources** (already configured)
3. **Compress APK**: Add to workflow

```yaml
- name: Compress APK
  run: |
    gzip -k app/build/outputs/apk/debug/app-debug.apk
    ls -lh app/build/outputs/apk/debug/app-debug.apk*
```

## Monitoring Builds

### GitHub Actions Dashboard

1. Go to **Actions** tab
2. View all workflow runs
3. Click run to see details
4. Check logs for each step
5. Download artifacts

### Email Notifications

GitHub automatically sends emails when:
- Workflow fails
- Workflow completes (if enabled)

Configure in **Settings** → **Notifications**

### Status Badge

Add to README.md:

```markdown
[![Build APK](https://github.com/YOUR_USERNAME/SOAnalyzer/actions/workflows/build-apk.yml/badge.svg)](https://github.com/YOUR_USERNAME/SOAnalyzer/actions/workflows/build-apk.yml)
```

## Deployment to Play Store

For production deployment:

1. **Create Play Store account**: https://play.google.com/console
2. **Create app in Play Store Console**
3. **Generate production keystore**:
   ```bash
   keytool -genkey -v -keystore soanalyzer-release.jks \
     -keyalg RSA -keysize 2048 -validity 10000 \
     -alias soanalyzer-key
   ```

4. **Add secrets to GitHub** (see Advanced Configuration)
5. **Update workflow to use production keystore**
6. **Upload APK to Play Store Console**

## CI/CD Best Practices

1. **Branch Protection**: Require passing builds before merge
   - Settings → Branches → Add rule
   - Require status checks to pass

2. **Semantic Versioning**: Use tags for releases
   - `v1.0.0` - Major release
   - `v1.0.1` - Patch release
   - `v1.1.0` - Minor release

3. **Changelog**: Maintain CHANGELOG.md
   - Document changes in each release
   - Reference in GitHub Release notes

4. **Testing**: Add unit tests before release
   - Workflow runs tests automatically
   - Blocks merge if tests fail

5. **Code Review**: Require pull request reviews
   - Settings → Branch protection rules
   - Require approvals before merge

## Troubleshooting Checklist

- [ ] Repository is public (or you have Actions enabled)
- [ ] Workflows are in `.github/workflows/` directory
- [ ] Workflow YAML syntax is valid
- [ ] JDK 17 is specified
- [ ] Android SDK API 34 is available
- [ ] NDK version is compatible
- [ ] CMake version is specified
- [ ] Gradle wrapper is executable
- [ ] No secrets are hardcoded in workflows
- [ ] Artifacts are being generated

## Support & Resources

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Android GitHub Actions](https://github.com/android-actions)
- [Setup Java Action](https://github.com/actions/setup-java)
- [Upload Artifact Action](https://github.com/actions/upload-artifact)
- [Android SDK Documentation](https://developer.android.com/docs)

## Next Steps

1. **Push to GitHub**: `git push origin main`
2. **Monitor build**: Go to Actions tab
3. **Download APK**: From artifacts section
4. **Install on phone**: `adb install app-debug.apk`
5. **Test functionality**: Verify file browsing and SO analysis
6. **Iterate**: Make changes, push, get new APK

---

**You now have production-grade CI/CD!** Every push automatically builds a working APK.
