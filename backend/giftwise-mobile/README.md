# GiftWise mobile — host and guest application

GiftWise mobile supports celebration hosts, guests, wishes, group gifts, contributions, invitations, memories, account settings, and the other existing host/guest web workflows. **Platform administration is excluded from mobile.** The original sibling web application's admin console and backend sources are unchanged.

The mobile app uses the isolated shared frontend in `web/`. Native navigation and deep links deny admin paths. The mobile portal returns 404 for admin pages and admin APIs, removes admin sign-in links, and rejects non-customer account roles after sign-in. The retained admin source files are shared-web reference code behind this denial layer; they are not accessible mobile routes.

## Standalone Android preview

Build the emulator preview locally:

```sh
npm ci
npm --prefix web ci
npm run web:build
npm run web:start
# Another terminal, using a Node version supported by Expo SDK 58:
npm run build:android:local
```

Output: `artifacts/GiftWise-android-host-preview.apk` (version 0.3.0, x86_64 emulator, test signed). JavaScript is embedded in this APK: it does **not** need Metro or Expo Go. It still needs the shared web service and the actual backend running.

The backend defaults to `http://localhost:8080` on this Mac; the mobile portal runs on port 3100. The local preview uses device loopback and ADB forwarding:

```sh
"$HOME/Library/Android/sdk/platform-tools/adb" -s emulator-5554 reverse tcp:3100 tcp:3100
"$HOME/Library/Android/sdk/platform-tools/adb" -s emulator-5554 install --no-streaming -r artifacts/GiftWise-android-host-preview.apk
"$HOME/Library/Android/sdk/platform-tools/adb" -s emulator-5554 shell am start -n com.giftwise.preview/.MainActivity
```

This is an emulator test build, not a Play Store release or an iPhone binary. For public distribution, configure an owned HTTPS frontend origin, real app identifiers, and release signing. Local HTTP preview flags are explicitly refused by production build configuration. Host and guest app code is shared with iOS, but no iOS binary is delivered here.

## Source map

- `App.tsx`: branded mobile home and host/guest entry points.
- `src/platform/Workspace.tsx`: embedded workspace, native back/share/export handling.
- `src/platform/policy.mjs`: URL, deep-link, admin-route, and bridge-message boundaries.
- `web/middleware.ts`, `web/lib/mobile-scope.mjs`: mobile portal admin-route exclusion.
- `web/app/login/page.tsx`: customer-only mobile login using existing backend sessions.
- `tooling/build-local-android.sh`: reproducible standalone Android preview build.
- `assets/brand/`, `brand/BRAND_SYSTEM.md`: editable GiftWise identity and strategy.
- `LOCAL_ANDROID.md`, `brand/VALIDATION.md`: installation and verification notes.

## Verification

```sh
npm run typecheck
npm test
npm run web:test
npm run web:build
```

Real account sign-in, event data, contribution outcomes, and provider actions remain governed by the backend. No account credentials or mock payment confirmations are embedded in the default app. The old `BrandPreview.tsx` is an archived design reference, not the running application.
