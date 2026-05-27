#!/bin/bash
# Build stub static library for iOS development.
# This avoids the need for Rust toolchain during initial Kotlin/iOS development.
# For production, replace with: native/build_ios.sh

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
STUB_DIR="$SCRIPT_DIR/puzzle-core/stub"
OUTPUT_DIR="$SCRIPT_DIR/ios-libs-stub"
HEADER_DIR="$SCRIPT_DIR/puzzle-core/include"

rm -rf "$OUTPUT_DIR"
mkdir -p "$OUTPUT_DIR"

# Build stub for device
echo "Building stub for ios-arm64..."
xcrun -sdk iphoneos clang \
    -arch arm64 \
    -miphoneos-version-min=15.0 \
    -I"$HEADER_DIR" \
    -c "$STUB_DIR/stub_puzzle_core.c" \
    -o "$OUTPUT_DIR/stub_puzzle_core.o"

xcrun -sdk iphoneos ar rcs "$OUTPUT_DIR/libpuzzle_core.a" "$OUTPUT_DIR/stub_puzzle_core.o"

echo "Stub library created at: $OUTPUT_DIR/libpuzzle_core.a"
echo ""
echo "For real native library, run: native/build_ios.sh"
