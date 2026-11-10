# Standalone host-only Android preview

Use Node 22.13+ within 22.x or 24.3+ within 24.x, Java 21, and the installed Android SDK at `$HOME/Library/Android/sdk`.

## Services

Keep the existing backend running on port 8080. Build and start the isolated mobile portal:

```sh
npm --prefix ../../web ci
npm run web:build
npm run web:start
```

Set `GIFTWISE_BACKEND_ORIGIN` in `gift-platform/web/.env.local` if the backend origin differs (see `web/.env.example`). The app loads the main web application, so requests use the same `/backend` proxy as the website. Platform-admin pages and APIs return 404 to the app.

## Build

```sh
npm run build:android:local
```

Output: `artifacts/GiftWise-android-host-preview.apk`. It embeds JavaScript, uses local test signing, and targets the x86_64 emulator. **No Metro or Expo development launcher is needed.** A public phone/store release needs a deployed HTTPS service, the correct device architecture and production signing.

## Emulator and installation

```sh
"$HOME/Library/Android/sdk/emulator/emulator" -avd GiftWise_API_36 -no-snapshot -memory 2048 -cores 2 -gpu swiftshader -no-metrics
"$HOME/Library/Android/sdk/platform-tools/adb" -s emulator-5554 wait-for-device
# Wait until Android finishes booting, then:
"$HOME/Library/Android/sdk/platform-tools/adb" -s emulator-5554 reverse tcp:3100 tcp:3100
"$HOME/Library/Android/sdk/platform-tools/adb" -s emulator-5554 install --no-streaming -r artifacts/GiftWise-android-host-preview.apk
"$HOME/Library/Android/sdk/platform-tools/adb" -s emulator-5554 shell am start -n com.giftwise.preview/.MainActivity
```

The local preview is pinned to device loopback (`http://127.0.0.1:3100`) and uses ADB forwarding to reach the Mac. It does not depend on the emulator's `10.0.2.2` alias. Use existing customer credentials; administration remains in the original web app.

The older `GiftWise-android-emulator-debug.apk` is superseded and contains the previous Metro-dependent implementation. Use the host-preview APK and the latest validation record.
