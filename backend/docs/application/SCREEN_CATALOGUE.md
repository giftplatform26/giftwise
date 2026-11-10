# Complete screen and wireframe catalogue

All 34 App Router `page.tsx` routes are included. Route variables are placeholders, not example authorization. Composed controls include shared layout imports, re-exports and local dynamic imports; visibility expressions determine what actually renders. This is a source-backed wireframe/control reference, not a claim that every shared component appears simultaneously.

Detailed field/button specifications are in [UI_CONTROLS.md](UI_CONTROLS.md). The source metadata in [frontend.json](catalogue/frontend.json) also records complete attributes, conditions, state expressions, request arguments and dynamic collections.

| Route | Screen | Access |
| --- | --- | --- |
| / | GiftWise public home | Public |
| /admin | Administration console | Authenticated PLATFORM_ADMIN |
| /admin/invitation-assets | Invitation asset gallery administration | Authenticated PLATFORM_ADMIN |
| /contact | Contact support | Public |
| /dashboard | Host dashboard | Authenticated CUSTOMER; admin has its own console |
| /demo | Sample invitation | Public; PUBLIC_DEMO growth flag |
| /e/[token] | Public guest invitation | Valid published token; per-feature/channel/readiness checks |
| /events | Celebrations | Authenticated owner for event reads/writes |
| /events/[id] | Celebration overview | Authenticated event owner |
| /events/[id]/album | Private memory album | Authenticated owner; platform + host + event MEMORY_ALBUM grants |
| /events/[id]/celebration | Guests, responses and wishes | Authenticated event owner; RSVP/GUESTBOOK grants |
| /events/[id]/contributions | Contribution report | Authenticated event owner |
| /events/[id]/notifications | Activity and notifications | Authenticated event owner |
| /events/[id]/planning | Celebration settings | Authenticated owner; CELEBRATION_PLANNING / EVENT_REUSE at platform, host and event |
| /events/[id]/preview | Private invitation preview | Authenticated event owner; not a public guest token |
| /events/[id]/qr | Invitation design and QR | Authenticated event owner; asset/commercial/AI gates where applicable |
| /events/[id]/receiving-accounts | Event receiving accounts | Authenticated event owner; payment profile feature gate |
| /events/[id]/thank-you | Thank-you card studio | Authenticated event owner |
| /events/[id]/transactions | Payment and transaction verification | Authenticated event owner; implemented verification controls |
| /events/new | Create celebration | Authenticated CUSTOMER |
| /forgot-password | Password/PIN recovery | Public scoped challenge flow; policy/provider gates |
| /forgot-username | Username recovery | Public scoped challenge flow; policy/provider gates |
| /gifts | Gift wishlist | Authenticated owner; eventId query scopes the selected event |
| /groups/[token]/[groupId] | Group invitation and member workspace | Published invitation/group readiness plus allowed guest/member access |
| /host/account | Host account | Authenticated account owner; verification/policy gates |
| /host/monetization | Plans and partnerships | Authenticated purchaser/placement owner; commercial stream gates |
| /host/payment-settings | Host receiving accounts | Authenticated CUSTOMER; payment profile feature gate |
| /login | Login | Public; authenticated result role determines destination |
| /memories/[token] | Shared approved-wishes keepsake | Published/completed event, global switch, owner consent and host Guestbook grant |
| /privacy | Privacy policy | Public |
| /register | Host registration | Public subject to registration/contact policies |
| /studio | Invitation studio | Client-rendered design workspace; authenticated owner required for protected QR/design/AI APIs |
| /terms | Terms and conditions | Public |
| /verify-email | Email verification | Scoped verification token/proof and applicable account context |


## GiftWise public home

| Screen information | Source-supported specification |
| --- | --- |
| Route | / |
| Source | [web/app/page.tsx:1](../../../web/app/page.tsx#L1) |
| Purpose | Explain invitation creation and lead visitors to registration or an enabled fictional demo |
| User access level | Public |
| Navigation source | Home logo / direct URL / search |
| Navigation destination | Registration, login, sample invitation or authenticated studio |
| Preconditions | Route parameters/query scope must be valid; applicable session/ownership/module/provider state above. Load errors do not imply an empty successful dataset. |
| Postconditions | Only the selected successful action changes its documented state. Navigation alone does not publish, confirm payment, verify contact or deliver a notification. |
| Route variables | No path variable |
| UI variants | Focused/general experience, responsive layout and module/state predicates can change visible sections. Exact predicates are in each control record. |


### Wireframe layout

| Section | Placement / behavior / evidence |
| --- | --- |
| Header | Shared identity/event or page title/back/actions, where declared. Composite header declarations: 2. |
| Navigation / sidebar | Host/admin navigation and event context where applicable; mobile/focused variants are conditional. nav=4, aside=3. |
| Main content | Purpose-specific forms/cards/tables/canvas/player from the composed sources. main=3, section=5, article=1. |
| Search and filters | Fields/selects listed below; exact value/handler/visibility controls are not inferred from placeholder labels. |
| Data grids and cards | Table declarations=0. Data collections and generated rows/cards are in frontend.json dynamicSources. |
| Popups / dialogs | Named/source-declared dialog control records below; opening/closing/loading/apply/cancel actions belong to those component handlers. |
| Alerts / notifications | Source controls with role alert/status are listed below. Notice strings may come from API results; do not assume a universal envelope. |
| Footer | Local/shared footer declarations=1. No undocumented legal/footer wording is invented. |
| Loading / empty / success / error | Observed state bindings and relevant action fields are listed below. Expected QA states from Document 2 do not establish a spinner/retry control where source does not implement one. |


### Composed implementation

- [web/app/page.tsx:1](../../../web/app/page.tsx#L1)
- [web/components/PublicLanding.tsx:1](../../../web/components/PublicLanding.tsx#L1)
- [web/components/GrowthProvider.tsx:1](../../../web/components/GrowthProvider.tsx#L1)
- [web/components/BrandingProvider.tsx:1](../../../web/components/BrandingProvider.tsx#L1)
- [web/components/LocalizationProvider.tsx:1](../../../web/components/LocalizationProvider.tsx#L1)
- [web/app/layout.tsx:1](../../../web/app/layout.tsx#L1)
- [web/components/LegalFooter.tsx:1](../../../web/components/LegalFooter.tsx#L1)
- [web/components/CookieNotice.tsx:1](../../../web/components/CookieNotice.tsx#L1)
- [web/app/components/ApplicationInteractions.tsx:1](../../../web/app/components/ApplicationInteractions.tsx#L1)
- [web/components/VerificationProvider.tsx:1](../../../web/components/VerificationProvider.tsx#L1)
- [web/components/ExperienceProvider.tsx:1](../../../web/components/ExperienceProvider.tsx#L1)
- [web/app/components/PerformanceDiagnostics.tsx:1](../../../web/app/components/PerformanceDiagnostics.tsx#L1)
- [web/app/components/HostSessionRedirect.tsx:1](../../../web/app/components/HostSessionRedirect.tsx#L1)
- [web/app/components/HostGlobalNav.tsx:1](../../../web/app/components/HostGlobalNav.tsx#L1)
- [web/components/HostWorkspace.tsx:1](../../../web/components/HostWorkspace.tsx#L1)
- [web/components/focused/HostShell.tsx:1](../../../web/components/focused/HostShell.tsx#L1)

### Fields

| Control ID | Label / expression | Type | Binding | Required | Native limits / format | Detailed specification |
| --- | --- | --- | --- | --- | --- | --- |
| UI-00908 | {localize("ui.bba5b40bdd28c66e",{defaultValue:"Choose language"})} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00908) |
| UI-00910 | {label} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00910) |
| UI-00912 | {localize("ui.140071feea59fe87",{defaultValue:"Preferred language"})} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00912) |


### Buttons

| ID | Label / expression | Action | Submit validation / handler | Source |
| --- | --- | --- | --- | --- |
| UI-00528 | Accept optional analytics | {()=>{saveConsent(true);setShow(false)}} | Not a native form submit or inherited from component context | [web/components/CookieNotice.tsx:5](../../../web/components/CookieNotice.tsx#L5) |
| UI-00529 | Reject optional analytics | {()=>{saveConsent(false);setShow(false)}} | Not a native form submit or inherited from component context | [web/components/CookieNotice.tsx:5](../../../web/components/CookieNotice.tsx#L5) |
| UI-00788 | {collapsed?<PanelLeftOpen size={20}/>:<PanelLeftClose size={20}/>} | {toggle} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:50](../../../web/components/HostWorkspace.tsx#L50) |
| UI-00791 |  | {()=>void signOut()} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:53](../../../web/components/HostWorkspace.tsx#L53) |
| UI-00792 |  | {()=>setCollapsed(true)} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:54](../../../web/components/HostWorkspace.tsx#L54) |
| UI-00876 | Cookie preferences | {()=>window.dispatchEvent(new Event('giftwise-cookie-settings'))} | Not a native form submit or inherited from component context | [web/components/LegalFooter.tsx:4](../../../web/components/LegalFooter.tsx#L4) |


### Dialog and alert/status inventory

| ID | Role / kind | Text / label | Visibility conditions | Source |
| --- | --- | --- | --- | --- |
| UI-00072 | "status" | {localize("ui.bed9096c411e6da3",{defaultValue:"Opening your workspace…"})} | Parent/source state | [web/app/components/HostGlobalNav.tsx:17](../../../web/app/components/HostGlobalNav.tsx#L17) |
| UI-00548 | "status" | {localize("ui.7a005f0f1dbc704f",{defaultValue:"Opening your celebration space…"})} | ready\|\|discovery | [web/components/ExperienceProvider.tsx:19](../../../web/components/ExperienceProvider.tsx#L19) |
| UI-00790 | "alert" | {localizeText(error)} | error | [web/components/HostWorkspace.tsx:53](../../../web/components/HostWorkspace.tsx#L53) |
| UI-00909 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:19](../../../web/components/LocalizationProvider.tsx#L19) |
| UI-00911 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:20](../../../web/components/LocalizationProvider.tsx#L20) |
| UI-00913 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:21](../../../web/components/LocalizationProvider.tsx#L21) |


### Workflow state bindings

| Source | Binding | Initial expression |
| --- | --- | --- |
| web/components/LocalizationProvider.tsx | busy | false |
| web/components/LocalizationProvider.tsx | error | '' |
| web/components/HostWorkspace.tsx | busy | false |
| web/components/HostWorkspace.tsx | error | '' |


## Administration console

| Screen information | Source-supported specification |
| --- | --- |
| Route | /admin |
| Source | [web/app/admin/page.tsx:1](../../../web/app/admin/page.tsx#L1) |
| Purpose | Dashboard; hosts/modules; event album/AI switches/details; reports; payment/commercial/policy/experience/audit tabs |
| User access level | Authenticated PLATFORM_ADMIN |
| Navigation source | Admin login / privileged navigation |
| Navigation destination | Audited admin changes, read-only details/history; gallery link; sign-out |
| Preconditions | Route parameters/query scope must be valid; applicable session/ownership/module/provider state above. Load errors do not imply an empty successful dataset. |
| Postconditions | Only the selected successful action changes its documented state. Navigation alone does not publish, confirm payment, verify contact or deliver a notification. |
| Route variables | No path variable |
| UI variants | Focused/general experience, responsive layout and module/state predicates can change visible sections. Exact predicates are in each control record. |


### Wireframe layout

| Section | Placement / behavior / evidence |
| --- | --- |
| Header | Shared identity/event or page title/back/actions, where declared. Composite header declarations: 3. |
| Navigation / sidebar | Host/admin navigation and event context where applicable; mobile/focused variants are conditional. nav=7, aside=4. |
| Main content | Purpose-specific forms/cards/tables/canvas/player from the composed sources. main=3, section=46, article=6. |
| Search and filters | Fields/selects listed below; exact value/handler/visibility controls are not inferred from placeholder labels. |
| Data grids and cards | Table declarations=9. Data collections and generated rows/cards are in frontend.json dynamicSources. |
| Popups / dialogs | Named/source-declared dialog control records below; opening/closing/loading/apply/cancel actions belong to those component handlers. |
| Alerts / notifications | Source controls with role alert/status are listed below. Notice strings may come from API results; do not assume a universal envelope. |
| Footer | Local/shared footer declarations=2. No undocumented legal/footer wording is invented. |
| Loading / empty / success / error | Observed state bindings and relevant action fields are listed below. Expected QA states from Document 2 do not establish a spinner/retry control where source does not implement one. |


### Composed implementation

- [web/app/admin/page.tsx:1](../../../web/app/admin/page.tsx#L1)
- [web/components/GrowthControls.tsx:1](../../../web/components/GrowthControls.tsx#L1)
- [web/components/GrowthProvider.tsx:1](../../../web/components/GrowthProvider.tsx#L1)
- [web/components/LocalizationProvider.tsx:1](../../../web/components/LocalizationProvider.tsx#L1)
- [web/components/GrowthFunnel.tsx:1](../../../web/components/GrowthFunnel.tsx#L1)
- [web/components/ProfilePreferences.tsx:1](../../../web/components/ProfilePreferences.tsx#L1)
- [web/components/BusinessAudit.tsx:1](../../../web/components/BusinessAudit.tsx#L1)
- [web/components/EmailDeliveryStatus.tsx:1](../../../web/components/EmailDeliveryStatus.tsx#L1)
- [web/components/BrandingProvider.tsx:1](../../../web/components/BrandingProvider.tsx#L1)
- [web/components/BrandingAdmin.tsx:1](../../../web/components/BrandingAdmin.tsx#L1)
- [web/components/LocalizationAdmin.tsx:1](../../../web/components/LocalizationAdmin.tsx#L1)
- [web/components/TranslationSupportTools.tsx:1](../../../web/components/TranslationSupportTools.tsx#L1)
- [web/components/ProductionReadiness.tsx:1](../../../web/components/ProductionReadiness.tsx#L1)
- [web/components/AdminEventModules.tsx:1](../../../web/components/AdminEventModules.tsx#L1)
- [web/components/AdminAiDesignDetails.tsx:1](../../../web/components/AdminAiDesignDetails.tsx#L1)
- [web/components/focused/ExperienceControl.tsx:1](../../../web/components/focused/ExperienceControl.tsx#L1)
- [web/components/ExperienceProvider.tsx:1](../../../web/components/ExperienceProvider.tsx#L1)
- [web/components/PlatformVerificationControls.tsx:1](../../../web/components/PlatformVerificationControls.tsx#L1)
- [web/components/VerificationProvider.tsx:1](../../../web/components/VerificationProvider.tsx#L1)
- [web/components/MobileVerification.tsx:1](../../../web/components/MobileVerification.tsx#L1)
- [web/components/AccountEmailVerification.tsx:1](../../../web/components/AccountEmailVerification.tsx#L1)
- [web/components/GroupMemberEmailPolicy.tsx:1](../../../web/components/GroupMemberEmailPolicy.tsx#L1)
- [web/app/admin/AccountPolicies.tsx:1](../../../web/app/admin/AccountPolicies.tsx#L1)
- [web/app/admin/AdminAudit.tsx:1](../../../web/app/admin/AdminAudit.tsx#L1)
- [web/components/HostInvitationUploads.tsx:1](../../../web/components/HostInvitationUploads.tsx#L1)
- [web/components/CommercePurchase.tsx:1](../../../web/components/CommercePurchase.tsx#L1)
- [web/app/admin/AdminInteractions.tsx:1](../../../web/app/admin/AdminInteractions.tsx#L1)
- [web/components/MonetizationControls.tsx:1](../../../web/components/MonetizationControls.tsx#L1)
- [web/components/CommercialAdminControls.tsx:1](../../../web/components/CommercialAdminControls.tsx#L1)
- [web/components/PaymentControls.tsx:1](../../../web/components/PaymentControls.tsx#L1)
- [web/components/RoutingCommissionControl.tsx:1](../../../web/components/RoutingCommissionControl.tsx#L1)
- [web/app/layout.tsx:1](../../../web/app/layout.tsx#L1)
- [web/components/LegalFooter.tsx:1](../../../web/components/LegalFooter.tsx#L1)
- [web/components/CookieNotice.tsx:1](../../../web/components/CookieNotice.tsx#L1)
- [web/app/components/ApplicationInteractions.tsx:1](../../../web/app/components/ApplicationInteractions.tsx#L1)
- [web/app/components/PerformanceDiagnostics.tsx:1](../../../web/app/components/PerformanceDiagnostics.tsx#L1)
- [web/app/components/HostSessionRedirect.tsx:1](../../../web/app/components/HostSessionRedirect.tsx#L1)
- [web/app/components/HostGlobalNav.tsx:1](../../../web/app/components/HostGlobalNav.tsx#L1)
- [web/components/HostWorkspace.tsx:1](../../../web/components/HostWorkspace.tsx#L1)
- [web/components/focused/HostShell.tsx:1](../../../web/components/focused/HostShell.tsx#L1)

### Fields

| Control ID | Label / expression | Type | Binding | Required | Native limits / format | Detailed specification |
| --- | --- | --- | --- | --- | --- | --- |
| UI-00001 | {localize("ui.1464bf3de3861a63",{defaultValue:"Policy level"})} | select  | {scope} | Not declared |  | [control](UI_CONTROLS.md#ui-00001) |
| UI-00002 | {localize("ui.26e576ad82c95d26",{defaultValue:"Policy host"})} | select  | {host} | Not declared |  | [control](UI_CONTROLS.md#ui-00002) |
| UI-00003 | {localize("ui.d26cf42d38c394cc",{defaultValue:"Policy event"})} | select  | {event} | Not declared |  | [control](UI_CONTROLS.md#ui-00003) |
| UI-00004 |  | input "checkbox" | {!!value?.[f.key]} | Not declared |  | [control](UI_CONTROLS.md#ui-00004) |
| UI-00009 | {localize("ui.a668022aed26067d",{defaultValue:"Keyword"})} | input "search" | {draft.search} | Not declared | maxLength={200} | [control](UI_CONTROLS.md#ui-00009) |
| UI-00010 | {localize("ui.c4d977eaddeb2c98",{defaultValue:"From date (UTC)"})} | input "date" | {draft.from} | Not declared | max={draft.to\|\|'9999-12-31'} | [control](UI_CONTROLS.md#ui-00010) |
| UI-00011 | {localize("ui.ffac76ee5d53171c",{defaultValue:"To date (UTC)"})} | input "date" | {draft.to} | Not declared | min={draft.from\|\|undefined}; max="9999-12-31" | [control](UI_CONTROLS.md#ui-00011) |
| UI-00012 | {localize("ui.64cff1319d2fd2cb",{defaultValue:"Action"})} | select  | {draft.action} | Not declared |  | [control](UI_CONTROLS.md#ui-00012) |
| UI-00013 | {localize("ui.f148224ae44fb843",{defaultValue:"Module"})} | select  | {draft.module} | Not declared |  | [control](UI_CONTROLS.md#ui-00013) |
| UI-00024 | {localizeText('Host')} | select  | {draft.hostId} | Not declared |  | [control](UI_CONTROLS.md#ui-00024) |
| UI-00025 | {localizeText('Event')} | select  | {draft.eventId} | Not declared |  | [control](UI_CONTROLS.md#ui-00025) |
| UI-00026 | {localizeText(key==='from'?'From date (UTC)':'To date (UTC)')} | input "date" | {draft[key]} | Not declared |  | [control](UI_CONTROLS.md#ui-00026) |
| UI-00027 | {localizeText('Action')} | input  | {draft.action} | Not declared | maxLength={64}; pattern="[A-Z0-9_]*" | [control](UI_CONTROLS.md#ui-00027) |
| UI-00028 | {localizeText('Outcome')} | select  | {draft.outcome} | Not declared |  | [control](UI_CONTROLS.md#ui-00028) |
| UI-00029 | {localizeText('Record type')} | select  | {draft.kind} | Not declared |  | [control](UI_CONTROLS.md#ui-00029) |
| UI-00066 |  | input "checkbox" | {m.enabled} | Not declared |  | [control](UI_CONTROLS.md#ui-00066) |
| UI-00067 | {localize("ui.542e34e1dd6aaae9",{defaultValue:"Search hosts"})} | input  | {search} | Not declared |  | [control](UI_CONTROLS.md#ui-00067) |
| UI-00068 | {localize("ui.f05e43a498a59f81",{defaultValue:"Select host"})} | select  | {selected} | Not declared |  | [control](UI_CONTROLS.md#ui-00068) |
| UI-00069 |  | input "checkbox" | {m.host_enabled} | Not declared |  | [control](UI_CONTROLS.md#ui-00069) |
| UI-00070 | {localize("ui.e6fa666a4ae99e06",{defaultValue:"Enable memory album for {{value0}}",value0:(e.title)})} | input "checkbox" | {e.album_enabled} | Not declared |  | [control](UI_CONTROLS.md#ui-00070) |
| UI-00071 | {localize("ui.a1a8ac106eb2578c",{defaultValue:"Enable Design with AI for {{value0}}",value0:(e.title)})} | input "checkbox" | {e.ai_enabled} | Not declared |  | [control](UI_CONTROLS.md#ui-00071) |
| UI-00419 | {localize("ui.f2488fd4ef4adbc6",{defaultValue:"Email address"})} | input "email" | {draftEmail} | true | maxLength={254} | [control](UI_CONTROLS.md#ui-00419) |
| UI-00424 | {localize("ui.3bcb66ee1a6feaf5",{defaultValue:"Six-digit code"})} | input  | {code} | true | maxLength={6}; pattern="[0-9]{6}" | [control](UI_CONTROLS.md#ui-00424) |
| UI-00435 | {names[g.module]} | input "checkbox" | {g.eventEnabled} | Not declared |  | [control](UI_CONTROLS.md#ui-00435) |
| UI-00461 | Application name | input  | {form.applicationName} | true | maxLength={80} | [control](UI_CONTROLS.md#ui-00461) |
| UI-00462 | Brand color | input "color" | {form.brandColor} | Not declared |  | [control](UI_CONTROLS.md#ui-00462) |
| UI-00463 | Footer text | input  | {form.footerText} | true | maxLength={240} | [control](UI_CONTROLS.md#ui-00463) |
| UI-00464 | Support email | input "email" | {form.supportEmail} | Not declared | maxLength={254} | [control](UI_CONTROLS.md#ui-00464) |
| UI-00465 | Social links (one HTTPS URL per line) | textarea  | {form.socialLinks} | Not declared | maxLength={2000} | [control](UI_CONTROLS.md#ui-00465) |
| UI-00468 | Upload {kind} | input "file" |  | Not declared |  | [control](UI_CONTROLS.md#ui-00468) |
| UI-00472 | Record type | select  | {kind} | Not declared |  | [control](UI_CONTROLS.md#ui-00472) |
| UI-00504 | {localizeText(label)} | input "number" | {policy[key]} | true | min={key==='free_active_events'?0:1}; max={key==='free_active_events'?1000:3660} | [control](UI_CONTROLS.md#ui-00504) |
| UI-00505 | {localize("ui.0e41495ce9f8d108",{defaultValue:"Change reason"})} | input  | {reason} | true | maxLength={500} | [control](UI_CONTROLS.md#ui-00505) |
| UI-00508 | {k} | input  | {partner[k]} | {!['clickParameter','redirectTemplate'].includes(k)} | maxLength={k==='redirectTemplate'?2048:k==='productDomain'?253:k==='name'?100:k.endsWith('Name')\|\|k==='clickParameter'?60:500} | [control](UI_CONTROLS.md#ui-00508) |
| UI-00509 | {localize("ui.660e3d474f262ac8",{defaultValue:" Enable partner attribution"})} | input "checkbox" | {partner.enabled} | Not declared |  | [control](UI_CONTROLS.md#ui-00509) |
| UI-00514 | {localize("ui.29035e784f9305c0",{defaultValue:"Partner"})} | select  | {statement.partnerId} | true |  | [control](UI_CONTROLS.md#ui-00514) |
| UI-00515 | {k} | input {k==='amountRupees'?'number':'text'} | {statement[k]} | {k!=='clickId'} | min={k==='amountRupees'?'0.01':undefined}; step={k==='amountRupees'?'0.01':undefined} | [control](UI_CONTROLS.md#ui-00515) |
| UI-00516 | {localize("ui.ee2584ee46b7b175",{defaultValue:"Statement status"})} | select  | {statement.state} | Not declared |  | [control](UI_CONTROLS.md#ui-00516) |
| UI-00518 | {localize("ui.d08d85e5e9b18410",{defaultValue:"Reason for updating {{value0}}",value0:(c.external_reference)})} | input  | {c.changeReason\|\|''} | Not declared |  | [control](UI_CONTROLS.md#ui-00518) |
| UI-00522 | {localize("ui.046bf9bffb47f29a",{defaultValue:"Review reason"})} | input  | {p.reviewReason\|\|''} | Not declared |  | [control](UI_CONTROLS.md#ui-00522) |
| UI-00673 | {localize("ui.7398a3f1d9ea36be",{defaultValue:" Require group members to verify email"})} | input "checkbox" | {!!(hostId?policy?.admin_enabled:policy?.host_enabled)} | Not declared |  | [control](UI_CONTROLS.md#ui-00673) |
| UI-00679 |  | input "checkbox" | {value.features[key]===true} | Not declared |  | [control](UI_CONTROLS.md#ui-00679) |
| UI-00776 | {localizeText(busy?'Preparing your artwork…':category==='DECORATION'?'Upload a sticker':'Upload a background')} | input "file" |  | Not declared |  | [control](UI_CONTROLS.md#ui-00776) |
| UI-00779 | {localizeText('Enable custom uploads for this host')} | input "checkbox" | {access.enabled} | Not declared |  | [control](UI_CONTROLS.md#ui-00779) |
| UI-00780 | {localizeText('Require a paid premium host plan')} | input "checkbox" | {access.requiresPayment} | Not declared |  | [control](UI_CONTROLS.md#ui-00780) |
| UI-00879 | {localize("ui.6fbd766b1a71cc4d",{defaultValue:"Translation"})} | textarea  | {text} | Not declared | maxLength={10000} | [control](UI_CONTROLS.md#ui-00879) |
| UI-00880 | {localize("ui.4fd940ed99030e6f",{defaultValue:" Publish this translation"})} | input "checkbox" | {published} | Not declared |  | [control](UI_CONTROLS.md#ui-00880) |
| UI-00883 | {localize("ui.0d52584f25b560bd",{defaultValue:"Multi-language support"})} | select  | {state} | Not declared |  | [control](UI_CONTROLS.md#ui-00883) |
| UI-00889 | {localize("ui.b55e6b4b4d3acac3",{defaultValue:" Enable multi-language support"})} | input "checkbox" | {settings.enabled} | Not declared |  | [control](UI_CONTROLS.md#ui-00889) |
| UI-00890 | {localize("ui.690531fdd1f79cd5",{defaultValue:"Platform default language"})} | select  | {settings.defaultLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00890) |
| UI-00891 | {localize("ui.94f9884df6b6b670",{defaultValue:"Additional fallback language"})} | select  | {settings.fallbackLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00891) |
| UI-00893 | {localize("ui.3d139f14a41cfbd4",{defaultValue:" Enabled"})} | input "checkbox" | {l.enabled} | Not declared |  | [control](UI_CONTROLS.md#ui-00893) |
| UI-00895 | {localize("ui.26dec0a3e684adcf",{defaultValue:"Language code"})} | input  | {code} | true | maxLength={63} | [control](UI_CONTROLS.md#ui-00895) |
| UI-00896 | {localize("ui.6fabde2e45d95823",{defaultValue:"Native name"})} | input  | {nativeName} | true | maxLength={120} | [control](UI_CONTROLS.md#ui-00896) |
| UI-00897 | {localize("ui.00799d7eb4caed17",{defaultValue:"English name"})} | input  | {englishName} | true | maxLength={120} | [control](UI_CONTROLS.md#ui-00897) |
| UI-00898 | {localize("ui.0935fd222194f211",{defaultValue:"Writing direction"})} | select  | {direction} | Not declared |  | [control](UI_CONTROLS.md#ui-00898) |
| UI-00900 | {localize("ui.f83edd8c38bdfbf5",{defaultValue:"Translate into"})} | select  | {selected} | Not declared |  | [control](UI_CONTROLS.md#ui-00900) |
| UI-00901 | {localize("ui.ddc9130e5c8b2d89",{defaultValue:"Find source text"})} | input  | {search} | Not declared | maxLength={200} | [control](UI_CONTROLS.md#ui-00901) |
| UI-00903 | {localize("ui.76b5d6a11ef90de0",{defaultValue:"Import translation JSON · up to 2 MB"})} | input "file" |  | Not declared |  | [control](UI_CONTROLS.md#ui-00903) |
| UI-00904 | {localize("ui.b59e69e61f4b0df9",{defaultValue:" Publish this reviewed translation file"})} | input "checkbox" | {publishImport} | Not declared |  | [control](UI_CONTROLS.md#ui-00904) |
| UI-00908 | {localize("ui.bba5b40bdd28c66e",{defaultValue:"Choose language"})} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00908) |
| UI-00910 | {label} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00910) |
| UI-00912 | {localize("ui.140071feea59fe87",{defaultValue:"Preferred language"})} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00912) |
| UI-00931 | {localize("ui.dae5cf30549f072d",{defaultValue:"Mobile number with country code"})} | input "tel" | {mobile} | true | pattern="\+[1-9][0-9]{7,14}" | [control](UI_CONTROLS.md#ui-00931) |
| UI-00932 | {localize("ui.f673a246d45f4b8f",{defaultValue:"Six-digit verification code"})} | input  | {code} | true | maxLength={6}; pattern="[0-9]{6}" | [control](UI_CONTROLS.md#ui-00932) |
| UI-00940 | {localize("ui.5f84101cbe45fdb0",{defaultValue:" Enable platform monetization"})} | input "checkbox" | {settings.enabled} | Not declared |  | [control](UI_CONTROLS.md#ui-00940) |
| UI-00941 | {localize("ui.e04946fb199a7d03",{defaultValue:" Enable this stream"})} | input "checkbox" | {s.enabled} | Not declared |  | [control](UI_CONTROLS.md#ui-00941) |
| UI-00942 | {localize("ui.f780de85654a8231",{defaultValue:"Pricing method"})} | select  | {s.feeMode} | Not declared |  | [control](UI_CONTROLS.md#ui-00942) |
| UI-00943 | {localize("ui.67ea4edbd08c50c0",{defaultValue:"Fixed price (₹)"})} | input "number" | {s.fixedPaise/100} | true | min="0"; max="1000000"; step="0.01" | [control](UI_CONTROLS.md#ui-00943) |
| UI-00944 | {localize("ui.91d63bddb5736df5",{defaultValue:"Percentage (%)"})} | input "number" | {s.rateBps/100} | true | min="0"; max="100"; step="0.01" | [control](UI_CONTROLS.md#ui-00944) |
| UI-00945 | {localize("ui.736281107355bec5",{defaultValue:"Maximum fee (₹ · 0 means no cap)"})} | input "number" | {s.capPaise/100} | true | min="0"; max="1000000"; step="0.01" | [control](UI_CONTROLS.md#ui-00945) |
| UI-00946 | {localize("ui.24cb06ed97a92d70",{defaultValue:"Who pays the fee?"})} | select  | {s.feeBearer} | Not declared |  | [control](UI_CONTROLS.md#ui-00946) |
| UI-00947 | {localize("ui.615dff528dbac347",{defaultValue:"Reason for change"})} | textarea  | {reason} | true | maxLength={500} | [control](UI_CONTROLS.md#ui-00947) |
| UI-00952 | {localize("ui.b073f6c68ef87211",{defaultValue:"Scope"})} | select  | {scope} | Not declared |  | [control](UI_CONTROLS.md#ui-00952) |
| UI-00953 | {scope==='HOST'?localize("ui.4a823118b9ba8baa",{defaultValue:"Host"}):localize("ui.4e1f49a9c8ae8a15",{defaultValue:"Event"})} | select  | {target} | Not declared |  | [control](UI_CONTROLS.md#ui-00953) |
| UI-00954 | {localize("ui.615dff528dbac347",{defaultValue:"Reason for change"})} | input  | {reason} | true | maxLength={500} | [control](UI_CONTROLS.md#ui-00954) |
| UI-00955 |  | select  | {c.configured===null?'INHERIT':c.configured?'ON':'OFF'} | Not declared |  | [control](UI_CONTROLS.md#ui-00955) |
| UI-00960 | {localize("ui.edcef097bea311f1",{defaultValue:" Enable mobile OTP across the application"})} | input "checkbox" | {value?.settings.mobileOtpEnabled\|\|false} | Not declared |  | [control](UI_CONTROLS.md#ui-00960) |
| UI-00961 | {localize("ui.b1b7108b10527df6",{defaultValue:" Bypass email verification across the application"})} | input "checkbox" | {!value?.settings.emailVerificationEnabled} | Not declared |  | [control](UI_CONTROLS.md#ui-00961) |
| UI-00969 | Upload profile picture | input "file" |  | Not declared |  | [control](UI_CONTROLS.md#ui-00969) |
| UI-00973 | Time zone | select  | {form.timezone} | Not declared |  | [control](UI_CONTROLS.md#ui-00973) |
| UI-00974 | {label} | input "checkbox" | {form[key]} | Not declared |  | [control](UI_CONTROLS.md#ui-00974) |
| UI-00990 | {localize("ui.5454edcd2bd30162",{defaultValue:"Platform routing commission (%)"})} | input "number" | {percentage} | true | min="0"; max="100"; step="0.01" | [control](UI_CONTROLS.md#ui-00990) |
| UI-00991 | {localize("ui.615dff528dbac347",{defaultValue:"Reason for change"})} | textarea  | {reason} | true | maxLength={500} | [control](UI_CONTROLS.md#ui-00991) |
| UI-01048 | {localize("ui.014d2e3bf4c99118",{defaultValue:"Approved MSG91 template ID"})} | input  | {template} | true | maxLength={120}; pattern="[A-Za-z0-9_-]+" | [control](UI_CONTROLS.md#ui-01048) |
| UI-01051 | {localize("ui.0a7fb024beadb185",{defaultValue:"Original interface text"})} | textarea  | {text} | true | maxLength={10000} | [control](UI_CONTROLS.md#ui-01051) |
| UI-01104 |  | input "radio" | {value} | Not declared |  | [control](UI_CONTROLS.md#ui-01104) |


### Buttons

| ID | Label / expression | Action | Submit validation / handler | Source |
| --- | --- | --- | --- | --- |
| UI-00007 | {busy?localize("ui.23e39291d6135814",{defaultValue:"Saving…"}):localize("ui.57ee14ce1425ca9b",{defaultValue:"Save policy"})} | {()=>void save()} | Not a native form submit or inherited from component context | [web/app/admin/AccountPolicies.tsx:56](../../../web/app/admin/AccountPolicies.tsx#L56) |
| UI-00014 | {localize("ui.d80ab19b7edb8272",{defaultValue:"Apply filters"})} | submit / component action | {apply} | [web/app/admin/AdminAudit.tsx:27](../../../web/app/admin/AdminAudit.tsx#L27) |
| UI-00015 | {localize("ui.7179ea0035fca783",{defaultValue:"Clear filters"})} | {()=>{setDraft(empty);setFilters(empty);setPage(0)}} | {apply} | [web/app/admin/AdminAudit.tsx:27](../../../web/app/admin/AdminAudit.tsx#L27) |
| UI-00017 | {localize("ui.942087cc2d41e013",{defaultValue:"Retry"})} | {()=>setReload(value=>value+1)} | Not a native form submit or inherited from component context | [web/app/admin/AdminAudit.tsx:27](../../../web/app/admin/AdminAudit.tsx#L27) |
| UI-00020 | {localize("ui.a57b08a480b822a0",{defaultValue:"Previous"})} | {()=>setPage(value=>Math.max(0,value-1))} | Not a native form submit or inherited from component context | [web/app/admin/AdminAudit.tsx:27](../../../web/app/admin/AdminAudit.tsx#L27) |
| UI-00021 | {localize("ui.1ff57a29d7c9d11b",{defaultValue:"Next"})} | {()=>setPage(value=>value+1)} | Not a native form submit or inherited from component context | [web/app/admin/AdminAudit.tsx:27](../../../web/app/admin/AdminAudit.tsx#L27) |
| UI-00022 | {r[type+'_name']\|\|localizeText('Unattributed / application-wide')} | {()=>{const next={...filters,[type+'Id']:r[type+'_id']};setDraft(next);setFilters(next);setPage(0)}} | Not a native form submit or inherited from component context | [web/app/admin/AdminInteractions.tsx:17](../../../web/app/admin/AdminInteractions.tsx#L17) |
| UI-00030 | {localizeText('Apply filters')} | submit / component action | {e=>{e.preventDefault();setFilters({...draft});setPage(0)}} | [web/app/admin/AdminInteractions.tsx:18](../../../web/app/admin/AdminInteractions.tsx#L18) |
| UI-00031 | {localizeText('Clear filters')} | {()=>{setDraft(initial);setFilters(initial);setPage(0)}} | {e=>{e.preventDefault();setFilters({...draft});setPage(0)}} | [web/app/admin/AdminInteractions.tsx:18](../../../web/app/admin/AdminInteractions.tsx#L18) |
| UI-00032 | {localizeText('Refresh')} | {()=>setRevision(x=>x+1)} | {e=>{e.preventDefault();setFilters({...draft});setPage(0)}} | [web/app/admin/AdminInteractions.tsx:18](../../../web/app/admin/AdminInteractions.tsx#L18) |
| UI-00035 | {name(r.action)} | {()=>{setDraft({...filters,action:r.action});setFilters({...filters,action:r.action});setPage(0)}} | Not a native form submit or inherited from component context | [web/app/admin/AdminInteractions.tsx:18](../../../web/app/admin/AdminInteractions.tsx#L18) |
| UI-00036 | {localizeText('What happened?')} | {()=>void inspect(r)} | Not a native form submit or inherited from component context | [web/app/admin/AdminInteractions.tsx:18](../../../web/app/admin/AdminInteractions.tsx#L18) |
| UI-00037 | {localizeText('Previous')} | {()=>setPage(data.page-1)} | Not a native form submit or inherited from component context | [web/app/admin/AdminInteractions.tsx:18](../../../web/app/admin/AdminInteractions.tsx#L18) |
| UI-00038 | {localizeText('Next')} | {()=>setPage(data.page+1)} | Not a native form submit or inherited from component context | [web/app/admin/AdminInteractions.tsx:18](../../../web/app/admin/AdminInteractions.tsx#L18) |
| UI-00057 | {localizeText(label)} | {()=>setTab(id)} | Not a native form submit or inherited from component context | [web/app/admin/page.tsx:87](../../../web/app/admin/page.tsx#L87) |
| UI-00059 | {localize("ui.0e91610117029a62",{defaultValue:"Refresh"})} | {()=>void refresh()} | Not a native form submit or inherited from component context | [web/app/admin/page.tsx:87](../../../web/app/admin/page.tsx#L87) |
| UI-00060 | {localize("ui.48f0d3d397d49f13",{defaultValue:"Sign out"})} | {()=>void signout()} | Not a native form submit or inherited from component context | [web/app/admin/page.tsx:87](../../../web/app/admin/page.tsx#L87) |
| UI-00063 | {localize("ui.cb0873cd79d0ca91",{defaultValue:"Manage privileges →"})} | {()=>setTab('hosts')} | Not a native form submit or inherited from component context | [web/app/admin/page.tsx:88](../../../web/app/admin/page.tsx#L88) |
| UI-00064 | {localize("ui.553f394af6d91173",{defaultValue:"View reports →"})} | {()=>setTab('reports')} | Not a native form submit or inherited from component context | [web/app/admin/page.tsx:88](../../../web/app/admin/page.tsx#L88) |
| UI-00420 | {busy?localize("ui.23e39291d6135814",{defaultValue:"Saving…"}):localize("ui.99583e152ccf6a58",{defaultValue:"Save email"})} | submit / component action | {updateEmail} | [web/components/AccountEmailVerification.tsx:19](../../../web/components/AccountEmailVerification.tsx#L19) |
| UI-00421 | {localize("ui.19766ed6ccb2f4a3",{defaultValue:"Cancel"})} | {()=>setEditing(false)} | {updateEmail} | [web/components/AccountEmailVerification.tsx:19](../../../web/components/AccountEmailVerification.tsx#L19) |
| UI-00422 | {localize("ui.c81c8805cb1148e6",{defaultValue:"Update email address"})} | {()=>{setDraftEmail(email);setEditing(true);setError('');setMessage('')}} | Not a native form submit or inherited from component context | [web/components/AccountEmailVerification.tsx:19](../../../web/components/AccountEmailVerification.tsx#L19) |
| UI-00425 | {busy?localize("ui.63bbd08c916b4e99",{defaultValue:"Verifying…"}):localize("ui.e89a77e7fe58a6d1",{defaultValue:"Verify email"})} | submit / component action | {finish} | [web/components/AccountEmailVerification.tsx:19](../../../web/components/AccountEmailVerification.tsx#L19) |
| UI-00426 | {busy?localize("ui.b8ed5279e897be5d",{defaultValue:"Sending…"}):localize("ui.19e86d2b7055340c",{defaultValue:"Send verification code"})} | {()=>void send()} | Not a native form submit or inherited from component context | [web/components/AccountEmailVerification.tsx:19](../../../web/components/AccountEmailVerification.tsx#L19) |
| UI-00429 | {localize("ui.0e91610117029a62",{defaultValue:"Refresh"})} | {()=>setRetry(v=>v+1)} | Not a native form submit or inherited from component context | [web/components/AccountEmailVerification.tsx:19](../../../web/components/AccountEmailVerification.tsx#L19) |
| UI-00430 | {open?localize("ui.04014c416ca4087a",{defaultValue:"Close AI details"}):localize("ui.53645afb93725e68",{defaultValue:"View AI design details"})} | {()=>setOpen(value=>!value)} | Not a native form submit or inherited from component context | [web/components/AdminAiDesignDetails.tsx:12](../../../web/components/AdminAiDesignDetails.tsx#L12) |
| UI-00433 | {localize("ui.1ca619d7f7fe0d35",{defaultValue:"Refresh AI details"})} | {()=>void load()} | Not a native form submit or inherited from component context | [web/components/AdminAiDesignDetails.tsx:12](../../../web/components/AdminAiDesignDetails.tsx#L12) |
| UI-00457 | Reload latest branding | {()=>void reload()} | Not a native form submit or inherited from component context | [web/components/BrandingAdmin.tsx:10](../../../web/components/BrandingAdmin.tsx#L10) |
| UI-00466 | {busy?'Saving…':'Save branding'} | submit / component action | {e=>{e.preventDefault();void change(()=>brandingRequest('',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(form)}),'Branding saved successfully.')}} | [web/components/BrandingAdmin.tsx:10](../../../web/components/BrandingAdmin.tsx#L10) |
| UI-00469 | Reset {kind} | {()=>void change(()=>brandingRequest('/'+kind+'?revision='+form.revision,{method:'DELETE'}),'Brand image reset.')} | Not a native form submit or inherited from component context | [web/components/BrandingAdmin.tsx:10](../../../web/components/BrandingAdmin.tsx#L10) |
| UI-00475 | Previous | {()=>setPage(v=>v-1)} | Not a native form submit or inherited from component context | [web/components/BusinessAudit.tsx:7](../../../web/components/BusinessAudit.tsx#L7) |
| UI-00476 | Next | {()=>setPage(v=>v+1)} | Not a native form submit or inherited from component context | [web/components/BusinessAudit.tsx:7](../../../web/components/BusinessAudit.tsx#L7) |
| UI-00497 | {busy?localize("ui.42074396a441a347",{defaultValue:"Processing…"}):offer.amountPaise===0?localize("ui.850fbbaefb4057d0",{defaultValue:"Activate access"}):localize("ui.0e3afe9550a3bde6",{defaultValue:"{{value0}} · {{value1}} (test mode)",value0:(offer.entitled?'Renew':'Purchase'),value1:(commercialMoney(offer.amountPaise))})} | {()=>void pay()} | Not a native form submit or inherited from component context | [web/components/CommercePurchase.tsx:34](../../../web/components/CommercePurchase.tsx#L34) |
| UI-00499 | {localize("ui.087a9cc69283c135",{defaultValue:"Reload price"})} | {()=>{setRetry(v=>v+1)}} | Not a native form submit or inherited from component context | [web/components/CommercePurchase.tsx:34](../../../web/components/CommercePurchase.tsx#L34) |
| UI-00502 | {localize("ui.c1563118340040b1",{defaultValue:"Refresh commercial setup"})} | {()=>void load()} | Not a native form submit or inherited from component context | [web/components/CommercialAdminControls.tsx:16](../../../web/components/CommercialAdminControls.tsx#L16) |
| UI-00506 | {localize("ui.57ee14ce1425ca9b",{defaultValue:"Save policy"})} | submit / component action | {e=>{e.preventDefault();void save('/policy',{revision:policy.revision,freeActiveEvents:Number(policy.free_active_events),hostPlanDays:Number(policy.host_plan_days),vendorDays:Number(policy.vendor_days),sponsorDays:Number(policy.sponsor_days),reason})}} | [web/components/CommercialAdminControls.tsx:16](../../../web/components/CommercialAdminControls.tsx#L16) |
| UI-00510 | {localize("ui.cb845f023bfb385d",{defaultValue:"Save partner"})} | submit / component action | {async e=>{e.preventDefault();if(await save('/partners',partner))setPartner(partnerInitial)}} | [web/components/CommercialAdminControls.tsx:16](../../../web/components/CommercialAdminControls.tsx#L16) |
| UI-00511 | {localize("ui.61afc96d7515dbef",{defaultValue:"New partner"})} | {()=>setPartner(partnerInitial)} | {async e=>{e.preventDefault();if(await save('/partners',partner))setPartner(partnerInitial)}} | [web/components/CommercialAdminControls.tsx:16](../../../web/components/CommercialAdminControls.tsx#L16) |
| UI-00512 | {localize("ui.464c4ffd019e1e96",{defaultValue:"Edit"})} | {()=>setPartner({id:p.id,revision:p.revision,name:p.name,productDomain:p.product_domain,enabled:p.enabled,parameterName:p.parameter_name,parameterValue:p.parameter_value,clickParameter:p.click_parameter\|\|'',redirectTemplate:p.redirect_template\|\|'',reason:''})} | Not a native form submit or inherited from component context | [web/components/CommercialAdminControls.tsx:16](../../../web/components/CommercialAdminControls.tsx#L16) |
| UI-00517 | {localize("ui.358dccadb0876b12",{defaultValue:"Record statement"})} | submit / component action | {e=>{e.preventDefault();void save('/commissions',{...statement,clickId:statement.clickId\|\|null,amountPaise:Math.round(Number(statement.amountRupees)*100),revision:0})}} | [web/components/CommercialAdminControls.tsx:16](../../../web/components/CommercialAdminControls.tsx#L16) |
| UI-00519 | {localize("ui.b1da6781fdf8bbd3",{defaultValue:"Mark partner reported paid"})} | {()=>void save('/commissions',{id:c.id,revision:c.revision,partnerId:c.partner_id,clickId:c.click_id,externalReference:c.external_reference,amountPaise:c.amount_paise,state:'PAID',reason:c.changeReason})} | Not a native form submit or inherited from component context | [web/components/CommercialAdminControls.tsx:16](../../../web/components/CommercialAdminControls.tsx#L16) |
| UI-00520 | {localize("ui.a9670be9cfc31727",{defaultValue:"Record reversal"})} | {()=>void save('/commissions',{id:c.id,revision:c.revision,partnerId:c.partner_id,clickId:c.click_id,externalReference:c.external_reference,amountPaise:c.amount_paise,state:'REVERSED',reason:c.changeReason})} | Not a native form submit or inherited from component context | [web/components/CommercialAdminControls.tsx:16](../../../web/components/CommercialAdminControls.tsx#L16) |
| UI-00523 | {state==='APPROVED'?localize("ui.6007acbe30b2cd98",{defaultValue:"Approve"}):localize("ui.ffd4f8a3226ee013",{defaultValue:"Reject / remove"})} | {()=>void save(`/placements/${p.id}`,{state,reason:p.reviewReason},'POST')} | Not a native form submit or inherited from component context | [web/components/CommercialAdminControls.tsx:16](../../../web/components/CommercialAdminControls.tsx#L16) |
| UI-00528 | Accept optional analytics | {()=>{saveConsent(true);setShow(false)}} | Not a native form submit or inherited from component context | [web/components/CookieNotice.tsx:5](../../../web/components/CookieNotice.tsx#L5) |
| UI-00529 | Reject optional analytics | {()=>{saveConsent(false);setShow(false)}} | Not a native form submit or inherited from component context | [web/components/CookieNotice.tsx:5](../../../web/components/CookieNotice.tsx#L5) |
| UI-00542 | Refresh delivery status | {()=>void load()} | Not a native form submit or inherited from component context | [web/components/EmailDeliveryStatus.tsx:4](../../../web/components/EmailDeliveryStatus.tsx#L4) |
| UI-00680 | {t(busy?'Saving controls…':'Save experience controls')} | submit / component action | {save} | [web/components/GrowthControls.tsx:19](../../../web/components/GrowthControls.tsx#L19) |
| UI-00681 | {t('Reload saved settings')} | {()=>void load()} | {save} | [web/components/GrowthControls.tsx:19](../../../web/components/GrowthControls.tsx#L19) |
| UI-00682 | {t('Load controls')} | {()=>void load()} | Not a native form submit or inherited from component context | [web/components/GrowthControls.tsx:19](../../../web/components/GrowthControls.tsx#L19) |
| UI-00683 | {t('Refresh journey activity')} | {()=>void load()} | Not a native form submit or inherited from component context | [web/components/GrowthFunnel.tsx:9](../../../web/components/GrowthFunnel.tsx#L9) |
| UI-00777 |  | {()=>onUse(asset)} | Not a native form submit or inherited from component context | [web/components/HostInvitationUploads.tsx:21](../../../web/components/HostInvitationUploads.tsx#L21) |
| UI-00781 | {localizeText(busy?'Saving…':'Save upload access')} | {()=>void save()} | Not a native form submit or inherited from component context | [web/components/HostInvitationUploads.tsx:26](../../../web/components/HostInvitationUploads.tsx#L26) |
| UI-00788 | {collapsed?<PanelLeftOpen size={20}/>:<PanelLeftClose size={20}/>} | {toggle} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:50](../../../web/components/HostWorkspace.tsx#L50) |
| UI-00791 |  | {()=>void signOut()} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:53](../../../web/components/HostWorkspace.tsx#L53) |
| UI-00792 |  | {()=>setCollapsed(true)} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:54](../../../web/components/HostWorkspace.tsx#L54) |
| UI-00876 | Cookie preferences | {()=>window.dispatchEvent(new Event('giftwise-cookie-settings'))} | Not a native form submit or inherited from component context | [web/components/LegalFooter.tsx:4](../../../web/components/LegalFooter.tsx#L4) |
| UI-00881 | {localize("ui.1d9eb7b9db2ca330",{defaultValue:"Save translation"})} | {async()=>{setBusy(true);setError('');try{await languageAdmin(`/translations/${encodeURIComponent(language)}/${encodeURIComponent(entry.message_key)}`,'PUT',{text,published,version:entry.version});onSaved();window.dispatchEvent(new Event('gift.localization.changed'))}catch(e){setError(e instanceof Error?e.message:'Could not save translation')}finally{setBusy(false)}}} | Not a native form submit or inherited from component context | [web/components/LocalizationAdmin.tsx:12](../../../web/components/LocalizationAdmin.tsx#L12) |
| UI-00892 | {localize("ui.6be0bc232384e364",{defaultValue:"Save platform language settings"})} | submit / component action | {e=>{e.preventDefault();void action(()=>languageAdmin('','PUT',settings))}} | [web/components/LocalizationAdmin.tsx:24](../../../web/components/LocalizationAdmin.tsx#L24) |
| UI-00899 | {localize("ui.7ef7c4415c8ea5de",{defaultValue:"Add language"})} | submit / component action | {e=>{e.preventDefault();void action(()=>languageAdmin(`/languages/${encodeURIComponent(code)}`,'PUT',{nativeName,englishName,direction,enabled:true,version:0}))}} | [web/components/LocalizationAdmin.tsx:24](../../../web/components/LocalizationAdmin.tsx#L24) |
| UI-00902 | {localize("ui.689774c693d29ded",{defaultValue:"Export translation JSON"})} | {()=>void exportFile()} | Not a native form submit or inherited from component context | [web/components/LocalizationAdmin.tsx:24](../../../web/components/LocalizationAdmin.tsx#L24) |
| UI-00905 | {localize("ui.0d760eddf786ab29",{defaultValue:"Apply translation import"})} | {()=>void action(async()=>{await languageAdmin(`/translations/${encodeURIComponent(selected)}/import`,'POST',{entries:imported,publish:publishImport});setImported(null)})} | Not a native form submit or inherited from component context | [web/components/LocalizationAdmin.tsx:24](../../../web/components/LocalizationAdmin.tsx#L24) |
| UI-00906 | {localize("ui.b395a23fb9ab1107",{defaultValue:"Previous messages"})} | {()=>setOffset(value=>Math.max(0,value-50))} | Not a native form submit or inherited from component context | [web/components/LocalizationAdmin.tsx:24](../../../web/components/LocalizationAdmin.tsx#L24) |
| UI-00907 | {localize("ui.507ba056623df909",{defaultValue:"Next messages"})} | {()=>setOffset(value=>value+50)} | Not a native form submit or inherited from component context | [web/components/LocalizationAdmin.tsx:24](../../../web/components/LocalizationAdmin.tsx#L24) |
| UI-00929 | {localize("ui.c76f29ae1543f068",{defaultValue:"Use a different mobile number"})} | {()=>{setVerified(false);setMessage('')}} | Not a native form submit or inherited from component context | [web/components/MobileVerification.tsx:17](../../../web/components/MobileVerification.tsx#L17) |
| UI-00933 | {busy?localize("ui.4660a983366f7101",{defaultValue:"Please wait…"}):challenge?localize("ui.6d8e8cd5ef890b1d",{defaultValue:"Verify mobile"}):localize("ui.19e86d2b7055340c",{defaultValue:"Send verification code"})} | submit / component action | {submit} | [web/components/MobileVerification.tsx:17](../../../web/components/MobileVerification.tsx#L17) |
| UI-00934 | {localize("ui.7fd05b1a433fd9c4",{defaultValue:"Change number / request another code"})} | {()=>{setChallenge('');setCode('');setMessage('Wait at least 60 seconds before requesting another code.')}} | {submit} | [web/components/MobileVerification.tsx:17](../../../web/components/MobileVerification.tsx#L17) |
| UI-00937 | {localize("ui.415db71a129627bb",{defaultValue:"Reload saved settings"})} | {()=>void load()} | Not a native form submit or inherited from component context | [web/components/MonetizationControls.tsx:26](../../../web/components/MonetizationControls.tsx#L26) |
| UI-00948 | {localize("ui.5a1678bea87bb75d",{defaultValue:"Save monetization setup"})} | submit / component action | {save} | [web/components/MonetizationControls.tsx:26](../../../web/components/MonetizationControls.tsx#L26) |
| UI-00950 | {localize("ui.93df6f30251f0d8e",{defaultValue:"Load earlier history"})} | {async()=>{setBusy(true);try{const next=await api<History[]>(`/history?beforeRevision=${history[history.length-1].revision}`);setHistory([...history,...next]);}catch(e){setError(String(e))}finally{setBusy(false)}}} | Not a native form submit or inherited from component context | [web/components/MonetizationControls.tsx:26](../../../web/components/MonetizationControls.tsx#L26) |
| UI-00951 | {localize("ui.5b246c671ae11f03",{defaultValue:"Reload payment controls"})} | {async()=>{setBusy(true);try{setView(await commercialApi<View>(base+'/controls'+query));await historyLoad();setError('')}catch(e){setError(String(e))}finally{setBusy(false)}}} | Not a native form submit or inherited from component context | [web/components/PaymentControls.tsx:22](../../../web/components/PaymentControls.tsx#L22) |
| UI-00957 | {localize("ui.4a0e25b6dcc99396",{defaultValue:"Load earlier changes"})} | {async()=>{setBusy(true);try{const older=await commercialApi<History[]>(`${base}/history?beforeRevision=${history[history.length-1].revision}`);setHistory([...history,...older])}catch(e){setError(String(e))}finally{setBusy(false)}}} | Not a native form submit or inherited from component context | [web/components/PaymentControls.tsx:22](../../../web/components/PaymentControls.tsx#L22) |
| UI-00964 | {busy?localize("ui.ec963ffc911b8401",{defaultValue:"Checking…"}):localize("ui.53b1cab6bbe4b0ab",{defaultValue:"Check configuration"})} | {()=>void load()} | Not a native form submit or inherited from component context | [web/components/ProductionReadiness.tsx:8](../../../web/components/ProductionReadiness.tsx#L8) |
| UI-00970 | Remove picture | {()=>void run(()=>request('/picture',{method:'DELETE'}),'Profile picture removed.')} | Not a native form submit or inherited from component context | [web/components/ProfilePreferences.tsx:8](../../../web/components/ProfilePreferences.tsx#L8) |
| UI-00975 | {busy?'Saving…':'Save preferences'} | submit / component action | {e=>{e.preventDefault();void run(()=>request('',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(form)}),'Preferences saved successfully.')}} | [web/components/ProfilePreferences.tsx:8](../../../web/components/ProfilePreferences.tsx#L8) |
| UI-00988 | {localize("ui.e5019033694616fc",{defaultValue:"Reload routing commission"})} | {()=>void load()} | Not a native form submit or inherited from component context | [web/components/RoutingCommissionControl.tsx:16](../../../web/components/RoutingCommissionControl.tsx#L16) |
| UI-00992 | {localize("ui.d11f34fbd0661c36",{defaultValue:"Save routing commission"})} | submit / component action | {save} | [web/components/RoutingCommissionControl.tsx:16](../../../web/components/RoutingCommissionControl.tsx#L16) |
| UI-00995 | {localize("ui.754f642800555986",{defaultValue:"Load earlier commission changes"})} | {async()=>{setBusy(true);try{const earlier=await commercialApi<History[]>(`${base}/history?beforeRevision=${history[history.length-1].revision}`);setHistory([...history,...earlier])}catch(e){setError(String(e))}finally{setBusy(false)}}} | Not a native form submit or inherited from component context | [web/components/RoutingCommissionControl.tsx:16](../../../web/components/RoutingCommissionControl.tsx#L16) |
| UI-01049 | {localize("ui.15f20916edf5d894",{defaultValue:"Save SMS language template"})} | submit / component action | {async e=>{e.preventDefault();setBusy(true);setError('');setNotice('');try{const result=await languageAdmin(`/delivery-templates/${encodeURIComponent(language)}`,'PUT',{templateId:template,version});setVersion(result.version);setNotice('SMS language template saved.')}catch(e){setError(e instanceof Error?e.message:'Could not save SMS template')}finally{setBusy(false)}}} | [web/components/TranslationSupportTools.tsx:11](../../../web/components/TranslationSupportTools.tsx#L11) |
| UI-01052 | {localize("ui.6d9d3b03d9930e66",{defaultValue:"Register source phrase"})} | submit / component action | {async e=>{e.preventDefault();setBusy(true);setError('');setNotice('');try{await languageAdmin('/sources','POST',{text});setText('');setNotice('Source phrase registered. Find it in the translation workspace.');onSourceAdded()}catch(e){setError(e instanceof Error?e.message:'Could not register source text')}finally{setBusy(false)}}} | [web/components/TranslationSupportTools.tsx:11](../../../web/components/TranslationSupportTools.tsx#L11) |


### Dialog and alert/status inventory

| ID | Role / kind | Text / label | Visibility conditions | Source |
| --- | --- | --- | --- | --- |
| UI-00005 | "alert" | {localizeText(error)} | error | [web/app/admin/AccountPolicies.tsx:54](../../../web/app/admin/AccountPolicies.tsx#L54) |
| UI-00006 | "status" | {localizeText(message)} | message | [web/app/admin/AccountPolicies.tsx:55](../../../web/app/admin/AccountPolicies.tsx#L55) |
| UI-00016 | "alert" | {localizeText(error)} | error | [web/app/admin/AdminAudit.tsx:27](../../../web/app/admin/AdminAudit.tsx#L27) |
| UI-00018 | "status" | {localize("ui.4f38b6dd9dcbb064",{defaultValue:"Loading audit records…"})} | loading | [web/app/admin/AdminAudit.tsx:27](../../../web/app/admin/AdminAudit.tsx#L27) |
| UI-00019 | "status" | {result.total?localize("ui.98bbaf491bb7d9f2",{defaultValue:"{{value0}}–{{value1}} of {{value2}} records",value0:(result.page*20+1),value1:(Math.min(result.total,(result.page+1)*20)),value2:(result.total)}):localize("ui.849bea313490647c",{defaultValue:"No audit records match these filters."})} | result; loading | [web/app/admin/AdminAudit.tsx:27](../../../web/app/admin/AdminAudit.tsx#L27) |
| UI-00033 | "alert" | {localizeText(error)} | error | [web/app/admin/AdminInteractions.tsx:18](../../../web/app/admin/AdminInteractions.tsx#L18) |
| UI-00034 | "status" | {localizeText('Loading interaction analytics…')} | loading | [web/app/admin/AdminInteractions.tsx:18](../../../web/app/admin/AdminInteractions.tsx#L18) |
| UI-00039 | "alert" | {localizeText(detailError)} | detailError; data&&!error | [web/app/admin/AdminInteractions.tsx:18](../../../web/app/admin/AdminInteractions.tsx#L18) |
| UI-00061 | "alert" | {localizeText(error)} | error | [web/app/admin/page.tsx:87](../../../web/app/admin/page.tsx#L87) |
| UI-00072 | "status" | {localize("ui.bed9096c411e6da3",{defaultValue:"Opening your workspace…"})} | Parent/source state | [web/app/components/HostGlobalNav.tsx:17](../../../web/app/components/HostGlobalNav.tsx#L17) |
| UI-00416 | "status" | {localize("ui.5c92717e04c64d34",{defaultValue:"Checking your email…"})} | loading | [web/components/AccountEmailVerification.tsx:19](../../../web/components/AccountEmailVerification.tsx#L19) |
| UI-00417 | "status" | {localize("ui.2487c43aa0f7620b",{defaultValue:"✓ Email verified for account recovery. Verified email addresses cannot be changed."})} | verified; loading | [web/components/AccountEmailVerification.tsx:19](../../../web/components/AccountEmailVerification.tsx#L19) |
| UI-00427 | "status" | {localizeText(message)} | message | [web/components/AccountEmailVerification.tsx:19](../../../web/components/AccountEmailVerification.tsx#L19) |
| UI-00428 | "alert" | {localizeText(error)} | error | [web/components/AccountEmailVerification.tsx:19](../../../web/components/AccountEmailVerification.tsx#L19) |
| UI-00431 | "status" | {localize("ui.920dde356499b99e",{defaultValue:"Loading AI details…"})} | loading; open | [web/components/AdminAiDesignDetails.tsx:12](../../../web/components/AdminAiDesignDetails.tsx#L12) |
| UI-00432 | "alert" | {localizeText(error)} | error; open | [web/components/AdminAiDesignDetails.tsx:12](../../../web/components/AdminAiDesignDetails.tsx#L12) |
| UI-00434 | "alert" | {localizeText(error)} | error | [web/components/AdminEventModules.tsx:17](../../../web/components/AdminEventModules.tsx#L17) |
| UI-00456 | "alert" | {error} | error | [web/components/BrandingAdmin.tsx:10](../../../web/components/BrandingAdmin.tsx#L10) |
| UI-00458 | "status" | Loading branding… | !ready | [web/components/BrandingAdmin.tsx:10](../../../web/components/BrandingAdmin.tsx#L10) |
| UI-00470 | "status" | {message} | message | [web/components/BrandingAdmin.tsx:10](../../../web/components/BrandingAdmin.tsx#L10) |
| UI-00473 | "alert" | {error} | error | [web/components/BusinessAudit.tsx:7](../../../web/components/BusinessAudit.tsx#L7) |
| UI-00474 | "status" | Loading audit records… | loading | [web/components/BusinessAudit.tsx:7](../../../web/components/BusinessAudit.tsx#L7) |
| UI-00498 | "alert" | {localizeText(error)} | error; groupBase&&error.includes('switched off') | [web/components/CommercePurchase.tsx:34](../../../web/components/CommercePurchase.tsx#L34) |
| UI-00500 | "status" | {localizeText(notice)} | notice | [web/components/CommercePurchase.tsx:34](../../../web/components/CommercePurchase.tsx#L34) |
| UI-00501 | "alert" | {localizeText(error)} | error | [web/components/CommercialAdminControls.tsx:16](../../../web/components/CommercialAdminControls.tsx#L16) |
| UI-00540 | "alert" | {error} | error | [web/components/EmailDeliveryStatus.tsx:4](../../../web/components/EmailDeliveryStatus.tsx#L4) |
| UI-00541 | "status" | Loading email delivery… | data | [web/components/EmailDeliveryStatus.tsx:4](../../../web/components/EmailDeliveryStatus.tsx#L4) |
| UI-00548 | "status" | {localize("ui.7a005f0f1dbc704f",{defaultValue:"Opening your celebration space…"})} | ready\|\|discovery | [web/components/ExperienceProvider.tsx:19](../../../web/components/ExperienceProvider.tsx#L19) |
| UI-00674 | "alert" | {localizeText(error)} | error | [web/components/GroupMemberEmailPolicy.tsx:15](../../../web/components/GroupMemberEmailPolicy.tsx#L15) |
| UI-00675 | "alert" | {t(error)} | error | [web/components/GrowthControls.tsx:19](../../../web/components/GrowthControls.tsx#L19) |
| UI-00676 | "status" | {t(notice)} | notice | [web/components/GrowthControls.tsx:19](../../../web/components/GrowthControls.tsx#L19) |
| UI-00684 | "status" | {t('Loading journey activity…')} | busy | [web/components/GrowthFunnel.tsx:9](../../../web/components/GrowthFunnel.tsx#L9) |
| UI-00685 | "alert" | {t(error)} | error; busy | [web/components/GrowthFunnel.tsx:9](../../../web/components/GrowthFunnel.tsx#L9) |
| UI-00778 | "alert" | {localizeText(error)} | error | [web/components/HostInvitationUploads.tsx:21](../../../web/components/HostInvitationUploads.tsx#L21) |
| UI-00782 | "status" | {localizeText('Upload access saved and audited.')} | saved | [web/components/HostInvitationUploads.tsx:26](../../../web/components/HostInvitationUploads.tsx#L26) |
| UI-00783 | "alert" | {localizeText(error)} | error | [web/components/HostInvitationUploads.tsx:26](../../../web/components/HostInvitationUploads.tsx#L26) |
| UI-00790 | "alert" | {localizeText(error)} | error | [web/components/HostWorkspace.tsx:53](../../../web/components/HostWorkspace.tsx#L53) |
| UI-00882 | "alert" | {localizeText(error)} | error | [web/components/LocalizationAdmin.tsx:12](../../../web/components/LocalizationAdmin.tsx#L12) |
| UI-00884 | "alert" | {localizeText(error)} | error | [web/components/LocalizationAdmin.tsx:13](../../../web/components/LocalizationAdmin.tsx#L13) |
| UI-00885 | "alert" | {localizeText(error)} | error | [web/components/LocalizationAdmin.tsx:24](../../../web/components/LocalizationAdmin.tsx#L24) |
| UI-00886 | "status" | {localizeText(notice)} | notice | [web/components/LocalizationAdmin.tsx:24](../../../web/components/LocalizationAdmin.tsx#L24) |
| UI-00887 | "status" | {localize("ui.81e93b920c5e96ef",{defaultValue:"Loading language settings…"})} | !settings | [web/components/LocalizationAdmin.tsx:24](../../../web/components/LocalizationAdmin.tsx#L24) |
| UI-00909 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:19](../../../web/components/LocalizationProvider.tsx#L19) |
| UI-00911 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:20](../../../web/components/LocalizationProvider.tsx#L20) |
| UI-00913 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:21](../../../web/components/LocalizationProvider.tsx#L21) |
| UI-00928 | "status" | {localize("ui.522a82b93e381483",{defaultValue:"Mobile number verified"})} | verified | [web/components/MobileVerification.tsx:17](../../../web/components/MobileVerification.tsx#L17) |
| UI-00935 | "status" | {localizeText(message)} | message | [web/components/MobileVerification.tsx:17](../../../web/components/MobileVerification.tsx#L17) |
| UI-00936 | "alert" | {localizeText(error)} | error | [web/components/MonetizationControls.tsx:26](../../../web/components/MonetizationControls.tsx#L26) |
| UI-00949 | "status" | {localizeText(message)} | message | [web/components/MonetizationControls.tsx:26](../../../web/components/MonetizationControls.tsx#L26) |
| UI-00956 | "alert" | {localizeText(error)} | error | [web/components/PaymentControls.tsx:22](../../../web/components/PaymentControls.tsx#L22) |
| UI-00962 | "status" | {localizeText(message)} | message | [web/components/PlatformVerificationControls.tsx:13](../../../web/components/PlatformVerificationControls.tsx#L13) |
| UI-00963 | "alert" | {localizeText(error)} | error | [web/components/PlatformVerificationControls.tsx:13](../../../web/components/PlatformVerificationControls.tsx#L13) |
| UI-00965 | "alert" | {localizeText(error)} | error | [web/components/ProductionReadiness.tsx:8](../../../web/components/ProductionReadiness.tsx#L8) |
| UI-00966 | "alert" | {error} | error | [web/components/ProfilePreferences.tsx:8](../../../web/components/ProfilePreferences.tsx#L8) |
| UI-00967 | "status" | Loading profile preferences… | !form | [web/components/ProfilePreferences.tsx:8](../../../web/components/ProfilePreferences.tsx#L8) |
| UI-00976 | "status" | {message} | message | [web/components/ProfilePreferences.tsx:8](../../../web/components/ProfilePreferences.tsx#L8) |
| UI-00993 | "alert" | {localizeText(error)} | error | [web/components/RoutingCommissionControl.tsx:16](../../../web/components/RoutingCommissionControl.tsx#L16) |
| UI-00994 | "status" | {localizeText(message)} | message | [web/components/RoutingCommissionControl.tsx:16](../../../web/components/RoutingCommissionControl.tsx#L16) |
| UI-01053 | "alert" | {localizeText(error)} | error | [web/components/TranslationSupportTools.tsx:11](../../../web/components/TranslationSupportTools.tsx#L11) |
| UI-01054 | "status" | {localizeText(notice)} | notice | [web/components/TranslationSupportTools.tsx:11](../../../web/components/TranslationSupportTools.tsx#L11) |
| UI-01105 | "status" | {localize("ui.c6a3c87c8708580c",{defaultValue:"Saving view…"})} | busy | [web/components/focused/ExperienceControl.tsx:17](../../../web/components/focused/ExperienceControl.tsx#L17) |
| UI-01106 | "alert" | {localizeText(error)} | error | [web/components/focused/ExperienceControl.tsx:17](../../../web/components/focused/ExperienceControl.tsx#L17) |
| UI-01107 | "status" | {localizeText(message)} | message | [web/components/focused/ExperienceControl.tsx:17](../../../web/components/focused/ExperienceControl.tsx#L17) |


### Workflow state bindings

| Source | Binding | Initial expression |
| --- | --- | --- |
| web/app/admin/page.tsx | error | '' |
| web/app/admin/page.tsx | busy | false |
| web/app/admin/page.tsx | tab | 'overview' |
| web/components/GrowthControls.tsx | busy | false |
| web/components/GrowthControls.tsx | error | '' |
| web/components/GrowthControls.tsx | notice | '' |
| web/components/LocalizationProvider.tsx | busy | false |
| web/components/LocalizationProvider.tsx | error | '' |
| web/components/GrowthFunnel.tsx | error | '' |
| web/components/GrowthFunnel.tsx | busy | true |
| web/components/ProfilePreferences.tsx | busy | false |
| web/components/ProfilePreferences.tsx | message | '' |
| web/components/ProfilePreferences.tsx | error | '' |
| web/components/BusinessAudit.tsx | error | '' |
| web/components/BusinessAudit.tsx | loading | false |
| web/components/EmailDeliveryStatus.tsx | error | '' |
| web/components/BrandingAdmin.tsx | busy | false |
| web/components/BrandingAdmin.tsx | message | '' |
| web/components/BrandingAdmin.tsx | error | '' |
| web/components/LocalizationAdmin.tsx | busy | false |
| web/components/LocalizationAdmin.tsx | error | '' |
| web/components/LocalizationAdmin.tsx | opened | false |
| web/components/LocalizationAdmin.tsx | busy | false |
| web/components/LocalizationAdmin.tsx | error | '' |
| web/components/LocalizationAdmin.tsx | busy | false |
| web/components/LocalizationAdmin.tsx | error | '' |
| web/components/LocalizationAdmin.tsx | notice | '' |
| web/components/TranslationSupportTools.tsx | busy | false |
| web/components/TranslationSupportTools.tsx | error | '' |
| web/components/TranslationSupportTools.tsx | notice | '' |
| web/components/ProductionReadiness.tsx | busy | false |
| web/components/ProductionReadiness.tsx | error | '' |
| web/components/AdminEventModules.tsx | error | '' |
| web/components/AdminEventModules.tsx | busy | false |
| web/components/AdminEventModules.tsx | opened | false |
| web/components/AdminAiDesignDetails.tsx | open | false |
| web/components/AdminAiDesignDetails.tsx | loading | false |
| web/components/AdminAiDesignDetails.tsx | error | '' |
| web/components/focused/ExperienceControl.tsx | busy | false |
| web/components/focused/ExperienceControl.tsx | error | '' |
| web/components/focused/ExperienceControl.tsx | message | '' |
| web/components/PlatformVerificationControls.tsx | busy | false |
| web/components/PlatformVerificationControls.tsx | message | '' |
| web/components/PlatformVerificationControls.tsx | error | '' |
| web/components/MobileVerification.tsx | busy | false |
| web/components/MobileVerification.tsx | message | '' |
| web/components/AccountEmailVerification.tsx | loading | true |
| web/components/AccountEmailVerification.tsx | busy | false |
| web/components/AccountEmailVerification.tsx | message | '' |
| web/components/AccountEmailVerification.tsx | error | '' |
| web/components/GroupMemberEmailPolicy.tsx | error | '' |
| web/components/GroupMemberEmailPolicy.tsx | busy | false |
| web/app/admin/AccountPolicies.tsx | error | '' |
| web/app/admin/AccountPolicies.tsx | message | '' |
| web/app/admin/AccountPolicies.tsx | busy | false |
| web/app/admin/AdminAudit.tsx | loading | true |
| web/app/admin/AdminAudit.tsx | error | '' |
| web/app/admin/AdminAudit.tsx | reload | 0 |
| web/components/HostInvitationUploads.tsx | error | '' |
| web/components/HostInvitationUploads.tsx | busy | false |
| web/components/HostInvitationUploads.tsx | error | '' |
| web/components/HostInvitationUploads.tsx | busy | false |
| web/components/CommercePurchase.tsx | error | '' |
| web/components/CommercePurchase.tsx | busy | false |
| web/components/CommercePurchase.tsx | notice | '' |
| web/app/admin/AdminInteractions.tsx | error | '' |
| web/app/admin/AdminInteractions.tsx | loading | false |
| web/app/admin/AdminInteractions.tsx | detailError | '' |
| web/components/MonetizationControls.tsx | busy | false |
| web/components/MonetizationControls.tsx | error | '' |
| web/components/MonetizationControls.tsx | message | '' |
| web/components/CommercialAdminControls.tsx | error | '' |
| web/components/CommercialAdminControls.tsx | busy | false |
| web/components/PaymentControls.tsx | error | '' |
| web/components/PaymentControls.tsx | busy | false |
| web/components/RoutingCommissionControl.tsx | busy | false |
| web/components/RoutingCommissionControl.tsx | error | '' |
| web/components/RoutingCommissionControl.tsx | message | '' |
| web/components/HostWorkspace.tsx | busy | false |
| web/components/HostWorkspace.tsx | error | '' |


## Invitation asset gallery administration

| Screen information | Source-supported specification |
| --- | --- |
| Route | /admin/invitation-assets |
| Source | [web/app/admin/invitation-assets/page.tsx:1](../../../web/app/admin/invitation-assets/page.tsx#L1) |
| Purpose | Search/filter/upload/review/publish/tier invitation backgrounds/decorations |
| User access level | Authenticated PLATFORM_ADMIN |
| Navigation source | Admin gallery link |
| Navigation destination | Validated assets and publication/tier state; back to admin |
| Preconditions | Route parameters/query scope must be valid; applicable session/ownership/module/provider state above. Load errors do not imply an empty successful dataset. |
| Postconditions | Only the selected successful action changes its documented state. Navigation alone does not publish, confirm payment, verify contact or deliver a notification. |
| Route variables | No path variable |
| UI variants | Focused/general experience, responsive layout and module/state predicates can change visible sections. Exact predicates are in each control record. |


### Wireframe layout

| Section | Placement / behavior / evidence |
| --- | --- |
| Header | Shared identity/event or page title/back/actions, where declared. Composite header declarations: 3. |
| Navigation / sidebar | Host/admin navigation and event context where applicable; mobile/focused variants are conditional. nav=3, aside=3. |
| Main content | Purpose-specific forms/cards/tables/canvas/player from the composed sources. main=3, section=4, article=1. |
| Search and filters | Fields/selects listed below; exact value/handler/visibility controls are not inferred from placeholder labels. |
| Data grids and cards | Table declarations=0. Data collections and generated rows/cards are in frontend.json dynamicSources. |
| Popups / dialogs | Named/source-declared dialog control records below; opening/closing/loading/apply/cancel actions belong to those component handlers. |
| Alerts / notifications | Source controls with role alert/status are listed below. Notice strings may come from API results; do not assume a universal envelope. |
| Footer | Local/shared footer declarations=1. No undocumented legal/footer wording is invented. |
| Loading / empty / success / error | Observed state bindings and relevant action fields are listed below. Expected QA states from Document 2 do not establish a spinner/retry control where source does not implement one. |


### Composed implementation

- [web/app/admin/invitation-assets/page.tsx:1](../../../web/app/admin/invitation-assets/page.tsx#L1)
- [web/components/LocalizationProvider.tsx:1](../../../web/components/LocalizationProvider.tsx#L1)
- [web/components/ExperienceProvider.tsx:1](../../../web/components/ExperienceProvider.tsx#L1)
- [web/components/CommercePurchase.tsx:1](../../../web/components/CommercePurchase.tsx#L1)
- [web/app/layout.tsx:1](../../../web/app/layout.tsx#L1)
- [web/components/GrowthProvider.tsx:1](../../../web/components/GrowthProvider.tsx#L1)
- [web/components/BrandingProvider.tsx:1](../../../web/components/BrandingProvider.tsx#L1)
- [web/components/LegalFooter.tsx:1](../../../web/components/LegalFooter.tsx#L1)
- [web/components/CookieNotice.tsx:1](../../../web/components/CookieNotice.tsx#L1)
- [web/app/components/ApplicationInteractions.tsx:1](../../../web/app/components/ApplicationInteractions.tsx#L1)
- [web/components/VerificationProvider.tsx:1](../../../web/components/VerificationProvider.tsx#L1)
- [web/app/components/PerformanceDiagnostics.tsx:1](../../../web/app/components/PerformanceDiagnostics.tsx#L1)
- [web/app/components/HostSessionRedirect.tsx:1](../../../web/app/components/HostSessionRedirect.tsx#L1)
- [web/app/components/HostGlobalNav.tsx:1](../../../web/app/components/HostGlobalNav.tsx#L1)
- [web/components/HostWorkspace.tsx:1](../../../web/components/HostWorkspace.tsx#L1)
- [web/components/focused/HostShell.tsx:1](../../../web/components/focused/HostShell.tsx#L1)

### Fields

| Control ID | Label / expression | Type | Binding | Required | Native limits / format | Detailed specification |
| --- | --- | --- | --- | --- | --- | --- |
| UI-00044 | {localize("ui.292c06f0045a45d0",{defaultValue:"Category"})} | select  | {category} | Not declared |  | [control](UI_CONTROLS.md#ui-00044) |
| UI-00045 | {localizeText('Template access')} | select  | {tier} | Not declared |  | [control](UI_CONTROLS.md#ui-00045) |
| UI-00046 | {localize("ui.f68e0b1ef1269bae",{defaultValue:"Occasion"})} | select  | {occasion} | Not declared |  | [control](UI_CONTROLS.md#ui-00046) |
| UI-00047 | {localize("ui.7e8cd2056da73a7f",{defaultValue:"Title"})} | input  | {title} | true | maxLength={120} | [control](UI_CONTROLS.md#ui-00047) |
| UI-00048 | {localize("ui.799bde27d87f6cd8",{defaultValue:"Image (automatically optimized to 2 MB)"})} | input "file" |  | true |  | [control](UI_CONTROLS.md#ui-00048) |
| UI-00052 | {localize("ui.f68e0b1ef1269bae",{defaultValue:"Occasion"})} | select  | {filter} | Not declared |  | [control](UI_CONTROLS.md#ui-00052) |
| UI-00053 | {localize("ui.920e413c7d411b61",{defaultValue:"Status"})} | select  | {status} | Not declared |  | [control](UI_CONTROLS.md#ui-00053) |
| UI-00054 | {localize("ui.3f7328c9b281ce5a",{defaultValue:"Access tier"})} | select  | {a.tier\|\|'FREE'} | Not declared |  | [control](UI_CONTROLS.md#ui-00054) |
| UI-00908 | {localize("ui.bba5b40bdd28c66e",{defaultValue:"Choose language"})} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00908) |
| UI-00910 | {label} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00910) |
| UI-00912 | {localize("ui.140071feea59fe87",{defaultValue:"Preferred language"})} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00912) |


### Buttons

| ID | Label / expression | Action | Submit validation / handler | Source |
| --- | --- | --- | --- | --- |
| UI-00051 | {busy?localize("ui.5474eef8d0f179c7",{defaultValue:"Working…"}):localize("ui.b79a6ae3e54c1ad7",{defaultValue:"Upload as draft"})} | submit / component action | {upload} | [web/app/admin/invitation-assets/page.tsx:96](../../../web/app/admin/invitation-assets/page.tsx#L96) |
| UI-00055 | {a.published?localize("ui.2db04a54fa743150",{defaultValue:"Unpublish"}):localize("ui.859390eb495b2ead",{defaultValue:"Publish"})} | {()=>void toggle(a)} | Not a native form submit or inherited from component context | [web/app/admin/invitation-assets/page.tsx:118](../../../web/app/admin/invitation-assets/page.tsx#L118) |
| UI-00497 | {busy?localize("ui.42074396a441a347",{defaultValue:"Processing…"}):offer.amountPaise===0?localize("ui.850fbbaefb4057d0",{defaultValue:"Activate access"}):localize("ui.0e3afe9550a3bde6",{defaultValue:"{{value0}} · {{value1}} (test mode)",value0:(offer.entitled?'Renew':'Purchase'),value1:(commercialMoney(offer.amountPaise))})} | {()=>void pay()} | Not a native form submit or inherited from component context | [web/components/CommercePurchase.tsx:34](../../../web/components/CommercePurchase.tsx#L34) |
| UI-00499 | {localize("ui.087a9cc69283c135",{defaultValue:"Reload price"})} | {()=>{setRetry(v=>v+1)}} | Not a native form submit or inherited from component context | [web/components/CommercePurchase.tsx:34](../../../web/components/CommercePurchase.tsx#L34) |
| UI-00528 | Accept optional analytics | {()=>{saveConsent(true);setShow(false)}} | Not a native form submit or inherited from component context | [web/components/CookieNotice.tsx:5](../../../web/components/CookieNotice.tsx#L5) |
| UI-00529 | Reject optional analytics | {()=>{saveConsent(false);setShow(false)}} | Not a native form submit or inherited from component context | [web/components/CookieNotice.tsx:5](../../../web/components/CookieNotice.tsx#L5) |
| UI-00788 | {collapsed?<PanelLeftOpen size={20}/>:<PanelLeftClose size={20}/>} | {toggle} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:50](../../../web/components/HostWorkspace.tsx#L50) |
| UI-00791 |  | {()=>void signOut()} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:53](../../../web/components/HostWorkspace.tsx#L53) |
| UI-00792 |  | {()=>setCollapsed(true)} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:54](../../../web/components/HostWorkspace.tsx#L54) |
| UI-00876 | Cookie preferences | {()=>window.dispatchEvent(new Event('giftwise-cookie-settings'))} | Not a native form submit or inherited from component context | [web/components/LegalFooter.tsx:4](../../../web/components/LegalFooter.tsx#L4) |


### Dialog and alert/status inventory

| ID | Role / kind | Text / label | Visibility conditions | Source |
| --- | --- | --- | --- | --- |
| UI-00041 | "alert" | {localizeText(error)} | error | [web/app/admin/invitation-assets/page.tsx:81](../../../web/app/admin/invitation-assets/page.tsx#L81) |
| UI-00042 | "status" | {localizeText(notice)} | notice | [web/app/admin/invitation-assets/page.tsx:82](../../../web/app/admin/invitation-assets/page.tsx#L82) |
| UI-00049 | "status" | {localize("ui.9d9d9b8f3296cca0",{defaultValue:"Optimizing image…"})} | optimizing | [web/app/admin/invitation-assets/page.tsx:92](../../../web/app/admin/invitation-assets/page.tsx#L92) |
| UI-00050 | "status" | {localize("ui.65cae0ad8637bf00",{defaultValue:"Ready to upload: "})} {(originalSize/1048576).toFixed(2)} {localize("ui.801b70ed64ae7623",{defaultValue:" MB → "})} {(file.size/1048576).toFixed(2)} {localize("ui.51190257ff1142ac",{defaultValue:" MB ("})} {file.type==='image/png'?localize("ui.796120837694d3f3",{defaultValue:"PNG"}):localize("ui.f4dc1c96a30fbdeb",{defaultValue:"JPEG"})} ) | file&&originalSize!==null | [web/app/admin/invitation-assets/page.tsx:93](../../../web/app/admin/invitation-assets/page.tsx#L93) |
| UI-00072 | "status" | {localize("ui.bed9096c411e6da3",{defaultValue:"Opening your workspace…"})} | Parent/source state | [web/app/components/HostGlobalNav.tsx:17](../../../web/app/components/HostGlobalNav.tsx#L17) |
| UI-00498 | "alert" | {localizeText(error)} | error; groupBase&&error.includes('switched off') | [web/components/CommercePurchase.tsx:34](../../../web/components/CommercePurchase.tsx#L34) |
| UI-00500 | "status" | {localizeText(notice)} | notice | [web/components/CommercePurchase.tsx:34](../../../web/components/CommercePurchase.tsx#L34) |
| UI-00548 | "status" | {localize("ui.7a005f0f1dbc704f",{defaultValue:"Opening your celebration space…"})} | ready\|\|discovery | [web/components/ExperienceProvider.tsx:19](../../../web/components/ExperienceProvider.tsx#L19) |
| UI-00790 | "alert" | {localizeText(error)} | error | [web/components/HostWorkspace.tsx:53](../../../web/components/HostWorkspace.tsx#L53) |
| UI-00909 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:19](../../../web/components/LocalizationProvider.tsx#L19) |
| UI-00911 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:20](../../../web/components/LocalizationProvider.tsx#L20) |
| UI-00913 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:21](../../../web/components/LocalizationProvider.tsx#L21) |


### Workflow state bindings

| Source | Binding | Initial expression |
| --- | --- | --- |
| web/app/admin/invitation-assets/page.tsx | loading | true |
| web/app/admin/invitation-assets/page.tsx | error | '' |
| web/app/admin/invitation-assets/page.tsx | notice | '' |
| web/app/admin/invitation-assets/page.tsx | busy | false |
| web/components/LocalizationProvider.tsx | busy | false |
| web/components/LocalizationProvider.tsx | error | '' |
| web/components/CommercePurchase.tsx | error | '' |
| web/components/CommercePurchase.tsx | busy | false |
| web/components/CommercePurchase.tsx | notice | '' |
| web/components/HostWorkspace.tsx | busy | false |
| web/components/HostWorkspace.tsx | error | '' |


## Contact support

| Screen information | Source-supported specification |
| --- | --- |
| Route | /contact |
| Source | [web/app/contact/page.tsx:1](../../../web/app/contact/page.tsx#L1) |
| Purpose | Read configured support details |
| User access level | Public |
| Navigation source | Footer / legal pages |
| Navigation destination | Owner-configured support email or other approved contact |
| Preconditions | Route parameters/query scope must be valid; applicable session/ownership/module/provider state above. Load errors do not imply an empty successful dataset. |
| Postconditions | Only the selected successful action changes its documented state. Navigation alone does not publish, confirm payment, verify contact or deliver a notification. |
| Route variables | No path variable |
| UI variants | Focused/general experience, responsive layout and module/state predicates can change visible sections. Exact predicates are in each control record. |


### Wireframe layout

| Section | Placement / behavior / evidence |
| --- | --- |
| Header | Shared identity/event or page title/back/actions, where declared. Composite header declarations: 2. |
| Navigation / sidebar | Host/admin navigation and event context where applicable; mobile/focused variants are conditional. nav=3, aside=3. |
| Main content | Purpose-specific forms/cards/tables/canvas/player from the composed sources. main=3, section=2, article=0. |
| Search and filters | Fields/selects listed below; exact value/handler/visibility controls are not inferred from placeholder labels. |
| Data grids and cards | Table declarations=0. Data collections and generated rows/cards are in frontend.json dynamicSources. |
| Popups / dialogs | Named/source-declared dialog control records below; opening/closing/loading/apply/cancel actions belong to those component handlers. |
| Alerts / notifications | Source controls with role alert/status are listed below. Notice strings may come from API results; do not assume a universal envelope. |
| Footer | Local/shared footer declarations=1. No undocumented legal/footer wording is invented. |
| Loading / empty / success / error | Observed state bindings and relevant action fields are listed below. Expected QA states from Document 2 do not establish a spinner/retry control where source does not implement one. |


### Composed implementation

- [web/app/contact/page.tsx:1](../../../web/app/contact/page.tsx#L1)
- [web/components/BrandingProvider.tsx:1](../../../web/components/BrandingProvider.tsx#L1)
- [web/app/layout.tsx:1](../../../web/app/layout.tsx#L1)
- [web/components/GrowthProvider.tsx:1](../../../web/components/GrowthProvider.tsx#L1)
- [web/components/LegalFooter.tsx:1](../../../web/components/LegalFooter.tsx#L1)
- [web/components/CookieNotice.tsx:1](../../../web/components/CookieNotice.tsx#L1)
- [web/app/components/ApplicationInteractions.tsx:1](../../../web/app/components/ApplicationInteractions.tsx#L1)
- [web/components/LocalizationProvider.tsx:1](../../../web/components/LocalizationProvider.tsx#L1)
- [web/components/VerificationProvider.tsx:1](../../../web/components/VerificationProvider.tsx#L1)
- [web/components/ExperienceProvider.tsx:1](../../../web/components/ExperienceProvider.tsx#L1)
- [web/app/components/PerformanceDiagnostics.tsx:1](../../../web/app/components/PerformanceDiagnostics.tsx#L1)
- [web/app/components/HostSessionRedirect.tsx:1](../../../web/app/components/HostSessionRedirect.tsx#L1)
- [web/app/components/HostGlobalNav.tsx:1](../../../web/app/components/HostGlobalNav.tsx#L1)
- [web/components/HostWorkspace.tsx:1](../../../web/components/HostWorkspace.tsx#L1)
- [web/components/focused/HostShell.tsx:1](../../../web/components/focused/HostShell.tsx#L1)

### Fields

| Control ID | Label / expression | Type | Binding | Required | Native limits / format | Detailed specification |
| --- | --- | --- | --- | --- | --- | --- |
| UI-00908 | {localize("ui.bba5b40bdd28c66e",{defaultValue:"Choose language"})} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00908) |
| UI-00910 | {label} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00910) |
| UI-00912 | {localize("ui.140071feea59fe87",{defaultValue:"Preferred language"})} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00912) |


### Buttons

| ID | Label / expression | Action | Submit validation / handler | Source |
| --- | --- | --- | --- | --- |
| UI-00528 | Accept optional analytics | {()=>{saveConsent(true);setShow(false)}} | Not a native form submit or inherited from component context | [web/components/CookieNotice.tsx:5](../../../web/components/CookieNotice.tsx#L5) |
| UI-00529 | Reject optional analytics | {()=>{saveConsent(false);setShow(false)}} | Not a native form submit or inherited from component context | [web/components/CookieNotice.tsx:5](../../../web/components/CookieNotice.tsx#L5) |
| UI-00788 | {collapsed?<PanelLeftOpen size={20}/>:<PanelLeftClose size={20}/>} | {toggle} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:50](../../../web/components/HostWorkspace.tsx#L50) |
| UI-00791 |  | {()=>void signOut()} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:53](../../../web/components/HostWorkspace.tsx#L53) |
| UI-00792 |  | {()=>setCollapsed(true)} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:54](../../../web/components/HostWorkspace.tsx#L54) |
| UI-00876 | Cookie preferences | {()=>window.dispatchEvent(new Event('giftwise-cookie-settings'))} | Not a native form submit or inherited from component context | [web/components/LegalFooter.tsx:4](../../../web/components/LegalFooter.tsx#L4) |


### Dialog and alert/status inventory

| ID | Role / kind | Text / label | Visibility conditions | Source |
| --- | --- | --- | --- | --- |
| UI-00072 | "status" | {localize("ui.bed9096c411e6da3",{defaultValue:"Opening your workspace…"})} | Parent/source state | [web/app/components/HostGlobalNav.tsx:17](../../../web/app/components/HostGlobalNav.tsx#L17) |
| UI-00548 | "status" | {localize("ui.7a005f0f1dbc704f",{defaultValue:"Opening your celebration space…"})} | ready\|\|discovery | [web/components/ExperienceProvider.tsx:19](../../../web/components/ExperienceProvider.tsx#L19) |
| UI-00790 | "alert" | {localizeText(error)} | error | [web/components/HostWorkspace.tsx:53](../../../web/components/HostWorkspace.tsx#L53) |
| UI-00909 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:19](../../../web/components/LocalizationProvider.tsx#L19) |
| UI-00911 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:20](../../../web/components/LocalizationProvider.tsx#L20) |
| UI-00913 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:21](../../../web/components/LocalizationProvider.tsx#L21) |


### Workflow state bindings

| Source | Binding | Initial expression |
| --- | --- | --- |
| web/components/LocalizationProvider.tsx | busy | false |
| web/components/LocalizationProvider.tsx | error | '' |
| web/components/HostWorkspace.tsx | busy | false |
| web/components/HostWorkspace.tsx | error | '' |


## Host dashboard

| Screen information | Source-supported specification |
| --- | --- |
| Route | /dashboard |
| Source | [web/app/dashboard/page.tsx:1](../../../web/app/dashboard/page.tsx#L1) |
| Purpose | Summarize own celebrations and navigate workspace |
| User access level | Authenticated CUSTOMER; admin has its own console |
| Navigation source | Successful host login / workspace Overview |
| Navigation destination | Create/open own event; studio; receiving accounts |
| Preconditions | Route parameters/query scope must be valid; applicable session/ownership/module/provider state above. Load errors do not imply an empty successful dataset. |
| Postconditions | Only the selected successful action changes its documented state. Navigation alone does not publish, confirm payment, verify contact or deliver a notification. |
| Route variables | No path variable |
| UI variants | Focused/general experience, responsive layout and module/state predicates can change visible sections. Exact predicates are in each control record. |


### Wireframe layout

| Section | Placement / behavior / evidence |
| --- | --- |
| Header | Shared identity/event or page title/back/actions, where declared. Composite header declarations: 4. |
| Navigation / sidebar | Host/admin navigation and event context where applicable; mobile/focused variants are conditional. nav=3, aside=3. |
| Main content | Purpose-specific forms/cards/tables/canvas/player from the composed sources. main=4, section=9, article=2. |
| Search and filters | Fields/selects listed below; exact value/handler/visibility controls are not inferred from placeholder labels. |
| Data grids and cards | Table declarations=0. Data collections and generated rows/cards are in frontend.json dynamicSources. |
| Popups / dialogs | Named/source-declared dialog control records below; opening/closing/loading/apply/cancel actions belong to those component handlers. |
| Alerts / notifications | Source controls with role alert/status are listed below. Notice strings may come from API results; do not assume a universal envelope. |
| Footer | Local/shared footer declarations=1. No undocumented legal/footer wording is invented. |
| Loading / empty / success / error | Observed state bindings and relevant action fields are listed below. Expected QA states from Document 2 do not establish a spinner/retry control where source does not implement one. |


### Composed implementation

- [web/app/dashboard/page.tsx:1](../../../web/app/dashboard/page.tsx#L1)
- [web/components/DashboardInsights.tsx:1](../../../web/components/DashboardInsights.tsx#L1)
- [web/components/LocalizationProvider.tsx:1](../../../web/components/LocalizationProvider.tsx#L1)
- [web/components/GrowthProvider.tsx:1](../../../web/components/GrowthProvider.tsx#L1)
- [web/components/ExperienceProvider.tsx:1](../../../web/components/ExperienceProvider.tsx#L1)
- [web/components/focused/Dashboard.tsx:1](../../../web/components/focused/Dashboard.tsx#L1)
- [web/app/components/HostToast.tsx:1](../../../web/app/components/HostToast.tsx#L1)
- [web/app/components/HostNotificationBell.tsx:1](../../../web/app/components/HostNotificationBell.tsx#L1)
- [web/app/layout.tsx:1](../../../web/app/layout.tsx#L1)
- [web/components/BrandingProvider.tsx:1](../../../web/components/BrandingProvider.tsx#L1)
- [web/components/LegalFooter.tsx:1](../../../web/components/LegalFooter.tsx#L1)
- [web/components/CookieNotice.tsx:1](../../../web/components/CookieNotice.tsx#L1)
- [web/app/components/ApplicationInteractions.tsx:1](../../../web/app/components/ApplicationInteractions.tsx#L1)
- [web/components/VerificationProvider.tsx:1](../../../web/components/VerificationProvider.tsx#L1)
- [web/app/components/PerformanceDiagnostics.tsx:1](../../../web/app/components/PerformanceDiagnostics.tsx#L1)
- [web/app/components/HostSessionRedirect.tsx:1](../../../web/app/components/HostSessionRedirect.tsx#L1)
- [web/app/components/HostGlobalNav.tsx:1](../../../web/app/components/HostGlobalNav.tsx#L1)
- [web/components/HostWorkspace.tsx:1](../../../web/components/HostWorkspace.tsx#L1)
- [web/components/focused/HostShell.tsx:1](../../../web/components/focused/HostShell.tsx#L1)

### Fields

| Control ID | Label / expression | Type | Binding | Required | Native limits / format | Detailed specification |
| --- | --- | --- | --- | --- | --- | --- |
| UI-00908 | {localize("ui.bba5b40bdd28c66e",{defaultValue:"Choose language"})} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00908) |
| UI-00910 | {label} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00910) |
| UI-00912 | {localize("ui.140071feea59fe87",{defaultValue:"Preferred language"})} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00912) |


### Buttons

| ID | Label / expression | Action | Submit validation / handler | Source |
| --- | --- | --- | --- | --- |
| UI-00073 | {localize("ui.3185b2520fd33953",{defaultValue:" Notifications "})} {inbox.unread>0&&<strong>({inbox.unread})</strong>} | {()=>setOpen(!open)} | Not a native form submit or inherited from component context | [web/app/components/HostNotificationBell.tsx:30](../../../web/app/components/HostNotificationBell.tsx#L30) |
| UI-00076 | {localize("ui.b49c9b6cdf43b308",{defaultValue:"Mark read"})} | {()=>void markRead(n.id)} | Not a native form submit or inherited from component context | [web/app/components/HostNotificationBell.tsx:37](../../../web/app/components/HostNotificationBell.tsx#L37) |
| UI-00077 | {localize("ui.157e675e3b4fadd3",{defaultValue:"Refresh alerts"})} | {()=>void load()} | Not a native form submit or inherited from component context | [web/app/components/HostNotificationBell.tsx:38](../../../web/app/components/HostNotificationBell.tsx#L38) |
| UI-00078 | × | {()=>{setFlash(null);onDismiss?.();}} | Not a native form submit or inherited from component context | [web/app/components/HostToast.tsx:16](../../../web/app/components/HostToast.tsx#L16) |
| UI-00084 | {localize("ui.942087cc2d41e013",{defaultValue:"Retry"})} | {()=>void load()} | Not a native form submit or inherited from component context | [web/app/dashboard/page.tsx:39](../../../web/app/dashboard/page.tsx#L39) |
| UI-00085 | {localize("ui.7ce64141b163cd0d",{defaultValue:" Refresh"})} | {()=>void load()} | Not a native form submit or inherited from component context | [web/app/dashboard/page.tsx:41](../../../web/app/dashboard/page.tsx#L41) |
| UI-00528 | Accept optional analytics | {()=>{saveConsent(true);setShow(false)}} | Not a native form submit or inherited from component context | [web/components/CookieNotice.tsx:5](../../../web/components/CookieNotice.tsx#L5) |
| UI-00529 | Reject optional analytics | {()=>{saveConsent(false);setShow(false)}} | Not a native form submit or inherited from component context | [web/components/CookieNotice.tsx:5](../../../web/components/CookieNotice.tsx#L5) |
| UI-00535 | {t('Retry')} | {()=>void load()} | Not a native form submit or inherited from component context | [web/components/DashboardInsights.tsx:13](../../../web/components/DashboardInsights.tsx#L13) |
| UI-00788 | {collapsed?<PanelLeftOpen size={20}/>:<PanelLeftClose size={20}/>} | {toggle} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:50](../../../web/components/HostWorkspace.tsx#L50) |
| UI-00791 |  | {()=>void signOut()} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:53](../../../web/components/HostWorkspace.tsx#L53) |
| UI-00792 |  | {()=>setCollapsed(true)} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:54](../../../web/components/HostWorkspace.tsx#L54) |
| UI-00876 | Cookie preferences | {()=>window.dispatchEvent(new Event('giftwise-cookie-settings'))} | Not a native form submit or inherited from component context | [web/components/LegalFooter.tsx:4](../../../web/components/LegalFooter.tsx#L4) |


### Dialog and alert/status inventory

| ID | Role / kind | Text / label | Visibility conditions | Source |
| --- | --- | --- | --- | --- |
| UI-00072 | "status" | {localize("ui.bed9096c411e6da3",{defaultValue:"Opening your workspace…"})} | Parent/source state | [web/app/components/HostGlobalNav.tsx:17](../../../web/app/components/HostGlobalNav.tsx#L17) |
| UI-00074 | "alert" | {localizeText(error)} | error; open | [web/app/components/HostNotificationBell.tsx:32](../../../web/app/components/HostNotificationBell.tsx#L32) |
| UI-00082 | "alert" | {localizeText(error)} {localize("ui.e1a3e78cb5c88586",{defaultValue:" or "})} | error | [web/app/dashboard/page.tsx:39](../../../web/app/dashboard/page.tsx#L39) |
| UI-00086 | "status" | {localize("ui.361b2cc13538cf9e",{defaultValue:"Loading your celebrations…"})} | loading | [web/app/dashboard/page.tsx:42](../../../web/app/dashboard/page.tsx#L42) |
| UI-00533 | "status" | {t('Loading next steps…')} | state==='loading' | [web/components/DashboardInsights.tsx:13](../../../web/components/DashboardInsights.tsx#L13) |
| UI-00534 | "alert" | {t('Unable to load next steps.')} | state==='error'; state==='loading' | [web/components/DashboardInsights.tsx:13](../../../web/components/DashboardInsights.tsx#L13) |
| UI-00548 | "status" | {localize("ui.7a005f0f1dbc704f",{defaultValue:"Opening your celebration space…"})} | ready\|\|discovery | [web/components/ExperienceProvider.tsx:19](../../../web/components/ExperienceProvider.tsx#L19) |
| UI-00790 | "alert" | {localizeText(error)} | error | [web/components/HostWorkspace.tsx:53](../../../web/components/HostWorkspace.tsx#L53) |
| UI-00909 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:19](../../../web/components/LocalizationProvider.tsx#L19) |
| UI-00911 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:20](../../../web/components/LocalizationProvider.tsx#L20) |
| UI-00913 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:21](../../../web/components/LocalizationProvider.tsx#L21) |
| UI-01074 | "alert" | {localizeText(error)} | error | [web/components/focused/Dashboard.tsx:18](../../../web/components/focused/Dashboard.tsx#L18) |
| UI-01075 | "status" | {localize("ui.be6e64345eb4f883",{defaultValue:"Loading your overview…"})} | loading | [web/components/focused/Dashboard.tsx:18](../../../web/components/focused/Dashboard.tsx#L18) |


### Workflow state bindings

| Source | Binding | Initial expression |
| --- | --- | --- |
| web/app/dashboard/page.tsx | loading | true |
| web/app/dashboard/page.tsx | error | '' |
| web/components/LocalizationProvider.tsx | busy | false |
| web/components/LocalizationProvider.tsx | error | '' |
| web/components/focused/Dashboard.tsx | loading | true |
| web/components/focused/Dashboard.tsx | error | '' |
| web/app/components/HostNotificationBell.tsx | open | false |
| web/app/components/HostNotificationBell.tsx | error | '' |
| web/components/HostWorkspace.tsx | busy | false |
| web/components/HostWorkspace.tsx | error | '' |


## Sample invitation

| Screen information | Source-supported specification |
| --- | --- |
| Route | /demo |
| Source | [web/app/demo/page.tsx:1](../../../web/app/demo/page.tsx#L1) |
| Purpose | Explore five fictional occasion previews and local-only sample response choices |
| User access level | Public; PUBLIC_DEMO growth flag |
| Navigation source | Public home demo CTA |
| Navigation destination | Registration or public home; no guest/event/payment writes |
| Preconditions | Route parameters/query scope must be valid; applicable session/ownership/module/provider state above. Load errors do not imply an empty successful dataset. |
| Postconditions | Only the selected successful action changes its documented state. Navigation alone does not publish, confirm payment, verify contact or deliver a notification. |
| Route variables | No path variable |
| UI variants | Focused/general experience, responsive layout and module/state predicates can change visible sections. Exact predicates are in each control record. |


### Wireframe layout

| Section | Placement / behavior / evidence |
| --- | --- |
| Header | Shared identity/event or page title/back/actions, where declared. Composite header declarations: 3. |
| Navigation / sidebar | Host/admin navigation and event context where applicable; mobile/focused variants are conditional. nav=3, aside=3. |
| Main content | Purpose-specific forms/cards/tables/canvas/player from the composed sources. main=5, section=2, article=1. |
| Search and filters | Fields/selects listed below; exact value/handler/visibility controls are not inferred from placeholder labels. |
| Data grids and cards | Table declarations=0. Data collections and generated rows/cards are in frontend.json dynamicSources. |
| Popups / dialogs | Named/source-declared dialog control records below; opening/closing/loading/apply/cancel actions belong to those component handlers. |
| Alerts / notifications | Source controls with role alert/status are listed below. Notice strings may come from API results; do not assume a universal envelope. |
| Footer | Local/shared footer declarations=2. No undocumented legal/footer wording is invented. |
| Loading / empty / success / error | Observed state bindings and relevant action fields are listed below. Expected QA states from Document 2 do not establish a spinner/retry control where source does not implement one. |


### Composed implementation

- [web/app/demo/page.tsx:1](../../../web/app/demo/page.tsx#L1)
- [web/components/GrowthProvider.tsx:1](../../../web/components/GrowthProvider.tsx#L1)
- [web/components/LocalizationProvider.tsx:1](../../../web/components/LocalizationProvider.tsx#L1)
- [web/app/layout.tsx:1](../../../web/app/layout.tsx#L1)
- [web/components/BrandingProvider.tsx:1](../../../web/components/BrandingProvider.tsx#L1)
- [web/components/LegalFooter.tsx:1](../../../web/components/LegalFooter.tsx#L1)
- [web/components/CookieNotice.tsx:1](../../../web/components/CookieNotice.tsx#L1)
- [web/app/components/ApplicationInteractions.tsx:1](../../../web/app/components/ApplicationInteractions.tsx#L1)
- [web/components/VerificationProvider.tsx:1](../../../web/components/VerificationProvider.tsx#L1)
- [web/components/ExperienceProvider.tsx:1](../../../web/components/ExperienceProvider.tsx#L1)
- [web/app/components/PerformanceDiagnostics.tsx:1](../../../web/app/components/PerformanceDiagnostics.tsx#L1)
- [web/app/components/HostSessionRedirect.tsx:1](../../../web/app/components/HostSessionRedirect.tsx#L1)
- [web/app/components/HostGlobalNav.tsx:1](../../../web/app/components/HostGlobalNav.tsx#L1)
- [web/components/HostWorkspace.tsx:1](../../../web/components/HostWorkspace.tsx#L1)
- [web/components/focused/HostShell.tsx:1](../../../web/components/focused/HostShell.tsx#L1)

### Fields

| Control ID | Label / expression | Type | Binding | Required | Native limits / format | Detailed specification |
| --- | --- | --- | --- | --- | --- | --- |
| UI-00093 | {t('Choose an occasion')} | select  | {occasion} | Not declared |  | [control](UI_CONTROLS.md#ui-00093) |
| UI-00908 | {localize("ui.bba5b40bdd28c66e",{defaultValue:"Choose language"})} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00908) |
| UI-00910 | {label} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00910) |
| UI-00912 | {localize("ui.140071feea59fe87",{defaultValue:"Preferred language"})} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00912) |


### Buttons

| ID | Label / expression | Action | Submit validation / handler | Source |
| --- | --- | --- | --- | --- |
| UI-00094 | {t(label)} | {()=>setResponse(label)} | Not a native form submit or inherited from component context | [web/app/demo/page.tsx:9](../../../web/app/demo/page.tsx#L9) |
| UI-00528 | Accept optional analytics | {()=>{saveConsent(true);setShow(false)}} | Not a native form submit or inherited from component context | [web/components/CookieNotice.tsx:5](../../../web/components/CookieNotice.tsx#L5) |
| UI-00529 | Reject optional analytics | {()=>{saveConsent(false);setShow(false)}} | Not a native form submit or inherited from component context | [web/components/CookieNotice.tsx:5](../../../web/components/CookieNotice.tsx#L5) |
| UI-00788 | {collapsed?<PanelLeftOpen size={20}/>:<PanelLeftClose size={20}/>} | {toggle} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:50](../../../web/components/HostWorkspace.tsx#L50) |
| UI-00791 |  | {()=>void signOut()} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:53](../../../web/components/HostWorkspace.tsx#L53) |
| UI-00792 |  | {()=>setCollapsed(true)} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:54](../../../web/components/HostWorkspace.tsx#L54) |
| UI-00876 | Cookie preferences | {()=>window.dispatchEvent(new Event('giftwise-cookie-settings'))} | Not a native form submit or inherited from component context | [web/components/LegalFooter.tsx:4](../../../web/components/LegalFooter.tsx#L4) |


### Dialog and alert/status inventory

| ID | Role / kind | Text / label | Visibility conditions | Source |
| --- | --- | --- | --- | --- |
| UI-00072 | "status" | {localize("ui.bed9096c411e6da3",{defaultValue:"Opening your workspace…"})} | Parent/source state | [web/app/components/HostGlobalNav.tsx:17](../../../web/app/components/HostGlobalNav.tsx#L17) |
| UI-00091 | "status" | {t('Loading sample invitation…')} | Parent/source state | [web/app/demo/page.tsx:8](../../../web/app/demo/page.tsx#L8) |
| UI-00095 | "status" | {t('Sample response selected. Nothing has been submitted.')} | response | [web/app/demo/page.tsx:9](../../../web/app/demo/page.tsx#L9) |
| UI-00548 | "status" | {localize("ui.7a005f0f1dbc704f",{defaultValue:"Opening your celebration space…"})} | ready\|\|discovery | [web/components/ExperienceProvider.tsx:19](../../../web/components/ExperienceProvider.tsx#L19) |
| UI-00790 | "alert" | {localizeText(error)} | error | [web/components/HostWorkspace.tsx:53](../../../web/components/HostWorkspace.tsx#L53) |
| UI-00909 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:19](../../../web/components/LocalizationProvider.tsx#L19) |
| UI-00911 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:20](../../../web/components/LocalizationProvider.tsx#L20) |
| UI-00913 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:21](../../../web/components/LocalizationProvider.tsx#L21) |


### Workflow state bindings

| Source | Binding | Initial expression |
| --- | --- | --- |
| web/components/LocalizationProvider.tsx | busy | false |
| web/components/LocalizationProvider.tsx | error | '' |
| web/components/HostWorkspace.tsx | busy | false |
| web/components/HostWorkspace.tsx | error | '' |


## Public guest invitation

| Screen information | Source-supported specification |
| --- | --- |
| Route | /e/[token] |
| Source | [web/app/e/[token]/page.tsx:1](../../../web/app/e/[token]/page.tsx#L1) |
| Purpose | Hero/event facts, wishlist/contributions, special/group gifts, RSVP/wishes and sharing |
| User access level | Valid published token; per-feature/channel/readiness checks |
| Navigation source | Shared invitation/QR |
| Navigation destination | Guest contribution, Special Gift chooser, group link, RSVP/wish or external share/retailer |
| Preconditions | Route parameters/query scope must be valid; applicable session/ownership/module/provider state above. Load errors do not imply an empty successful dataset. |
| Postconditions | Only the selected successful action changes its documented state. Navigation alone does not publish, confirm payment, verify contact or deliver a notification. |
| Route variables | token |
| UI variants | Focused/general experience, responsive layout and module/state predicates can change visible sections. Exact predicates are in each control record. |


### Wireframe layout

| Section | Placement / behavior / evidence |
| --- | --- |
| Header | Shared identity/event or page title/back/actions, where declared. Composite header declarations: 6. |
| Navigation / sidebar | Host/admin navigation and event context where applicable; mobile/focused variants are conditional. nav=4, aside=5. |
| Main content | Purpose-specific forms/cards/tables/canvas/player from the composed sources. main=10, section=27, article=6. |
| Search and filters | Fields/selects listed below; exact value/handler/visibility controls are not inferred from placeholder labels. |
| Data grids and cards | Table declarations=1. Data collections and generated rows/cards are in frontend.json dynamicSources. |
| Popups / dialogs | Named/source-declared dialog control records below; opening/closing/loading/apply/cancel actions belong to those component handlers. |
| Alerts / notifications | Source controls with role alert/status are listed below. Notice strings may come from API results; do not assume a universal envelope. |
| Footer | Local/shared footer declarations=4. No undocumented legal/footer wording is invented. |
| Loading / empty / success / error | Observed state bindings and relevant action fields are listed below. Expected QA states from Document 2 do not establish a spinner/retry control where source does not implement one. |


### Composed implementation

- [web/app/e/[token]/page.tsx:1](../../../web/app/e/[token]/page.tsx#L1)
- [web/app/e/[token]/PublicEventClient.tsx:1](../../../web/app/e/[token]/PublicEventClient.tsx#L1)
- [web/components/GrowthProvider.tsx:1](../../../web/components/GrowthProvider.tsx#L1)
- [web/components/GuestDiscovery.tsx:1](../../../web/components/GuestDiscovery.tsx#L1)
- [web/components/LocalizationProvider.tsx:1](../../../web/components/LocalizationProvider.tsx#L1)
- [web/components/GiftThumbnail.tsx:1](../../../web/components/GiftThumbnail.tsx#L1)
- [web/components/BrandingProvider.tsx:1](../../../web/components/BrandingProvider.tsx#L1)
- [web/components/CelebrationDetails.tsx:1](../../../web/components/CelebrationDetails.tsx#L1)
- [web/components/GroupContribution.tsx:1](../../../web/components/GroupContribution.tsx#L1)
- [web/components/CredentialRequirements.tsx:1](../../../web/components/CredentialRequirements.tsx#L1)
- [web/components/TermsAcceptance.tsx:1](../../../web/components/TermsAcceptance.tsx#L1)
- [web/components/VerificationProvider.tsx:1](../../../web/components/VerificationProvider.tsx#L1)
- [web/components/MobileVerification.tsx:1](../../../web/components/MobileVerification.tsx#L1)
- [web/components/ExperienceProvider.tsx:1](../../../web/components/ExperienceProvider.tsx#L1)
- [web/components/GroupGuestCheckout.tsx:1](../../../web/components/GroupGuestCheckout.tsx#L1)
- [web/components/SignupEmailField.tsx:1](../../../web/components/SignupEmailField.tsx#L1)
- [web/components/GroupEmailVerification.tsx:1](../../../web/components/GroupEmailVerification.tsx#L1)
- [web/components/GroupCreatorPaymentSettings.tsx:1](../../../web/components/GroupCreatorPaymentSettings.tsx#L1)
- [web/components/GroupGuestCreatorSettings.tsx:1](../../../web/components/GroupGuestCreatorSettings.tsx#L1)
- [web/components/GroupGiftPlan.tsx:1](../../../web/components/GroupGiftPlan.tsx#L1)
- [web/components/IndependentGroupPlan.tsx:1](../../../web/components/IndependentGroupPlan.tsx#L1)
- [web/components/GuestGiftManager.tsx:1](../../../web/components/GuestGiftManager.tsx#L1)
- [web/components/DeliveryAddressFields.tsx:1](../../../web/components/DeliveryAddressFields.tsx#L1)
- [web/components/GroupActivity.tsx:1](../../../web/components/GroupActivity.tsx#L1)
- [web/components/CommercePurchase.tsx:1](../../../web/components/CommercePurchase.tsx#L1)
- [web/components/GuestGiftBoard.tsx:1](../../../web/components/GuestGiftBoard.tsx#L1)
- [web/components/SpecialGiftEntry.tsx:1](../../../web/components/SpecialGiftEntry.tsx#L1)
- [web/components/IndividualHostContribution.tsx:1](../../../web/components/IndividualHostContribution.tsx#L1)
- [web/components/GuestVerifiedDestination.tsx:1](../../../web/components/GuestVerifiedDestination.tsx#L1)
- [web/components/GiftDescription.tsx:1](../../../web/components/GiftDescription.tsx#L1)
- [web/components/CommercialPlacements.tsx:1](../../../web/components/CommercialPlacements.tsx#L1)
- [web/components/focused/Invitation.tsx:1](../../../web/components/focused/Invitation.tsx#L1)
- [web/components/GuestContributionCheckout.tsx:1](../../../web/components/GuestContributionCheckout.tsx#L1)
- [web/components/PaymentDisclosure.tsx:1](../../../web/components/PaymentDisclosure.tsx#L1)
- [web/components/focused/Checkout.tsx:1](../../../web/components/focused/Checkout.tsx#L1)
- [web/components/GuestActions.tsx:1](../../../web/components/GuestActions.tsx#L1)
- [web/components/GuestCelebration.tsx:1](../../../web/components/GuestCelebration.tsx#L1)
- [web/components/SocialSharing.tsx:1](../../../web/components/SocialSharing.tsx#L1)
- [web/app/layout.tsx:1](../../../web/app/layout.tsx#L1)
- [web/components/LegalFooter.tsx:1](../../../web/components/LegalFooter.tsx#L1)
- [web/components/CookieNotice.tsx:1](../../../web/components/CookieNotice.tsx#L1)
- [web/app/components/ApplicationInteractions.tsx:1](../../../web/app/components/ApplicationInteractions.tsx#L1)
- [web/app/components/PerformanceDiagnostics.tsx:1](../../../web/app/components/PerformanceDiagnostics.tsx#L1)
- [web/app/components/HostSessionRedirect.tsx:1](../../../web/app/components/HostSessionRedirect.tsx#L1)
- [web/app/components/HostGlobalNav.tsx:1](../../../web/app/components/HostGlobalNav.tsx#L1)
- [web/components/HostWorkspace.tsx:1](../../../web/components/HostWorkspace.tsx#L1)
- [web/components/focused/HostShell.tsx:1](../../../web/components/focused/HostShell.tsx#L1)

### Fields

| Control ID | Label / expression | Type | Binding | Required | Native limits / format | Detailed specification |
| --- | --- | --- | --- | --- | --- | --- |
| UI-00103 | {localize("ui.a95dfa33544070f8",{defaultValue:"Sort gifts"})} | select  | {sortBy} | Not declared |  | [control](UI_CONTROLS.md#ui-00103) |
| UI-00538 | {localize("ui.455df35dd66a82ca",{defaultValue:"Same as event address"})} | input "checkbox" | {same} | Not declared |  | [control](UI_CONTROLS.md#ui-00538) |
| UI-00539 | {localizeText(label)} | input  | {address[key]} | true | maxLength={maxLength} | [control](UI_CONTROLS.md#ui-00539) |
| UI-00568 | {localize("ui.187e80ba4d8a22bb",{defaultValue:"Mobile number"})} | input "tel" | {registrationMobile} | true |  | [control](UI_CONTROLS.md#ui-00568) |
| UI-00569 | {localize("ui.f13a64ba2fea87ef",{defaultValue:"Full name"})} | input  | {fullName} | true | minLength={2}; maxLength={120} | [control](UI_CONTROLS.md#ui-00569) |
| UI-00570 | {localize("ui.e3b89e9d33f88e52",{defaultValue:"Username"})} | input  | {username} | true | minLength={tab==='register'?4:undefined}; maxLength={tab==='register'?30:undefined}; pattern={tab==='register'?'[A-Za-z0-9][A-Za-z0-9_]{3,29}':undefined} | [control](UI_CONTROLS.md#ui-00570) |
| UI-00571 | {localize("ui.11aaa14268df51b7",{defaultValue:"Sign in with"})} | select  | {loginMethod} | Not declared |  | [control](UI_CONTROLS.md#ui-00571) |
| UI-00572 | {localize("ui.e7cf3ef4f17c3999",{defaultValue:"Password"})} | input "password" | {password} | true | minLength={tab==='register'?8:undefined} | [control](UI_CONTROLS.md#ui-00572) |
| UI-00573 | {localize("ui.5ac265f396a2c586",{defaultValue:"Confirm password"})} | input "password" | {confirm} | true | minLength={8} | [control](UI_CONTROLS.md#ui-00573) |
| UI-00574 | {localize("ui.61c2b9f2c16ea005",{defaultValue:"Six-digit PIN"})} | input "password" | {pin} | true | minLength={6}; maxLength={6}; pattern="[0-9]{6}" | [control](UI_CONTROLS.md#ui-00574) |
| UI-00575 | {localize("ui.4fa10c7baa0c1d89",{defaultValue:"Confirm PIN"})} | input "password" | {confirmPin} | true | minLength={6}; maxLength={6}; pattern="[0-9]{6}" | [control](UI_CONTROLS.md#ui-00575) |
| UI-00576 | {localize("ui.61c2b9f2c16ea005",{defaultValue:"Six-digit PIN"})} | input "password" | {pin} | true | minLength={6}; maxLength={6}; pattern="[0-9]{6}" | [control](UI_CONTROLS.md#ui-00576) |
| UI-00580 | {localize("ui.d82e5fe24eed3db4",{defaultValue:"What should we call you?"})} | input  | {adminNameDraft} | true | minLength={2}; maxLength={120} | [control](UI_CONTROLS.md#ui-00580) |
| UI-00584 | {localize("ui.72ed2bd767cef627",{defaultValue:"Current password"})} | input "password" | {pinPassword} | true |  | [control](UI_CONTROLS.md#ui-00584) |
| UI-00585 | {localize("ui.61c2b9f2c16ea005",{defaultValue:"Six-digit PIN"})} | input "password" | {pin} | true | maxLength={6}; pattern="[0-9]{6}" | [control](UI_CONTROLS.md#ui-00585) |
| UI-00586 | {localize("ui.4fa10c7baa0c1d89",{defaultValue:"Confirm PIN"})} | input "password" | {confirmPin} | true | maxLength={6}; pattern="[0-9]{6}" | [control](UI_CONTROLS.md#ui-00586) |
| UI-00605 | {localize("ui.c99c586025d6769c",{defaultValue:"Who can join?"})} | select  | {privacy.join_policy} | Not declared |  | [control](UI_CONTROLS.md#ui-00605) |
| UI-00606 | {localize("ui.2ad6410b25fa2dbb",{defaultValue:"Who can see the member list?"})} | select  | {privacy.member_visibility} | Not declared |  | [control](UI_CONTROLS.md#ui-00606) |
| UI-00607 | {localize("ui.5bc6d84f2126eb5e",{defaultValue:"Who can see individual contribution amounts?"})} | select  | {privacy.amount_visibility} | Not declared |  | [control](UI_CONTROLS.md#ui-00607) |
| UI-00610 | {localize("ui.6cb353a65d0160d5",{defaultValue:"Group name *"})} | input  | {form.name} | true | maxLength={120} | [control](UI_CONTROLS.md#ui-00610) |
| UI-00611 | {localize("ui.7204645b7341d714",{defaultValue:"Your name *"})} | input  | {form.creatorName} | true | maxLength={120} | [control](UI_CONTROLS.md#ui-00611) |
| UI-00612 | {localize("ui.0836c3b7cadbef5b",{defaultValue:"Mobile number *"})} | input "tel" | {form.creatorMobile} | true | maxLength={20} | [control](UI_CONTROLS.md#ui-00612) |
| UI-00613 | {localize("ui.751e68347f10bcf7",{defaultValue:"Group description"})} | textarea  | {form.description} | Not declared | maxLength={500} | [control](UI_CONTROLS.md#ui-00613) |
| UI-00614 | {localize("ui.09b05cb4d017cf72",{defaultValue:"Collection plan"})} | select  | {giftId} | Not declared |  | [control](UI_CONTROLS.md#ui-00614) |
| UI-00615 | {localize("ui.4e82ed6b989eb0fb",{defaultValue:"Gift name (optional)"})} | input  | {form.giftName} | Not declared | maxLength={160} | [control](UI_CONTROLS.md#ui-00615) |
| UI-00616 | {localize("ui.da0378150bd065d4",{defaultValue:"Gift description"})} | textarea  | {form.giftDescription} | Not declared | maxLength={500} | [control](UI_CONTROLS.md#ui-00616) |
| UI-00617 | {localize("ui.5c7466d4572b8863",{defaultValue:"Gift link"})} | input "url" | {form.giftLink} | Not declared | maxLength={1000} | [control](UI_CONTROLS.md#ui-00617) |
| UI-00618 | {localize("ui.1c68de56ec168a54",{defaultValue:"Gift goal in ₹"})} | input  | {form.targetRupees} | Not declared |  | [control](UI_CONTROLS.md#ui-00618) |
| UI-00621 |  | input "file" |  | Not declared |  | [control](UI_CONTROLS.md#ui-00621) |
| UI-00628 | {localize("ui.b948ac04b854394f",{defaultValue:"Payment method"})} | select  | {kind} | Not declared |  | [control](UI_CONTROLS.md#ui-00628) |
| UI-00629 | {localize("ui.193fa77b177b9520",{defaultValue:"Account nickname"})} | input  | {label} | true | maxLength={80} | [control](UI_CONTROLS.md#ui-00629) |
| UI-00630 | {localize("ui.cec4a9c0b1e71189",{defaultValue:"UPI ID"})} | input  | {upiId} | true |  | [control](UI_CONTROLS.md#ui-00630) |
| UI-00631 | {localize("ui.53477ca834fa478d",{defaultValue:"Account holder"})} | input  | {accountHolder} | true | maxLength={120} | [control](UI_CONTROLS.md#ui-00631) |
| UI-00632 | {localize("ui.f7573b7f5db0b550",{defaultValue:"Account number"})} | input  | {accountNumber} | true |  | [control](UI_CONTROLS.md#ui-00632) |
| UI-00633 | {localize("ui.f3d8c96e71944625",{defaultValue:"IFSC"})} | input  | {ifsc} | true | maxLength={11} | [control](UI_CONTROLS.md#ui-00633) |
| UI-00637 | {localize("ui.f2488fd4ef4adbc6",{defaultValue:"Email address"})} | input "email" | {email} | true | maxLength={254} | [control](UI_CONTROLS.md#ui-00637) |
| UI-00640 | {localize("ui.3bcb66ee1a6feaf5",{defaultValue:"Six-digit code"})} | input  | {code} | true | maxLength={6}; pattern="[0-9]{6}" | [control](UI_CONTROLS.md#ui-00640) |
| UI-00647 | {localize("ui.b03cdfe17e82db86",{defaultValue:"Wishlist gift"})} | select  | {choice} | Not declared |  | [control](UI_CONTROLS.md#ui-00647) |
| UI-00654 | {localize("ui.749c97e490fa85d6",{defaultValue:"Your contribution amount (₹)"})} | input  | {amount} | true |  | [control](UI_CONTROLS.md#ui-00654) |
| UI-00655 | {localize("ui.7204645b7341d714",{defaultValue:"Your name *"})} | input  | {name} | true | minLength={2}; maxLength={120} | [control](UI_CONTROLS.md#ui-00655) |
| UI-00656 | {localize("ui.433f5f401017f2de",{defaultValue:"Mobile or email *"})} | input  | {contact} | true | maxLength={120} | [control](UI_CONTROLS.md#ui-00656) |
| UI-00660 | {localize("ui.5ffa24818e50be17",{defaultValue:"Transaction reference / UTR *"})} | input  | {reference} | true | minLength={6}; maxLength={120} | [control](UI_CONTROLS.md#ui-00660) |
| UI-00666 | {localize("ui.ec8e5c2f4fabcfa9",{defaultValue:"Invitation link options"})} | select  | {accessMode} | Not declared |  | [control](UI_CONTROLS.md#ui-00666) |
| UI-00667 | {localize("ui.f4228ef0788c1e26",{defaultValue:"Suggested contribution (₹)"})} | input  | {amount} | Not declared |  | [control](UI_CONTROLS.md#ui-00667) |
| UI-00668 | {localize("ui.de9329ae7c59130b",{defaultValue:"UPI account for guests"})} | select  | {upi} | Not declared |  | [control](UI_CONTROLS.md#ui-00668) |
| UI-00669 | {localize("ui.de0b78f10c8d9b42",{defaultValue:"Bank account for guests"})} | select  | {bank} | Not declared |  | [control](UI_CONTROLS.md#ui-00669) |
| UI-00670 | {localize("ui.559f892eac11e3cc",{defaultValue:" Enable manual guest contribution instructions"})} | input "checkbox" | {enabled} | Not declared |  | [control](UI_CONTROLS.md#ui-00670) |
| UI-00691 | {localize("ui.4d7c4f98db4e9668",{defaultValue:"RSVP guest name"})} | input  | {name} | true | maxLength={120} | [control](UI_CONTROLS.md#ui-00691) |
| UI-00692 | {localize("ui.70d4092a019c5b1e",{defaultValue:"RSVP email"})} | input "email" | {email} | true | maxLength={254} | [control](UI_CONTROLS.md#ui-00692) |
| UI-00694 |  | input "radio" | {value} | Not declared |  | [control](UI_CONTROLS.md#ui-00694) |
| UI-00695 | {localize("ui.5d9b03aa0b5067b7",{defaultValue:"People attending (including you)"})} | input "number" | {party} | true | min={1}; max={10} | [control](UI_CONTROLS.md#ui-00695) |
| UI-00699 | {localize("ui.a9d4201422911b11",{defaultValue:"Guestbook name"})} | input  | {wishName} | true | maxLength={120} | [control](UI_CONTROLS.md#ui-00699) |
| UI-00700 | {localize("ui.be979451f10245e8",{defaultValue:"Guestbook message"})} | textarea  | {message} | true | maxLength={1000} | [control](UI_CONTROLS.md#ui-00700) |
| UI-00701 | {localize("ui.5435fa51d13a2c03",{defaultValue:"Guestbook photo"})} | input "file" |  | Not declared |  | [control](UI_CONTROLS.md#ui-00701) |
| UI-00722 |  | input "radio" | {method.id} | Not declared |  | [control](UI_CONTROLS.md#ui-00722) |
| UI-00723 | {localize("ui.293d404a500f5f9a",{defaultValue:"Destination"})} | select  | {giftId} | Not declared |  | [control](UI_CONTROLS.md#ui-00723) |
| UI-00724 | {localize("ui.fcbb0bc384eed802",{defaultValue:"Amount (₹)"})} | input "number" | {amount} | true | min="1"; max="1000000"; step="0.01" | [control](UI_CONTROLS.md#ui-00724) |
| UI-00725 | {localize("ui.7204645b7341d714",{defaultValue:"Your name *"})} | input  | {name} | true | maxLength={200} | [control](UI_CONTROLS.md#ui-00725) |
| UI-00727 | {localize("ui.6eea8608c452711b",{defaultValue:"Email (optional)"})} | input "email" | {email} | Not declared | maxLength={320} | [control](UI_CONTROLS.md#ui-00727) |
| UI-00728 | {localize("ui.35878f0bd6c133b3",{defaultValue:"Phone (optional)"})} | input "tel" | {phone} | Not declared | maxLength={30} | [control](UI_CONTROLS.md#ui-00728) |
| UI-00729 | {localize("ui.0c0b7cca57d23288",{defaultValue:"UPI transaction ID / Bank UTR"})} | input  | {transferReference} | Not declared | maxLength={40} | [control](UI_CONTROLS.md#ui-00729) |
| UI-00745 | {localize("ui.64b4ff26c07d838e",{defaultValue:"Gift name"})} | input  | {name} | true | maxLength={160} | [control](UI_CONTROLS.md#ui-00745) |
| UI-00746 | {localize("ui.83688b69ed8d6461",{defaultValue:"Description · optional"})} | textarea  | {description} | Not declared | maxLength={500} | [control](UI_CONTROLS.md#ui-00746) |
| UI-00747 | {localize("ui.271401f0515cac0b",{defaultValue:"Product link · optional"})} | input "url" | {url} | Not declared | maxLength={1000}; pattern="https://[^\s]+" | [control](UI_CONTROLS.md#ui-00747) |
| UI-00749 | {localize("ui.9ead2ac60e7db3f4",{defaultValue:"How would you like to give?"})} | select  | {type} | Not declared |  | [control](UI_CONTROLS.md#ui-00749) |
| UI-00761 | {localize("ui.6f17cdcd888c485e",{defaultValue:"Preferred recipient"})} | select  | {selected?.id??''} | Not declared |  | [control](UI_CONTROLS.md#ui-00761) |
| UI-00793 | {localize("ui.09b05cb4d017cf72",{defaultValue:"Collection plan"})} | select  | {plan} | Not declared |  | [control](UI_CONTROLS.md#ui-00793) |
| UI-00794 | {localize("ui.5d824da34fa87848",{defaultValue:"Our gift’s product link"})} | input "url" | {value.giftLink} | true | maxLength={1000}; pattern="https://[^\s]+" | [control](UI_CONTROLS.md#ui-00794) |
| UI-00796 | {localize("ui.64b4ff26c07d838e",{defaultValue:"Gift name"})} | input  | {value.giftName} | true | maxLength={160} | [control](UI_CONTROLS.md#ui-00796) |
| UI-00797 | {localize("ui.da0378150bd065d4",{defaultValue:"Gift description"})} | textarea  | {value.giftDescription} | Not declared | maxLength={500} | [control](UI_CONTROLS.md#ui-00797) |
| UI-00798 | {localize("ui.259feaef1f119dee",{defaultValue:"Collection goal (₹) · optional"})} | input "number" | {value.targetRupees} | Not declared | min="0.01"; max="1000000000"; step="0.01" | [control](UI_CONTROLS.md#ui-00798) |
| UI-00807 | {localize("ui.b948ac04b854394f",{defaultValue:"Payment method"})} | select  | {method} | Not declared |  | [control](UI_CONTROLS.md#ui-00807) |
| UI-00808 | {localize("ui.0c0b7cca57d23288",{defaultValue:"UPI transaction ID / Bank UTR"})} | input  | {reference} | true | minLength={8}; maxLength={40}; pattern="[A-Za-z0-9\-]{8,40}" | [control](UI_CONTROLS.md#ui-00808) |
| UI-00908 | {localize("ui.bba5b40bdd28c66e",{defaultValue:"Choose language"})} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00908) |
| UI-00910 | {label} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00910) |
| UI-00912 | {localize("ui.140071feea59fe87",{defaultValue:"Preferred language"})} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00912) |
| UI-00931 | {localize("ui.dae5cf30549f072d",{defaultValue:"Mobile number with country code"})} | input "tel" | {mobile} | true | pattern="\+[1-9][0-9]{7,14}" | [control](UI_CONTROLS.md#ui-00931) |
| UI-00932 | {localize("ui.f673a246d45f4b8f",{defaultValue:"Six-digit verification code"})} | input  | {code} | true | maxLength={6}; pattern="[0-9]{6}" | [control](UI_CONTROLS.md#ui-00932) |
| UI-00998 | {localize("ui.f2488fd4ef4adbc6",{defaultValue:"Email address"})} | input "email" | {value} | true | maxLength={254} | [control](UI_CONTROLS.md#ui-00998) |
| UI-01012 | {localize("ui.88c3be35063553d5",{defaultValue:"Invitation link"})} | input  | {url} | Not declared |  | [control](UI_CONTROLS.md#ui-01012) |
| UI-01024 | {localize("ui.f296b67a545cd756",{defaultValue:"Shipment tracking details"})} | textarea  | {tracking} | true | maxLength={1000} | [control](UI_CONTROLS.md#ui-01024) |
| UI-01028 | {localize("ui.af7e58d035aad83e",{defaultValue:"Choose a gift"})} | select  | {source} | Not declared |  | [control](UI_CONTROLS.md#ui-01028) |
| UI-01030 | {localize("ui.2c6b2e253c01fb2e",{defaultValue:"Your name"})} | input  | {form.guestName} | true | maxLength={120} | [control](UI_CONTROLS.md#ui-01030) |
| UI-01031 | {localize("ui.f2488fd4ef4adbc6",{defaultValue:"Email address"})} | input "email" | {form.email} | true | maxLength={254} | [control](UI_CONTROLS.md#ui-01031) |
| UI-01032 | {localize("ui.187e80ba4d8a22bb",{defaultValue:"Mobile number"})} | input "tel" | {form.mobile} | true | maxLength={25}; pattern="\+?[0-9][0-9 \-]{7,23}" | [control](UI_CONTROLS.md#ui-01032) |
| UI-01034 | {localize("ui.64b4ff26c07d838e",{defaultValue:"Gift name"})} | input  | {form.name} | true | maxLength={160} | [control](UI_CONTROLS.md#ui-01034) |
| UI-01035 | {localize("ui.a06890de939bdcac",{defaultValue:"Product link"})} {method==='DIRECT_DELIVERY'?'':localize("ui.418e319e80da27d9",{defaultValue:" · optional if you add gift details"})} | input "url" | {form.productUrl} | {method==='DIRECT_DELIVERY'} | maxLength={1000}; pattern="https://[^\s]+" | [control](UI_CONTROLS.md#ui-01035) |
| UI-01036 | {localize("ui.f2da19dd48c937be",{defaultValue:"Gift details"})} | textarea  | {form.description} | Not declared | maxLength={500} | [control](UI_CONTROLS.md#ui-01036) |
| UI-01038 |  | input "radio" | {method==='LINK_AND_CONTRIBUTION'} | Not declared |  | [control](UI_CONTROLS.md#ui-01038) |
| UI-01039 |  | input "radio" | {method==='DIRECT_DELIVERY'} | Not declared |  | [control](UI_CONTROLS.md#ui-01039) |
| UI-01040 | {localize("ui.7fc910558a4cc82b",{defaultValue:"Contribution amount (₹)"})} | input "number" | {form.amount} | true | min="1"; max="1000000"; step="0.01" | [control](UI_CONTROLS.md#ui-01040) |
| UI-01044 |  | input "checkbox" | {checked} | true |  | [control](UI_CONTROLS.md#ui-01044) |
| UI-01060 | {localize("ui.fcbb0bc384eed802",{defaultValue:"Amount (₹)"})} | input "number" | {p.amount} | true | min="1"; max="1000000"; step="0.01" | [control](UI_CONTROLS.md#ui-01060) |
| UI-01061 | {localize("ui.2c6b2e253c01fb2e",{defaultValue:"Your name"})} | input  | {p.name} | true | maxLength={200} | [control](UI_CONTROLS.md#ui-01061) |
| UI-01063 | {localize("ui.969ccbd3cf6300ec",{defaultValue:"Email"})} | input "email" | {p.email} | Not declared | maxLength={320} | [control](UI_CONTROLS.md#ui-01063) |
| UI-01064 | {localize("ui.63dceb8800b2dd4a",{defaultValue:"Phone"})} | input "tel" | {p.phone} | Not declared | maxLength={30} | [control](UI_CONTROLS.md#ui-01064) |
| UI-01068 |  | input "radio" | {p.method===method} | Not declared |  | [control](UI_CONTROLS.md#ui-01068) |
| UI-01069 | {localize("ui.41a0811e12b57d34",{defaultValue:"Transaction reference / bank UTR"})} | input  | {p.reference} | true | maxLength={40} | [control](UI_CONTROLS.md#ui-01069) |
| UI-01111 | {localize("ui.bec69036aa27e7fa",{defaultValue:"Sort"})} | select  | {sort} | Not declared |  | [control](UI_CONTROLS.md#ui-01111) |


### Buttons

| ID | Label / expression | Action | Submit validation / handler | Source |
| --- | --- | --- | --- | --- |
| UI-00102 | {localize("ui.942087cc2d41e013",{defaultValue:"Retry"})} | {()=>void refreshProgress()} | Not a native form submit or inherited from component context | [web/app/e/[token]/PublicEventClient.tsx:73](../../../web/app/e/[token]/PublicEventClient.tsx#L73) |
| UI-00105 | {g.contributionEnabled===false?g.contributionDisabledReason:localize("ui.b90c7ad69079591b",{defaultValue:"♡ Contribute with Love"})} | {()=>setSelectedGift(g)} | Not a native form submit or inherited from component context | [web/app/e/[token]/PublicEventClient.tsx:85](../../../web/app/e/[token]/PublicEventClient.tsx#L85) |
| UI-00106 | {localize("ui.76c32149f8bae891",{defaultValue:"♡ Contribute to the Celebration"})} | {()=>setWalletOpen(true)} | Not a native form submit or inherited from component context | [web/app/e/[token]/PublicEventClient.tsx:89](../../../web/app/e/[token]/PublicEventClient.tsx#L89) |
| UI-00497 | {busy?localize("ui.42074396a441a347",{defaultValue:"Processing…"}):offer.amountPaise===0?localize("ui.850fbbaefb4057d0",{defaultValue:"Activate access"}):localize("ui.0e3afe9550a3bde6",{defaultValue:"{{value0}} · {{value1}} (test mode)",value0:(offer.entitled?'Renew':'Purchase'),value1:(commercialMoney(offer.amountPaise))})} | {()=>void pay()} | Not a native form submit or inherited from component context | [web/components/CommercePurchase.tsx:34](../../../web/components/CommercePurchase.tsx#L34) |
| UI-00499 | {localize("ui.087a9cc69283c135",{defaultValue:"Reload price"})} | {()=>{setRetry(v=>v+1)}} | Not a native form submit or inherited from component context | [web/components/CommercePurchase.tsx:34](../../../web/components/CommercePurchase.tsx#L34) |
| UI-00528 | Accept optional analytics | {()=>{saveConsent(true);setShow(false)}} | Not a native form submit or inherited from component context | [web/components/CookieNotice.tsx:5](../../../web/components/CookieNotice.tsx#L5) |
| UI-00529 | Reject optional analytics | {()=>{saveConsent(false);setShow(false)}} | Not a native form submit or inherited from component context | [web/components/CookieNotice.tsx:5](../../../web/components/CookieNotice.tsx#L5) |
| UI-00549 | {localize("ui.ef0edc1b48d46a83",{defaultValue:"View full description"})} | {()=>dialog.current?.showModal()} | Not a native form submit or inherited from component context | [web/components/GiftDescription.tsx:13](../../../web/components/GiftDescription.tsx#L13) |
| UI-00551 | {localize("ui.ad8c41bed4972296",{defaultValue:"Close ×"})} | {()=>dialog.current?.close()} | Not a native form submit or inherited from component context | [web/components/GiftDescription.tsx:13](../../../web/components/GiftDescription.tsx#L13) |
| UI-00553 | {localize("ui.c9bca82b557e962e",{defaultValue:"Newer activity"})} | {()=>setOffset(value=>Math.max(0,value-100))} | Not a native form submit or inherited from component context | [web/components/GroupActivity.tsx:13](../../../web/components/GroupActivity.tsx#L13) |
| UI-00554 | {localize("ui.0eed19abab86afcc",{defaultValue:"Older activity"})} | {()=>setOffset(value=>value+100)} | Not a native form submit or inherited from component context | [web/components/GroupActivity.tsx:13](../../../web/components/GroupActivity.tsx#L13) |
| UI-00557 | {guestGifts?localize("ui.72c7b5708f440207",{defaultValue:"Add a Special Gift"}):giftAvailable?localize("ui.f68ad0a9686950fe",{defaultValue:"Group contribution"}):localize("ui.ee91812bd92cd6d9",{defaultValue:"Gift held with love ♡"})} | {launch} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:88](../../../web/components/GroupContribution.tsx#L88) |
| UI-00559 | × | {()=>void closeAndLock()} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:89](../../../web/components/GroupContribution.tsx#L89) |
| UI-00560 | {localize("ui.48f0d3d397d49f13",{defaultValue:"Sign out"})} | {()=>void logout()} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:90](../../../web/components/GroupContribution.tsx#L90) |
| UI-00563 | {guestGifts?localize("ui.798ca2ce18bf2038",{defaultValue:"Create account"}):localize("ui.1213394682bfa8c6",{defaultValue:"Create Group"})} | {()=>{setTab('register');setError('')}} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:90](../../../web/components/GroupContribution.tsx#L90) |
| UI-00564 | {guestGifts?localize("ui.bfd402b2f6f38125",{defaultValue:"Sign in"}):localize("ui.3d65b17f4fb1f79f",{defaultValue:"Open Group"})} | {()=>{setTab('login');setError('')}} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:90](../../../web/components/GroupContribution.tsx#L90) |
| UI-00577 | {busy?localize("ui.4660a983366f7101",{defaultValue:"Please wait…"}):tab==='register'?localize("ui.6c9206e4b89975a5",{defaultValue:"Register →"}):localize("ui.8637e0051dee66ad",{defaultValue:"Sign in →"})} | submit / component action | {auth} | [web/components/GroupContribution.tsx:90](../../../web/components/GroupContribution.tsx#L90) |
| UI-00578 | {localize("ui.308b88fcc3dffe50",{defaultValue:"Edit name"})} | {()=>{setAdminNameDraft(adminName);setEditingName(true)}} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:90](../../../web/components/GroupContribution.tsx#L90) |
| UI-00581 | {localize("ui.b7297226fd1f4681",{defaultValue:"Save name"})} | submit / component action | {async e=>{e.preventDefault();try{const x=await call('/profile-name',{fullName:adminNameDraft});setAdminName(x.fullName);setEditingName(false);setError('')}catch(x){setError(x instanceof Error?x.message:'Unable to save name')}}} | [web/components/GroupContribution.tsx:90](../../../web/components/GroupContribution.tsx#L90) |
| UI-00582 | {localize("ui.48f0d3d397d49f13",{defaultValue:"Sign out"})} | {()=>void logout()} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:90](../../../web/components/GroupContribution.tsx#L90) |
| UI-00587 | {localize("ui.a70c9f89a0495049",{defaultValue:"Save PIN"})} | submit / component action | {async e=>{e.preventDefault();setPinNotice('');try{await call('/pin/setup',{password:pinPassword,pin,confirmPin});setPinNotice('PIN updated successfully. You can now sign in with your new six-digit PIN.');setPinPassword('');setPin('');setConfirmPin('');setEditingPin(false)}catch(x){setPinNotice(x instanceof Error?x.message:'PIN setup failed')}}} | [web/components/GroupContribution.tsx:90](../../../web/components/GroupContribution.tsx#L90) |
| UI-00589 | {localize("ui.331135fcb9bb6f25",{defaultValue:"1. Receiving account"})} | {()=>setSection('payments')} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:90](../../../web/components/GroupContribution.tsx#L90) |
| UI-00590 | {localize("ui.be5fb8c43cc3df3e",{defaultValue:"2. Enable contributions"})} | {()=>setSection('guest')} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:90](../../../web/components/GroupContribution.tsx#L90) |
| UI-00591 | {localize("ui.6bdf75ffd0fd9e1c",{defaultValue:"3. Invite friends"})} | {()=>setSection('overview')} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:90](../../../web/components/GroupContribution.tsx#L90) |
| UI-00592 | {localize("ui.d4b1ea5708dd5329",{defaultValue:"Overview"})} | {()=>{setSection('overview');void restore()}} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:90](../../../web/components/GroupContribution.tsx#L90) |
| UI-00593 | {localize("ui.3bc6107114c6fa86",{defaultValue:"Privacy & settings"})} | {()=>setSection('privacy')} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:90](../../../web/components/GroupContribution.tsx#L90) |
| UI-00594 | {localize("ui.7f65e0162671c9e9",{defaultValue:"Members & requests"})} | {()=>void loadMembers()} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:90](../../../web/components/GroupContribution.tsx#L90) |
| UI-00595 | {localize("ui.cc64bb493268ea0b",{defaultValue:"Payment accounts"})} | {()=>setSection('payments')} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:90](../../../web/components/GroupContribution.tsx#L90) |
| UI-00596 | {localize("ui.4398ddbea4af82a8",{defaultValue:"♡ My contribution"})} | {()=>setSection('contribute')} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:90](../../../web/components/GroupContribution.tsx#L90) |
| UI-00597 | {localize("ui.fd58f51f50008f89",{defaultValue:"Guest contributions"})} | {()=>setSection('guest')} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:90](../../../web/components/GroupContribution.tsx#L90) |
| UI-00599 | {localize("ui.5c58cd7963cac4e2",{defaultValue:"Copy invite link"})} | {()=>void trackedCopy(`${location.origin}/groups/${token}/${group.id}`)} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:90](../../../web/components/GroupContribution.tsx#L90) |
| UI-00600 | {localize("ui.16f6803a1f4a8a60",{defaultValue:"Invite link unavailable"})} | submit / component action | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:90](../../../web/components/GroupContribution.tsx#L90) |
| UI-00601 | {published?localize("ui.a1ddffdbaeaee49f",{defaultValue:"Collection published"}):localize("ui.a90c8deaaa3deaaf",{defaultValue:"Publish collection to host"})} | {()=>void publishGroup()} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:90](../../../web/components/GroupContribution.tsx#L90) |
| UI-00602 | {localize("ui.6007acbe30b2cd98",{defaultValue:"Approve"})} | {()=>void reviewMember(m.id,'APPROVE')} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:90](../../../web/components/GroupContribution.tsx#L90) |
| UI-00603 | {localize("ui.ab604a360777735f",{defaultValue:"Reject"})} | {()=>void reviewMember(m.id,'REJECT')} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:90](../../../web/components/GroupContribution.tsx#L90) |
| UI-00608 | {busy?localize("ui.23e39291d6135814",{defaultValue:"Saving…"}):localize("ui.cd933d481eefc0f0",{defaultValue:"Save privacy settings"})} | submit / component action | {savePrivacy} | [web/components/GroupContribution.tsx:90](../../../web/components/GroupContribution.tsx#L90) |
| UI-00619 | {busy?localize("ui.c79ed9492e3c1719",{defaultValue:"Creating…"}):localize("ui.5457c9f7d1371da0",{defaultValue:"Create private group →"})} | submit / component action | {setup} | [web/components/GroupContribution.tsx:90](../../../web/components/GroupContribution.tsx#L90) |
| UI-00620 | {d.enabled?localize("ui.9f024ada2282291c",{defaultValue:"Disable account"}):localize("ui.1965185f3488bfcf",{defaultValue:"Enable account"})} | {()=>void run(`/destinations/${d.id}/enabled`,{enabled:!d.enabled})} | Not a native form submit or inherited from component context | [web/components/GroupCreatorPaymentSettings.tsx:24](../../../web/components/GroupCreatorPaymentSettings.tsx#L24) |
| UI-00622 | {localize("ui.b81924056587bf3d",{defaultValue:"Generate QR"})} | {()=>void run(`/destinations/${d.id}/qr/generate`)} | Not a native form submit or inherited from component context | [web/components/GroupCreatorPaymentSettings.tsx:24](../../../web/components/GroupCreatorPaymentSettings.tsx#L24) |
| UI-00623 | {localize("ui.1e4b206d916c7767",{defaultValue:"Preview QR"})} | {()=>void showQr(d.id)} | Not a native form submit or inherited from component context | [web/components/GroupCreatorPaymentSettings.tsx:24](../../../web/components/GroupCreatorPaymentSettings.tsx#L24) |
| UI-00624 | {localize("ui.95e0dcafc3f37df8",{defaultValue:"Make preferred"})} | {()=>void run('/preferred',{destinationId:d.id})} | Not a native form submit or inherited from component context | [web/components/GroupCreatorPaymentSettings.tsx:24](../../../web/components/GroupCreatorPaymentSettings.tsx#L24) |
| UI-00626 | {localize("ui.7d8ab368210c5ae8",{defaultValue:"Close preview"})} | {()=>{URL.revokeObjectURL(preview);setPreview(null)}} | Not a native form submit or inherited from component context | [web/components/GroupCreatorPaymentSettings.tsx:26](../../../web/components/GroupCreatorPaymentSettings.tsx#L26) |
| UI-00634 | {busy?localize("ui.f9a4e6b43543c221",{defaultValue:"Saving securely…"}):localize("ui.4f474d1f3677e9a1",{defaultValue:"Save payment account"})} | submit / component action | {add} | [web/components/GroupCreatorPaymentSettings.tsx:28](../../../web/components/GroupCreatorPaymentSettings.tsx#L28) |
| UI-00638 | {localize("ui.19e86d2b7055340c",{defaultValue:"Send verification code"})} | submit / component action | {start} | [web/components/GroupEmailVerification.tsx:19](../../../web/components/GroupEmailVerification.tsx#L19) |
| UI-00641 | {localize("ui.e89a77e7fe58a6d1",{defaultValue:"Verify email"})} | submit / component action | {finish} | [web/components/GroupEmailVerification.tsx:19](../../../web/components/GroupEmailVerification.tsx#L19) |
| UI-00643 | {localize("ui.b6dc916237a08684",{defaultValue:"Try holding this gift again"})} | {()=>void change(false)} | Not a native form submit or inherited from component context | [web/components/GroupGiftPlan.tsx:11](../../../web/components/GroupGiftPlan.tsx#L11) |
| UI-00644 | {localize("ui.efd4d6bdf9a5a4b3",{defaultValue:"Yes, release gift"})} | {()=>void change(true)} | Not a native form submit or inherited from component context | [web/components/GroupGiftPlan.tsx:11](../../../web/components/GroupGiftPlan.tsx#L11) |
| UI-00645 | {localize("ui.7b9d556ef1178ceb",{defaultValue:"Keep gift"})} | {()=>setConfirmRelease(false)} | Not a native form submit or inherited from component context | [web/components/GroupGiftPlan.tsx:11](../../../web/components/GroupGiftPlan.tsx#L11) |
| UI-00646 | {localize("ui.350000ebff868e58",{defaultValue:"Release gift · keep our collection"})} | {()=>setConfirmRelease(true)} | Not a native form submit or inherited from component context | [web/components/GroupGiftPlan.tsx:11](../../../web/components/GroupGiftPlan.tsx#L11) |
| UI-00648 | {localize("ui.1c8928f0a2a51a6a",{defaultValue:"Add gift to our group"})} | {()=>void change(false)} | Not a native form submit or inherited from component context | [web/components/GroupGiftPlan.tsx:11](../../../web/components/GroupGiftPlan.tsx#L11) |
| UI-00652 | {localize("ui.e382a36729887241",{defaultValue:"Make another contribution"})} | {()=>{setStage('start');setReference('');setSubmissionKey(crypto.randomUUID());setDetails(null)}} | Not a native form submit or inherited from component context | [web/components/GroupGuestCheckout.tsx:29](../../../web/components/GroupGuestCheckout.tsx#L29) |
| UI-00653 | {localize("ui.4c363f056aaf6d63",{defaultValue:"Use suggestion"})} | {()=>setAmount(String(invite.suggested_contribution_paise!/100))} | Not a native form submit or inherited from component context | [web/components/GroupGuestCheckout.tsx:29](../../../web/components/GroupGuestCheckout.tsx#L29) |
| UI-00657 |  | {()=>void choosePayment(d.id)} | Not a native form submit or inherited from component context | [web/components/GroupGuestCheckout.tsx:29](../../../web/components/GroupGuestCheckout.tsx#L29) |
| UI-00659 | {details.kind==='UPI'?localize("ui.78f20d029371d3af",{defaultValue:"Show payment QR"}):localize("ui.00427c578c51e980",{defaultValue:"Show bank details"})} | {()=>setModal(true)} | {submit} | [web/components/GroupGuestCheckout.tsx:29](../../../web/components/GroupGuestCheckout.tsx#L29) |
| UI-00661 | {busy?localize("ui.49195f559e4a9f5b",{defaultValue:"Submitting…"}):localize("ui.6280d7748e32d3ac",{defaultValue:"I have done the payment"})} | submit / component action | {submit} | [web/components/GroupGuestCheckout.tsx:29](../../../web/components/GroupGuestCheckout.tsx#L29) |
| UI-00662 | {localize("ui.6e1b3d4c493ef1e2",{defaultValue:"Change payment method"})} | {()=>{setStage('start');setDetails(null);setSelected('');setReference('')}} | {submit} | [web/components/GroupGuestCheckout.tsx:29](../../../web/components/GroupGuestCheckout.tsx#L29) |
| UI-00663 | {localize("ui.0ab01a15c8871a3a",{defaultValue:"Copy bank details"})} | {()=>void trackedCopy(`${details.accountHolder}\n${details.accountNumber}\n${details.ifsc}`)} | Not a native form submit or inherited from component context | [web/components/GroupGuestCheckout.tsx:31](../../../web/components/GroupGuestCheckout.tsx#L31) |
| UI-00664 | {localize("ui.3866ccf908cc968d",{defaultValue:"Continue to reference details →"})} | {()=>setModal(false)} | Not a native form submit or inherited from component context | [web/components/GroupGuestCheckout.tsx:31](../../../web/components/GroupGuestCheckout.tsx#L31) |
| UI-00671 | {localize("ui.8f4dbb94585444b6",{defaultValue:"Save guest contribution settings"})} | submit / component action | {save} | [web/components/GroupGuestCreatorSettings.tsx:18](../../../web/components/GroupGuestCreatorSettings.tsx#L18) |
| UI-00686 | {localize("ui.a89376d87b358fce",{defaultValue:" Contribute"})} | {onContribute} | Not a native form submit or inherited from component context | [web/components/GuestActions.tsx:27](../../../web/components/GuestActions.tsx#L27) |
| UI-00688 | {localize("ui.fc40890ffdf9eb33",{defaultValue:" Save the date"})} | {saveDate} | Not a native form submit or inherited from component context | [web/components/GuestActions.tsx:27](../../../web/components/GuestActions.tsx#L27) |
| UI-00696 | {busy?localize("ui.b8ed5279e897be5d",{defaultValue:"Sending…"}):localize("ui.8536c15071befcf6",{defaultValue:"Send my RSVP →"})} | submit / component action | {submitRsvp} | [web/components/GuestCelebration.tsx:33](../../../web/components/GuestCelebration.tsx#L33) |
| UI-00702 | {localize("ui.c3812fc4acb861d5",{defaultValue:"Remove"})} | {()=>setPhoto(null)} | {submitWish} | [web/components/GuestCelebration.tsx:36](../../../web/components/GuestCelebration.tsx#L36) |
| UI-00703 | {busy?localize("ui.b8ed5279e897be5d",{defaultValue:"Sending…"}):localize("ui.1cae14f8ae4d97a2",{defaultValue:"Send my wishes ♡"})} | submit / component action | {submitWish} | [web/components/GuestCelebration.tsx:36](../../../web/components/GuestCelebration.tsx#L36) |
| UI-00705 | {localize("ui.710ae767f5e40eed",{defaultValue:"↻ Refresh wishes"})} | {()=>void refreshWishes()} | Not a native form submit or inherited from component context | [web/components/GuestCelebration.tsx:39](../../../web/components/GuestCelebration.tsx#L39) |
| UI-00706 | {showWishes?localize("ui.8fbc18362c024b5d",{defaultValue:"Show fewer wishes"}):localize("ui.99fabb2263ee65ee",{defaultValue:"View all wishes"})} | {()=>setShowWishes(v=>!v)} | Not a native form submit or inherited from component context | [web/components/GuestCelebration.tsx:40](../../../web/components/GuestCelebration.tsx#L40) |
| UI-00708 | {localize("ui.5b987d1a4ee6d349",{defaultValue:"Refresh fee"})} | {()=>{requestKey.current='';setPricingRetry(v=>v+1)}} | Not a native form submit or inherited from component context | [web/components/GuestContributionCheckout.tsx:117](../../../web/components/GuestContributionCheckout.tsx#L117) |
| UI-00711 | {localize("ui.942087cc2d41e013",{defaultValue:"Retry"})} | {()=>setDestinationRetry(v=>v+1)} | Not a native form submit or inherited from component context | [web/components/GuestContributionCheckout.tsx:140](../../../web/components/GuestContributionCheckout.tsx#L140) |
| UI-00712 | ✕ | {onClose} | Not a native form submit or inherited from component context | [web/components/GuestContributionCheckout.tsx:142](../../../web/components/GuestContributionCheckout.tsx#L142) |
| UI-00714 | {localize("ui.e382a36729887241",{defaultValue:"Make another contribution"})} | {()=>{setDirectStatus('');setTransferReference('');requestKey.current='';}} | Not a native form submit or inherited from component context | [web/components/GuestContributionCheckout.tsx:149](../../../web/components/GuestContributionCheckout.tsx#L149) |
| UI-00716 | {localize("ui.e382a36729887241",{defaultValue:"Make another contribution"})} | {()=>{setReceipt(null);requestKey.current='';setDirectStatus('');}} | Not a native form submit or inherited from component context | [web/components/GuestContributionCheckout.tsx:150](../../../web/components/GuestContributionCheckout.tsx#L150) |
| UI-00720 | {localize("ui.942087cc2d41e013",{defaultValue:"Retry"})} | {()=>setDestinationRetry(v=>v+1)} | {submit} | [web/components/GuestContributionCheckout.tsx:153](../../../web/components/GuestContributionCheckout.tsx#L153) |
| UI-00726 | {showContact?localize("ui.42485bdfe008e57e",{defaultValue:"− Hide optional contact details"}):localize("ui.958860832f4ffd8d",{defaultValue:"+ Add email or phone (optional)"})} | {()=>setShowContact(v=>!v)} | {submit} | [web/components/GuestContributionCheckout.tsx:164](../../../web/components/GuestContributionCheckout.tsx#L164) |
| UI-00731 | {localize("ui.6280d7748e32d3ac",{defaultValue:"I have done the payment"})} | {reportDirectPayment} | {submit} | [web/components/GuestContributionCheckout.tsx:174](../../../web/components/GuestContributionCheckout.tsx#L174) |
| UI-00733 | {busy?localize("ui.42074396a441a347",{defaultValue:"Processing…"}):localize("ui.0999a2d14c3594b1",{defaultValue:"Pay {{value0}} (Test Mode)",value0:(validAmount&&pricing?money(pricing.totalPaise):'')})} | submit / component action | {submit} | [web/components/GuestContributionCheckout.tsx:182](../../../web/components/GuestContributionCheckout.tsx#L182) |
| UI-00735 | {localize("ui.ab7ec41a34509538",{defaultValue:"Refresh gifts"})} | {()=>void load()} | Not a native form submit or inherited from component context | [web/components/GuestGiftBoard.tsx:14](../../../web/components/GuestGiftBoard.tsx#L14) |
| UI-00738 | {localize("ui.d8b8392e2c542950",{defaultValue:"Try again"})} | {()=>void load()} | Not a native form submit or inherited from component context | [web/components/GuestGiftBoard.tsx:14](../../../web/components/GuestGiftBoard.tsx#L14) |
| UI-00748 | {localize("ui.6ed0c15cca5b0b31",{defaultValue:"Add gift"})} | submit / component action | {add} | [web/components/GuestGiftManager.tsx:19](../../../web/components/GuestGiftManager.tsx#L19) |
| UI-00754 | {localize("ui.33f13d183c65606b",{defaultValue:"View delivery address"})} | {async()=>{setError('');setBusy(true);try{const address=await call(`/guest-gifts/${g.id}/delivery-address`);setAddresses(value=>({...value,[g.id]:address}))}catch(e){setError(e instanceof Error?e.message:'Could not load delivery address')}finally{setBusy(false)}}} | Not a native form submit or inherited from component context | [web/components/GuestGiftManager.tsx:19](../../../web/components/GuestGiftManager.tsx#L19) |
| UI-00755 | {localize("ui.672313aecf0e8f63",{defaultValue:"Publish gift to host"})} | {()=>void mutate(`/guest-gifts/${g.id}/publish`,{},'Gift published. The host can now see it in their contribution report.')} | Not a native form submit or inherited from component context | [web/components/GuestGiftManager.tsx:19](../../../web/components/GuestGiftManager.tsx#L19) |
| UI-00756 | {g.disabled?g.selection_label:type==='GROUP'?localize("ui.1fb7dd42a81e416b",{defaultValue:"Select for our group"}):localize("ui.54f1cbbbca8e29a8",{defaultValue:"Select this gift"})} | {()=>void mutate(`/guest-gifts/${g.id}/select`,{contributionType:type,...(type==='GROUP'?{groupId:group!.id}:{})},'Gift selected. You can now view the delivery address and publish when ready.')} | Not a native form submit or inherited from component context | [web/components/GuestGiftManager.tsx:19](../../../web/components/GuestGiftManager.tsx#L19) |
| UI-00759 | {localize("ui.942087cc2d41e013",{defaultValue:"Retry"})} | {()=>setRetry(v=>v+1)} | Not a native form submit or inherited from component context | [web/components/GuestVerifiedDestination.tsx:40](../../../web/components/GuestVerifiedDestination.tsx#L40) |
| UI-00764 | {localize("ui.56cab81c15312e7c",{defaultValue:"Copy UPI ID"})} | {()=>trackedCopy(selected.upiId\|\|'').then(()=>setCopyNotice('UPI ID copied.')).catch(()=>setCopyNotice('Select the UPI ID above to copy it.'))} | Not a native form submit or inherited from component context | [web/components/GuestVerifiedDestination.tsx:48](../../../web/components/GuestVerifiedDestination.tsx#L48) |
| UI-00766 | {localize("ui.a3c4e7c1a522f3dc",{defaultValue:"Show UPI QR"})} | {()=>setQrOpen(true)} | Not a native form submit or inherited from component context | [web/components/GuestVerifiedDestination.tsx:50](../../../web/components/GuestVerifiedDestination.tsx#L50) |
| UI-00768 | {localize("ui.e61997c6997c5afa",{defaultValue:"✕ Close"})} | {()=>setQrOpen(false)} | Not a native form submit or inherited from component context | [web/components/GuestVerifiedDestination.tsx:53](../../../web/components/GuestVerifiedDestination.tsx#L53) |
| UI-00788 | {collapsed?<PanelLeftOpen size={20}/>:<PanelLeftClose size={20}/>} | {toggle} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:50](../../../web/components/HostWorkspace.tsx#L50) |
| UI-00791 |  | {()=>void signOut()} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:53](../../../web/components/HostWorkspace.tsx#L53) |
| UI-00792 |  | {()=>setCollapsed(true)} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:54](../../../web/components/HostWorkspace.tsx#L54) |
| UI-00795 | {localize("ui.4b885106f27353d2",{defaultValue:"Fill gift details from link"})} | {()=>void lookup()} | Not a native form submit or inherited from component context | [web/components/IndependentGroupPlan.tsx:20](../../../web/components/IndependentGroupPlan.tsx#L20) |
| UI-00801 | {localize("ui.1ec42b70e129b073",{defaultValue:"Save our gift plan"})} | submit / component action | {save} | [web/components/IndependentGroupPlan.tsx:26](../../../web/components/IndependentGroupPlan.tsx#L26) |
| UI-00809 | {busy?localize("ui.72dd4b7eecd15686",{defaultValue:"Sharing payment details…"}):localize("ui.72f61dfd8fd1ae98",{defaultValue:"I’ve paid — share details with the host"})} | submit / component action | {submit} | [web/components/IndividualHostContribution.tsx:28](../../../web/components/IndividualHostContribution.tsx#L28) |
| UI-00810 | {localize("ui.d8b8392e2c542950",{defaultValue:"Try again"})} | {()=>setRetry(v=>v+1)} | Not a native form submit or inherited from component context | [web/components/IndividualHostContribution.tsx:28](../../../web/components/IndividualHostContribution.tsx#L28) |
| UI-00876 | Cookie preferences | {()=>window.dispatchEvent(new Event('giftwise-cookie-settings'))} | Not a native form submit or inherited from component context | [web/components/LegalFooter.tsx:4](../../../web/components/LegalFooter.tsx#L4) |
| UI-00929 | {localize("ui.c76f29ae1543f068",{defaultValue:"Use a different mobile number"})} | {()=>{setVerified(false);setMessage('')}} | Not a native form submit or inherited from component context | [web/components/MobileVerification.tsx:17](../../../web/components/MobileVerification.tsx#L17) |
| UI-00933 | {busy?localize("ui.4660a983366f7101",{defaultValue:"Please wait…"}):challenge?localize("ui.6d8e8cd5ef890b1d",{defaultValue:"Verify mobile"}):localize("ui.19e86d2b7055340c",{defaultValue:"Send verification code"})} | submit / component action | {submit} | [web/components/MobileVerification.tsx:17](../../../web/components/MobileVerification.tsx#L17) |
| UI-00934 | {localize("ui.7fd05b1a433fd9c4",{defaultValue:"Change number / request another code"})} | {()=>{setChallenge('');setCode('');setMessage('Wait at least 60 seconds before requesting another code.')}} | {submit} | [web/components/MobileVerification.tsx:17](../../../web/components/MobileVerification.tsx#L17) |
| UI-01006 | {localize("ui.517e0d761f33fe3b",{defaultValue:" Share invitation"})} | {()=>void share()} | Not a native form submit or inherited from component context | [web/components/SocialSharing.tsx:18](../../../web/components/SocialSharing.tsx#L18) |
| UI-01010 | {copied?<Check size={16}/>:<Copy size={16}/>} {copied?localize("ui.8d525e5f158b9afe",{defaultValue:"Copied"}):localize("ui.dbf362d4f210c780",{defaultValue:"Copy link"})} | {()=>void copy()} | Not a native form submit or inherited from component context | [web/components/SocialSharing.tsx:18](../../../web/components/SocialSharing.tsx#L18) |
| UI-01013 | {localize("ui.72c7b5708f440207",{defaultValue:"Add a Special Gift"})} | {()=>{setStep('choice');setError('')}} | Not a native form submit or inherited from component context | [web/components/SpecialGiftEntry.tsx:67](../../../web/components/SpecialGiftEntry.tsx#L67) |
| UI-01015 | × | {close} | Not a native form submit or inherited from component context | [web/components/SpecialGiftEntry.tsx:70](../../../web/components/SpecialGiftEntry.tsx#L70) |
| UI-01016 |  | {()=>void individual()} | Not a native form submit or inherited from component context | [web/components/SpecialGiftEntry.tsx:71](../../../web/components/SpecialGiftEntry.tsx#L71) |
| UI-01017 |  | {()=>{setError('');setStep('group')}} | Not a native form submit or inherited from component context | [web/components/SpecialGiftEntry.tsx:71](../../../web/components/SpecialGiftEntry.tsx#L71) |
| UI-01018 | {localize("ui.2f60681f8b63e96a",{defaultValue:"← Giving options"})} | {()=>setStep('choice')} | Not a native form submit or inherited from component context | [web/components/SpecialGiftEntry.tsx:72](../../../web/components/SpecialGiftEntry.tsx#L72) |
| UI-01021 | {localize("ui.0e1504cb20baa605",{defaultValue:"View host delivery address"})} | {async()=>{setBusy(true);setError('');try{await showAddress(saved.id)}catch(e){setError(e instanceof Error?e.message:'Address unavailable')}finally{setBusy(false)}}} | Not a native form submit or inherited from component context | [web/components/SpecialGiftEntry.tsx:73](../../../web/components/SpecialGiftEntry.tsx#L73) |
| UI-01022 | {saved.delivery_status==='GIFT_PURCHASED'\|\|saved.delivery_status==='TRACKING_SHARED'?localize("ui.e20ac06e7a6a8b8e",{defaultValue:"Gift purchased"}):localize("ui.9c788397748c304a",{defaultValue:"I have purchased the gift"})} | {()=>void updateDelivery('GIFT_PURCHASED')} | Not a native form submit or inherited from component context | [web/components/SpecialGiftEntry.tsx:73](../../../web/components/SpecialGiftEntry.tsx#L73) |
| UI-01025 | {localize("ui.15858678da15e73d",{defaultValue:"Share tracking with host"})} | submit / component action | {e=>{e.preventDefault();void updateDelivery('TRACKING_SHARED')}} | [web/components/SpecialGiftEntry.tsx:73](../../../web/components/SpecialGiftEntry.tsx#L73) |
| UI-01026 | {localize("ui.dabf10e2128f6ebd",{defaultValue:"Add another special gift"})} | {()=>{setSaved(null);setSource('');setForm(empty);setAddress(null);setTracking('');setNotice('');void individual()}} | Not a native form submit or inherited from component context | [web/components/SpecialGiftEntry.tsx:73](../../../web/components/SpecialGiftEntry.tsx#L73) |
| UI-01041 | {busy?localize("ui.2e3b4a7d1f13e7c3",{defaultValue:"Saving your gift…"}):method==='LINK_AND_CONTRIBUTION'?localize("ui.75bda07643c2766a",{defaultValue:"Continue to host payment"}):localize("ui.0580e20a82366727",{defaultValue:"Add my special gift"})} | submit / component action | {save} | [web/components/SpecialGiftEntry.tsx:79](../../../web/components/SpecialGiftEntry.tsx#L79) |
| UI-01042 | {localize("ui.d1f3b7c8d7b5ce7f",{defaultValue:"Manage "})} {g.name} | {()=>{setSaved(g);setAddress(null);setTracking('');setNotice('')}} | Not a native form submit or inherited from component context | [web/components/SpecialGiftEntry.tsx:81](../../../web/components/SpecialGiftEntry.tsx#L81) |
| UI-01055 | ✕ | {p.close} | Not a native form submit or inherited from component context | [web/components/focused/Checkout.tsx:13](../../../web/components/focused/Checkout.tsx#L13) |
| UI-01057 | {localize("ui.5aca8cea2b1e0071",{defaultValue:"Back to the celebration"})} | {p.close} | Not a native form submit or inherited from component context | [web/components/focused/Checkout.tsx:13](../../../web/components/focused/Checkout.tsx#L13) |
| UI-01059 | ₹ {Number(value).toLocaleString(activeLocale("en-IN"))} | {()=>p.setAmount(value)} | {e=>{if(step===0){e.preventDefault();if(p.valid&&p.name.trim())setStep(1)}else p.submit(e)}} | [web/components/focused/Checkout.tsx:13](../../../web/components/focused/Checkout.tsx#L13) |
| UI-01062 | {contact?localize("ui.3eed0166e7e7164d",{defaultValue:"Hide optional contact details"}):localize("ui.a4087e859882a5ba",{defaultValue:"+ Add email or phone · optional"})} | {()=>setContact(v=>!v)} | {e=>{if(step===0){e.preventDefault();if(p.valid&&p.name.trim())setStep(1)}else p.submit(e)}} | [web/components/focused/Checkout.tsx:13](../../../web/components/focused/Checkout.tsx#L13) |
| UI-01065 | {localize("ui.496267ccc6b01605",{defaultValue:"Continue →"})} | submit / component action | {e=>{if(step===0){e.preventDefault();if(p.valid&&p.name.trim())setStep(1)}else p.submit(e)}} | [web/components/focused/Checkout.tsx:13](../../../web/components/focused/Checkout.tsx#L13) |
| UI-01066 | {localize("ui.422db74b1f68a344",{defaultValue:"Edit amount"})} | {()=>setStep(0)} | {e=>{if(step===0){e.preventDefault();if(p.valid&&p.name.trim())setStep(1)}else p.submit(e)}} | [web/components/focused/Checkout.tsx:13](../../../web/components/focused/Checkout.tsx#L13) |
| UI-01070 | {p.busy?localize("ui.8d15b0fbf84af930",{defaultValue:"Opening secure checkout…"}):localize("ui.78ce67147916e011",{defaultValue:"Pay {{value0}} · Test mode",value0:(inr(p.totalPaise??amount))})} | submit / component action | {e=>{if(step===0){e.preventDefault();if(p.valid&&p.name.trim())setStep(1)}else p.submit(e)}} | [web/components/focused/Checkout.tsx:13](../../../web/components/focused/Checkout.tsx#L13) |
| UI-01071 | {p.busy?localize("ui.404bb57ce1e21631",{defaultValue:"Sending details…"}):localize("ui.7521686e8b908e2e",{defaultValue:"Submit transfer details"})} | {p.report} | {e=>{if(step===0){e.preventDefault();if(p.valid&&p.name.trim())setStep(1)}else p.submit(e)}} | [web/components/focused/Checkout.tsx:13](../../../web/components/focused/Checkout.tsx#L13) |
| UI-01113 | {localize("ui.d8b8392e2c542950",{defaultValue:"Try again"})} | {onRetry} | Not a native form submit or inherited from component context | [web/components/focused/Invitation.tsx:36](../../../web/components/focused/Invitation.tsx#L36) |
| UI-01115 | {(g.contributionEnabled===false)?g.contributionDisabledReason\|\|localize("ui.f111404f214c5080",{defaultValue:"A lovely surprise is on its way ♡"}):preview?localize("ui.aa35c713806f8aac",{defaultValue:"Contribution preview only"}):localize("ui.97b0c61b99f65f9e",{defaultValue:"Contribute"})} | {()=>open(g)} | Not a native form submit or inherited from component context | [web/components/focused/Invitation.tsx:36](../../../web/components/focused/Invitation.tsx#L36) |
| UI-01116 | {localize("ui.eaed0473cc93222a",{defaultValue:"Choose an amount"})} | {()=>open(null)} | Not a native form submit or inherited from component context | [web/components/focused/Invitation.tsx:36](../../../web/components/focused/Invitation.tsx#L36) |
| UI-01118 | {localize("ui.d8b8392e2c542950",{defaultValue:"Try again"})} | {()=>location.reload()} | Not a native form submit or inherited from component context | [web/components/focused/Invitation.tsx:41](../../../web/components/focused/Invitation.tsx#L41) |


### Dialog and alert/status inventory

| ID | Role / kind | Text / label | Visibility conditions | Source |
| --- | --- | --- | --- | --- |
| UI-00107 | "dialog" | {selectedGift&&<div className="loveModalGift"><h2>{selectedGift.name}</h2><p>{selectedGift.description}</p><div className="loveModalMetrics"><span>{localize("ui.8c87fd59d8a1da53",{defaultValue:"Gift Value: "})}{rupees(selectedGift.targetAmount)}</span><span>{localize("ui.ca343b5a42406c14",{defaultValue:"Contributions Received: "})}{progressValue(rupees((progress[selectedGift.id]?.contributedPaise??0)/100))}</span><span>{localize("ui.a471bb9c4511213c",{defaultValue:"Still to Be Gifted: "})}{progressValue(rupees(Math.max(0,selectedGift.targetAmount-(progress[selectedGift.id]?.contributedPaise??0)/100)))}</span><span>{localize("ui.06083acc7cce254b",{defaultValue:"Contributions Made: "})}{progressValue(progress[selectedGift.id]?.contributionCount??0)}</span></div></div>} | (selectedGift\|\|walletOpen) | [web/app/e/[token]/PublicEventClient.tsx:90](../../../web/app/e/[token]/PublicEventClient.tsx#L90) |
| UI-00550 | dialog |  | Parent/source state | [web/components/GiftDescription.tsx:13](../../../web/components/GiftDescription.tsx#L13) |
| UI-00558 | "dialog" | {logged&&guestGifts&&<><div className="gc-dash-head"><h2>{localize("ui.8256358119aa18be",{defaultValue:"Your guest account"})}</h2>{!group&&<button type="button" disabled={busy} onClick={()=>void logout()}>{localize("ui.48f0d3d397d49f13",{defaultValue:"Sign out"})}</button>}</div><GuestGiftManager token={token} call={call} group={group}/></>} {logged&&accountNotice&&<p role="status">{accountNotice}</p>} {logged&&(emailVerificationEnabled\|\|mobileOtpEnabled)&&<details className="gc-panel"><summary>{localize("ui.d3e0dc0fef4e6514",{defaultValue:"Account · contact verification"})}</summary><GroupEmailVerification token={token} pageId={pageId.current!} onVerified={()=>void refreshPlan()}/><MobileVerification token={token} pageId={pageId.current!} onVerified={()=>void restore()}/></details>} {error&&<p className="gc-error gc-sticky-feedback" role="alert">{localizeText(error)}</p>} {loading?<p>{localize("ui.82b3dda5006dbb6c",{defaultValue:"Opening your group…"})}</p>:!logged?<><div className="gc-auth-heading"><span className="gc-auth-icon" aria-hidden="true">♧</span><h2>{guestGifts?localize("ui.03eacf45d0c7ecb2",{defaultValue:"A thoughtful gift from you."}):localize("ui.fad5131ccbf85a5a",{defaultValue:"A thoughtful gift, together."})}</h2><p>{guestGifts?localize("ui.f61892905889c364",{defaultValue:"Sign in or create an account to manage an individual gift or organize a group gift."}):localize("ui.74e05d026c54ca8a",{defaultValue:"Create a group or sign in to organize your people."})}</p></div><div className="gc-tabs" role="tablist"><button type="button" aria-selected={tab==='register'} role="tab" onClick={()=>{setTab('register');setError('')}}>{guestGifts?localize("ui.798ca2ce18bf2038",{defaultValue:"Create account"}):localize("ui.1213394682bfa8c6",{defaultValue:"Create Group"})}</button><button type="button" aria-selected={tab==='login'} role="tab" onClick={()=>{setTab('login');setError('')}}>{guestGifts?localize("ui.bfd402b2f6f38125",{defaultValue:"Sign in"}):localize("ui.3d65b17f4fb1f79f",{defaultValue:"Open Group"})}</button></div>{emailVerificationEnabled&&<div className="recovery-links"><a href={`/forgot-username?kind=GROUP_ADMIN&eventToken=${encodeURIComponent(token)}&returnTo=${encodeURIComponent('/e/'+token)}`}>{localize("ui.91d00522b02cc29d",{defaultValue:"Forgot username?"})}</a><a href={`/forgot-password?kind=GROUP_ADMIN&returnTo=${encodeURIComponent('/e/'+token)}`}>{localize("ui.6dbd52ba198f47cc",{defaultValue:"Forgot password or PIN?"})}</a></div>}<form className={`gc-form gc-auth-form ${tab==='register'?'gc-register-grid':''}`} onSubmit={auth}>{tab==='register'&&<><SignupEmailField value={registrationEmail} onChange={setRegistrationEmail}/><label>{localize("ui.187e80ba4d8a22bb",{defaultValue:"Mobile number"})}<input required type="tel" autoComplete="tel" placeholder="+919876543210" value={registrationMobile} onChange={e=>setRegistrationMobile(e.target.value)}/></label><p>{localize("ui.b8c79ea3a5538c19",{defaultValue:"Use your own email and mobile for account recovery."})}</p></>}{tab==='register'&&<label>{localize("ui.f13a64ba2fea87ef",{defaultValue:"Full name"})}<input required minLength={2} maxLength={120} value={fullName} onChange={e=>setFullName(e.target.value)}/></label>}{<label>{localize("ui.e3b89e9d33f88e52",{defaultValue:"Username"})}<input required minLength={tab==='register'?4:undefined} maxLength={tab==='register'?30:undefined} pattern={tab==='register'?'[A-Za-z0-9][A-Za-z0-9_]{3,29}':undefined} aria-describedby={tab==='register'?'group-username-rules':undefined} autoComplete="username" autoCapitalize="none" spellCheck={false} value={username} onChange={e=>setUsername(e.target.value)}/></label>}{tab==='register'&&<CredentialRequirements kind="username" value={username} id="group-username-rules"/>}{tab==='login'&&<label>{localize("ui.11aaa14268df51b7",{defaultValue:"Sign in with"})}<select value={loginMethod} onChange={e=>setLoginMethod(e.target.value as 'password'\|'pin')}>{allowPin&&<option value="pin">{localize("ui.cb6246bd8eb1937b",{defaultValue:"Username and six-digit PIN"})}</option>}<option value="password">{localize("ui.e3db92128adfe5d4",{defaultValue:"Username and password"})}</option></select></label>}{(tab==='register'\|\|loginMethod==='password')&&<label>{localize("ui.e7cf3ef4f17c3999",{defaultValue:"Password"})}<input required minLength={tab==='register'?8:undefined} aria-describedby={tab==='register'?'group-password-rules':undefined} type="password" autoComplete={tab==='register'?'new-password':'current-password'} value={password} onChange={e=>setPassword(e.target.value)}/></label>}{tab==='register'&&<><CredentialRequirements kind="password" value={password} id="group-password-rules"/><TermsAcceptance checked={termsAccepted} onChange={setTermsAccepted}/></>}{tab==='register'&&<label>{localize("ui.5ac265f396a2c586",{defaultValue:"Confirm password"})}<input required type="password" minLength={8} value={confirm} onChange={e=>setConfirm(e.target.value)}/></label>}{tab==='register'&&allowPin&&<><label>{localize("ui.61c2b9f2c16ea005",{defaultValue:"Six-digit PIN"})}<input required type="password" inputMode="numeric" pattern="[0-9]{6}" minLength={6} maxLength={6} value={pin} onChange={e=>setPin(e.target.value)}/></label><label>{localize("ui.4fa10c7baa0c1d89",{defaultValue:"Confirm PIN"})}<input required type="password" inputMode="numeric" pattern="[0-9]{6}" minLength={6} maxLength={6} value={confirmPin} onChange={e=>setConfirmPin(e.target.value)}/></label></>}{tab==='login'&&loginMethod==='pin'&&<label>{localize("ui.61c2b9f2c16ea005",{defaultValue:"Six-digit PIN"})}<input required type="password" inputMode="numeric" pattern="[0-9]{6}" minLength={6} maxLength={6} value={pin} onChange={e=>setPin(e.target.value)}/></label>}<button disabled={busy\|\|allowPin===null\|\|(tab==='register'&&!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(registrationEmail.trim()))} className="gc-launch">{busy?localize("ui.4660a983366f7101",{defaultValue:"Please wait…"}):tab==='register'?localize("ui.6c9206e4b89975a5",{defaultValue:"Register →"}):localize("ui.8637e0051dee66ad",{defaultValue:"Sign in →"})}</button></form></>:group?<>{!published&&<section className="gc-panel"><h3>{localize("ui.91742f59a9aebbcc",{defaultValue:"Group coordination fee"})}</h3><p>{localize("ui.4c809ce0c8348bf6",{defaultValue:"A one-time fee paid by the creator. Member transfers still go directly to the creator. If the fee is switched off, publish without a purchase."})}</p><CommercePurchase stream="GROUP_GIFT" resourceId={group.id} groupBase={`/api/public/groups/${token}/coordination/${group.id}`} pageId={pageId.current!} onComplete={()=>void restore()}/></section>}<details className="gc-panel gc-profile"><summary>{localize("ui.9c9a74b827864251",{defaultValue:"Your profile · "})}{adminName\|\|localize("ui.715a9cc0c34c96c1",{defaultValue:"Organizer"})}</summary>{adminName&&!editingName?<><h3>{localize("ui.0c7229ae1986eb6d",{defaultValue:"Welcome back, "})}{adminName}!</h3><button type="button" className="gc-secondary" onClick={()=>{setAdminNameDraft(adminName);setEditingName(true)}}>{localize("ui.308b88fcc3dffe50",{defaultValue:"Edit name"})}</button></>:<form className="gc-form" onSubmit={async e=>{e.preventDefault();try{const x=await call('/profile-name',{fullName:adminNameDraft});setAdminName(x.fullName);setEditingName(false);setError('')}catch(x){setError(x instanceof Error?x.message:'Unable to save name')}}}><label>{localize("ui.d82e5fe24eed3db4",{defaultValue:"What should we call you?"})}<input required minLength={2} maxLength={120} value={adminNameDraft} onChange={e=>setAdminNameDraft(e.target.value)}/></label><button className="gc-secondary">{localize("ui.b7297226fd1f4681",{defaultValue:"Save name"})}</button></form>}</details><div className="gc-dash-head"><div><h2>{group.name}</h2><p>{localize("ui.b2742e6cae248f51",{defaultValue:"Created by "})}{group.creatorName}</p></div><div className="gc-dash-actions"><span className="gc-status">{independent?localize("ui.a2d472a9d7005732",{defaultValue:"Group created ♡"}):group.status==='DRAFT'?localize("ui.da71fe2468d51ec1",{defaultValue:"Private draft"}):group.status}</span><button type="button" className="gc-secondary" disabled={busy} onClick={()=>void logout()}>{localize("ui.48f0d3d397d49f13",{defaultValue:"Sign out"})}</button></div></div>{allowPin&&<details className="gc-panel" open={editingPin} onToggle={e=>setEditingPin(e.currentTarget.open)}><summary>{localize("ui.725e855de4090424",{defaultValue:"Account security (change PIN)"})}</summary><form className="gc-form" onSubmit={async e=>{e.preventDefault();setPinNotice('');try{await call('/pin/setup',{password:pinPassword,pin,confirmPin});setPinNotice('PIN updated successfully. You can now sign in with your new six-digit PIN.');setPinPassword('');setPin('');setConfirmPin('');setEditingPin(false)}catch(x){setPinNotice(x instanceof Error?x.message:'PIN setup failed')}}}><label>{localize("ui.72ed2bd767cef627",{defaultValue:"Current password"})}<input required type="password" value={pinPassword} onChange={e=>setPinPassword(e.target.value)}/></label><label>{localize("ui.61c2b9f2c16ea005",{defaultValue:"Six-digit PIN"})}<input required type="password" inputMode="numeric" pattern="[0-9]{6}" maxLength={6} value={pin} onChange={e=>setPin(e.target.value)}/></label><label>{localize("ui.4fa10c7baa0c1d89",{defaultValue:"Confirm PIN"})}<input required type="password" inputMode="numeric" pattern="[0-9]{6}" maxLength={6} value={confirmPin} onChange={e=>setConfirmPin(e.target.value)}/></label><button className="gc-secondary">{localize("ui.a70c9f89a0495049",{defaultValue:"Save PIN"})}</button>{pinNotice&&<p role="status">{pinNotice}</p>}</form></details>}<div className="gc-metrics"><div><small>{localize("ui.40195ce9e297eb0d",{defaultValue:"Gift goal"})}</small><strong>{group.targetPaise!=null?localize("ui.b3da1e4c465e3370",{defaultValue:"₹{{value0}}",value0:((group.targetPaise/100).toLocaleString(activeLocale("en-IN")))}):localize("ui.4895f73177ab5d67",{defaultValue:"Not set"})}</strong></div><div><small>{localize("ui.1044a4c056d0d685",{defaultValue:"Members"})}</small><strong>{summary?.member_count??'—'}</strong></div><div><small>{localize("ui.7eeed9ef9dbcff95",{defaultValue:"Reported transfers — unverified"})}</small><strong>{summary?localize("ui.b3da1e4c465e3370",{defaultValue:"₹{{value0}}",value0:((Number(summary.reported_paise)/100).toLocaleString(activeLocale("en-IN")))}):'—'}</strong></div></div>{independent&&<section className="gc-panel"><h3>{localize("ui.75b8cccda0da7123",{defaultValue:"Your group is cooking up something special 🍳"})}</h3><p>{localize("ui.f1cdc295845ac415",{defaultValue:"Your group is now listed on the invitation. Add a receiving account, enable contributions, then share your group link. Friends can give with or without signing in."})}</p><button type="button" className="gc-secondary" onClick={()=>setSection('payments')}>{localize("ui.331135fcb9bb6f25",{defaultValue:"1. Receiving account"})}</button> <button type="button" className="gc-secondary" onClick={()=>setSection('guest')}>{localize("ui.be5fb8c43cc3df3e",{defaultValue:"2. Enable contributions"})}</button> <button type="button" className="gc-secondary" onClick={()=>setSection('overview')}>{localize("ui.6bdf75ffd0fd9e1c",{defaultValue:"3. Invite friends"})}</button></section>}<div className="gc-nav"><button type="button" aria-pressed={section==='overview'} onClick={()=>{setSection('overview');void restore()}}>{localize("ui.d4b1ea5708dd5329",{defaultValue:"Overview"})}</button><button type="button" aria-pressed={section==='privacy'} onClick={()=>setSection('privacy')}>{localize("ui.3bc6107114c6fa86",{defaultValue:"Privacy & settings"})}</button><button type="button" aria-pressed={section==='members'} onClick={()=>void loadMembers()}>{localize("ui.7f65e0162671c9e9",{defaultValue:"Members & requests"})}</button><button type="button" aria-pressed={section==='payments'} onClick={()=>setSection('payments')}>{localize("ui.cc64bb493268ea0b",{defaultValue:"Payment accounts"})}</button><button type="button" aria-pressed={section==='contribute'} onClick={()=>setSection('contribute')}>{localize("ui.4398ddbea4af82a8",{defaultValue:"♡ My contribution"})}</button><button type="button" aria-pressed={section==='guest'} onClick={()=>setSection('guest')}>{localize("ui.fd58f51f50008f89",{defaultValue:"Guest contributions"})}</button></div>{section==='contribute'?<div className="gc-panel"><h3>{localize("ui.e7e545c8948180c1",{defaultValue:"Make your contribution"})}</h3><p>{localize("ui.05e8e727cb7f1398",{defaultValue:"Join your group’s gift and keep your transfer details in one place."})}</p>{ready.enabled?<GroupGuestCheckout token={token} groupId={group.id} creator creatorName={group.creatorName} pageId={pageId.current!}/>:<p>{localize("ui.f5dea2ce8ef939dd",{defaultValue:"Enable the invite link from Overview, then enable a payment method in Guest contributions to start."})}</p>}</div>:section==='guest'?<GroupGuestCreatorSettings token={token} groupId={group.id} pageId={pageId.current!}/>:section==='payments'?<GroupCreatorPaymentSettings token={token} groupId={group.id} pageId={pageId.current!}/>:section==='overview'?<div className="gc-panel"><h3>{localize("ui.8b2013104f003ec3",{defaultValue:"Group overview"})}</h3>{group.giftName&&<p>🎁 {group.giftName}</p>}{group.description&&<p>{group.description}</p>}<p>{localize("ui.c9824d81420f7218",{defaultValue:"Group membership is available. Share the invite link after enabling it."})}</p>{ready.enabled?<><p>{localize("ui.53f01086576d54b2",{defaultValue:"Group link: "})}<a href={`/groups/${token}/${group.id}`}>{localize("ui.e2779e3f74231d11",{defaultValue:"Open member join page"})}</a></p><button type="button" className="gc-secondary" onClick={()=>void trackedCopy(`${location.origin}/groups/${token}/${group.id}`)}>{localize("ui.5c58cd7963cac4e2",{defaultValue:"Copy invite link"})}</button></>:<><span title={ready.reasons.join('; ')}><button disabled>{localize("ui.16f6803a1f4a8a60",{defaultValue:"Invite link unavailable"})}</button></span><p>{ready.reasons.join('; ')}</p></>}<p><strong>{localize("ui.872f2464b9c2c706",{defaultValue:"Host visibility:"})}</strong> {published?localize("ui.6a18ba722ec4c775",{defaultValue:"Collection published to the host."}):localize("ui.e9f1ebfb29dc36b5",{defaultValue:"Your collection stays private until you publish it."})}</p><span title={ready.reasons.join('; ')}><button type="button" className="gc-launch" disabled={busy\|\|!ready.enabled\|\|published} onClick={()=>void publishGroup()}>{published?localize("ui.a1ddffdbaeaee49f",{defaultValue:"Collection published"}):localize("ui.a90c8deaaa3deaaf",{defaultValue:"Publish collection to host"})}</button></span>{independent?<IndependentGroupPlan key={group.id} group={group} call={call} onChange={refreshPlan} busy={busy} setBusy={setBusy}/>:<GroupGiftPlan groupId={group.id} giftName={group.giftName} selectedGiftId={selectedGiftId} reserved={reserved} emailVerified={creatorEmailVerified} wishlist={wishlist} call={call} onChange={refreshPlan} onNotice={setAccountNotice} busy={busy} setBusy={setBusy}/>}<GroupActivity url={base+`/activity/${group.id}`} pageId={pageId.current!} revision={historyRevision}/></div>:section==='members'?<div className="gc-panel"><h3>{localize("ui.51c8cfe9e55a8795",{defaultValue:"Member requests"})}</h3><p>{localize("ui.c0f285910c566cf7",{defaultValue:"Rejected members are visible only here. Mobile numbers are private."})}</p>{members.length===0?<p>{localize("ui.ea923f45a415c408",{defaultValue:"No requests yet."})}</p>:members.map(m=><div key={m.id} className="gc-member"><strong>{m.display_name}</strong> · {localizeText(m.status)}<small>{m.mobile}</small>{m.status==='PENDING'&&<div><button type="button" className="gc-secondary" disabled={busy} onClick={()=>void reviewMember(m.id,'APPROVE')}>{localize("ui.6007acbe30b2cd98",{defaultValue:"Approve"})}</button> <button type="button" className="gc-secondary" disabled={busy} onClick={()=>void reviewMember(m.id,'REJECT')}>{localize("ui.ab604a360777735f",{defaultValue:"Reject"})}</button></div>}</div>)}</div>:<form className="gc-form gc-panel" onSubmit={savePrivacy}><h3>{localize("ui.313cee8924674208",{defaultValue:"Who can see what?"})}</h3><p>{localize("ui.45855ba773e4b714",{defaultValue:"These settings control joining and member visibility. Manual transfers remain unverified until independently confirmed."})}</p><label>{localize("ui.c99c586025d6769c",{defaultValue:"Who can join?"})}<select value={privacy.join_policy} onChange={e=>setPrivacy({...privacy,join_policy:e.target.value})}><option value="APPROVAL">{localize("ui.695adaeb4a184ef9",{defaultValue:"Anyone with invite link — Admin approval"})}</option><option value="AUTO_JOIN">{localize("ui.017e668486e017cc",{defaultValue:"Anyone with invite link — Auto join"})}</option><option value="INVITE_ONLY">{localize("ui.f689ac53ea5b8b5a",{defaultValue:"Invitation only — individual invitation required"})}</option></select></label><label>{localize("ui.2ad6410b25fa2dbb",{defaultValue:"Who can see the member list?"})}<select value={privacy.member_visibility} onChange={e=>setPrivacy({...privacy,member_visibility:e.target.value})}><option value="ADMIN_ONLY">{localize("ui.f274de8e243d9852",{defaultValue:"Admin only"})}</option><option value="MEMBERS">{localize("ui.ecde6f8618226d17",{defaultValue:"Approved members"})}</option></select></label><label>{localize("ui.5bc6d84f2126eb5e",{defaultValue:"Who can see individual contribution amounts?"})}<select value={privacy.amount_visibility} onChange={e=>setPrivacy({...privacy,amount_visibility:e.target.value})}><option value="ADMIN_ONLY">{localize("ui.f274de8e243d9852",{defaultValue:"Admin only"})}</option><option value="OWN_ONLY">{localize("ui.984807e2f71329dd",{defaultValue:"Each member sees only their own"})}</option></select></label><p>{localize("ui.11d0ef3a4dbea7a0",{defaultValue:"To offer UPI or bank transfers, configure Payment accounts, then choose accounts and contribution access under Guest contributions. Reported transfers do not confirm receipt of funds."})}</p><p><strong>{localize("ui.1ee6f936b3bcf983",{defaultValue:"Host access:"})}</strong> {localize("ui.a9bb8e886b07bdad",{defaultValue:" Collection details are shared only when you publish to the host."})}</p><button className="gc-launch" disabled={busy}>{busy?localize("ui.23e39291d6135814",{defaultValue:"Saving…"}):localize("ui.cd933d481eefc0f0",{defaultValue:"Save privacy settings"})}</button></form>}</>:<><details open={independent\|\|!guestGifts}><summary>{localize("ui.bbc68131737f8c68",{defaultValue:"Create a group for a gift together"})}</summary><h2>{localize("ui.007dbac389390c94",{defaultValue:"Create your group"})}</h2><p>{independent?localize("ui.44803348cc757651",{defaultValue:"Tell us about your group. It will appear as Something is Cooking while you plan your special gift."}):localize("ui.af064717e6e90911",{defaultValue:"Tell us about your group. Only you can see these details for now."})}</p><form className="gc-form" onSubmit={setup}><label>{localize("ui.6cb353a65d0160d5",{defaultValue:"Group name *"})}<input required maxLength={120} value={form.name} onChange={e=>setForm({...form,name:e.target.value})}/></label><label>{localize("ui.7204645b7341d714",{defaultValue:"Your name *"})}<input required maxLength={120} value={form.creatorName} onChange={e=>setForm({...form,creatorName:e.target.value})}/></label><label>{localize("ui.0836c3b7cadbef5b",{defaultValue:"Mobile number *"})}<input required type="tel" maxLength={20} value={form.creatorMobile} onChange={e=>setForm({...form,creatorMobile:e.target.value})}/></label><label>{localize("ui.751e68347f10bcf7",{defaultValue:"Group description"})}<textarea maxLength={500} value={form.description} onChange={e=>setForm({...form,description:e.target.value})}/><small>{form.description.length}/500 Characters</small></label>{independent?<IndependentGroupPlanFields value={form} onChange={value=>setForm(current=>({...current,...value}))} call={call} busy={busy} setBusy={setBusy}/>:<><label>{localize("ui.09b05cb4d017cf72",{defaultValue:"Collection plan"})}<select value={giftId} onChange={e=>{setGiftId(e.target.value);if(!e.target.value)setForm(value=>({...value,giftName:'',giftDescription:'',giftLink:'',targetRupees:''}))}}><option value="">{localize("ui.571c56f35c99a8c9",{defaultValue:"General collection · choose a gift later"})}</option>{wishlist.filter(g=>g.contributionEnabled!==false).map(g=><option key={g.id} value={g.id}>{g.name}</option>)}</select></label>{giftId&&<p>{localize("ui.ba220a94b033285b",{defaultValue:"Wishlist gift details are filled in below. The gift stays available until your creator email is verified."})}</p>}<label>{localize("ui.4e82ed6b989eb0fb",{defaultValue:"Gift name (optional)"})}<input maxLength={160} readOnly={!!giftId} value={form.giftName} onChange={e=>setForm({...form,giftName:e.target.value})}/></label><label>{localize("ui.da0378150bd065d4",{defaultValue:"Gift description"})}<textarea maxLength={500} readOnly={!!giftId} value={form.giftDescription} onChange={e=>setForm({...form,giftDescription:e.target.value})}/></label><label>{localize("ui.5c7466d4572b8863",{defaultValue:"Gift link"})}<input type="url" maxLength={1000} readOnly={!!giftId} value={form.giftLink} onChange={e=>setForm({...form,giftLink:e.target.value})}/></label><label>{localize("ui.1c68de56ec168a54",{defaultValue:"Gift goal in ₹"})}<input inputMode="decimal" readOnly={!!giftId} value={form.targetRupees} onChange={e=>setForm({...form,targetRupees:e.target.value})}/></label></>}<button className="gc-launch" disabled={busy}>{busy?localize("ui.c79ed9492e3c1719",{defaultValue:"Creating…"}):localize("ui.5457c9f7d1371da0",{defaultValue:"Create private group →"})}</button></form></details></>} | open | [web/components/GroupContribution.tsx:89](../../../web/components/GroupContribution.tsx#L89) |
| UI-00625 | "dialog" | {localize("ui.cc2f3579e157b2d0",{defaultValue:"UPI QR preview"})} | preview | [web/components/GroupCreatorPaymentSettings.tsx:26](../../../web/components/GroupCreatorPaymentSettings.tsx#L26) |
| UI-00767 | "dialog" | {/* eslint-disable-next-line @next/next/no-img-element */} | !focused&&qrOpen; selected?.method==='UPI'&&selected.qrPngBase64&&!focused; eligible.length===0; error; loading | [web/components/GuestVerifiedDestination.tsx:52](../../../web/components/GuestVerifiedDestination.tsx#L52) |
| UI-01014 | "dialog" | {step==='choice'?<><h2 id="special-gift-title">{localize("ui.9ead2ac60e7db3f4",{defaultValue:"How would you like to give?"})}</h2><p>{localize("ui.a282a41c377c540a",{defaultValue:"Plan a thoughtful gift on your own or bring your favourite people together."})}</p><div className="special-gift-choices"><button data-autofocus type="button" className="gc-secondary" onClick={()=>void individual()}><strong>{localize("ui.c14c2c732437af98",{defaultValue:"Individual Gift"})}</strong><span>{localize("ui.1661cf99f1c904d8",{defaultValue:"Add your details and choose how to share your gift."})}</span><small>{localize("ui.8870479736489e78",{defaultValue:"No account or group needed"})}</small></button><button type="button" className="gc-secondary" onClick={()=>{setError('');setStep('group')}}><strong>{localize("ui.62d0a830e2756168",{defaultValue:"Group Gift"})}</strong><span>{localize("ui.3ab6895ab8f19cc9",{defaultValue:"Create a group and collect contributions together."})}</span><small>{localize("ui.e89f3bfbd1e4e4c0",{defaultValue:"Sign in or create an account to get started"})}</small></button></div></>:<> <button type="button" className="gc-secondary" disabled={busy} onClick={()=>setStep('choice')}>{localize("ui.2f60681f8b63e96a",{defaultValue:"← Giving options"})}</button><h2 id="special-gift-title">{localize("ui.57037bdffdc5602d",{defaultValue:"Your special gift"})}</h2> {saved?<><p role="status">{notice\|\|localize("ui.18be7e148c04c0fa",{defaultValue:"Manage {{value0}}",value0:(saved.name)})}</p>{saved.giving_method==='LINK_AND_CONTRIBUTION'&&<IndividualHostContribution token={token} giftId={saved.id} call={call} onBusyChange={setBusy} onReported={()=>notifyGuestGifts(token)}/>} {saved.giving_method==='DIRECT_DELIVERY'&&<section className="gc-panel"><h3>{localize("ui.02968357c8dc9617",{defaultValue:"Deliver your gift to the host"})}</h3><p>{localize("ui.ecf0f6235c0d7d99",{defaultValue:"Purchase the item from the retailer and use the host’s address at checkout."})}</p>{saved.product_url&&<a href={saved.product_url} target="_blank" rel="noopener noreferrer">{localize("ui.a1fc818d679ea436",{defaultValue:"Purchase gift from the retailer ↗"})}</a>}{address?<address>{address.addressLine1\|\|localize("ui.34e92463e22d5ad8",{defaultValue:"Please contact the host to confirm the delivery address."})}<br/>{address.city}, {address.state} {address.postalCode}<br/>{address.country}</address>:<button type="button" disabled={busy} onClick={async()=>{setBusy(true);setError('');try{await showAddress(saved.id)}catch(e){setError(e instanceof Error?e.message:'Address unavailable')}finally{setBusy(false)}}}>{localize("ui.0e1504cb20baa605",{defaultValue:"View host delivery address"})}</button>}<button type="button" className="gc-secondary" disabled={busy\|\|saved.delivery_status==='TRACKING_SHARED'\|\|saved.delivery_status==='GIFT_PURCHASED'} onClick={()=>void updateDelivery('GIFT_PURCHASED')}>{saved.delivery_status==='GIFT_PURCHASED'\|\|saved.delivery_status==='TRACKING_SHARED'?localize("ui.e20ac06e7a6a8b8e",{defaultValue:"Gift purchased"}):localize("ui.9c788397748c304a",{defaultValue:"I have purchased the gift"})}</button><form className="gc-form" onSubmit={e=>{e.preventDefault();void updateDelivery('TRACKING_SHARED')}}><label>{localize("ui.f296b67a545cd756",{defaultValue:"Shipment tracking details"})}<textarea required maxLength={1000} placeholder={localize("ui.a9da12665e488803",{defaultValue:"Carrier name, tracking number or tracking link"})} value={tracking} onChange={e=>setTracking(e.target.value)}/></label><button className="gc-launch" disabled={busy}>{localize("ui.15858678da15e73d",{defaultValue:"Share tracking with host"})}</button></form></section>}<button type="button" className="gc-secondary" disabled={busy} onClick={()=>{setSaved(null);setSource('');setForm(empty);setAddress(null);setTracking('');setNotice('');void individual()}}>{localize("ui.dabf10e2128f6ebd",{defaultValue:"Add another special gift"})}</button></>:<form className="gc-form" onSubmit={save}> {gifts.length>0&&<label>{localize("ui.af7e58d035aad83e",{defaultValue:"Choose a gift"})}<select disabled={busy} value={source} onChange={e=>choose(e.target.value)}><option value="">{localize("ui.cb480b3aa513d781",{defaultValue:"Add my own special gift"})}</option>{gifts.map(g=><option key={g.id} value={g.id}>{g.name}</option>)}</select></label>} <fieldset disabled={busy}><legend>{localize("ui.c0bcd4c0f8e70784",{defaultValue:"Your details"})}</legend><label>{localize("ui.2c6b2e253c01fb2e",{defaultValue:"Your name"})}<input data-autofocus required autoComplete="name" maxLength={120} value={form.guestName} onChange={e=>setForm({...form,guestName:e.target.value})}/></label><label>{localize("ui.f2488fd4ef4adbc6",{defaultValue:"Email address"})}<input required type="email" autoComplete="email" maxLength={254} value={form.email} onChange={e=>setForm({...form,email:e.target.value})}/></label><label>{localize("ui.187e80ba4d8a22bb",{defaultValue:"Mobile number"})}<input required type="tel" autoComplete="tel" pattern="\+?[0-9][0-9 \-]{7,23}" maxLength={25} value={form.mobile} onChange={e=>setForm({...form,mobile:e.target.value})}/></label></fieldset> <fieldset disabled={busy}><legend>{localize("ui.f2da19dd48c937be",{defaultValue:"Gift details"})}</legend><label>{localize("ui.64b4ff26c07d838e",{defaultValue:"Gift name"})}<input required maxLength={160} value={form.name} onChange={e=>setForm({...form,name:e.target.value})}/></label><label>{localize("ui.a06890de939bdcac",{defaultValue:"Product link"})}{method==='DIRECT_DELIVERY'?'':localize("ui.418e319e80da27d9",{defaultValue:" · optional if you add gift details"})}<input type="url" required={method==='DIRECT_DELIVERY'} pattern="https://[^\s]+" placeholder="https://…" maxLength={1000} value={form.productUrl} onChange={e=>setForm({...form,productUrl:e.target.value})}/></label><label>{localize("ui.f2da19dd48c937be",{defaultValue:"Gift details"})}<textarea maxLength={500} value={form.description} onChange={e=>setForm({...form,description:e.target.value})}/></label></fieldset> <fieldset className="special-gift-methods" disabled={busy}><legend>{localize("ui.5d7ced5b37aaf867",{defaultValue:"How would you like to share your gift?"})}</legend><label><input type="radio" name="special-gift-method" checked={method==='LINK_AND_CONTRIBUTION'} onChange={()=>setMethod('LINK_AND_CONTRIBUTION')}/><span>{localize("ui.466b0173b3baaea4",{defaultValue:"Share a gift link and contribute to the host"})}</span></label><label><input type="radio" name="special-gift-method" checked={method==='DIRECT_DELIVERY'} onChange={()=>setMethod('DIRECT_DELIVERY')}/><span>{localize("ui.764968df837ae88e",{defaultValue:"I’ll purchase and deliver the gift myself"})}</span></label></fieldset> {method==='LINK_AND_CONTRIBUTION'?<label>{localize("ui.7fc910558a4cc82b",{defaultValue:"Contribution amount (₹)"})}<input required type="number" inputMode="decimal" min="1" max="1000000" step="0.01" value={form.amount} onChange={e=>setForm({...form,amount:e.target.value})}/><small>{localize("ui.30e62cf816e2cf0b",{defaultValue:"Continue to send this amount directly to the host’s UPI or bank account."})}</small></label>:<p>{localize("ui.b04dd28ab478baae",{defaultValue:"After adding your gift, you can view the host delivery address, purchase from the retailer and share tracking details here."})}</p>} <p className="finePrint">{localize("ui.ada703e1b6b59c56",{defaultValue:"Your contact details and contribution amount are shared only with the host."})}</p><button className="gc-launch" disabled={busy}>{busy?localize("ui.2e3b4a7d1f13e7c3",{defaultValue:"Saving your gift…"}):method==='LINK_AND_CONTRIBUTION'?localize("ui.75bda07643c2766a",{defaultValue:"Continue to host payment"}):localize("ui.0580e20a82366727",{defaultValue:"Add my special gift"})}</button> </form>} {!saved&&previous.length>0&&<section className="gc-panel"><h3>{localize("ui.76d4f865d4b5307f",{defaultValue:"Your gifts in this browser"})}</h3>{previous.map(g=><button key={g.id} type="button" className="gc-secondary" disabled={busy} onClick={()=>{setSaved(g);setAddress(null);setTracking('');setNotice('')}}>{localize("ui.d1f3b7c8d7b5ce7f",{defaultValue:"Manage "})}{g.name}</button>)}</section>} </>} {error&&<p role="alert" className="gc-error">{localizeText(error)}</p>} | (step==='choice'\|\|step==='individual') | [web/components/SpecialGiftEntry.tsx:69](../../../web/components/SpecialGiftEntry.tsx#L69) |
| UI-01109 | dialog | {children} | Parent/source state | [web/components/focused/Invitation.tsx:26](../../../web/components/focused/Invitation.tsx#L26) |
| UI-00072 | "status" | {localize("ui.bed9096c411e6da3",{defaultValue:"Opening your workspace…"})} | Parent/source state | [web/app/components/HostGlobalNav.tsx:17](../../../web/app/components/HostGlobalNav.tsx#L17) |
| UI-00098 | "status" | {localize("ui.c926c2c50e65d5a5",{defaultValue:"Opening…"})} | Parent/source state | [web/app/e/[token]/PublicEventClient.tsx:19](../../../web/app/e/[token]/PublicEventClient.tsx#L19) |
| UI-00101 | "alert" | {localize("ui.14578d88ad5db954",{defaultValue:"Contribution progress is unavailable. "})} | progressState==='error'; event.features?.GIFTING_ENABLED!==false | [web/app/e/[token]/PublicEventClient.tsx:73](../../../web/app/e/[token]/PublicEventClient.tsx#L73) |
| UI-00108 | "status" | {localize('growth.invitation.loading',{defaultValue:'Opening your invitation…'})} | Parent/source state | [web/app/e/[token]/PublicEventClient.tsx:99](../../../web/app/e/[token]/PublicEventClient.tsx#L99) |
| UI-00498 | "alert" | {localizeText(error)} | error; groupBase&&error.includes('switched off') | [web/components/CommercePurchase.tsx:34](../../../web/components/CommercePurchase.tsx#L34) |
| UI-00500 | "status" | {localizeText(notice)} | notice | [web/components/CommercePurchase.tsx:34](../../../web/components/CommercePurchase.tsx#L34) |
| UI-00531 | "status" | {value?(remaining?localizeText(`${remaining} requirements still needed.`):localizeText("All requirements met.")):localizeText("Enter your "+kind+" to check the requirements.")} | Parent/source state | [web/components/CredentialRequirements.tsx:7](../../../web/components/CredentialRequirements.tsx#L7) |
| UI-00548 | "status" | {localize("ui.7a005f0f1dbc704f",{defaultValue:"Opening your celebration space…"})} | ready\|\|discovery | [web/components/ExperienceProvider.tsx:19](../../../web/components/ExperienceProvider.tsx#L19) |
| UI-00552 | "alert" | {localizeText(error)} | error | [web/components/GroupActivity.tsx:13](../../../web/components/GroupActivity.tsx#L13) |
| UI-00561 | "status" | {accountNotice} | logged&&accountNotice; open | [web/components/GroupContribution.tsx:90](../../../web/components/GroupContribution.tsx#L90) |
| UI-00562 | "alert" | {localizeText(error)} | error; open | [web/components/GroupContribution.tsx:90](../../../web/components/GroupContribution.tsx#L90) |
| UI-00588 | "status" | {pinNotice} | pinNotice; allowPin; group; !logged; loading; open | [web/components/GroupContribution.tsx:90](../../../web/components/GroupContribution.tsx#L90) |
| UI-00635 | "status" | {localize("ui.3850dde1a89614a2",{defaultValue:"✓ Email verified for account recovery"})} | Parent/source state | [web/components/GroupEmailVerification.tsx:18](../../../web/components/GroupEmailVerification.tsx#L18) |
| UI-00642 | "status" | {localizeText(notice)} | notice | [web/components/GroupEmailVerification.tsx:19](../../../web/components/GroupEmailVerification.tsx#L19) |
| UI-00649 | "status" | {localizeText(notice)} | notice | [web/components/GroupGiftPlan.tsx:11](../../../web/components/GroupGiftPlan.tsx#L11) |
| UI-00650 | "alert" | {localizeText(error)} | error | [web/components/GroupGuestCheckout.tsx:28](../../../web/components/GroupGuestCheckout.tsx#L28) |
| UI-00651 | "status" |  | stage==='thanks'; !allowed; !invite | [web/components/GroupGuestCheckout.tsx:29](../../../web/components/GroupGuestCheckout.tsx#L29) |
| UI-00672 | "status" | {localizeText(notice)} | notice | [web/components/GroupGuestCreatorSettings.tsx:18](../../../web/components/GroupGuestCreatorSettings.tsx#L18) |
| UI-00689 | "status" | {localizeText(notice)} | notice | [web/components/GuestActions.tsx:27](../../../web/components/GuestActions.tsx#L27) |
| UI-00697 | "status" | {localizeText(rsvpResult)} | features.RSVP | [web/components/GuestCelebration.tsx:33](../../../web/components/GuestCelebration.tsx#L33) |
| UI-00704 | "status" | {localizeText(wishResult)} | features.GUESTBOOK | [web/components/GuestCelebration.tsx:36](../../../web/components/GuestCelebration.tsx#L36) |
| UI-00707 | "alert" | {pricingError} | pricingError; paymentMethod==='razorpay'; paymentMethod==='razorpay'&&!razorpayAllowed | [web/components/GuestContributionCheckout.tsx:117](../../../web/components/GuestContributionCheckout.tsx#L117) |
| UI-00709 | "status" | {localize("ui.587f32a685d7c85e",{defaultValue:"Loading payment methods…"})} | destinationsLoading | [web/components/GuestContributionCheckout.tsx:140](../../../web/components/GuestContributionCheckout.tsx#L140) |
| UI-00710 | "alert" | {destinationsError} | destinationsError | [web/components/GuestContributionCheckout.tsx:140](../../../web/components/GuestContributionCheckout.tsx#L140) |
| UI-00713 | "status" |  | directStatus | [web/components/GuestContributionCheckout.tsx:143](../../../web/components/GuestContributionCheckout.tsx#L143) |
| UI-00715 | "status" |  | receipt; directStatus | [web/components/GuestContributionCheckout.tsx:150](../../../web/components/GuestContributionCheckout.tsx#L150) |
| UI-00718 | "status" | {localize("ui.8479e3a7bd313b71",{defaultValue:"Loading UPI and bank details…"})} | destinationsLoading; receipt; directStatus | [web/components/GuestContributionCheckout.tsx:152](../../../web/components/GuestContributionCheckout.tsx#L152) |
| UI-00719 | "alert" | {destinationsError} | destinationsError; receipt; directStatus | [web/components/GuestContributionCheckout.tsx:153](../../../web/components/GuestContributionCheckout.tsx#L153) |
| UI-00730 | "status" | {directStatus} | directStatus; paymentMethod!=='razorpay'; receipt | [web/components/GuestContributionCheckout.tsx:173](../../../web/components/GuestContributionCheckout.tsx#L173) |
| UI-00732 | "alert" | {localizeText(error)} | error; receipt; directStatus | [web/components/GuestContributionCheckout.tsx:180](../../../web/components/GuestContributionCheckout.tsx#L180) |
| UI-00736 | "status" | {localize("ui.1d1aaa033b0ac4be",{defaultValue:"Loading guest gifts…"})} | loading | [web/components/GuestGiftBoard.tsx:14](../../../web/components/GuestGiftBoard.tsx#L14) |
| UI-00737 | "alert" | {localizeText(error)} | error | [web/components/GuestGiftBoard.tsx:14](../../../web/components/GuestGiftBoard.tsx#L14) |
| UI-00740 | "status" | {g.display_status\|\|g.selection_label} | Parent/source state | [web/components/GuestGiftBoard.tsx:14](../../../web/components/GuestGiftBoard.tsx#L14) |
| UI-00742 | "status" | {g.display_status} | Parent/source state | [web/components/GuestGiftBoard.tsx:14](../../../web/components/GuestGiftBoard.tsx#L14) |
| UI-00750 | "alert" | {localizeText(error)} | error | [web/components/GuestGiftManager.tsx:19](../../../web/components/GuestGiftManager.tsx#L19) |
| UI-00751 | "status" | {localizeText(notice)} | notice | [web/components/GuestGiftManager.tsx:19](../../../web/components/GuestGiftManager.tsx#L19) |
| UI-00752 | "status" | {localize("ui.20fc5947f275b14c",{defaultValue:"Loading gifts…"})} | loading | [web/components/GuestGiftManager.tsx:19](../../../web/components/GuestGiftManager.tsx#L19) |
| UI-00757 | "status" | {localize("ui.bd4dee85230b343f",{defaultValue:"Checking eligible payment destinations…"})} | loading | [web/components/GuestVerifiedDestination.tsx:39](../../../web/components/GuestVerifiedDestination.tsx#L39) |
| UI-00758 | "alert" | {localizeText(error)} | error; loading | [web/components/GuestVerifiedDestination.tsx:40](../../../web/components/GuestVerifiedDestination.tsx#L40) |
| UI-00760 | "status" | {method==='upi'? localize("ui.2971c4992c8102c1",{defaultValue:"No eligible preferred UPI QR is currently available. Please do not make a transfer yet."}): localize("ui.e2679111c99736c6",{defaultValue:"No eligible preferred bank account is currently available. Please do not make a transfer yet."})} | eligible.length===0; error; loading | [web/components/GuestVerifiedDestination.tsx:40](../../../web/components/GuestVerifiedDestination.tsx#L40) |
| UI-00765 | "status" | {copyNotice} | copyNotice; selected?.method==='UPI'&&selected.qrPngBase64&&focused; eligible.length===0; error; loading | [web/components/GuestVerifiedDestination.tsx:48](../../../web/components/GuestVerifiedDestination.tsx#L48) |
| UI-00790 | "alert" | {localizeText(error)} | error | [web/components/HostWorkspace.tsx:53](../../../web/components/HostWorkspace.tsx#L53) |
| UI-00799 | "status" | {localizeText(notice)} | notice | [web/components/IndependentGroupPlan.tsx:21](../../../web/components/IndependentGroupPlan.tsx#L21) |
| UI-00802 | "status" | {localizeText(notice)} | notice | [web/components/IndependentGroupPlan.tsx:26](../../../web/components/IndependentGroupPlan.tsx#L26) |
| UI-00803 | "status" | {localize("ui.0a0f81c5c16fb959",{defaultValue:"Loading your contribution…"})} | loading | [web/components/IndividualHostContribution.tsx:25](../../../web/components/IndividualHostContribution.tsx#L25) |
| UI-00804 | "status" |  | payment?.claim_id; loading | [web/components/IndividualHostContribution.tsx:25](../../../web/components/IndividualHostContribution.tsx#L25) |
| UI-00811 | "alert" | {localizeText(error)} | error | [web/components/IndividualHostContribution.tsx:29](../../../web/components/IndividualHostContribution.tsx#L29) |
| UI-00909 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:19](../../../web/components/LocalizationProvider.tsx#L19) |
| UI-00911 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:20](../../../web/components/LocalizationProvider.tsx#L20) |
| UI-00913 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:21](../../../web/components/LocalizationProvider.tsx#L21) |
| UI-00928 | "status" | {localize("ui.522a82b93e381483",{defaultValue:"Mobile number verified"})} | verified | [web/components/MobileVerification.tsx:17](../../../web/components/MobileVerification.tsx#L17) |
| UI-00935 | "status" | {localizeText(message)} | message | [web/components/MobileVerification.tsx:17](../../../web/components/MobileVerification.tsx#L17) |
| UI-01011 | "status" | {localizeText(notice)} | notice | [web/components/SocialSharing.tsx:18](../../../web/components/SocialSharing.tsx#L18) |
| UI-01019 | "status" | {notice\|\|localize("ui.18be7e148c04c0fa",{defaultValue:"Manage {{value0}}",value0:(saved.name)})} | saved; step==='choice'; (step==='choice'\|\|step==='individual') | [web/components/SpecialGiftEntry.tsx:73](../../../web/components/SpecialGiftEntry.tsx#L73) |
| UI-01043 | "alert" | {localizeText(error)} | error; (step==='choice'\|\|step==='individual') | [web/components/SpecialGiftEntry.tsx:82](../../../web/components/SpecialGiftEntry.tsx#L82) |
| UI-01056 | "status" | {p.receipt?<><p>{localize("ui.dddc9242b436b8e8",{defaultValue:"Gift allocation: "})}{inr(p.receipt.giftPaise)}</p><p>{localize("ui.ae576bd1ca8463d9",{defaultValue:"Celebration allocation: "})}{inr(p.receipt.walletPaise)}</p><p className="finePrint">{localize("ui.1e029b2aa4518972",{defaultValue:"Payment ID: "})}{p.receipt.paymentId}</p></>:<><p>{inr(amount)} · {p.method==='upi'?localize("ui.7b06e0160857f939",{defaultValue:"UPI"}):localize("ui.d69d587c81659616",{defaultValue:"Bank transfer"})}</p><p className="finePrint">{localize("ui.63ee050b1747e8c3",{defaultValue:"Submission reference: "})}{p.reported.split('Reference: ')[1]\|\|localize("ui.a8536392c5bbbb8f",{defaultValue:"See the host for an update"})}</p></>} {p.close&&<button className="focusPrimary" onClick={p.close}>{localize("ui.5aca8cea2b1e0071",{defaultValue:"Back to the celebration"})}</button>} | p.receipt\|\|p.reported | [web/components/focused/Checkout.tsx:13](../../../web/components/focused/Checkout.tsx#L13) |
| UI-01072 | "alert" | {p.error} | p.error; p.receipt\|\|p.reported | [web/components/focused/Checkout.tsx:13](../../../web/components/focused/Checkout.tsx#L13) |
| UI-01112 | "status" | {localize("ui.e8981426afefa46f",{defaultValue:"Progress is temporarily unavailable. "})} | progressState==='error'; event.features?.GIFTING_ENABLED!==false | [web/components/focused/Invitation.tsx:36](../../../web/components/focused/Invitation.tsx#L36) |


### Workflow state bindings

| Source | Binding | Initial expression |
| --- | --- | --- |
| web/app/e/[token]/PublicEventClient.tsx | walletOpen | false |
| web/components/LocalizationProvider.tsx | busy | false |
| web/components/LocalizationProvider.tsx | error | '' |
| web/components/GroupContribution.tsx | open | false |
| web/components/GroupContribution.tsx | tab | 'register' |
| web/components/GroupContribution.tsx | pinNotice | '' |
| web/components/GroupContribution.tsx | error | '' |
| web/components/GroupContribution.tsx | busy | false |
| web/components/GroupContribution.tsx | loading | false |
| web/components/GroupContribution.tsx | section | 'overview' |
| web/components/GroupContribution.tsx | accountNotice | '' |
| web/components/MobileVerification.tsx | busy | false |
| web/components/MobileVerification.tsx | message | '' |
| web/components/GroupGuestCheckout.tsx | busy | false |
| web/components/GroupGuestCheckout.tsx | error | '' |
| web/components/GroupEmailVerification.tsx | notice | '' |
| web/components/GroupEmailVerification.tsx | busy | false |
| web/components/GroupCreatorPaymentSettings.tsx | error | '' |
| web/components/GroupCreatorPaymentSettings.tsx | message | '' |
| web/components/GroupCreatorPaymentSettings.tsx | busy | false |
| web/components/GroupGuestCreatorSettings.tsx | notice | '' |
| web/components/GroupGiftPlan.tsx | notice | '' |
| web/components/IndependentGroupPlan.tsx | notice | '' |
| web/components/IndependentGroupPlan.tsx | notice | '' |
| web/components/GuestGiftManager.tsx | busy | false |
| web/components/GuestGiftManager.tsx | loading | true |
| web/components/GuestGiftManager.tsx | error | '' |
| web/components/GuestGiftManager.tsx | notice | '' |
| web/components/GroupActivity.tsx | error | '' |
| web/components/CommercePurchase.tsx | error | '' |
| web/components/CommercePurchase.tsx | busy | false |
| web/components/CommercePurchase.tsx | notice | '' |
| web/components/GuestGiftBoard.tsx | loading | true |
| web/components/GuestGiftBoard.tsx | error | '' |
| web/components/SpecialGiftEntry.tsx | step | 'closed' |
| web/components/SpecialGiftEntry.tsx | notice | '' |
| web/components/SpecialGiftEntry.tsx | error | '' |
| web/components/SpecialGiftEntry.tsx | busy | false |
| web/components/IndividualHostContribution.tsx | loading | true |
| web/components/IndividualHostContribution.tsx | error | '' |
| web/components/IndividualHostContribution.tsx | busy | false |
| web/components/GuestVerifiedDestination.tsx | copyNotice | '' |
| web/components/GuestVerifiedDestination.tsx | loading | true |
| web/components/GuestVerifiedDestination.tsx | error | '' |
| web/components/GuestVerifiedDestination.tsx | qrOpen | false |
| web/components/focused/Invitation.tsx | checkoutBusy | false |
| web/components/focused/Invitation.tsx | providerOpen | false |
| web/components/focused/Invitation.tsx | error | '' |
| web/components/GuestContributionCheckout.tsx | destinationsError | '' |
| web/components/GuestContributionCheckout.tsx | destinationsLoading | true |
| web/components/GuestContributionCheckout.tsx | busy | false |
| web/components/GuestContributionCheckout.tsx | error | '' |
| web/components/GuestContributionCheckout.tsx | pricingError | '' |
| web/components/focused/Checkout.tsx | step | 0 |
| web/components/GuestActions.tsx | notice | '' |
| web/components/GuestCelebration.tsx | message | '' |
| web/components/GuestCelebration.tsx | busy | false |
| web/components/SocialSharing.tsx | notice | '' |
| web/components/HostWorkspace.tsx | busy | false |
| web/components/HostWorkspace.tsx | error | '' |


## Celebrations

| Screen information | Source-supported specification |
| --- | --- |
| Route | /events |
| Source | [web/app/events/page.tsx:1](../../../web/app/events/page.tsx#L1) |
| Purpose | List/search/manage own events; mode-dependent create/edit surfaces |
| User access level | Authenticated owner for event reads/writes |
| Navigation source | Workspace Celebrations / dashboard |
| Navigation destination | Event overview, gifts, QR, preview, reports or creation |
| Preconditions | Route parameters/query scope must be valid; applicable session/ownership/module/provider state above. Load errors do not imply an empty successful dataset. |
| Postconditions | Only the selected successful action changes its documented state. Navigation alone does not publish, confirm payment, verify contact or deliver a notification. |
| Route variables | No path variable |
| UI variants | Focused/general experience, responsive layout and module/state predicates can change visible sections. Exact predicates are in each control record. |


### Wireframe layout

| Section | Placement / behavior / evidence |
| --- | --- |
| Header | Shared identity/event or page title/back/actions, where declared. Composite header declarations: 3. |
| Navigation / sidebar | Host/admin navigation and event context where applicable; mobile/focused variants are conditional. nav=3, aside=3. |
| Main content | Purpose-specific forms/cards/tables/canvas/player from the composed sources. main=4, section=6, article=2. |
| Search and filters | Fields/selects listed below; exact value/handler/visibility controls are not inferred from placeholder labels. |
| Data grids and cards | Table declarations=0. Data collections and generated rows/cards are in frontend.json dynamicSources. |
| Popups / dialogs | Named/source-declared dialog control records below; opening/closing/loading/apply/cancel actions belong to those component handlers. |
| Alerts / notifications | Source controls with role alert/status are listed below. Notice strings may come from API results; do not assume a universal envelope. |
| Footer | Local/shared footer declarations=1. No undocumented legal/footer wording is invented. |
| Loading / empty / success / error | Observed state bindings and relevant action fields are listed below. Expected QA states from Document 2 do not establish a spinner/retry control where source does not implement one. |


### Composed implementation

- [web/app/events/page.tsx:1](../../../web/app/events/page.tsx#L1)
- [web/components/LocalizationProvider.tsx:1](../../../web/components/LocalizationProvider.tsx#L1)
- [web/components/GrowthProvider.tsx:1](../../../web/components/GrowthProvider.tsx#L1)
- [web/components/DeliveryAddressFields.tsx:1](../../../web/components/DeliveryAddressFields.tsx#L1)
- [web/components/ExperienceProvider.tsx:1](../../../web/components/ExperienceProvider.tsx#L1)
- [web/components/focused/EventList.tsx:1](../../../web/components/focused/EventList.tsx#L1)
- [web/app/components/HostToast.tsx:1](../../../web/app/components/HostToast.tsx#L1)
- [web/components/VerificationProvider.tsx:1](../../../web/components/VerificationProvider.tsx#L1)
- [web/app/layout.tsx:1](../../../web/app/layout.tsx#L1)
- [web/components/BrandingProvider.tsx:1](../../../web/components/BrandingProvider.tsx#L1)
- [web/components/LegalFooter.tsx:1](../../../web/components/LegalFooter.tsx#L1)
- [web/components/CookieNotice.tsx:1](../../../web/components/CookieNotice.tsx#L1)
- [web/app/components/ApplicationInteractions.tsx:1](../../../web/app/components/ApplicationInteractions.tsx#L1)
- [web/app/components/PerformanceDiagnostics.tsx:1](../../../web/app/components/PerformanceDiagnostics.tsx#L1)
- [web/app/components/HostSessionRedirect.tsx:1](../../../web/app/components/HostSessionRedirect.tsx#L1)
- [web/app/components/HostGlobalNav.tsx:1](../../../web/app/components/HostGlobalNav.tsx#L1)
- [web/components/HostWorkspace.tsx:1](../../../web/components/HostWorkspace.tsx#L1)
- [web/components/focused/HostShell.tsx:1](../../../web/components/focused/HostShell.tsx#L1)

### Fields

| Control ID | Label / expression | Type | Binding | Required | Native limits / format | Detailed specification |
| --- | --- | --- | --- | --- | --- | --- |
| UI-00204 | {localize("ui.eb821576f19fa523",{defaultValue:"What should we call you? "})} | input  | {nameDraft} | true | minLength={2}; maxLength={120} | [control](UI_CONTROLS.md#ui-00204) |
| UI-00209 | {localize("ui.25bc9efedfb71cbf",{defaultValue:"Event name"})} | input  | {title} | true | maxLength={200} | [control](UI_CONTROLS.md#ui-00209) |
| UI-00210 | {localize("ui.f68e0b1ef1269bae",{defaultValue:"Occasion"})} | select  | {occasionType} | Not declared |  | [control](UI_CONTROLS.md#ui-00210) |
| UI-00211 | {localize("ui.6807b347bcddc04f",{defaultValue:"Date (optional)"})} | input "date" | {eventDate} | Not declared |  | [control](UI_CONTROLS.md#ui-00211) |
| UI-00212 | {localize("ui.3cae814cc8c2d447",{defaultValue:"About this event"})} | textarea  | {description} | Not declared | maxLength={10000} | [control](UI_CONTROLS.md#ui-00212) |
| UI-00213 | {localize("ui.2ec422832b2a8042",{defaultValue:"Street address *"})} | input  | {addressLine1} | true | maxLength={200} | [control](UI_CONTROLS.md#ui-00213) |
| UI-00214 | {localize("ui.4b592363361d535f",{defaultValue:"City *"})} | input  | {city} | true | maxLength={120} | [control](UI_CONTROLS.md#ui-00214) |
| UI-00215 | {localize("ui.0553992d31b72bce",{defaultValue:"State *"})} | input  | {state} | true | maxLength={120} | [control](UI_CONTROLS.md#ui-00215) |
| UI-00216 | {localize("ui.3ce9d7ae8a85a19b",{defaultValue:"PIN / postal code *"})} | input  | {postalCode} | true | maxLength={20} | [control](UI_CONTROLS.md#ui-00216) |
| UI-00217 | {localize("ui.776f755f2fe0ac5b",{defaultValue:"Country *"})} | input  | {country} | true | maxLength={80} | [control](UI_CONTROLS.md#ui-00217) |
| UI-00218 | {localize("ui.7a7d38be87e59584",{defaultValue:"Host name (shown to guests) *"})} | input  | {hostName} | true | maxLength={200} | [control](UI_CONTROLS.md#ui-00218) |
| UI-00219 | {localize("ui.fcf0d0752236b977",{defaultValue:"Contact name *"})} | input  | {contactName} | true | maxLength={150} | [control](UI_CONTROLS.md#ui-00219) |
| UI-00220 | {localize("ui.f4a1cd67585beb93",{defaultValue:"Contact phone *"})} | input "tel" | {contactPhone} | true | maxLength={30} | [control](UI_CONTROLS.md#ui-00220) |
| UI-00221 | {localize("ui.fb685cc8ddb2ec70",{defaultValue:"Contact email (optional)"})} | input "email" | {contactEmail} | Not declared | maxLength={254} | [control](UI_CONTROLS.md#ui-00221) |
| UI-00538 | {localize("ui.455df35dd66a82ca",{defaultValue:"Same as event address"})} | input "checkbox" | {same} | Not declared |  | [control](UI_CONTROLS.md#ui-00538) |
| UI-00539 | {localizeText(label)} | input  | {address[key]} | true | maxLength={maxLength} | [control](UI_CONTROLS.md#ui-00539) |
| UI-00908 | {localize("ui.bba5b40bdd28c66e",{defaultValue:"Choose language"})} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00908) |
| UI-00910 | {label} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00910) |
| UI-00912 | {localize("ui.140071feea59fe87",{defaultValue:"Preferred language"})} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00912) |


### Buttons

| ID | Label / expression | Action | Submit validation / handler | Source |
| --- | --- | --- | --- | --- |
| UI-00078 | × | {()=>{setFlash(null);onDismiss?.();}} | Not a native form submit or inherited from component context | [web/app/components/HostToast.tsx:16](../../../web/app/components/HostToast.tsx#L16) |
| UI-00205 | {localize("ui.b7297226fd1f4681",{defaultValue:"Save name"})} | submit / component action | {saveName} | [web/app/events/page.tsx:71](../../../web/app/events/page.tsx#L71) |
| UI-00222 | {busy?localize("ui.c79ed9492e3c1719",{defaultValue:"Creating…"}):localize("ui.d12514e733617cf7",{defaultValue:"Create event →"})} | submit / component action | {create} | [web/app/events/page.tsx:71](../../../web/app/events/page.tsx#L71) |
| UI-00223 | {localize("ui.0e91610117029a62",{defaultValue:"Refresh"})} | {()=>void reload()} | Not a native form submit or inherited from component context | [web/app/events/page.tsx:71](../../../web/app/events/page.tsx#L71) |
| UI-00231 | {busy ? localize("ui.582e0f1abad43e9e",{defaultValue:"Publishing…"}) : localize("ui.56b8d9be82d69e8f",{defaultValue:"✨ Publish event"})} | {() => {<br>      if (<br>        window.confirm(<br>          `Publish "${e.title}"? Guests will be able to view this celebration.`<br>        )<br>      ) {<br>        void publishEvent(e.id);<br>      }<br>    }} | Not a native form submit or inherited from component context | [web/app/events/page.tsx:93](../../../web/app/events/page.tsx#L93) |
| UI-00234 | {localize("ui.50ca41c9e596c471",{defaultValue:"🔒 Unpublish event"})} | {() => {<br>      if (<br>        window.confirm(<br>          `Unpublish "${e.title}"?\n\nGuests will temporarily lose access to the invitation. You can publish it again later.`<br>        )<br>      ) {<br>        void unpublishEvent(e.id);<br>      }<br>    }} | Not a native form submit or inherited from component context | [web/app/events/page.tsx:121](../../../web/app/events/page.tsx#L121) |
| UI-00528 | Accept optional analytics | {()=>{saveConsent(true);setShow(false)}} | Not a native form submit or inherited from component context | [web/components/CookieNotice.tsx:5](../../../web/components/CookieNotice.tsx#L5) |
| UI-00529 | Reject optional analytics | {()=>{saveConsent(false);setShow(false)}} | Not a native form submit or inherited from component context | [web/components/CookieNotice.tsx:5](../../../web/components/CookieNotice.tsx#L5) |
| UI-00788 | {collapsed?<PanelLeftOpen size={20}/>:<PanelLeftClose size={20}/>} | {toggle} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:50](../../../web/components/HostWorkspace.tsx#L50) |
| UI-00791 |  | {()=>void signOut()} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:53](../../../web/components/HostWorkspace.tsx#L53) |
| UI-00792 |  | {()=>setCollapsed(true)} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:54](../../../web/components/HostWorkspace.tsx#L54) |
| UI-00876 | Cookie preferences | {()=>window.dispatchEvent(new Event('giftwise-cookie-settings'))} | Not a native form submit or inherited from component context | [web/components/LegalFooter.tsx:4](../../../web/components/LegalFooter.tsx#L4) |
| UI-01082 | {localize("ui.942087cc2d41e013",{defaultValue:"Retry"})} | {()=>location.reload()} | Not a native form submit or inherited from component context | [web/components/focused/EventList.tsx:16](../../../web/components/focused/EventList.tsx#L16) |


### Dialog and alert/status inventory

| ID | Role / kind | Text / label | Visibility conditions | Source |
| --- | --- | --- | --- | --- |
| UI-00232 | native-dialog | `Publish "${e.title}"? Guests will be able to view this celebration.` | e.status === 'DRAFT'; events.length===0; loading | [web/app/events/page.tsx:100](../../../web/app/events/page.tsx#L100) |
| UI-00235 | native-dialog | `Unpublish "${e.title}"?\n\nGuests will temporarily lose access to the invitation. You can publish it again later.` | e.status === 'PUBLISHED'; events.length===0; loading | [web/app/events/page.tsx:127](../../../web/app/events/page.tsx#L127) |
| UI-00072 | "status" | {localize("ui.bed9096c411e6da3",{defaultValue:"Opening your workspace…"})} | Parent/source state | [web/app/components/HostGlobalNav.tsx:17](../../../web/app/components/HostGlobalNav.tsx#L17) |
| UI-00206 | "alert" | {localizeText(error)} {error==='Please sign in'&&<Link href="/login">{localize("ui.b84cc3347b94fc0c",{defaultValue:"Go to login"})}</Link>} | error | [web/app/events/page.tsx:71](../../../web/app/events/page.tsx#L71) |
| UI-00548 | "status" | {localize("ui.7a005f0f1dbc704f",{defaultValue:"Opening your celebration space…"})} | ready\|\|discovery | [web/components/ExperienceProvider.tsx:19](../../../web/components/ExperienceProvider.tsx#L19) |
| UI-00790 | "alert" | {localizeText(error)} | error | [web/components/HostWorkspace.tsx:53](../../../web/components/HostWorkspace.tsx#L53) |
| UI-00909 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:19](../../../web/components/LocalizationProvider.tsx#L19) |
| UI-00911 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:20](../../../web/components/LocalizationProvider.tsx#L20) |
| UI-00913 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:21](../../../web/components/LocalizationProvider.tsx#L21) |
| UI-01081 | "alert" | {localizeText(error)} | error | [web/components/focused/EventList.tsx:16](../../../web/components/focused/EventList.tsx#L16) |
| UI-01083 | "status" | {localize("ui.b4bfe377d15d17aa",{defaultValue:"Loading celebrations…"})} | loading | [web/components/focused/EventList.tsx:16](../../../web/components/focused/EventList.tsx#L16) |


### Workflow state bindings

| Source | Binding | Initial expression |
| --- | --- | --- |
| web/app/events/page.tsx | loading | true |
| web/app/events/page.tsx | busy | false |
| web/app/events/page.tsx | error | '' |
| web/components/LocalizationProvider.tsx | busy | false |
| web/components/LocalizationProvider.tsx | error | '' |
| web/components/focused/EventList.tsx | loading | true |
| web/components/focused/EventList.tsx | error | '' |
| web/components/HostWorkspace.tsx | busy | false |
| web/components/HostWorkspace.tsx | error | '' |


## Celebration overview

| Screen information | Source-supported specification |
| --- | --- |
| Route | /events/[id] |
| Source | [web/app/events/[id]/page.tsx:1](../../../web/app/events/[id]/page.tsx#L1) |
| Purpose | Event checklist, invitation readiness, sharing/settings and enabled album entry |
| User access level | Authenticated event owner |
| Navigation source | Event card / event sidebar / related-page back link |
| Navigation destination | Receiving accounts, gifts, QR design, preview, album, publish/unpublish controls |
| Preconditions | Route parameters/query scope must be valid; applicable session/ownership/module/provider state above. Load errors do not imply an empty successful dataset. |
| Postconditions | Only the selected successful action changes its documented state. Navigation alone does not publish, confirm payment, verify contact or deliver a notification. |
| Route variables | id |
| UI variants | Focused/general experience, responsive layout and module/state predicates can change visible sections. Exact predicates are in each control record. |


### Wireframe layout

| Section | Placement / behavior / evidence |
| --- | --- |
| Header | Shared identity/event or page title/back/actions, where declared. Composite header declarations: 2. |
| Navigation / sidebar | Host/admin navigation and event context where applicable; mobile/focused variants are conditional. nav=3, aside=3. |
| Main content | Purpose-specific forms/cards/tables/canvas/player from the composed sources. main=5, section=8, article=0. |
| Search and filters | Fields/selects listed below; exact value/handler/visibility controls are not inferred from placeholder labels. |
| Data grids and cards | Table declarations=0. Data collections and generated rows/cards are in frontend.json dynamicSources. |
| Popups / dialogs | Named/source-declared dialog control records below; opening/closing/loading/apply/cancel actions belong to those component handlers. |
| Alerts / notifications | Source controls with role alert/status are listed below. Notice strings may come from API results; do not assume a universal envelope. |
| Footer | Local/shared footer declarations=1. No undocumented legal/footer wording is invented. |
| Loading / empty / success / error | Observed state bindings and relevant action fields are listed below. Expected QA states from Document 2 do not establish a spinner/retry control where source does not implement one. |


### Composed implementation

- [web/app/events/[id]/page.tsx:1](../../../web/app/events/[id]/page.tsx#L1)
- [web/components/focused/EventOverview.tsx:1](../../../web/components/focused/EventOverview.tsx#L1)
- [web/components/LocalizationProvider.tsx:1](../../../web/components/LocalizationProvider.tsx#L1)
- [web/components/VerificationProvider.tsx:1](../../../web/components/VerificationProvider.tsx#L1)
- [web/app/components/HostToast.tsx:1](../../../web/app/components/HostToast.tsx#L1)
- [web/components/DeliveryAddressFields.tsx:1](../../../web/components/DeliveryAddressFields.tsx#L1)
- [web/components/focused/FocusedOnly.tsx:1](../../../web/components/focused/FocusedOnly.tsx#L1)
- [web/components/ExperienceProvider.tsx:1](../../../web/components/ExperienceProvider.tsx#L1)
- [web/app/layout.tsx:1](../../../web/app/layout.tsx#L1)
- [web/components/GrowthProvider.tsx:1](../../../web/components/GrowthProvider.tsx#L1)
- [web/components/BrandingProvider.tsx:1](../../../web/components/BrandingProvider.tsx#L1)
- [web/components/LegalFooter.tsx:1](../../../web/components/LegalFooter.tsx#L1)
- [web/components/CookieNotice.tsx:1](../../../web/components/CookieNotice.tsx#L1)
- [web/app/components/ApplicationInteractions.tsx:1](../../../web/app/components/ApplicationInteractions.tsx#L1)
- [web/app/components/PerformanceDiagnostics.tsx:1](../../../web/app/components/PerformanceDiagnostics.tsx#L1)
- [web/app/components/HostSessionRedirect.tsx:1](../../../web/app/components/HostSessionRedirect.tsx#L1)
- [web/app/components/HostGlobalNav.tsx:1](../../../web/app/components/HostGlobalNav.tsx#L1)
- [web/components/HostWorkspace.tsx:1](../../../web/components/HostWorkspace.tsx#L1)
- [web/components/focused/HostShell.tsx:1](../../../web/components/focused/HostShell.tsx#L1)

### Fields

| Control ID | Label / expression | Type | Binding | Required | Native limits / format | Detailed specification |
| --- | --- | --- | --- | --- | --- | --- |
| UI-00538 | {localize("ui.455df35dd66a82ca",{defaultValue:"Same as event address"})} | input "checkbox" | {same} | Not declared |  | [control](UI_CONTROLS.md#ui-00538) |
| UI-00539 | {localizeText(label)} | input  | {address[key]} | true | maxLength={maxLength} | [control](UI_CONTROLS.md#ui-00539) |
| UI-00908 | {localize("ui.bba5b40bdd28c66e",{defaultValue:"Choose language"})} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00908) |
| UI-00910 | {label} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00910) |
| UI-00912 | {localize("ui.140071feea59fe87",{defaultValue:"Preferred language"})} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00912) |
| UI-01098 | {localize("ui.88c3be35063553d5",{defaultValue:"Invitation link"})} | input  | {invitation} | Not declared |  | [control](UI_CONTROLS.md#ui-01098) |


### Buttons

| ID | Label / expression | Action | Submit validation / handler | Source |
| --- | --- | --- | --- | --- |
| UI-00078 | × | {()=>{setFlash(null);onDismiss?.();}} | Not a native form submit or inherited from component context | [web/app/components/HostToast.tsx:16](../../../web/app/components/HostToast.tsx#L16) |
| UI-00528 | Accept optional analytics | {()=>{saveConsent(true);setShow(false)}} | Not a native form submit or inherited from component context | [web/components/CookieNotice.tsx:5](../../../web/components/CookieNotice.tsx#L5) |
| UI-00529 | Reject optional analytics | {()=>{saveConsent(false);setShow(false)}} | Not a native form submit or inherited from component context | [web/components/CookieNotice.tsx:5](../../../web/components/CookieNotice.tsx#L5) |
| UI-00788 | {collapsed?<PanelLeftOpen size={20}/>:<PanelLeftClose size={20}/>} | {toggle} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:50](../../../web/components/HostWorkspace.tsx#L50) |
| UI-00791 |  | {()=>void signOut()} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:53](../../../web/components/HostWorkspace.tsx#L53) |
| UI-00792 |  | {()=>setCollapsed(true)} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:54](../../../web/components/HostWorkspace.tsx#L54) |
| UI-00876 | Cookie preferences | {()=>window.dispatchEvent(new Event('giftwise-cookie-settings'))} | Not a native form submit or inherited from component context | [web/components/LegalFooter.tsx:4](../../../web/components/LegalFooter.tsx#L4) |
| UI-01086 | {localize("ui.82bdb5c811838b5e",{defaultValue:"Share invitation"})} | {()=>setSharing(true)} | Not a native form submit or inherited from component context | [web/components/focused/EventOverview.tsx:22](../../../web/components/focused/EventOverview.tsx#L22) |
| UI-01093 | {busy?localize("ui.582e0f1abad43e9e",{defaultValue:"Publishing…"}):localize("ui.b25616696efea57e",{defaultValue:"Publish invitation"})} | {()=>void publish()} | Not a native form submit or inherited from component context | [web/components/focused/EventOverview.tsx:22](../../../web/components/focused/EventOverview.tsx#L22) |
| UI-01094 | {localize("ui.e79aed376df91c81",{defaultValue:"Make invitation private"})} | {async()=>{if(!confirm('Make this invitation private? Existing guest links will temporarily stop working.'))return;setBusy(true);try{setEvent(await hostApi.unpublishEvent(fetch,event.id));setSharing(false)}catch{setError('Could not make this invitation private. Try again.')}finally{setBusy(false)}}} | Not a native form submit or inherited from component context | [web/components/focused/EventOverview.tsx:22](../../../web/components/focused/EventOverview.tsx#L22) |
| UI-01097 | ✕ | {()=>setSharing(false)} | Not a native form submit or inherited from component context | [web/components/focused/EventOverview.tsx:22](../../../web/components/focused/EventOverview.tsx#L22) |
| UI-01099 | {localize("ui.dbf362d4f210c780",{defaultValue:"Copy link"})} | {()=>trackedCopy(invitation).then(()=>setMessage('Invitation link copied.')).catch(()=>setMessage('Select and copy the link above.'))} | Not a native form submit or inherited from component context | [web/components/focused/EventOverview.tsx:22](../../../web/components/focused/EventOverview.tsx#L22) |


### Dialog and alert/status inventory

| ID | Role / kind | Text / label | Visibility conditions | Source |
| --- | --- | --- | --- | --- |
| UI-01095 | native-dialog | 'Make this invitation private? Existing guest links will temporarily stop working.' | published | [web/components/focused/EventOverview.tsx:22](../../../web/components/focused/EventOverview.tsx#L22) |
| UI-00072 | "status" | {localize("ui.bed9096c411e6da3",{defaultValue:"Opening your workspace…"})} | Parent/source state | [web/app/components/HostGlobalNav.tsx:17](../../../web/app/components/HostGlobalNav.tsx#L17) |
| UI-00548 | "status" | {localize("ui.7a005f0f1dbc704f",{defaultValue:"Opening your celebration space…"})} | ready\|\|discovery | [web/components/ExperienceProvider.tsx:19](../../../web/components/ExperienceProvider.tsx#L19) |
| UI-00790 | "alert" | {localizeText(error)} | error | [web/components/HostWorkspace.tsx:53](../../../web/components/HostWorkspace.tsx#L53) |
| UI-00909 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:19](../../../web/components/LocalizationProvider.tsx#L19) |
| UI-00911 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:20](../../../web/components/LocalizationProvider.tsx#L20) |
| UI-00913 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:21](../../../web/components/LocalizationProvider.tsx#L21) |
| UI-01085 | "alert" | {localizeText(error)} | error | [web/components/focused/EventOverview.tsx:22](../../../web/components/focused/EventOverview.tsx#L22) |
| UI-01096 | "status" | {localizeText(message)} | message | [web/components/focused/EventOverview.tsx:22](../../../web/components/focused/EventOverview.tsx#L22) |
| UI-01108 | "status" | {localize("ui.2ee32085a6223e09",{defaultValue:"Opening your celebrations…"})} | mode==='FOCUSED' | [web/components/focused/FocusedOnly.tsx:9](../../../web/components/focused/FocusedOnly.tsx#L9) |


### Workflow state bindings

| Source | Binding | Initial expression |
| --- | --- | --- |
| web/components/focused/EventOverview.tsx | error | '' |
| web/components/focused/EventOverview.tsx | busy | false |
| web/components/focused/EventOverview.tsx | message | '' |
| web/components/LocalizationProvider.tsx | busy | false |
| web/components/LocalizationProvider.tsx | error | '' |
| web/components/HostWorkspace.tsx | busy | false |
| web/components/HostWorkspace.tsx | error | '' |


## Private memory album

| Screen information | Source-supported specification |
| --- | --- |
| Route | /events/[id]/album |
| Source | [web/app/events/[id]/album/page.tsx:1](../../../web/app/events/[id]/album/page.tsx#L1) |
| Purpose | Chronological photo/message slideshow, paging, hidden-wish option and fullscreen |
| User access level | Authenticated owner; platform + host + event MEMORY_ALBUM grants |
| Navigation source | Event sidebar / overview / guestbook album link |
| Navigation destination | Playback/refresh/load more; back to celebration or share invitation from empty state |
| Preconditions | Route parameters/query scope must be valid; applicable session/ownership/module/provider state above. Load errors do not imply an empty successful dataset. |
| Postconditions | Only the selected successful action changes its documented state. Navigation alone does not publish, confirm payment, verify contact or deliver a notification. |
| Route variables | id |
| UI variants | Focused/general experience, responsive layout and module/state predicates can change visible sections. Exact predicates are in each control record. |


### Wireframe layout

| Section | Placement / behavior / evidence |
| --- | --- |
| Header | Shared identity/event or page title/back/actions, where declared. Composite header declarations: 3. |
| Navigation / sidebar | Host/admin navigation and event context where applicable; mobile/focused variants are conditional. nav=3, aside=3. |
| Main content | Purpose-specific forms/cards/tables/canvas/player from the composed sources. main=3, section=4, article=0. |
| Search and filters | Fields/selects listed below; exact value/handler/visibility controls are not inferred from placeholder labels. |
| Data grids and cards | Table declarations=0. Data collections and generated rows/cards are in frontend.json dynamicSources. |
| Popups / dialogs | Named/source-declared dialog control records below; opening/closing/loading/apply/cancel actions belong to those component handlers. |
| Alerts / notifications | Source controls with role alert/status are listed below. Notice strings may come from API results; do not assume a universal envelope. |
| Footer | Local/shared footer declarations=2. No undocumented legal/footer wording is invented. |
| Loading / empty / success / error | Observed state bindings and relevant action fields are listed below. Expected QA states from Document 2 do not establish a spinner/retry control where source does not implement one. |


### Composed implementation

- [web/app/events/[id]/album/page.tsx:1](../../../web/app/events/[id]/album/page.tsx#L1)
- [web/components/MemoryAlbum.tsx:1](../../../web/components/MemoryAlbum.tsx#L1)
- [web/components/LocalizationProvider.tsx:1](../../../web/components/LocalizationProvider.tsx#L1)
- [web/app/layout.tsx:1](../../../web/app/layout.tsx#L1)
- [web/components/GrowthProvider.tsx:1](../../../web/components/GrowthProvider.tsx#L1)
- [web/components/BrandingProvider.tsx:1](../../../web/components/BrandingProvider.tsx#L1)
- [web/components/LegalFooter.tsx:1](../../../web/components/LegalFooter.tsx#L1)
- [web/components/CookieNotice.tsx:1](../../../web/components/CookieNotice.tsx#L1)
- [web/app/components/ApplicationInteractions.tsx:1](../../../web/app/components/ApplicationInteractions.tsx#L1)
- [web/components/VerificationProvider.tsx:1](../../../web/components/VerificationProvider.tsx#L1)
- [web/components/ExperienceProvider.tsx:1](../../../web/components/ExperienceProvider.tsx#L1)
- [web/app/components/PerformanceDiagnostics.tsx:1](../../../web/app/components/PerformanceDiagnostics.tsx#L1)
- [web/app/components/HostSessionRedirect.tsx:1](../../../web/app/components/HostSessionRedirect.tsx#L1)
- [web/app/components/HostGlobalNav.tsx:1](../../../web/app/components/HostGlobalNav.tsx#L1)
- [web/components/HostWorkspace.tsx:1](../../../web/components/HostWorkspace.tsx#L1)
- [web/components/focused/HostShell.tsx:1](../../../web/components/focused/HostShell.tsx#L1)

### Fields

| Control ID | Label / expression | Type | Binding | Required | Native limits / format | Detailed specification |
| --- | --- | --- | --- | --- | --- | --- |
| UI-00908 | {localize("ui.bba5b40bdd28c66e",{defaultValue:"Choose language"})} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00908) |
| UI-00910 | {label} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00910) |
| UI-00912 | {localize("ui.140071feea59fe87",{defaultValue:"Preferred language"})} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00912) |
| UI-00916 | {localize("ui.5729ddc0c0cb1201",{defaultValue:" Include hidden wishes"})} | input "checkbox" | {hidden} | Not declared |  | [control](UI_CONTROLS.md#ui-00916) |
| UI-00924 | {localize("ui.e0adcfcf556ebd36",{defaultValue:"Take your time"})} | select  | {seconds} | Not declared |  | [control](UI_CONTROLS.md#ui-00924) |


### Buttons

| ID | Label / expression | Action | Submit validation / handler | Source |
| --- | --- | --- | --- | --- |
| UI-00528 | Accept optional analytics | {()=>{saveConsent(true);setShow(false)}} | Not a native form submit or inherited from component context | [web/components/CookieNotice.tsx:5](../../../web/components/CookieNotice.tsx#L5) |
| UI-00529 | Reject optional analytics | {()=>{saveConsent(false);setShow(false)}} | Not a native form submit or inherited from component context | [web/components/CookieNotice.tsx:5](../../../web/components/CookieNotice.tsx#L5) |
| UI-00788 | {collapsed?<PanelLeftOpen size={20}/>:<PanelLeftClose size={20}/>} | {toggle} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:50](../../../web/components/HostWorkspace.tsx#L50) |
| UI-00791 |  | {()=>void signOut()} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:53](../../../web/components/HostWorkspace.tsx#L53) |
| UI-00792 |  | {()=>setCollapsed(true)} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:54](../../../web/components/HostWorkspace.tsx#L54) |
| UI-00876 | Cookie preferences | {()=>window.dispatchEvent(new Event('giftwise-cookie-settings'))} | Not a native form submit or inherited from component context | [web/components/LegalFooter.tsx:4](../../../web/components/LegalFooter.tsx#L4) |
| UI-00917 | {localize("ui.7ce64141b163cd0d",{defaultValue:" Refresh"})} | {()=>void refresh()} | Not a native form submit or inherited from component context | [web/components/MemoryAlbum.tsx:57](../../../web/components/MemoryAlbum.tsx#L57) |
| UI-00921 |  | {()=>{setPlaying(false);void advance(-1)}} | Not a native form submit or inherited from component context | [web/components/MemoryAlbum.tsx:66](../../../web/components/MemoryAlbum.tsx#L66) |
| UI-00922 | {playing?<Pause size={18}/>:<Play size={18}/>} {playing?localize("ui.dca0331d5734fb7d",{defaultValue:"Pause the moment"}):localize("ui.22a40ecca59e6973",{defaultValue:"Play our memories"})} | {()=>setPlaying(value=>!value)} | Not a native form submit or inherited from component context | [web/components/MemoryAlbum.tsx:66](../../../web/components/MemoryAlbum.tsx#L66) |
| UI-00923 |  | {()=>{setPlaying(false);void advance(1)}} | Not a native form submit or inherited from component context | [web/components/MemoryAlbum.tsx:66](../../../web/components/MemoryAlbum.tsx#L66) |
| UI-00925 | {full?<Minimize size={18}/>:<Maximize size={18}/>} | {()=>void fullscreen()} | Not a native form submit or inherited from component context | [web/components/MemoryAlbum.tsx:66](../../../web/components/MemoryAlbum.tsx#L66) |
| UI-00926 | {memory.photoAvailable?<img loading="lazy" src={photo(memory)} alt="" onError={e=>{e.currentTarget.style.visibility='hidden'}}/>:<span className="ma-thumb-quote" aria-hidden="true">“</span>} | {()=>{setPlaying(false);setIndex(i)}} | Not a native form submit or inherited from component context | [web/components/MemoryAlbum.tsx:68](../../../web/components/MemoryAlbum.tsx#L68) |
| UI-00927 | {more?localize("ui.c4333689235b8b60",{defaultValue:"Gathering more memories…"}):localize("ui.645c7f6715d44a1a",{defaultValue:"Unfold more memories"})} | {()=>void loadMore()} | Not a native form submit or inherited from component context | [web/components/MemoryAlbum.tsx:69](../../../web/components/MemoryAlbum.tsx#L69) |


### Dialog and alert/status inventory

| ID | Role / kind | Text / label | Visibility conditions | Source |
| --- | --- | --- | --- | --- |
| UI-00072 | "status" | {localize("ui.bed9096c411e6da3",{defaultValue:"Opening your workspace…"})} | Parent/source state | [web/app/components/HostGlobalNav.tsx:17](../../../web/app/components/HostGlobalNav.tsx#L17) |
| UI-00548 | "status" | {localize("ui.7a005f0f1dbc704f",{defaultValue:"Opening your celebration space…"})} | ready\|\|discovery | [web/components/ExperienceProvider.tsx:19](../../../web/components/ExperienceProvider.tsx#L19) |
| UI-00790 | "alert" | {localizeText(error)} | error | [web/components/HostWorkspace.tsx:53](../../../web/components/HostWorkspace.tsx#L53) |
| UI-00909 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:19](../../../web/components/LocalizationProvider.tsx#L19) |
| UI-00911 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:20](../../../web/components/LocalizationProvider.tsx#L20) |
| UI-00913 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:21](../../../web/components/LocalizationProvider.tsx#L21) |
| UI-00918 | "alert" | {localizeText(error)} | error | [web/components/MemoryAlbum.tsx:58](../../../web/components/MemoryAlbum.tsx#L58) |
| UI-00919 | "status" |  | loading | [web/components/MemoryAlbum.tsx:59](../../../web/components/MemoryAlbum.tsx#L59) |


### Workflow state bindings

| Source | Binding | Initial expression |
| --- | --- | --- |
| web/components/MemoryAlbum.tsx | loading | true |
| web/components/MemoryAlbum.tsx | error | '' |
| web/components/LocalizationProvider.tsx | busy | false |
| web/components/LocalizationProvider.tsx | error | '' |
| web/components/HostWorkspace.tsx | busy | false |
| web/components/HostWorkspace.tsx | error | '' |


## Guests, responses and wishes

| Screen information | Source-supported specification |
| --- | --- |
| Route | /events/[id]/celebration |
| Source | [web/app/events/[id]/celebration/page.tsx:1](../../../web/app/events/[id]/celebration/page.tsx#L1) |
| Purpose | RSVP counts/filters and guestbook moderation/photo review |
| User access level | Authenticated event owner; RSVP/GUESTBOOK grants |
| Navigation source | Event sidebar / guest management |
| Navigation destination | Approve/hide wishes; album entry when enabled; refresh responses |
| Preconditions | Route parameters/query scope must be valid; applicable session/ownership/module/provider state above. Load errors do not imply an empty successful dataset. |
| Postconditions | Only the selected successful action changes its documented state. Navigation alone does not publish, confirm payment, verify contact or deliver a notification. |
| Route variables | id |
| UI variants | Focused/general experience, responsive layout and module/state predicates can change visible sections. Exact predicates are in each control record. |


### Wireframe layout

| Section | Placement / behavior / evidence |
| --- | --- |
| Header | Shared identity/event or page title/back/actions, where declared. Composite header declarations: 3. |
| Navigation / sidebar | Host/admin navigation and event context where applicable; mobile/focused variants are conditional. nav=3, aside=3. |
| Main content | Purpose-specific forms/cards/tables/canvas/player from the composed sources. main=3, section=5, article=2. |
| Search and filters | Fields/selects listed below; exact value/handler/visibility controls are not inferred from placeholder labels. |
| Data grids and cards | Table declarations=1. Data collections and generated rows/cards are in frontend.json dynamicSources. |
| Popups / dialogs | Named/source-declared dialog control records below; opening/closing/loading/apply/cancel actions belong to those component handlers. |
| Alerts / notifications | Source controls with role alert/status are listed below. Notice strings may come from API results; do not assume a universal envelope. |
| Footer | Local/shared footer declarations=1. No undocumented legal/footer wording is invented. |
| Loading / empty / success / error | Observed state bindings and relevant action fields are listed below. Expected QA states from Document 2 do not establish a spinner/retry control where source does not implement one. |


### Composed implementation

- [web/app/events/[id]/celebration/page.tsx:1](../../../web/app/events/[id]/celebration/page.tsx#L1)
- [web/components/LocalizationProvider.tsx:1](../../../web/components/LocalizationProvider.tsx#L1)
- [web/components/ExperienceProvider.tsx:1](../../../web/components/ExperienceProvider.tsx#L1)
- [web/app/layout.tsx:1](../../../web/app/layout.tsx#L1)
- [web/components/GrowthProvider.tsx:1](../../../web/components/GrowthProvider.tsx#L1)
- [web/components/BrandingProvider.tsx:1](../../../web/components/BrandingProvider.tsx#L1)
- [web/components/LegalFooter.tsx:1](../../../web/components/LegalFooter.tsx#L1)
- [web/components/CookieNotice.tsx:1](../../../web/components/CookieNotice.tsx#L1)
- [web/app/components/ApplicationInteractions.tsx:1](../../../web/app/components/ApplicationInteractions.tsx#L1)
- [web/components/VerificationProvider.tsx:1](../../../web/components/VerificationProvider.tsx#L1)
- [web/app/components/PerformanceDiagnostics.tsx:1](../../../web/app/components/PerformanceDiagnostics.tsx#L1)
- [web/app/components/HostSessionRedirect.tsx:1](../../../web/app/components/HostSessionRedirect.tsx#L1)
- [web/app/components/HostGlobalNav.tsx:1](../../../web/app/components/HostGlobalNav.tsx#L1)
- [web/components/HostWorkspace.tsx:1](../../../web/components/HostWorkspace.tsx#L1)
- [web/components/focused/HostShell.tsx:1](../../../web/components/focused/HostShell.tsx#L1)

### Fields

| Control ID | Label / expression | Type | Binding | Required | Native limits / format | Detailed specification |
| --- | --- | --- | --- | --- | --- | --- |
| UI-00114 | "ch-rsvp-filter" | select  | {filter} | Not declared |  | [control](UI_CONTROLS.md#ui-00114) |
| UI-00908 | {localize("ui.bba5b40bdd28c66e",{defaultValue:"Choose language"})} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00908) |
| UI-00910 | {label} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00910) |
| UI-00912 | {localize("ui.140071feea59fe87",{defaultValue:"Preferred language"})} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00912) |


### Buttons

| ID | Label / expression | Action | Submit validation / handler | Source |
| --- | --- | --- | --- | --- |
| UI-00110 | {localize("ui.0eb696a8732f2a8d",{defaultValue:"↻ Refresh responses"})} | {()=>void reload()} | Not a native form submit or inherited from component context | [web/app/events/[id]/celebration/page.tsx:34](../../../web/app/events/[id]/celebration/page.tsx#L34) |
| UI-00112 | {localize("ui.9b4c6d73aac13b77",{defaultValue:"Responses"})} | {()=>setTab('responses')} | Not a native form submit or inherited from component context | [web/app/events/[id]/celebration/page.tsx:35](../../../web/app/events/[id]/celebration/page.tsx#L35) |
| UI-00113 | {localize("ui.403cc21502ebdc1e",{defaultValue:"Wishes"})} | {()=>setTab('wishes')} | Not a native form submit or inherited from component context | [web/app/events/[id]/celebration/page.tsx:35](../../../web/app/events/[id]/celebration/page.tsx#L35) |
| UI-00116 | {w.status==='APPROVED'?localize("ui.94e22afc6b4ff377",{defaultValue:"✓ Approved"}):localize("ui.89682a334d5583aa",{defaultValue:"✓ Approve & publish"})} | {()=>void moderate(w.id,'APPROVED')} | Not a native form submit or inherited from component context | [web/app/events/[id]/celebration/page.tsx:38](../../../web/app/events/[id]/celebration/page.tsx#L38) |
| UI-00117 | {w.status==='HIDDEN'?localize("ui.7e6fefff0f0aeb6c",{defaultValue:"Hidden"}):localize("ui.50165099781955a1",{defaultValue:"Hide message"})} | {()=>void moderate(w.id,'HIDDEN')} | Not a native form submit or inherited from component context | [web/app/events/[id]/celebration/page.tsx:38](../../../web/app/events/[id]/celebration/page.tsx#L38) |
| UI-00528 | Accept optional analytics | {()=>{saveConsent(true);setShow(false)}} | Not a native form submit or inherited from component context | [web/components/CookieNotice.tsx:5](../../../web/components/CookieNotice.tsx#L5) |
| UI-00529 | Reject optional analytics | {()=>{saveConsent(false);setShow(false)}} | Not a native form submit or inherited from component context | [web/components/CookieNotice.tsx:5](../../../web/components/CookieNotice.tsx#L5) |
| UI-00788 | {collapsed?<PanelLeftOpen size={20}/>:<PanelLeftClose size={20}/>} | {toggle} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:50](../../../web/components/HostWorkspace.tsx#L50) |
| UI-00791 |  | {()=>void signOut()} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:53](../../../web/components/HostWorkspace.tsx#L53) |
| UI-00792 |  | {()=>setCollapsed(true)} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:54](../../../web/components/HostWorkspace.tsx#L54) |
| UI-00876 | Cookie preferences | {()=>window.dispatchEvent(new Event('giftwise-cookie-settings'))} | Not a native form submit or inherited from component context | [web/components/LegalFooter.tsx:4](../../../web/components/LegalFooter.tsx#L4) |


### Dialog and alert/status inventory

| ID | Role / kind | Text / label | Visibility conditions | Source |
| --- | --- | --- | --- | --- |
| UI-00072 | "status" | {localize("ui.bed9096c411e6da3",{defaultValue:"Opening your workspace…"})} | Parent/source state | [web/app/components/HostGlobalNav.tsx:17](../../../web/app/components/HostGlobalNav.tsx#L17) |
| UI-00111 | "alert" | {localizeText(error)} | error | [web/app/events/[id]/celebration/page.tsx:34](../../../web/app/events/[id]/celebration/page.tsx#L34) |
| UI-00548 | "status" | {localize("ui.7a005f0f1dbc704f",{defaultValue:"Opening your celebration space…"})} | ready\|\|discovery | [web/components/ExperienceProvider.tsx:19](../../../web/components/ExperienceProvider.tsx#L19) |
| UI-00790 | "alert" | {localizeText(error)} | error | [web/components/HostWorkspace.tsx:53](../../../web/components/HostWorkspace.tsx#L53) |
| UI-00909 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:19](../../../web/components/LocalizationProvider.tsx#L19) |
| UI-00911 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:20](../../../web/components/LocalizationProvider.tsx#L20) |
| UI-00913 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:21](../../../web/components/LocalizationProvider.tsx#L21) |


### Workflow state bindings

| Source | Binding | Initial expression |
| --- | --- | --- |
| web/app/events/[id]/celebration/page.tsx | tab | 'responses' |
| web/app/events/[id]/celebration/page.tsx | error | '' |
| web/app/events/[id]/celebration/page.tsx | loading | true |
| web/components/LocalizationProvider.tsx | busy | false |
| web/components/LocalizationProvider.tsx | error | '' |
| web/components/HostWorkspace.tsx | busy | false |
| web/components/HostWorkspace.tsx | error | '' |


## Contribution report

| Screen information | Source-supported specification |
| --- | --- |
| Route | /events/[id]/contributions |
| Source | [web/app/events/[id]/contributions/page.tsx:1](../../../web/app/events/[id]/contributions/page.tsx#L1) |
| Purpose | Filter/search/page/export contributions and view individual/group reports |
| User access level | Authenticated event owner |
| Navigation source | Event sidebar / overview |
| Navigation destination | Read scoped totals/details, export and inspect relevant gift/group |
| Preconditions | Route parameters/query scope must be valid; applicable session/ownership/module/provider state above. Load errors do not imply an empty successful dataset. |
| Postconditions | Only the selected successful action changes its documented state. Navigation alone does not publish, confirm payment, verify contact or deliver a notification. |
| Route variables | id |
| UI variants | Focused/general experience, responsive layout and module/state predicates can change visible sections. Exact predicates are in each control record. |


### Wireframe layout

| Section | Placement / behavior / evidence |
| --- | --- |
| Header | Shared identity/event or page title/back/actions, where declared. Composite header declarations: 2. |
| Navigation / sidebar | Host/admin navigation and event context where applicable; mobile/focused variants are conditional. nav=4, aside=3. |
| Main content | Purpose-specific forms/cards/tables/canvas/player from the composed sources. main=3, section=5, article=2. |
| Search and filters | Fields/selects listed below; exact value/handler/visibility controls are not inferred from placeholder labels. |
| Data grids and cards | Table declarations=1. Data collections and generated rows/cards are in frontend.json dynamicSources. |
| Popups / dialogs | Named/source-declared dialog control records below; opening/closing/loading/apply/cancel actions belong to those component handlers. |
| Alerts / notifications | Source controls with role alert/status are listed below. Notice strings may come from API results; do not assume a universal envelope. |
| Footer | Local/shared footer declarations=1. No undocumented legal/footer wording is invented. |
| Loading / empty / success / error | Observed state bindings and relevant action fields are listed below. Expected QA states from Document 2 do not establish a spinner/retry control where source does not implement one. |


### Composed implementation

- [web/app/events/[id]/contributions/page.tsx:1](../../../web/app/events/[id]/contributions/page.tsx#L1)
- [web/components/LocalizationProvider.tsx:1](../../../web/components/LocalizationProvider.tsx#L1)
- [web/components/GroupCollectionReport.tsx:1](../../../web/components/GroupCollectionReport.tsx#L1)
- [web/components/GroupActivity.tsx:1](../../../web/components/GroupActivity.tsx#L1)
- [web/components/PublishedGuestGifts.tsx:1](../../../web/components/PublishedGuestGifts.tsx#L1)
- [web/components/ExperienceProvider.tsx:1](../../../web/components/ExperienceProvider.tsx#L1)
- [web/components/focused/ResponsiveTable.tsx:1](../../../web/components/focused/ResponsiveTable.tsx#L1)
- [web/app/layout.tsx:1](../../../web/app/layout.tsx#L1)
- [web/components/GrowthProvider.tsx:1](../../../web/components/GrowthProvider.tsx#L1)
- [web/components/BrandingProvider.tsx:1](../../../web/components/BrandingProvider.tsx#L1)
- [web/components/LegalFooter.tsx:1](../../../web/components/LegalFooter.tsx#L1)
- [web/components/CookieNotice.tsx:1](../../../web/components/CookieNotice.tsx#L1)
- [web/app/components/ApplicationInteractions.tsx:1](../../../web/app/components/ApplicationInteractions.tsx#L1)
- [web/components/VerificationProvider.tsx:1](../../../web/components/VerificationProvider.tsx#L1)
- [web/app/components/PerformanceDiagnostics.tsx:1](../../../web/app/components/PerformanceDiagnostics.tsx#L1)
- [web/app/components/HostSessionRedirect.tsx:1](../../../web/app/components/HostSessionRedirect.tsx#L1)
- [web/app/components/HostGlobalNav.tsx:1](../../../web/app/components/HostGlobalNav.tsx#L1)
- [web/components/HostWorkspace.tsx:1](../../../web/components/HostWorkspace.tsx#L1)
- [web/components/focused/HostShell.tsx:1](../../../web/components/focused/HostShell.tsx#L1)

### Fields

| Control ID | Label / expression | Type | Binding | Required | Native limits / format | Detailed specification |
| --- | --- | --- | --- | --- | --- | --- |
| UI-00120 | {localize("ui.01c26b590041ccb7",{defaultValue:"Search contributions"})} | input "search" | {search} | Not declared | maxLength={120} | [control](UI_CONTROLS.md#ui-00120) |
| UI-00122 | {localize("ui.217d7015f739d301",{defaultValue:"Select gift"})} | select  | {giftId} | Not declared |  | [control](UI_CONTROLS.md#ui-00122) |
| UI-00123 | {localize("ui.126e55b257ac9c81",{defaultValue:"Contribution"})} | select  | {type} | Not declared |  | [control](UI_CONTROLS.md#ui-00123) |
| UI-00124 | {localize("ui.920e413c7d411b61",{defaultValue:"Status"})} | select  | {status} | Not declared |  | [control](UI_CONTROLS.md#ui-00124) |
| UI-00125 | {localize("ui.5e9a4c90801e55c2",{defaultValue:"From date UTC"})} | input "date" | {from} | Not declared | max={to\|\|undefined} | [control](UI_CONTROLS.md#ui-00125) |
| UI-00126 | {localize("ui.969484056d674a71",{defaultValue:"To date UTC"})} | input "date" | {to} | Not declared | min={from\|\|undefined} | [control](UI_CONTROLS.md#ui-00126) |
| UI-00132 | {localize("ui.9b18eb21547c9c65",{defaultValue:"Rows per page "})} | select  | {pageSize} | Not declared |  | [control](UI_CONTROLS.md#ui-00132) |
| UI-00908 | {localize("ui.bba5b40bdd28c66e",{defaultValue:"Choose language"})} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00908) |
| UI-00910 | {label} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00910) |
| UI-00912 | {localize("ui.140071feea59fe87",{defaultValue:"Preferred language"})} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00912) |


### Buttons

| ID | Label / expression | Action | Submit validation / handler | Source |
| --- | --- | --- | --- | --- |
| UI-00121 | {localize("ui.f57f3af427df697a",{defaultValue:"Filter contributions"})} | {()=>setFilterOpen(v=>!v)} | Not a native form submit or inherited from component context | [web/app/events/[id]/contributions/page.tsx:50](../../../web/app/events/[id]/contributions/page.tsx#L50) |
| UI-00127 | {localize("ui.0e91610117029a62",{defaultValue:"Refresh"})} | {()=>void load()} | Not a native form submit or inherited from component context | [web/app/events/[id]/contributions/page.tsx:57](../../../web/app/events/[id]/contributions/page.tsx#L57) |
| UI-00128 | {exporting?localize("ui.a2dd9cbb7013bb70",{defaultValue:"Exporting…"}):localize("ui.91f71c14c8d6cc64",{defaultValue:"Export CSV"})} | {()=>void exportCsv()} | Not a native form submit or inherited from component context | [web/app/events/[id]/contributions/page.tsx:58](../../../web/app/events/[id]/contributions/page.tsx#L58) |
| UI-00134 | {localize("ui.a57b08a480b822a0",{defaultValue:"Previous"})} | {()=>setOffset(Math.max(0,offset-pageSize))} | Not a native form submit or inherited from component context | [web/app/events/[id]/contributions/page.tsx:63](../../../web/app/events/[id]/contributions/page.tsx#L63) |
| UI-00135 | {localize("ui.1ff57a29d7c9d11b",{defaultValue:"Next"})} | {()=>setOffset(offset+pageSize)} | Not a native form submit or inherited from component context | [web/app/events/[id]/contributions/page.tsx:63](../../../web/app/events/[id]/contributions/page.tsx#L63) |
| UI-00528 | Accept optional analytics | {()=>{saveConsent(true);setShow(false)}} | Not a native form submit or inherited from component context | [web/components/CookieNotice.tsx:5](../../../web/components/CookieNotice.tsx#L5) |
| UI-00529 | Reject optional analytics | {()=>{saveConsent(false);setShow(false)}} | Not a native form submit or inherited from component context | [web/components/CookieNotice.tsx:5](../../../web/components/CookieNotice.tsx#L5) |
| UI-00553 | {localize("ui.c9bca82b557e962e",{defaultValue:"Newer activity"})} | {()=>setOffset(value=>Math.max(0,value-100))} | Not a native form submit or inherited from component context | [web/components/GroupActivity.tsx:13](../../../web/components/GroupActivity.tsx#L13) |
| UI-00554 | {localize("ui.0eed19abab86afcc",{defaultValue:"Older activity"})} | {()=>setOffset(value=>value+100)} | Not a native form submit or inherited from component context | [web/components/GroupActivity.tsx:13](../../../web/components/GroupActivity.tsx#L13) |
| UI-00556 | {localize("ui.f4391af539414b54",{defaultValue:"View contribution details"})} | {async()=>{try{const response=await fetch(`/backend/api/v1/events/${eventId}/contributions/groups/${r.id}`,{credentials:'include',cache:'no-store'});if(!response.ok)throw Error('Could not load group details');const data=await response.json();setDetails(x=>({...x,[r.id]:data}))}catch(e){setError(e instanceof Error?e.message:'Could not load details')}}} | Not a native form submit or inherited from component context | [web/components/GroupCollectionReport.tsx:14](../../../web/components/GroupCollectionReport.tsx#L14) |
| UI-00788 | {collapsed?<PanelLeftOpen size={20}/>:<PanelLeftClose size={20}/>} | {toggle} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:50](../../../web/components/HostWorkspace.tsx#L50) |
| UI-00791 |  | {()=>void signOut()} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:53](../../../web/components/HostWorkspace.tsx#L53) |
| UI-00792 |  | {()=>setCollapsed(true)} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:54](../../../web/components/HostWorkspace.tsx#L54) |
| UI-00876 | Cookie preferences | {()=>window.dispatchEvent(new Event('giftwise-cookie-settings'))} | Not a native form submit or inherited from component context | [web/components/LegalFooter.tsx:4](../../../web/components/LegalFooter.tsx#L4) |
| UI-00981 | {localize("ui.ab7ec41a34509538",{defaultValue:"Refresh gifts"})} | {()=>void load()} | Not a native form submit or inherited from component context | [web/components/PublishedGuestGifts.tsx:14](../../../web/components/PublishedGuestGifts.tsx#L14) |


### Dialog and alert/status inventory

| ID | Role / kind | Text / label | Visibility conditions | Source |
| --- | --- | --- | --- | --- |
| UI-00072 | "status" | {localize("ui.bed9096c411e6da3",{defaultValue:"Opening your workspace…"})} | Parent/source state | [web/app/components/HostGlobalNav.tsx:17](../../../web/app/components/HostGlobalNav.tsx#L17) |
| UI-00129 | "alert" | {localizeText(error)} {error.includes('(401)')&&<Link href="/login">{localize("ui.51fbe1dc8a7319b7",{defaultValue:"Sign in again"})}</Link>} | error | [web/app/events/[id]/contributions/page.tsx:59](../../../web/app/events/[id]/contributions/page.tsx#L59) |
| UI-00131 | "status" | {localize("ui.ba3bbbe10d8bef66",{defaultValue:"Loading…"})} | loading | [web/app/events/[id]/contributions/page.tsx:59](../../../web/app/events/[id]/contributions/page.tsx#L59) |
| UI-00133 | "status" | {report.matchingCount===0?localize("ui.ff7009f4a9652ed5",{defaultValue:"0 results"}):localize("ui.0bd34e0f6d9b5fa4",{defaultValue:"{{value0}}–{{value1}} of {{value2}}",value0:(offset+1),value1:(Math.min(offset+report.entries.length,report.matchingCount)),value2:(report.matchingCount)})} {localize("ui.e85181afe2a39a70",{defaultValue:" · Page "})} {Math.floor(offset/pageSize)+1} {localize("ui.a4282e4b2298b90b",{defaultValue:" of "})} {Math.max(1,Math.ceil(report.matchingCount/pageSize))} | !loading; report | [web/app/events/[id]/contributions/page.tsx:63](../../../web/app/events/[id]/contributions/page.tsx#L63) |
| UI-00548 | "status" | {localize("ui.7a005f0f1dbc704f",{defaultValue:"Opening your celebration space…"})} | ready\|\|discovery | [web/components/ExperienceProvider.tsx:19](../../../web/components/ExperienceProvider.tsx#L19) |
| UI-00552 | "alert" | {localizeText(error)} | error | [web/components/GroupActivity.tsx:13](../../../web/components/GroupActivity.tsx#L13) |
| UI-00555 | "alert" | {localizeText(error)} | error | [web/components/GroupCollectionReport.tsx:14](../../../web/components/GroupCollectionReport.tsx#L14) |
| UI-00790 | "alert" | {localizeText(error)} | error | [web/components/HostWorkspace.tsx:53](../../../web/components/HostWorkspace.tsx#L53) |
| UI-00909 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:19](../../../web/components/LocalizationProvider.tsx#L19) |
| UI-00911 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:20](../../../web/components/LocalizationProvider.tsx#L20) |
| UI-00913 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:21](../../../web/components/LocalizationProvider.tsx#L21) |
| UI-00982 | "alert" | {localizeText(error)} | error | [web/components/PublishedGuestGifts.tsx:14](../../../web/components/PublishedGuestGifts.tsx#L14) |
| UI-00983 | "status" | {localize("ui.20fc5947f275b14c",{defaultValue:"Loading gifts…"})} | loading | [web/components/PublishedGuestGifts.tsx:14](../../../web/components/PublishedGuestGifts.tsx#L14) |


### Workflow state bindings

| Source | Binding | Initial expression |
| --- | --- | --- |
| web/app/events/[id]/contributions/page.tsx | filterOpen | true |
| web/app/events/[id]/contributions/page.tsx | error | '' |
| web/app/events/[id]/contributions/page.tsx | loading | true |
| web/components/LocalizationProvider.tsx | busy | false |
| web/components/LocalizationProvider.tsx | error | '' |
| web/components/GroupCollectionReport.tsx | error | '' |
| web/components/GroupActivity.tsx | error | '' |
| web/components/PublishedGuestGifts.tsx | error | '' |
| web/components/PublishedGuestGifts.tsx | loading | true |
| web/components/HostWorkspace.tsx | busy | false |
| web/components/HostWorkspace.tsx | error | '' |


## Activity and notifications

| Screen information | Source-supported specification |
| --- | --- |
| Route | /events/[id]/notifications |
| Source | [web/app/events/[id]/notifications/page.tsx:1](../../../web/app/events/[id]/notifications/page.tsx#L1) |
| Purpose | View recorded notifications/activity and group notices |
| User access level | Authenticated event owner |
| Navigation source | Event sidebar / reports |
| Navigation destination | Refresh/page/read relevant history; recorded intent remains distinct from delivery |
| Preconditions | Route parameters/query scope must be valid; applicable session/ownership/module/provider state above. Load errors do not imply an empty successful dataset. |
| Postconditions | Only the selected successful action changes its documented state. Navigation alone does not publish, confirm payment, verify contact or deliver a notification. |
| Route variables | id |
| UI variants | Focused/general experience, responsive layout and module/state predicates can change visible sections. Exact predicates are in each control record. |


### Wireframe layout

| Section | Placement / behavior / evidence |
| --- | --- |
| Header | Shared identity/event or page title/back/actions, where declared. Composite header declarations: 2. |
| Navigation / sidebar | Host/admin navigation and event context where applicable; mobile/focused variants are conditional. nav=3, aside=3. |
| Main content | Purpose-specific forms/cards/tables/canvas/player from the composed sources. main=3, section=4, article=1. |
| Search and filters | Fields/selects listed below; exact value/handler/visibility controls are not inferred from placeholder labels. |
| Data grids and cards | Table declarations=2. Data collections and generated rows/cards are in frontend.json dynamicSources. |
| Popups / dialogs | Named/source-declared dialog control records below; opening/closing/loading/apply/cancel actions belong to those component handlers. |
| Alerts / notifications | Source controls with role alert/status are listed below. Notice strings may come from API results; do not assume a universal envelope. |
| Footer | Local/shared footer declarations=1. No undocumented legal/footer wording is invented. |
| Loading / empty / success / error | Observed state bindings and relevant action fields are listed below. Expected QA states from Document 2 do not establish a spinner/retry control where source does not implement one. |


### Composed implementation

- [web/app/events/[id]/notifications/page.tsx:1](../../../web/app/events/[id]/notifications/page.tsx#L1)
- [web/components/EventInvitationEmail.tsx:1](../../../web/components/EventInvitationEmail.tsx#L1)
- [web/components/LocalizationProvider.tsx:1](../../../web/components/LocalizationProvider.tsx#L1)
- [web/components/GroupCollectionReport.tsx:1](../../../web/components/GroupCollectionReport.tsx#L1)
- [web/components/GroupActivity.tsx:1](../../../web/components/GroupActivity.tsx#L1)
- [web/components/ExperienceProvider.tsx:1](../../../web/components/ExperienceProvider.tsx#L1)
- [web/components/focused/ResponsiveTable.tsx:1](../../../web/components/focused/ResponsiveTable.tsx#L1)
- [web/app/layout.tsx:1](../../../web/app/layout.tsx#L1)
- [web/components/GrowthProvider.tsx:1](../../../web/components/GrowthProvider.tsx#L1)
- [web/components/BrandingProvider.tsx:1](../../../web/components/BrandingProvider.tsx#L1)
- [web/components/LegalFooter.tsx:1](../../../web/components/LegalFooter.tsx#L1)
- [web/components/CookieNotice.tsx:1](../../../web/components/CookieNotice.tsx#L1)
- [web/app/components/ApplicationInteractions.tsx:1](../../../web/app/components/ApplicationInteractions.tsx#L1)
- [web/components/VerificationProvider.tsx:1](../../../web/components/VerificationProvider.tsx#L1)
- [web/app/components/PerformanceDiagnostics.tsx:1](../../../web/app/components/PerformanceDiagnostics.tsx#L1)
- [web/app/components/HostSessionRedirect.tsx:1](../../../web/app/components/HostSessionRedirect.tsx#L1)
- [web/app/components/HostGlobalNav.tsx:1](../../../web/app/components/HostGlobalNav.tsx#L1)
- [web/components/HostWorkspace.tsx:1](../../../web/components/HostWorkspace.tsx#L1)
- [web/components/focused/HostShell.tsx:1](../../../web/components/focused/HostShell.tsx#L1)

### Fields

| Control ID | Label / expression | Type | Binding | Required | Native limits / format | Detailed specification |
| --- | --- | --- | --- | --- | --- | --- |
| UI-00544 | Guest email | input "email" | {email} | true | maxLength={254} | [control](UI_CONTROLS.md#ui-00544) |
| UI-00908 | {localize("ui.bba5b40bdd28c66e",{defaultValue:"Choose language"})} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00908) |
| UI-00910 | {label} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00910) |
| UI-00912 | {localize("ui.140071feea59fe87",{defaultValue:"Preferred language"})} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00912) |


### Buttons

| ID | Label / expression | Action | Submit validation / handler | Source |
| --- | --- | --- | --- | --- |
| UI-00528 | Accept optional analytics | {()=>{saveConsent(true);setShow(false)}} | Not a native form submit or inherited from component context | [web/components/CookieNotice.tsx:5](../../../web/components/CookieNotice.tsx#L5) |
| UI-00529 | Reject optional analytics | {()=>{saveConsent(false);setShow(false)}} | Not a native form submit or inherited from component context | [web/components/CookieNotice.tsx:5](../../../web/components/CookieNotice.tsx#L5) |
| UI-00545 | {busy?'Queuing invitation…':'Send invitation email'} | submit / component action | {send} | [web/components/EventInvitationEmail.tsx:3](../../../web/components/EventInvitationEmail.tsx#L3) |
| UI-00553 | {localize("ui.c9bca82b557e962e",{defaultValue:"Newer activity"})} | {()=>setOffset(value=>Math.max(0,value-100))} | Not a native form submit or inherited from component context | [web/components/GroupActivity.tsx:13](../../../web/components/GroupActivity.tsx#L13) |
| UI-00554 | {localize("ui.0eed19abab86afcc",{defaultValue:"Older activity"})} | {()=>setOffset(value=>value+100)} | Not a native form submit or inherited from component context | [web/components/GroupActivity.tsx:13](../../../web/components/GroupActivity.tsx#L13) |
| UI-00556 | {localize("ui.f4391af539414b54",{defaultValue:"View contribution details"})} | {async()=>{try{const response=await fetch(`/backend/api/v1/events/${eventId}/contributions/groups/${r.id}`,{credentials:'include',cache:'no-store'});if(!response.ok)throw Error('Could not load group details');const data=await response.json();setDetails(x=>({...x,[r.id]:data}))}catch(e){setError(e instanceof Error?e.message:'Could not load details')}}} | Not a native form submit or inherited from component context | [web/components/GroupCollectionReport.tsx:14](../../../web/components/GroupCollectionReport.tsx#L14) |
| UI-00788 | {collapsed?<PanelLeftOpen size={20}/>:<PanelLeftClose size={20}/>} | {toggle} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:50](../../../web/components/HostWorkspace.tsx#L50) |
| UI-00791 |  | {()=>void signOut()} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:53](../../../web/components/HostWorkspace.tsx#L53) |
| UI-00792 |  | {()=>setCollapsed(true)} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:54](../../../web/components/HostWorkspace.tsx#L54) |
| UI-00876 | Cookie preferences | {()=>window.dispatchEvent(new Event('giftwise-cookie-settings'))} | Not a native form submit or inherited from component context | [web/components/LegalFooter.tsx:4](../../../web/components/LegalFooter.tsx#L4) |


### Dialog and alert/status inventory

| ID | Role / kind | Text / label | Visibility conditions | Source |
| --- | --- | --- | --- | --- |
| UI-00072 | "status" | {localize("ui.bed9096c411e6da3",{defaultValue:"Opening your workspace…"})} | Parent/source state | [web/app/components/HostGlobalNav.tsx:17](../../../web/app/components/HostGlobalNav.tsx#L17) |
| UI-00139 | "status" | {localize("ui.eae12e0d3bab022d",{defaultValue:"Loading notifications…"})} | loading | [web/app/events/[id]/notifications/page.tsx:116](../../../web/app/events/[id]/notifications/page.tsx#L116) |
| UI-00140 | "alert" | {localizeText(error)} | error | [web/app/events/[id]/notifications/page.tsx:118](../../../web/app/events/[id]/notifications/page.tsx#L118) |
| UI-00546 | "alert" | {error} | error | [web/components/EventInvitationEmail.tsx:3](../../../web/components/EventInvitationEmail.tsx#L3) |
| UI-00547 | "status" | {message} | message | [web/components/EventInvitationEmail.tsx:3](../../../web/components/EventInvitationEmail.tsx#L3) |
| UI-00548 | "status" | {localize("ui.7a005f0f1dbc704f",{defaultValue:"Opening your celebration space…"})} | ready\|\|discovery | [web/components/ExperienceProvider.tsx:19](../../../web/components/ExperienceProvider.tsx#L19) |
| UI-00552 | "alert" | {localizeText(error)} | error | [web/components/GroupActivity.tsx:13](../../../web/components/GroupActivity.tsx#L13) |
| UI-00555 | "alert" | {localizeText(error)} | error | [web/components/GroupCollectionReport.tsx:14](../../../web/components/GroupCollectionReport.tsx#L14) |
| UI-00790 | "alert" | {localizeText(error)} | error | [web/components/HostWorkspace.tsx:53](../../../web/components/HostWorkspace.tsx#L53) |
| UI-00909 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:19](../../../web/components/LocalizationProvider.tsx#L19) |
| UI-00911 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:20](../../../web/components/LocalizationProvider.tsx#L20) |
| UI-00913 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:21](../../../web/components/LocalizationProvider.tsx#L21) |


### Workflow state bindings

| Source | Binding | Initial expression |
| --- | --- | --- |
| web/app/events/[id]/notifications/page.tsx | loading | true |
| web/app/events/[id]/notifications/page.tsx | error | '' |
| web/components/EventInvitationEmail.tsx | busy | false |
| web/components/EventInvitationEmail.tsx | message | '' |
| web/components/EventInvitationEmail.tsx | error | '' |
| web/components/LocalizationProvider.tsx | busy | false |
| web/components/LocalizationProvider.tsx | error | '' |
| web/components/GroupCollectionReport.tsx | error | '' |
| web/components/GroupActivity.tsx | error | '' |
| web/components/HostWorkspace.tsx | busy | false |
| web/components/HostWorkspace.tsx | error | '' |


## Celebration settings

| Screen information | Source-supported specification |
| --- | --- |
| Route | /events/[id]/planning |
| Source | [web/app/events/[id]/planning/page.tsx:1](../../../web/app/events/[id]/planning/page.tsx#L1) |
| Purpose | Optional gifting, welcome, explicit public venue/timezone/calendar settings and safe repeat drafts |
| User access level | Authenticated owner; CELEBRATION_PLANNING / EVENT_REUSE at platform, host and event |
| Navigation source | Event sidebar / overview settings link |
| Navigation destination | Save draft settings, preview, repeat as new private draft, return to events |
| Preconditions | Route parameters/query scope must be valid; applicable session/ownership/module/provider state above. Load errors do not imply an empty successful dataset. |
| Postconditions | Only the selected successful action changes its documented state. Navigation alone does not publish, confirm payment, verify contact or deliver a notification. |
| Route variables | id |
| UI variants | Focused/general experience, responsive layout and module/state predicates can change visible sections. Exact predicates are in each control record. |


### Wireframe layout

| Section | Placement / behavior / evidence |
| --- | --- |
| Header | Shared identity/event or page title/back/actions, where declared. Composite header declarations: 3. |
| Navigation / sidebar | Host/admin navigation and event context where applicable; mobile/focused variants are conditional. nav=3, aside=3. |
| Main content | Purpose-specific forms/cards/tables/canvas/player from the composed sources. main=3, section=4, article=0. |
| Search and filters | Fields/selects listed below; exact value/handler/visibility controls are not inferred from placeholder labels. |
| Data grids and cards | Table declarations=0. Data collections and generated rows/cards are in frontend.json dynamicSources. |
| Popups / dialogs | Named/source-declared dialog control records below; opening/closing/loading/apply/cancel actions belong to those component handlers. |
| Alerts / notifications | Source controls with role alert/status are listed below. Notice strings may come from API results; do not assume a universal envelope. |
| Footer | Local/shared footer declarations=1. No undocumented legal/footer wording is invented. |
| Loading / empty / success / error | Observed state bindings and relevant action fields are listed below. Expected QA states from Document 2 do not establish a spinner/retry control where source does not implement one. |


### Composed implementation

- [web/app/events/[id]/planning/page.tsx:1](../../../web/app/events/[id]/planning/page.tsx#L1)
- [web/components/CelebrationSettings.tsx:1](../../../web/components/CelebrationSettings.tsx#L1)
- [web/components/LocalizationProvider.tsx:1](../../../web/components/LocalizationProvider.tsx#L1)
- [web/app/layout.tsx:1](../../../web/app/layout.tsx#L1)
- [web/components/GrowthProvider.tsx:1](../../../web/components/GrowthProvider.tsx#L1)
- [web/components/BrandingProvider.tsx:1](../../../web/components/BrandingProvider.tsx#L1)
- [web/components/LegalFooter.tsx:1](../../../web/components/LegalFooter.tsx#L1)
- [web/components/CookieNotice.tsx:1](../../../web/components/CookieNotice.tsx#L1)
- [web/app/components/ApplicationInteractions.tsx:1](../../../web/app/components/ApplicationInteractions.tsx#L1)
- [web/components/VerificationProvider.tsx:1](../../../web/components/VerificationProvider.tsx#L1)
- [web/components/ExperienceProvider.tsx:1](../../../web/components/ExperienceProvider.tsx#L1)
- [web/app/components/PerformanceDiagnostics.tsx:1](../../../web/app/components/PerformanceDiagnostics.tsx#L1)
- [web/app/components/HostSessionRedirect.tsx:1](../../../web/app/components/HostSessionRedirect.tsx#L1)
- [web/app/components/HostGlobalNav.tsx:1](../../../web/app/components/HostGlobalNav.tsx#L1)
- [web/components/HostWorkspace.tsx:1](../../../web/components/HostWorkspace.tsx#L1)
- [web/components/focused/HostShell.tsx:1](../../../web/components/focused/HostShell.tsx#L1)

### Fields

| Control ID | Label / expression | Type | Binding | Required | Native limits / format | Detailed specification |
| --- | --- | --- | --- | --- | --- | --- |
| UI-00484 | {localize("ui.547d3ef63225ecb4",{defaultValue:" Welcome gifts and contributions"})} | input "checkbox" | {settings.giftingEnabled} | Not declared |  | [control](UI_CONTROLS.md#ui-00484) |
| UI-00485 | {localize("ui.c14eee30fcece73a",{defaultValue:"A warm welcome · optional"})} | textarea  | {settings.welcomeMessage} | Not declared | maxLength={500} | [control](UI_CONTROLS.md#ui-00485) |
| UI-00487 | {localize("ui.9f6ef01555fe46a7",{defaultValue:"Start time · optional"})} | input "time" | {settings.startTime\|\|''} | Not declared |  | [control](UI_CONTROLS.md#ui-00487) |
| UI-00488 | {localize("ui.4ceca1d52cede44d",{defaultValue:"Timezone"})} | input  | {settings.timezone} | true | maxLength={80} | [control](UI_CONTROLS.md#ui-00488) |
| UI-00489 | {localize("ui.91d7528868594410",{defaultValue:"Duration in minutes"})} | input "number" | {settings.durationMinutes} | true | min={15}; max={1440} | [control](UI_CONTROLS.md#ui-00489) |
| UI-00490 | {localize("ui.fe81f63e49746d7b",{defaultValue:" Show the event address and arrival notes to people with the invitation link"})} | input "checkbox" | {settings.shareVenue} | Not declared |  | [control](UI_CONTROLS.md#ui-00490) |
| UI-00491 | {localize("ui.2e98288e3eacd461",{defaultValue:"Parking, directions or accessibility information · optional"})} | textarea  | {settings.arrivalNotes} | Not declared | maxLength={1000} | [control](UI_CONTROLS.md#ui-00491) |
| UI-00493 | {localize("ui.034cf3bc08f7f4e3",{defaultValue:"New celebration name"})} | input  | {title} | true | maxLength={200} | [control](UI_CONTROLS.md#ui-00493) |
| UI-00908 | {localize("ui.bba5b40bdd28c66e",{defaultValue:"Choose language"})} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00908) |
| UI-00910 | {label} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00910) |
| UI-00912 | {localize("ui.140071feea59fe87",{defaultValue:"Preferred language"})} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00912) |


### Buttons

| ID | Label / expression | Action | Submit validation / handler | Source |
| --- | --- | --- | --- | --- |
| UI-00479 | {localize("ui.7b9e42ee5b80f7a8",{defaultValue:"Reload settings"})} | {()=>void load()} | Not a native form submit or inherited from component context | [web/components/CelebrationSettings.tsx:18](../../../web/components/CelebrationSettings.tsx#L18) |
| UI-00492 | {busy?localize("ui.23e39291d6135814",{defaultValue:"Saving…"}):localize("ui.4539e8bc3f484c55",{defaultValue:"Save celebration settings"})} | submit / component action | {save} | [web/components/CelebrationSettings.tsx:18](../../../web/components/CelebrationSettings.tsx#L18) |
| UI-00494 | {localize("ui.1cbff9671d6a34ac",{defaultValue:"Create a new draft"})} | {()=>void repeat()} | Not a native form submit or inherited from component context | [web/components/CelebrationSettings.tsx:18](../../../web/components/CelebrationSettings.tsx#L18) |
| UI-00528 | Accept optional analytics | {()=>{saveConsent(true);setShow(false)}} | Not a native form submit or inherited from component context | [web/components/CookieNotice.tsx:5](../../../web/components/CookieNotice.tsx#L5) |
| UI-00529 | Reject optional analytics | {()=>{saveConsent(false);setShow(false)}} | Not a native form submit or inherited from component context | [web/components/CookieNotice.tsx:5](../../../web/components/CookieNotice.tsx#L5) |
| UI-00788 | {collapsed?<PanelLeftOpen size={20}/>:<PanelLeftClose size={20}/>} | {toggle} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:50](../../../web/components/HostWorkspace.tsx#L50) |
| UI-00791 |  | {()=>void signOut()} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:53](../../../web/components/HostWorkspace.tsx#L53) |
| UI-00792 |  | {()=>setCollapsed(true)} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:54](../../../web/components/HostWorkspace.tsx#L54) |
| UI-00876 | Cookie preferences | {()=>window.dispatchEvent(new Event('giftwise-cookie-settings'))} | Not a native form submit or inherited from component context | [web/components/LegalFooter.tsx:4](../../../web/components/LegalFooter.tsx#L4) |


### Dialog and alert/status inventory

| ID | Role / kind | Text / label | Visibility conditions | Source |
| --- | --- | --- | --- | --- |
| UI-00072 | "status" | {localize("ui.bed9096c411e6da3",{defaultValue:"Opening your workspace…"})} | Parent/source state | [web/app/components/HostGlobalNav.tsx:17](../../../web/app/components/HostGlobalNav.tsx#L17) |
| UI-00478 | "alert" | {localizeText(error)} | error | [web/components/CelebrationSettings.tsx:18](../../../web/components/CelebrationSettings.tsx#L18) |
| UI-00480 | "status" | {localizeText(notice)} | notice | [web/components/CelebrationSettings.tsx:18](../../../web/components/CelebrationSettings.tsx#L18) |
| UI-00481 | "status" | {localize("ui.56e4573ee1ff3366",{defaultValue:"Opening celebration settings…"})} | loading | [web/components/CelebrationSettings.tsx:18](../../../web/components/CelebrationSettings.tsx#L18) |
| UI-00548 | "status" | {localize("ui.7a005f0f1dbc704f",{defaultValue:"Opening your celebration space…"})} | ready\|\|discovery | [web/components/ExperienceProvider.tsx:19](../../../web/components/ExperienceProvider.tsx#L19) |
| UI-00790 | "alert" | {localizeText(error)} | error | [web/components/HostWorkspace.tsx:53](../../../web/components/HostWorkspace.tsx#L53) |
| UI-00909 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:19](../../../web/components/LocalizationProvider.tsx#L19) |
| UI-00911 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:20](../../../web/components/LocalizationProvider.tsx#L20) |
| UI-00913 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:21](../../../web/components/LocalizationProvider.tsx#L21) |


### Workflow state bindings

| Source | Binding | Initial expression |
| --- | --- | --- |
| web/components/CelebrationSettings.tsx | error | '' |
| web/components/CelebrationSettings.tsx | notice | '' |
| web/components/CelebrationSettings.tsx | busy | false |
| web/components/CelebrationSettings.tsx | loading | true |
| web/components/LocalizationProvider.tsx | busy | false |
| web/components/LocalizationProvider.tsx | error | '' |
| web/components/HostWorkspace.tsx | busy | false |
| web/components/HostWorkspace.tsx | error | '' |


## Private invitation preview

| Screen information | Source-supported specification |
| --- | --- |
| Route | /events/[id]/preview |
| Source | [web/app/events/[id]/preview/page.tsx:1](../../../web/app/events/[id]/preview/page.tsx#L1) |
| Purpose | Preview event styling/gifts without authorizing guest submissions/payments |
| User access level | Authenticated event owner; not a public guest token |
| Navigation source | Checklist / QR/editor / event sidebar |
| Navigation destination | Back to edit/publish; preview-only actions stay disabled |
| Preconditions | Route parameters/query scope must be valid; applicable session/ownership/module/provider state above. Load errors do not imply an empty successful dataset. |
| Postconditions | Only the selected successful action changes its documented state. Navigation alone does not publish, confirm payment, verify contact or deliver a notification. |
| Route variables | id |
| UI variants | Focused/general experience, responsive layout and module/state predicates can change visible sections. Exact predicates are in each control record. |


### Wireframe layout

| Section | Placement / behavior / evidence |
| --- | --- |
| Header | Shared identity/event or page title/back/actions, where declared. Composite header declarations: 6. |
| Navigation / sidebar | Host/admin navigation and event context where applicable; mobile/focused variants are conditional. nav=4, aside=5. |
| Main content | Purpose-specific forms/cards/tables/canvas/player from the composed sources. main=6, section=27, article=6. |
| Search and filters | Fields/selects listed below; exact value/handler/visibility controls are not inferred from placeholder labels. |
| Data grids and cards | Table declarations=1. Data collections and generated rows/cards are in frontend.json dynamicSources. |
| Popups / dialogs | Named/source-declared dialog control records below; opening/closing/loading/apply/cancel actions belong to those component handlers. |
| Alerts / notifications | Source controls with role alert/status are listed below. Notice strings may come from API results; do not assume a universal envelope. |
| Footer | Local/shared footer declarations=3. No undocumented legal/footer wording is invented. |
| Loading / empty / success / error | Observed state bindings and relevant action fields are listed below. Expected QA states from Document 2 do not establish a spinner/retry control where source does not implement one. |


### Composed implementation

- [web/app/events/[id]/preview/page.tsx:1](../../../web/app/events/[id]/preview/page.tsx#L1)
- [web/components/LocalizationProvider.tsx:1](../../../web/components/LocalizationProvider.tsx#L1)
- [web/components/GiftDescription.tsx:1](../../../web/components/GiftDescription.tsx#L1)
- [web/components/ExperienceProvider.tsx:1](../../../web/components/ExperienceProvider.tsx#L1)
- [web/components/focused/Preview.tsx:1](../../../web/components/focused/Preview.tsx#L1)
- [web/components/focused/Invitation.tsx:1](../../../web/components/focused/Invitation.tsx#L1)
- [web/components/GuestDiscovery.tsx:1](../../../web/components/GuestDiscovery.tsx#L1)
- [web/components/GrowthProvider.tsx:1](../../../web/components/GrowthProvider.tsx#L1)
- [web/components/GiftThumbnail.tsx:1](../../../web/components/GiftThumbnail.tsx#L1)
- [web/components/BrandingProvider.tsx:1](../../../web/components/BrandingProvider.tsx#L1)
- [web/components/CelebrationDetails.tsx:1](../../../web/components/CelebrationDetails.tsx#L1)
- [web/components/GuestGiftBoard.tsx:1](../../../web/components/GuestGiftBoard.tsx#L1)
- [web/components/SpecialGiftEntry.tsx:1](../../../web/components/SpecialGiftEntry.tsx#L1)
- [web/components/GroupContribution.tsx:1](../../../web/components/GroupContribution.tsx#L1)
- [web/components/CredentialRequirements.tsx:1](../../../web/components/CredentialRequirements.tsx#L1)
- [web/components/TermsAcceptance.tsx:1](../../../web/components/TermsAcceptance.tsx#L1)
- [web/components/VerificationProvider.tsx:1](../../../web/components/VerificationProvider.tsx#L1)
- [web/components/MobileVerification.tsx:1](../../../web/components/MobileVerification.tsx#L1)
- [web/components/GroupGuestCheckout.tsx:1](../../../web/components/GroupGuestCheckout.tsx#L1)
- [web/components/SignupEmailField.tsx:1](../../../web/components/SignupEmailField.tsx#L1)
- [web/components/GroupEmailVerification.tsx:1](../../../web/components/GroupEmailVerification.tsx#L1)
- [web/components/GroupCreatorPaymentSettings.tsx:1](../../../web/components/GroupCreatorPaymentSettings.tsx#L1)
- [web/components/GroupGuestCreatorSettings.tsx:1](../../../web/components/GroupGuestCreatorSettings.tsx#L1)
- [web/components/GroupGiftPlan.tsx:1](../../../web/components/GroupGiftPlan.tsx#L1)
- [web/components/IndependentGroupPlan.tsx:1](../../../web/components/IndependentGroupPlan.tsx#L1)
- [web/components/GuestGiftManager.tsx:1](../../../web/components/GuestGiftManager.tsx#L1)
- [web/components/DeliveryAddressFields.tsx:1](../../../web/components/DeliveryAddressFields.tsx#L1)
- [web/components/GroupActivity.tsx:1](../../../web/components/GroupActivity.tsx#L1)
- [web/components/CommercePurchase.tsx:1](../../../web/components/CommercePurchase.tsx#L1)
- [web/components/IndividualHostContribution.tsx:1](../../../web/components/IndividualHostContribution.tsx#L1)
- [web/components/GuestVerifiedDestination.tsx:1](../../../web/components/GuestVerifiedDestination.tsx#L1)
- [web/components/CommercialPlacements.tsx:1](../../../web/components/CommercialPlacements.tsx#L1)
- [web/components/GuestContributionCheckout.tsx:1](../../../web/components/GuestContributionCheckout.tsx#L1)
- [web/components/PaymentDisclosure.tsx:1](../../../web/components/PaymentDisclosure.tsx#L1)
- [web/components/focused/Checkout.tsx:1](../../../web/components/focused/Checkout.tsx#L1)
- [web/components/GuestActions.tsx:1](../../../web/components/GuestActions.tsx#L1)
- [web/components/GuestCelebration.tsx:1](../../../web/components/GuestCelebration.tsx#L1)
- [web/components/SocialSharing.tsx:1](../../../web/components/SocialSharing.tsx#L1)
- [web/app/layout.tsx:1](../../../web/app/layout.tsx#L1)
- [web/components/LegalFooter.tsx:1](../../../web/components/LegalFooter.tsx#L1)
- [web/components/CookieNotice.tsx:1](../../../web/components/CookieNotice.tsx#L1)
- [web/app/components/ApplicationInteractions.tsx:1](../../../web/app/components/ApplicationInteractions.tsx#L1)
- [web/app/components/PerformanceDiagnostics.tsx:1](../../../web/app/components/PerformanceDiagnostics.tsx#L1)
- [web/app/components/HostSessionRedirect.tsx:1](../../../web/app/components/HostSessionRedirect.tsx#L1)
- [web/app/components/HostGlobalNav.tsx:1](../../../web/app/components/HostGlobalNav.tsx#L1)
- [web/components/HostWorkspace.tsx:1](../../../web/components/HostWorkspace.tsx#L1)
- [web/components/focused/HostShell.tsx:1](../../../web/components/focused/HostShell.tsx#L1)

### Fields

| Control ID | Label / expression | Type | Binding | Required | Native limits / format | Detailed specification |
| --- | --- | --- | --- | --- | --- | --- |
| UI-00538 | {localize("ui.455df35dd66a82ca",{defaultValue:"Same as event address"})} | input "checkbox" | {same} | Not declared |  | [control](UI_CONTROLS.md#ui-00538) |
| UI-00539 | {localizeText(label)} | input  | {address[key]} | true | maxLength={maxLength} | [control](UI_CONTROLS.md#ui-00539) |
| UI-00568 | {localize("ui.187e80ba4d8a22bb",{defaultValue:"Mobile number"})} | input "tel" | {registrationMobile} | true |  | [control](UI_CONTROLS.md#ui-00568) |
| UI-00569 | {localize("ui.f13a64ba2fea87ef",{defaultValue:"Full name"})} | input  | {fullName} | true | minLength={2}; maxLength={120} | [control](UI_CONTROLS.md#ui-00569) |
| UI-00570 | {localize("ui.e3b89e9d33f88e52",{defaultValue:"Username"})} | input  | {username} | true | minLength={tab==='register'?4:undefined}; maxLength={tab==='register'?30:undefined}; pattern={tab==='register'?'[A-Za-z0-9][A-Za-z0-9_]{3,29}':undefined} | [control](UI_CONTROLS.md#ui-00570) |
| UI-00571 | {localize("ui.11aaa14268df51b7",{defaultValue:"Sign in with"})} | select  | {loginMethod} | Not declared |  | [control](UI_CONTROLS.md#ui-00571) |
| UI-00572 | {localize("ui.e7cf3ef4f17c3999",{defaultValue:"Password"})} | input "password" | {password} | true | minLength={tab==='register'?8:undefined} | [control](UI_CONTROLS.md#ui-00572) |
| UI-00573 | {localize("ui.5ac265f396a2c586",{defaultValue:"Confirm password"})} | input "password" | {confirm} | true | minLength={8} | [control](UI_CONTROLS.md#ui-00573) |
| UI-00574 | {localize("ui.61c2b9f2c16ea005",{defaultValue:"Six-digit PIN"})} | input "password" | {pin} | true | minLength={6}; maxLength={6}; pattern="[0-9]{6}" | [control](UI_CONTROLS.md#ui-00574) |
| UI-00575 | {localize("ui.4fa10c7baa0c1d89",{defaultValue:"Confirm PIN"})} | input "password" | {confirmPin} | true | minLength={6}; maxLength={6}; pattern="[0-9]{6}" | [control](UI_CONTROLS.md#ui-00575) |
| UI-00576 | {localize("ui.61c2b9f2c16ea005",{defaultValue:"Six-digit PIN"})} | input "password" | {pin} | true | minLength={6}; maxLength={6}; pattern="[0-9]{6}" | [control](UI_CONTROLS.md#ui-00576) |
| UI-00580 | {localize("ui.d82e5fe24eed3db4",{defaultValue:"What should we call you?"})} | input  | {adminNameDraft} | true | minLength={2}; maxLength={120} | [control](UI_CONTROLS.md#ui-00580) |
| UI-00584 | {localize("ui.72ed2bd767cef627",{defaultValue:"Current password"})} | input "password" | {pinPassword} | true |  | [control](UI_CONTROLS.md#ui-00584) |
| UI-00585 | {localize("ui.61c2b9f2c16ea005",{defaultValue:"Six-digit PIN"})} | input "password" | {pin} | true | maxLength={6}; pattern="[0-9]{6}" | [control](UI_CONTROLS.md#ui-00585) |
| UI-00586 | {localize("ui.4fa10c7baa0c1d89",{defaultValue:"Confirm PIN"})} | input "password" | {confirmPin} | true | maxLength={6}; pattern="[0-9]{6}" | [control](UI_CONTROLS.md#ui-00586) |
| UI-00605 | {localize("ui.c99c586025d6769c",{defaultValue:"Who can join?"})} | select  | {privacy.join_policy} | Not declared |  | [control](UI_CONTROLS.md#ui-00605) |
| UI-00606 | {localize("ui.2ad6410b25fa2dbb",{defaultValue:"Who can see the member list?"})} | select  | {privacy.member_visibility} | Not declared |  | [control](UI_CONTROLS.md#ui-00606) |
| UI-00607 | {localize("ui.5bc6d84f2126eb5e",{defaultValue:"Who can see individual contribution amounts?"})} | select  | {privacy.amount_visibility} | Not declared |  | [control](UI_CONTROLS.md#ui-00607) |
| UI-00610 | {localize("ui.6cb353a65d0160d5",{defaultValue:"Group name *"})} | input  | {form.name} | true | maxLength={120} | [control](UI_CONTROLS.md#ui-00610) |
| UI-00611 | {localize("ui.7204645b7341d714",{defaultValue:"Your name *"})} | input  | {form.creatorName} | true | maxLength={120} | [control](UI_CONTROLS.md#ui-00611) |
| UI-00612 | {localize("ui.0836c3b7cadbef5b",{defaultValue:"Mobile number *"})} | input "tel" | {form.creatorMobile} | true | maxLength={20} | [control](UI_CONTROLS.md#ui-00612) |
| UI-00613 | {localize("ui.751e68347f10bcf7",{defaultValue:"Group description"})} | textarea  | {form.description} | Not declared | maxLength={500} | [control](UI_CONTROLS.md#ui-00613) |
| UI-00614 | {localize("ui.09b05cb4d017cf72",{defaultValue:"Collection plan"})} | select  | {giftId} | Not declared |  | [control](UI_CONTROLS.md#ui-00614) |
| UI-00615 | {localize("ui.4e82ed6b989eb0fb",{defaultValue:"Gift name (optional)"})} | input  | {form.giftName} | Not declared | maxLength={160} | [control](UI_CONTROLS.md#ui-00615) |
| UI-00616 | {localize("ui.da0378150bd065d4",{defaultValue:"Gift description"})} | textarea  | {form.giftDescription} | Not declared | maxLength={500} | [control](UI_CONTROLS.md#ui-00616) |
| UI-00617 | {localize("ui.5c7466d4572b8863",{defaultValue:"Gift link"})} | input "url" | {form.giftLink} | Not declared | maxLength={1000} | [control](UI_CONTROLS.md#ui-00617) |
| UI-00618 | {localize("ui.1c68de56ec168a54",{defaultValue:"Gift goal in ₹"})} | input  | {form.targetRupees} | Not declared |  | [control](UI_CONTROLS.md#ui-00618) |
| UI-00621 |  | input "file" |  | Not declared |  | [control](UI_CONTROLS.md#ui-00621) |
| UI-00628 | {localize("ui.b948ac04b854394f",{defaultValue:"Payment method"})} | select  | {kind} | Not declared |  | [control](UI_CONTROLS.md#ui-00628) |
| UI-00629 | {localize("ui.193fa77b177b9520",{defaultValue:"Account nickname"})} | input  | {label} | true | maxLength={80} | [control](UI_CONTROLS.md#ui-00629) |
| UI-00630 | {localize("ui.cec4a9c0b1e71189",{defaultValue:"UPI ID"})} | input  | {upiId} | true |  | [control](UI_CONTROLS.md#ui-00630) |
| UI-00631 | {localize("ui.53477ca834fa478d",{defaultValue:"Account holder"})} | input  | {accountHolder} | true | maxLength={120} | [control](UI_CONTROLS.md#ui-00631) |
| UI-00632 | {localize("ui.f7573b7f5db0b550",{defaultValue:"Account number"})} | input  | {accountNumber} | true |  | [control](UI_CONTROLS.md#ui-00632) |
| UI-00633 | {localize("ui.f3d8c96e71944625",{defaultValue:"IFSC"})} | input  | {ifsc} | true | maxLength={11} | [control](UI_CONTROLS.md#ui-00633) |
| UI-00637 | {localize("ui.f2488fd4ef4adbc6",{defaultValue:"Email address"})} | input "email" | {email} | true | maxLength={254} | [control](UI_CONTROLS.md#ui-00637) |
| UI-00640 | {localize("ui.3bcb66ee1a6feaf5",{defaultValue:"Six-digit code"})} | input  | {code} | true | maxLength={6}; pattern="[0-9]{6}" | [control](UI_CONTROLS.md#ui-00640) |
| UI-00647 | {localize("ui.b03cdfe17e82db86",{defaultValue:"Wishlist gift"})} | select  | {choice} | Not declared |  | [control](UI_CONTROLS.md#ui-00647) |
| UI-00654 | {localize("ui.749c97e490fa85d6",{defaultValue:"Your contribution amount (₹)"})} | input  | {amount} | true |  | [control](UI_CONTROLS.md#ui-00654) |
| UI-00655 | {localize("ui.7204645b7341d714",{defaultValue:"Your name *"})} | input  | {name} | true | minLength={2}; maxLength={120} | [control](UI_CONTROLS.md#ui-00655) |
| UI-00656 | {localize("ui.433f5f401017f2de",{defaultValue:"Mobile or email *"})} | input  | {contact} | true | maxLength={120} | [control](UI_CONTROLS.md#ui-00656) |
| UI-00660 | {localize("ui.5ffa24818e50be17",{defaultValue:"Transaction reference / UTR *"})} | input  | {reference} | true | minLength={6}; maxLength={120} | [control](UI_CONTROLS.md#ui-00660) |
| UI-00666 | {localize("ui.ec8e5c2f4fabcfa9",{defaultValue:"Invitation link options"})} | select  | {accessMode} | Not declared |  | [control](UI_CONTROLS.md#ui-00666) |
| UI-00667 | {localize("ui.f4228ef0788c1e26",{defaultValue:"Suggested contribution (₹)"})} | input  | {amount} | Not declared |  | [control](UI_CONTROLS.md#ui-00667) |
| UI-00668 | {localize("ui.de9329ae7c59130b",{defaultValue:"UPI account for guests"})} | select  | {upi} | Not declared |  | [control](UI_CONTROLS.md#ui-00668) |
| UI-00669 | {localize("ui.de0b78f10c8d9b42",{defaultValue:"Bank account for guests"})} | select  | {bank} | Not declared |  | [control](UI_CONTROLS.md#ui-00669) |
| UI-00670 | {localize("ui.559f892eac11e3cc",{defaultValue:" Enable manual guest contribution instructions"})} | input "checkbox" | {enabled} | Not declared |  | [control](UI_CONTROLS.md#ui-00670) |
| UI-00691 | {localize("ui.4d7c4f98db4e9668",{defaultValue:"RSVP guest name"})} | input  | {name} | true | maxLength={120} | [control](UI_CONTROLS.md#ui-00691) |
| UI-00692 | {localize("ui.70d4092a019c5b1e",{defaultValue:"RSVP email"})} | input "email" | {email} | true | maxLength={254} | [control](UI_CONTROLS.md#ui-00692) |
| UI-00694 |  | input "radio" | {value} | Not declared |  | [control](UI_CONTROLS.md#ui-00694) |
| UI-00695 | {localize("ui.5d9b03aa0b5067b7",{defaultValue:"People attending (including you)"})} | input "number" | {party} | true | min={1}; max={10} | [control](UI_CONTROLS.md#ui-00695) |
| UI-00699 | {localize("ui.a9d4201422911b11",{defaultValue:"Guestbook name"})} | input  | {wishName} | true | maxLength={120} | [control](UI_CONTROLS.md#ui-00699) |
| UI-00700 | {localize("ui.be979451f10245e8",{defaultValue:"Guestbook message"})} | textarea  | {message} | true | maxLength={1000} | [control](UI_CONTROLS.md#ui-00700) |
| UI-00701 | {localize("ui.5435fa51d13a2c03",{defaultValue:"Guestbook photo"})} | input "file" |  | Not declared |  | [control](UI_CONTROLS.md#ui-00701) |
| UI-00722 |  | input "radio" | {method.id} | Not declared |  | [control](UI_CONTROLS.md#ui-00722) |
| UI-00723 | {localize("ui.293d404a500f5f9a",{defaultValue:"Destination"})} | select  | {giftId} | Not declared |  | [control](UI_CONTROLS.md#ui-00723) |
| UI-00724 | {localize("ui.fcbb0bc384eed802",{defaultValue:"Amount (₹)"})} | input "number" | {amount} | true | min="1"; max="1000000"; step="0.01" | [control](UI_CONTROLS.md#ui-00724) |
| UI-00725 | {localize("ui.7204645b7341d714",{defaultValue:"Your name *"})} | input  | {name} | true | maxLength={200} | [control](UI_CONTROLS.md#ui-00725) |
| UI-00727 | {localize("ui.6eea8608c452711b",{defaultValue:"Email (optional)"})} | input "email" | {email} | Not declared | maxLength={320} | [control](UI_CONTROLS.md#ui-00727) |
| UI-00728 | {localize("ui.35878f0bd6c133b3",{defaultValue:"Phone (optional)"})} | input "tel" | {phone} | Not declared | maxLength={30} | [control](UI_CONTROLS.md#ui-00728) |
| UI-00729 | {localize("ui.0c0b7cca57d23288",{defaultValue:"UPI transaction ID / Bank UTR"})} | input  | {transferReference} | Not declared | maxLength={40} | [control](UI_CONTROLS.md#ui-00729) |
| UI-00745 | {localize("ui.64b4ff26c07d838e",{defaultValue:"Gift name"})} | input  | {name} | true | maxLength={160} | [control](UI_CONTROLS.md#ui-00745) |
| UI-00746 | {localize("ui.83688b69ed8d6461",{defaultValue:"Description · optional"})} | textarea  | {description} | Not declared | maxLength={500} | [control](UI_CONTROLS.md#ui-00746) |
| UI-00747 | {localize("ui.271401f0515cac0b",{defaultValue:"Product link · optional"})} | input "url" | {url} | Not declared | maxLength={1000}; pattern="https://[^\s]+" | [control](UI_CONTROLS.md#ui-00747) |
| UI-00749 | {localize("ui.9ead2ac60e7db3f4",{defaultValue:"How would you like to give?"})} | select  | {type} | Not declared |  | [control](UI_CONTROLS.md#ui-00749) |
| UI-00761 | {localize("ui.6f17cdcd888c485e",{defaultValue:"Preferred recipient"})} | select  | {selected?.id??''} | Not declared |  | [control](UI_CONTROLS.md#ui-00761) |
| UI-00793 | {localize("ui.09b05cb4d017cf72",{defaultValue:"Collection plan"})} | select  | {plan} | Not declared |  | [control](UI_CONTROLS.md#ui-00793) |
| UI-00794 | {localize("ui.5d824da34fa87848",{defaultValue:"Our gift’s product link"})} | input "url" | {value.giftLink} | true | maxLength={1000}; pattern="https://[^\s]+" | [control](UI_CONTROLS.md#ui-00794) |
| UI-00796 | {localize("ui.64b4ff26c07d838e",{defaultValue:"Gift name"})} | input  | {value.giftName} | true | maxLength={160} | [control](UI_CONTROLS.md#ui-00796) |
| UI-00797 | {localize("ui.da0378150bd065d4",{defaultValue:"Gift description"})} | textarea  | {value.giftDescription} | Not declared | maxLength={500} | [control](UI_CONTROLS.md#ui-00797) |
| UI-00798 | {localize("ui.259feaef1f119dee",{defaultValue:"Collection goal (₹) · optional"})} | input "number" | {value.targetRupees} | Not declared | min="0.01"; max="1000000000"; step="0.01" | [control](UI_CONTROLS.md#ui-00798) |
| UI-00807 | {localize("ui.b948ac04b854394f",{defaultValue:"Payment method"})} | select  | {method} | Not declared |  | [control](UI_CONTROLS.md#ui-00807) |
| UI-00808 | {localize("ui.0c0b7cca57d23288",{defaultValue:"UPI transaction ID / Bank UTR"})} | input  | {reference} | true | minLength={8}; maxLength={40}; pattern="[A-Za-z0-9\-]{8,40}" | [control](UI_CONTROLS.md#ui-00808) |
| UI-00908 | {localize("ui.bba5b40bdd28c66e",{defaultValue:"Choose language"})} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00908) |
| UI-00910 | {label} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00910) |
| UI-00912 | {localize("ui.140071feea59fe87",{defaultValue:"Preferred language"})} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00912) |
| UI-00931 | {localize("ui.dae5cf30549f072d",{defaultValue:"Mobile number with country code"})} | input "tel" | {mobile} | true | pattern="\+[1-9][0-9]{7,14}" | [control](UI_CONTROLS.md#ui-00931) |
| UI-00932 | {localize("ui.f673a246d45f4b8f",{defaultValue:"Six-digit verification code"})} | input  | {code} | true | maxLength={6}; pattern="[0-9]{6}" | [control](UI_CONTROLS.md#ui-00932) |
| UI-00998 | {localize("ui.f2488fd4ef4adbc6",{defaultValue:"Email address"})} | input "email" | {value} | true | maxLength={254} | [control](UI_CONTROLS.md#ui-00998) |
| UI-01012 | {localize("ui.88c3be35063553d5",{defaultValue:"Invitation link"})} | input  | {url} | Not declared |  | [control](UI_CONTROLS.md#ui-01012) |
| UI-01024 | {localize("ui.f296b67a545cd756",{defaultValue:"Shipment tracking details"})} | textarea  | {tracking} | true | maxLength={1000} | [control](UI_CONTROLS.md#ui-01024) |
| UI-01028 | {localize("ui.af7e58d035aad83e",{defaultValue:"Choose a gift"})} | select  | {source} | Not declared |  | [control](UI_CONTROLS.md#ui-01028) |
| UI-01030 | {localize("ui.2c6b2e253c01fb2e",{defaultValue:"Your name"})} | input  | {form.guestName} | true | maxLength={120} | [control](UI_CONTROLS.md#ui-01030) |
| UI-01031 | {localize("ui.f2488fd4ef4adbc6",{defaultValue:"Email address"})} | input "email" | {form.email} | true | maxLength={254} | [control](UI_CONTROLS.md#ui-01031) |
| UI-01032 | {localize("ui.187e80ba4d8a22bb",{defaultValue:"Mobile number"})} | input "tel" | {form.mobile} | true | maxLength={25}; pattern="\+?[0-9][0-9 \-]{7,23}" | [control](UI_CONTROLS.md#ui-01032) |
| UI-01034 | {localize("ui.64b4ff26c07d838e",{defaultValue:"Gift name"})} | input  | {form.name} | true | maxLength={160} | [control](UI_CONTROLS.md#ui-01034) |
| UI-01035 | {localize("ui.a06890de939bdcac",{defaultValue:"Product link"})} {method==='DIRECT_DELIVERY'?'':localize("ui.418e319e80da27d9",{defaultValue:" · optional if you add gift details"})} | input "url" | {form.productUrl} | {method==='DIRECT_DELIVERY'} | maxLength={1000}; pattern="https://[^\s]+" | [control](UI_CONTROLS.md#ui-01035) |
| UI-01036 | {localize("ui.f2da19dd48c937be",{defaultValue:"Gift details"})} | textarea  | {form.description} | Not declared | maxLength={500} | [control](UI_CONTROLS.md#ui-01036) |
| UI-01038 |  | input "radio" | {method==='LINK_AND_CONTRIBUTION'} | Not declared |  | [control](UI_CONTROLS.md#ui-01038) |
| UI-01039 |  | input "radio" | {method==='DIRECT_DELIVERY'} | Not declared |  | [control](UI_CONTROLS.md#ui-01039) |
| UI-01040 | {localize("ui.7fc910558a4cc82b",{defaultValue:"Contribution amount (₹)"})} | input "number" | {form.amount} | true | min="1"; max="1000000"; step="0.01" | [control](UI_CONTROLS.md#ui-01040) |
| UI-01044 |  | input "checkbox" | {checked} | true |  | [control](UI_CONTROLS.md#ui-01044) |
| UI-01060 | {localize("ui.fcbb0bc384eed802",{defaultValue:"Amount (₹)"})} | input "number" | {p.amount} | true | min="1"; max="1000000"; step="0.01" | [control](UI_CONTROLS.md#ui-01060) |
| UI-01061 | {localize("ui.2c6b2e253c01fb2e",{defaultValue:"Your name"})} | input  | {p.name} | true | maxLength={200} | [control](UI_CONTROLS.md#ui-01061) |
| UI-01063 | {localize("ui.969ccbd3cf6300ec",{defaultValue:"Email"})} | input "email" | {p.email} | Not declared | maxLength={320} | [control](UI_CONTROLS.md#ui-01063) |
| UI-01064 | {localize("ui.63dceb8800b2dd4a",{defaultValue:"Phone"})} | input "tel" | {p.phone} | Not declared | maxLength={30} | [control](UI_CONTROLS.md#ui-01064) |
| UI-01068 |  | input "radio" | {p.method===method} | Not declared |  | [control](UI_CONTROLS.md#ui-01068) |
| UI-01069 | {localize("ui.41a0811e12b57d34",{defaultValue:"Transaction reference / bank UTR"})} | input  | {p.reference} | true | maxLength={40} | [control](UI_CONTROLS.md#ui-01069) |
| UI-01111 | {localize("ui.bec69036aa27e7fa",{defaultValue:"Sort"})} | select  | {sort} | Not declared |  | [control](UI_CONTROLS.md#ui-01111) |
| UI-01120 | {localize("ui.0644dda59d887ed5",{defaultValue:"Preview width"})} | select  | {width} | Not declared |  | [control](UI_CONTROLS.md#ui-01120) |


### Buttons

| ID | Label / expression | Action | Submit validation / handler | Source |
| --- | --- | --- | --- | --- |
| UI-00497 | {busy?localize("ui.42074396a441a347",{defaultValue:"Processing…"}):offer.amountPaise===0?localize("ui.850fbbaefb4057d0",{defaultValue:"Activate access"}):localize("ui.0e3afe9550a3bde6",{defaultValue:"{{value0}} · {{value1}} (test mode)",value0:(offer.entitled?'Renew':'Purchase'),value1:(commercialMoney(offer.amountPaise))})} | {()=>void pay()} | Not a native form submit or inherited from component context | [web/components/CommercePurchase.tsx:34](../../../web/components/CommercePurchase.tsx#L34) |
| UI-00499 | {localize("ui.087a9cc69283c135",{defaultValue:"Reload price"})} | {()=>{setRetry(v=>v+1)}} | Not a native form submit or inherited from component context | [web/components/CommercePurchase.tsx:34](../../../web/components/CommercePurchase.tsx#L34) |
| UI-00528 | Accept optional analytics | {()=>{saveConsent(true);setShow(false)}} | Not a native form submit or inherited from component context | [web/components/CookieNotice.tsx:5](../../../web/components/CookieNotice.tsx#L5) |
| UI-00529 | Reject optional analytics | {()=>{saveConsent(false);setShow(false)}} | Not a native form submit or inherited from component context | [web/components/CookieNotice.tsx:5](../../../web/components/CookieNotice.tsx#L5) |
| UI-00549 | {localize("ui.ef0edc1b48d46a83",{defaultValue:"View full description"})} | {()=>dialog.current?.showModal()} | Not a native form submit or inherited from component context | [web/components/GiftDescription.tsx:13](../../../web/components/GiftDescription.tsx#L13) |
| UI-00551 | {localize("ui.ad8c41bed4972296",{defaultValue:"Close ×"})} | {()=>dialog.current?.close()} | Not a native form submit or inherited from component context | [web/components/GiftDescription.tsx:13](../../../web/components/GiftDescription.tsx#L13) |
| UI-00553 | {localize("ui.c9bca82b557e962e",{defaultValue:"Newer activity"})} | {()=>setOffset(value=>Math.max(0,value-100))} | Not a native form submit or inherited from component context | [web/components/GroupActivity.tsx:13](../../../web/components/GroupActivity.tsx#L13) |
| UI-00554 | {localize("ui.0eed19abab86afcc",{defaultValue:"Older activity"})} | {()=>setOffset(value=>value+100)} | Not a native form submit or inherited from component context | [web/components/GroupActivity.tsx:13](../../../web/components/GroupActivity.tsx#L13) |
| UI-00557 | {guestGifts?localize("ui.72c7b5708f440207",{defaultValue:"Add a Special Gift"}):giftAvailable?localize("ui.f68ad0a9686950fe",{defaultValue:"Group contribution"}):localize("ui.ee91812bd92cd6d9",{defaultValue:"Gift held with love ♡"})} | {launch} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:88](../../../web/components/GroupContribution.tsx#L88) |
| UI-00559 | × | {()=>void closeAndLock()} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:89](../../../web/components/GroupContribution.tsx#L89) |
| UI-00560 | {localize("ui.48f0d3d397d49f13",{defaultValue:"Sign out"})} | {()=>void logout()} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:90](../../../web/components/GroupContribution.tsx#L90) |
| UI-00563 | {guestGifts?localize("ui.798ca2ce18bf2038",{defaultValue:"Create account"}):localize("ui.1213394682bfa8c6",{defaultValue:"Create Group"})} | {()=>{setTab('register');setError('')}} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:90](../../../web/components/GroupContribution.tsx#L90) |
| UI-00564 | {guestGifts?localize("ui.bfd402b2f6f38125",{defaultValue:"Sign in"}):localize("ui.3d65b17f4fb1f79f",{defaultValue:"Open Group"})} | {()=>{setTab('login');setError('')}} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:90](../../../web/components/GroupContribution.tsx#L90) |
| UI-00577 | {busy?localize("ui.4660a983366f7101",{defaultValue:"Please wait…"}):tab==='register'?localize("ui.6c9206e4b89975a5",{defaultValue:"Register →"}):localize("ui.8637e0051dee66ad",{defaultValue:"Sign in →"})} | submit / component action | {auth} | [web/components/GroupContribution.tsx:90](../../../web/components/GroupContribution.tsx#L90) |
| UI-00578 | {localize("ui.308b88fcc3dffe50",{defaultValue:"Edit name"})} | {()=>{setAdminNameDraft(adminName);setEditingName(true)}} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:90](../../../web/components/GroupContribution.tsx#L90) |
| UI-00581 | {localize("ui.b7297226fd1f4681",{defaultValue:"Save name"})} | submit / component action | {async e=>{e.preventDefault();try{const x=await call('/profile-name',{fullName:adminNameDraft});setAdminName(x.fullName);setEditingName(false);setError('')}catch(x){setError(x instanceof Error?x.message:'Unable to save name')}}} | [web/components/GroupContribution.tsx:90](../../../web/components/GroupContribution.tsx#L90) |
| UI-00582 | {localize("ui.48f0d3d397d49f13",{defaultValue:"Sign out"})} | {()=>void logout()} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:90](../../../web/components/GroupContribution.tsx#L90) |
| UI-00587 | {localize("ui.a70c9f89a0495049",{defaultValue:"Save PIN"})} | submit / component action | {async e=>{e.preventDefault();setPinNotice('');try{await call('/pin/setup',{password:pinPassword,pin,confirmPin});setPinNotice('PIN updated successfully. You can now sign in with your new six-digit PIN.');setPinPassword('');setPin('');setConfirmPin('');setEditingPin(false)}catch(x){setPinNotice(x instanceof Error?x.message:'PIN setup failed')}}} | [web/components/GroupContribution.tsx:90](../../../web/components/GroupContribution.tsx#L90) |
| UI-00589 | {localize("ui.331135fcb9bb6f25",{defaultValue:"1. Receiving account"})} | {()=>setSection('payments')} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:90](../../../web/components/GroupContribution.tsx#L90) |
| UI-00590 | {localize("ui.be5fb8c43cc3df3e",{defaultValue:"2. Enable contributions"})} | {()=>setSection('guest')} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:90](../../../web/components/GroupContribution.tsx#L90) |
| UI-00591 | {localize("ui.6bdf75ffd0fd9e1c",{defaultValue:"3. Invite friends"})} | {()=>setSection('overview')} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:90](../../../web/components/GroupContribution.tsx#L90) |
| UI-00592 | {localize("ui.d4b1ea5708dd5329",{defaultValue:"Overview"})} | {()=>{setSection('overview');void restore()}} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:90](../../../web/components/GroupContribution.tsx#L90) |
| UI-00593 | {localize("ui.3bc6107114c6fa86",{defaultValue:"Privacy & settings"})} | {()=>setSection('privacy')} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:90](../../../web/components/GroupContribution.tsx#L90) |
| UI-00594 | {localize("ui.7f65e0162671c9e9",{defaultValue:"Members & requests"})} | {()=>void loadMembers()} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:90](../../../web/components/GroupContribution.tsx#L90) |
| UI-00595 | {localize("ui.cc64bb493268ea0b",{defaultValue:"Payment accounts"})} | {()=>setSection('payments')} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:90](../../../web/components/GroupContribution.tsx#L90) |
| UI-00596 | {localize("ui.4398ddbea4af82a8",{defaultValue:"♡ My contribution"})} | {()=>setSection('contribute')} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:90](../../../web/components/GroupContribution.tsx#L90) |
| UI-00597 | {localize("ui.fd58f51f50008f89",{defaultValue:"Guest contributions"})} | {()=>setSection('guest')} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:90](../../../web/components/GroupContribution.tsx#L90) |
| UI-00599 | {localize("ui.5c58cd7963cac4e2",{defaultValue:"Copy invite link"})} | {()=>void trackedCopy(`${location.origin}/groups/${token}/${group.id}`)} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:90](../../../web/components/GroupContribution.tsx#L90) |
| UI-00600 | {localize("ui.16f6803a1f4a8a60",{defaultValue:"Invite link unavailable"})} | submit / component action | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:90](../../../web/components/GroupContribution.tsx#L90) |
| UI-00601 | {published?localize("ui.a1ddffdbaeaee49f",{defaultValue:"Collection published"}):localize("ui.a90c8deaaa3deaaf",{defaultValue:"Publish collection to host"})} | {()=>void publishGroup()} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:90](../../../web/components/GroupContribution.tsx#L90) |
| UI-00602 | {localize("ui.6007acbe30b2cd98",{defaultValue:"Approve"})} | {()=>void reviewMember(m.id,'APPROVE')} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:90](../../../web/components/GroupContribution.tsx#L90) |
| UI-00603 | {localize("ui.ab604a360777735f",{defaultValue:"Reject"})} | {()=>void reviewMember(m.id,'REJECT')} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:90](../../../web/components/GroupContribution.tsx#L90) |
| UI-00608 | {busy?localize("ui.23e39291d6135814",{defaultValue:"Saving…"}):localize("ui.cd933d481eefc0f0",{defaultValue:"Save privacy settings"})} | submit / component action | {savePrivacy} | [web/components/GroupContribution.tsx:90](../../../web/components/GroupContribution.tsx#L90) |
| UI-00619 | {busy?localize("ui.c79ed9492e3c1719",{defaultValue:"Creating…"}):localize("ui.5457c9f7d1371da0",{defaultValue:"Create private group →"})} | submit / component action | {setup} | [web/components/GroupContribution.tsx:90](../../../web/components/GroupContribution.tsx#L90) |
| UI-00620 | {d.enabled?localize("ui.9f024ada2282291c",{defaultValue:"Disable account"}):localize("ui.1965185f3488bfcf",{defaultValue:"Enable account"})} | {()=>void run(`/destinations/${d.id}/enabled`,{enabled:!d.enabled})} | Not a native form submit or inherited from component context | [web/components/GroupCreatorPaymentSettings.tsx:24](../../../web/components/GroupCreatorPaymentSettings.tsx#L24) |
| UI-00622 | {localize("ui.b81924056587bf3d",{defaultValue:"Generate QR"})} | {()=>void run(`/destinations/${d.id}/qr/generate`)} | Not a native form submit or inherited from component context | [web/components/GroupCreatorPaymentSettings.tsx:24](../../../web/components/GroupCreatorPaymentSettings.tsx#L24) |
| UI-00623 | {localize("ui.1e4b206d916c7767",{defaultValue:"Preview QR"})} | {()=>void showQr(d.id)} | Not a native form submit or inherited from component context | [web/components/GroupCreatorPaymentSettings.tsx:24](../../../web/components/GroupCreatorPaymentSettings.tsx#L24) |
| UI-00624 | {localize("ui.95e0dcafc3f37df8",{defaultValue:"Make preferred"})} | {()=>void run('/preferred',{destinationId:d.id})} | Not a native form submit or inherited from component context | [web/components/GroupCreatorPaymentSettings.tsx:24](../../../web/components/GroupCreatorPaymentSettings.tsx#L24) |
| UI-00626 | {localize("ui.7d8ab368210c5ae8",{defaultValue:"Close preview"})} | {()=>{URL.revokeObjectURL(preview);setPreview(null)}} | Not a native form submit or inherited from component context | [web/components/GroupCreatorPaymentSettings.tsx:26](../../../web/components/GroupCreatorPaymentSettings.tsx#L26) |
| UI-00634 | {busy?localize("ui.f9a4e6b43543c221",{defaultValue:"Saving securely…"}):localize("ui.4f474d1f3677e9a1",{defaultValue:"Save payment account"})} | submit / component action | {add} | [web/components/GroupCreatorPaymentSettings.tsx:28](../../../web/components/GroupCreatorPaymentSettings.tsx#L28) |
| UI-00638 | {localize("ui.19e86d2b7055340c",{defaultValue:"Send verification code"})} | submit / component action | {start} | [web/components/GroupEmailVerification.tsx:19](../../../web/components/GroupEmailVerification.tsx#L19) |
| UI-00641 | {localize("ui.e89a77e7fe58a6d1",{defaultValue:"Verify email"})} | submit / component action | {finish} | [web/components/GroupEmailVerification.tsx:19](../../../web/components/GroupEmailVerification.tsx#L19) |
| UI-00643 | {localize("ui.b6dc916237a08684",{defaultValue:"Try holding this gift again"})} | {()=>void change(false)} | Not a native form submit or inherited from component context | [web/components/GroupGiftPlan.tsx:11](../../../web/components/GroupGiftPlan.tsx#L11) |
| UI-00644 | {localize("ui.efd4d6bdf9a5a4b3",{defaultValue:"Yes, release gift"})} | {()=>void change(true)} | Not a native form submit or inherited from component context | [web/components/GroupGiftPlan.tsx:11](../../../web/components/GroupGiftPlan.tsx#L11) |
| UI-00645 | {localize("ui.7b9d556ef1178ceb",{defaultValue:"Keep gift"})} | {()=>setConfirmRelease(false)} | Not a native form submit or inherited from component context | [web/components/GroupGiftPlan.tsx:11](../../../web/components/GroupGiftPlan.tsx#L11) |
| UI-00646 | {localize("ui.350000ebff868e58",{defaultValue:"Release gift · keep our collection"})} | {()=>setConfirmRelease(true)} | Not a native form submit or inherited from component context | [web/components/GroupGiftPlan.tsx:11](../../../web/components/GroupGiftPlan.tsx#L11) |
| UI-00648 | {localize("ui.1c8928f0a2a51a6a",{defaultValue:"Add gift to our group"})} | {()=>void change(false)} | Not a native form submit or inherited from component context | [web/components/GroupGiftPlan.tsx:11](../../../web/components/GroupGiftPlan.tsx#L11) |
| UI-00652 | {localize("ui.e382a36729887241",{defaultValue:"Make another contribution"})} | {()=>{setStage('start');setReference('');setSubmissionKey(crypto.randomUUID());setDetails(null)}} | Not a native form submit or inherited from component context | [web/components/GroupGuestCheckout.tsx:29](../../../web/components/GroupGuestCheckout.tsx#L29) |
| UI-00653 | {localize("ui.4c363f056aaf6d63",{defaultValue:"Use suggestion"})} | {()=>setAmount(String(invite.suggested_contribution_paise!/100))} | Not a native form submit or inherited from component context | [web/components/GroupGuestCheckout.tsx:29](../../../web/components/GroupGuestCheckout.tsx#L29) |
| UI-00657 |  | {()=>void choosePayment(d.id)} | Not a native form submit or inherited from component context | [web/components/GroupGuestCheckout.tsx:29](../../../web/components/GroupGuestCheckout.tsx#L29) |
| UI-00659 | {details.kind==='UPI'?localize("ui.78f20d029371d3af",{defaultValue:"Show payment QR"}):localize("ui.00427c578c51e980",{defaultValue:"Show bank details"})} | {()=>setModal(true)} | {submit} | [web/components/GroupGuestCheckout.tsx:29](../../../web/components/GroupGuestCheckout.tsx#L29) |
| UI-00661 | {busy?localize("ui.49195f559e4a9f5b",{defaultValue:"Submitting…"}):localize("ui.6280d7748e32d3ac",{defaultValue:"I have done the payment"})} | submit / component action | {submit} | [web/components/GroupGuestCheckout.tsx:29](../../../web/components/GroupGuestCheckout.tsx#L29) |
| UI-00662 | {localize("ui.6e1b3d4c493ef1e2",{defaultValue:"Change payment method"})} | {()=>{setStage('start');setDetails(null);setSelected('');setReference('')}} | {submit} | [web/components/GroupGuestCheckout.tsx:29](../../../web/components/GroupGuestCheckout.tsx#L29) |
| UI-00663 | {localize("ui.0ab01a15c8871a3a",{defaultValue:"Copy bank details"})} | {()=>void trackedCopy(`${details.accountHolder}\n${details.accountNumber}\n${details.ifsc}`)} | Not a native form submit or inherited from component context | [web/components/GroupGuestCheckout.tsx:31](../../../web/components/GroupGuestCheckout.tsx#L31) |
| UI-00664 | {localize("ui.3866ccf908cc968d",{defaultValue:"Continue to reference details →"})} | {()=>setModal(false)} | Not a native form submit or inherited from component context | [web/components/GroupGuestCheckout.tsx:31](../../../web/components/GroupGuestCheckout.tsx#L31) |
| UI-00671 | {localize("ui.8f4dbb94585444b6",{defaultValue:"Save guest contribution settings"})} | submit / component action | {save} | [web/components/GroupGuestCreatorSettings.tsx:18](../../../web/components/GroupGuestCreatorSettings.tsx#L18) |
| UI-00686 | {localize("ui.a89376d87b358fce",{defaultValue:" Contribute"})} | {onContribute} | Not a native form submit or inherited from component context | [web/components/GuestActions.tsx:27](../../../web/components/GuestActions.tsx#L27) |
| UI-00688 | {localize("ui.fc40890ffdf9eb33",{defaultValue:" Save the date"})} | {saveDate} | Not a native form submit or inherited from component context | [web/components/GuestActions.tsx:27](../../../web/components/GuestActions.tsx#L27) |
| UI-00696 | {busy?localize("ui.b8ed5279e897be5d",{defaultValue:"Sending…"}):localize("ui.8536c15071befcf6",{defaultValue:"Send my RSVP →"})} | submit / component action | {submitRsvp} | [web/components/GuestCelebration.tsx:33](../../../web/components/GuestCelebration.tsx#L33) |
| UI-00702 | {localize("ui.c3812fc4acb861d5",{defaultValue:"Remove"})} | {()=>setPhoto(null)} | {submitWish} | [web/components/GuestCelebration.tsx:36](../../../web/components/GuestCelebration.tsx#L36) |
| UI-00703 | {busy?localize("ui.b8ed5279e897be5d",{defaultValue:"Sending…"}):localize("ui.1cae14f8ae4d97a2",{defaultValue:"Send my wishes ♡"})} | submit / component action | {submitWish} | [web/components/GuestCelebration.tsx:36](../../../web/components/GuestCelebration.tsx#L36) |
| UI-00705 | {localize("ui.710ae767f5e40eed",{defaultValue:"↻ Refresh wishes"})} | {()=>void refreshWishes()} | Not a native form submit or inherited from component context | [web/components/GuestCelebration.tsx:39](../../../web/components/GuestCelebration.tsx#L39) |
| UI-00706 | {showWishes?localize("ui.8fbc18362c024b5d",{defaultValue:"Show fewer wishes"}):localize("ui.99fabb2263ee65ee",{defaultValue:"View all wishes"})} | {()=>setShowWishes(v=>!v)} | Not a native form submit or inherited from component context | [web/components/GuestCelebration.tsx:40](../../../web/components/GuestCelebration.tsx#L40) |
| UI-00708 | {localize("ui.5b987d1a4ee6d349",{defaultValue:"Refresh fee"})} | {()=>{requestKey.current='';setPricingRetry(v=>v+1)}} | Not a native form submit or inherited from component context | [web/components/GuestContributionCheckout.tsx:117](../../../web/components/GuestContributionCheckout.tsx#L117) |
| UI-00711 | {localize("ui.942087cc2d41e013",{defaultValue:"Retry"})} | {()=>setDestinationRetry(v=>v+1)} | Not a native form submit or inherited from component context | [web/components/GuestContributionCheckout.tsx:140](../../../web/components/GuestContributionCheckout.tsx#L140) |
| UI-00712 | ✕ | {onClose} | Not a native form submit or inherited from component context | [web/components/GuestContributionCheckout.tsx:142](../../../web/components/GuestContributionCheckout.tsx#L142) |
| UI-00714 | {localize("ui.e382a36729887241",{defaultValue:"Make another contribution"})} | {()=>{setDirectStatus('');setTransferReference('');requestKey.current='';}} | Not a native form submit or inherited from component context | [web/components/GuestContributionCheckout.tsx:149](../../../web/components/GuestContributionCheckout.tsx#L149) |
| UI-00716 | {localize("ui.e382a36729887241",{defaultValue:"Make another contribution"})} | {()=>{setReceipt(null);requestKey.current='';setDirectStatus('');}} | Not a native form submit or inherited from component context | [web/components/GuestContributionCheckout.tsx:150](../../../web/components/GuestContributionCheckout.tsx#L150) |
| UI-00720 | {localize("ui.942087cc2d41e013",{defaultValue:"Retry"})} | {()=>setDestinationRetry(v=>v+1)} | {submit} | [web/components/GuestContributionCheckout.tsx:153](../../../web/components/GuestContributionCheckout.tsx#L153) |
| UI-00726 | {showContact?localize("ui.42485bdfe008e57e",{defaultValue:"− Hide optional contact details"}):localize("ui.958860832f4ffd8d",{defaultValue:"+ Add email or phone (optional)"})} | {()=>setShowContact(v=>!v)} | {submit} | [web/components/GuestContributionCheckout.tsx:164](../../../web/components/GuestContributionCheckout.tsx#L164) |
| UI-00731 | {localize("ui.6280d7748e32d3ac",{defaultValue:"I have done the payment"})} | {reportDirectPayment} | {submit} | [web/components/GuestContributionCheckout.tsx:174](../../../web/components/GuestContributionCheckout.tsx#L174) |
| UI-00733 | {busy?localize("ui.42074396a441a347",{defaultValue:"Processing…"}):localize("ui.0999a2d14c3594b1",{defaultValue:"Pay {{value0}} (Test Mode)",value0:(validAmount&&pricing?money(pricing.totalPaise):'')})} | submit / component action | {submit} | [web/components/GuestContributionCheckout.tsx:182](../../../web/components/GuestContributionCheckout.tsx#L182) |
| UI-00735 | {localize("ui.ab7ec41a34509538",{defaultValue:"Refresh gifts"})} | {()=>void load()} | Not a native form submit or inherited from component context | [web/components/GuestGiftBoard.tsx:14](../../../web/components/GuestGiftBoard.tsx#L14) |
| UI-00738 | {localize("ui.d8b8392e2c542950",{defaultValue:"Try again"})} | {()=>void load()} | Not a native form submit or inherited from component context | [web/components/GuestGiftBoard.tsx:14](../../../web/components/GuestGiftBoard.tsx#L14) |
| UI-00748 | {localize("ui.6ed0c15cca5b0b31",{defaultValue:"Add gift"})} | submit / component action | {add} | [web/components/GuestGiftManager.tsx:19](../../../web/components/GuestGiftManager.tsx#L19) |
| UI-00754 | {localize("ui.33f13d183c65606b",{defaultValue:"View delivery address"})} | {async()=>{setError('');setBusy(true);try{const address=await call(`/guest-gifts/${g.id}/delivery-address`);setAddresses(value=>({...value,[g.id]:address}))}catch(e){setError(e instanceof Error?e.message:'Could not load delivery address')}finally{setBusy(false)}}} | Not a native form submit or inherited from component context | [web/components/GuestGiftManager.tsx:19](../../../web/components/GuestGiftManager.tsx#L19) |
| UI-00755 | {localize("ui.672313aecf0e8f63",{defaultValue:"Publish gift to host"})} | {()=>void mutate(`/guest-gifts/${g.id}/publish`,{},'Gift published. The host can now see it in their contribution report.')} | Not a native form submit or inherited from component context | [web/components/GuestGiftManager.tsx:19](../../../web/components/GuestGiftManager.tsx#L19) |
| UI-00756 | {g.disabled?g.selection_label:type==='GROUP'?localize("ui.1fb7dd42a81e416b",{defaultValue:"Select for our group"}):localize("ui.54f1cbbbca8e29a8",{defaultValue:"Select this gift"})} | {()=>void mutate(`/guest-gifts/${g.id}/select`,{contributionType:type,...(type==='GROUP'?{groupId:group!.id}:{})},'Gift selected. You can now view the delivery address and publish when ready.')} | Not a native form submit or inherited from component context | [web/components/GuestGiftManager.tsx:19](../../../web/components/GuestGiftManager.tsx#L19) |
| UI-00759 | {localize("ui.942087cc2d41e013",{defaultValue:"Retry"})} | {()=>setRetry(v=>v+1)} | Not a native form submit or inherited from component context | [web/components/GuestVerifiedDestination.tsx:40](../../../web/components/GuestVerifiedDestination.tsx#L40) |
| UI-00764 | {localize("ui.56cab81c15312e7c",{defaultValue:"Copy UPI ID"})} | {()=>trackedCopy(selected.upiId\|\|'').then(()=>setCopyNotice('UPI ID copied.')).catch(()=>setCopyNotice('Select the UPI ID above to copy it.'))} | Not a native form submit or inherited from component context | [web/components/GuestVerifiedDestination.tsx:48](../../../web/components/GuestVerifiedDestination.tsx#L48) |
| UI-00766 | {localize("ui.a3c4e7c1a522f3dc",{defaultValue:"Show UPI QR"})} | {()=>setQrOpen(true)} | Not a native form submit or inherited from component context | [web/components/GuestVerifiedDestination.tsx:50](../../../web/components/GuestVerifiedDestination.tsx#L50) |
| UI-00768 | {localize("ui.e61997c6997c5afa",{defaultValue:"✕ Close"})} | {()=>setQrOpen(false)} | Not a native form submit or inherited from component context | [web/components/GuestVerifiedDestination.tsx:53](../../../web/components/GuestVerifiedDestination.tsx#L53) |
| UI-00788 | {collapsed?<PanelLeftOpen size={20}/>:<PanelLeftClose size={20}/>} | {toggle} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:50](../../../web/components/HostWorkspace.tsx#L50) |
| UI-00791 |  | {()=>void signOut()} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:53](../../../web/components/HostWorkspace.tsx#L53) |
| UI-00792 |  | {()=>setCollapsed(true)} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:54](../../../web/components/HostWorkspace.tsx#L54) |
| UI-00795 | {localize("ui.4b885106f27353d2",{defaultValue:"Fill gift details from link"})} | {()=>void lookup()} | Not a native form submit or inherited from component context | [web/components/IndependentGroupPlan.tsx:20](../../../web/components/IndependentGroupPlan.tsx#L20) |
| UI-00801 | {localize("ui.1ec42b70e129b073",{defaultValue:"Save our gift plan"})} | submit / component action | {save} | [web/components/IndependentGroupPlan.tsx:26](../../../web/components/IndependentGroupPlan.tsx#L26) |
| UI-00809 | {busy?localize("ui.72dd4b7eecd15686",{defaultValue:"Sharing payment details…"}):localize("ui.72f61dfd8fd1ae98",{defaultValue:"I’ve paid — share details with the host"})} | submit / component action | {submit} | [web/components/IndividualHostContribution.tsx:28](../../../web/components/IndividualHostContribution.tsx#L28) |
| UI-00810 | {localize("ui.d8b8392e2c542950",{defaultValue:"Try again"})} | {()=>setRetry(v=>v+1)} | Not a native form submit or inherited from component context | [web/components/IndividualHostContribution.tsx:28](../../../web/components/IndividualHostContribution.tsx#L28) |
| UI-00876 | Cookie preferences | {()=>window.dispatchEvent(new Event('giftwise-cookie-settings'))} | Not a native form submit or inherited from component context | [web/components/LegalFooter.tsx:4](../../../web/components/LegalFooter.tsx#L4) |
| UI-00929 | {localize("ui.c76f29ae1543f068",{defaultValue:"Use a different mobile number"})} | {()=>{setVerified(false);setMessage('')}} | Not a native form submit or inherited from component context | [web/components/MobileVerification.tsx:17](../../../web/components/MobileVerification.tsx#L17) |
| UI-00933 | {busy?localize("ui.4660a983366f7101",{defaultValue:"Please wait…"}):challenge?localize("ui.6d8e8cd5ef890b1d",{defaultValue:"Verify mobile"}):localize("ui.19e86d2b7055340c",{defaultValue:"Send verification code"})} | submit / component action | {submit} | [web/components/MobileVerification.tsx:17](../../../web/components/MobileVerification.tsx#L17) |
| UI-00934 | {localize("ui.7fd05b1a433fd9c4",{defaultValue:"Change number / request another code"})} | {()=>{setChallenge('');setCode('');setMessage('Wait at least 60 seconds before requesting another code.')}} | {submit} | [web/components/MobileVerification.tsx:17](../../../web/components/MobileVerification.tsx#L17) |
| UI-01006 | {localize("ui.517e0d761f33fe3b",{defaultValue:" Share invitation"})} | {()=>void share()} | Not a native form submit or inherited from component context | [web/components/SocialSharing.tsx:18](../../../web/components/SocialSharing.tsx#L18) |
| UI-01010 | {copied?<Check size={16}/>:<Copy size={16}/>} {copied?localize("ui.8d525e5f158b9afe",{defaultValue:"Copied"}):localize("ui.dbf362d4f210c780",{defaultValue:"Copy link"})} | {()=>void copy()} | Not a native form submit or inherited from component context | [web/components/SocialSharing.tsx:18](../../../web/components/SocialSharing.tsx#L18) |
| UI-01013 | {localize("ui.72c7b5708f440207",{defaultValue:"Add a Special Gift"})} | {()=>{setStep('choice');setError('')}} | Not a native form submit or inherited from component context | [web/components/SpecialGiftEntry.tsx:67](../../../web/components/SpecialGiftEntry.tsx#L67) |
| UI-01015 | × | {close} | Not a native form submit or inherited from component context | [web/components/SpecialGiftEntry.tsx:70](../../../web/components/SpecialGiftEntry.tsx#L70) |
| UI-01016 |  | {()=>void individual()} | Not a native form submit or inherited from component context | [web/components/SpecialGiftEntry.tsx:71](../../../web/components/SpecialGiftEntry.tsx#L71) |
| UI-01017 |  | {()=>{setError('');setStep('group')}} | Not a native form submit or inherited from component context | [web/components/SpecialGiftEntry.tsx:71](../../../web/components/SpecialGiftEntry.tsx#L71) |
| UI-01018 | {localize("ui.2f60681f8b63e96a",{defaultValue:"← Giving options"})} | {()=>setStep('choice')} | Not a native form submit or inherited from component context | [web/components/SpecialGiftEntry.tsx:72](../../../web/components/SpecialGiftEntry.tsx#L72) |
| UI-01021 | {localize("ui.0e1504cb20baa605",{defaultValue:"View host delivery address"})} | {async()=>{setBusy(true);setError('');try{await showAddress(saved.id)}catch(e){setError(e instanceof Error?e.message:'Address unavailable')}finally{setBusy(false)}}} | Not a native form submit or inherited from component context | [web/components/SpecialGiftEntry.tsx:73](../../../web/components/SpecialGiftEntry.tsx#L73) |
| UI-01022 | {saved.delivery_status==='GIFT_PURCHASED'\|\|saved.delivery_status==='TRACKING_SHARED'?localize("ui.e20ac06e7a6a8b8e",{defaultValue:"Gift purchased"}):localize("ui.9c788397748c304a",{defaultValue:"I have purchased the gift"})} | {()=>void updateDelivery('GIFT_PURCHASED')} | Not a native form submit or inherited from component context | [web/components/SpecialGiftEntry.tsx:73](../../../web/components/SpecialGiftEntry.tsx#L73) |
| UI-01025 | {localize("ui.15858678da15e73d",{defaultValue:"Share tracking with host"})} | submit / component action | {e=>{e.preventDefault();void updateDelivery('TRACKING_SHARED')}} | [web/components/SpecialGiftEntry.tsx:73](../../../web/components/SpecialGiftEntry.tsx#L73) |
| UI-01026 | {localize("ui.dabf10e2128f6ebd",{defaultValue:"Add another special gift"})} | {()=>{setSaved(null);setSource('');setForm(empty);setAddress(null);setTracking('');setNotice('');void individual()}} | Not a native form submit or inherited from component context | [web/components/SpecialGiftEntry.tsx:73](../../../web/components/SpecialGiftEntry.tsx#L73) |
| UI-01041 | {busy?localize("ui.2e3b4a7d1f13e7c3",{defaultValue:"Saving your gift…"}):method==='LINK_AND_CONTRIBUTION'?localize("ui.75bda07643c2766a",{defaultValue:"Continue to host payment"}):localize("ui.0580e20a82366727",{defaultValue:"Add my special gift"})} | submit / component action | {save} | [web/components/SpecialGiftEntry.tsx:79](../../../web/components/SpecialGiftEntry.tsx#L79) |
| UI-01042 | {localize("ui.d1f3b7c8d7b5ce7f",{defaultValue:"Manage "})} {g.name} | {()=>{setSaved(g);setAddress(null);setTracking('');setNotice('')}} | Not a native form submit or inherited from component context | [web/components/SpecialGiftEntry.tsx:81](../../../web/components/SpecialGiftEntry.tsx#L81) |
| UI-01055 | ✕ | {p.close} | Not a native form submit or inherited from component context | [web/components/focused/Checkout.tsx:13](../../../web/components/focused/Checkout.tsx#L13) |
| UI-01057 | {localize("ui.5aca8cea2b1e0071",{defaultValue:"Back to the celebration"})} | {p.close} | Not a native form submit or inherited from component context | [web/components/focused/Checkout.tsx:13](../../../web/components/focused/Checkout.tsx#L13) |
| UI-01059 | ₹ {Number(value).toLocaleString(activeLocale("en-IN"))} | {()=>p.setAmount(value)} | {e=>{if(step===0){e.preventDefault();if(p.valid&&p.name.trim())setStep(1)}else p.submit(e)}} | [web/components/focused/Checkout.tsx:13](../../../web/components/focused/Checkout.tsx#L13) |
| UI-01062 | {contact?localize("ui.3eed0166e7e7164d",{defaultValue:"Hide optional contact details"}):localize("ui.a4087e859882a5ba",{defaultValue:"+ Add email or phone · optional"})} | {()=>setContact(v=>!v)} | {e=>{if(step===0){e.preventDefault();if(p.valid&&p.name.trim())setStep(1)}else p.submit(e)}} | [web/components/focused/Checkout.tsx:13](../../../web/components/focused/Checkout.tsx#L13) |
| UI-01065 | {localize("ui.496267ccc6b01605",{defaultValue:"Continue →"})} | submit / component action | {e=>{if(step===0){e.preventDefault();if(p.valid&&p.name.trim())setStep(1)}else p.submit(e)}} | [web/components/focused/Checkout.tsx:13](../../../web/components/focused/Checkout.tsx#L13) |
| UI-01066 | {localize("ui.422db74b1f68a344",{defaultValue:"Edit amount"})} | {()=>setStep(0)} | {e=>{if(step===0){e.preventDefault();if(p.valid&&p.name.trim())setStep(1)}else p.submit(e)}} | [web/components/focused/Checkout.tsx:13](../../../web/components/focused/Checkout.tsx#L13) |
| UI-01070 | {p.busy?localize("ui.8d15b0fbf84af930",{defaultValue:"Opening secure checkout…"}):localize("ui.78ce67147916e011",{defaultValue:"Pay {{value0}} · Test mode",value0:(inr(p.totalPaise??amount))})} | submit / component action | {e=>{if(step===0){e.preventDefault();if(p.valid&&p.name.trim())setStep(1)}else p.submit(e)}} | [web/components/focused/Checkout.tsx:13](../../../web/components/focused/Checkout.tsx#L13) |
| UI-01071 | {p.busy?localize("ui.404bb57ce1e21631",{defaultValue:"Sending details…"}):localize("ui.7521686e8b908e2e",{defaultValue:"Submit transfer details"})} | {p.report} | {e=>{if(step===0){e.preventDefault();if(p.valid&&p.name.trim())setStep(1)}else p.submit(e)}} | [web/components/focused/Checkout.tsx:13](../../../web/components/focused/Checkout.tsx#L13) |
| UI-01113 | {localize("ui.d8b8392e2c542950",{defaultValue:"Try again"})} | {onRetry} | Not a native form submit or inherited from component context | [web/components/focused/Invitation.tsx:36](../../../web/components/focused/Invitation.tsx#L36) |
| UI-01115 | {(g.contributionEnabled===false)?g.contributionDisabledReason\|\|localize("ui.f111404f214c5080",{defaultValue:"A lovely surprise is on its way ♡"}):preview?localize("ui.aa35c713806f8aac",{defaultValue:"Contribution preview only"}):localize("ui.97b0c61b99f65f9e",{defaultValue:"Contribute"})} | {()=>open(g)} | Not a native form submit or inherited from component context | [web/components/focused/Invitation.tsx:36](../../../web/components/focused/Invitation.tsx#L36) |
| UI-01116 | {localize("ui.eaed0473cc93222a",{defaultValue:"Choose an amount"})} | {()=>open(null)} | Not a native form submit or inherited from component context | [web/components/focused/Invitation.tsx:36](../../../web/components/focused/Invitation.tsx#L36) |
| UI-01118 | {localize("ui.d8b8392e2c542950",{defaultValue:"Try again"})} | {()=>location.reload()} | Not a native form submit or inherited from component context | [web/components/focused/Invitation.tsx:41](../../../web/components/focused/Invitation.tsx#L41) |


### Dialog and alert/status inventory

| ID | Role / kind | Text / label | Visibility conditions | Source |
| --- | --- | --- | --- | --- |
| UI-00550 | dialog |  | Parent/source state | [web/components/GiftDescription.tsx:13](../../../web/components/GiftDescription.tsx#L13) |
| UI-00558 | "dialog" | {logged&&guestGifts&&<><div className="gc-dash-head"><h2>{localize("ui.8256358119aa18be",{defaultValue:"Your guest account"})}</h2>{!group&&<button type="button" disabled={busy} onClick={()=>void logout()}>{localize("ui.48f0d3d397d49f13",{defaultValue:"Sign out"})}</button>}</div><GuestGiftManager token={token} call={call} group={group}/></>} {logged&&accountNotice&&<p role="status">{accountNotice}</p>} {logged&&(emailVerificationEnabled\|\|mobileOtpEnabled)&&<details className="gc-panel"><summary>{localize("ui.d3e0dc0fef4e6514",{defaultValue:"Account · contact verification"})}</summary><GroupEmailVerification token={token} pageId={pageId.current!} onVerified={()=>void refreshPlan()}/><MobileVerification token={token} pageId={pageId.current!} onVerified={()=>void restore()}/></details>} {error&&<p className="gc-error gc-sticky-feedback" role="alert">{localizeText(error)}</p>} {loading?<p>{localize("ui.82b3dda5006dbb6c",{defaultValue:"Opening your group…"})}</p>:!logged?<><div className="gc-auth-heading"><span className="gc-auth-icon" aria-hidden="true">♧</span><h2>{guestGifts?localize("ui.03eacf45d0c7ecb2",{defaultValue:"A thoughtful gift from you."}):localize("ui.fad5131ccbf85a5a",{defaultValue:"A thoughtful gift, together."})}</h2><p>{guestGifts?localize("ui.f61892905889c364",{defaultValue:"Sign in or create an account to manage an individual gift or organize a group gift."}):localize("ui.74e05d026c54ca8a",{defaultValue:"Create a group or sign in to organize your people."})}</p></div><div className="gc-tabs" role="tablist"><button type="button" aria-selected={tab==='register'} role="tab" onClick={()=>{setTab('register');setError('')}}>{guestGifts?localize("ui.798ca2ce18bf2038",{defaultValue:"Create account"}):localize("ui.1213394682bfa8c6",{defaultValue:"Create Group"})}</button><button type="button" aria-selected={tab==='login'} role="tab" onClick={()=>{setTab('login');setError('')}}>{guestGifts?localize("ui.bfd402b2f6f38125",{defaultValue:"Sign in"}):localize("ui.3d65b17f4fb1f79f",{defaultValue:"Open Group"})}</button></div>{emailVerificationEnabled&&<div className="recovery-links"><a href={`/forgot-username?kind=GROUP_ADMIN&eventToken=${encodeURIComponent(token)}&returnTo=${encodeURIComponent('/e/'+token)}`}>{localize("ui.91d00522b02cc29d",{defaultValue:"Forgot username?"})}</a><a href={`/forgot-password?kind=GROUP_ADMIN&returnTo=${encodeURIComponent('/e/'+token)}`}>{localize("ui.6dbd52ba198f47cc",{defaultValue:"Forgot password or PIN?"})}</a></div>}<form className={`gc-form gc-auth-form ${tab==='register'?'gc-register-grid':''}`} onSubmit={auth}>{tab==='register'&&<><SignupEmailField value={registrationEmail} onChange={setRegistrationEmail}/><label>{localize("ui.187e80ba4d8a22bb",{defaultValue:"Mobile number"})}<input required type="tel" autoComplete="tel" placeholder="+919876543210" value={registrationMobile} onChange={e=>setRegistrationMobile(e.target.value)}/></label><p>{localize("ui.b8c79ea3a5538c19",{defaultValue:"Use your own email and mobile for account recovery."})}</p></>}{tab==='register'&&<label>{localize("ui.f13a64ba2fea87ef",{defaultValue:"Full name"})}<input required minLength={2} maxLength={120} value={fullName} onChange={e=>setFullName(e.target.value)}/></label>}{<label>{localize("ui.e3b89e9d33f88e52",{defaultValue:"Username"})}<input required minLength={tab==='register'?4:undefined} maxLength={tab==='register'?30:undefined} pattern={tab==='register'?'[A-Za-z0-9][A-Za-z0-9_]{3,29}':undefined} aria-describedby={tab==='register'?'group-username-rules':undefined} autoComplete="username" autoCapitalize="none" spellCheck={false} value={username} onChange={e=>setUsername(e.target.value)}/></label>}{tab==='register'&&<CredentialRequirements kind="username" value={username} id="group-username-rules"/>}{tab==='login'&&<label>{localize("ui.11aaa14268df51b7",{defaultValue:"Sign in with"})}<select value={loginMethod} onChange={e=>setLoginMethod(e.target.value as 'password'\|'pin')}>{allowPin&&<option value="pin">{localize("ui.cb6246bd8eb1937b",{defaultValue:"Username and six-digit PIN"})}</option>}<option value="password">{localize("ui.e3db92128adfe5d4",{defaultValue:"Username and password"})}</option></select></label>}{(tab==='register'\|\|loginMethod==='password')&&<label>{localize("ui.e7cf3ef4f17c3999",{defaultValue:"Password"})}<input required minLength={tab==='register'?8:undefined} aria-describedby={tab==='register'?'group-password-rules':undefined} type="password" autoComplete={tab==='register'?'new-password':'current-password'} value={password} onChange={e=>setPassword(e.target.value)}/></label>}{tab==='register'&&<><CredentialRequirements kind="password" value={password} id="group-password-rules"/><TermsAcceptance checked={termsAccepted} onChange={setTermsAccepted}/></>}{tab==='register'&&<label>{localize("ui.5ac265f396a2c586",{defaultValue:"Confirm password"})}<input required type="password" minLength={8} value={confirm} onChange={e=>setConfirm(e.target.value)}/></label>}{tab==='register'&&allowPin&&<><label>{localize("ui.61c2b9f2c16ea005",{defaultValue:"Six-digit PIN"})}<input required type="password" inputMode="numeric" pattern="[0-9]{6}" minLength={6} maxLength={6} value={pin} onChange={e=>setPin(e.target.value)}/></label><label>{localize("ui.4fa10c7baa0c1d89",{defaultValue:"Confirm PIN"})}<input required type="password" inputMode="numeric" pattern="[0-9]{6}" minLength={6} maxLength={6} value={confirmPin} onChange={e=>setConfirmPin(e.target.value)}/></label></>}{tab==='login'&&loginMethod==='pin'&&<label>{localize("ui.61c2b9f2c16ea005",{defaultValue:"Six-digit PIN"})}<input required type="password" inputMode="numeric" pattern="[0-9]{6}" minLength={6} maxLength={6} value={pin} onChange={e=>setPin(e.target.value)}/></label>}<button disabled={busy\|\|allowPin===null\|\|(tab==='register'&&!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(registrationEmail.trim()))} className="gc-launch">{busy?localize("ui.4660a983366f7101",{defaultValue:"Please wait…"}):tab==='register'?localize("ui.6c9206e4b89975a5",{defaultValue:"Register →"}):localize("ui.8637e0051dee66ad",{defaultValue:"Sign in →"})}</button></form></>:group?<>{!published&&<section className="gc-panel"><h3>{localize("ui.91742f59a9aebbcc",{defaultValue:"Group coordination fee"})}</h3><p>{localize("ui.4c809ce0c8348bf6",{defaultValue:"A one-time fee paid by the creator. Member transfers still go directly to the creator. If the fee is switched off, publish without a purchase."})}</p><CommercePurchase stream="GROUP_GIFT" resourceId={group.id} groupBase={`/api/public/groups/${token}/coordination/${group.id}`} pageId={pageId.current!} onComplete={()=>void restore()}/></section>}<details className="gc-panel gc-profile"><summary>{localize("ui.9c9a74b827864251",{defaultValue:"Your profile · "})}{adminName\|\|localize("ui.715a9cc0c34c96c1",{defaultValue:"Organizer"})}</summary>{adminName&&!editingName?<><h3>{localize("ui.0c7229ae1986eb6d",{defaultValue:"Welcome back, "})}{adminName}!</h3><button type="button" className="gc-secondary" onClick={()=>{setAdminNameDraft(adminName);setEditingName(true)}}>{localize("ui.308b88fcc3dffe50",{defaultValue:"Edit name"})}</button></>:<form className="gc-form" onSubmit={async e=>{e.preventDefault();try{const x=await call('/profile-name',{fullName:adminNameDraft});setAdminName(x.fullName);setEditingName(false);setError('')}catch(x){setError(x instanceof Error?x.message:'Unable to save name')}}}><label>{localize("ui.d82e5fe24eed3db4",{defaultValue:"What should we call you?"})}<input required minLength={2} maxLength={120} value={adminNameDraft} onChange={e=>setAdminNameDraft(e.target.value)}/></label><button className="gc-secondary">{localize("ui.b7297226fd1f4681",{defaultValue:"Save name"})}</button></form>}</details><div className="gc-dash-head"><div><h2>{group.name}</h2><p>{localize("ui.b2742e6cae248f51",{defaultValue:"Created by "})}{group.creatorName}</p></div><div className="gc-dash-actions"><span className="gc-status">{independent?localize("ui.a2d472a9d7005732",{defaultValue:"Group created ♡"}):group.status==='DRAFT'?localize("ui.da71fe2468d51ec1",{defaultValue:"Private draft"}):group.status}</span><button type="button" className="gc-secondary" disabled={busy} onClick={()=>void logout()}>{localize("ui.48f0d3d397d49f13",{defaultValue:"Sign out"})}</button></div></div>{allowPin&&<details className="gc-panel" open={editingPin} onToggle={e=>setEditingPin(e.currentTarget.open)}><summary>{localize("ui.725e855de4090424",{defaultValue:"Account security (change PIN)"})}</summary><form className="gc-form" onSubmit={async e=>{e.preventDefault();setPinNotice('');try{await call('/pin/setup',{password:pinPassword,pin,confirmPin});setPinNotice('PIN updated successfully. You can now sign in with your new six-digit PIN.');setPinPassword('');setPin('');setConfirmPin('');setEditingPin(false)}catch(x){setPinNotice(x instanceof Error?x.message:'PIN setup failed')}}}><label>{localize("ui.72ed2bd767cef627",{defaultValue:"Current password"})}<input required type="password" value={pinPassword} onChange={e=>setPinPassword(e.target.value)}/></label><label>{localize("ui.61c2b9f2c16ea005",{defaultValue:"Six-digit PIN"})}<input required type="password" inputMode="numeric" pattern="[0-9]{6}" maxLength={6} value={pin} onChange={e=>setPin(e.target.value)}/></label><label>{localize("ui.4fa10c7baa0c1d89",{defaultValue:"Confirm PIN"})}<input required type="password" inputMode="numeric" pattern="[0-9]{6}" maxLength={6} value={confirmPin} onChange={e=>setConfirmPin(e.target.value)}/></label><button className="gc-secondary">{localize("ui.a70c9f89a0495049",{defaultValue:"Save PIN"})}</button>{pinNotice&&<p role="status">{pinNotice}</p>}</form></details>}<div className="gc-metrics"><div><small>{localize("ui.40195ce9e297eb0d",{defaultValue:"Gift goal"})}</small><strong>{group.targetPaise!=null?localize("ui.b3da1e4c465e3370",{defaultValue:"₹{{value0}}",value0:((group.targetPaise/100).toLocaleString(activeLocale("en-IN")))}):localize("ui.4895f73177ab5d67",{defaultValue:"Not set"})}</strong></div><div><small>{localize("ui.1044a4c056d0d685",{defaultValue:"Members"})}</small><strong>{summary?.member_count??'—'}</strong></div><div><small>{localize("ui.7eeed9ef9dbcff95",{defaultValue:"Reported transfers — unverified"})}</small><strong>{summary?localize("ui.b3da1e4c465e3370",{defaultValue:"₹{{value0}}",value0:((Number(summary.reported_paise)/100).toLocaleString(activeLocale("en-IN")))}):'—'}</strong></div></div>{independent&&<section className="gc-panel"><h3>{localize("ui.75b8cccda0da7123",{defaultValue:"Your group is cooking up something special 🍳"})}</h3><p>{localize("ui.f1cdc295845ac415",{defaultValue:"Your group is now listed on the invitation. Add a receiving account, enable contributions, then share your group link. Friends can give with or without signing in."})}</p><button type="button" className="gc-secondary" onClick={()=>setSection('payments')}>{localize("ui.331135fcb9bb6f25",{defaultValue:"1. Receiving account"})}</button> <button type="button" className="gc-secondary" onClick={()=>setSection('guest')}>{localize("ui.be5fb8c43cc3df3e",{defaultValue:"2. Enable contributions"})}</button> <button type="button" className="gc-secondary" onClick={()=>setSection('overview')}>{localize("ui.6bdf75ffd0fd9e1c",{defaultValue:"3. Invite friends"})}</button></section>}<div className="gc-nav"><button type="button" aria-pressed={section==='overview'} onClick={()=>{setSection('overview');void restore()}}>{localize("ui.d4b1ea5708dd5329",{defaultValue:"Overview"})}</button><button type="button" aria-pressed={section==='privacy'} onClick={()=>setSection('privacy')}>{localize("ui.3bc6107114c6fa86",{defaultValue:"Privacy & settings"})}</button><button type="button" aria-pressed={section==='members'} onClick={()=>void loadMembers()}>{localize("ui.7f65e0162671c9e9",{defaultValue:"Members & requests"})}</button><button type="button" aria-pressed={section==='payments'} onClick={()=>setSection('payments')}>{localize("ui.cc64bb493268ea0b",{defaultValue:"Payment accounts"})}</button><button type="button" aria-pressed={section==='contribute'} onClick={()=>setSection('contribute')}>{localize("ui.4398ddbea4af82a8",{defaultValue:"♡ My contribution"})}</button><button type="button" aria-pressed={section==='guest'} onClick={()=>setSection('guest')}>{localize("ui.fd58f51f50008f89",{defaultValue:"Guest contributions"})}</button></div>{section==='contribute'?<div className="gc-panel"><h3>{localize("ui.e7e545c8948180c1",{defaultValue:"Make your contribution"})}</h3><p>{localize("ui.05e8e727cb7f1398",{defaultValue:"Join your group’s gift and keep your transfer details in one place."})}</p>{ready.enabled?<GroupGuestCheckout token={token} groupId={group.id} creator creatorName={group.creatorName} pageId={pageId.current!}/>:<p>{localize("ui.f5dea2ce8ef939dd",{defaultValue:"Enable the invite link from Overview, then enable a payment method in Guest contributions to start."})}</p>}</div>:section==='guest'?<GroupGuestCreatorSettings token={token} groupId={group.id} pageId={pageId.current!}/>:section==='payments'?<GroupCreatorPaymentSettings token={token} groupId={group.id} pageId={pageId.current!}/>:section==='overview'?<div className="gc-panel"><h3>{localize("ui.8b2013104f003ec3",{defaultValue:"Group overview"})}</h3>{group.giftName&&<p>🎁 {group.giftName}</p>}{group.description&&<p>{group.description}</p>}<p>{localize("ui.c9824d81420f7218",{defaultValue:"Group membership is available. Share the invite link after enabling it."})}</p>{ready.enabled?<><p>{localize("ui.53f01086576d54b2",{defaultValue:"Group link: "})}<a href={`/groups/${token}/${group.id}`}>{localize("ui.e2779e3f74231d11",{defaultValue:"Open member join page"})}</a></p><button type="button" className="gc-secondary" onClick={()=>void trackedCopy(`${location.origin}/groups/${token}/${group.id}`)}>{localize("ui.5c58cd7963cac4e2",{defaultValue:"Copy invite link"})}</button></>:<><span title={ready.reasons.join('; ')}><button disabled>{localize("ui.16f6803a1f4a8a60",{defaultValue:"Invite link unavailable"})}</button></span><p>{ready.reasons.join('; ')}</p></>}<p><strong>{localize("ui.872f2464b9c2c706",{defaultValue:"Host visibility:"})}</strong> {published?localize("ui.6a18ba722ec4c775",{defaultValue:"Collection published to the host."}):localize("ui.e9f1ebfb29dc36b5",{defaultValue:"Your collection stays private until you publish it."})}</p><span title={ready.reasons.join('; ')}><button type="button" className="gc-launch" disabled={busy\|\|!ready.enabled\|\|published} onClick={()=>void publishGroup()}>{published?localize("ui.a1ddffdbaeaee49f",{defaultValue:"Collection published"}):localize("ui.a90c8deaaa3deaaf",{defaultValue:"Publish collection to host"})}</button></span>{independent?<IndependentGroupPlan key={group.id} group={group} call={call} onChange={refreshPlan} busy={busy} setBusy={setBusy}/>:<GroupGiftPlan groupId={group.id} giftName={group.giftName} selectedGiftId={selectedGiftId} reserved={reserved} emailVerified={creatorEmailVerified} wishlist={wishlist} call={call} onChange={refreshPlan} onNotice={setAccountNotice} busy={busy} setBusy={setBusy}/>}<GroupActivity url={base+`/activity/${group.id}`} pageId={pageId.current!} revision={historyRevision}/></div>:section==='members'?<div className="gc-panel"><h3>{localize("ui.51c8cfe9e55a8795",{defaultValue:"Member requests"})}</h3><p>{localize("ui.c0f285910c566cf7",{defaultValue:"Rejected members are visible only here. Mobile numbers are private."})}</p>{members.length===0?<p>{localize("ui.ea923f45a415c408",{defaultValue:"No requests yet."})}</p>:members.map(m=><div key={m.id} className="gc-member"><strong>{m.display_name}</strong> · {localizeText(m.status)}<small>{m.mobile}</small>{m.status==='PENDING'&&<div><button type="button" className="gc-secondary" disabled={busy} onClick={()=>void reviewMember(m.id,'APPROVE')}>{localize("ui.6007acbe30b2cd98",{defaultValue:"Approve"})}</button> <button type="button" className="gc-secondary" disabled={busy} onClick={()=>void reviewMember(m.id,'REJECT')}>{localize("ui.ab604a360777735f",{defaultValue:"Reject"})}</button></div>}</div>)}</div>:<form className="gc-form gc-panel" onSubmit={savePrivacy}><h3>{localize("ui.313cee8924674208",{defaultValue:"Who can see what?"})}</h3><p>{localize("ui.45855ba773e4b714",{defaultValue:"These settings control joining and member visibility. Manual transfers remain unverified until independently confirmed."})}</p><label>{localize("ui.c99c586025d6769c",{defaultValue:"Who can join?"})}<select value={privacy.join_policy} onChange={e=>setPrivacy({...privacy,join_policy:e.target.value})}><option value="APPROVAL">{localize("ui.695adaeb4a184ef9",{defaultValue:"Anyone with invite link — Admin approval"})}</option><option value="AUTO_JOIN">{localize("ui.017e668486e017cc",{defaultValue:"Anyone with invite link — Auto join"})}</option><option value="INVITE_ONLY">{localize("ui.f689ac53ea5b8b5a",{defaultValue:"Invitation only — individual invitation required"})}</option></select></label><label>{localize("ui.2ad6410b25fa2dbb",{defaultValue:"Who can see the member list?"})}<select value={privacy.member_visibility} onChange={e=>setPrivacy({...privacy,member_visibility:e.target.value})}><option value="ADMIN_ONLY">{localize("ui.f274de8e243d9852",{defaultValue:"Admin only"})}</option><option value="MEMBERS">{localize("ui.ecde6f8618226d17",{defaultValue:"Approved members"})}</option></select></label><label>{localize("ui.5bc6d84f2126eb5e",{defaultValue:"Who can see individual contribution amounts?"})}<select value={privacy.amount_visibility} onChange={e=>setPrivacy({...privacy,amount_visibility:e.target.value})}><option value="ADMIN_ONLY">{localize("ui.f274de8e243d9852",{defaultValue:"Admin only"})}</option><option value="OWN_ONLY">{localize("ui.984807e2f71329dd",{defaultValue:"Each member sees only their own"})}</option></select></label><p>{localize("ui.11d0ef3a4dbea7a0",{defaultValue:"To offer UPI or bank transfers, configure Payment accounts, then choose accounts and contribution access under Guest contributions. Reported transfers do not confirm receipt of funds."})}</p><p><strong>{localize("ui.1ee6f936b3bcf983",{defaultValue:"Host access:"})}</strong> {localize("ui.a9bb8e886b07bdad",{defaultValue:" Collection details are shared only when you publish to the host."})}</p><button className="gc-launch" disabled={busy}>{busy?localize("ui.23e39291d6135814",{defaultValue:"Saving…"}):localize("ui.cd933d481eefc0f0",{defaultValue:"Save privacy settings"})}</button></form>}</>:<><details open={independent\|\|!guestGifts}><summary>{localize("ui.bbc68131737f8c68",{defaultValue:"Create a group for a gift together"})}</summary><h2>{localize("ui.007dbac389390c94",{defaultValue:"Create your group"})}</h2><p>{independent?localize("ui.44803348cc757651",{defaultValue:"Tell us about your group. It will appear as Something is Cooking while you plan your special gift."}):localize("ui.af064717e6e90911",{defaultValue:"Tell us about your group. Only you can see these details for now."})}</p><form className="gc-form" onSubmit={setup}><label>{localize("ui.6cb353a65d0160d5",{defaultValue:"Group name *"})}<input required maxLength={120} value={form.name} onChange={e=>setForm({...form,name:e.target.value})}/></label><label>{localize("ui.7204645b7341d714",{defaultValue:"Your name *"})}<input required maxLength={120} value={form.creatorName} onChange={e=>setForm({...form,creatorName:e.target.value})}/></label><label>{localize("ui.0836c3b7cadbef5b",{defaultValue:"Mobile number *"})}<input required type="tel" maxLength={20} value={form.creatorMobile} onChange={e=>setForm({...form,creatorMobile:e.target.value})}/></label><label>{localize("ui.751e68347f10bcf7",{defaultValue:"Group description"})}<textarea maxLength={500} value={form.description} onChange={e=>setForm({...form,description:e.target.value})}/><small>{form.description.length}/500 Characters</small></label>{independent?<IndependentGroupPlanFields value={form} onChange={value=>setForm(current=>({...current,...value}))} call={call} busy={busy} setBusy={setBusy}/>:<><label>{localize("ui.09b05cb4d017cf72",{defaultValue:"Collection plan"})}<select value={giftId} onChange={e=>{setGiftId(e.target.value);if(!e.target.value)setForm(value=>({...value,giftName:'',giftDescription:'',giftLink:'',targetRupees:''}))}}><option value="">{localize("ui.571c56f35c99a8c9",{defaultValue:"General collection · choose a gift later"})}</option>{wishlist.filter(g=>g.contributionEnabled!==false).map(g=><option key={g.id} value={g.id}>{g.name}</option>)}</select></label>{giftId&&<p>{localize("ui.ba220a94b033285b",{defaultValue:"Wishlist gift details are filled in below. The gift stays available until your creator email is verified."})}</p>}<label>{localize("ui.4e82ed6b989eb0fb",{defaultValue:"Gift name (optional)"})}<input maxLength={160} readOnly={!!giftId} value={form.giftName} onChange={e=>setForm({...form,giftName:e.target.value})}/></label><label>{localize("ui.da0378150bd065d4",{defaultValue:"Gift description"})}<textarea maxLength={500} readOnly={!!giftId} value={form.giftDescription} onChange={e=>setForm({...form,giftDescription:e.target.value})}/></label><label>{localize("ui.5c7466d4572b8863",{defaultValue:"Gift link"})}<input type="url" maxLength={1000} readOnly={!!giftId} value={form.giftLink} onChange={e=>setForm({...form,giftLink:e.target.value})}/></label><label>{localize("ui.1c68de56ec168a54",{defaultValue:"Gift goal in ₹"})}<input inputMode="decimal" readOnly={!!giftId} value={form.targetRupees} onChange={e=>setForm({...form,targetRupees:e.target.value})}/></label></>}<button className="gc-launch" disabled={busy}>{busy?localize("ui.c79ed9492e3c1719",{defaultValue:"Creating…"}):localize("ui.5457c9f7d1371da0",{defaultValue:"Create private group →"})}</button></form></details></>} | open | [web/components/GroupContribution.tsx:89](../../../web/components/GroupContribution.tsx#L89) |
| UI-00625 | "dialog" | {localize("ui.cc2f3579e157b2d0",{defaultValue:"UPI QR preview"})} | preview | [web/components/GroupCreatorPaymentSettings.tsx:26](../../../web/components/GroupCreatorPaymentSettings.tsx#L26) |
| UI-00767 | "dialog" | {/* eslint-disable-next-line @next/next/no-img-element */} | !focused&&qrOpen; selected?.method==='UPI'&&selected.qrPngBase64&&!focused; eligible.length===0; error; loading | [web/components/GuestVerifiedDestination.tsx:52](../../../web/components/GuestVerifiedDestination.tsx#L52) |
| UI-01014 | "dialog" | {step==='choice'?<><h2 id="special-gift-title">{localize("ui.9ead2ac60e7db3f4",{defaultValue:"How would you like to give?"})}</h2><p>{localize("ui.a282a41c377c540a",{defaultValue:"Plan a thoughtful gift on your own or bring your favourite people together."})}</p><div className="special-gift-choices"><button data-autofocus type="button" className="gc-secondary" onClick={()=>void individual()}><strong>{localize("ui.c14c2c732437af98",{defaultValue:"Individual Gift"})}</strong><span>{localize("ui.1661cf99f1c904d8",{defaultValue:"Add your details and choose how to share your gift."})}</span><small>{localize("ui.8870479736489e78",{defaultValue:"No account or group needed"})}</small></button><button type="button" className="gc-secondary" onClick={()=>{setError('');setStep('group')}}><strong>{localize("ui.62d0a830e2756168",{defaultValue:"Group Gift"})}</strong><span>{localize("ui.3ab6895ab8f19cc9",{defaultValue:"Create a group and collect contributions together."})}</span><small>{localize("ui.e89f3bfbd1e4e4c0",{defaultValue:"Sign in or create an account to get started"})}</small></button></div></>:<> <button type="button" className="gc-secondary" disabled={busy} onClick={()=>setStep('choice')}>{localize("ui.2f60681f8b63e96a",{defaultValue:"← Giving options"})}</button><h2 id="special-gift-title">{localize("ui.57037bdffdc5602d",{defaultValue:"Your special gift"})}</h2> {saved?<><p role="status">{notice\|\|localize("ui.18be7e148c04c0fa",{defaultValue:"Manage {{value0}}",value0:(saved.name)})}</p>{saved.giving_method==='LINK_AND_CONTRIBUTION'&&<IndividualHostContribution token={token} giftId={saved.id} call={call} onBusyChange={setBusy} onReported={()=>notifyGuestGifts(token)}/>} {saved.giving_method==='DIRECT_DELIVERY'&&<section className="gc-panel"><h3>{localize("ui.02968357c8dc9617",{defaultValue:"Deliver your gift to the host"})}</h3><p>{localize("ui.ecf0f6235c0d7d99",{defaultValue:"Purchase the item from the retailer and use the host’s address at checkout."})}</p>{saved.product_url&&<a href={saved.product_url} target="_blank" rel="noopener noreferrer">{localize("ui.a1fc818d679ea436",{defaultValue:"Purchase gift from the retailer ↗"})}</a>}{address?<address>{address.addressLine1\|\|localize("ui.34e92463e22d5ad8",{defaultValue:"Please contact the host to confirm the delivery address."})}<br/>{address.city}, {address.state} {address.postalCode}<br/>{address.country}</address>:<button type="button" disabled={busy} onClick={async()=>{setBusy(true);setError('');try{await showAddress(saved.id)}catch(e){setError(e instanceof Error?e.message:'Address unavailable')}finally{setBusy(false)}}}>{localize("ui.0e1504cb20baa605",{defaultValue:"View host delivery address"})}</button>}<button type="button" className="gc-secondary" disabled={busy\|\|saved.delivery_status==='TRACKING_SHARED'\|\|saved.delivery_status==='GIFT_PURCHASED'} onClick={()=>void updateDelivery('GIFT_PURCHASED')}>{saved.delivery_status==='GIFT_PURCHASED'\|\|saved.delivery_status==='TRACKING_SHARED'?localize("ui.e20ac06e7a6a8b8e",{defaultValue:"Gift purchased"}):localize("ui.9c788397748c304a",{defaultValue:"I have purchased the gift"})}</button><form className="gc-form" onSubmit={e=>{e.preventDefault();void updateDelivery('TRACKING_SHARED')}}><label>{localize("ui.f296b67a545cd756",{defaultValue:"Shipment tracking details"})}<textarea required maxLength={1000} placeholder={localize("ui.a9da12665e488803",{defaultValue:"Carrier name, tracking number or tracking link"})} value={tracking} onChange={e=>setTracking(e.target.value)}/></label><button className="gc-launch" disabled={busy}>{localize("ui.15858678da15e73d",{defaultValue:"Share tracking with host"})}</button></form></section>}<button type="button" className="gc-secondary" disabled={busy} onClick={()=>{setSaved(null);setSource('');setForm(empty);setAddress(null);setTracking('');setNotice('');void individual()}}>{localize("ui.dabf10e2128f6ebd",{defaultValue:"Add another special gift"})}</button></>:<form className="gc-form" onSubmit={save}> {gifts.length>0&&<label>{localize("ui.af7e58d035aad83e",{defaultValue:"Choose a gift"})}<select disabled={busy} value={source} onChange={e=>choose(e.target.value)}><option value="">{localize("ui.cb480b3aa513d781",{defaultValue:"Add my own special gift"})}</option>{gifts.map(g=><option key={g.id} value={g.id}>{g.name}</option>)}</select></label>} <fieldset disabled={busy}><legend>{localize("ui.c0bcd4c0f8e70784",{defaultValue:"Your details"})}</legend><label>{localize("ui.2c6b2e253c01fb2e",{defaultValue:"Your name"})}<input data-autofocus required autoComplete="name" maxLength={120} value={form.guestName} onChange={e=>setForm({...form,guestName:e.target.value})}/></label><label>{localize("ui.f2488fd4ef4adbc6",{defaultValue:"Email address"})}<input required type="email" autoComplete="email" maxLength={254} value={form.email} onChange={e=>setForm({...form,email:e.target.value})}/></label><label>{localize("ui.187e80ba4d8a22bb",{defaultValue:"Mobile number"})}<input required type="tel" autoComplete="tel" pattern="\+?[0-9][0-9 \-]{7,23}" maxLength={25} value={form.mobile} onChange={e=>setForm({...form,mobile:e.target.value})}/></label></fieldset> <fieldset disabled={busy}><legend>{localize("ui.f2da19dd48c937be",{defaultValue:"Gift details"})}</legend><label>{localize("ui.64b4ff26c07d838e",{defaultValue:"Gift name"})}<input required maxLength={160} value={form.name} onChange={e=>setForm({...form,name:e.target.value})}/></label><label>{localize("ui.a06890de939bdcac",{defaultValue:"Product link"})}{method==='DIRECT_DELIVERY'?'':localize("ui.418e319e80da27d9",{defaultValue:" · optional if you add gift details"})}<input type="url" required={method==='DIRECT_DELIVERY'} pattern="https://[^\s]+" placeholder="https://…" maxLength={1000} value={form.productUrl} onChange={e=>setForm({...form,productUrl:e.target.value})}/></label><label>{localize("ui.f2da19dd48c937be",{defaultValue:"Gift details"})}<textarea maxLength={500} value={form.description} onChange={e=>setForm({...form,description:e.target.value})}/></label></fieldset> <fieldset className="special-gift-methods" disabled={busy}><legend>{localize("ui.5d7ced5b37aaf867",{defaultValue:"How would you like to share your gift?"})}</legend><label><input type="radio" name="special-gift-method" checked={method==='LINK_AND_CONTRIBUTION'} onChange={()=>setMethod('LINK_AND_CONTRIBUTION')}/><span>{localize("ui.466b0173b3baaea4",{defaultValue:"Share a gift link and contribute to the host"})}</span></label><label><input type="radio" name="special-gift-method" checked={method==='DIRECT_DELIVERY'} onChange={()=>setMethod('DIRECT_DELIVERY')}/><span>{localize("ui.764968df837ae88e",{defaultValue:"I’ll purchase and deliver the gift myself"})}</span></label></fieldset> {method==='LINK_AND_CONTRIBUTION'?<label>{localize("ui.7fc910558a4cc82b",{defaultValue:"Contribution amount (₹)"})}<input required type="number" inputMode="decimal" min="1" max="1000000" step="0.01" value={form.amount} onChange={e=>setForm({...form,amount:e.target.value})}/><small>{localize("ui.30e62cf816e2cf0b",{defaultValue:"Continue to send this amount directly to the host’s UPI or bank account."})}</small></label>:<p>{localize("ui.b04dd28ab478baae",{defaultValue:"After adding your gift, you can view the host delivery address, purchase from the retailer and share tracking details here."})}</p>} <p className="finePrint">{localize("ui.ada703e1b6b59c56",{defaultValue:"Your contact details and contribution amount are shared only with the host."})}</p><button className="gc-launch" disabled={busy}>{busy?localize("ui.2e3b4a7d1f13e7c3",{defaultValue:"Saving your gift…"}):method==='LINK_AND_CONTRIBUTION'?localize("ui.75bda07643c2766a",{defaultValue:"Continue to host payment"}):localize("ui.0580e20a82366727",{defaultValue:"Add my special gift"})}</button> </form>} {!saved&&previous.length>0&&<section className="gc-panel"><h3>{localize("ui.76d4f865d4b5307f",{defaultValue:"Your gifts in this browser"})}</h3>{previous.map(g=><button key={g.id} type="button" className="gc-secondary" disabled={busy} onClick={()=>{setSaved(g);setAddress(null);setTracking('');setNotice('')}}>{localize("ui.d1f3b7c8d7b5ce7f",{defaultValue:"Manage "})}{g.name}</button>)}</section>} </>} {error&&<p role="alert" className="gc-error">{localizeText(error)}</p>} | (step==='choice'\|\|step==='individual') | [web/components/SpecialGiftEntry.tsx:69](../../../web/components/SpecialGiftEntry.tsx#L69) |
| UI-01109 | dialog | {children} | Parent/source state | [web/components/focused/Invitation.tsx:26](../../../web/components/focused/Invitation.tsx#L26) |
| UI-00072 | "status" | {localize("ui.bed9096c411e6da3",{defaultValue:"Opening your workspace…"})} | Parent/source state | [web/app/components/HostGlobalNav.tsx:17](../../../web/app/components/HostGlobalNav.tsx#L17) |
| UI-00141 | "status" | {localize("ui.c926c2c50e65d5a5",{defaultValue:"Opening…"})} | Parent/source state | [web/app/events/[id]/preview/page.tsx:10](../../../web/app/events/[id]/preview/page.tsx#L10) |
| UI-00144 | "status" | {localize("ui.8fe30ad66c95faea",{defaultValue:"Preparing your celebration…"})} | loading | [web/app/events/[id]/preview/page.tsx:44](../../../web/app/events/[id]/preview/page.tsx#L44) |
| UI-00145 | "alert" | · | error; loading | [web/app/events/[id]/preview/page.tsx:45](../../../web/app/events/[id]/preview/page.tsx#L45) |
| UI-00150 | "alert" | {localize("ui.126db8776b98b7aa",{defaultValue:"Event unavailable"})} | event; error; loading | [web/app/events/[id]/preview/page.tsx:51](../../../web/app/events/[id]/preview/page.tsx#L51) |
| UI-00498 | "alert" | {localizeText(error)} | error; groupBase&&error.includes('switched off') | [web/components/CommercePurchase.tsx:34](../../../web/components/CommercePurchase.tsx#L34) |
| UI-00500 | "status" | {localizeText(notice)} | notice | [web/components/CommercePurchase.tsx:34](../../../web/components/CommercePurchase.tsx#L34) |
| UI-00531 | "status" | {value?(remaining?localizeText(`${remaining} requirements still needed.`):localizeText("All requirements met.")):localizeText("Enter your "+kind+" to check the requirements.")} | Parent/source state | [web/components/CredentialRequirements.tsx:7](../../../web/components/CredentialRequirements.tsx#L7) |
| UI-00548 | "status" | {localize("ui.7a005f0f1dbc704f",{defaultValue:"Opening your celebration space…"})} | ready\|\|discovery | [web/components/ExperienceProvider.tsx:19](../../../web/components/ExperienceProvider.tsx#L19) |
| UI-00552 | "alert" | {localizeText(error)} | error | [web/components/GroupActivity.tsx:13](../../../web/components/GroupActivity.tsx#L13) |
| UI-00561 | "status" | {accountNotice} | logged&&accountNotice; open | [web/components/GroupContribution.tsx:90](../../../web/components/GroupContribution.tsx#L90) |
| UI-00562 | "alert" | {localizeText(error)} | error; open | [web/components/GroupContribution.tsx:90](../../../web/components/GroupContribution.tsx#L90) |
| UI-00588 | "status" | {pinNotice} | pinNotice; allowPin; group; !logged; loading; open | [web/components/GroupContribution.tsx:90](../../../web/components/GroupContribution.tsx#L90) |
| UI-00635 | "status" | {localize("ui.3850dde1a89614a2",{defaultValue:"✓ Email verified for account recovery"})} | Parent/source state | [web/components/GroupEmailVerification.tsx:18](../../../web/components/GroupEmailVerification.tsx#L18) |
| UI-00642 | "status" | {localizeText(notice)} | notice | [web/components/GroupEmailVerification.tsx:19](../../../web/components/GroupEmailVerification.tsx#L19) |
| UI-00649 | "status" | {localizeText(notice)} | notice | [web/components/GroupGiftPlan.tsx:11](../../../web/components/GroupGiftPlan.tsx#L11) |
| UI-00650 | "alert" | {localizeText(error)} | error | [web/components/GroupGuestCheckout.tsx:28](../../../web/components/GroupGuestCheckout.tsx#L28) |
| UI-00651 | "status" |  | stage==='thanks'; !allowed; !invite | [web/components/GroupGuestCheckout.tsx:29](../../../web/components/GroupGuestCheckout.tsx#L29) |
| UI-00672 | "status" | {localizeText(notice)} | notice | [web/components/GroupGuestCreatorSettings.tsx:18](../../../web/components/GroupGuestCreatorSettings.tsx#L18) |
| UI-00689 | "status" | {localizeText(notice)} | notice | [web/components/GuestActions.tsx:27](../../../web/components/GuestActions.tsx#L27) |
| UI-00697 | "status" | {localizeText(rsvpResult)} | features.RSVP | [web/components/GuestCelebration.tsx:33](../../../web/components/GuestCelebration.tsx#L33) |
| UI-00704 | "status" | {localizeText(wishResult)} | features.GUESTBOOK | [web/components/GuestCelebration.tsx:36](../../../web/components/GuestCelebration.tsx#L36) |
| UI-00707 | "alert" | {pricingError} | pricingError; paymentMethod==='razorpay'; paymentMethod==='razorpay'&&!razorpayAllowed | [web/components/GuestContributionCheckout.tsx:117](../../../web/components/GuestContributionCheckout.tsx#L117) |
| UI-00709 | "status" | {localize("ui.587f32a685d7c85e",{defaultValue:"Loading payment methods…"})} | destinationsLoading | [web/components/GuestContributionCheckout.tsx:140](../../../web/components/GuestContributionCheckout.tsx#L140) |
| UI-00710 | "alert" | {destinationsError} | destinationsError | [web/components/GuestContributionCheckout.tsx:140](../../../web/components/GuestContributionCheckout.tsx#L140) |
| UI-00713 | "status" |  | directStatus | [web/components/GuestContributionCheckout.tsx:143](../../../web/components/GuestContributionCheckout.tsx#L143) |
| UI-00715 | "status" |  | receipt; directStatus | [web/components/GuestContributionCheckout.tsx:150](../../../web/components/GuestContributionCheckout.tsx#L150) |
| UI-00718 | "status" | {localize("ui.8479e3a7bd313b71",{defaultValue:"Loading UPI and bank details…"})} | destinationsLoading; receipt; directStatus | [web/components/GuestContributionCheckout.tsx:152](../../../web/components/GuestContributionCheckout.tsx#L152) |
| UI-00719 | "alert" | {destinationsError} | destinationsError; receipt; directStatus | [web/components/GuestContributionCheckout.tsx:153](../../../web/components/GuestContributionCheckout.tsx#L153) |
| UI-00730 | "status" | {directStatus} | directStatus; paymentMethod!=='razorpay'; receipt | [web/components/GuestContributionCheckout.tsx:173](../../../web/components/GuestContributionCheckout.tsx#L173) |
| UI-00732 | "alert" | {localizeText(error)} | error; receipt; directStatus | [web/components/GuestContributionCheckout.tsx:180](../../../web/components/GuestContributionCheckout.tsx#L180) |
| UI-00736 | "status" | {localize("ui.1d1aaa033b0ac4be",{defaultValue:"Loading guest gifts…"})} | loading | [web/components/GuestGiftBoard.tsx:14](../../../web/components/GuestGiftBoard.tsx#L14) |
| UI-00737 | "alert" | {localizeText(error)} | error | [web/components/GuestGiftBoard.tsx:14](../../../web/components/GuestGiftBoard.tsx#L14) |
| UI-00740 | "status" | {g.display_status\|\|g.selection_label} | Parent/source state | [web/components/GuestGiftBoard.tsx:14](../../../web/components/GuestGiftBoard.tsx#L14) |
| UI-00742 | "status" | {g.display_status} | Parent/source state | [web/components/GuestGiftBoard.tsx:14](../../../web/components/GuestGiftBoard.tsx#L14) |
| UI-00750 | "alert" | {localizeText(error)} | error | [web/components/GuestGiftManager.tsx:19](../../../web/components/GuestGiftManager.tsx#L19) |
| UI-00751 | "status" | {localizeText(notice)} | notice | [web/components/GuestGiftManager.tsx:19](../../../web/components/GuestGiftManager.tsx#L19) |
| UI-00752 | "status" | {localize("ui.20fc5947f275b14c",{defaultValue:"Loading gifts…"})} | loading | [web/components/GuestGiftManager.tsx:19](../../../web/components/GuestGiftManager.tsx#L19) |
| UI-00757 | "status" | {localize("ui.bd4dee85230b343f",{defaultValue:"Checking eligible payment destinations…"})} | loading | [web/components/GuestVerifiedDestination.tsx:39](../../../web/components/GuestVerifiedDestination.tsx#L39) |
| UI-00758 | "alert" | {localizeText(error)} | error; loading | [web/components/GuestVerifiedDestination.tsx:40](../../../web/components/GuestVerifiedDestination.tsx#L40) |
| UI-00760 | "status" | {method==='upi'? localize("ui.2971c4992c8102c1",{defaultValue:"No eligible preferred UPI QR is currently available. Please do not make a transfer yet."}): localize("ui.e2679111c99736c6",{defaultValue:"No eligible preferred bank account is currently available. Please do not make a transfer yet."})} | eligible.length===0; error; loading | [web/components/GuestVerifiedDestination.tsx:40](../../../web/components/GuestVerifiedDestination.tsx#L40) |
| UI-00765 | "status" | {copyNotice} | copyNotice; selected?.method==='UPI'&&selected.qrPngBase64&&focused; eligible.length===0; error; loading | [web/components/GuestVerifiedDestination.tsx:48](../../../web/components/GuestVerifiedDestination.tsx#L48) |
| UI-00790 | "alert" | {localizeText(error)} | error | [web/components/HostWorkspace.tsx:53](../../../web/components/HostWorkspace.tsx#L53) |
| UI-00799 | "status" | {localizeText(notice)} | notice | [web/components/IndependentGroupPlan.tsx:21](../../../web/components/IndependentGroupPlan.tsx#L21) |
| UI-00802 | "status" | {localizeText(notice)} | notice | [web/components/IndependentGroupPlan.tsx:26](../../../web/components/IndependentGroupPlan.tsx#L26) |
| UI-00803 | "status" | {localize("ui.0a0f81c5c16fb959",{defaultValue:"Loading your contribution…"})} | loading | [web/components/IndividualHostContribution.tsx:25](../../../web/components/IndividualHostContribution.tsx#L25) |
| UI-00804 | "status" |  | payment?.claim_id; loading | [web/components/IndividualHostContribution.tsx:25](../../../web/components/IndividualHostContribution.tsx#L25) |
| UI-00811 | "alert" | {localizeText(error)} | error | [web/components/IndividualHostContribution.tsx:29](../../../web/components/IndividualHostContribution.tsx#L29) |
| UI-00909 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:19](../../../web/components/LocalizationProvider.tsx#L19) |
| UI-00911 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:20](../../../web/components/LocalizationProvider.tsx#L20) |
| UI-00913 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:21](../../../web/components/LocalizationProvider.tsx#L21) |
| UI-00928 | "status" | {localize("ui.522a82b93e381483",{defaultValue:"Mobile number verified"})} | verified | [web/components/MobileVerification.tsx:17](../../../web/components/MobileVerification.tsx#L17) |
| UI-00935 | "status" | {localizeText(message)} | message | [web/components/MobileVerification.tsx:17](../../../web/components/MobileVerification.tsx#L17) |
| UI-01011 | "status" | {localizeText(notice)} | notice | [web/components/SocialSharing.tsx:18](../../../web/components/SocialSharing.tsx#L18) |
| UI-01019 | "status" | {notice\|\|localize("ui.18be7e148c04c0fa",{defaultValue:"Manage {{value0}}",value0:(saved.name)})} | saved; step==='choice'; (step==='choice'\|\|step==='individual') | [web/components/SpecialGiftEntry.tsx:73](../../../web/components/SpecialGiftEntry.tsx#L73) |
| UI-01043 | "alert" | {localizeText(error)} | error; (step==='choice'\|\|step==='individual') | [web/components/SpecialGiftEntry.tsx:82](../../../web/components/SpecialGiftEntry.tsx#L82) |
| UI-01056 | "status" | {p.receipt?<><p>{localize("ui.dddc9242b436b8e8",{defaultValue:"Gift allocation: "})}{inr(p.receipt.giftPaise)}</p><p>{localize("ui.ae576bd1ca8463d9",{defaultValue:"Celebration allocation: "})}{inr(p.receipt.walletPaise)}</p><p className="finePrint">{localize("ui.1e029b2aa4518972",{defaultValue:"Payment ID: "})}{p.receipt.paymentId}</p></>:<><p>{inr(amount)} · {p.method==='upi'?localize("ui.7b06e0160857f939",{defaultValue:"UPI"}):localize("ui.d69d587c81659616",{defaultValue:"Bank transfer"})}</p><p className="finePrint">{localize("ui.63ee050b1747e8c3",{defaultValue:"Submission reference: "})}{p.reported.split('Reference: ')[1]\|\|localize("ui.a8536392c5bbbb8f",{defaultValue:"See the host for an update"})}</p></>} {p.close&&<button className="focusPrimary" onClick={p.close}>{localize("ui.5aca8cea2b1e0071",{defaultValue:"Back to the celebration"})}</button>} | p.receipt\|\|p.reported | [web/components/focused/Checkout.tsx:13](../../../web/components/focused/Checkout.tsx#L13) |
| UI-01072 | "alert" | {p.error} | p.error; p.receipt\|\|p.reported | [web/components/focused/Checkout.tsx:13](../../../web/components/focused/Checkout.tsx#L13) |
| UI-01112 | "status" | {localize("ui.e8981426afefa46f",{defaultValue:"Progress is temporarily unavailable. "})} | progressState==='error'; event.features?.GIFTING_ENABLED!==false | [web/components/focused/Invitation.tsx:36](../../../web/components/focused/Invitation.tsx#L36) |


### Workflow state bindings

| Source | Binding | Initial expression |
| --- | --- | --- |
| web/app/events/[id]/preview/page.tsx | loading | true |
| web/app/events/[id]/preview/page.tsx | error | '' |
| web/components/LocalizationProvider.tsx | busy | false |
| web/components/LocalizationProvider.tsx | error | '' |
| web/components/focused/Preview.tsx | error | '' |
| web/components/focused/Invitation.tsx | checkoutBusy | false |
| web/components/focused/Invitation.tsx | providerOpen | false |
| web/components/focused/Invitation.tsx | error | '' |
| web/components/GuestGiftBoard.tsx | loading | true |
| web/components/GuestGiftBoard.tsx | error | '' |
| web/components/SpecialGiftEntry.tsx | step | 'closed' |
| web/components/SpecialGiftEntry.tsx | notice | '' |
| web/components/SpecialGiftEntry.tsx | error | '' |
| web/components/SpecialGiftEntry.tsx | busy | false |
| web/components/GroupContribution.tsx | open | false |
| web/components/GroupContribution.tsx | tab | 'register' |
| web/components/GroupContribution.tsx | pinNotice | '' |
| web/components/GroupContribution.tsx | error | '' |
| web/components/GroupContribution.tsx | busy | false |
| web/components/GroupContribution.tsx | loading | false |
| web/components/GroupContribution.tsx | section | 'overview' |
| web/components/GroupContribution.tsx | accountNotice | '' |
| web/components/MobileVerification.tsx | busy | false |
| web/components/MobileVerification.tsx | message | '' |
| web/components/GroupGuestCheckout.tsx | busy | false |
| web/components/GroupGuestCheckout.tsx | error | '' |
| web/components/GroupEmailVerification.tsx | notice | '' |
| web/components/GroupEmailVerification.tsx | busy | false |
| web/components/GroupCreatorPaymentSettings.tsx | error | '' |
| web/components/GroupCreatorPaymentSettings.tsx | message | '' |
| web/components/GroupCreatorPaymentSettings.tsx | busy | false |
| web/components/GroupGuestCreatorSettings.tsx | notice | '' |
| web/components/GroupGiftPlan.tsx | notice | '' |
| web/components/IndependentGroupPlan.tsx | notice | '' |
| web/components/IndependentGroupPlan.tsx | notice | '' |
| web/components/GuestGiftManager.tsx | busy | false |
| web/components/GuestGiftManager.tsx | loading | true |
| web/components/GuestGiftManager.tsx | error | '' |
| web/components/GuestGiftManager.tsx | notice | '' |
| web/components/GroupActivity.tsx | error | '' |
| web/components/CommercePurchase.tsx | error | '' |
| web/components/CommercePurchase.tsx | busy | false |
| web/components/CommercePurchase.tsx | notice | '' |
| web/components/IndividualHostContribution.tsx | loading | true |
| web/components/IndividualHostContribution.tsx | error | '' |
| web/components/IndividualHostContribution.tsx | busy | false |
| web/components/GuestVerifiedDestination.tsx | copyNotice | '' |
| web/components/GuestVerifiedDestination.tsx | loading | true |
| web/components/GuestVerifiedDestination.tsx | error | '' |
| web/components/GuestVerifiedDestination.tsx | qrOpen | false |
| web/components/GuestContributionCheckout.tsx | destinationsError | '' |
| web/components/GuestContributionCheckout.tsx | destinationsLoading | true |
| web/components/GuestContributionCheckout.tsx | busy | false |
| web/components/GuestContributionCheckout.tsx | error | '' |
| web/components/GuestContributionCheckout.tsx | pricingError | '' |
| web/components/focused/Checkout.tsx | step | 0 |
| web/components/GuestActions.tsx | notice | '' |
| web/components/GuestCelebration.tsx | message | '' |
| web/components/GuestCelebration.tsx | busy | false |
| web/components/SocialSharing.tsx | notice | '' |
| web/components/HostWorkspace.tsx | busy | false |
| web/components/HostWorkspace.tsx | error | '' |


## Invitation design and QR

| Screen information | Source-supported specification |
| --- | --- |
| Route | /events/[id]/qr |
| Source | [web/app/events/[id]/qr/page.tsx:1](../../../web/app/events/[id]/qr/page.tsx#L1) |
| Purpose | Load real event QR, edit invitation, manage artwork and exports |
| User access level | Authenticated event owner; asset/commercial/AI gates where applicable |
| Navigation source | Event sidebar / checklist / studio |
| Navigation destination | Preview/download/share; optional AI form and template purchase |
| Preconditions | Route parameters/query scope must be valid; applicable session/ownership/module/provider state above. Load errors do not imply an empty successful dataset. |
| Postconditions | Only the selected successful action changes its documented state. Navigation alone does not publish, confirm payment, verify contact or deliver a notification. |
| Route variables | id |
| UI variants | Focused/general experience, responsive layout and module/state predicates can change visible sections. Exact predicates are in each control record. |


### Wireframe layout

| Section | Placement / behavior / evidence |
| --- | --- |
| Header | Shared identity/event or page title/back/actions, where declared. Composite header declarations: 5. |
| Navigation / sidebar | Host/admin navigation and event context where applicable; mobile/focused variants are conditional. nav=6, aside=5. |
| Main content | Purpose-specific forms/cards/tables/canvas/player from the composed sources. main=4, section=11, article=0. |
| Search and filters | Fields/selects listed below; exact value/handler/visibility controls are not inferred from placeholder labels. |
| Data grids and cards | Table declarations=0. Data collections and generated rows/cards are in frontend.json dynamicSources. |
| Popups / dialogs | Named/source-declared dialog control records below; opening/closing/loading/apply/cancel actions belong to those component handlers. |
| Alerts / notifications | Source controls with role alert/status are listed below. Notice strings may come from API results; do not assume a universal envelope. |
| Footer | Local/shared footer declarations=2. No undocumented legal/footer wording is invented. |
| Loading / empty / success / error | Observed state bindings and relevant action fields are listed below. Expected QA states from Document 2 do not establish a spinner/retry control where source does not implement one. |


### Composed implementation

- [web/app/events/[id]/qr/page.tsx:1](../../../web/app/events/[id]/qr/page.tsx#L1)
- [web/components/SharePreviewControl.tsx:1](../../../web/components/SharePreviewControl.tsx#L1)
- [web/components/LocalizationProvider.tsx:1](../../../web/components/LocalizationProvider.tsx#L1)
- [web/components/ExperienceProvider.tsx:1](../../../web/components/ExperienceProvider.tsx#L1)
- [web/components/InvitationEditor.tsx:1](../../../web/components/InvitationEditor.tsx#L1)
- [web/components/HostInvitationUploads.tsx:1](../../../web/components/HostInvitationUploads.tsx#L1)
- [web/components/CommercePurchase.tsx:1](../../../web/components/CommercePurchase.tsx#L1)
- [web/components/InvitationTemplateGallery.tsx:1](../../../web/components/InvitationTemplateGallery.tsx#L1)
- [web/components/AiInvitationDesigner.tsx:1](../../../web/components/AiInvitationDesigner.tsx#L1)
- [web/app/layout.tsx:1](../../../web/app/layout.tsx#L1)
- [web/components/GrowthProvider.tsx:1](../../../web/components/GrowthProvider.tsx#L1)
- [web/components/BrandingProvider.tsx:1](../../../web/components/BrandingProvider.tsx#L1)
- [web/components/LegalFooter.tsx:1](../../../web/components/LegalFooter.tsx#L1)
- [web/components/CookieNotice.tsx:1](../../../web/components/CookieNotice.tsx#L1)
- [web/app/components/ApplicationInteractions.tsx:1](../../../web/app/components/ApplicationInteractions.tsx#L1)
- [web/components/VerificationProvider.tsx:1](../../../web/components/VerificationProvider.tsx#L1)
- [web/app/components/PerformanceDiagnostics.tsx:1](../../../web/app/components/PerformanceDiagnostics.tsx#L1)
- [web/app/components/HostSessionRedirect.tsx:1](../../../web/app/components/HostSessionRedirect.tsx#L1)
- [web/app/components/HostGlobalNav.tsx:1](../../../web/app/components/HostGlobalNav.tsx#L1)
- [web/components/HostWorkspace.tsx:1](../../../web/components/HostWorkspace.tsx#L1)
- [web/components/focused/HostShell.tsx:1](../../../web/components/focused/HostShell.tsx#L1)

### Fields

| Control ID | Label / expression | Type | Binding | Required | Native limits / format | Detailed specification |
| --- | --- | --- | --- | --- | --- | --- |
| UI-00166 | {localize("ui.96664140215e0a17",{defaultValue:"QR theme"})} | select  | {details.theme} | Not declared |  | [control](UI_CONTROLS.md#ui-00166) |
| UI-00440 | {localize("ui.4a8b403035a2b673",{defaultValue:"Celebration"})} | select  | {selected} | true |  | [control](UI_CONTROLS.md#ui-00440) |
| UI-00441 | {localize("ui.e00cf1143c6cdbec",{defaultValue:"Visual style"})} | select  | {form.visualStyle} | Not declared |  | [control](UI_CONTROLS.md#ui-00441) |
| UI-00442 | {localize("ui.c2c8b72e302ec730",{defaultValue:"Tone"})} | select  | {form.tone} | Not declared |  | [control](UI_CONTROLS.md#ui-00442) |
| UI-00443 | {localize("ui.a4fe65264ef7dbb3",{defaultValue:"Language"})} | input  | {form.language} | true | maxLength={40} | [control](UI_CONTROLS.md#ui-00443) |
| UI-00444 | {localize("ui.684d81462214c9b2",{defaultValue:"Names shown on the invitation "})} | input  | {form.displayNames} | Not declared | maxLength={200} | [control](UI_CONTROLS.md#ui-00444) |
| UI-00445 | {localize("ui.1173054af55a299b",{defaultValue:"Public venue "})} | input  | {form.publicVenue} | Not declared | maxLength={160} | [control](UI_CONTROLS.md#ui-00445) |
| UI-00446 | {localize("ui.981b0a32ff53be60",{defaultValue:"Time "})} | input  | {form.startTime} | Not declared | maxLength={40} | [control](UI_CONTROLS.md#ui-00446) |
| UI-00447 | {localize("ui.2d94a21f97ff5bfa",{defaultValue:"A personal touch "})} | textarea  | {form.specialMessage} | Not declared | maxLength={400} | [control](UI_CONTROLS.md#ui-00447) |
| UI-00776 | {localizeText(busy?'Preparing your artwork…':category==='DECORATION'?'Upload a sticker':'Upload a background')} | input "file" |  | Not declared |  | [control](UI_CONTROLS.md#ui-00776) |
| UI-00779 | {localizeText('Enable custom uploads for this host')} | input "checkbox" | {access.enabled} | Not declared |  | [control](UI_CONTROLS.md#ui-00779) |
| UI-00780 | {localizeText('Require a paid premium host plan')} | input "checkbox" | {access.requiresPayment} | Not declared |  | [control](UI_CONTROLS.md#ui-00780) |
| UI-00824 | No static label; inspect component context | input "file" |  | Not declared |  | [control](UI_CONTROLS.md#ui-00824) |
| UI-00827 | {localizeText('Choose your celebration for an instant invitation')} | select  | {eventId} | Not declared |  | [control](UI_CONTROLS.md#ui-00827) |
| UI-00831 | {localize("ui.f68e0b1ef1269bae",{defaultValue:"Occasion"})} | select  | {occasion} | Not declared |  | [control](UI_CONTROLS.md#ui-00831) |
| UI-00832 | {localize("ui.25bc9efedfb71cbf",{defaultValue:"Event name"})} | input  | {eventTitle} | Not declared | maxLength={200} | [control](UI_CONTROLS.md#ui-00832) |
| UI-00843 | {localize("ui.7b790708ff1b1713",{defaultValue:"Collection"})} | select  | {galleryCategory} | Not declared |  | [control](UI_CONTROLS.md#ui-00843) |
| UI-00845 | {localize("ui.e3ce6241ad144b88",{defaultValue:"Zoom "})} | select  | {scale} | Not declared |  | [control](UI_CONTROLS.md#ui-00845) |
| UI-00846 | {localize("ui.2f77668a9dfbf8d5",{defaultValue:"Message"})} | textarea  | {active.text} | Not declared |  | [control](UI_CONTROLS.md#ui-00846) |
| UI-00847 | {localize("ui.4a69d01660468781",{defaultValue:"Text color"})} | input "color" | {active.color} | Not declared |  | [control](UI_CONTROLS.md#ui-00847) |
| UI-00848 | {localize("ui.bf9a32d3cac90bed",{defaultValue:"Font style"})} | select  | {active.fontFamily\|\|"Georgia, serif"} | Not declared |  | [control](UI_CONTROLS.md#ui-00848) |
| UI-00849 | {localize("ui.6d784d95c63833e6",{defaultValue:"Font size"})} | input "range" | {active.fontSize} | Not declared | min="12"; max="70" | [control](UI_CONTROLS.md#ui-00849) |
| UI-00850 | {localize("ui.9d597870c4b3f76d",{defaultValue:"Alignment"})} | select  | {active.align} | Not declared |  | [control](UI_CONTROLS.md#ui-00850) |
| UI-00851 | {localizeText(({x:'Left',y:'Top',w:'Width',h:'Height'} as Record<string,string>)[k])} | input "number" | {active[k as 'x'\|'y'\|'w'\|'h']} | Not declared |  | [control](UI_CONTROLS.md#ui-00851) |
| UI-00854 | {localize("ui.e7d1ff81125efbcb",{defaultValue:"Background zoom: "})} {Math.round(normalizeBackgroundTransform(design.backgroundTransform).zoom*100)} % | input "range" | {Math.round(normalizeBackgroundTransform(design.backgroundTransform).zoom*100)} | Not declared | min="100"; max="300"; step="5" | [control](UI_CONTROLS.md#ui-00854) |
| UI-00856 | {localizeText('Soften the background')} | input "range" | {Math.round((design.backgroundOverlay??.53)*100)} | Not declared | min="0"; max="100" | [control](UI_CONTROLS.md#ui-00856) |
| UI-00857 | {localize("ui.3e314dafdf7c6c51",{defaultValue:"Background color"})} | input "color" | {design.background} | Not declared |  | [control](UI_CONTROLS.md#ui-00857) |
| UI-00859 | {localize("ui.4a8b403035a2b673",{defaultValue:"Celebration"})} | select  | {eventId} | Not declared |  | [control](UI_CONTROLS.md#ui-00859) |
| UI-00860 | No static label; inspect component context | input  | {eventId} | Not declared |  | [control](UI_CONTROLS.md#ui-00860) |
| UI-00862 | {localizeText('Explore the collection')} | select  | {filter} | Not declared |  | [control](UI_CONTROLS.md#ui-00862) |
| UI-00908 | {localize("ui.bba5b40bdd28c66e",{defaultValue:"Choose language"})} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00908) |
| UI-00910 | {label} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00910) |
| UI-00912 | {localize("ui.140071feea59fe87",{defaultValue:"Preferred language"})} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00912) |
| UI-00996 | {t('Allow a rich preview for this invitation')} | input "checkbox" | {value.enabled} | Not declared |  | [control](UI_CONTROLS.md#ui-00996) |


### Buttons

| ID | Label / expression | Action | Submit validation / handler | Source |
| --- | --- | --- | --- | --- |
| UI-00164 | {localize("ui.641dce44f65bbd68",{defaultValue:"Copy invitation link"})} | {copyLink} | Not a native form submit or inherited from component context | [web/app/events/[id]/qr/page.tsx:179](../../../web/app/events/[id]/qr/page.tsx#L179) |
| UI-00165 | {localize("ui.82c96565542669d6",{defaultValue:"Download QR PNG"})} | {() => void downloadQr()} | Not a native form submit or inherited from component context | [web/app/events/[id]/qr/page.tsx:182](../../../web/app/events/[id]/qr/page.tsx#L182) |
| UI-00437 |  | {onClose} | Not a native form submit or inherited from component context | [web/components/AiInvitationDesigner.tsx:31](../../../web/components/AiInvitationDesigner.tsx#L31) |
| UI-00452 | {localize("ui.ef8f7cae77294c93",{defaultValue:"Retry QR"})} | {()=>setQrRetry(v=>v+1)} | Not a native form submit or inherited from component context | [web/components/AiInvitationDesigner.tsx:31](../../../web/components/AiInvitationDesigner.tsx#L31) |
| UI-00453 | {localize("ui.2ec3ba1bb1368153",{defaultValue:"Use this design"})} | {()=>{if(qrUrl){onApply(preview!,suggestion);onClose()}}} | Not a native form submit or inherited from component context | [web/components/AiInvitationDesigner.tsx:31](../../../web/components/AiInvitationDesigner.tsx#L31) |
| UI-00454 | {localize("ui.4a0b4c8e7bbe8910",{defaultValue:"Keep my current design"})} | {onClose} | Not a native form submit or inherited from component context | [web/components/AiInvitationDesigner.tsx:31](../../../web/components/AiInvitationDesigner.tsx#L31) |
| UI-00455 | {busy?localize("ui.b56b17c7d3c34b3a",{defaultValue:"Designing your invitation…"}):suggestion?localize("ui.3f3d13862baceafc",{defaultValue:"Generate another design"}):localize("ui.d66ac0e85de2baad",{defaultValue:"Generate invitation"})} | submit / component action | Not a native form submit or inherited from component context | [web/components/AiInvitationDesigner.tsx:31](../../../web/components/AiInvitationDesigner.tsx#L31) |
| UI-00497 | {busy?localize("ui.42074396a441a347",{defaultValue:"Processing…"}):offer.amountPaise===0?localize("ui.850fbbaefb4057d0",{defaultValue:"Activate access"}):localize("ui.0e3afe9550a3bde6",{defaultValue:"{{value0}} · {{value1}} (test mode)",value0:(offer.entitled?'Renew':'Purchase'),value1:(commercialMoney(offer.amountPaise))})} | {()=>void pay()} | Not a native form submit or inherited from component context | [web/components/CommercePurchase.tsx:34](../../../web/components/CommercePurchase.tsx#L34) |
| UI-00499 | {localize("ui.087a9cc69283c135",{defaultValue:"Reload price"})} | {()=>{setRetry(v=>v+1)}} | Not a native form submit or inherited from component context | [web/components/CommercePurchase.tsx:34](../../../web/components/CommercePurchase.tsx#L34) |
| UI-00528 | Accept optional analytics | {()=>{saveConsent(true);setShow(false)}} | Not a native form submit or inherited from component context | [web/components/CookieNotice.tsx:5](../../../web/components/CookieNotice.tsx#L5) |
| UI-00529 | Reject optional analytics | {()=>{saveConsent(false);setShow(false)}} | Not a native form submit or inherited from component context | [web/components/CookieNotice.tsx:5](../../../web/components/CookieNotice.tsx#L5) |
| UI-00777 |  | {()=>onUse(asset)} | Not a native form submit or inherited from component context | [web/components/HostInvitationUploads.tsx:21](../../../web/components/HostInvitationUploads.tsx#L21) |
| UI-00781 | {localizeText(busy?'Saving…':'Save upload access')} | {()=>void save()} | Not a native form submit or inherited from component context | [web/components/HostInvitationUploads.tsx:26](../../../web/components/HostInvitationUploads.tsx#L26) |
| UI-00788 | {collapsed?<PanelLeftOpen size={20}/>:<PanelLeftClose size={20}/>} | {toggle} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:50](../../../web/components/HostWorkspace.tsx#L50) |
| UI-00791 |  | {()=>void signOut()} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:53](../../../web/components/HostWorkspace.tsx#L53) |
| UI-00792 |  | {()=>setCollapsed(true)} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:54](../../../web/components/HostWorkspace.tsx#L54) |
| UI-00818 | {localizeText(qrDownloading?'Preparing QR…':'Download QR')} | {()=>void downloadStandaloneQr()} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:182](../../../web/components/InvitationEditor.tsx#L182) |
| UI-00819 | {localize("ui.920bf770754f42cf",{defaultValue:" Design with AI"})} | {()=>setAiOpen(true)} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:183](../../../web/components/InvitationEditor.tsx#L183) |
| UI-00820 | {localizeText('Download invitation')} | {()=>void downloadArtwork()} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:184](../../../web/components/InvitationEditor.tsx#L184) |
| UI-00821 |  | {undo} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:186](../../../web/components/InvitationEditor.tsx#L186) |
| UI-00822 |  | {redo} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:186](../../../web/components/InvitationEditor.tsx#L186) |
| UI-00823 | {localize("ui.bfe3fd0809ff3230",{defaultValue:" Import draft"})} | {()=>importInput.current?.click()} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:190](../../../web/components/InvitationEditor.tsx#L190) |
| UI-00825 | {localize("ui.791a355f853121f3",{defaultValue:" Export draft"})} | {()=>{exportDraft();if(moreMenu.current)moreMenu.current.open=false}} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:191](../../../web/components/InvitationEditor.tsx#L191) |
| UI-00826 | {localize("ui.d1f59ae5b10e2c87",{defaultValue:" Reset invitation to default"})} | {()=>{if(moreMenu.current)moreMenu.current.open=false;resetDefault()}} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:192](../../../web/components/InvitationEditor.tsx#L192) |
| UI-00829 | {localizeText(label)} | {()=>setToolPanel(value)} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:197](../../../web/components/InvitationEditor.tsx#L197) |
| UI-00830 |  | {()=>setLibraryTab(id)} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:197](../../../web/components/InvitationEditor.tsx#L197) |
| UI-00833 | {localize("ui.3931578de88dbe3f",{defaultValue:" Add text or tagline"})} | {()=>{const next=addText(design);commit(next);setSelected(next.elements.at(-1)!.id)}} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:197](../../../web/components/InvitationEditor.tsx#L197) |
| UI-00834 | {e.type==='qr'?<Gift size={16}/>:<Type size={16}/>} | {()=>setSelected(e.id)} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:197](../../../web/components/InvitationEditor.tsx#L197) |
| UI-00835 | {localizeText(category)} | {()=>setDecorationCategory(category)} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:201](../../../web/components/InvitationEditor.tsx#L201) |
| UI-00836 |  | {()=>insertSticker(sticker.id)} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:202](../../../web/components/InvitationEditor.tsx#L202) |
| UI-00838 |  | {()=>insertSticker(platformDecorationId(a.id),true)} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:226](../../../web/components/InvitationEditor.tsx#L226) |
| UI-00842 |  | {()=>commit({...design,backgroundImage:src,backgroundOverlay:0})} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:269](../../../web/components/InvitationEditor.tsx#L269) |
| UI-00844 |  | {()=>commit({...design,backgroundImage:b.src,backgroundOverlay:0})} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:324](../../../web/components/InvitationEditor.tsx#L324) |
| UI-00852 | {localize("ui.246a4e073a502e68",{defaultValue:" Delete element"})} | {()=>{commit(deleteElement(design,active.id));setSelected('qr')}} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:375](../../../web/components/InvitationEditor.tsx#L375) |
| UI-00853 | {editBackground?localize("ui.3f318e4e1654bf8d",{defaultValue:"Finish positioning"}):localize("ui.e16337fe95686a20",{defaultValue:"Edit background — drag to position"})} | {()=>setEditBackground(v=>!v)} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:376](../../../web/components/InvitationEditor.tsx#L376) |
| UI-00855 | {localize("ui.11385dfe21d5ef61",{defaultValue:"Reset background position"})} | {()=>backgroundChange(resetBackgroundTransform())} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:387](../../../web/components/InvitationEditor.tsx#L387) |
| UI-00858 | {localize("ui.e0134396e5e148fb",{defaultValue:" Remove background photo"})} | {()=>commit({...design,backgroundImage:''})} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:391](../../../web/components/InvitationEditor.tsx#L391) |
| UI-00861 | {localize("ui.c3f50438f104955b",{defaultValue:" Load authenticated QR"})} | {()=>void loadQr()} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:391](../../../web/components/InvitationEditor.tsx#L391) |
| UI-00863 |  | {()=>setPreview(t)} | Not a native form submit or inherited from component context | [web/components/InvitationTemplateGallery.tsx:26](../../../web/components/InvitationTemplateGallery.tsx#L26) |
| UI-00866 |  | {()=>setPreview(null)} | Not a native form submit or inherited from component context | [web/components/InvitationTemplateGallery.tsx:26](../../../web/components/InvitationTemplateGallery.tsx#L26) |
| UI-00867 | {localizeText('Use this invitation')} | {()=>void apply(preview)} | Not a native form submit or inherited from component context | [web/components/InvitationTemplateGallery.tsx:26](../../../web/components/InvitationTemplateGallery.tsx#L26) |
| UI-00876 | Cookie preferences | {()=>window.dispatchEvent(new Event('giftwise-cookie-settings'))} | Not a native form submit or inherited from component context | [web/components/LegalFooter.tsx:4](../../../web/components/LegalFooter.tsx#L4) |


### Dialog and alert/status inventory

| ID | Role / kind | Text / label | Visibility conditions | Source |
| --- | --- | --- | --- | --- |
| UI-00161 | native-dialog | 'Invitation link copied!' | Parent/source state | [web/app/events/[id]/qr/page.tsx:119](../../../web/app/events/[id]/qr/page.tsx#L119) |
| UI-00436 | dialog |  | Parent/source state | [web/components/AiInvitationDesigner.tsx:31](../../../web/components/AiInvitationDesigner.tsx#L31) |
| UI-00814 | native-dialog | 'Reset the invitation design to its default template? This will discard your current visual changes, but will not change event details, gifts, or the event QR.' | Parent/source state | [web/components/InvitationEditor.tsx:118](../../../web/components/InvitationEditor.tsx#L118) |
| UI-00815 | native-dialog | 'Invalid invitation draft' | Parent/source state | [web/components/InvitationEditor.tsx:121](../../../web/components/InvitationEditor.tsx#L121) |
| UI-00816 | native-dialog | 'Enter a valid event UUID' | Parent/source state | [web/components/InvitationEditor.tsx:122](../../../web/components/InvitationEditor.tsx#L122) |
| UI-00817 | native-dialog | `Could not load QR: ${String(e)}. Backend authentication is required.` | Parent/source state | [web/components/InvitationEditor.tsx:122](../../../web/components/InvitationEditor.tsx#L122) |
| UI-00828 | native-dialog | 'Replace the current design with an invitation for this celebration?' | editedDraft.current; !linkedEventId | [web/components/InvitationEditor.tsx:197](../../../web/components/InvitationEditor.tsx#L197) |
| UI-00865 | dialog | {preview&&<><header><h2>{preview.title}</h2><button type="button" autoFocus onClick={()=>setPreview(null)} aria-label={localizeText('Close preview')}><X size={20}/></button></header><Artwork design={designFor(preview)} qrUrl={qrUrl} watermark={preview.tier==='PREMIUM'}/>{preview.tier==='PREMIUM'&&<><p>{localizeText('GiftWise watermarks cover premium previews. Unlock this template to use the clean invitation.')}</p><CommercePurchase stream="PREMIUM_TEMPLATE" resourceId={preview.id} onComplete={()=>{void refresh().then(items=>{const unlocked=items.find(t=>t.id===preview.id);if(unlocked)setPreview(unlocked)}).catch(()=>setError('Could not verify template access'))}}/></>}{(preview.tier==='FREE'\|\|preview.accessible)&&<button type="button" className="primary" onClick={()=>void apply(preview)}>{localizeText('Use this invitation')}</button>}{error&&<p role="alert">{localizeText(error)}</p>}</>} | Parent/source state | [web/components/InvitationTemplateGallery.tsx:26](../../../web/components/InvitationTemplateGallery.tsx#L26) |
| UI-00072 | "status" | {localize("ui.bed9096c411e6da3",{defaultValue:"Opening your workspace…"})} | Parent/source state | [web/app/components/HostGlobalNav.tsx:17](../../../web/app/components/HostGlobalNav.tsx#L17) |
| UI-00163 | "alert" | {localizeText(error)} | error | [web/app/events/[id]/qr/page.tsx:158](../../../web/app/events/[id]/qr/page.tsx#L158) |
| UI-00448 | "status" | {localize("ui.7b5f0c1c569b9d55",{defaultValue:"Checking AI availability…"})} | checking | [web/components/AiInvitationDesigner.tsx:31](../../../web/components/AiInvitationDesigner.tsx#L31) |
| UI-00449 | "status" | {setup} | !checking&&selected&&!available&&setup | [web/components/AiInvitationDesigner.tsx:31](../../../web/components/AiInvitationDesigner.tsx#L31) |
| UI-00450 | "alert" | {localizeText(error)} | error | [web/components/AiInvitationDesigner.tsx:31](../../../web/components/AiInvitationDesigner.tsx#L31) |
| UI-00451 | "alert" | {qrError} | qrError; preview&&suggestion | [web/components/AiInvitationDesigner.tsx:31](../../../web/components/AiInvitationDesigner.tsx#L31) |
| UI-00498 | "alert" | {localizeText(error)} | error; groupBase&&error.includes('switched off') | [web/components/CommercePurchase.tsx:34](../../../web/components/CommercePurchase.tsx#L34) |
| UI-00500 | "status" | {localizeText(notice)} | notice | [web/components/CommercePurchase.tsx:34](../../../web/components/CommercePurchase.tsx#L34) |
| UI-00548 | "status" | {localize("ui.7a005f0f1dbc704f",{defaultValue:"Opening your celebration space…"})} | ready\|\|discovery | [web/components/ExperienceProvider.tsx:19](../../../web/components/ExperienceProvider.tsx#L19) |
| UI-00778 | "alert" | {localizeText(error)} | error | [web/components/HostInvitationUploads.tsx:21](../../../web/components/HostInvitationUploads.tsx#L21) |
| UI-00782 | "status" | {localizeText('Upload access saved and audited.')} | saved | [web/components/HostInvitationUploads.tsx:26](../../../web/components/HostInvitationUploads.tsx#L26) |
| UI-00783 | "alert" | {localizeText(error)} | error | [web/components/HostInvitationUploads.tsx:26](../../../web/components/HostInvitationUploads.tsx#L26) |
| UI-00790 | "alert" | {localizeText(error)} | error | [web/components/HostWorkspace.tsx:53](../../../web/components/HostWorkspace.tsx#L53) |
| UI-00812 | "status" | Loading invitation templates… | Parent/source state | [web/components/InvitationEditor.tsx:14](../../../web/components/InvitationEditor.tsx#L14) |
| UI-00813 | "status" | Loading design assistant… | Parent/source state | [web/components/InvitationEditor.tsx:15](../../../web/components/InvitationEditor.tsx#L15) |
| UI-00840 | "alert" | {platformError} | platformError | [web/components/InvitationEditor.tsx:254](../../../web/components/InvitationEditor.tsx#L254) |
| UI-00864 | "alert" | {localizeText(error)} | error | [web/components/InvitationTemplateGallery.tsx:26](../../../web/components/InvitationTemplateGallery.tsx#L26) |
| UI-00868 | "alert" | {localizeText(error)} | error; preview | [web/components/InvitationTemplateGallery.tsx:26](../../../web/components/InvitationTemplateGallery.tsx#L26) |
| UI-00909 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:19](../../../web/components/LocalizationProvider.tsx#L19) |
| UI-00911 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:20](../../../web/components/LocalizationProvider.tsx#L20) |
| UI-00913 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:21](../../../web/components/LocalizationProvider.tsx#L21) |
| UI-00997 | "status" | {t(notice)} | notice | [web/components/SharePreviewControl.tsx:10](../../../web/components/SharePreviewControl.tsx#L10) |


### Workflow state bindings

| Source | Binding | Initial expression |
| --- | --- | --- |
| web/app/events/[id]/qr/page.tsx | error | '' |
| web/app/events/[id]/qr/page.tsx | busy | false |
| web/components/SharePreviewControl.tsx | busy | false |
| web/components/SharePreviewControl.tsx | notice | '' |
| web/components/LocalizationProvider.tsx | busy | false |
| web/components/LocalizationProvider.tsx | error | '' |
| web/components/InvitationEditor.tsx | aiOpen | false |
| web/components/InvitationEditor.tsx | libraryTab | 'templates' |
| web/components/InvitationEditor.tsx | qrDownloading | false |
| web/components/InvitationEditor.tsx | platformError | '' |
| web/components/HostInvitationUploads.tsx | error | '' |
| web/components/HostInvitationUploads.tsx | busy | false |
| web/components/HostInvitationUploads.tsx | error | '' |
| web/components/HostInvitationUploads.tsx | busy | false |
| web/components/CommercePurchase.tsx | error | '' |
| web/components/CommercePurchase.tsx | busy | false |
| web/components/CommercePurchase.tsx | notice | '' |
| web/components/InvitationTemplateGallery.tsx | error | '' |
| web/components/AiInvitationDesigner.tsx | qrError | '' |
| web/components/AiInvitationDesigner.tsx | busy | false |
| web/components/AiInvitationDesigner.tsx | error | '' |
| web/components/HostWorkspace.tsx | busy | false |
| web/components/HostWorkspace.tsx | error | '' |


## Event receiving accounts

| Screen information | Source-supported specification |
| --- | --- |
| Route | /events/[id]/receiving-accounts |
| Source | [web/app/events/[id]/receiving-accounts/page.tsx:1](../../../web/app/events/[id]/receiving-accounts/page.tsx#L1) |
| Purpose | Re-export of host payment-settings screen with event route context |
| User access level | Authenticated event owner; payment profile feature gate |
| Navigation source | Event checklist / sidebar |
| Navigation destination | Register/select/link/enable/prefer eligible event account; return to checklist |
| Preconditions | Route parameters/query scope must be valid; applicable session/ownership/module/provider state above. Load errors do not imply an empty successful dataset. |
| Postconditions | Only the selected successful action changes its documented state. Navigation alone does not publish, confirm payment, verify contact or deliver a notification. |
| Route variables | id |
| UI variants | Focused/general experience, responsive layout and module/state predicates can change visible sections. Exact predicates are in each control record. |


### Wireframe layout

| Section | Placement / behavior / evidence |
| --- | --- |
| Header | Shared identity/event or page title/back/actions, where declared. Composite header declarations: 3. |
| Navigation / sidebar | Host/admin navigation and event context where applicable; mobile/focused variants are conditional. nav=3, aside=3. |
| Main content | Purpose-specific forms/cards/tables/canvas/player from the composed sources. main=3, section=9, article=2. |
| Search and filters | Fields/selects listed below; exact value/handler/visibility controls are not inferred from placeholder labels. |
| Data grids and cards | Table declarations=0. Data collections and generated rows/cards are in frontend.json dynamicSources. |
| Popups / dialogs | Named/source-declared dialog control records below; opening/closing/loading/apply/cancel actions belong to those component handlers. |
| Alerts / notifications | Source controls with role alert/status are listed below. Notice strings may come from API results; do not assume a universal envelope. |
| Footer | Local/shared footer declarations=1. No undocumented legal/footer wording is invented. |
| Loading / empty / success / error | Observed state bindings and relevant action fields are listed below. Expected QA states from Document 2 do not establish a spinner/retry control where source does not implement one. |


### Composed implementation

- [web/app/events/[id]/receiving-accounts/page.tsx:1](../../../web/app/events/[id]/receiving-accounts/page.tsx#L1)
- [web/app/host/payment-settings/page.tsx:1](../../../web/app/host/payment-settings/page.tsx#L1)
- [web/components/LocalizationProvider.tsx:1](../../../web/components/LocalizationProvider.tsx#L1)
- [web/components/ReceivingAccountHealth.tsx:1](../../../web/components/ReceivingAccountHealth.tsx#L1)
- [web/components/ExperienceProvider.tsx:1](../../../web/components/ExperienceProvider.tsx#L1)
- [web/app/layout.tsx:1](../../../web/app/layout.tsx#L1)
- [web/components/GrowthProvider.tsx:1](../../../web/components/GrowthProvider.tsx#L1)
- [web/components/BrandingProvider.tsx:1](../../../web/components/BrandingProvider.tsx#L1)
- [web/components/LegalFooter.tsx:1](../../../web/components/LegalFooter.tsx#L1)
- [web/components/CookieNotice.tsx:1](../../../web/components/CookieNotice.tsx#L1)
- [web/app/components/ApplicationInteractions.tsx:1](../../../web/app/components/ApplicationInteractions.tsx#L1)
- [web/components/VerificationProvider.tsx:1](../../../web/components/VerificationProvider.tsx#L1)
- [web/app/components/PerformanceDiagnostics.tsx:1](../../../web/app/components/PerformanceDiagnostics.tsx#L1)
- [web/app/components/HostSessionRedirect.tsx:1](../../../web/app/components/HostSessionRedirect.tsx#L1)
- [web/app/components/HostGlobalNav.tsx:1](../../../web/app/components/HostGlobalNav.tsx#L1)
- [web/components/HostWorkspace.tsx:1](../../../web/components/HostWorkspace.tsx#L1)
- [web/components/focused/HostShell.tsx:1](../../../web/components/focused/HostShell.tsx#L1)

### Fields

| Control ID | Label / expression | Type | Binding | Required | Native limits / format | Detailed specification |
| --- | --- | --- | --- | --- | --- | --- |
| UI-00331 | {localize("ui.6bf6133a3bfda168",{defaultValue:"Host receiving account"})} | select  | {existingAccount} | true |  | [control](UI_CONTROLS.md#ui-00331) |
| UI-00334 | {localize("ui.107285bae04647b6",{defaultValue:"Related host or event"})} | select  | {upiEvent} | Not declared |  | [control](UI_CONTROLS.md#ui-00334) |
| UI-00335 | {localize("ui.21ac0fa367d84202",{defaultValue:"Account name / tag (optional)"})} | input  | {upiLabel} | Not declared | maxLength={50} | [control](UI_CONTROLS.md#ui-00335) |
| UI-00336 | {localize("ui.cec4a9c0b1e71189",{defaultValue:"UPI ID"})} | input  | {upi} | true |  | [control](UI_CONTROLS.md#ui-00336) |
| UI-00347 | {localize("ui.d303920dee3450a5",{defaultValue:"Upload UPI QR PNG"})} | input "file" |  | Not declared |  | [control](UI_CONTROLS.md#ui-00347) |
| UI-00353 | {localize("ui.107285bae04647b6",{defaultValue:"Related host or event"})} | select  | {bankEvent} | Not declared |  | [control](UI_CONTROLS.md#ui-00353) |
| UI-00354 | {localize("ui.21ac0fa367d84202",{defaultValue:"Account name / tag (optional)"})} | input  | {bankLabel} | Not declared | maxLength={50} | [control](UI_CONTROLS.md#ui-00354) |
| UI-00355 | {localize("ui.5596842fc1e511ec",{defaultValue:"Account holder name"})} | input  | {holder} | true |  | [control](UI_CONTROLS.md#ui-00355) |
| UI-00356 | {localize("ui.f7573b7f5db0b550",{defaultValue:"Account number"})} | input "password" | {number} | true |  | [control](UI_CONTROLS.md#ui-00356) |
| UI-00357 | {localize("ui.f3d8c96e71944625",{defaultValue:"IFSC"})} | input  | {ifsc} | true | maxLength={11} | [control](UI_CONTROLS.md#ui-00357) |
| UI-00908 | {localize("ui.bba5b40bdd28c66e",{defaultValue:"Choose language"})} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00908) |
| UI-00910 | {label} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00910) |
| UI-00912 | {localize("ui.140071feea59fe87",{defaultValue:"Preferred language"})} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00912) |


### Buttons

| ID | Label / expression | Action | Submit validation / handler | Source |
| --- | --- | --- | --- | --- |
| UI-00325 | × | {()=>setError('')} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:69](../../../web/app/host/payment-settings/page.tsx#L69) |
| UI-00327 | × | {()=>setNotice('')} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:69](../../../web/app/host/payment-settings/page.tsx#L69) |
| UI-00329 | {localize("ui.7ce64141b163cd0d",{defaultValue:" Refresh"})} | {()=>void load()} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:70](../../../web/app/host/payment-settings/page.tsx#L70) |
| UI-00332 | {localize("ui.73d3125789193ff1",{defaultValue:"Link to this event"})} | submit / component action | {e=>{e.preventDefault();const [kind,id]=existingAccount.split(':');if(id)void action(async()=>{await paymentProfiles.linkToEvent(authFetch,eventId,kind,id);setExistingAccount('')},'Account linked to this event. Select Preferred to make it ready for publishing.')}} | [web/app/host/payment-settings/page.tsx:71](../../../web/app/host/payment-settings/page.tsx#L71) |
| UI-00337 | {localize("ui.f7f91a76b26e1579",{defaultValue:" Add UPI ID"})} | submit / component action | {e=>{e.preventDefault();void action(async()=>{await paymentProfiles.addUpi(authFetch,upi,undefined,upiLabel,upiEvent);setUpi('');setUpiLabel('');},'UPI ID saved. Upload or generate its QR, then confirm your details.');}} | [web/app/host/payment-settings/page.tsx:71](../../../web/app/host/payment-settings/page.tsx#L71) |
| UI-00338 | {localize("ui.df5c1bee560a14ad",{defaultValue:"Verify "})} {verification?.environment==='SANDBOX'?localize("ui.bb80a42e6fedc731",{defaultValue:"in sandbox"}):localize("ui.9af211329b2fc82e",{defaultValue:"account"})} | {()=>void verifyAccount(u.id,'upi')} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:71](../../../web/app/host/payment-settings/page.tsx#L71) |
| UI-00339 | {localize("ui.b92e4b4fd541d9cc",{defaultValue:"Verification details"})} | {()=>void verificationHistory(u.id,'upi')} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:71](../../../web/app/host/payment-settings/page.tsx#L71) |
| UI-00341 | {localize("ui.dc53bdac523a8cb5",{defaultValue:"Confirm my details"})} | {()=>void confirmDestination(u.id,'upi')} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:71](../../../web/app/host/payment-settings/page.tsx#L71) |
| UI-00342 | {u.preferred\|\|u.isPreferred?localize("ui.7dd2dc467c1ca5f7",{defaultValue:"✓ Preferred"}):!u.qrReady?localize("ui.95672cecf0b961e9",{defaultValue:"QR required"}):(u.verificationStatus==='VERIFIED'\|\|u.hostConfirmed)?localize("ui.0428680794e8223a",{defaultValue:"Set preferred"}):localize("ui.cf0b7ae423a9b52e",{defaultValue:"Confirm details first"})} | {()=>void action(()=>paymentProfiles.preferUpi(authFetch,u.id,undefined,eventId),'Preferred destination updated.')} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:71](../../../web/app/host/payment-settings/page.tsx#L71) |
| UI-00343 | {localize("ui.5342e09f2729fbc6",{defaultValue:"Enable"})} | {()=>void action(()=>paymentProfiles.enableUpi(authFetch,u.id,undefined,eventId),'UPI ID enabled. Independent verification is still required.')} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:71](../../../web/app/host/payment-settings/page.tsx#L71) |
| UI-00344 | {localize("ui.b7e3e4aa4257b9a1",{defaultValue:"Disable"})} | {()=>void action(()=>paymentProfiles.disableUpi(authFetch,u.id,undefined,eventId),'UPI ID disabled.')} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:71](../../../web/app/host/payment-settings/page.tsx#L71) |
| UI-00345 | {localize("ui.27291c678160fb61",{defaultValue:" Generate QR"})} | {()=>void viewQr(u.id,'generate')} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:71](../../../web/app/host/payment-settings/page.tsx#L71) |
| UI-00346 | {localize("ui.5400d8e48024d5b2",{defaultValue:"View uploaded QR"})} | {()=>void viewQr(u.id,'uploaded')} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:71](../../../web/app/host/payment-settings/page.tsx#L71) |
| UI-00348 | {localize("ui.3064d79a295cefe4",{defaultValue:"Rename"})} | {()=>{const value=window.prompt('Account name / tag (maximum 50 characters)',u.accountLabel\|\|'');if(value!==null)void action(()=>paymentProfiles.rename(authFetch,'upi',u.id,value),'UPI account name updated.');}} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:71](../../../web/app/host/payment-settings/page.tsx#L71) |
| UI-00350 | {eventId?localize("ui.7636631d120ebe99",{defaultValue:"Remove from event"}):localize("ui.e2d0a54968ead24e",{defaultValue:"Delete"})} | {()=>{if(window.confirm(eventId?'Remove this account from this event? Shared host accounts will remain saved.':`Archive ${u.accountLabel\|\|u.masked\|\|'this UPI ID'}? It will disappear from payment choices. Historical records will be retained.`))void action(()=>paymentProfiles.archive(authFetch,'upi',u.id,undefined,eventId),eventId?'Account removed from this event.':'UPI account archived.');}} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:71](../../../web/app/host/payment-settings/page.tsx#L71) |
| UI-00358 | {localize("ui.c83b83d985d434f5",{defaultValue:" Add bank account"})} | submit / component action | {e=>{e.preventDefault();void action(async()=>{await paymentProfiles.addBank(authFetch,{accountNumber:number,ifsc,accountHolderName:holder,accountLabel:bankLabel,eventId:bankEvent\|\|null});setBankLabel('');setNumber('');setIfsc('');setHolder('');},'Bank account saved. Confirm the details; independent verification is required before guest use.');}} | [web/app/host/payment-settings/page.tsx:72](../../../web/app/host/payment-settings/page.tsx#L72) |
| UI-00359 | {localize("ui.df5c1bee560a14ad",{defaultValue:"Verify "})} {verification?.environment==='SANDBOX'?localize("ui.bb80a42e6fedc731",{defaultValue:"in sandbox"}):localize("ui.9af211329b2fc82e",{defaultValue:"account"})} | {()=>void verifyAccount(b.id,'bank')} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:72](../../../web/app/host/payment-settings/page.tsx#L72) |
| UI-00360 | {localize("ui.b92e4b4fd541d9cc",{defaultValue:"Verification details"})} | {()=>void verificationHistory(b.id,'bank')} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:72](../../../web/app/host/payment-settings/page.tsx#L72) |
| UI-00361 | {localize("ui.dc53bdac523a8cb5",{defaultValue:"Confirm my details"})} | {()=>void confirmDestination(b.id,'banks')} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:72](../../../web/app/host/payment-settings/page.tsx#L72) |
| UI-00362 | {b.preferred\|\|b.isPreferred?localize("ui.7dd2dc467c1ca5f7",{defaultValue:"✓ Preferred"}):(b.verificationStatus==='VERIFIED'\|\|b.hostConfirmed)?localize("ui.0428680794e8223a",{defaultValue:"Set preferred"}):localize("ui.cf0b7ae423a9b52e",{defaultValue:"Confirm details first"})} | {()=>void action(()=>paymentProfiles.preferBank(authFetch,b.id,undefined,eventId),'Preferred destination updated.')} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:72](../../../web/app/host/payment-settings/page.tsx#L72) |
| UI-00363 | {localize("ui.5342e09f2729fbc6",{defaultValue:"Enable"})} | {()=>void action(()=>paymentProfiles.enableBank(authFetch,b.id,undefined,eventId),'Bank account enabled. Independent verification is still required.')} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:72](../../../web/app/host/payment-settings/page.tsx#L72) |
| UI-00364 | {localize("ui.b7e3e4aa4257b9a1",{defaultValue:"Disable"})} | {()=>void action(()=>paymentProfiles.disableBank(authFetch,b.id,undefined,eventId),'Bank account disabled.')} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:72](../../../web/app/host/payment-settings/page.tsx#L72) |
| UI-00365 | {localize("ui.3064d79a295cefe4",{defaultValue:"Rename"})} | {()=>{const value=window.prompt('Account name / tag (maximum 50 characters)',b.accountLabel\|\|'');if(value!==null)void action(()=>paymentProfiles.rename(authFetch,'banks',b.id,value),'Bank account name updated.');}} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:72](../../../web/app/host/payment-settings/page.tsx#L72) |
| UI-00367 | {eventId?localize("ui.7636631d120ebe99",{defaultValue:"Remove from event"}):localize("ui.e2d0a54968ead24e",{defaultValue:"Delete"})} | {()=>{if(window.confirm(eventId?'Remove this account from this event? Shared host accounts will remain saved.':`Archive ${b.accountLabel\|\|maskedBank(b)}? It will disappear from payment choices. Historical records will be retained.`))void action(()=>paymentProfiles.archive(authFetch,'banks',b.id,undefined,eventId),eventId?'Account removed from this event.':'Bank account archived.');}} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:72](../../../web/app/host/payment-settings/page.tsx#L72) |
| UI-00370 | {localize("ui.29b31220e82fa254",{defaultValue:"Close QR"})} | {closeQr} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:73](../../../web/app/host/payment-settings/page.tsx#L73) |
| UI-00528 | Accept optional analytics | {()=>{saveConsent(true);setShow(false)}} | Not a native form submit or inherited from component context | [web/components/CookieNotice.tsx:5](../../../web/components/CookieNotice.tsx#L5) |
| UI-00529 | Reject optional analytics | {()=>{saveConsent(false);setShow(false)}} | Not a native form submit or inherited from component context | [web/components/CookieNotice.tsx:5](../../../web/components/CookieNotice.tsx#L5) |
| UI-00788 | {collapsed?<PanelLeftOpen size={20}/>:<PanelLeftClose size={20}/>} | {toggle} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:50](../../../web/components/HostWorkspace.tsx#L50) |
| UI-00791 |  | {()=>void signOut()} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:53](../../../web/components/HostWorkspace.tsx#L53) |
| UI-00792 |  | {()=>setCollapsed(true)} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:54](../../../web/components/HostWorkspace.tsx#L54) |
| UI-00876 | Cookie preferences | {()=>window.dispatchEvent(new Event('giftwise-cookie-settings'))} | Not a native form submit or inherited from component context | [web/components/LegalFooter.tsx:4](../../../web/components/LegalFooter.tsx#L4) |
| UI-00985 | {localize("ui.d0521c5f68365b07",{defaultValue:"Refresh check"})} | {()=>setRetry(v=>v+1)} | Not a native form submit or inherited from component context | [web/components/ReceivingAccountHealth.tsx:14](../../../web/components/ReceivingAccountHealth.tsx#L14) |


### Dialog and alert/status inventory

| ID | Role / kind | Text / label | Visibility conditions | Source |
| --- | --- | --- | --- | --- |
| UI-00319 | native-dialog | 'Account holder name exactly as registered with the bank' | Parent/source state | [web/app/host/payment-settings/page.tsx:42](../../../web/app/host/payment-settings/page.tsx#L42) |
| UI-00320 | native-dialog | `Send these account details to Cashfree for ${verification?.environment\|\|'sandbox'} verification?` | Parent/source state | [web/app/host/payment-settings/page.tsx:43](../../../web/app/host/payment-settings/page.tsx#L43) |
| UI-00321 | native-dialog | 'I have checked this destination and confirm it belongs to me. This does not independently verify ownership or enable guest transfers.' | Parent/source state | [web/app/host/payment-settings/page.tsx:60](../../../web/app/host/payment-settings/page.tsx#L60) |
| UI-00349 | native-dialog | 'Account name / tag (maximum 50 characters)' | upis.length===0; loading | [web/app/host/payment-settings/page.tsx:71](../../../web/app/host/payment-settings/page.tsx#L71) |
| UI-00351 | native-dialog | eventId?'Remove this account from this event? Shared host accounts will remain saved.':`Archive ${u.accountLabel\|\|u.masked\|\|'this UPI ID'}? It will disappear from payment choices. Historical records will be retained.` | upis.length===0; loading | [web/app/host/payment-settings/page.tsx:71](../../../web/app/host/payment-settings/page.tsx#L71) |
| UI-00366 | native-dialog | 'Account name / tag (maximum 50 characters)' | banks.length===0; loading | [web/app/host/payment-settings/page.tsx:72](../../../web/app/host/payment-settings/page.tsx#L72) |
| UI-00368 | native-dialog | eventId?'Remove this account from this event? Shared host accounts will remain saved.':`Archive ${b.accountLabel\|\|maskedBank(b)}? It will disappear from payment choices. Historical records will be retained.` | banks.length===0; loading | [web/app/host/payment-settings/page.tsx:72](../../../web/app/host/payment-settings/page.tsx#L72) |
| UI-00369 | "dialog" | {localize("ui.cc2f3579e157b2d0",{defaultValue:"UPI QR preview"})} | qr | [web/app/host/payment-settings/page.tsx:73](../../../web/app/host/payment-settings/page.tsx#L73) |
| UI-00072 | "status" | {localize("ui.bed9096c411e6da3",{defaultValue:"Opening your workspace…"})} | Parent/source state | [web/app/components/HostGlobalNav.tsx:17](../../../web/app/components/HostGlobalNav.tsx#L17) |
| UI-00324 | "alert" | {localizeText(error)} | error | [web/app/host/payment-settings/page.tsx:69](../../../web/app/host/payment-settings/page.tsx#L69) |
| UI-00326 | "status" | {localizeText(notice)} | notice | [web/app/host/payment-settings/page.tsx:69](../../../web/app/host/payment-settings/page.tsx#L69) |
| UI-00328 | "status" | {verificationDetails} | verificationDetails | [web/app/host/payment-settings/page.tsx:70](../../../web/app/host/payment-settings/page.tsx#L70) |
| UI-00340 | "status" | {localize("ui.a179b98670de0176",{defaultValue:"Upload or generate a valid QR before confirming or selecting Preferred."})} | !u.qrReady; upis.length===0; loading | [web/app/host/payment-settings/page.tsx:71](../../../web/app/host/payment-settings/page.tsx#L71) |
| UI-00548 | "status" | {localize("ui.7a005f0f1dbc704f",{defaultValue:"Opening your celebration space…"})} | ready\|\|discovery | [web/components/ExperienceProvider.tsx:19](../../../web/components/ExperienceProvider.tsx#L19) |
| UI-00790 | "alert" | {localizeText(error)} | error | [web/components/HostWorkspace.tsx:53](../../../web/components/HostWorkspace.tsx#L53) |
| UI-00909 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:19](../../../web/components/LocalizationProvider.tsx#L19) |
| UI-00911 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:20](../../../web/components/LocalizationProvider.tsx#L20) |
| UI-00913 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:21](../../../web/components/LocalizationProvider.tsx#L21) |
| UI-00986 | "status" | {localize("ui.7b641b6e0d99ccbb",{defaultValue:"Checking linked accounts…"})} | loading | [web/components/ReceivingAccountHealth.tsx:14](../../../web/components/ReceivingAccountHealth.tsx#L14) |
| UI-00987 | "alert" | {localizeText(error)} | error; loading | [web/components/ReceivingAccountHealth.tsx:14](../../../web/components/ReceivingAccountHealth.tsx#L14) |


### Workflow state bindings

| Source | Binding | Initial expression |
| --- | --- | --- |
| web/app/host/payment-settings/page.tsx | busy | false |
| web/app/host/payment-settings/page.tsx | loading | true |
| web/app/host/payment-settings/page.tsx | error | '' |
| web/app/host/payment-settings/page.tsx | notice | '' |
| web/app/host/payment-settings/page.tsx | uploadedQr | {} |
| web/components/LocalizationProvider.tsx | busy | false |
| web/components/LocalizationProvider.tsx | error | '' |
| web/components/ReceivingAccountHealth.tsx | loading | true |
| web/components/ReceivingAccountHealth.tsx | error | '' |
| web/components/HostWorkspace.tsx | busy | false |
| web/components/HostWorkspace.tsx | error | '' |


## Thank-you card studio

| Screen information | Source-supported specification |
| --- | --- |
| Route | /events/[id]/thank-you |
| Source | [web/app/events/[id]/thank-you/page.tsx:1](../../../web/app/events/[id]/thank-you/page.tsx#L1) |
| Purpose | Edit card/message/channel preferences and preview acknowledgement |
| User access level | Authenticated event owner |
| Navigation source | Event sidebar / notifications |
| Navigation destination | Saved settings/preview/export as implemented; no automatic delivered claim |
| Preconditions | Route parameters/query scope must be valid; applicable session/ownership/module/provider state above. Load errors do not imply an empty successful dataset. |
| Postconditions | Only the selected successful action changes its documented state. Navigation alone does not publish, confirm payment, verify contact or deliver a notification. |
| Route variables | id |
| UI variants | Focused/general experience, responsive layout and module/state predicates can change visible sections. Exact predicates are in each control record. |


### Wireframe layout

| Section | Placement / behavior / evidence |
| --- | --- |
| Header | Shared identity/event or page title/back/actions, where declared. Composite header declarations: 2. |
| Navigation / sidebar | Host/admin navigation and event context where applicable; mobile/focused variants are conditional. nav=3, aside=3. |
| Main content | Purpose-specific forms/cards/tables/canvas/player from the composed sources. main=3, section=4, article=0. |
| Search and filters | Fields/selects listed below; exact value/handler/visibility controls are not inferred from placeholder labels. |
| Data grids and cards | Table declarations=0. Data collections and generated rows/cards are in frontend.json dynamicSources. |
| Popups / dialogs | Named/source-declared dialog control records below; opening/closing/loading/apply/cancel actions belong to those component handlers. |
| Alerts / notifications | Source controls with role alert/status are listed below. Notice strings may come from API results; do not assume a universal envelope. |
| Footer | Local/shared footer declarations=1. No undocumented legal/footer wording is invented. |
| Loading / empty / success / error | Observed state bindings and relevant action fields are listed below. Expected QA states from Document 2 do not establish a spinner/retry control where source does not implement one. |


### Composed implementation

- [web/app/events/[id]/thank-you/page.tsx:1](../../../web/app/events/[id]/thank-you/page.tsx#L1)
- [web/components/LocalizationProvider.tsx:1](../../../web/components/LocalizationProvider.tsx#L1)
- [web/components/KeepsakeSharing.tsx:1](../../../web/components/KeepsakeSharing.tsx#L1)
- [web/components/ExperienceProvider.tsx:1](../../../web/components/ExperienceProvider.tsx#L1)
- [web/app/layout.tsx:1](../../../web/app/layout.tsx#L1)
- [web/components/GrowthProvider.tsx:1](../../../web/components/GrowthProvider.tsx#L1)
- [web/components/BrandingProvider.tsx:1](../../../web/components/BrandingProvider.tsx#L1)
- [web/components/LegalFooter.tsx:1](../../../web/components/LegalFooter.tsx#L1)
- [web/components/CookieNotice.tsx:1](../../../web/components/CookieNotice.tsx#L1)
- [web/app/components/ApplicationInteractions.tsx:1](../../../web/app/components/ApplicationInteractions.tsx#L1)
- [web/components/VerificationProvider.tsx:1](../../../web/components/VerificationProvider.tsx#L1)
- [web/app/components/PerformanceDiagnostics.tsx:1](../../../web/app/components/PerformanceDiagnostics.tsx#L1)
- [web/app/components/HostSessionRedirect.tsx:1](../../../web/app/components/HostSessionRedirect.tsx#L1)
- [web/app/components/HostGlobalNav.tsx:1](../../../web/app/components/HostGlobalNav.tsx#L1)
- [web/components/HostWorkspace.tsx:1](../../../web/components/HostWorkspace.tsx#L1)
- [web/components/focused/HostShell.tsx:1](../../../web/components/focused/HostShell.tsx#L1)

### Fields

| Control ID | Label / expression | Type | Binding | Required | Native limits / format | Detailed specification |
| --- | --- | --- | --- | --- | --- | --- |
| UI-00169 | {localize("ui.c34b360f7db1ee14",{defaultValue:"Card signature / host display name"})} | input  | {settings.hostName} | Not declared | maxLength={200} | [control](UI_CONTROLS.md#ui-00169) |
| UI-00170 | {localize("ui.848f0ca2c125e4dc",{defaultValue:"Email subject"})} | input  | {settings.emailSubject} | Not declared | maxLength={200} | [control](UI_CONTROLS.md#ui-00170) |
| UI-00171 | {localize("ui.eb653a6eee22afc1",{defaultValue:"Email message"})} | textarea  | {settings.emailBody} | Not declared | maxLength={10000} | [control](UI_CONTROLS.md#ui-00171) |
| UI-00172 | {localize("ui.d5f85d6b6ed53a00",{defaultValue:"SMS message"})} | textarea  | {settings.smsBody} | Not declared | maxLength={1000} | [control](UI_CONTROLS.md#ui-00172) |
| UI-00173 | {localize("ui.ba3776ceea05bb99",{defaultValue:"Card theme"})} | select  | {settings.cardTemplate} | Not declared |  | [control](UI_CONTROLS.md#ui-00173) |
| UI-00174 | {localize("ui.f0b369d7e0055fd0",{defaultValue:" Queue acknowledgments automatically"})} | input "checkbox" | {settings.autoSend} | Not declared |  | [control](UI_CONTROLS.md#ui-00174) |
| UI-00175 | {localize("ui.700c8ae481cdde15",{defaultValue:" Queue guest email acknowledgments"})} | input "checkbox" | {settings.emailEnabled} | Not declared |  | [control](UI_CONTROLS.md#ui-00175) |
| UI-00869 | {t('Share approved wishes as a keepsake')} | input "checkbox" | {value.enabled} | Not declared |  | [control](UI_CONTROLS.md#ui-00869) |
| UI-00908 | {localize("ui.bba5b40bdd28c66e",{defaultValue:"Choose language"})} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00908) |
| UI-00910 | {label} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00910) |
| UI-00912 | {localize("ui.140071feea59fe87",{defaultValue:"Preferred language"})} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00912) |


### Buttons

| ID | Label / expression | Action | Submit validation / handler | Source |
| --- | --- | --- | --- | --- |
| UI-00176 | {saving ? localize("ui.23e39291d6135814",{defaultValue:"Saving…"}) : localize("ui.039de9dce407cb01",{defaultValue:"Save design"})} | {() => void save()} | Not a native form submit or inherited from component context | [web/app/events/[id]/thank-you/page.tsx:97](../../../web/app/events/[id]/thank-you/page.tsx#L97) |
| UI-00528 | Accept optional analytics | {()=>{saveConsent(true);setShow(false)}} | Not a native form submit or inherited from component context | [web/components/CookieNotice.tsx:5](../../../web/components/CookieNotice.tsx#L5) |
| UI-00529 | Reject optional analytics | {()=>{saveConsent(false);setShow(false)}} | Not a native form submit or inherited from component context | [web/components/CookieNotice.tsx:5](../../../web/components/CookieNotice.tsx#L5) |
| UI-00788 | {collapsed?<PanelLeftOpen size={20}/>:<PanelLeftClose size={20}/>} | {toggle} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:50](../../../web/components/HostWorkspace.tsx#L50) |
| UI-00791 |  | {()=>void signOut()} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:53](../../../web/components/HostWorkspace.tsx#L53) |
| UI-00792 |  | {()=>setCollapsed(true)} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:54](../../../web/components/HostWorkspace.tsx#L54) |
| UI-00871 | {t('Copy keepsake link')} | {()=>trackedCopy(`${location.origin}/memories/${value.token}`).then(()=>setNotice('Keepsake link copied.')).catch(()=>setNotice('Open the keepsake and copy its address to share it.'))} | Not a native form submit or inherited from component context | [web/components/KeepsakeSharing.tsx:12](../../../web/components/KeepsakeSharing.tsx#L12) |
| UI-00876 | Cookie preferences | {()=>window.dispatchEvent(new Event('giftwise-cookie-settings'))} | Not a native form submit or inherited from component context | [web/components/LegalFooter.tsx:4](../../../web/components/LegalFooter.tsx#L4) |


### Dialog and alert/status inventory

| ID | Role / kind | Text / label | Visibility conditions | Source |
| --- | --- | --- | --- | --- |
| UI-00072 | "status" | {localize("ui.bed9096c411e6da3",{defaultValue:"Opening your workspace…"})} | Parent/source state | [web/app/components/HostGlobalNav.tsx:17](../../../web/app/components/HostGlobalNav.tsx#L17) |
| UI-00168 | "status" | {localizeText(notice)} | notice | [web/app/events/[id]/thank-you/page.tsx:77](../../../web/app/events/[id]/thank-you/page.tsx#L77) |
| UI-00548 | "status" | {localize("ui.7a005f0f1dbc704f",{defaultValue:"Opening your celebration space…"})} | ready\|\|discovery | [web/components/ExperienceProvider.tsx:19](../../../web/components/ExperienceProvider.tsx#L19) |
| UI-00790 | "alert" | {localizeText(error)} | error | [web/components/HostWorkspace.tsx:53](../../../web/components/HostWorkspace.tsx#L53) |
| UI-00872 | "status" | {t(notice)} | notice | [web/components/KeepsakeSharing.tsx:12](../../../web/components/KeepsakeSharing.tsx#L12) |
| UI-00909 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:19](../../../web/components/LocalizationProvider.tsx#L19) |
| UI-00911 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:20](../../../web/components/LocalizationProvider.tsx#L20) |
| UI-00913 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:21](../../../web/components/LocalizationProvider.tsx#L21) |


### Workflow state bindings

| Source | Binding | Initial expression |
| --- | --- | --- |
| web/app/events/[id]/thank-you/page.tsx | notice | '' |
| web/components/LocalizationProvider.tsx | busy | false |
| web/components/LocalizationProvider.tsx | error | '' |
| web/components/KeepsakeSharing.tsx | busy | false |
| web/components/KeepsakeSharing.tsx | notice | '' |
| web/components/HostWorkspace.tsx | busy | false |
| web/components/HostWorkspace.tsx | error | '' |


## Payment and transaction verification

| Screen information | Source-supported specification |
| --- | --- |
| Route | /events/[id]/transactions |
| Source | [web/app/events/[id]/transactions/page.tsx:1](../../../web/app/events/[id]/transactions/page.tsx#L1) |
| Purpose | Inspect provider/manual transaction status and available verification actions |
| User access level | Authenticated event owner; implemented verification controls |
| Navigation source | Event sidebar / contribution report |
| Navigation destination | Updated verified/pending/error detail; no unsupported settlement claim |
| Preconditions | Route parameters/query scope must be valid; applicable session/ownership/module/provider state above. Load errors do not imply an empty successful dataset. |
| Postconditions | Only the selected successful action changes its documented state. Navigation alone does not publish, confirm payment, verify contact or deliver a notification. |
| Route variables | id |
| UI variants | Focused/general experience, responsive layout and module/state predicates can change visible sections. Exact predicates are in each control record. |


### Wireframe layout

| Section | Placement / behavior / evidence |
| --- | --- |
| Header | Shared identity/event or page title/back/actions, where declared. Composite header declarations: 3. |
| Navigation / sidebar | Host/admin navigation and event context where applicable; mobile/focused variants are conditional. nav=3, aside=3. |
| Main content | Purpose-specific forms/cards/tables/canvas/player from the composed sources. main=3, section=3, article=2. |
| Search and filters | Fields/selects listed below; exact value/handler/visibility controls are not inferred from placeholder labels. |
| Data grids and cards | Table declarations=0. Data collections and generated rows/cards are in frontend.json dynamicSources. |
| Popups / dialogs | Named/source-declared dialog control records below; opening/closing/loading/apply/cancel actions belong to those component handlers. |
| Alerts / notifications | Source controls with role alert/status are listed below. Notice strings may come from API results; do not assume a universal envelope. |
| Footer | Local/shared footer declarations=1. No undocumented legal/footer wording is invented. |
| Loading / empty / success / error | Observed state bindings and relevant action fields are listed below. Expected QA states from Document 2 do not establish a spinner/retry control where source does not implement one. |


### Composed implementation

- [web/app/events/[id]/transactions/page.tsx:1](../../../web/app/events/[id]/transactions/page.tsx#L1)
- [web/components/LocalizationProvider.tsx:1](../../../web/components/LocalizationProvider.tsx#L1)
- [web/components/HostPaymentBreakdown.tsx:1](../../../web/components/HostPaymentBreakdown.tsx#L1)
- [web/components/CommercePurchase.tsx:1](../../../web/components/CommercePurchase.tsx#L1)
- [web/app/layout.tsx:1](../../../web/app/layout.tsx#L1)
- [web/components/GrowthProvider.tsx:1](../../../web/components/GrowthProvider.tsx#L1)
- [web/components/BrandingProvider.tsx:1](../../../web/components/BrandingProvider.tsx#L1)
- [web/components/LegalFooter.tsx:1](../../../web/components/LegalFooter.tsx#L1)
- [web/components/CookieNotice.tsx:1](../../../web/components/CookieNotice.tsx#L1)
- [web/app/components/ApplicationInteractions.tsx:1](../../../web/app/components/ApplicationInteractions.tsx#L1)
- [web/components/VerificationProvider.tsx:1](../../../web/components/VerificationProvider.tsx#L1)
- [web/components/ExperienceProvider.tsx:1](../../../web/components/ExperienceProvider.tsx#L1)
- [web/app/components/PerformanceDiagnostics.tsx:1](../../../web/app/components/PerformanceDiagnostics.tsx#L1)
- [web/app/components/HostSessionRedirect.tsx:1](../../../web/app/components/HostSessionRedirect.tsx#L1)
- [web/app/components/HostGlobalNav.tsx:1](../../../web/app/components/HostGlobalNav.tsx#L1)
- [web/components/HostWorkspace.tsx:1](../../../web/components/HostWorkspace.tsx#L1)
- [web/components/focused/HostShell.tsx:1](../../../web/components/focused/HostShell.tsx#L1)

### Fields

| Control ID | Label / expression | Type | Binding | Required | Native limits / format | Detailed specification |
| --- | --- | --- | --- | --- | --- | --- |
| UI-00908 | {localize("ui.bba5b40bdd28c66e",{defaultValue:"Choose language"})} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00908) |
| UI-00910 | {label} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00910) |
| UI-00912 | {localize("ui.140071feea59fe87",{defaultValue:"Preferred language"})} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00912) |


### Buttons

| ID | Label / expression | Action | Submit validation / handler | Source |
| --- | --- | --- | --- | --- |
| UI-00180 | {localize("ui.0e91610117029a62",{defaultValue:"Refresh"})} | {()=>void load()} | Not a native form submit or inherited from component context | [web/app/events/[id]/transactions/page.tsx:22](../../../web/app/events/[id]/transactions/page.tsx#L22) |
| UI-00182 | {localize("ui.34bdfc74e30982b0",{defaultValue:"Recheck provider payment"})} | {()=>void verify(row)} | Not a native form submit or inherited from component context | [web/app/events/[id]/transactions/page.tsx:22](../../../web/app/events/[id]/transactions/page.tsx#L22) |
| UI-00183 | {localize("ui.a57b08a480b822a0",{defaultValue:"Previous"})} | {()=>setOffset(n=>Math.max(0,n-20))} | Not a native form submit or inherited from component context | [web/app/events/[id]/transactions/page.tsx:22](../../../web/app/events/[id]/transactions/page.tsx#L22) |
| UI-00184 | {localize("ui.1ff57a29d7c9d11b",{defaultValue:"Next"})} | {()=>setOffset(n=>n+20)} | Not a native form submit or inherited from component context | [web/app/events/[id]/transactions/page.tsx:22](../../../web/app/events/[id]/transactions/page.tsx#L22) |
| UI-00497 | {busy?localize("ui.42074396a441a347",{defaultValue:"Processing…"}):offer.amountPaise===0?localize("ui.850fbbaefb4057d0",{defaultValue:"Activate access"}):localize("ui.0e3afe9550a3bde6",{defaultValue:"{{value0}} · {{value1}} (test mode)",value0:(offer.entitled?'Renew':'Purchase'),value1:(commercialMoney(offer.amountPaise))})} | {()=>void pay()} | Not a native form submit or inherited from component context | [web/components/CommercePurchase.tsx:34](../../../web/components/CommercePurchase.tsx#L34) |
| UI-00499 | {localize("ui.087a9cc69283c135",{defaultValue:"Reload price"})} | {()=>{setRetry(v=>v+1)}} | Not a native form submit or inherited from component context | [web/components/CommercePurchase.tsx:34](../../../web/components/CommercePurchase.tsx#L34) |
| UI-00528 | Accept optional analytics | {()=>{saveConsent(true);setShow(false)}} | Not a native form submit or inherited from component context | [web/components/CookieNotice.tsx:5](../../../web/components/CookieNotice.tsx#L5) |
| UI-00529 | Reject optional analytics | {()=>{saveConsent(false);setShow(false)}} | Not a native form submit or inherited from component context | [web/components/CookieNotice.tsx:5](../../../web/components/CookieNotice.tsx#L5) |
| UI-00785 | {localize("ui.3a36edc4d973a342",{defaultValue:"Previous breakdowns"})} | {()=>setOffset(v=>Math.max(0,v-20))} | Not a native form submit or inherited from component context | [web/components/HostPaymentBreakdown.tsx:12](../../../web/components/HostPaymentBreakdown.tsx#L12) |
| UI-00786 | {localize("ui.3f7d92a9872b29da",{defaultValue:"Next breakdowns"})} | {()=>setOffset(v=>v+20)} | Not a native form submit or inherited from component context | [web/components/HostPaymentBreakdown.tsx:12](../../../web/components/HostPaymentBreakdown.tsx#L12) |
| UI-00788 | {collapsed?<PanelLeftOpen size={20}/>:<PanelLeftClose size={20}/>} | {toggle} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:50](../../../web/components/HostWorkspace.tsx#L50) |
| UI-00791 |  | {()=>void signOut()} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:53](../../../web/components/HostWorkspace.tsx#L53) |
| UI-00792 |  | {()=>setCollapsed(true)} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:54](../../../web/components/HostWorkspace.tsx#L54) |
| UI-00876 | Cookie preferences | {()=>window.dispatchEvent(new Event('giftwise-cookie-settings'))} | Not a native form submit or inherited from component context | [web/components/LegalFooter.tsx:4](../../../web/components/LegalFooter.tsx#L4) |


### Dialog and alert/status inventory

| ID | Role / kind | Text / label | Visibility conditions | Source |
| --- | --- | --- | --- | --- |
| UI-00072 | "status" | {localize("ui.bed9096c411e6da3",{defaultValue:"Opening your workspace…"})} | Parent/source state | [web/app/components/HostGlobalNav.tsx:17](../../../web/app/components/HostGlobalNav.tsx#L17) |
| UI-00178 | "alert" | {localizeText(error)} | error | [web/app/events/[id]/transactions/page.tsx:22](../../../web/app/events/[id]/transactions/page.tsx#L22) |
| UI-00179 | "status" | {localizeText(message)} | message | [web/app/events/[id]/transactions/page.tsx:22](../../../web/app/events/[id]/transactions/page.tsx#L22) |
| UI-00181 | "status" | {localize("ui.d69971d3db4883a6",{defaultValue:"Loading transactions…"})} | loading | [web/app/events/[id]/transactions/page.tsx:22](../../../web/app/events/[id]/transactions/page.tsx#L22) |
| UI-00498 | "alert" | {localizeText(error)} | error; groupBase&&error.includes('switched off') | [web/components/CommercePurchase.tsx:34](../../../web/components/CommercePurchase.tsx#L34) |
| UI-00500 | "status" | {localizeText(notice)} | notice | [web/components/CommercePurchase.tsx:34](../../../web/components/CommercePurchase.tsx#L34) |
| UI-00548 | "status" | {localize("ui.7a005f0f1dbc704f",{defaultValue:"Opening your celebration space…"})} | ready\|\|discovery | [web/components/ExperienceProvider.tsx:19](../../../web/components/ExperienceProvider.tsx#L19) |
| UI-00784 | "alert" | {localizeText(error)} | error | [web/components/HostPaymentBreakdown.tsx:12](../../../web/components/HostPaymentBreakdown.tsx#L12) |
| UI-00790 | "alert" | {localizeText(error)} | error | [web/components/HostWorkspace.tsx:53](../../../web/components/HostWorkspace.tsx#L53) |
| UI-00909 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:19](../../../web/components/LocalizationProvider.tsx#L19) |
| UI-00911 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:20](../../../web/components/LocalizationProvider.tsx#L20) |
| UI-00913 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:21](../../../web/components/LocalizationProvider.tsx#L21) |


### Workflow state bindings

| Source | Binding | Initial expression |
| --- | --- | --- |
| web/app/events/[id]/transactions/page.tsx | loading | true |
| web/app/events/[id]/transactions/page.tsx | busy | false |
| web/app/events/[id]/transactions/page.tsx | error | '' |
| web/app/events/[id]/transactions/page.tsx | message | '' |
| web/components/LocalizationProvider.tsx | busy | false |
| web/components/LocalizationProvider.tsx | error | '' |
| web/components/HostPaymentBreakdown.tsx | error | '' |
| web/components/CommercePurchase.tsx | error | '' |
| web/components/CommercePurchase.tsx | busy | false |
| web/components/CommercePurchase.tsx | notice | '' |
| web/components/HostWorkspace.tsx | busy | false |
| web/components/HostWorkspace.tsx | error | '' |


## Create celebration

| Screen information | Source-supported specification |
| --- | --- |
| Route | /events/new |
| Source | [web/app/events/new/page.tsx:1](../../../web/app/events/new/page.tsx#L1) |
| Purpose | Collect event, occasion, date, host, venue/contact and delivery details |
| User access level | Authenticated CUSTOMER |
| Navigation source | Create celebration / empty event list |
| Navigation destination | Owned DRAFT event and checklist; failure retains safe form state |
| Preconditions | Route parameters/query scope must be valid; applicable session/ownership/module/provider state above. Load errors do not imply an empty successful dataset. |
| Postconditions | Only the selected successful action changes its documented state. Navigation alone does not publish, confirm payment, verify contact or deliver a notification. |
| Route variables | No path variable |
| UI variants | Focused/general experience, responsive layout and module/state predicates can change visible sections. Exact predicates are in each control record. |


### Wireframe layout

| Section | Placement / behavior / evidence |
| --- | --- |
| Header | Shared identity/event or page title/back/actions, where declared. Composite header declarations: 3. |
| Navigation / sidebar | Host/admin navigation and event context where applicable; mobile/focused variants are conditional. nav=3, aside=3. |
| Main content | Purpose-specific forms/cards/tables/canvas/player from the composed sources. main=4, section=3, article=0. |
| Search and filters | Fields/selects listed below; exact value/handler/visibility controls are not inferred from placeholder labels. |
| Data grids and cards | Table declarations=0. Data collections and generated rows/cards are in frontend.json dynamicSources. |
| Popups / dialogs | Named/source-declared dialog control records below; opening/closing/loading/apply/cancel actions belong to those component handlers. |
| Alerts / notifications | Source controls with role alert/status are listed below. Notice strings may come from API results; do not assume a universal envelope. |
| Footer | Local/shared footer declarations=1. No undocumented legal/footer wording is invented. |
| Loading / empty / success / error | Observed state bindings and relevant action fields are listed below. Expected QA states from Document 2 do not establish a spinner/retry control where source does not implement one. |


### Composed implementation

- [web/app/events/new/page.tsx:1](../../../web/app/events/new/page.tsx#L1)
- [web/components/LocalizationProvider.tsx:1](../../../web/components/LocalizationProvider.tsx#L1)
- [web/components/DeliveryAddressFields.tsx:1](../../../web/components/DeliveryAddressFields.tsx#L1)
- [web/components/GrowthProvider.tsx:1](../../../web/components/GrowthProvider.tsx#L1)
- [web/app/layout.tsx:1](../../../web/app/layout.tsx#L1)
- [web/components/BrandingProvider.tsx:1](../../../web/components/BrandingProvider.tsx#L1)
- [web/components/LegalFooter.tsx:1](../../../web/components/LegalFooter.tsx#L1)
- [web/components/CookieNotice.tsx:1](../../../web/components/CookieNotice.tsx#L1)
- [web/app/components/ApplicationInteractions.tsx:1](../../../web/app/components/ApplicationInteractions.tsx#L1)
- [web/components/VerificationProvider.tsx:1](../../../web/components/VerificationProvider.tsx#L1)
- [web/components/ExperienceProvider.tsx:1](../../../web/components/ExperienceProvider.tsx#L1)
- [web/app/components/PerformanceDiagnostics.tsx:1](../../../web/app/components/PerformanceDiagnostics.tsx#L1)
- [web/app/components/HostSessionRedirect.tsx:1](../../../web/app/components/HostSessionRedirect.tsx#L1)
- [web/app/components/HostGlobalNav.tsx:1](../../../web/app/components/HostGlobalNav.tsx#L1)
- [web/components/HostWorkspace.tsx:1](../../../web/components/HostWorkspace.tsx#L1)
- [web/components/focused/HostShell.tsx:1](../../../web/components/focused/HostShell.tsx#L1)

### Fields

| Control ID | Label / expression | Type | Binding | Required | Native limits / format | Detailed specification |
| --- | --- | --- | --- | --- | --- | --- |
| UI-00186 | {localizeText(label)} | input  | {form[key]} | Not declared |  | [control](UI_CONTROLS.md#ui-00186) |
| UI-00194 | {localize("ui.ec79a50e7b0be627",{defaultValue:"A few words for your guests "})} | textarea  | {form.description} | Not declared | maxLength={10000} | [control](UI_CONTROLS.md#ui-00194) |
| UI-00538 | {localize("ui.455df35dd66a82ca",{defaultValue:"Same as event address"})} | input "checkbox" | {same} | Not declared |  | [control](UI_CONTROLS.md#ui-00538) |
| UI-00539 | {localizeText(label)} | input  | {address[key]} | true | maxLength={maxLength} | [control](UI_CONTROLS.md#ui-00539) |
| UI-00908 | {localize("ui.bba5b40bdd28c66e",{defaultValue:"Choose language"})} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00908) |
| UI-00910 | {label} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00910) |
| UI-00912 | {localize("ui.140071feea59fe87",{defaultValue:"Preferred language"})} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00912) |


### Buttons

| ID | Label / expression | Action | Submit validation / handler | Source |
| --- | --- | --- | --- | --- |
| UI-00189 | {i+1} . {localizeText(label)} | {()=>setStep(i)} | Not a native form submit or inherited from component context | [web/app/events/new/page.tsx:27](../../../web/app/events/new/page.tsx#L27) |
| UI-00193 |  | {()=>setForm(f=>applyOccasionStarter(f,occasion,features.OCCASION_STARTERS) as typeof empty)} | {step===4?e=>{e.preventDefault();void create()}:next} | [web/app/events/new/page.tsx:27](../../../web/app/events/new/page.tsx#L27) |
| UI-00197 |  | {()=>field('theme',value)} | {step===4?e=>{e.preventDefault();void create()}:next} | [web/app/events/new/page.tsx:27](../../../web/app/events/new/page.tsx#L27) |
| UI-00198 | {localize("ui.fd9c61ac500e5118",{defaultValue:"Edit details"})} | {()=>setStep(1)} | {step===4?e=>{e.preventDefault();void create()}:next} | [web/app/events/new/page.tsx:27](../../../web/app/events/new/page.tsx#L27) |
| UI-00199 | {localize("ui.76900f1bfd16c8d4",{defaultValue:"Back"})} | {()=>setStep(s=>s-1)} | {step===4?e=>{e.preventDefault();void create()}:next} | [web/app/events/new/page.tsx:27](../../../web/app/events/new/page.tsx#L27) |
| UI-00200 | {busy?localize("ui.c79ed9492e3c1719",{defaultValue:"Creating…"}):step===4?localize("ui.190093907c6a44a1",{defaultValue:"Create celebration"}):localize("ui.496267ccc6b01605",{defaultValue:"Continue →"})} | submit / component action | {step===4?e=>{e.preventDefault();void create()}:next} | [web/app/events/new/page.tsx:27](../../../web/app/events/new/page.tsx#L27) |
| UI-00528 | Accept optional analytics | {()=>{saveConsent(true);setShow(false)}} | Not a native form submit or inherited from component context | [web/components/CookieNotice.tsx:5](../../../web/components/CookieNotice.tsx#L5) |
| UI-00529 | Reject optional analytics | {()=>{saveConsent(false);setShow(false)}} | Not a native form submit or inherited from component context | [web/components/CookieNotice.tsx:5](../../../web/components/CookieNotice.tsx#L5) |
| UI-00788 | {collapsed?<PanelLeftOpen size={20}/>:<PanelLeftClose size={20}/>} | {toggle} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:50](../../../web/components/HostWorkspace.tsx#L50) |
| UI-00791 |  | {()=>void signOut()} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:53](../../../web/components/HostWorkspace.tsx#L53) |
| UI-00792 |  | {()=>setCollapsed(true)} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:54](../../../web/components/HostWorkspace.tsx#L54) |
| UI-00876 | Cookie preferences | {()=>window.dispatchEvent(new Event('giftwise-cookie-settings'))} | Not a native form submit or inherited from component context | [web/components/LegalFooter.tsx:4](../../../web/components/LegalFooter.tsx#L4) |


### Dialog and alert/status inventory

| ID | Role / kind | Text / label | Visibility conditions | Source |
| --- | --- | --- | --- | --- |
| UI-00072 | "status" | {localize("ui.bed9096c411e6da3",{defaultValue:"Opening your workspace…"})} | Parent/source state | [web/app/components/HostGlobalNav.tsx:17](../../../web/app/components/HostGlobalNav.tsx#L17) |
| UI-00190 | "alert" | {localizeText(error)} | error | [web/app/events/new/page.tsx:27](../../../web/app/events/new/page.tsx#L27) |
| UI-00191 | "status" | {localize("ui.81afa7808593bd56",{defaultValue:"Preparing your space…"})} | !ready | [web/app/events/new/page.tsx:27](../../../web/app/events/new/page.tsx#L27) |
| UI-00201 | "status" | {localizeText('Opening event creation…')} | ready&&features.GUIDED_CREATION | [web/app/events/new/page.tsx:30](../../../web/app/events/new/page.tsx#L30) |
| UI-00548 | "status" | {localize("ui.7a005f0f1dbc704f",{defaultValue:"Opening your celebration space…"})} | ready\|\|discovery | [web/components/ExperienceProvider.tsx:19](../../../web/components/ExperienceProvider.tsx#L19) |
| UI-00790 | "alert" | {localizeText(error)} | error | [web/components/HostWorkspace.tsx:53](../../../web/components/HostWorkspace.tsx#L53) |
| UI-00909 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:19](../../../web/components/LocalizationProvider.tsx#L19) |
| UI-00911 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:20](../../../web/components/LocalizationProvider.tsx#L20) |
| UI-00913 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:21](../../../web/components/LocalizationProvider.tsx#L21) |


### Workflow state bindings

| Source | Binding | Initial expression |
| --- | --- | --- |
| web/app/events/new/page.tsx | step | 0 |
| web/app/events/new/page.tsx | busy | false |
| web/app/events/new/page.tsx | error | '' |
| web/components/LocalizationProvider.tsx | busy | false |
| web/components/LocalizationProvider.tsx | error | '' |
| web/components/HostWorkspace.tsx | busy | false |
| web/components/HostWorkspace.tsx | error | '' |


## Password/PIN recovery

| Screen information | Source-supported specification |
| --- | --- |
| Route | /forgot-password |
| Source | [web/app/forgot-password/page.tsx:1](../../../web/app/forgot-password/page.tsx#L1) |
| Purpose | Request and consume a scoped recovery proof; never reveal unrelated account data |
| User access level | Public scoped challenge flow; policy/provider gates |
| Navigation source | Login / account help / creator/member recovery links |
| Navigation destination | Complete scoped reset; return to approved sign-in route |
| Preconditions | Route parameters/query scope must be valid; applicable session/ownership/module/provider state above. Load errors do not imply an empty successful dataset. |
| Postconditions | Only the selected successful action changes its documented state. Navigation alone does not publish, confirm payment, verify contact or deliver a notification. |
| Route variables | No path variable |
| UI variants | Focused/general experience, responsive layout and module/state predicates can change visible sections. Exact predicates are in each control record. |


### Wireframe layout

| Section | Placement / behavior / evidence |
| --- | --- |
| Header | Shared identity/event or page title/back/actions, where declared. Composite header declarations: 2. |
| Navigation / sidebar | Host/admin navigation and event context where applicable; mobile/focused variants are conditional. nav=3, aside=3. |
| Main content | Purpose-specific forms/cards/tables/canvas/player from the composed sources. main=4, section=2, article=0. |
| Search and filters | Fields/selects listed below; exact value/handler/visibility controls are not inferred from placeholder labels. |
| Data grids and cards | Table declarations=0. Data collections and generated rows/cards are in frontend.json dynamicSources. |
| Popups / dialogs | Named/source-declared dialog control records below; opening/closing/loading/apply/cancel actions belong to those component handlers. |
| Alerts / notifications | Source controls with role alert/status are listed below. Notice strings may come from API results; do not assume a universal envelope. |
| Footer | Local/shared footer declarations=1. No undocumented legal/footer wording is invented. |
| Loading / empty / success / error | Observed state bindings and relevant action fields are listed below. Expected QA states from Document 2 do not establish a spinner/retry control where source does not implement one. |


### Composed implementation

- [web/app/forgot-password/page.tsx:1](../../../web/app/forgot-password/page.tsx#L1)
- [web/components/LocalizationProvider.tsx:1](../../../web/components/LocalizationProvider.tsx#L1)
- [web/components/CredentialRequirements.tsx:1](../../../web/components/CredentialRequirements.tsx#L1)
- [web/components/VerificationProvider.tsx:1](../../../web/components/VerificationProvider.tsx#L1)
- [web/app/layout.tsx:1](../../../web/app/layout.tsx#L1)
- [web/components/GrowthProvider.tsx:1](../../../web/components/GrowthProvider.tsx#L1)
- [web/components/BrandingProvider.tsx:1](../../../web/components/BrandingProvider.tsx#L1)
- [web/components/LegalFooter.tsx:1](../../../web/components/LegalFooter.tsx#L1)
- [web/components/CookieNotice.tsx:1](../../../web/components/CookieNotice.tsx#L1)
- [web/app/components/ApplicationInteractions.tsx:1](../../../web/app/components/ApplicationInteractions.tsx#L1)
- [web/components/ExperienceProvider.tsx:1](../../../web/components/ExperienceProvider.tsx#L1)
- [web/app/components/PerformanceDiagnostics.tsx:1](../../../web/app/components/PerformanceDiagnostics.tsx#L1)
- [web/app/components/HostSessionRedirect.tsx:1](../../../web/app/components/HostSessionRedirect.tsx#L1)
- [web/app/components/HostGlobalNav.tsx:1](../../../web/app/components/HostGlobalNav.tsx#L1)
- [web/components/HostWorkspace.tsx:1](../../../web/components/HostWorkspace.tsx#L1)
- [web/components/focused/HostShell.tsx:1](../../../web/components/focused/HostShell.tsx#L1)

### Fields

| Control ID | Label / expression | Type | Binding | Required | Native limits / format | Detailed specification |
| --- | --- | --- | --- | --- | --- | --- |
| UI-00240 | {localize("ui.dac7998fba76f342",{defaultValue:"Account type"})} | select  | {kind} | Not declared |  | [control](UI_CONTROLS.md#ui-00240) |
| UI-00241 | {localize("ui.1685290ae957e899",{defaultValue:"Verified email"})} | input "email" | {identifier} | true |  | [control](UI_CONTROLS.md#ui-00241) |
| UI-00242 | {localize("ui.0c5327fd45c72552",{defaultValue:"Recover"})} | select  | {purpose} | Not declared |  | [control](UI_CONTROLS.md#ui-00242) |
| UI-00243 | {localize("ui.d89d6f4e43b43227",{defaultValue:"Six-digit email code"})} | input  | {code} | true | pattern="[0-9]{6}" | [control](UI_CONTROLS.md#ui-00243) |
| UI-00244 | {localize("ui.277e39a233253d6b",{defaultValue:"New "})} {purpose==='RESET_PIN'?localize("ui.709e58acb65ef4a5",{defaultValue:"PIN"}):localize("ui.5e884898da280471",{defaultValue:"password"})} | input "password" | {newCredential} | true |  | [control](UI_CONTROLS.md#ui-00244) |
| UI-00245 | {localize("ui.eebdd24a77d9ad32",{defaultValue:"Confirm"})} | input "password" | {confirmCredential} | true |  | [control](UI_CONTROLS.md#ui-00245) |
| UI-00908 | {localize("ui.bba5b40bdd28c66e",{defaultValue:"Choose language"})} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00908) |
| UI-00910 | {label} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00910) |
| UI-00912 | {localize("ui.140071feea59fe87",{defaultValue:"Preferred language"})} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00912) |


### Buttons

| ID | Label / expression | Action | Submit validation / handler | Source |
| --- | --- | --- | --- | --- |
| UI-00246 | {busy?localize("ui.4660a983366f7101",{defaultValue:"Please wait…"}):challengeId?localize("ui.a74efe2cb8b750eb",{defaultValue:"Reset credential"}):localize("ui.5dbdb1b3a00e9d4f",{defaultValue:"Send recovery code"})} | submit / component action | {challengeId?complete:request} | [web/app/forgot-password/page.tsx:22](../../../web/app/forgot-password/page.tsx#L22) |
| UI-00528 | Accept optional analytics | {()=>{saveConsent(true);setShow(false)}} | Not a native form submit or inherited from component context | [web/components/CookieNotice.tsx:5](../../../web/components/CookieNotice.tsx#L5) |
| UI-00529 | Reject optional analytics | {()=>{saveConsent(false);setShow(false)}} | Not a native form submit or inherited from component context | [web/components/CookieNotice.tsx:5](../../../web/components/CookieNotice.tsx#L5) |
| UI-00788 | {collapsed?<PanelLeftOpen size={20}/>:<PanelLeftClose size={20}/>} | {toggle} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:50](../../../web/components/HostWorkspace.tsx#L50) |
| UI-00791 |  | {()=>void signOut()} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:53](../../../web/components/HostWorkspace.tsx#L53) |
| UI-00792 |  | {()=>setCollapsed(true)} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:54](../../../web/components/HostWorkspace.tsx#L54) |
| UI-00876 | Cookie preferences | {()=>window.dispatchEvent(new Event('giftwise-cookie-settings'))} | Not a native form submit or inherited from component context | [web/components/LegalFooter.tsx:4](../../../web/components/LegalFooter.tsx#L4) |


### Dialog and alert/status inventory

| ID | Role / kind | Text / label | Visibility conditions | Source |
| --- | --- | --- | --- | --- |
| UI-00072 | "status" | {localize("ui.bed9096c411e6da3",{defaultValue:"Opening your workspace…"})} | Parent/source state | [web/app/components/HostGlobalNav.tsx:17](../../../web/app/components/HostGlobalNav.tsx#L17) |
| UI-00247 | "status" | {localizeText(message)} | message | [web/app/forgot-password/page.tsx:22](../../../web/app/forgot-password/page.tsx#L22) |
| UI-00531 | "status" | {value?(remaining?localizeText(`${remaining} requirements still needed.`):localizeText("All requirements met.")):localizeText("Enter your "+kind+" to check the requirements.")} | Parent/source state | [web/components/CredentialRequirements.tsx:7](../../../web/components/CredentialRequirements.tsx#L7) |
| UI-00548 | "status" | {localize("ui.7a005f0f1dbc704f",{defaultValue:"Opening your celebration space…"})} | ready\|\|discovery | [web/components/ExperienceProvider.tsx:19](../../../web/components/ExperienceProvider.tsx#L19) |
| UI-00790 | "alert" | {localizeText(error)} | error | [web/components/HostWorkspace.tsx:53](../../../web/components/HostWorkspace.tsx#L53) |
| UI-00909 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:19](../../../web/components/LocalizationProvider.tsx#L19) |
| UI-00911 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:20](../../../web/components/LocalizationProvider.tsx#L20) |
| UI-00913 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:21](../../../web/components/LocalizationProvider.tsx#L21) |


### Workflow state bindings

| Source | Binding | Initial expression |
| --- | --- | --- |
| web/app/forgot-password/page.tsx | message | '' |
| web/app/forgot-password/page.tsx | busy | false |
| web/components/LocalizationProvider.tsx | busy | false |
| web/components/LocalizationProvider.tsx | error | '' |
| web/components/HostWorkspace.tsx | busy | false |
| web/components/HostWorkspace.tsx | error | '' |


## Username recovery

| Screen information | Source-supported specification |
| --- | --- |
| Route | /forgot-username |
| Source | [web/app/forgot-username/page.tsx:1](../../../web/app/forgot-username/page.tsx#L1) |
| Purpose | Request account-identifier reminder through approved contact/channel |
| User access level | Public scoped challenge flow; policy/provider gates |
| Navigation source | Login / group account help |
| Navigation destination | Safe submitted/availability state and sign-in return |
| Preconditions | Route parameters/query scope must be valid; applicable session/ownership/module/provider state above. Load errors do not imply an empty successful dataset. |
| Postconditions | Only the selected successful action changes its documented state. Navigation alone does not publish, confirm payment, verify contact or deliver a notification. |
| Route variables | No path variable |
| UI variants | Focused/general experience, responsive layout and module/state predicates can change visible sections. Exact predicates are in each control record. |


### Wireframe layout

| Section | Placement / behavior / evidence |
| --- | --- |
| Header | Shared identity/event or page title/back/actions, where declared. Composite header declarations: 2. |
| Navigation / sidebar | Host/admin navigation and event context where applicable; mobile/focused variants are conditional. nav=3, aside=3. |
| Main content | Purpose-specific forms/cards/tables/canvas/player from the composed sources. main=4, section=3, article=0. |
| Search and filters | Fields/selects listed below; exact value/handler/visibility controls are not inferred from placeholder labels. |
| Data grids and cards | Table declarations=0. Data collections and generated rows/cards are in frontend.json dynamicSources. |
| Popups / dialogs | Named/source-declared dialog control records below; opening/closing/loading/apply/cancel actions belong to those component handlers. |
| Alerts / notifications | Source controls with role alert/status are listed below. Notice strings may come from API results; do not assume a universal envelope. |
| Footer | Local/shared footer declarations=1. No undocumented legal/footer wording is invented. |
| Loading / empty / success / error | Observed state bindings and relevant action fields are listed below. Expected QA states from Document 2 do not establish a spinner/retry control where source does not implement one. |


### Composed implementation

- [web/app/forgot-username/page.tsx:1](../../../web/app/forgot-username/page.tsx#L1)
- [web/components/LocalizationProvider.tsx:1](../../../web/components/LocalizationProvider.tsx#L1)
- [web/components/VerificationProvider.tsx:1](../../../web/components/VerificationProvider.tsx#L1)
- [web/app/layout.tsx:1](../../../web/app/layout.tsx#L1)
- [web/components/GrowthProvider.tsx:1](../../../web/components/GrowthProvider.tsx#L1)
- [web/components/BrandingProvider.tsx:1](../../../web/components/BrandingProvider.tsx#L1)
- [web/components/LegalFooter.tsx:1](../../../web/components/LegalFooter.tsx#L1)
- [web/components/CookieNotice.tsx:1](../../../web/components/CookieNotice.tsx#L1)
- [web/app/components/ApplicationInteractions.tsx:1](../../../web/app/components/ApplicationInteractions.tsx#L1)
- [web/components/ExperienceProvider.tsx:1](../../../web/components/ExperienceProvider.tsx#L1)
- [web/app/components/PerformanceDiagnostics.tsx:1](../../../web/app/components/PerformanceDiagnostics.tsx#L1)
- [web/app/components/HostSessionRedirect.tsx:1](../../../web/app/components/HostSessionRedirect.tsx#L1)
- [web/app/components/HostGlobalNav.tsx:1](../../../web/app/components/HostGlobalNav.tsx#L1)
- [web/components/HostWorkspace.tsx:1](../../../web/components/HostWorkspace.tsx#L1)
- [web/components/focused/HostShell.tsx:1](../../../web/components/focused/HostShell.tsx#L1)

### Fields

| Control ID | Label / expression | Type | Binding | Required | Native limits / format | Detailed specification |
| --- | --- | --- | --- | --- | --- | --- |
| UI-00251 | {localize("ui.dbb5f6371b6a2a67",{defaultValue:"Your account"})} | select  | {kind} | Not declared |  | [control](UI_CONTROLS.md#ui-00251) |
| UI-00252 | {localize("ui.07c3b65a5eaa1a25",{defaultValue:"Verified email address"})} | input "email" | {email} | true | maxLength={254} | [control](UI_CONTROLS.md#ui-00252) |
| UI-00908 | {localize("ui.bba5b40bdd28c66e",{defaultValue:"Choose language"})} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00908) |
| UI-00910 | {label} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00910) |
| UI-00912 | {localize("ui.140071feea59fe87",{defaultValue:"Preferred language"})} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00912) |


### Buttons

| ID | Label / expression | Action | Submit validation / handler | Source |
| --- | --- | --- | --- | --- |
| UI-00253 | {busy?localize("ui.94b1ce97199dbd15",{defaultValue:"Sending request…"}):localize("ui.99ac76b5fab90b53",{defaultValue:"Email my username"})} | submit / component action | {submit} | [web/app/forgot-username/page.tsx:17](../../../web/app/forgot-username/page.tsx#L17) |
| UI-00528 | Accept optional analytics | {()=>{saveConsent(true);setShow(false)}} | Not a native form submit or inherited from component context | [web/components/CookieNotice.tsx:5](../../../web/components/CookieNotice.tsx#L5) |
| UI-00529 | Reject optional analytics | {()=>{saveConsent(false);setShow(false)}} | Not a native form submit or inherited from component context | [web/components/CookieNotice.tsx:5](../../../web/components/CookieNotice.tsx#L5) |
| UI-00788 | {collapsed?<PanelLeftOpen size={20}/>:<PanelLeftClose size={20}/>} | {toggle} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:50](../../../web/components/HostWorkspace.tsx#L50) |
| UI-00791 |  | {()=>void signOut()} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:53](../../../web/components/HostWorkspace.tsx#L53) |
| UI-00792 |  | {()=>setCollapsed(true)} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:54](../../../web/components/HostWorkspace.tsx#L54) |
| UI-00876 | Cookie preferences | {()=>window.dispatchEvent(new Event('giftwise-cookie-settings'))} | Not a native form submit or inherited from component context | [web/components/LegalFooter.tsx:4](../../../web/components/LegalFooter.tsx#L4) |


### Dialog and alert/status inventory

| ID | Role / kind | Text / label | Visibility conditions | Source |
| --- | --- | --- | --- | --- |
| UI-00072 | "status" | {localize("ui.bed9096c411e6da3",{defaultValue:"Opening your workspace…"})} | Parent/source state | [web/app/components/HostGlobalNav.tsx:17](../../../web/app/components/HostGlobalNav.tsx#L17) |
| UI-00254 | "status" |  | message | [web/app/forgot-username/page.tsx:17](../../../web/app/forgot-username/page.tsx#L17) |
| UI-00255 | "alert" | {localizeText(error)} | error | [web/app/forgot-username/page.tsx:17](../../../web/app/forgot-username/page.tsx#L17) |
| UI-00548 | "status" | {localize("ui.7a005f0f1dbc704f",{defaultValue:"Opening your celebration space…"})} | ready\|\|discovery | [web/components/ExperienceProvider.tsx:19](../../../web/components/ExperienceProvider.tsx#L19) |
| UI-00790 | "alert" | {localizeText(error)} | error | [web/components/HostWorkspace.tsx:53](../../../web/components/HostWorkspace.tsx#L53) |
| UI-00909 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:19](../../../web/components/LocalizationProvider.tsx#L19) |
| UI-00911 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:20](../../../web/components/LocalizationProvider.tsx#L20) |
| UI-00913 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:21](../../../web/components/LocalizationProvider.tsx#L21) |


### Workflow state bindings

| Source | Binding | Initial expression |
| --- | --- | --- |
| web/app/forgot-username/page.tsx | busy | false |
| web/app/forgot-username/page.tsx | message | '' |
| web/app/forgot-username/page.tsx | error | '' |
| web/components/LocalizationProvider.tsx | busy | false |
| web/components/LocalizationProvider.tsx | error | '' |
| web/components/HostWorkspace.tsx | busy | false |
| web/components/HostWorkspace.tsx | error | '' |


## Gift wishlist

| Screen information | Source-supported specification |
| --- | --- |
| Route | /gifts |
| Source | [web/app/gifts/page.tsx:1](../../../web/app/gifts/page.tsx#L1) |
| Purpose | Create/edit/prioritize/activate/retire gifts, product metadata and photos |
| User access level | Authenticated owner; eventId query scopes the selected event |
| Navigation source | Event checklist / sidebar / gift navigation |
| Navigation destination | Saved owned wishlist; retailer lookup/manual entry; invitation preview |
| Preconditions | Route parameters/query scope must be valid; applicable session/ownership/module/provider state above. Load errors do not imply an empty successful dataset. |
| Postconditions | Only the selected successful action changes its documented state. Navigation alone does not publish, confirm payment, verify contact or deliver a notification. |
| Route variables | No path variable |
| UI variants | Focused/general experience, responsive layout and module/state predicates can change visible sections. Exact predicates are in each control record. |


### Wireframe layout

| Section | Placement / behavior / evidence |
| --- | --- |
| Header | Shared identity/event or page title/back/actions, where declared. Composite header declarations: 4. |
| Navigation / sidebar | Host/admin navigation and event context where applicable; mobile/focused variants are conditional. nav=3, aside=3. |
| Main content | Purpose-specific forms/cards/tables/canvas/player from the composed sources. main=4, section=3, article=1. |
| Search and filters | Fields/selects listed below; exact value/handler/visibility controls are not inferred from placeholder labels. |
| Data grids and cards | Table declarations=0. Data collections and generated rows/cards are in frontend.json dynamicSources. |
| Popups / dialogs | Named/source-declared dialog control records below; opening/closing/loading/apply/cancel actions belong to those component handlers. |
| Alerts / notifications | Source controls with role alert/status are listed below. Notice strings may come from API results; do not assume a universal envelope. |
| Footer | Local/shared footer declarations=1. No undocumented legal/footer wording is invented. |
| Loading / empty / success / error | Observed state bindings and relevant action fields are listed below. Expected QA states from Document 2 do not establish a spinner/retry control where source does not implement one. |


### Composed implementation

- [web/app/gifts/page.tsx:1](../../../web/app/gifts/page.tsx#L1)
- [web/components/LocalizationProvider.tsx:1](../../../web/components/LocalizationProvider.tsx#L1)
- [web/components/GiftDescription.tsx:1](../../../web/components/GiftDescription.tsx#L1)
- [web/app/components/HostToast.tsx:1](../../../web/app/components/HostToast.tsx#L1)
- [web/app/layout.tsx:1](../../../web/app/layout.tsx#L1)
- [web/components/GrowthProvider.tsx:1](../../../web/components/GrowthProvider.tsx#L1)
- [web/components/BrandingProvider.tsx:1](../../../web/components/BrandingProvider.tsx#L1)
- [web/components/LegalFooter.tsx:1](../../../web/components/LegalFooter.tsx#L1)
- [web/components/CookieNotice.tsx:1](../../../web/components/CookieNotice.tsx#L1)
- [web/app/components/ApplicationInteractions.tsx:1](../../../web/app/components/ApplicationInteractions.tsx#L1)
- [web/components/VerificationProvider.tsx:1](../../../web/components/VerificationProvider.tsx#L1)
- [web/components/ExperienceProvider.tsx:1](../../../web/components/ExperienceProvider.tsx#L1)
- [web/app/components/PerformanceDiagnostics.tsx:1](../../../web/app/components/PerformanceDiagnostics.tsx#L1)
- [web/app/components/HostSessionRedirect.tsx:1](../../../web/app/components/HostSessionRedirect.tsx#L1)
- [web/app/components/HostGlobalNav.tsx:1](../../../web/app/components/HostGlobalNav.tsx#L1)
- [web/components/HostWorkspace.tsx:1](../../../web/components/HostWorkspace.tsx#L1)
- [web/components/focused/HostShell.tsx:1](../../../web/components/focused/HostShell.tsx#L1)

### Fields

| Control ID | Label / expression | Type | Binding | Required | Native limits / format | Detailed specification |
| --- | --- | --- | --- | --- | --- | --- |
| UI-00258 | {localize("ui.fc832ad901abec7d",{defaultValue:"Choose a celebration"})} | select  |  | Not declared |  | [control](UI_CONTROLS.md#ui-00258) |
| UI-00263 | {localize("ui.a1d2af078e7baa74",{defaultValue:"Retailer product HTTPS link (optional)"})} | input "url" | {form.productUrl} | Not declared |  | [control](UI_CONTROLS.md#ui-00263) |
| UI-00265 | {localize("ui.64b4ff26c07d838e",{defaultValue:"Gift name"})} | input  | {form.name} | true | maxLength={160} | [control](UI_CONTROLS.md#ui-00265) |
| UI-00266 | {localize("ui.52e25dc51ef1a983",{defaultValue:"Target amount (₹)"})} | input "number" | {form.targetAmount} | true | min="0.01"; max="10000000"; step="0.01" | [control](UI_CONTROLS.md#ui-00266) |
| UI-00267 | {localize("ui.526e0087cc3f254d",{defaultValue:"Description"})} | textarea  | {form.description} | Not declared | maxLength={2000} | [control](UI_CONTROLS.md#ui-00267) |
| UI-00268 | {localize("ui.9dc5bd948b41f5ea",{defaultValue:"Gift photo (optional)"})} | input "file" |  | Not declared |  | [control](UI_CONTROLS.md#ui-00268) |
| UI-00273 | {localize("ui.85ccc5ef109388cf",{defaultValue:"Gift priority"})} | select  | {form.priority} | Not declared |  | [control](UI_CONTROLS.md#ui-00273) |
| UI-00274 | {localize("ui.7448611d5f93aa8a",{defaultValue:"Visibility"})} | select  | {form.status} | Not declared |  | [control](UI_CONTROLS.md#ui-00274) |
| UI-00278 | {localize("ui.a7a0a67cae7bba5a",{defaultValue:"Search gifts"})} | input  | {search} | Not declared |  | [control](UI_CONTROLS.md#ui-00278) |
| UI-00286 | No static label; inspect component context | input "file" |  | Not declared |  | [control](UI_CONTROLS.md#ui-00286) |
| UI-00908 | {localize("ui.bba5b40bdd28c66e",{defaultValue:"Choose language"})} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00908) |
| UI-00910 | {label} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00910) |
| UI-00912 | {localize("ui.140071feea59fe87",{defaultValue:"Preferred language"})} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00912) |


### Buttons

| ID | Label / expression | Action | Submit validation / handler | Source |
| --- | --- | --- | --- | --- |
| UI-00078 | × | {()=>{setFlash(null);onDismiss?.();}} | Not a native form submit or inherited from component context | [web/app/components/HostToast.tsx:16](../../../web/app/components/HostToast.tsx#L16) |
| UI-00264 | {fetchingRetailer?localize("ui.3428ce60bb354725",{defaultValue:"Checking product…"}):localize("ui.e25f10c861e2b36a",{defaultValue:"Validate product link & add details"})} | {()=>void fetchRetailer(true).catch(e=>setError(message(e,'Retailer lookup failed')))} | {e=>void save(e)} | [web/app/gifts/page.tsx:87](../../../web/app/gifts/page.tsx#L87) |
| UI-00271 | {localize("ui.f46512ab2099fb0c",{defaultValue:"Remove photo"})} | {()=>{setForm(f=>({...f,imageKey:''}));setManualPhoto(true)}} | {e=>void save(e)} | [web/app/gifts/page.tsx:88](../../../web/app/gifts/page.tsx#L88) |
| UI-00272 | {localize("ui.d1e1a924ebcdfea2",{defaultValue:"Use retailer photo instead"})} | {()=>{setForm(f=>({...f,imageKey:retailer.imageKey\|\|''}));setManualPhoto(false)}} | {e=>void save(e)} | [web/app/gifts/page.tsx:88](../../../web/app/gifts/page.tsx#L88) |
| UI-00275 | {busy?localize("ui.23e39291d6135814",{defaultValue:"Saving…"}):photoBusy?localize("ui.93a48bbffe6bf9ff",{defaultValue:"Checking photo…"}):editing?localize("ui.1805aec5f6769afc",{defaultValue:"Save gift"}):localize("ui.fb69fa89bd6b5d4f",{defaultValue:"Create gift"})} | submit / component action | {e=>void save(e)} | [web/app/gifts/page.tsx:89](../../../web/app/gifts/page.tsx#L89) |
| UI-00276 | {localize("ui.6fa271453f8ac490",{defaultValue:"Cancel edit"})} | {reset} | {e=>void save(e)} | [web/app/gifts/page.tsx:89](../../../web/app/gifts/page.tsx#L89) |
| UI-00277 | {localize("ui.0e91610117029a62",{defaultValue:"Refresh"})} | {()=>void load(eventId)} | Not a native form submit or inherited from component context | [web/app/gifts/page.tsx:90](../../../web/app/gifts/page.tsx#L90) |
| UI-00281 | {localize("ui.464c4ffd019e1e96",{defaultValue:"Edit"})} | {()=>edit(g)} | Not a native form submit or inherited from component context | [web/app/gifts/page.tsx:90](../../../web/app/gifts/page.tsx#L90) |
| UI-00282 | {g.status==='PUBLIC'?localize("ui.2db04a54fa743150",{defaultValue:"Unpublish"}):localize("ui.859390eb495b2ead",{defaultValue:"Publish"})} | {()=>void changeVisibility(g)} | Not a native form submit or inherited from component context | [web/app/gifts/page.tsx:90](../../../web/app/gifts/page.tsx#L90) |
| UI-00283 | {localize("ui.be92065430af062e",{defaultValue:"Cancel gift"})} | {()=>void cancel(g)} | Not a native form submit or inherited from component context | [web/app/gifts/page.tsx:90](../../../web/app/gifts/page.tsx#L90) |
| UI-00284 | {localize("ui.fab780d4db7a7687",{defaultValue:" Export wishlist"})} | {()=>download(items,'gift-wishlist-backup.json')} | Not a native form submit or inherited from component context | [web/app/gifts/page.tsx:91](../../../web/app/gifts/page.tsx#L91) |
| UI-00285 | {localize("ui.a4c8f47c4d5712dc",{defaultValue:"Import backup"})} | {()=>importInput.current?.click()} | Not a native form submit or inherited from component context | [web/app/gifts/page.tsx:91](../../../web/app/gifts/page.tsx#L91) |
| UI-00287 | {localize("ui.58c3610df2db293d",{defaultValue:"Export older drafts"})} | {()=>download(localDrafts,'gift-platform-local-drafts-backup.json')} | Not a native form submit or inherited from component context | [web/app/gifts/page.tsx:91](../../../web/app/gifts/page.tsx#L91) |
| UI-00288 | {localize("ui.02da1cf8ceb528d1",{defaultValue:"Review older drafts"})} | {()=>setImportDrafts(localDrafts)} | Not a native form submit or inherited from component context | [web/app/gifts/page.tsx:91](../../../web/app/gifts/page.tsx#L91) |
| UI-00289 | {localize("ui.5d434becdedeff1f",{defaultValue:"Import gift"})} | {()=>void importGift(g)} | Not a native form submit or inherited from component context | [web/app/gifts/page.tsx:91](../../../web/app/gifts/page.tsx#L91) |
| UI-00290 | {localize("ui.28d03596d24eeb4e",{defaultValue:"Skip"})} | {()=>setImportDrafts(current=>current.filter(row=>row.id!==g.id))} | Not a native form submit or inherited from component context | [web/app/gifts/page.tsx:91](../../../web/app/gifts/page.tsx#L91) |
| UI-00528 | Accept optional analytics | {()=>{saveConsent(true);setShow(false)}} | Not a native form submit or inherited from component context | [web/components/CookieNotice.tsx:5](../../../web/components/CookieNotice.tsx#L5) |
| UI-00529 | Reject optional analytics | {()=>{saveConsent(false);setShow(false)}} | Not a native form submit or inherited from component context | [web/components/CookieNotice.tsx:5](../../../web/components/CookieNotice.tsx#L5) |
| UI-00549 | {localize("ui.ef0edc1b48d46a83",{defaultValue:"View full description"})} | {()=>dialog.current?.showModal()} | Not a native form submit or inherited from component context | [web/components/GiftDescription.tsx:13](../../../web/components/GiftDescription.tsx#L13) |
| UI-00551 | {localize("ui.ad8c41bed4972296",{defaultValue:"Close ×"})} | {()=>dialog.current?.close()} | Not a native form submit or inherited from component context | [web/components/GiftDescription.tsx:13](../../../web/components/GiftDescription.tsx#L13) |
| UI-00788 | {collapsed?<PanelLeftOpen size={20}/>:<PanelLeftClose size={20}/>} | {toggle} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:50](../../../web/components/HostWorkspace.tsx#L50) |
| UI-00791 |  | {()=>void signOut()} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:53](../../../web/components/HostWorkspace.tsx#L53) |
| UI-00792 |  | {()=>setCollapsed(true)} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:54](../../../web/components/HostWorkspace.tsx#L54) |
| UI-00876 | Cookie preferences | {()=>window.dispatchEvent(new Event('giftwise-cookie-settings'))} | Not a native form submit or inherited from component context | [web/components/LegalFooter.tsx:4](../../../web/components/LegalFooter.tsx#L4) |


### Dialog and alert/status inventory

| ID | Role / kind | Text / label | Visibility conditions | Source |
| --- | --- | --- | --- | --- |
| UI-00256 | native-dialog | `Cancel ${g.name}?` | Parent/source state | [web/app/gifts/page.tsx:80](../../../web/app/gifts/page.tsx#L80) |
| UI-00550 | dialog |  | Parent/source state | [web/components/GiftDescription.tsx:13](../../../web/components/GiftDescription.tsx#L13) |
| UI-00072 | "status" | {localize("ui.bed9096c411e6da3",{defaultValue:"Opening your workspace…"})} | Parent/source state | [web/app/components/HostGlobalNav.tsx:17](../../../web/app/components/HostGlobalNav.tsx#L17) |
| UI-00259 | "alert" | {localizeText(error)} | error | [web/app/gifts/page.tsx:86](../../../web/app/gifts/page.tsx#L86) |
| UI-00260 | "status" | {localizeText(notice)} | notice | [web/app/gifts/page.tsx:86](../../../web/app/gifts/page.tsx#L86) |
| UI-00269 | "alert" | {photoError} | photoError; eventId | [web/app/gifts/page.tsx:88](../../../web/app/gifts/page.tsx#L88) |
| UI-00270 | "status" | {photoStatus} | photoStatus; eventId | [web/app/gifts/page.tsx:88](../../../web/app/gifts/page.tsx#L88) |
| UI-00279 | "status" | {localize("ui.20fc5947f275b14c",{defaultValue:"Loading gifts…"})} | loading | [web/app/gifts/page.tsx:90](../../../web/app/gifts/page.tsx#L90) |
| UI-00548 | "status" | {localize("ui.7a005f0f1dbc704f",{defaultValue:"Opening your celebration space…"})} | ready\|\|discovery | [web/components/ExperienceProvider.tsx:19](../../../web/components/ExperienceProvider.tsx#L19) |
| UI-00790 | "alert" | {localizeText(error)} | error | [web/components/HostWorkspace.tsx:53](../../../web/components/HostWorkspace.tsx#L53) |
| UI-00909 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:19](../../../web/components/LocalizationProvider.tsx#L19) |
| UI-00911 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:20](../../../web/components/LocalizationProvider.tsx#L20) |
| UI-00913 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:21](../../../web/components/LocalizationProvider.tsx#L21) |


### Workflow state bindings

| Source | Binding | Initial expression |
| --- | --- | --- |
| web/app/gifts/page.tsx | loading | false |
| web/app/gifts/page.tsx | busy | false |
| web/app/gifts/page.tsx | error | '' |
| web/app/gifts/page.tsx | notice | '' |
| web/app/gifts/page.tsx | photoBusy | false |
| web/app/gifts/page.tsx | photoError | '' |
| web/components/LocalizationProvider.tsx | busy | false |
| web/components/LocalizationProvider.tsx | error | '' |
| web/components/HostWorkspace.tsx | busy | false |
| web/components/HostWorkspace.tsx | error | '' |


## Group invitation and member workspace

| Screen information | Source-supported specification |
| --- | --- |
| Route | /groups/[token]/[groupId] |
| Source | [web/app/groups/[token]/[groupId]/page.tsx:1](../../../web/app/groups/[token]/[groupId]/page.tsx#L1) |
| Purpose | Guest contribution without registration; join/sign in; approved-member private view |
| User access level | Published invitation/group readiness plus allowed guest/member access |
| Navigation source | Independent group card / copied creator invite / shared group link |
| Navigation destination | Report transfer or join/sign in; pending/approved/member/logout states |
| Preconditions | Route parameters/query scope must be valid; applicable session/ownership/module/provider state above. Load errors do not imply an empty successful dataset. |
| Postconditions | Only the selected successful action changes its documented state. Navigation alone does not publish, confirm payment, verify contact or deliver a notification. |
| Route variables | token, groupId |
| UI variants | Focused/general experience, responsive layout and module/state predicates can change visible sections. Exact predicates are in each control record. |


### Wireframe layout

| Section | Placement / behavior / evidence |
| --- | --- |
| Header | Shared identity/event or page title/back/actions, where declared. Composite header declarations: 2. |
| Navigation / sidebar | Host/admin navigation and event context where applicable; mobile/focused variants are conditional. nav=4, aside=3. |
| Main content | Purpose-specific forms/cards/tables/canvas/player from the composed sources. main=5, section=4, article=0. |
| Search and filters | Fields/selects listed below; exact value/handler/visibility controls are not inferred from placeholder labels. |
| Data grids and cards | Table declarations=0. Data collections and generated rows/cards are in frontend.json dynamicSources. |
| Popups / dialogs | Named/source-declared dialog control records below; opening/closing/loading/apply/cancel actions belong to those component handlers. |
| Alerts / notifications | Source controls with role alert/status are listed below. Notice strings may come from API results; do not assume a universal envelope. |
| Footer | Local/shared footer declarations=1. No undocumented legal/footer wording is invented. |
| Loading / empty / success / error | Observed state bindings and relevant action fields are listed below. Expected QA states from Document 2 do not establish a spinner/retry control where source does not implement one. |


### Composed implementation

- [web/app/groups/[token]/[groupId]/page.tsx:1](../../../web/app/groups/[token]/[groupId]/page.tsx#L1)
- [web/components/CredentialRequirements.tsx:1](../../../web/components/CredentialRequirements.tsx#L1)
- [web/components/TermsAcceptance.tsx:1](../../../web/components/TermsAcceptance.tsx#L1)
- [web/components/LocalizationProvider.tsx:1](../../../web/components/LocalizationProvider.tsx#L1)
- [web/components/VerificationProvider.tsx:1](../../../web/components/VerificationProvider.tsx#L1)
- [web/components/SignupEmailVerification.tsx:1](../../../web/components/SignupEmailVerification.tsx#L1)
- [web/components/SignupEmailField.tsx:1](../../../web/components/SignupEmailField.tsx#L1)
- [web/components/GroupEmailVerification.tsx:1](../../../web/components/GroupEmailVerification.tsx#L1)
- [web/components/GroupMobileVerification.tsx:1](../../../web/components/GroupMobileVerification.tsx#L1)
- [web/components/MobileVerification.tsx:1](../../../web/components/MobileVerification.tsx#L1)
- [web/components/ExperienceProvider.tsx:1](../../../web/components/ExperienceProvider.tsx#L1)
- [web/components/GroupGuestCheckout.tsx:1](../../../web/components/GroupGuestCheckout.tsx#L1)
- [web/app/layout.tsx:1](../../../web/app/layout.tsx#L1)
- [web/components/GrowthProvider.tsx:1](../../../web/components/GrowthProvider.tsx#L1)
- [web/components/BrandingProvider.tsx:1](../../../web/components/BrandingProvider.tsx#L1)
- [web/components/LegalFooter.tsx:1](../../../web/components/LegalFooter.tsx#L1)
- [web/components/CookieNotice.tsx:1](../../../web/components/CookieNotice.tsx#L1)
- [web/app/components/ApplicationInteractions.tsx:1](../../../web/app/components/ApplicationInteractions.tsx#L1)
- [web/app/components/PerformanceDiagnostics.tsx:1](../../../web/app/components/PerformanceDiagnostics.tsx#L1)
- [web/app/components/HostSessionRedirect.tsx:1](../../../web/app/components/HostSessionRedirect.tsx#L1)
- [web/app/components/HostGlobalNav.tsx:1](../../../web/app/components/HostGlobalNav.tsx#L1)
- [web/components/HostWorkspace.tsx:1](../../../web/components/HostWorkspace.tsx#L1)
- [web/components/focused/HostShell.tsx:1](../../../web/components/focused/HostShell.tsx#L1)

### Fields

| Control ID | Label / expression | Type | Binding | Required | Native limits / format | Detailed specification |
| --- | --- | --- | --- | --- | --- | --- |
| UI-00301 | {mode==='join'?localize("ui.2cbfe48f2cd10d4d",{defaultValue:"Username (unique within this group)"}):localize("ui.20394e55e2f6ad25",{defaultValue:"Username (the name you joined with)"})} | input  | {name} | true | minLength={mode==='join'?4:undefined}; maxLength={mode==='join'?30:undefined}; pattern={mode==='join'?'[A-Za-z0-9][A-Za-z0-9_]{3,29}':undefined} | [control](UI_CONTROLS.md#ui-00301) |
| UI-00302 | {localize("ui.11aaa14268df51b7",{defaultValue:"Sign in with"})} | select  | {loginMethod} | Not declared |  | [control](UI_CONTROLS.md#ui-00302) |
| UI-00303 | {localize("ui.e7cf3ef4f17c3999",{defaultValue:"Password"})} | input "password" | {password} | true | minLength={mode==='join'?8:undefined} | [control](UI_CONTROLS.md#ui-00303) |
| UI-00304 | {localize("ui.de7aa7f6133ffaf9",{defaultValue:"Email address (optional unless verification is required)"})} | input "email" | {email} | {emailVerificationEnabled&&group.memberEmailVerificationRequired} |  | [control](UI_CONTROLS.md#ui-00304) |
| UI-00305 | {localize("ui.187e80ba4d8a22bb",{defaultValue:"Mobile number"})} | input "tel" | {mobile} | true |  | [control](UI_CONTROLS.md#ui-00305) |
| UI-00306 | {localize("ui.5ac265f396a2c586",{defaultValue:"Confirm password"})} | input "password" | {confirmPassword} | true | minLength={8} | [control](UI_CONTROLS.md#ui-00306) |
| UI-00307 | {localize("ui.61c2b9f2c16ea005",{defaultValue:"Six-digit PIN"})} | input "password" | {pin} | true | maxLength={6}; pattern="[0-9]{6}" | [control](UI_CONTROLS.md#ui-00307) |
| UI-00308 | {localize("ui.4fa10c7baa0c1d89",{defaultValue:"Confirm PIN"})} | input "password" | {confirmPin} | true | maxLength={6}; pattern="[0-9]{6}" | [control](UI_CONTROLS.md#ui-00308) |
| UI-00309 | {localize("ui.61c2b9f2c16ea005",{defaultValue:"Six-digit PIN"})} | input "password" | {pin} | true | maxLength={6}; pattern="[0-9]{6}" | [control](UI_CONTROLS.md#ui-00309) |
| UI-00637 | {localize("ui.f2488fd4ef4adbc6",{defaultValue:"Email address"})} | input "email" | {email} | true | maxLength={254} | [control](UI_CONTROLS.md#ui-00637) |
| UI-00640 | {localize("ui.3bcb66ee1a6feaf5",{defaultValue:"Six-digit code"})} | input  | {code} | true | maxLength={6}; pattern="[0-9]{6}" | [control](UI_CONTROLS.md#ui-00640) |
| UI-00654 | {localize("ui.749c97e490fa85d6",{defaultValue:"Your contribution amount (₹)"})} | input  | {amount} | true |  | [control](UI_CONTROLS.md#ui-00654) |
| UI-00655 | {localize("ui.7204645b7341d714",{defaultValue:"Your name *"})} | input  | {name} | true | minLength={2}; maxLength={120} | [control](UI_CONTROLS.md#ui-00655) |
| UI-00656 | {localize("ui.433f5f401017f2de",{defaultValue:"Mobile or email *"})} | input  | {contact} | true | maxLength={120} | [control](UI_CONTROLS.md#ui-00656) |
| UI-00660 | {localize("ui.5ffa24818e50be17",{defaultValue:"Transaction reference / UTR *"})} | input  | {reference} | true | minLength={6}; maxLength={120} | [control](UI_CONTROLS.md#ui-00660) |
| UI-00908 | {localize("ui.bba5b40bdd28c66e",{defaultValue:"Choose language"})} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00908) |
| UI-00910 | {label} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00910) |
| UI-00912 | {localize("ui.140071feea59fe87",{defaultValue:"Preferred language"})} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00912) |
| UI-00931 | {localize("ui.dae5cf30549f072d",{defaultValue:"Mobile number with country code"})} | input "tel" | {mobile} | true | pattern="\+[1-9][0-9]{7,14}" | [control](UI_CONTROLS.md#ui-00931) |
| UI-00932 | {localize("ui.f673a246d45f4b8f",{defaultValue:"Six-digit verification code"})} | input  | {code} | true | maxLength={6}; pattern="[0-9]{6}" | [control](UI_CONTROLS.md#ui-00932) |
| UI-00998 | {localize("ui.f2488fd4ef4adbc6",{defaultValue:"Email address"})} | input "email" | {value} | true | maxLength={254} | [control](UI_CONTROLS.md#ui-00998) |
| UI-01001 | {localize("ui.d89d6f4e43b43227",{defaultValue:"Six-digit email code"})} | input  | {code} | Not declared | maxLength={6} | [control](UI_CONTROLS.md#ui-01001) |
| UI-01044 |  | input "checkbox" | {checked} | true |  | [control](UI_CONTROLS.md#ui-01044) |


### Buttons

| ID | Label / expression | Action | Submit validation / handler | Source |
| --- | --- | --- | --- | --- |
| UI-00292 | {localize("ui.c7fbeb70c75ec6df",{defaultValue:"Contribute to the gift"})} | {()=>setSection('contribute')} | Not a native form submit or inherited from component context | [web/app/groups/[token]/[groupId]/page.tsx:37](../../../web/app/groups/[token]/[groupId]/page.tsx#L37) |
| UI-00293 | {localize("ui.390a8175c10bc5c0",{defaultValue:"Join / Member sign in"})} | {()=>setSection('membership')} | Not a native form submit or inherited from component context | [web/app/groups/[token]/[groupId]/page.tsx:37](../../../web/app/groups/[token]/[groupId]/page.tsx#L37) |
| UI-00294 | {localize("ui.1e3442dcac90cbf3",{defaultValue:"Member sign out"})} | {async()=>{try{await api(`/member-logout/${groupId}`,{});setView(null);setMode('login');setMessage('Signed out of your member account.')}catch{setMessage('Unable to sign out. Please try again.')}}} | Not a native form submit or inherited from component context | [web/app/groups/[token]/[groupId]/page.tsx:38](../../../web/app/groups/[token]/[groupId]/page.tsx#L38) |
| UI-00296 | {localize("ui.48a2587a6cb220e8",{defaultValue:"Join group"})} | {()=>setMode('join')} | Not a native form submit or inherited from component context | [web/app/groups/[token]/[groupId]/page.tsx:38](../../../web/app/groups/[token]/[groupId]/page.tsx#L38) |
| UI-00297 | {localize("ui.bd6a9163c07b13b4",{defaultValue:"Member sign in"})} | {()=>setMode('login')} | Not a native form submit or inherited from component context | [web/app/groups/[token]/[groupId]/page.tsx:38](../../../web/app/groups/[token]/[groupId]/page.tsx#L38) |
| UI-00310 | {busy?localize("ui.4660a983366f7101",{defaultValue:"Please wait…"}):mode==='join'?localize("ui.dc80ecbe94f9f27c",{defaultValue:"Request to join"}):localize("ui.bfd402b2f6f38125",{defaultValue:"Sign in"})} | submit / component action | {submit} | [web/app/groups/[token]/[groupId]/page.tsx:38](../../../web/app/groups/[token]/[groupId]/page.tsx#L38) |
| UI-00528 | Accept optional analytics | {()=>{saveConsent(true);setShow(false)}} | Not a native form submit or inherited from component context | [web/components/CookieNotice.tsx:5](../../../web/components/CookieNotice.tsx#L5) |
| UI-00529 | Reject optional analytics | {()=>{saveConsent(false);setShow(false)}} | Not a native form submit or inherited from component context | [web/components/CookieNotice.tsx:5](../../../web/components/CookieNotice.tsx#L5) |
| UI-00638 | {localize("ui.19e86d2b7055340c",{defaultValue:"Send verification code"})} | submit / component action | {start} | [web/components/GroupEmailVerification.tsx:19](../../../web/components/GroupEmailVerification.tsx#L19) |
| UI-00641 | {localize("ui.e89a77e7fe58a6d1",{defaultValue:"Verify email"})} | submit / component action | {finish} | [web/components/GroupEmailVerification.tsx:19](../../../web/components/GroupEmailVerification.tsx#L19) |
| UI-00652 | {localize("ui.e382a36729887241",{defaultValue:"Make another contribution"})} | {()=>{setStage('start');setReference('');setSubmissionKey(crypto.randomUUID());setDetails(null)}} | Not a native form submit or inherited from component context | [web/components/GroupGuestCheckout.tsx:29](../../../web/components/GroupGuestCheckout.tsx#L29) |
| UI-00653 | {localize("ui.4c363f056aaf6d63",{defaultValue:"Use suggestion"})} | {()=>setAmount(String(invite.suggested_contribution_paise!/100))} | Not a native form submit or inherited from component context | [web/components/GroupGuestCheckout.tsx:29](../../../web/components/GroupGuestCheckout.tsx#L29) |
| UI-00657 |  | {()=>void choosePayment(d.id)} | Not a native form submit or inherited from component context | [web/components/GroupGuestCheckout.tsx:29](../../../web/components/GroupGuestCheckout.tsx#L29) |
| UI-00659 | {details.kind==='UPI'?localize("ui.78f20d029371d3af",{defaultValue:"Show payment QR"}):localize("ui.00427c578c51e980",{defaultValue:"Show bank details"})} | {()=>setModal(true)} | {submit} | [web/components/GroupGuestCheckout.tsx:29](../../../web/components/GroupGuestCheckout.tsx#L29) |
| UI-00661 | {busy?localize("ui.49195f559e4a9f5b",{defaultValue:"Submitting…"}):localize("ui.6280d7748e32d3ac",{defaultValue:"I have done the payment"})} | submit / component action | {submit} | [web/components/GroupGuestCheckout.tsx:29](../../../web/components/GroupGuestCheckout.tsx#L29) |
| UI-00662 | {localize("ui.6e1b3d4c493ef1e2",{defaultValue:"Change payment method"})} | {()=>{setStage('start');setDetails(null);setSelected('');setReference('')}} | {submit} | [web/components/GroupGuestCheckout.tsx:29](../../../web/components/GroupGuestCheckout.tsx#L29) |
| UI-00663 | {localize("ui.0ab01a15c8871a3a",{defaultValue:"Copy bank details"})} | {()=>void trackedCopy(`${details.accountHolder}\n${details.accountNumber}\n${details.ifsc}`)} | Not a native form submit or inherited from component context | [web/components/GroupGuestCheckout.tsx:31](../../../web/components/GroupGuestCheckout.tsx#L31) |
| UI-00664 | {localize("ui.3866ccf908cc968d",{defaultValue:"Continue to reference details →"})} | {()=>setModal(false)} | Not a native form submit or inherited from component context | [web/components/GroupGuestCheckout.tsx:31](../../../web/components/GroupGuestCheckout.tsx#L31) |
| UI-00788 | {collapsed?<PanelLeftOpen size={20}/>:<PanelLeftClose size={20}/>} | {toggle} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:50](../../../web/components/HostWorkspace.tsx#L50) |
| UI-00791 |  | {()=>void signOut()} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:53](../../../web/components/HostWorkspace.tsx#L53) |
| UI-00792 |  | {()=>setCollapsed(true)} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:54](../../../web/components/HostWorkspace.tsx#L54) |
| UI-00876 | Cookie preferences | {()=>window.dispatchEvent(new Event('giftwise-cookie-settings'))} | Not a native form submit or inherited from component context | [web/components/LegalFooter.tsx:4](../../../web/components/LegalFooter.tsx#L4) |
| UI-00929 | {localize("ui.c76f29ae1543f068",{defaultValue:"Use a different mobile number"})} | {()=>{setVerified(false);setMessage('')}} | Not a native form submit or inherited from component context | [web/components/MobileVerification.tsx:17](../../../web/components/MobileVerification.tsx#L17) |
| UI-00933 | {busy?localize("ui.4660a983366f7101",{defaultValue:"Please wait…"}):challenge?localize("ui.6d8e8cd5ef890b1d",{defaultValue:"Verify mobile"}):localize("ui.19e86d2b7055340c",{defaultValue:"Send verification code"})} | submit / component action | {submit} | [web/components/MobileVerification.tsx:17](../../../web/components/MobileVerification.tsx#L17) |
| UI-00934 | {localize("ui.7fd05b1a433fd9c4",{defaultValue:"Change number / request another code"})} | {()=>{setChallenge('');setCode('');setMessage('Wait at least 60 seconds before requesting another code.')}} | {submit} | [web/components/MobileVerification.tsx:17](../../../web/components/MobileVerification.tsx#L17) |
| UI-01000 | {busy?localize("ui.4660a983366f7101",{defaultValue:"Please wait…"}):challenge?localize("ui.9200fc2f31ae11ab",{defaultValue:"Send a new code"}):localize("ui.66a5b4090d14cb41",{defaultValue:"Send code"})} | {()=>void send()} | Not a native form submit or inherited from component context | [web/components/SignupEmailVerification.tsx:21](../../../web/components/SignupEmailVerification.tsx#L21) |
| UI-01002 | {localize("ui.e89a77e7fe58a6d1",{defaultValue:"Verify email"})} | {()=>void verify()} | Not a native form submit or inherited from component context | [web/components/SignupEmailVerification.tsx:21](../../../web/components/SignupEmailVerification.tsx#L21) |


### Dialog and alert/status inventory

| ID | Role / kind | Text / label | Visibility conditions | Source |
| --- | --- | --- | --- | --- |
| UI-00072 | "status" | {localize("ui.bed9096c411e6da3",{defaultValue:"Opening your workspace…"})} | Parent/source state | [web/app/components/HostGlobalNav.tsx:17](../../../web/app/components/HostGlobalNav.tsx#L17) |
| UI-00291 | "status" | {localize("ui.451845cf34f2a4f4",{defaultValue:"Opening invitation…"})} | Parent/source state | [web/app/groups/[token]/[groupId]/page.tsx:35](../../../web/app/groups/[token]/[groupId]/page.tsx#L35) |
| UI-00311 | "status" | {localizeText(message)} | message; (mode==='member'\|\|(accessMode!=='GUESTS_ONLY'&&section==='membership')) | [web/app/groups/[token]/[groupId]/page.tsx:38](../../../web/app/groups/[token]/[groupId]/page.tsx#L38) |
| UI-00531 | "status" | {value?(remaining?localizeText(`${remaining} requirements still needed.`):localizeText("All requirements met.")):localizeText("Enter your "+kind+" to check the requirements.")} | Parent/source state | [web/components/CredentialRequirements.tsx:7](../../../web/components/CredentialRequirements.tsx#L7) |
| UI-00548 | "status" | {localize("ui.7a005f0f1dbc704f",{defaultValue:"Opening your celebration space…"})} | ready\|\|discovery | [web/components/ExperienceProvider.tsx:19](../../../web/components/ExperienceProvider.tsx#L19) |
| UI-00635 | "status" | {localize("ui.3850dde1a89614a2",{defaultValue:"✓ Email verified for account recovery"})} | Parent/source state | [web/components/GroupEmailVerification.tsx:18](../../../web/components/GroupEmailVerification.tsx#L18) |
| UI-00642 | "status" | {localizeText(notice)} | notice | [web/components/GroupEmailVerification.tsx:19](../../../web/components/GroupEmailVerification.tsx#L19) |
| UI-00650 | "alert" | {localizeText(error)} | error | [web/components/GroupGuestCheckout.tsx:28](../../../web/components/GroupGuestCheckout.tsx#L28) |
| UI-00651 | "status" |  | stage==='thanks'; !allowed; !invite | [web/components/GroupGuestCheckout.tsx:29](../../../web/components/GroupGuestCheckout.tsx#L29) |
| UI-00790 | "alert" | {localizeText(error)} | error | [web/components/HostWorkspace.tsx:53](../../../web/components/HostWorkspace.tsx#L53) |
| UI-00909 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:19](../../../web/components/LocalizationProvider.tsx#L19) |
| UI-00911 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:20](../../../web/components/LocalizationProvider.tsx#L20) |
| UI-00913 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:21](../../../web/components/LocalizationProvider.tsx#L21) |
| UI-00928 | "status" | {localize("ui.522a82b93e381483",{defaultValue:"Mobile number verified"})} | verified | [web/components/MobileVerification.tsx:17](../../../web/components/MobileVerification.tsx#L17) |
| UI-00935 | "status" | {localizeText(message)} | message | [web/components/MobileVerification.tsx:17](../../../web/components/MobileVerification.tsx#L17) |
| UI-00999 | "status" | {localize("ui.ab62c1ef5875569a",{defaultValue:"✓ Email verified"})} | verified | [web/components/SignupEmailVerification.tsx:21](../../../web/components/SignupEmailVerification.tsx#L21) |
| UI-01003 | "status" | {localizeText(message)} | message&&!verified | [web/components/SignupEmailVerification.tsx:21](../../../web/components/SignupEmailVerification.tsx#L21) |
| UI-01004 | "alert" | {localizeText(error)} | error | [web/components/SignupEmailVerification.tsx:21](../../../web/components/SignupEmailVerification.tsx#L21) |


### Workflow state bindings

| Source | Binding | Initial expression |
| --- | --- | --- |
| web/app/groups/[token]/[groupId]/page.tsx | busy | false |
| web/app/groups/[token]/[groupId]/page.tsx | section | 'contribute' |
| web/app/groups/[token]/[groupId]/page.tsx | message | '' |
| web/components/LocalizationProvider.tsx | busy | false |
| web/components/LocalizationProvider.tsx | error | '' |
| web/components/SignupEmailVerification.tsx | busy | false |
| web/components/SignupEmailVerification.tsx | message | '' |
| web/components/SignupEmailVerification.tsx | error | '' |
| web/components/GroupEmailVerification.tsx | notice | '' |
| web/components/GroupEmailVerification.tsx | busy | false |
| web/components/MobileVerification.tsx | busy | false |
| web/components/MobileVerification.tsx | message | '' |
| web/components/GroupGuestCheckout.tsx | busy | false |
| web/components/GroupGuestCheckout.tsx | error | '' |
| web/components/HostWorkspace.tsx | busy | false |
| web/components/HostWorkspace.tsx | error | '' |


## Host account

| Screen information | Source-supported specification |
| --- | --- |
| Route | /host/account |
| Source | [web/app/host/account/page.tsx:1](../../../web/app/host/account/page.tsx#L1) |
| Purpose | Profile name, contact/account verification, applicable member policy controls |
| User access level | Authenticated account owner; verification/policy gates |
| Navigation source | Workspace Account / verification links |
| Navigation destination | Saved scoped profile/contact state; safe recovery and account navigation |
| Preconditions | Route parameters/query scope must be valid; applicable session/ownership/module/provider state above. Load errors do not imply an empty successful dataset. |
| Postconditions | Only the selected successful action changes its documented state. Navigation alone does not publish, confirm payment, verify contact or deliver a notification. |
| Route variables | No path variable |
| UI variants | Focused/general experience, responsive layout and module/state predicates can change visible sections. Exact predicates are in each control record. |


### Wireframe layout

| Section | Placement / behavior / evidence |
| --- | --- |
| Header | Shared identity/event or page title/back/actions, where declared. Composite header declarations: 3. |
| Navigation / sidebar | Host/admin navigation and event context where applicable; mobile/focused variants are conditional. nav=3, aside=3. |
| Main content | Purpose-specific forms/cards/tables/canvas/player from the composed sources. main=3, section=8, article=0. |
| Search and filters | Fields/selects listed below; exact value/handler/visibility controls are not inferred from placeholder labels. |
| Data grids and cards | Table declarations=0. Data collections and generated rows/cards are in frontend.json dynamicSources. |
| Popups / dialogs | Named/source-declared dialog control records below; opening/closing/loading/apply/cancel actions belong to those component handlers. |
| Alerts / notifications | Source controls with role alert/status are listed below. Notice strings may come from API results; do not assume a universal envelope. |
| Footer | Local/shared footer declarations=1. No undocumented legal/footer wording is invented. |
| Loading / empty / success / error | Observed state bindings and relevant action fields are listed below. Expected QA states from Document 2 do not establish a spinner/retry control where source does not implement one. |


### Composed implementation

- [web/app/host/account/page.tsx:1](../../../web/app/host/account/page.tsx#L1)
- [web/components/ProfilePreferences.tsx:1](../../../web/components/ProfilePreferences.tsx#L1)
- [web/components/LocalizationProvider.tsx:1](../../../web/components/LocalizationProvider.tsx#L1)
- [web/components/GroupMemberEmailPolicy.tsx:1](../../../web/components/GroupMemberEmailPolicy.tsx#L1)
- [web/components/VerificationProvider.tsx:1](../../../web/components/VerificationProvider.tsx#L1)
- [web/components/MobileVerification.tsx:1](../../../web/components/MobileVerification.tsx#L1)
- [web/components/AccountEmailVerification.tsx:1](../../../web/components/AccountEmailVerification.tsx#L1)
- [web/app/layout.tsx:1](../../../web/app/layout.tsx#L1)
- [web/components/GrowthProvider.tsx:1](../../../web/components/GrowthProvider.tsx#L1)
- [web/components/BrandingProvider.tsx:1](../../../web/components/BrandingProvider.tsx#L1)
- [web/components/LegalFooter.tsx:1](../../../web/components/LegalFooter.tsx#L1)
- [web/components/CookieNotice.tsx:1](../../../web/components/CookieNotice.tsx#L1)
- [web/app/components/ApplicationInteractions.tsx:1](../../../web/app/components/ApplicationInteractions.tsx#L1)
- [web/components/ExperienceProvider.tsx:1](../../../web/components/ExperienceProvider.tsx#L1)
- [web/app/components/PerformanceDiagnostics.tsx:1](../../../web/app/components/PerformanceDiagnostics.tsx#L1)
- [web/app/components/HostSessionRedirect.tsx:1](../../../web/app/components/HostSessionRedirect.tsx#L1)
- [web/app/components/HostGlobalNav.tsx:1](../../../web/app/components/HostGlobalNav.tsx#L1)
- [web/components/HostWorkspace.tsx:1](../../../web/components/HostWorkspace.tsx#L1)
- [web/components/focused/HostShell.tsx:1](../../../web/components/focused/HostShell.tsx#L1)

### Fields

| Control ID | Label / expression | Type | Binding | Required | Native limits / format | Detailed specification |
| --- | --- | --- | --- | --- | --- | --- |
| UI-00313 | {localize("ui.f13a64ba2fea87ef",{defaultValue:"Full name"})} | input  | {name} | true | minLength={2}; maxLength={120} | [control](UI_CONTROLS.md#ui-00313) |
| UI-00419 | {localize("ui.f2488fd4ef4adbc6",{defaultValue:"Email address"})} | input "email" | {draftEmail} | true | maxLength={254} | [control](UI_CONTROLS.md#ui-00419) |
| UI-00424 | {localize("ui.3bcb66ee1a6feaf5",{defaultValue:"Six-digit code"})} | input  | {code} | true | maxLength={6}; pattern="[0-9]{6}" | [control](UI_CONTROLS.md#ui-00424) |
| UI-00673 | {localize("ui.7398a3f1d9ea36be",{defaultValue:" Require group members to verify email"})} | input "checkbox" | {!!(hostId?policy?.admin_enabled:policy?.host_enabled)} | Not declared |  | [control](UI_CONTROLS.md#ui-00673) |
| UI-00908 | {localize("ui.bba5b40bdd28c66e",{defaultValue:"Choose language"})} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00908) |
| UI-00910 | {label} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00910) |
| UI-00912 | {localize("ui.140071feea59fe87",{defaultValue:"Preferred language"})} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00912) |
| UI-00931 | {localize("ui.dae5cf30549f072d",{defaultValue:"Mobile number with country code"})} | input "tel" | {mobile} | true | pattern="\+[1-9][0-9]{7,14}" | [control](UI_CONTROLS.md#ui-00931) |
| UI-00932 | {localize("ui.f673a246d45f4b8f",{defaultValue:"Six-digit verification code"})} | input  | {code} | true | maxLength={6}; pattern="[0-9]{6}" | [control](UI_CONTROLS.md#ui-00932) |
| UI-00969 | Upload profile picture | input "file" |  | Not declared |  | [control](UI_CONTROLS.md#ui-00969) |
| UI-00973 | Time zone | select  | {form.timezone} | Not declared |  | [control](UI_CONTROLS.md#ui-00973) |
| UI-00974 | {label} | input "checkbox" | {form[key]} | Not declared |  | [control](UI_CONTROLS.md#ui-00974) |


### Buttons

| ID | Label / expression | Action | Submit validation / handler | Source |
| --- | --- | --- | --- | --- |
| UI-00314 | {busy?localize("ui.23e39291d6135814",{defaultValue:"Saving…"}):localize("ui.b7297226fd1f4681",{defaultValue:"Save name"})} | submit / component action | {save} | [web/app/host/account/page.tsx:23](../../../web/app/host/account/page.tsx#L23) |
| UI-00317 | {localize("ui.48f0d3d397d49f13",{defaultValue:"Sign out"})} | {()=>void signOut()} | Not a native form submit or inherited from component context | [web/app/host/account/page.tsx:23](../../../web/app/host/account/page.tsx#L23) |
| UI-00420 | {busy?localize("ui.23e39291d6135814",{defaultValue:"Saving…"}):localize("ui.99583e152ccf6a58",{defaultValue:"Save email"})} | submit / component action | {updateEmail} | [web/components/AccountEmailVerification.tsx:19](../../../web/components/AccountEmailVerification.tsx#L19) |
| UI-00421 | {localize("ui.19766ed6ccb2f4a3",{defaultValue:"Cancel"})} | {()=>setEditing(false)} | {updateEmail} | [web/components/AccountEmailVerification.tsx:19](../../../web/components/AccountEmailVerification.tsx#L19) |
| UI-00422 | {localize("ui.c81c8805cb1148e6",{defaultValue:"Update email address"})} | {()=>{setDraftEmail(email);setEditing(true);setError('');setMessage('')}} | Not a native form submit or inherited from component context | [web/components/AccountEmailVerification.tsx:19](../../../web/components/AccountEmailVerification.tsx#L19) |
| UI-00425 | {busy?localize("ui.63bbd08c916b4e99",{defaultValue:"Verifying…"}):localize("ui.e89a77e7fe58a6d1",{defaultValue:"Verify email"})} | submit / component action | {finish} | [web/components/AccountEmailVerification.tsx:19](../../../web/components/AccountEmailVerification.tsx#L19) |
| UI-00426 | {busy?localize("ui.b8ed5279e897be5d",{defaultValue:"Sending…"}):localize("ui.19e86d2b7055340c",{defaultValue:"Send verification code"})} | {()=>void send()} | Not a native form submit or inherited from component context | [web/components/AccountEmailVerification.tsx:19](../../../web/components/AccountEmailVerification.tsx#L19) |
| UI-00429 | {localize("ui.0e91610117029a62",{defaultValue:"Refresh"})} | {()=>setRetry(v=>v+1)} | Not a native form submit or inherited from component context | [web/components/AccountEmailVerification.tsx:19](../../../web/components/AccountEmailVerification.tsx#L19) |
| UI-00528 | Accept optional analytics | {()=>{saveConsent(true);setShow(false)}} | Not a native form submit or inherited from component context | [web/components/CookieNotice.tsx:5](../../../web/components/CookieNotice.tsx#L5) |
| UI-00529 | Reject optional analytics | {()=>{saveConsent(false);setShow(false)}} | Not a native form submit or inherited from component context | [web/components/CookieNotice.tsx:5](../../../web/components/CookieNotice.tsx#L5) |
| UI-00788 | {collapsed?<PanelLeftOpen size={20}/>:<PanelLeftClose size={20}/>} | {toggle} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:50](../../../web/components/HostWorkspace.tsx#L50) |
| UI-00791 |  | {()=>void signOut()} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:53](../../../web/components/HostWorkspace.tsx#L53) |
| UI-00792 |  | {()=>setCollapsed(true)} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:54](../../../web/components/HostWorkspace.tsx#L54) |
| UI-00876 | Cookie preferences | {()=>window.dispatchEvent(new Event('giftwise-cookie-settings'))} | Not a native form submit or inherited from component context | [web/components/LegalFooter.tsx:4](../../../web/components/LegalFooter.tsx#L4) |
| UI-00929 | {localize("ui.c76f29ae1543f068",{defaultValue:"Use a different mobile number"})} | {()=>{setVerified(false);setMessage('')}} | Not a native form submit or inherited from component context | [web/components/MobileVerification.tsx:17](../../../web/components/MobileVerification.tsx#L17) |
| UI-00933 | {busy?localize("ui.4660a983366f7101",{defaultValue:"Please wait…"}):challenge?localize("ui.6d8e8cd5ef890b1d",{defaultValue:"Verify mobile"}):localize("ui.19e86d2b7055340c",{defaultValue:"Send verification code"})} | submit / component action | {submit} | [web/components/MobileVerification.tsx:17](../../../web/components/MobileVerification.tsx#L17) |
| UI-00934 | {localize("ui.7fd05b1a433fd9c4",{defaultValue:"Change number / request another code"})} | {()=>{setChallenge('');setCode('');setMessage('Wait at least 60 seconds before requesting another code.')}} | {submit} | [web/components/MobileVerification.tsx:17](../../../web/components/MobileVerification.tsx#L17) |
| UI-00970 | Remove picture | {()=>void run(()=>request('/picture',{method:'DELETE'}),'Profile picture removed.')} | Not a native form submit or inherited from component context | [web/components/ProfilePreferences.tsx:8](../../../web/components/ProfilePreferences.tsx#L8) |
| UI-00975 | {busy?'Saving…':'Save preferences'} | submit / component action | {e=>{e.preventDefault();void run(()=>request('',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(form)}),'Preferences saved successfully.')}} | [web/components/ProfilePreferences.tsx:8](../../../web/components/ProfilePreferences.tsx#L8) |


### Dialog and alert/status inventory

| ID | Role / kind | Text / label | Visibility conditions | Source |
| --- | --- | --- | --- | --- |
| UI-00072 | "status" | {localize("ui.bed9096c411e6da3",{defaultValue:"Opening your workspace…"})} | Parent/source state | [web/app/components/HostGlobalNav.tsx:17](../../../web/app/components/HostGlobalNav.tsx#L17) |
| UI-00318 | "status" | {localizeText(message)} | message | [web/app/host/account/page.tsx:23](../../../web/app/host/account/page.tsx#L23) |
| UI-00416 | "status" | {localize("ui.5c92717e04c64d34",{defaultValue:"Checking your email…"})} | loading | [web/components/AccountEmailVerification.tsx:19](../../../web/components/AccountEmailVerification.tsx#L19) |
| UI-00417 | "status" | {localize("ui.2487c43aa0f7620b",{defaultValue:"✓ Email verified for account recovery. Verified email addresses cannot be changed."})} | verified; loading | [web/components/AccountEmailVerification.tsx:19](../../../web/components/AccountEmailVerification.tsx#L19) |
| UI-00427 | "status" | {localizeText(message)} | message | [web/components/AccountEmailVerification.tsx:19](../../../web/components/AccountEmailVerification.tsx#L19) |
| UI-00428 | "alert" | {localizeText(error)} | error | [web/components/AccountEmailVerification.tsx:19](../../../web/components/AccountEmailVerification.tsx#L19) |
| UI-00548 | "status" | {localize("ui.7a005f0f1dbc704f",{defaultValue:"Opening your celebration space…"})} | ready\|\|discovery | [web/components/ExperienceProvider.tsx:19](../../../web/components/ExperienceProvider.tsx#L19) |
| UI-00674 | "alert" | {localizeText(error)} | error | [web/components/GroupMemberEmailPolicy.tsx:15](../../../web/components/GroupMemberEmailPolicy.tsx#L15) |
| UI-00790 | "alert" | {localizeText(error)} | error | [web/components/HostWorkspace.tsx:53](../../../web/components/HostWorkspace.tsx#L53) |
| UI-00909 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:19](../../../web/components/LocalizationProvider.tsx#L19) |
| UI-00911 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:20](../../../web/components/LocalizationProvider.tsx#L20) |
| UI-00913 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:21](../../../web/components/LocalizationProvider.tsx#L21) |
| UI-00928 | "status" | {localize("ui.522a82b93e381483",{defaultValue:"Mobile number verified"})} | verified | [web/components/MobileVerification.tsx:17](../../../web/components/MobileVerification.tsx#L17) |
| UI-00935 | "status" | {localizeText(message)} | message | [web/components/MobileVerification.tsx:17](../../../web/components/MobileVerification.tsx#L17) |
| UI-00966 | "alert" | {error} | error | [web/components/ProfilePreferences.tsx:8](../../../web/components/ProfilePreferences.tsx#L8) |
| UI-00967 | "status" | Loading profile preferences… | !form | [web/components/ProfilePreferences.tsx:8](../../../web/components/ProfilePreferences.tsx#L8) |
| UI-00976 | "status" | {message} | message | [web/components/ProfilePreferences.tsx:8](../../../web/components/ProfilePreferences.tsx#L8) |


### Workflow state bindings

| Source | Binding | Initial expression |
| --- | --- | --- |
| web/app/host/account/page.tsx | message | '' |
| web/app/host/account/page.tsx | busy | false |
| web/components/ProfilePreferences.tsx | busy | false |
| web/components/ProfilePreferences.tsx | message | '' |
| web/components/ProfilePreferences.tsx | error | '' |
| web/components/LocalizationProvider.tsx | busy | false |
| web/components/LocalizationProvider.tsx | error | '' |
| web/components/GroupMemberEmailPolicy.tsx | error | '' |
| web/components/GroupMemberEmailPolicy.tsx | busy | false |
| web/components/MobileVerification.tsx | busy | false |
| web/components/MobileVerification.tsx | message | '' |
| web/components/AccountEmailVerification.tsx | loading | true |
| web/components/AccountEmailVerification.tsx | busy | false |
| web/components/AccountEmailVerification.tsx | message | '' |
| web/components/AccountEmailVerification.tsx | error | '' |
| web/components/HostWorkspace.tsx | busy | false |
| web/components/HostWorkspace.tsx | error | '' |


## Plans and partnerships

| Screen information | Source-supported specification |
| --- | --- |
| Route | /host/monetization |
| Source | [web/app/host/monetization/page.tsx:1](../../../web/app/host/monetization/page.tsx#L1) |
| Purpose | Offers, plan/premium access, own placements and purchase history |
| User access level | Authenticated purchaser/placement owner; commercial stream gates |
| Navigation source | Workspace Plans & partnerships / premium offer |
| Navigation destination | Reviewed checkout/confirmation/free activation or placement submission/history |
| Preconditions | Route parameters/query scope must be valid; applicable session/ownership/module/provider state above. Load errors do not imply an empty successful dataset. |
| Postconditions | Only the selected successful action changes its documented state. Navigation alone does not publish, confirm payment, verify contact or deliver a notification. |
| Route variables | No path variable |
| UI variants | Focused/general experience, responsive layout and module/state predicates can change visible sections. Exact predicates are in each control record. |


### Wireframe layout

| Section | Placement / behavior / evidence |
| --- | --- |
| Header | Shared identity/event or page title/back/actions, where declared. Composite header declarations: 2. |
| Navigation / sidebar | Host/admin navigation and event context where applicable; mobile/focused variants are conditional. nav=3, aside=3. |
| Main content | Purpose-specific forms/cards/tables/canvas/player from the composed sources. main=3, section=7, article=2. |
| Search and filters | Fields/selects listed below; exact value/handler/visibility controls are not inferred from placeholder labels. |
| Data grids and cards | Table declarations=1. Data collections and generated rows/cards are in frontend.json dynamicSources. |
| Popups / dialogs | Named/source-declared dialog control records below; opening/closing/loading/apply/cancel actions belong to those component handlers. |
| Alerts / notifications | Source controls with role alert/status are listed below. Notice strings may come from API results; do not assume a universal envelope. |
| Footer | Local/shared footer declarations=1. No undocumented legal/footer wording is invented. |
| Loading / empty / success / error | Observed state bindings and relevant action fields are listed below. Expected QA states from Document 2 do not establish a spinner/retry control where source does not implement one. |


### Composed implementation

- [web/app/host/monetization/page.tsx:1](../../../web/app/host/monetization/page.tsx#L1)
- [web/components/HostCommerce.tsx:1](../../../web/components/HostCommerce.tsx#L1)
- [web/components/LocalizationProvider.tsx:1](../../../web/components/LocalizationProvider.tsx#L1)
- [web/components/CommercialAnalytics.tsx:1](../../../web/components/CommercialAnalytics.tsx#L1)
- [web/components/CommercePurchase.tsx:1](../../../web/components/CommercePurchase.tsx#L1)
- [web/app/layout.tsx:1](../../../web/app/layout.tsx#L1)
- [web/components/GrowthProvider.tsx:1](../../../web/components/GrowthProvider.tsx#L1)
- [web/components/BrandingProvider.tsx:1](../../../web/components/BrandingProvider.tsx#L1)
- [web/components/LegalFooter.tsx:1](../../../web/components/LegalFooter.tsx#L1)
- [web/components/CookieNotice.tsx:1](../../../web/components/CookieNotice.tsx#L1)
- [web/app/components/ApplicationInteractions.tsx:1](../../../web/app/components/ApplicationInteractions.tsx#L1)
- [web/components/VerificationProvider.tsx:1](../../../web/components/VerificationProvider.tsx#L1)
- [web/components/ExperienceProvider.tsx:1](../../../web/components/ExperienceProvider.tsx#L1)
- [web/app/components/PerformanceDiagnostics.tsx:1](../../../web/app/components/PerformanceDiagnostics.tsx#L1)
- [web/app/components/HostSessionRedirect.tsx:1](../../../web/app/components/HostSessionRedirect.tsx#L1)
- [web/app/components/HostGlobalNav.tsx:1](../../../web/app/components/HostGlobalNav.tsx#L1)
- [web/components/HostWorkspace.tsx:1](../../../web/components/HostWorkspace.tsx#L1)
- [web/components/focused/HostShell.tsx:1](../../../web/components/focused/HostShell.tsx#L1)

### Fields

| Control ID | Label / expression | Type | Binding | Required | Native limits / format | Detailed specification |
| --- | --- | --- | --- | --- | --- | --- |
| UI-00524 | {localize("ui.4e1f49a9c8ae8a15",{defaultValue:"Event"})} | select  | {selected} | Not declared |  | [control](UI_CONTROLS.md#ui-00524) |
| UI-00773 | {localize("ui.ee2d91c2033c3144",{defaultValue:"Placement type"})} | select  | {form.stream} | Not declared |  | [control](UI_CONTROLS.md#ui-00773) |
| UI-00774 | {k} | input {k==='url'?'url':'text'} | {form[k]} | true | maxLength={k==='description'?1000:k==='url'?2048:k==='title'?160:k==='category'?80:50} | [control](UI_CONTROLS.md#ui-00774) |
| UI-00908 | {localize("ui.bba5b40bdd28c66e",{defaultValue:"Choose language"})} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00908) |
| UI-00910 | {label} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00910) |
| UI-00912 | {localize("ui.140071feea59fe87",{defaultValue:"Preferred language"})} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00912) |


### Buttons

| ID | Label / expression | Action | Submit validation / handler | Source |
| --- | --- | --- | --- | --- |
| UI-00497 | {busy?localize("ui.42074396a441a347",{defaultValue:"Processing…"}):offer.amountPaise===0?localize("ui.850fbbaefb4057d0",{defaultValue:"Activate access"}):localize("ui.0e3afe9550a3bde6",{defaultValue:"{{value0}} · {{value1}} (test mode)",value0:(offer.entitled?'Renew':'Purchase'),value1:(commercialMoney(offer.amountPaise))})} | {()=>void pay()} | Not a native form submit or inherited from component context | [web/components/CommercePurchase.tsx:34](../../../web/components/CommercePurchase.tsx#L34) |
| UI-00499 | {localize("ui.087a9cc69283c135",{defaultValue:"Reload price"})} | {()=>{setRetry(v=>v+1)}} | Not a native form submit or inherited from component context | [web/components/CommercePurchase.tsx:34](../../../web/components/CommercePurchase.tsx#L34) |
| UI-00525 | {busy?localize("ui.ba3bbbe10d8bef66",{defaultValue:"Loading…"}):localize("ui.eb8e0a4b8ee292e0",{defaultValue:"Load analytics"})} | {()=>void load()} | Not a native form submit or inherited from component context | [web/components/CommercialAnalytics.tsx:14](../../../web/components/CommercialAnalytics.tsx#L14) |
| UI-00528 | Accept optional analytics | {()=>{saveConsent(true);setShow(false)}} | Not a native form submit or inherited from component context | [web/components/CookieNotice.tsx:5](../../../web/components/CookieNotice.tsx#L5) |
| UI-00529 | Reject optional analytics | {()=>{saveConsent(false);setShow(false)}} | Not a native form submit or inherited from component context | [web/components/CookieNotice.tsx:5](../../../web/components/CookieNotice.tsx#L5) |
| UI-00775 | {localize("ui.beec6c108c0009ae",{defaultValue:"Submit for approval"})} | submit / component action | {submit} | [web/components/HostCommerce.tsx:20](../../../web/components/HostCommerce.tsx#L20) |
| UI-00788 | {collapsed?<PanelLeftOpen size={20}/>:<PanelLeftClose size={20}/>} | {toggle} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:50](../../../web/components/HostWorkspace.tsx#L50) |
| UI-00791 |  | {()=>void signOut()} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:53](../../../web/components/HostWorkspace.tsx#L53) |
| UI-00792 |  | {()=>setCollapsed(true)} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:54](../../../web/components/HostWorkspace.tsx#L54) |
| UI-00876 | Cookie preferences | {()=>window.dispatchEvent(new Event('giftwise-cookie-settings'))} | Not a native form submit or inherited from component context | [web/components/LegalFooter.tsx:4](../../../web/components/LegalFooter.tsx#L4) |


### Dialog and alert/status inventory

| ID | Role / kind | Text / label | Visibility conditions | Source |
| --- | --- | --- | --- | --- |
| UI-00072 | "status" | {localize("ui.bed9096c411e6da3",{defaultValue:"Opening your workspace…"})} | Parent/source state | [web/app/components/HostGlobalNav.tsx:17](../../../web/app/components/HostGlobalNav.tsx#L17) |
| UI-00498 | "alert" | {localizeText(error)} | error; groupBase&&error.includes('switched off') | [web/components/CommercePurchase.tsx:34](../../../web/components/CommercePurchase.tsx#L34) |
| UI-00500 | "status" | {localizeText(notice)} | notice | [web/components/CommercePurchase.tsx:34](../../../web/components/CommercePurchase.tsx#L34) |
| UI-00526 | "alert" | {localizeText(error)} | error | [web/components/CommercialAnalytics.tsx:14](../../../web/components/CommercialAnalytics.tsx#L14) |
| UI-00548 | "status" | {localize("ui.7a005f0f1dbc704f",{defaultValue:"Opening your celebration space…"})} | ready\|\|discovery | [web/components/ExperienceProvider.tsx:19](../../../web/components/ExperienceProvider.tsx#L19) |
| UI-00769 | "alert" | {localizeText(error)} | error | [web/components/HostCommerce.tsx:20](../../../web/components/HostCommerce.tsx#L20) |
| UI-00790 | "alert" | {localizeText(error)} | error | [web/components/HostWorkspace.tsx:53](../../../web/components/HostWorkspace.tsx#L53) |
| UI-00909 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:19](../../../web/components/LocalizationProvider.tsx#L19) |
| UI-00911 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:20](../../../web/components/LocalizationProvider.tsx#L20) |
| UI-00913 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:21](../../../web/components/LocalizationProvider.tsx#L21) |


### Workflow state bindings

| Source | Binding | Initial expression |
| --- | --- | --- |
| web/components/HostCommerce.tsx | error | '' |
| web/components/HostCommerce.tsx | busy | false |
| web/components/LocalizationProvider.tsx | busy | false |
| web/components/LocalizationProvider.tsx | error | '' |
| web/components/CommercialAnalytics.tsx | error | '' |
| web/components/CommercialAnalytics.tsx | busy | false |
| web/components/CommercePurchase.tsx | error | '' |
| web/components/CommercePurchase.tsx | busy | false |
| web/components/CommercePurchase.tsx | notice | '' |
| web/components/HostWorkspace.tsx | busy | false |
| web/components/HostWorkspace.tsx | error | '' |


## Host receiving accounts

| Screen information | Source-supported specification |
| --- | --- |
| Route | /host/payment-settings |
| Source | [web/app/host/payment-settings/page.tsx:1](../../../web/app/host/payment-settings/page.tsx#L1) |
| Purpose | Manage own encrypted UPI/bank profiles, QR, verification and event linking |
| User access level | Authenticated CUSTOMER; payment profile feature gate |
| Navigation source | Workspace Receiving accounts / event account route |
| Navigation destination | Save/enable/disable/archive/rename/link/prefer as allowed; return to event |
| Preconditions | Route parameters/query scope must be valid; applicable session/ownership/module/provider state above. Load errors do not imply an empty successful dataset. |
| Postconditions | Only the selected successful action changes its documented state. Navigation alone does not publish, confirm payment, verify contact or deliver a notification. |
| Route variables | No path variable |
| UI variants | Focused/general experience, responsive layout and module/state predicates can change visible sections. Exact predicates are in each control record. |


### Wireframe layout

| Section | Placement / behavior / evidence |
| --- | --- |
| Header | Shared identity/event or page title/back/actions, where declared. Composite header declarations: 3. |
| Navigation / sidebar | Host/admin navigation and event context where applicable; mobile/focused variants are conditional. nav=3, aside=3. |
| Main content | Purpose-specific forms/cards/tables/canvas/player from the composed sources. main=3, section=9, article=2. |
| Search and filters | Fields/selects listed below; exact value/handler/visibility controls are not inferred from placeholder labels. |
| Data grids and cards | Table declarations=0. Data collections and generated rows/cards are in frontend.json dynamicSources. |
| Popups / dialogs | Named/source-declared dialog control records below; opening/closing/loading/apply/cancel actions belong to those component handlers. |
| Alerts / notifications | Source controls with role alert/status are listed below. Notice strings may come from API results; do not assume a universal envelope. |
| Footer | Local/shared footer declarations=1. No undocumented legal/footer wording is invented. |
| Loading / empty / success / error | Observed state bindings and relevant action fields are listed below. Expected QA states from Document 2 do not establish a spinner/retry control where source does not implement one. |


### Composed implementation

- [web/app/host/payment-settings/page.tsx:1](../../../web/app/host/payment-settings/page.tsx#L1)
- [web/components/LocalizationProvider.tsx:1](../../../web/components/LocalizationProvider.tsx#L1)
- [web/components/ReceivingAccountHealth.tsx:1](../../../web/components/ReceivingAccountHealth.tsx#L1)
- [web/components/ExperienceProvider.tsx:1](../../../web/components/ExperienceProvider.tsx#L1)
- [web/app/layout.tsx:1](../../../web/app/layout.tsx#L1)
- [web/components/GrowthProvider.tsx:1](../../../web/components/GrowthProvider.tsx#L1)
- [web/components/BrandingProvider.tsx:1](../../../web/components/BrandingProvider.tsx#L1)
- [web/components/LegalFooter.tsx:1](../../../web/components/LegalFooter.tsx#L1)
- [web/components/CookieNotice.tsx:1](../../../web/components/CookieNotice.tsx#L1)
- [web/app/components/ApplicationInteractions.tsx:1](../../../web/app/components/ApplicationInteractions.tsx#L1)
- [web/components/VerificationProvider.tsx:1](../../../web/components/VerificationProvider.tsx#L1)
- [web/app/components/PerformanceDiagnostics.tsx:1](../../../web/app/components/PerformanceDiagnostics.tsx#L1)
- [web/app/components/HostSessionRedirect.tsx:1](../../../web/app/components/HostSessionRedirect.tsx#L1)
- [web/app/components/HostGlobalNav.tsx:1](../../../web/app/components/HostGlobalNav.tsx#L1)
- [web/components/HostWorkspace.tsx:1](../../../web/components/HostWorkspace.tsx#L1)
- [web/components/focused/HostShell.tsx:1](../../../web/components/focused/HostShell.tsx#L1)

### Fields

| Control ID | Label / expression | Type | Binding | Required | Native limits / format | Detailed specification |
| --- | --- | --- | --- | --- | --- | --- |
| UI-00331 | {localize("ui.6bf6133a3bfda168",{defaultValue:"Host receiving account"})} | select  | {existingAccount} | true |  | [control](UI_CONTROLS.md#ui-00331) |
| UI-00334 | {localize("ui.107285bae04647b6",{defaultValue:"Related host or event"})} | select  | {upiEvent} | Not declared |  | [control](UI_CONTROLS.md#ui-00334) |
| UI-00335 | {localize("ui.21ac0fa367d84202",{defaultValue:"Account name / tag (optional)"})} | input  | {upiLabel} | Not declared | maxLength={50} | [control](UI_CONTROLS.md#ui-00335) |
| UI-00336 | {localize("ui.cec4a9c0b1e71189",{defaultValue:"UPI ID"})} | input  | {upi} | true |  | [control](UI_CONTROLS.md#ui-00336) |
| UI-00347 | {localize("ui.d303920dee3450a5",{defaultValue:"Upload UPI QR PNG"})} | input "file" |  | Not declared |  | [control](UI_CONTROLS.md#ui-00347) |
| UI-00353 | {localize("ui.107285bae04647b6",{defaultValue:"Related host or event"})} | select  | {bankEvent} | Not declared |  | [control](UI_CONTROLS.md#ui-00353) |
| UI-00354 | {localize("ui.21ac0fa367d84202",{defaultValue:"Account name / tag (optional)"})} | input  | {bankLabel} | Not declared | maxLength={50} | [control](UI_CONTROLS.md#ui-00354) |
| UI-00355 | {localize("ui.5596842fc1e511ec",{defaultValue:"Account holder name"})} | input  | {holder} | true |  | [control](UI_CONTROLS.md#ui-00355) |
| UI-00356 | {localize("ui.f7573b7f5db0b550",{defaultValue:"Account number"})} | input "password" | {number} | true |  | [control](UI_CONTROLS.md#ui-00356) |
| UI-00357 | {localize("ui.f3d8c96e71944625",{defaultValue:"IFSC"})} | input  | {ifsc} | true | maxLength={11} | [control](UI_CONTROLS.md#ui-00357) |
| UI-00908 | {localize("ui.bba5b40bdd28c66e",{defaultValue:"Choose language"})} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00908) |
| UI-00910 | {label} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00910) |
| UI-00912 | {localize("ui.140071feea59fe87",{defaultValue:"Preferred language"})} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00912) |


### Buttons

| ID | Label / expression | Action | Submit validation / handler | Source |
| --- | --- | --- | --- | --- |
| UI-00325 | × | {()=>setError('')} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:69](../../../web/app/host/payment-settings/page.tsx#L69) |
| UI-00327 | × | {()=>setNotice('')} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:69](../../../web/app/host/payment-settings/page.tsx#L69) |
| UI-00329 | {localize("ui.7ce64141b163cd0d",{defaultValue:" Refresh"})} | {()=>void load()} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:70](../../../web/app/host/payment-settings/page.tsx#L70) |
| UI-00332 | {localize("ui.73d3125789193ff1",{defaultValue:"Link to this event"})} | submit / component action | {e=>{e.preventDefault();const [kind,id]=existingAccount.split(':');if(id)void action(async()=>{await paymentProfiles.linkToEvent(authFetch,eventId,kind,id);setExistingAccount('')},'Account linked to this event. Select Preferred to make it ready for publishing.')}} | [web/app/host/payment-settings/page.tsx:71](../../../web/app/host/payment-settings/page.tsx#L71) |
| UI-00337 | {localize("ui.f7f91a76b26e1579",{defaultValue:" Add UPI ID"})} | submit / component action | {e=>{e.preventDefault();void action(async()=>{await paymentProfiles.addUpi(authFetch,upi,undefined,upiLabel,upiEvent);setUpi('');setUpiLabel('');},'UPI ID saved. Upload or generate its QR, then confirm your details.');}} | [web/app/host/payment-settings/page.tsx:71](../../../web/app/host/payment-settings/page.tsx#L71) |
| UI-00338 | {localize("ui.df5c1bee560a14ad",{defaultValue:"Verify "})} {verification?.environment==='SANDBOX'?localize("ui.bb80a42e6fedc731",{defaultValue:"in sandbox"}):localize("ui.9af211329b2fc82e",{defaultValue:"account"})} | {()=>void verifyAccount(u.id,'upi')} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:71](../../../web/app/host/payment-settings/page.tsx#L71) |
| UI-00339 | {localize("ui.b92e4b4fd541d9cc",{defaultValue:"Verification details"})} | {()=>void verificationHistory(u.id,'upi')} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:71](../../../web/app/host/payment-settings/page.tsx#L71) |
| UI-00341 | {localize("ui.dc53bdac523a8cb5",{defaultValue:"Confirm my details"})} | {()=>void confirmDestination(u.id,'upi')} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:71](../../../web/app/host/payment-settings/page.tsx#L71) |
| UI-00342 | {u.preferred\|\|u.isPreferred?localize("ui.7dd2dc467c1ca5f7",{defaultValue:"✓ Preferred"}):!u.qrReady?localize("ui.95672cecf0b961e9",{defaultValue:"QR required"}):(u.verificationStatus==='VERIFIED'\|\|u.hostConfirmed)?localize("ui.0428680794e8223a",{defaultValue:"Set preferred"}):localize("ui.cf0b7ae423a9b52e",{defaultValue:"Confirm details first"})} | {()=>void action(()=>paymentProfiles.preferUpi(authFetch,u.id,undefined,eventId),'Preferred destination updated.')} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:71](../../../web/app/host/payment-settings/page.tsx#L71) |
| UI-00343 | {localize("ui.5342e09f2729fbc6",{defaultValue:"Enable"})} | {()=>void action(()=>paymentProfiles.enableUpi(authFetch,u.id,undefined,eventId),'UPI ID enabled. Independent verification is still required.')} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:71](../../../web/app/host/payment-settings/page.tsx#L71) |
| UI-00344 | {localize("ui.b7e3e4aa4257b9a1",{defaultValue:"Disable"})} | {()=>void action(()=>paymentProfiles.disableUpi(authFetch,u.id,undefined,eventId),'UPI ID disabled.')} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:71](../../../web/app/host/payment-settings/page.tsx#L71) |
| UI-00345 | {localize("ui.27291c678160fb61",{defaultValue:" Generate QR"})} | {()=>void viewQr(u.id,'generate')} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:71](../../../web/app/host/payment-settings/page.tsx#L71) |
| UI-00346 | {localize("ui.5400d8e48024d5b2",{defaultValue:"View uploaded QR"})} | {()=>void viewQr(u.id,'uploaded')} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:71](../../../web/app/host/payment-settings/page.tsx#L71) |
| UI-00348 | {localize("ui.3064d79a295cefe4",{defaultValue:"Rename"})} | {()=>{const value=window.prompt('Account name / tag (maximum 50 characters)',u.accountLabel\|\|'');if(value!==null)void action(()=>paymentProfiles.rename(authFetch,'upi',u.id,value),'UPI account name updated.');}} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:71](../../../web/app/host/payment-settings/page.tsx#L71) |
| UI-00350 | {eventId?localize("ui.7636631d120ebe99",{defaultValue:"Remove from event"}):localize("ui.e2d0a54968ead24e",{defaultValue:"Delete"})} | {()=>{if(window.confirm(eventId?'Remove this account from this event? Shared host accounts will remain saved.':`Archive ${u.accountLabel\|\|u.masked\|\|'this UPI ID'}? It will disappear from payment choices. Historical records will be retained.`))void action(()=>paymentProfiles.archive(authFetch,'upi',u.id,undefined,eventId),eventId?'Account removed from this event.':'UPI account archived.');}} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:71](../../../web/app/host/payment-settings/page.tsx#L71) |
| UI-00358 | {localize("ui.c83b83d985d434f5",{defaultValue:" Add bank account"})} | submit / component action | {e=>{e.preventDefault();void action(async()=>{await paymentProfiles.addBank(authFetch,{accountNumber:number,ifsc,accountHolderName:holder,accountLabel:bankLabel,eventId:bankEvent\|\|null});setBankLabel('');setNumber('');setIfsc('');setHolder('');},'Bank account saved. Confirm the details; independent verification is required before guest use.');}} | [web/app/host/payment-settings/page.tsx:72](../../../web/app/host/payment-settings/page.tsx#L72) |
| UI-00359 | {localize("ui.df5c1bee560a14ad",{defaultValue:"Verify "})} {verification?.environment==='SANDBOX'?localize("ui.bb80a42e6fedc731",{defaultValue:"in sandbox"}):localize("ui.9af211329b2fc82e",{defaultValue:"account"})} | {()=>void verifyAccount(b.id,'bank')} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:72](../../../web/app/host/payment-settings/page.tsx#L72) |
| UI-00360 | {localize("ui.b92e4b4fd541d9cc",{defaultValue:"Verification details"})} | {()=>void verificationHistory(b.id,'bank')} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:72](../../../web/app/host/payment-settings/page.tsx#L72) |
| UI-00361 | {localize("ui.dc53bdac523a8cb5",{defaultValue:"Confirm my details"})} | {()=>void confirmDestination(b.id,'banks')} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:72](../../../web/app/host/payment-settings/page.tsx#L72) |
| UI-00362 | {b.preferred\|\|b.isPreferred?localize("ui.7dd2dc467c1ca5f7",{defaultValue:"✓ Preferred"}):(b.verificationStatus==='VERIFIED'\|\|b.hostConfirmed)?localize("ui.0428680794e8223a",{defaultValue:"Set preferred"}):localize("ui.cf0b7ae423a9b52e",{defaultValue:"Confirm details first"})} | {()=>void action(()=>paymentProfiles.preferBank(authFetch,b.id,undefined,eventId),'Preferred destination updated.')} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:72](../../../web/app/host/payment-settings/page.tsx#L72) |
| UI-00363 | {localize("ui.5342e09f2729fbc6",{defaultValue:"Enable"})} | {()=>void action(()=>paymentProfiles.enableBank(authFetch,b.id,undefined,eventId),'Bank account enabled. Independent verification is still required.')} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:72](../../../web/app/host/payment-settings/page.tsx#L72) |
| UI-00364 | {localize("ui.b7e3e4aa4257b9a1",{defaultValue:"Disable"})} | {()=>void action(()=>paymentProfiles.disableBank(authFetch,b.id,undefined,eventId),'Bank account disabled.')} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:72](../../../web/app/host/payment-settings/page.tsx#L72) |
| UI-00365 | {localize("ui.3064d79a295cefe4",{defaultValue:"Rename"})} | {()=>{const value=window.prompt('Account name / tag (maximum 50 characters)',b.accountLabel\|\|'');if(value!==null)void action(()=>paymentProfiles.rename(authFetch,'banks',b.id,value),'Bank account name updated.');}} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:72](../../../web/app/host/payment-settings/page.tsx#L72) |
| UI-00367 | {eventId?localize("ui.7636631d120ebe99",{defaultValue:"Remove from event"}):localize("ui.e2d0a54968ead24e",{defaultValue:"Delete"})} | {()=>{if(window.confirm(eventId?'Remove this account from this event? Shared host accounts will remain saved.':`Archive ${b.accountLabel\|\|maskedBank(b)}? It will disappear from payment choices. Historical records will be retained.`))void action(()=>paymentProfiles.archive(authFetch,'banks',b.id,undefined,eventId),eventId?'Account removed from this event.':'Bank account archived.');}} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:72](../../../web/app/host/payment-settings/page.tsx#L72) |
| UI-00370 | {localize("ui.29b31220e82fa254",{defaultValue:"Close QR"})} | {closeQr} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:73](../../../web/app/host/payment-settings/page.tsx#L73) |
| UI-00528 | Accept optional analytics | {()=>{saveConsent(true);setShow(false)}} | Not a native form submit or inherited from component context | [web/components/CookieNotice.tsx:5](../../../web/components/CookieNotice.tsx#L5) |
| UI-00529 | Reject optional analytics | {()=>{saveConsent(false);setShow(false)}} | Not a native form submit or inherited from component context | [web/components/CookieNotice.tsx:5](../../../web/components/CookieNotice.tsx#L5) |
| UI-00788 | {collapsed?<PanelLeftOpen size={20}/>:<PanelLeftClose size={20}/>} | {toggle} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:50](../../../web/components/HostWorkspace.tsx#L50) |
| UI-00791 |  | {()=>void signOut()} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:53](../../../web/components/HostWorkspace.tsx#L53) |
| UI-00792 |  | {()=>setCollapsed(true)} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:54](../../../web/components/HostWorkspace.tsx#L54) |
| UI-00876 | Cookie preferences | {()=>window.dispatchEvent(new Event('giftwise-cookie-settings'))} | Not a native form submit or inherited from component context | [web/components/LegalFooter.tsx:4](../../../web/components/LegalFooter.tsx#L4) |
| UI-00985 | {localize("ui.d0521c5f68365b07",{defaultValue:"Refresh check"})} | {()=>setRetry(v=>v+1)} | Not a native form submit or inherited from component context | [web/components/ReceivingAccountHealth.tsx:14](../../../web/components/ReceivingAccountHealth.tsx#L14) |


### Dialog and alert/status inventory

| ID | Role / kind | Text / label | Visibility conditions | Source |
| --- | --- | --- | --- | --- |
| UI-00319 | native-dialog | 'Account holder name exactly as registered with the bank' | Parent/source state | [web/app/host/payment-settings/page.tsx:42](../../../web/app/host/payment-settings/page.tsx#L42) |
| UI-00320 | native-dialog | `Send these account details to Cashfree for ${verification?.environment\|\|'sandbox'} verification?` | Parent/source state | [web/app/host/payment-settings/page.tsx:43](../../../web/app/host/payment-settings/page.tsx#L43) |
| UI-00321 | native-dialog | 'I have checked this destination and confirm it belongs to me. This does not independently verify ownership or enable guest transfers.' | Parent/source state | [web/app/host/payment-settings/page.tsx:60](../../../web/app/host/payment-settings/page.tsx#L60) |
| UI-00349 | native-dialog | 'Account name / tag (maximum 50 characters)' | upis.length===0; loading | [web/app/host/payment-settings/page.tsx:71](../../../web/app/host/payment-settings/page.tsx#L71) |
| UI-00351 | native-dialog | eventId?'Remove this account from this event? Shared host accounts will remain saved.':`Archive ${u.accountLabel\|\|u.masked\|\|'this UPI ID'}? It will disappear from payment choices. Historical records will be retained.` | upis.length===0; loading | [web/app/host/payment-settings/page.tsx:71](../../../web/app/host/payment-settings/page.tsx#L71) |
| UI-00366 | native-dialog | 'Account name / tag (maximum 50 characters)' | banks.length===0; loading | [web/app/host/payment-settings/page.tsx:72](../../../web/app/host/payment-settings/page.tsx#L72) |
| UI-00368 | native-dialog | eventId?'Remove this account from this event? Shared host accounts will remain saved.':`Archive ${b.accountLabel\|\|maskedBank(b)}? It will disappear from payment choices. Historical records will be retained.` | banks.length===0; loading | [web/app/host/payment-settings/page.tsx:72](../../../web/app/host/payment-settings/page.tsx#L72) |
| UI-00369 | "dialog" | {localize("ui.cc2f3579e157b2d0",{defaultValue:"UPI QR preview"})} | qr | [web/app/host/payment-settings/page.tsx:73](../../../web/app/host/payment-settings/page.tsx#L73) |
| UI-00072 | "status" | {localize("ui.bed9096c411e6da3",{defaultValue:"Opening your workspace…"})} | Parent/source state | [web/app/components/HostGlobalNav.tsx:17](../../../web/app/components/HostGlobalNav.tsx#L17) |
| UI-00324 | "alert" | {localizeText(error)} | error | [web/app/host/payment-settings/page.tsx:69](../../../web/app/host/payment-settings/page.tsx#L69) |
| UI-00326 | "status" | {localizeText(notice)} | notice | [web/app/host/payment-settings/page.tsx:69](../../../web/app/host/payment-settings/page.tsx#L69) |
| UI-00328 | "status" | {verificationDetails} | verificationDetails | [web/app/host/payment-settings/page.tsx:70](../../../web/app/host/payment-settings/page.tsx#L70) |
| UI-00340 | "status" | {localize("ui.a179b98670de0176",{defaultValue:"Upload or generate a valid QR before confirming or selecting Preferred."})} | !u.qrReady; upis.length===0; loading | [web/app/host/payment-settings/page.tsx:71](../../../web/app/host/payment-settings/page.tsx#L71) |
| UI-00548 | "status" | {localize("ui.7a005f0f1dbc704f",{defaultValue:"Opening your celebration space…"})} | ready\|\|discovery | [web/components/ExperienceProvider.tsx:19](../../../web/components/ExperienceProvider.tsx#L19) |
| UI-00790 | "alert" | {localizeText(error)} | error | [web/components/HostWorkspace.tsx:53](../../../web/components/HostWorkspace.tsx#L53) |
| UI-00909 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:19](../../../web/components/LocalizationProvider.tsx#L19) |
| UI-00911 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:20](../../../web/components/LocalizationProvider.tsx#L20) |
| UI-00913 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:21](../../../web/components/LocalizationProvider.tsx#L21) |
| UI-00986 | "status" | {localize("ui.7b641b6e0d99ccbb",{defaultValue:"Checking linked accounts…"})} | loading | [web/components/ReceivingAccountHealth.tsx:14](../../../web/components/ReceivingAccountHealth.tsx#L14) |
| UI-00987 | "alert" | {localizeText(error)} | error; loading | [web/components/ReceivingAccountHealth.tsx:14](../../../web/components/ReceivingAccountHealth.tsx#L14) |


### Workflow state bindings

| Source | Binding | Initial expression |
| --- | --- | --- |
| web/app/host/payment-settings/page.tsx | busy | false |
| web/app/host/payment-settings/page.tsx | loading | true |
| web/app/host/payment-settings/page.tsx | error | '' |
| web/app/host/payment-settings/page.tsx | notice | '' |
| web/app/host/payment-settings/page.tsx | uploadedQr | {} |
| web/components/LocalizationProvider.tsx | busy | false |
| web/components/LocalizationProvider.tsx | error | '' |
| web/components/ReceivingAccountHealth.tsx | loading | true |
| web/components/ReceivingAccountHealth.tsx | error | '' |
| web/components/HostWorkspace.tsx | busy | false |
| web/components/HostWorkspace.tsx | error | '' |


## Login

| Screen information | Source-supported specification |
| --- | --- |
| Route | /login |
| Source | [web/app/login/page.tsx:1](../../../web/app/login/page.tsx#L1) |
| Purpose | Enter username/email and password and establish the correct session |
| User access level | Public; authenticated result role determines destination |
| Navigation source | Protected route/session expiry / register / direct URL |
| Navigation destination | Safe host destination or admin console; recover username/password or register |
| Preconditions | Route parameters/query scope must be valid; applicable session/ownership/module/provider state above. Load errors do not imply an empty successful dataset. |
| Postconditions | Only the selected successful action changes its documented state. Navigation alone does not publish, confirm payment, verify contact or deliver a notification. |
| Route variables | No path variable |
| UI variants | Focused/general experience, responsive layout and module/state predicates can change visible sections. Exact predicates are in each control record. |


### Wireframe layout

| Section | Placement / behavior / evidence |
| --- | --- |
| Header | Shared identity/event or page title/back/actions, where declared. Composite header declarations: 2. |
| Navigation / sidebar | Host/admin navigation and event context where applicable; mobile/focused variants are conditional. nav=3, aside=3. |
| Main content | Purpose-specific forms/cards/tables/canvas/player from the composed sources. main=3, section=2, article=0. |
| Search and filters | Fields/selects listed below; exact value/handler/visibility controls are not inferred from placeholder labels. |
| Data grids and cards | Table declarations=0. Data collections and generated rows/cards are in frontend.json dynamicSources. |
| Popups / dialogs | Named/source-declared dialog control records below; opening/closing/loading/apply/cancel actions belong to those component handlers. |
| Alerts / notifications | Source controls with role alert/status are listed below. Notice strings may come from API results; do not assume a universal envelope. |
| Footer | Local/shared footer declarations=1. No undocumented legal/footer wording is invented. |
| Loading / empty / success / error | Observed state bindings and relevant action fields are listed below. Expected QA states from Document 2 do not establish a spinner/retry control where source does not implement one. |


### Composed implementation

- [web/app/login/page.tsx:1](../../../web/app/login/page.tsx#L1)
- [web/components/BrandingProvider.tsx:1](../../../web/components/BrandingProvider.tsx#L1)
- [web/components/LocalizationProvider.tsx:1](../../../web/components/LocalizationProvider.tsx#L1)
- [web/components/VerificationProvider.tsx:1](../../../web/components/VerificationProvider.tsx#L1)
- [web/app/components/HostToast.tsx:1](../../../web/app/components/HostToast.tsx#L1)
- [web/app/layout.tsx:1](../../../web/app/layout.tsx#L1)
- [web/components/GrowthProvider.tsx:1](../../../web/components/GrowthProvider.tsx#L1)
- [web/components/LegalFooter.tsx:1](../../../web/components/LegalFooter.tsx#L1)
- [web/components/CookieNotice.tsx:1](../../../web/components/CookieNotice.tsx#L1)
- [web/app/components/ApplicationInteractions.tsx:1](../../../web/app/components/ApplicationInteractions.tsx#L1)
- [web/components/ExperienceProvider.tsx:1](../../../web/components/ExperienceProvider.tsx#L1)
- [web/app/components/PerformanceDiagnostics.tsx:1](../../../web/app/components/PerformanceDiagnostics.tsx#L1)
- [web/app/components/HostSessionRedirect.tsx:1](../../../web/app/components/HostSessionRedirect.tsx#L1)
- [web/app/components/HostGlobalNav.tsx:1](../../../web/app/components/HostGlobalNav.tsx#L1)
- [web/components/HostWorkspace.tsx:1](../../../web/components/HostWorkspace.tsx#L1)
- [web/components/focused/HostShell.tsx:1](../../../web/components/focused/HostShell.tsx#L1)

### Fields

| Control ID | Label / expression | Type | Binding | Required | Native limits / format | Detailed specification |
| --- | --- | --- | --- | --- | --- | --- |
| UI-00375 | {localize("ui.2d4a46f3688886c0",{defaultValue:"Username or email"})} | input  | {identifier} | true |  | [control](UI_CONTROLS.md#ui-00375) |
| UI-00376 | {localize("ui.e7cf3ef4f17c3999",{defaultValue:"Password"})} | input "password" | {password} | true |  | [control](UI_CONTROLS.md#ui-00376) |
| UI-00908 | {localize("ui.bba5b40bdd28c66e",{defaultValue:"Choose language"})} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00908) |
| UI-00910 | {label} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00910) |
| UI-00912 | {localize("ui.140071feea59fe87",{defaultValue:"Preferred language"})} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00912) |


### Buttons

| ID | Label / expression | Action | Submit validation / handler | Source |
| --- | --- | --- | --- | --- |
| UI-00078 | × | {()=>{setFlash(null);onDismiss?.();}} | Not a native form submit or inherited from component context | [web/app/components/HostToast.tsx:16](../../../web/app/components/HostToast.tsx#L16) |
| UI-00378 | {busy?localize("ui.8b9d3de8ae00c89c",{defaultValue:"Signing in…"}):localize("ui.8637e0051dee66ad",{defaultValue:"Sign in →"})} | submit / component action | {submit} | [web/app/login/page.tsx:18](../../../web/app/login/page.tsx#L18) |
| UI-00528 | Accept optional analytics | {()=>{saveConsent(true);setShow(false)}} | Not a native form submit or inherited from component context | [web/components/CookieNotice.tsx:5](../../../web/components/CookieNotice.tsx#L5) |
| UI-00529 | Reject optional analytics | {()=>{saveConsent(false);setShow(false)}} | Not a native form submit or inherited from component context | [web/components/CookieNotice.tsx:5](../../../web/components/CookieNotice.tsx#L5) |
| UI-00788 | {collapsed?<PanelLeftOpen size={20}/>:<PanelLeftClose size={20}/>} | {toggle} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:50](../../../web/components/HostWorkspace.tsx#L50) |
| UI-00791 |  | {()=>void signOut()} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:53](../../../web/components/HostWorkspace.tsx#L53) |
| UI-00792 |  | {()=>setCollapsed(true)} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:54](../../../web/components/HostWorkspace.tsx#L54) |
| UI-00876 | Cookie preferences | {()=>window.dispatchEvent(new Event('giftwise-cookie-settings'))} | Not a native form submit or inherited from component context | [web/components/LegalFooter.tsx:4](../../../web/components/LegalFooter.tsx#L4) |


### Dialog and alert/status inventory

| ID | Role / kind | Text / label | Visibility conditions | Source |
| --- | --- | --- | --- | --- |
| UI-00072 | "status" | {localize("ui.bed9096c411e6da3",{defaultValue:"Opening your workspace…"})} | Parent/source state | [web/app/components/HostGlobalNav.tsx:17](../../../web/app/components/HostGlobalNav.tsx#L17) |
| UI-00373 | "status" | {localize("ui.c2eb28f1f14bc763",{defaultValue:"Your session has expired. Please sign in again to continue."})} | expired | [web/app/login/page.tsx:18](../../../web/app/login/page.tsx#L18) |
| UI-00377 | "alert" | {localizeText(error)} | error | [web/app/login/page.tsx:18](../../../web/app/login/page.tsx#L18) |
| UI-00548 | "status" | {localize("ui.7a005f0f1dbc704f",{defaultValue:"Opening your celebration space…"})} | ready\|\|discovery | [web/components/ExperienceProvider.tsx:19](../../../web/components/ExperienceProvider.tsx#L19) |
| UI-00790 | "alert" | {localizeText(error)} | error | [web/components/HostWorkspace.tsx:53](../../../web/components/HostWorkspace.tsx#L53) |
| UI-00909 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:19](../../../web/components/LocalizationProvider.tsx#L19) |
| UI-00911 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:20](../../../web/components/LocalizationProvider.tsx#L20) |
| UI-00913 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:21](../../../web/components/LocalizationProvider.tsx#L21) |


### Workflow state bindings

| Source | Binding | Initial expression |
| --- | --- | --- |
| web/app/login/page.tsx | error | '' |
| web/app/login/page.tsx | busy | false |
| web/components/LocalizationProvider.tsx | busy | false |
| web/components/LocalizationProvider.tsx | error | '' |
| web/components/HostWorkspace.tsx | busy | false |
| web/components/HostWorkspace.tsx | error | '' |


## Shared approved-wishes keepsake

| Screen information | Source-supported specification |
| --- | --- |
| Route | /memories/[token] |
| Source | [web/app/memories/[token]/page.tsx:1](../../../web/app/memories/[token]/page.tsx#L1) |
| Purpose | Read twenty approved names/messages per page; private albums, contacts and payment data excluded |
| User access level | Published/completed event, global switch, owner consent and host Guestbook grant |
| Navigation source | Host copied keepsake link |
| Navigation destination | Next/previous wishes; optional guest discovery; unavailable when revoked |
| Preconditions | Route parameters/query scope must be valid; applicable session/ownership/module/provider state above. Load errors do not imply an empty successful dataset. |
| Postconditions | Only the selected successful action changes its documented state. Navigation alone does not publish, confirm payment, verify contact or deliver a notification. |
| Route variables | token |
| UI variants | Focused/general experience, responsive layout and module/state predicates can change visible sections. Exact predicates are in each control record. |


### Wireframe layout

| Section | Placement / behavior / evidence |
| --- | --- |
| Header | Shared identity/event or page title/back/actions, where declared. Composite header declarations: 3. |
| Navigation / sidebar | Host/admin navigation and event context where applicable; mobile/focused variants are conditional. nav=4, aside=4. |
| Main content | Purpose-specific forms/cards/tables/canvas/player from the composed sources. main=3, section=3, article=1. |
| Search and filters | Fields/selects listed below; exact value/handler/visibility controls are not inferred from placeholder labels. |
| Data grids and cards | Table declarations=0. Data collections and generated rows/cards are in frontend.json dynamicSources. |
| Popups / dialogs | Named/source-declared dialog control records below; opening/closing/loading/apply/cancel actions belong to those component handlers. |
| Alerts / notifications | Source controls with role alert/status are listed below. Notice strings may come from API results; do not assume a universal envelope. |
| Footer | Local/shared footer declarations=1. No undocumented legal/footer wording is invented. |
| Loading / empty / success / error | Observed state bindings and relevant action fields are listed below. Expected QA states from Document 2 do not establish a spinner/retry control where source does not implement one. |


### Composed implementation

- [web/app/memories/[token]/page.tsx:1](../../../web/app/memories/[token]/page.tsx#L1)
- [web/components/LocalizationProvider.tsx:1](../../../web/components/LocalizationProvider.tsx#L1)
- [web/components/BrandingProvider.tsx:1](../../../web/components/BrandingProvider.tsx#L1)
- [web/components/GuestDiscovery.tsx:1](../../../web/components/GuestDiscovery.tsx#L1)
- [web/components/GrowthProvider.tsx:1](../../../web/components/GrowthProvider.tsx#L1)
- [web/app/layout.tsx:1](../../../web/app/layout.tsx#L1)
- [web/components/LegalFooter.tsx:1](../../../web/components/LegalFooter.tsx#L1)
- [web/components/CookieNotice.tsx:1](../../../web/components/CookieNotice.tsx#L1)
- [web/app/components/ApplicationInteractions.tsx:1](../../../web/app/components/ApplicationInteractions.tsx#L1)
- [web/components/VerificationProvider.tsx:1](../../../web/components/VerificationProvider.tsx#L1)
- [web/components/ExperienceProvider.tsx:1](../../../web/components/ExperienceProvider.tsx#L1)
- [web/app/components/PerformanceDiagnostics.tsx:1](../../../web/app/components/PerformanceDiagnostics.tsx#L1)
- [web/app/components/HostSessionRedirect.tsx:1](../../../web/app/components/HostSessionRedirect.tsx#L1)
- [web/app/components/HostGlobalNav.tsx:1](../../../web/app/components/HostGlobalNav.tsx#L1)
- [web/components/HostWorkspace.tsx:1](../../../web/components/HostWorkspace.tsx#L1)
- [web/components/focused/HostShell.tsx:1](../../../web/components/focused/HostShell.tsx#L1)

### Fields

| Control ID | Label / expression | Type | Binding | Required | Native limits / format | Detailed specification |
| --- | --- | --- | --- | --- | --- | --- |
| UI-00908 | {localize("ui.bba5b40bdd28c66e",{defaultValue:"Choose language"})} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00908) |
| UI-00910 | {label} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00910) |
| UI-00912 | {localize("ui.140071feea59fe87",{defaultValue:"Preferred language"})} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00912) |


### Buttons

| ID | Label / expression | Action | Submit validation / handler | Source |
| --- | --- | --- | --- | --- |
| UI-00384 | {t('Retry')} | {()=>setRevision(n=>n+1)} | Not a native form submit or inherited from component context | [web/app/memories/[token]/page.tsx:11](../../../web/app/memories/[token]/page.tsx#L11) |
| UI-00385 | {t('Previous')} | {()=>setPage(n=>n-1)} | Not a native form submit or inherited from component context | [web/app/memories/[token]/page.tsx:11](../../../web/app/memories/[token]/page.tsx#L11) |
| UI-00386 | {t('Next')} | {()=>setPage(n=>n+1)} | Not a native form submit or inherited from component context | [web/app/memories/[token]/page.tsx:11](../../../web/app/memories/[token]/page.tsx#L11) |
| UI-00528 | Accept optional analytics | {()=>{saveConsent(true);setShow(false)}} | Not a native form submit or inherited from component context | [web/components/CookieNotice.tsx:5](../../../web/components/CookieNotice.tsx#L5) |
| UI-00529 | Reject optional analytics | {()=>{saveConsent(false);setShow(false)}} | Not a native form submit or inherited from component context | [web/components/CookieNotice.tsx:5](../../../web/components/CookieNotice.tsx#L5) |
| UI-00788 | {collapsed?<PanelLeftOpen size={20}/>:<PanelLeftClose size={20}/>} | {toggle} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:50](../../../web/components/HostWorkspace.tsx#L50) |
| UI-00791 |  | {()=>void signOut()} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:53](../../../web/components/HostWorkspace.tsx#L53) |
| UI-00792 |  | {()=>setCollapsed(true)} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:54](../../../web/components/HostWorkspace.tsx#L54) |
| UI-00876 | Cookie preferences | {()=>window.dispatchEvent(new Event('giftwise-cookie-settings'))} | Not a native form submit or inherited from component context | [web/components/LegalFooter.tsx:4](../../../web/components/LegalFooter.tsx#L4) |


### Dialog and alert/status inventory

| ID | Role / kind | Text / label | Visibility conditions | Source |
| --- | --- | --- | --- | --- |
| UI-00072 | "status" | {localize("ui.bed9096c411e6da3",{defaultValue:"Opening your workspace…"})} | Parent/source state | [web/app/components/HostGlobalNav.tsx:17](../../../web/app/components/HostGlobalNav.tsx#L17) |
| UI-00382 | "status" | {t('Opening your keepsake…')} | busy | [web/app/memories/[token]/page.tsx:11](../../../web/app/memories/[token]/page.tsx#L11) |
| UI-00383 | "alert" | {t(error)} | error | [web/app/memories/[token]/page.tsx:11](../../../web/app/memories/[token]/page.tsx#L11) |
| UI-00548 | "status" | {localize("ui.7a005f0f1dbc704f",{defaultValue:"Opening your celebration space…"})} | ready\|\|discovery | [web/components/ExperienceProvider.tsx:19](../../../web/components/ExperienceProvider.tsx#L19) |
| UI-00790 | "alert" | {localizeText(error)} | error | [web/components/HostWorkspace.tsx:53](../../../web/components/HostWorkspace.tsx#L53) |
| UI-00909 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:19](../../../web/components/LocalizationProvider.tsx#L19) |
| UI-00911 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:20](../../../web/components/LocalizationProvider.tsx#L20) |
| UI-00913 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:21](../../../web/components/LocalizationProvider.tsx#L21) |


### Workflow state bindings

| Source | Binding | Initial expression |
| --- | --- | --- |
| web/app/memories/[token]/page.tsx | error | '' |
| web/app/memories/[token]/page.tsx | busy | true |
| web/components/LocalizationProvider.tsx | busy | false |
| web/components/LocalizationProvider.tsx | error | '' |
| web/components/HostWorkspace.tsx | busy | false |
| web/components/HostWorkspace.tsx | error | '' |


## Privacy policy

| Screen information | Source-supported specification |
| --- | --- |
| Route | /privacy |
| Source | [web/app/privacy/page.tsx:1](../../../web/app/privacy/page.tsx#L1) |
| Purpose | Read current application privacy and cookie wording |
| User access level | Public |
| Navigation source | Registration consent / footer / cookie notice |
| Navigation destination | Terms, contact or return navigation |
| Preconditions | Route parameters/query scope must be valid; applicable session/ownership/module/provider state above. Load errors do not imply an empty successful dataset. |
| Postconditions | Only the selected successful action changes its documented state. Navigation alone does not publish, confirm payment, verify contact or deliver a notification. |
| Route variables | No path variable |
| UI variants | Focused/general experience, responsive layout and module/state predicates can change visible sections. Exact predicates are in each control record. |


### Wireframe layout

| Section | Placement / behavior / evidence |
| --- | --- |
| Header | Shared identity/event or page title/back/actions, where declared. Composite header declarations: 2. |
| Navigation / sidebar | Host/admin navigation and event context where applicable; mobile/focused variants are conditional. nav=3, aside=3. |
| Main content | Purpose-specific forms/cards/tables/canvas/player from the composed sources. main=3, section=3, article=0. |
| Search and filters | Fields/selects listed below; exact value/handler/visibility controls are not inferred from placeholder labels. |
| Data grids and cards | Table declarations=0. Data collections and generated rows/cards are in frontend.json dynamicSources. |
| Popups / dialogs | Named/source-declared dialog control records below; opening/closing/loading/apply/cancel actions belong to those component handlers. |
| Alerts / notifications | Source controls with role alert/status are listed below. Notice strings may come from API results; do not assume a universal envelope. |
| Footer | Local/shared footer declarations=1. No undocumented legal/footer wording is invented. |
| Loading / empty / success / error | Observed state bindings and relevant action fields are listed below. Expected QA states from Document 2 do not establish a spinner/retry control where source does not implement one. |


### Composed implementation

- [web/app/privacy/page.tsx:1](../../../web/app/privacy/page.tsx#L1)
- [web/app/layout.tsx:1](../../../web/app/layout.tsx#L1)
- [web/components/GrowthProvider.tsx:1](../../../web/components/GrowthProvider.tsx#L1)
- [web/components/BrandingProvider.tsx:1](../../../web/components/BrandingProvider.tsx#L1)
- [web/components/LegalFooter.tsx:1](../../../web/components/LegalFooter.tsx#L1)
- [web/components/CookieNotice.tsx:1](../../../web/components/CookieNotice.tsx#L1)
- [web/app/components/ApplicationInteractions.tsx:1](../../../web/app/components/ApplicationInteractions.tsx#L1)
- [web/components/LocalizationProvider.tsx:1](../../../web/components/LocalizationProvider.tsx#L1)
- [web/components/VerificationProvider.tsx:1](../../../web/components/VerificationProvider.tsx#L1)
- [web/components/ExperienceProvider.tsx:1](../../../web/components/ExperienceProvider.tsx#L1)
- [web/app/components/PerformanceDiagnostics.tsx:1](../../../web/app/components/PerformanceDiagnostics.tsx#L1)
- [web/app/components/HostSessionRedirect.tsx:1](../../../web/app/components/HostSessionRedirect.tsx#L1)
- [web/app/components/HostGlobalNav.tsx:1](../../../web/app/components/HostGlobalNav.tsx#L1)
- [web/components/HostWorkspace.tsx:1](../../../web/components/HostWorkspace.tsx#L1)
- [web/components/focused/HostShell.tsx:1](../../../web/components/focused/HostShell.tsx#L1)

### Fields

| Control ID | Label / expression | Type | Binding | Required | Native limits / format | Detailed specification |
| --- | --- | --- | --- | --- | --- | --- |
| UI-00908 | {localize("ui.bba5b40bdd28c66e",{defaultValue:"Choose language"})} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00908) |
| UI-00910 | {label} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00910) |
| UI-00912 | {localize("ui.140071feea59fe87",{defaultValue:"Preferred language"})} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00912) |


### Buttons

| ID | Label / expression | Action | Submit validation / handler | Source |
| --- | --- | --- | --- | --- |
| UI-00528 | Accept optional analytics | {()=>{saveConsent(true);setShow(false)}} | Not a native form submit or inherited from component context | [web/components/CookieNotice.tsx:5](../../../web/components/CookieNotice.tsx#L5) |
| UI-00529 | Reject optional analytics | {()=>{saveConsent(false);setShow(false)}} | Not a native form submit or inherited from component context | [web/components/CookieNotice.tsx:5](../../../web/components/CookieNotice.tsx#L5) |
| UI-00788 | {collapsed?<PanelLeftOpen size={20}/>:<PanelLeftClose size={20}/>} | {toggle} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:50](../../../web/components/HostWorkspace.tsx#L50) |
| UI-00791 |  | {()=>void signOut()} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:53](../../../web/components/HostWorkspace.tsx#L53) |
| UI-00792 |  | {()=>setCollapsed(true)} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:54](../../../web/components/HostWorkspace.tsx#L54) |
| UI-00876 | Cookie preferences | {()=>window.dispatchEvent(new Event('giftwise-cookie-settings'))} | Not a native form submit or inherited from component context | [web/components/LegalFooter.tsx:4](../../../web/components/LegalFooter.tsx#L4) |


### Dialog and alert/status inventory

| ID | Role / kind | Text / label | Visibility conditions | Source |
| --- | --- | --- | --- | --- |
| UI-00072 | "status" | {localize("ui.bed9096c411e6da3",{defaultValue:"Opening your workspace…"})} | Parent/source state | [web/app/components/HostGlobalNav.tsx:17](../../../web/app/components/HostGlobalNav.tsx#L17) |
| UI-00548 | "status" | {localize("ui.7a005f0f1dbc704f",{defaultValue:"Opening your celebration space…"})} | ready\|\|discovery | [web/components/ExperienceProvider.tsx:19](../../../web/components/ExperienceProvider.tsx#L19) |
| UI-00790 | "alert" | {localizeText(error)} | error | [web/components/HostWorkspace.tsx:53](../../../web/components/HostWorkspace.tsx#L53) |
| UI-00909 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:19](../../../web/components/LocalizationProvider.tsx#L19) |
| UI-00911 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:20](../../../web/components/LocalizationProvider.tsx#L20) |
| UI-00913 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:21](../../../web/components/LocalizationProvider.tsx#L21) |


### Workflow state bindings

| Source | Binding | Initial expression |
| --- | --- | --- |
| web/components/LocalizationProvider.tsx | busy | false |
| web/components/LocalizationProvider.tsx | error | '' |
| web/components/HostWorkspace.tsx | busy | false |
| web/components/HostWorkspace.tsx | error | '' |


## Host registration

| Screen information | Source-supported specification |
| --- | --- |
| Route | /register |
| Source | [web/app/register/page.tsx:1](../../../web/app/register/page.tsx#L1) |
| Purpose | Create account using required name/username/email/mobile/password and applicable proof |
| User access level | Public subject to registration/contact policies |
| Navigation source | Login / direct signup link |
| Navigation destination | Login/host workspace; verification or recovery as allowed |
| Preconditions | Route parameters/query scope must be valid; applicable session/ownership/module/provider state above. Load errors do not imply an empty successful dataset. |
| Postconditions | Only the selected successful action changes its documented state. Navigation alone does not publish, confirm payment, verify contact or deliver a notification. |
| Route variables | No path variable |
| UI variants | Focused/general experience, responsive layout and module/state predicates can change visible sections. Exact predicates are in each control record. |


### Wireframe layout

| Section | Placement / behavior / evidence |
| --- | --- |
| Header | Shared identity/event or page title/back/actions, where declared. Composite header declarations: 2. |
| Navigation / sidebar | Host/admin navigation and event context where applicable; mobile/focused variants are conditional. nav=3, aside=3. |
| Main content | Purpose-specific forms/cards/tables/canvas/player from the composed sources. main=3, section=2, article=0. |
| Search and filters | Fields/selects listed below; exact value/handler/visibility controls are not inferred from placeholder labels. |
| Data grids and cards | Table declarations=0. Data collections and generated rows/cards are in frontend.json dynamicSources. |
| Popups / dialogs | Named/source-declared dialog control records below; opening/closing/loading/apply/cancel actions belong to those component handlers. |
| Alerts / notifications | Source controls with role alert/status are listed below. Notice strings may come from API results; do not assume a universal envelope. |
| Footer | Local/shared footer declarations=1. No undocumented legal/footer wording is invented. |
| Loading / empty / success / error | Observed state bindings and relevant action fields are listed below. Expected QA states from Document 2 do not establish a spinner/retry control where source does not implement one. |


### Composed implementation

- [web/app/register/page.tsx:1](../../../web/app/register/page.tsx#L1)
- [web/components/BrandingProvider.tsx:1](../../../web/components/BrandingProvider.tsx#L1)
- [web/components/LocalizationProvider.tsx:1](../../../web/components/LocalizationProvider.tsx#L1)
- [web/components/CredentialRequirements.tsx:1](../../../web/components/CredentialRequirements.tsx#L1)
- [web/components/VerificationProvider.tsx:1](../../../web/components/VerificationProvider.tsx#L1)
- [web/components/SignupEmailField.tsx:1](../../../web/components/SignupEmailField.tsx#L1)
- [web/components/ExperienceProvider.tsx:1](../../../web/components/ExperienceProvider.tsx#L1)
- [web/app/components/HostToast.tsx:1](../../../web/app/components/HostToast.tsx#L1)
- [web/app/layout.tsx:1](../../../web/app/layout.tsx#L1)
- [web/components/GrowthProvider.tsx:1](../../../web/components/GrowthProvider.tsx#L1)
- [web/components/LegalFooter.tsx:1](../../../web/components/LegalFooter.tsx#L1)
- [web/components/CookieNotice.tsx:1](../../../web/components/CookieNotice.tsx#L1)
- [web/app/components/ApplicationInteractions.tsx:1](../../../web/app/components/ApplicationInteractions.tsx#L1)
- [web/app/components/PerformanceDiagnostics.tsx:1](../../../web/app/components/PerformanceDiagnostics.tsx#L1)
- [web/app/components/HostSessionRedirect.tsx:1](../../../web/app/components/HostSessionRedirect.tsx#L1)
- [web/app/components/HostGlobalNav.tsx:1](../../../web/app/components/HostGlobalNav.tsx#L1)
- [web/components/HostWorkspace.tsx:1](../../../web/components/HostWorkspace.tsx#L1)
- [web/components/focused/HostShell.tsx:1](../../../web/components/focused/HostShell.tsx#L1)

### Fields

| Control ID | Label / expression | Type | Binding | Required | Native limits / format | Detailed specification |
| --- | --- | --- | --- | --- | --- | --- |
| UI-00390 | {localize("ui.f13a64ba2fea87ef",{defaultValue:"Full name"})} | input  | {fullName} | true | minLength={2}; maxLength={120} | [control](UI_CONTROLS.md#ui-00390) |
| UI-00391 | {localize("ui.e3b89e9d33f88e52",{defaultValue:"Username"})} | input  | {username} | true | minLength={4}; maxLength={30}; pattern="[A-Za-z0-9][A-Za-z0-9_]{3,29}" | [control](UI_CONTROLS.md#ui-00391) |
| UI-00392 | {localize("ui.513ace4f7bbbdc7c",{defaultValue:"Mobile number (country code required)"})} | input "tel" | {mobile} | true | pattern="\+[1-9][0-9]{7,14}" | [control](UI_CONTROLS.md#ui-00392) |
| UI-00393 | {localize("ui.c75a30689d009a63",{defaultValue:"Password "})} | input "password" | {password} | true | minLength={8} | [control](UI_CONTROLS.md#ui-00393) |
| UI-00394 |  | input "checkbox" | {termsAccepted} | true |  | [control](UI_CONTROLS.md#ui-00394) |
| UI-00908 | {localize("ui.bba5b40bdd28c66e",{defaultValue:"Choose language"})} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00908) |
| UI-00910 | {label} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00910) |
| UI-00912 | {localize("ui.140071feea59fe87",{defaultValue:"Preferred language"})} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00912) |
| UI-00998 | {localize("ui.f2488fd4ef4adbc6",{defaultValue:"Email address"})} | input "email" | {value} | true | maxLength={254} | [control](UI_CONTROLS.md#ui-00998) |


### Buttons

| ID | Label / expression | Action | Submit validation / handler | Source |
| --- | --- | --- | --- | --- |
| UI-00078 | × | {()=>{setFlash(null);onDismiss?.();}} | Not a native form submit or inherited from component context | [web/app/components/HostToast.tsx:16](../../../web/app/components/HostToast.tsx#L16) |
| UI-00397 | {localize("ui.a2fe55208689a5de",{defaultValue:"Edit your details"})} | {()=>setStep(0)} | {submit} | [web/app/register/page.tsx:14](../../../web/app/register/page.tsx#L14) |
| UI-00399 | {busy?localize("ui.f2daf61e527b4f1c",{defaultValue:"Creating account…"}):focused&&step===0?localize("ui.496267ccc6b01605",{defaultValue:"Continue →"}):localize("ui.b44f53d3c6ec0d33",{defaultValue:"Create account →"})} | submit / component action | {submit} | [web/app/register/page.tsx:14](../../../web/app/register/page.tsx#L14) |
| UI-00528 | Accept optional analytics | {()=>{saveConsent(true);setShow(false)}} | Not a native form submit or inherited from component context | [web/components/CookieNotice.tsx:5](../../../web/components/CookieNotice.tsx#L5) |
| UI-00529 | Reject optional analytics | {()=>{saveConsent(false);setShow(false)}} | Not a native form submit or inherited from component context | [web/components/CookieNotice.tsx:5](../../../web/components/CookieNotice.tsx#L5) |
| UI-00788 | {collapsed?<PanelLeftOpen size={20}/>:<PanelLeftClose size={20}/>} | {toggle} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:50](../../../web/components/HostWorkspace.tsx#L50) |
| UI-00791 |  | {()=>void signOut()} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:53](../../../web/components/HostWorkspace.tsx#L53) |
| UI-00792 |  | {()=>setCollapsed(true)} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:54](../../../web/components/HostWorkspace.tsx#L54) |
| UI-00876 | Cookie preferences | {()=>window.dispatchEvent(new Event('giftwise-cookie-settings'))} | Not a native form submit or inherited from component context | [web/components/LegalFooter.tsx:4](../../../web/components/LegalFooter.tsx#L4) |


### Dialog and alert/status inventory

| ID | Role / kind | Text / label | Visibility conditions | Source |
| --- | --- | --- | --- | --- |
| UI-00072 | "status" | {localize("ui.bed9096c411e6da3",{defaultValue:"Opening your workspace…"})} | Parent/source state | [web/app/components/HostGlobalNav.tsx:17](../../../web/app/components/HostGlobalNav.tsx#L17) |
| UI-00398 | "alert" | {localizeText(error)} | error | [web/app/register/page.tsx:14](../../../web/app/register/page.tsx#L14) |
| UI-00531 | "status" | {value?(remaining?localizeText(`${remaining} requirements still needed.`):localizeText("All requirements met.")):localizeText("Enter your "+kind+" to check the requirements.")} | Parent/source state | [web/components/CredentialRequirements.tsx:7](../../../web/components/CredentialRequirements.tsx#L7) |
| UI-00548 | "status" | {localize("ui.7a005f0f1dbc704f",{defaultValue:"Opening your celebration space…"})} | ready\|\|discovery | [web/components/ExperienceProvider.tsx:19](../../../web/components/ExperienceProvider.tsx#L19) |
| UI-00790 | "alert" | {localizeText(error)} | error | [web/components/HostWorkspace.tsx:53](../../../web/components/HostWorkspace.tsx#L53) |
| UI-00909 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:19](../../../web/components/LocalizationProvider.tsx#L19) |
| UI-00911 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:20](../../../web/components/LocalizationProvider.tsx#L20) |
| UI-00913 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:21](../../../web/components/LocalizationProvider.tsx#L21) |


### Workflow state bindings

| Source | Binding | Initial expression |
| --- | --- | --- |
| web/app/register/page.tsx | step | 0 |
| web/app/register/page.tsx | busy | false |
| web/app/register/page.tsx | error | '' |
| web/components/LocalizationProvider.tsx | busy | false |
| web/components/LocalizationProvider.tsx | error | '' |
| web/components/HostWorkspace.tsx | busy | false |
| web/components/HostWorkspace.tsx | error | '' |


## Invitation studio

| Screen information | Source-supported specification |
| --- | --- |
| Route | /studio |
| Source | [web/app/studio/page.tsx:1](../../../web/app/studio/page.tsx#L1) |
| Purpose | Create/edit/export a local invitation design; templates, backgrounds, safe geometry and real QR |
| User access level | Client-rendered design workspace; authenticated owner required for protected QR/design/AI APIs |
| Navigation source | Studio / dashboard / event design entry |
| Navigation destination | Choose event or tools; import/export/download; open AI only for an allowed event |
| Preconditions | Route parameters/query scope must be valid; applicable session/ownership/module/provider state above. Load errors do not imply an empty successful dataset. |
| Postconditions | Only the selected successful action changes its documented state. Navigation alone does not publish, confirm payment, verify contact or deliver a notification. |
| Route variables | No path variable |
| UI variants | Focused/general experience, responsive layout and module/state predicates can change visible sections. Exact predicates are in each control record. |


### Wireframe layout

| Section | Placement / behavior / evidence |
| --- | --- |
| Header | Shared identity/event or page title/back/actions, where declared. Composite header declarations: 5. |
| Navigation / sidebar | Host/admin navigation and event context where applicable; mobile/focused variants are conditional. nav=5, aside=5. |
| Main content | Purpose-specific forms/cards/tables/canvas/player from the composed sources. main=4, section=8, article=0. |
| Search and filters | Fields/selects listed below; exact value/handler/visibility controls are not inferred from placeholder labels. |
| Data grids and cards | Table declarations=0. Data collections and generated rows/cards are in frontend.json dynamicSources. |
| Popups / dialogs | Named/source-declared dialog control records below; opening/closing/loading/apply/cancel actions belong to those component handlers. |
| Alerts / notifications | Source controls with role alert/status are listed below. Notice strings may come from API results; do not assume a universal envelope. |
| Footer | Local/shared footer declarations=2. No undocumented legal/footer wording is invented. |
| Loading / empty / success / error | Observed state bindings and relevant action fields are listed below. Expected QA states from Document 2 do not establish a spinner/retry control where source does not implement one. |


### Composed implementation

- [web/app/studio/page.tsx:1](../../../web/app/studio/page.tsx#L1)
- [web/components/InvitationEditor.tsx:1](../../../web/components/InvitationEditor.tsx#L1)
- [web/components/LocalizationProvider.tsx:1](../../../web/components/LocalizationProvider.tsx#L1)
- [web/components/HostInvitationUploads.tsx:1](../../../web/components/HostInvitationUploads.tsx#L1)
- [web/components/CommercePurchase.tsx:1](../../../web/components/CommercePurchase.tsx#L1)
- [web/components/InvitationTemplateGallery.tsx:1](../../../web/components/InvitationTemplateGallery.tsx#L1)
- [web/components/AiInvitationDesigner.tsx:1](../../../web/components/AiInvitationDesigner.tsx#L1)
- [web/components/ExperienceProvider.tsx:1](../../../web/components/ExperienceProvider.tsx#L1)
- [web/app/layout.tsx:1](../../../web/app/layout.tsx#L1)
- [web/components/GrowthProvider.tsx:1](../../../web/components/GrowthProvider.tsx#L1)
- [web/components/BrandingProvider.tsx:1](../../../web/components/BrandingProvider.tsx#L1)
- [web/components/LegalFooter.tsx:1](../../../web/components/LegalFooter.tsx#L1)
- [web/components/CookieNotice.tsx:1](../../../web/components/CookieNotice.tsx#L1)
- [web/app/components/ApplicationInteractions.tsx:1](../../../web/app/components/ApplicationInteractions.tsx#L1)
- [web/components/VerificationProvider.tsx:1](../../../web/components/VerificationProvider.tsx#L1)
- [web/app/components/PerformanceDiagnostics.tsx:1](../../../web/app/components/PerformanceDiagnostics.tsx#L1)
- [web/app/components/HostSessionRedirect.tsx:1](../../../web/app/components/HostSessionRedirect.tsx#L1)
- [web/app/components/HostGlobalNav.tsx:1](../../../web/app/components/HostGlobalNav.tsx#L1)
- [web/components/HostWorkspace.tsx:1](../../../web/components/HostWorkspace.tsx#L1)
- [web/components/focused/HostShell.tsx:1](../../../web/components/focused/HostShell.tsx#L1)

### Fields

| Control ID | Label / expression | Type | Binding | Required | Native limits / format | Detailed specification |
| --- | --- | --- | --- | --- | --- | --- |
| UI-00440 | {localize("ui.4a8b403035a2b673",{defaultValue:"Celebration"})} | select  | {selected} | true |  | [control](UI_CONTROLS.md#ui-00440) |
| UI-00441 | {localize("ui.e00cf1143c6cdbec",{defaultValue:"Visual style"})} | select  | {form.visualStyle} | Not declared |  | [control](UI_CONTROLS.md#ui-00441) |
| UI-00442 | {localize("ui.c2c8b72e302ec730",{defaultValue:"Tone"})} | select  | {form.tone} | Not declared |  | [control](UI_CONTROLS.md#ui-00442) |
| UI-00443 | {localize("ui.a4fe65264ef7dbb3",{defaultValue:"Language"})} | input  | {form.language} | true | maxLength={40} | [control](UI_CONTROLS.md#ui-00443) |
| UI-00444 | {localize("ui.684d81462214c9b2",{defaultValue:"Names shown on the invitation "})} | input  | {form.displayNames} | Not declared | maxLength={200} | [control](UI_CONTROLS.md#ui-00444) |
| UI-00445 | {localize("ui.1173054af55a299b",{defaultValue:"Public venue "})} | input  | {form.publicVenue} | Not declared | maxLength={160} | [control](UI_CONTROLS.md#ui-00445) |
| UI-00446 | {localize("ui.981b0a32ff53be60",{defaultValue:"Time "})} | input  | {form.startTime} | Not declared | maxLength={40} | [control](UI_CONTROLS.md#ui-00446) |
| UI-00447 | {localize("ui.2d94a21f97ff5bfa",{defaultValue:"A personal touch "})} | textarea  | {form.specialMessage} | Not declared | maxLength={400} | [control](UI_CONTROLS.md#ui-00447) |
| UI-00776 | {localizeText(busy?'Preparing your artwork…':category==='DECORATION'?'Upload a sticker':'Upload a background')} | input "file" |  | Not declared |  | [control](UI_CONTROLS.md#ui-00776) |
| UI-00779 | {localizeText('Enable custom uploads for this host')} | input "checkbox" | {access.enabled} | Not declared |  | [control](UI_CONTROLS.md#ui-00779) |
| UI-00780 | {localizeText('Require a paid premium host plan')} | input "checkbox" | {access.requiresPayment} | Not declared |  | [control](UI_CONTROLS.md#ui-00780) |
| UI-00824 | No static label; inspect component context | input "file" |  | Not declared |  | [control](UI_CONTROLS.md#ui-00824) |
| UI-00827 | {localizeText('Choose your celebration for an instant invitation')} | select  | {eventId} | Not declared |  | [control](UI_CONTROLS.md#ui-00827) |
| UI-00831 | {localize("ui.f68e0b1ef1269bae",{defaultValue:"Occasion"})} | select  | {occasion} | Not declared |  | [control](UI_CONTROLS.md#ui-00831) |
| UI-00832 | {localize("ui.25bc9efedfb71cbf",{defaultValue:"Event name"})} | input  | {eventTitle} | Not declared | maxLength={200} | [control](UI_CONTROLS.md#ui-00832) |
| UI-00843 | {localize("ui.7b790708ff1b1713",{defaultValue:"Collection"})} | select  | {galleryCategory} | Not declared |  | [control](UI_CONTROLS.md#ui-00843) |
| UI-00845 | {localize("ui.e3ce6241ad144b88",{defaultValue:"Zoom "})} | select  | {scale} | Not declared |  | [control](UI_CONTROLS.md#ui-00845) |
| UI-00846 | {localize("ui.2f77668a9dfbf8d5",{defaultValue:"Message"})} | textarea  | {active.text} | Not declared |  | [control](UI_CONTROLS.md#ui-00846) |
| UI-00847 | {localize("ui.4a69d01660468781",{defaultValue:"Text color"})} | input "color" | {active.color} | Not declared |  | [control](UI_CONTROLS.md#ui-00847) |
| UI-00848 | {localize("ui.bf9a32d3cac90bed",{defaultValue:"Font style"})} | select  | {active.fontFamily\|\|"Georgia, serif"} | Not declared |  | [control](UI_CONTROLS.md#ui-00848) |
| UI-00849 | {localize("ui.6d784d95c63833e6",{defaultValue:"Font size"})} | input "range" | {active.fontSize} | Not declared | min="12"; max="70" | [control](UI_CONTROLS.md#ui-00849) |
| UI-00850 | {localize("ui.9d597870c4b3f76d",{defaultValue:"Alignment"})} | select  | {active.align} | Not declared |  | [control](UI_CONTROLS.md#ui-00850) |
| UI-00851 | {localizeText(({x:'Left',y:'Top',w:'Width',h:'Height'} as Record<string,string>)[k])} | input "number" | {active[k as 'x'\|'y'\|'w'\|'h']} | Not declared |  | [control](UI_CONTROLS.md#ui-00851) |
| UI-00854 | {localize("ui.e7d1ff81125efbcb",{defaultValue:"Background zoom: "})} {Math.round(normalizeBackgroundTransform(design.backgroundTransform).zoom*100)} % | input "range" | {Math.round(normalizeBackgroundTransform(design.backgroundTransform).zoom*100)} | Not declared | min="100"; max="300"; step="5" | [control](UI_CONTROLS.md#ui-00854) |
| UI-00856 | {localizeText('Soften the background')} | input "range" | {Math.round((design.backgroundOverlay??.53)*100)} | Not declared | min="0"; max="100" | [control](UI_CONTROLS.md#ui-00856) |
| UI-00857 | {localize("ui.3e314dafdf7c6c51",{defaultValue:"Background color"})} | input "color" | {design.background} | Not declared |  | [control](UI_CONTROLS.md#ui-00857) |
| UI-00859 | {localize("ui.4a8b403035a2b673",{defaultValue:"Celebration"})} | select  | {eventId} | Not declared |  | [control](UI_CONTROLS.md#ui-00859) |
| UI-00860 | No static label; inspect component context | input  | {eventId} | Not declared |  | [control](UI_CONTROLS.md#ui-00860) |
| UI-00862 | {localizeText('Explore the collection')} | select  | {filter} | Not declared |  | [control](UI_CONTROLS.md#ui-00862) |
| UI-00908 | {localize("ui.bba5b40bdd28c66e",{defaultValue:"Choose language"})} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00908) |
| UI-00910 | {label} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00910) |
| UI-00912 | {localize("ui.140071feea59fe87",{defaultValue:"Preferred language"})} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00912) |


### Buttons

| ID | Label / expression | Action | Submit validation / handler | Source |
| --- | --- | --- | --- | --- |
| UI-00437 |  | {onClose} | Not a native form submit or inherited from component context | [web/components/AiInvitationDesigner.tsx:31](../../../web/components/AiInvitationDesigner.tsx#L31) |
| UI-00452 | {localize("ui.ef8f7cae77294c93",{defaultValue:"Retry QR"})} | {()=>setQrRetry(v=>v+1)} | Not a native form submit or inherited from component context | [web/components/AiInvitationDesigner.tsx:31](../../../web/components/AiInvitationDesigner.tsx#L31) |
| UI-00453 | {localize("ui.2ec3ba1bb1368153",{defaultValue:"Use this design"})} | {()=>{if(qrUrl){onApply(preview!,suggestion);onClose()}}} | Not a native form submit or inherited from component context | [web/components/AiInvitationDesigner.tsx:31](../../../web/components/AiInvitationDesigner.tsx#L31) |
| UI-00454 | {localize("ui.4a0b4c8e7bbe8910",{defaultValue:"Keep my current design"})} | {onClose} | Not a native form submit or inherited from component context | [web/components/AiInvitationDesigner.tsx:31](../../../web/components/AiInvitationDesigner.tsx#L31) |
| UI-00455 | {busy?localize("ui.b56b17c7d3c34b3a",{defaultValue:"Designing your invitation…"}):suggestion?localize("ui.3f3d13862baceafc",{defaultValue:"Generate another design"}):localize("ui.d66ac0e85de2baad",{defaultValue:"Generate invitation"})} | submit / component action | Not a native form submit or inherited from component context | [web/components/AiInvitationDesigner.tsx:31](../../../web/components/AiInvitationDesigner.tsx#L31) |
| UI-00497 | {busy?localize("ui.42074396a441a347",{defaultValue:"Processing…"}):offer.amountPaise===0?localize("ui.850fbbaefb4057d0",{defaultValue:"Activate access"}):localize("ui.0e3afe9550a3bde6",{defaultValue:"{{value0}} · {{value1}} (test mode)",value0:(offer.entitled?'Renew':'Purchase'),value1:(commercialMoney(offer.amountPaise))})} | {()=>void pay()} | Not a native form submit or inherited from component context | [web/components/CommercePurchase.tsx:34](../../../web/components/CommercePurchase.tsx#L34) |
| UI-00499 | {localize("ui.087a9cc69283c135",{defaultValue:"Reload price"})} | {()=>{setRetry(v=>v+1)}} | Not a native form submit or inherited from component context | [web/components/CommercePurchase.tsx:34](../../../web/components/CommercePurchase.tsx#L34) |
| UI-00528 | Accept optional analytics | {()=>{saveConsent(true);setShow(false)}} | Not a native form submit or inherited from component context | [web/components/CookieNotice.tsx:5](../../../web/components/CookieNotice.tsx#L5) |
| UI-00529 | Reject optional analytics | {()=>{saveConsent(false);setShow(false)}} | Not a native form submit or inherited from component context | [web/components/CookieNotice.tsx:5](../../../web/components/CookieNotice.tsx#L5) |
| UI-00777 |  | {()=>onUse(asset)} | Not a native form submit or inherited from component context | [web/components/HostInvitationUploads.tsx:21](../../../web/components/HostInvitationUploads.tsx#L21) |
| UI-00781 | {localizeText(busy?'Saving…':'Save upload access')} | {()=>void save()} | Not a native form submit or inherited from component context | [web/components/HostInvitationUploads.tsx:26](../../../web/components/HostInvitationUploads.tsx#L26) |
| UI-00788 | {collapsed?<PanelLeftOpen size={20}/>:<PanelLeftClose size={20}/>} | {toggle} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:50](../../../web/components/HostWorkspace.tsx#L50) |
| UI-00791 |  | {()=>void signOut()} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:53](../../../web/components/HostWorkspace.tsx#L53) |
| UI-00792 |  | {()=>setCollapsed(true)} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:54](../../../web/components/HostWorkspace.tsx#L54) |
| UI-00818 | {localizeText(qrDownloading?'Preparing QR…':'Download QR')} | {()=>void downloadStandaloneQr()} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:182](../../../web/components/InvitationEditor.tsx#L182) |
| UI-00819 | {localize("ui.920bf770754f42cf",{defaultValue:" Design with AI"})} | {()=>setAiOpen(true)} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:183](../../../web/components/InvitationEditor.tsx#L183) |
| UI-00820 | {localizeText('Download invitation')} | {()=>void downloadArtwork()} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:184](../../../web/components/InvitationEditor.tsx#L184) |
| UI-00821 |  | {undo} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:186](../../../web/components/InvitationEditor.tsx#L186) |
| UI-00822 |  | {redo} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:186](../../../web/components/InvitationEditor.tsx#L186) |
| UI-00823 | {localize("ui.bfe3fd0809ff3230",{defaultValue:" Import draft"})} | {()=>importInput.current?.click()} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:190](../../../web/components/InvitationEditor.tsx#L190) |
| UI-00825 | {localize("ui.791a355f853121f3",{defaultValue:" Export draft"})} | {()=>{exportDraft();if(moreMenu.current)moreMenu.current.open=false}} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:191](../../../web/components/InvitationEditor.tsx#L191) |
| UI-00826 | {localize("ui.d1f59ae5b10e2c87",{defaultValue:" Reset invitation to default"})} | {()=>{if(moreMenu.current)moreMenu.current.open=false;resetDefault()}} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:192](../../../web/components/InvitationEditor.tsx#L192) |
| UI-00829 | {localizeText(label)} | {()=>setToolPanel(value)} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:197](../../../web/components/InvitationEditor.tsx#L197) |
| UI-00830 |  | {()=>setLibraryTab(id)} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:197](../../../web/components/InvitationEditor.tsx#L197) |
| UI-00833 | {localize("ui.3931578de88dbe3f",{defaultValue:" Add text or tagline"})} | {()=>{const next=addText(design);commit(next);setSelected(next.elements.at(-1)!.id)}} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:197](../../../web/components/InvitationEditor.tsx#L197) |
| UI-00834 | {e.type==='qr'?<Gift size={16}/>:<Type size={16}/>} | {()=>setSelected(e.id)} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:197](../../../web/components/InvitationEditor.tsx#L197) |
| UI-00835 | {localizeText(category)} | {()=>setDecorationCategory(category)} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:201](../../../web/components/InvitationEditor.tsx#L201) |
| UI-00836 |  | {()=>insertSticker(sticker.id)} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:202](../../../web/components/InvitationEditor.tsx#L202) |
| UI-00838 |  | {()=>insertSticker(platformDecorationId(a.id),true)} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:226](../../../web/components/InvitationEditor.tsx#L226) |
| UI-00842 |  | {()=>commit({...design,backgroundImage:src,backgroundOverlay:0})} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:269](../../../web/components/InvitationEditor.tsx#L269) |
| UI-00844 |  | {()=>commit({...design,backgroundImage:b.src,backgroundOverlay:0})} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:324](../../../web/components/InvitationEditor.tsx#L324) |
| UI-00852 | {localize("ui.246a4e073a502e68",{defaultValue:" Delete element"})} | {()=>{commit(deleteElement(design,active.id));setSelected('qr')}} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:375](../../../web/components/InvitationEditor.tsx#L375) |
| UI-00853 | {editBackground?localize("ui.3f318e4e1654bf8d",{defaultValue:"Finish positioning"}):localize("ui.e16337fe95686a20",{defaultValue:"Edit background — drag to position"})} | {()=>setEditBackground(v=>!v)} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:376](../../../web/components/InvitationEditor.tsx#L376) |
| UI-00855 | {localize("ui.11385dfe21d5ef61",{defaultValue:"Reset background position"})} | {()=>backgroundChange(resetBackgroundTransform())} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:387](../../../web/components/InvitationEditor.tsx#L387) |
| UI-00858 | {localize("ui.e0134396e5e148fb",{defaultValue:" Remove background photo"})} | {()=>commit({...design,backgroundImage:''})} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:391](../../../web/components/InvitationEditor.tsx#L391) |
| UI-00861 | {localize("ui.c3f50438f104955b",{defaultValue:" Load authenticated QR"})} | {()=>void loadQr()} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:391](../../../web/components/InvitationEditor.tsx#L391) |
| UI-00863 |  | {()=>setPreview(t)} | Not a native form submit or inherited from component context | [web/components/InvitationTemplateGallery.tsx:26](../../../web/components/InvitationTemplateGallery.tsx#L26) |
| UI-00866 |  | {()=>setPreview(null)} | Not a native form submit or inherited from component context | [web/components/InvitationTemplateGallery.tsx:26](../../../web/components/InvitationTemplateGallery.tsx#L26) |
| UI-00867 | {localizeText('Use this invitation')} | {()=>void apply(preview)} | Not a native form submit or inherited from component context | [web/components/InvitationTemplateGallery.tsx:26](../../../web/components/InvitationTemplateGallery.tsx#L26) |
| UI-00876 | Cookie preferences | {()=>window.dispatchEvent(new Event('giftwise-cookie-settings'))} | Not a native form submit or inherited from component context | [web/components/LegalFooter.tsx:4](../../../web/components/LegalFooter.tsx#L4) |


### Dialog and alert/status inventory

| ID | Role / kind | Text / label | Visibility conditions | Source |
| --- | --- | --- | --- | --- |
| UI-00436 | dialog |  | Parent/source state | [web/components/AiInvitationDesigner.tsx:31](../../../web/components/AiInvitationDesigner.tsx#L31) |
| UI-00814 | native-dialog | 'Reset the invitation design to its default template? This will discard your current visual changes, but will not change event details, gifts, or the event QR.' | Parent/source state | [web/components/InvitationEditor.tsx:118](../../../web/components/InvitationEditor.tsx#L118) |
| UI-00815 | native-dialog | 'Invalid invitation draft' | Parent/source state | [web/components/InvitationEditor.tsx:121](../../../web/components/InvitationEditor.tsx#L121) |
| UI-00816 | native-dialog | 'Enter a valid event UUID' | Parent/source state | [web/components/InvitationEditor.tsx:122](../../../web/components/InvitationEditor.tsx#L122) |
| UI-00817 | native-dialog | `Could not load QR: ${String(e)}. Backend authentication is required.` | Parent/source state | [web/components/InvitationEditor.tsx:122](../../../web/components/InvitationEditor.tsx#L122) |
| UI-00828 | native-dialog | 'Replace the current design with an invitation for this celebration?' | editedDraft.current; !linkedEventId | [web/components/InvitationEditor.tsx:197](../../../web/components/InvitationEditor.tsx#L197) |
| UI-00865 | dialog | {preview&&<><header><h2>{preview.title}</h2><button type="button" autoFocus onClick={()=>setPreview(null)} aria-label={localizeText('Close preview')}><X size={20}/></button></header><Artwork design={designFor(preview)} qrUrl={qrUrl} watermark={preview.tier==='PREMIUM'}/>{preview.tier==='PREMIUM'&&<><p>{localizeText('GiftWise watermarks cover premium previews. Unlock this template to use the clean invitation.')}</p><CommercePurchase stream="PREMIUM_TEMPLATE" resourceId={preview.id} onComplete={()=>{void refresh().then(items=>{const unlocked=items.find(t=>t.id===preview.id);if(unlocked)setPreview(unlocked)}).catch(()=>setError('Could not verify template access'))}}/></>}{(preview.tier==='FREE'\|\|preview.accessible)&&<button type="button" className="primary" onClick={()=>void apply(preview)}>{localizeText('Use this invitation')}</button>}{error&&<p role="alert">{localizeText(error)}</p>}</>} | Parent/source state | [web/components/InvitationTemplateGallery.tsx:26](../../../web/components/InvitationTemplateGallery.tsx#L26) |
| UI-00072 | "status" | {localize("ui.bed9096c411e6da3",{defaultValue:"Opening your workspace…"})} | Parent/source state | [web/app/components/HostGlobalNav.tsx:17](../../../web/app/components/HostGlobalNav.tsx#L17) |
| UI-00401 | "status" | Loading invitation designer… | Parent/source state | [web/app/studio/page.tsx:3](../../../web/app/studio/page.tsx#L3) |
| UI-00448 | "status" | {localize("ui.7b5f0c1c569b9d55",{defaultValue:"Checking AI availability…"})} | checking | [web/components/AiInvitationDesigner.tsx:31](../../../web/components/AiInvitationDesigner.tsx#L31) |
| UI-00449 | "status" | {setup} | !checking&&selected&&!available&&setup | [web/components/AiInvitationDesigner.tsx:31](../../../web/components/AiInvitationDesigner.tsx#L31) |
| UI-00450 | "alert" | {localizeText(error)} | error | [web/components/AiInvitationDesigner.tsx:31](../../../web/components/AiInvitationDesigner.tsx#L31) |
| UI-00451 | "alert" | {qrError} | qrError; preview&&suggestion | [web/components/AiInvitationDesigner.tsx:31](../../../web/components/AiInvitationDesigner.tsx#L31) |
| UI-00498 | "alert" | {localizeText(error)} | error; groupBase&&error.includes('switched off') | [web/components/CommercePurchase.tsx:34](../../../web/components/CommercePurchase.tsx#L34) |
| UI-00500 | "status" | {localizeText(notice)} | notice | [web/components/CommercePurchase.tsx:34](../../../web/components/CommercePurchase.tsx#L34) |
| UI-00548 | "status" | {localize("ui.7a005f0f1dbc704f",{defaultValue:"Opening your celebration space…"})} | ready\|\|discovery | [web/components/ExperienceProvider.tsx:19](../../../web/components/ExperienceProvider.tsx#L19) |
| UI-00778 | "alert" | {localizeText(error)} | error | [web/components/HostInvitationUploads.tsx:21](../../../web/components/HostInvitationUploads.tsx#L21) |
| UI-00782 | "status" | {localizeText('Upload access saved and audited.')} | saved | [web/components/HostInvitationUploads.tsx:26](../../../web/components/HostInvitationUploads.tsx#L26) |
| UI-00783 | "alert" | {localizeText(error)} | error | [web/components/HostInvitationUploads.tsx:26](../../../web/components/HostInvitationUploads.tsx#L26) |
| UI-00790 | "alert" | {localizeText(error)} | error | [web/components/HostWorkspace.tsx:53](../../../web/components/HostWorkspace.tsx#L53) |
| UI-00812 | "status" | Loading invitation templates… | Parent/source state | [web/components/InvitationEditor.tsx:14](../../../web/components/InvitationEditor.tsx#L14) |
| UI-00813 | "status" | Loading design assistant… | Parent/source state | [web/components/InvitationEditor.tsx:15](../../../web/components/InvitationEditor.tsx#L15) |
| UI-00840 | "alert" | {platformError} | platformError | [web/components/InvitationEditor.tsx:254](../../../web/components/InvitationEditor.tsx#L254) |
| UI-00864 | "alert" | {localizeText(error)} | error | [web/components/InvitationTemplateGallery.tsx:26](../../../web/components/InvitationTemplateGallery.tsx#L26) |
| UI-00868 | "alert" | {localizeText(error)} | error; preview | [web/components/InvitationTemplateGallery.tsx:26](../../../web/components/InvitationTemplateGallery.tsx#L26) |
| UI-00909 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:19](../../../web/components/LocalizationProvider.tsx#L19) |
| UI-00911 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:20](../../../web/components/LocalizationProvider.tsx#L20) |
| UI-00913 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:21](../../../web/components/LocalizationProvider.tsx#L21) |


### Workflow state bindings

| Source | Binding | Initial expression |
| --- | --- | --- |
| web/components/InvitationEditor.tsx | aiOpen | false |
| web/components/InvitationEditor.tsx | libraryTab | 'templates' |
| web/components/InvitationEditor.tsx | qrDownloading | false |
| web/components/InvitationEditor.tsx | platformError | '' |
| web/components/LocalizationProvider.tsx | busy | false |
| web/components/LocalizationProvider.tsx | error | '' |
| web/components/HostInvitationUploads.tsx | error | '' |
| web/components/HostInvitationUploads.tsx | busy | false |
| web/components/HostInvitationUploads.tsx | error | '' |
| web/components/HostInvitationUploads.tsx | busy | false |
| web/components/CommercePurchase.tsx | error | '' |
| web/components/CommercePurchase.tsx | busy | false |
| web/components/CommercePurchase.tsx | notice | '' |
| web/components/InvitationTemplateGallery.tsx | error | '' |
| web/components/AiInvitationDesigner.tsx | qrError | '' |
| web/components/AiInvitationDesigner.tsx | busy | false |
| web/components/AiInvitationDesigner.tsx | error | '' |
| web/components/HostWorkspace.tsx | busy | false |
| web/components/HostWorkspace.tsx | error | '' |


## Terms and conditions

| Screen information | Source-supported specification |
| --- | --- |
| Route | /terms |
| Source | [web/app/terms/page.tsx:1](../../../web/app/terms/page.tsx#L1) |
| Purpose | Read the published platform terms |
| User access level | Public |
| Navigation source | Registration consent / footer |
| Navigation destination | Privacy, contact or return navigation |
| Preconditions | Route parameters/query scope must be valid; applicable session/ownership/module/provider state above. Load errors do not imply an empty successful dataset. |
| Postconditions | Only the selected successful action changes its documented state. Navigation alone does not publish, confirm payment, verify contact or deliver a notification. |
| Route variables | No path variable |
| UI variants | Focused/general experience, responsive layout and module/state predicates can change visible sections. Exact predicates are in each control record. |


### Wireframe layout

| Section | Placement / behavior / evidence |
| --- | --- |
| Header | Shared identity/event or page title/back/actions, where declared. Composite header declarations: 2. |
| Navigation / sidebar | Host/admin navigation and event context where applicable; mobile/focused variants are conditional. nav=3, aside=3. |
| Main content | Purpose-specific forms/cards/tables/canvas/player from the composed sources. main=3, section=3, article=0. |
| Search and filters | Fields/selects listed below; exact value/handler/visibility controls are not inferred from placeholder labels. |
| Data grids and cards | Table declarations=0. Data collections and generated rows/cards are in frontend.json dynamicSources. |
| Popups / dialogs | Named/source-declared dialog control records below; opening/closing/loading/apply/cancel actions belong to those component handlers. |
| Alerts / notifications | Source controls with role alert/status are listed below. Notice strings may come from API results; do not assume a universal envelope. |
| Footer | Local/shared footer declarations=1. No undocumented legal/footer wording is invented. |
| Loading / empty / success / error | Observed state bindings and relevant action fields are listed below. Expected QA states from Document 2 do not establish a spinner/retry control where source does not implement one. |


### Composed implementation

- [web/app/terms/page.tsx:1](../../../web/app/terms/page.tsx#L1)
- [web/app/layout.tsx:1](../../../web/app/layout.tsx#L1)
- [web/components/GrowthProvider.tsx:1](../../../web/components/GrowthProvider.tsx#L1)
- [web/components/BrandingProvider.tsx:1](../../../web/components/BrandingProvider.tsx#L1)
- [web/components/LegalFooter.tsx:1](../../../web/components/LegalFooter.tsx#L1)
- [web/components/CookieNotice.tsx:1](../../../web/components/CookieNotice.tsx#L1)
- [web/app/components/ApplicationInteractions.tsx:1](../../../web/app/components/ApplicationInteractions.tsx#L1)
- [web/components/LocalizationProvider.tsx:1](../../../web/components/LocalizationProvider.tsx#L1)
- [web/components/VerificationProvider.tsx:1](../../../web/components/VerificationProvider.tsx#L1)
- [web/components/ExperienceProvider.tsx:1](../../../web/components/ExperienceProvider.tsx#L1)
- [web/app/components/PerformanceDiagnostics.tsx:1](../../../web/app/components/PerformanceDiagnostics.tsx#L1)
- [web/app/components/HostSessionRedirect.tsx:1](../../../web/app/components/HostSessionRedirect.tsx#L1)
- [web/app/components/HostGlobalNav.tsx:1](../../../web/app/components/HostGlobalNav.tsx#L1)
- [web/components/HostWorkspace.tsx:1](../../../web/components/HostWorkspace.tsx#L1)
- [web/components/focused/HostShell.tsx:1](../../../web/components/focused/HostShell.tsx#L1)

### Fields

| Control ID | Label / expression | Type | Binding | Required | Native limits / format | Detailed specification |
| --- | --- | --- | --- | --- | --- | --- |
| UI-00908 | {localize("ui.bba5b40bdd28c66e",{defaultValue:"Choose language"})} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00908) |
| UI-00910 | {label} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00910) |
| UI-00912 | {localize("ui.140071feea59fe87",{defaultValue:"Preferred language"})} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00912) |


### Buttons

| ID | Label / expression | Action | Submit validation / handler | Source |
| --- | --- | --- | --- | --- |
| UI-00528 | Accept optional analytics | {()=>{saveConsent(true);setShow(false)}} | Not a native form submit or inherited from component context | [web/components/CookieNotice.tsx:5](../../../web/components/CookieNotice.tsx#L5) |
| UI-00529 | Reject optional analytics | {()=>{saveConsent(false);setShow(false)}} | Not a native form submit or inherited from component context | [web/components/CookieNotice.tsx:5](../../../web/components/CookieNotice.tsx#L5) |
| UI-00788 | {collapsed?<PanelLeftOpen size={20}/>:<PanelLeftClose size={20}/>} | {toggle} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:50](../../../web/components/HostWorkspace.tsx#L50) |
| UI-00791 |  | {()=>void signOut()} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:53](../../../web/components/HostWorkspace.tsx#L53) |
| UI-00792 |  | {()=>setCollapsed(true)} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:54](../../../web/components/HostWorkspace.tsx#L54) |
| UI-00876 | Cookie preferences | {()=>window.dispatchEvent(new Event('giftwise-cookie-settings'))} | Not a native form submit or inherited from component context | [web/components/LegalFooter.tsx:4](../../../web/components/LegalFooter.tsx#L4) |


### Dialog and alert/status inventory

| ID | Role / kind | Text / label | Visibility conditions | Source |
| --- | --- | --- | --- | --- |
| UI-00072 | "status" | {localize("ui.bed9096c411e6da3",{defaultValue:"Opening your workspace…"})} | Parent/source state | [web/app/components/HostGlobalNav.tsx:17](../../../web/app/components/HostGlobalNav.tsx#L17) |
| UI-00548 | "status" | {localize("ui.7a005f0f1dbc704f",{defaultValue:"Opening your celebration space…"})} | ready\|\|discovery | [web/components/ExperienceProvider.tsx:19](../../../web/components/ExperienceProvider.tsx#L19) |
| UI-00790 | "alert" | {localizeText(error)} | error | [web/components/HostWorkspace.tsx:53](../../../web/components/HostWorkspace.tsx#L53) |
| UI-00909 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:19](../../../web/components/LocalizationProvider.tsx#L19) |
| UI-00911 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:20](../../../web/components/LocalizationProvider.tsx#L20) |
| UI-00913 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:21](../../../web/components/LocalizationProvider.tsx#L21) |


### Workflow state bindings

| Source | Binding | Initial expression |
| --- | --- | --- |
| web/components/LocalizationProvider.tsx | busy | false |
| web/components/LocalizationProvider.tsx | error | '' |
| web/components/HostWorkspace.tsx | busy | false |
| web/components/HostWorkspace.tsx | error | '' |


## Email verification

| Screen information | Source-supported specification |
| --- | --- |
| Route | /verify-email |
| Source | [web/app/verify-email/page.tsx:1](../../../web/app/verify-email/page.tsx#L1) |
| Purpose | Complete/inspect permitted email verification or enrollment |
| User access level | Scoped verification token/proof and applicable account context |
| Navigation source | Recovery/account verification link |
| Navigation destination | Verified or invalid/expired proof state; return to account/login |
| Preconditions | Route parameters/query scope must be valid; applicable session/ownership/module/provider state above. Load errors do not imply an empty successful dataset. |
| Postconditions | Only the selected successful action changes its documented state. Navigation alone does not publish, confirm payment, verify contact or deliver a notification. |
| Route variables | No path variable |
| UI variants | Focused/general experience, responsive layout and module/state predicates can change visible sections. Exact predicates are in each control record. |


### Wireframe layout

| Section | Placement / behavior / evidence |
| --- | --- |
| Header | Shared identity/event or page title/back/actions, where declared. Composite header declarations: 2. |
| Navigation / sidebar | Host/admin navigation and event context where applicable; mobile/focused variants are conditional. nav=3, aside=3. |
| Main content | Purpose-specific forms/cards/tables/canvas/player from the composed sources. main=4, section=3, article=0. |
| Search and filters | Fields/selects listed below; exact value/handler/visibility controls are not inferred from placeholder labels. |
| Data grids and cards | Table declarations=0. Data collections and generated rows/cards are in frontend.json dynamicSources. |
| Popups / dialogs | Named/source-declared dialog control records below; opening/closing/loading/apply/cancel actions belong to those component handlers. |
| Alerts / notifications | Source controls with role alert/status are listed below. Notice strings may come from API results; do not assume a universal envelope. |
| Footer | Local/shared footer declarations=1. No undocumented legal/footer wording is invented. |
| Loading / empty / success / error | Observed state bindings and relevant action fields are listed below. Expected QA states from Document 2 do not establish a spinner/retry control where source does not implement one. |


### Composed implementation

- [web/app/verify-email/page.tsx:1](../../../web/app/verify-email/page.tsx#L1)
- [web/components/LocalizationProvider.tsx:1](../../../web/components/LocalizationProvider.tsx#L1)
- [web/components/VerificationProvider.tsx:1](../../../web/components/VerificationProvider.tsx#L1)
- [web/app/layout.tsx:1](../../../web/app/layout.tsx#L1)
- [web/components/GrowthProvider.tsx:1](../../../web/components/GrowthProvider.tsx#L1)
- [web/components/BrandingProvider.tsx:1](../../../web/components/BrandingProvider.tsx#L1)
- [web/components/LegalFooter.tsx:1](../../../web/components/LegalFooter.tsx#L1)
- [web/components/CookieNotice.tsx:1](../../../web/components/CookieNotice.tsx#L1)
- [web/app/components/ApplicationInteractions.tsx:1](../../../web/app/components/ApplicationInteractions.tsx#L1)
- [web/components/ExperienceProvider.tsx:1](../../../web/components/ExperienceProvider.tsx#L1)
- [web/app/components/PerformanceDiagnostics.tsx:1](../../../web/app/components/PerformanceDiagnostics.tsx#L1)
- [web/app/components/HostSessionRedirect.tsx:1](../../../web/app/components/HostSessionRedirect.tsx#L1)
- [web/app/components/HostGlobalNav.tsx:1](../../../web/app/components/HostGlobalNav.tsx#L1)
- [web/components/HostWorkspace.tsx:1](../../../web/components/HostWorkspace.tsx#L1)
- [web/components/focused/HostShell.tsx:1](../../../web/components/focused/HostShell.tsx#L1)

### Fields

| Control ID | Label / expression | Type | Binding | Required | Native limits / format | Detailed specification |
| --- | --- | --- | --- | --- | --- | --- |
| UI-00412 | {localize("ui.3bcb66ee1a6feaf5",{defaultValue:"Six-digit code"})} | input  | {code} | true | maxLength={6}; pattern="[0-9]{6}" | [control](UI_CONTROLS.md#ui-00412) |
| UI-00908 | {localize("ui.bba5b40bdd28c66e",{defaultValue:"Choose language"})} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00908) |
| UI-00910 | {label} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00910) |
| UI-00912 | {localize("ui.140071feea59fe87",{defaultValue:"Preferred language"})} | select  | {catalog.selectedLanguage} | Not declared |  | [control](UI_CONTROLS.md#ui-00912) |


### Buttons

| ID | Label / expression | Action | Submit validation / handler | Source |
| --- | --- | --- | --- | --- |
| UI-00407 | {localize("ui.d9be54806e4a52b5",{defaultValue:"Check session again"})} | {()=>setRetry(v=>v+1)} | Not a native form submit or inherited from component context | [web/app/verify-email/page.tsx:38](../../../web/app/verify-email/page.tsx#L38) |
| UI-00410 | {busy?localize("ui.94b1ce97199dbd15",{defaultValue:"Sending request…"}):localize("ui.3f176c11922aa31a",{defaultValue:"Send email verification code"})} | submit / component action | {request} | [web/app/verify-email/page.tsx:38](../../../web/app/verify-email/page.tsx#L38) |
| UI-00413 | {busy?localize("ui.63bbd08c916b4e99",{defaultValue:"Verifying…"}):localize("ui.e89a77e7fe58a6d1",{defaultValue:"Verify email"})} | submit / component action | {verify} | [web/app/verify-email/page.tsx:38](../../../web/app/verify-email/page.tsx#L38) |
| UI-00528 | Accept optional analytics | {()=>{saveConsent(true);setShow(false)}} | Not a native form submit or inherited from component context | [web/components/CookieNotice.tsx:5](../../../web/components/CookieNotice.tsx#L5) |
| UI-00529 | Reject optional analytics | {()=>{saveConsent(false);setShow(false)}} | Not a native form submit or inherited from component context | [web/components/CookieNotice.tsx:5](../../../web/components/CookieNotice.tsx#L5) |
| UI-00788 | {collapsed?<PanelLeftOpen size={20}/>:<PanelLeftClose size={20}/>} | {toggle} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:50](../../../web/components/HostWorkspace.tsx#L50) |
| UI-00791 |  | {()=>void signOut()} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:53](../../../web/components/HostWorkspace.tsx#L53) |
| UI-00792 |  | {()=>setCollapsed(true)} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:54](../../../web/components/HostWorkspace.tsx#L54) |
| UI-00876 | Cookie preferences | {()=>window.dispatchEvent(new Event('giftwise-cookie-settings'))} | Not a native form submit or inherited from component context | [web/components/LegalFooter.tsx:4](../../../web/components/LegalFooter.tsx#L4) |


### Dialog and alert/status inventory

| ID | Role / kind | Text / label | Visibility conditions | Source |
| --- | --- | --- | --- | --- |
| UI-00072 | "status" | {localize("ui.bed9096c411e6da3",{defaultValue:"Opening your workspace…"})} | Parent/source state | [web/app/components/HostGlobalNav.tsx:17](../../../web/app/components/HostGlobalNav.tsx#L17) |
| UI-00405 | "status" | {localize("ui.707b5a0ae798e351",{defaultValue:"Checking your sign-in session…"})} | session==='checking' | [web/app/verify-email/page.tsx:38](../../../web/app/verify-email/page.tsx#L38) |
| UI-00414 | "alert" | {localizeText(error)} | error | [web/app/verify-email/page.tsx:38](../../../web/app/verify-email/page.tsx#L38) |
| UI-00415 | "status" | {localizeText(message)} | message | [web/app/verify-email/page.tsx:38](../../../web/app/verify-email/page.tsx#L38) |
| UI-00548 | "status" | {localize("ui.7a005f0f1dbc704f",{defaultValue:"Opening your celebration space…"})} | ready\|\|discovery | [web/components/ExperienceProvider.tsx:19](../../../web/components/ExperienceProvider.tsx#L19) |
| UI-00790 | "alert" | {localizeText(error)} | error | [web/components/HostWorkspace.tsx:53](../../../web/components/HostWorkspace.tsx#L53) |
| UI-00909 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:19](../../../web/components/LocalizationProvider.tsx#L19) |
| UI-00911 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:20](../../../web/components/LocalizationProvider.tsx#L20) |
| UI-00913 | "alert" | {localizeText(error)} | error | [web/components/LocalizationProvider.tsx:21](../../../web/components/LocalizationProvider.tsx#L21) |


### Workflow state bindings

| Source | Binding | Initial expression |
| --- | --- | --- |
| web/app/verify-email/page.tsx | message | '' |
| web/app/verify-email/page.tsx | error | '' |
| web/app/verify-email/page.tsx | busy | false |
| web/components/LocalizationProvider.tsx | busy | false |
| web/components/LocalizationProvider.tsx | error | '' |
| web/components/HostWorkspace.tsx | busy | false |
| web/components/HostWorkspace.tsx | error | '' |

