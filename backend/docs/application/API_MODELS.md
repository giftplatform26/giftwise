# API data models

Field annotations are the actual reflected Java constraints. A field without an annotation is not thereby optional: handler/service guards and database constraints may still require it. Nullable/default/business meaning absent from the declaration is explicitly unspecified. Money fields named *Paise are integer paise; gift targetAmount is a decimal rupee value where declared BigDecimal.

## com.giftplatform.admin.AccountPolicyController$Policy

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| requireEmail | boolean | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| requireMobile | boolean | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| verifyEmail | boolean | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| verifyMobile | boolean | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| allowGroupPin | boolean | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.admin.ExperienceSettingsService$Settings

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| viewMode | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.admin.GroupMemberEmailPolicyController$Input

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| enabled | boolean | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.admin.PlatformAdminAuditController$Page

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| entries | java.util.List<java.util.Map<java.lang.String, java.lang.Object>> | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| total | long | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| page | int | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| pageSize | int | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| totalPages | long | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| actions | java.util.List<java.lang.String> | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.admin.PlatformAdminConsoleController$Toggle

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| enabled | boolean | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.asset.GalleryAssetController$Tier

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| tier | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.asset.GalleryAssetService$AssetInfo

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| id | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| category | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| occasion | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| title | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| published | boolean | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| width | int | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| height | int | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| tier | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.audit.api.AdminAuditController$AuditEntry

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| id | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| actorId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| actorType | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| entityType | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| entityId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| action | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| oldValues | java.util.Map<java.lang.String, java.lang.Object> | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| newValues | java.util.Map<java.lang.String, java.lang.Object> | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| occurredAt | java.time.Instant | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| requestId | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.celebration.CelebrationController$GuestbookModeRequest

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| mode | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.celebration.CelebrationController$HostRsvp

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| id | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| guestName | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| guestEmail | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| attendance | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| partySize | int | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.celebration.CelebrationController$HostWish

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| id | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| guestName | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| message | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| status | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| photoAvailable | boolean | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.celebration.CelebrationController$RsvpRequest

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| guestName | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| guestEmail | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| attendance | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| partySize | java.lang.Integer | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.celebration.CelebrationController$RsvpSummary

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| yes | long | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| no | long | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| maybe | long | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| attendees | long | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.celebration.CelebrationController$Wish

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| id | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| guestName | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| message | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| photoAvailable | boolean | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.celebration.CelebrationController$WishRequest

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| guestName | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| message | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.celebration.MemoryAlbumController$Album

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| title | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| eventDate | java.time.LocalDate | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| memories | java.util.List<com.giftplatform.celebration.MemoryAlbumController$Memory> | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| totalCount | long | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| photoCount | long | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| hasMore | boolean | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| nextOffset | int | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.celebration.MemoryAlbumController$Memory

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| id | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| guestName | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| message | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| photoAvailable | boolean | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| createdAt | java.time.Instant | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| status | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.event.dto.EventRequest

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| title | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| description | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| occasionType | com.giftplatform.event.entity.OccasionType | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| eventDate | java.time.LocalDate | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| addressLine1 | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| city | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| state | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| postalCode | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| country | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| contactName | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| contactPhone | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| contactEmail | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| hostName | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| deliverySameAsEvent | java.lang.Boolean | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| deliveryAddress | com.giftplatform.event.entity.DeliveryAddress | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.event.dto.EventResponse

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| id | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| ownerId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| title | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| description | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| occasionType | com.giftplatform.event.entity.OccasionType | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| eventDate | java.time.LocalDate | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| status | com.giftplatform.event.entity.EventStatus | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| publicToken | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| hostName | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| canPublish | boolean | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| publishDisabledReasons | java.util.List<java.lang.String> | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| eventAddress | com.giftplatform.event.entity.DeliveryAddress | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| deliveryAddress | com.giftplatform.event.entity.DeliveryAddress | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| deliverySameAsEvent | boolean | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.event.entity.DeliveryAddress

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| addressLine1 | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| city | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| state | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| postalCode | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| country | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.event.entity.EventStatus

