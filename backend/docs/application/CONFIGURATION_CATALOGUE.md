# Configuration catalogue

79 source-declared property keys; values in private configuration are redacted. This is not a dump of a deployed environment. Properties can be supplied by Spring relaxed environment binding even when no explicit environment name appears below. For example `gift.payment.profiles.enabled` can be supplied as `GIFT_PAYMENT_PROFILES_ENABLED`; check the deployed configuration rather than assuming an example file is active.

## gift.build.configuration

| Attribute | Specification |
| --- | --- |
| Purpose | Framework/application property; inspect the declaration for its exact consumer |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | No explicit placeholder name found; Spring relaxed binding may apply. |
| Conventional environment name | GIFT_BUILD_CONFIGURATION |
| Sensitivity | Not classified as a secret by the catalogue; review deployment exposure. |
| Production value | Not provided. Choose deliberately from the observed defaults below; production proposal is described in Document 3. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/release/resources/application.properties:2 | application.properties | `environment-only-release` |


## gift.cashfree.secureid.client-id

| Attribute | Specification |
| --- | --- |
| Purpose | Cashfree Secure ID sandbox adapter gates/credentials |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | CASHFREE_SECUREID_CLIENT_ID |
| Conventional environment name | GIFT_CASHFREE_SECUREID_CLIENT_ID |
| Sensitivity | Sensitive; use a secret reference, never a checked-in production value. |
| Production value | Deployment secret reference; provision and rotation-test before enabling the dependent feature. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/java/com/giftplatform/hostpayment/verification/account/AccountVerificationClient.java:21 | @Value injection | `${gift.cashfree.secureid.client-id:<redacted>}` |
| backend/src/main/java/com/giftplatform/hostpayment/verification/cashfree/CashfreeSecureIdTransport.java:19 | @Value injection | `${gift.cashfree.secureid.client-id:<redacted>}` |
| backend/src/main/resources/application.properties:40 | application.properties | `${CASHFREE_SECUREID_CLIENT_ID:<redacted>}` |
| backend/src/main/resources/application.properties.example:40 | application.properties.example | `${CASHFREE_SECUREID_CLIENT_ID}` |


## gift.cashfree.secureid.client-secret

| Attribute | Specification |
| --- | --- |
| Purpose | Cashfree Secure ID sandbox adapter gates/credentials |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | CASHFREE_SECUREID_CLIENT_SECRET |
| Conventional environment name | GIFT_CASHFREE_SECUREID_CLIENT_SECRET |
| Sensitivity | Sensitive; use a secret reference, never a checked-in production value. |
| Production value | Deployment secret reference; provision and rotation-test before enabling the dependent feature. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/java/com/giftplatform/hostpayment/verification/account/AccountVerificationClient.java:22 | @Value injection | `${gift.cashfree.secureid.client-secret:<redacted>}` |
| backend/src/main/java/com/giftplatform/hostpayment/verification/cashfree/CashfreeSecureIdTransport.java:20 | @Value injection | `${gift.cashfree.secureid.client-secret:<redacted>}` |
| backend/src/main/resources/application.properties:41 | application.properties | `${CASHFREE_SECUREID_CLIENT_SECRET:<redacted>}` |
| backend/src/main/resources/application.properties.example:41 | application.properties.example | `${CASHFREE_SECUREID_CLIENT_SECRET}` |


## gift.cashfree.secureid.sandbox.enabled

| Attribute | Specification |
| --- | --- |
| Purpose | Cashfree Secure ID sandbox adapter gates/credentials |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | CASHFREE_SECUREID_SANDBOX_ENABLED |
| Conventional environment name | GIFT_CASHFREE_SECUREID_SANDBOX_ENABLED |
| Sensitivity | Not classified as a secret by the catalogue; review deployment exposure. |
| Production value | Not provided. Choose deliberately from the observed defaults below; production proposal is described in Document 3. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/java/com/giftplatform/hostpayment/verification/cashfree/CashfreeSecureIdTransport.java:18 | @Value injection | `${gift.cashfree.secureid.sandbox.enabled:false}` |
| backend/src/main/resources/application.properties:39 | application.properties | `${CASHFREE_SECUREID_SANDBOX_ENABLED:false}` |
| backend/src/main/resources/application.properties.example:39 | application.properties.example | `${CASHFREE_SECUREID_SANDBOX_ENABLED:false}` |
| backend/src/release/resources/application.properties:21 | application.properties | `false` |


## gift.guest-base-url

| Attribute | Specification |
| --- | --- |
| Purpose | Public invitation link base |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | No explicit placeholder name found; Spring relaxed binding may apply. |
| Conventional environment name | GIFT_GUEST_BASE_URL |
| Sensitivity | Not classified as a secret by the catalogue; review deployment exposure. |
| Production value | Not provided. Choose deliberately from the observed defaults below; production proposal is described in Document 3. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/java/com/giftplatform/qr/EventQrService.java:26 | @Value injection | `${gift.guest-base-url:http://localhost:3000}` |
| backend/src/main/java/com/giftplatform/qr/design/InvitationDesignService.java:26 | @Value injection | `${gift.guest-base-url:http://localhost:3000}` |


## gift.guestbook.photos.dir

| Attribute | Specification |
| --- | --- |
| Purpose | Private normalized guest photo storage |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | GIFT_GUESTBOOK_PHOTOS_DIR |
| Conventional environment name | GIFT_GUESTBOOK_PHOTOS_DIR |
| Sensitivity | Not classified as a secret by the catalogue; review deployment exposure. |
| Production value | Not provided. Choose deliberately from the observed defaults below; production proposal is described in Document 3. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/java/com/giftplatform/celebration/GuestbookPhotoController.java:31 | @Value injection | `${gift.guestbook.photos.dir:${user.home}/.gift-platform/guestbook-photos}` |
| backend/src/main/java/com/giftplatform/operations/ProductionConfigurationPolicy.java:27 | Environment.getProperty | `""` |
| backend/src/main/resources/application-production.properties:21 | application-production.properties | `${GIFT_GUESTBOOK_PHOTOS_DIR:}` |


## gift.invitation.ai.api-key

| Attribute | Specification |
| --- | --- |
| Purpose | AI invitation provider gate, credential or model |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | OPENAI_API_KEY |
| Conventional environment name | GIFT_INVITATION_AI_API_KEY |
| Sensitivity | Sensitive; use a secret reference, never a checked-in production value. |
| Production value | Deployment secret reference; provision and rotation-test before enabling the dependent feature. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/java/com/giftplatform/operations/ProductionConfigurationPolicy.java:31 | Environment.getProperty | `<redacted configured value>` |
| backend/src/main/java/com/giftplatform/qr/design/ai/AiInvitationClient.java:24 | @Value injection | `${gift.invitation.ai.api-key:<redacted>}` |
| backend/src/main/resources/application.properties:54 | application.properties | `${OPENAI_API_KEY:<redacted>}` |
| backend/src/main/resources/application.properties.example:54 | application.properties.example | `${OPENAI_API_KEY}` |
| backend/src/release/resources/application.properties:28 | application.properties | `${OPENAI_API_KEY:<redacted>}` |


## gift.invitation.ai.enabled

| Attribute | Specification |
| --- | --- |
| Purpose | AI invitation provider gate, credential or model |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | GIFT_INVITATION_AI_ENABLED |
| Conventional environment name | GIFT_INVITATION_AI_ENABLED |
| Sensitivity | Not classified as a secret by the catalogue; review deployment exposure. |
| Production value | Not provided. Choose deliberately from the observed defaults below; production proposal is described in Document 3. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/java/com/giftplatform/operations/ProductionConfigurationPolicy.java:31 | Environment.getProperty | `Boolean.class,false` |
| backend/src/main/java/com/giftplatform/qr/design/ai/AiInvitationClient.java:23 | @Value injection | `${gift.invitation.ai.enabled:true}` |
| backend/src/main/resources/application-production.properties:28 | application-production.properties | `${GIFT_INVITATION_AI_ENABLED:false}` |
| backend/src/main/resources/application.properties:53 | application.properties | `${GIFT_INVITATION_AI_ENABLED:true}` |
| backend/src/main/resources/application.properties.example:53 | application.properties.example | `${GIFT_INVITATION_AI_ENABLED:true}` |


## gift.invitation.ai.model

| Attribute | Specification |
| --- | --- |
| Purpose | AI invitation provider gate, credential or model |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | OPENAI_INVITATION_MODEL |
| Conventional environment name | GIFT_INVITATION_AI_MODEL |
| Sensitivity | Not classified as a secret by the catalogue; review deployment exposure. |
| Production value | Not provided. Choose deliberately from the observed defaults below; production proposal is described in Document 3. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/java/com/giftplatform/qr/design/ai/AiInvitationClient.java:25 | @Value injection | `${gift.invitation.ai.model:gpt-4.1-mini}` |
| backend/src/main/resources/application.properties:55 | application.properties | `${OPENAI_INVITATION_MODEL:gpt-4.1-mini}` |
| backend/src/main/resources/application.properties.example:55 | application.properties.example | `${OPENAI_INVITATION_MODEL:gpt-4.1-mini}` |
| backend/src/release/resources/application.properties:29 | application.properties | `${OPENAI_INVITATION_MODEL:gpt-4.1-mini}` |


## gift.otp.email.from

