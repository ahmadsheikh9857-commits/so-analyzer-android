@echo off
REM SO Analyzer Build Script for Windows
REM Compiles Debug and Release APKs with optional signing configuration

setlocal enabledelayedexpansion

REM Configuration
set BUILD_TYPE=%1
if "!BUILD_TYPE!"=="" set BUILD_TYPE=debug

set SIGN_RELEASE=%2
if "!SIGN_RELEASE!"=="" set SIGN_RELEASE=false

REM Validate build type
if not "!BUILD_TYPE!"=="debug" if not "!BUILD_TYPE!"=="release" (
    echo Error: Invalid BUILD_TYPE. Must be 'debug' or 'release'
    goto :show_usage
)

REM Check if gradlew.bat exists
if not exist "%CD%\gradlew.bat" (
    echo Error: gradlew.bat not found. Please run from project root directory.
    exit /b 1
)

echo.
echo ================================
echo SO Analyzer Build System
echo ================================
echo Build Type: !BUILD_TYPE!
echo Sign Release: !SIGN_RELEASE!
echo.

REM Clean build
echo Cleaning previous builds...
call gradlew.bat clean
if errorlevel 1 (
    echo Error: Clean failed
    exit /b 1
)

REM Build APK
if "!BUILD_TYPE!"=="debug" (
    echo Building Debug APK...
    call gradlew.bat assembleDebug
    if errorlevel 1 (
        echo Error: Debug build failed
        exit /b 1
    )
    echo.
    echo Debug APK built successfully
    echo Location: app\build\outputs\apk\debug\app-debug.apk
) else (
    if "!SIGN_RELEASE!"=="true" (
        echo Building Signed Release APK...
        if "!KEYSTORE_PATH!"=="" (
            echo Error: KEYSTORE_PATH not set
            goto :show_signing_help
        )
        call gradlew.bat assembleRelease ^
            -Pandroid.injected.signing.store.file="!KEYSTORE_PATH!" ^
            -Pandroid.injected.signing.store.password="!KEYSTORE_PASS!" ^
            -Pandroid.injected.signing.key.alias="!KEY_ALIAS!" ^
            -Pandroid.injected.signing.key.password="!KEY_PASS!"
    ) else (
        echo Building Unsigned Release APK...
        call gradlew.bat assembleRelease
    )
    if errorlevel 1 (
        echo Error: Release build failed
        exit /b 1
    )
    echo.
    echo Release APK built successfully
    echo Location: app\build\outputs\apk\release\app-release.apk
)

echo.
echo ================================
echo Build Complete
echo ================================
goto :end

:show_usage
echo.
echo Usage: build.bat [BUILD_TYPE] [SIGN_RELEASE]
echo.
echo Arguments:
echo   BUILD_TYPE   - 'debug' (default) or 'release'
echo   SIGN_RELEASE - 'true' to sign release, 'false' (default) for unsigned
echo.
echo Examples:
echo   build.bat debug
echo   build.bat release
echo   build.bat release true
echo.
goto :end

:show_signing_help
echo.
echo To build a signed release APK, set environment variables:
echo   set KEYSTORE_PATH=path\to\keystore.jks
echo   set KEYSTORE_PASS=your_password
echo   set KEY_ALIAS=your_key_alias
echo   set KEY_PASS=your_key_password
echo.
echo Then run: build.bat release true
echo.
goto :end

:end
endlocal
