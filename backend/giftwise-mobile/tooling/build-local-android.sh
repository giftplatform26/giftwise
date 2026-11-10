#!/bin/sh
# Standalone, debug-signed emulator preview. Does not publish or use cloud signing.
set -eu
project_root=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
cd "$project_root"
export ANDROID_HOME="${ANDROID_HOME:-$HOME/Library/Android/sdk}"
export ANDROID_SDK_ROOT="$ANDROID_HOME"
export EXPO_PUBLIC_GIFTWISE_WEB_URL="${EXPO_PUBLIC_GIFTWISE_WEB_URL:-http://127.0.0.1:3100}"
export EXPO_PUBLIC_GIFTWISE_LOCAL_PREVIEW=1
export GIFTWISE_RELEASE=0
npx expo prebuild --platform android --no-install
cd android
./gradlew :app:assembleRelease -PreactNativeArchitectures=x86_64 -Pandroid.enableMinifyInReleaseBuilds=false -Pandroid.enableShrinkResourcesInReleaseBuilds=false --no-daemon --max-workers=2
cd ..
mkdir -p artifacts
cp android/app/build/outputs/apk/release/app-release.apk artifacts/GiftWise-android-host-preview.apk
shasum -a 256 artifacts/GiftWise-android-host-preview.apk