Kind: enum.

Allowed values: `DRAFT`, `PUBLISHED`, `COMPLETED`, `CANCELLED`

## com.giftplatform.event.entity.OccasionType

Kind: enum.

Allowed values: `WEDDING`, `BIRTHDAY`, `BABY_SHOWER`, `HOUSEWARMING`, `ANNIVERSARY`, `OTHER`

## com.giftplatform.gift.controller.GiftController$ProductLinkRequest

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| productUrl | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.gift.controller.GiftController$PublicationRequest

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| visibility | com.giftplatform.gift.entity.GiftVisibility | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.gift.dto.GiftRequest

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| name | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| description | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| imageKey | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| targetAmount | java.math.BigDecimal | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| fulfillmentOverride | com.giftplatform.gift.entity.FulfillmentPreference | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| priority | com.giftplatform.gift.entity.GiftPriority | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| productUrl | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| retailerLookupId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.gift.dto.GiftResponse

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| id | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| eventId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| version | long | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| name | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| description | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| imageKey | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| targetAmount | java.math.BigDecimal | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| status | com.giftplatform.gift.entity.GiftStatus | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| visibility | com.giftplatform.gift.entity.GiftVisibility | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| productUrl | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| fulfillmentOverride | com.giftplatform.gift.entity.FulfillmentPreference | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| effectiveFulfillmentPreference | com.giftplatform.gift.entity.FulfillmentPreference | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| priority | com.giftplatform.gift.entity.GiftPriority | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.gift.entity.FulfillmentPreference

Kind: enum.

Allowed values: `DELIVER_WHEN_FUNDED`, `AFTER_EVENT_CONFIRMATION`

## com.giftplatform.gift.entity.GiftPriority

Kind: enum.

Allowed values: `HIGH`, `MEDIUM`, `LOW`

## com.giftplatform.gift.entity.GiftStatus

Kind: enum.

Allowed values: `ACTIVE`, `CANCELLED`, `CLOSED_UNFUNDED`

## com.giftplatform.gift.entity.GiftVisibility

Kind: enum.

Allowed values: `DRAFT`, `PUBLIC`

## com.giftplatform.gift.photo.GiftPhotoService$Photo

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| imageKey | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| width | int | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| height | int | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.gift.retailer.RetailerMetadataController$Lookup

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| productUrl | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.gift.retailer.RetailerMetadataService$Preview

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| lookupId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| sourceUrl | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| name | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| description | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| targetAmount | java.math.BigDecimal | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| currency | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| imageKey | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| imageWarning | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.group.GroupContributionController$Credentials

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| username | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| password | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| pin | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| confirmPin | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| fullName | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| email | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| mobile | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| emailVerificationToken | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.group.GroupContributionController$FullNameInput

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| fullName | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.group.GroupContributionController$GroupView

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| id | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| name | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| creatorName | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| description | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| giftName | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| giftDescription | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| giftLink | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| targetPaise | java.lang.Long | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| status | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| independentGift | boolean | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.group.GroupContributionController$JoinRequest

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| displayName | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| mobile | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| password | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| confirmPassword | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| pin | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| confirmPin | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| email | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| emailVerificationToken | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.group.GroupContributionController$MemberLogin

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| displayName | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| password | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| pin | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| username | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.group.GroupContributionController$PinSetup

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| password | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| pin | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| confirmPin | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.group.GroupContributionController$PrivacySettings

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| joinPolicy | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| memberVisibility | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| amountVisibility | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| creatorDirectPaymentsEnabled | java.lang.Boolean | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.group.GroupContributionController$Review

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| memberId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| action | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.group.GroupContributionController$Setup

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| name | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| creatorName | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| creatorMobile | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| description | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| giftName | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| giftDescription | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| giftLink | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| targetPaise | java.lang.Long | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| giftId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| independentGift | java.lang.Boolean | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.group.GroupCreatorPaymentController$DestinationInput

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| kind | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| label | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| upiId | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| accountNumber | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| ifsc | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| accountHolder | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.group.GroupCreatorPaymentController$EnabledInput

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| enabled | boolean | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.group.GroupCreatorPaymentController$SelectInput

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| destinationId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.group.GroupCreatorPaymentController$SwitchInput

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| enabled | boolean | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.group.GroupGiftCollectionController$Reservation

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| giftId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.group.GroupGiftCompletionController$Fulfilment

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| status | com.giftplatform.group.GroupGiftCompletionController$Status | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| trackingDetails | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.group.GroupGiftCompletionController$Status

