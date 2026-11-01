# Database catalogue

Fresh PostgreSQL migration snapshot: V95. BASE TABLE and VIEW entries are distinguished. Column defaults are SQL expressions, not application defaults. FK rules below are the actual database-enforced relationships; logical associations are described in Document 1. No customer rows were exported.

## account_contact_challenges

Type: **BASE TABLE**. Purpose: Authentication, contact verification, account recovery, throttling or session invalidation metadata.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| account_kind | character varying / varchar | NO | No SQL default | 20 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored account kind. |
| account_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| channel | character varying / varchar | NO | No SQL default | 8 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored channel. |
| purpose | character varying / varchar | NO | No SQL default | 24 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored purpose. |
| destination_fingerprint | character varying / varchar | NO | No SQL default | 64 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored destination fingerprint. |
| secret_hash | character varying / varchar | NO | No SQL default | 255 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Sensitive representation; do not expose plaintext or hashes. |
| expires_at | timestamp with time zone / timestamptz | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored expires at. |
| consumed_at | timestamp with time zone / timestamptz | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored consumed at. |
| attempt_count | integer / int4 | NO | `0` | None / 32 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored attempt count. |
| created_at | timestamp with time zone / timestamptz | NO | `now()` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored created at. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| account_contact_challenges_account_kind_check | c | `CHECK (((account_kind)::text = ANY ((ARRAY['PLATFORM_ADMIN'::character varying, 'HOST'::character varying, 'GROUP_ADMIN'::character varying, 'GROUP_MEMBER'::character varying])::text[])))` |
| account_contact_challenges_attempt_count_check | c | `CHECK (((attempt_count >= 0) AND (attempt_count <= 10)))` |
| account_contact_challenges_channel_check | c | `CHECK (((channel)::text = ANY ((ARRAY['EMAIL'::character varying, 'SMS'::character varying])::text[])))` |
| account_contact_challenges_pkey | p | `PRIMARY KEY (id)` |
| account_contact_challenges_purpose_check | c | `CHECK (((purpose)::text = ANY ((ARRAY['VERIFY_CONTACT'::character varying, 'RESET_PASSWORD'::character varying, 'RESET_PIN'::character varying])::text[])))` |


### Indexes

| Index | Definition |
| --- | --- |
| account_contact_challenges_lookup | `CREATE INDEX account_contact_challenges_lookup ON public.account_contact_challenges USING btree (account_kind, account_id, purpose, created_at DESC)` |
| account_contact_challenges_pkey | `CREATE UNIQUE INDEX account_contact_challenges_pkey ON public.account_contact_challenges USING btree (id)` |


