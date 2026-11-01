# Flexible group collections

A creator can start a general collection or start from a public wishlist gift. Starting from a gift copies its name, description, product link and target from the server; browser-supplied gift details cannot override the wishlist.

`selected_gift_id` records the group's plan. `reserved_gift_id` holds the wishlist item and disables individual and other-group contribution buttons with an anonymous surprise message. A creator must have a verified email before a selection becomes a reservation, including when global email verification is bypassed. Mobile verification alone is insufficient. Completing creator email verification attempts to reserve the selected gift. If another group reserved it first or the item is no longer public/active, verification still succeeds and the creator can retry or choose another gift.

The creator can attach an available gift to a general collection, including after publication. Release clears the selected/reserved gift and copied gift fields, while keeping the group, members, invitations, receiving accounts and transfers. The group continues as a general collection and can choose another gift. Changing a gift never transfers, refunds, confirms or reallocates money.

Host group summaries, transaction details, history, notifications and the main contribution report require `collection_published_at`. Creating a group, verifying contact or reserving an item does not publish it. A published group remains published when its gift changes. Invite links can be shared and members/guests can contribute before publication, subject to existing readiness and payment settings.

The main host report includes published group guest/creator/member transfers and legacy member reports. These are reported transfers, not proof of receipt at the host. They stay pending in financial reports and do not credit verified gift or wallet balances. Each new transfer stores a gift-ID snapshot; changing or releasing a group's gift does not rewrite past transfer purpose, amount, reference or contributor.

Migration V86 adds append-only database activity for group setup/settings/publication, gift selection/reservation/release, membership changes, receiving accounts, invitations, creator-account updates and transfer reports/status changes. History is available to the creator and to the owning host after publication. Activity lists are paginated; credentials and payment ciphertext are excluded. Existing groups get a history-started snapshot, not invented historical actions. Existing transaction records are retained and their initial gift snapshots use the group's gift at migration time.

Existing unverified reservations become pending selections during migration; the wishlist item becomes available until the creator verifies email. Migration V86 runs through Flyway on normal backend startup. Deploy the backend and web changes together.

## Validation

`GroupGiftLifecycleIntegrationTest` uses an isolated PostgreSQL database to cover selection versus reservation, trusted wishlist metadata at setup, email-code activation, cross-event/creator access, concurrent competing reservations, draft report privacy, publication, release with preserved transaction snapshots, and member/activity history. `GroupCollectionDatabaseIntegrationTest` checks invitation readiness independently of publication and idempotent host notifications.
