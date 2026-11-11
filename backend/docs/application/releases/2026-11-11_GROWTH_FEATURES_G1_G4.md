# GiftWise growth, event-management and acquisition release (G1–G6) — 11 November 2026

Twenty-eight opt-in features that make GiftWise useful before, during and after a celebration, and bring guests back as hosts. Every feature reuses the existing module-permission system and **starts switched off**. Migrations V113–V118 are additive. This release also contains the direct-transfer payment codes, host confirmation and custom receipts work (V111–V112).

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
| `HOST_REFERRALS` | platform, host | `/referrals`; admin rewards in Admin → Monetization |
| `YEARLY_REHOST` | platform, host | Suggestion cards on `/dashboard` |
| `MEMORY_BOOK` | platform, host, event | `/events/[id]/memory-book` |
| `VENDOR_DIRECTORY` | platform, host | `/vendors`; admin curation in Admin → Monetization |
| `EXPERIENCE_GIFTS` | platform, host, event | "Gift type" on wishlist cards (`/gifts`); badges and donate buttons on the invitation |
| `PLANNING_CHECKLIST` | platform, host, event | `/events/[id]/checklist` (co-hosts too) |
| `EVENT_BUDGET` | platform, host, event | `/events/[id]/budget` (owner only) |
| `RUN_SHEET` | platform, host, event | `/events/[id]/run-sheet`; vendors: `/run/[token]` |
| `CATERING_HEADCOUNT` | platform, host, event | Meal choice in the guest schedule RSVP; totals on `/events/[id]/functions` (needs `EVENT_FUNCTIONS`) |
| `GUEST_CHECKIN` | platform, host, event | `/events/[id]/checkin` (co-hosts and gift desk too) |
| `LIVE_UPDATES` | platform, host, event | `/events/[id]/updates`; banner on the invitation, ticker on the live wall |
| `RETURN_GIFTS` | platform, host, event | "Return gift" tick on gift register rows |
| `TRY_WITHOUT_ACCOUNT` | platform only | `/try` (visitors have no host account or event yet, so host and event switches don't apply) |
| `STORY_CARDS` | platform, host, event | Host: `/events/[id]/story-cards`. Guests: "Share this invitation to your Status" on the invitation |
| `PARTNER_ACCOUNTS` | platform, host (the partner), event (branding and access) | `/partner`, client handover `/handover/[token]`, public page `/p/[slug]`, "Planned by" on invitations |
| `GROUP_COLLECTIONS` | platform, host, event (each collection) | `/collections`; guest page header on the collection's invitation link |
| `GUEST_FIRST_FREE` | platform, host, event | Offer on invitations and receipts of enabled events; claimed automatically after sign-up |
| `CELEBRATION_SHOWCASE` | platform, host, event + admin approval | Host: `/events/[id]/showcase`. Admin: Admin → Monetization → Showcase submissions. Public: `/showcase` and the home page |

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

**Invite friends (referrals).** Built on the V105 referral tables. A friend who signs up with `/register?ref=CODE` within 14 days of account creation is attributed once; the referral qualifies when they publish their first celebration. The admin grants the reward and marks it in Admin → Monetization (audited as `REFERRAL_REWARDED`). The reward text is configurable (`gift.referrals.reward-text`); granting the reward itself is manual.

**Yearly re-hosting.** Birthdays and anniversaries hosted at least six months ago are suggested about 60 days before their next date. One tap creates a private draft with the same details, this year's date, a retitled name (year updated) and the WhatsApp guest list minus opted-out numbers. Each source event is suggested once a year.

**Memory book.** Print-ready A4 keepsake (cover, story, schedule, approved wishes with photos, people who gave gifts). Amounts are excluded unless the host ticks "Include amounts". Saved as PDF through the browser's print dialog.

**Vendor directory.** Admin-curated vendors (category, city, phone, website, Instagram, price hint, verified flag). Hosts filter, shortlist and contact vendors directly (WhatsApp, call, website). No bookings or payments.

**Experience and charity wishes.** Wishlist items can be marked *Experience* (works like any gift) or *Charity* (name plus https donation link). Guests donate on the charity's site; the database trigger `reject_charity_gift_payment` refuses payment orders and direct-transfer claims for charity wishes. A gift with existing contributions cannot become a charity wish.

## Event-management tools (G5, migration V117)

**Planning checklist.** Tasks with due dates, categories, an owner (host or co-host) and notes, grouped as Overdue, This week, Coming up, No date and Done, with a progress ring and a days-to-go count. "Add starter tasks" inserts 8–12 occasion-specific tasks dated back from the event (tasks whose date has already passed move to three days from now). Co-hosts share the list; gift-desk helpers can't see it.

**Budget and vendor payments.** Owner only. Optional total budget, then one line per expense with planned, booked (quoted) and paid amounts and the next due date. Shows totals, health ("82% of budget allocated", "Over budget"), payments due in the next 14 days, and per-category groups. Paid can't exceed the booked amount. A planning aid; it never pays anyone.

**Day-of run sheet.** Time, what happens, who is responsible, where, optional link to a function, and a done tick. Printable. "Share with vendors" creates a read-only link (`/run/[token]`) that refreshes every minute and can be replaced at any time.

**Catering headcount.** When on (with the schedule), guests choose a meal preference (vegetarian, Jain, non-vegetarian, vegan, no meal) per function. The host's schedule shows people per meal for each function. When off, no meal is stored.

**Entry check-in.** A phone-first door screen: live headcount, search across RSVPs, schedule answers and the WhatsApp guest list (highest expected party size wins), one tap plus party size per family, walk-ins, and undo. Refreshes every 15 seconds so several helpers can check guests in at once.

**Live updates and stream link.** Short announcements (200 characters, up to 40 a day) shown on the invitation for 24 hours and on the live wall for 6 hours, with quick suggestions such as "Dinner is now being served". An optional https livestream link appears as a "Watch live" button on the invitation. Guests never see who posted.

**Return gifts.** A "Return gift" tick on each gift register row and a "given / total" counter. Gift-desk helpers can tick it too.

Co-host roles now include: co-host → planning checklist, run sheet, check-in and live updates; gift-desk helper → check-in. The budget stays with the owner.

## Customer acquisition (G6, migration V118)

**Try without an account.** `/try` lets a visitor pick the occasion, title, host name, date and city with a live phone preview. The draft stays on the device for 7 days. "Save & share" asks for a free account; after sign-up or sign-in, `/try/claim` moves the draft into the normal creator, which then creates the event as usual. The home page shows "Try it free — no sign-up" when on. Sign-up now forwards `next` to sign-in, which also fixes the return path for receipt and invite links.

**Status and Story cards.** 1080×1920 images drawn in the browser: Invitation, Countdown ("5 days to go"), Thank you and Happening now, using the occasion artwork, title, date, host and the invitation link, with a small "Made with GiftWise" line. "Share to Status" shares the image file where the phone supports it, otherwise downloads it. Guests get the invitation card on the invitation page.

**Planner and venue partners.** A host granted `PARTNER_ACCOUNTS` creates a business profile (name, type, city, phone, website, colour, logo, page address) and a public page at `/p/[slug]`. Partners create celebrations as usual, then press "Hand over" to get a one-time link (hashed, 14 days). When the client accepts: the client becomes the owner, the partner becomes a PARTNER co-host (co-host abilities, no payments or budget), and the client receives the partner's host-level feature grants so the celebration keeps working (audited as `PARTNER_HANDOVER`; the admin can change grants any time). Handover is refused once receiving accounts, payments or gift reports exist, because money is tied to the owner. Guests see "Planned by …" when the event switch is on.

**Office and group gifts.** `/collections` creates a collection in three steps: who it's for and the occasion, a target and closing date, and one of the organiser's confirmed UPI or bank accounts. It becomes a published event of kind `COLLECTION` with one gift, so contributions use direct transfers, payment codes, receipts and host confirmation unchanged, and the existing publish checks apply. Its event switch starts on so the admin can switch off an individual collection. The guest page shows a header with the recipient, confirmed progress (if the organiser allows), contributor count and closing date. Guests' wishes form the card.

**First celebration free for guests.** On invitations and receipts of enabled events, guests see "Planning your own celebration? Start free". After sign-up the claim runs once: the account must be under 14 days old, never have published a celebration and never claimed before, and can't be the inviting host. It grants premium (`HOST_PLAN`) for `gift.offers.first-free-days` (default 90) through a zero-value `FREE` commercial order and entitlement, so every premium check honours it.

**Celebration showcase.** Hosts submit a published celebration with a public title, city, one line and up to six approved wishes, after confirming consent. Submissions wait for platform-admin approval; any edit sends them back for review; hosts can withdraw at any time. Public cards show first names only, never amounts, contacts or unapproved photos. Approved items appear on `/showcase`, the home page and the partner's page.

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
| `gift.offers.first-free-days` | `90` | Premium days granted by the first-celebration-free offer (1–365). |
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

- Backend, full: 686 unit tests (`./mvnw test`) and 726 PostgreSQL integration tests across all 79 integration classes (`./mvnw test-compile failsafe:integration-test failsafe:verify`), all passing. Note: `./mvnw test` alone does not run the `*IntegrationTest` classes.
- New integration suites: `LiveWallRegisterLedgerIntegrationTest` (10), `CoHostFunctionsIntegrationTest` (8), `WhatsAppIntegrationTest` (8), `GrowthBatchFourIntegrationTest` (7), `EventManagementIntegrationTest` (8), `AcquisitionIntegrationTest` (7).
- Web: 483 unit tests passing, TypeScript clean, production build compiled (all new routes).
- Mobile: 14 tests passing.
- Not yet performed: browser acceptance on desktop and phones, live WhatsApp delivery (needs Meta approval), and physical-device checks.

## Rollout checklist

1. Deploy backend (Flyway applies V111–V118) and web together.
2. In Admin, enable one feature at platform level, grant it to a test host and test event, and try it end to end.
3. For WhatsApp, keep `DEV_OUTBOX` until templates are approved; then set `META_CLOUD`, the credentials, and the webhook in the Meta dashboard.
4. Add a few verified vendors before enabling `VENDOR_DIRECTORY`.
5. Decide the referral reward and update `gift.referrals.reward-text`.

## Page speed

Measured 11 November 2026 on the development Mac. The time to fetch a host page when navigating was 0.012–0.038 s with a production build (`npm run preview`), against 0.7–3.9 s on the first visit to each page under `npm run dev`, which compiles pages on demand. Browser testing should use the production build. `npm run dev:fast` (Turbopack) roughly halves first-visit compile time for development. The localization catalog is no longer re-downloaded on every page change.
