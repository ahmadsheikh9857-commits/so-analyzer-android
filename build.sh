#!/bin/bash

################################################################################
# SO Analyzer Build Script
# Compiles Debug and Release APKs with optional signing configuration
################################################################################

set -e

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$SCRIPT_DIR"

# Configuration
BUILD_TYPE="${1:-debug}"  # debug or release
SIGN_RELEASE="${2:-false}"
KEYSTORE_PATH="${KEYSTORE_PATH:-}"
KEYSTORE_PASS="${KEYSTORE_PASS:-}"
KEY_ALIAS="${KEY_ALIAS:-}"
KEY_PASS="${KEY_PASS:-}"

# Output directories
BUILD_OUTPUT_DIR="$PROJECT_ROOT/app/build/outputs/apk"
DEBUG_APK="$BUILD_OUTPUT_DIR/debug/app-debug.apk"
RELEASE_APK="$BUILD_OUTPUT_DIR/release/app-release.apk"

################################################################################
# Functions
################################################################################

print_header() {
    echo -e "${BLUE}================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}================================${NC}"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ $1${NC}"
}

check_prerequisites() {
    print_header "Checking Prerequisites"

    # Check if gradlew exists
    if [ ! -f "$PROJECT_ROOT/gradlew" ]; then
        print_error "gradlew not found. Please ensure you're in the project root directory."
        exit 1
    fi

    # Make gradlew executable
    chmod +x "$PROJECT_ROOT/gradlew"
    print_success "gradlew is executable"

    # Check for Java
    if ! command -v java &> /dev/null; then
        print_error "Java not found. Please install JDK 17 or later."
        exit 1
    fi

    local java_version=$(java -version 2>&1 | head -1)
    print_success "Java found: $java_version"

    # Check for Android SDK
    if [ -z "$ANDROID_HOME" ]; then
        print_warning "ANDROID_HOME not set. Gradle will attempt to find SDK automatically."
    else
        print_success "ANDROID_HOME: $ANDROID_HOME"
    fi
}

clean_build() {
    print_header "Cleaning Previous Builds"
    
    print_info "Running: ./gradlew clean"
    "$PROJECT_ROOT/gradlew" -p "$PROJECT_ROOT" clean
    
    print_success "Build cleaned"
}

build_debug() {
    print_header "Building Debug APK"
    
    print_info "Running: ./gradlew assembleDebug"
    "$PROJECT_ROOT/gradlew" -p "$PROJECT_ROOT" assembleDebug
    
    if [ -f "$DEBUG_APK" ]; then
        local size=$(du -h "$DEBUG_APK" | cut -f1)
        print_success "Debug APK built successfully"
        print_info "Location: $DEBUG_APK"
        print_info "Size: $size"
    else
        print_error "Debug APK not found after build"
        exit 1
    fi
}

build_release_unsigned() {
    print_header "Building Release APK (Unsigned)"
    
    print_info "Running: ./gradlew assembleRelease"
    "$PROJECT_ROOT/gradlew" -p "$PROJECT_ROOT" assembleRelease
    
    if [ -f "$RELEASE_APK" ]; then
        local size=$(du -h "$RELEASE_APK" | cut -f1)
        print_success "Release APK built successfully"
        print_info "Location: $RELEASE_APK"
        print_info "Size: $size"
        print_warning "APK is unsigned. Sign it before uploading to Play Store."
    else
        print_error "Release APK not found after build"
        exit 1
    fi
}

build_release_signed() {
    print_header "Building Release APK (Signed)"
    
    # Validate signing configuration
    if [ -z "$KEYSTORE_PATH" ] || [ -z "$KEYSTORE_PASS" ] || [ -z "$KEY_ALIAS" ] || [ -z "$KEY_PASS" ]; then
        print_error "Signing configuration incomplete. Please set:"
        echo "  KEYSTORE_PATH=$KEYSTORE_PATH"
        echo "  KEYSTORE_PASS=***"
        echo "  KEY_ALIAS=$KEY_ALIAS"
        echo "  KEY_PASS=***"
        exit 1
    fi

    if [ ! -f "$KEYSTORE_PATH" ]; then
        print_error "Keystore file not found: $KEYSTORE_PATH"
        exit 1
    fi

    print_info "Keystore: $KEYSTORE_PATH"
    print_info "Key Alias: $KEY_ALIAS"

    print_info "Running: ./gradlew assembleRelease with signing"
    "$PROJECT_ROOT/gradlew" -p "$PROJECT_ROOT" assembleRelease \
        -Pandroid.injected.signing.store.file="$KEYSTORE_PATH" \
        -Pandroid.injected.signing.store.password="$KEYSTORE_PASS" \
        -Pandroid.injected.signing.key.alias="$KEY_ALIAS" \
        -Pandroid.injected.signing.key.password="$KEY_PASS"
    
    if [ -f "$RELEASE_APK" ]; then
        local size=$(du -h "$RELEASE_APK" | cut -f1)
        print_success "Signed Release APK built successfully"
        print_info "Location: $RELEASE_APK"
        print_info "Size: $size"
    else
        print_error "Release APK not found after build"
        exit 1
    fi
}