Kind: enum.

Allowed values: `GIFT_PURCHASED`, `TRACKING_SHARED`, `AMOUNT_TRANSFERRED_TO_HOST`, `COMPLETED`

## com.giftplatform.group.GroupGuestContributionController$Report

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| destinationId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| guestName | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| contact | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| amountPaise | java.lang.Long | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| transactionReference | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| submissionKey | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.group.GroupGuestContributionController$Settings

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| suggestedAmountPaise | java.lang.Long | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| enabled | boolean | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| upiDestinationId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| bankDestinationId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| accessMode | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.group.GuestGiftController$Delivery

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| status | com.giftplatform.group.GuestGiftController$DeliveryStatus | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| trackingDetails | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.group.GuestGiftController$DeliveryStatus

Kind: enum.

Allowed values: `PLANNED`, `GIFT_PURCHASED`, `TRACKING_SHARED`, `COMPLETED`

## com.giftplatform.group.GuestGiftController$IndividualGift

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| name | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| description | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| productUrl | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| guestName | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| email | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| mobile | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| contributionPaise | java.lang.Long | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.group.GuestGiftController$NewGift

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| name | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| description | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| productUrl | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.group.GuestGiftController$Selection

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| contributionType | com.giftplatform.group.GuestGiftController$Type | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| groupId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.group.GuestGiftController$Type

Kind: enum.

Allowed values: `INDIVIDUAL`, `GROUP`

## com.giftplatform.group.IndependentGroupGiftController$Link

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| productUrl | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.group.IndependentGroupGiftController$Plan

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| giftName | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| giftDescription | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| giftLink | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| targetPaise | java.lang.Long | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.group.IndividualGuestGiftController$Entry

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| sourceGiftId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| name | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| description | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| productUrl | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| guestName | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| email | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| mobile | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| givingMethod | com.giftplatform.group.IndividualGuestGiftController$Method | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| contributionPaise | java.lang.Long | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.group.IndividualGuestGiftController$HostContribution

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| destinationId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| method | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| transactionReference | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.group.IndividualGuestGiftController$Method

Kind: enum.

Allowed values: `LINK_AND_CONTRIBUTION`, `DIRECT_DELIVERY`

## com.giftplatform.hostpayment.controller.HostPaymentProfileController$AddBank

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| accountNumber | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| ifsc | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| accountHolderName | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| accountLabel | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| eventId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.hostpayment.controller.HostPaymentProfileController$AddUpi

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| upiId | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| accountLabel | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| eventId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.hostpayment.controller.HostPaymentProfileController$RenameAccount

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| accountLabel | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.hostpayment.controller.ReceivingAccountHealthController$Health

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| id | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| method | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| label | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| status | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| message | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| guestVisible | boolean | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.hostpayment.entity.VerificationStatus

Kind: enum.

Allowed values: `PENDING`, `VERIFIED`, `FAILED`, `REVOKED`

