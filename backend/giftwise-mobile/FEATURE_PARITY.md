# Mobile feature scope — v0.3

The mobile app retains the shared host and guest experience. Platform administration is intentionally excluded, as requested. The original web application at `gift-platform/web` retains administration independently.

## Available host/guest feature areas

- Registration, customer login, account recovery, verification, profile, session expiry and logout.
- Dashboard and celebrations: create/edit, planning/reuse, publish, invitations, previews and QR.
- Wishlists, guest gifts, group gifting, creator/member participation and contribution histories.
- Receiving accounts, contribution reports, payment verification and existing provider checkout flows.
- Guests/RSVP/attendance, guestbook, memory albums, notification history and thank-you cards.
- Invitation studio, templates/backgrounds/stickers, AI tools where enabled, file exports and sharing.
- Existing host plans/partnerships, localization, focused/classic presentation and feature eligibility.

These functions reuse existing backend contracts and permissions. Source availability does not certify every payment/provider workflow on every device.

## Excluded from mobile

- Native platform-admin sign-in and administration card.
- Admin console, invitation-gallery administration and all `/admin` descendants.
- Administration through custom deep links, same-origin navigation, window targets, or encoded paths.
- Admin-role sign-in to the mobile portal; the attempted session is signed out and the user is directed to use the web application.
- Same-origin `/backend/api/admin` and `/api/admin` access through the mobile portal.

The isolated web snapshot retains reference admin implementation files for source continuity. Its middleware rejects the routes with HTTP 404 before they can be served. Production deployment of this mobile frontend must include its middleware. No existing backend role or original desktop web feature was removed.

## Delivery model

The standalone Android preview contains its JavaScript and needs no Metro/Expo Go. It opens the host-only shared frontend, which talks to the actual backend. The local emulator preview uses ADB-forwarded `127.0.0.1:3100`; web and backend services must be running. Production builds continue to require HTTPS and proper release identities/signing.

Native adapters cover back/reload navigation, native sharing, clipboard fallback and bounded file export. Device payment redirects, uploads, authenticated host/guest journeys, full accessibility, and physical-device coverage need separate release QA. iOS source shares the exclusion rules; an iOS signed binary is not included.
