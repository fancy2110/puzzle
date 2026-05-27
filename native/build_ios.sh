#!/bin/bash
# Build puzzle-core for iOS targets.
# Prerequisites:
#   - Rust toolchain with iOS targets installed:
#     rustup target add aarch64-apple-ios aarch64-apple-ios-sim x86_64-apple-ios
#   - macOS with Xcode installed
#
# Output: static libraries (.a) in native/ios-libs/

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$SCRIPT_DIR/puzzle-core"
OUTPUT_DIR="$SCRIPT_DIR/ios-libs"

cd "$PROJECT_DIR"

rm -rf "$OUTPUT_DIR"
mkdir -p "$OUTPUT_DIR"

# Build for device (arm64)
echo "=== Building for iOS device (aarch64-apple-ios) ==="
cargo build --target aarch64-apple-ios --release
cp target/aarch64-apple-ios/release/libpuzzle_core.a "$OUTPUT_DIR/libpuzzle_core-ios-arm64.a"

# Build for simulator (arm64)
echo "=== Building for iOS simulator (aarch64-apple-ios-sim) ==="
cargo build --target aarch64-apple-ios-sim --release
cp target/aarch64-apple-ios-sim/release/libpuzzle_core.a "$OUTPUT_DIR/libpuzzle_core-ios-sim-arm64.a"

# Build for simulator (x86_64) — for older Intel Macs
echo "=== Building for iOS simulator (x86_64-apple-ios) ==="
cargo build --target x86_64-apple-ios --release
cp target/x86_64-apple-ios/release/libpuzzle_core.a "$OUTPUT_DIR/libpuzzle_core-ios-sim-x86_64.a"

# Create XCFramework
echo "=== Creating XCFramework ==="
XCFRAMEWORK_DIR="$OUTPUT_DIR/PuzzleCore.xcframework"
rm -rf "$XCFRAMEWORK_DIR"

# Create universal simulator lib
lipo -create \
    "$OUTPUT_DIR/libpuzzle_core-ios-sim-arm64.a" \
    "$OUTPUT_DIR/libpuzzle_core-ios-sim-x86_64.a" \
    -output "$OUTPUT_DIR/libpuzzle_core-ios-sim.a" 2>/dev/null || \
    cp "$OUTPUT_DIR/libpuzzle_core-ios-sim-arm64.a" "$OUTPUT_DIR/libpuzzle_core-ios-sim.a"

xcodebuild -create-xcframework \
    -library "$OUTPUT_DIR/libpuzzle_core-ios-arm64.a" \
    -library "$OUTPUT_DIR/libpuzzle_core-ios-sim.a" \
    -output "$XCFRAMEWORK_DIR"

echo ""
echo "=== iOS build complete ==="
echo "XCFramework: $XCFRAMEWORK_DIR"
echo "Header: $PROJECT_DIR/include/puzzle_core.h"