| Attribute | Specification |
| --- | --- |
| Purpose | Scoped OTP or email/SMS verification and delivery |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | GIFT_OTP_EMAIL_FROM |
| Conventional environment name | GIFT_OTP_EMAIL_FROM |
| Sensitivity | Sensitive; use a secret reference, never a checked-in production value. |
| Production value | Deployment secret reference; provision and rotation-test before enabling the dependent feature. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/java/com/giftplatform/notification/email/BrandedEmailService.java:21 | @Value injection | `${gift.otp.email.from:<redacted>}` |
| backend/src/main/java/com/giftplatform/security/delivery/EmailDeliveryConfiguration.java:21 | @Value injection | `${gift.otp.email.from:<redacted>}` |
| backend/src/main/java/com/giftplatform/security/delivery/EmailDeliveryConfiguration.java:28 | @Value injection | `${gift.otp.email.from:<redacted>}` |
| backend/src/main/resources/application.properties:63 | application.properties | `${GIFT_OTP_EMAIL_FROM:<redacted>}` |
| backend/src/main/resources/application.properties.example:63 | application.properties.example | `${GIFT_OTP_EMAIL_FROM:<redacted>}` |
| backend/src/release/resources/application.properties:24 | application.properties | `${GIFT_OTP_EMAIL_FROM:<redacted>}` |


## gift.otp.email.provider

| Attribute | Specification |
| --- | --- |
| Purpose | Scoped OTP or email/SMS verification and delivery |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | GIFT_OTP_EMAIL_PROVIDER |
| Conventional environment name | GIFT_OTP_EMAIL_PROVIDER |
| Sensitivity | Not classified as a secret by the catalogue; review deployment exposure. |
| Production value | Not provided. Choose deliberately from the observed defaults below; production proposal is described in Document 3. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/java/com/giftplatform/notification/email/BrandedEmailService.java:21 | @Value injection | `${gift.otp.email.provider:disabled}` |
| backend/src/main/java/com/giftplatform/security/delivery/EmailDeliveryConfiguration.java:19 | @Value injection | `${gift.otp.email.provider:disabled}` |
| backend/src/main/java/com/giftplatform/security/delivery/EmailDeliveryConfiguration.java:26 | @Value injection | `${gift.otp.email.provider:disabled}` |
| backend/src/main/resources/application.properties:61 | application.properties | `${GIFT_OTP_EMAIL_PROVIDER:resend}` |
| backend/src/main/resources/application.properties.example:61 | application.properties.example | `${GIFT_OTP_EMAIL_PROVIDER:disabled}` |
| backend/src/release/resources/application.properties:22 | application.properties | `${GIFT_OTP_EMAIL_PROVIDER:disabled}` |


## gift.otp.email.resend.api-key

| Attribute | Specification |
| --- | --- |
| Purpose | Scoped OTP or email/SMS verification and delivery |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | GIFT_OTP_EMAIL_RESEND_API_KEY |
| Conventional environment name | GIFT_OTP_EMAIL_RESEND_API_KEY |
| Sensitivity | Sensitive; use a secret reference, never a checked-in production value. |
| Production value | Deployment secret reference; provision and rotation-test before enabling the dependent feature. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/java/com/giftplatform/notification/email/BrandedEmailService.java:21 | @Value injection | `${gift.otp.email.resend.api-key:<redacted>}` |
| backend/src/main/java/com/giftplatform/security/delivery/EmailDeliveryConfiguration.java:20 | @Value injection | `${gift.otp.email.resend.api-key:<redacted>}` |
| backend/src/main/java/com/giftplatform/security/delivery/EmailDeliveryConfiguration.java:27 | @Value injection | `${gift.otp.email.resend.api-key:<redacted>}` |
| backend/src/main/resources/application.properties:62 | application.properties | `${GIFT_OTP_EMAIL_RESEND_API_KEY:<redacted>}` |
| backend/src/main/resources/application.properties.example:62 | application.properties.example | `${GIFT_OTP_EMAIL_RESEND_API_KEY:<redacted>}` |
| backend/src/release/resources/application.properties:23 | application.properties | `${GIFT_OTP_EMAIL_RESEND_API_KEY:<redacted>}` |


## gift.otp.hmac-key-base64

| Attribute | Specification |
| --- | --- |
| Purpose | Scoped OTP or email/SMS verification and delivery |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | GIFT_OTP_HMAC_KEY_BASE64 |
| Conventional environment name | GIFT_OTP_HMAC_KEY_BASE64 |
| Sensitivity | Sensitive; use a secret reference, never a checked-in production value. |
| Production value | Deployment secret reference; provision and rotation-test before enabling the dependent feature. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/java/com/giftplatform/otp/config/OtpConfiguration.java:24 | @Value injection | `${gift.otp.hmac-key-base64:<redacted>}` |
| backend/src/main/java/com/giftplatform/security/recovery/EmailRecoveryController.java:42 | @Value injection | `${gift.otp.hmac-key-base64:<redacted>}` |
| backend/src/main/java/com/giftplatform/security/recovery/GroupEmailEnrollmentController.java:36 | @Value injection | `${gift.otp.hmac-key-base64:<redacted>}` |
| backend/src/main/java/com/giftplatform/security/recovery/GroupMobileEnrollmentController.java:43 | @Value injection | `${gift.otp.hmac-key-base64:<redacted>}` |
| backend/src/main/java/com/giftplatform/security/recovery/SignupEmailVerificationService.java:28 | @Value injection | `${gift.otp.hmac-key-base64:<redacted>}}` |
| backend/src/main/java/com/giftplatform/security/verification/MobileVerificationService.java:24 | @Value injection | `${gift.otp.hmac-key-base64:<redacted>}}` |
| backend/src/main/java/com/giftplatform/security/verification/VerificationSettingsController.java:19 | @Value injection | `${gift.otp.hmac-key-base64:<redacted>}}` |
| backend/src/main/resources/application-production.properties:23 | application-production.properties | `${GIFT_OTP_HMAC_KEY_BASE64:<redacted>}` |
| backend/src/main/resources/application.properties:58 | application.properties | `${GIFT_OTP_HMAC_KEY_BASE64:<redacted>}}` |
| backend/src/main/resources/application.properties.example:58 | application.properties.example | `${GIFT_OTP_HMAC_KEY_BASE64:<redacted>}}` |


## gift.otp.secret-base64

| Attribute | Specification |
| --- | --- |
| Purpose | Scoped OTP or email/SMS verification and delivery |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | GIFT_OTP_SECRET_BASE64 |
| Conventional environment name | GIFT_OTP_SECRET_BASE64 |
| Sensitivity | Sensitive; use a secret reference, never a checked-in production value. |
| Production value | Deployment secret reference; provision and rotation-test before enabling the dependent feature. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/java/com/giftplatform/otp/config/OtpConfiguration.java:22 | @Value injection | `${gift.otp.secret-base64:<redacted>}` |
| backend/src/main/resources/application-production.properties:22 | application-production.properties | `${GIFT_OTP_SECRET_BASE64:<redacted>}` |
| backend/src/main/resources/application.properties:19 | application.properties | `${GIFT_OTP_SECRET_BASE64:<redacted>}` |
| backend/src/main/resources/application.properties.example:19 | application.properties.example | `${GIFT_OTP_SECRET_BASE64:<redacted>}` |


## gift.otp.sms.expiry-minutes

| Attribute | Specification |
| --- | --- |
| Purpose | Scoped OTP or email/SMS verification and delivery |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | GIFT_OTP_SMS_EXPIRY_MINUTES |
| Conventional environment name | GIFT_OTP_SMS_EXPIRY_MINUTES |
| Sensitivity | Not classified as a secret by the catalogue; review deployment exposure. |
| Production value | Not provided. Choose deliberately from the observed defaults below; production proposal is described in Document 3. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/java/com/giftplatform/security/delivery/SmsDeliveryConfiguration.java:31 | Environment.getProperty | `Integer.class,5` |
| backend/src/main/java/com/giftplatform/security/verification/MobileVerificationService.java:25 | @Value injection | `${gift.otp.sms.expiry-minutes:5}` |
| backend/src/main/resources/application.properties:85 | application.properties | `${GIFT_OTP_SMS_EXPIRY_MINUTES:5}` |
| backend/src/main/resources/application.properties.example:84 | application.properties.example | `${GIFT_OTP_SMS_EXPIRY_MINUTES:5}` |


## gift.otp.sms.msg91.auth-key

| Attribute | Specification |
| --- | --- |
| Purpose | Scoped OTP or email/SMS verification and delivery |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | GIFT_MSG91_AUTH_KEY |
| Conventional environment name | GIFT_OTP_SMS_MSG91_AUTH_KEY |
| Sensitivity | Sensitive; use a secret reference, never a checked-in production value. |
| Production value | Deployment secret reference; provision and rotation-test before enabling the dependent feature. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/java/com/giftplatform/security/delivery/SmsDeliveryConfiguration.java:29 | Environment.getProperty | `<redacted configured value>` |
| backend/src/main/resources/application.properties:83 | application.properties | `${GIFT_MSG91_AUTH_KEY:<redacted>}` |
| backend/src/main/resources/application.properties.example:82 | application.properties.example | `${GIFT_MSG91_AUTH_KEY:<redacted>}` |
| backend/src/release/resources/application.properties:26 | application.properties | `${GIFT_MSG91_AUTH_KEY:<redacted>}` |


## gift.otp.sms.msg91.template-id

