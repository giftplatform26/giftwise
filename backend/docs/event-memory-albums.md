Event memory albums
===================
Each host event has a private Memory album at `/events/{id}/album`, linked from the event sidebar, overview, and guestbook. Existing guest photos and wishes become chronological slideshow moments, including text-only wishes. Playback is opt-in, pauses when the page is hidden, and includes keyboard navigation, fullscreen, reading-aware timings, and a thumbnail strip. Photos retain their full framing. Hidden wishes are excluded unless the host explicitly includes them.

Admin enablement (Flyway V94):
1. Hosts & privileges → Platform-wide modules → Event memory albums.
2. Select the host and enable Event memory albums in Host-specific privileges.
3. Events → enable Memory album for the chosen event.
All three switches are required. Disabling access preserves the underlying memories. Event changes are recorded in the admin audit. This feature starts disabled; no existing host/event receives an automatic grant.

GET `/api/v1/events/{id}/album?limit=60&offset=0&includeHidden=false` returns event title/date, photo/message moments, counts, and the next page. Page sizes are bounded to 200. Photos use `/api/v1/events/{id}/album/{wishId}/photo`. Both routes require the authenticated owner and the effective album grant, including the per-event switch. Responses are not cached. Albums remain readable when guestbook submission is disabled, provided album access is still enabled. Public guestbook moderation and public photo access remain separate.

No new upload or storage pipeline is introduced. Existing guestbook wishes and normalized photos are reused, and private photo paths, guest contact details and contributions are never returned in the album data.
