# Guest module privileges — 27 October 2026

The guest celebration component rendered group gifting and WhatsApp sharing unconditionally. The public invitation API did not include effective module grants, and group API routes did not enforce `GROUP_GIFTING`.

The existing effective permission rule is now used throughout these flows:

`available = platform enabled AND event owner's host grant enabled`

Missing grants default to disabled. The public event response includes a boolean `features` map, and `/api/public/events/{token}/features` provides an uncached refresh of those flags. Only the published event's owner is used to resolve grants; a guest cannot supply a different host ID.

Guest sections (group gifting, WhatsApp sharing, RSVP and guestbook) follow the server flags. Direct group invitation pages follow the same rule. Initial loading and failed privilege requests never enable features. Pages refresh flags on browser focus/visibility changes without clearing active forms during successful background checks.

A central MVC interceptor applies `GROUP_GIFTING` enforcement to `/api/public/groups/**`, including creator, member, guest, payment destination and contact enrollment routes. Existing group sign-out routes remain usable after module revocation and retain CSRF protection. Disabled group routes return 404 before executing the controller, including when a creator is already signed in.

No stored grants, payment balances or migrations were changed. Deploy/restart the backend and frontend changes together, then reload the guest page. Existing open guest tabs recheck privileges when brought back into focus. Already shared WhatsApp links cannot be recalled by disabling the sharing button.

Verification covers the four platform/host combinations, missing grants, host isolation, existing creator sessions, direct group endpoints, uncached public flags, unpublished invitations and logout after revocation. Frontend tests cover explicit grants/denials and fail-closed responses.

Validation passed: 569 backend tests, 8 PostgreSQL privilege integration cases, 336 frontend tests, and the production web build. Browser visual testing and restarting the user's running application were not performed.
