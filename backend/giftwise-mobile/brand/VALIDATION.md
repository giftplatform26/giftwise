# GiftWise v0.3 validation — 5 November 2026

## Completed

- Standalone Android APK built successfully (`assembleRelease`; local test signing, no production publishing).
- Installed version 0.3.0 / versionCode 2 on `GiftWise_API_36`; ADB returned `Success`.
- App launched directly, with customer login loaded in its native WebView. No Metro or Expo development launcher is required.
- Read-only check **inside the installed Android WebView** returned HTTP 200 from `/backend/api/public/experience`, with live backend data `{ "viewMode": "FOCUSED" }`.
- The same installed-app check returned **404** for `/admin`.
- Sent `giftwise://open/admin` to the running app; it remained on customer login, and the repeated backend check passed. The temporary inspection port was then closed.
- Portal HTTP checks: `/login`, `/register`, and live public backend data returned 200; `/admin`, `/admin/login`, `/admin/invitation-assets`, encoded `/%61dmin`, and `/backend/api/admin/console/summary` returned 404.
- All **415 shared web regression tests** and **14 mobile tests** passed. Tests include customer-only login and normalized admin-path/deep-link exclusion.
- Strict TypeScript and production Next.js build passed.
- APK signature verified; embedded `assets/index.android.bundle` confirmed.

## Artifact

`artifacts/GiftWise-android-host-preview.apk` — 26,245,779 bytes; x86_64 emulator; min SDK 24, target SDK 36.

SHA-256: `a1e76d690d37d5432e998e1f1e89cf751919f17647951eeb376597982db54344`.

Machine-readable evidence: `artifacts/build-info.json` and `artifacts/backend-verification.json`.

## Scope and remaining limits

Platform administration is excluded from the native welcome, mobile login, deep links, first-party navigation, and the mobile portal's pages/APIs. Original backend Java and sibling web administration are unchanged.

This is a test-signed **Android emulator** preview using ADB-forwarded loopback, not a public phone/store release. The actual web/backend services must stay running. No iOS binary was built. Existing customer credentials are required to access private events; authenticated customer flows, provider payments, uploads/exports, full accessibility, and physical-device QA were not completed in this check. No payment, invitation send, admin policy change, or account creation was performed.

The local preview enables WebView inspection for verification only. Production configuration refuses the local-preview flag and continues to require HTTPS and release identities/signing.

The earlier package audit reported five moderate findings excluding development dependencies; the EAS development tooling audit included additional findings. No forced historical SDK/CLI downgrade was applied. Review compatible upstream patches before public distribution.