## com.giftplatform.hostpayment.service.HostPaymentProfileService$BankView

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| id | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| accountMasked | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| ifscMasked | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| verificationStatus | com.giftplatform.hostpayment.entity.VerificationStatus | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| enabled | boolean | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| preferred | boolean | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| hostConfirmed | boolean | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| accountLabel | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| hostId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| eventId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.hostpayment.service.HostPaymentProfileService$UpiView

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| id | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| masked | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| verificationStatus | com.giftplatform.hostpayment.entity.VerificationStatus | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| enabled | boolean | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| preferred | boolean | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| hostConfirmed | boolean | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| accountLabel | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| qrReady | boolean | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| hostId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| eventId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.hostpayment.verification.HostDestinationVerificationInboxController$Attempt

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| id | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| destinationId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| destinationKind | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| state | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| provider | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| createdAt | java.time.Instant | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| updatedAt | java.time.Instant | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.hostpayment.verification.HostDestinationVerificationInboxController$RequestResult

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| attemptId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| state | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| message | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.hostpayment.verification.account.AccountVerificationController$Check

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| id | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| environment | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| outcome | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| providerStatus | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| providerReference | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| nameMatched | boolean | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| createdAt | java.time.OffsetDateTime | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| completedAt | java.time.OffsetDateTime | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.hostpayment.verification.account.AccountVerificationController$Request

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| accountHolderName | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| consent | boolean | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.hostpayment.verification.account.AccountVerificationController$Settings

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| provider | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| environment | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| available | boolean | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| message | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.monetization.CommercialPurchaseService$Checkout

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| id | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| providerOrderId | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| keyId | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| amountPaise | long | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| currency | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| status | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| stream | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| resourceId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.monetization.CommercialPurchaseService$Confirmation

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| orderId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| providerOrderId | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| paymentId | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| signature | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.monetization.CommercialPurchaseService$Offer

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| stream | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| resourceId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| title | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| amountPaise | long | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| revision | long | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| durationDays | int | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| policyRevision | long | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| entitled | boolean | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.monetization.CommercialPurchaseService$Request

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| stream | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| resourceId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| idempotencyKey | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| acceptedAmountPaise | long | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.monetization.CommercialSetupService$Commission

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| id | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| revision | long | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| partnerId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| clickId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| externalReference | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| amountPaise | long | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| state | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| reason | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.monetization.CommercialSetupService$Moderation

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| state | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| reason | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.monetization.CommercialSetupService$Partner

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| id | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| revision | long | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| name | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| productDomain | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| enabled | boolean | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| parameterName | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| parameterValue | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| clickParameter | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| redirectTemplate | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| reason | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.monetization.CommercialSetupService$Placement

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| stream | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| title | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| description | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| url | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| category | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| occasion | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.monetization.CommercialSetupService$Policy

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| revision | long | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| freeActiveEvents | int | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| hostPlanDays | int | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| vendorDays | int | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| sponsorDays | int | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| reason | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.monetization.MonetizationService$Settings

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| revision | long | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| enabled | boolean | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| streams | java.util.List<com.giftplatform.monetization.MonetizationService$Stream> | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.monetization.MonetizationService$Stream

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| code | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| enabled | boolean | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| feeMode | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| fixedPaise | long | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| rateBps | int | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| capPaise | long | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| feeBearer | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| collectionReady | boolean | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.monetization.MonetizationService$Update

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| revision | long | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| enabled | boolean | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| streams | java.util.List<com.giftplatform.monetization.MonetizationService$Stream> | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| reason | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.notification.HostInboxController$Inbox

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| unread | long | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| entries | java.util.List<com.giftplatform.notification.HostInboxController$Item> | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.notification.HostInboxController$Item

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| id | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| eventId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| eventTitle | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| subject | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| message | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| evidence | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| sourceKind | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| createdAt | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| read | boolean | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.notification.HostThankYouService$Settings

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| hostName | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| emailSubject | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| emailBody | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| smsBody | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| cardTemplate | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| autoSend | boolean | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| emailEnabled | boolean | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| smsEnabled | boolean | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| whatsappEnabled | boolean | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.notification.HostThankYouService$Update

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| hostName | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| emailSubject | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| emailBody | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| smsBody | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| cardTemplate | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| autoSend | boolean | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| emailEnabled | boolean | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| smsEnabled | boolean | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| whatsappEnabled | boolean | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.otp.controller.dto.PhoneOtpRequest

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| phoneNumber | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.otp.controller.dto.PhoneOtpResponse

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| status | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| challengeId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| retryAfterSeconds | long | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.otp.controller.dto.PhoneOtpVerifyRequest

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| otp | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.payment.control.PaymentControlService$Change

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| revision | long | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| enabled | java.lang.Boolean | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| reason | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.payment.control.PaymentControlService$Control

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| channel | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| configured | java.lang.Boolean | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| platformEnabled | boolean | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| hostEnabled | boolean | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| eventEnabled | boolean | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| effectiveEnabled | boolean | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.payment.control.PaymentControlService$View

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| revision | long | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| scope | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| scopeId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| controls | java.util.List<com.giftplatform.payment.control.PaymentControlService$Control> | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.payment.control.RoutingCommissionService$Settings

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| revision | long | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| rateBps | int | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| routingActive | boolean | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| appliesTo | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.payment.control.RoutingCommissionService$Update

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| revision | java.lang.Long | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| rateBps | java.lang.Integer | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| reason | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.payment.controller.GuestPaymentConfirmationController$Request

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| localOrderId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| razorpayOrderId | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| razorpayPaymentId | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| razorpaySignature | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.payment.controller.PublicGiftProgressController$GiftProgress

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| giftId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| contributedPaise | long | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| contributionCount | long | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.payment.direct.GuestDirectTransferService$ClaimRequest

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| destinationId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| method | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| giftId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| amountPaise | long | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| guestName | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| guestEmail | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| guestPhone | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| transactionReference | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| idempotencyKey | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.payment.direct.GuestDirectTransferService$ClaimResult

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| claimId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| status | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| message | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.payment.direct.GuestDirectTransferService$Destination

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| id | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| method | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| masked | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| available | boolean | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.payment.direct.GuestVerifiedDestinationService$Destination

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| id | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| method | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| preferred | boolean | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| label | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| upiId | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| qrPngBase64 | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| beneficiaryName | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| accountNumber | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| ifsc | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.payment.hybrid.HybridCheckoutQuoteController$Request

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| eventId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| giftId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| contributionPaise | long | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.payment.hybrid.HybridCheckoutQuoteService$Quote

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| quoteId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| eventId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| giftId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| contributionPaise | long | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| processingFeePaise | long | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| totalPaise | long | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| method | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| notice | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| platformFeePaise | long | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| hostReceivesPaise | long | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| feeBearer | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| revision | long | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.payment.report.HostContributionReportService$Entry

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| id | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| source | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| guestName | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| giftId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| giftName | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| contributionType | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| method | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| amountPaise | long | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| giftPaise | long | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| walletPaise | long | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| verificationStatus | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| createdAt | java.time.OffsetDateTime | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.payment.report.HostContributionReportService$GiftOption

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| id | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| name | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| targetPaise | long | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.payment.report.HostContributionReportService$Report

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| eventId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| pendingPaise | long | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| verifiedPaise | long | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| verifiedGiftPaise | long | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| verifiedWalletPaise | long | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| entries | java.util.List<com.giftplatform.payment.report.HostContributionReportService$Entry> | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| gifts | java.util.List<com.giftplatform.payment.report.HostContributionReportService$GiftOption> | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| selectedGiftId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| selectedGiftTargetPaise | long | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| matchingCount | long | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| reportedGiftPaise | long | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| reportedWalletPaise | long | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| selectedGiftVerifiedPaise | long | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| selectedGiftReportedPaise | long | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.payment.service.GuestCheckoutService$Request

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| eventId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| giftId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| contributionType | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| amountPaise | long | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| idempotencyKey | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| guestName | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| guestEmail | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| guestPhone | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| acceptedFeePaise | java.lang.Long | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| acceptedTotalPaise | java.lang.Long | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| acceptedHostReceivesPaise | java.lang.Long | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.payment.service.GuestCheckoutService$Response

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| localOrderId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| razorpayOrderId | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| razorpayKeyId | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| amountPaise | long | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| currency | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| status | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| platformFeePaise | long | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| hostReceivesPaise | long | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.payment.service.RazorpayCaptureService$CaptureResult

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| orderId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| contributionId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| paymentId | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| giftPaise | long | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| walletPaise | long | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| alreadyProcessed | boolean | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.payment.settlement.HostPayoutRequestService$Result

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| requestId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| state | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| reason | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.payment.settlement.HostSettlementUiController$PayoutRequestBody

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| destinationId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| amountPaise | long | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| idempotencyKey | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.payment.settlement.HostSettlementUiService$Bank

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| id | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| maskedAccount | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| maskedIfsc | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| preferred | boolean | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.payment.settlement.HostSettlementUiService$Payout

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| id | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| eventId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| destinationId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| amountPaise | long | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| state | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| reason | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| cancelled | boolean | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.payment.settlement.HostSettlementUiService$Wallet

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| eventId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| availablePaise | long | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| heldPaise | long | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| withdrawablePaise | long | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| livePayoutsEnabled | boolean | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.payment.settlement.PayoutSafetyFinalizationService$Cancellation

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| requestId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| alreadyCancelled | boolean | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| releasedHolds | int | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.payment.verification.HostTransactionVerificationController$Transaction

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| id | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| source | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| provider | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| providerOrderId | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| providerPaymentId | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| method | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| amountPaise | long | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| currency | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| status | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| verifiedAt | java.time.OffsetDateTime | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| createdAt | java.time.OffsetDateTime | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| explanation | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| bankCode | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| bankTransactionId | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| rrn | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| upiTransactionId | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.publicevent.PublicEventController$PublicEventView

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| title | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| description | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| occasionType | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| eventDate | java.time.LocalDate | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| gifts | java.util.List<com.giftplatform.publicevent.PublicEventController$PublicGiftView> | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| features | java.util.Map<java.lang.String, java.lang.Boolean> | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| hostName | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| invitationTheme | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.publicevent.PublicEventController$PublicGiftView

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| id | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| name | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| description | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| targetAmount | java.math.BigDecimal | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| priority | com.giftplatform.gift.entity.GiftPriority | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| imageUrl | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| contributionEnabled | boolean | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| contributionDisabledReason | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| productUrl | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.qr.EventQrController$ThemeRequest

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| theme | com.giftplatform.qr.QrTheme | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.qr.EventQrService$QrDetails

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| eventId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| publicToken | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| theme | com.giftplatform.qr.QrTheme | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| url | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.qr.QrTheme

