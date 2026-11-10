# GiftWise growth and performance release

This release improves how prospective hosts discover GiftWise, how hosts create invitations, and how guests view them. It extends existing RSVP, calendar, invitation design, thank-you, event reuse and analytics workflows instead of creating parallel permission systems. Migration V107 is additive. Live-money payments remain outside the production-ready scope described by the deployment runbook.

## Delivered experiences

| Area | Implementation and placement |
| --- | --- |
| Public home | `/` now presents a branded introduction, three-step explanation and sign-up CTA. The authenticated invitation designer moves to `/studio`; existing event-specific studios remain at `/events/[id]/qr`. |
| Demonstration | `/demo` offers five fictional occasion previews and local-only response selection. It creates no event, guest response or payment. It is clearly labelled as a sample. |
| Guided creation | `/events/new` works in both Classic and Focused views when enabled. Five steps, progress, back navigation, scoped browser-tab draft storage, debounced autosave and duplicate-submit protection precede private draft creation. Publishing remains a separate owner action with existing server readiness checks. |
| Occasion starters | Nine occasion-specific titles, editable notes, themes and existing illustrations. Changing occasion replaces prior suggested wording but preserves host-written content. The design studio retains its existing free/premium template permissions. |
| Mobile invitation | Classic hosts can use the polished mobile invitation layout without switching the entire platform to Focused view. Branded footer, accessible checkout, large actions and reduced-motion support remain. |
| Sharing | Existing WhatsApp, copy, native sharing and QR features remain under module/host permissions. Rich Open Graph/Twitter previews add a separate platform switch and explicit per-event host consent. Preview data is limited to event title, occasion and date. |
| Images and startup | Guest gift photos use reusable 480-pixel JPEG thumbnails; existing photos are backfilled on authorized first access. Fixed image dimensions, async decoding and lazy loading reduce unnecessary rendering/network work. The public home does not import the full designer. AI/template tools use separate dynamic chunks. Invitation-only events skip contribution-progress calls. |
| Dashboard | Owner-scoped `/api/v1/events/workspace-summary` returns at most six relevant events with next-step links and RSVP counts only when the host has RSVP access. Existing full dashboard reports remain. |
| Guest convenience | Existing anonymous RSVP, calendar and host-approved directions are retained. Calendar failures now show feedback on the invitation instead of navigating guests to an API error page. |
| Completion | Existing thank-you cards, private memory albums and permission-controlled repeat drafts remain. A new optional `/memories/[token]` page shares only approved guestbook names/messages, with pagination and explicit host consent. It never exposes private album photos, RSVP contacts or payment information. |
| Discovery | Optional discreet create-your-own invitation link on guest pages and keepsakes. It does not enroll the guest or automatically send invitations. |
| Measurement | Admin analytics adds last-30-day journey stage counts for registrations, drafts, publishing, share handoffs, invitation opens and RSVPs. Collection requires visitor consent and the master switch. Stage counts are not an ordered conversion funnel or unique-person counts. |

## Admin configuration

Navigate to **Admin → Branding & appearance → Experience and growth controls**. Save all switches together. The server requires an authenticated platform administrator, CSRF token, a complete known-key payload and the current version. Concurrent stale saves return HTTP 409. Changes are audited as `GROWTH_SETTINGS_UPDATED`; previews and keepsake consent changes have their own audit entries. Other open tabs refresh settings on focus, visibility changes or a broadcast from the saving tab. Reload is sufficient across browsers.

