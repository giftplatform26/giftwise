Guest gifts and delivery addresses
=================================

Frontend wiring is included in the web project for both invitation experiences and both event setup screens. The host contribution report includes a separate published guest gifts section.

Event setup keeps the existing address fields for the venue. Send `deliverySameAsEvent: true` for the checkbox, or `false` with `deliveryAddress: {addressLine1, city, state, postalCode, country}`. Omitting the flag preserves the previous behavior of delivering to the event address. Host event responses include `eventAddress`, `deliveryAddress` and `deliverySameAsEvent` for editing.

Guest gifts use the existing invitation creator registration/login endpoints under `/api/public/groups/{token}`. An individual can use this account without creating a group. Existing CSRF cookies and the page-bound creator session header are required for mutations and private views. Possession of a published invitation token allows viewing its guest gift list, consistent with the existing invitation access model.

* GET `/api/public/groups/{token}/guest-gifts`: invitation gift list; `disabled` and `selection_label` drive unavailable gift cards. Does not disclose account IDs or delivery details.
* POST the same URL with `{name, description, productUrl}` to add a gift. Optional product links must use HTTPS.
* POST `.../guest-gifts/{id}/select` with `{contributionType: "INDIVIDUAL"}` or `{contributionType: "GROUP", groupId}`. Group selection requires the group's creator login. The first selection wins; subsequent attempts return 409.
* GET `.../guest-gifts/mine`: gift management for the logged-in guest. `managed_by_me` distinguishes gifts selected by this account from gifts this account added that someone else selected.
* GET `.../guest-gifts/{id}/delivery-address`: host delivery address, accessible only to the selecting contributor/creator.
* POST `.../guest-gifts/{id}/publish`: selecting contributor/creator publishes the gift to the host report.
* GET `/api/v1/events/{eventId}/contributions/guest-gifts`: owner-only host report section, including individual/group type and group name. Only explicitly published gifts, or gifts selected by an already published group, appear.

Guest gifts are stored separately from wishlist gifts, preventing draft surprises from appearing in host gift management. Gifts remain visible and disabled to other invitation guests after selection. Publication reports the gift commitment; it does not assert payment or delivery. Existing group monetary reports remain the source for contribution amounts.

Apply Flyway V87 before deploying. Existing events default to delivery at the venue. This change does not arrange shipping with a carrier.

Platform control (V88)
----------------------
Admin → Hosts & privileges → Platform-wide modules → Guest-added gifts controls this feature. It defaults to off. Existing host Group gifting grants are copied to Guest-added gifts during migration. Admins can grant or revoke it per host in the same screen. Both Group gifting and Guest-added gifts must be enabled for the host and platform. Guest endpoints enforce both permissions on every request; disabled features are omitted from the invitation UI. Published host reports remain available. Event address setup is independent of this switch.