generate_signing_template() {
    print_header "Generating Signing Configuration Template"
    
    local template_file="$PROJECT_ROOT/signing.template.sh"
    
    cat > "$template_file" << 'EOF'
#!/bin/bash
# Signing Configuration Template
# Copy this file to signing.sh and fill in your keystore details
# DO NOT commit signing.sh to version control

# Path to your keystore file
export KEYSTORE_PATH="/path/to/your/keystore.jks"

# Keystore password
export KEYSTORE_PASS="your_keystore_password"

# Key alias (the name of the key in the keystore)
export KEY_ALIAS="your_key_alias"

# Key password
export KEY_PASS="your_key_password"

# To build a signed release APK:
# source signing.sh
# ./build.sh release true
EOF

    chmod +x "$template_file"
    print_success "Signing template created: $template_file"
    print_info "Copy to signing.sh and fill in your details"
}

show_usage() {
    cat << EOF
${BLUE}SO Analyzer Build Script${NC}

Usage: $0 [BUILD_TYPE] [SIGN_RELEASE]

Arguments:
  BUILD_TYPE    - 'debug' (default) or 'release'
  SIGN_RELEASE  - 'true' to sign release APK, 'false' (default) for unsigned

Environment Variables (for signed builds):
  KEYSTORE_PATH - Path to your .jks keystore file
  KEYSTORE_PASS - Keystore password
  KEY_ALIAS     - Key alias in keystore
  KEY_PASS      - Key password

Examples:
  # Build debug APK
  $0 debug

  # Build unsigned release APK
  $0 release

  # Build signed release APK (requires environment variables)
  export KEYSTORE_PATH="/path/to/keystore.jks"
  export KEYSTORE_PASS="password"
  export KEY_ALIAS="my_key"
  export KEY_PASS="key_password"
  $0 release true

  # Or use signing configuration file
  source signing.sh
  $0 release true

EOF
}

################################################################################
# Main Script
################################################################################

main() {
    print_header "SO Analyzer Build System"
    
    # Show usage if requested
    if [ "$BUILD_TYPE" = "help" ] || [ "$BUILD_TYPE" = "-h" ] || [ "$BUILD_TYPE" = "--help" ]; then
        show_usage
        exit 0
    fi

    # Validate build type
    if [ "$BUILD_TYPE" != "debug" ] && [ "$BUILD_TYPE" != "release" ]; then
        print_error "Invalid BUILD_TYPE: $BUILD_TYPE (must be 'debug' or 'release')"
        show_usage
        exit 1
    fi

    # Check prerequisites
    check_prerequisites

    # Clean build
    clean_build

    # Build APK
    case "$BUILD_TYPE" in
        debug)
            build_debug
            ;;
        release)
            if [ "$SIGN_RELEASE" = "true" ]; then
                build_release_signed
            else
                build_release_unsigned
            fi
            ;;
    esac

    # Generate signing template if it doesn't exist
    if [ ! -f "$PROJECT_ROOT/signing.sh" ]; then
        generate_signing_template
    fi

    print_header "Build Complete"
    print_success "APK ready for testing/deployment"
    
    # Summary
    echo ""
    print_info "Build Summary:"
    if [ -f "$DEBUG_APK" ]; then
        echo "  Debug APK: $(du -h "$DEBUG_APK" | cut -f1)"
    fi
    if [ -f "$RELEASE_APK" ]; then
        echo "  Release APK: $(du -h "$RELEASE_APK" | cut -f1)"
    fi
}

# Run main
main "$@"
