# Event account requirements for publishing

An event can be published only when it has an explicit account link that is enabled and preferred. The underlying account must belong to the event owner, be enabled and unarchived, and be host-confirmed or provider-verified. FAILED/REVOKED accounts are ineligible. UPI additionally requires a valid saved QR.

Host-wide accounts and host-wide preferences never make an event ready automatically. In the event's Receiving accounts page, use **Link an existing host account**, then select **Preferred**. Adding a new account directly to the event creates an explicit link with no initial preference.

Links have independent enabled/preferred settings per event. The same host account can be linked to multiple events; selecting or disabling a link in one event does not change another event or the host account. Unlink removes only that event link for a shared host account. Removing an event-only account archives its source row while retaining history. Disabling, archiving, or invalidating the underlying account clears all event preferences for it.

Flyway V73 preserves previously explicit event assignments and their enabled/preferred settings. It does not infer links from host-wide accounts. Restart the backend to apply the migration and the new publishing rule. Already published events are not automatically unpublished by this change.

Guest payment choices and transfer claims use the same enabled/preferred event links. The backend publish endpoint checks this rule even if the frontend is stale or a caller invokes the API directly.