Consumers: [backend/src/main/java/com/giftplatform/security/recovery/EmailRecoveryController.java:1](../../src/main/java/com/giftplatform/security/recovery/EmailRecoveryController.java#L1); [backend/src/main/java/com/giftplatform/security/recovery/GroupEmailEnrollmentController.java:1](../../src/main/java/com/giftplatform/security/recovery/GroupEmailEnrollmentController.java#L1); [backend/src/main/java/com/giftplatform/security/recovery/GroupMobileEnrollmentController.java:1](../../src/main/java/com/giftplatform/security/recovery/GroupMobileEnrollmentController.java#L1); [backend/src/main/java/com/giftplatform/security/recovery/RecoveryChallengeStore.java:1](../../src/main/java/com/giftplatform/security/recovery/RecoveryChallengeStore.java#L1); [backend/src/main/java/com/giftplatform/security/verification/MobileVerificationService.java:1](../../src/main/java/com/giftplatform/security/verification/MobileVerificationService.java#L1)

## account_pending_email_verifications

Type: **BASE TABLE**. Purpose: Authentication, contact verification, account recovery, throttling or session invalidation metadata.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| challenge_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| account_kind | character varying / varchar | NO | No SQL default | 20 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored account kind. |
| account_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| email | character varying / varchar | NO | No SQL default | 254 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored email. |
| expires_at | timestamp with time zone / timestamptz | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored expires at. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| account_pending_email_verifications_account_kind_check | c | `CHECK (((account_kind)::text = ANY ((ARRAY['GROUP_ADMIN'::character varying, 'GROUP_MEMBER'::character varying])::text[])))` |
| account_pending_email_verifications_challenge_id_fkey | f | `FOREIGN KEY (challenge_id) REFERENCES account_contact_challenges(id) ON DELETE CASCADE` |
| account_pending_email_verifications_pkey | p | `PRIMARY KEY (challenge_id)` |


### Indexes

| Index | Definition |
| --- | --- |
| account_pending_email_account | `CREATE INDEX account_pending_email_account ON public.account_pending_email_verifications USING btree (account_kind, account_id)` |
| account_pending_email_verifications_pkey | `CREATE UNIQUE INDEX account_pending_email_verifications_pkey ON public.account_pending_email_verifications USING btree (challenge_id)` |


Consumers: [backend/src/main/java/com/giftplatform/security/recovery/GroupEmailEnrollmentController.java:1](../../src/main/java/com/giftplatform/security/recovery/GroupEmailEnrollmentController.java#L1)

## account_pending_mobile_verifications

Type: **BASE TABLE**. Purpose: Authentication, contact verification, account recovery, throttling or session invalidation metadata.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| challenge_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| account_kind | character varying / varchar | NO | No SQL default | 20 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored account kind. |
| account_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| mobile | character varying / varchar | NO | No SQL default | 16 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored mobile. |
| expires_at | timestamp with time zone / timestamptz | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored expires at. |
| delivered_at | timestamp with time zone / timestamptz | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored delivered at. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| account_pending_mobile_verifications_challenge_id_fkey | f | `FOREIGN KEY (challenge_id) REFERENCES account_contact_challenges(id) ON DELETE CASCADE` |
| account_pending_mobile_verifications_pkey | p | `PRIMARY KEY (challenge_id)` |


### Indexes

| Index | Definition |
| --- | --- |
| account_pending_mobile_account_idx | `CREATE INDEX account_pending_mobile_account_idx ON public.account_pending_mobile_verifications USING btree (account_kind, account_id)` |
| account_pending_mobile_verifications_pkey | `CREATE UNIQUE INDEX account_pending_mobile_verifications_pkey ON public.account_pending_mobile_verifications USING btree (challenge_id)` |


Consumers: [backend/src/main/java/com/giftplatform/security/recovery/GroupMobileEnrollmentController.java:1](../../src/main/java/com/giftplatform/security/recovery/GroupMobileEnrollmentController.java#L1); [backend/src/main/java/com/giftplatform/security/verification/MobileVerificationService.java:1](../../src/main/java/com/giftplatform/security/verification/MobileVerificationService.java#L1)

## account_recovery_challenges

Type: **BASE TABLE**. Purpose: Authentication, contact verification, account recovery, throttling or session invalidation metadata.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| account_kind | character varying / varchar | NO | No SQL default | 20 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored account kind. |
| account_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| secret_hash | character varying / varchar | NO | No SQL default | 128 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Sensitive representation; do not expose plaintext or hashes. |
| expires_at | timestamp with time zone / timestamptz | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored expires at. |
| consumed_at | timestamp with time zone / timestamptz | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored consumed at. |
| attempt_count | integer / int4 | NO | `0` | None / 32 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored attempt count. |
| created_at | timestamp with time zone / timestamptz | NO | `now()` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored created at. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| account_recovery_challenges_account_kind_check | c | `CHECK (((account_kind)::text = ANY ((ARRAY['PLATFORM_ADMIN'::character varying, 'HOST'::character varying, 'GROUP_ADMIN'::character varying, 'GROUP_MEMBER'::character varying])::text[])))` |
| account_recovery_challenges_attempt_count_check | c | `CHECK (((attempt_count >= 0) AND (attempt_count <= 10)))` |
| account_recovery_challenges_pkey | p | `PRIMARY KEY (id)` |


### Indexes

| Index | Definition |
| --- | --- |
| account_recovery_challenges_lookup | `CREATE INDEX account_recovery_challenges_lookup ON public.account_recovery_challenges USING btree (account_kind, account_id, created_at DESC)` |
| account_recovery_challenges_pkey | `CREATE UNIQUE INDEX account_recovery_challenges_pkey ON public.account_recovery_challenges USING btree (id)` |


Consumers: No direct Java literal reference found; migrations, views, triggers or indirect table selection may be the consumer.

## account_registration_policies

Type: **BASE TABLE**. Purpose: Authentication, contact verification, account recovery, throttling or session invalidation metadata.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| scope | character varying / varchar | NO | No SQL default | 12 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored scope. |
| scope_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| require_email | boolean / bool | NO | `true` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored require email. |
| require_mobile | boolean / bool | NO | `true` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored require mobile. |
| verify_email | boolean / bool | NO | `true` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored verify email. |
| verify_mobile | boolean / bool | NO | `true` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored verify mobile. |
| allow_group_pin | boolean / bool | NO | `true` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored allow group pin. |
| changed_by | uuid / uuid | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored changed by. |
| changed_at | timestamp with time zone / timestamptz | NO | `now()` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored changed at. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| account_registration_policies_changed_by_fkey | f | `FOREIGN KEY (changed_by) REFERENCES users(id)` |
| account_registration_policies_pkey | p | `PRIMARY KEY (scope, scope_id)` |
| account_registration_policies_scope_check | c | `CHECK (((scope)::text = ANY ((ARRAY['PLATFORM'::character varying, 'HOST'::character varying, 'EVENT'::character varying])::text[])))` |


### Indexes

| Index | Definition |
| --- | --- |
| account_registration_policies_pkey | `CREATE UNIQUE INDEX account_registration_policies_pkey ON public.account_registration_policies USING btree (scope, scope_id)` |


Consumers: [backend/src/main/java/com/giftplatform/admin/AccountPolicyController.java:1](../../src/main/java/com/giftplatform/admin/AccountPolicyController.java#L1); [backend/src/main/java/com/giftplatform/security/recovery/EffectiveRecoveryPolicyService.java:1](../../src/main/java/com/giftplatform/security/recovery/EffectiveRecoveryPolicyService.java#L1)

## affiliate_clicks

Type: **BASE TABLE**. Purpose: Monetization, commercial access, attribution, provider-reported statements or routing-policy history.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| partner_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| event_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| gift_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| destination_url | text / text | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored destination url. |
| created_at | timestamp with time zone / timestamptz | NO | `now()` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored created at. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| affiliate_clicks_destination_url_check | c | `CHECK ((length(destination_url) <= 16384))` |
| affiliate_clicks_event_id_fkey | f | `FOREIGN KEY (event_id) REFERENCES events(id)` |
| affiliate_clicks_gift_id_fkey | f | `FOREIGN KEY (gift_id) REFERENCES gifts(id)` |
| affiliate_clicks_partner_id_fkey | f | `FOREIGN KEY (partner_id) REFERENCES affiliate_partners(id)` |
| affiliate_clicks_pkey | p | `PRIMARY KEY (id)` |


### Indexes

| Index | Definition |
| --- | --- |
| affiliate_clicks_pkey | `CREATE UNIQUE INDEX affiliate_clicks_pkey ON public.affiliate_clicks USING btree (id)` |


Consumers: [backend/src/main/java/com/giftplatform/monetization/CommercialController.java:1](../../src/main/java/com/giftplatform/monetization/CommercialController.java#L1); [backend/src/main/java/com/giftplatform/monetization/CommercialPublicService.java:1](../../src/main/java/com/giftplatform/monetization/CommercialPublicService.java#L1); [backend/src/main/java/com/giftplatform/monetization/CommercialSetupService.java:1](../../src/main/java/com/giftplatform/monetization/CommercialSetupService.java#L1)

## affiliate_commissions

Type: **BASE TABLE**. Purpose: Monetization, commercial access, attribution, provider-reported statements or routing-policy history.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| partner_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| click_id | uuid / uuid | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| external_reference | character varying / varchar | NO | No SQL default | 160 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored external reference. |
| amount_paise | bigint / int8 | NO | No SQL default | None / 64 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Money in integer paise; preserve distinction between reported, confirmed and settled. |
| state | character varying / varchar | NO | No SQL default | 20 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. State value; see CHECK constraints. |
| revision | bigint / int8 | NO | `0` | None / 64 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored revision. |
| changed_at | timestamp with time zone / timestamptz | NO | `now()` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored changed at. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| affiliate_commissions_amount_paise_check | c | `CHECK ((amount_paise > 0))` |
| affiliate_commissions_click_id_fkey | f | `FOREIGN KEY (click_id) REFERENCES affiliate_clicks(id)` |
| affiliate_commissions_partner_id_external_reference_key | u | `UNIQUE (partner_id, external_reference)` |
| affiliate_commissions_partner_id_fkey | f | `FOREIGN KEY (partner_id) REFERENCES affiliate_partners(id)` |
| affiliate_commissions_pkey | p | `PRIMARY KEY (id)` |
| affiliate_commissions_state_check | c | `CHECK (((state)::text = ANY ((ARRAY['CONFIRMED'::character varying, 'PAID'::character varying, 'REVERSED'::character varying])::text[])))` |


### Indexes

| Index | Definition |
| --- | --- |
| affiliate_commissions_partner_id_external_reference_key | `CREATE UNIQUE INDEX affiliate_commissions_partner_id_external_reference_key ON public.affiliate_commissions USING btree (partner_id, external_reference)` |
| affiliate_commissions_pkey | `CREATE UNIQUE INDEX affiliate_commissions_pkey ON public.affiliate_commissions USING btree (id)` |


Consumers: [backend/src/main/java/com/giftplatform/monetization/CommercialSetupService.java:1](../../src/main/java/com/giftplatform/monetization/CommercialSetupService.java#L1)

## affiliate_partners

Type: **BASE TABLE**. Purpose: Monetization, commercial access, attribution, provider-reported statements or routing-policy history.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| name | character varying / varchar | NO | No SQL default | 100 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored name. |
| product_domain | character varying / varchar | NO | No SQL default | 253 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored product domain. |
| enabled | boolean / bool | NO | `false` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored enabled. |
| parameter_name | character varying / varchar | NO | No SQL default | 60 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored parameter name. |
| parameter_value | character varying / varchar | NO | No SQL default | 500 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored parameter value. |
| click_parameter | character varying / varchar | YES | No SQL default | 60 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored click parameter. |
| redirect_template | character varying / varchar | YES | No SQL default | 2048 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored redirect template. |
| revision | bigint / int8 | NO | `0` | None / 64 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored revision. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| affiliate_partners_pkey | p | `PRIMARY KEY (id)` |
| affiliate_partners_product_domain_key | u | `UNIQUE (product_domain)` |


### Indexes

| Index | Definition |
| --- | --- |
| affiliate_partners_pkey | `CREATE UNIQUE INDEX affiliate_partners_pkey ON public.affiliate_partners USING btree (id)` |
| affiliate_partners_product_domain_key | `CREATE UNIQUE INDEX affiliate_partners_product_domain_key ON public.affiliate_partners USING btree (product_domain)` |


Consumers: [backend/src/main/java/com/giftplatform/monetization/CommercialPublicService.java:1](../../src/main/java/com/giftplatform/monetization/CommercialPublicService.java#L1); [backend/src/main/java/com/giftplatform/monetization/CommercialSetupService.java:1](../../src/main/java/com/giftplatform/monetization/CommercialSetupService.java#L1)

## audit_logs

Type: **BASE TABLE**. Purpose: Application audit history for actor/resource changes.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| actor_id | uuid / uuid | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| actor_type | character varying / varchar | NO | No SQL default | 30 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored actor type. |
| entity_type | character varying / varchar | NO | No SQL default | 50 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored entity type. |
| entity_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| action | character varying / varchar | NO | No SQL default | 30 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored action. |
| old_values | jsonb / jsonb | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored old values. |
| new_values | jsonb / jsonb | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored new values. |
| occurred_at | timestamp with time zone / timestamptz | NO | `CURRENT_TIMESTAMP` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored occurred at. |
| request_id | character varying / varchar | YES | No SQL default | 100 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| audit_logs_pkey | p | `PRIMARY KEY (id)` |
| chk_audit_action | c | `CHECK (((action)::text = ANY ((ARRAY['CREATE'::character varying, 'UPDATE'::character varying, 'DELETE'::character varying, 'STATUS_CHANGE'::character varying])::text[])))` |
| chk_audit_actor_type | c | `CHECK (((actor_type)::text = ANY ((ARRAY['CUSTOMER'::character varying, 'ADMIN'::character varying, 'SYSTEM'::character varying, 'GUEST'::character varying])::text[])))` |


### Indexes

| Index | Definition |
| --- | --- |
| audit_logs_pkey | `CREATE UNIQUE INDEX audit_logs_pkey ON public.audit_logs USING btree (id)` |
| idx_audit_actor | `CREATE INDEX idx_audit_actor ON public.audit_logs USING btree (actor_id)` |
| idx_audit_entity | `CREATE INDEX idx_audit_entity ON public.audit_logs USING btree (entity_type, entity_id)` |
| idx_audit_occurred_at | `CREATE INDEX idx_audit_occurred_at ON public.audit_logs USING btree (occurred_at DESC)` |


Consumers: [backend/src/main/java/com/giftplatform/audit/entity/AuditLog.java:1](../../src/main/java/com/giftplatform/audit/entity/AuditLog.java#L1); [backend/src/main/java/com/giftplatform/payment/settlement/HostSettlementUiService.java:1](../../src/main/java/com/giftplatform/payment/settlement/HostSettlementUiService.java#L1)

## commercial_entitlements

Type: **BASE TABLE**. Purpose: Monetization, commercial access, attribution, provider-reported statements or routing-policy history.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| order_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| buyer_kind | character varying / varchar | NO | No SQL default | 20 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored buyer kind. |
| buyer_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| stream | character varying / varchar | NO | No SQL default | 40 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored stream. |
| resource_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| starts_at | timestamp with time zone / timestamptz | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored starts at. |
| expires_at | timestamp with time zone / timestamptz | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored expires at. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| commercial_entitlements_check | c | `CHECK (((expires_at IS NULL) OR (expires_at > starts_at)))` |
| commercial_entitlements_order_id_fkey | f | `FOREIGN KEY (order_id) REFERENCES commercial_orders(id)` |
| commercial_entitlements_pkey | p | `PRIMARY KEY (order_id)` |


### Indexes

| Index | Definition |
| --- | --- |
| commercial_entitlements_lookup | `CREATE INDEX commercial_entitlements_lookup ON public.commercial_entitlements USING btree (buyer_kind, buyer_id, stream, resource_id)` |
| commercial_entitlements_pkey | `CREATE UNIQUE INDEX commercial_entitlements_pkey ON public.commercial_entitlements USING btree (order_id)` |


Consumers: [backend/src/main/java/com/giftplatform/monetization/CommercialAccessService.java:1](../../src/main/java/com/giftplatform/monetization/CommercialAccessService.java#L1); [backend/src/main/java/com/giftplatform/monetization/CommercialController.java:1](../../src/main/java/com/giftplatform/monetization/CommercialController.java#L1); [backend/src/main/java/com/giftplatform/monetization/CommercialPublicService.java:1](../../src/main/java/com/giftplatform/monetization/CommercialPublicService.java#L1); [backend/src/main/java/com/giftplatform/monetization/CommercialPurchaseService.java:1](../../src/main/java/com/giftplatform/monetization/CommercialPurchaseService.java#L1)

## commercial_orders

Type: **BASE TABLE**. Purpose: Monetization, commercial access, attribution, provider-reported statements or routing-policy history.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| buyer_kind | character varying / varchar | NO | No SQL default | 20 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored buyer kind. |
| buyer_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| stream | character varying / varchar | NO | No SQL default | 40 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored stream. |
| resource_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| amount_paise | bigint / int8 | NO | No SQL default | None / 64 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Money in integer paise; preserve distinction between reported, confirmed and settled. |
| pricing_revision | bigint / int8 | NO | No SQL default | None / 64 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored pricing revision. |
| policy_revision | bigint / int8 | NO | No SQL default | None / 64 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored policy revision. |
| duration_days | integer / int4 | NO | No SQL default | None / 32 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored duration days. |
| idempotency_key | character varying / varchar | NO | No SQL default | 120 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored idempotency key. |
| provider_order_id | character varying / varchar | YES | No SQL default | 100 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| provider_payment_id | character varying / varchar | YES | No SQL default | 100 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| status | character varying / varchar | NO | No SQL default | 20 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. State value; see CHECK constraints. |
| created_at | timestamp with time zone / timestamptz | NO | `now()` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored created at. |
| paid_at | timestamp with time zone / timestamptz | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored paid at. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| commercial_orders_amount_paise_check | c | `CHECK ((amount_paise >= 0))` |
| commercial_orders_buyer_kind_buyer_id_idempotency_key_key | u | `UNIQUE (buyer_kind, buyer_id, idempotency_key)` |
| commercial_orders_buyer_kind_check | c | `CHECK (((buyer_kind)::text = ANY ((ARRAY['HOST'::character varying, 'GROUP_ADMIN'::character varying])::text[])))` |
| commercial_orders_check | c | `CHECK (((((status)::text = 'CAPTURED'::text) AND (amount_paise > 0) AND (provider_payment_id IS NOT NULL) AND (paid_at IS NOT NULL)) OR (((status)::text = 'FREE'::text) AND (amount_paise = 0) AND (paid_at IS NOT NULL)) OR ((status)::text = ANY ((ARRAY['CREATED'::character varying, 'PENDING'::character varying])::text[]))))` |
| commercial_orders_duration_days_check | c | `CHECK (((duration_days >= 0) AND (duration_days <= 3660)))` |
| commercial_orders_pkey | p | `PRIMARY KEY (id)` |
| commercial_orders_pricing_revision_fkey | f | `FOREIGN KEY (pricing_revision) REFERENCES platform_monetization_history(revision)` |
| commercial_orders_provider_order_id_key | u | `UNIQUE (provider_order_id)` |
| commercial_orders_provider_payment_id_key | u | `UNIQUE (provider_payment_id)` |
| commercial_orders_status_check | c | `CHECK (((status)::text = ANY ((ARRAY['CREATED'::character varying, 'PENDING'::character varying, 'CAPTURED'::character varying, 'FREE'::character varying])::text[])))` |
| commercial_orders_stream_check | c | `CHECK (((stream)::text = ANY ((ARRAY['HOST_PLAN'::character varying, 'PREMIUM_TEMPLATE'::character varying, 'VENDOR_LISTING'::character varying, 'SPONSORED_GIFT'::character varying, 'GROUP_GIFT'::character varying])::text[])))` |


### Indexes

| Index | Definition |
| --- | --- |
| commercial_orders_buyer_kind_buyer_id_idempotency_key_key | `CREATE UNIQUE INDEX commercial_orders_buyer_kind_buyer_id_idempotency_key_key ON public.commercial_orders USING btree (buyer_kind, buyer_id, idempotency_key)` |
| commercial_orders_pkey | `CREATE UNIQUE INDEX commercial_orders_pkey ON public.commercial_orders USING btree (id)` |
| commercial_orders_provider_order_id_key | `CREATE UNIQUE INDEX commercial_orders_provider_order_id_key ON public.commercial_orders USING btree (provider_order_id)` |
| commercial_orders_provider_payment_id_key | `CREATE UNIQUE INDEX commercial_orders_provider_payment_id_key ON public.commercial_orders USING btree (provider_payment_id)` |


Consumers: [backend/src/main/java/com/giftplatform/monetization/CommercialController.java:1](../../src/main/java/com/giftplatform/monetization/CommercialController.java#L1); [backend/src/main/java/com/giftplatform/monetization/CommercialPurchaseService.java:1](../../src/main/java/com/giftplatform/monetization/CommercialPurchaseService.java#L1); [backend/src/main/java/com/giftplatform/payment/service/RazorpayWebhookService.java:1](../../src/main/java/com/giftplatform/payment/service/RazorpayWebhookService.java#L1)

## commercial_placement_clicks

Type: **BASE TABLE**. Purpose: Monetization, commercial access, attribution, provider-reported statements or routing-policy history.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| placement_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| created_at | timestamp with time zone / timestamptz | NO | `now()` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored created at. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| commercial_placement_clicks_pkey | p | `PRIMARY KEY (id)` |
| commercial_placement_clicks_placement_id_fkey | f | `FOREIGN KEY (placement_id) REFERENCES commercial_placements(id)` |


### Indexes

| Index | Definition |
| --- | --- |
| commercial_placement_clicks_pkey | `CREATE UNIQUE INDEX commercial_placement_clicks_pkey ON public.commercial_placement_clicks USING btree (id)` |


Consumers: [backend/src/main/java/com/giftplatform/monetization/CommercialPublicService.java:1](../../src/main/java/com/giftplatform/monetization/CommercialPublicService.java#L1); [backend/src/main/java/com/giftplatform/monetization/CommercialSetupService.java:1](../../src/main/java/com/giftplatform/monetization/CommercialSetupService.java#L1)

## commercial_placements

Type: **BASE TABLE**. Purpose: Monetization, commercial access, attribution, provider-reported statements or routing-policy history.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| owner_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| stream | character varying / varchar | NO | No SQL default | 40 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored stream. |
| title | character varying / varchar | NO | No SQL default | 160 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored title. |
| description | character varying / varchar | NO | No SQL default | 1000 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored description. |
| url | character varying / varchar | NO | No SQL default | 2048 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored url. |
| category | character varying / varchar | NO | No SQL default | 80 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored category. |
| occasion | character varying / varchar | NO | No SQL default | 50 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored occasion. |
| moderation | character varying / varchar | NO | `'PENDING'::character varying` | 20 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored moderation. |
| created_at | timestamp with time zone / timestamptz | NO | `now()` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored created at. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| commercial_placements_moderation_check | c | `CHECK (((moderation)::text = ANY ((ARRAY['PENDING'::character varying, 'APPROVED'::character varying, 'REJECTED'::character varying])::text[])))` |
| commercial_placements_owner_id_fkey | f | `FOREIGN KEY (owner_id) REFERENCES users(id)` |
| commercial_placements_pkey | p | `PRIMARY KEY (id)` |
| commercial_placements_stream_check | c | `CHECK (((stream)::text = ANY ((ARRAY['VENDOR_LISTING'::character varying, 'SPONSORED_GIFT'::character varying])::text[])))` |


### Indexes

| Index | Definition |
| --- | --- |
| commercial_placements_pkey | `CREATE UNIQUE INDEX commercial_placements_pkey ON public.commercial_placements USING btree (id)` |


Consumers: [backend/src/main/java/com/giftplatform/monetization/CommercialPublicService.java:1](../../src/main/java/com/giftplatform/monetization/CommercialPublicService.java#L1); [backend/src/main/java/com/giftplatform/monetization/CommercialPurchaseService.java:1](../../src/main/java/com/giftplatform/monetization/CommercialPurchaseService.java#L1); [backend/src/main/java/com/giftplatform/monetization/CommercialSetupService.java:1](../../src/main/java/com/giftplatform/monetization/CommercialSetupService.java#L1)

## commercial_policy

Type: **BASE TABLE**. Purpose: Monetization, commercial access, attribution, provider-reported statements or routing-policy history.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| id | smallint / int2 | NO | No SQL default | None / 16 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| revision | bigint / int8 | NO | `0` | None / 64 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored revision. |
| free_active_events | integer / int4 | NO | No SQL default | None / 32 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored free active events. |
| host_plan_days | integer / int4 | NO | No SQL default | None / 32 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored host plan days. |
| vendor_days | integer / int4 | NO | No SQL default | None / 32 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored vendor days. |
| sponsor_days | integer / int4 | NO | No SQL default | None / 32 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored sponsor days. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| commercial_policy_free_active_events_check | c | `CHECK (((free_active_events >= 0) AND (free_active_events <= 1000)))` |
| commercial_policy_host_plan_days_check | c | `CHECK (((host_plan_days >= 1) AND (host_plan_days <= 3660)))` |
| commercial_policy_id_check | c | `CHECK ((id = 1))` |
| commercial_policy_pkey | p | `PRIMARY KEY (id)` |
| commercial_policy_sponsor_days_check | c | `CHECK (((sponsor_days >= 1) AND (sponsor_days <= 3660)))` |
| commercial_policy_vendor_days_check | c | `CHECK (((vendor_days >= 1) AND (vendor_days <= 3660)))` |


### Indexes

| Index | Definition |
| --- | --- |
| commercial_policy_pkey | `CREATE UNIQUE INDEX commercial_policy_pkey ON public.commercial_policy USING btree (id)` |


Consumers: [backend/src/main/java/com/giftplatform/monetization/CommercialAccessService.java:1](../../src/main/java/com/giftplatform/monetization/CommercialAccessService.java#L1); [backend/src/main/java/com/giftplatform/monetization/CommercialPurchaseService.java:1](../../src/main/java/com/giftplatform/monetization/CommercialPurchaseService.java#L1); [backend/src/main/java/com/giftplatform/monetization/CommercialSetupService.java:1](../../src/main/java/com/giftplatform/monetization/CommercialSetupService.java#L1)

## commercial_revenue

Type: **BASE TABLE**. Purpose: Monetization, commercial access, attribution, provider-reported statements or routing-policy history.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| order_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| amount_paise | bigint / int8 | NO | No SQL default | None / 64 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Money in integer paise; preserve distinction between reported, confirmed and settled. |
| currency | character varying / varchar | NO | No SQL default | 3 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored currency. |
| created_at | timestamp with time zone / timestamptz | NO | `now()` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored created at. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| commercial_revenue_amount_paise_check | c | `CHECK ((amount_paise > 0))` |
| commercial_revenue_currency_check | c | `CHECK (((currency)::text = 'INR'::text))` |
| commercial_revenue_order_id_fkey | f | `FOREIGN KEY (order_id) REFERENCES commercial_orders(id)` |
| commercial_revenue_pkey | p | `PRIMARY KEY (order_id)` |


### Indexes

| Index | Definition |
| --- | --- |
| commercial_revenue_pkey | `CREATE UNIQUE INDEX commercial_revenue_pkey ON public.commercial_revenue USING btree (order_id)` |


Consumers: [backend/src/main/java/com/giftplatform/monetization/CommercialPurchaseService.java:1](../../src/main/java/com/giftplatform/monetization/CommercialPurchaseService.java#L1); [backend/src/main/java/com/giftplatform/monetization/CommercialSetupService.java:1](../../src/main/java/com/giftplatform/monetization/CommercialSetupService.java#L1)

## commercial_setup_history

Type: **BASE TABLE**. Purpose: Monetization, commercial access, attribution, provider-reported statements or routing-policy history.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| actor_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| resource_type | character varying / varchar | NO | No SQL default | 40 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored resource type. |
| resource_id | character varying / varchar | NO | No SQL default | 100 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| reason | character varying / varchar | NO | No SQL default | 500 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored reason. |
| before_config | jsonb / jsonb | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored before config. |
| after_config | jsonb / jsonb | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored after config. |
| created_at | timestamp with time zone / timestamptz | NO | `now()` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored created at. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| commercial_setup_history_actor_id_fkey | f | `FOREIGN KEY (actor_id) REFERENCES users(id)` |
| commercial_setup_history_pkey | p | `PRIMARY KEY (id)` |


### Indexes

| Index | Definition |
| --- | --- |
| commercial_setup_history_pkey | `CREATE UNIQUE INDEX commercial_setup_history_pkey ON public.commercial_setup_history USING btree (id)` |


Consumers: [backend/src/main/java/com/giftplatform/monetization/CommercialSetupService.java:1](../../src/main/java/com/giftplatform/monetization/CommercialSetupService.java#L1)

## contribution_allocation_postings

Type: **BASE TABLE**. Purpose: Payment, accounting, pricing, reconciliation or blocked-payout workflow. State and idempotency constraints are listed below.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| contribution_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| entry_group_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| created_at | timestamp with time zone / timestamptz | NO | `CURRENT_TIMESTAMP` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored created at. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| contribution_allocation_postings_contribution_id_fkey | f | `FOREIGN KEY (contribution_id) REFERENCES contributions(id)` |
| contribution_allocation_postings_entry_group_id_key | u | `UNIQUE (entry_group_id)` |
| contribution_allocation_postings_pkey | p | `PRIMARY KEY (contribution_id)` |


### Indexes

| Index | Definition |
| --- | --- |
| contribution_allocation_postings_entry_group_id_key | `CREATE UNIQUE INDEX contribution_allocation_postings_entry_group_id_key ON public.contribution_allocation_postings USING btree (entry_group_id)` |
| contribution_allocation_postings_pkey | `CREATE UNIQUE INDEX contribution_allocation_postings_pkey ON public.contribution_allocation_postings USING btree (contribution_id)` |


Consumers: [backend/src/main/java/com/giftplatform/monetization/CommercialController.java:1](../../src/main/java/com/giftplatform/monetization/CommercialController.java#L1); [backend/src/main/java/com/giftplatform/payment/report/HostContributionReportService.java:1](../../src/main/java/com/giftplatform/payment/report/HostContributionReportService.java#L1); [backend/src/main/java/com/giftplatform/payment/service/ContributionAllocationService.java:1](../../src/main/java/com/giftplatform/payment/service/ContributionAllocationService.java#L1)

## contribution_allocations

Type: **BASE TABLE**. Purpose: Payment, accounting, pricing, reconciliation or blocked-payout workflow. State and idempotency constraints are listed below.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| contribution_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| event_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| gift_id | uuid / uuid | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| allocation_type | character varying / varchar | NO | No SQL default | 30 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored allocation type. |
| amount_paise | bigint / int8 | NO | No SQL default | None / 64 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Money in integer paise; preserve distinction between reported, confirmed and settled. |
| created_at | timestamp with time zone / timestamptz | NO | `CURRENT_TIMESTAMP` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored created at. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| ck_contribution_allocation_target | c | `CHECK (((((allocation_type)::text = 'GIFT'::text) AND (gift_id IS NOT NULL)) OR (((allocation_type)::text = 'EVENT_WALLET'::text) AND (gift_id IS NULL))))` |
| ck_contribution_allocation_type | c | `CHECK (((allocation_type)::text = ANY ((ARRAY['GIFT'::character varying, 'EVENT_WALLET'::character varying])::text[])))` |
| contribution_allocations_amount_paise_check | c | `CHECK ((amount_paise > 0))` |
| contribution_allocations_contribution_id_fkey | f | `FOREIGN KEY (contribution_id) REFERENCES contributions(id)` |
| contribution_allocations_event_id_fkey | f | `FOREIGN KEY (event_id) REFERENCES events(id)` |
| contribution_allocations_pkey | p | `PRIMARY KEY (id)` |
| fk_allocation_contribution_event | f | `FOREIGN KEY (contribution_id, event_id) REFERENCES contributions(id, event_id)` |
| fk_allocation_gift_event | f | `FOREIGN KEY (gift_id, event_id) REFERENCES gifts(id, event_id)` |
| uq_contribution_allocation_type | u | `UNIQUE (contribution_id, allocation_type)` |


### Indexes

| Index | Definition |
| --- | --- |
| contribution_allocations_pkey | `CREATE UNIQUE INDEX contribution_allocations_pkey ON public.contribution_allocations USING btree (id)` |
| idx_contribution_allocations_contribution | `CREATE INDEX idx_contribution_allocations_contribution ON public.contribution_allocations USING btree (contribution_id)` |
| idx_contribution_allocations_event | `CREATE INDEX idx_contribution_allocations_event ON public.contribution_allocations USING btree (event_id)` |
| idx_contribution_allocations_gift | `CREATE INDEX idx_contribution_allocations_gift ON public.contribution_allocations USING btree (gift_id) WHERE (gift_id IS NOT NULL)` |
| uq_contribution_allocation_type | `CREATE UNIQUE INDEX uq_contribution_allocation_type ON public.contribution_allocations USING btree (contribution_id, allocation_type)` |


Consumers: [backend/src/main/java/com/giftplatform/payment/controller/PublicGiftProgressController.java:1](../../src/main/java/com/giftplatform/payment/controller/PublicGiftProgressController.java#L1); [backend/src/main/java/com/giftplatform/payment/direct/GuestDirectTransferService.java:1](../../src/main/java/com/giftplatform/payment/direct/GuestDirectTransferService.java#L1); [backend/src/main/java/com/giftplatform/payment/report/HostContributionReportService.java:1](../../src/main/java/com/giftplatform/payment/report/HostContributionReportService.java#L1); [backend/src/main/java/com/giftplatform/payment/service/ContributionAllocationService.java:1](../../src/main/java/com/giftplatform/payment/service/ContributionAllocationService.java#L1); [backend/src/main/java/com/giftplatform/payment/service/GiftFundingService.java:1](../../src/main/java/com/giftplatform/payment/service/GiftFundingService.java#L1)

## contributions

Type: **BASE TABLE**. Purpose: Confirmed or recorded payment contribution entities; inspect allocation/ledger records for financial effects.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| payment_order_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| event_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| gift_id | uuid / uuid | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| guest_name | character varying / varchar | NO | No SQL default | 200 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored guest name. |
| guest_email | character varying / varchar | YES | No SQL default | 320 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored guest email. |
| guest_phone | character varying / varchar | YES | No SQL default | 30 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored guest phone. |
| amount_paise | bigint / int8 | NO | No SQL default | None / 64 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Money in integer paise; preserve distinction between reported, confirmed and settled. |
| allocation_status | character varying / varchar | NO | `'PENDING'::character varying` | 30 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. State value; see CHECK constraints. |
| created_at | timestamp with time zone / timestamptz | NO | `CURRENT_TIMESTAMP` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored created at. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| ck_contribution_allocation | c | `CHECK (((allocation_status)::text = ANY ((ARRAY['PENDING'::character varying, 'GIFT_ALLOCATED'::character varying, 'WALLET_ALLOCATED'::character varying, 'REALLOCATED'::character varying, 'REFUNDED'::character varying])::text[])))` |
| contributions_amount_paise_check | c | `CHECK ((amount_paise > 0))` |
| contributions_event_id_fkey | f | `FOREIGN KEY (event_id) REFERENCES events(id)` |
| contributions_gift_id_fkey | f | `FOREIGN KEY (gift_id) REFERENCES gifts(id)` |
| contributions_guest_name_check | c | `CHECK ((length(btrim((guest_name)::text)) > 0))` |
| contributions_payment_order_id_fkey | f | `FOREIGN KEY (payment_order_id) REFERENCES payment_orders(id)` |
| contributions_payment_order_id_key | u | `UNIQUE (payment_order_id)` |
| contributions_pkey | p | `PRIMARY KEY (id)` |
| fk_contribution_gift_event | f | `FOREIGN KEY (gift_id, event_id) REFERENCES gifts(id, event_id)` |
| uq_contributions_id_event | u | `UNIQUE (id, event_id)` |


### Indexes

| Index | Definition |
| --- | --- |
| contributions_payment_order_id_key | `CREATE UNIQUE INDEX contributions_payment_order_id_key ON public.contributions USING btree (payment_order_id)` |
| contributions_pkey | `CREATE UNIQUE INDEX contributions_pkey ON public.contributions USING btree (id)` |
| idx_contributions_event | `CREATE INDEX idx_contributions_event ON public.contributions USING btree (event_id, created_at)` |
| idx_contributions_gift | `CREATE INDEX idx_contributions_gift ON public.contributions USING btree (gift_id)` |
| idx_contributions_gift_event | `CREATE INDEX idx_contributions_gift_event ON public.contributions USING btree (gift_id, event_id)` |
| uq_contributions_id_event | `CREATE UNIQUE INDEX uq_contributions_id_event ON public.contributions USING btree (id, event_id)` |


Consumers: [backend/src/main/java/com/giftplatform/gift/service/GiftManagementService.java:1](../../src/main/java/com/giftplatform/gift/service/GiftManagementService.java#L1); [backend/src/main/java/com/giftplatform/group/GroupGiftCollectionController.java:1](../../src/main/java/com/giftplatform/group/GroupGiftCollectionController.java#L1); [backend/src/main/java/com/giftplatform/group/GroupGiftLifecycleService.java:1](../../src/main/java/com/giftplatform/group/GroupGiftLifecycleService.java#L1); [backend/src/main/java/com/giftplatform/group/GroupGuestContributionController.java:1](../../src/main/java/com/giftplatform/group/GroupGuestContributionController.java#L1); [backend/src/main/java/com/giftplatform/group/GuestGiftController.java:1](../../src/main/java/com/giftplatform/group/GuestGiftController.java#L1); [backend/src/main/java/com/giftplatform/group/IndependentGroupGiftController.java:1](../../src/main/java/com/giftplatform/group/IndependentGroupGiftController.java#L1); [backend/src/main/java/com/giftplatform/group/IndividualGuestGiftController.java:1](../../src/main/java/com/giftplatform/group/IndividualGuestGiftController.java#L1); [backend/src/main/java/com/giftplatform/hostpayment/verification/cashfree/CashfreeVerificationAttemptService.java:1](../../src/main/java/com/giftplatform/hostpayment/verification/cashfree/CashfreeVerificationAttemptService.java#L1); [backend/src/main/java/com/giftplatform/monetization/CommercialController.java:1](../../src/main/java/com/giftplatform/monetization/CommercialController.java#L1); [backend/src/main/java/com/giftplatform/monetization/MonetizationService.java:1](../../src/main/java/com/giftplatform/monetization/MonetizationService.java#L1); [backend/src/main/java/com/giftplatform/payment/controller/PublicGiftProgressController.java:1](../../src/main/java/com/giftplatform/payment/controller/PublicGiftProgressController.java#L1); [backend/src/main/java/com/giftplatform/payment/direct/GuestDirectTransferService.java:1](../../src/main/java/com/giftplatform/payment/direct/GuestDirectTransferService.java#L1); [backend/src/main/java/com/giftplatform/payment/entity/Contribution.java:1](../../src/main/java/com/giftplatform/payment/entity/Contribution.java#L1); [backend/src/main/java/com/giftplatform/payment/report/HostContributionReportController.java:1](../../src/main/java/com/giftplatform/payment/report/HostContributionReportController.java#L1); [backend/src/main/java/com/giftplatform/payment/report/HostContributionReportService.java:1](../../src/main/java/com/giftplatform/payment/report/HostContributionReportService.java#L1); [backend/src/main/java/com/giftplatform/payment/service/ContributionAllocationService.java:1](../../src/main/java/com/giftplatform/payment/service/ContributionAllocationService.java#L1); [backend/src/main/java/com/giftplatform/payment/service/GiftFundingService.java:1](../../src/main/java/com/giftplatform/payment/service/GiftFundingService.java#L1); [backend/src/main/java/com/giftplatform/payment/service/GuestCheckoutService.java:1](../../src/main/java/com/giftplatform/payment/service/GuestCheckoutService.java#L1); [backend/src/main/java/com/giftplatform/payment/service/PaymentNotificationService.java:1](../../src/main/java/com/giftplatform/payment/service/PaymentNotificationService.java#L1); [backend/src/main/java/com/giftplatform/payment/service/PaymentOrderService.java:1](../../src/main/java/com/giftplatform/payment/service/PaymentOrderService.java#L1); [backend/src/main/java/com/giftplatform/payment/service/RazorpayCaptureService.java:1](../../src/main/java/com/giftplatform/payment/service/RazorpayCaptureService.java#L1)

## credential_session_versions

Type: **BASE TABLE**. Purpose: Authentication, contact verification, account recovery, throttling or session invalidation metadata.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| account_kind | character varying / varchar | NO | No SQL default | 20 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored account kind. |
| account_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| version | bigint / int8 | NO | `0` | None / 64 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored version. |
| updated_at | timestamp with time zone / timestamptz | NO | `now()` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored updated at. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| credential_session_versions_account_kind_check | c | `CHECK (((account_kind)::text = ANY ((ARRAY['PLATFORM_ADMIN'::character varying, 'HOST'::character varying, 'GROUP_ADMIN'::character varying, 'GROUP_MEMBER'::character varying])::text[])))` |
| credential_session_versions_pkey | p | `PRIMARY KEY (account_kind, account_id)` |
| credential_session_versions_version_check | c | `CHECK ((version >= 0))` |


### Indexes

| Index | Definition |
| --- | --- |
| credential_session_versions_pkey | `CREATE UNIQUE INDEX credential_session_versions_pkey ON public.credential_session_versions USING btree (account_kind, account_id)` |


Consumers: [backend/src/main/java/com/giftplatform/security/recovery/CredentialSessionVersionStore.java:1](../../src/main/java/com/giftplatform/security/recovery/CredentialSessionVersionStore.java#L1)

## event_guest_submission_limits

Type: **BASE TABLE**. Purpose: Event-scoped guest responses, guestbook, notification preferences or submission limits.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| event_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| fingerprint | character varying / varchar | NO | No SQL default | 64 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored fingerprint. |
| submission_date | date / date | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored submission date. |
| submission_type | character varying / varchar | NO | No SQL default | 12 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored submission type. |
| count | integer / int4 | NO | No SQL default | None / 32 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored count. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| event_guest_submission_limits_count_check | c | `CHECK (((count >= 1) AND (count <= 100)))` |
| event_guest_submission_limits_event_id_fkey | f | `FOREIGN KEY (event_id) REFERENCES events(id)` |
| event_guest_submission_limits_pkey | p | `PRIMARY KEY (event_id, fingerprint, submission_date, submission_type)` |


### Indexes

| Index | Definition |
| --- | --- |
| event_guest_submission_limits_pkey | `CREATE UNIQUE INDEX event_guest_submission_limits_pkey ON public.event_guest_submission_limits USING btree (event_id, fingerprint, submission_date, submission_type)` |


Consumers: [backend/src/main/java/com/giftplatform/celebration/CelebrationController.java:1](../../src/main/java/com/giftplatform/celebration/CelebrationController.java#L1); [backend/src/main/java/com/giftplatform/celebration/GuestbookPhotoController.java:1](../../src/main/java/com/giftplatform/celebration/GuestbookPhotoController.java#L1)

## event_guestbook

Type: **BASE TABLE**. Purpose: Event-scoped guest responses, guestbook, notification preferences or submission limits.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| event_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| guest_name | character varying / varchar | NO | No SQL default | 120 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored guest name. |
| message | character varying / varchar | NO | No SQL default | 1000 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored message. |
| status | character varying / varchar | NO | `'PENDING'::character varying` | 12 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. State value; see CHECK constraints. |
| created_at | timestamp with time zone / timestamptz | NO | `now()` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored created at. |
| moderated_at | timestamp with time zone / timestamptz | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored moderated at. |
| photo_path | character varying / varchar | YES | No SQL default | 100 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored photo path. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| event_guestbook_event_id_fkey | f | `FOREIGN KEY (event_id) REFERENCES events(id)` |
| event_guestbook_pkey | p | `PRIMARY KEY (id)` |
| event_guestbook_status_check | c | `CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'APPROVED'::character varying, 'HIDDEN'::character varying])::text[])))` |


### Indexes

| Index | Definition |
| --- | --- |
| event_guestbook_pkey | `CREATE UNIQUE INDEX event_guestbook_pkey ON public.event_guestbook USING btree (id)` |
| ix_event_guestbook_event_status | `CREATE INDEX ix_event_guestbook_event_status ON public.event_guestbook USING btree (event_id, status, created_at DESC)` |


Consumers: [backend/src/main/java/com/giftplatform/admin/PlatformAdminConsoleController.java:1](../../src/main/java/com/giftplatform/admin/PlatformAdminConsoleController.java#L1); [backend/src/main/java/com/giftplatform/celebration/CelebrationController.java:1](../../src/main/java/com/giftplatform/celebration/CelebrationController.java#L1); [backend/src/main/java/com/giftplatform/celebration/GuestbookPhotoController.java:1](../../src/main/java/com/giftplatform/celebration/GuestbookPhotoController.java#L1); [backend/src/main/java/com/giftplatform/celebration/MemoryAlbumController.java:1](../../src/main/java/com/giftplatform/celebration/MemoryAlbumController.java#L1)

## event_payment_account_links

Type: **BASE TABLE**. Purpose: Host receiving destinations, event-scoped account selection or independent account-verification attempts.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| event_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| host_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| bank_account_id | uuid / uuid | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| upi_account_id | uuid / uuid | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| destination_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| destination_kind | text / text | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored destination kind. |
| enabled | boolean / bool | NO | `true` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored enabled. |
| preferred | boolean / bool | NO | `false` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored preferred. |
| created_at | timestamp with time zone / timestamptz | NO | `now()` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored created at. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| event_payment_account_links_bank_account_id_host_id_fkey | f | `FOREIGN KEY (bank_account_id, host_id) REFERENCES host_bank_accounts(id, host_id)` |
| event_payment_account_links_check | c | `CHECK (((bank_account_id IS NOT NULL) <> (upi_account_id IS NOT NULL)))` |
| event_payment_account_links_check1 | c | `CHECK (((NOT preferred) OR enabled))` |
| event_payment_account_links_event_id_host_id_fkey | f | `FOREIGN KEY (event_id, host_id) REFERENCES events(id, owner_id)` |
| event_payment_account_links_pkey | p | `PRIMARY KEY (event_id, destination_kind, destination_id)` |
| event_payment_account_links_upi_account_id_host_id_fkey | f | `FOREIGN KEY (upi_account_id, host_id) REFERENCES host_upi_accounts(id, host_id)` |


### Indexes

| Index | Definition |
| --- | --- |
| event_payment_account_links_pkey | `CREATE UNIQUE INDEX event_payment_account_links_pkey ON public.event_payment_account_links USING btree (event_id, destination_kind, destination_id)` |
| ux_event_link_preferred | `CREATE UNIQUE INDEX ux_event_link_preferred ON public.event_payment_account_links USING btree (event_id, destination_kind) WHERE preferred` |


Consumers: [backend/src/main/java/com/giftplatform/event/service/EventPublishReadiness.java:1](../../src/main/java/com/giftplatform/event/service/EventPublishReadiness.java#L1); [backend/src/main/java/com/giftplatform/hostpayment/controller/ReceivingAccountHealthController.java:1](../../src/main/java/com/giftplatform/hostpayment/controller/ReceivingAccountHealthController.java#L1); [backend/src/main/java/com/giftplatform/hostpayment/service/EventAccountLinkService.java:1](../../src/main/java/com/giftplatform/hostpayment/service/EventAccountLinkService.java#L1); [backend/src/main/java/com/giftplatform/payment/direct/GuestDirectTransferService.java:1](../../src/main/java/com/giftplatform/payment/direct/GuestDirectTransferService.java#L1); [backend/src/main/java/com/giftplatform/payment/direct/GuestVerifiedDestinationService.java:1](../../src/main/java/com/giftplatform/payment/direct/GuestVerifiedDestinationService.java#L1); [backend/src/main/java/com/giftplatform/payment/settlement/HostPayoutRequestService.java:1](../../src/main/java/com/giftplatform/payment/settlement/HostPayoutRequestService.java#L1); [backend/src/main/java/com/giftplatform/payment/settlement/HostSettlementUiService.java:1](../../src/main/java/com/giftplatform/payment/settlement/HostSettlementUiService.java#L1)

## event_rsvps

Type: **BASE TABLE**. Purpose: Event-scoped guest responses, guestbook, notification preferences or submission limits.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| event_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| guest_name | character varying / varchar | NO | No SQL default | 120 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored guest name. |
| guest_email | character varying / varchar | NO | No SQL default | 254 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored guest email. |
| attendance | character varying / varchar | NO | No SQL default | 12 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored attendance. |
| party_size | integer / int4 | NO | No SQL default | None / 32 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored party size. |
| created_at | timestamp with time zone / timestamptz | NO | `now()` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored created at. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| chk_event_rsvp_party | c | `CHECK (((((attendance)::text = 'YES'::text) AND ((party_size >= 1) AND (party_size <= 10))) OR (((attendance)::text <> 'YES'::text) AND (party_size = 0))))` |
| event_rsvps_attendance_check | c | `CHECK (((attendance)::text = ANY ((ARRAY['YES'::character varying, 'NO'::character varying, 'MAYBE'::character varying])::text[])))` |
| event_rsvps_event_id_fkey | f | `FOREIGN KEY (event_id) REFERENCES events(id)` |
| event_rsvps_party_size_check | c | `CHECK (((party_size >= 0) AND (party_size <= 10)))` |
| event_rsvps_pkey | p | `PRIMARY KEY (id)` |
| ux_event_rsvp_email | u | `UNIQUE (event_id, guest_email)` |


### Indexes

| Index | Definition |
| --- | --- |
| event_rsvps_pkey | `CREATE UNIQUE INDEX event_rsvps_pkey ON public.event_rsvps USING btree (id)` |
| ix_event_rsvp_event | `CREATE INDEX ix_event_rsvp_event ON public.event_rsvps USING btree (event_id)` |
| ux_event_rsvp_email | `CREATE UNIQUE INDEX ux_event_rsvp_email ON public.event_rsvps USING btree (event_id, guest_email)` |


Consumers: [backend/src/main/java/com/giftplatform/celebration/CelebrationController.java:1](../../src/main/java/com/giftplatform/celebration/CelebrationController.java#L1)

## event_thank_you_settings

Type: **BASE TABLE**. Purpose: Event-scoped guest responses, guestbook, notification preferences or submission limits.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| event_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| host_display_name | character varying / varchar | NO | No SQL default | 200 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored host display name. |
| email_subject | character varying / varchar | NO | `'Thank you for celebrating with us'::character varying` | 200 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored email subject. |
| email_body | text / text | NO | `'Dear {{guestName}}, thank you for your thoughtful contribution to {{eventName}}. With love, {{hostName}}'::text` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored email body. |
| sms_body | text / text | NO | `'Thank you {{guestName}} for celebrating {{eventName}} with us! - {{hostName}}'::text` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored sms body. |
| card_template | character varying / varchar | NO | `'CLASSIC'::character varying` | 60 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored card template. |
| auto_send | boolean / bool | NO | `true` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored auto send. |
| email_enabled | boolean / bool | NO | `true` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored email enabled. |
| sms_enabled | boolean / bool | NO | `false` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored sms enabled. |
| whatsapp_enabled | boolean / bool | NO | `false` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored whatsapp enabled. |
| updated_at | timestamp with time zone / timestamptz | NO | `CURRENT_TIMESTAMP` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored updated at. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| event_thank_you_settings_event_id_fkey | f | `FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE` |
| event_thank_you_settings_pkey | p | `PRIMARY KEY (event_id)` |
| event_thanks_email_body_length | c | `CHECK (((length(email_body) >= 1) AND (length(email_body) <= 10000)))` |
| event_thanks_email_subject_nonempty | c | `CHECK (((length(btrim((email_subject)::text)) >= 1) AND (length(btrim((email_subject)::text)) <= 200)))` |
| event_thanks_host_nonempty | c | `CHECK (((length(btrim((host_display_name)::text)) >= 1) AND (length(btrim((host_display_name)::text)) <= 200)))` |
| event_thanks_sms_body_length | c | `CHECK (((length(sms_body) >= 1) AND (length(sms_body) <= 1000)))` |


### Indexes

| Index | Definition |
| --- | --- |
| event_thank_you_settings_pkey | `CREATE UNIQUE INDEX event_thank_you_settings_pkey ON public.event_thank_you_settings USING btree (event_id)` |


Consumers: [backend/src/main/java/com/giftplatform/notification/HostThankYouService.java:1](../../src/main/java/com/giftplatform/notification/HostThankYouService.java#L1); [backend/src/main/java/com/giftplatform/notification/ManualClaimNotificationService.java:1](../../src/main/java/com/giftplatform/notification/ManualClaimNotificationService.java#L1)

## events

Type: **BASE TABLE**. Purpose: Host-owned celebration, invitation identity, lifecycle and delivery details.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| title | character varying / varchar | NO | No SQL default | 200 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored title. |
| description | text / text | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored description. |
| occasion_type | character varying / varchar | NO | No SQL default | 50 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored occasion type. |
| event_date | date / date | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored event date. |
| status | character varying / varchar | NO | `'DRAFT'::character varying` | 30 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. State value; see CHECK constraints. |
| created_at | timestamp with time zone / timestamptz | NO | `CURRENT_TIMESTAMP` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored created at. |
| updated_at | timestamp with time zone / timestamptz | NO | `CURRENT_TIMESTAMP` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored updated at. |
| owner_id | uuid / uuid | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| version | bigint / int8 | NO | `0` | None / 64 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored version. |
| fulfillment_default | character varying / varchar | NO | `'AFTER_EVENT_CONFIRMATION'::character varying` | 40 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored fulfillment default. |
| public_token | uuid / uuid | NO | `gen_random_uuid()` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored public token. |
| qr_theme | character varying / varchar | NO | `'MODERN_MINIMAL'::character varying` | 40 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored qr theme. |
| address_line1 | character varying / varchar | YES | No SQL default | 200 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored address line1. |
| address_city | character varying / varchar | YES | No SQL default | 120 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored address city. |
| address_state | character varying / varchar | YES | No SQL default | 120 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored address state. |
| address_postal_code | character varying / varchar | YES | No SQL default | 20 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored address postal code. |
| address_country | character varying / varchar | YES | No SQL default | 80 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored address country. |
| contact_name | character varying / varchar | YES | No SQL default | 150 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored contact name. |
| contact_phone | character varying / varchar | YES | No SQL default | 30 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored contact phone. |
| contact_email | character varying / varchar | YES | No SQL default | 254 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored contact email. |
| host_display_name | character varying / varchar | NO | No SQL default | 200 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored host display name. |
| delivery_same_as_event | boolean / bool | NO | `true` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored delivery same as event. |
| delivery_address_line1 | character varying / varchar | YES | No SQL default | 200 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored delivery address line1. |
| delivery_city | character varying / varchar | YES | No SQL default | 120 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored delivery city. |
| delivery_state | character varying / varchar | YES | No SQL default | 120 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored delivery state. |
| delivery_postal_code | character varying / varchar | YES | No SQL default | 20 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored delivery postal code. |
| delivery_country | character varying / varchar | YES | No SQL default | 80 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored delivery country. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| ck_event_fulfillment_default | c | `CHECK (((fulfillment_default)::text = ANY ((ARRAY['DELIVER_WHEN_FUNDED'::character varying, 'AFTER_EVENT_CONFIRMATION'::character varying])::text[])))` |
| ck_events_qr_theme | c | `CHECK (((qr_theme)::text = ANY ((ARRAY['MODERN_MINIMAL'::character varying, 'ELEGANT_GOLD'::character varying, 'BIRTHDAY_CELEBRATION'::character varying, 'BABY_SHOWER'::character varying, 'ANNIVERSARY'::character varying, 'HOUSEWARMING'::character varying])::text[])))` |
| events_host_display_name_valid | c | `CHECK (((length(btrim((host_display_name)::text)) >= 1) AND (length(btrim((host_display_name)::text)) <= 200)))` |
| events_pkey | p | `PRIMARY KEY (id)` |
| fk_events_owner | f | `FOREIGN KEY (owner_id) REFERENCES users(id)` |
| uq_events_public_token | u | `UNIQUE (public_token)` |
| ux_events_id_owner | u | `UNIQUE (id, owner_id)` |
| valid_delivery_address | c | `CHECK ((delivery_same_as_event OR ((NULLIF(TRIM(BOTH FROM delivery_address_line1), ''::text) IS NOT NULL) AND (NULLIF(TRIM(BOTH FROM delivery_city), ''::text) IS NOT NULL) AND (NULLIF(TRIM(BOTH FROM delivery_state), ''::text) IS NOT NULL) AND (NULLIF(TRIM(BOTH FROM delivery_postal_code), ''::text) IS NOT NULL) AND (NULLIF(TRIM(BOTH FROM delivery_country), ''::text) IS NOT NULL))))` |


### Indexes

| Index | Definition |
| --- | --- |
| events_pkey | `CREATE UNIQUE INDEX events_pkey ON public.events USING btree (id)` |
| idx_events_event_date | `CREATE INDEX idx_events_event_date ON public.events USING btree (event_date)` |
| idx_events_owner_id | `CREATE INDEX idx_events_owner_id ON public.events USING btree (owner_id)` |
| idx_events_status | `CREATE INDEX idx_events_status ON public.events USING btree (status)` |
| uq_events_public_token | `CREATE UNIQUE INDEX uq_events_public_token ON public.events USING btree (public_token)` |
| ux_events_id_owner | `CREATE UNIQUE INDEX ux_events_id_owner ON public.events USING btree (id, owner_id)` |


Consumers: [backend/src/main/java/com/giftplatform/admin/AccountPolicyController.java:1](../../src/main/java/com/giftplatform/admin/AccountPolicyController.java#L1); [backend/src/main/java/com/giftplatform/admin/AiInvitationAdminController.java:1](../../src/main/java/com/giftplatform/admin/AiInvitationAdminController.java#L1); [backend/src/main/java/com/giftplatform/admin/EffectiveModulePermissionService.java:1](../../src/main/java/com/giftplatform/admin/EffectiveModulePermissionService.java#L1); [backend/src/main/java/com/giftplatform/admin/ExperienceSettingsController.java:1](../../src/main/java/com/giftplatform/admin/ExperienceSettingsController.java#L1); [backend/src/main/java/com/giftplatform/admin/GroupModuleInterceptor.java:1](../../src/main/java/com/giftplatform/admin/GroupModuleInterceptor.java#L1); [backend/src/main/java/com/giftplatform/admin/ModulePrivilegeWebConfiguration.java:1](../../src/main/java/com/giftplatform/admin/ModulePrivilegeWebConfiguration.java#L1); [backend/src/main/java/com/giftplatform/admin/PlatformAdminConsoleController.java:1](../../src/main/java/com/giftplatform/admin/PlatformAdminConsoleController.java#L1); [backend/src/main/java/com/giftplatform/celebration/CelebrationController.java:1](../../src/main/java/com/giftplatform/celebration/CelebrationController.java#L1); [backend/src/main/java/com/giftplatform/celebration/GuestbookPhotoController.java:1](../../src/main/java/com/giftplatform/celebration/GuestbookPhotoController.java#L1); [backend/src/main/java/com/giftplatform/celebration/MemoryAlbumController.java:1](../../src/main/java/com/giftplatform/celebration/MemoryAlbumController.java#L1); [backend/src/main/java/com/giftplatform/event/Controller/EventController.java:1](../../src/main/java/com/giftplatform/event/Controller/EventController.java#L1); [backend/src/main/java/com/giftplatform/event/entity/Event.java:1](../../src/main/java/com/giftplatform/event/entity/Event.java#L1); [backend/src/main/java/com/giftplatform/gift/controller/GiftController.java:1](../../src/main/java/com/giftplatform/gift/controller/GiftController.java#L1); [backend/src/main/java/com/giftplatform/gift/photo/GiftPhotoController.java:1](../../src/main/java/com/giftplatform/gift/photo/GiftPhotoController.java#L1); [backend/src/main/java/com/giftplatform/gift/photo/GiftPhotoService.java:1](../../src/main/java/com/giftplatform/gift/photo/GiftPhotoService.java#L1); [backend/src/main/java/com/giftplatform/gift/retailer/RetailerMetadataController.java:1](../../src/main/java/com/giftplatform/gift/retailer/RetailerMetadataController.java#L1); [backend/src/main/java/com/giftplatform/gift/retailer/RetailerMetadataService.java:1](../../src/main/java/com/giftplatform/gift/retailer/RetailerMetadataService.java#L1); [backend/src/main/java/com/giftplatform/gift/service/GiftManagementService.java:1](../../src/main/java/com/giftplatform/gift/service/GiftManagementService.java#L1); [backend/src/main/java/com/giftplatform/group/GroupContributionController.java:1](../../src/main/java/com/giftplatform/group/GroupContributionController.java#L1); [backend/src/main/java/com/giftplatform/group/GroupGiftCollectionController.java:1](../../src/main/java/com/giftplatform/group/GroupGiftCollectionController.java#L1); [backend/src/main/java/com/giftplatform/group/GroupGiftCompletionController.java:1](../../src/main/java/com/giftplatform/group/GroupGiftCompletionController.java#L1); [backend/src/main/java/com/giftplatform/group/GroupGuestContributionController.java:1](../../src/main/java/com/giftplatform/group/GroupGuestContributionController.java#L1); [backend/src/main/java/com/giftplatform/group/GroupReadinessService.java:1](../../src/main/java/com/giftplatform/group/GroupReadinessService.java#L1); [backend/src/main/java/com/giftplatform/group/GuestGiftController.java:1](../../src/main/java/com/giftplatform/group/GuestGiftController.java#L1); [backend/src/main/java/com/giftplatform/group/IndividualGuestGiftController.java:1](../../src/main/java/com/giftplatform/group/IndividualGuestGiftController.java#L1); [backend/src/main/java/com/giftplatform/hostpayment/controller/ReceivingAccountHealthController.java:1](../../src/main/java/com/giftplatform/hostpayment/controller/ReceivingAccountHealthController.java#L1); [backend/src/main/java/com/giftplatform/hostpayment/service/EventAccountLinkService.java:1](../../src/main/java/com/giftplatform/hostpayment/service/EventAccountLinkService.java#L1); [backend/src/main/java/com/giftplatform/hostpayment/service/HostPaymentProfileService.java:1](../../src/main/java/com/giftplatform/hostpayment/service/HostPaymentProfileService.java#L1); [backend/src/main/java/com/giftplatform/monetization/CommercialAccessService.java:1](../../src/main/java/com/giftplatform/monetization/CommercialAccessService.java#L1); [backend/src/main/java/com/giftplatform/monetization/CommercialController.java:1](../../src/main/java/com/giftplatform/monetization/CommercialController.java#L1); [backend/src/main/java/com/giftplatform/monetization/CommercialPublicService.java:1](../../src/main/java/com/giftplatform/monetization/CommercialPublicService.java#L1); [backend/src/main/java/com/giftplatform/notification/HostInboxController.java:1](../../src/main/java/com/giftplatform/notification/HostInboxController.java#L1); [backend/src/main/java/com/giftplatform/notification/HostThankYouController.java:1](../../src/main/java/com/giftplatform/notification/HostThankYouController.java#L1); [backend/src/main/java/com/giftplatform/notification/HostThankYouService.java:1](../../src/main/java/com/giftplatform/notification/HostThankYouService.java#L1); [backend/src/main/java/com/giftplatform/notification/ManualClaimNotificationService.java:1](../../src/main/java/com/giftplatform/notification/ManualClaimNotificationService.java#L1); [backend/src/main/java/com/giftplatform/payment/control/PaymentControlController.java:1](../../src/main/java/com/giftplatform/payment/control/PaymentControlController.java#L1); [backend/src/main/java/com/giftplatform/payment/control/PaymentControlService.java:1](../../src/main/java/com/giftplatform/payment/control/PaymentControlService.java#L1); [backend/src/main/java/com/giftplatform/payment/controller/PublicGiftProgressController.java:1](../../src/main/java/com/giftplatform/payment/controller/PublicGiftProgressController.java#L1); [backend/src/main/java/com/giftplatform/payment/controller/PublicPaymentContextController.java:1](../../src/main/java/com/giftplatform/payment/controller/PublicPaymentContextController.java#L1); [backend/src/main/java/com/giftplatform/payment/direct/GuestDirectTransferService.java:1](../../src/main/java/com/giftplatform/payment/direct/GuestDirectTransferService.java#L1); [backend/src/main/java/com/giftplatform/payment/direct/GuestVerifiedDestinationService.java:1](../../src/main/java/com/giftplatform/payment/direct/GuestVerifiedDestinationService.java#L1); [backend/src/main/java/com/giftplatform/payment/hybrid/HybridCheckoutQuoteService.java:1](../../src/main/java/com/giftplatform/payment/hybrid/HybridCheckoutQuoteService.java#L1); [backend/src/main/java/com/giftplatform/payment/report/HostContributionReportController.java:1](../../src/main/java/com/giftplatform/payment/report/HostContributionReportController.java#L1); [backend/src/main/java/com/giftplatform/payment/report/HostContributionReportService.java:1](../../src/main/java/com/giftplatform/payment/report/HostContributionReportService.java#L1); [backend/src/main/java/com/giftplatform/payment/service/PaymentOrderService.java:1](../../src/main/java/com/giftplatform/payment/service/PaymentOrderService.java#L1); [backend/src/main/java/com/giftplatform/payment/service/RazorpayWebhookService.java:1](../../src/main/java/com/giftplatform/payment/service/RazorpayWebhookService.java#L1); [backend/src/main/java/com/giftplatform/payment/settlement/HostPayoutRequestService.java:1](../../src/main/java/com/giftplatform/payment/settlement/HostPayoutRequestService.java#L1); [backend/src/main/java/com/giftplatform/payment/settlement/HostSettlementUiController.java:1](../../src/main/java/com/giftplatform/payment/settlement/HostSettlementUiController.java#L1); [backend/src/main/java/com/giftplatform/payment/settlement/HostSettlementUiService.java:1](../../src/main/java/com/giftplatform/payment/settlement/HostSettlementUiService.java#L1); [backend/src/main/java/com/giftplatform/payment/verification/HostTransactionVerificationController.java:1](../../src/main/java/com/giftplatform/payment/verification/HostTransactionVerificationController.java#L1); [backend/src/main/java/com/giftplatform/publicevent/PublicEventController.java:1](../../src/main/java/com/giftplatform/publicevent/PublicEventController.java#L1); [backend/src/main/java/com/giftplatform/qr/EventQrController.java:1](../../src/main/java/com/giftplatform/qr/EventQrController.java#L1); [backend/src/main/java/com/giftplatform/qr/EventQrService.java:1](../../src/main/java/com/giftplatform/qr/EventQrService.java#L1); [backend/src/main/java/com/giftplatform/qr/design/InvitationDesignController.java:1](../../src/main/java/com/giftplatform/qr/design/InvitationDesignController.java#L1); [backend/src/main/java/com/giftplatform/qr/design/InvitationDesignService.java:1](../../src/main/java/com/giftplatform/qr/design/InvitationDesignService.java#L1); [backend/src/main/java/com/giftplatform/qr/design/ai/AiInvitationController.java:1](../../src/main/java/com/giftplatform/qr/design/ai/AiInvitationController.java#L1); [backend/src/main/java/com/giftplatform/qr/design/ai/AiInvitationOptionsController.java:1](../../src/main/java/com/giftplatform/qr/design/ai/AiInvitationOptionsController.java#L1); [backend/src/main/java/com/giftplatform/qr/design/ai/AiInvitationService.java:1](../../src/main/java/com/giftplatform/qr/design/ai/AiInvitationService.java#L1); [backend/src/main/java/com/giftplatform/security/SecurityConfig.java:1](../../src/main/java/com/giftplatform/security/SecurityConfig.java#L1); [backend/src/main/java/com/giftplatform/security/recovery/EffectiveRecoveryPolicyService.java:1](../../src/main/java/com/giftplatform/security/recovery/EffectiveRecoveryPolicyService.java#L1); [backend/src/main/java/com/giftplatform/security/recovery/EmailRecoveryController.java:1](../../src/main/java/com/giftplatform/security/recovery/EmailRecoveryController.java#L1); [backend/src/main/java/com/giftplatform/security/recovery/GroupEmailEnrollmentController.java:1](../../src/main/java/com/giftplatform/security/recovery/GroupEmailEnrollmentController.java#L1); [backend/src/main/java/com/giftplatform/security/recovery/GroupMobileEnrollmentController.java:1](../../src/main/java/com/giftplatform/security/recovery/GroupMobileEnrollmentController.java#L1); [backend/src/main/java/com/giftplatform/security/recovery/SignupEmailVerificationService.java:1](../../src/main/java/com/giftplatform/security/recovery/SignupEmailVerificationService.java#L1); [backend/src/main/java/com/giftplatform/security/recovery/UsernameRecoveryController.java:1](../../src/main/java/com/giftplatform/security/recovery/UsernameRecoveryController.java#L1)

## flyway_schema_history

Type: **BASE TABLE**. Purpose: Flyway infrastructure: migration execution and checksum history.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| installed_rank | integer / int4 | NO | No SQL default | None / 32 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored installed rank. |
| version | character varying / varchar | YES | No SQL default | 50 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored version. |
| description | character varying / varchar | NO | No SQL default | 200 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored description. |
| type | character varying / varchar | NO | No SQL default | 20 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored type. |
| script | character varying / varchar | NO | No SQL default | 1000 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored script. |
| checksum | integer / int4 | YES | No SQL default | None / 32 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored checksum. |
| installed_by | character varying / varchar | NO | No SQL default | 100 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored installed by. |
| installed_on | timestamp without time zone / timestamp | NO | `now()` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored installed on. |
| execution_time | integer / int4 | NO | No SQL default | None / 32 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored execution time. |
| success | boolean / bool | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored success. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| flyway_schema_history_pk | p | `PRIMARY KEY (installed_rank)` |


### Indexes

| Index | Definition |
| --- | --- |
| flyway_schema_history_pk | `CREATE UNIQUE INDEX flyway_schema_history_pk ON public.flyway_schema_history USING btree (installed_rank)` |
| flyway_schema_history_s_idx | `CREATE INDEX flyway_schema_history_s_idx ON public.flyway_schema_history USING btree (success)` |


Consumers: No direct Java literal reference found; migrations, views, triggers or indirect table selection may be the consumer.

## gift_group_accounts

Type: **BASE TABLE**. Purpose: Invitation/group/individual gift coordination, contribution reporting or private gift progress. Monetary reporting is distinct from verified settlement.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| event_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| username | character varying / varchar | NO | No SQL default | 40 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored username. |
| password_hash | character varying / varchar | NO | No SQL default | 100 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Sensitive representation; do not expose plaintext or hashes. |
| failed_attempts | integer / int4 | NO | `0` | None / 32 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored failed attempts. |
| locked_until | timestamp with time zone / timestamptz | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored locked until. |
| created_at | timestamp with time zone / timestamptz | NO | `now()` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored created at. |
| pin_hash | character varying / varchar | YES | No SQL default | 100 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Sensitive representation; do not expose plaintext or hashes. |
| full_name | character varying / varchar | YES | No SQL default | 120 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored full name. |
| email | character varying / varchar | YES | No SQL default | 254 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored email. |
| mobile | character varying / varchar | YES | No SQL default | 16 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored mobile. |
| email_verified_at | timestamp with time zone / timestamptz | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored email verified at. |
| mobile_verified_at | timestamp with time zone / timestamptz | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored mobile verified at. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| gift_group_accounts_event_id_fkey | f | `FOREIGN KEY (event_id) REFERENCES events(id)` |
| gift_group_accounts_pkey | p | `PRIMARY KEY (id)` |
| gift_group_username_lower | c | `CHECK (((username)::text = lower((username)::text)))` |
| gift_group_username_unique | u | `UNIQUE (event_id, username)` |


### Indexes

| Index | Definition |
| --- | --- |
| gift_group_accounts_pkey | `CREATE UNIQUE INDEX gift_group_accounts_pkey ON public.gift_group_accounts USING btree (id)` |
| gift_group_username_unique | `CREATE UNIQUE INDEX gift_group_username_unique ON public.gift_group_accounts USING btree (event_id, username)` |


Consumers: [backend/src/main/java/com/giftplatform/group/GroupContributionController.java:1](../../src/main/java/com/giftplatform/group/GroupContributionController.java#L1); [backend/src/main/java/com/giftplatform/group/GroupGiftCollectionController.java:1](../../src/main/java/com/giftplatform/group/GroupGiftCollectionController.java#L1); [backend/src/main/java/com/giftplatform/group/GroupGiftLifecycleService.java:1](../../src/main/java/com/giftplatform/group/GroupGiftLifecycleService.java#L1); [backend/src/main/java/com/giftplatform/group/GroupReadinessService.java:1](../../src/main/java/com/giftplatform/group/GroupReadinessService.java#L1); [backend/src/main/java/com/giftplatform/group/GuestGiftController.java:1](../../src/main/java/com/giftplatform/group/GuestGiftController.java#L1); [backend/src/main/java/com/giftplatform/group/IndividualGuestGiftController.java:1](../../src/main/java/com/giftplatform/group/IndividualGuestGiftController.java#L1); [backend/src/main/java/com/giftplatform/security/recovery/EmailRecoveryController.java:1](../../src/main/java/com/giftplatform/security/recovery/EmailRecoveryController.java#L1); [backend/src/main/java/com/giftplatform/security/recovery/GroupEmailEnrollmentController.java:1](../../src/main/java/com/giftplatform/security/recovery/GroupEmailEnrollmentController.java#L1); [backend/src/main/java/com/giftplatform/security/recovery/GroupMobileEnrollmentController.java:1](../../src/main/java/com/giftplatform/security/recovery/GroupMobileEnrollmentController.java#L1); [backend/src/main/java/com/giftplatform/security/recovery/UsernameRecoveryController.java:1](../../src/main/java/com/giftplatform/security/recovery/UsernameRecoveryController.java#L1); [backend/src/main/java/com/giftplatform/security/verification/MobileVerificationService.java:1](../../src/main/java/com/giftplatform/security/verification/MobileVerificationService.java#L1)

## gift_group_activity

Type: **BASE TABLE**. Purpose: Invitation/group/individual gift coordination, contribution reporting or private gift progress. Monetary reporting is distinct from verified settlement.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| id | uuid / uuid | NO | `gen_random_uuid()` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| group_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| actor_id | uuid / uuid | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| action | character varying / varchar | NO | No SQL default | 60 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored action. |
| details | jsonb / jsonb | NO | `'{}'::jsonb` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored details. |
| created_at | timestamp with time zone / timestamptz | NO | `now()` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored created at. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| gift_group_activity_group_id_fkey | f | `FOREIGN KEY (group_id) REFERENCES gift_groups(id)` |
| gift_group_activity_pkey | p | `PRIMARY KEY (id)` |


### Indexes

| Index | Definition |
| --- | --- |
| gift_group_activity_history | `CREATE INDEX gift_group_activity_history ON public.gift_group_activity USING btree (group_id, created_at, id)` |
| gift_group_activity_pkey | `CREATE UNIQUE INDEX gift_group_activity_pkey ON public.gift_group_activity USING btree (id)` |


Consumers: [backend/src/main/java/com/giftplatform/group/GroupGiftCollectionController.java:1](../../src/main/java/com/giftplatform/group/GroupGiftCollectionController.java#L1)

## gift_group_destination_verification_attempts

Type: **BASE TABLE**. Purpose: Invitation/group/individual gift coordination, contribution reporting or private gift progress. Monetary reporting is distinct from verified settlement.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| group_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| destination_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| requested_by | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored requested by. |
| provider | character varying / varchar | NO | No SQL default | 24 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored provider. |
| environment | character varying / varchar | NO | No SQL default | 12 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored environment. |
| request_reference | character varying / varchar | NO | No SQL default | 120 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored request reference. |
| provider_reference | character varying / varchar | YES | No SQL default | 120 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored provider reference. |
| state | character varying / varchar | NO | `'PENDING'::character varying` | 20 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. State value; see CHECK constraints. |
| created_at | timestamp with time zone / timestamptz | NO | `now()` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored created at. |
| completed_at | timestamp with time zone / timestamptz | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored completed at. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| gift_group_destination_verification_attem_request_reference_key | u | `UNIQUE (request_reference)` |
| gift_group_destination_verification_attempt_destination_id_fkey | f | `FOREIGN KEY (destination_id) REFERENCES gift_group_payment_destinations(id) ON DELETE CASCADE` |
| gift_group_destination_verification_attempts_environment_check | c | `CHECK (((environment)::text = ANY ((ARRAY['SANDBOX'::character varying, 'PRODUCTION'::character varying])::text[])))` |
| gift_group_destination_verification_attempts_group_id_fkey | f | `FOREIGN KEY (group_id) REFERENCES gift_groups(id) ON DELETE CASCADE` |
| gift_group_destination_verification_attempts_pkey | p | `PRIMARY KEY (id)` |
| gift_group_destination_verification_attempts_provider_check | c | `CHECK (((provider)::text = ANY ((ARRAY['CASHFREE'::character varying, 'RAZORPAYX'::character varying])::text[])))` |
| gift_group_destination_verification_attempts_requested_by_fkey | f | `FOREIGN KEY (requested_by) REFERENCES users(id)` |
| gift_group_destination_verification_attempts_state_check | c | `CHECK (((state)::text = ANY ((ARRAY['PENDING'::character varying, 'REJECTED'::character varying, 'UNAVAILABLE'::character varying, 'VERIFIED'::character varying])::text[])))` |
| group_verification_completion | c | `CHECK (((((state)::text = 'PENDING'::text) AND (completed_at IS NULL)) OR (((state)::text <> 'PENDING'::text) AND (completed_at IS NOT NULL))))` |
| group_verification_verified_evidence | c | `CHECK ((((state)::text <> 'VERIFIED'::text) OR (((environment)::text = 'PRODUCTION'::text) AND (provider_reference IS NOT NULL) AND (length(TRIM(BOTH FROM provider_reference)) > 0))))` |


### Indexes

| Index | Definition |
| --- | --- |
| gift_group_destination_verification_attem_request_reference_key | `CREATE UNIQUE INDEX gift_group_destination_verification_attem_request_reference_key ON public.gift_group_destination_verification_attempts USING btree (request_reference)` |
| gift_group_destination_verification_attempts_pkey | `CREATE UNIQUE INDEX gift_group_destination_verification_attempts_pkey ON public.gift_group_destination_verification_attempts USING btree (id)` |
| group_destination_one_pending_verification_idx | `CREATE UNIQUE INDEX group_destination_one_pending_verification_idx ON public.gift_group_destination_verification_attempts USING btree (destination_id) WHERE ((state)::text = 'PENDING'::text)` |
| group_destination_verification_history_idx | `CREATE INDEX group_destination_verification_history_idx ON public.gift_group_destination_verification_attempts USING btree (group_id, destination_id, created_at DESC)` |
| group_destination_verification_provider_reference_idx | `CREATE UNIQUE INDEX group_destination_verification_provider_reference_idx ON public.gift_group_destination_verification_attempts USING btree (provider, environment, provider_reference) WHERE (provider_reference IS NOT NULL)` |


Consumers: [backend/src/main/java/com/giftplatform/hostpayment/verification/cashfree/CashfreeVerificationAttemptService.java:1](../../src/main/java/com/giftplatform/hostpayment/verification/cashfree/CashfreeVerificationAttemptService.java#L1)

## gift_group_guest_reports

Type: **BASE TABLE**. Purpose: Invitation/group/individual gift coordination, contribution reporting or private gift progress. Monetary reporting is distinct from verified settlement.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| group_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| destination_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| guest_name | character varying / varchar | NO | No SQL default | 120 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored guest name. |
| guest_contact | character varying / varchar | YES | No SQL default | 120 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored guest contact. |
| amount_paise | bigint / int8 | NO | No SQL default | None / 64 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Money in integer paise; preserve distinction between reported, confirmed and settled. |
| method | character varying / varchar | NO | No SQL default | 8 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored method. |
| transaction_reference | character varying / varchar | NO | No SQL default | 120 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored transaction reference. |
| submission_key | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored submission key. |
| state | character varying / varchar | NO | `'REPORTED_UNVERIFIED'::character varying` | 32 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. State value; see CHECK constraints. |
| created_at | timestamp with time zone / timestamptz | NO | `now()` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored created at. |
| member_id | uuid / uuid | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| creator_admin_id | uuid / uuid | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| gift_id_snapshot | uuid / uuid | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored gift id snapshot. |
| creator_received_at | timestamp with time zone / timestamptz | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored creator received at. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| fk_group_report_creator | f | `FOREIGN KEY (group_id, creator_admin_id) REFERENCES gift_groups(id, admin_id)` |
| gift_group_guest_reports_amount_paise_check | c | `CHECK (((amount_paise >= 100) AND (amount_paise <= 1000000000)))` |
| gift_group_guest_reports_creator_admin_id_fkey | f | `FOREIGN KEY (creator_admin_id) REFERENCES gift_group_accounts(id)` |
| gift_group_guest_reports_destination_id_fkey | f | `FOREIGN KEY (destination_id) REFERENCES gift_group_payment_destinations(id)` |
| gift_group_guest_reports_gift_id_snapshot_fkey | f | `FOREIGN KEY (gift_id_snapshot) REFERENCES gifts(id)` |
| gift_group_guest_reports_group_id_fkey | f | `FOREIGN KEY (group_id) REFERENCES gift_groups(id)` |
| gift_group_guest_reports_group_id_method_transaction_refere_key | u | `UNIQUE (group_id, method, transaction_reference)` |
| gift_group_guest_reports_group_id_submission_key_key | u | `UNIQUE (group_id, submission_key)` |
| gift_group_guest_reports_member_id_fkey | f | `FOREIGN KEY (member_id) REFERENCES gift_group_members(id)` |
| gift_group_guest_reports_method_check | c | `CHECK (((method)::text = ANY ((ARRAY['UPI'::character varying, 'BANK'::character varying])::text[])))` |
| gift_group_guest_reports_pkey | p | `PRIMARY KEY (id)` |
| gift_group_guest_reports_state_check | c | `CHECK (((state)::text = ANY ((ARRAY['REPORTED_UNVERIFIED'::character varying, 'VERIFIED'::character varying, 'REJECTED'::character varying])::text[])))` |
| group_report_contributor_identity | c | `CHECK (((member_id IS NULL) OR (creator_admin_id IS NULL)))` |


### Indexes

| Index | Definition |
| --- | --- |
| gift_group_guest_reports_group_date_idx | `CREATE INDEX gift_group_guest_reports_group_date_idx ON public.gift_group_guest_reports USING btree (group_id, created_at DESC)` |
| gift_group_guest_reports_group_id_method_transaction_refere_key | `CREATE UNIQUE INDEX gift_group_guest_reports_group_id_method_transaction_refere_key ON public.gift_group_guest_reports USING btree (group_id, method, transaction_reference)` |
| gift_group_guest_reports_group_id_submission_key_key | `CREATE UNIQUE INDEX gift_group_guest_reports_group_id_submission_key_key ON public.gift_group_guest_reports USING btree (group_id, submission_key)` |
| gift_group_guest_reports_pkey | `CREATE UNIQUE INDEX gift_group_guest_reports_pkey ON public.gift_group_guest_reports USING btree (id)` |
| gift_group_reports_member_history_idx | `CREATE INDEX gift_group_reports_member_history_idx ON public.gift_group_guest_reports USING btree (member_id, created_at DESC) WHERE (member_id IS NOT NULL)` |
| group_reports_creator_idx | `CREATE INDEX group_reports_creator_idx ON public.gift_group_guest_reports USING btree (group_id, creator_admin_id, created_at DESC) WHERE (creator_admin_id IS NOT NULL)` |


Consumers: [backend/src/main/java/com/giftplatform/admin/PlatformAdminConsoleController.java:1](../../src/main/java/com/giftplatform/admin/PlatformAdminConsoleController.java#L1); [backend/src/main/java/com/giftplatform/group/GroupGiftCompletionController.java:1](../../src/main/java/com/giftplatform/group/GroupGiftCompletionController.java#L1); [backend/src/main/java/com/giftplatform/group/GroupGuestContributionController.java:1](../../src/main/java/com/giftplatform/group/GroupGuestContributionController.java#L1)

## gift_group_invitations

Type: **BASE TABLE**. Purpose: Invitation/group/individual gift coordination, contribution reporting or private gift progress. Monetary reporting is distinct from verified settlement.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| group_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| token_hash | character varying / varchar | NO | No SQL default | 64 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Sensitive representation; do not expose plaintext or hashes. |
| expires_at | timestamp with time zone / timestamptz | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored expires at. |
| revoked_at | timestamp with time zone / timestamptz | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored revoked at. |
| max_uses | integer / int4 | NO | `1` | None / 32 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored max uses. |
| used_count | integer / int4 | NO | `0` | None / 32 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored used count. |
| created_at | timestamp with time zone / timestamptz | NO | `now()` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored created at. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| gift_group_invitations_group_id_fkey | f | `FOREIGN KEY (group_id) REFERENCES gift_groups(id)` |
| gift_group_invitations_max_uses_check | c | `CHECK (((max_uses >= 1) AND (max_uses <= 100)))` |
| gift_group_invitations_pkey | p | `PRIMARY KEY (id)` |
| gift_group_invitations_token_hash_key | u | `UNIQUE (token_hash)` |
| gift_group_invitations_used_count_check | c | `CHECK ((used_count >= 0))` |


### Indexes

| Index | Definition |
| --- | --- |
| gift_group_invitations_group_idx | `CREATE INDEX gift_group_invitations_group_idx ON public.gift_group_invitations USING btree (group_id)` |
| gift_group_invitations_pkey | `CREATE UNIQUE INDEX gift_group_invitations_pkey ON public.gift_group_invitations USING btree (id)` |
| gift_group_invitations_token_hash_key | `CREATE UNIQUE INDEX gift_group_invitations_token_hash_key ON public.gift_group_invitations USING btree (token_hash)` |


Consumers: No direct Java literal reference found; migrations, views, triggers or indirect table selection may be the consumer.

## gift_group_members

Type: **BASE TABLE**. Purpose: Invitation/group/individual gift coordination, contribution reporting or private gift progress. Monetary reporting is distinct from verified settlement.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| group_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| display_name | character varying / varchar | NO | No SQL default | 120 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored display name. |
| username | character varying / varchar | NO | No SQL default | 40 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored username. |
| password_hash | character varying / varchar | NO | No SQL default | 100 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Sensitive representation; do not expose plaintext or hashes. |
| status | character varying / varchar | NO | `'PENDING'::character varying` | 16 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. State value; see CHECK constraints. |
| created_at | timestamp with time zone / timestamptz | NO | `now()` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored created at. |
| reviewed_at | timestamp with time zone / timestamptz | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored reviewed at. |
| mobile | character varying / varchar | YES | No SQL default | 20 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored mobile. |
| pin_hash | character varying / varchar | YES | No SQL default | 100 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Sensitive representation; do not expose plaintext or hashes. |
| email | character varying / varchar | YES | No SQL default | 254 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored email. |
| email_verified_at | timestamp with time zone / timestamptz | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored email verified at. |
| mobile_verified_at | timestamp with time zone / timestamptz | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored mobile verified at. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| gift_group_members_group_id_fkey | f | `FOREIGN KEY (group_id) REFERENCES gift_groups(id)` |
| gift_group_members_group_id_username_key | u | `UNIQUE (group_id, username)` |
| gift_group_members_pkey | p | `PRIMARY KEY (id)` |
| gift_group_members_status_check | c | `CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'APPROVED'::character varying, 'REJECTED'::character varying, 'REMOVED'::character varying])::text[])))` |
| gift_group_members_username_check | c | `CHECK (((username)::text = lower((username)::text)))` |


### Indexes

| Index | Definition |
| --- | --- |
| gift_group_members_group_id_username_key | `CREATE UNIQUE INDEX gift_group_members_group_id_username_key ON public.gift_group_members USING btree (group_id, username)` |
| gift_group_members_pkey | `CREATE UNIQUE INDEX gift_group_members_pkey ON public.gift_group_members USING btree (id)` |
| gift_group_members_status_idx | `CREATE INDEX gift_group_members_status_idx ON public.gift_group_members USING btree (group_id, status)` |
| gift_group_members_unique_name_ci | `CREATE UNIQUE INDEX gift_group_members_unique_name_ci ON public.gift_group_members USING btree (group_id, lower((display_name)::text))` |


Consumers: [backend/src/main/java/com/giftplatform/group/GroupContributionController.java:1](../../src/main/java/com/giftplatform/group/GroupContributionController.java#L1); [backend/src/main/java/com/giftplatform/group/GroupGuestContributionController.java:1](../../src/main/java/com/giftplatform/group/GroupGuestContributionController.java#L1); [backend/src/main/java/com/giftplatform/security/recovery/EmailRecoveryController.java:1](../../src/main/java/com/giftplatform/security/recovery/EmailRecoveryController.java#L1); [backend/src/main/java/com/giftplatform/security/recovery/GroupEmailEnrollmentController.java:1](../../src/main/java/com/giftplatform/security/recovery/GroupEmailEnrollmentController.java#L1); [backend/src/main/java/com/giftplatform/security/recovery/GroupMobileEnrollmentController.java:1](../../src/main/java/com/giftplatform/security/recovery/GroupMobileEnrollmentController.java#L1); [backend/src/main/java/com/giftplatform/security/recovery/UsernameRecoveryController.java:1](../../src/main/java/com/giftplatform/security/recovery/UsernameRecoveryController.java#L1); [backend/src/main/java/com/giftplatform/security/verification/MobileVerificationService.java:1](../../src/main/java/com/giftplatform/security/verification/MobileVerificationService.java#L1)

## gift_group_payment_audit

Type: **BASE TABLE**. Purpose: Invitation/group/individual gift coordination, contribution reporting or private gift progress. Monetary reporting is distinct from verified settlement.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| group_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| actor_type | character varying / varchar | NO | No SQL default | 20 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored actor type. |
| actor_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| action | character varying / varchar | NO | No SQL default | 40 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored action. |
| destination_id | uuid / uuid | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| created_at | timestamp with time zone / timestamptz | NO | `now()` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored created at. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| gift_group_payment_audit_group_id_fkey | f | `FOREIGN KEY (group_id) REFERENCES gift_groups(id)` |
| gift_group_payment_audit_pkey | p | `PRIMARY KEY (id)` |


### Indexes

| Index | Definition |
| --- | --- |
| gift_group_payment_audit_pkey | `CREATE UNIQUE INDEX gift_group_payment_audit_pkey ON public.gift_group_payment_audit USING btree (id)` |


Consumers: [backend/src/main/java/com/giftplatform/group/GroupCreatorPaymentController.java:1](../../src/main/java/com/giftplatform/group/GroupCreatorPaymentController.java#L1)

## gift_group_payment_destinations

Type: **BASE TABLE**. Purpose: Invitation/group/individual gift coordination, contribution reporting or private gift progress. Monetary reporting is distinct from verified settlement.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| group_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| kind | character varying / varchar | NO | No SQL default | 8 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored kind. |
| label | character varying / varchar | NO | No SQL default | 80 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored label. |
| upi_ciphertext | text / text | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Sensitive representation; do not expose plaintext or hashes. |
| upi_fingerprint | character varying / varchar | YES | No SQL default | 64 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored upi fingerprint. |
| account_ciphertext | text / text | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Sensitive representation; do not expose plaintext or hashes. |
| account_fingerprint | character varying / varchar | YES | No SQL default | 64 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored account fingerprint. |
| ifsc_ciphertext | text / text | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Sensitive representation; do not expose plaintext or hashes. |
| holder_ciphertext | text / text | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Sensitive representation; do not expose plaintext or hashes. |
| status | character varying / varchar | NO | `'REGISTERED'::character varying` | 24 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. State value; see CHECK constraints. |
| preferred | boolean / bool | NO | `false` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored preferred. |
| created_at | timestamp with time zone / timestamptz | NO | `now()` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored created at. |
| enabled | boolean / bool | NO | `true` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored enabled. |
| qr_image | bytea / bytea | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored qr image. |
| qr_source | character varying / varchar | YES | No SQL default | 12 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored qr source. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| gift_group_destination_fields | c | `CHECK (((((kind)::text = 'UPI'::text) AND (upi_ciphertext IS NOT NULL) AND (upi_fingerprint IS NOT NULL) AND (account_ciphertext IS NULL)) OR (((kind)::text = 'BANK'::text) AND (account_ciphertext IS NOT NULL) AND (account_fingerprint IS NOT NULL) AND (ifsc_ciphertext IS NOT NULL) AND (holder_ciphertext IS NOT NULL) AND (upi_ciphertext IS NULL))))` |
| gift_group_payment_destinations_group_id_fkey | f | `FOREIGN KEY (group_id) REFERENCES gift_groups(id) ON DELETE CASCADE` |
| gift_group_payment_destinations_kind_check | c | `CHECK (((kind)::text = ANY ((ARRAY['UPI'::character varying, 'BANK'::character varying])::text[])))` |
| gift_group_payment_destinations_pkey | p | `PRIMARY KEY (id)` |
| gift_group_payment_destinations_qr_source_check | c | `CHECK (((qr_source)::text = ANY ((ARRAY['UPLOADED'::character varying, 'GENERATED'::character varying])::text[])))` |
| gift_group_payment_destinations_status_check | c | `CHECK (((status)::text = ANY ((ARRAY['REGISTERED'::character varying, 'VERIFIED'::character varying, 'REVOKED'::character varying])::text[])))` |
| gift_group_qr_upi_only | c | `CHECK (((qr_image IS NULL) OR ((kind)::text = 'UPI'::text)))` |


### Indexes

| Index | Definition |
| --- | --- |
| gift_group_dest_unique_bank | `CREATE UNIQUE INDEX gift_group_dest_unique_bank ON public.gift_group_payment_destinations USING btree (group_id, account_fingerprint) WHERE ((account_fingerprint IS NOT NULL) AND ((status)::text <> 'REVOKED'::text))` |
| gift_group_dest_unique_upi | `CREATE UNIQUE INDEX gift_group_dest_unique_upi ON public.gift_group_payment_destinations USING btree (group_id, upi_fingerprint) WHERE ((upi_fingerprint IS NOT NULL) AND ((status)::text <> 'REVOKED'::text))` |
| gift_group_one_preferred_per_kind | `CREATE UNIQUE INDEX gift_group_one_preferred_per_kind ON public.gift_group_payment_destinations USING btree (group_id, kind) WHERE (preferred AND ((status)::text <> 'REVOKED'::text))` |
| gift_group_payment_destinations_pkey | `CREATE UNIQUE INDEX gift_group_payment_destinations_pkey ON public.gift_group_payment_destinations USING btree (id)` |


Consumers: [backend/src/main/java/com/giftplatform/group/GroupCreatorPaymentController.java:1](../../src/main/java/com/giftplatform/group/GroupCreatorPaymentController.java#L1); [backend/src/main/java/com/giftplatform/group/GroupGuestContributionController.java:1](../../src/main/java/com/giftplatform/group/GroupGuestContributionController.java#L1); [backend/src/main/java/com/giftplatform/group/GroupReadinessService.java:1](../../src/main/java/com/giftplatform/group/GroupReadinessService.java#L1); [backend/src/main/java/com/giftplatform/hostpayment/verification/cashfree/CashfreeVerificationAttemptService.java:1](../../src/main/java/com/giftplatform/hostpayment/verification/cashfree/CashfreeVerificationAttemptService.java#L1)

## gift_group_payment_permissions

Type: **BASE TABLE**. Purpose: Invitation/group/individual gift coordination, contribution reporting or private gift progress. Monetary reporting is distinct from verified settlement.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| group_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| switching_enabled | boolean / bool | NO | `false` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored switching enabled. |
| changed_by | uuid / uuid | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored changed by. |
| changed_at | timestamp with time zone / timestamptz | NO | `now()` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored changed at. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| gift_group_payment_permissions_changed_by_fkey | f | `FOREIGN KEY (changed_by) REFERENCES users(id)` |
| gift_group_payment_permissions_group_id_fkey | f | `FOREIGN KEY (group_id) REFERENCES gift_groups(id) ON DELETE CASCADE` |
| gift_group_payment_permissions_pkey | p | `PRIMARY KEY (group_id)` |


### Indexes

| Index | Definition |
| --- | --- |
| gift_group_payment_permissions_pkey | `CREATE UNIQUE INDEX gift_group_payment_permissions_pkey ON public.gift_group_payment_permissions USING btree (group_id)` |


Consumers: [backend/src/main/java/com/giftplatform/group/GroupCreatorPaymentController.java:1](../../src/main/java/com/giftplatform/group/GroupCreatorPaymentController.java#L1)

## gift_group_payment_reports

Type: **BASE TABLE**. Purpose: Invitation/group/individual gift coordination, contribution reporting or private gift progress. Monetary reporting is distinct from verified settlement.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| group_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| member_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| amount_paise | bigint / int8 | NO | No SQL default | None / 64 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Money in integer paise; preserve distinction between reported, confirmed and settled. |
| method | character varying / varchar | NO | No SQL default | 16 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored method. |
| transaction_reference | character varying / varchar | NO | No SQL default | 120 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored transaction reference. |
| state | character varying / varchar | NO | `'REPORTED_UNVERIFIED'::character varying` | 24 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. State value; see CHECK constraints. |
| created_at | timestamp with time zone / timestamptz | NO | `now()` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored created at. |
| verified_at | timestamp with time zone / timestamptz | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored verified at. |
| gift_id_snapshot | uuid / uuid | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored gift id snapshot. |
| creator_received_at | timestamp with time zone / timestamptz | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored creator received at. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| gift_group_payment_reports_amount_paise_check | c | `CHECK ((amount_paise > 0))` |
| gift_group_payment_reports_check | c | `CHECK (((((state)::text = 'VERIFIED'::text) AND (verified_at IS NOT NULL)) OR (((state)::text <> 'VERIFIED'::text) AND (verified_at IS NULL))))` |
| gift_group_payment_reports_gift_id_snapshot_fkey | f | `FOREIGN KEY (gift_id_snapshot) REFERENCES gifts(id)` |
| gift_group_payment_reports_group_id_fkey | f | `FOREIGN KEY (group_id) REFERENCES gift_groups(id)` |
| gift_group_payment_reports_group_id_member_id_method_transa_key | u | `UNIQUE (group_id, member_id, method, transaction_reference)` |
| gift_group_payment_reports_member_id_fkey | f | `FOREIGN KEY (member_id) REFERENCES gift_group_members(id)` |
| gift_group_payment_reports_method_check | c | `CHECK (((method)::text = ANY ((ARRAY['UPI'::character varying, 'BANK'::character varying])::text[])))` |
| gift_group_payment_reports_pkey | p | `PRIMARY KEY (id)` |
| gift_group_payment_reports_state_check | c | `CHECK (((state)::text = ANY ((ARRAY['REPORTED_UNVERIFIED'::character varying, 'VERIFIED'::character varying, 'REJECTED'::character varying])::text[])))` |


### Indexes

| Index | Definition |
| --- | --- |
| gift_group_payment_reports_group_id_member_id_method_transa_key | `CREATE UNIQUE INDEX gift_group_payment_reports_group_id_member_id_method_transa_key ON public.gift_group_payment_reports USING btree (group_id, member_id, method, transaction_reference)` |
| gift_group_payment_reports_group_idx | `CREATE INDEX gift_group_payment_reports_group_idx ON public.gift_group_payment_reports USING btree (group_id, state)` |
| gift_group_payment_reports_pkey | `CREATE UNIQUE INDEX gift_group_payment_reports_pkey ON public.gift_group_payment_reports USING btree (id)` |


Consumers: [backend/src/main/java/com/giftplatform/group/GroupGiftCompletionController.java:1](../../src/main/java/com/giftplatform/group/GroupGiftCompletionController.java#L1)

## gift_groups

Type: **BASE TABLE**. Purpose: Invitation/group/individual gift coordination, contribution reporting or private gift progress. Monetary reporting is distinct from verified settlement.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| event_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| admin_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| name | character varying / varchar | NO | No SQL default | 120 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored name. |
| creator_name | character varying / varchar | NO | No SQL default | 120 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored creator name. |
| creator_mobile | character varying / varchar | NO | No SQL default | 20 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored creator mobile. |
| description | character varying / varchar | YES | No SQL default | 500 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored description. |
| gift_name | character varying / varchar | YES | No SQL default | 160 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored gift name. |
| gift_description | character varying / varchar | YES | No SQL default | 500 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored gift description. |
| gift_link | character varying / varchar | YES | No SQL default | 1000 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored gift link. |
| target_paise | bigint / int8 | YES | No SQL default | None / 64 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Money in integer paise; preserve distinction between reported, confirmed and settled. |
| status | character varying / varchar | NO | `'DRAFT'::character varying` | 20 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. State value; see CHECK constraints. |
| created_at | timestamp with time zone / timestamptz | NO | `now()` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored created at. |
| join_policy | character varying / varchar | NO | `'APPROVAL'::character varying` | 20 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored join policy. |
| member_visibility | character varying / varchar | NO | `'ADMIN_ONLY'::character varying` | 20 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored member visibility. |
| amount_visibility | character varying / varchar | NO | `'ADMIN_ONLY'::character varying` | 20 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored amount visibility. |
| creator_direct_payments_enabled | boolean / bool | NO | `false` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored creator direct payments enabled. |
| suggested_contribution_paise | bigint / int8 | YES | No SQL default | None / 64 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Money in integer paise; preserve distinction between reported, confirmed and settled. |
| manual_contributions_enabled | boolean / bool | NO | `false` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored manual contributions enabled. |
| guest_upi_destination_id | uuid / uuid | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| guest_bank_destination_id | uuid / uuid | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| contribution_access_mode | character varying / varchar | NO | `'BOTH'::character varying` | 16 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored contribution access mode. |
| reserved_gift_id | uuid / uuid | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| collection_published_at | timestamp with time zone / timestamptz | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored collection published at. |
| selected_gift_id | uuid / uuid | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| gift_published_at | timestamp with time zone / timestamptz | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored gift published at. |
| fulfilment_status | character varying / varchar | NO | `'PLANNED'::character varying` | 40 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. State value; see CHECK constraints. |
| tracking_details | character varying / varchar | YES | No SQL default | 1000 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored tracking details. |
| independent_gift | boolean / bool | NO | `false` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored independent gift. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| fk_reserved_group_gift | f | `FOREIGN KEY (reserved_gift_id, event_id) REFERENCES gifts(id, event_id)` |
| fk_selected_group_gift | f | `FOREIGN KEY (selected_gift_id, event_id) REFERENCES gifts(id, event_id)` |
| gift_group_amount_visibility_ck | c | `CHECK (((amount_visibility)::text = ANY ((ARRAY['ADMIN_ONLY'::character varying, 'OWN_ONLY'::character varying])::text[])))` |
| gift_group_member_visibility_ck | c | `CHECK (((member_visibility)::text = ANY ((ARRAY['ADMIN_ONLY'::character varying, 'MEMBERS'::character varying])::text[])))` |
| gift_group_status_valid | c | `CHECK (((status)::text = ANY ((ARRAY['DRAFT'::character varying, 'PUBLISHED'::character varying])::text[])))` |
| gift_group_target_positive | c | `CHECK (((target_paise IS NULL) OR (target_paise > 0)))` |
| gift_groups_admin_id_fkey | f | `FOREIGN KEY (admin_id) REFERENCES gift_group_accounts(id)` |
| gift_groups_admin_id_key | u | `UNIQUE (admin_id)` |
| gift_groups_contribution_access_mode_check | c | `CHECK (((contribution_access_mode)::text = ANY ((ARRAY['BOTH'::character varying, 'GUESTS_ONLY'::character varying, 'MEMBERS_ONLY'::character varying])::text[])))` |
| gift_groups_event_id_fkey | f | `FOREIGN KEY (event_id) REFERENCES events(id)` |
| gift_groups_fulfilment_status_check | c | `CHECK (((fulfilment_status)::text = ANY ((ARRAY['PLANNED'::character varying, 'GIFT_PURCHASED'::character varying, 'TRACKING_SHARED'::character varying, 'AMOUNT_TRANSFERRED_TO_HOST'::character varying, 'COMPLETED'::character varying])::text[])))` |
| gift_groups_guest_bank_destination_id_fkey | f | `FOREIGN KEY (guest_bank_destination_id) REFERENCES gift_group_payment_destinations(id)` |
| gift_groups_guest_upi_destination_id_fkey | f | `FOREIGN KEY (guest_upi_destination_id) REFERENCES gift_group_payment_destinations(id)` |
| gift_groups_join_policy_check | c | `CHECK (((join_policy)::text = ANY ((ARRAY['APPROVAL'::character varying, 'AUTO_JOIN'::character varying, 'INVITE_ONLY'::character varying])::text[])))` |
| gift_groups_pkey | p | `PRIMARY KEY (id)` |
| gift_groups_suggested_contribution_paise_check | c | `CHECK (((suggested_contribution_paise >= 100) AND (suggested_contribution_paise <= 1000000000)))` |
| independent_group_no_wishlist | c | `CHECK (((NOT independent_gift) OR ((selected_gift_id IS NULL) AND (reserved_gift_id IS NULL))))` |
| uq_group_creator_identity | u | `UNIQUE (id, admin_id)` |


### Indexes

| Index | Definition |
| --- | --- |
| gift_group_event_idx | `CREATE INDEX gift_group_event_idx ON public.gift_groups USING btree (event_id)` |
| gift_groups_admin_id_key | `CREATE UNIQUE INDEX gift_groups_admin_id_key ON public.gift_groups USING btree (admin_id)` |
| gift_groups_pkey | `CREATE UNIQUE INDEX gift_groups_pkey ON public.gift_groups USING btree (id)` |
| uq_group_creator_identity | `CREATE UNIQUE INDEX uq_group_creator_identity ON public.gift_groups USING btree (id, admin_id)` |
| uq_reserved_group_gift | `CREATE UNIQUE INDEX uq_reserved_group_gift ON public.gift_groups USING btree (reserved_gift_id) WHERE (reserved_gift_id IS NOT NULL)` |


Consumers: [backend/src/main/java/com/giftplatform/admin/PlatformAdminConsoleController.java:1](../../src/main/java/com/giftplatform/admin/PlatformAdminConsoleController.java#L1); [backend/src/main/java/com/giftplatform/group/GroupContributionController.java:1](../../src/main/java/com/giftplatform/group/GroupContributionController.java#L1); [backend/src/main/java/com/giftplatform/group/GroupCreatorPaymentController.java:1](../../src/main/java/com/giftplatform/group/GroupCreatorPaymentController.java#L1); [backend/src/main/java/com/giftplatform/group/GroupGiftCollectionController.java:1](../../src/main/java/com/giftplatform/group/GroupGiftCollectionController.java#L1); [backend/src/main/java/com/giftplatform/group/GroupGiftCompletionController.java:1](../../src/main/java/com/giftplatform/group/GroupGiftCompletionController.java#L1); [backend/src/main/java/com/giftplatform/group/GroupGiftLifecycleService.java:1](../../src/main/java/com/giftplatform/group/GroupGiftLifecycleService.java#L1); [backend/src/main/java/com/giftplatform/group/GroupGuestContributionController.java:1](../../src/main/java/com/giftplatform/group/GroupGuestContributionController.java#L1); [backend/src/main/java/com/giftplatform/group/GroupReadinessService.java:1](../../src/main/java/com/giftplatform/group/GroupReadinessService.java#L1); [backend/src/main/java/com/giftplatform/group/GuestGiftController.java:1](../../src/main/java/com/giftplatform/group/GuestGiftController.java#L1); [backend/src/main/java/com/giftplatform/group/IndependentGroupGiftController.java:1](../../src/main/java/com/giftplatform/group/IndependentGroupGiftController.java#L1); [backend/src/main/java/com/giftplatform/monetization/CommercialAccessService.java:1](../../src/main/java/com/giftplatform/monetization/CommercialAccessService.java#L1); [backend/src/main/java/com/giftplatform/monetization/CommercialPurchaseService.java:1](../../src/main/java/com/giftplatform/monetization/CommercialPurchaseService.java#L1); [backend/src/main/java/com/giftplatform/payment/direct/GuestDirectTransferService.java:1](../../src/main/java/com/giftplatform/payment/direct/GuestDirectTransferService.java#L1); [backend/src/main/java/com/giftplatform/payment/report/HostContributionReportService.java:1](../../src/main/java/com/giftplatform/payment/report/HostContributionReportService.java#L1); [backend/src/main/java/com/giftplatform/payment/service/PaymentOrderService.java:1](../../src/main/java/com/giftplatform/payment/service/PaymentOrderService.java#L1); [backend/src/main/java/com/giftplatform/publicevent/PublicEventController.java:1](../../src/main/java/com/giftplatform/publicevent/PublicEventController.java#L1); [backend/src/main/java/com/giftplatform/security/recovery/EmailRecoveryController.java:1](../../src/main/java/com/giftplatform/security/recovery/EmailRecoveryController.java#L1); [backend/src/main/java/com/giftplatform/security/recovery/GroupEmailEnrollmentController.java:1](../../src/main/java/com/giftplatform/security/recovery/GroupEmailEnrollmentController.java#L1); [backend/src/main/java/com/giftplatform/security/recovery/GroupMobileEnrollmentController.java:1](../../src/main/java/com/giftplatform/security/recovery/GroupMobileEnrollmentController.java#L1); [backend/src/main/java/com/giftplatform/security/recovery/SignupEmailVerificationService.java:1](../../src/main/java/com/giftplatform/security/recovery/SignupEmailVerificationService.java#L1); [backend/src/main/java/com/giftplatform/security/recovery/UsernameRecoveryController.java:1](../../src/main/java/com/giftplatform/security/recovery/UsernameRecoveryController.java#L1)

## gift_photos

Type: **BASE TABLE**. Purpose: Invitation/media/AI request metadata or validated retailer lookup tracking.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| event_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| actor_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| content | bytea / bytea | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored content. |
| width | integer / int4 | NO | No SQL default | None / 32 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored width. |
| height | integer / int4 | NO | No SQL default | None / 32 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored height. |
| created_at | timestamp with time zone / timestamptz | NO | `CURRENT_TIMESTAMP` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored created at. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| gift_photos_actor_id_fkey | f | `FOREIGN KEY (actor_id) REFERENCES users(id)` |
| gift_photos_event_id_fkey | f | `FOREIGN KEY (event_id) REFERENCES events(id)` |
| gift_photos_height_check | c | `CHECK (((height >= 160) AND (height <= 1200)))` |
| gift_photos_pkey | p | `PRIMARY KEY (id)` |
| gift_photos_width_check | c | `CHECK (((width >= 160) AND (width <= 1200)))` |


### Indexes

| Index | Definition |
| --- | --- |
| gift_photos_pkey | `CREATE UNIQUE INDEX gift_photos_pkey ON public.gift_photos USING btree (id)` |
| idx_gift_photos_event | `CREATE INDEX idx_gift_photos_event ON public.gift_photos USING btree (event_id)` |


Consumers: [backend/src/main/java/com/giftplatform/gift/photo/GiftPhotoService.java:1](../../src/main/java/com/giftplatform/gift/photo/GiftPhotoService.java#L1)

## gift_retailer_metadata_audit

Type: **BASE TABLE**. Purpose: Invitation/media/AI request metadata or validated retailer lookup tracking.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| event_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| actor_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| gift_id | uuid / uuid | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| source_url | character varying / varchar | NO | No SQL default | 2048 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored source url. |
| metadata | jsonb / jsonb | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored metadata. |
| fetched_at | timestamp with time zone / timestamptz | NO | `CURRENT_TIMESTAMP` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored fetched at. |
| saved_at | timestamp with time zone / timestamptz | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored saved at. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| gift_retailer_metadata_audit_actor_id_fkey | f | `FOREIGN KEY (actor_id) REFERENCES users(id)` |
| gift_retailer_metadata_audit_event_id_fkey | f | `FOREIGN KEY (event_id) REFERENCES events(id)` |
| gift_retailer_metadata_audit_gift_id_fkey | f | `FOREIGN KEY (gift_id) REFERENCES gifts(id)` |
| gift_retailer_metadata_audit_pkey | p | `PRIMARY KEY (id)` |


### Indexes

| Index | Definition |
| --- | --- |
| gift_retailer_metadata_audit_pkey | `CREATE UNIQUE INDEX gift_retailer_metadata_audit_pkey ON public.gift_retailer_metadata_audit USING btree (id)` |
| idx_gift_retailer_metadata_gift | `CREATE INDEX idx_gift_retailer_metadata_gift ON public.gift_retailer_metadata_audit USING btree (gift_id, saved_at)` |


Consumers: [backend/src/main/java/com/giftplatform/gift/retailer/RetailerMetadataService.java:1](../../src/main/java/com/giftplatform/gift/retailer/RetailerMetadataService.java#L1)

## gifts

Type: **BASE TABLE**. Purpose: Host wishlist gift and its publication/funding metadata.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| event_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| version | bigint / int8 | NO | `0` | None / 64 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored version. |
| name | character varying / varchar | NO | No SQL default | 200 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored name. |
| description | text / text | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored description. |
| image_key | character varying / varchar | YES | No SQL default | 512 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored image key. |
| target_amount | numeric / numeric | NO | No SQL default | None / 15 / 2 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored target amount. |
| status | character varying / varchar | NO | `'ACTIVE'::character varying` | 30 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. State value; see CHECK constraints. |
| fulfillment_override | character varying / varchar | YES | No SQL default | 40 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored fulfillment override. |
| created_at | timestamp with time zone / timestamptz | NO | `CURRENT_TIMESTAMP` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored created at. |
| updated_at | timestamp with time zone / timestamptz | NO | `CURRENT_TIMESTAMP` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored updated at. |
| visibility | character varying / varchar | NO | `'DRAFT'::character varying` | 12 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored visibility. |
| product_url | character varying / varchar | YES | No SQL default | 2048 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored product url. |
| priority | character varying / varchar | NO | `'MEDIUM'::character varying` | 6 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored priority. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| ck_gift_name_nonempty | c | `CHECK ((length(btrim((name)::text)) > 0))` |
| ck_gifts_priority | c | `CHECK (((priority)::text = ANY ((ARRAY['HIGH'::character varying, 'MEDIUM'::character varying, 'LOW'::character varying])::text[])))` |
| ck_gifts_visibility | c | `CHECK (((visibility)::text = ANY ((ARRAY['DRAFT'::character varying, 'PUBLIC'::character varying])::text[])))` |
| gifts_event_id_fkey | f | `FOREIGN KEY (event_id) REFERENCES events(id)` |
| gifts_fulfillment_override_check | c | `CHECK (((fulfillment_override)::text = ANY ((ARRAY['DELIVER_WHEN_FUNDED'::character varying, 'AFTER_EVENT_CONFIRMATION'::character varying])::text[])))` |
| gifts_pkey | p | `PRIMARY KEY (id)` |
| gifts_status_check | c | `CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'CANCELLED'::character varying, 'CLOSED_UNFUNDED'::character varying])::text[])))` |
| gifts_target_amount_check | c | `CHECK ((target_amount > (0)::numeric))` |
| uq_gifts_id_event | u | `UNIQUE (id, event_id)` |


### Indexes

| Index | Definition |
| --- | --- |
| gifts_pkey | `CREATE UNIQUE INDEX gifts_pkey ON public.gifts USING btree (id)` |
| idx_gifts_event_created | `CREATE INDEX idx_gifts_event_created ON public.gifts USING btree (event_id, created_at)` |
| idx_gifts_event_priority | `CREATE INDEX idx_gifts_event_priority ON public.gifts USING btree (event_id, priority, created_at, id)` |
| idx_gifts_event_visibility | `CREATE INDEX idx_gifts_event_visibility ON public.gifts USING btree (event_id, visibility)` |
| uq_gifts_id_event | `CREATE UNIQUE INDEX uq_gifts_id_event ON public.gifts USING btree (id, event_id)` |


Consumers: [backend/src/main/java/com/giftplatform/admin/GroupModuleInterceptor.java:1](../../src/main/java/com/giftplatform/admin/GroupModuleInterceptor.java#L1); [backend/src/main/java/com/giftplatform/gift/controller/GiftController.java:1](../../src/main/java/com/giftplatform/gift/controller/GiftController.java#L1); [backend/src/main/java/com/giftplatform/gift/entity/Gift.java:1](../../src/main/java/com/giftplatform/gift/entity/Gift.java#L1); [backend/src/main/java/com/giftplatform/gift/photo/GiftPhotoController.java:1](../../src/main/java/com/giftplatform/gift/photo/GiftPhotoController.java#L1); [backend/src/main/java/com/giftplatform/gift/photo/GiftPhotoService.java:1](../../src/main/java/com/giftplatform/gift/photo/GiftPhotoService.java#L1); [backend/src/main/java/com/giftplatform/gift/retailer/RetailerMetadataController.java:1](../../src/main/java/com/giftplatform/gift/retailer/RetailerMetadataController.java#L1); [backend/src/main/java/com/giftplatform/gift/service/GiftManagementService.java:1](../../src/main/java/com/giftplatform/gift/service/GiftManagementService.java#L1); [backend/src/main/java/com/giftplatform/group/GroupGiftCollectionController.java:1](../../src/main/java/com/giftplatform/group/GroupGiftCollectionController.java#L1); [backend/src/main/java/com/giftplatform/group/GroupGiftLifecycleService.java:1](../../src/main/java/com/giftplatform/group/GroupGiftLifecycleService.java#L1); [backend/src/main/java/com/giftplatform/group/GuestGiftController.java:1](../../src/main/java/com/giftplatform/group/GuestGiftController.java#L1); [backend/src/main/java/com/giftplatform/group/IndividualGuestGiftController.java:1](../../src/main/java/com/giftplatform/group/IndividualGuestGiftController.java#L1); [backend/src/main/java/com/giftplatform/monetization/CommercialPublicService.java:1](../../src/main/java/com/giftplatform/monetization/CommercialPublicService.java#L1); [backend/src/main/java/com/giftplatform/monetization/CommercialPurchaseService.java:1](../../src/main/java/com/giftplatform/monetization/CommercialPurchaseService.java#L1); [backend/src/main/java/com/giftplatform/payment/controller/PublicGiftProgressController.java:1](../../src/main/java/com/giftplatform/payment/controller/PublicGiftProgressController.java#L1); [backend/src/main/java/com/giftplatform/payment/direct/GuestDirectTransferService.java:1](../../src/main/java/com/giftplatform/payment/direct/GuestDirectTransferService.java#L1); [backend/src/main/java/com/giftplatform/payment/hybrid/HybridCheckoutQuoteService.java:1](../../src/main/java/com/giftplatform/payment/hybrid/HybridCheckoutQuoteService.java#L1); [backend/src/main/java/com/giftplatform/payment/report/HostContributionReportService.java:1](../../src/main/java/com/giftplatform/payment/report/HostContributionReportService.java#L1); [backend/src/main/java/com/giftplatform/payment/service/ContributionAllocationService.java:1](../../src/main/java/com/giftplatform/payment/service/ContributionAllocationService.java#L1); [backend/src/main/java/com/giftplatform/payment/service/GiftFundingService.java:1](../../src/main/java/com/giftplatform/payment/service/GiftFundingService.java#L1); [backend/src/main/java/com/giftplatform/payment/service/PaymentOrderService.java:1](../../src/main/java/com/giftplatform/payment/service/PaymentOrderService.java#L1); [backend/src/main/java/com/giftplatform/publicevent/PublicEventController.java:1](../../src/main/java/com/giftplatform/publicevent/PublicEventController.java#L1); [backend/src/main/java/com/giftplatform/qr/design/InvitationComposer.java:1](../../src/main/java/com/giftplatform/qr/design/InvitationComposer.java#L1); [backend/src/main/java/com/giftplatform/security/SecurityConfig.java:1](../../src/main/java/com/giftplatform/security/SecurityConfig.java#L1)

## group_collection_notifications

Type: **BASE TABLE**. Purpose: Invitation/group/individual gift coordination, contribution reporting or private gift progress. Monetary reporting is distinct from verified settlement.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| group_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| event_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| created_at | timestamp with time zone / timestamptz | NO | `now()` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored created at. |
| read_at | timestamp with time zone / timestamptz | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored read at. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| group_collection_notifications_event_id_fkey | f | `FOREIGN KEY (event_id) REFERENCES events(id)` |
| group_collection_notifications_group_id_fkey | f | `FOREIGN KEY (group_id) REFERENCES gift_groups(id)` |
| group_collection_notifications_group_id_key | u | `UNIQUE (group_id)` |
| group_collection_notifications_pkey | p | `PRIMARY KEY (id)` |


### Indexes

| Index | Definition |
| --- | --- |
| group_collection_notifications_group_id_key | `CREATE UNIQUE INDEX group_collection_notifications_group_id_key ON public.group_collection_notifications USING btree (group_id)` |
| group_collection_notifications_pkey | `CREATE UNIQUE INDEX group_collection_notifications_pkey ON public.group_collection_notifications USING btree (id)` |


Consumers: [backend/src/main/java/com/giftplatform/group/GroupContributionController.java:1](../../src/main/java/com/giftplatform/group/GroupContributionController.java#L1); [backend/src/main/java/com/giftplatform/group/GroupGiftCollectionController.java:1](../../src/main/java/com/giftplatform/group/GroupGiftCollectionController.java#L1)

## group_collection_transactions

Type: **VIEW**. Purpose: Invitation/group/individual gift coordination, contribution reporting or private gift progress. Monetary reporting is distinct from verified settlement.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| id | uuid / uuid | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| group_id | uuid / uuid | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| guest_name | character varying / varchar | YES | No SQL default | 120 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored guest name. |
| guest_contact | character varying / varchar | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored guest contact. |
| amount_paise | bigint / int8 | YES | No SQL default | None / 64 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Money in integer paise; preserve distinction between reported, confirmed and settled. |
| method | character varying / varchar | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored method. |
| transaction_reference | character varying / varchar | YES | No SQL default | 120 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored transaction reference. |
| state | character varying / varchar | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. State value; see CHECK constraints. |
| created_at | timestamp with time zone / timestamptz | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored created at. |
| gift_id_snapshot | uuid / uuid | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored gift id snapshot. |
| contributor_type | text / text | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored contributor type. |


Consumers: [backend/src/main/java/com/giftplatform/group/GroupContributionController.java:1](../../src/main/java/com/giftplatform/group/GroupContributionController.java#L1); [backend/src/main/java/com/giftplatform/group/GroupGiftCollectionController.java:1](../../src/main/java/com/giftplatform/group/GroupGiftCollectionController.java#L1); [backend/src/main/java/com/giftplatform/payment/report/HostContributionReportService.java:1](../../src/main/java/com/giftplatform/payment/report/HostContributionReportService.java#L1)

## group_gift_progress

Type: **VIEW**. Purpose: Invitation/group/individual gift coordination, contribution reporting or private gift progress. Monetary reporting is distinct from verified settlement.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| id | uuid / uuid | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| collected_paise | bigint / int8 | YES | No SQL default | None / 64 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Money in integer paise; preserve distinction between reported, confirmed and settled. |
| reported_paise | bigint / int8 | YES | No SQL default | None / 64 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Money in integer paise; preserve distinction between reported, confirmed and settled. |
| completion_percentage | numeric / numeric | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored completion percentage. |


Consumers: [backend/src/main/java/com/giftplatform/group/GroupContributionController.java:1](../../src/main/java/com/giftplatform/group/GroupContributionController.java#L1); [backend/src/main/java/com/giftplatform/group/GroupGiftCollectionController.java:1](../../src/main/java/com/giftplatform/group/GroupGiftCollectionController.java#L1); [backend/src/main/java/com/giftplatform/group/GroupGiftCompletionController.java:1](../../src/main/java/com/giftplatform/group/GroupGiftCompletionController.java#L1)

## group_gift_receipts

Type: **VIEW**. Purpose: Invitation/group/individual gift coordination, contribution reporting or private gift progress. Monetary reporting is distinct from verified settlement.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| id | uuid / uuid | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| group_id | uuid / uuid | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| amount_paise | bigint / int8 | YES | No SQL default | None / 64 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Money in integer paise; preserve distinction between reported, confirmed and settled. |
| state | character varying / varchar | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. State value; see CHECK constraints. |
| creator_received_at | timestamp with time zone / timestamptz | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored creator received at. |


Consumers: [backend/src/main/java/com/giftplatform/group/GroupGiftCollectionController.java:1](../../src/main/java/com/giftplatform/group/GroupGiftCollectionController.java#L1)

## group_member_email_policies

Type: **BASE TABLE**. Purpose: Invitation/group/individual gift coordination, contribution reporting or private gift progress. Monetary reporting is distinct from verified settlement.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| host_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| admin_enabled | boolean / bool | NO | `false` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored admin enabled. |
| host_enabled | boolean / bool | NO | `false` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored host enabled. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| group_member_email_policies_host_id_fkey | f | `FOREIGN KEY (host_id) REFERENCES users(id)` |
| group_member_email_policies_pkey | p | `PRIMARY KEY (host_id)` |


### Indexes

| Index | Definition |
| --- | --- |
| group_member_email_policies_pkey | `CREATE UNIQUE INDEX group_member_email_policies_pkey ON public.group_member_email_policies USING btree (host_id)` |


Consumers: [backend/src/main/java/com/giftplatform/admin/GroupMemberEmailPolicyController.java:1](../../src/main/java/com/giftplatform/admin/GroupMemberEmailPolicyController.java#L1); [backend/src/main/java/com/giftplatform/group/GroupReadinessService.java:1](../../src/main/java/com/giftplatform/group/GroupReadinessService.java#L1)

## guest_direct_transfer_claims

Type: **BASE TABLE**. Purpose: Persisted application object; exact structural contract follows. Narrative ownership beyond its FK/consumer code requires review.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| event_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| gift_id | uuid / uuid | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| destination_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| method | character varying / varchar | NO | No SQL default | 8 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored method. |
| amount_paise | bigint / int8 | NO | No SQL default | None / 64 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Money in integer paise; preserve distinction between reported, confirmed and settled. |
| guest_name | character varying / varchar | NO | No SQL default | 200 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored guest name. |
| reference_hash | character / bpchar | NO | No SQL default | 64 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Sensitive representation; do not expose plaintext or hashes. |
| client_key | character varying / varchar | NO | No SQL default | 120 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored client key. |
| status | character varying / varchar | NO | `'AWAITING_VERIFICATION'::character varying` | 32 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. State value; see CHECK constraints. |
| created_at | timestamp with time zone / timestamptz | NO | `CURRENT_TIMESTAMP` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored created at. |
| guest_email | character varying / varchar | YES | No SQL default | 320 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored guest email. |
| guest_phone | character varying / varchar | YES | No SQL default | 30 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored guest phone. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| guest_direct_transfer_claims_amount_paise_check | c | `CHECK (((amount_paise >= 100) AND (amount_paise <= 100000000)))` |
| guest_direct_transfer_claims_event_id_fkey | f | `FOREIGN KEY (event_id) REFERENCES events(id)` |
| guest_direct_transfer_claims_gift_id_fkey | f | `FOREIGN KEY (gift_id) REFERENCES gifts(id)` |
| guest_direct_transfer_claims_method_check | c | `CHECK (((method)::text = ANY ((ARRAY['UPI'::character varying, 'BANK'::character varying])::text[])))` |
| guest_direct_transfer_claims_pkey | p | `PRIMARY KEY (id)` |
| guest_direct_transfer_claims_status_check | c | `CHECK (((status)::text = ANY ((ARRAY['AWAITING_VERIFICATION'::character varying, 'REJECTED'::character varying, 'EXPIRED'::character varying])::text[])))` |
| uq_direct_claim_key | u | `UNIQUE (event_id, client_key)` |
| uq_direct_claim_reference | u | `UNIQUE (method, reference_hash)` |


### Indexes

| Index | Definition |
| --- | --- |
| guest_direct_transfer_claims_pkey | `CREATE UNIQUE INDEX guest_direct_transfer_claims_pkey ON public.guest_direct_transfer_claims USING btree (id)` |
| idx_direct_claim_event | `CREATE INDEX idx_direct_claim_event ON public.guest_direct_transfer_claims USING btree (event_id, created_at DESC)` |
| idx_guest_direct_report_method_status | `CREATE INDEX idx_guest_direct_report_method_status ON public.guest_direct_transfer_claims USING btree (method, status, created_at DESC)` |
| uq_direct_claim_key | `CREATE UNIQUE INDEX uq_direct_claim_key ON public.guest_direct_transfer_claims USING btree (event_id, client_key)` |
| uq_direct_claim_reference | `CREATE UNIQUE INDEX uq_direct_claim_reference ON public.guest_direct_transfer_claims USING btree (method, reference_hash)` |


Consumers: [backend/src/main/java/com/giftplatform/group/GuestGiftController.java:1](../../src/main/java/com/giftplatform/group/GuestGiftController.java#L1); [backend/src/main/java/com/giftplatform/group/IndividualGuestGiftController.java:1](../../src/main/java/com/giftplatform/group/IndividualGuestGiftController.java#L1); [backend/src/main/java/com/giftplatform/payment/controller/PublicGiftProgressController.java:1](../../src/main/java/com/giftplatform/payment/controller/PublicGiftProgressController.java#L1); [backend/src/main/java/com/giftplatform/payment/direct/GuestDirectTransferService.java:1](../../src/main/java/com/giftplatform/payment/direct/GuestDirectTransferService.java#L1); [backend/src/main/java/com/giftplatform/payment/report/HostContributionReportService.java:1](../../src/main/java/com/giftplatform/payment/report/HostContributionReportService.java#L1); [backend/src/main/java/com/giftplatform/payment/verification/HostTransactionVerificationController.java:1](../../src/main/java/com/giftplatform/payment/verification/HostTransactionVerificationController.java#L1)

## guest_gift_host_contributions

Type: **BASE TABLE**. Purpose: Invitation/group/individual gift coordination, contribution reporting or private gift progress. Monetary reporting is distinct from verified settlement.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| guest_gift_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| claim_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| created_at | timestamp with time zone / timestamptz | NO | `now()` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored created at. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| guest_gift_host_contributions_claim_id_fkey | f | `FOREIGN KEY (claim_id) REFERENCES guest_direct_transfer_claims(id)` |
| guest_gift_host_contributions_claim_id_key | u | `UNIQUE (claim_id)` |
| guest_gift_host_contributions_guest_gift_id_fkey | f | `FOREIGN KEY (guest_gift_id) REFERENCES guest_gifts(id)` |
| guest_gift_host_contributions_pkey | p | `PRIMARY KEY (guest_gift_id)` |


### Indexes

| Index | Definition |
| --- | --- |
| guest_gift_host_contributions_claim_id_key | `CREATE UNIQUE INDEX guest_gift_host_contributions_claim_id_key ON public.guest_gift_host_contributions USING btree (claim_id)` |
| guest_gift_host_contributions_pkey | `CREATE UNIQUE INDEX guest_gift_host_contributions_pkey ON public.guest_gift_host_contributions USING btree (guest_gift_id)` |


Consumers: [backend/src/main/java/com/giftplatform/group/GuestGiftController.java:1](../../src/main/java/com/giftplatform/group/GuestGiftController.java#L1); [backend/src/main/java/com/giftplatform/group/IndividualGuestGiftController.java:1](../../src/main/java/com/giftplatform/group/IndividualGuestGiftController.java#L1)

## guest_gifts

Type: **BASE TABLE**. Purpose: Invitation/group/individual gift coordination, contribution reporting or private gift progress. Monetary reporting is distinct from verified settlement.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| event_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| added_by | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored added by. |
| name | character varying / varchar | NO | No SQL default | 160 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored name. |
| description | character varying / varchar | YES | No SQL default | 500 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored description. |
| product_url | character varying / varchar | YES | No SQL default | 1000 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored product url. |
| selected_by | uuid / uuid | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored selected by. |
| contribution_type | character varying / varchar | YES | No SQL default | 20 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored contribution type. |
| group_id | uuid / uuid | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| published_at | timestamp with time zone / timestamptz | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored published at. |
| created_at | timestamp with time zone / timestamptz | NO | `now()` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored created at. |
| guest_name | character varying / varchar | YES | No SQL default | 120 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored guest name. |
| guest_email | character varying / varchar | YES | No SQL default | 254 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored guest email. |
| guest_mobile | character varying / varchar | YES | No SQL default | 25 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored guest mobile. |
| contribution_paise | bigint / int8 | YES | No SQL default | None / 64 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Money in integer paise; preserve distinction between reported, confirmed and settled. |
| delivery_status | character varying / varchar | NO | `'PLANNED'::character varying` | 40 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. State value; see CHECK constraints. |
| tracking_details | character varying / varchar | YES | No SQL default | 1000 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored tracking details. |
| giving_method | character varying / varchar | YES | No SQL default | 30 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored giving method. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| guest_gifts_added_by_fkey | f | `FOREIGN KEY (added_by) REFERENCES gift_group_accounts(id)` |
| guest_gifts_check | c | `CHECK ((((selected_by IS NULL) AND (contribution_type IS NULL) AND (group_id IS NULL) AND (published_at IS NULL)) OR ((selected_by IS NOT NULL) AND (contribution_type IS NOT NULL) AND ((((contribution_type)::text = 'INDIVIDUAL'::text) AND (group_id IS NULL)) OR (((contribution_type)::text = 'GROUP'::text) AND (group_id IS NOT NULL))))))` |
| guest_gifts_contribution_paise_check | c | `CHECK ((contribution_paise > 0))` |
| guest_gifts_delivery_status_check | c | `CHECK (((delivery_status)::text = ANY ((ARRAY['PLANNED'::character varying, 'GIFT_PURCHASED'::character varying, 'TRACKING_SHARED'::character varying, 'COMPLETED'::character varying])::text[])))` |
| guest_gifts_event_id_fkey | f | `FOREIGN KEY (event_id) REFERENCES events(id)` |
| guest_gifts_giving_method_check | c | `CHECK (((giving_method)::text = ANY ((ARRAY['LINK_AND_CONTRIBUTION'::character varying, 'DIRECT_DELIVERY'::character varying])::text[])))` |
| guest_gifts_group_id_fkey | f | `FOREIGN KEY (group_id) REFERENCES gift_groups(id)` |
| guest_gifts_pkey | p | `PRIMARY KEY (id)` |
| guest_gifts_selected_by_fkey | f | `FOREIGN KEY (selected_by) REFERENCES gift_group_accounts(id)` |


### Indexes

| Index | Definition |
| --- | --- |
| guest_gifts_event | `CREATE INDEX guest_gifts_event ON public.guest_gifts USING btree (event_id, created_at, id)` |
| guest_gifts_pkey | `CREATE UNIQUE INDEX guest_gifts_pkey ON public.guest_gifts USING btree (id)` |


Consumers: [backend/src/main/java/com/giftplatform/group/GuestGiftController.java:1](../../src/main/java/com/giftplatform/group/GuestGiftController.java#L1); [backend/src/main/java/com/giftplatform/group/IndividualGuestGiftController.java:1](../../src/main/java/com/giftplatform/group/IndividualGuestGiftController.java#L1)

## guest_manual_display_allocations

Type: **BASE TABLE**. Purpose: Persisted application object; exact structural contract follows. Narrative ownership beyond its FK/consumer code requires review.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| claim_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| event_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| gift_id | uuid / uuid | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| gift_paise | bigint / int8 | NO | `0` | None / 64 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Money in integer paise; preserve distinction between reported, confirmed and settled. |
| wallet_paise | bigint / int8 | NO | `0` | None / 64 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Money in integer paise; preserve distinction between reported, confirmed and settled. |
| created_at | timestamp with time zone / timestamptz | NO | `CURRENT_TIMESTAMP` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored created at. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| guest_manual_display_allocations_check | c | `CHECK (((gift_id IS NOT NULL) OR (gift_paise = 0)))` |
| guest_manual_display_allocations_claim_id_fkey | f | `FOREIGN KEY (claim_id) REFERENCES guest_direct_transfer_claims(id)` |
| guest_manual_display_allocations_event_id_fkey | f | `FOREIGN KEY (event_id) REFERENCES events(id)` |
| guest_manual_display_allocations_gift_id_fkey | f | `FOREIGN KEY (gift_id) REFERENCES gifts(id)` |
| guest_manual_display_allocations_gift_paise_check | c | `CHECK ((gift_paise >= 0))` |
| guest_manual_display_allocations_pkey | p | `PRIMARY KEY (claim_id)` |
| guest_manual_display_allocations_wallet_paise_check | c | `CHECK ((wallet_paise >= 0))` |


### Indexes

| Index | Definition |
| --- | --- |
| guest_manual_display_allocations_pkey | `CREATE UNIQUE INDEX guest_manual_display_allocations_pkey ON public.guest_manual_display_allocations USING btree (claim_id)` |
| idx_guest_manual_display_event | `CREATE INDEX idx_guest_manual_display_event ON public.guest_manual_display_allocations USING btree (event_id)` |
| idx_guest_manual_display_gift | `CREATE INDEX idx_guest_manual_display_gift ON public.guest_manual_display_allocations USING btree (event_id, gift_id)` |


Consumers: [backend/src/main/java/com/giftplatform/payment/controller/PublicGiftProgressController.java:1](../../src/main/java/com/giftplatform/payment/controller/PublicGiftProgressController.java#L1); [backend/src/main/java/com/giftplatform/payment/direct/GuestDirectTransferService.java:1](../../src/main/java/com/giftplatform/payment/direct/GuestDirectTransferService.java#L1); [backend/src/main/java/com/giftplatform/payment/report/HostContributionReportService.java:1](../../src/main/java/com/giftplatform/payment/report/HostContributionReportService.java#L1)

## host_account_verification_checks

Type: **BASE TABLE**. Purpose: Host receiving destinations, event-scoped account selection or independent account-verification attempts.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| host_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| destination_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| destination_kind | character varying / varchar | NO | No SQL default | 8 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored destination kind. |
| environment | character varying / varchar | NO | No SQL default | 16 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored environment. |
| outcome | character varying / varchar | NO | No SQL default | 24 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored outcome. |
| provider_status | character varying / varchar | YES | No SQL default | 40 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. State value; see CHECK constraints. |
| provider_reference | character varying / varchar | YES | No SQL default | 120 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored provider reference. |
| name_matched | boolean / bool | NO | `false` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored name matched. |
| created_at | timestamp with time zone / timestamptz | NO | `now()` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored created at. |
| completed_at | timestamp with time zone / timestamptz | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored completed at. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| host_account_verification_checks_check | c | `CHECK ((((outcome)::text <> 'VERIFIED'::text) OR (((environment)::text = 'PRODUCTION'::text) AND (provider_reference IS NOT NULL) AND name_matched)))` |
| host_account_verification_checks_destination_kind_check | c | `CHECK (((destination_kind)::text = ANY ((ARRAY['BANK'::character varying, 'UPI'::character varying])::text[])))` |
| host_account_verification_checks_environment_check | c | `CHECK (((environment)::text = ANY ((ARRAY['SANDBOX'::character varying, 'PRODUCTION'::character varying])::text[])))` |
| host_account_verification_checks_host_id_fkey | f | `FOREIGN KEY (host_id) REFERENCES users(id)` |
| host_account_verification_checks_outcome_check | c | `CHECK (((outcome)::text = ANY ((ARRAY['PENDING'::character varying, 'VERIFIED'::character varying, 'SANDBOX_VALID'::character varying, 'REJECTED'::character varying, 'NAME_MISMATCH'::character varying, 'INCONCLUSIVE'::character varying, 'UNAVAILABLE'::character varying, 'STALE'::character varying])::text[])))` |
| host_account_verification_checks_pkey | p | `PRIMARY KEY (id)` |


### Indexes

| Index | Definition |
| --- | --- |
| host_account_verification_checks_pkey | `CREATE UNIQUE INDEX host_account_verification_checks_pkey ON public.host_account_verification_checks USING btree (id)` |
| idx_host_account_checks | `CREATE INDEX idx_host_account_checks ON public.host_account_verification_checks USING btree (host_id, destination_kind, destination_id, created_at DESC)` |
| ux_account_check_evidence | `CREATE UNIQUE INDEX ux_account_check_evidence ON public.host_account_verification_checks USING btree (environment, provider_reference) WHERE ((provider_reference IS NOT NULL) AND ((environment)::text = 'PRODUCTION'::text))` |
| ux_account_check_pending | `CREATE UNIQUE INDEX ux_account_check_pending ON public.host_account_verification_checks USING btree (host_id, destination_kind, destination_id) WHERE ((outcome)::text = 'PENDING'::text)` |


Consumers: [backend/src/main/java/com/giftplatform/hostpayment/verification/account/AccountVerificationController.java:1](../../src/main/java/com/giftplatform/hostpayment/verification/account/AccountVerificationController.java#L1)

## host_bank_accounts

Type: **BASE TABLE**. Purpose: Host receiving destinations, event-scoped account selection or independent account-verification attempts.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| host_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| account_number_encrypted | text / text | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored account number encrypted. |
| account_fingerprint | character varying / varchar | NO | No SQL default | 64 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored account fingerprint. |
| account_number_masked | character varying / varchar | NO | No SQL default | 32 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored account number masked. |
| ifsc_encrypted | text / text | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored ifsc encrypted. |
| ifsc_masked | character varying / varchar | NO | No SQL default | 20 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored ifsc masked. |
| account_holder_encrypted | text / text | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored account holder encrypted. |
| verification_status | character varying / varchar | NO | `'PENDING'::character varying` | 20 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. State value; see CHECK constraints. |
| enabled | boolean / bool | NO | `true` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored enabled. |
| preferred | boolean / bool | NO | `false` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored preferred. |
| version | bigint / int8 | NO | `0` | None / 64 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored version. |
| created_at | timestamp with time zone / timestamptz | NO | `CURRENT_TIMESTAMP` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored created at. |
| updated_at | timestamp with time zone / timestamptz | NO | `CURRENT_TIMESTAMP` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored updated at. |
| host_confirmed_at | timestamp with time zone / timestamptz | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored host confirmed at. |
| account_label | character varying / varchar | YES | No SQL default | 50 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored account label. |
| archived_at | timestamp with time zone / timestamptz | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored archived at. |
| event_id | uuid / uuid | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| chk_host_bank_archived_disabled | c | `CHECK (((archived_at IS NULL) OR ((NOT enabled) AND (NOT preferred))))` |
| chk_host_bank_preferred | c | `CHECK (((NOT preferred) OR (enabled AND (((verification_status)::text = 'VERIFIED'::text) OR (host_confirmed_at IS NOT NULL)))))` |
| chk_host_bank_verification | c | `CHECK (((verification_status)::text = ANY ((ARRAY['PENDING'::character varying, 'VERIFIED'::character varying, 'FAILED'::character varying, 'REVOKED'::character varying])::text[])))` |
| fk_bank_event_owner | f | `FOREIGN KEY (event_id, host_id) REFERENCES events(id, owner_id)` |
| host_bank_accounts_host_id_fkey | f | `FOREIGN KEY (host_id) REFERENCES users(id)` |
| host_bank_accounts_pkey | p | `PRIMARY KEY (id)` |
| ux_bank_account_owner | u | `UNIQUE (id, host_id)` |


### Indexes

| Index | Definition |
| --- | --- |
| host_bank_accounts_pkey | `CREATE UNIQUE INDEX host_bank_accounts_pkey ON public.host_bank_accounts USING btree (id)` |
| idx_host_bank_accounts_host | `CREATE INDEX idx_host_bank_accounts_host ON public.host_bank_accounts USING btree (host_id)` |
| ux_bank_account_owner | `CREATE UNIQUE INDEX ux_bank_account_owner ON public.host_bank_accounts USING btree (id, host_id)` |
| ux_event_bank_fingerprint | `CREATE UNIQUE INDEX ux_event_bank_fingerprint ON public.host_bank_accounts USING btree (event_id, account_fingerprint) WHERE ((archived_at IS NULL) AND (event_id IS NOT NULL))` |
| ux_event_preferred_bank | `CREATE UNIQUE INDEX ux_event_preferred_bank ON public.host_bank_accounts USING btree (event_id) WHERE (preferred AND (event_id IS NOT NULL))` |
| ux_host_bank_fingerprint | `CREATE UNIQUE INDEX ux_host_bank_fingerprint ON public.host_bank_accounts USING btree (host_id, account_fingerprint) WHERE ((archived_at IS NULL) AND (event_id IS NULL))` |
| ux_host_preferred_bank | `CREATE UNIQUE INDEX ux_host_preferred_bank ON public.host_bank_accounts USING btree (host_id) WHERE (preferred AND (event_id IS NULL))` |


Consumers: [backend/src/main/java/com/giftplatform/event/service/EventPublishReadiness.java:1](../../src/main/java/com/giftplatform/event/service/EventPublishReadiness.java#L1); [backend/src/main/java/com/giftplatform/hostpayment/controller/ReceivingAccountHealthController.java:1](../../src/main/java/com/giftplatform/hostpayment/controller/ReceivingAccountHealthController.java#L1); [backend/src/main/java/com/giftplatform/hostpayment/entity/HostBankAccount.java:1](../../src/main/java/com/giftplatform/hostpayment/entity/HostBankAccount.java#L1); [backend/src/main/java/com/giftplatform/hostpayment/service/EventAccountLinkService.java:1](../../src/main/java/com/giftplatform/hostpayment/service/EventAccountLinkService.java#L1); [backend/src/main/java/com/giftplatform/hostpayment/verification/HostDestinationVerificationInboxController.java:1](../../src/main/java/com/giftplatform/hostpayment/verification/HostDestinationVerificationInboxController.java#L1); [backend/src/main/java/com/giftplatform/hostpayment/verification/account/AccountVerificationController.java:1](../../src/main/java/com/giftplatform/hostpayment/verification/account/AccountVerificationController.java#L1); [backend/src/main/java/com/giftplatform/payment/direct/GuestDirectTransferService.java:1](../../src/main/java/com/giftplatform/payment/direct/GuestDirectTransferService.java#L1); [backend/src/main/java/com/giftplatform/payment/direct/GuestVerifiedDestinationService.java:1](../../src/main/java/com/giftplatform/payment/direct/GuestVerifiedDestinationService.java#L1); [backend/src/main/java/com/giftplatform/payment/settlement/HostPayoutRequestService.java:1](../../src/main/java/com/giftplatform/payment/settlement/HostPayoutRequestService.java#L1); [backend/src/main/java/com/giftplatform/payment/settlement/HostSettlementUiService.java:1](../../src/main/java/com/giftplatform/payment/settlement/HostSettlementUiService.java#L1); [backend/src/main/java/com/giftplatform/payment/settlement/VerifiedBankFallbackService.java:1](../../src/main/java/com/giftplatform/payment/settlement/VerifiedBankFallbackService.java#L1)

## host_destination_verification_attempts

Type: **BASE TABLE**. Purpose: Host receiving destinations, event-scoped account selection or independent account-verification attempts.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| host_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| destination_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| destination_kind | character varying / varchar | NO | No SQL default | 8 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored destination kind. |
| state | character varying / varchar | NO | No SQL default | 16 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. State value; see CHECK constraints. |
| provider | character varying / varchar | YES | No SQL default | 40 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored provider. |
| provider_reference | character varying / varchar | YES | No SQL default | 120 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored provider reference. |
| created_at | timestamp with time zone / timestamptz | NO | `CURRENT_TIMESTAMP` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored created at. |
| updated_at | timestamp with time zone / timestamptz | NO | `CURRENT_TIMESTAMP` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored updated at. |
| request_provider | character varying / varchar | YES | No SQL default | 40 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored request provider. |
| request_reference | character varying / varchar | YES | No SQL default | 120 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored request reference. |
| request_fund_account_id | character varying / varchar | YES | No SQL default | 120 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| chk_verification_evidence | c | `CHECK ((((state)::text <> 'VERIFIED'::text) OR ((provider IS NOT NULL) AND (length(TRIM(BOTH FROM provider)) > 0) AND (provider_reference IS NOT NULL) AND (length(TRIM(BOTH FROM provider_reference)) > 0))))` |
| chk_verification_request_binding | c | `CHECK ((((request_provider IS NULL) AND (request_reference IS NULL) AND (request_fund_account_id IS NULL)) OR ((request_provider IS NOT NULL) AND (length(TRIM(BOTH FROM request_provider)) > 0) AND (request_reference IS NOT NULL) AND (length(TRIM(BOTH FROM request_reference)) > 0) AND (request_fund_account_id IS NOT NULL) AND (length(TRIM(BOTH FROM request_fund_account_id)) > 0))))` |
| host_destination_verification_attempts_destination_kind_check | c | `CHECK (((destination_kind)::text = ANY ((ARRAY['BANK'::character varying, 'UPI'::character varying])::text[])))` |
| host_destination_verification_attempts_host_id_fkey | f | `FOREIGN KEY (host_id) REFERENCES users(id)` |
| host_destination_verification_attempts_pkey | p | `PRIMARY KEY (id)` |
| host_destination_verification_attempts_state_check | c | `CHECK (((state)::text = ANY ((ARRAY['PENDING'::character varying, 'VERIFIED'::character varying, 'REJECTED'::character varying, 'UNAVAILABLE'::character varying])::text[])))` |


### Indexes

| Index | Definition |
| --- | --- |
| host_destination_verification_attempts_pkey | `CREATE UNIQUE INDEX host_destination_verification_attempts_pkey ON public.host_destination_verification_attempts USING btree (id)` |
| idx_host_verification_attempt_lookup | `CREATE INDEX idx_host_verification_attempt_lookup ON public.host_destination_verification_attempts USING btree (host_id, destination_kind, destination_id, created_at DESC)` |
| ux_host_verification_pending | `CREATE UNIQUE INDEX ux_host_verification_pending ON public.host_destination_verification_attempts USING btree (host_id, destination_kind, destination_id) WHERE ((state)::text = 'PENDING'::text)` |
| ux_host_verification_provider_reference | `CREATE UNIQUE INDEX ux_host_verification_provider_reference ON public.host_destination_verification_attempts USING btree (provider, provider_reference) WHERE (provider_reference IS NOT NULL)` |
| ux_host_verification_request_reference | `CREATE UNIQUE INDEX ux_host_verification_request_reference ON public.host_destination_verification_attempts USING btree (request_provider, request_reference) WHERE (request_reference IS NOT NULL)` |


Consumers: [backend/src/main/java/com/giftplatform/hostpayment/verification/HostDestinationVerificationInboxController.java:1](../../src/main/java/com/giftplatform/hostpayment/verification/HostDestinationVerificationInboxController.java#L1); [backend/src/main/java/com/giftplatform/hostpayment/verification/VerificationAttempt.java:1](../../src/main/java/com/giftplatform/hostpayment/verification/VerificationAttempt.java#L1)

## host_payout_requests

Type: **BASE TABLE**. Purpose: Payment, accounting, pricing, reconciliation or blocked-payout workflow. State and idempotency constraints are listed below.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| host_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| event_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| destination_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| amount_paise | bigint / int8 | NO | No SQL default | None / 64 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Money in integer paise; preserve distinction between reported, confirmed and settled. |
| currency | character varying / varchar | NO | `'INR'::character varying` | 3 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored currency. |
| idempotency_key | character varying / varchar | NO | No SQL default | 120 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored idempotency key. |
| state | character varying / varchar | NO | `'BLOCKED'::character varying` | 24 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. State value; see CHECK constraints. |
| reason | character varying / varchar | NO | `'LIVE_PAYOUTS_DISABLED'::character varying` | 160 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored reason. |
| created_at | timestamp with time zone / timestamptz | NO | `CURRENT_TIMESTAMP` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored created at. |
| cancelled_at | timestamp with time zone / timestamptz | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored cancelled at. |
| cancellation_reason | character varying / varchar | YES | No SQL default | 160 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored cancellation reason. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| ck_payout_cancel_pair | c | `CHECK ((((cancelled_at IS NULL) AND (cancellation_reason IS NULL)) OR ((cancelled_at IS NOT NULL) AND (cancellation_reason IS NOT NULL))))` |
| host_payout_requests_amount_paise_check | c | `CHECK ((amount_paise > 0))` |
| host_payout_requests_currency_check | c | `CHECK (((currency)::text = 'INR'::text))` |
| host_payout_requests_destination_id_fkey | f | `FOREIGN KEY (destination_id) REFERENCES host_bank_accounts(id)` |
| host_payout_requests_event_id_fkey | f | `FOREIGN KEY (event_id) REFERENCES events(id)` |
| host_payout_requests_host_id_fkey | f | `FOREIGN KEY (host_id) REFERENCES users(id)` |
| host_payout_requests_host_id_idempotency_key_key | u | `UNIQUE (host_id, idempotency_key)` |
| host_payout_requests_pkey | p | `PRIMARY KEY (id)` |
| host_payout_requests_state_check | c | `CHECK (((state)::text = 'BLOCKED'::text))` |


### Indexes

| Index | Definition |
| --- | --- |
| host_payout_requests_host_id_idempotency_key_key | `CREATE UNIQUE INDEX host_payout_requests_host_id_idempotency_key_key ON public.host_payout_requests USING btree (host_id, idempotency_key)` |
| host_payout_requests_pkey | `CREATE UNIQUE INDEX host_payout_requests_pkey ON public.host_payout_requests USING btree (id)` |
| idx_host_payout_requests_event | `CREATE INDEX idx_host_payout_requests_event ON public.host_payout_requests USING btree (event_id, created_at DESC)` |


Consumers: [backend/src/main/java/com/giftplatform/payment/settlement/HostPayoutRequestService.java:1](../../src/main/java/com/giftplatform/payment/settlement/HostPayoutRequestService.java#L1); [backend/src/main/java/com/giftplatform/payment/settlement/HostSettlementUiService.java:1](../../src/main/java/com/giftplatform/payment/settlement/HostSettlementUiService.java#L1); [backend/src/main/java/com/giftplatform/payment/settlement/PayoutReservationService.java:1](../../src/main/java/com/giftplatform/payment/settlement/PayoutReservationService.java#L1); [backend/src/main/java/com/giftplatform/payment/settlement/PayoutSafetyFinalizationService.java:1](../../src/main/java/com/giftplatform/payment/settlement/PayoutSafetyFinalizationService.java#L1)

## host_upi_accounts

Type: **BASE TABLE**. Purpose: Host receiving destinations, event-scoped account selection or independent account-verification attempts.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| host_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| upi_id_encrypted | text / text | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored upi id encrypted. |
| upi_id_fingerprint | character varying / varchar | NO | No SQL default | 64 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored upi id fingerprint. |
| upi_id_masked | character varying / varchar | NO | No SQL default | 120 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored upi id masked. |
| verification_status | character varying / varchar | NO | `'PENDING'::character varying` | 20 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. State value; see CHECK constraints. |
| enabled | boolean / bool | NO | `true` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored enabled. |
| preferred | boolean / bool | NO | `false` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored preferred. |
| version | bigint / int8 | NO | `0` | None / 64 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored version. |
| created_at | timestamp with time zone / timestamptz | NO | `CURRENT_TIMESTAMP` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored created at. |
| updated_at | timestamp with time zone / timestamptz | NO | `CURRENT_TIMESTAMP` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored updated at. |
| host_confirmed_at | timestamp with time zone / timestamptz | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored host confirmed at. |
| account_label | character varying / varchar | YES | No SQL default | 50 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored account label. |
| archived_at | timestamp with time zone / timestamptz | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored archived at. |
| event_id | uuid / uuid | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| chk_host_upi_archived_disabled | c | `CHECK (((archived_at IS NULL) OR ((NOT enabled) AND (NOT preferred))))` |
| chk_host_upi_preferred | c | `CHECK (((NOT preferred) OR (enabled AND (((verification_status)::text = 'VERIFIED'::text) OR (host_confirmed_at IS NOT NULL)))))` |
| chk_host_upi_verification | c | `CHECK (((verification_status)::text = ANY ((ARRAY['PENDING'::character varying, 'VERIFIED'::character varying, 'FAILED'::character varying, 'REVOKED'::character varying])::text[])))` |
| fk_upi_event_owner | f | `FOREIGN KEY (event_id, host_id) REFERENCES events(id, owner_id)` |
| host_upi_accounts_host_id_fkey | f | `FOREIGN KEY (host_id) REFERENCES users(id)` |
| host_upi_accounts_pkey | p | `PRIMARY KEY (id)` |
| ux_upi_account_owner | u | `UNIQUE (id, host_id)` |


### Indexes

| Index | Definition |
| --- | --- |
| host_upi_accounts_pkey | `CREATE UNIQUE INDEX host_upi_accounts_pkey ON public.host_upi_accounts USING btree (id)` |
| idx_host_upi_accounts_host | `CREATE INDEX idx_host_upi_accounts_host ON public.host_upi_accounts USING btree (host_id)` |
| ux_event_preferred_upi | `CREATE UNIQUE INDEX ux_event_preferred_upi ON public.host_upi_accounts USING btree (event_id) WHERE (preferred AND (event_id IS NOT NULL))` |
| ux_event_upi_fingerprint | `CREATE UNIQUE INDEX ux_event_upi_fingerprint ON public.host_upi_accounts USING btree (event_id, upi_id_fingerprint) WHERE ((archived_at IS NULL) AND (event_id IS NOT NULL))` |
| ux_host_preferred_upi | `CREATE UNIQUE INDEX ux_host_preferred_upi ON public.host_upi_accounts USING btree (host_id) WHERE (preferred AND (event_id IS NULL))` |
| ux_host_upi_fingerprint | `CREATE UNIQUE INDEX ux_host_upi_fingerprint ON public.host_upi_accounts USING btree (host_id, upi_id_fingerprint) WHERE ((archived_at IS NULL) AND (event_id IS NULL))` |
| ux_upi_account_owner | `CREATE UNIQUE INDEX ux_upi_account_owner ON public.host_upi_accounts USING btree (id, host_id)` |


Consumers: [backend/src/main/java/com/giftplatform/event/service/EventPublishReadiness.java:1](../../src/main/java/com/giftplatform/event/service/EventPublishReadiness.java#L1); [backend/src/main/java/com/giftplatform/hostpayment/controller/ReceivingAccountHealthController.java:1](../../src/main/java/com/giftplatform/hostpayment/controller/ReceivingAccountHealthController.java#L1); [backend/src/main/java/com/giftplatform/hostpayment/entity/HostUpiAccount.java:1](../../src/main/java/com/giftplatform/hostpayment/entity/HostUpiAccount.java#L1); [backend/src/main/java/com/giftplatform/hostpayment/service/EventAccountLinkService.java:1](../../src/main/java/com/giftplatform/hostpayment/service/EventAccountLinkService.java#L1); [backend/src/main/java/com/giftplatform/hostpayment/verification/HostDestinationVerificationInboxController.java:1](../../src/main/java/com/giftplatform/hostpayment/verification/HostDestinationVerificationInboxController.java#L1); [backend/src/main/java/com/giftplatform/hostpayment/verification/account/AccountVerificationController.java:1](../../src/main/java/com/giftplatform/hostpayment/verification/account/AccountVerificationController.java#L1); [backend/src/main/java/com/giftplatform/payment/direct/GuestDirectTransferService.java:1](../../src/main/java/com/giftplatform/payment/direct/GuestDirectTransferService.java#L1); [backend/src/main/java/com/giftplatform/payment/direct/GuestVerifiedDestinationService.java:1](../../src/main/java/com/giftplatform/payment/direct/GuestVerifiedDestinationService.java#L1)

## host_upi_qr_assets

Type: **BASE TABLE**. Purpose: Host receiving destinations, event-scoped account selection or independent account-verification attempts.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| host_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| upi_account_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| source | character varying / varchar | NO | No SQL default | 16 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored source. |
| storage_key | character varying / varchar | YES | No SQL default | 255 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored storage key. |
| payload_sha256 | character varying / varchar | NO | No SQL default | 64 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored payload sha256. |
| verified_payee_match | boolean / bool | NO | `false` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored verified payee match. |
| active | boolean / bool | NO | `false` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored active. |
| created_at | timestamp with time zone / timestamptz | NO | `CURRENT_TIMESTAMP` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored created at. |
| image_png | bytea / bytea | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored image png. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| chk_host_upi_qr_active | c | `CHECK (((NOT active) OR verified_payee_match))` |
| chk_host_upi_qr_source_storage | c | `CHECK (((((source)::text = 'UPLOADED'::text) AND (storage_key IS NOT NULL)) OR (((source)::text = 'GENERATED'::text) AND (storage_key IS NULL))))` |
| chk_upi_qr_png_size | c | `CHECK (((image_png IS NULL) OR ((octet_length(image_png) >= 100) AND (octet_length(image_png) <= 2000000))))` |
| chk_upi_qr_upload_bytes | c | `CHECK ((((source)::text <> 'UPLOADED'::text) OR (image_png IS NOT NULL)))` |
| host_upi_qr_assets_host_id_fkey | f | `FOREIGN KEY (host_id) REFERENCES users(id)` |
| host_upi_qr_assets_payload_sha256_check | c | `CHECK (((payload_sha256)::text ~ '^[0-9a-f]{64}$'::text))` |
| host_upi_qr_assets_pkey | p | `PRIMARY KEY (id)` |
| host_upi_qr_assets_source_check | c | `CHECK (((source)::text = ANY ((ARRAY['UPLOADED'::character varying, 'GENERATED'::character varying])::text[])))` |
| host_upi_qr_assets_upi_account_id_fkey | f | `FOREIGN KEY (upi_account_id) REFERENCES host_upi_accounts(id)` |


### Indexes

| Index | Definition |
| --- | --- |
| host_upi_qr_assets_pkey | `CREATE UNIQUE INDEX host_upi_qr_assets_pkey ON public.host_upi_qr_assets USING btree (id)` |
| idx_host_upi_qr_assets_account | `CREATE INDEX idx_host_upi_qr_assets_account ON public.host_upi_qr_assets USING btree (upi_account_id)` |
| ux_host_upi_qr_active | `CREATE UNIQUE INDEX ux_host_upi_qr_active ON public.host_upi_qr_assets USING btree (upi_account_id) WHERE active` |


Consumers: No direct Java literal reference found; migrations, views, triggers or indirect table selection may be the consumer.

## host_upi_qr_images

Type: **BASE TABLE**. Purpose: Host receiving destinations, event-scoped account selection or independent account-verification attempts.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| host_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| upi_account_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| image_png | bytea / bytea | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored image png. |
| payload_sha256 | character / bpchar | NO | No SQL default | 64 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored payload sha256. |
| source | character varying / varchar | NO | No SQL default | 16 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored source. |
| updated_at | timestamp with time zone / timestamptz | NO | `CURRENT_TIMESTAMP` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored updated at. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| chk_upi_qr_image_source | c | `CHECK (((((source)::text = 'UPLOADED'::text) AND (image_png IS NOT NULL)) OR (((source)::text = 'GENERATED'::text) AND (image_png IS NULL))))` |
| host_upi_qr_images_host_id_fkey | f | `FOREIGN KEY (host_id) REFERENCES users(id)` |
| host_upi_qr_images_payload_sha256_check | c | `CHECK ((payload_sha256 ~ '^[0-9a-f]{64}$'::text))` |
| host_upi_qr_images_pkey | p | `PRIMARY KEY (host_id, upi_account_id, source)` |
| host_upi_qr_images_source_check | c | `CHECK (((source)::text = ANY ((ARRAY['UPLOADED'::character varying, 'GENERATED'::character varying])::text[])))` |
| host_upi_qr_images_upi_account_id_fkey | f | `FOREIGN KEY (upi_account_id) REFERENCES host_upi_accounts(id)` |


### Indexes

| Index | Definition |
| --- | --- |
| host_upi_qr_images_pkey | `CREATE UNIQUE INDEX host_upi_qr_images_pkey ON public.host_upi_qr_images USING btree (host_id, upi_account_id, source)` |


Consumers: [backend/src/main/java/com/giftplatform/event/service/EventPublishReadiness.java:1](../../src/main/java/com/giftplatform/event/service/EventPublishReadiness.java#L1); [backend/src/main/java/com/giftplatform/hostpayment/controller/ReceivingAccountHealthController.java:1](../../src/main/java/com/giftplatform/hostpayment/controller/ReceivingAccountHealthController.java#L1); [backend/src/main/java/com/giftplatform/hostpayment/qr/JdbcUpiQrImageStorage.java:1](../../src/main/java/com/giftplatform/hostpayment/qr/JdbcUpiQrImageStorage.java#L1); [backend/src/main/java/com/giftplatform/payment/direct/GuestDirectTransferService.java:1](../../src/main/java/com/giftplatform/payment/direct/GuestDirectTransferService.java#L1); [backend/src/main/java/com/giftplatform/payment/direct/GuestVerifiedDestinationService.java:1](../../src/main/java/com/giftplatform/payment/direct/GuestVerifiedDestinationService.java#L1)

## hybrid_checkout_quotes

Type: **BASE TABLE**. Purpose: Payment, accounting, pricing, reconciliation or blocked-payout workflow. State and idempotency constraints are listed below.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| event_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| gift_id | uuid / uuid | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| contribution_paise | bigint / int8 | NO | No SQL default | None / 64 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Money in integer paise; preserve distinction between reported, confirmed and settled. |
| processing_fee_paise | bigint / int8 | NO | `0` | None / 64 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Money in integer paise; preserve distinction between reported, confirmed and settled. |
| total_paise | bigint / int8 | NO | No SQL default | None / 64 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Money in integer paise; preserve distinction between reported, confirmed and settled. |
| method | character varying / varchar | NO | No SQL default | 20 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored method. |
| created_at | timestamp with time zone / timestamptz | NO | `CURRENT_TIMESTAMP` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored created at. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| hybrid_checkout_quotes_check | c | `CHECK ((total_paise = (contribution_paise + processing_fee_paise)))` |
| hybrid_checkout_quotes_contribution_paise_check | c | `CHECK ((contribution_paise > 0))` |
| hybrid_checkout_quotes_event_id_fkey | f | `FOREIGN KEY (event_id) REFERENCES events(id)` |
| hybrid_checkout_quotes_gift_id_fkey | f | `FOREIGN KEY (gift_id) REFERENCES gifts(id)` |
| hybrid_checkout_quotes_method_check | c | `CHECK (((method)::text = 'RAZORPAY_CHECKOUT'::text))` |
| hybrid_checkout_quotes_pkey | p | `PRIMARY KEY (id)` |
| hybrid_checkout_quotes_processing_fee_paise_check | c | `CHECK ((processing_fee_paise >= 0))` |


### Indexes

| Index | Definition |
| --- | --- |
| hybrid_checkout_quotes_pkey | `CREATE UNIQUE INDEX hybrid_checkout_quotes_pkey ON public.hybrid_checkout_quotes USING btree (id)` |


Consumers: [backend/src/main/java/com/giftplatform/payment/hybrid/HybridCheckoutQuoteService.java:1](../../src/main/java/com/giftplatform/payment/hybrid/HybridCheckoutQuoteService.java#L1)

## invitation_ai_requests

Type: **BASE TABLE**. Purpose: Invitation/media/AI request metadata or validated retailer lookup tracking.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| host_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| event_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| status | character varying / varchar | NO | No SQL default | 16 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. State value; see CHECK constraints. |
| created_at | timestamp with time zone / timestamptz | NO | `now()` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored created at. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| invitation_ai_requests_event_id_fkey | f | `FOREIGN KEY (event_id) REFERENCES events(id)` |
| invitation_ai_requests_host_id_fkey | f | `FOREIGN KEY (host_id) REFERENCES users(id)` |
| invitation_ai_requests_pkey | p | `PRIMARY KEY (id)` |
| invitation_ai_requests_status_check | c | `CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'COMPLETED'::character varying, 'FAILED'::character varying])::text[])))` |


### Indexes

| Index | Definition |
| --- | --- |
| idx_invitation_ai_host_time | `CREATE INDEX idx_invitation_ai_host_time ON public.invitation_ai_requests USING btree (host_id, created_at DESC)` |
| invitation_ai_requests_pkey | `CREATE UNIQUE INDEX invitation_ai_requests_pkey ON public.invitation_ai_requests USING btree (id)` |


Consumers: [backend/src/main/java/com/giftplatform/admin/AiInvitationAdminController.java:1](../../src/main/java/com/giftplatform/admin/AiInvitationAdminController.java#L1); [backend/src/main/java/com/giftplatform/qr/design/ai/AiInvitationService.java:1](../../src/main/java/com/giftplatform/qr/design/ai/AiInvitationService.java#L1)

## invitation_gallery_assets

Type: **BASE TABLE**. Purpose: Invitation/media/AI request metadata or validated retailer lookup tracking.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| category | character varying / varchar | NO | No SQL default | 20 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored category. |
| occasion | character varying / varchar | NO | No SQL default | 50 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored occasion. |
| title | character varying / varchar | NO | No SQL default | 120 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored title. |
| content_type | character varying / varchar | NO | No SQL default | 20 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored content type. |
| image_png | bytea / bytea | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored image png. |
| width | integer / int4 | NO | No SQL default | None / 32 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored width. |
| height | integer / int4 | NO | No SQL default | None / 32 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored height. |
| published | boolean / bool | NO | `false` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored published. |
| created_at | timestamp with time zone / timestamptz | NO | `CURRENT_TIMESTAMP` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored created at. |
| updated_at | timestamp with time zone / timestamptz | NO | `CURRENT_TIMESTAMP` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored updated at. |
| tier | character varying / varchar | NO | `'FREE'::character varying` | 20 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored tier. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| chk_invitation_gallery_asset_tier | c | `CHECK (((tier)::text = ANY ((ARRAY['FREE'::character varying, 'PREMIUM'::character varying])::text[])))` |
| invitation_gallery_assets_category_check | c | `CHECK (((category)::text = ANY ((ARRAY['BACKGROUND'::character varying, 'DECORATION'::character varying])::text[])))` |
| invitation_gallery_assets_content_type_check | c | `CHECK (((content_type)::text = 'image/png'::text))` |
| invitation_gallery_assets_height_check | c | `CHECK (((height >= 32) AND (height <= 4096)))` |
| invitation_gallery_assets_pkey | p | `PRIMARY KEY (id)` |
| invitation_gallery_assets_width_check | c | `CHECK (((width >= 32) AND (width <= 4096)))` |


### Indexes

| Index | Definition |
| --- | --- |
| idx_invitation_gallery_published | `CREATE INDEX idx_invitation_gallery_published ON public.invitation_gallery_assets USING btree (category, occasion, published)` |
| idx_invitation_gallery_tier | `CREATE INDEX idx_invitation_gallery_tier ON public.invitation_gallery_assets USING btree (tier, published)` |
| invitation_gallery_assets_pkey | `CREATE UNIQUE INDEX invitation_gallery_assets_pkey ON public.invitation_gallery_assets USING btree (id)` |


Consumers: [backend/src/main/java/com/giftplatform/asset/GalleryAsset.java:1](../../src/main/java/com/giftplatform/asset/GalleryAsset.java#L1); [backend/src/main/java/com/giftplatform/monetization/CommercialController.java:1](../../src/main/java/com/giftplatform/monetization/CommercialController.java#L1); [backend/src/main/java/com/giftplatform/monetization/CommercialPurchaseService.java:1](../../src/main/java/com/giftplatform/monetization/CommercialPurchaseService.java#L1)

## notification_outbox

Type: **BASE TABLE**. Purpose: Notification intent/status history; a stored row does not establish transport delivery.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| event_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| notification_kind | character varying / varchar | NO | No SQL default | 40 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored notification kind. |
| recipient_kind | character varying / varchar | NO | No SQL default | 12 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored recipient kind. |
| channel | character varying / varchar | NO | No SQL default | 12 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored channel. |
| recipient_reference | character varying / varchar | YES | No SQL default | 160 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored recipient reference. |
| source_kind | character varying / varchar | NO | No SQL default | 32 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored source kind. |
| source_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| payment_evidence | character varying / varchar | NO | No SQL default | 20 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored payment evidence. |
| subject | character varying / varchar | YES | No SQL default | 200 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored subject. |
| message | text / text | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored message. |
| card_template | character varying / varchar | YES | No SQL default | 60 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored card template. |
| status | character varying / varchar | NO | `'PENDING'::character varying` | 20 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. State value; see CHECK constraints. |
| attempts | integer / int4 | NO | `0` | None / 32 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored attempts. |
| next_attempt_at | timestamp with time zone / timestamptz | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored next attempt at. |
| created_at | timestamp with time zone / timestamptz | NO | `CURRENT_TIMESTAMP` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored created at. |
| sent_at | timestamp with time zone / timestamptz | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored sent at. |
| last_error_code | character varying / varchar | YES | No SQL default | 100 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored last error code. |
| read_at | timestamp with time zone / timestamptz | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored read at. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| notification_outbox_attempts_check | c | `CHECK ((attempts >= 0))` |
| notification_outbox_channel_check | c | `CHECK (((channel)::text = ANY ((ARRAY['IN_APP'::character varying, 'EMAIL'::character varying, 'SMS'::character varying, 'WHATSAPP'::character varying])::text[])))` |
| notification_outbox_event_id_fkey | f | `FOREIGN KEY (event_id) REFERENCES events(id)` |
| notification_outbox_event_id_notification_kind_recipient_ki_key | u | `UNIQUE (event_id, notification_kind, recipient_kind, channel, source_kind, source_id)` |
| notification_outbox_message_check | c | `CHECK (((length(message) >= 1) AND (length(message) <= 10000)))` |
| notification_outbox_payment_evidence_check | c | `CHECK (((payment_evidence)::text = ANY ((ARRAY['GUEST_REPORTED'::character varying, 'CAPTURED'::character varying, 'CHECKOUT_PENDING'::character varying])::text[])))` |
| notification_outbox_pkey | p | `PRIMARY KEY (id)` |
| notification_outbox_recipient_kind_check | c | `CHECK (((recipient_kind)::text = ANY ((ARRAY['GUEST'::character varying, 'HOST'::character varying])::text[])))` |
| notification_outbox_source_kind_check | c | `CHECK (((source_kind)::text = ANY ((ARRAY['MANUAL_CLAIM'::character varying, 'VERIFIED_CAPTURE'::character varying, 'RAZORPAY_ORDER'::character varying])::text[])))` |
| notification_outbox_status_check | c | `CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'SENDING'::character varying, 'SENT'::character varying, 'FAILED'::character varying, 'CANCELLED'::character varying])::text[])))` |


### Indexes

| Index | Definition |
| --- | --- |
| idx_notification_host_unread | `CREATE INDEX idx_notification_host_unread ON public.notification_outbox USING btree (event_id, created_at DESC) WHERE (((recipient_kind)::text = 'HOST'::text) AND ((channel)::text = 'IN_APP'::text) AND (read_at IS NULL))` |
| idx_notification_outbox_delivery | `CREATE INDEX idx_notification_outbox_delivery ON public.notification_outbox USING btree (status, next_attempt_at) WHERE ((status)::text = ANY ((ARRAY['PENDING'::character varying, 'FAILED'::character varying])::text[]))` |
| idx_notification_outbox_event_created | `CREATE INDEX idx_notification_outbox_event_created ON public.notification_outbox USING btree (event_id, created_at DESC)` |
| notification_outbox_event_id_notification_kind_recipient_ki_key | `CREATE UNIQUE INDEX notification_outbox_event_id_notification_kind_recipient_ki_key ON public.notification_outbox USING btree (event_id, notification_kind, recipient_kind, channel, source_kind, source_id)` |
| notification_outbox_pkey | `CREATE UNIQUE INDEX notification_outbox_pkey ON public.notification_outbox USING btree (id)` |


Consumers: [backend/src/main/java/com/giftplatform/admin/PlatformAdminConsoleController.java:1](../../src/main/java/com/giftplatform/admin/PlatformAdminConsoleController.java#L1); [backend/src/main/java/com/giftplatform/notification/HostInboxController.java:1](../../src/main/java/com/giftplatform/notification/HostInboxController.java#L1); [backend/src/main/java/com/giftplatform/notification/HostThankYouService.java:1](../../src/main/java/com/giftplatform/notification/HostThankYouService.java#L1); [backend/src/main/java/com/giftplatform/notification/ManualClaimNotificationService.java:1](../../src/main/java/com/giftplatform/notification/ManualClaimNotificationService.java#L1); [backend/src/main/java/com/giftplatform/payment/service/GuestCheckoutService.java:1](../../src/main/java/com/giftplatform/payment/service/GuestCheckoutService.java#L1); [backend/src/main/java/com/giftplatform/payment/service/RazorpayCaptureService.java:1](../../src/main/java/com/giftplatform/payment/service/RazorpayCaptureService.java#L1)

## otp_request_rate_limits

Type: **BASE TABLE**. Purpose: Authentication, contact verification, account recovery, throttling or session invalidation metadata.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| scope | character varying / varchar | NO | No SQL default | 16 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored scope. |
| subject_key | character varying / varchar | NO | No SQL default | 128 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored subject key. |
| window_start | timestamp with time zone / timestamptz | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored window start. |
| request_count | integer / int4 | NO | No SQL default | None / 32 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored request count. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| otp_request_rate_limits_pkey | p | `PRIMARY KEY (scope, subject_key)` |
| otp_request_rate_limits_request_count_check | c | `CHECK ((request_count >= 0))` |


### Indexes

| Index | Definition |
| --- | --- |
| ix_otp_request_rate_limits_window | `CREATE INDEX ix_otp_request_rate_limits_window ON public.otp_request_rate_limits USING btree (window_start)` |
| otp_request_rate_limits_pkey | `CREATE UNIQUE INDEX otp_request_rate_limits_pkey ON public.otp_request_rate_limits USING btree (scope, subject_key)` |


Consumers: [backend/src/main/java/com/giftplatform/otp/service/OtpEndpointRateLimiter.java:1](../../src/main/java/com/giftplatform/otp/service/OtpEndpointRateLimiter.java#L1); [backend/src/main/java/com/giftplatform/otp/service/OtpRequestRateLimiter.java:1](../../src/main/java/com/giftplatform/otp/service/OtpRequestRateLimiter.java#L1)

## payment_channel_controls

Type: **BASE TABLE**. Purpose: Administrative configuration, grants, revision controls or change history; use the documented admin workflow.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| scope | character varying / varchar | NO | No SQL default | 10 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored scope. |
| scope_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| channel | character varying / varchar | NO | No SQL default | 20 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored channel. |
| enabled | boolean / bool | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored enabled. |
| changed_by | uuid / uuid | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored changed by. |
| changed_at | timestamp with time zone / timestamptz | NO | `now()` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored changed at. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| payment_channel_controls_changed_by_fkey | f | `FOREIGN KEY (changed_by) REFERENCES users(id)` |
| payment_channel_controls_channel_check | c | `CHECK (((channel)::text = ANY ((ARRAY['UPI'::character varying, 'BANK'::character varying, 'RAZORPAY'::character varying, 'ROUTING'::character varying])::text[])))` |
| payment_channel_controls_check | c | `CHECK ((((scope)::text <> 'PLATFORM'::text) OR (scope_id = '00000000-0000-0000-0000-000000000000'::uuid)))` |
| payment_channel_controls_pkey | p | `PRIMARY KEY (scope, scope_id, channel)` |
| payment_channel_controls_scope_check | c | `CHECK (((scope)::text = ANY ((ARRAY['PLATFORM'::character varying, 'HOST'::character varying, 'EVENT'::character varying])::text[])))` |


### Indexes

| Index | Definition |
| --- | --- |
| payment_channel_controls_pkey | `CREATE UNIQUE INDEX payment_channel_controls_pkey ON public.payment_channel_controls USING btree (scope, scope_id, channel)` |


Consumers: [backend/src/main/java/com/giftplatform/payment/control/PaymentControlService.java:1](../../src/main/java/com/giftplatform/payment/control/PaymentControlService.java#L1)

## payment_control_history

Type: **BASE TABLE**. Purpose: Administrative configuration, grants, revision controls or change history; use the documented admin workflow.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| revision | bigint / int8 | NO | No SQL default | None / 64 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored revision. |
| actor_id | uuid / uuid | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| scope | character varying / varchar | NO | No SQL default | 10 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored scope. |
| scope_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| channel | character varying / varchar | NO | No SQL default | 20 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored channel. |
| reason | character varying / varchar | NO | No SQL default | 500 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored reason. |
| before_enabled | boolean / bool | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored before enabled. |
| after_enabled | boolean / bool | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored after enabled. |
| created_at | timestamp with time zone / timestamptz | NO | `now()` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored created at. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| payment_control_history_actor_id_fkey | f | `FOREIGN KEY (actor_id) REFERENCES users(id)` |
| payment_control_history_pkey | p | `PRIMARY KEY (revision)` |


### Indexes

| Index | Definition |
| --- | --- |
| payment_control_history_pkey | `CREATE UNIQUE INDEX payment_control_history_pkey ON public.payment_control_history USING btree (revision)` |


Consumers: [backend/src/main/java/com/giftplatform/payment/control/PaymentControlService.java:1](../../src/main/java/com/giftplatform/payment/control/PaymentControlService.java#L1)

## payment_control_revision

Type: **BASE TABLE**. Purpose: Administrative configuration, grants, revision controls or change history; use the documented admin workflow.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| id | smallint / int2 | NO | No SQL default | None / 16 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| revision | bigint / int8 | NO | No SQL default | None / 64 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored revision. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| payment_control_revision_id_check | c | `CHECK ((id = 1))` |
| payment_control_revision_pkey | p | `PRIMARY KEY (id)` |


### Indexes

| Index | Definition |
| --- | --- |
| payment_control_revision_pkey | `CREATE UNIQUE INDEX payment_control_revision_pkey ON public.payment_control_revision USING btree (id)` |


Consumers: [backend/src/main/java/com/giftplatform/payment/control/PaymentControlService.java:1](../../src/main/java/com/giftplatform/payment/control/PaymentControlService.java#L1)

## payment_notifications

Type: **BASE TABLE**. Purpose: Payment, accounting, pricing, reconciliation or blocked-payout workflow. State and idempotency constraints are listed below.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| contribution_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| recipient_type | character varying / varchar | NO | No SQL default | 10 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored recipient type. |
| recipient_address | character varying / varchar | YES | No SQL default | 320 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored recipient address. |
| event_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| gift_amount_paise | bigint / int8 | NO | No SQL default | None / 64 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Money in integer paise; preserve distinction between reported, confirmed and settled. |
| wallet_amount_paise | bigint / int8 | NO | No SQL default | None / 64 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Money in integer paise; preserve distinction between reported, confirmed and settled. |
| status | character varying / varchar | NO | `'PENDING'::character varying` | 15 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. State value; see CHECK constraints. |
| created_at | timestamp with time zone / timestamptz | NO | `CURRENT_TIMESTAMP` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored created at. |
| sent_at | timestamp with time zone / timestamptz | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored sent at. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| payment_notifications_contribution_id_fkey | f | `FOREIGN KEY (contribution_id) REFERENCES contributions(id)` |
| payment_notifications_contribution_id_recipient_type_key | u | `UNIQUE (contribution_id, recipient_type)` |
| payment_notifications_event_id_fkey | f | `FOREIGN KEY (event_id) REFERENCES events(id)` |
| payment_notifications_gift_amount_paise_check | c | `CHECK ((gift_amount_paise >= 0))` |
| payment_notifications_pkey | p | `PRIMARY KEY (id)` |
| payment_notifications_recipient_type_check | c | `CHECK (((recipient_type)::text = ANY ((ARRAY['GUEST'::character varying, 'HOST'::character varying])::text[])))` |
| payment_notifications_status_check | c | `CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'SENT'::character varying, 'FAILED'::character varying])::text[])))` |
| payment_notifications_wallet_amount_paise_check | c | `CHECK ((wallet_amount_paise >= 0))` |


### Indexes

| Index | Definition |
| --- | --- |
| idx_payment_notifications_pending | `CREATE INDEX idx_payment_notifications_pending ON public.payment_notifications USING btree (created_at) WHERE ((status)::text = 'PENDING'::text)` |
| payment_notifications_contribution_id_recipient_type_key | `CREATE UNIQUE INDEX payment_notifications_contribution_id_recipient_type_key ON public.payment_notifications USING btree (contribution_id, recipient_type)` |
| payment_notifications_pkey | `CREATE UNIQUE INDEX payment_notifications_pkey ON public.payment_notifications USING btree (id)` |


Consumers: [backend/src/main/java/com/giftplatform/payment/service/PaymentNotificationService.java:1](../../src/main/java/com/giftplatform/payment/service/PaymentNotificationService.java#L1)

## payment_orders

Type: **BASE TABLE**. Purpose: Payment, accounting, pricing, reconciliation or blocked-payout workflow. State and idempotency constraints are listed below.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| event_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| gift_id | uuid / uuid | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| provider | character varying / varchar | NO | `'RAZORPAY'::character varying` | 20 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored provider. |
| provider_order_id | character varying / varchar | YES | No SQL default | 100 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| contribution_type | character varying / varchar | NO | No SQL default | 30 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored contribution type. |
| amount_paise | bigint / int8 | NO | No SQL default | None / 64 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Money in integer paise; preserve distinction between reported, confirmed and settled. |
| currency | character varying / varchar | NO | `'INR'::character varying` | 3 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored currency. |
| status | character varying / varchar | NO | `'CREATED'::character varying` | 30 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. State value; see CHECK constraints. |
| idempotency_key | character varying / varchar | NO | No SQL default | 120 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored idempotency key. |
| created_at | timestamp with time zone / timestamptz | NO | `CURRENT_TIMESTAMP` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored created at. |
| updated_at | timestamp with time zone / timestamptz | NO | `CURRENT_TIMESTAMP` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored updated at. |
| requested_amount_paise | bigint / int8 | NO | No SQL default | None / 64 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Money in integer paise; preserve distinction between reported, confirmed and settled. |
| platform_fee_paise | bigint / int8 | NO | `0` | None / 64 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Money in integer paise; preserve distinction between reported, confirmed and settled. |
| host_receives_paise | bigint / int8 | YES | No SQL default | None / 64 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Money in integer paise; preserve distinction between reported, confirmed and settled. |
| monetization_revision | bigint / int8 | YES | No SQL default | None / 64 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored monetization revision. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| ck_order_fee_snapshot | c | `CHECK (((requested_amount_paise > 0) AND (platform_fee_paise >= 0) AND (COALESCE(host_receives_paise, amount_paise) > 0) AND (amount_paise = (COALESCE(host_receives_paise, amount_paise) + platform_fee_paise)) AND ((requested_amount_paise = amount_paise) OR (requested_amount_paise = COALESCE(host_receives_paise, amount_paise)))))` |
| ck_payment_order_currency | c | `CHECK (((currency)::text = 'INR'::text))` |
| ck_payment_order_gift | c | `CHECK (((((contribution_type)::text = 'EVENT_WALLET'::text) AND (gift_id IS NULL)) OR (((contribution_type)::text = ANY ((ARRAY['GIFT_PARTIAL'::character varying, 'GIFT_FULL'::character varying])::text[])) AND (gift_id IS NOT NULL))))` |
| ck_payment_order_status | c | `CHECK (((status)::text = ANY ((ARRAY['CREATED'::character varying, 'PENDING'::character varying, 'AUTHORIZED'::character varying, 'CAPTURED'::character varying, 'FAILED'::character varying, 'CANCELLED'::character varying])::text[])))` |
| ck_payment_order_type | c | `CHECK (((contribution_type)::text = ANY ((ARRAY['GIFT_PARTIAL'::character varying, 'GIFT_FULL'::character varying, 'EVENT_WALLET'::character varying])::text[])))` |
| fk_payment_order_gift_event | f | `FOREIGN KEY (gift_id, event_id) REFERENCES gifts(id, event_id)` |
| payment_orders_amount_paise_check | c | `CHECK ((amount_paise > 0))` |
| payment_orders_event_id_fkey | f | `FOREIGN KEY (event_id) REFERENCES events(id)` |
| payment_orders_gift_id_fkey | f | `FOREIGN KEY (gift_id) REFERENCES gifts(id)` |
| payment_orders_idempotency_key_key | u | `UNIQUE (idempotency_key)` |
| payment_orders_monetization_revision_fkey | f | `FOREIGN KEY (monetization_revision) REFERENCES platform_monetization_history(revision)` |
| payment_orders_pkey | p | `PRIMARY KEY (id)` |
| payment_orders_provider_order_id_key | u | `UNIQUE (provider_order_id)` |


### Indexes

| Index | Definition |
| --- | --- |
| idx_payment_orders_event | `CREATE INDEX idx_payment_orders_event ON public.payment_orders USING btree (event_id, created_at)` |
| idx_payment_orders_gift | `CREATE INDEX idx_payment_orders_gift ON public.payment_orders USING btree (gift_id)` |
| idx_payment_orders_gift_event | `CREATE INDEX idx_payment_orders_gift_event ON public.payment_orders USING btree (gift_id, event_id)` |
| payment_orders_idempotency_key_key | `CREATE UNIQUE INDEX payment_orders_idempotency_key_key ON public.payment_orders USING btree (idempotency_key)` |
| payment_orders_pkey | `CREATE UNIQUE INDEX payment_orders_pkey ON public.payment_orders USING btree (id)` |
| payment_orders_provider_order_id_key | `CREATE UNIQUE INDEX payment_orders_provider_order_id_key ON public.payment_orders USING btree (provider_order_id)` |


Consumers: [backend/src/main/java/com/giftplatform/monetization/CommercialController.java:1](../../src/main/java/com/giftplatform/monetization/CommercialController.java#L1); [backend/src/main/java/com/giftplatform/payment/control/PaymentControlController.java:1](../../src/main/java/com/giftplatform/payment/control/PaymentControlController.java#L1); [backend/src/main/java/com/giftplatform/payment/controller/PublicGiftProgressController.java:1](../../src/main/java/com/giftplatform/payment/controller/PublicGiftProgressController.java#L1); [backend/src/main/java/com/giftplatform/payment/direct/GuestDirectTransferService.java:1](../../src/main/java/com/giftplatform/payment/direct/GuestDirectTransferService.java#L1); [backend/src/main/java/com/giftplatform/payment/entity/PaymentOrder.java:1](../../src/main/java/com/giftplatform/payment/entity/PaymentOrder.java#L1); [backend/src/main/java/com/giftplatform/payment/report/HostContributionReportService.java:1](../../src/main/java/com/giftplatform/payment/report/HostContributionReportService.java#L1); [backend/src/main/java/com/giftplatform/payment/service/ContributionAllocationService.java:1](../../src/main/java/com/giftplatform/payment/service/ContributionAllocationService.java#L1); [backend/src/main/java/com/giftplatform/payment/service/PaymentNotificationService.java:1](../../src/main/java/com/giftplatform/payment/service/PaymentNotificationService.java#L1); [backend/src/main/java/com/giftplatform/payment/service/RazorpayCaptureService.java:1](../../src/main/java/com/giftplatform/payment/service/RazorpayCaptureService.java#L1); [backend/src/main/java/com/giftplatform/payment/service/RazorpayOrderCreationService.java:1](../../src/main/java/com/giftplatform/payment/service/RazorpayOrderCreationService.java#L1); [backend/src/main/java/com/giftplatform/payment/service/RazorpayWebhookService.java:1](../../src/main/java/com/giftplatform/payment/service/RazorpayWebhookService.java#L1); [backend/src/main/java/com/giftplatform/payment/verification/HostTransactionVerificationController.java:1](../../src/main/java/com/giftplatform/payment/verification/HostTransactionVerificationController.java#L1)

## payment_transactions

Type: **BASE TABLE**. Purpose: Payment, accounting, pricing, reconciliation or blocked-payout workflow. State and idempotency constraints are listed below.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| payment_order_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| provider_payment_id | character varying / varchar | NO | No SQL default | 100 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| amount_paise | bigint / int8 | NO | No SQL default | None / 64 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Money in integer paise; preserve distinction between reported, confirmed and settled. |
| currency | character varying / varchar | NO | `'INR'::character varying` | 3 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored currency. |
| status | character varying / varchar | NO | No SQL default | 30 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. State value; see CHECK constraints. |
| verified_at | timestamp with time zone / timestamptz | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored verified at. |
| created_at | timestamp with time zone / timestamptz | NO | `CURRENT_TIMESTAMP` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored created at. |
| payment_method | text / text | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored payment method. |
| bank_code | text / text | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored bank code. |
| bank_transaction_id | text / text | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| rrn | text / text | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored rrn. |
| upi_transaction_id | text / text | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| provider_fee_paise | bigint / int8 | YES | No SQL default | None / 64 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Money in integer paise; preserve distinction between reported, confirmed and settled. |
| provider_tax_paise | bigint / int8 | YES | No SQL default | None / 64 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Money in integer paise; preserve distinction between reported, confirmed and settled. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| ck_payment_transaction_status | c | `CHECK (((status)::text = ANY ((ARRAY['AUTHORIZED'::character varying, 'CAPTURED'::character varying, 'FAILED'::character varying, 'REFUNDED'::character varying, 'PARTIALLY_REFUNDED'::character varying])::text[])))` |
| payment_transactions_amount_paise_check | c | `CHECK ((amount_paise > 0))` |
| payment_transactions_payment_order_id_fkey | f | `FOREIGN KEY (payment_order_id) REFERENCES payment_orders(id)` |
| payment_transactions_pkey | p | `PRIMARY KEY (id)` |
| payment_transactions_provider_fee_paise_check | c | `CHECK ((provider_fee_paise >= 0))` |
| payment_transactions_provider_payment_id_key | u | `UNIQUE (provider_payment_id)` |
| payment_transactions_provider_tax_paise_check | c | `CHECK ((provider_tax_paise >= 0))` |


### Indexes

| Index | Definition |
| --- | --- |
| payment_transactions_pkey | `CREATE UNIQUE INDEX payment_transactions_pkey ON public.payment_transactions USING btree (id)` |
| payment_transactions_provider_payment_id_key | `CREATE UNIQUE INDEX payment_transactions_provider_payment_id_key ON public.payment_transactions USING btree (provider_payment_id)` |


Consumers: [backend/src/main/java/com/giftplatform/monetization/CommercialController.java:1](../../src/main/java/com/giftplatform/monetization/CommercialController.java#L1); [backend/src/main/java/com/giftplatform/payment/control/PaymentControlController.java:1](../../src/main/java/com/giftplatform/payment/control/PaymentControlController.java#L1); [backend/src/main/java/com/giftplatform/payment/entity/PaymentTransaction.java:1](../../src/main/java/com/giftplatform/payment/entity/PaymentTransaction.java#L1); [backend/src/main/java/com/giftplatform/payment/report/HostContributionReportService.java:1](../../src/main/java/com/giftplatform/payment/report/HostContributionReportService.java#L1); [backend/src/main/java/com/giftplatform/payment/service/ContributionAllocationService.java:1](../../src/main/java/com/giftplatform/payment/service/ContributionAllocationService.java#L1); [backend/src/main/java/com/giftplatform/payment/service/RazorpayCaptureService.java:1](../../src/main/java/com/giftplatform/payment/service/RazorpayCaptureService.java#L1); [backend/src/main/java/com/giftplatform/payment/verification/HostTransactionVerificationController.java:1](../../src/main/java/com/giftplatform/payment/verification/HostTransactionVerificationController.java#L1)

## payment_webhook_events

Type: **BASE TABLE**. Purpose: Payment, accounting, pricing, reconciliation or blocked-payout workflow. State and idempotency constraints are listed below.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| provider | character varying / varchar | NO | `'RAZORPAY'::character varying` | 20 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored provider. |
| provider_event_id | character varying / varchar | NO | No SQL default | 150 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| event_type | character varying / varchar | NO | No SQL default | 100 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored event type. |
| status | character varying / varchar | NO | `'RECEIVED'::character varying` | 30 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. State value; see CHECK constraints. |
| received_at | timestamp with time zone / timestamptz | NO | `CURRENT_TIMESTAMP` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored received at. |
| processed_at | timestamp with time zone / timestamptz | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored processed at. |
| raw_payload | text / text | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored raw payload. |
| attempt_count | integer / int4 | NO | `0` | None / 32 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored attempt count. |
| last_error | text / text | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored last error. |
| last_attempt_at | timestamp with time zone / timestamptz | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored last attempt at. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| ck_webhook_attempt_count | c | `CHECK ((attempt_count >= 0))` |
| ck_webhook_status | c | `CHECK (((status)::text = ANY ((ARRAY['RECEIVED'::character varying, 'PROCESSED'::character varying, 'FAILED'::character varying])::text[])))` |
| payment_webhook_events_pkey | p | `PRIMARY KEY (id)` |
| payment_webhook_events_provider_provider_event_id_key | u | `UNIQUE (provider, provider_event_id)` |


### Indexes

| Index | Definition |
| --- | --- |
| idx_payment_webhook_retry | `CREATE INDEX idx_payment_webhook_retry ON public.payment_webhook_events USING btree (status, received_at) WHERE ((status)::text = ANY ((ARRAY['RECEIVED'::character varying, 'FAILED'::character varying])::text[]))` |
| payment_webhook_events_pkey | `CREATE UNIQUE INDEX payment_webhook_events_pkey ON public.payment_webhook_events USING btree (id)` |
| payment_webhook_events_provider_provider_event_id_key | `CREATE UNIQUE INDEX payment_webhook_events_provider_provider_event_id_key ON public.payment_webhook_events USING btree (provider, provider_event_id)` |


Consumers: [backend/src/main/java/com/giftplatform/payment/entity/PaymentWebhookEvent.java:1](../../src/main/java/com/giftplatform/payment/entity/PaymentWebhookEvent.java#L1); [backend/src/main/java/com/giftplatform/payment/service/RazorpayWebhookService.java:1](../../src/main/java/com/giftplatform/payment/service/RazorpayWebhookService.java#L1)

## payout_reconciliation_observations

Type: **BASE TABLE**. Purpose: Payment, accounting, pricing, reconciliation or blocked-payout workflow. State and idempotency constraints are listed below.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| payout_request_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| source | character varying / varchar | NO | No SQL default | 32 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored source. |
| observed_state | character varying / varchar | NO | No SQL default | 24 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored observed state. |
| correlation_reference | character varying / varchar | YES | No SQL default | 160 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored correlation reference. |
| observed_at | timestamp with time zone / timestamptz | NO | `CURRENT_TIMESTAMP` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored observed at. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| ck_provider_observation_reference | c | `CHECK ((((source)::text <> 'PROVIDER_PREVIEW'::text) OR (correlation_reference IS NOT NULL)))` |
| payout_reconciliation_observations_observed_state_check | c | `CHECK (((observed_state)::text = ANY ((ARRAY['UNKNOWN'::character varying, 'PENDING'::character varying, 'FAILED'::character varying, 'SUCCEEDED'::character varying])::text[])))` |
| payout_reconciliation_observations_payout_request_id_fkey | f | `FOREIGN KEY (payout_request_id) REFERENCES host_payout_requests(id)` |
| payout_reconciliation_observations_pkey | p | `PRIMARY KEY (id)` |
| payout_reconciliation_observations_source_check | c | `CHECK (((source)::text = ANY ((ARRAY['INTERNAL_REVIEW'::character varying, 'PROVIDER_PREVIEW'::character varying])::text[])))` |


### Indexes

| Index | Definition |
| --- | --- |
| idx_payout_observations_request | `CREATE INDEX idx_payout_observations_request ON public.payout_reconciliation_observations USING btree (payout_request_id, observed_at DESC)` |
| payout_reconciliation_observations_pkey | `CREATE UNIQUE INDEX payout_reconciliation_observations_pkey ON public.payout_reconciliation_observations USING btree (id)` |


Consumers: [backend/src/main/java/com/giftplatform/payment/settlement/PayoutSafetyFinalizationService.java:1](../../src/main/java/com/giftplatform/payment/settlement/PayoutSafetyFinalizationService.java#L1)

## payout_reservations

Type: **BASE TABLE**. Purpose: Payment, accounting, pricing, reconciliation or blocked-payout workflow. State and idempotency constraints are listed below.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| payout_request_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| event_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| amount_paise | bigint / int8 | NO | No SQL default | None / 64 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Money in integer paise; preserve distinction between reported, confirmed and settled. |
| state | character varying / varchar | NO | `'HELD'::character varying` | 16 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. State value; see CHECK constraints. |
| created_at | timestamp with time zone / timestamptz | NO | `CURRENT_TIMESTAMP` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored created at. |
| released_at | timestamp with time zone / timestamptz | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored released at. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| ck_payout_reservation_release | c | `CHECK (((((state)::text = 'HELD'::text) AND (released_at IS NULL)) OR (((state)::text = 'RELEASED'::text) AND (released_at IS NOT NULL))))` |
| payout_reservations_amount_paise_check | c | `CHECK ((amount_paise > 0))` |
| payout_reservations_event_id_fkey | f | `FOREIGN KEY (event_id) REFERENCES events(id)` |
| payout_reservations_payout_request_id_fkey | f | `FOREIGN KEY (payout_request_id) REFERENCES host_payout_requests(id)` |
| payout_reservations_payout_request_id_key | u | `UNIQUE (payout_request_id)` |
| payout_reservations_pkey | p | `PRIMARY KEY (id)` |
| payout_reservations_state_check | c | `CHECK (((state)::text = ANY ((ARRAY['HELD'::character varying, 'RELEASED'::character varying])::text[])))` |


### Indexes

| Index | Definition |
| --- | --- |
| idx_payout_reservations_event_state | `CREATE INDEX idx_payout_reservations_event_state ON public.payout_reservations USING btree (event_id, state)` |
| payout_reservations_payout_request_id_key | `CREATE UNIQUE INDEX payout_reservations_payout_request_id_key ON public.payout_reservations USING btree (payout_request_id)` |
| payout_reservations_pkey | `CREATE UNIQUE INDEX payout_reservations_pkey ON public.payout_reservations USING btree (id)` |


Consumers: [backend/src/main/java/com/giftplatform/payment/settlement/PayoutReservationService.java:1](../../src/main/java/com/giftplatform/payment/settlement/PayoutReservationService.java#L1); [backend/src/main/java/com/giftplatform/payment/settlement/PayoutSafetyFinalizationService.java:1](../../src/main/java/com/giftplatform/payment/settlement/PayoutSafetyFinalizationService.java#L1); [backend/src/main/java/com/giftplatform/payment/settlement/SettlementBalanceService.java:1](../../src/main/java/com/giftplatform/payment/settlement/SettlementBalanceService.java#L1)

## phone_otp_challenges

Type: **BASE TABLE**. Purpose: Authentication, contact verification, account recovery, throttling or session invalidation metadata.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| user_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| phone_number | character varying / varchar | NO | No SQL default | 16 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored phone number. |
| otp_digest | character varying / varchar | NO | No SQL default | 128 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored otp digest. |
| created_at | timestamp with time zone / timestamptz | NO | `CURRENT_TIMESTAMP` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored created at. |
| expires_at | timestamp with time zone / timestamptz | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored expires at. |
| consumed_at | timestamp with time zone / timestamptz | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored consumed at. |
| attempt_count | integer / int4 | NO | `0` | None / 32 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored attempt count. |
| delivery_status | character varying / varchar | NO | `'PENDING'::character varying` | 20 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. State value; see CHECK constraints. |
| delivery_updated_at | timestamp with time zone / timestamptz | NO | `CURRENT_TIMESTAMP` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored delivery updated at. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| chk_otp_attempt_count | c | `CHECK (((attempt_count >= 0) AND (attempt_count <= 5)))` |
| chk_otp_consumption | c | `CHECK (((consumed_at IS NULL) OR (consumed_at >= created_at)))` |
| chk_otp_delivery_status | c | `CHECK (((delivery_status)::text = ANY ((ARRAY['PENDING'::character varying, 'SENT'::character varying, 'FAILED'::character varying, 'INVALIDATED'::character varying])::text[])))` |
| chk_otp_expiration | c | `CHECK ((expires_at > created_at))` |
| chk_otp_phone_format | c | `CHECK (((phone_number)::text ~ '^\+[1-9][0-9]{1,14}$'::text))` |
| phone_otp_challenges_pkey | p | `PRIMARY KEY (id)` |
| phone_otp_challenges_user_id_fkey | f | `FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE` |


### Indexes

| Index | Definition |
| --- | --- |
| ix_otp_user_delivery | `CREATE INDEX ix_otp_user_delivery ON public.phone_otp_challenges USING btree (user_id, delivery_status, created_at DESC)` |
| ix_phone_otp_expiration | `CREATE INDEX ix_phone_otp_expiration ON public.phone_otp_challenges USING btree (expires_at)` |
| ix_phone_otp_user_created | `CREATE INDEX ix_phone_otp_user_created ON public.phone_otp_challenges USING btree (user_id, created_at DESC)` |
| phone_otp_challenges_pkey | `CREATE UNIQUE INDEX phone_otp_challenges_pkey ON public.phone_otp_challenges USING btree (id)` |
| ux_otp_active_user | `CREATE UNIQUE INDEX ux_otp_active_user ON public.phone_otp_challenges USING btree (user_id) WHERE ((consumed_at IS NULL) AND ((delivery_status)::text = ANY ((ARRAY['PENDING'::character varying, 'SENT'::character varying])::text[])))` |


Consumers: [backend/src/main/java/com/giftplatform/otp/entity/PhoneOtpChallenge.java:1](../../src/main/java/com/giftplatform/otp/entity/PhoneOtpChallenge.java#L1)

## platform_admin_action_audit

Type: **BASE TABLE**. Purpose: Administrative configuration, grants, revision controls or change history; use the documented admin workflow.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| actor_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| action | character varying / varchar | NO | No SQL default | 60 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored action. |
| target_id | uuid / uuid | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| module_code | character varying / varchar | YES | No SQL default | 40 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored module code. |
| created_at | timestamp with time zone / timestamptz | NO | `now()` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored created at. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| platform_admin_action_audit_actor_id_fkey | f | `FOREIGN KEY (actor_id) REFERENCES users(id)` |
| platform_admin_action_audit_pkey | p | `PRIMARY KEY (id)` |


### Indexes

| Index | Definition |
| --- | --- |
| platform_admin_action_audit_pkey | `CREATE UNIQUE INDEX platform_admin_action_audit_pkey ON public.platform_admin_action_audit USING btree (id)` |
| platform_admin_audit_page_idx | `CREATE INDEX platform_admin_audit_page_idx ON public.platform_admin_action_audit USING btree (created_at DESC, id DESC)` |


Consumers: [backend/src/main/java/com/giftplatform/admin/AccountPolicyController.java:1](../../src/main/java/com/giftplatform/admin/AccountPolicyController.java#L1); [backend/src/main/java/com/giftplatform/admin/ExperienceSettingsService.java:1](../../src/main/java/com/giftplatform/admin/ExperienceSettingsService.java#L1); [backend/src/main/java/com/giftplatform/admin/GroupMemberEmailPolicyController.java:1](../../src/main/java/com/giftplatform/admin/GroupMemberEmailPolicyController.java#L1); [backend/src/main/java/com/giftplatform/admin/PlatformAdminAuditController.java:1](../../src/main/java/com/giftplatform/admin/PlatformAdminAuditController.java#L1); [backend/src/main/java/com/giftplatform/admin/PlatformAdminConsoleController.java:1](../../src/main/java/com/giftplatform/admin/PlatformAdminConsoleController.java#L1); [backend/src/main/java/com/giftplatform/monetization/MonetizationService.java:1](../../src/main/java/com/giftplatform/monetization/MonetizationService.java#L1); [backend/src/main/java/com/giftplatform/payment/control/RoutingCommissionService.java:1](../../src/main/java/com/giftplatform/payment/control/RoutingCommissionService.java#L1); [backend/src/main/java/com/giftplatform/security/verification/VerificationSettingsController.java:1](../../src/main/java/com/giftplatform/security/verification/VerificationSettingsController.java#L1)

## platform_event_ai_design_settings

Type: **BASE TABLE**. Purpose: Administrative configuration, grants, revision controls or change history; use the documented admin workflow.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| event_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| enabled | boolean / bool | NO | `false` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored enabled. |
| changed_by | uuid / uuid | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored changed by. |
| changed_at | timestamp with time zone / timestamptz | NO | `now()` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored changed at. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| platform_event_ai_design_settings_changed_by_fkey | f | `FOREIGN KEY (changed_by) REFERENCES users(id)` |
| platform_event_ai_design_settings_event_id_fkey | f | `FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE` |
| platform_event_ai_design_settings_pkey | p | `PRIMARY KEY (event_id)` |


### Indexes

| Index | Definition |
| --- | --- |
| platform_event_ai_design_settings_pkey | `CREATE UNIQUE INDEX platform_event_ai_design_settings_pkey ON public.platform_event_ai_design_settings USING btree (event_id)` |


Consumers: [backend/src/main/java/com/giftplatform/admin/AiInvitationAdminController.java:1](../../src/main/java/com/giftplatform/admin/AiInvitationAdminController.java#L1); [backend/src/main/java/com/giftplatform/admin/EffectiveModulePermissionService.java:1](../../src/main/java/com/giftplatform/admin/EffectiveModulePermissionService.java#L1); [backend/src/main/java/com/giftplatform/admin/PlatformAdminConsoleController.java:1](../../src/main/java/com/giftplatform/admin/PlatformAdminConsoleController.java#L1); [backend/src/main/java/com/giftplatform/qr/design/ai/AiInvitationService.java:1](../../src/main/java/com/giftplatform/qr/design/ai/AiInvitationService.java#L1)

## platform_event_album_settings

Type: **BASE TABLE**. Purpose: Administrative configuration, grants, revision controls or change history; use the documented admin workflow.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| event_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| enabled | boolean / bool | NO | `false` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored enabled. |
| changed_by | uuid / uuid | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored changed by. |
| changed_at | timestamp with time zone / timestamptz | NO | `now()` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored changed at. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| platform_event_album_settings_changed_by_fkey | f | `FOREIGN KEY (changed_by) REFERENCES users(id)` |
| platform_event_album_settings_event_id_fkey | f | `FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE` |
| platform_event_album_settings_pkey | p | `PRIMARY KEY (event_id)` |


### Indexes

| Index | Definition |
| --- | --- |
| platform_event_album_settings_pkey | `CREATE UNIQUE INDEX platform_event_album_settings_pkey ON public.platform_event_album_settings USING btree (event_id)` |


Consumers: [backend/src/main/java/com/giftplatform/admin/EffectiveModulePermissionService.java:1](../../src/main/java/com/giftplatform/admin/EffectiveModulePermissionService.java#L1); [backend/src/main/java/com/giftplatform/admin/PlatformAdminConsoleController.java:1](../../src/main/java/com/giftplatform/admin/PlatformAdminConsoleController.java#L1)

## platform_event_message_settings

Type: **BASE TABLE**. Purpose: Administrative configuration, grants, revision controls or change history; use the documented admin workflow.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| event_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| display_mode | character varying / varchar | NO | `'AUTO'::character varying` | 20 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored display mode. |
| changed_at | timestamp with time zone / timestamptz | NO | `now()` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored changed at. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| platform_event_message_settings_display_mode_check | c | `CHECK (((display_mode)::text = ANY ((ARRAY['AUTO'::character varying, 'APPROVAL'::character varying])::text[])))` |
| platform_event_message_settings_event_id_fkey | f | `FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE` |
| platform_event_message_settings_pkey | p | `PRIMARY KEY (event_id)` |


### Indexes

| Index | Definition |
| --- | --- |
| platform_event_message_settings_pkey | `CREATE UNIQUE INDEX platform_event_message_settings_pkey ON public.platform_event_message_settings USING btree (event_id)` |


Consumers: [backend/src/main/java/com/giftplatform/admin/EffectiveModulePermissionService.java:1](../../src/main/java/com/giftplatform/admin/EffectiveModulePermissionService.java#L1); [backend/src/main/java/com/giftplatform/celebration/CelebrationController.java:1](../../src/main/java/com/giftplatform/celebration/CelebrationController.java#L1)

## platform_experience_settings

Type: **BASE TABLE**. Purpose: Administrative configuration, grants, revision controls or change history; use the documented admin workflow.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| id | smallint / int2 | NO | No SQL default | None / 16 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| view_mode | character varying / varchar | NO | `'CLASSIC'::character varying` | 16 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored view mode. |
| changed_by | uuid / uuid | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored changed by. |
| changed_at | timestamp with time zone / timestamptz | NO | `now()` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored changed at. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| platform_experience_settings_changed_by_fkey | f | `FOREIGN KEY (changed_by) REFERENCES users(id)` |
| platform_experience_settings_id_check | c | `CHECK ((id = 1))` |
| platform_experience_settings_pkey | p | `PRIMARY KEY (id)` |
| platform_experience_settings_view_mode_check | c | `CHECK (((view_mode)::text = ANY ((ARRAY['CLASSIC'::character varying, 'FOCUSED'::character varying])::text[])))` |


### Indexes

| Index | Definition |
| --- | --- |
| platform_experience_settings_pkey | `CREATE UNIQUE INDEX platform_experience_settings_pkey ON public.platform_experience_settings USING btree (id)` |


Consumers: [backend/src/main/java/com/giftplatform/admin/ExperienceSettingsService.java:1](../../src/main/java/com/giftplatform/admin/ExperienceSettingsService.java#L1)

## platform_host_module_grants

Type: **BASE TABLE**. Purpose: Administrative configuration, grants, revision controls or change history; use the documented admin workflow.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| host_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| module_code | character varying / varchar | NO | No SQL default | 40 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored module code. |
| enabled | boolean / bool | NO | `false` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored enabled. |
| changed_by | uuid / uuid | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored changed by. |
| changed_at | timestamp with time zone / timestamptz | NO | `now()` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored changed at. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| platform_host_module_grants_changed_by_fkey | f | `FOREIGN KEY (changed_by) REFERENCES users(id)` |
| platform_host_module_grants_host_id_fkey | f | `FOREIGN KEY (host_id) REFERENCES users(id) ON DELETE CASCADE` |
| platform_host_module_grants_module_code_fkey | f | `FOREIGN KEY (module_code) REFERENCES platform_module_controls(module_code)` |
| platform_host_module_grants_pkey | p | `PRIMARY KEY (host_id, module_code)` |


### Indexes

| Index | Definition |
| --- | --- |
| platform_host_module_grants_pkey | `CREATE UNIQUE INDEX platform_host_module_grants_pkey ON public.platform_host_module_grants USING btree (host_id, module_code)` |


Consumers: [backend/src/main/java/com/giftplatform/admin/AiInvitationAdminController.java:1](../../src/main/java/com/giftplatform/admin/AiInvitationAdminController.java#L1); [backend/src/main/java/com/giftplatform/admin/EffectiveModulePermissionService.java:1](../../src/main/java/com/giftplatform/admin/EffectiveModulePermissionService.java#L1); [backend/src/main/java/com/giftplatform/admin/PlatformAdminConsoleController.java:1](../../src/main/java/com/giftplatform/admin/PlatformAdminConsoleController.java#L1)

## platform_module_controls

Type: **BASE TABLE**. Purpose: Administrative configuration, grants, revision controls or change history; use the documented admin workflow.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| module_code | character varying / varchar | NO | No SQL default | 40 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored module code. |
| enabled | boolean / bool | NO | `false` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored enabled. |
| changed_by | uuid / uuid | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored changed by. |
| changed_at | timestamp with time zone / timestamptz | NO | `now()` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored changed at. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| module_code_allowed | c | `CHECK (((module_code)::text = ANY ((ARRAY['GROUP_GIFTING'::character varying, 'GUEST_GIFTS'::character varying, 'RSVP'::character varying, 'GUEST_ATTENDANCE'::character varying, 'WHATSAPP_SHARING'::character varying, 'GUESTBOOK'::character varying, 'SOCIAL_SHARING'::character varying, 'MEMORY_ALBUM'::character varying, 'AI_INVITATION'::character varying])::text[])))` |
| platform_module_controls_changed_by_fkey | f | `FOREIGN KEY (changed_by) REFERENCES users(id)` |
| platform_module_controls_pkey | p | `PRIMARY KEY (module_code)` |


### Indexes

| Index | Definition |
| --- | --- |
| platform_module_controls_pkey | `CREATE UNIQUE INDEX platform_module_controls_pkey ON public.platform_module_controls USING btree (module_code)` |


Consumers: [backend/src/main/java/com/giftplatform/admin/AiInvitationAdminController.java:1](../../src/main/java/com/giftplatform/admin/AiInvitationAdminController.java#L1); [backend/src/main/java/com/giftplatform/admin/EffectiveModulePermissionService.java:1](../../src/main/java/com/giftplatform/admin/EffectiveModulePermissionService.java#L1); [backend/src/main/java/com/giftplatform/admin/PlatformAdminConsoleController.java:1](../../src/main/java/com/giftplatform/admin/PlatformAdminConsoleController.java#L1)

## platform_monetization_history

Type: **BASE TABLE**. Purpose: Administrative configuration, grants, revision controls or change history; use the documented admin workflow.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| revision | bigint / int8 | NO | No SQL default | None / 64 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored revision. |
| actor_id | uuid / uuid | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| reason | character varying / varchar | NO | No SQL default | 500 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored reason. |
| before_config | jsonb / jsonb | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored before config. |
| after_config | jsonb / jsonb | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored after config. |
| created_at | timestamp with time zone / timestamptz | NO | `now()` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored created at. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| platform_monetization_history_actor_id_fkey | f | `FOREIGN KEY (actor_id) REFERENCES users(id)` |
| platform_monetization_history_pkey | p | `PRIMARY KEY (revision)` |


### Indexes

| Index | Definition |
| --- | --- |
| platform_monetization_history_pkey | `CREATE UNIQUE INDEX platform_monetization_history_pkey ON public.platform_monetization_history USING btree (revision)` |


Consumers: [backend/src/main/java/com/giftplatform/monetization/MonetizationService.java:1](../../src/main/java/com/giftplatform/monetization/MonetizationService.java#L1)

## platform_monetization_settings

Type: **BASE TABLE**. Purpose: Administrative configuration, grants, revision controls or change history; use the documented admin workflow.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| id | smallint / int2 | NO | No SQL default | None / 16 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| enabled | boolean / bool | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored enabled. |
| revision | bigint / int8 | NO | No SQL default | None / 64 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored revision. |
| changed_by | uuid / uuid | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored changed by. |
| changed_at | timestamp with time zone / timestamptz | NO | `now()` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored changed at. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| platform_monetization_settings_changed_by_fkey | f | `FOREIGN KEY (changed_by) REFERENCES users(id)` |
| platform_monetization_settings_id_check | c | `CHECK ((id = 1))` |
| platform_monetization_settings_pkey | p | `PRIMARY KEY (id)` |
| platform_monetization_settings_revision_check | c | `CHECK ((revision >= 0))` |


### Indexes

| Index | Definition |
| --- | --- |
| platform_monetization_settings_pkey | `CREATE UNIQUE INDEX platform_monetization_settings_pkey ON public.platform_monetization_settings USING btree (id)` |


Consumers: [backend/src/main/java/com/giftplatform/monetization/MonetizationService.java:1](../../src/main/java/com/giftplatform/monetization/MonetizationService.java#L1)

## platform_monetization_streams

Type: **BASE TABLE**. Purpose: Administrative configuration, grants, revision controls or change history; use the documented admin workflow.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| code | character varying / varchar | NO | No SQL default | 40 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored code. |
| enabled | boolean / bool | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored enabled. |
| fee_mode | character varying / varchar | NO | No SQL default | 20 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored fee mode. |
| fixed_paise | bigint / int8 | NO | No SQL default | None / 64 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Money in integer paise; preserve distinction between reported, confirmed and settled. |
| rate_bps | integer / int4 | NO | No SQL default | None / 32 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored rate bps. |
| cap_paise | bigint / int8 | NO | No SQL default | None / 64 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Money in integer paise; preserve distinction between reported, confirmed and settled. |
| fee_bearer | character varying / varchar | NO | No SQL default | 20 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored fee bearer. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| platform_monetization_streams_cap_paise_check | c | `CHECK (((cap_paise >= 0) AND (cap_paise <= 100000000)))` |
| platform_monetization_streams_code_check | c | `CHECK (((code)::text = ANY ((ARRAY['CONTRIBUTION'::character varying, 'WALLET'::character varying, 'AFFILIATE'::character varying, 'PREMIUM_TEMPLATE'::character varying, 'HOST_PLAN'::character varying, 'VENDOR_LISTING'::character varying, 'SPONSORED_GIFT'::character varying, 'GROUP_GIFT'::character varying])::text[])))` |
| platform_monetization_streams_fee_bearer_check | c | `CHECK (((fee_bearer)::text = ANY ((ARRAY['GUEST'::character varying, 'HOST'::character varying])::text[])))` |
| platform_monetization_streams_fee_mode_check | c | `CHECK (((fee_mode)::text = ANY ((ARRAY['FIXED'::character varying, 'PERCENTAGE'::character varying])::text[])))` |
| platform_monetization_streams_fixed_paise_check | c | `CHECK (((fixed_paise >= 0) AND (fixed_paise <= 100000000)))` |
| platform_monetization_streams_pkey | p | `PRIMARY KEY (code)` |
| platform_monetization_streams_rate_bps_check | c | `CHECK (((rate_bps >= 0) AND (rate_bps <= 10000)))` |


### Indexes

| Index | Definition |
| --- | --- |
| platform_monetization_streams_pkey | `CREATE UNIQUE INDEX platform_monetization_streams_pkey ON public.platform_monetization_streams USING btree (code)` |


Consumers: [backend/src/main/java/com/giftplatform/monetization/MonetizationService.java:1](../../src/main/java/com/giftplatform/monetization/MonetizationService.java#L1)

## platform_verification_settings

Type: **BASE TABLE**. Purpose: Administrative configuration, grants, revision controls or change history; use the documented admin workflow.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| id | integer / int4 | NO | No SQL default | None / 32 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| mobile_otp_enabled | boolean / bool | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored mobile otp enabled. |
| email_verification_enabled | boolean / bool | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored email verification enabled. |
| changed_by | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored changed by. |
| changed_at | timestamp with time zone / timestamptz | NO | `now()` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored changed at. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| platform_verification_settings_changed_by_fkey | f | `FOREIGN KEY (changed_by) REFERENCES users(id)` |
| platform_verification_settings_id_check | c | `CHECK ((id = 1))` |
| platform_verification_settings_pkey | p | `PRIMARY KEY (id)` |


### Indexes

| Index | Definition |
| --- | --- |
| platform_verification_settings_pkey | `CREATE UNIQUE INDEX platform_verification_settings_pkey ON public.platform_verification_settings USING btree (id)` |


Consumers: [backend/src/main/java/com/giftplatform/security/verification/VerificationSettings.java:1](../../src/main/java/com/giftplatform/security/verification/VerificationSettings.java#L1); [backend/src/main/java/com/giftplatform/security/verification/VerificationSettingsController.java:1](../../src/main/java/com/giftplatform/security/verification/VerificationSettingsController.java#L1)

## qr_invitation_designs

Type: **BASE TABLE**. Purpose: Invitation/media/AI request metadata or validated retailer lookup tracking.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| event_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| name | character varying / varchar | NO | No SQL default | 100 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored name. |
| style | character varying / varchar | NO | No SQL default | 30 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored style. |
| greeting | character varying / varchar | YES | No SQL default | 180 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored greeting. |
| image_png | bytea / bytea | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored image png. |
| created_at | timestamp with time zone / timestamptz | NO | `CURRENT_TIMESTAMP` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored created at. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| ck_invitation_name | c | `CHECK ((length(btrim((name)::text)) > 0))` |
| ck_invitation_style | c | `CHECK (((style)::text = ANY ((ARRAY['PHOTO_FRAME'::character varying, 'FLORAL'::character varying, 'CELEBRATION'::character varying, 'MINIMAL'::character varying])::text[])))` |
| qr_invitation_designs_event_id_fkey | f | `FOREIGN KEY (event_id) REFERENCES events(id)` |
| qr_invitation_designs_pkey | p | `PRIMARY KEY (id)` |


### Indexes

| Index | Definition |
| --- | --- |
| idx_qr_invitation_event | `CREATE INDEX idx_qr_invitation_event ON public.qr_invitation_designs USING btree (event_id, created_at)` |
| qr_invitation_designs_pkey | `CREATE UNIQUE INDEX qr_invitation_designs_pkey ON public.qr_invitation_designs USING btree (id)` |


Consumers: [backend/src/main/java/com/giftplatform/qr/design/InvitationDesign.java:1](../../src/main/java/com/giftplatform/qr/design/InvitationDesign.java#L1)

## routing_commission_history

Type: **BASE TABLE**. Purpose: Monetization, commercial access, attribution, provider-reported statements or routing-policy history.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| revision | bigint / int8 | NO | No SQL default | None / 64 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored revision. |
| actor_id | uuid / uuid | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| before_rate_bps | integer / int4 | YES | No SQL default | None / 32 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored before rate bps. |
| after_rate_bps | integer / int4 | NO | No SQL default | None / 32 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored after rate bps. |
| reason | character varying / varchar | NO | No SQL default | 500 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored reason. |
| created_at | timestamp with time zone / timestamptz | NO | `now()` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored created at. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| routing_commission_history_actor_id_fkey | f | `FOREIGN KEY (actor_id) REFERENCES users(id)` |
| routing_commission_history_after_rate_bps_check | c | `CHECK (((after_rate_bps >= 0) AND (after_rate_bps <= 10000)))` |
| routing_commission_history_before_rate_bps_check | c | `CHECK (((before_rate_bps >= 0) AND (before_rate_bps <= 10000)))` |
| routing_commission_history_pkey | p | `PRIMARY KEY (revision)` |


### Indexes

| Index | Definition |
| --- | --- |
| routing_commission_history_pkey | `CREATE UNIQUE INDEX routing_commission_history_pkey ON public.routing_commission_history USING btree (revision)` |


Consumers: [backend/src/main/java/com/giftplatform/payment/control/RoutingCommissionService.java:1](../../src/main/java/com/giftplatform/payment/control/RoutingCommissionService.java#L1)

## routing_commission_settings

Type: **BASE TABLE**. Purpose: Monetization, commercial access, attribution, provider-reported statements or routing-policy history.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| id | smallint / int2 | NO | No SQL default | None / 16 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| revision | bigint / int8 | NO | No SQL default | None / 64 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored revision. |
| rate_bps | integer / int4 | NO | No SQL default | None / 32 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored rate bps. |
| changed_by | uuid / uuid | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored changed by. |
| changed_at | timestamp with time zone / timestamptz | NO | `now()` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored changed at. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| routing_commission_settings_changed_by_fkey | f | `FOREIGN KEY (changed_by) REFERENCES users(id)` |
| routing_commission_settings_id_check | c | `CHECK ((id = 1))` |
| routing_commission_settings_pkey | p | `PRIMARY KEY (id)` |
| routing_commission_settings_rate_bps_check | c | `CHECK (((rate_bps >= 0) AND (rate_bps <= 10000)))` |
| routing_commission_settings_revision_check | c | `CHECK ((revision >= 0))` |


### Indexes

| Index | Definition |
| --- | --- |
| routing_commission_settings_pkey | `CREATE UNIQUE INDEX routing_commission_settings_pkey ON public.routing_commission_settings USING btree (id)` |


Consumers: [backend/src/main/java/com/giftplatform/payment/control/RoutingCommissionService.java:1](../../src/main/java/com/giftplatform/payment/control/RoutingCommissionService.java#L1)

## signup_email_verifications

Type: **BASE TABLE**. Purpose: Authentication, contact verification, account recovery, throttling or session invalidation metadata.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| account_kind | character varying / varchar | NO | No SQL default | 20 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored account kind. |
| scope_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| email | character varying / varchar | NO | No SQL default | 254 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored email. |
| ip_fingerprint | character / bpchar | NO | No SQL default | 64 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored ip fingerprint. |
| secret_hash | character / bpchar | NO | No SQL default | 64 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Sensitive representation; do not expose plaintext or hashes. |
| proof_hash | character / bpchar | YES | No SQL default | 64 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Sensitive representation; do not expose plaintext or hashes. |
| attempt_count | integer / int4 | NO | `0` | None / 32 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored attempt count. |
| created_at | timestamp with time zone / timestamptz | NO | `now()` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored created at. |
| expires_at | timestamp with time zone / timestamptz | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored expires at. |
| verified_at | timestamp with time zone / timestamptz | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored verified at. |
| consumed_at | timestamp with time zone / timestamptz | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored consumed at. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| signup_email_verifications_account_kind_check | c | `CHECK (((account_kind)::text = ANY ((ARRAY['HOST'::character varying, 'GROUP_ADMIN'::character varying, 'GROUP_MEMBER'::character varying])::text[])))` |
| signup_email_verifications_pkey | p | `PRIMARY KEY (id)` |
| signup_email_verifications_proof_hash_key | u | `UNIQUE (proof_hash)` |


### Indexes

| Index | Definition |
| --- | --- |
| signup_email_address_time | `CREATE INDEX signup_email_address_time ON public.signup_email_verifications USING btree (email, created_at)` |
| signup_email_ip_time | `CREATE INDEX signup_email_ip_time ON public.signup_email_verifications USING btree (ip_fingerprint, created_at)` |
| signup_email_verifications_pkey | `CREATE UNIQUE INDEX signup_email_verifications_pkey ON public.signup_email_verifications USING btree (id)` |
| signup_email_verifications_proof_hash_key | `CREATE UNIQUE INDEX signup_email_verifications_proof_hash_key ON public.signup_email_verifications USING btree (proof_hash)` |


Consumers: [backend/src/main/java/com/giftplatform/security/recovery/SignupEmailVerificationService.java:1](../../src/main/java/com/giftplatform/security/recovery/SignupEmailVerificationService.java#L1)

## username_recovery_attempts

Type: **BASE TABLE**. Purpose: Authentication, contact verification, account recovery, throttling or session invalidation metadata.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| ip_fingerprint | character / bpchar | NO | No SQL default | 64 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored ip fingerprint. |
| account_kind | character varying / varchar | NO | No SQL default | 20 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored account kind. |
| account_id | uuid / uuid | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| requested_at | timestamp with time zone / timestamptz | NO | `now()` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored requested at. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| username_recovery_attempts_account_kind_check | c | `CHECK (((account_kind)::text = ANY ((ARRAY['HOST'::character varying, 'PLATFORM_ADMIN'::character varying, 'GROUP_ADMIN'::character varying, 'GROUP_MEMBER'::character varying])::text[])))` |
| username_recovery_attempts_pkey | p | `PRIMARY KEY (id)` |


### Indexes

| Index | Definition |
| --- | --- |
| username_recovery_account_time | `CREATE INDEX username_recovery_account_time ON public.username_recovery_attempts USING btree (account_kind, account_id, requested_at)` |
| username_recovery_attempts_pkey | `CREATE UNIQUE INDEX username_recovery_attempts_pkey ON public.username_recovery_attempts USING btree (id)` |
| username_recovery_ip_time | `CREATE INDEX username_recovery_ip_time ON public.username_recovery_attempts USING btree (ip_fingerprint, requested_at)` |


Consumers: [backend/src/main/java/com/giftplatform/security/recovery/UsernameRecoveryController.java:1](../../src/main/java/com/giftplatform/security/recovery/UsernameRecoveryController.java#L1)

## users

Type: **BASE TABLE**. Purpose: Authenticated account identity and credential metadata.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| email | character varying / varchar | NO | No SQL default | 254 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored email. |
| password_hash | character varying / varchar | NO | No SQL default | 255 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Sensitive representation; do not expose plaintext or hashes. |
| role | character varying / varchar | NO | `'CUSTOMER'::character varying` | 30 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored role. |
| enabled | boolean / bool | NO | `true` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored enabled. |
| created_at | timestamp with time zone / timestamptz | NO | `CURRENT_TIMESTAMP` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored created at. |
| updated_at | timestamp with time zone / timestamptz | NO | `CURRENT_TIMESTAMP` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored updated at. |
| username | character varying / varchar | YES | No SQL default | 50 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored username. |
| phone_number | character varying / varchar | YES | No SQL default | 16 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored phone number. |
| phone_verified_at | timestamp with time zone / timestamptz | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored phone verified at. |
| full_name | character varying / varchar | YES | No SQL default | 120 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored full name. |
| email_verified_at | timestamp with time zone / timestamptz | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored email verified at. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| chk_users_email_not_blank | c | `CHECK ((length(TRIM(BOTH FROM email)) > 0))` |
| chk_users_password_not_blank | c | `CHECK ((length(TRIM(BOTH FROM password_hash)) > 0))` |
| chk_users_phone_format | c | `CHECK (((phone_number IS NULL) OR ((phone_number)::text ~ '^\+[1-9][0-9]{1,14}$'::text)))` |
| chk_users_phone_verification | c | `CHECK (((phone_verified_at IS NULL) OR (phone_number IS NOT NULL)))` |
| chk_users_role | c | `CHECK (((role)::text = ANY ((ARRAY['CUSTOMER'::character varying, 'PLATFORM_ADMIN'::character varying])::text[])))` |
| chk_users_username_format | c | `CHECK (((username IS NULL) OR ((username)::text ~ '^[a-z][a-z0-9_]{3,29}$'::text)))` |
| users_pkey | p | `PRIMARY KEY (id)` |


### Indexes

| Index | Definition |
| --- | --- |
| users_pkey | `CREATE UNIQUE INDEX users_pkey ON public.users USING btree (id)` |
| ux_users_email_normalized | `CREATE UNIQUE INDEX ux_users_email_normalized ON public.users USING btree (lower((email)::text))` |
| ux_users_phone_number | `CREATE UNIQUE INDEX ux_users_phone_number ON public.users USING btree (phone_number) WHERE (phone_number IS NOT NULL)` |
| ux_users_username_normalized | `CREATE UNIQUE INDEX ux_users_username_normalized ON public.users USING btree (lower((username)::text)) WHERE (username IS NOT NULL)` |


Consumers: [backend/src/main/java/com/giftplatform/admin/AccountPolicyController.java:1](../../src/main/java/com/giftplatform/admin/AccountPolicyController.java#L1); [backend/src/main/java/com/giftplatform/admin/AiInvitationAdminController.java:1](../../src/main/java/com/giftplatform/admin/AiInvitationAdminController.java#L1); [backend/src/main/java/com/giftplatform/admin/GroupMemberEmailPolicyController.java:1](../../src/main/java/com/giftplatform/admin/GroupMemberEmailPolicyController.java#L1); [backend/src/main/java/com/giftplatform/admin/PlatformAdminAuditController.java:1](../../src/main/java/com/giftplatform/admin/PlatformAdminAuditController.java#L1); [backend/src/main/java/com/giftplatform/admin/PlatformAdminConsoleController.java:1](../../src/main/java/com/giftplatform/admin/PlatformAdminConsoleController.java#L1); [backend/src/main/java/com/giftplatform/admin/bootstrap/AdminPasswordRecoveryRunner.java:1](../../src/main/java/com/giftplatform/admin/bootstrap/AdminPasswordRecoveryRunner.java#L1); [backend/src/main/java/com/giftplatform/admin/bootstrap/AdminProvisionRunner.java:1](../../src/main/java/com/giftplatform/admin/bootstrap/AdminProvisionRunner.java#L1); [backend/src/main/java/com/giftplatform/event/service/EventPublishReadiness.java:1](../../src/main/java/com/giftplatform/event/service/EventPublishReadiness.java#L1); [backend/src/main/java/com/giftplatform/hostpayment/service/HostPaymentProfileService.java:1](../../src/main/java/com/giftplatform/hostpayment/service/HostPaymentProfileService.java#L1); [backend/src/main/java/com/giftplatform/hostpayment/verification/account/AccountVerificationController.java:1](../../src/main/java/com/giftplatform/hostpayment/verification/account/AccountVerificationController.java#L1); [backend/src/main/java/com/giftplatform/monetization/CommercialAccessService.java:1](../../src/main/java/com/giftplatform/monetization/CommercialAccessService.java#L1); [backend/src/main/java/com/giftplatform/otp/service/PhoneOtpIssuanceTransactionService.java:1](../../src/main/java/com/giftplatform/otp/service/PhoneOtpIssuanceTransactionService.java#L1); [backend/src/main/java/com/giftplatform/otp/service/PhoneOtpTransactionService.java:1](../../src/main/java/com/giftplatform/otp/service/PhoneOtpTransactionService.java#L1); [backend/src/main/java/com/giftplatform/payment/control/PaymentControlService.java:1](../../src/main/java/com/giftplatform/payment/control/PaymentControlService.java#L1); [backend/src/main/java/com/giftplatform/qr/design/ai/AiInvitationService.java:1](../../src/main/java/com/giftplatform/qr/design/ai/AiInvitationService.java#L1); [backend/src/main/java/com/giftplatform/security/recovery/EmailRecoveryController.java:1](../../src/main/java/com/giftplatform/security/recovery/EmailRecoveryController.java#L1); [backend/src/main/java/com/giftplatform/security/recovery/UsernameRecoveryController.java:1](../../src/main/java/com/giftplatform/security/recovery/UsernameRecoveryController.java#L1); [backend/src/main/java/com/giftplatform/security/verification/MobileVerificationService.java:1](../../src/main/java/com/giftplatform/security/verification/MobileVerificationService.java#L1); [backend/src/main/java/com/giftplatform/user/controller/ProfileNameController.java:1](../../src/main/java/com/giftplatform/user/controller/ProfileNameController.java#L1); [backend/src/main/java/com/giftplatform/user/entity/User.java:1](../../src/main/java/com/giftplatform/user/entity/User.java#L1); [backend/src/main/java/com/giftplatform/user/service/RegistrationServiceImpl.java:1](../../src/main/java/com/giftplatform/user/service/RegistrationServiceImpl.java#L1)

## wallet_ledger_entries

Type: **BASE TABLE**. Purpose: Payment, accounting, pricing, reconciliation or blocked-payout workflow. State and idempotency constraints are listed below.

| Column | PostgreSQL type | Nullable | SQL default | Length / precision / scale | Description / validation |
| --- | --- | --- | --- | --- | --- |
| id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| event_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| contribution_id | uuid / uuid | YES | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| entry_group_id | uuid / uuid | NO | No SQL default | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Identifier/reference. |
| account_code | character varying / varchar | NO | No SQL default | 40 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored account code. |
| amount_paise | bigint / int8 | NO | No SQL default | None / 64 / 0 | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Money in integer paise; preserve distinction between reported, confirmed and settled. |
| currency | character varying / varchar | NO | `'INR'::character varying` | 3 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored currency. |
| entry_type | character varying / varchar | NO | No SQL default | 40 / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored entry type. |
| created_at | timestamp with time zone / timestamptz | NO | `CURRENT_TIMESTAMP` | None / None / None | PK/FK/check/uniqueness below; application validations are in API models and rule catalogue. Stored created at. |


### Constraints

| Name | Type (p primary, f foreign, u unique, c check) | Exact definition |
| --- | --- | --- |
| ck_wallet_account_code | c | `CHECK (((account_code)::text = ANY ((ARRAY['PAYMENT_CLEARING'::character varying, 'GIFT_RESERVED'::character varying, 'HOST_PENDING'::character varying, 'HOST_AVAILABLE'::character varying, 'REFUND_PENDING'::character varying, 'PLATFORM_REVENUE'::character varying])::text[])))` |
| wallet_ledger_entries_amount_paise_check | c | `CHECK ((amount_paise <> 0))` |
| wallet_ledger_entries_contribution_id_fkey | f | `FOREIGN KEY (contribution_id) REFERENCES contributions(id)` |
| wallet_ledger_entries_event_id_fkey | f | `FOREIGN KEY (event_id) REFERENCES events(id)` |
| wallet_ledger_entries_pkey | p | `PRIMARY KEY (id)` |


### Indexes

| Index | Definition |
| --- | --- |
| idx_wallet_ledger_event | `CREATE INDEX idx_wallet_ledger_event ON public.wallet_ledger_entries USING btree (event_id, created_at)` |
| idx_wallet_ledger_group | `CREATE INDEX idx_wallet_ledger_group ON public.wallet_ledger_entries USING btree (entry_group_id)` |
| wallet_ledger_entries_pkey | `CREATE UNIQUE INDEX wallet_ledger_entries_pkey ON public.wallet_ledger_entries USING btree (id)` |


Consumers: [backend/src/main/java/com/giftplatform/monetization/CommercialSetupService.java:1](../../src/main/java/com/giftplatform/monetization/CommercialSetupService.java#L1); [backend/src/main/java/com/giftplatform/payment/entity/WalletLedgerEntry.java:1](../../src/main/java/com/giftplatform/payment/entity/WalletLedgerEntry.java#L1); [backend/src/main/java/com/giftplatform/payment/service/ContributionAllocationService.java:1](../../src/main/java/com/giftplatform/payment/service/ContributionAllocationService.java#L1); [backend/src/main/java/com/giftplatform/payment/settlement/SettlementBalanceService.java:1](../../src/main/java/com/giftplatform/payment/settlement/SettlementBalanceService.java#L1)

# Views

## group_collection_transactions


```sql
SELECT r.id,
    r.group_id,
    r.guest_name,
    r.guest_contact,
    r.amount_paise,
    r.method,
    r.transaction_reference,
    r.state,
    r.created_at,
    r.gift_id_snapshot,
        CASE
            WHEN (r.creator_admin_id IS NOT NULL) THEN 'CREATOR'::text
            WHEN (r.member_id IS NOT NULL) THEN 'MEMBER'::text
            ELSE 'GUEST'::text
        END AS contributor_type
   FROM gift_group_guest_reports r
UNION ALL
 SELECT r.id,
    r.group_id,
    m.display_name AS guest_name,
    NULL::character varying AS guest_contact,
    r.amount_paise,
    r.method,
    r.transaction_reference,
    r.state,
    r.created_at,
    r.gift_id_snapshot,
    'MEMBER'::text AS contributor_type
   FROM (gift_group_payment_reports r
     JOIN gift_group_members m ON ((m.id = r.member_id)));
```

## group_gift_progress


```sql
SELECT g.id,
    (COALESCE(sum(t.amount_paise) FILTER (WHERE (((t.state)::text = 'VERIFIED'::text) OR (((t.state)::text = 'REPORTED_UNVERIFIED'::text) AND (t.creator_received_at IS NOT NULL)))), (0)::numeric))::bigint AS collected_paise,
    (COALESCE(sum(t.amount_paise) FILTER (WHERE ((t.state)::text = 'REPORTED_UNVERIFIED'::text)), (0)::numeric))::bigint AS reported_paise,
        CASE
            WHEN (g.target_paise IS NULL) THEN (0)::numeric
            ELSE LEAST((100)::numeric, ((COALESCE(sum(t.amount_paise) FILTER (WHERE (((t.state)::text = 'VERIFIED'::text) OR (((t.state)::text = 'REPORTED_UNVERIFIED'::text) AND (t.creator_received_at IS NOT NULL)))), (0)::numeric) * 100.0) / (g.target_paise)::numeric))
        END AS completion_percentage
   FROM (gift_groups g
     LEFT JOIN group_gift_receipts t ON ((t.group_id = g.id)))
  GROUP BY g.id;
```

## group_gift_receipts


```sql
SELECT gift_group_guest_reports.id,
    gift_group_guest_reports.group_id,
    gift_group_guest_reports.amount_paise,
    gift_group_guest_reports.state,
    gift_group_guest_reports.creator_received_at
   FROM gift_group_guest_reports
UNION ALL
 SELECT gift_group_payment_reports.id,
    gift_group_payment_reports.group_id,
    gift_group_payment_reports.amount_paise,
    gift_group_payment_reports.state,
    gift_group_payment_reports.creator_received_at
   FROM gift_group_payment_reports;
```

# Triggers

## commercial_order_immutable

Table: commercial_orders
```sql
CREATE TRIGGER commercial_order_immutable BEFORE UPDATE ON public.commercial_orders FOR EACH ROW EXECUTE FUNCTION preserve_commercial_order_pricing()
```

## commercial_setup_history_immutable

Table: commercial_setup_history
```sql
CREATE TRIGGER commercial_setup_history_immutable BEFORE DELETE OR UPDATE ON public.commercial_setup_history FOR EACH ROW EXECUTE FUNCTION reject_monetization_history_mutation()
```

## trg_check_contribution_allocation_gift

Table: contribution_allocations
```sql
CREATE TRIGGER trg_check_contribution_allocation_gift BEFORE INSERT OR UPDATE ON public.contribution_allocations FOR EACH ROW EXECUTE FUNCTION check_contribution_allocation_gift()
```

## group_creator_history

Table: gift_group_accounts
```sql
CREATE TRIGGER group_creator_history AFTER UPDATE ON public.gift_group_accounts FOR EACH ROW EXECUTE FUNCTION record_group_activity()
```

## immutable_group_activity

Table: gift_group_activity
```sql
CREATE TRIGGER immutable_group_activity BEFORE DELETE OR UPDATE ON public.gift_group_activity FOR EACH ROW EXECUTE FUNCTION protect_group_activity()
```

## group_destination_verification_binding

Table: gift_group_destination_verification_attempts
```sql
CREATE TRIGGER group_destination_verification_binding BEFORE INSERT OR UPDATE OF group_id, destination_id ON public.gift_group_destination_verification_attempts FOR EACH ROW EXECUTE FUNCTION gift_check_group_destination_verification_binding()
```

## group_guest_gift_snapshot

Table: gift_group_guest_reports
```sql
CREATE TRIGGER group_guest_gift_snapshot BEFORE INSERT ON public.gift_group_guest_reports FOR EACH ROW EXECUTE FUNCTION snapshot_group_contribution_gift()
```

## group_guest_report_history

Table: gift_group_guest_reports
```sql
CREATE TRIGGER group_guest_report_history AFTER INSERT OR UPDATE ON public.gift_group_guest_reports FOR EACH ROW EXECUTE FUNCTION record_group_activity()
```

## group_invitation_history

Table: gift_group_invitations
```sql
CREATE TRIGGER group_invitation_history AFTER INSERT OR UPDATE ON public.gift_group_invitations FOR EACH ROW EXECUTE FUNCTION record_group_activity()
```

## group_member_history

Table: gift_group_members
```sql
CREATE TRIGGER group_member_history AFTER INSERT OR UPDATE OF status ON public.gift_group_members FOR EACH ROW EXECUTE FUNCTION record_group_activity()
```

## group_destination_history

Table: gift_group_payment_destinations
```sql
CREATE TRIGGER group_destination_history AFTER INSERT OR UPDATE ON public.gift_group_payment_destinations FOR EACH ROW EXECUTE FUNCTION record_group_activity()
```

## group_member_gift_snapshot

Table: gift_group_payment_reports
```sql
CREATE TRIGGER group_member_gift_snapshot BEFORE INSERT ON public.gift_group_payment_reports FOR EACH ROW EXECUTE FUNCTION snapshot_group_contribution_gift()
```

## group_member_report_history

Table: gift_group_payment_reports
```sql
CREATE TRIGGER group_member_report_history AFTER INSERT OR UPDATE ON public.gift_group_payment_reports FOR EACH ROW EXECUTE FUNCTION record_group_activity()
```

## group_history

Table: gift_groups
```sql
CREATE TRIGGER group_history AFTER INSERT OR UPDATE ON public.gift_groups FOR EACH ROW EXECUTE FUNCTION record_group_activity()
```

## trg_bank_event_preferences

Table: host_bank_accounts
```sql
CREATE TRIGGER trg_bank_event_preferences AFTER UPDATE OF enabled, archived_at, verification_status ON public.host_bank_accounts FOR EACH ROW EXECUTE FUNCTION clear_event_account_preferences('BANK')
```

## trg_upi_event_preferences

Table: host_upi_accounts
```sql
CREATE TRIGGER trg_upi_event_preferences AFTER UPDATE OF enabled, archived_at, verification_status ON public.host_upi_accounts FOR EACH ROW EXECUTE FUNCTION clear_event_account_preferences('UPI')
```

## payment_control_history_immutable

Table: payment_control_history
```sql
CREATE TRIGGER payment_control_history_immutable BEFORE DELETE OR UPDATE ON public.payment_control_history FOR EACH ROW EXECUTE FUNCTION reject_monetization_history_mutation()
```

## payment_order_pricing_immutable

Table: payment_orders
```sql
CREATE TRIGGER payment_order_pricing_immutable BEFORE UPDATE ON public.payment_orders FOR EACH ROW EXECUTE FUNCTION preserve_payment_order_pricing()
```

## payment_order_requested_amount

Table: payment_orders
```sql
CREATE TRIGGER payment_order_requested_amount BEFORE INSERT ON public.payment_orders FOR EACH ROW EXECUTE FUNCTION payment_order_requested_amount()
```

## monetization_history_immutable

Table: platform_monetization_history
```sql
CREATE TRIGGER monetization_history_immutable BEFORE DELETE OR UPDATE ON public.platform_monetization_history FOR EACH ROW EXECUTE FUNCTION reject_monetization_history_mutation()
```

## routing_commission_history_immutable

Table: routing_commission_history
```sql
CREATE TRIGGER routing_commission_history_immutable BEFORE DELETE OR UPDATE ON public.routing_commission_history FOR EACH ROW EXECUTE FUNCTION reject_monetization_history_mutation()
```

# Routines

## check_contribution_allocation_gift


```sql
CREATE OR REPLACE FUNCTION public.check_contribution_allocation_gift()
 RETURNS trigger
 LANGUAGE plpgsql
AS $function$
DECLARE
    original_gift UUID;
BEGIN
    SELECT gift_id INTO original_gift
    FROM contributions
    WHERE id = NEW.contribution_id;

    IF NEW.allocation_type = 'GIFT'
       AND NEW.gift_id IS DISTINCT FROM original_gift THEN
        RAISE EXCEPTION
            'Gift allocation must match contribution gift';
    END IF;

    RETURN NEW;
END;
$function$
```

## clear_event_account_preferences


```sql
CREATE OR REPLACE FUNCTION public.clear_event_account_preferences()
 RETURNS trigger
 LANGUAGE plpgsql
AS $function$
BEGIN
 IF NOT NEW.enabled OR NEW.archived_at IS NOT NULL OR NEW.verification_status IN ('FAILED','REVOKED') THEN
  UPDATE event_payment_account_links SET preferred=FALSE
   WHERE host_id=NEW.host_id AND destination_id=NEW.id AND destination_kind=TG_ARGV[0];
 END IF;
 RETURN NEW;
END;
$function$
```

## gift_check_group_destination_verification_binding


```sql
CREATE OR REPLACE FUNCTION public.gift_check_group_destination_verification_binding()
 RETURNS trigger
 LANGUAGE plpgsql
AS $function$
BEGIN
 IF NOT EXISTS (SELECT 1 FROM gift_group_payment_destinations d
                WHERE d.id=NEW.destination_id AND d.group_id=NEW.group_id) THEN
  RAISE EXCEPTION 'Verification destination does not belong to group';
 END IF;
 RETURN NEW;
END $function$
```

## payment_order_requested_amount


```sql
CREATE OR REPLACE FUNCTION public.payment_order_requested_amount()
 RETURNS trigger
 LANGUAGE plpgsql
AS $function$
BEGIN
 IF NEW.requested_amount_paise IS NULL THEN NEW.requested_amount_paise=NEW.amount_paise; END IF;
 RETURN NEW;
END $function$
```

## preserve_commercial_order_pricing


```sql
CREATE OR REPLACE FUNCTION public.preserve_commercial_order_pricing()
 RETURNS trigger
 LANGUAGE plpgsql
AS $function$
BEGIN
 IF ROW(NEW.buyer_kind,NEW.buyer_id,NEW.stream,NEW.resource_id,NEW.amount_paise,NEW.pricing_revision,NEW.policy_revision,NEW.duration_days,NEW.idempotency_key)
 IS DISTINCT FROM ROW(OLD.buyer_kind,OLD.buyer_id,OLD.stream,OLD.resource_id,OLD.amount_paise,OLD.pricing_revision,OLD.policy_revision,OLD.duration_days,OLD.idempotency_key)
 THEN RAISE EXCEPTION 'Commercial order pricing is immutable'; END IF;
 IF OLD.status IN ('CAPTURED','FREE') AND NEW IS DISTINCT FROM OLD THEN RAISE EXCEPTION 'Completed commercial orders are immutable'; END IF;
 RETURN NEW;
END $function$
```

## preserve_payment_order_pricing


```sql
CREATE OR REPLACE FUNCTION public.preserve_payment_order_pricing()
 RETURNS trigger
 LANGUAGE plpgsql
AS $function$
BEGIN
 IF ROW(NEW.amount_paise,NEW.requested_amount_paise,NEW.platform_fee_paise,NEW.host_receives_paise,NEW.monetization_revision)
    IS DISTINCT FROM ROW(OLD.amount_paise,OLD.requested_amount_paise,OLD.platform_fee_paise,OLD.host_receives_paise,OLD.monetization_revision)
 THEN RAISE EXCEPTION 'Payment order pricing is immutable'; END IF;
 RETURN NEW;
END $function$
```

## protect_group_activity


```sql
CREATE OR REPLACE FUNCTION public.protect_group_activity()
 RETURNS trigger
 LANGUAGE plpgsql
AS $function$
BEGIN RAISE EXCEPTION 'Group activity is append-only'; END $function$
```

## record_group_activity


```sql
CREATE OR REPLACE FUNCTION public.record_group_activity()
 RETURNS trigger
 LANGUAGE plpgsql
AS $function$
DECLARE gid UUID; creator UUID; label TEXT; info JSONB; before_row JSONB; after_row JSONB;
BEGIN
 after_row=to_jsonb(NEW);
 IF TG_OP='UPDATE' THEN before_row=to_jsonb(OLD); ELSE before_row='{}'; END IF;
 IF TG_TABLE_NAME='gift_group_accounts' THEN
  FOR gid IN SELECT id FROM gift_groups WHERE admin_id=NEW.id LOOP
   INSERT INTO gift_group_activity(group_id,actor_id,action,details)
    VALUES(gid,NEW.id,'CREATOR_ACCOUNT_UPDATED',jsonb_build_object('emailVerified',NEW.email_verified_at IS NOT NULL,
     'mobileVerified',NEW.mobile_verified_at IS NOT NULL,'credentialsChanged',
     (before_row->'password_hash' IS DISTINCT FROM after_row->'password_hash') OR (before_row->'pin_hash' IS DISTINCT FROM after_row->'pin_hash')));
  END LOOP;
  RETURN NEW;
 ELSIF TG_TABLE_NAME='gift_groups' THEN
  gid=NEW.id; creator=NEW.admin_id;
  IF TG_OP='INSERT' THEN label='GROUP_CREATED';
  ELSIF OLD.collection_published_at IS NULL AND NEW.collection_published_at IS NOT NULL THEN label='GROUP_PUBLISHED';
  ELSIF OLD.selected_gift_id IS DISTINCT FROM NEW.selected_gift_id THEN
   IF NEW.selected_gift_id IS NULL THEN label='GIFT_RELEASED'; ELSE label='GIFT_SELECTED'; END IF;
  ELSIF OLD.reserved_gift_id IS DISTINCT FROM NEW.reserved_gift_id THEN label='GIFT_RESERVATION_CHANGED';
  ELSE label='GROUP_SETTINGS_UPDATED'; END IF;
  info=jsonb_build_object('previousGiftId',before_row->'selected_gift_id','giftId',NEW.selected_gift_id,
   'previousReservedGiftId',before_row->'reserved_gift_id','reservedGiftId',NEW.reserved_gift_id,
   'name',NEW.name,'targetPaise',NEW.target_paise,'published',NEW.collection_published_at IS NOT NULL,
   'previous',before_row-'creator_mobile','current',after_row-'creator_mobile');
 ELSE
  gid=NEW.group_id;
  IF TG_TABLE_NAME IN ('gift_group_guest_reports','gift_group_payment_reports') THEN
   creator=COALESCE((after_row->>'creator_admin_id')::uuid,(after_row->>'member_id')::uuid);
   label=CASE WHEN TG_OP='INSERT' THEN 'CONTRIBUTION_REPORTED' ELSE 'CONTRIBUTION_STATUS_CHANGED' END;
   info=jsonb_build_object('recordId',NEW.id,'source',TG_TABLE_NAME,'amountPaise',after_row->'amount_paise',
    'method',after_row->'method','reference',after_row->'transaction_reference','state',after_row->'state',
    'previousState',before_row->'state','giftId',after_row->'gift_id_snapshot');
  ELSIF TG_TABLE_NAME='gift_group_members' THEN
   IF TG_OP='INSERT' THEN creator=NEW.id; ELSE SELECT admin_id INTO creator FROM gift_groups WHERE id=gid; END IF;
   label=CASE WHEN TG_OP='INSERT' THEN 'MEMBER_JOINED' ELSE 'MEMBER_UPDATED' END;
   info=jsonb_build_object('memberId',NEW.id,'previousStatus',before_row->'status','status',NEW.status);
  ELSIF TG_TABLE_NAME='gift_group_payment_destinations' THEN
   SELECT admin_id INTO creator FROM gift_groups WHERE id=gid;
   label='PAYMENT_DESTINATION_UPDATED';
   info=jsonb_build_object('destinationId',NEW.id,'kind',after_row->'kind','status',after_row->'status','enabled',after_row->'enabled');
  ELSE
   SELECT admin_id INTO creator FROM gift_groups WHERE id=gid;
   label='INVITATION_UPDATED'; info=jsonb_build_object('invitationId',NEW.id,'revoked',after_row->'revoked_at');
  END IF;
 END IF;
 INSERT INTO gift_group_activity(group_id,actor_id,action,details) VALUES(gid,creator,label,info);
 RETURN NEW;
END $function$
```

## reject_monetization_history_mutation


```sql
CREATE OR REPLACE FUNCTION public.reject_monetization_history_mutation()
 RETURNS trigger
 LANGUAGE plpgsql
AS $function$
BEGIN RAISE EXCEPTION 'Monetization history is append-only'; END $function$
```

## snapshot_group_contribution_gift


```sql
CREATE OR REPLACE FUNCTION public.snapshot_group_contribution_gift()
 RETURNS trigger
 LANGUAGE plpgsql
AS $function$
BEGIN
 SELECT selected_gift_id INTO NEW.gift_id_snapshot FROM gift_groups WHERE id=NEW.group_id FOR SHARE;
 RETURN NEW;
END $function$
```

# Row-level security

| Table | RLS enabled | RLS forced |
| --- | --- | --- |
| account_contact_challenges | False | False |
| account_pending_email_verifications | False | False |
| account_pending_mobile_verifications | False | False |
| account_recovery_challenges | False | False |
| account_registration_policies | False | False |
| affiliate_clicks | False | False |
| affiliate_commissions | False | False |
| affiliate_partners | False | False |
| audit_logs | False | False |
| commercial_entitlements | False | False |
| commercial_orders | False | False |
| commercial_placement_clicks | False | False |
| commercial_placements | False | False |
| commercial_policy | False | False |
| commercial_revenue | False | False |
| commercial_setup_history | False | False |
| contribution_allocation_postings | False | False |
| contribution_allocations | False | False |
| contributions | False | False |
| credential_session_versions | False | False |
| event_guest_submission_limits | False | False |
| event_guestbook | False | False |
| event_payment_account_links | False | False |
| event_rsvps | False | False |
| event_thank_you_settings | False | False |
| events | False | False |
| flyway_schema_history | False | False |
| gift_group_accounts | False | False |
| gift_group_activity | False | False |
| gift_group_destination_verification_attempts | False | False |
| gift_group_guest_reports | False | False |
| gift_group_invitations | False | False |
| gift_group_members | False | False |
| gift_group_payment_audit | False | False |
| gift_group_payment_destinations | False | False |
| gift_group_payment_permissions | False | False |
| gift_group_payment_reports | False | False |
| gift_groups | False | False |
| gift_photos | False | False |
| gift_retailer_metadata_audit | False | False |
| gifts | False | False |
| group_collection_notifications | False | False |
| group_member_email_policies | False | False |
| guest_direct_transfer_claims | False | False |
| guest_gift_host_contributions | False | False |
| guest_gifts | False | False |
| guest_manual_display_allocations | False | False |
| host_account_verification_checks | False | False |
| host_bank_accounts | False | False |
| host_destination_verification_attempts | False | False |
| host_payout_requests | False | False |
| host_upi_accounts | False | False |
| host_upi_qr_assets | False | False |
| host_upi_qr_images | False | False |
| hybrid_checkout_quotes | False | False |
| invitation_ai_requests | False | False |
| invitation_gallery_assets | False | False |
| notification_outbox | False | False |
| otp_request_rate_limits | False | False |
| payment_channel_controls | False | False |
| payment_control_history | False | False |
| payment_control_revision | False | False |
| payment_notifications | False | False |
| payment_orders | False | False |
| payment_transactions | False | False |
| payment_webhook_events | False | False |
| payout_reconciliation_observations | False | False |
| payout_reservations | False | False |
| phone_otp_challenges | False | False |
| platform_admin_action_audit | False | False |
| platform_event_ai_design_settings | False | False |
| platform_event_album_settings | False | False |
| platform_event_message_settings | False | False |
| platform_experience_settings | False | False |
| platform_host_module_grants | False | False |
| platform_module_controls | False | False |
| platform_monetization_history | False | False |
| platform_monetization_settings | False | False |
| platform_monetization_streams | False | False |
| platform_verification_settings | False | False |
| qr_invitation_designs | False | False |
| routing_commission_history | False | False |
| routing_commission_settings | False | False |
| signup_email_verifications | False | False |
| username_recovery_attempts | False | False |
| users | False | False |
| wallet_ledger_entries | False | False |


Policies exported: 0. Row-level ownership is principally enforced in the application. Do not describe PostgreSQL RLS as deployed where the catalogue shows false.