| Attribute | Specification |
| --- | --- |
| Purpose | Scoped OTP or email/SMS verification and delivery |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | GIFT_MSG91_TEMPLATE_ID |
| Conventional environment name | GIFT_OTP_SMS_MSG91_TEMPLATE_ID |
| Sensitivity | Not classified as a secret by the catalogue; review deployment exposure. |
| Production value | Not provided. Choose deliberately from the observed defaults below; production proposal is described in Document 3. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/java/com/giftplatform/security/delivery/SmsDeliveryConfiguration.java:30 | Environment.getProperty | `""` |
| backend/src/main/java/com/giftplatform/security/delivery/SmsDeliveryConfiguration.java:31 | Environment.getProperty | `""` |
| backend/src/main/java/com/giftplatform/security/delivery/SmsDeliveryConfiguration.java:31 | Environment.getProperty | `""` |
| backend/src/main/resources/application.properties:84 | application.properties | `${GIFT_MSG91_TEMPLATE_ID:}` |
| backend/src/main/resources/application.properties.example:83 | application.properties.example | `${GIFT_MSG91_TEMPLATE_ID:}` |
| backend/src/release/resources/application.properties:27 | application.properties | `${GIFT_MSG91_TEMPLATE_ID:}` |


## gift.otp.sms.provider

| Attribute | Specification |
| --- | --- |
| Purpose | Scoped OTP or email/SMS verification and delivery |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | GIFT_OTP_SMS_PROVIDER |
| Conventional environment name | GIFT_OTP_SMS_PROVIDER |
| Sensitivity | Not classified as a secret by the catalogue; review deployment exposure. |
| Production value | Not provided. Choose deliberately from the observed defaults below; production proposal is described in Document 3. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/java/com/giftplatform/operations/ProductionConfigurationPolicy.java:19 | Environment.getProperty | `""` |
| backend/src/main/java/com/giftplatform/security/delivery/SmsDeliveryConfiguration.java:21 | @Value injection | `${gift.otp.sms.provider:disabled}` |
| backend/src/main/resources/application.properties:82 | application.properties | `${GIFT_OTP_SMS_PROVIDER:disabled}` |
| backend/src/main/resources/application.properties.example:81 | application.properties.example | `${GIFT_OTP_SMS_PROVIDER:disabled}` |
| backend/src/release/resources/application.properties:25 | application.properties | `${GIFT_OTP_SMS_PROVIDER:disabled}` |


## gift.otp.sms.real-verification-enabled

| Attribute | Specification |
| --- | --- |
| Purpose | Scoped OTP or email/SMS verification and delivery |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | No explicit placeholder name found; Spring relaxed binding may apply. |
| Conventional environment name | GIFT_OTP_SMS_REAL_VERIFICATION_ENABLED |
| Sensitivity | Not classified as a secret by the catalogue; review deployment exposure. |
| Production value | Not provided. Choose deliberately from the observed defaults below; production proposal is described in Document 3. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/java/com/giftplatform/security/recovery/GroupMobileEnrollmentController.java:44 | @Value injection | `${gift.otp.sms.real-verification-enabled:false}` |


## gift.otp.sms.simulator.enabled

| Attribute | Specification |
| --- | --- |
| Purpose | Scoped OTP or email/SMS verification and delivery |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | No explicit placeholder name found; Spring relaxed binding may apply. |
| Conventional environment name | GIFT_OTP_SMS_SIMULATOR_ENABLED |
| Sensitivity | Not classified as a secret by the catalogue; review deployment exposure. |
| Production value | Not provided. Choose deliberately from the observed defaults below; production proposal is described in Document 3. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/java/com/giftplatform/operations/ProductionConfigurationPolicy.java:19 | Environment.getProperty | `Boolean.class,false` |
| backend/src/main/java/com/giftplatform/security/delivery/SmsDeliveryConfiguration.java:22 | @Value injection | `${gift.otp.sms.simulator.enabled:false}` |
| backend/src/main/java/com/giftplatform/security/recovery/GroupMobileEnrollmentController.java:45 | @Value injection | `${gift.otp.sms.simulator.enabled:false}` |
| backend/src/main/resources/application-production.properties:26 | application-production.properties | `false` |


## gift.otp.sms.simulator.inbox-dir

| Attribute | Specification |
| --- | --- |
| Purpose | Scoped OTP or email/SMS verification and delivery |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | No explicit placeholder name found; Spring relaxed binding may apply. |
| Conventional environment name | GIFT_OTP_SMS_SIMULATOR_INBOX_DIR |
| Sensitivity | Not classified as a secret by the catalogue; review deployment exposure. |
| Production value | Not provided. Choose deliberately from the observed defaults below; production proposal is described in Document 3. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/java/com/giftplatform/security/delivery/SmsDeliveryConfiguration.java:37 | Environment.getProperty | `""` |


## gift.payment.account-verification.enabled

| Attribute | Specification |
| --- | --- |
| Purpose | Receiving destination, manual contribution or account-verification gate |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | GIFT_ACCOUNT_VERIFICATION_ENABLED |
| Conventional environment name | GIFT_PAYMENT_ACCOUNT_VERIFICATION_ENABLED |
| Sensitivity | Not classified as a secret by the catalogue; review deployment exposure. |
| Production value | Not provided. Choose deliberately from the observed defaults below; production proposal is described in Document 3. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/java/com/giftplatform/hostpayment/verification/account/AccountVerificationClient.java:19 | @Value injection | `${gift.payment.account-verification.enabled:true}` |
| backend/src/main/resources/application.properties:49 | application.properties | `${GIFT_ACCOUNT_VERIFICATION_ENABLED:true}` |
| backend/src/main/resources/application.properties.example:49 | application.properties.example | `${GIFT_ACCOUNT_VERIFICATION_ENABLED:true}` |
| backend/src/release/resources/application.properties:20 | application.properties | `false` |


## gift.payment.account-verification.environment

| Attribute | Specification |
| --- | --- |
| Purpose | Receiving destination, manual contribution or account-verification gate |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | GIFT_ACCOUNT_VERIFICATION_ENVIRONMENT |
| Conventional environment name | GIFT_PAYMENT_ACCOUNT_VERIFICATION_ENVIRONMENT |
| Sensitivity | Not classified as a secret by the catalogue; review deployment exposure. |
| Production value | Not provided. Choose deliberately from the observed defaults below; production proposal is described in Document 3. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/java/com/giftplatform/hostpayment/verification/account/AccountVerificationClient.java:20 | @Value injection | `${gift.payment.account-verification.environment:SANDBOX}` |
| backend/src/main/resources/application.properties:50 | application.properties | `${GIFT_ACCOUNT_VERIFICATION_ENVIRONMENT:SANDBOX}` |
| backend/src/main/resources/application.properties.example:50 | application.properties.example | `${GIFT_ACCOUNT_VERIFICATION_ENVIRONMENT:SANDBOX}` |


## gift.payment.destination-verification.enabled

| Attribute | Specification |
| --- | --- |
| Purpose | Receiving destination, manual contribution or account-verification gate |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | No explicit placeholder name found; Spring relaxed binding may apply. |
| Conventional environment name | GIFT_PAYMENT_DESTINATION_VERIFICATION_ENABLED |
| Sensitivity | Not classified as a secret by the catalogue; review deployment exposure. |
| Production value | Not provided. Choose deliberately from the observed defaults below; production proposal is described in Document 3. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/java/com/giftplatform/hostpayment/verification/HostDestinationVerificationInboxController.java:29 | @Value injection | `${gift.payment.destination-verification.enabled:false}` |
| backend/src/main/java/com/giftplatform/hostpayment/verification/ProviderVerificationGate.java:17 | @Value injection | `${gift.payment.destination-verification.enabled:false}` |


## gift.payment.destination-verification.key-id

| Attribute | Specification |
| --- | --- |
| Purpose | Receiving destination, manual contribution or account-verification gate |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | No explicit placeholder name found; Spring relaxed binding may apply. |
| Conventional environment name | GIFT_PAYMENT_DESTINATION_VERIFICATION_KEY_ID |
| Sensitivity | Not classified as a secret by the catalogue; review deployment exposure. |
| Production value | Not provided. Choose deliberately from the observed defaults below; production proposal is described in Document 3. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/java/com/giftplatform/hostpayment/verification/ProviderVerificationGate.java:19 | @Value injection | `${gift.payment.destination-verification.key-id:}` |


## gift.payment.destination-verification.key-secret

| Attribute | Specification |
| --- | --- |
| Purpose | Receiving destination, manual contribution or account-verification gate |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | No explicit placeholder name found; Spring relaxed binding may apply. |
| Conventional environment name | GIFT_PAYMENT_DESTINATION_VERIFICATION_KEY_SECRET |
| Sensitivity | Sensitive; use a secret reference, never a checked-in production value. |
| Production value | Deployment secret reference; provision and rotation-test before enabling the dependent feature. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/java/com/giftplatform/hostpayment/verification/ProviderVerificationGate.java:20 | @Value injection | `${gift.payment.destination-verification.key-secret:<redacted>}` |


## gift.payment.destination-verification.provider

| Attribute | Specification |
| --- | --- |
| Purpose | Receiving destination, manual contribution or account-verification gate |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | No explicit placeholder name found; Spring relaxed binding may apply. |
| Conventional environment name | GIFT_PAYMENT_DESTINATION_VERIFICATION_PROVIDER |
| Sensitivity | Not classified as a secret by the catalogue; review deployment exposure. |
| Production value | Not provided. Choose deliberately from the observed defaults below; production proposal is described in Document 3. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/java/com/giftplatform/hostpayment/verification/ProviderVerificationGate.java:18 | @Value injection | `${gift.payment.destination-verification.provider:DISABLED}` |


## gift.payment.direct-transfers.enabled