| Switch | New database default | Behaviour when disabled |
| --- | --- | --- |
| `GUIDED_CREATION` | On | `/events/new` returns hosts to the existing classic event form. Existing events/drafts are not deleted. |
| `OCCASION_STARTERS` | On | Occasion changes do not insert suggested titles, descriptions or themes. |
| `SMART_DASHBOARD` | On | Extra dashboard insights are hidden and the summary API returns 404. Existing dashboard remains. |
| `POLISHED_GUEST_VIEW` | On | Classic view uses its classic invitation. Focused view retains its pre-existing focused invitation. |
| `PUBLIC_DEMO` | On | Demo link disappears and the direct demo route shows unavailable. |
| `SHARE_PREVIEWS` | Off | Preview metadata/image endpoints return 404; invitation links still work. |
| `PUBLIC_KEEPSAKES` | Off | Public keepsake endpoint returns 404, without deleting messages or host consent. |
| `GUEST_DISCOVERY` | Off | No acquisition CTA is shown on guest pages. |
| `INTERACTION_ANALYTICS` | On | Browser tracking is uninstalled and server-side interaction writes stop. Historical reports remain. Visitor consent is still required when enabled. |

RSVP, guestbook, WhatsApp/social sharing, event reuse, albums, premium templates, email policies and payments retain their existing controls. Global growth switches do not grant those modules. Performance, accessibility, authentication, consent and ownership protections are not optional switches.

## Host and guest procedures

1. **Create:** Sign in → Celebrations → guided creation → choose occasion → complete details/address → style → review → create private draft. The five steps create a draft, not a published invitation. Finish gifts or invitation-only settings, then use the existing publish action.
2. **Share a preview:** Admin enables rich sharing previews. Host opens the event's Design & QR screen and explicitly checks “Allow a rich preview for this invitation”. Publish the event. Copy or share its existing link. Third-party services may cache already-fetched previews; later disablement stops new server responses but cannot retract those caches.
3. **Guest convenience:** Open the published link. Only enabled modules appear. RSVP requires no host registration. Venue/directions are shown only after the host opts to share them. Calendar download failure leaves the invitation open with a retryable message.
4. **Completion:** Use thank-you cards and the existing album/repeat features as permitted. For public wishes, admin enables Public keepsakes and the host's Guestbook module. Host opens Thank-you cards and explicitly enables keepsake sharing for a published/completed event, then copies the keepsake link. Host can revoke it immediately. Making an event private or cancelling it also makes the public keepsake unavailable.
5. **Measure:** Admin → Clicks & outcomes shows journey activity and existing detailed reporting. Compare stage totals as diagnostics, respecting the documented consent/session limitations. Do not represent WhatsApp/native-share handoffs as delivered invitations.

## API contracts

All API paths below are backend paths; browser requests add `/backend`. Private responses use `Cache-Control: no-store`. Existing session, role and CSRF rules apply.

| Method and path | Input or response | Access and guards |
| --- | --- | --- |
| `GET /api/public/growth-settings` | `{features: {CODE: boolean}, version: number}` | Public, no-store; contains no secrets. |
| `PUT /api/admin/console/growth-settings` | Complete features map and current version; returns saved settings | Platform admin + CSRF; 400 unknown/missing/null flags; 409 stale version. |
| `GET /api/v1/events/workspace-summary` | At most six event IDs/titles/status/dates, gift counts, permitted RSVP totals | Authenticated owner scope; 404 if insights disabled. No guest contact data. |
| `GET /api/v1/events/{id}/share-preview` | `{enabled, platformEnabled}` | Owner only. |
| `PUT /api/v1/events/{id}/share-preview` | `{enabled: boolean}` | Owner + CSRF; enabling requires platform switch. |
| `GET /api/public/events/{token}/share-preview` | `{title, occasion, date}` | Published event, host consent and global switch; otherwise 404. |
| `GET /api/public/events/{token}/share-preview.png` | 1200 × 630 PNG | Same gates. No contact/venue/gift data. |
| `GET /api/v1/events/{id}/keepsake-sharing` | `{enabled, platformEnabled, guestbookEnabled, token, status}` | Owner only. |
| `PUT /api/v1/events/{id}/keepsake-sharing` | `{enabled: boolean}` | Owner + CSRF. Enabling requires published/completed event, global switch and Guestbook access. Revocation remains available when a gate is off. |
| `GET /api/public/keepsakes/{token}?page=0` | `{title, date, wishes: [{id,guest_name,message}], page, hasMore}` | Explicit host consent, global switch, Guestbook grant, published/completed state. Twenty approved wishes per page; 400 invalid page. |
| `GET /api/public/events/{token}/gifts/{giftId}/photo?thumbnail=true` | Fixed 480-pixel JPEG | Published gifting-enabled event, ACTIVE/PUBLIC gift and matching photo/event relation. No arbitrary resize or remote URL fetch. |
| `GET /api/admin/console/interactions/funnel` | Rows `{action, visitors, occurrences}` for last 30 days | Platform admin only; consented successful server activity and browser share handoffs. |

