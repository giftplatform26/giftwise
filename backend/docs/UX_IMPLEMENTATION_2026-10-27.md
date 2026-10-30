# Responsive UX implementation — 27 October 2026

The first implementation of the UX blueprint is in source. It adds an optional Focused experience alongside the existing Classic experience. It has not been deployed or enabled in the running application.

## Enable and roll back

1. Restart the updated backend so Flyway applies `V68__platform_experience_settings.sql`.
2. Build and restart the updated `web` application using the normal deployment process.
3. Sign in as a platform administrator, open **Application view**, and select **Focused**.
4. Reload an existing host or guest page to see the selected experience.

Classic is the database default and the client fallback when settings cannot be loaded. Selecting Classic in the same admin panel restores the existing route layouts. New focused-only routes redirect to Celebrations in Classic mode. The choice is platform-wide, persisted, administrator-only, CSRF-protected and audited. Existing open pages keep their view until reload so a remote admin change does not replace an active form.

## Implemented

- Guest invitation: one event identity, occasion artwork, a personal note, concise gift cards, optional details, on-demand RSVP/wishes, and privilege-controlled group/sharing features.
- Contribution checkout: amount/name followed by payment method; optional contact details collapsed; inline eligible UPI instructions with app link and QR; clearly separate confirmed payments and unverified transfer reports. Hosted checkout temporarily suspends the native dialog and restores it when cancelled.
- Host workspace: focused dashboard and celebration list; selected-event header, local navigation, overview/checklist and one invitation-sharing panel.
- Creation: separate five-step occasion/people/details/style/review wizard, profile prefill, tab-local draft recovery and the existing complete creation request. Approved public host name is preserved by request validation.
- Preview: shared guest presentation, owner-authorised draft data and effective module permissions; payment and guest submission actions disabled.
- Supporting screens: progressive gift and receiving-account forms; mobile contribution/activity rows; RSVP/guestbook tabs; notification details; thank-you preview; registration steps; reduced group checkout repetition; collapsed group account/security sections; admin gallery disclosure and effective privilege explanations.
- Invitation editor: mobile Preview / Design tools / Selected element tabs and fit-to-screen canvas; event selection by title; truthful local-save error handling.
- Shared scoped typography, spacing, touch controls, responsive layouts, focus states and reduced-motion styles. Focused route components load dynamically, and independent invitation/progress data requests run concurrently.

The public event response adds only the approved host display name and invitation theme. Private address and contact data remain outside that response. Existing server ownership, module permissions, payment verification, allocation and ledger rules remain authoritative.

## Validation

- Backend unit suite: 569 tests, zero failures/errors.
- Final module-privilege and experience-switch integration suite: 10 tests, zero failures/errors, including administrator access, CSRF, persistence/audit, invalid settings, draft ownership and public-response boundaries.
- Frontend: 340 tests passed; TypeScript check passed; isolated Next.js production build passed.
- Headless Chrome against fixture APIs: mobile invitation reflow at 320/390px, desktop invitation, manual-transfer receipt wording, hosted-payment handoff/cancellation, complete event creation, private preview, gift management, mobile activity, collapsed receiving-account forms, mobile editor panels, Classic rollback and revoked sharing permissions passed.

Browser checks used sample data without live transfers. Native iOS/Android applications, Safari/device testing, a full accessibility audit, moderated usability sessions and measured conversion/latency comparisons remain follow-up work. No speed or conversion improvement is claimed without production measurements. The blueprint's longer-term workflow redesigns, such as complete admin report drawers, native sharing and server-persisted partial creation drafts, are not all part of this implementation.

## Review screenshots

These show fixture data from the final production build:

- [Guest invitation](ux-review/guest-mobile.png)
- [Contribution checkout](ux-review/checkout-mobile.png)
- [Event overview](ux-review/event-overview-mobile.png)
- [Creation wizard](ux-review/create-event-mobile.png)
- [Mobile invitation editor](ux-review/invitation-editor-mobile.png)
- [Admin view switch](ux-review/admin-view-switch.png)
