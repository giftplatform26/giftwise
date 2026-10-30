# Gifting platform UX blueprint

Date: 27 October 2026. Scope: responsive web redesign, with an information architecture that can later support native iOS/Android. This is a source-based design review, not a rendered browser/device usability study. No product implementation is included.

## Design direction

Make one celebration and one next action the centre of each task. Use the recipient's identity, an invitation image, and their own words to create emotion. Keep amounts, payment status and actions precise. Replace repeated hearts, generic celebration slogans and competing panels with a calm hierarchy.

Three environments:

- Guest invitation: the celebration, gifting, optional attendance/wishes. No host navigation.
- Host workspace: account-level event list; focused event workspace after selecting an event.
- Platform administration: operational controls, permissions, policies and audit. Calm, explicit and separate from celebration styling.

## Observed issues

- My events combines profile-name editing, a marketing banner, a long creation form, and an event list. Individual cards contain many management links.
- Gift cards show value, received, remaining and count; checkout repeats the same detail. A separate celebration contribution section competes with wishlist gifting.
- Guest pages repeat generic emotional headings instead of giving the recipient's own story priority.
- The host sidebar remains around individual event tasks and preview. Preview is a separate implementation from the actual guest invitation.
- The gift manager exposes PostgreSQL, backend connectivity, manual UUID entry and server-storage copy.
- Creator setup, security, verification and contributions compete within a large group panel. Member/guest pages repeat the gift and celebration context inside embedded checkout.
- Reports expose many totals, filters and columns simultaneously. Mobile needs a summary and expandable transaction rows.
- Payment setup places saved destinations, forms, verification explanations, QR tools and withdrawals together.

## Guest invitation

Initial mobile layout:

1. Small occasion label. No clickable host-workspace logo.
2. One cover image, selected invitation design or restrained occasion illustration. No tall empty hero.
3. Event title, one invitation sentence, date. Approved public host display name only if exposed by the public API.
4. One primary action: Choose a gift. RSVP is a secondary text action when enabled.
5. One or two lines of the host's story; expand for more.
6. Gift cards with image/illustration, name, short description and one concise progress summary. Put accounting detail behind View details. A selected gift opens its contribution flow directly.
7. An alternate choice within the same gifting section: Contribute to the celebration. Avoid a separate full-size hero for the wallet.
8. Optional RSVP and wishes summaries. Forms open on demand. Approved wishes show a short preview, with View all for the complete list.
9. Start a group gift as a secondary action when granted. Existing private groups remain accessible through their private links; never expose a private group directory.
10. One share action, subject to privileges, in the event actions menu or footer. No repeated WhatsApp banners.

The fixed mobile contribution action appears only after the hero action scrolls out of view; it disappears while checkout is open. There must never be two simultaneous primary buttons for the same action. It must not obscure the last card, input, validation message or keyboard.

Desktop retains the same ordering. Use a restrained content width and a two-column gift grid. Checkout becomes a side panel or centred dialog, with exactly the same fields and states as mobile.

Financial honesty is part of the hierarchy. Current public progress combines captured amounts and manual reports. Do not label the combined value Confirmed, Received or Fully funded. Prefer separate confirmed and reported figures once the API exposes both; until then label the aggregate Recorded contributions and explain that it includes unverified reports. A reported manual transfer must not trigger a verified-funds badge.

## Contribution flow

Use a full-height mobile sheet or focused page for multistep checkout, and a side panel on desktop. Reserve compact sheets for short tasks. Never nest a payment dialog inside a checkout dialog.

- Amount: compact gift context, suggested amounts plus Other, contributor name, optional contact collapsed where policy allows.
- Method: only eligible enabled methods. Recipient and amount appear once in a compact summary. Communicate any excess-allocation rule before final commitment.
- Payment: UPI app action only when the server supplies a valid approved link. QR is an alternate option, useful for another device; provide Save QR/copy details where supported. A same-phone guest should not need to scan their own screen.
- Result: Confirmed, Reported—unverified, Confirmation pending, Failed or Cancelled. Each has distinct wording and a recoverable next action.