| Attribute | Specification |
| --- | --- |
| Purpose | Receiving destination, manual contribution or account-verification gate |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | GIFT_DIRECT_TRANSFERS_ENABLED |
| Conventional environment name | GIFT_PAYMENT_DIRECT_TRANSFERS_ENABLED |
| Sensitivity | Not classified as a secret by the catalogue; review deployment exposure. |
| Production value | Not provided. Choose deliberately from the observed defaults below; production proposal is described in Document 3. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/java/com/giftplatform/payment/direct/GuestDirectTransferService.java:25 | @Value injection | `${gift.payment.direct-transfers.enabled:false}` |
| backend/src/main/java/com/giftplatform/payment/direct/GuestVerifiedDestinationService.java:26 | @Value injection | `${gift.payment.direct-transfers.enabled:false}` |
| backend/src/main/resources/application.properties:34 | application.properties | `true` |
| backend/src/main/resources/application.properties.example:34 | application.properties.example | `true` |
| backend/src/release/resources/application.properties:15 | application.properties | `${GIFT_DIRECT_TRANSFERS_ENABLED:false}` |


## gift.payment.direct-transfers.reconciliation.enabled

| Attribute | Specification |
| --- | --- |
| Purpose | Receiving destination, manual contribution or account-verification gate |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | No explicit placeholder name found; Spring relaxed binding may apply. |
| Conventional environment name | GIFT_PAYMENT_DIRECT_TRANSFERS_RECONCILIATION_ENABLED |
| Sensitivity | Not classified as a secret by the catalogue; review deployment exposure. |
| Production value | Not provided. Choose deliberately from the observed defaults below; production proposal is described in Document 3. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/java/com/giftplatform/payment/direct/GuestDirectTransferService.java:26 | @Value injection | `${gift.payment.direct-transfers.reconciliation.enabled:false}` |
| backend/src/main/java/com/giftplatform/payment/direct/GuestVerifiedDestinationService.java:27 | @Value injection | `${gift.payment.direct-transfers.reconciliation.enabled:false}` |
| backend/src/main/resources/application.properties:35 | application.properties | `false` |
| backend/src/main/resources/application.properties.example:35 | application.properties.example | `false` |
| backend/src/release/resources/application.properties:16 | application.properties | `false` |


## gift.payment.encryption-key-base64

| Attribute | Specification |
| --- | --- |
| Purpose | Receiving-account field encryption key |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | GIFT_PAYMENT_ENCRYPTION_KEY_BASE64 |
| Conventional environment name | GIFT_PAYMENT_ENCRYPTION_KEY_BASE64 |
| Sensitivity | Sensitive; use a secret reference, never a checked-in production value. |
| Production value | Deployment secret reference; provision and rotation-test before enabling the dependent feature. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/java/com/giftplatform/hostpayment/security/PaymentDataProtectionConfig.java:14 | Environment.getProperty | `<redacted configured value>` |
| backend/src/main/java/com/giftplatform/operations/ProductionConfigurationPolicy.java:25 | Environment.getProperty | `<redacted configured value>` |
| backend/src/main/resources/application-production.properties:24 | application-production.properties | `${GIFT_PAYMENT_ENCRYPTION_KEY_BASE64:<redacted>}` |


## gift.payment.fingerprint-key-base64

| Attribute | Specification |
| --- | --- |
| Purpose | Stable receiving-account duplicate fingerprint key |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | GIFT_PAYMENT_FINGERPRINT_KEY_BASE64 |
| Conventional environment name | GIFT_PAYMENT_FINGERPRINT_KEY_BASE64 |
| Sensitivity | Sensitive; use a secret reference, never a checked-in production value. |
| Production value | Deployment secret reference; provision and rotation-test before enabling the dependent feature. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/java/com/giftplatform/hostpayment/security/PaymentDataProtectionConfig.java:15 | Environment.getProperty | `<redacted configured value>` |
| backend/src/main/java/com/giftplatform/operations/ProductionConfigurationPolicy.java:25 | Environment.getProperty | `<redacted configured value>` |
| backend/src/main/resources/application-production.properties:25 | application-production.properties | `${GIFT_PAYMENT_FINGERPRINT_KEY_BASE64:<redacted>}` |


## gift.payment.profiles.enabled

| Attribute | Specification |
| --- | --- |
| Purpose | Receiving destination, manual contribution or account-verification gate |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | No explicit placeholder name found; Spring relaxed binding may apply. |
| Conventional environment name | GIFT_PAYMENT_PROFILES_ENABLED |
| Sensitivity | Not classified as a secret by the catalogue; review deployment exposure. |
| Production value | Not provided. Choose deliberately from the observed defaults below; production proposal is described in Document 3. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/java/com/giftplatform/hostpayment/controller/EventAccountLinkController.java:11 | conditional bean registration | `name="gift.payment.profiles.enabled",havingValue="true"` |
| backend/src/main/java/com/giftplatform/hostpayment/controller/HostPaymentProfileController.java:8 | conditional bean registration | `name="gift.payment.profiles.enabled",havingValue="true"` |
| backend/src/main/java/com/giftplatform/hostpayment/controller/HostPaymentProfileExceptionHandler.java:8 | conditional bean registration | `name="gift.payment.profiles.enabled",havingValue="true"` |
| backend/src/main/java/com/giftplatform/hostpayment/controller/ReceivingAccountHealthController.java:14 | conditional bean registration | `name="gift.payment.profiles.enabled",havingValue="true"` |
| backend/src/main/java/com/giftplatform/hostpayment/qr/HostUpiQrConfiguration.java:11 | conditional bean registration | `prefix="gift.payment.profiles",name="enabled",havingValue="true"` |
| backend/src/main/java/com/giftplatform/hostpayment/qr/HostUpiQrController.java:16 | conditional bean registration | `prefix="gift.payment.profiles",name="enabled",havingValue="true"` |
| backend/src/main/java/com/giftplatform/hostpayment/security/PaymentDataProtectionConfig.java:10 | conditional bean registration | `name = "gift.payment.profiles.enabled", havingValue = "true"` |
| backend/src/main/java/com/giftplatform/hostpayment/service/EventAccountLinkService.java:17 | conditional bean registration | `name="gift.payment.profiles.enabled",havingValue="true"` |
| backend/src/main/java/com/giftplatform/hostpayment/service/HostPaymentProfileService.java:16 | conditional bean registration | `name="gift.payment.profiles.enabled",havingValue="true"` |
| backend/src/main/java/com/giftplatform/hostpayment/verification/HostDestinationVerificationInboxController.java:22 | conditional bean registration | `name="gift.payment.profiles.enabled", havingValue="true"` |
| backend/src/main/java/com/giftplatform/hostpayment/verification/HostDestinationVerificationService.java:25 | conditional bean registration | `name = "gift.payment.profiles.enabled",     havingValue = "true"` |
| backend/src/main/java/com/giftplatform/hostpayment/verification/HostDestinationVerifierConfiguration.java:8 | conditional bean registration | `name = "gift.payment.profiles.enabled",     havingValue = "true"` |
| backend/src/main/java/com/giftplatform/hostpayment/verification/VerificationAttemptCoordinator.java:12 | conditional bean registration | `name="gift.payment.profiles.enabled",havingValue="true"` |
| backend/src/main/java/com/giftplatform/hostpayment/verification/account/AccountVerificationController.java:18 | conditional bean registration | `name="gift.payment.profiles.enabled",havingValue="true"` |
| backend/src/main/java/com/giftplatform/operations/ProductionConfigurationPolicy.java:23 | Environment.getProperty | `Boolean.class,false` |
| backend/src/main/java/com/giftplatform/payment/direct/GuestVerifiedDestinationController.java:8 | conditional bean registration | `name="gift.payment.profiles.enabled",havingValue="true"` |
| backend/src/main/java/com/giftplatform/payment/direct/GuestVerifiedDestinationService.java:17 | conditional bean registration | `name="gift.payment.profiles.enabled",havingValue="true"` |
| backend/src/main/resources/application.properties:33 | application.properties | `true` |
| backend/src/main/resources/application.properties.example:33 | application.properties.example | `true` |
| backend/src/release/resources/application.properties:14 | application.properties | `true` |


## gift.payment.razorpay.key-id

| Attribute | Specification |
| --- | --- |
| Purpose | Razorpay contribution/commercial checkout and webhook configuration |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | RAZORPAY_KEY_ID |
| Conventional environment name | GIFT_PAYMENT_RAZORPAY_KEY_ID |
| Sensitivity | Not classified as a secret by the catalogue; review deployment exposure. |
| Production value | Not provided. Choose deliberately from the observed defaults below; production proposal is described in Document 3. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/java/com/giftplatform/monetization/CommercialPurchaseService.java:27 | @Value injection | `${gift.payment.razorpay.key-id:}` |
| backend/src/main/java/com/giftplatform/payment/provider/RazorpayClient.java:27 | @Value injection | `${gift.payment.razorpay.key-id:}` |
| backend/src/main/java/com/giftplatform/payment/service/GuestCheckoutService.java:21 | @Value injection | `${gift.payment.razorpay.key-id:}` |
| backend/src/main/resources/application.properties:28 | application.properties | `${RAZORPAY_KEY_ID:rzp_test_Tox0zMexeQFHDf}` |
| backend/src/main/resources/application.properties.example:28 | application.properties.example | `${RAZORPAY_KEY_ID}` |
| backend/src/release/resources/application.properties:17 | application.properties | `${RAZORPAY_KEY_ID:}` |


## gift.payment.razorpay.key-secret