Example settings save uses exactly the nine codes in the table. Fetch the current revision before saving; do not fabricate it. Inputs are validated server-side independently of checkboxes.

## Data model and compatibility

V107 creates `platform_growth_settings` (singleton revision/audit metadata) and `platform_growth_features` (known code/boolean rows), adds `events.share_preview_enabled` and `events.keepsake_enabled` defaulting false, adds nullable `gift_photos.thumbnail`, and indexes relevant interaction stages. No development users, credentials, event data or provider settings are imported. Existing passwords and usernames are unchanged.

The preview renderer uses Java headless AWT/ImageIO and installed fonts. It embeds no remote merchant images or private photos. The web metadata handler is server-only and fetches from a configured backend origin, never a browser-supplied host header. Invitation and keepsake routes remain noindex; the public home is indexable. Robots exclusions remain in place for private routes.

## Deployment and operations

For rich metadata set the Node server's runtime environment:

```sh
GIFTWISE_PUBLIC_ORIGIN=https://YOUR_REAL_HOSTNAME
GIFTWISE_BACKEND_ORIGIN=http://127.0.0.1:8080
```

The first must be a canonical HTTPS origin without credentials, path, query or fragment. No value means generic invitation metadata, not a broken invitation. The second is a trusted operator setting, not a public Next.js variable. The deployment kit writes `/etc/giftwise/web.env`, adds it to the web systemd service and includes it in encrypted configuration backups. Existing servers should create/review this file, install the updated service, run `sudo systemctl daemon-reload` and restart the web service within the normal reviewed deployment procedure. Never regenerate backend secrets or reset the database for this update.

Enable previews/keepsakes only after privacy review and staging acceptance. Disabling them does not erase host preferences or approved messages. Stop public access when investigating failed migrations. Existing Flyway history must not be edited. The additional features do not enable live payment collection or alter provider restrictions.

## Verification and practical limits

See the current validation record appended below. Automated tests cover new gates, ownership, CSRF, stale saves, migration startup, server analytics disablement, thumbnail authorization and approved-only keepsakes. Browser/device acceptance, real messaging-service preview refresh and production performance measurements remain deployment checks.

No throughput, search ranking, acquisition improvement or Core Web Vitals result is claimed from these code changes. Existing full gift/event lists remain compatible; a later measured scaling release can add cursor pagination to those existing contracts. Do not cache invitation bodies, keepsakes, auth responses or gift photos in a shared CDN; revocation must continue to take effect. Existing business audit logs remain independent of optional interaction analytics.

### Validation record on 6 November 2026

- Backend compile and complete unit/HTTP suite: 680 tests passed with no failures, plus the isolated documentation-export test.
- New PostgreSQL growth integration suite: 10 tests passed, including migration V107, role/ownership/CSRF/revision guards, thumbnail authorization, analytics disablement and approved-only keepsakes.
- Frontend: 428 tests passed; production build and TypeScript checks passed.
- Schema/API export: fresh disposable PostgreSQL 17, head V107; documentation structural validator passed with 6,879 checks and no errors before archive cleanup. Revalidate after moving historical files.
- Browser access was unavailable in the session. Visual/device acceptance, real social-preview delivery, production load measurements and external provider verification remain staging/deployment checks.
- No production database or live provider transaction was used. Existing deployment upgrades have a companion [step-by-step supplement](../../production-deployment/Growth_Upgrade_Steps.md).