Kind: enum.

Allowed values: `MODERN_MINIMAL`, `ELEGANT_GOLD`, `BIRTHDAY_CELEBRATION`, `BABY_SHOWER`, `ANNIVERSARY`, `HOUSEWARMING`

## com.giftplatform.qr.design.InvitationDesignService$DesignResponse

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| id | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| eventId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| name | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| style | com.giftplatform.qr.design.InvitationStyle | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| greeting | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| hasImage | boolean | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.qr.design.InvitationStyle

Kind: enum.

Allowed values: `PHOTO_FRAME`, `FLORAL`, `CELEBRATION`, `MINIMAL`

## com.giftplatform.qr.design.ai.AiInvitationClient$Blueprint

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| designName | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| headline | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| message | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| closing | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| palette | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| layout | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| font | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.qr.design.ai.AiInvitationService$EventContext

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| eventId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| title | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| occasionType | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| eventDate | java.time.LocalDate | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| hostName | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| description | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| venue | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| visualStyle | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.qr.design.ai.AiInvitationService$Option

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| id | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| title | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.qr.design.ai.AiInvitationService$Preferences

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| visualStyle | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| tone | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| language | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| displayNames | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| publicVenue | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| startTime | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| specialMessage | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.qr.design.ai.AiInvitationService$Settings

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| enabled | boolean | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| available | boolean | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| message | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.qr.design.ai.AiInvitationService$Suggestion

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| eventId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| eventTitle | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| occasionType | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| hostName | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| eventDate | java.time.LocalDate | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| publicVenue | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| startTime | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| blueprint | com.giftplatform.qr.design.ai.AiInvitationClient$Blueprint | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.security.GiftUserPrincipal