| Attribute | Specification |
| --- | --- |
| Purpose | Razorpay contribution/commercial checkout and webhook configuration |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | RAZORPAY_KEY_SECRET |
| Conventional environment name | GIFT_PAYMENT_RAZORPAY_KEY_SECRET |
| Sensitivity | Sensitive; use a secret reference, never a checked-in production value. |
| Production value | Deployment secret reference; provision and rotation-test before enabling the dependent feature. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/java/com/giftplatform/monetization/CommercialPurchaseService.java:28 | @Value injection | `${gift.payment.razorpay.key-secret:<redacted>}` |
| backend/src/main/java/com/giftplatform/payment/provider/RazorpayClient.java:28 | @Value injection | `${gift.payment.razorpay.key-secret:<redacted>}` |
| backend/src/main/java/com/giftplatform/payment/service/RazorpayCaptureService.java:32 | @Value injection | `${gift.payment.razorpay.key-secret:<redacted>}` |
| backend/src/main/resources/application.properties:29 | application.properties | `${RAZORPAY_KEY_SECRET:<redacted>}` |
| backend/src/main/resources/application.properties.example:29 | application.properties.example | `${RAZORPAY_KEY_SECRET}` |
| backend/src/release/resources/application.properties:18 | application.properties | `${RAZORPAY_KEY_SECRET:<redacted>}` |


## gift.payment.razorpay.webhook-secret

| Attribute | Specification |
| --- | --- |
| Purpose | Razorpay contribution/commercial checkout and webhook configuration |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | RAZORPAY_WEBHOOK_SECRET |
| Conventional environment name | GIFT_PAYMENT_RAZORPAY_WEBHOOK_SECRET |
| Sensitivity | Sensitive; use a secret reference, never a checked-in production value. |
| Production value | Deployment secret reference; provision and rotation-test before enabling the dependent feature. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/java/com/giftplatform/payment/service/RazorpayWebhookService.java:32 | @Value injection | `${gift.payment.razorpay.webhook-secret:<redacted>}` |
| backend/src/main/resources/application.properties:30 | application.properties | `${RAZORPAY_WEBHOOK_SECRET:<redacted>}` |
| backend/src/main/resources/application.properties.example:30 | application.properties.example | `${RAZORPAY_WEBHOOK_SECRET}` |
| backend/src/release/resources/application.properties:19 | application.properties | `${RAZORPAY_WEBHOOK_SECRET:<redacted>}` |


## gift.payment.razorpayx.verification.enabled

| Attribute | Specification |
| --- | --- |
| Purpose | Independent bank-account validation configuration; not live payouts |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | No explicit placeholder name found; Spring relaxed binding may apply. |
| Conventional environment name | GIFT_PAYMENT_RAZORPAYX_VERIFICATION_ENABLED |
| Sensitivity | Not classified as a secret by the catalogue; review deployment exposure. |
| Production value | Not provided. Choose deliberately from the observed defaults below; production proposal is described in Document 3. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/java/com/giftplatform/hostpayment/verification/RazorpayXVerificationConfiguration.java:1 | @ConfigurationProperties record | `<binding default not declared; inspect safe accessor>` |


## gift.payment.razorpayx.verification.key-id

| Attribute | Specification |
| --- | --- |
| Purpose | Independent bank-account validation configuration; not live payouts |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | No explicit placeholder name found; Spring relaxed binding may apply. |
| Conventional environment name | GIFT_PAYMENT_RAZORPAYX_VERIFICATION_KEY_ID |
| Sensitivity | Not classified as a secret by the catalogue; review deployment exposure. |
| Production value | Not provided. Choose deliberately from the observed defaults below; production proposal is described in Document 3. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/java/com/giftplatform/hostpayment/verification/RazorpayXVerificationConfiguration.java:1 | @ConfigurationProperties record | `<binding default not declared; inspect safe accessor>` |


## gift.payment.razorpayx.verification.key-secret

| Attribute | Specification |
| --- | --- |
| Purpose | Independent bank-account validation configuration; not live payouts |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | No explicit placeholder name found; Spring relaxed binding may apply. |
| Conventional environment name | GIFT_PAYMENT_RAZORPAYX_VERIFICATION_KEY_SECRET |
| Sensitivity | Sensitive; use a secret reference, never a checked-in production value. |
| Production value | Deployment secret reference; provision and rotation-test before enabling the dependent feature. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/java/com/giftplatform/hostpayment/verification/RazorpayXVerificationConfiguration.java:1 | @ConfigurationProperties record | `<redacted configured value>` |


## gift.payment.razorpayx.verification.source-account-number

| Attribute | Specification |
| --- | --- |
| Purpose | Independent bank-account validation configuration; not live payouts |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | No explicit placeholder name found; Spring relaxed binding may apply. |
| Conventional environment name | GIFT_PAYMENT_RAZORPAYX_VERIFICATION_SOURCE_ACCOUNT_NUMBER |
| Sensitivity | Sensitive; use a secret reference, never a checked-in production value. |
| Production value | Deployment secret reference; provision and rotation-test before enabling the dependent feature. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/java/com/giftplatform/hostpayment/verification/RazorpayXVerificationConfiguration.java:1 | @ConfigurationProperties record | `<redacted configured value>` |


## gift.payment.settlement-ui.enabled

| Attribute | Specification |
| --- | --- |
| Purpose | Conditional blocked settlement/payout UI API, not provider dispatch |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | No explicit placeholder name found; Spring relaxed binding may apply. |
| Conventional environment name | GIFT_PAYMENT_SETTLEMENT_UI_ENABLED |
| Sensitivity | Not classified as a secret by the catalogue; review deployment exposure. |
| Production value | Not provided. Choose deliberately from the observed defaults below; production proposal is described in Document 3. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/java/com/giftplatform/payment/settlement/HostSettlementUiController.java:13 | conditional bean registration | `name="gift.payment.settlement-ui.enabled",havingValue="true"` |
| backend/src/main/resources/application-production.properties:29 | application-production.properties | `false` |


## gift.production.instance-count

| Attribute | Specification |
| --- | --- |
| Purpose | Framework/application property; inspect the declaration for its exact consumer |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | GIFT_PRODUCTION_INSTANCE_COUNT |
| Conventional environment name | GIFT_PRODUCTION_INSTANCE_COUNT |
| Sensitivity | Not classified as a secret by the catalogue; review deployment exposure. |
| Production value | Not provided. Choose deliberately from the observed defaults below; production proposal is described in Document 3. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/java/com/giftplatform/operations/ProductionConfigurationPolicy.java:20 | Environment.getProperty | `Integer.class,1` |
| backend/src/main/resources/application-production.properties:20 | application-production.properties | `${GIFT_PRODUCTION_INSTANCE_COUNT:1}` |


## gift.production.public-origin

| Attribute | Specification |
| --- | --- |
| Purpose | Framework/application property; inspect the declaration for its exact consumer |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | GIFT_PRODUCTION_PUBLIC_ORIGIN |
| Conventional environment name | GIFT_PRODUCTION_PUBLIC_ORIGIN |
| Sensitivity | Not classified as a secret by the catalogue; review deployment exposure. |
| Production value | Not provided. Choose deliberately from the observed defaults below; production proposal is described in Document 3. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/java/com/giftplatform/operations/ProductionConfigurationPolicy.java:17 | Environment.getProperty | `""` |
| backend/src/main/resources/application-production.properties:19 | application-production.properties | `${GIFT_PRODUCTION_PUBLIC_ORIGIN:}` |


## gift.public-base-url

| Attribute | Specification |
| --- | --- |
| Purpose | Framework/application property; inspect the declaration for its exact consumer |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | GIFT_PUBLIC_BASE_URL, GIFT_PRODUCTION_PUBLIC_ORIGIN |
| Conventional environment name | GIFT_PUBLIC_BASE_URL |
| Sensitivity | Not classified as a secret by the catalogue; review deployment exposure. |
| Production value | Not provided. Choose deliberately from the observed defaults below; production proposal is described in Document 3. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/java/com/giftplatform/notification/email/BrandedEmailService.java:21 | @Value injection | `${gift.public-base-url:${gift.production.public-origin:}}` |
| backend/src/main/resources/application.properties:96 | application.properties | `${GIFT_PUBLIC_BASE_URL:${GIFT_PRODUCTION_PUBLIC_ORIGIN:}}` |


## gift.sms.aws.region

| Attribute | Specification |
| --- | --- |
| Purpose | Phone-OTP SMS transport selection |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | GIFT_SMS_AWS_REGION |
| Conventional environment name | GIFT_SMS_AWS_REGION |
| Sensitivity | Not classified as a secret by the catalogue; review deployment exposure. |
| Production value | Not provided. Choose deliberately from the observed defaults below; production proposal is described in Document 3. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/java/com/giftplatform/otp/service/AwsSnsSmsConfiguration.java:14 | @Value injection | `${gift.sms.aws.region:ap-south-1}` |
| backend/src/main/resources/application-sms-example.yml:7 | application-sms-example.yml | `${GIFT_SMS_AWS_REGION:ap-south-1}` |


## gift.sms.aws.sender-id

| Attribute | Specification |
| --- | --- |
| Purpose | Phone-OTP SMS transport selection |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | GIFT_SMS_SENDER_ID |
| Conventional environment name | GIFT_SMS_AWS_SENDER_ID |
| Sensitivity | Not classified as a secret by the catalogue; review deployment exposure. |
| Production value | Not provided. Choose deliberately from the observed defaults below; production proposal is described in Document 3. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/java/com/giftplatform/otp/service/AwsSnsSmsSender.java:25 | @Value injection | `${gift.sms.aws.sender-id:}` |
| backend/src/main/resources/application-sms-example.yml:8 | application-sms-example.yml | `${GIFT_SMS_SENDER_ID:}` |


