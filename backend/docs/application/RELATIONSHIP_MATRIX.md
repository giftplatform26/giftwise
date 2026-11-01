# Database relationship matrix

Every row is a foreign-key column. Composite FKs contribute multiple rows. Deletion/update behavior is the exact SQL definition.

| Constraint | From table.column | To table.column | Definition |
| --- | --- | --- | --- |
| account_pending_email_verifications_challenge_id_fkey | account_pending_email_verifications.challenge_id | account_contact_challenges.id | `FOREIGN KEY (challenge_id) REFERENCES account_contact_challenges(id) ON DELETE CASCADE` |
| account_pending_mobile_verifications_challenge_id_fkey | account_pending_mobile_verifications.challenge_id | account_contact_challenges.id | `FOREIGN KEY (challenge_id) REFERENCES account_contact_challenges(id) ON DELETE CASCADE` |
| account_registration_policies_changed_by_fkey | account_registration_policies.changed_by | users.id | `FOREIGN KEY (changed_by) REFERENCES users(id)` |
| affiliate_clicks_event_id_fkey | affiliate_clicks.event_id | events.id | `FOREIGN KEY (event_id) REFERENCES events(id)` |
| affiliate_clicks_gift_id_fkey | affiliate_clicks.gift_id | gifts.id | `FOREIGN KEY (gift_id) REFERENCES gifts(id)` |
| affiliate_clicks_partner_id_fkey | affiliate_clicks.partner_id | affiliate_partners.id | `FOREIGN KEY (partner_id) REFERENCES affiliate_partners(id)` |
| affiliate_commissions_click_id_fkey | affiliate_commissions.click_id | affiliate_clicks.id | `FOREIGN KEY (click_id) REFERENCES affiliate_clicks(id)` |
| affiliate_commissions_partner_id_fkey | affiliate_commissions.partner_id | affiliate_partners.id | `FOREIGN KEY (partner_id) REFERENCES affiliate_partners(id)` |
| commercial_entitlements_order_id_fkey | commercial_entitlements.order_id | commercial_orders.id | `FOREIGN KEY (order_id) REFERENCES commercial_orders(id)` |
| commercial_orders_pricing_revision_fkey | commercial_orders.pricing_revision | platform_monetization_history.revision | `FOREIGN KEY (pricing_revision) REFERENCES platform_monetization_history(revision)` |
| commercial_placement_clicks_placement_id_fkey | commercial_placement_clicks.placement_id | commercial_placements.id | `FOREIGN KEY (placement_id) REFERENCES commercial_placements(id)` |
| commercial_placements_owner_id_fkey | commercial_placements.owner_id | users.id | `FOREIGN KEY (owner_id) REFERENCES users(id)` |
| commercial_revenue_order_id_fkey | commercial_revenue.order_id | commercial_orders.id | `FOREIGN KEY (order_id) REFERENCES commercial_orders(id)` |
| commercial_setup_history_actor_id_fkey | commercial_setup_history.actor_id | users.id | `FOREIGN KEY (actor_id) REFERENCES users(id)` |
| contribution_allocation_postings_contribution_id_fkey | contribution_allocation_postings.contribution_id | contributions.id | `FOREIGN KEY (contribution_id) REFERENCES contributions(id)` |
| contribution_allocations_contribution_id_fkey | contribution_allocations.contribution_id | contributions.id | `FOREIGN KEY (contribution_id) REFERENCES contributions(id)` |
| contribution_allocations_event_id_fkey | contribution_allocations.event_id | events.id | `FOREIGN KEY (event_id) REFERENCES events(id)` |
| fk_allocation_contribution_event | contribution_allocations.contribution_id | contributions.id | `FOREIGN KEY (contribution_id, event_id) REFERENCES contributions(id, event_id)` |
| fk_allocation_contribution_event | contribution_allocations.event_id | contributions.event_id | `FOREIGN KEY (contribution_id, event_id) REFERENCES contributions(id, event_id)` |
| fk_allocation_gift_event | contribution_allocations.gift_id | gifts.id | `FOREIGN KEY (gift_id, event_id) REFERENCES gifts(id, event_id)` |
| fk_allocation_gift_event | contribution_allocations.event_id | gifts.event_id | `FOREIGN KEY (gift_id, event_id) REFERENCES gifts(id, event_id)` |
| contributions_event_id_fkey | contributions.event_id | events.id | `FOREIGN KEY (event_id) REFERENCES events(id)` |
| contributions_gift_id_fkey | contributions.gift_id | gifts.id | `FOREIGN KEY (gift_id) REFERENCES gifts(id)` |
| contributions_payment_order_id_fkey | contributions.payment_order_id | payment_orders.id | `FOREIGN KEY (payment_order_id) REFERENCES payment_orders(id)` |
| fk_contribution_gift_event | contributions.gift_id | gifts.id | `FOREIGN KEY (gift_id, event_id) REFERENCES gifts(id, event_id)` |
| fk_contribution_gift_event | contributions.event_id | gifts.event_id | `FOREIGN KEY (gift_id, event_id) REFERENCES gifts(id, event_id)` |
| event_guest_submission_limits_event_id_fkey | event_guest_submission_limits.event_id | events.id | `FOREIGN KEY (event_id) REFERENCES events(id)` |
| event_guestbook_event_id_fkey | event_guestbook.event_id | events.id | `FOREIGN KEY (event_id) REFERENCES events(id)` |
| event_payment_account_links_bank_account_id_host_id_fkey | event_payment_account_links.bank_account_id | host_bank_accounts.id | `FOREIGN KEY (bank_account_id, host_id) REFERENCES host_bank_accounts(id, host_id)` |
| event_payment_account_links_bank_account_id_host_id_fkey | event_payment_account_links.host_id | host_bank_accounts.host_id | `FOREIGN KEY (bank_account_id, host_id) REFERENCES host_bank_accounts(id, host_id)` |
| event_payment_account_links_event_id_host_id_fkey | event_payment_account_links.event_id | events.id | `FOREIGN KEY (event_id, host_id) REFERENCES events(id, owner_id)` |
| event_payment_account_links_event_id_host_id_fkey | event_payment_account_links.host_id | events.owner_id | `FOREIGN KEY (event_id, host_id) REFERENCES events(id, owner_id)` |
| event_payment_account_links_upi_account_id_host_id_fkey | event_payment_account_links.upi_account_id | host_upi_accounts.id | `FOREIGN KEY (upi_account_id, host_id) REFERENCES host_upi_accounts(id, host_id)` |
| event_payment_account_links_upi_account_id_host_id_fkey | event_payment_account_links.host_id | host_upi_accounts.host_id | `FOREIGN KEY (upi_account_id, host_id) REFERENCES host_upi_accounts(id, host_id)` |
| event_rsvps_event_id_fkey | event_rsvps.event_id | events.id | `FOREIGN KEY (event_id) REFERENCES events(id)` |
| event_thank_you_settings_event_id_fkey | event_thank_you_settings.event_id | events.id | `FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE` |
| fk_events_owner | events.owner_id | users.id | `FOREIGN KEY (owner_id) REFERENCES users(id)` |
| gift_group_accounts_event_id_fkey | gift_group_accounts.event_id | events.id | `FOREIGN KEY (event_id) REFERENCES events(id)` |
| gift_group_activity_group_id_fkey | gift_group_activity.group_id | gift_groups.id | `FOREIGN KEY (group_id) REFERENCES gift_groups(id)` |
| gift_group_destination_verification_attempt_destination_id_fkey | gift_group_destination_verification_attempts.destination_id | gift_group_payment_destinations.id | `FOREIGN KEY (destination_id) REFERENCES gift_group_payment_destinations(id) ON DELETE CASCADE` |
| gift_group_destination_verification_attempts_group_id_fkey | gift_group_destination_verification_attempts.group_id | gift_groups.id | `FOREIGN KEY (group_id) REFERENCES gift_groups(id) ON DELETE CASCADE` |
| gift_group_destination_verification_attempts_requested_by_fkey | gift_group_destination_verification_attempts.requested_by | users.id | `FOREIGN KEY (requested_by) REFERENCES users(id)` |
| fk_group_report_creator | gift_group_guest_reports.group_id | gift_groups.id | `FOREIGN KEY (group_id, creator_admin_id) REFERENCES gift_groups(id, admin_id)` |
| fk_group_report_creator | gift_group_guest_reports.creator_admin_id | gift_groups.admin_id | `FOREIGN KEY (group_id, creator_admin_id) REFERENCES gift_groups(id, admin_id)` |
| gift_group_guest_reports_creator_admin_id_fkey | gift_group_guest_reports.creator_admin_id | gift_group_accounts.id | `FOREIGN KEY (creator_admin_id) REFERENCES gift_group_accounts(id)` |
| gift_group_guest_reports_destination_id_fkey | gift_group_guest_reports.destination_id | gift_group_payment_destinations.id | `FOREIGN KEY (destination_id) REFERENCES gift_group_payment_destinations(id)` |
| gift_group_guest_reports_gift_id_snapshot_fkey | gift_group_guest_reports.gift_id_snapshot | gifts.id | `FOREIGN KEY (gift_id_snapshot) REFERENCES gifts(id)` |
| gift_group_guest_reports_group_id_fkey | gift_group_guest_reports.group_id | gift_groups.id | `FOREIGN KEY (group_id) REFERENCES gift_groups(id)` |
| gift_group_guest_reports_member_id_fkey | gift_group_guest_reports.member_id | gift_group_members.id | `FOREIGN KEY (member_id) REFERENCES gift_group_members(id)` |
| gift_group_invitations_group_id_fkey | gift_group_invitations.group_id | gift_groups.id | `FOREIGN KEY (group_id) REFERENCES gift_groups(id)` |
| gift_group_members_group_id_fkey | gift_group_members.group_id | gift_groups.id | `FOREIGN KEY (group_id) REFERENCES gift_groups(id)` |
| gift_group_payment_audit_group_id_fkey | gift_group_payment_audit.group_id | gift_groups.id | `FOREIGN KEY (group_id) REFERENCES gift_groups(id)` |
| gift_group_payment_destinations_group_id_fkey | gift_group_payment_destinations.group_id | gift_groups.id | `FOREIGN KEY (group_id) REFERENCES gift_groups(id) ON DELETE CASCADE` |
| gift_group_payment_permissions_changed_by_fkey | gift_group_payment_permissions.changed_by | users.id | `FOREIGN KEY (changed_by) REFERENCES users(id)` |
| gift_group_payment_permissions_group_id_fkey | gift_group_payment_permissions.group_id | gift_groups.id | `FOREIGN KEY (group_id) REFERENCES gift_groups(id) ON DELETE CASCADE` |
| gift_group_payment_reports_gift_id_snapshot_fkey | gift_group_payment_reports.gift_id_snapshot | gifts.id | `FOREIGN KEY (gift_id_snapshot) REFERENCES gifts(id)` |
| gift_group_payment_reports_group_id_fkey | gift_group_payment_reports.group_id | gift_groups.id | `FOREIGN KEY (group_id) REFERENCES gift_groups(id)` |
| gift_group_payment_reports_member_id_fkey | gift_group_payment_reports.member_id | gift_group_members.id | `FOREIGN KEY (member_id) REFERENCES gift_group_members(id)` |
| fk_reserved_group_gift | gift_groups.reserved_gift_id | gifts.id | `FOREIGN KEY (reserved_gift_id, event_id) REFERENCES gifts(id, event_id)` |
| fk_reserved_group_gift | gift_groups.event_id | gifts.event_id | `FOREIGN KEY (reserved_gift_id, event_id) REFERENCES gifts(id, event_id)` |
| fk_selected_group_gift | gift_groups.selected_gift_id | gifts.id | `FOREIGN KEY (selected_gift_id, event_id) REFERENCES gifts(id, event_id)` |
| fk_selected_group_gift | gift_groups.event_id | gifts.event_id | `FOREIGN KEY (selected_gift_id, event_id) REFERENCES gifts(id, event_id)` |
| gift_groups_admin_id_fkey | gift_groups.admin_id | gift_group_accounts.id | `FOREIGN KEY (admin_id) REFERENCES gift_group_accounts(id)` |
| gift_groups_event_id_fkey | gift_groups.event_id | events.id | `FOREIGN KEY (event_id) REFERENCES events(id)` |
| gift_groups_guest_bank_destination_id_fkey | gift_groups.guest_bank_destination_id | gift_group_payment_destinations.id | `FOREIGN KEY (guest_bank_destination_id) REFERENCES gift_group_payment_destinations(id)` |
| gift_groups_guest_upi_destination_id_fkey | gift_groups.guest_upi_destination_id | gift_group_payment_destinations.id | `FOREIGN KEY (guest_upi_destination_id) REFERENCES gift_group_payment_destinations(id)` |
| gift_photos_actor_id_fkey | gift_photos.actor_id | users.id | `FOREIGN KEY (actor_id) REFERENCES users(id)` |
| gift_photos_event_id_fkey | gift_photos.event_id | events.id | `FOREIGN KEY (event_id) REFERENCES events(id)` |
| gift_retailer_metadata_audit_actor_id_fkey | gift_retailer_metadata_audit.actor_id | users.id | `FOREIGN KEY (actor_id) REFERENCES users(id)` |
| gift_retailer_metadata_audit_event_id_fkey | gift_retailer_metadata_audit.event_id | events.id | `FOREIGN KEY (event_id) REFERENCES events(id)` |
| gift_retailer_metadata_audit_gift_id_fkey | gift_retailer_metadata_audit.gift_id | gifts.id | `FOREIGN KEY (gift_id) REFERENCES gifts(id)` |
| gifts_event_id_fkey | gifts.event_id | events.id | `FOREIGN KEY (event_id) REFERENCES events(id)` |
| group_collection_notifications_event_id_fkey | group_collection_notifications.event_id | events.id | `FOREIGN KEY (event_id) REFERENCES events(id)` |
| group_collection_notifications_group_id_fkey | group_collection_notifications.group_id | gift_groups.id | `FOREIGN KEY (group_id) REFERENCES gift_groups(id)` |
| group_member_email_policies_host_id_fkey | group_member_email_policies.host_id | users.id | `FOREIGN KEY (host_id) REFERENCES users(id)` |
| guest_direct_transfer_claims_event_id_fkey | guest_direct_transfer_claims.event_id | events.id | `FOREIGN KEY (event_id) REFERENCES events(id)` |
| guest_direct_transfer_claims_gift_id_fkey | guest_direct_transfer_claims.gift_id | gifts.id | `FOREIGN KEY (gift_id) REFERENCES gifts(id)` |
| guest_gift_host_contributions_claim_id_fkey | guest_gift_host_contributions.claim_id | guest_direct_transfer_claims.id | `FOREIGN KEY (claim_id) REFERENCES guest_direct_transfer_claims(id)` |
| guest_gift_host_contributions_guest_gift_id_fkey | guest_gift_host_contributions.guest_gift_id | guest_gifts.id | `FOREIGN KEY (guest_gift_id) REFERENCES guest_gifts(id)` |
| guest_gifts_added_by_fkey | guest_gifts.added_by | gift_group_accounts.id | `FOREIGN KEY (added_by) REFERENCES gift_group_accounts(id)` |
| guest_gifts_event_id_fkey | guest_gifts.event_id | events.id | `FOREIGN KEY (event_id) REFERENCES events(id)` |
| guest_gifts_group_id_fkey | guest_gifts.group_id | gift_groups.id | `FOREIGN KEY (group_id) REFERENCES gift_groups(id)` |
| guest_gifts_selected_by_fkey | guest_gifts.selected_by | gift_group_accounts.id | `FOREIGN KEY (selected_by) REFERENCES gift_group_accounts(id)` |
| guest_manual_display_allocations_claim_id_fkey | guest_manual_display_allocations.claim_id | guest_direct_transfer_claims.id | `FOREIGN KEY (claim_id) REFERENCES guest_direct_transfer_claims(id)` |
| guest_manual_display_allocations_event_id_fkey | guest_manual_display_allocations.event_id | events.id | `FOREIGN KEY (event_id) REFERENCES events(id)` |
| guest_manual_display_allocations_gift_id_fkey | guest_manual_display_allocations.gift_id | gifts.id | `FOREIGN KEY (gift_id) REFERENCES gifts(id)` |
| host_account_verification_checks_host_id_fkey | host_account_verification_checks.host_id | users.id | `FOREIGN KEY (host_id) REFERENCES users(id)` |
| fk_bank_event_owner | host_bank_accounts.event_id | events.id | `FOREIGN KEY (event_id, host_id) REFERENCES events(id, owner_id)` |
| fk_bank_event_owner | host_bank_accounts.host_id | events.owner_id | `FOREIGN KEY (event_id, host_id) REFERENCES events(id, owner_id)` |
| host_bank_accounts_host_id_fkey | host_bank_accounts.host_id | users.id | `FOREIGN KEY (host_id) REFERENCES users(id)` |
| host_destination_verification_attempts_host_id_fkey | host_destination_verification_attempts.host_id | users.id | `FOREIGN KEY (host_id) REFERENCES users(id)` |
| host_payout_requests_destination_id_fkey | host_payout_requests.destination_id | host_bank_accounts.id | `FOREIGN KEY (destination_id) REFERENCES host_bank_accounts(id)` |
| host_payout_requests_event_id_fkey | host_payout_requests.event_id | events.id | `FOREIGN KEY (event_id) REFERENCES events(id)` |
| host_payout_requests_host_id_fkey | host_payout_requests.host_id | users.id | `FOREIGN KEY (host_id) REFERENCES users(id)` |
| fk_upi_event_owner | host_upi_accounts.event_id | events.id | `FOREIGN KEY (event_id, host_id) REFERENCES events(id, owner_id)` |
| fk_upi_event_owner | host_upi_accounts.host_id | events.owner_id | `FOREIGN KEY (event_id, host_id) REFERENCES events(id, owner_id)` |
| host_upi_accounts_host_id_fkey | host_upi_accounts.host_id | users.id | `FOREIGN KEY (host_id) REFERENCES users(id)` |
| host_upi_qr_assets_host_id_fkey | host_upi_qr_assets.host_id | users.id | `FOREIGN KEY (host_id) REFERENCES users(id)` |
| host_upi_qr_assets_upi_account_id_fkey | host_upi_qr_assets.upi_account_id | host_upi_accounts.id | `FOREIGN KEY (upi_account_id) REFERENCES host_upi_accounts(id)` |
| host_upi_qr_images_host_id_fkey | host_upi_qr_images.host_id | users.id | `FOREIGN KEY (host_id) REFERENCES users(id)` |
| host_upi_qr_images_upi_account_id_fkey | host_upi_qr_images.upi_account_id | host_upi_accounts.id | `FOREIGN KEY (upi_account_id) REFERENCES host_upi_accounts(id)` |
| hybrid_checkout_quotes_event_id_fkey | hybrid_checkout_quotes.event_id | events.id | `FOREIGN KEY (event_id) REFERENCES events(id)` |
| hybrid_checkout_quotes_gift_id_fkey | hybrid_checkout_quotes.gift_id | gifts.id | `FOREIGN KEY (gift_id) REFERENCES gifts(id)` |
| invitation_ai_requests_event_id_fkey | invitation_ai_requests.event_id | events.id | `FOREIGN KEY (event_id) REFERENCES events(id)` |
| invitation_ai_requests_host_id_fkey | invitation_ai_requests.host_id | users.id | `FOREIGN KEY (host_id) REFERENCES users(id)` |
| notification_outbox_event_id_fkey | notification_outbox.event_id | events.id | `FOREIGN KEY (event_id) REFERENCES events(id)` |
| payment_channel_controls_changed_by_fkey | payment_channel_controls.changed_by | users.id | `FOREIGN KEY (changed_by) REFERENCES users(id)` |
| payment_control_history_actor_id_fkey | payment_control_history.actor_id | users.id | `FOREIGN KEY (actor_id) REFERENCES users(id)` |
| payment_notifications_contribution_id_fkey | payment_notifications.contribution_id | contributions.id | `FOREIGN KEY (contribution_id) REFERENCES contributions(id)` |
| payment_notifications_event_id_fkey | payment_notifications.event_id | events.id | `FOREIGN KEY (event_id) REFERENCES events(id)` |
| fk_payment_order_gift_event | payment_orders.gift_id | gifts.id | `FOREIGN KEY (gift_id, event_id) REFERENCES gifts(id, event_id)` |
| fk_payment_order_gift_event | payment_orders.event_id | gifts.event_id | `FOREIGN KEY (gift_id, event_id) REFERENCES gifts(id, event_id)` |
| payment_orders_event_id_fkey | payment_orders.event_id | events.id | `FOREIGN KEY (event_id) REFERENCES events(id)` |
| payment_orders_gift_id_fkey | payment_orders.gift_id | gifts.id | `FOREIGN KEY (gift_id) REFERENCES gifts(id)` |
| payment_orders_monetization_revision_fkey | payment_orders.monetization_revision | platform_monetization_history.revision | `FOREIGN KEY (monetization_revision) REFERENCES platform_monetization_history(revision)` |
| payment_transactions_payment_order_id_fkey | payment_transactions.payment_order_id | payment_orders.id | `FOREIGN KEY (payment_order_id) REFERENCES payment_orders(id)` |
| payout_reconciliation_observations_payout_request_id_fkey | payout_reconciliation_observations.payout_request_id | host_payout_requests.id | `FOREIGN KEY (payout_request_id) REFERENCES host_payout_requests(id)` |
| payout_reservations_event_id_fkey | payout_reservations.event_id | events.id | `FOREIGN KEY (event_id) REFERENCES events(id)` |
| payout_reservations_payout_request_id_fkey | payout_reservations.payout_request_id | host_payout_requests.id | `FOREIGN KEY (payout_request_id) REFERENCES host_payout_requests(id)` |
| phone_otp_challenges_user_id_fkey | phone_otp_challenges.user_id | users.id | `FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE` |
| platform_admin_action_audit_actor_id_fkey | platform_admin_action_audit.actor_id | users.id | `FOREIGN KEY (actor_id) REFERENCES users(id)` |
| platform_event_ai_design_settings_changed_by_fkey | platform_event_ai_design_settings.changed_by | users.id | `FOREIGN KEY (changed_by) REFERENCES users(id)` |
| platform_event_ai_design_settings_event_id_fkey | platform_event_ai_design_settings.event_id | events.id | `FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE` |
| platform_event_album_settings_changed_by_fkey | platform_event_album_settings.changed_by | users.id | `FOREIGN KEY (changed_by) REFERENCES users(id)` |
| platform_event_album_settings_event_id_fkey | platform_event_album_settings.event_id | events.id | `FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE` |
| platform_event_message_settings_event_id_fkey | platform_event_message_settings.event_id | events.id | `FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE` |
| platform_experience_settings_changed_by_fkey | platform_experience_settings.changed_by | users.id | `FOREIGN KEY (changed_by) REFERENCES users(id)` |
| platform_host_module_grants_changed_by_fkey | platform_host_module_grants.changed_by | users.id | `FOREIGN KEY (changed_by) REFERENCES users(id)` |
| platform_host_module_grants_host_id_fkey | platform_host_module_grants.host_id | users.id | `FOREIGN KEY (host_id) REFERENCES users(id) ON DELETE CASCADE` |
| platform_host_module_grants_module_code_fkey | platform_host_module_grants.module_code | platform_module_controls.module_code | `FOREIGN KEY (module_code) REFERENCES platform_module_controls(module_code)` |
| platform_module_controls_changed_by_fkey | platform_module_controls.changed_by | users.id | `FOREIGN KEY (changed_by) REFERENCES users(id)` |
| platform_monetization_history_actor_id_fkey | platform_monetization_history.actor_id | users.id | `FOREIGN KEY (actor_id) REFERENCES users(id)` |
| platform_monetization_settings_changed_by_fkey | platform_monetization_settings.changed_by | users.id | `FOREIGN KEY (changed_by) REFERENCES users(id)` |
| platform_verification_settings_changed_by_fkey | platform_verification_settings.changed_by | users.id | `FOREIGN KEY (changed_by) REFERENCES users(id)` |
| qr_invitation_designs_event_id_fkey | qr_invitation_designs.event_id | events.id | `FOREIGN KEY (event_id) REFERENCES events(id)` |
| routing_commission_history_actor_id_fkey | routing_commission_history.actor_id | users.id | `FOREIGN KEY (actor_id) REFERENCES users(id)` |
| routing_commission_settings_changed_by_fkey | routing_commission_settings.changed_by | users.id | `FOREIGN KEY (changed_by) REFERENCES users(id)` |
| wallet_ledger_entries_contribution_id_fkey | wallet_ledger_entries.contribution_id | contributions.id | `FOREIGN KEY (contribution_id) REFERENCES contributions(id)` |
| wallet_ledger_entries_event_id_fkey | wallet_ledger_entries.event_id | events.id | `FOREIGN KEY (event_id) REFERENCES events(id)` |
