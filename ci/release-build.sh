#!/usr/bin/env bash

# Fail if any commands fails.
set -e

BUILD_OUTPUT_DIR="app/build/outputs/apk/release"
UNSIGNED_APK="$BUILD_OUTPUT_DIR/app-release-unsigned.apk"
ALIGNED_APK="$BUILD_OUTPUT_DIR/app-release-unsigned-aligned.apk"
SIGNED_APK="secrets/ps42.apk"

BUILD_TOOLS_DIR="$ANDROID_HOME/build-tools/30.0.3"

if [[ -z "$ANDROID_HOME" ]]; then
  echo "ANDROID_HOME not set"
  exit 1
fi

if [[ ! -f "secrets/secret-environment-variables.sh" ]]; then
  echo "Secrets not decrypted"
  exit 1
fi

echo "Accepting Android SDK licenses…"
yes | "${ANDROID_HOME}/cmdline-tools/latest/bin/sdkmanager" --licenses > /dev/null

echo "Initializing secrets…"
source "secrets/secret-environment-variables.sh"

echo "Building…"
./gradlew clean :app:assembleRelease

echo "Aligning…"
"$BUILD_TOOLS_DIR/zipalign" -p 4 "$UNSIGNED_APK" "$ALIGNED_APK"

echo "Signing…"
"$BUILD_TOOLS_DIR/apksigner" sign \
  --ks "secrets/release.jks" \
  --ks-pass "$RELEASE_KEYSTORE_PASS" \
  --ks-key-alias "$RELEASE_KEY_ALIAS" \
  --key-pass "$RELEASE_KEY_PASS" \
  --out "$SIGNED_APK" "$ALIGNED_APK"

echo "Verifying…"
"$BUILD_TOOLS_DIR/apksigner" verify --verbose "$SIGNED_APK"

echo "Done!"
echo "The signed APK is at $SIGNED_APK"