## gift.sms.aws.sms-type

| Attribute | Specification |
| --- | --- |
| Purpose | Phone-OTP SMS transport selection |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | No explicit placeholder name found; Spring relaxed binding may apply. |
| Conventional environment name | GIFT_SMS_AWS_SMS_TYPE |
| Sensitivity | Not classified as a secret by the catalogue; review deployment exposure. |
| Production value | Not provided. Choose deliberately from the observed defaults below; production proposal is described in Document 3. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/java/com/giftplatform/otp/service/AwsSnsSmsSender.java:26 | @Value injection | `${gift.sms.aws.sms-type:Transactional}` |
| backend/src/main/resources/application-sms-example.yml:9 | application-sms-example.yml | `Transactional` |


## gift.sms.provider

| Attribute | Specification |
| --- | --- |
| Purpose | Phone-OTP SMS transport selection |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | GIFT_SMS_PROVIDER |
| Conventional environment name | GIFT_SMS_PROVIDER |
| Sensitivity | Not classified as a secret by the catalogue; review deployment exposure. |
| Production value | Not provided. Choose deliberately from the observed defaults below; production proposal is described in Document 3. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/java/com/giftplatform/otp/service/AwsSnsSmsConfiguration.java:11 | conditional bean registration | `prefix = "gift.sms", name = "provider", havingValue = "sns"` |
| backend/src/main/java/com/giftplatform/otp/service/AwsSnsSmsSender.java:17 | conditional bean registration | `prefix = "gift.sms", name = "provider", havingValue = "sns"` |
| backend/src/main/java/com/giftplatform/otp/service/DisabledSmsSender.java:8 | conditional bean registration | `prefix = "gift.sms",         name = "provider",         havingValue = "disabled",         matchIfMissing = true` |
| backend/src/main/resources/application-sms-example.yml:5 | application-sms-example.yml | `${GIFT_SMS_PROVIDER:disabled}` |


## gift.verification.email.enabled

| Attribute | Specification |
| --- | --- |
| Purpose | Default verification channel behavior; runtime platform controls also apply |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | GIFT_EMAIL_VERIFICATION_ENABLED |
| Conventional environment name | GIFT_VERIFICATION_EMAIL_ENABLED |
| Sensitivity | Not classified as a secret by the catalogue; review deployment exposure. |
| Production value | Not provided. Choose deliberately from the observed defaults below; production proposal is described in Document 3. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/java/com/giftplatform/security/verification/VerificationSettings.java:17 | @Value injection | `${gift.verification.email.enabled:false}` |
| backend/src/main/resources/application.properties:70 | application.properties | `${GIFT_EMAIL_VERIFICATION_ENABLED:false}` |
| backend/src/main/resources/application.properties.example:69 | application.properties.example | `${GIFT_EMAIL_VERIFICATION_ENABLED:false}` |


## gift.verification.mobile.enabled

| Attribute | Specification |
| --- | --- |
| Purpose | Default verification channel behavior; runtime platform controls also apply |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | GIFT_MOBILE_OTP_ENABLED |
| Conventional environment name | GIFT_VERIFICATION_MOBILE_ENABLED |
| Sensitivity | Not classified as a secret by the catalogue; review deployment exposure. |
| Production value | Not provided. Choose deliberately from the observed defaults below; production proposal is described in Document 3. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/java/com/giftplatform/security/verification/VerificationSettings.java:16 | @Value injection | `${gift.verification.mobile.enabled:false}` |
| backend/src/main/resources/application.properties:69 | application.properties | `${GIFT_MOBILE_OTP_ENABLED:false}` |
| backend/src/main/resources/application.properties.example:68 | application.properties.example | `${GIFT_MOBILE_OTP_ENABLED:false}` |


## java.io.tmpdir

| Attribute | Specification |
| --- | --- |
| Purpose | Framework/application property; inspect the declaration for its exact consumer |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | No explicit placeholder name found; Spring relaxed binding may apply. |
| Conventional environment name | JAVA_IO_TMPDIR |
| Sensitivity | Not classified as a secret by the catalogue; review deployment exposure. |
| Production value | Not provided. Choose deliberately from the observed defaults below; production proposal is described in Document 3. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/java/com/giftplatform/celebration/GuestbookPhotoController.java:55 | Environment.getProperty | `<no source default>` |


## management.endpoint.health.group.readiness.include

| Attribute | Specification |
| --- | --- |
| Purpose | Actuator exposure/latency metrics |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | No explicit placeholder name found; Spring relaxed binding may apply. |
| Conventional environment name | MANAGEMENT_ENDPOINT_HEALTH_GROUP_READINESS_INCLUDE |
| Sensitivity | Not classified as a secret by the catalogue; review deployment exposure. |
| Production value | Not provided. Choose deliberately from the observed defaults below; production proposal is described in Document 3. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/resources/application-production.properties:17 | application-production.properties | `readinessState,db` |


## management.endpoint.health.probes.enabled

| Attribute | Specification |
| --- | --- |
| Purpose | Actuator exposure/latency metrics |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | No explicit placeholder name found; Spring relaxed binding may apply. |
| Conventional environment name | MANAGEMENT_ENDPOINT_HEALTH_PROBES_ENABLED |
| Sensitivity | Not classified as a secret by the catalogue; review deployment exposure. |
| Production value | Not provided. Choose deliberately from the observed defaults below; production proposal is described in Document 3. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/resources/application-production.properties:18 | application-production.properties | `true` |


## management.endpoint.health.show-components

| Attribute | Specification |
| --- | --- |
| Purpose | Actuator exposure/latency metrics |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | No explicit placeholder name found; Spring relaxed binding may apply. |
| Conventional environment name | MANAGEMENT_ENDPOINT_HEALTH_SHOW_COMPONENTS |
| Sensitivity | Not classified as a secret by the catalogue; review deployment exposure. |
| Production value | Not provided. Choose deliberately from the observed defaults below; production proposal is described in Document 3. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/resources/application-production.properties:16 | application-production.properties | `never` |


## management.endpoint.health.show-details

| Attribute | Specification |
| --- | --- |
| Purpose | Actuator exposure/latency metrics |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | No explicit placeholder name found; Spring relaxed binding may apply. |
| Conventional environment name | MANAGEMENT_ENDPOINT_HEALTH_SHOW_DETAILS |
| Sensitivity | Not classified as a secret by the catalogue; review deployment exposure. |
| Production value | Not provided. Choose deliberately from the observed defaults below; production proposal is described in Document 3. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/resources/application-production.properties:15 | application-production.properties | `never` |


## management.endpoints.web.base-path

| Attribute | Specification |
| --- | --- |
| Purpose | Actuator exposure/latency metrics |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | No explicit placeholder name found; Spring relaxed binding may apply. |
| Conventional environment name | MANAGEMENT_ENDPOINTS_WEB_BASE_PATH |
| Sensitivity | Not classified as a secret by the catalogue; review deployment exposure. |
| Production value | Not provided. Choose deliberately from the observed defaults below; production proposal is described in Document 3. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/resources/application-production.properties:14 | application-production.properties | `/actuator` |


## management.endpoints.web.exposure.include

| Attribute | Specification |
| --- | --- |
| Purpose | Actuator exposure/latency metrics |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | No explicit placeholder name found; Spring relaxed binding may apply. |
| Conventional environment name | MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE |
| Sensitivity | Not classified as a secret by the catalogue; review deployment exposure. |
| Production value | Not provided. Choose deliberately from the observed defaults below; production proposal is described in Document 3. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/resources/application-production.properties:13 | application-production.properties | `health,metrics` |
| backend/src/main/resources/application.properties:44 | application.properties | `health,metrics` |
| backend/src/main/resources/application.properties.example:44 | application.properties.example | `health,metrics` |


## management.metrics.distribution.percentiles-histogram.http.server.requests

| Attribute | Specification |
| --- | --- |
| Purpose | Actuator exposure/latency metrics |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | No explicit placeholder name found; Spring relaxed binding may apply. |
| Conventional environment name | MANAGEMENT_METRICS_DISTRIBUTION_PERCENTILES_HISTOGRAM_HTTP_SERVER_REQUESTS |
| Sensitivity | Not classified as a secret by the catalogue; review deployment exposure. |
| Production value | Not provided. Choose deliberately from the observed defaults below; production proposal is described in Document 3. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/resources/application.properties:45 | application.properties | `true` |
| backend/src/main/resources/application.properties.example:45 | application.properties.example | `true` |


## management.metrics.distribution.slo.http.server.requests

| Attribute | Specification |
| --- | --- |
| Purpose | Actuator exposure/latency metrics |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | No explicit placeholder name found; Spring relaxed binding may apply. |
| Conventional environment name | MANAGEMENT_METRICS_DISTRIBUTION_SLO_HTTP_SERVER_REQUESTS |
| Sensitivity | Not classified as a secret by the catalogue; review deployment exposure. |
| Production value | Not provided. Choose deliberately from the observed defaults below; production proposal is described in Document 3. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/resources/application.properties:46 | application.properties | `100ms,300ms,1s,3s` |
| backend/src/main/resources/application.properties.example:46 | application.properties.example | `100ms,300ms,1s,3s` |


## server.error.include-binding-errors