For manual transfers: Open payment instructions → transfer externally → return → enter reference → Submit transfer details. Never turn a self-reported transfer into a paid receipt. Preserve amount, name, selected method and reference during app switching. Do not automatically repeat a payment attempt. Keep demo/test notices visible for the relevant payment method.

## Event-focused host workspace

Account-level navigation: Celebrations, Activity, Account. Creation is an action in Celebrations, not a permanent competing destination.

Within an event:

- Header: Back to celebrations, current event title/date/status, overflow menu.
- Local navigation: Overview, Gifts, Guests, Activity. Show only available sections.
- Overview: one next-step card, invitation preview, concise status summary. Draft primary action is Continue setup or Review & publish; published primary action is Share invitation.
- Share opens one panel containing copy link, QR download and enabled sharing channels. Preview lives within this flow or an understated header action, not on every card and footer.
- Hide global dashboard shortcuts and marketing banners inside event tasks. Keep a single escape back to Celebrations.

The event list is only a list. Each event card contains one visual, title/date/status, a context-sensitive Continue setup/Open event action, and an overflow menu. Unpublish and archive are secondary/destructive actions in event settings, with clear consequences.

Preview must use the same guest presentation component and effective feature flags. Add only a compact preview toolbar with Back to event and viewport controls. Preview shows the current draft without changing public visibility and blocks real payment actions. This requires an authorised host preview data adapter; a public URL cannot preview an unpublished event.

## Conversational event creation

Move creation out of the event-list screen. Use a dedicated, focused route with Back, a short step label, and one Continue action. Entered data survives Back, validation and accidental navigation.

1. What are we celebrating? Occasion choices and a suggested editable title. No default birthday selection before the person chooses an occasion.
2. Who is this moment for? Guest-facing host name, event title and short personal invitation. Prefill from the profile where appropriate; distinguish public host name from private contact name.
3. When and where? Date, with Date to be decided where supported, then a grouped venue/address block. Show country once, preselected but editable. Expand address lines as needed rather than showing every empty input at once.
4. How should it feel? A small set of occasion-appropriate invitation styles with a live thumbnail. Use existing design assets first; advanced editing stays optional and separate.
5. Review your celebration. Plain-language public/private summary, compact Edit actions and Create celebration. Then route into the event checklist: add gifts, configure receiving details where needed, preview and publish.

Do not add wishlist setup, bank verification and full invitation editing to the creation wizard. Those remain resumable event setup tasks after creation.

Current APIs require event address/contact data at creation. A front-end wizard can collect these progressively before the final request. Saving partial server drafts or deferring mandatory fields until publication needs a backend contract change. Never claim a draft was saved before persistence succeeds. Preserve mandatory contact/PIN/verification policy requirements.

## Other screen blueprints