Kind: class.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| userId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| email | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| passwordHash | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| role | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| enabled | boolean | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.security.dto.AuthResponse

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| userId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| email | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| role | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.security.dto.LoginRequest

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| identifier | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| password | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.security.recovery.EmailRecoveryController$CodeIssued

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| challengeId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| message | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.security.recovery.EmailRecoveryController$EmailStatus

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| email | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| verified | boolean | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.security.recovery.EmailRecoveryController$RequestCode

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| accountKind | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| identifier | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| purpose | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.security.recovery.EmailRecoveryController$Reset

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| challengeId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| accountKind | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| identifier | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| purpose | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| code | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| newCredential | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| confirmCredential | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.security.recovery.EmailRecoveryController$Result

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| message | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.security.recovery.EmailRecoveryController$UpdateEmail

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| email | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.security.recovery.EmailRecoveryController$VerifyEmail

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| challengeId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| code | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.security.recovery.GroupEmailEnrollmentController$Complete

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| challengeId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| code | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| groupId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.security.recovery.GroupEmailEnrollmentController$EmailStatus

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| email | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| verified | boolean | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.security.recovery.GroupEmailEnrollmentController$Result

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| message | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.security.recovery.GroupEmailEnrollmentController$Start

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| email | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| groupId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.security.recovery.GroupEmailEnrollmentController$Started

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| challengeId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| message | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.security.recovery.GroupMobileEnrollmentController$Complete

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| challengeId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| code | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| groupId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.security.recovery.GroupMobileEnrollmentController$Result

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| message | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.security.recovery.GroupMobileEnrollmentController$Start

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| mobile | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| groupId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.security.recovery.GroupMobileEnrollmentController$Started

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| challengeId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| message | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.security.recovery.SignupEmailVerificationController$Start

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| accountKind | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| email | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| eventToken | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| groupId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.security.recovery.SignupEmailVerificationController$Verify

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| challengeId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| code | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.security.recovery.SignupEmailVerificationService$Started

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| challengeId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| message | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.security.recovery.SignupEmailVerificationService$Verified

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| verificationToken | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| message | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| expiresAt | java.time.Instant | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.security.recovery.UsernameRecoveryController$Request

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| accountKind | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| identifier | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| eventToken | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| groupId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.security.recovery.UsernameRecoveryController$Result

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| message | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.security.verification.AccountMobileVerificationController$Complete

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| challengeId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| code | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.security.verification.AccountMobileVerificationController$Start

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| mobile | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.security.verification.MobileVerificationService$Started

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| challengeId | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| message | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.security.verification.MobileVerificationService$Status

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| mobile | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| verified | boolean | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.security.verification.VerificationSettings$Settings

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| mobileOtpEnabled | boolean | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| emailVerificationEnabled | boolean | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.user.controller.ProfileNameController$NameInput

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| fullName | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.user.dto.RegisterRequest

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| fullName | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| username | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| email | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| mobile | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| password | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| emailVerificationToken | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |


## com.giftplatform.user.dto.RegistrationResponse

Kind: record.

| Field | Java/JSON type | Validation / annotations | Default / optionality |
| --- | --- | --- | --- |
| id | java.util.UUID | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| username | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| email | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
| role | java.lang.String | No field annotation | Not declared; constructor, handler, service and DB may supply a value. See source/rules. |