| Attribute | Specification |
| --- | --- |
| Purpose | Framework/application property; inspect the declaration for its exact consumer |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | No explicit placeholder name found; Spring relaxed binding may apply. |
| Conventional environment name | SERVER_ERROR_INCLUDE_BINDING_ERRORS |
| Sensitivity | Not classified as a secret by the catalogue; review deployment exposure. |
| Production value | Not provided. Choose deliberately from the observed defaults below; production proposal is described in Document 3. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/resources/application-production.properties:8 | application-production.properties | `never` |


## server.error.include-message

| Attribute | Specification |
| --- | --- |
| Purpose | Framework/application property; inspect the declaration for its exact consumer |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | No explicit placeholder name found; Spring relaxed binding may apply. |
| Conventional environment name | SERVER_ERROR_INCLUDE_MESSAGE |
| Sensitivity | Not classified as a secret by the catalogue; review deployment exposure. |
| Production value | Not provided. Choose deliberately from the observed defaults below; production proposal is described in Document 3. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/java/com/giftplatform/operations/ProductionConfigurationPolicy.java:21 | Environment.getProperty | `""` |
| backend/src/main/resources/application-production.properties:6 | application-production.properties | `never` |


## server.error.include-stacktrace

| Attribute | Specification |
| --- | --- |
| Purpose | Framework/application property; inspect the declaration for its exact consumer |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | No explicit placeholder name found; Spring relaxed binding may apply. |
| Conventional environment name | SERVER_ERROR_INCLUDE_STACKTRACE |
| Sensitivity | Not classified as a secret by the catalogue; review deployment exposure. |
| Production value | Not provided. Choose deliberately from the observed defaults below; production proposal is described in Document 3. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/resources/application-production.properties:7 | application-production.properties | `never` |


## server.servlet.session.cookie.http-only

| Attribute | Specification |
| --- | --- |
| Purpose | Framework/application property; inspect the declaration for its exact consumer |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | No explicit placeholder name found; Spring relaxed binding may apply. |
| Conventional environment name | SERVER_SERVLET_SESSION_COOKIE_HTTP_ONLY |
| Sensitivity | Not classified as a secret by the catalogue; review deployment exposure. |
| Production value | Not provided. Choose deliberately from the observed defaults below; production proposal is described in Document 3. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/java/com/giftplatform/operations/ProductionConfigurationPolicy.java:15 | Environment.getProperty | `Boolean.class,false` |
| backend/src/main/resources/application-production.properties:3 | application-production.properties | `true` |


## server.servlet.session.cookie.same-site

| Attribute | Specification |
| --- | --- |
| Purpose | Framework/application property; inspect the declaration for its exact consumer |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | No explicit placeholder name found; Spring relaxed binding may apply. |
| Conventional environment name | SERVER_SERVLET_SESSION_COOKIE_SAME_SITE |
| Sensitivity | Not classified as a secret by the catalogue; review deployment exposure. |
| Production value | Not provided. Choose deliberately from the observed defaults below; production proposal is described in Document 3. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/java/com/giftplatform/operations/ProductionConfigurationPolicy.java:16 | Environment.getProperty | `""` |
| backend/src/main/resources/application-production.properties:4 | application-production.properties | `lax` |


## server.servlet.session.cookie.secure

| Attribute | Specification |
| --- | --- |
| Purpose | Framework/application property; inspect the declaration for its exact consumer |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | No explicit placeholder name found; Spring relaxed binding may apply. |
| Conventional environment name | SERVER_SERVLET_SESSION_COOKIE_SECURE |
| Sensitivity | Not classified as a secret by the catalogue; review deployment exposure. |
| Production value | Not provided. Choose deliberately from the observed defaults below; production proposal is described in Document 3. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/java/com/giftplatform/operations/ProductionConfigurationPolicy.java:14 | Environment.getProperty | `Boolean.class,false` |
| backend/src/main/resources/application-production.properties:2 | application-production.properties | `true` |


## server.servlet.session.timeout

| Attribute | Specification |
| --- | --- |
| Purpose | Framework/application property; inspect the declaration for its exact consumer |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | No explicit placeholder name found; Spring relaxed binding may apply. |
| Conventional environment name | SERVER_SERVLET_SESSION_TIMEOUT |
| Sensitivity | Not classified as a secret by the catalogue; review deployment exposure. |
| Production value | Not provided. Choose deliberately from the observed defaults below; production proposal is described in Document 3. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/resources/application-production.properties:5 | application-production.properties | `30m` |


## spring.application.name

| Attribute | Specification |
| --- | --- |
| Purpose | Framework/application property; inspect the declaration for its exact consumer |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | No explicit placeholder name found; Spring relaxed binding may apply. |
| Conventional environment name | SPRING_APPLICATION_NAME |
| Sensitivity | Not classified as a secret by the catalogue; review deployment exposure. |
| Production value | Not provided. Choose deliberately from the observed defaults below; production proposal is described in Document 3. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/resources/application.properties:1 | application.properties | `gift-platform-backend` |
| backend/src/main/resources/application.properties.example:1 | application.properties.example | `gift-platform-backend` |
| backend/src/release/resources/application.properties:3 | application.properties | `gift-platform-backend` |


## spring.config.import

| Attribute | Specification |
| --- | --- |
| Purpose | Framework/application property; inspect the declaration for its exact consumer |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | No explicit placeholder name found; Spring relaxed binding may apply. |
| Conventional environment name | SPRING_CONFIG_IMPORT |
| Sensitivity | Not classified as a secret by the catalogue; review deployment exposure. |
| Production value | Not provided. Choose deliberately from the observed defaults below; production proposal is described in Document 3. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/resources/application-prod.properties:3 | application-prod.properties | `classpath:application-production.properties` |


## spring.datasource.driver-class-name

| Attribute | Specification |
| --- | --- |
| Purpose | JDBC connectivity and DB credentials |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | No explicit placeholder name found; Spring relaxed binding may apply. |
| Conventional environment name | SPRING_DATASOURCE_DRIVER_CLASS_NAME |
| Sensitivity | Not classified as a secret by the catalogue; review deployment exposure. |
| Production value | Not provided. Choose deliberately from the observed defaults below; production proposal is described in Document 3. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/resources/application.properties:9 | application.properties | `org.postgresql.Driver` |
| backend/src/main/resources/application.properties.example:9 | application.properties.example | `org.postgresql.Driver` |
| backend/src/release/resources/application.properties:8 | application.properties | `org.postgresql.Driver` |


## spring.datasource.password

| Attribute | Specification |
| --- | --- |
| Purpose | JDBC connectivity and DB credentials |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | DB_PASSWORD, SPRING_DATASOURCE_PASSWORD |
| Conventional environment name | SPRING_DATASOURCE_PASSWORD |
| Sensitivity | Sensitive; use a secret reference, never a checked-in production value. |
| Production value | Deployment secret reference; provision and rotation-test before enabling the dependent feature. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/resources/application.properties:6 | application.properties | `${DB_PASSWORD}` |
| backend/src/main/resources/application.properties.example:6 | application.properties.example | `${SPRING_DATASOURCE_PASSWORD}` |
| backend/src/release/resources/application.properties:7 | application.properties | `${SPRING_DATASOURCE_PASSWORD:<redacted>}` |


## spring.datasource.url

| Attribute | Specification |
| --- | --- |
| Purpose | JDBC connectivity and DB credentials |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | SPRING_DATASOURCE_URL |
| Conventional environment name | SPRING_DATASOURCE_URL |
| Sensitivity | Not classified as a secret by the catalogue; review deployment exposure. |
| Production value | Not provided. Choose deliberately from the observed defaults below; production proposal is described in Document 3. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/resources/application.properties:4 | application.properties | `jdbc:postgresql://localhost:5433/giftplatform` |
| backend/src/main/resources/application.properties.example:4 | application.properties.example | `jdbc:postgresql://localhost:5433/giftplatform` |
| backend/src/release/resources/application.properties:5 | application.properties | `${SPRING_DATASOURCE_URL:}` |


## spring.datasource.username

| Attribute | Specification |
| --- | --- |
| Purpose | JDBC connectivity and DB credentials |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | DB_USERNAME, SPRING_DATASOURCE_USERNAME |
| Conventional environment name | SPRING_DATASOURCE_USERNAME |
| Sensitivity | Sensitive; use a secret reference, never a checked-in production value. |
| Production value | Deployment secret reference; provision and rotation-test before enabling the dependent feature. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/resources/application.properties:5 | application.properties | `${DB_USERNAME:<redacted>}` |
| backend/src/main/resources/application.properties.example:5 | application.properties.example | `${DB_USERNAME:<redacted>}` |
| backend/src/release/resources/application.properties:6 | application.properties | `${SPRING_DATASOURCE_USERNAME:<redacted>}` |


## spring.flyway.clean-disabled

| Attribute | Specification |
| --- | --- |
| Purpose | Versioned migration execution |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | No explicit placeholder name found; Spring relaxed binding may apply. |
| Conventional environment name | SPRING_FLYWAY_CLEAN_DISABLED |
| Sensitivity | Not classified as a secret by the catalogue; review deployment exposure. |
| Production value | Not provided. Choose deliberately from the observed defaults below; production proposal is described in Document 3. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/java/com/giftplatform/operations/ProductionConfigurationPolicy.java:30 | Environment.getProperty | `Boolean.class,false` |
| backend/src/main/resources/application-production.properties:12 | application-production.properties | `true` |


## spring.flyway.enabled

