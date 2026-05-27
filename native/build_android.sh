#!/bin/bash
# Build puzzle-core for Android targets.
# Prerequisites:
#   - Rust toolchain with Android targets installed:
#     rustup target add aarch64-linux-android armv7-linux-androideabi x86_64-linux-android
#   - Android NDK installed (set ANDROID_NDK_HOME or NDK_HOME)
#   - cargo-ndk: cargo install cargo-ndk (recommended) or manual linker config

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$SCRIPT_DIR/puzzle-core"
OUTPUT_DIR="$SCRIPT_DIR/../composeApp/src/androidMain/jniLibs"

# Detect NDK
NDK="${ANDROID_NDK_HOME:-${NDK_HOME:-}}"
if [ -z "$NDK" ]; then
    # Try common locations
    if [ -d "$ANDROID_HOME/ndk" ]; then
        NDK=$(ls -d "$ANDROID_HOME/ndk/"* 2>/dev/null | sort -V | tail -1)
    elif [ -d "$HOME/Library/Android/sdk/ndk" ]; then
        NDK=$(ls -d "$HOME/Library/Android/sdk/ndk/"* 2>/dev/null | sort -V | tail -1)
    fi
fi

echo "Using NDK: ${NDK:-<not found, trying cargo-ndk>}"

# Method 1: Try cargo-ndk (simplest)
if command -v cargo-ndk &>/dev/null; then
    echo "=== Building with cargo-ndk ==="
    cd "$PROJECT_DIR"

    cargo ndk \
        --target aarch64-linux-android \
        --target armv7-linux-androideabi \
        --target x86_64-linux-android \
        --platform 26 \
        build --release

    # Copy libs
    mkdir -p "$OUTPUT_DIR/arm64-v8a"
    mkdir -p "$OUTPUT_DIR/armeabi-v7a"
    mkdir -p "$OUTPUT_DIR/x86_64"

    cp target/aarch64-linux-android/release/libpuzzle_core.so "$OUTPUT_DIR/arm64-v8a/"
    cp target/armv7-linux-androideabi/release/libpuzzle_core.so "$OUTPUT_DIR/armeabi-v7a/"
    cp target/x86_64-linux-android/release/libpuzzle_core.so "$OUTPUT_DIR/x86_64/"

    echo "=== Android libs built ==="
    ls -lh "$OUTPUT_DIR"/*/libpuzzle_core.so
    exit 0
fi

# Method 2: Manual cross-compile with NDK linker
if [ -n "$NDK" ]; then
    echo "=== Building with NDK toolchain ==="
    cd "$PROJECT_DIR"

    HOST_TAG="darwin-x86_64"
    if [[ "$(uname)" == "Linux" ]]; then
        HOST_TAG="linux-x86_64"
    fi

    TOOLCHAIN="$NDK/toolchains/llvm/prebuilt/$HOST_TAG"

    declare -A TARGETS=(
        ["aarch64-linux-android"]="arm64-v8a"
        ["armv7-linux-androideabi"]="armv7-linux-androideabi"
        ["x86_64-linux-android"]="x86_64"
    )

    for TARGET in "${!TARGETS[@]}"; do
        ABI="${TARGETS[$TARGET]}"
        echo "Building for $TARGET ($ABI)..."

        CC="$TOOLCHAIN/bin/${TARGET}26-clang"
        export CC_${TARGET//-/_}="$CC"
        export AR_${TARGET//-/_}="$TOOLCHAIN/bin/llvm-ar"
        export CARGO_TARGET_$(echo $TARGET | tr '[:lower:]-' '[:upper:]_')_LINKER="$CC"

        cargo build --target "$TARGET" --release

        mkdir -p "$OUTPUT_DIR/$ABI"
        cp "target/$TARGET/release/libpuzzle_core.so" "$OUTPUT_DIR/$ABI/"
    done

    echo "=== Android libs built with NDK ==="
    ls -lh "$OUTPUT_DIR"/*/libpuzzle_core.so
    exit 0
fi

echo "ERROR: Neither cargo-ndk nor Android NDK found."
echo "Install one of:"
echo "  cargo install cargo-ndk"
echo "  or set ANDROID_NDK_HOME environment variable"
exit 1
