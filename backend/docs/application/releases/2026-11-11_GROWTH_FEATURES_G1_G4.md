# GiftWise growth features release (G1–G4) — 11 November 2026

Fifteen opt-in features that make GiftWise useful before, during and after a celebration, and bring guests back as hosts. Every feature reuses the existing module-permission system and **starts switched off**. Migrations V113–V116 are additive. This release also contains the direct-transfer payment codes, host confirmation and custom receipts work (V111–V112).

## Switch model

Each feature is a module code in `platform_module_controls`, enabled at up to three levels in the admin console:

1. **Platform** — Admin → Hosts & privileges → Platform-wide modules.
2. **Host** — Admin → Hosts & privileges → select host → module grants.
3. **Event** (event features only) — Admin → Events → Celebration feature controls.

A feature is available only when every applicable level is on. Turning any level off hides it immediately: pages disappear from navigation and APIs return 404. Existing data is kept.

| Module code | Levels | Host / guest location |
| --- | --- | --- |
| `LIVE_WALL` | platform, host, event | Host: `/events/[id]/live-wall`. Screen: `/live/[wallToken]` |
| `SHAGUN_REGISTER` | platform, host, event | `/events/[id]/register` |
| `GIFT_LEDGER` | platform, host | `/ledger`; "Save to my gift ledger" on `/r/[token]` receipts |
| `EVENT_FUNCTIONS` | platform, host, event | Host: `/events/[id]/functions`. Guests: schedule on the invitation |
| `CO_HOSTS` | platform, host, event | Owner: `/events/[id]/co-hosts`. Invitee: `/join-team/[token]`, `/helping` |
| `WHATSAPP_MESSAGING` | platform, host, event | `/events/[id]/whatsapp` |
| `OCCASION_REMINDERS` | platform, host | `/reminders` |
| `HOST_REFERRALS` | platform, host | `/referrals`; admin rewards in Admin → Plans & partnerships |
| `YEARLY_REHOST` | platform, host | Suggestion cards on `/dashboard` |
| `MEMORY_BOOK` | platform, host, event | `/events/[id]/memory-book` |
| `VENDOR_DIRECTORY` | platform, host | `/vendors`; admin curation in Admin → Plans & partnerships |
| `EXPERIENCE_GIFTS` | platform, host, event | "Gift type" on wishlist cards (`/gifts`); badges and donate buttons on the invitation |

The live wall also needs `GUESTBOOK` (it shows guestbook wishes).

## Features

**Live venue wall.** Full-screen slideshow of *approved* guestbook wishes and photos for a TV or projector, with three themes, a configurable slide time and an optional "scan to add your wish" QR code. The wall has its own rotatable token (`event_live_walls.wall_token`), separate from host access; pausing or rotating stops the old screen. Newly approved wishes appear next. The screen keeps itself awake and survives short network drops.

**Shagun / gift register.** Host's record of envelopes, cash, UPI and gift items handed over in person: quick ₹501–₹11,000 chips, name suggestions from RSVPs and guestbook, side/family groups, edit with versioning, delete with undo, CSV export (protected against spreadsheet formula injection). Each entry carries a client key, so double taps and offline retries never duplicate; entries made without a connection are queued on the device and synced later. Confirmed online gifts (verified captures and host-confirmed direct transfers) appear alongside. **The register never confirms or moves money and never posts to the payment ledger.**

**Personal gift ledger.** "What I gave" (entered by the user) and "what I received" (from their registers and confirmed online gifts), matched by name (case, accents, spacing and punctuation ignored), with per-person history and balance. Guests can save a direct-transfer receipt into their ledger once (`source_ref`).

**Multi-function schedule.** Up to 15 functions per celebration with date, times, venue, map link (https only), dress code and notes; reorder; per-function RSVP. Guests answer all functions in one form and can change answers with the same email. Public RSVP is rate limited per IP and day.

**Co-hosts and gift-desk helpers.** The owner creates one-time invite links (stored as SHA-256 hashes, valid 7 days, maximum 20 open, 30 members). Roles:
- *Co-host*: gift register (full), guests/RSVPs/wishes moderation, live wall, schedule.
- *Gift-desk helper*: add and view register entries only.

Payments, receiving accounts, invitation design, event details and deletion remain owner-only. All checks go through `cohost/EventAccess`, and feature switches are always evaluated for the owner. Switching `CO_HOSTS` off removes co-host access immediately; owners can always remove people.

**WhatsApp messages.** Paste a guest list ("Name, phone" per line; Indian numbers without +91 accepted), then send an invitation, reminder or thank-you to everyone, guests who have not replied, or guests who are coming. A guest receives each kind at most once per day. A background worker delivers queued messages with retries and backoff. Replies such as `YES 3`, `haan`, `NO`, `maybe 2` become RSVP answers; `STOP` opts the number out across all events and stays respected on re-import. Delivery receipts only move forward (sent → delivered → read).

**Dates to remember.** Birthdays and anniversaries with a six-week "coming up" list, "turns N", and what the user gave that person last time.