| Attribute | Specification |
| --- | --- |
| Purpose | Versioned migration execution |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | No explicit placeholder name found; Spring relaxed binding may apply. |
| Conventional environment name | SPRING_FLYWAY_ENABLED |
| Sensitivity | Not classified as a secret by the catalogue; review deployment exposure. |
| Production value | Not provided. Choose deliberately from the observed defaults below; production proposal is described in Document 3. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/java/com/giftplatform/operations/ProductionConfigurationPolicy.java:29 | Environment.getProperty | `Boolean.class,true` |
| backend/src/main/resources/application-production.properties:11 | application-production.properties | `true` |
| backend/src/main/resources/application.properties:16 | application.properties | `true` |
| backend/src/main/resources/application.properties.example:16 | application.properties.example | `true` |
| backend/src/release/resources/application.properties:11 | application.properties | `true` |


## spring.flyway.locations

| Attribute | Specification |
| --- | --- |
| Purpose | Versioned migration execution |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | No explicit placeholder name found; Spring relaxed binding may apply. |
| Conventional environment name | SPRING_FLYWAY_LOCATIONS |
| Sensitivity | Not classified as a secret by the catalogue; review deployment exposure. |
| Production value | Not provided. Choose deliberately from the observed defaults below; production proposal is described in Document 3. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/resources/application.properties:17 | application.properties | `classpath:db/migration` |
| backend/src/main/resources/application.properties.example:17 | application.properties.example | `classpath:db/migration` |


## spring.jpa.hibernate.ddl-auto

| Attribute | Specification |
| --- | --- |
| Purpose | ORM/schema validation and persistence behavior |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | No explicit placeholder name found; Spring relaxed binding may apply. |
| Conventional environment name | SPRING_JPA_HIBERNATE_DDL_AUTO |
| Sensitivity | Not classified as a secret by the catalogue; review deployment exposure. |
| Production value | Not provided. Choose deliberately from the observed defaults below; production proposal is described in Document 3. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/java/com/giftplatform/operations/ProductionConfigurationPolicy.java:28 | Environment.getProperty | `""` |
| backend/src/main/resources/application-production.properties:9 | application-production.properties | `validate` |
| backend/src/main/resources/application.properties:12 | application.properties | `validate` |
| backend/src/main/resources/application.properties.example:12 | application.properties.example | `validate` |
| backend/src/release/resources/application.properties:9 | application.properties | `validate` |


## spring.jpa.open-in-view

| Attribute | Specification |
| --- | --- |
| Purpose | ORM/schema validation and persistence behavior |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | No explicit placeholder name found; Spring relaxed binding may apply. |
| Conventional environment name | SPRING_JPA_OPEN_IN_VIEW |
| Sensitivity | Not classified as a secret by the catalogue; review deployment exposure. |
| Production value | Not provided. Choose deliberately from the observed defaults below; production proposal is described in Document 3. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/resources/application-production.properties:10 | application-production.properties | `false` |
| backend/src/main/resources/application.properties:13 | application.properties | `false` |
| backend/src/main/resources/application.properties.example:13 | application.properties.example | `false` |
| backend/src/release/resources/application.properties:10 | application.properties | `false` |


## spring.profiles.active

| Attribute | Specification |
| --- | --- |
| Purpose | Environment/profile selection; local simulators must not be used in production |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | SPRING_PROFILES_ACTIVE |
| Conventional environment name | SPRING_PROFILES_ACTIVE |
| Sensitivity | Not classified as a secret by the catalogue; review deployment exposure. |
| Production value | Not provided. Choose deliberately from the observed defaults below; production proposal is described in Document 3. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/java/com/giftplatform/security/recovery/GroupMobileEnrollmentController.java:46 | @Value injection | `${spring.profiles.active:}` |
| backend/src/release/resources/application.properties:4 | application.properties | `${SPRING_PROFILES_ACTIVE:production}` |


## spring.servlet.multipart.max-file-size

| Attribute | Specification |
| --- | --- |
| Purpose | HTTP multipart request limits |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | No explicit placeholder name found; Spring relaxed binding may apply. |
| Conventional environment name | SPRING_SERVLET_MULTIPART_MAX_FILE_SIZE |
| Sensitivity | Not classified as a secret by the catalogue; review deployment exposure. |
| Production value | Not provided. Choose deliberately from the observed defaults below; production proposal is described in Document 3. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/resources/application.properties:22 | application.properties | `5MB` |
| backend/src/main/resources/application.properties.example:22 | application.properties.example | `5MB` |
| backend/src/release/resources/application.properties:12 | application.properties | `5MB` |


## spring.servlet.multipart.max-request-size

| Attribute | Specification |
| --- | --- |
| Purpose | HTTP multipart request limits |
| Environments | Development/test/staging/production as selected by Spring profiles and deployment configuration. |
| Explicit environment names | No explicit placeholder name found; Spring relaxed binding may apply. |
| Conventional environment name | SPRING_SERVLET_MULTIPART_MAX_REQUEST_SIZE |
| Sensitivity | Not classified as a secret by the catalogue; review deployment exposure. |
| Production value | Not provided. Choose deliberately from the observed defaults below; production proposal is described in Document 3. |
| Dependencies | Classes/properties in the declarations below and the corresponding module/provider. A disabled feature can remove controller beans entirely. |
| Default value | See each declaration: injection fallback, example file and active local property are separate layers and may conflict. |


| Source | Declaration kind | Safe observed value / expression |
| --- | --- | --- |
| backend/src/main/resources/application.properties:25 | application.properties | `6MB` |
| backend/src/main/resources/application.properties.example:25 | application.properties.example | `6MB` |
| backend/src/release/resources/application.properties:13 | application.properties | `6MB` |


# Database-backed configuration

These runtime tables also contain production configuration, grants, or commercial policy. Their fields/defaults/constraints are part of the [database catalogue](DATABASE_CATALOGUE.md), and changes belong to authenticated admin/owner APIs rather than environment substitution.

| Configuration table | Purpose |
| --- | --- |
| commercial_policy | Monetization, commercial access, attribution, provider-reported statements or routing-policy history. |
| event_celebration_settings | Event-scoped guest responses, guestbook, notification preferences or submission limits. |
| event_payment_account_links | Host receiving destinations, event-scoped account selection or independent account-verification attempts. |
| event_thank_you_settings | Event-scoped guest responses, guestbook, notification preferences or submission limits. |
| gift_group_payment_permissions | Invitation/group/individual gift coordination, contribution reporting or private gift progress. Monetary reporting is distinct from verified settlement. |
| host_invitation_upload_controls | Host receiving destinations, event-scoped account selection or independent account-verification attempts. |
| localization_event_controls | Persisted application object; exact structural contract follows. Narrative ownership beyond its FK/consumer code requires review. |
| localization_host_controls | Persisted application object; exact structural contract follows. Narrative ownership beyond its FK/consumer code requires review. |
| payment_channel_controls | Administrative configuration, grants, revision controls or change history; use the documented admin workflow. |
| payment_control_revision | Administrative configuration, grants, revision controls or change history; use the documented admin workflow. |
| platform_admin_action_audit | Administrative configuration, grants, revision controls or change history; use the documented admin workflow. |
| platform_brand_assets | Administrative configuration, grants, revision controls or change history; use the documented admin workflow. |
| platform_branding | Administrative configuration, grants, revision controls or change history; use the documented admin workflow. |
| platform_event_ai_design_settings | Administrative configuration, grants, revision controls or change history; use the documented admin workflow. |
| platform_event_album_settings | Administrative configuration, grants, revision controls or change history; use the documented admin workflow. |
| platform_event_message_settings | Administrative configuration, grants, revision controls or change history; use the documented admin workflow. |
| platform_event_module_grants | Administrative configuration, grants, revision controls or change history; use the documented admin workflow. |
| platform_experience_settings | Administrative configuration, grants, revision controls or change history; use the documented admin workflow. |
| platform_growth_features | Administrative configuration, grants, revision controls or change history; use the documented admin workflow. |
| platform_growth_settings | Administrative configuration, grants, revision controls or change history; use the documented admin workflow. |
| platform_host_module_grants | Administrative configuration, grants, revision controls or change history; use the documented admin workflow. |
| platform_languages | Administrative configuration, grants, revision controls or change history; use the documented admin workflow. |
| platform_localization_settings | Administrative configuration, grants, revision controls or change history; use the documented admin workflow. |
| platform_module_controls | Administrative configuration, grants, revision controls or change history; use the documented admin workflow. |
| platform_monetization_history | Administrative configuration, grants, revision controls or change history; use the documented admin workflow. |
| platform_monetization_settings | Administrative configuration, grants, revision controls or change history; use the documented admin workflow. |
| platform_monetization_streams | Administrative configuration, grants, revision controls or change history; use the documented admin workflow. |
| platform_tenants | Administrative configuration, grants, revision controls or change history; use the documented admin workflow. |
| platform_verification_settings | Administrative configuration, grants, revision controls or change history; use the documented admin workflow. |
| routing_commission_settings | Monetization, commercial access, attribution, provider-reported statements or routing-policy history. |


# Deployment-layer settings missing from source

TLS termination, canonical domains, DNS, trusted forwarded headers, session cookie Secure/SameSite/domain, external session store, JVM/container CPU/memory, DB pools/HA/PITR, storage mount replication, backup schedule, alert endpoints, WAF/CDN policy and telemetry retention are not completely declared here. Proposed values and unresolved decisions are in Document 3. JVM/AWS SDK environment credentials also come from their runtime credential chain and are not literal application secrets in this export.