| Screen | Layout and primary action | Remove or defer |
|---|---|---|
| Host home | Upcoming/current celebrations first, one useful next step per event, Create celebration | Status-count tile wall; repeated studio/gift shortcuts |
| Gift management | Event header → gift list → Add gift sheet; detail expands for editing | UUID input, backend/DB notices, permanently open creation form |
| Invitation editor / QR | Focused canvas → templates/text/photo tools → Save → share preview | Multiple overlapping studios and QR/settings panels; full tool palettes on phones |
| Guest RSVP | Event/date summary → attendance choice → party size if applicable → Send RSVP | Always-open large form on the main invitation |
| Wishes | Short approved-wishes preview → Leave a message → optional photo | Simultaneous form, gallery and lengthy instructions |
| Contributions | Confirmed total and clearly separate unverified reports → activity list → filters sheet | Seven or eight equal-weight totals; wide table as the only mobile view |
| Contribution details | Amount/status/person first, then allocation and reference detail | Every accounting field in the summary row |
| RSVP management | Attendance summary → searchable guest rows → expandable guest detail | Full guestbook moderation mixed into the same initial viewport |
| Guestbook moderation | Pending first; message/photo cards with Approve/Hide and clear undo where supported | Spreadsheet-style moderation and repeated global totals |
| Notifications | One event activity timeline; group by date; expand technical delivery detail | Separate competing activity/delivery sections; long bodies on every row |
| Thank-you studio | Preview with editable message; Send/queue action states its actual capability | A second unrelated editor; delivery claims based only on queued records |
| Payment settings | Saved accounts with masked details/status/default → Add account | Always-open add forms; QR generation and payouts mixed into account setup |
| Withdrawals | Eligible balance, pending requests, clear Request withdrawal flow | Unverified guest reports shown as withdrawable funds |
| Sign-in | Account context → identifier/password → Sign in; recovery secondary | Generic celebration hero; user-facing role codes |
| Registration | Short identity step then security step; clear required contact explanation | Premature decorative setup; extra confirmation fields without a policy/security purpose |
| Recovery / verification | Contextual account type, masked destination, code step, clear result/return path | Manual role selection when a trusted entry context already identifies it; vague Send buttons |
| Group creator | Gift/goal → contribution summary → Share group invitation; Members, Activity and Settings secondary | Account security/contact verification competing with the main gift workflow |
| Group member | Shared gift → own contribution/status → Contribute; Members/history secondary | Repeat group/event descriptions inside checkout; other members' amounts without permission |
| Group guest | Group creator/gift context → amount → method → report → receipt; Continue without joining if allowed | A forced registration or member login before guest contribution |
| Admin overview | Exceptions and operational status → hosts/events/reports | Emotional guest branding; every dataset at once |
| Admin privileges | Select host → platform switch, host grant, effective availability, reason | Ambiguous disabled toggles; mixing stored host grant with effective availability |
| Admin policies | Requirements and scope → edit → impact summary → audited save | Unexplained internal scope IDs and unsupported options |
| Admin gallery | Asset grid → upload/replace → preview/publish status | Upload form permanently above the whole catalogue |
| Admin reports/audit | Filters → concise rows → detail drawer; desktop table/mobile list | Repeated counters and dense unstructured audit paragraphs |

All existing authentication, ownership, privacy, module-grant and payment-verification rules remain authoritative. Visual simplification must not broaden access.

## Visual language and delight

- Use warm ivory/white surfaces, deep plum or cocoa for readable text, one restrained celebration accent and genuine recipient imagery when available and permitted. Occasion variations should change a cover/accent rather than every component.
- One expressive heading style and one highly readable body family, at most two font families. Suggested web base: 16 px body, 14 px secondary text, 28–36 px mobile event title; accommodate text resizing.
- Spacing tokens: 4/8/12/16/24/32 px. Mobile page gutters 16–20 px. Use whitespace for grouping; cards only for independently actionable content.
- Standard, not exaggerated, rounding and shadows. Keep system status colours consistent and use text/icons as well as colour.
- Occasion selection updates the invitation thumbnail; template selection uses a brief crossfade; successful save resolves to a quiet checkmark and Saved.
- A small celebration may follow publication or independently confirmed payment. Manual reports get a warm acknowledgement without a paid animation. No repeated confetti, forced countdowns, autoplay sound or fabricated social proof.
- Respect reduced-motion preferences. On native apps, use platform-appropriate light haptics sparingly; do not depend on browser vibration support.

## Microcopy

| Current / technical wording | Proposed |
|---|---|
| Contribute with Love | Contribute |
| Send Love to the Celebration | Contribute to the celebration |
| I have done the payment | Submit transfer details |
| Payment reported by guest — automatic verification pending | Reported — not yet verified |
| Loaded gifts from PostgreSQL | Your wishlist is up to date |
| Server gifts | Gifts |
| Event UUID | Remove from normal host flow; use event title |
| Payment Facilities | Receiving accounts |
| Enable invite link | Share group invitation, after the publish action actually succeeds |
| Registration failed for unrelated request errors | Explain the failed task: We couldn't load your group. Try again. |
| Publish event | Publish invitation |