**Invite friends (referrals).** Built on the V105 referral tables. A friend who signs up with `/register?ref=CODE` within 14 days of account creation is attributed once; the referral qualifies when they publish their first celebration. The admin grants the reward and marks it in Admin → Plans & partnerships (audited as `REFERRAL_REWARDED`). The reward text is configurable (`gift.referrals.reward-text`); granting the reward itself is manual.

**Yearly re-hosting.** Birthdays and anniversaries hosted at least six months ago are suggested about 60 days before their next date. One tap creates a private draft with the same details, this year's date, a retitled name (year updated) and the WhatsApp guest list minus opted-out numbers. Each source event is suggested once a year.

**Memory book.** Print-ready A4 keepsake (cover, story, schedule, approved wishes with photos, people who gave gifts). Amounts are excluded unless the host ticks "Include amounts". Saved as PDF through the browser's print dialog.

**Vendor directory.** Admin-curated vendors (category, city, phone, website, Instagram, price hint, verified flag). Hosts filter, shortlist and contact vendors directly (WhatsApp, call, website). No bookings or payments.

**Experience and charity wishes.** Wishlist items can be marked *Experience* (works like any gift) or *Charity* (name plus https donation link). Guests donate on the charity's site; the database trigger `reject_charity_gift_payment` refuses payment orders and direct-transfer claims for charity wishes. A gift with existing contributions cannot become a charity wish.

## Configuration

| Property | Default | Purpose |
| --- | --- | --- |
| `gift.whatsapp.provider` | `DEV_OUTBOX` | `DEV_OUTBOX` records messages without sending (practice mode, simulated replies allowed). `META_CLOUD` sends through the WhatsApp Business Cloud API. |
| `gift.whatsapp.meta.phone-number-id`, `gift.whatsapp.meta.access-token` | — | Required for `META_CLOUD`; startup fails without them. Keep the token secret. |
| `gift.whatsapp.meta.api-version` | `v21.0` | Graph API version. |
| `gift.whatsapp.meta.verify-token`, `gift.whatsapp.meta.app-secret` | — | Webhook subscription token and HMAC secret (each at least 16 characters). The webhook returns 404 until both are set. |
| `gift.whatsapp.templates.invitation` / `.reminder` / `.thank-you` | `giftwise_invitation` / `giftwise_reminder` / `giftwise_thank_you` | Approved template names. |
| `gift.whatsapp.templates.language` | `en` | Template language code. |
| `gift.whatsapp.default-country-code` | `91` | Country code for 10-digit numbers. |
| `gift.whatsapp.batch-size`, `gift.whatsapp.worker-delay-ms` | `20`, `10000` | Delivery worker pace. |
| `gift.referrals.reward-text` | "A free premium celebration…" | Shown to hosts on `/referrals`. |
| `gift.guest-base-url` | existing | Used in WhatsApp links, wall QR codes and referral links. |

### WhatsApp templates to submit to Meta

Body parameters are positional and must match exactly:

- **Invitation** — `{{1}}` guest name, `{{2}}` host name, `{{3}}` event title, `{{4}}` invitation link. Suggested text: "Hi {{1}}! {{2}} invites you to {{3}}. See the invitation: {{4}}. Reply YES, NO or MAYBE (add how many are coming, e.g. YES 3). Reply STOP to stop messages."
- **Reminder** — `{{1}}` guest name, `{{2}}` event title, `{{3}}` date, `{{4}}` link.
- **Thank you** — `{{1}}` guest name, `{{2}}` event title, `{{3}}` host name.

Webhook URL: `https://<backend-host>/api/webhooks/whatsapp` (GET verification and POST notifications, signature `X-Hub-Signature-256` verified).

## Mobile

The mobile app loads the main web application, so all host and guest features above are available in the app with no mobile code change. Its navigation policy treats non-admin pages as internal and opens WhatsApp, phone, vendor and charity links externally. The new admin panels (vendors, referral rewards) are admin-only and therefore web-only, like the rest of administration.

## Verification performed (11 November 2026)

- New backend suites: `LiveWallRegisterLedgerIntegrationTest` (10), `CoHostFunctionsIntegrationTest` (8), `WhatsAppIntegrationTest` (8), `GrowthBatchFourIntegrationTest` (7), plus the affected existing suites — 173 tests in total, all passing.
- Web: 473 unit tests passing, TypeScript clean, production build compiled (all new routes).
- Mobile: 14 tests passing.
- Not yet performed: browser acceptance on desktop and phones, live WhatsApp delivery (needs Meta approval), and physical-device checks.

## Rollout checklist

1. Deploy backend (Flyway applies V111–V116) and web together.
2. In Admin, enable one feature at platform level, grant it to a test host and test event, and try it end to end.
3. For WhatsApp, keep `DEV_OUTBOX` until templates are approved; then set `META_CLOUD`, the credentials, and the webhook in the Meta dashboard.
4. Add a few verified vendors before enabling `VENDOR_DIRECTORY`.
5. Decide the referral reward and update `gift.referrals.reward-text`.