Suggested receipt copy:

- Confirmed: Your gift is confirmed. Thank you for celebrating with [name].
- Manual: Your transfer details were sent. Receipt of funds has not been verified.
- Pending: We're checking this payment. Please don't pay again.
- Draft saved: Saved. You can come back whenever you're ready. Only show this after a successful save.

## Responsive and native behaviour

Web design target: comfortable 48 CSS px touch areas; native sizing uses platform units, with 44 pt iOS and 48 dp Android targets. Use keyboard focus, visible labels, properly managed dialog focus, keyboard-operable alternatives and screen-reader status messages. Support reflow at 320 CSS px and large text. Contrast-check the final palette rather than assuming pale colours are accessible.

Keep bottom actions above safe areas and the keyboard. Long checkout forms become focused mobile pages/sheets; desktop can use a side panel. Keep key actions available without hover. Respect Back and Escape, restore scroll position and preserve form values.

Share screen structure, field definitions, feature flags and component tokens with future native clients. Adapt navigation, pickers, sheets, sharing and haptics to each platform instead of reproducing a website sidebar inside a native app. Guest invitation links should remain usable without installing an app.

## Prioritised implementation checklist

### Next sprint: focused guest and host foundations

1. Guest hierarchy: one event hero, one primary action, smaller gift cards, no repeated full metrics, on-demand RSVP/wishes and gated sharing/group entry.
2. Contribution layout: focused mobile checkout, no nested dialogs, explicit method/result states and preservation through app switching. Clarify combined progress versus confirmed funds.
3. Event workspace: one selected event context, reduced event-card actions, contextual navigation and one shared invitation preview presentation.
4. Creation wizard: separate route and conversational steps, prefill approved profile fields, preserve values and validate step by step. Retain the existing complete creation request initially.
5. Shared mobile primitives: spacing/type/button/field/status tokens, sticky action behaviour, focus and keyboard rules. Apply them to the guest and creation work first.

### Following sprint: extend the shared patterns

6. Group creator/member/guest layouts and contextual sign-in/recovery.
7. Gift management, invitation studio, receiving accounts, reports and moderation.
8. Admin privileges/policies and responsive operational reports.
9. Apply the same components to thank-you, notifications and gallery management.

Suggested development handoff units: EventHeader, EventNavigation, GiftCard, ContributionSummary, PaymentStatus, PrimaryActionBar, FormStep, ActivityRow, EmptyState, LoadingState and ConfirmationState. Reuse data-backed labels and states; do not create a different payment-status vocabulary in each role.

## Acceptance checks

- A first-time guest can name the celebration, find its date and start gifting without finding a host login screen.
- All primary controls work at 320, 375/390, 768 and 1280 CSS px, with large text, keyboard navigation and visible focus.
- Disabled modules leave no invitation forms, buttons or empty placeholder sections. Direct APIs stay gated.
- No UUID, database name or infrastructure status appears in the normal guest/host journey.
- Back/navigation restores entered values. Payment app return preserves contribution state. Retrying cannot initiate an unintended duplicate payment.
- Confirmed, reported, pending, cancelled, failed and unavailable are distinguishable by wording, not only colour.
- Each event screen names the current event and has one obvious way back to its overview.
- Creation handles validation, network interruption and incomplete drafts without claiming an unsuccessful save.
- Preview and published guest presentation use the same layout and module rules, with host-only draft data kept private.

Run moderated tasks with representative hosts, older/less technical guests, group creators and members. Track completion, time, backtracking, abandonment and status comprehension against a baseline; do not promise a conversion increase before measuring it.

## Primary design references

- WCAG 2.2: https://www.w3.org/TR/WCAG22/
- WCAG reflow: https://www.w3.org/WAI/WCAG21/Understanding/reflow
- Apple buttons: https://developer.apple.com/design/human-interface-guidelines/buttons
- Apple motion: https://developer.apple.com/design/human-interface-guidelines/motion
- Android touch targets: https://developer.android.com/develop/ui/compose/accessibility/api-defaults
