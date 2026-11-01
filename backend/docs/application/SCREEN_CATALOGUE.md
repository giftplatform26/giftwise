# Complete screen and wireframe catalogue

All 27 App Router `page.tsx` routes are included. Route variables are placeholders, not example authorization. Composed controls include shared layout imports, re-exports and local dynamic imports; visibility expressions determine what actually renders. This is a source-backed wireframe/control reference, not a claim that every shared component appears simultaneously.

Detailed field/button specifications are in [UI_CONTROLS.md](UI_CONTROLS.md). The source metadata in [frontend.json](catalogue/frontend.json) also records complete attributes, conditions, state expressions, request arguments and dynamic collections.

| Route | Screen | Access |
| --- | --- | --- |
| / | Invitation studio | Client-rendered design workspace; authenticated owner required for protected QR/design/AI APIs |
| /admin | Administration console | Authenticated PLATFORM_ADMIN |
| /admin/invitation-assets | Invitation asset gallery administration | Authenticated PLATFORM_ADMIN |
| /dashboard | Host dashboard | Authenticated CUSTOMER; admin has its own console |
| /e/[token] | Public guest invitation | Valid published token; per-feature/channel/readiness checks |
| /events | Celebrations | Authenticated owner for event reads/writes |
| /events/[id] | Celebration overview | Authenticated event owner |
| /events/[id]/album | Private memory album | Authenticated owner; platform + host + event MEMORY_ALBUM grants |
| /events/[id]/celebration | Guests, responses and wishes | Authenticated event owner; RSVP/GUESTBOOK grants |
| /events/[id]/contributions | Contribution report | Authenticated event owner |
| /events/[id]/notifications | Activity and notifications | Authenticated event owner |
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
| /register | Host registration | Public subject to registration/contact policies |
| /verify-email | Email verification | Scoped verification token/proof and applicable account context |


## Invitation studio

| Screen information | Source-supported specification |
| --- | --- |
| Route | / |
| Source | [web/app/page.tsx:1](../../../web/app/page.tsx#L1) |
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

- [web/app/page.tsx:1](../../../web/app/page.tsx#L1)
- [web/components/InvitationEditor.tsx:1](../../../web/components/InvitationEditor.tsx#L1)
- [web/components/AiInvitationDesigner.tsx:1](../../../web/components/AiInvitationDesigner.tsx#L1)
- [web/components/ExperienceProvider.tsx:1](../../../web/components/ExperienceProvider.tsx#L1)
- [web/app/layout.tsx:1](../../../web/app/layout.tsx#L1)
- [web/components/VerificationProvider.tsx:1](../../../web/components/VerificationProvider.tsx#L1)
- [web/app/components/PerformanceDiagnostics.tsx:1](../../../web/app/components/PerformanceDiagnostics.tsx#L1)
- [web/app/components/HostSessionRedirect.tsx:1](../../../web/app/components/HostSessionRedirect.tsx#L1)
- [web/app/components/HostGlobalNav.tsx:1](../../../web/app/components/HostGlobalNav.tsx#L1)
- [web/components/HostWorkspace.tsx:1](../../../web/components/HostWorkspace.tsx#L1)
- [web/components/focused/HostShell.tsx:1](../../../web/components/focused/HostShell.tsx#L1)

### Fields

| Control ID | Label / expression | Type | Binding | Required | Native limits / format | Detailed specification |
| --- | --- | --- | --- | --- | --- | --- |
| UI-00394 | Celebration | select  | {selected} | true |  | [control](UI_CONTROLS.md#ui-00394) |
| UI-00395 | Visual style | select  | {form.visualStyle} | Not declared |  | [control](UI_CONTROLS.md#ui-00395) |
| UI-00396 | Tone | select  | {form.tone} | Not declared |  | [control](UI_CONTROLS.md#ui-00396) |
| UI-00397 | Language | input  | {form.language} | true | maxLength={40} | [control](UI_CONTROLS.md#ui-00397) |
| UI-00398 | Names shown on the invitation | input  | {form.displayNames} | Not declared | maxLength={200} | [control](UI_CONTROLS.md#ui-00398) |
| UI-00399 | Public venue | input  | {form.publicVenue} | Not declared | maxLength={160} | [control](UI_CONTROLS.md#ui-00399) |
| UI-00400 | Time | input  | {form.startTime} | Not declared | maxLength={40} | [control](UI_CONTROLS.md#ui-00400) |
| UI-00401 | A personal touch | textarea  | {form.specialMessage} | Not declared | maxLength={400} | [control](UI_CONTROLS.md#ui-00401) |
| UI-00699 | No static label; inspect component context | input "file" |  | Not declared |  | [control](UI_CONTROLS.md#ui-00699) |
| UI-00703 | Occasion | select  | {occasion} | Not declared |  | [control](UI_CONTROLS.md#ui-00703) |
| UI-00704 | Event name | input  | {eventTitle} | Not declared | maxLength={200} | [control](UI_CONTROLS.md#ui-00704) |
| UI-00714 | Collection | select  | {galleryCategory} | Not declared |  | [control](UI_CONTROLS.md#ui-00714) |
| UI-00716 | Zoom | select  | {scale} | Not declared |  | [control](UI_CONTROLS.md#ui-00716) |
| UI-00717 | Message | textarea  | {active.text} | Not declared |  | [control](UI_CONTROLS.md#ui-00717) |
| UI-00718 | Text color | input "color" | {active.color} | Not declared |  | [control](UI_CONTROLS.md#ui-00718) |
| UI-00719 | Font style | select  | {active.fontFamily\|\|"Georgia, serif"} | Not declared |  | [control](UI_CONTROLS.md#ui-00719) |
| UI-00720 | Font size | input "range" | {active.fontSize} | Not declared | min="12"; max="70" | [control](UI_CONTROLS.md#ui-00720) |
| UI-00721 | Alignment | select  | {active.align} | Not declared |  | [control](UI_CONTROLS.md#ui-00721) |
| UI-00722 | {({x:'Left',y:'Top',w:'Width',h:'Height'} as Record<string,string>)[k]} | input "number" | {active[k as 'x'\|'y'\|'w'\|'h']} | Not declared |  | [control](UI_CONTROLS.md#ui-00722) |
| UI-00725 | Background zoom: {Math.round(normalizeBackgroundTransform(design.backgroundTransform).zoom*100)} % | input "range" | {Math.round(normalizeBackgroundTransform(design.backgroundTransform).zoom*100)} | Not declared | min="100"; max="300"; step="5" | [control](UI_CONTROLS.md#ui-00725) |
| UI-00727 | Upload photo | input "file" |  | Not declared |  | [control](UI_CONTROLS.md#ui-00727) |
| UI-00728 | Background color | input "color" | {design.background} | Not declared |  | [control](UI_CONTROLS.md#ui-00728) |
| UI-00730 | Celebration | select  | {eventId} | Not declared |  | [control](UI_CONTROLS.md#ui-00730) |
| UI-00731 | No static label; inspect component context | input  | {eventId} | Not declared |  | [control](UI_CONTROLS.md#ui-00731) |


### Buttons

| ID | Label / expression | Action | Submit validation / handler | Source |
| --- | --- | --- | --- | --- |
| UI-00391 |  | {onClose} | Not a native form submit or inherited from component context | [web/components/AiInvitationDesigner.tsx:27](../../../web/components/AiInvitationDesigner.tsx#L27) |
| UI-00406 | Retry QR | {()=>setQrRetry(v=>v+1)} | Not a native form submit or inherited from component context | [web/components/AiInvitationDesigner.tsx:27](../../../web/components/AiInvitationDesigner.tsx#L27) |
| UI-00407 | Use this design | {()=>{if(qrUrl){onApply(preview!,suggestion);onClose()}}} | Not a native form submit or inherited from component context | [web/components/AiInvitationDesigner.tsx:27](../../../web/components/AiInvitationDesigner.tsx#L27) |
| UI-00408 | Keep my current design | {onClose} | Not a native form submit or inherited from component context | [web/components/AiInvitationDesigner.tsx:27](../../../web/components/AiInvitationDesigner.tsx#L27) |
| UI-00409 | {busy?'Designing your invitation…':suggestion?'Generate another design':'Generate invitation'} | submit / component action | Not a native form submit or inherited from component context | [web/components/AiInvitationDesigner.tsx:27](../../../web/components/AiInvitationDesigner.tsx#L27) |
| UI-00664 | {collapsed?<PanelLeftOpen size={20}/>:<PanelLeftClose size={20}/>} | {toggle} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:43](../../../web/components/HostWorkspace.tsx#L43) |
| UI-00667 |  | {()=>void signOut()} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:46](../../../web/components/HostWorkspace.tsx#L46) |
| UI-00668 |  | {()=>setCollapsed(true)} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:47](../../../web/components/HostWorkspace.tsx#L47) |
| UI-00694 | Design with AI | {()=>setAiOpen(true)} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:176](../../../web/components/InvitationEditor.tsx#L176) |
| UI-00695 | Download PNG | {()=>void downloadArtwork()} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:177](../../../web/components/InvitationEditor.tsx#L177) |
| UI-00696 |  | {undo} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:179](../../../web/components/InvitationEditor.tsx#L179) |
| UI-00697 |  | {redo} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:179](../../../web/components/InvitationEditor.tsx#L179) |
| UI-00698 | Import draft | {()=>importInput.current?.click()} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:183](../../../web/components/InvitationEditor.tsx#L183) |
| UI-00700 | Export draft | {()=>{exportDraft();if(moreMenu.current)moreMenu.current.open=false}} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:184](../../../web/components/InvitationEditor.tsx#L184) |
| UI-00701 | Reset invitation to default | {()=>{if(moreMenu.current)moreMenu.current.open=false;resetDefault()}} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:185](../../../web/components/InvitationEditor.tsx#L185) |
| UI-00702 | {label} | {()=>setToolPanel(value)} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:190](../../../web/components/InvitationEditor.tsx#L190) |
| UI-00705 | Add text or tagline | {()=>{const next=addText(design);commit(next);setSelected(next.elements.at(-1)!.id)}} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:190](../../../web/components/InvitationEditor.tsx#L190) |
| UI-00706 | {e.type==='qr'?<Gift size={16}/>:<Type size={16}/>} | {()=>setSelected(e.id)} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:190](../../../web/components/InvitationEditor.tsx#L190) |
| UI-00707 |  | {()=>insertSticker(sticker.id)} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:195](../../../web/components/InvitationEditor.tsx#L195) |
| UI-00709 |  | {()=>insertSticker(platformDecorationId(a.id),!!linkedEventId)} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:219](../../../web/components/InvitationEditor.tsx#L219) |
| UI-00713 |  | {()=>commit({...design,backgroundImage:src})} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:262](../../../web/components/InvitationEditor.tsx#L262) |
| UI-00715 |  | {()=>commit({...design,backgroundImage:b.src})} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:318](../../../web/components/InvitationEditor.tsx#L318) |
| UI-00723 | Delete element | {()=>{commit(deleteElement(design,active.id));setSelected('qr')}} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:371](../../../web/components/InvitationEditor.tsx#L371) |
| UI-00724 | {editBackground?'Finish positioning':'Edit background — drag to position'} | {()=>setEditBackground(v=>!v)} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:372](../../../web/components/InvitationEditor.tsx#L372) |
| UI-00726 | Reset background position | {()=>backgroundChange(resetBackgroundTransform())} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:383](../../../web/components/InvitationEditor.tsx#L383) |
| UI-00729 | Remove background photo | {()=>commit({...design,backgroundImage:''})} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:388](../../../web/components/InvitationEditor.tsx#L388) |
| UI-00732 | Load authenticated QR | {()=>void loadQr()} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:388](../../../web/components/InvitationEditor.tsx#L388) |


### Dialog and alert/status inventory

| ID | Role / kind | Text / label | Visibility conditions | Source |
| --- | --- | --- | --- | --- |
| UI-00390 | dialog |  | Parent/source state | [web/components/AiInvitationDesigner.tsx:27](../../../web/components/AiInvitationDesigner.tsx#L27) |
| UI-00688 | native-dialog | 'Choose a PNG, JPG or WebP under 3 MB.' | Parent/source state | [web/components/InvitationEditor.tsx:115](../../../web/components/InvitationEditor.tsx#L115) |
| UI-00689 | native-dialog | 'Image dimensions must be at most 4096 pixels.' | Parent/source state | [web/components/InvitationEditor.tsx:115](../../../web/components/InvitationEditor.tsx#L115) |
| UI-00690 | native-dialog | 'Reset the invitation design to its default template? This will discard your current visual changes, but will not change event details, gifts, or the event QR.' | Parent/source state | [web/components/InvitationEditor.tsx:116](../../../web/components/InvitationEditor.tsx#L116) |
| UI-00691 | native-dialog | 'Invalid invitation draft' | Parent/source state | [web/components/InvitationEditor.tsx:119](../../../web/components/InvitationEditor.tsx#L119) |
| UI-00692 | native-dialog | 'Enter a valid event UUID' | Parent/source state | [web/components/InvitationEditor.tsx:120](../../../web/components/InvitationEditor.tsx#L120) |
| UI-00693 | native-dialog | `Could not load QR: ${String(e)}. Backend authentication is required.` | Parent/source state | [web/components/InvitationEditor.tsx:120](../../../web/components/InvitationEditor.tsx#L120) |
| UI-00053 | "status" | Opening your workspace… | Parent/source state | [web/app/components/HostGlobalNav.tsx:13](../../../web/app/components/HostGlobalNav.tsx#L13) |
| UI-00402 | "status" | Checking AI availability… | checking | [web/components/AiInvitationDesigner.tsx:27](../../../web/components/AiInvitationDesigner.tsx#L27) |
| UI-00403 | "status" | {setup} | !checking&&selected&&!available&&setup | [web/components/AiInvitationDesigner.tsx:27](../../../web/components/AiInvitationDesigner.tsx#L27) |
| UI-00404 | "alert" | {error} | error | [web/components/AiInvitationDesigner.tsx:27](../../../web/components/AiInvitationDesigner.tsx#L27) |
| UI-00405 | "alert" | {qrError} | qrError; preview&&suggestion | [web/components/AiInvitationDesigner.tsx:27](../../../web/components/AiInvitationDesigner.tsx#L27) |
| UI-00444 | "status" | Opening your celebration space… | ready | [web/components/ExperienceProvider.tsx:13](../../../web/components/ExperienceProvider.tsx#L13) |
| UI-00666 | "alert" | {error} | error | [web/components/HostWorkspace.tsx:46](../../../web/components/HostWorkspace.tsx#L46) |
| UI-00711 | "alert" | {platformError} | platformError | [web/components/InvitationEditor.tsx:247](../../../web/components/InvitationEditor.tsx#L247) |


### Workflow state bindings

| Source | Binding | Initial expression |
| --- | --- | --- |
| web/components/InvitationEditor.tsx | aiOpen | false |
| web/components/InvitationEditor.tsx | platformError | '' |
| web/components/AiInvitationDesigner.tsx | qrError | '' |
| web/components/AiInvitationDesigner.tsx | busy | false |
| web/components/AiInvitationDesigner.tsx | error | '' |
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
| Header | Shared identity/event or page title/back/actions, where declared. Composite header declarations: 2. |
| Navigation / sidebar | Host/admin navigation and event context where applicable; mobile/focused variants are conditional. nav=4, aside=2. |
| Main content | Purpose-specific forms/cards/tables/canvas/player from the composed sources. main=3, section=30, article=4. |
| Search and filters | Fields/selects listed below; exact value/handler/visibility controls are not inferred from placeholder labels. |
| Data grids and cards | Table declarations=2. Data collections and generated rows/cards are in frontend.json dynamicSources. |
| Popups / dialogs | Named/source-declared dialog control records below; opening/closing/loading/apply/cancel actions belong to those component handlers. |
| Alerts / notifications | Source controls with role alert/status are listed below. Notice strings may come from API results; do not assume a universal envelope. |
| Footer | Local/shared footer declarations=1. No undocumented legal/footer wording is invented. |
| Loading / empty / success / error | Observed state bindings and relevant action fields are listed below. Expected QA states from Document 2 do not establish a spinner/retry control where source does not implement one. |


### Composed implementation

- [web/app/admin/page.tsx:1](../../../web/app/admin/page.tsx#L1)
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
- [web/components/MonetizationControls.tsx:1](../../../web/components/MonetizationControls.tsx#L1)
- [web/components/CommercialAdminControls.tsx:1](../../../web/components/CommercialAdminControls.tsx#L1)
- [web/components/CommercePurchase.tsx:1](../../../web/components/CommercePurchase.tsx#L1)
- [web/components/PaymentControls.tsx:1](../../../web/components/PaymentControls.tsx#L1)
- [web/components/RoutingCommissionControl.tsx:1](../../../web/components/RoutingCommissionControl.tsx#L1)
- [web/app/layout.tsx:1](../../../web/app/layout.tsx#L1)
- [web/app/components/PerformanceDiagnostics.tsx:1](../../../web/app/components/PerformanceDiagnostics.tsx#L1)
- [web/app/components/HostSessionRedirect.tsx:1](../../../web/app/components/HostSessionRedirect.tsx#L1)
- [web/app/components/HostGlobalNav.tsx:1](../../../web/app/components/HostGlobalNav.tsx#L1)
- [web/components/HostWorkspace.tsx:1](../../../web/components/HostWorkspace.tsx#L1)
- [web/components/focused/HostShell.tsx:1](../../../web/components/focused/HostShell.tsx#L1)

### Fields

| Control ID | Label / expression | Type | Binding | Required | Native limits / format | Detailed specification |
| --- | --- | --- | --- | --- | --- | --- |
| UI-00001 | "Policy level" | select  | {scope} | Not declared |  | [control](UI_CONTROLS.md#ui-00001) |
| UI-00002 | "Policy host" | select  | {host} | Not declared |  | [control](UI_CONTROLS.md#ui-00002) |
| UI-00003 | "Policy event" | select  | {event} | Not declared |  | [control](UI_CONTROLS.md#ui-00003) |
| UI-00004 |  | input "checkbox" | {!!value?.[f.key]} | Not declared |  | [control](UI_CONTROLS.md#ui-00004) |
| UI-00009 | Keyword | input "search" | {draft.search} | Not declared | maxLength={200} | [control](UI_CONTROLS.md#ui-00009) |
| UI-00010 | From date (UTC) | input "date" | {draft.from} | Not declared | max={draft.to\|\|'9999-12-31'} | [control](UI_CONTROLS.md#ui-00010) |
| UI-00011 | To date (UTC) | input "date" | {draft.to} | Not declared | min={draft.from\|\|undefined}; max="9999-12-31" | [control](UI_CONTROLS.md#ui-00011) |
| UI-00012 | Action | select  | {draft.action} | Not declared |  | [control](UI_CONTROLS.md#ui-00012) |
| UI-00013 | Module | select  | {draft.module} | Not declared |  | [control](UI_CONTROLS.md#ui-00013) |
| UI-00047 |  | input "checkbox" | {m.enabled} | Not declared |  | [control](UI_CONTROLS.md#ui-00047) |
| UI-00048 | "Search hosts" | input  | {search} | Not declared |  | [control](UI_CONTROLS.md#ui-00048) |
| UI-00049 | "Select host" | select  | {selected} | Not declared |  | [control](UI_CONTROLS.md#ui-00049) |
| UI-00050 |  | input "checkbox" | {m.host_enabled} | Not declared |  | [control](UI_CONTROLS.md#ui-00050) |
| UI-00051 | {`Enable memory album for ${e.title}`} | input "checkbox" | {e.album_enabled} | Not declared |  | [control](UI_CONTROLS.md#ui-00051) |
| UI-00052 | {`Enable Design with AI for ${e.title}`} | input "checkbox" | {e.ai_enabled} | Not declared |  | [control](UI_CONTROLS.md#ui-00052) |
| UI-00375 | Email address | input "email" | {draftEmail} | true | maxLength={254} | [control](UI_CONTROLS.md#ui-00375) |
| UI-00380 | Six-digit code | input  | {code} | true | maxLength={6}; pattern="[0-9]{6}" | [control](UI_CONTROLS.md#ui-00380) |
| UI-00417 | {label} | input "number" | {policy[key]} | true | min={key==='free_active_events'?0:1}; max={key==='free_active_events'?1000:3660} | [control](UI_CONTROLS.md#ui-00417) |
| UI-00418 | Change reason | input  | {reason} | true | maxLength={500} | [control](UI_CONTROLS.md#ui-00418) |
| UI-00421 | {k} | input  | {partner[k]} | {!['clickParameter','redirectTemplate'].includes(k)} | maxLength={k==='redirectTemplate'?2048:k==='productDomain'?253:k==='name'?100:k.endsWith('Name')\|\|k==='clickParameter'?60:500} | [control](UI_CONTROLS.md#ui-00421) |
| UI-00422 | Enable partner attribution | input "checkbox" | {partner.enabled} | Not declared |  | [control](UI_CONTROLS.md#ui-00422) |
| UI-00427 | Partner | select  | {statement.partnerId} | true |  | [control](UI_CONTROLS.md#ui-00427) |
| UI-00428 | {k} | input {k==='amountRupees'?'number':'text'} | {statement[k]} | {k!=='clickId'} | min={k==='amountRupees'?'0.01':undefined}; step={k==='amountRupees'?'0.01':undefined} | [control](UI_CONTROLS.md#ui-00428) |
| UI-00429 | Statement status | select  | {statement.state} | Not declared |  | [control](UI_CONTROLS.md#ui-00429) |
| UI-00431 | {`Reason for updating ${c.external_reference}`} | input  | {c.changeReason\|\|''} | Not declared |  | [control](UI_CONTROLS.md#ui-00431) |
| UI-00435 | Review reason | input  | {p.reviewReason\|\|''} | Not declared |  | [control](UI_CONTROLS.md#ui-00435) |
| UI-00569 | Require group members to verify email | input "checkbox" | {!!(hostId?policy?.admin_enabled:policy?.host_enabled)} | Not declared |  | [control](UI_CONTROLS.md#ui-00569) |
| UI-00749 | Mobile number with country code | input "tel" | {mobile} | true | pattern="\+[1-9][0-9]{7,14}" | [control](UI_CONTROLS.md#ui-00749) |
| UI-00750 | Six-digit verification code | input  | {code} | true | maxLength={6}; pattern="[0-9]{6}" | [control](UI_CONTROLS.md#ui-00750) |
| UI-00758 | Enable platform monetization | input "checkbox" | {settings.enabled} | Not declared |  | [control](UI_CONTROLS.md#ui-00758) |
| UI-00759 | Enable this stream | input "checkbox" | {s.enabled} | Not declared |  | [control](UI_CONTROLS.md#ui-00759) |
| UI-00760 | Pricing method | select  | {s.feeMode} | Not declared |  | [control](UI_CONTROLS.md#ui-00760) |
| UI-00761 | Fixed price (₹) | input "number" | {s.fixedPaise/100} | true | min="0"; max="1000000"; step="0.01" | [control](UI_CONTROLS.md#ui-00761) |
| UI-00762 | Percentage (%) | input "number" | {s.rateBps/100} | true | min="0"; max="100"; step="0.01" | [control](UI_CONTROLS.md#ui-00762) |
| UI-00763 | Maximum fee (₹ · 0 means no cap) | input "number" | {s.capPaise/100} | true | min="0"; max="1000000"; step="0.01" | [control](UI_CONTROLS.md#ui-00763) |
| UI-00764 | Who pays the fee? | select  | {s.feeBearer} | Not declared |  | [control](UI_CONTROLS.md#ui-00764) |
| UI-00765 | Reason for change | textarea  | {reason} | true | maxLength={500} | [control](UI_CONTROLS.md#ui-00765) |
| UI-00770 | Scope | select  | {scope} | Not declared |  | [control](UI_CONTROLS.md#ui-00770) |
| UI-00771 | {scope==='HOST'?'Host':'Event'} | select  | {target} | Not declared |  | [control](UI_CONTROLS.md#ui-00771) |
| UI-00772 | Reason for change | input  | {reason} | true | maxLength={500} | [control](UI_CONTROLS.md#ui-00772) |
| UI-00773 |  | select  | {c.configured===null?'INHERIT':c.configured?'ON':'OFF'} | Not declared |  | [control](UI_CONTROLS.md#ui-00773) |
| UI-00776 | Enable mobile OTP across the application | input "checkbox" | {value?.settings.mobileOtpEnabled\|\|false} | Not declared |  | [control](UI_CONTROLS.md#ui-00776) |
| UI-00777 | Bypass email verification across the application | input "checkbox" | {!value?.settings.emailVerificationEnabled} | Not declared |  | [control](UI_CONTROLS.md#ui-00777) |
| UI-00789 | Platform routing commission (%) | input "number" | {percentage} | true | min="0"; max="100"; step="0.01" | [control](UI_CONTROLS.md#ui-00789) |
| UI-00790 | Reason for change | textarea  | {reason} | true | maxLength={500} | [control](UI_CONTROLS.md#ui-00790) |
| UI-00889 |  | input "radio" | {value} | Not declared |  | [control](UI_CONTROLS.md#ui-00889) |


### Buttons

| ID | Label / expression | Action | Submit validation / handler | Source |
| --- | --- | --- | --- | --- |
| UI-00007 | {busy?'Saving…':'Save policy'} | {()=>void save()} | Not a native form submit or inherited from component context | [web/app/admin/AccountPolicies.tsx:52](../../../web/app/admin/AccountPolicies.tsx#L52) |
| UI-00014 | Apply filters | submit / component action | {apply} | [web/app/admin/AdminAudit.tsx:23](../../../web/app/admin/AdminAudit.tsx#L23) |
| UI-00015 | Clear filters | {()=>{setDraft(empty);setFilters(empty);setPage(0)}} | {apply} | [web/app/admin/AdminAudit.tsx:23](../../../web/app/admin/AdminAudit.tsx#L23) |
| UI-00017 | Retry | {()=>setReload(value=>value+1)} | Not a native form submit or inherited from component context | [web/app/admin/AdminAudit.tsx:23](../../../web/app/admin/AdminAudit.tsx#L23) |
| UI-00020 | Previous | {()=>setPage(value=>Math.max(0,value-1))} | Not a native form submit or inherited from component context | [web/app/admin/AdminAudit.tsx:23](../../../web/app/admin/AdminAudit.tsx#L23) |
| UI-00021 | Next | {()=>setPage(value=>value+1)} | Not a native form submit or inherited from component context | [web/app/admin/AdminAudit.tsx:23](../../../web/app/admin/AdminAudit.tsx#L23) |
| UI-00038 | {label} | {()=>setTab(id)} | Not a native form submit or inherited from component context | [web/app/admin/page.tsx:68](../../../web/app/admin/page.tsx#L68) |
| UI-00040 | Refresh | {()=>void refresh()} | Not a native form submit or inherited from component context | [web/app/admin/page.tsx:68](../../../web/app/admin/page.tsx#L68) |
| UI-00041 | Sign out | {()=>void signout()} | Not a native form submit or inherited from component context | [web/app/admin/page.tsx:68](../../../web/app/admin/page.tsx#L68) |
| UI-00044 | Manage privileges → | {()=>setTab('hosts')} | Not a native form submit or inherited from component context | [web/app/admin/page.tsx:69](../../../web/app/admin/page.tsx#L69) |
| UI-00045 | View reports → | {()=>setTab('reports')} | Not a native form submit or inherited from component context | [web/app/admin/page.tsx:69](../../../web/app/admin/page.tsx#L69) |
| UI-00376 | {busy?'Saving…':'Save email'} | submit / component action | {updateEmail} | [web/components/AccountEmailVerification.tsx:15](../../../web/components/AccountEmailVerification.tsx#L15) |
| UI-00377 | Cancel | {()=>setEditing(false)} | {updateEmail} | [web/components/AccountEmailVerification.tsx:15](../../../web/components/AccountEmailVerification.tsx#L15) |
| UI-00378 | Update email address | {()=>{setDraftEmail(email);setEditing(true);setError('');setMessage('')}} | Not a native form submit or inherited from component context | [web/components/AccountEmailVerification.tsx:15](../../../web/components/AccountEmailVerification.tsx#L15) |
| UI-00381 | {busy?'Verifying…':'Verify email'} | submit / component action | {finish} | [web/components/AccountEmailVerification.tsx:15](../../../web/components/AccountEmailVerification.tsx#L15) |
| UI-00382 | {busy?'Sending…':'Send verification code'} | {()=>void send()} | Not a native form submit or inherited from component context | [web/components/AccountEmailVerification.tsx:15](../../../web/components/AccountEmailVerification.tsx#L15) |
| UI-00385 | Refresh | {()=>setRetry(v=>v+1)} | Not a native form submit or inherited from component context | [web/components/AccountEmailVerification.tsx:15](../../../web/components/AccountEmailVerification.tsx#L15) |
| UI-00386 | {open?'Close AI details':'View AI design details'} | {()=>setOpen(value=>!value)} | Not a native form submit or inherited from component context | [web/components/AdminAiDesignDetails.tsx:8](../../../web/components/AdminAiDesignDetails.tsx#L8) |
| UI-00389 | Refresh AI details | {()=>void load()} | Not a native form submit or inherited from component context | [web/components/AdminAiDesignDetails.tsx:8](../../../web/components/AdminAiDesignDetails.tsx#L8) |
| UI-00410 | {busy?'Processing…':offer.amountPaise===0?'Activate access':`${offer.entitled?'Renew':'Purchase'} · ${commercialMoney(offer.amountPaise)} (test mode)`} | {()=>void pay()} | Not a native form submit or inherited from component context | [web/components/CommercePurchase.tsx:27](../../../web/components/CommercePurchase.tsx#L27) |
| UI-00412 | Reload price | {()=>{setRetry(v=>v+1)}} | Not a native form submit or inherited from component context | [web/components/CommercePurchase.tsx:27](../../../web/components/CommercePurchase.tsx#L27) |
| UI-00415 | Refresh commercial setup | {()=>void load()} | Not a native form submit or inherited from component context | [web/components/CommercialAdminControls.tsx:12](../../../web/components/CommercialAdminControls.tsx#L12) |
| UI-00419 | Save policy | submit / component action | {e=>{e.preventDefault();void save('/policy',{revision:policy.revision,freeActiveEvents:Number(policy.free_active_events),hostPlanDays:Number(policy.host_plan_days),vendorDays:Number(policy.vendor_days),sponsorDays:Number(policy.sponsor_days),reason})}} | [web/components/CommercialAdminControls.tsx:12](../../../web/components/CommercialAdminControls.tsx#L12) |
| UI-00423 | Save partner | submit / component action | {async e=>{e.preventDefault();if(await save('/partners',partner))setPartner(partnerInitial)}} | [web/components/CommercialAdminControls.tsx:12](../../../web/components/CommercialAdminControls.tsx#L12) |
| UI-00424 | New partner | {()=>setPartner(partnerInitial)} | {async e=>{e.preventDefault();if(await save('/partners',partner))setPartner(partnerInitial)}} | [web/components/CommercialAdminControls.tsx:12](../../../web/components/CommercialAdminControls.tsx#L12) |
| UI-00425 | Edit | {()=>setPartner({id:p.id,revision:p.revision,name:p.name,productDomain:p.product_domain,enabled:p.enabled,parameterName:p.parameter_name,parameterValue:p.parameter_value,clickParameter:p.click_parameter\|\|'',redirectTemplate:p.redirect_template\|\|'',reason:''})} | Not a native form submit or inherited from component context | [web/components/CommercialAdminControls.tsx:12](../../../web/components/CommercialAdminControls.tsx#L12) |
| UI-00430 | Record statement | submit / component action | {e=>{e.preventDefault();void save('/commissions',{...statement,clickId:statement.clickId\|\|null,amountPaise:Math.round(Number(statement.amountRupees)*100),revision:0})}} | [web/components/CommercialAdminControls.tsx:12](../../../web/components/CommercialAdminControls.tsx#L12) |
| UI-00432 | Mark partner reported paid | {()=>void save('/commissions',{id:c.id,revision:c.revision,partnerId:c.partner_id,clickId:c.click_id,externalReference:c.external_reference,amountPaise:c.amount_paise,state:'PAID',reason:c.changeReason})} | Not a native form submit or inherited from component context | [web/components/CommercialAdminControls.tsx:12](../../../web/components/CommercialAdminControls.tsx#L12) |
| UI-00433 | Record reversal | {()=>void save('/commissions',{id:c.id,revision:c.revision,partnerId:c.partner_id,clickId:c.click_id,externalReference:c.external_reference,amountPaise:c.amount_paise,state:'REVERSED',reason:c.changeReason})} | Not a native form submit or inherited from component context | [web/components/CommercialAdminControls.tsx:12](../../../web/components/CommercialAdminControls.tsx#L12) |
| UI-00436 | {state==='APPROVED'?'Approve':'Reject / remove'} | {()=>void save(`/placements/${p.id}`,{state,reason:p.reviewReason},'POST')} | Not a native form submit or inherited from component context | [web/components/CommercialAdminControls.tsx:12](../../../web/components/CommercialAdminControls.tsx#L12) |
| UI-00664 | {collapsed?<PanelLeftOpen size={20}/>:<PanelLeftClose size={20}/>} | {toggle} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:43](../../../web/components/HostWorkspace.tsx#L43) |
| UI-00667 |  | {()=>void signOut()} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:46](../../../web/components/HostWorkspace.tsx#L46) |
| UI-00668 |  | {()=>setCollapsed(true)} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:47](../../../web/components/HostWorkspace.tsx#L47) |
| UI-00747 | Use a different mobile number | {()=>{setVerified(false);setMessage('')}} | Not a native form submit or inherited from component context | [web/components/MobileVerification.tsx:13](../../../web/components/MobileVerification.tsx#L13) |
| UI-00751 | {busy?'Please wait…':challenge?'Verify mobile':'Send verification code'} | submit / component action | {submit} | [web/components/MobileVerification.tsx:13](../../../web/components/MobileVerification.tsx#L13) |
| UI-00752 | Change number / request another code | {()=>{setChallenge('');setCode('');setMessage('Wait at least 60 seconds before requesting another code.')}} | {submit} | [web/components/MobileVerification.tsx:13](../../../web/components/MobileVerification.tsx#L13) |
| UI-00755 | Reload saved settings | {()=>void load()} | Not a native form submit or inherited from component context | [web/components/MonetizationControls.tsx:20](../../../web/components/MonetizationControls.tsx#L20) |
| UI-00766 | Save monetization setup | submit / component action | {save} | [web/components/MonetizationControls.tsx:20](../../../web/components/MonetizationControls.tsx#L20) |
| UI-00768 | Load earlier history | {async()=>{setBusy(true);try{const next=await api<History[]>(`/history?beforeRevision=${history[history.length-1].revision}`);setHistory([...history,...next]);}catch(e){setError(String(e))}finally{setBusy(false)}}} | Not a native form submit or inherited from component context | [web/components/MonetizationControls.tsx:20](../../../web/components/MonetizationControls.tsx#L20) |
| UI-00769 | Reload payment controls | {async()=>{setBusy(true);try{setView(await commercialApi<View>(base+'/controls'+query));await historyLoad();setError('')}catch(e){setError(String(e))}finally{setBusy(false)}}} | Not a native form submit or inherited from component context | [web/components/PaymentControls.tsx:18](../../../web/components/PaymentControls.tsx#L18) |
| UI-00775 | Load earlier changes | {async()=>{setBusy(true);try{const older=await commercialApi<History[]>(`${base}/history?beforeRevision=${history[history.length-1].revision}`);setHistory([...history,...older])}catch(e){setError(String(e))}finally{setBusy(false)}}} | Not a native form submit or inherited from component context | [web/components/PaymentControls.tsx:18](../../../web/components/PaymentControls.tsx#L18) |
| UI-00787 | Reload routing commission | {()=>void load()} | Not a native form submit or inherited from component context | [web/components/RoutingCommissionControl.tsx:12](../../../web/components/RoutingCommissionControl.tsx#L12) |
| UI-00791 | Save routing commission | submit / component action | {save} | [web/components/RoutingCommissionControl.tsx:12](../../../web/components/RoutingCommissionControl.tsx#L12) |
| UI-00794 | Load earlier commission changes | {async()=>{setBusy(true);try{const earlier=await commercialApi<History[]>(`${base}/history?beforeRevision=${history[history.length-1].revision}`);setHistory([...history,...earlier])}catch(e){setError(String(e))}finally{setBusy(false)}}} | Not a native form submit or inherited from component context | [web/components/RoutingCommissionControl.tsx:12](../../../web/components/RoutingCommissionControl.tsx#L12) |


### Dialog and alert/status inventory

| ID | Role / kind | Text / label | Visibility conditions | Source |
| --- | --- | --- | --- | --- |
| UI-00005 | "alert" | {error} | error | [web/app/admin/AccountPolicies.tsx:50](../../../web/app/admin/AccountPolicies.tsx#L50) |
| UI-00006 | "status" | {message} | message | [web/app/admin/AccountPolicies.tsx:51](../../../web/app/admin/AccountPolicies.tsx#L51) |
| UI-00016 | "alert" | {error} | error | [web/app/admin/AdminAudit.tsx:23](../../../web/app/admin/AdminAudit.tsx#L23) |
| UI-00018 | "status" | Loading audit records… | loading | [web/app/admin/AdminAudit.tsx:23](../../../web/app/admin/AdminAudit.tsx#L23) |
| UI-00019 | "status" | {result.total?`${result.page*20+1}–${Math.min(result.total,(result.page+1)*20)} of ${result.total} records`:'No audit records match these filters.'} | result; loading | [web/app/admin/AdminAudit.tsx:23](../../../web/app/admin/AdminAudit.tsx#L23) |
| UI-00042 | "alert" | {error} | error | [web/app/admin/page.tsx:68](../../../web/app/admin/page.tsx#L68) |
| UI-00053 | "status" | Opening your workspace… | Parent/source state | [web/app/components/HostGlobalNav.tsx:13](../../../web/app/components/HostGlobalNav.tsx#L13) |
| UI-00372 | "status" | Checking your email… | loading | [web/components/AccountEmailVerification.tsx:15](../../../web/components/AccountEmailVerification.tsx#L15) |
| UI-00373 | "status" | ✓ Email verified for account recovery. Verified email addresses cannot be changed. | verified; loading | [web/components/AccountEmailVerification.tsx:15](../../../web/components/AccountEmailVerification.tsx#L15) |
| UI-00383 | "status" | {message} | message | [web/components/AccountEmailVerification.tsx:15](../../../web/components/AccountEmailVerification.tsx#L15) |
| UI-00384 | "alert" | {error} | error | [web/components/AccountEmailVerification.tsx:15](../../../web/components/AccountEmailVerification.tsx#L15) |
| UI-00387 | "status" | Loading AI details… | loading; open | [web/components/AdminAiDesignDetails.tsx:8](../../../web/components/AdminAiDesignDetails.tsx#L8) |
| UI-00388 | "alert" | {error} | error; open | [web/components/AdminAiDesignDetails.tsx:8](../../../web/components/AdminAiDesignDetails.tsx#L8) |
| UI-00411 | "alert" | {error} | error; groupBase&&error.includes('switched off') | [web/components/CommercePurchase.tsx:27](../../../web/components/CommercePurchase.tsx#L27) |
| UI-00413 | "status" | {notice} | notice | [web/components/CommercePurchase.tsx:27](../../../web/components/CommercePurchase.tsx#L27) |
| UI-00414 | "alert" | {error} | error | [web/components/CommercialAdminControls.tsx:12](../../../web/components/CommercialAdminControls.tsx#L12) |
| UI-00444 | "status" | Opening your celebration space… | ready | [web/components/ExperienceProvider.tsx:13](../../../web/components/ExperienceProvider.tsx#L13) |
| UI-00570 | "alert" | {error} | error | [web/components/GroupMemberEmailPolicy.tsx:11](../../../web/components/GroupMemberEmailPolicy.tsx#L11) |
| UI-00666 | "alert" | {error} | error | [web/components/HostWorkspace.tsx:46](../../../web/components/HostWorkspace.tsx#L46) |
| UI-00746 | "status" | Mobile number verified | verified | [web/components/MobileVerification.tsx:13](../../../web/components/MobileVerification.tsx#L13) |
| UI-00753 | "status" | {message} | message | [web/components/MobileVerification.tsx:13](../../../web/components/MobileVerification.tsx#L13) |
| UI-00754 | "alert" | {error} | error | [web/components/MonetizationControls.tsx:20](../../../web/components/MonetizationControls.tsx#L20) |
| UI-00767 | "status" | {message} | message | [web/components/MonetizationControls.tsx:20](../../../web/components/MonetizationControls.tsx#L20) |
| UI-00774 | "alert" | {error} | error | [web/components/PaymentControls.tsx:18](../../../web/components/PaymentControls.tsx#L18) |
| UI-00778 | "status" | {message} | message | [web/components/PlatformVerificationControls.tsx:9](../../../web/components/PlatformVerificationControls.tsx#L9) |
| UI-00779 | "alert" | {error} | error | [web/components/PlatformVerificationControls.tsx:9](../../../web/components/PlatformVerificationControls.tsx#L9) |
| UI-00792 | "alert" | {error} | error | [web/components/RoutingCommissionControl.tsx:12](../../../web/components/RoutingCommissionControl.tsx#L12) |
| UI-00793 | "status" | {message} | message | [web/components/RoutingCommissionControl.tsx:12](../../../web/components/RoutingCommissionControl.tsx#L12) |
| UI-00890 | "status" | Saving view… | busy | [web/components/focused/ExperienceControl.tsx:13](../../../web/components/focused/ExperienceControl.tsx#L13) |
| UI-00891 | "alert" | {error} | error | [web/components/focused/ExperienceControl.tsx:13](../../../web/components/focused/ExperienceControl.tsx#L13) |
| UI-00892 | "status" | {message} | message | [web/components/focused/ExperienceControl.tsx:13](../../../web/components/focused/ExperienceControl.tsx#L13) |


### Workflow state bindings

| Source | Binding | Initial expression |
| --- | --- | --- |
| web/app/admin/page.tsx | error | '' |
| web/app/admin/page.tsx | busy | false |
| web/app/admin/page.tsx | tab | 'overview' |
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
| web/components/MonetizationControls.tsx | busy | false |
| web/components/MonetizationControls.tsx | error | '' |
| web/components/MonetizationControls.tsx | message | '' |
| web/components/CommercialAdminControls.tsx | error | '' |
| web/components/CommercialAdminControls.tsx | busy | false |
| web/components/CommercePurchase.tsx | error | '' |
| web/components/CommercePurchase.tsx | busy | false |
| web/components/CommercePurchase.tsx | notice | '' |
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
| Header | Shared identity/event or page title/back/actions, where declared. Composite header declarations: 2. |
| Navigation / sidebar | Host/admin navigation and event context where applicable; mobile/focused variants are conditional. nav=2, aside=1. |
| Main content | Purpose-specific forms/cards/tables/canvas/player from the composed sources. main=3, section=3, article=1. |
| Search and filters | Fields/selects listed below; exact value/handler/visibility controls are not inferred from placeholder labels. |
| Data grids and cards | Table declarations=0. Data collections and generated rows/cards are in frontend.json dynamicSources. |
| Popups / dialogs | Named/source-declared dialog control records below; opening/closing/loading/apply/cancel actions belong to those component handlers. |
| Alerts / notifications | Source controls with role alert/status are listed below. Notice strings may come from API results; do not assume a universal envelope. |
| Footer | Local/shared footer declarations=0. No undocumented legal/footer wording is invented. |
| Loading / empty / success / error | Observed state bindings and relevant action fields are listed below. Expected QA states from Document 2 do not establish a spinner/retry control where source does not implement one. |


### Composed implementation

- [web/app/admin/invitation-assets/page.tsx:1](../../../web/app/admin/invitation-assets/page.tsx#L1)
- [web/components/ExperienceProvider.tsx:1](../../../web/components/ExperienceProvider.tsx#L1)
- [web/components/CommercePurchase.tsx:1](../../../web/components/CommercePurchase.tsx#L1)
- [web/app/layout.tsx:1](../../../web/app/layout.tsx#L1)
- [web/components/VerificationProvider.tsx:1](../../../web/components/VerificationProvider.tsx#L1)
- [web/app/components/PerformanceDiagnostics.tsx:1](../../../web/app/components/PerformanceDiagnostics.tsx#L1)
- [web/app/components/HostSessionRedirect.tsx:1](../../../web/app/components/HostSessionRedirect.tsx#L1)
- [web/app/components/HostGlobalNav.tsx:1](../../../web/app/components/HostGlobalNav.tsx#L1)
- [web/components/HostWorkspace.tsx:1](../../../web/components/HostWorkspace.tsx#L1)
- [web/components/focused/HostShell.tsx:1](../../../web/components/focused/HostShell.tsx#L1)

### Fields

| Control ID | Label / expression | Type | Binding | Required | Native limits / format | Detailed specification |
| --- | --- | --- | --- | --- | --- | --- |
| UI-00026 | Category | select  | {category} | Not declared |  | [control](UI_CONTROLS.md#ui-00026) |
| UI-00027 | Occasion | select  | {occasion} | Not declared |  | [control](UI_CONTROLS.md#ui-00027) |
| UI-00028 | Title | input  | {title} | true | maxLength={120} | [control](UI_CONTROLS.md#ui-00028) |
| UI-00029 | Image (automatically optimized to 2 MB) | input "file" |  | true |  | [control](UI_CONTROLS.md#ui-00029) |
| UI-00033 | Occasion | select  | {filter} | Not declared |  | [control](UI_CONTROLS.md#ui-00033) |
| UI-00034 | Status | select  | {status} | Not declared |  | [control](UI_CONTROLS.md#ui-00034) |
| UI-00035 | Access tier | select  | {a.tier\|\|'FREE'} | Not declared |  | [control](UI_CONTROLS.md#ui-00035) |


### Buttons

| ID | Label / expression | Action | Submit validation / handler | Source |
| --- | --- | --- | --- | --- |
| UI-00032 | {busy?'Working…':'Upload as draft'} | submit / component action | {upload} | [web/app/admin/invitation-assets/page.tsx:91](../../../web/app/admin/invitation-assets/page.tsx#L91) |
| UI-00036 | {a.published?'Unpublish':'Publish'} | {()=>void toggle(a)} | Not a native form submit or inherited from component context | [web/app/admin/invitation-assets/page.tsx:113](../../../web/app/admin/invitation-assets/page.tsx#L113) |
| UI-00410 | {busy?'Processing…':offer.amountPaise===0?'Activate access':`${offer.entitled?'Renew':'Purchase'} · ${commercialMoney(offer.amountPaise)} (test mode)`} | {()=>void pay()} | Not a native form submit or inherited from component context | [web/components/CommercePurchase.tsx:27](../../../web/components/CommercePurchase.tsx#L27) |
| UI-00412 | Reload price | {()=>{setRetry(v=>v+1)}} | Not a native form submit or inherited from component context | [web/components/CommercePurchase.tsx:27](../../../web/components/CommercePurchase.tsx#L27) |
| UI-00664 | {collapsed?<PanelLeftOpen size={20}/>:<PanelLeftClose size={20}/>} | {toggle} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:43](../../../web/components/HostWorkspace.tsx#L43) |
| UI-00667 |  | {()=>void signOut()} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:46](../../../web/components/HostWorkspace.tsx#L46) |
| UI-00668 |  | {()=>setCollapsed(true)} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:47](../../../web/components/HostWorkspace.tsx#L47) |


### Dialog and alert/status inventory

| ID | Role / kind | Text / label | Visibility conditions | Source |
| --- | --- | --- | --- | --- |
| UI-00023 | "alert" | {error} | error | [web/app/admin/invitation-assets/page.tsx:76](../../../web/app/admin/invitation-assets/page.tsx#L76) |
| UI-00024 | "status" | {notice} | notice | [web/app/admin/invitation-assets/page.tsx:77](../../../web/app/admin/invitation-assets/page.tsx#L77) |
| UI-00030 | "status" | Optimizing image… | optimizing | [web/app/admin/invitation-assets/page.tsx:87](../../../web/app/admin/invitation-assets/page.tsx#L87) |
| UI-00031 | "status" | Ready to upload: {(originalSize/1048576).toFixed(2)} MB → {(file.size/1048576).toFixed(2)} MB ( {file.type==='image/png'?'PNG':'JPEG'} ) | file&&originalSize!==null | [web/app/admin/invitation-assets/page.tsx:88](../../../web/app/admin/invitation-assets/page.tsx#L88) |
| UI-00053 | "status" | Opening your workspace… | Parent/source state | [web/app/components/HostGlobalNav.tsx:13](../../../web/app/components/HostGlobalNav.tsx#L13) |
| UI-00411 | "alert" | {error} | error; groupBase&&error.includes('switched off') | [web/components/CommercePurchase.tsx:27](../../../web/components/CommercePurchase.tsx#L27) |
| UI-00413 | "status" | {notice} | notice | [web/components/CommercePurchase.tsx:27](../../../web/components/CommercePurchase.tsx#L27) |
| UI-00444 | "status" | Opening your celebration space… | ready | [web/components/ExperienceProvider.tsx:13](../../../web/components/ExperienceProvider.tsx#L13) |
| UI-00666 | "alert" | {error} | error | [web/components/HostWorkspace.tsx:46](../../../web/components/HostWorkspace.tsx#L46) |


### Workflow state bindings

| Source | Binding | Initial expression |
| --- | --- | --- |
| web/app/admin/invitation-assets/page.tsx | loading | true |
| web/app/admin/invitation-assets/page.tsx | error | '' |
| web/app/admin/invitation-assets/page.tsx | notice | '' |
| web/app/admin/invitation-assets/page.tsx | busy | false |
| web/components/CommercePurchase.tsx | error | '' |
| web/components/CommercePurchase.tsx | busy | false |
| web/components/CommercePurchase.tsx | notice | '' |
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
| Header | Shared identity/event or page title/back/actions, where declared. Composite header declarations: 3. |
| Navigation / sidebar | Host/admin navigation and event context where applicable; mobile/focused variants are conditional. nav=2, aside=1. |
| Main content | Purpose-specific forms/cards/tables/canvas/player from the composed sources. main=4, section=7, article=1. |
| Search and filters | Fields/selects listed below; exact value/handler/visibility controls are not inferred from placeholder labels. |
| Data grids and cards | Table declarations=0. Data collections and generated rows/cards are in frontend.json dynamicSources. |
| Popups / dialogs | Named/source-declared dialog control records below; opening/closing/loading/apply/cancel actions belong to those component handlers. |
| Alerts / notifications | Source controls with role alert/status are listed below. Notice strings may come from API results; do not assume a universal envelope. |
| Footer | Local/shared footer declarations=0. No undocumented legal/footer wording is invented. |
| Loading / empty / success / error | Observed state bindings and relevant action fields are listed below. Expected QA states from Document 2 do not establish a spinner/retry control where source does not implement one. |


### Composed implementation

- [web/app/dashboard/page.tsx:1](../../../web/app/dashboard/page.tsx#L1)
- [web/components/ExperienceProvider.tsx:1](../../../web/components/ExperienceProvider.tsx#L1)
- [web/components/focused/Dashboard.tsx:1](../../../web/components/focused/Dashboard.tsx#L1)
- [web/app/components/HostToast.tsx:1](../../../web/app/components/HostToast.tsx#L1)
- [web/app/components/HostNotificationBell.tsx:1](../../../web/app/components/HostNotificationBell.tsx#L1)
- [web/app/layout.tsx:1](../../../web/app/layout.tsx#L1)
- [web/components/VerificationProvider.tsx:1](../../../web/components/VerificationProvider.tsx#L1)
- [web/app/components/PerformanceDiagnostics.tsx:1](../../../web/app/components/PerformanceDiagnostics.tsx#L1)
- [web/app/components/HostSessionRedirect.tsx:1](../../../web/app/components/HostSessionRedirect.tsx#L1)
- [web/app/components/HostGlobalNav.tsx:1](../../../web/app/components/HostGlobalNav.tsx#L1)
- [web/components/HostWorkspace.tsx:1](../../../web/components/HostWorkspace.tsx#L1)
- [web/components/focused/HostShell.tsx:1](../../../web/components/focused/HostShell.tsx#L1)

### Fields

| Control ID | Label / expression | Type | Binding | Required | Native limits / format | Detailed specification |
| --- | --- | --- | --- | --- | --- | --- |


### Buttons

| ID | Label / expression | Action | Submit validation / handler | Source |
| --- | --- | --- | --- | --- |
| UI-00054 | Notifications {inbox.unread>0&&<strong>({inbox.unread})</strong>} | {()=>setOpen(!open)} | Not a native form submit or inherited from component context | [web/app/components/HostNotificationBell.tsx:26](../../../web/app/components/HostNotificationBell.tsx#L26) |
| UI-00057 | Mark read | {()=>void markRead(n.id)} | Not a native form submit or inherited from component context | [web/app/components/HostNotificationBell.tsx:33](../../../web/app/components/HostNotificationBell.tsx#L33) |
| UI-00058 | Refresh alerts | {()=>void load()} | Not a native form submit or inherited from component context | [web/app/components/HostNotificationBell.tsx:34](../../../web/app/components/HostNotificationBell.tsx#L34) |
| UI-00059 | × | {()=>{setFlash(null);onDismiss?.();}} | Not a native form submit or inherited from component context | [web/app/components/HostToast.tsx:12](../../../web/app/components/HostToast.tsx#L12) |
| UI-00063 | Retry | {()=>void load()} | Not a native form submit or inherited from component context | [web/app/dashboard/page.tsx:34](../../../web/app/dashboard/page.tsx#L34) |
| UI-00064 | Refresh | {()=>void load()} | Not a native form submit or inherited from component context | [web/app/dashboard/page.tsx:36](../../../web/app/dashboard/page.tsx#L36) |
| UI-00664 | {collapsed?<PanelLeftOpen size={20}/>:<PanelLeftClose size={20}/>} | {toggle} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:43](../../../web/components/HostWorkspace.tsx#L43) |
| UI-00667 |  | {()=>void signOut()} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:46](../../../web/components/HostWorkspace.tsx#L46) |
| UI-00668 |  | {()=>setCollapsed(true)} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:47](../../../web/components/HostWorkspace.tsx#L47) |


### Dialog and alert/status inventory

| ID | Role / kind | Text / label | Visibility conditions | Source |
| --- | --- | --- | --- | --- |
| UI-00053 | "status" | Opening your workspace… | Parent/source state | [web/app/components/HostGlobalNav.tsx:13](../../../web/app/components/HostGlobalNav.tsx#L13) |
| UI-00055 | "alert" | {error} | error; open | [web/app/components/HostNotificationBell.tsx:28](../../../web/app/components/HostNotificationBell.tsx#L28) |
| UI-00061 | "alert" | {error} or | error | [web/app/dashboard/page.tsx:34](../../../web/app/dashboard/page.tsx#L34) |
| UI-00065 | "status" | Loading your celebrations… | loading | [web/app/dashboard/page.tsx:37](../../../web/app/dashboard/page.tsx#L37) |
| UI-00444 | "status" | Opening your celebration space… | ready | [web/components/ExperienceProvider.tsx:13](../../../web/components/ExperienceProvider.tsx#L13) |
| UI-00666 | "alert" | {error} | error | [web/components/HostWorkspace.tsx:46](../../../web/components/HostWorkspace.tsx#L46) |
| UI-00860 | "alert" | {error} | error | [web/components/focused/Dashboard.tsx:13](../../../web/components/focused/Dashboard.tsx#L13) |
| UI-00861 | "status" | Loading your overview… | loading | [web/components/focused/Dashboard.tsx:13](../../../web/components/focused/Dashboard.tsx#L13) |


### Workflow state bindings

| Source | Binding | Initial expression |
| --- | --- | --- |
| web/app/dashboard/page.tsx | loading | true |
| web/app/dashboard/page.tsx | error | '' |
| web/components/focused/Dashboard.tsx | loading | true |
| web/components/focused/Dashboard.tsx | error | '' |
| web/app/components/HostNotificationBell.tsx | open | false |
| web/app/components/HostNotificationBell.tsx | error | '' |
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
| Header | Shared identity/event or page title/back/actions, where declared. Composite header declarations: 5. |
| Navigation / sidebar | Host/admin navigation and event context where applicable; mobile/focused variants are conditional. nav=3, aside=1. |
| Main content | Purpose-specific forms/cards/tables/canvas/player from the composed sources. main=9, section=25, article=6. |
| Search and filters | Fields/selects listed below; exact value/handler/visibility controls are not inferred from placeholder labels. |
| Data grids and cards | Table declarations=1. Data collections and generated rows/cards are in frontend.json dynamicSources. |
| Popups / dialogs | Named/source-declared dialog control records below; opening/closing/loading/apply/cancel actions belong to those component handlers. |
| Alerts / notifications | Source controls with role alert/status are listed below. Notice strings may come from API results; do not assume a universal envelope. |
| Footer | Local/shared footer declarations=3. No undocumented legal/footer wording is invented. |
| Loading / empty / success / error | Observed state bindings and relevant action fields are listed below. Expected QA states from Document 2 do not establish a spinner/retry control where source does not implement one. |


### Composed implementation

- [web/app/e/[token]/page.tsx:1](../../../web/app/e/[token]/page.tsx#L1)
- [web/components/GroupContribution.tsx:1](../../../web/components/GroupContribution.tsx#L1)
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
- [web/components/focused/Checkout.tsx:1](../../../web/components/focused/Checkout.tsx#L1)
- [web/components/GuestActions.tsx:1](../../../web/components/GuestActions.tsx#L1)
- [web/components/GuestCelebration.tsx:1](../../../web/components/GuestCelebration.tsx#L1)
- [web/components/SocialSharing.tsx:1](../../../web/components/SocialSharing.tsx#L1)
- [web/app/layout.tsx:1](../../../web/app/layout.tsx#L1)
- [web/app/components/PerformanceDiagnostics.tsx:1](../../../web/app/components/PerformanceDiagnostics.tsx#L1)
- [web/app/components/HostSessionRedirect.tsx:1](../../../web/app/components/HostSessionRedirect.tsx#L1)
- [web/app/components/HostGlobalNav.tsx:1](../../../web/app/components/HostGlobalNav.tsx#L1)
- [web/components/HostWorkspace.tsx:1](../../../web/components/HostWorkspace.tsx#L1)
- [web/components/focused/HostShell.tsx:1](../../../web/components/focused/HostShell.tsx#L1)

### Fields

| Control ID | Label / expression | Type | Binding | Required | Native limits / format | Detailed specification |
| --- | --- | --- | --- | --- | --- | --- |
| UI-00075 | "Sort gifts" | select  | {sortBy} | Not declared |  | [control](UI_CONTROLS.md#ui-00075) |
| UI-00442 | Same as event address | input "checkbox" | {same} | Not declared |  | [control](UI_CONTROLS.md#ui-00442) |
| UI-00443 | {label} | input  | {address[key]} | true | maxLength={maxLength} | [control](UI_CONTROLS.md#ui-00443) |
| UI-00464 | Mobile number | input "tel" | {registrationMobile} | true |  | [control](UI_CONTROLS.md#ui-00464) |
| UI-00465 | Full name | input  | {fullName} | true | minLength={2}; maxLength={120} | [control](UI_CONTROLS.md#ui-00465) |
| UI-00466 | Username | input  | {username} | true | minLength={3}; maxLength={40} | [control](UI_CONTROLS.md#ui-00466) |
| UI-00467 | Sign in with | select  | {loginMethod} | Not declared |  | [control](UI_CONTROLS.md#ui-00467) |
| UI-00468 | Password | input "password" | {password} | true | minLength={8}; maxLength={128} | [control](UI_CONTROLS.md#ui-00468) |
| UI-00469 | Confirm password | input "password" | {confirm} | true | minLength={8} | [control](UI_CONTROLS.md#ui-00469) |
| UI-00470 | Six-digit PIN | input "password" | {pin} | true | minLength={6}; maxLength={6}; pattern="[0-9]{6}" | [control](UI_CONTROLS.md#ui-00470) |
| UI-00471 | Confirm PIN | input "password" | {confirmPin} | true | minLength={6}; maxLength={6}; pattern="[0-9]{6}" | [control](UI_CONTROLS.md#ui-00471) |
| UI-00472 | Six-digit PIN | input "password" | {pin} | true | minLength={6}; maxLength={6}; pattern="[0-9]{6}" | [control](UI_CONTROLS.md#ui-00472) |
| UI-00476 | What should we call you? | input  | {adminNameDraft} | true | minLength={2}; maxLength={120} | [control](UI_CONTROLS.md#ui-00476) |
| UI-00480 | Current password | input "password" | {pinPassword} | true |  | [control](UI_CONTROLS.md#ui-00480) |
| UI-00481 | Six-digit PIN | input "password" | {pin} | true | maxLength={6}; pattern="[0-9]{6}" | [control](UI_CONTROLS.md#ui-00481) |
| UI-00482 | Confirm PIN | input "password" | {confirmPin} | true | maxLength={6}; pattern="[0-9]{6}" | [control](UI_CONTROLS.md#ui-00482) |
| UI-00501 | Who can join? | select  | {privacy.join_policy} | Not declared |  | [control](UI_CONTROLS.md#ui-00501) |
| UI-00502 | Who can see the member list? | select  | {privacy.member_visibility} | Not declared |  | [control](UI_CONTROLS.md#ui-00502) |
| UI-00503 | Who can see individual contribution amounts? | select  | {privacy.amount_visibility} | Not declared |  | [control](UI_CONTROLS.md#ui-00503) |
| UI-00506 | Group name * | input  | {form.name} | true | maxLength={120} | [control](UI_CONTROLS.md#ui-00506) |
| UI-00507 | Your name * | input  | {form.creatorName} | true | maxLength={120} | [control](UI_CONTROLS.md#ui-00507) |
| UI-00508 | Mobile number * | input "tel" | {form.creatorMobile} | true | maxLength={20} | [control](UI_CONTROLS.md#ui-00508) |
| UI-00509 | Group description | textarea  | {form.description} | Not declared | maxLength={500} | [control](UI_CONTROLS.md#ui-00509) |
| UI-00510 | Collection plan | select  | {giftId} | Not declared |  | [control](UI_CONTROLS.md#ui-00510) |
| UI-00511 | Gift name (optional) | input  | {form.giftName} | Not declared | maxLength={160} | [control](UI_CONTROLS.md#ui-00511) |
| UI-00512 | Gift description | textarea  | {form.giftDescription} | Not declared | maxLength={500} | [control](UI_CONTROLS.md#ui-00512) |
| UI-00513 | Gift link | input "url" | {form.giftLink} | Not declared | maxLength={1000} | [control](UI_CONTROLS.md#ui-00513) |
| UI-00514 | Gift goal in ₹ | input  | {form.targetRupees} | Not declared |  | [control](UI_CONTROLS.md#ui-00514) |
| UI-00517 |  | input "file" |  | Not declared |  | [control](UI_CONTROLS.md#ui-00517) |
| UI-00524 | Payment method | select  | {kind} | Not declared |  | [control](UI_CONTROLS.md#ui-00524) |
| UI-00525 | Account nickname | input  | {label} | true | maxLength={80} | [control](UI_CONTROLS.md#ui-00525) |
| UI-00526 | UPI ID | input  | {upiId} | true |  | [control](UI_CONTROLS.md#ui-00526) |
| UI-00527 | Account holder | input  | {accountHolder} | true | maxLength={120} | [control](UI_CONTROLS.md#ui-00527) |
| UI-00528 | Account number | input  | {accountNumber} | true |  | [control](UI_CONTROLS.md#ui-00528) |
| UI-00529 | IFSC | input  | {ifsc} | true | maxLength={11} | [control](UI_CONTROLS.md#ui-00529) |
| UI-00533 | Email address | input "email" | {email} | true | maxLength={254} | [control](UI_CONTROLS.md#ui-00533) |
| UI-00536 | Six-digit code | input  | {code} | true | maxLength={6}; pattern="[0-9]{6}" | [control](UI_CONTROLS.md#ui-00536) |
| UI-00543 | Wishlist gift | select  | {choice} | Not declared |  | [control](UI_CONTROLS.md#ui-00543) |
| UI-00550 | Your contribution amount (₹) | input  | {amount} | true |  | [control](UI_CONTROLS.md#ui-00550) |
| UI-00551 | Your name * | input  | {name} | true | minLength={2}; maxLength={120} | [control](UI_CONTROLS.md#ui-00551) |
| UI-00552 | Mobile or email * | input  | {contact} | true | maxLength={120} | [control](UI_CONTROLS.md#ui-00552) |
| UI-00556 | Transaction reference / UTR * | input  | {reference} | true | minLength={6}; maxLength={120} | [control](UI_CONTROLS.md#ui-00556) |
| UI-00562 | Invitation link options | select  | {accessMode} | Not declared |  | [control](UI_CONTROLS.md#ui-00562) |
| UI-00563 | Suggested contribution (₹) | input  | {amount} | Not declared |  | [control](UI_CONTROLS.md#ui-00563) |
| UI-00564 | UPI account for guests | select  | {upi} | Not declared |  | [control](UI_CONTROLS.md#ui-00564) |
| UI-00565 | Bank account for guests | select  | {bank} | Not declared |  | [control](UI_CONTROLS.md#ui-00565) |
| UI-00566 | Enable manual guest contribution instructions | input "checkbox" | {enabled} | Not declared |  | [control](UI_CONTROLS.md#ui-00566) |
| UI-00576 | "RSVP guest name" | input  | {name} | true | maxLength={120} | [control](UI_CONTROLS.md#ui-00576) |
| UI-00577 | "RSVP email" | input "email" | {email} | true | maxLength={254} | [control](UI_CONTROLS.md#ui-00577) |
| UI-00579 |  | input "radio" | {value} | Not declared |  | [control](UI_CONTROLS.md#ui-00579) |
| UI-00580 | People attending (including you) | input "number" | {party} | true | min={1}; max={10} | [control](UI_CONTROLS.md#ui-00580) |
| UI-00584 | "Guestbook name" | input  | {wishName} | true | maxLength={120} | [control](UI_CONTROLS.md#ui-00584) |
| UI-00585 | "Guestbook message" | textarea  | {message} | true | maxLength={1000} | [control](UI_CONTROLS.md#ui-00585) |
| UI-00586 | "Guestbook photo" | input "file" |  | Not declared |  | [control](UI_CONTROLS.md#ui-00586) |
| UI-00607 |  | input "radio" | {method.id} | Not declared |  | [control](UI_CONTROLS.md#ui-00607) |
| UI-00608 | Destination | select  | {giftId} | Not declared |  | [control](UI_CONTROLS.md#ui-00608) |
| UI-00609 | Amount (₹) | input "number" | {amount} | true | min="1"; max="1000000"; step="0.01" | [control](UI_CONTROLS.md#ui-00609) |
| UI-00610 | Your name * | input  | {name} | true | maxLength={200} | [control](UI_CONTROLS.md#ui-00610) |
| UI-00612 | Email (optional) | input "email" | {email} | Not declared | maxLength={320} | [control](UI_CONTROLS.md#ui-00612) |
| UI-00613 | Phone (optional) | input "tel" | {phone} | Not declared | maxLength={30} | [control](UI_CONTROLS.md#ui-00613) |
| UI-00614 | UPI transaction ID / Bank UTR | input  | {transferReference} | Not declared | maxLength={40} | [control](UI_CONTROLS.md#ui-00614) |
| UI-00629 | Gift name | input  | {name} | true | maxLength={160} | [control](UI_CONTROLS.md#ui-00629) |
| UI-00630 | Description · optional | textarea  | {description} | Not declared | maxLength={500} | [control](UI_CONTROLS.md#ui-00630) |
| UI-00631 | Product link · optional | input "url" | {url} | Not declared | maxLength={1000}; pattern="https://[^\s]+" | [control](UI_CONTROLS.md#ui-00631) |
| UI-00633 | How would you like to give? | select  | {type} | Not declared |  | [control](UI_CONTROLS.md#ui-00633) |
| UI-00645 | Preferred recipient | select  | {selected?.id??''} | Not declared |  | [control](UI_CONTROLS.md#ui-00645) |
| UI-00669 | Collection plan | select  | {plan} | Not declared |  | [control](UI_CONTROLS.md#ui-00669) |
| UI-00670 | Our gift’s product link | input "url" | {value.giftLink} | true | maxLength={1000}; pattern="https://[^\s]+" | [control](UI_CONTROLS.md#ui-00670) |
| UI-00672 | Gift name | input  | {value.giftName} | true | maxLength={160} | [control](UI_CONTROLS.md#ui-00672) |
| UI-00673 | Gift description | textarea  | {value.giftDescription} | Not declared | maxLength={500} | [control](UI_CONTROLS.md#ui-00673) |
| UI-00674 | Collection goal (₹) · optional | input "number" | {value.targetRupees} | Not declared | min="0.01"; max="1000000000"; step="0.01" | [control](UI_CONTROLS.md#ui-00674) |
| UI-00683 | Payment method | select  | {method} | Not declared |  | [control](UI_CONTROLS.md#ui-00683) |
| UI-00684 | UPI transaction ID / Bank UTR | input  | {reference} | true | minLength={8}; maxLength={40}; pattern="[A-Za-z0-9\-]{8,40}" | [control](UI_CONTROLS.md#ui-00684) |
| UI-00749 | Mobile number with country code | input "tel" | {mobile} | true | pattern="\+[1-9][0-9]{7,14}" | [control](UI_CONTROLS.md#ui-00749) |
| UI-00750 | Six-digit verification code | input  | {code} | true | maxLength={6}; pattern="[0-9]{6}" | [control](UI_CONTROLS.md#ui-00750) |
| UI-00795 | Email address | input "email" | {value} | true | maxLength={254} | [control](UI_CONTROLS.md#ui-00795) |
| UI-00809 | "Invitation link" | input  | {url} | Not declared |  | [control](UI_CONTROLS.md#ui-00809) |
| UI-00821 | Shipment tracking details | textarea  | {tracking} | true | maxLength={1000} | [control](UI_CONTROLS.md#ui-00821) |
| UI-00825 | Choose a gift | select  | {source} | Not declared |  | [control](UI_CONTROLS.md#ui-00825) |
| UI-00827 | Your name | input  | {form.guestName} | true | maxLength={120} | [control](UI_CONTROLS.md#ui-00827) |
| UI-00828 | Email address | input "email" | {form.email} | true | maxLength={254} | [control](UI_CONTROLS.md#ui-00828) |
| UI-00829 | Mobile number | input "tel" | {form.mobile} | true | maxLength={25}; pattern="\+?[0-9][0-9 \-]{7,23}" | [control](UI_CONTROLS.md#ui-00829) |
| UI-00831 | Gift name | input  | {form.name} | true | maxLength={160} | [control](UI_CONTROLS.md#ui-00831) |
| UI-00832 | Product link {method==='DIRECT_DELIVERY'?'':' · optional if you add gift details'} | input "url" | {form.productUrl} | {method==='DIRECT_DELIVERY'} | maxLength={1000}; pattern="https://[^\s]+" | [control](UI_CONTROLS.md#ui-00832) |
| UI-00833 | Gift details | textarea  | {form.description} | Not declared | maxLength={500} | [control](UI_CONTROLS.md#ui-00833) |
| UI-00835 |  | input "radio" | {method==='LINK_AND_CONTRIBUTION'} | Not declared |  | [control](UI_CONTROLS.md#ui-00835) |
| UI-00836 |  | input "radio" | {method==='DIRECT_DELIVERY'} | Not declared |  | [control](UI_CONTROLS.md#ui-00836) |
| UI-00837 | Contribution amount (₹) | input "number" | {form.amount} | true | min="1"; max="1000000"; step="0.01" | [control](UI_CONTROLS.md#ui-00837) |
| UI-00846 | Amount (₹) | input "number" | {p.amount} | true | min="1"; max="1000000"; step="0.01" | [control](UI_CONTROLS.md#ui-00846) |
| UI-00847 | Your name | input  | {p.name} | true | maxLength={200} | [control](UI_CONTROLS.md#ui-00847) |
| UI-00849 | Email | input "email" | {p.email} | Not declared | maxLength={320} | [control](UI_CONTROLS.md#ui-00849) |
| UI-00850 | Phone | input "tel" | {p.phone} | Not declared | maxLength={30} | [control](UI_CONTROLS.md#ui-00850) |
| UI-00854 |  | input "radio" | {p.method===method} | Not declared |  | [control](UI_CONTROLS.md#ui-00854) |
| UI-00855 | Transaction reference / bank UTR | input  | {p.reference} | true | maxLength={40} | [control](UI_CONTROLS.md#ui-00855) |
| UI-00896 | Sort | select  | {sort} | Not declared |  | [control](UI_CONTROLS.md#ui-00896) |


### Buttons

| ID | Label / expression | Action | Submit validation / handler | Source |
| --- | --- | --- | --- | --- |
| UI-00074 | Retry | {()=>void refreshProgress()} | Not a native form submit or inherited from component context | [web/app/e/[token]/page.tsx:62](../../../web/app/e/[token]/page.tsx#L62) |
| UI-00077 | {g.contributionEnabled===false?g.contributionDisabledReason:"♡ Contribute with Love"} | {()=>setSelectedGift(g)} | Not a native form submit or inherited from component context | [web/app/e/[token]/page.tsx:74](../../../web/app/e/[token]/page.tsx#L74) |
| UI-00078 | ♡ Contribute to the Celebration | {()=>setWalletOpen(true)} | Not a native form submit or inherited from component context | [web/app/e/[token]/page.tsx:78](../../../web/app/e/[token]/page.tsx#L78) |
| UI-00410 | {busy?'Processing…':offer.amountPaise===0?'Activate access':`${offer.entitled?'Renew':'Purchase'} · ${commercialMoney(offer.amountPaise)} (test mode)`} | {()=>void pay()} | Not a native form submit or inherited from component context | [web/components/CommercePurchase.tsx:27](../../../web/components/CommercePurchase.tsx#L27) |
| UI-00412 | Reload price | {()=>{setRetry(v=>v+1)}} | Not a native form submit or inherited from component context | [web/components/CommercePurchase.tsx:27](../../../web/components/CommercePurchase.tsx#L27) |
| UI-00445 | View full description | {()=>dialog.current?.showModal()} | Not a native form submit or inherited from component context | [web/components/GiftDescription.tsx:9](../../../web/components/GiftDescription.tsx#L9) |
| UI-00447 | Close × | {()=>dialog.current?.close()} | Not a native form submit or inherited from component context | [web/components/GiftDescription.tsx:9](../../../web/components/GiftDescription.tsx#L9) |
| UI-00449 | Newer activity | {()=>setOffset(value=>Math.max(0,value-100))} | Not a native form submit or inherited from component context | [web/components/GroupActivity.tsx:7](../../../web/components/GroupActivity.tsx#L7) |
| UI-00450 | Older activity | {()=>setOffset(value=>value+100)} | Not a native form submit or inherited from component context | [web/components/GroupActivity.tsx:7](../../../web/components/GroupActivity.tsx#L7) |
| UI-00453 | {guestGifts?'Add a Special Gift':giftAvailable?'Group contribution':'Gift held with love ♡'} | {launch} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:79](../../../web/components/GroupContribution.tsx#L79) |
| UI-00455 | × | {()=>void closeAndLock()} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:80](../../../web/components/GroupContribution.tsx#L80) |
| UI-00456 | Sign out | {()=>void logout()} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:81](../../../web/components/GroupContribution.tsx#L81) |
| UI-00459 | {guestGifts?'Create account':'Create Group'} | {()=>{setTab('register');setError('')}} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:81](../../../web/components/GroupContribution.tsx#L81) |
| UI-00460 | {guestGifts?'Sign in':'Open Group'} | {()=>{setTab('login');setError('')}} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:81](../../../web/components/GroupContribution.tsx#L81) |
| UI-00473 | {busy?'Please wait…':tab==='register'?'Register →':'Sign in →'} | submit / component action | {auth} | [web/components/GroupContribution.tsx:81](../../../web/components/GroupContribution.tsx#L81) |
| UI-00474 | Edit name | {()=>{setAdminNameDraft(adminName);setEditingName(true)}} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:81](../../../web/components/GroupContribution.tsx#L81) |
| UI-00477 | Save name | submit / component action | {async e=>{e.preventDefault();try{const x=await call('/profile-name',{fullName:adminNameDraft});setAdminName(x.fullName);setEditingName(false);setError('')}catch(x){setError(x instanceof Error?x.message:'Unable to save name')}}} | [web/components/GroupContribution.tsx:81](../../../web/components/GroupContribution.tsx#L81) |
| UI-00478 | Sign out | {()=>void logout()} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:81](../../../web/components/GroupContribution.tsx#L81) |
| UI-00483 | Save PIN | submit / component action | {async e=>{e.preventDefault();setPinNotice('');try{await call('/pin/setup',{password:pinPassword,pin,confirmPin});setPinNotice('PIN updated successfully. You can now sign in with your new six-digit PIN.');setPinPassword('');setPin('');setConfirmPin('');setEditingPin(false)}catch(x){setPinNotice(x instanceof Error?x.message:'PIN setup failed')}}} | [web/components/GroupContribution.tsx:81](../../../web/components/GroupContribution.tsx#L81) |
| UI-00485 | 1. Receiving account | {()=>setSection('payments')} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:81](../../../web/components/GroupContribution.tsx#L81) |
| UI-00486 | 2. Enable contributions | {()=>setSection('guest')} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:81](../../../web/components/GroupContribution.tsx#L81) |
| UI-00487 | 3. Invite friends | {()=>setSection('overview')} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:81](../../../web/components/GroupContribution.tsx#L81) |
| UI-00488 | Overview | {()=>{setSection('overview');void restore()}} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:81](../../../web/components/GroupContribution.tsx#L81) |
| UI-00489 | Privacy & settings | {()=>setSection('privacy')} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:81](../../../web/components/GroupContribution.tsx#L81) |
| UI-00490 | Members & requests | {()=>void loadMembers()} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:81](../../../web/components/GroupContribution.tsx#L81) |
| UI-00491 | Payment accounts | {()=>setSection('payments')} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:81](../../../web/components/GroupContribution.tsx#L81) |
| UI-00492 | ♡ My contribution | {()=>setSection('contribute')} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:81](../../../web/components/GroupContribution.tsx#L81) |
| UI-00493 | Guest contributions | {()=>setSection('guest')} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:81](../../../web/components/GroupContribution.tsx#L81) |
| UI-00495 | Copy invite link | {()=>void navigator.clipboard.writeText(`${location.origin}/groups/${token}/${group.id}`)} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:81](../../../web/components/GroupContribution.tsx#L81) |
| UI-00496 | Invite link unavailable | submit / component action | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:81](../../../web/components/GroupContribution.tsx#L81) |
| UI-00497 | {published?'Collection published':'Publish collection to host'} | {()=>void publishGroup()} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:81](../../../web/components/GroupContribution.tsx#L81) |
| UI-00498 | Approve | {()=>void reviewMember(m.id,'APPROVE')} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:81](../../../web/components/GroupContribution.tsx#L81) |
| UI-00499 | Reject | {()=>void reviewMember(m.id,'REJECT')} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:81](../../../web/components/GroupContribution.tsx#L81) |
| UI-00504 | {busy?'Saving…':'Save privacy settings'} | submit / component action | {savePrivacy} | [web/components/GroupContribution.tsx:81](../../../web/components/GroupContribution.tsx#L81) |
| UI-00515 | {busy?'Creating…':'Create private group →'} | submit / component action | {setup} | [web/components/GroupContribution.tsx:81](../../../web/components/GroupContribution.tsx#L81) |
| UI-00516 | {d.enabled?'Disable account':'Enable account'} | {()=>void run(`/destinations/${d.id}/enabled`,{enabled:!d.enabled})} | Not a native form submit or inherited from component context | [web/components/GroupCreatorPaymentSettings.tsx:20](../../../web/components/GroupCreatorPaymentSettings.tsx#L20) |
| UI-00518 | Generate QR | {()=>void run(`/destinations/${d.id}/qr/generate`)} | Not a native form submit or inherited from component context | [web/components/GroupCreatorPaymentSettings.tsx:20](../../../web/components/GroupCreatorPaymentSettings.tsx#L20) |
| UI-00519 | Preview QR | {()=>void showQr(d.id)} | Not a native form submit or inherited from component context | [web/components/GroupCreatorPaymentSettings.tsx:20](../../../web/components/GroupCreatorPaymentSettings.tsx#L20) |
| UI-00520 | Make preferred | {()=>void run('/preferred',{destinationId:d.id})} | Not a native form submit or inherited from component context | [web/components/GroupCreatorPaymentSettings.tsx:20](../../../web/components/GroupCreatorPaymentSettings.tsx#L20) |
| UI-00522 | Close preview | {()=>{URL.revokeObjectURL(preview);setPreview(null)}} | Not a native form submit or inherited from component context | [web/components/GroupCreatorPaymentSettings.tsx:22](../../../web/components/GroupCreatorPaymentSettings.tsx#L22) |
| UI-00530 | {busy?'Saving securely…':'Save payment account'} | submit / component action | {add} | [web/components/GroupCreatorPaymentSettings.tsx:24](../../../web/components/GroupCreatorPaymentSettings.tsx#L24) |
| UI-00534 | Send verification code | submit / component action | {start} | [web/components/GroupEmailVerification.tsx:15](../../../web/components/GroupEmailVerification.tsx#L15) |
| UI-00537 | Verify email | submit / component action | {finish} | [web/components/GroupEmailVerification.tsx:15](../../../web/components/GroupEmailVerification.tsx#L15) |
| UI-00539 | Try holding this gift again | {()=>void change(false)} | Not a native form submit or inherited from component context | [web/components/GroupGiftPlan.tsx:7](../../../web/components/GroupGiftPlan.tsx#L7) |
| UI-00540 | Yes, release gift | {()=>void change(true)} | Not a native form submit or inherited from component context | [web/components/GroupGiftPlan.tsx:7](../../../web/components/GroupGiftPlan.tsx#L7) |
| UI-00541 | Keep gift | {()=>setConfirmRelease(false)} | Not a native form submit or inherited from component context | [web/components/GroupGiftPlan.tsx:7](../../../web/components/GroupGiftPlan.tsx#L7) |
| UI-00542 | Release gift · keep our collection | {()=>setConfirmRelease(true)} | Not a native form submit or inherited from component context | [web/components/GroupGiftPlan.tsx:7](../../../web/components/GroupGiftPlan.tsx#L7) |
| UI-00544 | Add gift to our group | {()=>void change(false)} | Not a native form submit or inherited from component context | [web/components/GroupGiftPlan.tsx:7](../../../web/components/GroupGiftPlan.tsx#L7) |
| UI-00548 | Make another contribution | {()=>{setStage('start');setReference('');setSubmissionKey(crypto.randomUUID());setDetails(null)}} | Not a native form submit or inherited from component context | [web/components/GroupGuestCheckout.tsx:22](../../../web/components/GroupGuestCheckout.tsx#L22) |
| UI-00549 | Use suggestion | {()=>setAmount(String(invite.suggested_contribution_paise!/100))} | Not a native form submit or inherited from component context | [web/components/GroupGuestCheckout.tsx:22](../../../web/components/GroupGuestCheckout.tsx#L22) |
| UI-00553 |  | {()=>void choosePayment(d.id)} | Not a native form submit or inherited from component context | [web/components/GroupGuestCheckout.tsx:22](../../../web/components/GroupGuestCheckout.tsx#L22) |
| UI-00555 | {details.kind==='UPI'?'Show payment QR':'Show bank details'} | {()=>setModal(true)} | {submit} | [web/components/GroupGuestCheckout.tsx:22](../../../web/components/GroupGuestCheckout.tsx#L22) |
| UI-00557 | {busy?'Submitting…':'I have done the payment'} | submit / component action | {submit} | [web/components/GroupGuestCheckout.tsx:22](../../../web/components/GroupGuestCheckout.tsx#L22) |
| UI-00558 | Change payment method | {()=>{setStage('start');setDetails(null);setSelected('');setReference('')}} | {submit} | [web/components/GroupGuestCheckout.tsx:22](../../../web/components/GroupGuestCheckout.tsx#L22) |
| UI-00559 | Copy bank details | {()=>void navigator.clipboard.writeText(`${details.accountHolder}\n${details.accountNumber}\n${details.ifsc}`)} | Not a native form submit or inherited from component context | [web/components/GroupGuestCheckout.tsx:24](../../../web/components/GroupGuestCheckout.tsx#L24) |
| UI-00560 | Continue to reference details → | {()=>setModal(false)} | Not a native form submit or inherited from component context | [web/components/GroupGuestCheckout.tsx:24](../../../web/components/GroupGuestCheckout.tsx#L24) |
| UI-00567 | Save guest contribution settings | submit / component action | {save} | [web/components/GroupGuestCreatorSettings.tsx:12](../../../web/components/GroupGuestCreatorSettings.tsx#L12) |
| UI-00571 | Contribute | {onContribute} | Not a native form submit or inherited from component context | [web/components/GuestActions.tsx:11](../../../web/components/GuestActions.tsx#L11) |
| UI-00573 | Save the date | {saveDate} | Not a native form submit or inherited from component context | [web/components/GuestActions.tsx:11](../../../web/components/GuestActions.tsx#L11) |
| UI-00581 | {busy?'Sending…':'Send my RSVP →'} | submit / component action | {submitRsvp} | [web/components/GuestCelebration.tsx:29](../../../web/components/GuestCelebration.tsx#L29) |
| UI-00587 | Remove | {()=>setPhoto(null)} | {submitWish} | [web/components/GuestCelebration.tsx:32](../../../web/components/GuestCelebration.tsx#L32) |
| UI-00588 | {busy?'Sending…':'Send my wishes ♡'} | submit / component action | {submitWish} | [web/components/GuestCelebration.tsx:32](../../../web/components/GuestCelebration.tsx#L32) |
| UI-00590 | ↻ Refresh wishes | {()=>void refreshWishes()} | Not a native form submit or inherited from component context | [web/components/GuestCelebration.tsx:35](../../../web/components/GuestCelebration.tsx#L35) |
| UI-00591 | {showWishes?'Show fewer wishes':'View all wishes'} | {()=>setShowWishes(v=>!v)} | Not a native form submit or inherited from component context | [web/components/GuestCelebration.tsx:36](../../../web/components/GuestCelebration.tsx#L36) |
| UI-00593 | Refresh fee | {()=>{requestKey.current='';setPricingRetry(v=>v+1)}} | Not a native form submit or inherited from component context | [web/components/GuestContributionCheckout.tsx:108](../../../web/components/GuestContributionCheckout.tsx#L108) |
| UI-00596 | Retry | {()=>setDestinationRetry(v=>v+1)} | Not a native form submit or inherited from component context | [web/components/GuestContributionCheckout.tsx:130](../../../web/components/GuestContributionCheckout.tsx#L130) |
| UI-00597 | ✕ | {onClose} | Not a native form submit or inherited from component context | [web/components/GuestContributionCheckout.tsx:132](../../../web/components/GuestContributionCheckout.tsx#L132) |
| UI-00599 | Make another contribution | {()=>{setDirectStatus('');setTransferReference('');requestKey.current='';}} | Not a native form submit or inherited from component context | [web/components/GuestContributionCheckout.tsx:139](../../../web/components/GuestContributionCheckout.tsx#L139) |
| UI-00601 | Make another contribution | {()=>{setReceipt(null);requestKey.current='';setDirectStatus('');}} | Not a native form submit or inherited from component context | [web/components/GuestContributionCheckout.tsx:140](../../../web/components/GuestContributionCheckout.tsx#L140) |
| UI-00605 | Retry | {()=>setDestinationRetry(v=>v+1)} | {submit} | [web/components/GuestContributionCheckout.tsx:143](../../../web/components/GuestContributionCheckout.tsx#L143) |
| UI-00611 | {showContact?'− Hide optional contact details':'+ Add email or phone (optional)'} | {()=>setShowContact(v=>!v)} | {submit} | [web/components/GuestContributionCheckout.tsx:154](../../../web/components/GuestContributionCheckout.tsx#L154) |
| UI-00616 | I have done the payment | {reportDirectPayment} | {submit} | [web/components/GuestContributionCheckout.tsx:165](../../../web/components/GuestContributionCheckout.tsx#L165) |
| UI-00618 | {busy?'Processing…':`Pay ${validAmount&&pricing?money(pricing.totalPaise):''} (Test Mode)`} | submit / component action | {submit} | [web/components/GuestContributionCheckout.tsx:173](../../../web/components/GuestContributionCheckout.tsx#L173) |
| UI-00619 | Refresh gifts | {()=>void load()} | Not a native form submit or inherited from component context | [web/components/GuestGiftBoard.tsx:10](../../../web/components/GuestGiftBoard.tsx#L10) |
| UI-00622 | Try again | {()=>void load()} | Not a native form submit or inherited from component context | [web/components/GuestGiftBoard.tsx:10](../../../web/components/GuestGiftBoard.tsx#L10) |
| UI-00632 | Add gift | submit / component action | {add} | [web/components/GuestGiftManager.tsx:15](../../../web/components/GuestGiftManager.tsx#L15) |
| UI-00638 | View delivery address | {async()=>{setError('');setBusy(true);try{const address=await call(`/guest-gifts/${g.id}/delivery-address`);setAddresses(value=>({...value,[g.id]:address}))}catch(e){setError(e instanceof Error?e.message:'Could not load delivery address')}finally{setBusy(false)}}} | Not a native form submit or inherited from component context | [web/components/GuestGiftManager.tsx:15](../../../web/components/GuestGiftManager.tsx#L15) |
| UI-00639 | Publish gift to host | {()=>void mutate(`/guest-gifts/${g.id}/publish`,{},'Gift published. The host can now see it in their contribution report.')} | Not a native form submit or inherited from component context | [web/components/GuestGiftManager.tsx:15](../../../web/components/GuestGiftManager.tsx#L15) |
| UI-00640 | {g.disabled?g.selection_label:type==='GROUP'?'Select for our group':'Select this gift'} | {()=>void mutate(`/guest-gifts/${g.id}/select`,{contributionType:type,...(type==='GROUP'?{groupId:group!.id}:{})},'Gift selected. You can now view the delivery address and publish when ready.')} | Not a native form submit or inherited from component context | [web/components/GuestGiftManager.tsx:15](../../../web/components/GuestGiftManager.tsx#L15) |
| UI-00643 | Retry | {()=>setRetry(v=>v+1)} | Not a native form submit or inherited from component context | [web/components/GuestVerifiedDestination.tsx:35](../../../web/components/GuestVerifiedDestination.tsx#L35) |
| UI-00648 | Copy UPI ID | {()=>navigator.clipboard.writeText(selected.upiId\|\|'').then(()=>setCopyNotice('UPI ID copied.')).catch(()=>setCopyNotice('Select the UPI ID above to copy it.'))} | Not a native form submit or inherited from component context | [web/components/GuestVerifiedDestination.tsx:44](../../../web/components/GuestVerifiedDestination.tsx#L44) |
| UI-00650 | Show UPI QR | {()=>setQrOpen(true)} | Not a native form submit or inherited from component context | [web/components/GuestVerifiedDestination.tsx:46](../../../web/components/GuestVerifiedDestination.tsx#L46) |
| UI-00652 | ✕ Close | {()=>setQrOpen(false)} | Not a native form submit or inherited from component context | [web/components/GuestVerifiedDestination.tsx:49](../../../web/components/GuestVerifiedDestination.tsx#L49) |
| UI-00664 | {collapsed?<PanelLeftOpen size={20}/>:<PanelLeftClose size={20}/>} | {toggle} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:43](../../../web/components/HostWorkspace.tsx#L43) |
| UI-00667 |  | {()=>void signOut()} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:46](../../../web/components/HostWorkspace.tsx#L46) |
| UI-00668 |  | {()=>setCollapsed(true)} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:47](../../../web/components/HostWorkspace.tsx#L47) |
| UI-00671 | Fill gift details from link | {()=>void lookup()} | Not a native form submit or inherited from component context | [web/components/IndependentGroupPlan.tsx:16](../../../web/components/IndependentGroupPlan.tsx#L16) |
| UI-00677 | Save our gift plan | submit / component action | {save} | [web/components/IndependentGroupPlan.tsx:22](../../../web/components/IndependentGroupPlan.tsx#L22) |
| UI-00685 | {busy?'Sharing payment details…':'I’ve paid — share details with the host'} | submit / component action | {submit} | [web/components/IndividualHostContribution.tsx:22](../../../web/components/IndividualHostContribution.tsx#L22) |
| UI-00686 | Try again | {()=>setRetry(v=>v+1)} | Not a native form submit or inherited from component context | [web/components/IndividualHostContribution.tsx:22](../../../web/components/IndividualHostContribution.tsx#L22) |
| UI-00747 | Use a different mobile number | {()=>{setVerified(false);setMessage('')}} | Not a native form submit or inherited from component context | [web/components/MobileVerification.tsx:13](../../../web/components/MobileVerification.tsx#L13) |
| UI-00751 | {busy?'Please wait…':challenge?'Verify mobile':'Send verification code'} | submit / component action | {submit} | [web/components/MobileVerification.tsx:13](../../../web/components/MobileVerification.tsx#L13) |
| UI-00752 | Change number / request another code | {()=>{setChallenge('');setCode('');setMessage('Wait at least 60 seconds before requesting another code.')}} | {submit} | [web/components/MobileVerification.tsx:13](../../../web/components/MobileVerification.tsx#L13) |
| UI-00803 | Share invitation | {()=>void share()} | Not a native form submit or inherited from component context | [web/components/SocialSharing.tsx:13](../../../web/components/SocialSharing.tsx#L13) |
| UI-00807 | {copied?<Check size={16}/>:<Copy size={16}/>} {copied?'Copied':'Copy link'} | {()=>void copy()} | Not a native form submit or inherited from component context | [web/components/SocialSharing.tsx:13](../../../web/components/SocialSharing.tsx#L13) |
| UI-00810 | Add a Special Gift | {()=>{setStep('choice');setError('')}} | Not a native form submit or inherited from component context | [web/components/SpecialGiftEntry.tsx:63](../../../web/components/SpecialGiftEntry.tsx#L63) |
| UI-00812 | × | {close} | Not a native form submit or inherited from component context | [web/components/SpecialGiftEntry.tsx:66](../../../web/components/SpecialGiftEntry.tsx#L66) |
| UI-00813 |  | {()=>void individual()} | Not a native form submit or inherited from component context | [web/components/SpecialGiftEntry.tsx:67](../../../web/components/SpecialGiftEntry.tsx#L67) |
| UI-00814 |  | {()=>{setError('');setStep('group')}} | Not a native form submit or inherited from component context | [web/components/SpecialGiftEntry.tsx:67](../../../web/components/SpecialGiftEntry.tsx#L67) |
| UI-00815 | ← Giving options | {()=>setStep('choice')} | Not a native form submit or inherited from component context | [web/components/SpecialGiftEntry.tsx:68](../../../web/components/SpecialGiftEntry.tsx#L68) |
| UI-00818 | View host delivery address | {async()=>{setBusy(true);setError('');try{await showAddress(saved.id)}catch(e){setError(e instanceof Error?e.message:'Address unavailable')}finally{setBusy(false)}}} | Not a native form submit or inherited from component context | [web/components/SpecialGiftEntry.tsx:69](../../../web/components/SpecialGiftEntry.tsx#L69) |
| UI-00819 | {saved.delivery_status==='GIFT_PURCHASED'\|\|saved.delivery_status==='TRACKING_SHARED'?'Gift purchased':'I have purchased the gift'} | {()=>void updateDelivery('GIFT_PURCHASED')} | Not a native form submit or inherited from component context | [web/components/SpecialGiftEntry.tsx:69](../../../web/components/SpecialGiftEntry.tsx#L69) |
| UI-00822 | Share tracking with host | submit / component action | {e=>{e.preventDefault();void updateDelivery('TRACKING_SHARED')}} | [web/components/SpecialGiftEntry.tsx:69](../../../web/components/SpecialGiftEntry.tsx#L69) |
| UI-00823 | Add another special gift | {()=>{setSaved(null);setSource('');setForm(empty);setAddress(null);setTracking('');setNotice('');void individual()}} | Not a native form submit or inherited from component context | [web/components/SpecialGiftEntry.tsx:69](../../../web/components/SpecialGiftEntry.tsx#L69) |
| UI-00838 | {busy?'Saving your gift…':method==='LINK_AND_CONTRIBUTION'?'Continue to host payment':'Add my special gift'} | submit / component action | {save} | [web/components/SpecialGiftEntry.tsx:75](../../../web/components/SpecialGiftEntry.tsx#L75) |
| UI-00839 | Manage {g.name} | {()=>{setSaved(g);setAddress(null);setTracking('');setNotice('')}} | Not a native form submit or inherited from component context | [web/components/SpecialGiftEntry.tsx:77](../../../web/components/SpecialGiftEntry.tsx#L77) |
| UI-00841 | ✕ | {p.close} | Not a native form submit or inherited from component context | [web/components/focused/Checkout.tsx:7](../../../web/components/focused/Checkout.tsx#L7) |
| UI-00843 | Back to the celebration | {p.close} | Not a native form submit or inherited from component context | [web/components/focused/Checkout.tsx:7](../../../web/components/focused/Checkout.tsx#L7) |
| UI-00845 | ₹ {Number(value).toLocaleString('en-IN')} | {()=>p.setAmount(value)} | {e=>{if(step===0){e.preventDefault();if(p.valid&&p.name.trim())setStep(1)}else p.submit(e)}} | [web/components/focused/Checkout.tsx:7](../../../web/components/focused/Checkout.tsx#L7) |
| UI-00848 | {contact?'Hide optional contact details':'+ Add email or phone · optional'} | {()=>setContact(v=>!v)} | {e=>{if(step===0){e.preventDefault();if(p.valid&&p.name.trim())setStep(1)}else p.submit(e)}} | [web/components/focused/Checkout.tsx:7](../../../web/components/focused/Checkout.tsx#L7) |
| UI-00851 | Continue → | submit / component action | {e=>{if(step===0){e.preventDefault();if(p.valid&&p.name.trim())setStep(1)}else p.submit(e)}} | [web/components/focused/Checkout.tsx:7](../../../web/components/focused/Checkout.tsx#L7) |
| UI-00852 | Edit amount | {()=>setStep(0)} | {e=>{if(step===0){e.preventDefault();if(p.valid&&p.name.trim())setStep(1)}else p.submit(e)}} | [web/components/focused/Checkout.tsx:7](../../../web/components/focused/Checkout.tsx#L7) |
| UI-00856 | {p.busy?'Opening secure checkout…':`Pay ${inr(p.totalPaise??amount)} · Test mode`} | submit / component action | {e=>{if(step===0){e.preventDefault();if(p.valid&&p.name.trim())setStep(1)}else p.submit(e)}} | [web/components/focused/Checkout.tsx:7](../../../web/components/focused/Checkout.tsx#L7) |
| UI-00857 | {p.busy?'Sending details…':'Submit transfer details'} | {p.report} | {e=>{if(step===0){e.preventDefault();if(p.valid&&p.name.trim())setStep(1)}else p.submit(e)}} | [web/components/focused/Checkout.tsx:7](../../../web/components/focused/Checkout.tsx#L7) |
| UI-00898 | Try again | {onRetry} | Not a native form submit or inherited from component context | [web/components/focused/Invitation.tsx:26](../../../web/components/focused/Invitation.tsx#L26) |
| UI-00900 | {(g.contributionEnabled===false)?g.contributionDisabledReason\|\|"A lovely surprise is on its way ♡":preview?'Contribution preview only':'Contribute'} | {()=>open(g)} | Not a native form submit or inherited from component context | [web/components/focused/Invitation.tsx:26](../../../web/components/focused/Invitation.tsx#L26) |
| UI-00901 | Choose an amount | {()=>open(null)} | Not a native form submit or inherited from component context | [web/components/focused/Invitation.tsx:26](../../../web/components/focused/Invitation.tsx#L26) |
| UI-00903 | Try again | {()=>location.reload()} | Not a native form submit or inherited from component context | [web/components/focused/Invitation.tsx:31](../../../web/components/focused/Invitation.tsx#L31) |


### Dialog and alert/status inventory

| ID | Role / kind | Text / label | Visibility conditions | Source |
| --- | --- | --- | --- | --- |
| UI-00079 | "dialog" | {selectedGift&&<div className="loveModalGift"><h2>{selectedGift.name}</h2><p>{selectedGift.description}</p><div className="loveModalMetrics"><span>Gift Value: {rupees(selectedGift.targetAmount)}</span><span>Contributions Received: {progressValue(rupees((progress[selectedGift.id]?.contributedPaise??0)/100))}</span><span>Still to Be Gifted: {progressValue(rupees(Math.max(0,selectedGift.targetAmount-(progress[selectedGift.id]?.contributedPaise??0)/100)))}</span><span>Contributions Made: {progressValue(progress[selectedGift.id]?.contributionCount??0)}</span></div></div>} | (selectedGift\|\|walletOpen) | [web/app/e/[token]/page.tsx:79](../../../web/app/e/[token]/page.tsx#L79) |
| UI-00446 | dialog |  | Parent/source state | [web/components/GiftDescription.tsx:9](../../../web/components/GiftDescription.tsx#L9) |
| UI-00454 | "dialog" | {logged&&guestGifts&&<><div className="gc-dash-head"><h2>Your guest account</h2>{!group&&<button type="button" disabled={busy} onClick={()=>void logout()}>Sign out</button>}</div><GuestGiftManager token={token} call={call} group={group}/></>} {logged&&accountNotice&&<p role="status">{accountNotice}</p>} {logged&&(emailVerificationEnabled\|\|mobileOtpEnabled)&&<details className="gc-panel"><summary>Account · contact verification</summary><GroupEmailVerification token={token} pageId={pageId.current!} onVerified={()=>void refreshPlan()}/><MobileVerification token={token} pageId={pageId.current!} onVerified={()=>void restore()}/></details>} {error&&<p className="gc-error gc-sticky-feedback" role="alert">{error}</p>} {loading?<p>Opening your group…</p>:!logged?<><div className="gc-auth-heading"><span className="gc-auth-icon" aria-hidden="true">♧</span><h2>{guestGifts?"A thoughtful gift from you.":"A thoughtful gift, together."}</h2><p>{guestGifts?"Sign in or create an account to manage an individual gift or organize a group gift.":"Create a group or sign in to organize your people."}</p></div><div className="gc-tabs" role="tablist"><button type="button" aria-selected={tab==='register'} role="tab" onClick={()=>{setTab('register');setError('')}}>{guestGifts?'Create account':'Create Group'}</button><button type="button" aria-selected={tab==='login'} role="tab" onClick={()=>{setTab('login');setError('')}}>{guestGifts?'Sign in':'Open Group'}</button></div>{emailVerificationEnabled&&<div className="recovery-links"><a href={`/forgot-username?kind=GROUP_ADMIN&eventToken=${encodeURIComponent(token)}&returnTo=${encodeURIComponent('/e/'+token)}`}>Forgot username?</a><a href={`/forgot-password?kind=GROUP_ADMIN&returnTo=${encodeURIComponent('/e/'+token)}`}>Forgot password or PIN?</a></div>}<form className={`gc-form gc-auth-form ${tab==='register'?'gc-register-grid':''}`} onSubmit={auth}>{tab==='register'&&<><SignupEmailField value={registrationEmail} onChange={setRegistrationEmail}/><label>Mobile number<input required type="tel" autoComplete="tel" placeholder="+919876543210" value={registrationMobile} onChange={e=>setRegistrationMobile(e.target.value)}/></label><p>Use your own email and mobile for account recovery.</p></>}{tab==='register'&&<label>Full name<input required minLength={2} maxLength={120} value={fullName} onChange={e=>setFullName(e.target.value)}/></label>}{<label>Username<input required minLength={3} maxLength={40} autoComplete="username" autoCapitalize="none" spellCheck={false} value={username} onChange={e=>setUsername(e.target.value)}/></label>}{tab==='login'&&<label>Sign in with<select value={loginMethod} onChange={e=>setLoginMethod(e.target.value as 'password'\|'pin')}>{allowPin&&<option value="pin">Username and six-digit PIN</option>}<option value="password">Username and password</option></select></label>}{(tab==='register'\|\|loginMethod==='password')&&<label>Password<input required minLength={8} maxLength={128} type="password" autoComplete={tab==='register'?'new-password':'current-password'} value={password} onChange={e=>setPassword(e.target.value)}/></label>}{tab==='register'&&<label>Confirm password<input required type="password" minLength={8} value={confirm} onChange={e=>setConfirm(e.target.value)}/></label>}{tab==='register'&&allowPin&&<><label>Six-digit PIN<input required type="password" inputMode="numeric" pattern="[0-9]{6}" minLength={6} maxLength={6} value={pin} onChange={e=>setPin(e.target.value)}/></label><label>Confirm PIN<input required type="password" inputMode="numeric" pattern="[0-9]{6}" minLength={6} maxLength={6} value={confirmPin} onChange={e=>setConfirmPin(e.target.value)}/></label></>}{tab==='login'&&loginMethod==='pin'&&<label>Six-digit PIN<input required type="password" inputMode="numeric" pattern="[0-9]{6}" minLength={6} maxLength={6} value={pin} onChange={e=>setPin(e.target.value)}/></label>}<button disabled={busy\|\|allowPin===null\|\|(tab==='register'&&!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(registrationEmail.trim()))} className="gc-launch">{busy?'Please wait…':tab==='register'?'Register →':'Sign in →'}</button></form></>:group?<>{!published&&<section className="gc-panel"><h3>Group coordination fee</h3><p>A one-time fee paid by the creator. Member transfers still go directly to the creator. If the fee is switched off, publish without a purchase.</p><CommercePurchase stream="GROUP_GIFT" resourceId={group.id} groupBase={`/api/public/groups/${token}/coordination/${group.id}`} pageId={pageId.current!} onComplete={()=>void restore()}/></section>}<details className="gc-panel gc-profile"><summary>Your profile · {adminName\|\|'Organizer'}</summary>{adminName&&!editingName?<><h3>Welcome back, {adminName}!</h3><button type="button" className="gc-secondary" onClick={()=>{setAdminNameDraft(adminName);setEditingName(true)}}>Edit name</button></>:<form className="gc-form" onSubmit={async e=>{e.preventDefault();try{const x=await call('/profile-name',{fullName:adminNameDraft});setAdminName(x.fullName);setEditingName(false);setError('')}catch(x){setError(x instanceof Error?x.message:'Unable to save name')}}}><label>What should we call you?<input required minLength={2} maxLength={120} value={adminNameDraft} onChange={e=>setAdminNameDraft(e.target.value)}/></label><button className="gc-secondary">Save name</button></form>}</details><div className="gc-dash-head"><div><h2>{group.name}</h2><p>Created by {group.creatorName}</p></div><div className="gc-dash-actions"><span className="gc-status">{independent?'Group created ♡':group.status==='DRAFT'?'Private draft':group.status}</span><button type="button" className="gc-secondary" disabled={busy} onClick={()=>void logout()}>Sign out</button></div></div>{allowPin&&<details className="gc-panel" open={editingPin} onToggle={e=>setEditingPin(e.currentTarget.open)}><summary>Account security (change PIN)</summary><form className="gc-form" onSubmit={async e=>{e.preventDefault();setPinNotice('');try{await call('/pin/setup',{password:pinPassword,pin,confirmPin});setPinNotice('PIN updated successfully. You can now sign in with your new six-digit PIN.');setPinPassword('');setPin('');setConfirmPin('');setEditingPin(false)}catch(x){setPinNotice(x instanceof Error?x.message:'PIN setup failed')}}}><label>Current password<input required type="password" value={pinPassword} onChange={e=>setPinPassword(e.target.value)}/></label><label>Six-digit PIN<input required type="password" inputMode="numeric" pattern="[0-9]{6}" maxLength={6} value={pin} onChange={e=>setPin(e.target.value)}/></label><label>Confirm PIN<input required type="password" inputMode="numeric" pattern="[0-9]{6}" maxLength={6} value={confirmPin} onChange={e=>setConfirmPin(e.target.value)}/></label><button className="gc-secondary">Save PIN</button>{pinNotice&&<p role="status">{pinNotice}</p>}</form></details>}<div className="gc-metrics"><div><small>Gift goal</small><strong>{group.targetPaise!=null?`₹${(group.targetPaise/100).toLocaleString('en-IN')}`:'Not set'}</strong></div><div><small>Members</small><strong>{summary?.member_count??'—'}</strong></div><div><small>Reported transfers — unverified</small><strong>{summary?`₹${(Number(summary.reported_paise)/100).toLocaleString('en-IN')}`:'—'}</strong></div></div>{independent&&<section className="gc-panel"><h3>Your group is cooking up something special 🍳</h3><p>Your group is now listed on the invitation. Add a receiving account, enable contributions, then share your group link. Friends can give with or without signing in.</p><button type="button" className="gc-secondary" onClick={()=>setSection('payments')}>1. Receiving account</button> <button type="button" className="gc-secondary" onClick={()=>setSection('guest')}>2. Enable contributions</button> <button type="button" className="gc-secondary" onClick={()=>setSection('overview')}>3. Invite friends</button></section>}<div className="gc-nav"><button type="button" aria-pressed={section==='overview'} onClick={()=>{setSection('overview');void restore()}}>Overview</button><button type="button" aria-pressed={section==='privacy'} onClick={()=>setSection('privacy')}>Privacy & settings</button><button type="button" aria-pressed={section==='members'} onClick={()=>void loadMembers()}>Members & requests</button><button type="button" aria-pressed={section==='payments'} onClick={()=>setSection('payments')}>Payment accounts</button><button type="button" aria-pressed={section==='contribute'} onClick={()=>setSection('contribute')}>♡ My contribution</button><button type="button" aria-pressed={section==='guest'} onClick={()=>setSection('guest')}>Guest contributions</button></div>{section==='contribute'?<div className="gc-panel"><h3>Make your contribution</h3><p>Join your group’s gift and keep your transfer details in one place.</p>{ready.enabled?<GroupGuestCheckout token={token} groupId={group.id} creator creatorName={group.creatorName} pageId={pageId.current!}/>:<p>Enable the invite link from Overview, then enable a payment method in Guest contributions to start.</p>}</div>:section==='guest'?<GroupGuestCreatorSettings token={token} groupId={group.id} pageId={pageId.current!}/>:section==='payments'?<GroupCreatorPaymentSettings token={token} groupId={group.id} pageId={pageId.current!}/>:section==='overview'?<div className="gc-panel"><h3>Group overview</h3>{group.giftName&&<p>🎁 {group.giftName}</p>}{group.description&&<p>{group.description}</p>}<p>Group membership is available. Share the invite link after enabling it.</p>{ready.enabled?<><p>Group link: <a href={`/groups/${token}/${group.id}`}>Open member join page</a></p><button type="button" className="gc-secondary" onClick={()=>void navigator.clipboard.writeText(`${location.origin}/groups/${token}/${group.id}`)}>Copy invite link</button></>:<><span title={ready.reasons.join('; ')}><button disabled>Invite link unavailable</button></span><p>{ready.reasons.join('; ')}</p></>}<p><strong>Host visibility:</strong> {published?'Collection published to the host.':'Your collection stays private until you publish it.'}</p><span title={ready.reasons.join('; ')}><button type="button" className="gc-launch" disabled={busy\|\|!ready.enabled\|\|published} onClick={()=>void publishGroup()}>{published?'Collection published':'Publish collection to host'}</button></span>{independent?<IndependentGroupPlan key={group.id} group={group} call={call} onChange={refreshPlan} busy={busy} setBusy={setBusy}/>:<GroupGiftPlan groupId={group.id} giftName={group.giftName} selectedGiftId={selectedGiftId} reserved={reserved} emailVerified={creatorEmailVerified} wishlist={wishlist} call={call} onChange={refreshPlan} onNotice={setAccountNotice} busy={busy} setBusy={setBusy}/>}<GroupActivity url={base+`/activity/${group.id}`} pageId={pageId.current!} revision={historyRevision}/></div>:section==='members'?<div className="gc-panel"><h3>Member requests</h3><p>Rejected members are visible only here. Mobile numbers are private.</p>{members.length===0?<p>No requests yet.</p>:members.map(m=><div key={m.id} className="gc-member"><strong>{m.display_name}</strong> · {m.status}<small>{m.mobile}</small>{m.status==='PENDING'&&<div><button type="button" className="gc-secondary" disabled={busy} onClick={()=>void reviewMember(m.id,'APPROVE')}>Approve</button> <button type="button" className="gc-secondary" disabled={busy} onClick={()=>void reviewMember(m.id,'REJECT')}>Reject</button></div>}</div>)}</div>:<form className="gc-form gc-panel" onSubmit={savePrivacy}><h3>Who can see what?</h3><p>These settings control joining and member visibility. Manual transfers remain unverified until independently confirmed.</p><label>Who can join?<select value={privacy.join_policy} onChange={e=>setPrivacy({...privacy,join_policy:e.target.value})}><option value="APPROVAL">Anyone with invite link — Admin approval</option><option value="AUTO_JOIN">Anyone with invite link — Auto join</option><option value="INVITE_ONLY">Invitation only — individual invitation required</option></select></label><label>Who can see the member list?<select value={privacy.member_visibility} onChange={e=>setPrivacy({...privacy,member_visibility:e.target.value})}><option value="ADMIN_ONLY">Admin only</option><option value="MEMBERS">Approved members</option></select></label><label>Who can see individual contribution amounts?<select value={privacy.amount_visibility} onChange={e=>setPrivacy({...privacy,amount_visibility:e.target.value})}><option value="ADMIN_ONLY">Admin only</option><option value="OWN_ONLY">Each member sees only their own</option></select></label><p>To offer UPI or bank transfers, configure Payment accounts, then choose accounts and contribution access under Guest contributions. Reported transfers do not confirm receipt of funds.</p><p><strong>Host access:</strong> Collection details are shared only when you publish to the host.</p><button className="gc-launch" disabled={busy}>{busy?'Saving…':'Save privacy settings'}</button></form>}</>:<><details open={independent\|\|!guestGifts}><summary>Create a group for a gift together</summary><h2>Create your group</h2><p>{independent?'Tell us about your group. It will appear as Something is Cooking while you plan your special gift.':'Tell us about your group. Only you can see these details for now.'}</p><form className="gc-form" onSubmit={setup}><label>Group name *<input required maxLength={120} value={form.name} onChange={e=>setForm({...form,name:e.target.value})}/></label><label>Your name *<input required maxLength={120} value={form.creatorName} onChange={e=>setForm({...form,creatorName:e.target.value})}/></label><label>Mobile number *<input required type="tel" maxLength={20} value={form.creatorMobile} onChange={e=>setForm({...form,creatorMobile:e.target.value})}/></label><label>Group description<textarea maxLength={500} value={form.description} onChange={e=>setForm({...form,description:e.target.value})}/></label>{independent?<IndependentGroupPlanFields value={form} onChange={value=>setForm(current=>({...current,...value}))} call={call} busy={busy} setBusy={setBusy}/>:<><label>Collection plan<select value={giftId} onChange={e=>{setGiftId(e.target.value);if(!e.target.value)setForm(value=>({...value,giftName:'',giftDescription:'',giftLink:'',targetRupees:''}))}}><option value="">General collection · choose a gift later</option>{wishlist.filter(g=>g.contributionEnabled!==false).map(g=><option key={g.id} value={g.id}>{g.name}</option>)}</select></label>{giftId&&<p>Wishlist gift details are filled in below. The gift stays available until your creator email is verified.</p>}<label>Gift name (optional)<input maxLength={160} readOnly={!!giftId} value={form.giftName} onChange={e=>setForm({...form,giftName:e.target.value})}/></label><label>Gift description<textarea maxLength={500} readOnly={!!giftId} value={form.giftDescription} onChange={e=>setForm({...form,giftDescription:e.target.value})}/></label><label>Gift link<input type="url" maxLength={1000} readOnly={!!giftId} value={form.giftLink} onChange={e=>setForm({...form,giftLink:e.target.value})}/></label><label>Gift goal in ₹<input inputMode="decimal" readOnly={!!giftId} value={form.targetRupees} onChange={e=>setForm({...form,targetRupees:e.target.value})}/></label></>}<button className="gc-launch" disabled={busy}>{busy?'Creating…':'Create private group →'}</button></form></details></>} | open | [web/components/GroupContribution.tsx:80](../../../web/components/GroupContribution.tsx#L80) |
| UI-00521 | "dialog" | "UPI QR preview" | preview | [web/components/GroupCreatorPaymentSettings.tsx:22](../../../web/components/GroupCreatorPaymentSettings.tsx#L22) |
| UI-00651 | "dialog" | {/* eslint-disable-next-line @next/next/no-img-element */} | !focused&&qrOpen; selected?.method==='UPI'&&selected.qrPngBase64&&!focused; eligible.length===0; error; loading | [web/components/GuestVerifiedDestination.tsx:48](../../../web/components/GuestVerifiedDestination.tsx#L48) |
| UI-00811 | "dialog" | {step==='choice'?<><h2 id="special-gift-title">How would you like to give?</h2><p>Plan a thoughtful gift on your own or bring your favourite people together.</p><div className="special-gift-choices"><button data-autofocus type="button" className="gc-secondary" onClick={()=>void individual()}><strong>Individual Gift</strong><span>Add your details and choose how to share your gift.</span><small>No account or group needed</small></button><button type="button" className="gc-secondary" onClick={()=>{setError('');setStep('group')}}><strong>Group Gift</strong><span>Create a group and collect contributions together.</span><small>Sign in or create an account to get started</small></button></div></>:<> <button type="button" className="gc-secondary" disabled={busy} onClick={()=>setStep('choice')}>← Giving options</button><h2 id="special-gift-title">Your special gift</h2> {saved?<><p role="status">{notice\|\|`Manage ${saved.name}`}</p>{saved.giving_method==='LINK_AND_CONTRIBUTION'&&<IndividualHostContribution token={token} giftId={saved.id} call={call} onBusyChange={setBusy} onReported={()=>notifyGuestGifts(token)}/>} {saved.giving_method==='DIRECT_DELIVERY'&&<section className="gc-panel"><h3>Deliver your gift to the host</h3><p>Purchase the item from the retailer and use the host’s address at checkout.</p>{saved.product_url&&<a href={saved.product_url} target="_blank" rel="noopener noreferrer">Purchase gift from the retailer ↗</a>}{address?<address>{address.addressLine1\|\|'Please contact the host to confirm the delivery address.'}<br/>{address.city}, {address.state} {address.postalCode}<br/>{address.country}</address>:<button type="button" disabled={busy} onClick={async()=>{setBusy(true);setError('');try{await showAddress(saved.id)}catch(e){setError(e instanceof Error?e.message:'Address unavailable')}finally{setBusy(false)}}}>View host delivery address</button>}<button type="button" className="gc-secondary" disabled={busy\|\|saved.delivery_status==='TRACKING_SHARED'\|\|saved.delivery_status==='GIFT_PURCHASED'} onClick={()=>void updateDelivery('GIFT_PURCHASED')}>{saved.delivery_status==='GIFT_PURCHASED'\|\|saved.delivery_status==='TRACKING_SHARED'?'Gift purchased':'I have purchased the gift'}</button><form className="gc-form" onSubmit={e=>{e.preventDefault();void updateDelivery('TRACKING_SHARED')}}><label>Shipment tracking details<textarea required maxLength={1000} placeholder="Carrier name, tracking number or tracking link" value={tracking} onChange={e=>setTracking(e.target.value)}/></label><button className="gc-launch" disabled={busy}>Share tracking with host</button></form></section>}<button type="button" className="gc-secondary" disabled={busy} onClick={()=>{setSaved(null);setSource('');setForm(empty);setAddress(null);setTracking('');setNotice('');void individual()}}>Add another special gift</button></>:<form className="gc-form" onSubmit={save}> {gifts.length>0&&<label>Choose a gift<select disabled={busy} value={source} onChange={e=>choose(e.target.value)}><option value="">Add my own special gift</option>{gifts.map(g=><option key={g.id} value={g.id}>{g.name}</option>)}</select></label>} <fieldset disabled={busy}><legend>Your details</legend><label>Your name<input data-autofocus required autoComplete="name" maxLength={120} value={form.guestName} onChange={e=>setForm({...form,guestName:e.target.value})}/></label><label>Email address<input required type="email" autoComplete="email" maxLength={254} value={form.email} onChange={e=>setForm({...form,email:e.target.value})}/></label><label>Mobile number<input required type="tel" autoComplete="tel" pattern="\+?[0-9][0-9 \-]{7,23}" maxLength={25} value={form.mobile} onChange={e=>setForm({...form,mobile:e.target.value})}/></label></fieldset> <fieldset disabled={busy}><legend>Gift details</legend><label>Gift name<input required maxLength={160} value={form.name} onChange={e=>setForm({...form,name:e.target.value})}/></label><label>Product link{method==='DIRECT_DELIVERY'?'':' · optional if you add gift details'}<input type="url" required={method==='DIRECT_DELIVERY'} pattern="https://[^\s]+" placeholder="https://…" maxLength={1000} value={form.productUrl} onChange={e=>setForm({...form,productUrl:e.target.value})}/></label><label>Gift details<textarea maxLength={500} value={form.description} onChange={e=>setForm({...form,description:e.target.value})}/></label></fieldset> <fieldset className="special-gift-methods" disabled={busy}><legend>How would you like to share your gift?</legend><label><input type="radio" name="special-gift-method" checked={method==='LINK_AND_CONTRIBUTION'} onChange={()=>setMethod('LINK_AND_CONTRIBUTION')}/><span>Share a gift link and contribute to the host</span></label><label><input type="radio" name="special-gift-method" checked={method==='DIRECT_DELIVERY'} onChange={()=>setMethod('DIRECT_DELIVERY')}/><span>I’ll purchase and deliver the gift myself</span></label></fieldset> {method==='LINK_AND_CONTRIBUTION'?<label>Contribution amount (₹)<input required type="number" inputMode="decimal" min="1" max="1000000" step="0.01" value={form.amount} onChange={e=>setForm({...form,amount:e.target.value})}/><small>Continue to send this amount directly to the host’s UPI or bank account.</small></label>:<p>After adding your gift, you can view the host delivery address, purchase from the retailer and share tracking details here.</p>} <p className="finePrint">Your contact details and contribution amount are shared only with the host.</p><button className="gc-launch" disabled={busy}>{busy?'Saving your gift…':method==='LINK_AND_CONTRIBUTION'?'Continue to host payment':'Add my special gift'}</button> </form>} {!saved&&previous.length>0&&<section className="gc-panel"><h3>Your gifts in this browser</h3>{previous.map(g=><button key={g.id} type="button" className="gc-secondary" disabled={busy} onClick={()=>{setSaved(g);setAddress(null);setTracking('');setNotice('')}}>Manage {g.name}</button>)}</section>} </>} {error&&<p role="alert" className="gc-error">{error}</p>} | (step==='choice'\|\|step==='individual') | [web/components/SpecialGiftEntry.tsx:65](../../../web/components/SpecialGiftEntry.tsx#L65) |
| UI-00894 | dialog | {children} | Parent/source state | [web/components/focused/Invitation.tsx:16](../../../web/components/focused/Invitation.tsx#L16) |
| UI-00053 | "status" | Opening your workspace… | Parent/source state | [web/app/components/HostGlobalNav.tsx:13](../../../web/app/components/HostGlobalNav.tsx#L13) |
| UI-00070 | "status" | Opening… | Parent/source state | [web/app/e/[token]/page.tsx:8](../../../web/app/e/[token]/page.tsx#L8) |
| UI-00073 | "alert" | Contribution progress is unavailable. | progressState==='error' | [web/app/e/[token]/page.tsx:62](../../../web/app/e/[token]/page.tsx#L62) |
| UI-00411 | "alert" | {error} | error; groupBase&&error.includes('switched off') | [web/components/CommercePurchase.tsx:27](../../../web/components/CommercePurchase.tsx#L27) |
| UI-00413 | "status" | {notice} | notice | [web/components/CommercePurchase.tsx:27](../../../web/components/CommercePurchase.tsx#L27) |
| UI-00444 | "status" | Opening your celebration space… | ready | [web/components/ExperienceProvider.tsx:13](../../../web/components/ExperienceProvider.tsx#L13) |
| UI-00448 | "alert" | {error} | error | [web/components/GroupActivity.tsx:7](../../../web/components/GroupActivity.tsx#L7) |
| UI-00457 | "status" | {accountNotice} | logged&&accountNotice; open | [web/components/GroupContribution.tsx:81](../../../web/components/GroupContribution.tsx#L81) |
| UI-00458 | "alert" | {error} | error; open | [web/components/GroupContribution.tsx:81](../../../web/components/GroupContribution.tsx#L81) |
| UI-00484 | "status" | {pinNotice} | pinNotice; allowPin; group; !logged; loading; open | [web/components/GroupContribution.tsx:81](../../../web/components/GroupContribution.tsx#L81) |
| UI-00531 | "status" | ✓ Email verified for account recovery | Parent/source state | [web/components/GroupEmailVerification.tsx:14](../../../web/components/GroupEmailVerification.tsx#L14) |
| UI-00538 | "status" | {notice} | notice | [web/components/GroupEmailVerification.tsx:15](../../../web/components/GroupEmailVerification.tsx#L15) |
| UI-00545 | "status" | {notice} | notice | [web/components/GroupGiftPlan.tsx:7](../../../web/components/GroupGiftPlan.tsx#L7) |
| UI-00546 | "alert" | {error} | error | [web/components/GroupGuestCheckout.tsx:21](../../../web/components/GroupGuestCheckout.tsx#L21) |
| UI-00547 | "status" |  | stage==='thanks'; !allowed; !invite | [web/components/GroupGuestCheckout.tsx:22](../../../web/components/GroupGuestCheckout.tsx#L22) |
| UI-00568 | "status" | {notice} | notice | [web/components/GroupGuestCreatorSettings.tsx:12](../../../web/components/GroupGuestCreatorSettings.tsx#L12) |
| UI-00574 | "status" | {notice} | notice | [web/components/GuestActions.tsx:11](../../../web/components/GuestActions.tsx#L11) |
| UI-00582 | "status" | {rsvpResult} | features.RSVP | [web/components/GuestCelebration.tsx:29](../../../web/components/GuestCelebration.tsx#L29) |
| UI-00589 | "status" | {wishResult} | features.GUESTBOOK | [web/components/GuestCelebration.tsx:32](../../../web/components/GuestCelebration.tsx#L32) |
| UI-00592 | "alert" | {pricingError} | pricingError; paymentMethod==='razorpay'; paymentMethod==='razorpay'&&!razorpayAllowed | [web/components/GuestContributionCheckout.tsx:108](../../../web/components/GuestContributionCheckout.tsx#L108) |
| UI-00594 | "status" | Loading payment methods… | destinationsLoading | [web/components/GuestContributionCheckout.tsx:130](../../../web/components/GuestContributionCheckout.tsx#L130) |
| UI-00595 | "alert" | {destinationsError} | destinationsError | [web/components/GuestContributionCheckout.tsx:130](../../../web/components/GuestContributionCheckout.tsx#L130) |
| UI-00598 | "status" |  | directStatus | [web/components/GuestContributionCheckout.tsx:133](../../../web/components/GuestContributionCheckout.tsx#L133) |
| UI-00600 | "status" |  | receipt; directStatus | [web/components/GuestContributionCheckout.tsx:140](../../../web/components/GuestContributionCheckout.tsx#L140) |
| UI-00603 | "status" | Loading UPI and bank details… | destinationsLoading; receipt; directStatus | [web/components/GuestContributionCheckout.tsx:142](../../../web/components/GuestContributionCheckout.tsx#L142) |
| UI-00604 | "alert" | {destinationsError} | destinationsError; receipt; directStatus | [web/components/GuestContributionCheckout.tsx:143](../../../web/components/GuestContributionCheckout.tsx#L143) |
| UI-00615 | "status" | {directStatus} | directStatus; paymentMethod!=='razorpay'; receipt | [web/components/GuestContributionCheckout.tsx:164](../../../web/components/GuestContributionCheckout.tsx#L164) |
| UI-00617 | "alert" | {error} | error; receipt; directStatus | [web/components/GuestContributionCheckout.tsx:171](../../../web/components/GuestContributionCheckout.tsx#L171) |
| UI-00620 | "status" | Loading guest gifts… | loading | [web/components/GuestGiftBoard.tsx:10](../../../web/components/GuestGiftBoard.tsx#L10) |
| UI-00621 | "alert" | {error} | error | [web/components/GuestGiftBoard.tsx:10](../../../web/components/GuestGiftBoard.tsx#L10) |
| UI-00624 | "status" | {g.display_status\|\|g.selection_label} | Parent/source state | [web/components/GuestGiftBoard.tsx:10](../../../web/components/GuestGiftBoard.tsx#L10) |
| UI-00626 | "status" | {g.display_status} | Parent/source state | [web/components/GuestGiftBoard.tsx:10](../../../web/components/GuestGiftBoard.tsx#L10) |
| UI-00634 | "alert" | {error} | error | [web/components/GuestGiftManager.tsx:15](../../../web/components/GuestGiftManager.tsx#L15) |
| UI-00635 | "status" | {notice} | notice | [web/components/GuestGiftManager.tsx:15](../../../web/components/GuestGiftManager.tsx#L15) |
| UI-00636 | "status" | Loading gifts… | loading | [web/components/GuestGiftManager.tsx:15](../../../web/components/GuestGiftManager.tsx#L15) |
| UI-00641 | "status" | Checking eligible payment destinations… | loading | [web/components/GuestVerifiedDestination.tsx:34](../../../web/components/GuestVerifiedDestination.tsx#L34) |
| UI-00642 | "alert" | {error} | error; loading | [web/components/GuestVerifiedDestination.tsx:35](../../../web/components/GuestVerifiedDestination.tsx#L35) |
| UI-00644 | "status" | {method==='upi'? 'No eligible preferred UPI QR is currently available. Please do not make a transfer yet.': 'No eligible preferred bank account is currently available. Please do not make a transfer yet.'} | eligible.length===0; error; loading | [web/components/GuestVerifiedDestination.tsx:35](../../../web/components/GuestVerifiedDestination.tsx#L35) |
| UI-00649 | "status" | {copyNotice} | copyNotice; selected?.method==='UPI'&&selected.qrPngBase64&&focused; eligible.length===0; error; loading | [web/components/GuestVerifiedDestination.tsx:44](../../../web/components/GuestVerifiedDestination.tsx#L44) |
| UI-00666 | "alert" | {error} | error | [web/components/HostWorkspace.tsx:46](../../../web/components/HostWorkspace.tsx#L46) |
| UI-00675 | "status" | {notice} | notice | [web/components/IndependentGroupPlan.tsx:17](../../../web/components/IndependentGroupPlan.tsx#L17) |
| UI-00678 | "status" | {notice} | notice | [web/components/IndependentGroupPlan.tsx:22](../../../web/components/IndependentGroupPlan.tsx#L22) |
| UI-00679 | "status" | Loading your contribution… | loading | [web/components/IndividualHostContribution.tsx:19](../../../web/components/IndividualHostContribution.tsx#L19) |
| UI-00680 | "status" |  | payment?.claim_id; loading | [web/components/IndividualHostContribution.tsx:19](../../../web/components/IndividualHostContribution.tsx#L19) |
| UI-00687 | "alert" | {error} | error | [web/components/IndividualHostContribution.tsx:23](../../../web/components/IndividualHostContribution.tsx#L23) |
| UI-00746 | "status" | Mobile number verified | verified | [web/components/MobileVerification.tsx:13](../../../web/components/MobileVerification.tsx#L13) |
| UI-00753 | "status" | {message} | message | [web/components/MobileVerification.tsx:13](../../../web/components/MobileVerification.tsx#L13) |
| UI-00808 | "status" | {notice} | notice | [web/components/SocialSharing.tsx:13](../../../web/components/SocialSharing.tsx#L13) |
| UI-00816 | "status" | {notice\|\|`Manage ${saved.name}`} | saved; step==='choice'; (step==='choice'\|\|step==='individual') | [web/components/SpecialGiftEntry.tsx:69](../../../web/components/SpecialGiftEntry.tsx#L69) |
| UI-00840 | "alert" | {error} | error; (step==='choice'\|\|step==='individual') | [web/components/SpecialGiftEntry.tsx:78](../../../web/components/SpecialGiftEntry.tsx#L78) |
| UI-00842 | "status" | {p.receipt?<><p>Gift allocation: {inr(p.receipt.giftPaise)}</p><p>Celebration allocation: {inr(p.receipt.walletPaise)}</p><p className="finePrint">Payment ID: {p.receipt.paymentId}</p></>:<><p>{inr(amount)} · {p.method==='upi'?'UPI':'Bank transfer'}</p><p className="finePrint">Submission reference: {p.reported.split('Reference: ')[1]\|\|'See the host for an update'}</p></>} {p.close&&<button className="focusPrimary" onClick={p.close}>Back to the celebration</button>} | p.receipt\|\|p.reported | [web/components/focused/Checkout.tsx:7](../../../web/components/focused/Checkout.tsx#L7) |
| UI-00858 | "alert" | {p.error} | p.error; p.receipt\|\|p.reported | [web/components/focused/Checkout.tsx:7](../../../web/components/focused/Checkout.tsx#L7) |
| UI-00897 | "status" | Progress is temporarily unavailable. | progressState==='error' | [web/components/focused/Invitation.tsx:26](../../../web/components/focused/Invitation.tsx#L26) |


### Workflow state bindings

| Source | Binding | Initial expression |
| --- | --- | --- |
| web/app/e/[token]/page.tsx | walletOpen | false |
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
| Header | Shared identity/event or page title/back/actions, where declared. Composite header declarations: 2. |
| Navigation / sidebar | Host/admin navigation and event context where applicable; mobile/focused variants are conditional. nav=2, aside=1. |
| Main content | Purpose-specific forms/cards/tables/canvas/player from the composed sources. main=4, section=5, article=2. |
| Search and filters | Fields/selects listed below; exact value/handler/visibility controls are not inferred from placeholder labels. |
| Data grids and cards | Table declarations=0. Data collections and generated rows/cards are in frontend.json dynamicSources. |
| Popups / dialogs | Named/source-declared dialog control records below; opening/closing/loading/apply/cancel actions belong to those component handlers. |
| Alerts / notifications | Source controls with role alert/status are listed below. Notice strings may come from API results; do not assume a universal envelope. |
| Footer | Local/shared footer declarations=0. No undocumented legal/footer wording is invented. |
| Loading / empty / success / error | Observed state bindings and relevant action fields are listed below. Expected QA states from Document 2 do not establish a spinner/retry control where source does not implement one. |


### Composed implementation

- [web/app/events/page.tsx:1](../../../web/app/events/page.tsx#L1)
- [web/components/DeliveryAddressFields.tsx:1](../../../web/components/DeliveryAddressFields.tsx#L1)
- [web/components/ExperienceProvider.tsx:1](../../../web/components/ExperienceProvider.tsx#L1)
- [web/components/focused/EventList.tsx:1](../../../web/components/focused/EventList.tsx#L1)
- [web/app/components/HostToast.tsx:1](../../../web/app/components/HostToast.tsx#L1)
- [web/components/VerificationProvider.tsx:1](../../../web/components/VerificationProvider.tsx#L1)
- [web/app/layout.tsx:1](../../../web/app/layout.tsx#L1)
- [web/app/components/PerformanceDiagnostics.tsx:1](../../../web/app/components/PerformanceDiagnostics.tsx#L1)
- [web/app/components/HostSessionRedirect.tsx:1](../../../web/app/components/HostSessionRedirect.tsx#L1)
- [web/app/components/HostGlobalNav.tsx:1](../../../web/app/components/HostGlobalNav.tsx#L1)
- [web/components/HostWorkspace.tsx:1](../../../web/components/HostWorkspace.tsx#L1)
- [web/components/focused/HostShell.tsx:1](../../../web/components/focused/HostShell.tsx#L1)

### Fields

| Control ID | Label / expression | Type | Binding | Required | Native limits / format | Detailed specification |
| --- | --- | --- | --- | --- | --- | --- |
| UI-00171 | What should we call you? | input  | {nameDraft} | true | minLength={2}; maxLength={120} | [control](UI_CONTROLS.md#ui-00171) |
| UI-00176 | Event name | input  | {title} | true | maxLength={200} | [control](UI_CONTROLS.md#ui-00176) |
| UI-00177 | Occasion | select  | {occasionType} | Not declared |  | [control](UI_CONTROLS.md#ui-00177) |
| UI-00178 | Date (optional) | input "date" | {eventDate} | Not declared |  | [control](UI_CONTROLS.md#ui-00178) |
| UI-00179 | About this event | textarea  | {description} | Not declared | maxLength={10000} | [control](UI_CONTROLS.md#ui-00179) |
| UI-00180 | Street address * | input  | {addressLine1} | true | maxLength={200} | [control](UI_CONTROLS.md#ui-00180) |
| UI-00181 | City * | input  | {city} | true | maxLength={120} | [control](UI_CONTROLS.md#ui-00181) |
| UI-00182 | State * | input  | {state} | true | maxLength={120} | [control](UI_CONTROLS.md#ui-00182) |
| UI-00183 | PIN / postal code * | input  | {postalCode} | true | maxLength={20} | [control](UI_CONTROLS.md#ui-00183) |
| UI-00184 | Country * | input  | {country} | true | maxLength={80} | [control](UI_CONTROLS.md#ui-00184) |
| UI-00185 | Host name (shown to guests) * | input  | {hostName} | true | maxLength={200} | [control](UI_CONTROLS.md#ui-00185) |
| UI-00186 | Contact name * | input  | {contactName} | true | maxLength={150} | [control](UI_CONTROLS.md#ui-00186) |
| UI-00187 | Contact phone * | input "tel" | {contactPhone} | true | maxLength={30} | [control](UI_CONTROLS.md#ui-00187) |
| UI-00188 | Contact email (optional) | input "email" | {contactEmail} | Not declared | maxLength={254} | [control](UI_CONTROLS.md#ui-00188) |
| UI-00442 | Same as event address | input "checkbox" | {same} | Not declared |  | [control](UI_CONTROLS.md#ui-00442) |
| UI-00443 | {label} | input  | {address[key]} | true | maxLength={maxLength} | [control](UI_CONTROLS.md#ui-00443) |


### Buttons

| ID | Label / expression | Action | Submit validation / handler | Source |
| --- | --- | --- | --- | --- |
| UI-00059 | × | {()=>{setFlash(null);onDismiss?.();}} | Not a native form submit or inherited from component context | [web/app/components/HostToast.tsx:12](../../../web/app/components/HostToast.tsx#L12) |
| UI-00172 | Save name | submit / component action | {saveName} | [web/app/events/page.tsx:66](../../../web/app/events/page.tsx#L66) |
| UI-00189 | {busy?'Creating…':'Create event →'} | submit / component action | {create} | [web/app/events/page.tsx:66](../../../web/app/events/page.tsx#L66) |
| UI-00190 | Refresh | {()=>void reload()} | Not a native form submit or inherited from component context | [web/app/events/page.tsx:66](../../../web/app/events/page.tsx#L66) |
| UI-00198 | {busy ? 'Publishing…' : '✨ Publish event'} | {() => {<br>      if (<br>        window.confirm(<br>          `Publish "${e.title}"? Guests will be able to view this celebration.`<br>        )<br>      ) {<br>        void publishEvent(e.id);<br>      }<br>    }} | Not a native form submit or inherited from component context | [web/app/events/page.tsx:93](../../../web/app/events/page.tsx#L93) |
| UI-00201 | 🔒 Unpublish event | {() => {<br>      if (<br>        window.confirm(<br>          `Unpublish "${e.title}"?\n\nGuests will temporarily lose access to the invitation. You can publish it again later.`<br>        )<br>      ) {<br>        void unpublishEvent(e.id);<br>      }<br>    }} | Not a native form submit or inherited from component context | [web/app/events/page.tsx:122](../../../web/app/events/page.tsx#L122) |
| UI-00664 | {collapsed?<PanelLeftOpen size={20}/>:<PanelLeftClose size={20}/>} | {toggle} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:43](../../../web/components/HostWorkspace.tsx#L43) |
| UI-00667 |  | {()=>void signOut()} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:46](../../../web/components/HostWorkspace.tsx#L46) |
| UI-00668 |  | {()=>setCollapsed(true)} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:47](../../../web/components/HostWorkspace.tsx#L47) |
| UI-00868 | Retry | {()=>location.reload()} | Not a native form submit or inherited from component context | [web/components/focused/EventList.tsx:12](../../../web/components/focused/EventList.tsx#L12) |


### Dialog and alert/status inventory

| ID | Role / kind | Text / label | Visibility conditions | Source |
| --- | --- | --- | --- | --- |
| UI-00199 | native-dialog | `Publish "${e.title}"? Guests will be able to view this celebration.` | e.status === 'DRAFT'; events.length===0; loading | [web/app/events/page.tsx:100](../../../web/app/events/page.tsx#L100) |
| UI-00202 | native-dialog | `Unpublish "${e.title}"?\n\nGuests will temporarily lose access to the invitation. You can publish it again later.` | e.status === 'PUBLISHED'; events.length===0; loading | [web/app/events/page.tsx:128](../../../web/app/events/page.tsx#L128) |
| UI-00053 | "status" | Opening your workspace… | Parent/source state | [web/app/components/HostGlobalNav.tsx:13](../../../web/app/components/HostGlobalNav.tsx#L13) |
| UI-00173 | "alert" | {error} {error==='Please sign in'&&<Link href="/login">Go to login</Link>} | error | [web/app/events/page.tsx:66](../../../web/app/events/page.tsx#L66) |
| UI-00444 | "status" | Opening your celebration space… | ready | [web/components/ExperienceProvider.tsx:13](../../../web/components/ExperienceProvider.tsx#L13) |
| UI-00666 | "alert" | {error} | error | [web/components/HostWorkspace.tsx:46](../../../web/components/HostWorkspace.tsx#L46) |
| UI-00867 | "alert" | {error} | error | [web/components/focused/EventList.tsx:12](../../../web/components/focused/EventList.tsx#L12) |
| UI-00869 | "status" | Loading celebrations… | loading | [web/components/focused/EventList.tsx:12](../../../web/components/focused/EventList.tsx#L12) |


### Workflow state bindings

| Source | Binding | Initial expression |
| --- | --- | --- |
| web/app/events/page.tsx | loading | true |
| web/app/events/page.tsx | busy | false |
| web/app/events/page.tsx | error | '' |
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
| Header | Shared identity/event or page title/back/actions, where declared. Composite header declarations: 1. |
| Navigation / sidebar | Host/admin navigation and event context where applicable; mobile/focused variants are conditional. nav=2, aside=1. |
| Main content | Purpose-specific forms/cards/tables/canvas/player from the composed sources. main=5, section=7, article=0. |
| Search and filters | Fields/selects listed below; exact value/handler/visibility controls are not inferred from placeholder labels. |
| Data grids and cards | Table declarations=0. Data collections and generated rows/cards are in frontend.json dynamicSources. |
| Popups / dialogs | Named/source-declared dialog control records below; opening/closing/loading/apply/cancel actions belong to those component handlers. |
| Alerts / notifications | Source controls with role alert/status are listed below. Notice strings may come from API results; do not assume a universal envelope. |
| Footer | Local/shared footer declarations=0. No undocumented legal/footer wording is invented. |
| Loading / empty / success / error | Observed state bindings and relevant action fields are listed below. Expected QA states from Document 2 do not establish a spinner/retry control where source does not implement one. |


### Composed implementation

- [web/app/events/[id]/page.tsx:1](../../../web/app/events/[id]/page.tsx#L1)
- [web/components/focused/EventOverview.tsx:1](../../../web/components/focused/EventOverview.tsx#L1)
- [web/components/VerificationProvider.tsx:1](../../../web/components/VerificationProvider.tsx#L1)
- [web/app/components/HostToast.tsx:1](../../../web/app/components/HostToast.tsx#L1)
- [web/components/DeliveryAddressFields.tsx:1](../../../web/components/DeliveryAddressFields.tsx#L1)
- [web/components/focused/FocusedOnly.tsx:1](../../../web/components/focused/FocusedOnly.tsx#L1)
- [web/components/ExperienceProvider.tsx:1](../../../web/components/ExperienceProvider.tsx#L1)
- [web/app/layout.tsx:1](../../../web/app/layout.tsx#L1)
- [web/app/components/PerformanceDiagnostics.tsx:1](../../../web/app/components/PerformanceDiagnostics.tsx#L1)
- [web/app/components/HostSessionRedirect.tsx:1](../../../web/app/components/HostSessionRedirect.tsx#L1)
- [web/app/components/HostGlobalNav.tsx:1](../../../web/app/components/HostGlobalNav.tsx#L1)
- [web/components/HostWorkspace.tsx:1](../../../web/components/HostWorkspace.tsx#L1)
- [web/components/focused/HostShell.tsx:1](../../../web/components/focused/HostShell.tsx#L1)

### Fields

| Control ID | Label / expression | Type | Binding | Required | Native limits / format | Detailed specification |
| --- | --- | --- | --- | --- | --- | --- |
| UI-00442 | Same as event address | input "checkbox" | {same} | Not declared |  | [control](UI_CONTROLS.md#ui-00442) |
| UI-00443 | {label} | input  | {address[key]} | true | maxLength={maxLength} | [control](UI_CONTROLS.md#ui-00443) |
| UI-00883 | "Invitation link" | input  | {invitation} | Not declared |  | [control](UI_CONTROLS.md#ui-00883) |


### Buttons

| ID | Label / expression | Action | Submit validation / handler | Source |
| --- | --- | --- | --- | --- |
| UI-00059 | × | {()=>{setFlash(null);onDismiss?.();}} | Not a native form submit or inherited from component context | [web/app/components/HostToast.tsx:12](../../../web/app/components/HostToast.tsx#L12) |
| UI-00664 | {collapsed?<PanelLeftOpen size={20}/>:<PanelLeftClose size={20}/>} | {toggle} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:43](../../../web/components/HostWorkspace.tsx#L43) |
| UI-00667 |  | {()=>void signOut()} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:46](../../../web/components/HostWorkspace.tsx#L46) |
| UI-00668 |  | {()=>setCollapsed(true)} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:47](../../../web/components/HostWorkspace.tsx#L47) |
| UI-00872 | Share invitation | {()=>setSharing(true)} | Not a native form submit or inherited from component context | [web/components/focused/EventOverview.tsx:17](../../../web/components/focused/EventOverview.tsx#L17) |
| UI-00878 | {busy?'Publishing…':'Publish invitation'} | {()=>void publish()} | Not a native form submit or inherited from component context | [web/components/focused/EventOverview.tsx:17](../../../web/components/focused/EventOverview.tsx#L17) |
| UI-00879 | Make invitation private | {async()=>{if(!confirm('Make this invitation private? Existing guest links will temporarily stop working.'))return;setBusy(true);try{setEvent(await hostApi.unpublishEvent(fetch,event.id));setSharing(false)}catch{setError('Could not make this invitation private. Try again.')}finally{setBusy(false)}}} | Not a native form submit or inherited from component context | [web/components/focused/EventOverview.tsx:17](../../../web/components/focused/EventOverview.tsx#L17) |
| UI-00882 | ✕ | {()=>setSharing(false)} | Not a native form submit or inherited from component context | [web/components/focused/EventOverview.tsx:17](../../../web/components/focused/EventOverview.tsx#L17) |
| UI-00884 | Copy link | {()=>navigator.clipboard.writeText(invitation).then(()=>setMessage('Invitation link copied.')).catch(()=>setMessage('Select and copy the link above.'))} | Not a native form submit or inherited from component context | [web/components/focused/EventOverview.tsx:17](../../../web/components/focused/EventOverview.tsx#L17) |


### Dialog and alert/status inventory

| ID | Role / kind | Text / label | Visibility conditions | Source |
| --- | --- | --- | --- | --- |
| UI-00880 | native-dialog | 'Make this invitation private? Existing guest links will temporarily stop working.' | published | [web/components/focused/EventOverview.tsx:17](../../../web/components/focused/EventOverview.tsx#L17) |
| UI-00053 | "status" | Opening your workspace… | Parent/source state | [web/app/components/HostGlobalNav.tsx:13](../../../web/app/components/HostGlobalNav.tsx#L13) |
| UI-00444 | "status" | Opening your celebration space… | ready | [web/components/ExperienceProvider.tsx:13](../../../web/components/ExperienceProvider.tsx#L13) |
| UI-00666 | "alert" | {error} | error | [web/components/HostWorkspace.tsx:46](../../../web/components/HostWorkspace.tsx#L46) |
| UI-00871 | "alert" | {error} | error | [web/components/focused/EventOverview.tsx:17](../../../web/components/focused/EventOverview.tsx#L17) |
| UI-00881 | "status" | {message} | message | [web/components/focused/EventOverview.tsx:17](../../../web/components/focused/EventOverview.tsx#L17) |
| UI-00893 | "status" | Opening your celebrations… | mode==='FOCUSED' | [web/components/focused/FocusedOnly.tsx:5](../../../web/components/focused/FocusedOnly.tsx#L5) |


### Workflow state bindings

| Source | Binding | Initial expression |
| --- | --- | --- |
| web/components/focused/EventOverview.tsx | error | '' |
| web/components/focused/EventOverview.tsx | busy | false |
| web/components/focused/EventOverview.tsx | message | '' |
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
| Header | Shared identity/event or page title/back/actions, where declared. Composite header declarations: 2. |
| Navigation / sidebar | Host/admin navigation and event context where applicable; mobile/focused variants are conditional. nav=2, aside=1. |
| Main content | Purpose-specific forms/cards/tables/canvas/player from the composed sources. main=3, section=3, article=0. |
| Search and filters | Fields/selects listed below; exact value/handler/visibility controls are not inferred from placeholder labels. |
| Data grids and cards | Table declarations=0. Data collections and generated rows/cards are in frontend.json dynamicSources. |
| Popups / dialogs | Named/source-declared dialog control records below; opening/closing/loading/apply/cancel actions belong to those component handlers. |
| Alerts / notifications | Source controls with role alert/status are listed below. Notice strings may come from API results; do not assume a universal envelope. |
| Footer | Local/shared footer declarations=1. No undocumented legal/footer wording is invented. |
| Loading / empty / success / error | Observed state bindings and relevant action fields are listed below. Expected QA states from Document 2 do not establish a spinner/retry control where source does not implement one. |


### Composed implementation

- [web/app/events/[id]/album/page.tsx:1](../../../web/app/events/[id]/album/page.tsx#L1)
- [web/components/MemoryAlbum.tsx:1](../../../web/components/MemoryAlbum.tsx#L1)
- [web/app/layout.tsx:1](../../../web/app/layout.tsx#L1)
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
| UI-00734 | Include hidden wishes | input "checkbox" | {hidden} | Not declared |  | [control](UI_CONTROLS.md#ui-00734) |
| UI-00742 | Take your time | select  | {seconds} | Not declared |  | [control](UI_CONTROLS.md#ui-00742) |


### Buttons

| ID | Label / expression | Action | Submit validation / handler | Source |
| --- | --- | --- | --- | --- |
| UI-00664 | {collapsed?<PanelLeftOpen size={20}/>:<PanelLeftClose size={20}/>} | {toggle} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:43](../../../web/components/HostWorkspace.tsx#L43) |
| UI-00667 |  | {()=>void signOut()} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:46](../../../web/components/HostWorkspace.tsx#L46) |
| UI-00668 |  | {()=>setCollapsed(true)} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:47](../../../web/components/HostWorkspace.tsx#L47) |
| UI-00735 | Refresh | {()=>void refresh()} | Not a native form submit or inherited from component context | [web/components/MemoryAlbum.tsx:51](../../../web/components/MemoryAlbum.tsx#L51) |
| UI-00739 |  | {()=>{setPlaying(false);void advance(-1)}} | Not a native form submit or inherited from component context | [web/components/MemoryAlbum.tsx:60](../../../web/components/MemoryAlbum.tsx#L60) |
| UI-00740 | {playing?<Pause size={18}/>:<Play size={18}/>} {playing?'Pause the moment':'Play our memories'} | {()=>setPlaying(value=>!value)} | Not a native form submit or inherited from component context | [web/components/MemoryAlbum.tsx:60](../../../web/components/MemoryAlbum.tsx#L60) |
| UI-00741 |  | {()=>{setPlaying(false);void advance(1)}} | Not a native form submit or inherited from component context | [web/components/MemoryAlbum.tsx:60](../../../web/components/MemoryAlbum.tsx#L60) |
| UI-00743 | {full?<Minimize size={18}/>:<Maximize size={18}/>} | {()=>void fullscreen()} | Not a native form submit or inherited from component context | [web/components/MemoryAlbum.tsx:60](../../../web/components/MemoryAlbum.tsx#L60) |
| UI-00744 | {memory.photoAvailable?<img loading="lazy" src={photo(memory)} alt="" onError={e=>{e.currentTarget.style.visibility='hidden'}}/>:<span className="ma-thumb-quote" aria-hidden="true">“</span>} | {()=>{setPlaying(false);setIndex(i)}} | Not a native form submit or inherited from component context | [web/components/MemoryAlbum.tsx:62](../../../web/components/MemoryAlbum.tsx#L62) |
| UI-00745 | {more?'Gathering more memories…':'Unfold more memories'} | {()=>void loadMore()} | Not a native form submit or inherited from component context | [web/components/MemoryAlbum.tsx:63](../../../web/components/MemoryAlbum.tsx#L63) |


### Dialog and alert/status inventory

| ID | Role / kind | Text / label | Visibility conditions | Source |
| --- | --- | --- | --- | --- |
| UI-00053 | "status" | Opening your workspace… | Parent/source state | [web/app/components/HostGlobalNav.tsx:13](../../../web/app/components/HostGlobalNav.tsx#L13) |
| UI-00444 | "status" | Opening your celebration space… | ready | [web/components/ExperienceProvider.tsx:13](../../../web/components/ExperienceProvider.tsx#L13) |
| UI-00666 | "alert" | {error} | error | [web/components/HostWorkspace.tsx:46](../../../web/components/HostWorkspace.tsx#L46) |
| UI-00736 | "alert" | {error} | error | [web/components/MemoryAlbum.tsx:52](../../../web/components/MemoryAlbum.tsx#L52) |
| UI-00737 | "status" |  | loading | [web/components/MemoryAlbum.tsx:53](../../../web/components/MemoryAlbum.tsx#L53) |


### Workflow state bindings

| Source | Binding | Initial expression |
| --- | --- | --- |
| web/components/MemoryAlbum.tsx | loading | true |
| web/components/MemoryAlbum.tsx | error | '' |
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
| Header | Shared identity/event or page title/back/actions, where declared. Composite header declarations: 2. |
| Navigation / sidebar | Host/admin navigation and event context where applicable; mobile/focused variants are conditional. nav=2, aside=1. |
| Main content | Purpose-specific forms/cards/tables/canvas/player from the composed sources. main=3, section=4, article=2. |
| Search and filters | Fields/selects listed below; exact value/handler/visibility controls are not inferred from placeholder labels. |
| Data grids and cards | Table declarations=1. Data collections and generated rows/cards are in frontend.json dynamicSources. |
| Popups / dialogs | Named/source-declared dialog control records below; opening/closing/loading/apply/cancel actions belong to those component handlers. |
| Alerts / notifications | Source controls with role alert/status are listed below. Notice strings may come from API results; do not assume a universal envelope. |
| Footer | Local/shared footer declarations=0. No undocumented legal/footer wording is invented. |
| Loading / empty / success / error | Observed state bindings and relevant action fields are listed below. Expected QA states from Document 2 do not establish a spinner/retry control where source does not implement one. |


### Composed implementation

- [web/app/events/[id]/celebration/page.tsx:1](../../../web/app/events/[id]/celebration/page.tsx#L1)
- [web/components/ExperienceProvider.tsx:1](../../../web/components/ExperienceProvider.tsx#L1)
- [web/app/layout.tsx:1](../../../web/app/layout.tsx#L1)
- [web/components/VerificationProvider.tsx:1](../../../web/components/VerificationProvider.tsx#L1)
- [web/app/components/PerformanceDiagnostics.tsx:1](../../../web/app/components/PerformanceDiagnostics.tsx#L1)
- [web/app/components/HostSessionRedirect.tsx:1](../../../web/app/components/HostSessionRedirect.tsx#L1)
- [web/app/components/HostGlobalNav.tsx:1](../../../web/app/components/HostGlobalNav.tsx#L1)
- [web/components/HostWorkspace.tsx:1](../../../web/components/HostWorkspace.tsx#L1)
- [web/components/focused/HostShell.tsx:1](../../../web/components/focused/HostShell.tsx#L1)

### Fields

| Control ID | Label / expression | Type | Binding | Required | Native limits / format | Detailed specification |
| --- | --- | --- | --- | --- | --- | --- |
| UI-00085 | "ch-rsvp-filter" | select  | {filter} | Not declared |  | [control](UI_CONTROLS.md#ui-00085) |


### Buttons

| ID | Label / expression | Action | Submit validation / handler | Source |
| --- | --- | --- | --- | --- |
| UI-00081 | ↻ Refresh responses | {()=>void reload()} | Not a native form submit or inherited from component context | [web/app/events/[id]/celebration/page.tsx:30](../../../web/app/events/[id]/celebration/page.tsx#L30) |
| UI-00083 | Responses | {()=>setTab('responses')} | Not a native form submit or inherited from component context | [web/app/events/[id]/celebration/page.tsx:31](../../../web/app/events/[id]/celebration/page.tsx#L31) |
| UI-00084 | Wishes | {()=>setTab('wishes')} | Not a native form submit or inherited from component context | [web/app/events/[id]/celebration/page.tsx:31](../../../web/app/events/[id]/celebration/page.tsx#L31) |
| UI-00087 | {w.status==='APPROVED'?'✓ Approved':'✓ Approve & publish'} | {()=>void moderate(w.id,'APPROVED')} | Not a native form submit or inherited from component context | [web/app/events/[id]/celebration/page.tsx:34](../../../web/app/events/[id]/celebration/page.tsx#L34) |
| UI-00088 | {w.status==='HIDDEN'?'Hidden':'Hide message'} | {()=>void moderate(w.id,'HIDDEN')} | Not a native form submit or inherited from component context | [web/app/events/[id]/celebration/page.tsx:34](../../../web/app/events/[id]/celebration/page.tsx#L34) |
| UI-00664 | {collapsed?<PanelLeftOpen size={20}/>:<PanelLeftClose size={20}/>} | {toggle} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:43](../../../web/components/HostWorkspace.tsx#L43) |
| UI-00667 |  | {()=>void signOut()} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:46](../../../web/components/HostWorkspace.tsx#L46) |
| UI-00668 |  | {()=>setCollapsed(true)} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:47](../../../web/components/HostWorkspace.tsx#L47) |


### Dialog and alert/status inventory

| ID | Role / kind | Text / label | Visibility conditions | Source |
| --- | --- | --- | --- | --- |
| UI-00053 | "status" | Opening your workspace… | Parent/source state | [web/app/components/HostGlobalNav.tsx:13](../../../web/app/components/HostGlobalNav.tsx#L13) |
| UI-00082 | "alert" | {error} | error | [web/app/events/[id]/celebration/page.tsx:30](../../../web/app/events/[id]/celebration/page.tsx#L30) |
| UI-00444 | "status" | Opening your celebration space… | ready | [web/components/ExperienceProvider.tsx:13](../../../web/components/ExperienceProvider.tsx#L13) |
| UI-00666 | "alert" | {error} | error | [web/components/HostWorkspace.tsx:46](../../../web/components/HostWorkspace.tsx#L46) |


### Workflow state bindings

| Source | Binding | Initial expression |
| --- | --- | --- |
| web/app/events/[id]/celebration/page.tsx | tab | 'responses' |
| web/app/events/[id]/celebration/page.tsx | error | '' |
| web/app/events/[id]/celebration/page.tsx | loading | true |
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
| Header | Shared identity/event or page title/back/actions, where declared. Composite header declarations: 1. |
| Navigation / sidebar | Host/admin navigation and event context where applicable; mobile/focused variants are conditional. nav=3, aside=1. |
| Main content | Purpose-specific forms/cards/tables/canvas/player from the composed sources. main=3, section=4, article=2. |
| Search and filters | Fields/selects listed below; exact value/handler/visibility controls are not inferred from placeholder labels. |
| Data grids and cards | Table declarations=1. Data collections and generated rows/cards are in frontend.json dynamicSources. |
| Popups / dialogs | Named/source-declared dialog control records below; opening/closing/loading/apply/cancel actions belong to those component handlers. |
| Alerts / notifications | Source controls with role alert/status are listed below. Notice strings may come from API results; do not assume a universal envelope. |
| Footer | Local/shared footer declarations=0. No undocumented legal/footer wording is invented. |
| Loading / empty / success / error | Observed state bindings and relevant action fields are listed below. Expected QA states from Document 2 do not establish a spinner/retry control where source does not implement one. |


### Composed implementation

- [web/app/events/[id]/contributions/page.tsx:1](../../../web/app/events/[id]/contributions/page.tsx#L1)
- [web/components/GroupCollectionReport.tsx:1](../../../web/components/GroupCollectionReport.tsx#L1)
- [web/components/GroupActivity.tsx:1](../../../web/components/GroupActivity.tsx#L1)
- [web/components/PublishedGuestGifts.tsx:1](../../../web/components/PublishedGuestGifts.tsx#L1)
- [web/components/ExperienceProvider.tsx:1](../../../web/components/ExperienceProvider.tsx#L1)
- [web/components/focused/ResponsiveTable.tsx:1](../../../web/components/focused/ResponsiveTable.tsx#L1)
- [web/app/layout.tsx:1](../../../web/app/layout.tsx#L1)
- [web/components/VerificationProvider.tsx:1](../../../web/components/VerificationProvider.tsx#L1)
- [web/app/components/PerformanceDiagnostics.tsx:1](../../../web/app/components/PerformanceDiagnostics.tsx#L1)
- [web/app/components/HostSessionRedirect.tsx:1](../../../web/app/components/HostSessionRedirect.tsx#L1)
- [web/app/components/HostGlobalNav.tsx:1](../../../web/app/components/HostGlobalNav.tsx#L1)
- [web/components/HostWorkspace.tsx:1](../../../web/components/HostWorkspace.tsx#L1)
- [web/components/focused/HostShell.tsx:1](../../../web/components/focused/HostShell.tsx#L1)

### Fields

| Control ID | Label / expression | Type | Binding | Required | Native limits / format | Detailed specification |
| --- | --- | --- | --- | --- | --- | --- |
| UI-00091 | Search contributions | input "search" | {search} | Not declared | maxLength={120} | [control](UI_CONTROLS.md#ui-00091) |
| UI-00093 | "Select gift" | select  | {giftId} | Not declared |  | [control](UI_CONTROLS.md#ui-00093) |
| UI-00094 | "Contribution" | select  | {type} | Not declared |  | [control](UI_CONTROLS.md#ui-00094) |
| UI-00095 | "Status" | select  | {status} | Not declared |  | [control](UI_CONTROLS.md#ui-00095) |
| UI-00096 | "From date UTC" | input "date" | {from} | Not declared | max={to\|\|undefined} | [control](UI_CONTROLS.md#ui-00096) |
| UI-00097 | "To date UTC" | input "date" | {to} | Not declared | min={from\|\|undefined} | [control](UI_CONTROLS.md#ui-00097) |
| UI-00103 | Rows per page | select  | {pageSize} | Not declared |  | [control](UI_CONTROLS.md#ui-00103) |


### Buttons

| ID | Label / expression | Action | Submit validation / handler | Source |
| --- | --- | --- | --- | --- |
| UI-00092 | Filter contributions | {()=>setFilterOpen(v=>!v)} | Not a native form submit or inherited from component context | [web/app/events/[id]/contributions/page.tsx:43](../../../web/app/events/[id]/contributions/page.tsx#L43) |
| UI-00098 | Refresh | {()=>void load()} | Not a native form submit or inherited from component context | [web/app/events/[id]/contributions/page.tsx:50](../../../web/app/events/[id]/contributions/page.tsx#L50) |
| UI-00099 | {exporting?'Exporting…':'Export CSV'} | {()=>void exportCsv()} | Not a native form submit or inherited from component context | [web/app/events/[id]/contributions/page.tsx:51](../../../web/app/events/[id]/contributions/page.tsx#L51) |
| UI-00105 | Previous | {()=>setOffset(Math.max(0,offset-pageSize))} | Not a native form submit or inherited from component context | [web/app/events/[id]/contributions/page.tsx:56](../../../web/app/events/[id]/contributions/page.tsx#L56) |
| UI-00106 | Next | {()=>setOffset(offset+pageSize)} | Not a native form submit or inherited from component context | [web/app/events/[id]/contributions/page.tsx:56](../../../web/app/events/[id]/contributions/page.tsx#L56) |
| UI-00449 | Newer activity | {()=>setOffset(value=>Math.max(0,value-100))} | Not a native form submit or inherited from component context | [web/components/GroupActivity.tsx:7](../../../web/components/GroupActivity.tsx#L7) |
| UI-00450 | Older activity | {()=>setOffset(value=>value+100)} | Not a native form submit or inherited from component context | [web/components/GroupActivity.tsx:7](../../../web/components/GroupActivity.tsx#L7) |
| UI-00452 | View contribution details | {async()=>{try{const response=await fetch(`/backend/api/v1/events/${eventId}/contributions/groups/${r.id}`,{credentials:'include',cache:'no-store'});if(!response.ok)throw Error('Could not load group details');const data=await response.json();setDetails(x=>({...x,[r.id]:data}))}catch(e){setError(e instanceof Error?e.message:'Could not load details')}}} | Not a native form submit or inherited from component context | [web/components/GroupCollectionReport.tsx:8](../../../web/components/GroupCollectionReport.tsx#L8) |
| UI-00664 | {collapsed?<PanelLeftOpen size={20}/>:<PanelLeftClose size={20}/>} | {toggle} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:43](../../../web/components/HostWorkspace.tsx#L43) |
| UI-00667 |  | {()=>void signOut()} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:46](../../../web/components/HostWorkspace.tsx#L46) |
| UI-00668 |  | {()=>setCollapsed(true)} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:47](../../../web/components/HostWorkspace.tsx#L47) |
| UI-00780 | Refresh gifts | {()=>void load()} | Not a native form submit or inherited from component context | [web/components/PublishedGuestGifts.tsx:8](../../../web/components/PublishedGuestGifts.tsx#L8) |


### Dialog and alert/status inventory

| ID | Role / kind | Text / label | Visibility conditions | Source |
| --- | --- | --- | --- | --- |
| UI-00053 | "status" | Opening your workspace… | Parent/source state | [web/app/components/HostGlobalNav.tsx:13](../../../web/app/components/HostGlobalNav.tsx#L13) |
| UI-00100 | "alert" | {error} {error.includes('(401)')&&<Link href="/login">Sign in again</Link>} | error | [web/app/events/[id]/contributions/page.tsx:52](../../../web/app/events/[id]/contributions/page.tsx#L52) |
| UI-00102 | "status" | Loading… | loading | [web/app/events/[id]/contributions/page.tsx:52](../../../web/app/events/[id]/contributions/page.tsx#L52) |
| UI-00104 | "status" | {report.matchingCount===0?'0 results':`${offset+1}–${Math.min(offset+report.entries.length,report.matchingCount)} of ${report.matchingCount}`} · Page {Math.floor(offset/pageSize)+1} of {Math.max(1,Math.ceil(report.matchingCount/pageSize))} | !loading; report | [web/app/events/[id]/contributions/page.tsx:56](../../../web/app/events/[id]/contributions/page.tsx#L56) |
| UI-00444 | "status" | Opening your celebration space… | ready | [web/components/ExperienceProvider.tsx:13](../../../web/components/ExperienceProvider.tsx#L13) |
| UI-00448 | "alert" | {error} | error | [web/components/GroupActivity.tsx:7](../../../web/components/GroupActivity.tsx#L7) |
| UI-00451 | "alert" | {error} | error | [web/components/GroupCollectionReport.tsx:8](../../../web/components/GroupCollectionReport.tsx#L8) |
| UI-00666 | "alert" | {error} | error | [web/components/HostWorkspace.tsx:46](../../../web/components/HostWorkspace.tsx#L46) |
| UI-00781 | "alert" | {error} | error | [web/components/PublishedGuestGifts.tsx:8](../../../web/components/PublishedGuestGifts.tsx#L8) |
| UI-00782 | "status" | Loading gifts… | loading | [web/components/PublishedGuestGifts.tsx:8](../../../web/components/PublishedGuestGifts.tsx#L8) |


### Workflow state bindings

| Source | Binding | Initial expression |
| --- | --- | --- |
| web/app/events/[id]/contributions/page.tsx | filterOpen | true |
| web/app/events/[id]/contributions/page.tsx | error | '' |
| web/app/events/[id]/contributions/page.tsx | loading | true |
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
| Header | Shared identity/event or page title/back/actions, where declared. Composite header declarations: 1. |
| Navigation / sidebar | Host/admin navigation and event context where applicable; mobile/focused variants are conditional. nav=2, aside=1. |
| Main content | Purpose-specific forms/cards/tables/canvas/player from the composed sources. main=3, section=2, article=1. |
| Search and filters | Fields/selects listed below; exact value/handler/visibility controls are not inferred from placeholder labels. |
| Data grids and cards | Table declarations=2. Data collections and generated rows/cards are in frontend.json dynamicSources. |
| Popups / dialogs | Named/source-declared dialog control records below; opening/closing/loading/apply/cancel actions belong to those component handlers. |
| Alerts / notifications | Source controls with role alert/status are listed below. Notice strings may come from API results; do not assume a universal envelope. |
| Footer | Local/shared footer declarations=0. No undocumented legal/footer wording is invented. |
| Loading / empty / success / error | Observed state bindings and relevant action fields are listed below. Expected QA states from Document 2 do not establish a spinner/retry control where source does not implement one. |


### Composed implementation

- [web/app/events/[id]/notifications/page.tsx:1](../../../web/app/events/[id]/notifications/page.tsx#L1)
- [web/components/GroupCollectionReport.tsx:1](../../../web/components/GroupCollectionReport.tsx#L1)
- [web/components/GroupActivity.tsx:1](../../../web/components/GroupActivity.tsx#L1)
- [web/components/ExperienceProvider.tsx:1](../../../web/components/ExperienceProvider.tsx#L1)
- [web/components/focused/ResponsiveTable.tsx:1](../../../web/components/focused/ResponsiveTable.tsx#L1)
- [web/app/layout.tsx:1](../../../web/app/layout.tsx#L1)
- [web/components/VerificationProvider.tsx:1](../../../web/components/VerificationProvider.tsx#L1)
- [web/app/components/PerformanceDiagnostics.tsx:1](../../../web/app/components/PerformanceDiagnostics.tsx#L1)
- [web/app/components/HostSessionRedirect.tsx:1](../../../web/app/components/HostSessionRedirect.tsx#L1)
- [web/app/components/HostGlobalNav.tsx:1](../../../web/app/components/HostGlobalNav.tsx#L1)
- [web/components/HostWorkspace.tsx:1](../../../web/components/HostWorkspace.tsx#L1)
- [web/components/focused/HostShell.tsx:1](../../../web/components/focused/HostShell.tsx#L1)

### Fields

| Control ID | Label / expression | Type | Binding | Required | Native limits / format | Detailed specification |
| --- | --- | --- | --- | --- | --- | --- |


### Buttons

| ID | Label / expression | Action | Submit validation / handler | Source |
| --- | --- | --- | --- | --- |
| UI-00449 | Newer activity | {()=>setOffset(value=>Math.max(0,value-100))} | Not a native form submit or inherited from component context | [web/components/GroupActivity.tsx:7](../../../web/components/GroupActivity.tsx#L7) |
| UI-00450 | Older activity | {()=>setOffset(value=>value+100)} | Not a native form submit or inherited from component context | [web/components/GroupActivity.tsx:7](../../../web/components/GroupActivity.tsx#L7) |
| UI-00452 | View contribution details | {async()=>{try{const response=await fetch(`/backend/api/v1/events/${eventId}/contributions/groups/${r.id}`,{credentials:'include',cache:'no-store'});if(!response.ok)throw Error('Could not load group details');const data=await response.json();setDetails(x=>({...x,[r.id]:data}))}catch(e){setError(e instanceof Error?e.message:'Could not load details')}}} | Not a native form submit or inherited from component context | [web/components/GroupCollectionReport.tsx:8](../../../web/components/GroupCollectionReport.tsx#L8) |
| UI-00664 | {collapsed?<PanelLeftOpen size={20}/>:<PanelLeftClose size={20}/>} | {toggle} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:43](../../../web/components/HostWorkspace.tsx#L43) |
| UI-00667 |  | {()=>void signOut()} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:46](../../../web/components/HostWorkspace.tsx#L46) |
| UI-00668 |  | {()=>setCollapsed(true)} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:47](../../../web/components/HostWorkspace.tsx#L47) |


### Dialog and alert/status inventory

| ID | Role / kind | Text / label | Visibility conditions | Source |
| --- | --- | --- | --- | --- |
| UI-00053 | "status" | Opening your workspace… | Parent/source state | [web/app/components/HostGlobalNav.tsx:13](../../../web/app/components/HostGlobalNav.tsx#L13) |
| UI-00110 | "status" | Loading notifications… | loading | [web/app/events/[id]/notifications/page.tsx:113](../../../web/app/events/[id]/notifications/page.tsx#L113) |
| UI-00111 | "alert" | {error} | error | [web/app/events/[id]/notifications/page.tsx:115](../../../web/app/events/[id]/notifications/page.tsx#L115) |
| UI-00444 | "status" | Opening your celebration space… | ready | [web/components/ExperienceProvider.tsx:13](../../../web/components/ExperienceProvider.tsx#L13) |
| UI-00448 | "alert" | {error} | error | [web/components/GroupActivity.tsx:7](../../../web/components/GroupActivity.tsx#L7) |
| UI-00451 | "alert" | {error} | error | [web/components/GroupCollectionReport.tsx:8](../../../web/components/GroupCollectionReport.tsx#L8) |
| UI-00666 | "alert" | {error} | error | [web/components/HostWorkspace.tsx:46](../../../web/components/HostWorkspace.tsx#L46) |


### Workflow state bindings

| Source | Binding | Initial expression |
| --- | --- | --- |
| web/app/events/[id]/notifications/page.tsx | loading | true |
| web/app/events/[id]/notifications/page.tsx | error | '' |
| web/components/GroupCollectionReport.tsx | error | '' |
| web/components/GroupActivity.tsx | error | '' |
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
| Header | Shared identity/event or page title/back/actions, where declared. Composite header declarations: 5. |
| Navigation / sidebar | Host/admin navigation and event context where applicable; mobile/focused variants are conditional. nav=3, aside=1. |
| Main content | Purpose-specific forms/cards/tables/canvas/player from the composed sources. main=6, section=25, article=6. |
| Search and filters | Fields/selects listed below; exact value/handler/visibility controls are not inferred from placeholder labels. |
| Data grids and cards | Table declarations=1. Data collections and generated rows/cards are in frontend.json dynamicSources. |
| Popups / dialogs | Named/source-declared dialog control records below; opening/closing/loading/apply/cancel actions belong to those component handlers. |
| Alerts / notifications | Source controls with role alert/status are listed below. Notice strings may come from API results; do not assume a universal envelope. |
| Footer | Local/shared footer declarations=2. No undocumented legal/footer wording is invented. |
| Loading / empty / success / error | Observed state bindings and relevant action fields are listed below. Expected QA states from Document 2 do not establish a spinner/retry control where source does not implement one. |


### Composed implementation

- [web/app/events/[id]/preview/page.tsx:1](../../../web/app/events/[id]/preview/page.tsx#L1)
- [web/components/GiftDescription.tsx:1](../../../web/components/GiftDescription.tsx#L1)
- [web/components/ExperienceProvider.tsx:1](../../../web/components/ExperienceProvider.tsx#L1)
- [web/components/focused/Preview.tsx:1](../../../web/components/focused/Preview.tsx#L1)
- [web/components/focused/Invitation.tsx:1](../../../web/components/focused/Invitation.tsx#L1)
- [web/components/GuestGiftBoard.tsx:1](../../../web/components/GuestGiftBoard.tsx#L1)
- [web/components/SpecialGiftEntry.tsx:1](../../../web/components/SpecialGiftEntry.tsx#L1)
- [web/components/GroupContribution.tsx:1](../../../web/components/GroupContribution.tsx#L1)
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
- [web/components/focused/Checkout.tsx:1](../../../web/components/focused/Checkout.tsx#L1)
- [web/components/GuestActions.tsx:1](../../../web/components/GuestActions.tsx#L1)
- [web/components/GuestCelebration.tsx:1](../../../web/components/GuestCelebration.tsx#L1)
- [web/components/SocialSharing.tsx:1](../../../web/components/SocialSharing.tsx#L1)
- [web/app/layout.tsx:1](../../../web/app/layout.tsx#L1)
- [web/app/components/PerformanceDiagnostics.tsx:1](../../../web/app/components/PerformanceDiagnostics.tsx#L1)
- [web/app/components/HostSessionRedirect.tsx:1](../../../web/app/components/HostSessionRedirect.tsx#L1)
- [web/app/components/HostGlobalNav.tsx:1](../../../web/app/components/HostGlobalNav.tsx#L1)
- [web/components/HostWorkspace.tsx:1](../../../web/components/HostWorkspace.tsx#L1)
- [web/components/focused/HostShell.tsx:1](../../../web/components/focused/HostShell.tsx#L1)

### Fields

| Control ID | Label / expression | Type | Binding | Required | Native limits / format | Detailed specification |
| --- | --- | --- | --- | --- | --- | --- |
| UI-00442 | Same as event address | input "checkbox" | {same} | Not declared |  | [control](UI_CONTROLS.md#ui-00442) |
| UI-00443 | {label} | input  | {address[key]} | true | maxLength={maxLength} | [control](UI_CONTROLS.md#ui-00443) |
| UI-00464 | Mobile number | input "tel" | {registrationMobile} | true |  | [control](UI_CONTROLS.md#ui-00464) |
| UI-00465 | Full name | input  | {fullName} | true | minLength={2}; maxLength={120} | [control](UI_CONTROLS.md#ui-00465) |
| UI-00466 | Username | input  | {username} | true | minLength={3}; maxLength={40} | [control](UI_CONTROLS.md#ui-00466) |
| UI-00467 | Sign in with | select  | {loginMethod} | Not declared |  | [control](UI_CONTROLS.md#ui-00467) |
| UI-00468 | Password | input "password" | {password} | true | minLength={8}; maxLength={128} | [control](UI_CONTROLS.md#ui-00468) |
| UI-00469 | Confirm password | input "password" | {confirm} | true | minLength={8} | [control](UI_CONTROLS.md#ui-00469) |
| UI-00470 | Six-digit PIN | input "password" | {pin} | true | minLength={6}; maxLength={6}; pattern="[0-9]{6}" | [control](UI_CONTROLS.md#ui-00470) |
| UI-00471 | Confirm PIN | input "password" | {confirmPin} | true | minLength={6}; maxLength={6}; pattern="[0-9]{6}" | [control](UI_CONTROLS.md#ui-00471) |
| UI-00472 | Six-digit PIN | input "password" | {pin} | true | minLength={6}; maxLength={6}; pattern="[0-9]{6}" | [control](UI_CONTROLS.md#ui-00472) |
| UI-00476 | What should we call you? | input  | {adminNameDraft} | true | minLength={2}; maxLength={120} | [control](UI_CONTROLS.md#ui-00476) |
| UI-00480 | Current password | input "password" | {pinPassword} | true |  | [control](UI_CONTROLS.md#ui-00480) |
| UI-00481 | Six-digit PIN | input "password" | {pin} | true | maxLength={6}; pattern="[0-9]{6}" | [control](UI_CONTROLS.md#ui-00481) |
| UI-00482 | Confirm PIN | input "password" | {confirmPin} | true | maxLength={6}; pattern="[0-9]{6}" | [control](UI_CONTROLS.md#ui-00482) |
| UI-00501 | Who can join? | select  | {privacy.join_policy} | Not declared |  | [control](UI_CONTROLS.md#ui-00501) |
| UI-00502 | Who can see the member list? | select  | {privacy.member_visibility} | Not declared |  | [control](UI_CONTROLS.md#ui-00502) |
| UI-00503 | Who can see individual contribution amounts? | select  | {privacy.amount_visibility} | Not declared |  | [control](UI_CONTROLS.md#ui-00503) |
| UI-00506 | Group name * | input  | {form.name} | true | maxLength={120} | [control](UI_CONTROLS.md#ui-00506) |
| UI-00507 | Your name * | input  | {form.creatorName} | true | maxLength={120} | [control](UI_CONTROLS.md#ui-00507) |
| UI-00508 | Mobile number * | input "tel" | {form.creatorMobile} | true | maxLength={20} | [control](UI_CONTROLS.md#ui-00508) |
| UI-00509 | Group description | textarea  | {form.description} | Not declared | maxLength={500} | [control](UI_CONTROLS.md#ui-00509) |
| UI-00510 | Collection plan | select  | {giftId} | Not declared |  | [control](UI_CONTROLS.md#ui-00510) |
| UI-00511 | Gift name (optional) | input  | {form.giftName} | Not declared | maxLength={160} | [control](UI_CONTROLS.md#ui-00511) |
| UI-00512 | Gift description | textarea  | {form.giftDescription} | Not declared | maxLength={500} | [control](UI_CONTROLS.md#ui-00512) |
| UI-00513 | Gift link | input "url" | {form.giftLink} | Not declared | maxLength={1000} | [control](UI_CONTROLS.md#ui-00513) |
| UI-00514 | Gift goal in ₹ | input  | {form.targetRupees} | Not declared |  | [control](UI_CONTROLS.md#ui-00514) |
| UI-00517 |  | input "file" |  | Not declared |  | [control](UI_CONTROLS.md#ui-00517) |
| UI-00524 | Payment method | select  | {kind} | Not declared |  | [control](UI_CONTROLS.md#ui-00524) |
| UI-00525 | Account nickname | input  | {label} | true | maxLength={80} | [control](UI_CONTROLS.md#ui-00525) |
| UI-00526 | UPI ID | input  | {upiId} | true |  | [control](UI_CONTROLS.md#ui-00526) |
| UI-00527 | Account holder | input  | {accountHolder} | true | maxLength={120} | [control](UI_CONTROLS.md#ui-00527) |
| UI-00528 | Account number | input  | {accountNumber} | true |  | [control](UI_CONTROLS.md#ui-00528) |
| UI-00529 | IFSC | input  | {ifsc} | true | maxLength={11} | [control](UI_CONTROLS.md#ui-00529) |
| UI-00533 | Email address | input "email" | {email} | true | maxLength={254} | [control](UI_CONTROLS.md#ui-00533) |
| UI-00536 | Six-digit code | input  | {code} | true | maxLength={6}; pattern="[0-9]{6}" | [control](UI_CONTROLS.md#ui-00536) |
| UI-00543 | Wishlist gift | select  | {choice} | Not declared |  | [control](UI_CONTROLS.md#ui-00543) |
| UI-00550 | Your contribution amount (₹) | input  | {amount} | true |  | [control](UI_CONTROLS.md#ui-00550) |
| UI-00551 | Your name * | input  | {name} | true | minLength={2}; maxLength={120} | [control](UI_CONTROLS.md#ui-00551) |
| UI-00552 | Mobile or email * | input  | {contact} | true | maxLength={120} | [control](UI_CONTROLS.md#ui-00552) |
| UI-00556 | Transaction reference / UTR * | input  | {reference} | true | minLength={6}; maxLength={120} | [control](UI_CONTROLS.md#ui-00556) |
| UI-00562 | Invitation link options | select  | {accessMode} | Not declared |  | [control](UI_CONTROLS.md#ui-00562) |
| UI-00563 | Suggested contribution (₹) | input  | {amount} | Not declared |  | [control](UI_CONTROLS.md#ui-00563) |
| UI-00564 | UPI account for guests | select  | {upi} | Not declared |  | [control](UI_CONTROLS.md#ui-00564) |
| UI-00565 | Bank account for guests | select  | {bank} | Not declared |  | [control](UI_CONTROLS.md#ui-00565) |
| UI-00566 | Enable manual guest contribution instructions | input "checkbox" | {enabled} | Not declared |  | [control](UI_CONTROLS.md#ui-00566) |
| UI-00576 | "RSVP guest name" | input  | {name} | true | maxLength={120} | [control](UI_CONTROLS.md#ui-00576) |
| UI-00577 | "RSVP email" | input "email" | {email} | true | maxLength={254} | [control](UI_CONTROLS.md#ui-00577) |
| UI-00579 |  | input "radio" | {value} | Not declared |  | [control](UI_CONTROLS.md#ui-00579) |
| UI-00580 | People attending (including you) | input "number" | {party} | true | min={1}; max={10} | [control](UI_CONTROLS.md#ui-00580) |
| UI-00584 | "Guestbook name" | input  | {wishName} | true | maxLength={120} | [control](UI_CONTROLS.md#ui-00584) |
| UI-00585 | "Guestbook message" | textarea  | {message} | true | maxLength={1000} | [control](UI_CONTROLS.md#ui-00585) |
| UI-00586 | "Guestbook photo" | input "file" |  | Not declared |  | [control](UI_CONTROLS.md#ui-00586) |
| UI-00607 |  | input "radio" | {method.id} | Not declared |  | [control](UI_CONTROLS.md#ui-00607) |
| UI-00608 | Destination | select  | {giftId} | Not declared |  | [control](UI_CONTROLS.md#ui-00608) |
| UI-00609 | Amount (₹) | input "number" | {amount} | true | min="1"; max="1000000"; step="0.01" | [control](UI_CONTROLS.md#ui-00609) |
| UI-00610 | Your name * | input  | {name} | true | maxLength={200} | [control](UI_CONTROLS.md#ui-00610) |
| UI-00612 | Email (optional) | input "email" | {email} | Not declared | maxLength={320} | [control](UI_CONTROLS.md#ui-00612) |
| UI-00613 | Phone (optional) | input "tel" | {phone} | Not declared | maxLength={30} | [control](UI_CONTROLS.md#ui-00613) |
| UI-00614 | UPI transaction ID / Bank UTR | input  | {transferReference} | Not declared | maxLength={40} | [control](UI_CONTROLS.md#ui-00614) |
| UI-00629 | Gift name | input  | {name} | true | maxLength={160} | [control](UI_CONTROLS.md#ui-00629) |
| UI-00630 | Description · optional | textarea  | {description} | Not declared | maxLength={500} | [control](UI_CONTROLS.md#ui-00630) |
| UI-00631 | Product link · optional | input "url" | {url} | Not declared | maxLength={1000}; pattern="https://[^\s]+" | [control](UI_CONTROLS.md#ui-00631) |
| UI-00633 | How would you like to give? | select  | {type} | Not declared |  | [control](UI_CONTROLS.md#ui-00633) |
| UI-00645 | Preferred recipient | select  | {selected?.id??''} | Not declared |  | [control](UI_CONTROLS.md#ui-00645) |
| UI-00669 | Collection plan | select  | {plan} | Not declared |  | [control](UI_CONTROLS.md#ui-00669) |
| UI-00670 | Our gift’s product link | input "url" | {value.giftLink} | true | maxLength={1000}; pattern="https://[^\s]+" | [control](UI_CONTROLS.md#ui-00670) |
| UI-00672 | Gift name | input  | {value.giftName} | true | maxLength={160} | [control](UI_CONTROLS.md#ui-00672) |
| UI-00673 | Gift description | textarea  | {value.giftDescription} | Not declared | maxLength={500} | [control](UI_CONTROLS.md#ui-00673) |
| UI-00674 | Collection goal (₹) · optional | input "number" | {value.targetRupees} | Not declared | min="0.01"; max="1000000000"; step="0.01" | [control](UI_CONTROLS.md#ui-00674) |
| UI-00683 | Payment method | select  | {method} | Not declared |  | [control](UI_CONTROLS.md#ui-00683) |
| UI-00684 | UPI transaction ID / Bank UTR | input  | {reference} | true | minLength={8}; maxLength={40}; pattern="[A-Za-z0-9\-]{8,40}" | [control](UI_CONTROLS.md#ui-00684) |
| UI-00749 | Mobile number with country code | input "tel" | {mobile} | true | pattern="\+[1-9][0-9]{7,14}" | [control](UI_CONTROLS.md#ui-00749) |
| UI-00750 | Six-digit verification code | input  | {code} | true | maxLength={6}; pattern="[0-9]{6}" | [control](UI_CONTROLS.md#ui-00750) |
| UI-00795 | Email address | input "email" | {value} | true | maxLength={254} | [control](UI_CONTROLS.md#ui-00795) |
| UI-00809 | "Invitation link" | input  | {url} | Not declared |  | [control](UI_CONTROLS.md#ui-00809) |
| UI-00821 | Shipment tracking details | textarea  | {tracking} | true | maxLength={1000} | [control](UI_CONTROLS.md#ui-00821) |
| UI-00825 | Choose a gift | select  | {source} | Not declared |  | [control](UI_CONTROLS.md#ui-00825) |
| UI-00827 | Your name | input  | {form.guestName} | true | maxLength={120} | [control](UI_CONTROLS.md#ui-00827) |
| UI-00828 | Email address | input "email" | {form.email} | true | maxLength={254} | [control](UI_CONTROLS.md#ui-00828) |
| UI-00829 | Mobile number | input "tel" | {form.mobile} | true | maxLength={25}; pattern="\+?[0-9][0-9 \-]{7,23}" | [control](UI_CONTROLS.md#ui-00829) |
| UI-00831 | Gift name | input  | {form.name} | true | maxLength={160} | [control](UI_CONTROLS.md#ui-00831) |
| UI-00832 | Product link {method==='DIRECT_DELIVERY'?'':' · optional if you add gift details'} | input "url" | {form.productUrl} | {method==='DIRECT_DELIVERY'} | maxLength={1000}; pattern="https://[^\s]+" | [control](UI_CONTROLS.md#ui-00832) |
| UI-00833 | Gift details | textarea  | {form.description} | Not declared | maxLength={500} | [control](UI_CONTROLS.md#ui-00833) |
| UI-00835 |  | input "radio" | {method==='LINK_AND_CONTRIBUTION'} | Not declared |  | [control](UI_CONTROLS.md#ui-00835) |
| UI-00836 |  | input "radio" | {method==='DIRECT_DELIVERY'} | Not declared |  | [control](UI_CONTROLS.md#ui-00836) |
| UI-00837 | Contribution amount (₹) | input "number" | {form.amount} | true | min="1"; max="1000000"; step="0.01" | [control](UI_CONTROLS.md#ui-00837) |
| UI-00846 | Amount (₹) | input "number" | {p.amount} | true | min="1"; max="1000000"; step="0.01" | [control](UI_CONTROLS.md#ui-00846) |
| UI-00847 | Your name | input  | {p.name} | true | maxLength={200} | [control](UI_CONTROLS.md#ui-00847) |
| UI-00849 | Email | input "email" | {p.email} | Not declared | maxLength={320} | [control](UI_CONTROLS.md#ui-00849) |
| UI-00850 | Phone | input "tel" | {p.phone} | Not declared | maxLength={30} | [control](UI_CONTROLS.md#ui-00850) |
| UI-00854 |  | input "radio" | {p.method===method} | Not declared |  | [control](UI_CONTROLS.md#ui-00854) |
| UI-00855 | Transaction reference / bank UTR | input  | {p.reference} | true | maxLength={40} | [control](UI_CONTROLS.md#ui-00855) |
| UI-00896 | Sort | select  | {sort} | Not declared |  | [control](UI_CONTROLS.md#ui-00896) |
| UI-00905 | Preview width | select  | {width} | Not declared |  | [control](UI_CONTROLS.md#ui-00905) |


### Buttons

| ID | Label / expression | Action | Submit validation / handler | Source |
| --- | --- | --- | --- | --- |
| UI-00410 | {busy?'Processing…':offer.amountPaise===0?'Activate access':`${offer.entitled?'Renew':'Purchase'} · ${commercialMoney(offer.amountPaise)} (test mode)`} | {()=>void pay()} | Not a native form submit or inherited from component context | [web/components/CommercePurchase.tsx:27](../../../web/components/CommercePurchase.tsx#L27) |
| UI-00412 | Reload price | {()=>{setRetry(v=>v+1)}} | Not a native form submit or inherited from component context | [web/components/CommercePurchase.tsx:27](../../../web/components/CommercePurchase.tsx#L27) |
| UI-00445 | View full description | {()=>dialog.current?.showModal()} | Not a native form submit or inherited from component context | [web/components/GiftDescription.tsx:9](../../../web/components/GiftDescription.tsx#L9) |
| UI-00447 | Close × | {()=>dialog.current?.close()} | Not a native form submit or inherited from component context | [web/components/GiftDescription.tsx:9](../../../web/components/GiftDescription.tsx#L9) |
| UI-00449 | Newer activity | {()=>setOffset(value=>Math.max(0,value-100))} | Not a native form submit or inherited from component context | [web/components/GroupActivity.tsx:7](../../../web/components/GroupActivity.tsx#L7) |
| UI-00450 | Older activity | {()=>setOffset(value=>value+100)} | Not a native form submit or inherited from component context | [web/components/GroupActivity.tsx:7](../../../web/components/GroupActivity.tsx#L7) |
| UI-00453 | {guestGifts?'Add a Special Gift':giftAvailable?'Group contribution':'Gift held with love ♡'} | {launch} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:79](../../../web/components/GroupContribution.tsx#L79) |
| UI-00455 | × | {()=>void closeAndLock()} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:80](../../../web/components/GroupContribution.tsx#L80) |
| UI-00456 | Sign out | {()=>void logout()} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:81](../../../web/components/GroupContribution.tsx#L81) |
| UI-00459 | {guestGifts?'Create account':'Create Group'} | {()=>{setTab('register');setError('')}} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:81](../../../web/components/GroupContribution.tsx#L81) |
| UI-00460 | {guestGifts?'Sign in':'Open Group'} | {()=>{setTab('login');setError('')}} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:81](../../../web/components/GroupContribution.tsx#L81) |
| UI-00473 | {busy?'Please wait…':tab==='register'?'Register →':'Sign in →'} | submit / component action | {auth} | [web/components/GroupContribution.tsx:81](../../../web/components/GroupContribution.tsx#L81) |
| UI-00474 | Edit name | {()=>{setAdminNameDraft(adminName);setEditingName(true)}} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:81](../../../web/components/GroupContribution.tsx#L81) |
| UI-00477 | Save name | submit / component action | {async e=>{e.preventDefault();try{const x=await call('/profile-name',{fullName:adminNameDraft});setAdminName(x.fullName);setEditingName(false);setError('')}catch(x){setError(x instanceof Error?x.message:'Unable to save name')}}} | [web/components/GroupContribution.tsx:81](../../../web/components/GroupContribution.tsx#L81) |
| UI-00478 | Sign out | {()=>void logout()} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:81](../../../web/components/GroupContribution.tsx#L81) |
| UI-00483 | Save PIN | submit / component action | {async e=>{e.preventDefault();setPinNotice('');try{await call('/pin/setup',{password:pinPassword,pin,confirmPin});setPinNotice('PIN updated successfully. You can now sign in with your new six-digit PIN.');setPinPassword('');setPin('');setConfirmPin('');setEditingPin(false)}catch(x){setPinNotice(x instanceof Error?x.message:'PIN setup failed')}}} | [web/components/GroupContribution.tsx:81](../../../web/components/GroupContribution.tsx#L81) |
| UI-00485 | 1. Receiving account | {()=>setSection('payments')} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:81](../../../web/components/GroupContribution.tsx#L81) |
| UI-00486 | 2. Enable contributions | {()=>setSection('guest')} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:81](../../../web/components/GroupContribution.tsx#L81) |
| UI-00487 | 3. Invite friends | {()=>setSection('overview')} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:81](../../../web/components/GroupContribution.tsx#L81) |
| UI-00488 | Overview | {()=>{setSection('overview');void restore()}} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:81](../../../web/components/GroupContribution.tsx#L81) |
| UI-00489 | Privacy & settings | {()=>setSection('privacy')} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:81](../../../web/components/GroupContribution.tsx#L81) |
| UI-00490 | Members & requests | {()=>void loadMembers()} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:81](../../../web/components/GroupContribution.tsx#L81) |
| UI-00491 | Payment accounts | {()=>setSection('payments')} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:81](../../../web/components/GroupContribution.tsx#L81) |
| UI-00492 | ♡ My contribution | {()=>setSection('contribute')} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:81](../../../web/components/GroupContribution.tsx#L81) |
| UI-00493 | Guest contributions | {()=>setSection('guest')} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:81](../../../web/components/GroupContribution.tsx#L81) |
| UI-00495 | Copy invite link | {()=>void navigator.clipboard.writeText(`${location.origin}/groups/${token}/${group.id}`)} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:81](../../../web/components/GroupContribution.tsx#L81) |
| UI-00496 | Invite link unavailable | submit / component action | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:81](../../../web/components/GroupContribution.tsx#L81) |
| UI-00497 | {published?'Collection published':'Publish collection to host'} | {()=>void publishGroup()} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:81](../../../web/components/GroupContribution.tsx#L81) |
| UI-00498 | Approve | {()=>void reviewMember(m.id,'APPROVE')} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:81](../../../web/components/GroupContribution.tsx#L81) |
| UI-00499 | Reject | {()=>void reviewMember(m.id,'REJECT')} | Not a native form submit or inherited from component context | [web/components/GroupContribution.tsx:81](../../../web/components/GroupContribution.tsx#L81) |
| UI-00504 | {busy?'Saving…':'Save privacy settings'} | submit / component action | {savePrivacy} | [web/components/GroupContribution.tsx:81](../../../web/components/GroupContribution.tsx#L81) |
| UI-00515 | {busy?'Creating…':'Create private group →'} | submit / component action | {setup} | [web/components/GroupContribution.tsx:81](../../../web/components/GroupContribution.tsx#L81) |
| UI-00516 | {d.enabled?'Disable account':'Enable account'} | {()=>void run(`/destinations/${d.id}/enabled`,{enabled:!d.enabled})} | Not a native form submit or inherited from component context | [web/components/GroupCreatorPaymentSettings.tsx:20](../../../web/components/GroupCreatorPaymentSettings.tsx#L20) |
| UI-00518 | Generate QR | {()=>void run(`/destinations/${d.id}/qr/generate`)} | Not a native form submit or inherited from component context | [web/components/GroupCreatorPaymentSettings.tsx:20](../../../web/components/GroupCreatorPaymentSettings.tsx#L20) |
| UI-00519 | Preview QR | {()=>void showQr(d.id)} | Not a native form submit or inherited from component context | [web/components/GroupCreatorPaymentSettings.tsx:20](../../../web/components/GroupCreatorPaymentSettings.tsx#L20) |
| UI-00520 | Make preferred | {()=>void run('/preferred',{destinationId:d.id})} | Not a native form submit or inherited from component context | [web/components/GroupCreatorPaymentSettings.tsx:20](../../../web/components/GroupCreatorPaymentSettings.tsx#L20) |
| UI-00522 | Close preview | {()=>{URL.revokeObjectURL(preview);setPreview(null)}} | Not a native form submit or inherited from component context | [web/components/GroupCreatorPaymentSettings.tsx:22](../../../web/components/GroupCreatorPaymentSettings.tsx#L22) |
| UI-00530 | {busy?'Saving securely…':'Save payment account'} | submit / component action | {add} | [web/components/GroupCreatorPaymentSettings.tsx:24](../../../web/components/GroupCreatorPaymentSettings.tsx#L24) |
| UI-00534 | Send verification code | submit / component action | {start} | [web/components/GroupEmailVerification.tsx:15](../../../web/components/GroupEmailVerification.tsx#L15) |
| UI-00537 | Verify email | submit / component action | {finish} | [web/components/GroupEmailVerification.tsx:15](../../../web/components/GroupEmailVerification.tsx#L15) |
| UI-00539 | Try holding this gift again | {()=>void change(false)} | Not a native form submit or inherited from component context | [web/components/GroupGiftPlan.tsx:7](../../../web/components/GroupGiftPlan.tsx#L7) |
| UI-00540 | Yes, release gift | {()=>void change(true)} | Not a native form submit or inherited from component context | [web/components/GroupGiftPlan.tsx:7](../../../web/components/GroupGiftPlan.tsx#L7) |
| UI-00541 | Keep gift | {()=>setConfirmRelease(false)} | Not a native form submit or inherited from component context | [web/components/GroupGiftPlan.tsx:7](../../../web/components/GroupGiftPlan.tsx#L7) |
| UI-00542 | Release gift · keep our collection | {()=>setConfirmRelease(true)} | Not a native form submit or inherited from component context | [web/components/GroupGiftPlan.tsx:7](../../../web/components/GroupGiftPlan.tsx#L7) |
| UI-00544 | Add gift to our group | {()=>void change(false)} | Not a native form submit or inherited from component context | [web/components/GroupGiftPlan.tsx:7](../../../web/components/GroupGiftPlan.tsx#L7) |
| UI-00548 | Make another contribution | {()=>{setStage('start');setReference('');setSubmissionKey(crypto.randomUUID());setDetails(null)}} | Not a native form submit or inherited from component context | [web/components/GroupGuestCheckout.tsx:22](../../../web/components/GroupGuestCheckout.tsx#L22) |
| UI-00549 | Use suggestion | {()=>setAmount(String(invite.suggested_contribution_paise!/100))} | Not a native form submit or inherited from component context | [web/components/GroupGuestCheckout.tsx:22](../../../web/components/GroupGuestCheckout.tsx#L22) |
| UI-00553 |  | {()=>void choosePayment(d.id)} | Not a native form submit or inherited from component context | [web/components/GroupGuestCheckout.tsx:22](../../../web/components/GroupGuestCheckout.tsx#L22) |
| UI-00555 | {details.kind==='UPI'?'Show payment QR':'Show bank details'} | {()=>setModal(true)} | {submit} | [web/components/GroupGuestCheckout.tsx:22](../../../web/components/GroupGuestCheckout.tsx#L22) |
| UI-00557 | {busy?'Submitting…':'I have done the payment'} | submit / component action | {submit} | [web/components/GroupGuestCheckout.tsx:22](../../../web/components/GroupGuestCheckout.tsx#L22) |
| UI-00558 | Change payment method | {()=>{setStage('start');setDetails(null);setSelected('');setReference('')}} | {submit} | [web/components/GroupGuestCheckout.tsx:22](../../../web/components/GroupGuestCheckout.tsx#L22) |
| UI-00559 | Copy bank details | {()=>void navigator.clipboard.writeText(`${details.accountHolder}\n${details.accountNumber}\n${details.ifsc}`)} | Not a native form submit or inherited from component context | [web/components/GroupGuestCheckout.tsx:24](../../../web/components/GroupGuestCheckout.tsx#L24) |
| UI-00560 | Continue to reference details → | {()=>setModal(false)} | Not a native form submit or inherited from component context | [web/components/GroupGuestCheckout.tsx:24](../../../web/components/GroupGuestCheckout.tsx#L24) |
| UI-00567 | Save guest contribution settings | submit / component action | {save} | [web/components/GroupGuestCreatorSettings.tsx:12](../../../web/components/GroupGuestCreatorSettings.tsx#L12) |
| UI-00571 | Contribute | {onContribute} | Not a native form submit or inherited from component context | [web/components/GuestActions.tsx:11](../../../web/components/GuestActions.tsx#L11) |
| UI-00573 | Save the date | {saveDate} | Not a native form submit or inherited from component context | [web/components/GuestActions.tsx:11](../../../web/components/GuestActions.tsx#L11) |
| UI-00581 | {busy?'Sending…':'Send my RSVP →'} | submit / component action | {submitRsvp} | [web/components/GuestCelebration.tsx:29](../../../web/components/GuestCelebration.tsx#L29) |
| UI-00587 | Remove | {()=>setPhoto(null)} | {submitWish} | [web/components/GuestCelebration.tsx:32](../../../web/components/GuestCelebration.tsx#L32) |
| UI-00588 | {busy?'Sending…':'Send my wishes ♡'} | submit / component action | {submitWish} | [web/components/GuestCelebration.tsx:32](../../../web/components/GuestCelebration.tsx#L32) |
| UI-00590 | ↻ Refresh wishes | {()=>void refreshWishes()} | Not a native form submit or inherited from component context | [web/components/GuestCelebration.tsx:35](../../../web/components/GuestCelebration.tsx#L35) |
| UI-00591 | {showWishes?'Show fewer wishes':'View all wishes'} | {()=>setShowWishes(v=>!v)} | Not a native form submit or inherited from component context | [web/components/GuestCelebration.tsx:36](../../../web/components/GuestCelebration.tsx#L36) |
| UI-00593 | Refresh fee | {()=>{requestKey.current='';setPricingRetry(v=>v+1)}} | Not a native form submit or inherited from component context | [web/components/GuestContributionCheckout.tsx:108](../../../web/components/GuestContributionCheckout.tsx#L108) |
| UI-00596 | Retry | {()=>setDestinationRetry(v=>v+1)} | Not a native form submit or inherited from component context | [web/components/GuestContributionCheckout.tsx:130](../../../web/components/GuestContributionCheckout.tsx#L130) |
| UI-00597 | ✕ | {onClose} | Not a native form submit or inherited from component context | [web/components/GuestContributionCheckout.tsx:132](../../../web/components/GuestContributionCheckout.tsx#L132) |
| UI-00599 | Make another contribution | {()=>{setDirectStatus('');setTransferReference('');requestKey.current='';}} | Not a native form submit or inherited from component context | [web/components/GuestContributionCheckout.tsx:139](../../../web/components/GuestContributionCheckout.tsx#L139) |
| UI-00601 | Make another contribution | {()=>{setReceipt(null);requestKey.current='';setDirectStatus('');}} | Not a native form submit or inherited from component context | [web/components/GuestContributionCheckout.tsx:140](../../../web/components/GuestContributionCheckout.tsx#L140) |
| UI-00605 | Retry | {()=>setDestinationRetry(v=>v+1)} | {submit} | [web/components/GuestContributionCheckout.tsx:143](../../../web/components/GuestContributionCheckout.tsx#L143) |
| UI-00611 | {showContact?'− Hide optional contact details':'+ Add email or phone (optional)'} | {()=>setShowContact(v=>!v)} | {submit} | [web/components/GuestContributionCheckout.tsx:154](../../../web/components/GuestContributionCheckout.tsx#L154) |
| UI-00616 | I have done the payment | {reportDirectPayment} | {submit} | [web/components/GuestContributionCheckout.tsx:165](../../../web/components/GuestContributionCheckout.tsx#L165) |
| UI-00618 | {busy?'Processing…':`Pay ${validAmount&&pricing?money(pricing.totalPaise):''} (Test Mode)`} | submit / component action | {submit} | [web/components/GuestContributionCheckout.tsx:173](../../../web/components/GuestContributionCheckout.tsx#L173) |
| UI-00619 | Refresh gifts | {()=>void load()} | Not a native form submit or inherited from component context | [web/components/GuestGiftBoard.tsx:10](../../../web/components/GuestGiftBoard.tsx#L10) |
| UI-00622 | Try again | {()=>void load()} | Not a native form submit or inherited from component context | [web/components/GuestGiftBoard.tsx:10](../../../web/components/GuestGiftBoard.tsx#L10) |
| UI-00632 | Add gift | submit / component action | {add} | [web/components/GuestGiftManager.tsx:15](../../../web/components/GuestGiftManager.tsx#L15) |
| UI-00638 | View delivery address | {async()=>{setError('');setBusy(true);try{const address=await call(`/guest-gifts/${g.id}/delivery-address`);setAddresses(value=>({...value,[g.id]:address}))}catch(e){setError(e instanceof Error?e.message:'Could not load delivery address')}finally{setBusy(false)}}} | Not a native form submit or inherited from component context | [web/components/GuestGiftManager.tsx:15](../../../web/components/GuestGiftManager.tsx#L15) |
| UI-00639 | Publish gift to host | {()=>void mutate(`/guest-gifts/${g.id}/publish`,{},'Gift published. The host can now see it in their contribution report.')} | Not a native form submit or inherited from component context | [web/components/GuestGiftManager.tsx:15](../../../web/components/GuestGiftManager.tsx#L15) |
| UI-00640 | {g.disabled?g.selection_label:type==='GROUP'?'Select for our group':'Select this gift'} | {()=>void mutate(`/guest-gifts/${g.id}/select`,{contributionType:type,...(type==='GROUP'?{groupId:group!.id}:{})},'Gift selected. You can now view the delivery address and publish when ready.')} | Not a native form submit or inherited from component context | [web/components/GuestGiftManager.tsx:15](../../../web/components/GuestGiftManager.tsx#L15) |
| UI-00643 | Retry | {()=>setRetry(v=>v+1)} | Not a native form submit or inherited from component context | [web/components/GuestVerifiedDestination.tsx:35](../../../web/components/GuestVerifiedDestination.tsx#L35) |
| UI-00648 | Copy UPI ID | {()=>navigator.clipboard.writeText(selected.upiId\|\|'').then(()=>setCopyNotice('UPI ID copied.')).catch(()=>setCopyNotice('Select the UPI ID above to copy it.'))} | Not a native form submit or inherited from component context | [web/components/GuestVerifiedDestination.tsx:44](../../../web/components/GuestVerifiedDestination.tsx#L44) |
| UI-00650 | Show UPI QR | {()=>setQrOpen(true)} | Not a native form submit or inherited from component context | [web/components/GuestVerifiedDestination.tsx:46](../../../web/components/GuestVerifiedDestination.tsx#L46) |
| UI-00652 | ✕ Close | {()=>setQrOpen(false)} | Not a native form submit or inherited from component context | [web/components/GuestVerifiedDestination.tsx:49](../../../web/components/GuestVerifiedDestination.tsx#L49) |
| UI-00664 | {collapsed?<PanelLeftOpen size={20}/>:<PanelLeftClose size={20}/>} | {toggle} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:43](../../../web/components/HostWorkspace.tsx#L43) |
| UI-00667 |  | {()=>void signOut()} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:46](../../../web/components/HostWorkspace.tsx#L46) |
| UI-00668 |  | {()=>setCollapsed(true)} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:47](../../../web/components/HostWorkspace.tsx#L47) |
| UI-00671 | Fill gift details from link | {()=>void lookup()} | Not a native form submit or inherited from component context | [web/components/IndependentGroupPlan.tsx:16](../../../web/components/IndependentGroupPlan.tsx#L16) |
| UI-00677 | Save our gift plan | submit / component action | {save} | [web/components/IndependentGroupPlan.tsx:22](../../../web/components/IndependentGroupPlan.tsx#L22) |
| UI-00685 | {busy?'Sharing payment details…':'I’ve paid — share details with the host'} | submit / component action | {submit} | [web/components/IndividualHostContribution.tsx:22](../../../web/components/IndividualHostContribution.tsx#L22) |
| UI-00686 | Try again | {()=>setRetry(v=>v+1)} | Not a native form submit or inherited from component context | [web/components/IndividualHostContribution.tsx:22](../../../web/components/IndividualHostContribution.tsx#L22) |
| UI-00747 | Use a different mobile number | {()=>{setVerified(false);setMessage('')}} | Not a native form submit or inherited from component context | [web/components/MobileVerification.tsx:13](../../../web/components/MobileVerification.tsx#L13) |
| UI-00751 | {busy?'Please wait…':challenge?'Verify mobile':'Send verification code'} | submit / component action | {submit} | [web/components/MobileVerification.tsx:13](../../../web/components/MobileVerification.tsx#L13) |
| UI-00752 | Change number / request another code | {()=>{setChallenge('');setCode('');setMessage('Wait at least 60 seconds before requesting another code.')}} | {submit} | [web/components/MobileVerification.tsx:13](../../../web/components/MobileVerification.tsx#L13) |
| UI-00803 | Share invitation | {()=>void share()} | Not a native form submit or inherited from component context | [web/components/SocialSharing.tsx:13](../../../web/components/SocialSharing.tsx#L13) |
| UI-00807 | {copied?<Check size={16}/>:<Copy size={16}/>} {copied?'Copied':'Copy link'} | {()=>void copy()} | Not a native form submit or inherited from component context | [web/components/SocialSharing.tsx:13](../../../web/components/SocialSharing.tsx#L13) |
| UI-00810 | Add a Special Gift | {()=>{setStep('choice');setError('')}} | Not a native form submit or inherited from component context | [web/components/SpecialGiftEntry.tsx:63](../../../web/components/SpecialGiftEntry.tsx#L63) |
| UI-00812 | × | {close} | Not a native form submit or inherited from component context | [web/components/SpecialGiftEntry.tsx:66](../../../web/components/SpecialGiftEntry.tsx#L66) |
| UI-00813 |  | {()=>void individual()} | Not a native form submit or inherited from component context | [web/components/SpecialGiftEntry.tsx:67](../../../web/components/SpecialGiftEntry.tsx#L67) |
| UI-00814 |  | {()=>{setError('');setStep('group')}} | Not a native form submit or inherited from component context | [web/components/SpecialGiftEntry.tsx:67](../../../web/components/SpecialGiftEntry.tsx#L67) |
| UI-00815 | ← Giving options | {()=>setStep('choice')} | Not a native form submit or inherited from component context | [web/components/SpecialGiftEntry.tsx:68](../../../web/components/SpecialGiftEntry.tsx#L68) |
| UI-00818 | View host delivery address | {async()=>{setBusy(true);setError('');try{await showAddress(saved.id)}catch(e){setError(e instanceof Error?e.message:'Address unavailable')}finally{setBusy(false)}}} | Not a native form submit or inherited from component context | [web/components/SpecialGiftEntry.tsx:69](../../../web/components/SpecialGiftEntry.tsx#L69) |
| UI-00819 | {saved.delivery_status==='GIFT_PURCHASED'\|\|saved.delivery_status==='TRACKING_SHARED'?'Gift purchased':'I have purchased the gift'} | {()=>void updateDelivery('GIFT_PURCHASED')} | Not a native form submit or inherited from component context | [web/components/SpecialGiftEntry.tsx:69](../../../web/components/SpecialGiftEntry.tsx#L69) |
| UI-00822 | Share tracking with host | submit / component action | {e=>{e.preventDefault();void updateDelivery('TRACKING_SHARED')}} | [web/components/SpecialGiftEntry.tsx:69](../../../web/components/SpecialGiftEntry.tsx#L69) |
| UI-00823 | Add another special gift | {()=>{setSaved(null);setSource('');setForm(empty);setAddress(null);setTracking('');setNotice('');void individual()}} | Not a native form submit or inherited from component context | [web/components/SpecialGiftEntry.tsx:69](../../../web/components/SpecialGiftEntry.tsx#L69) |
| UI-00838 | {busy?'Saving your gift…':method==='LINK_AND_CONTRIBUTION'?'Continue to host payment':'Add my special gift'} | submit / component action | {save} | [web/components/SpecialGiftEntry.tsx:75](../../../web/components/SpecialGiftEntry.tsx#L75) |
| UI-00839 | Manage {g.name} | {()=>{setSaved(g);setAddress(null);setTracking('');setNotice('')}} | Not a native form submit or inherited from component context | [web/components/SpecialGiftEntry.tsx:77](../../../web/components/SpecialGiftEntry.tsx#L77) |
| UI-00841 | ✕ | {p.close} | Not a native form submit or inherited from component context | [web/components/focused/Checkout.tsx:7](../../../web/components/focused/Checkout.tsx#L7) |
| UI-00843 | Back to the celebration | {p.close} | Not a native form submit or inherited from component context | [web/components/focused/Checkout.tsx:7](../../../web/components/focused/Checkout.tsx#L7) |
| UI-00845 | ₹ {Number(value).toLocaleString('en-IN')} | {()=>p.setAmount(value)} | {e=>{if(step===0){e.preventDefault();if(p.valid&&p.name.trim())setStep(1)}else p.submit(e)}} | [web/components/focused/Checkout.tsx:7](../../../web/components/focused/Checkout.tsx#L7) |
| UI-00848 | {contact?'Hide optional contact details':'+ Add email or phone · optional'} | {()=>setContact(v=>!v)} | {e=>{if(step===0){e.preventDefault();if(p.valid&&p.name.trim())setStep(1)}else p.submit(e)}} | [web/components/focused/Checkout.tsx:7](../../../web/components/focused/Checkout.tsx#L7) |
| UI-00851 | Continue → | submit / component action | {e=>{if(step===0){e.preventDefault();if(p.valid&&p.name.trim())setStep(1)}else p.submit(e)}} | [web/components/focused/Checkout.tsx:7](../../../web/components/focused/Checkout.tsx#L7) |
| UI-00852 | Edit amount | {()=>setStep(0)} | {e=>{if(step===0){e.preventDefault();if(p.valid&&p.name.trim())setStep(1)}else p.submit(e)}} | [web/components/focused/Checkout.tsx:7](../../../web/components/focused/Checkout.tsx#L7) |
| UI-00856 | {p.busy?'Opening secure checkout…':`Pay ${inr(p.totalPaise??amount)} · Test mode`} | submit / component action | {e=>{if(step===0){e.preventDefault();if(p.valid&&p.name.trim())setStep(1)}else p.submit(e)}} | [web/components/focused/Checkout.tsx:7](../../../web/components/focused/Checkout.tsx#L7) |
| UI-00857 | {p.busy?'Sending details…':'Submit transfer details'} | {p.report} | {e=>{if(step===0){e.preventDefault();if(p.valid&&p.name.trim())setStep(1)}else p.submit(e)}} | [web/components/focused/Checkout.tsx:7](../../../web/components/focused/Checkout.tsx#L7) |
| UI-00898 | Try again | {onRetry} | Not a native form submit or inherited from component context | [web/components/focused/Invitation.tsx:26](../../../web/components/focused/Invitation.tsx#L26) |
| UI-00900 | {(g.contributionEnabled===false)?g.contributionDisabledReason\|\|"A lovely surprise is on its way ♡":preview?'Contribution preview only':'Contribute'} | {()=>open(g)} | Not a native form submit or inherited from component context | [web/components/focused/Invitation.tsx:26](../../../web/components/focused/Invitation.tsx#L26) |
| UI-00901 | Choose an amount | {()=>open(null)} | Not a native form submit or inherited from component context | [web/components/focused/Invitation.tsx:26](../../../web/components/focused/Invitation.tsx#L26) |
| UI-00903 | Try again | {()=>location.reload()} | Not a native form submit or inherited from component context | [web/components/focused/Invitation.tsx:31](../../../web/components/focused/Invitation.tsx#L31) |


### Dialog and alert/status inventory

| ID | Role / kind | Text / label | Visibility conditions | Source |
| --- | --- | --- | --- | --- |
| UI-00446 | dialog |  | Parent/source state | [web/components/GiftDescription.tsx:9](../../../web/components/GiftDescription.tsx#L9) |
| UI-00454 | "dialog" | {logged&&guestGifts&&<><div className="gc-dash-head"><h2>Your guest account</h2>{!group&&<button type="button" disabled={busy} onClick={()=>void logout()}>Sign out</button>}</div><GuestGiftManager token={token} call={call} group={group}/></>} {logged&&accountNotice&&<p role="status">{accountNotice}</p>} {logged&&(emailVerificationEnabled\|\|mobileOtpEnabled)&&<details className="gc-panel"><summary>Account · contact verification</summary><GroupEmailVerification token={token} pageId={pageId.current!} onVerified={()=>void refreshPlan()}/><MobileVerification token={token} pageId={pageId.current!} onVerified={()=>void restore()}/></details>} {error&&<p className="gc-error gc-sticky-feedback" role="alert">{error}</p>} {loading?<p>Opening your group…</p>:!logged?<><div className="gc-auth-heading"><span className="gc-auth-icon" aria-hidden="true">♧</span><h2>{guestGifts?"A thoughtful gift from you.":"A thoughtful gift, together."}</h2><p>{guestGifts?"Sign in or create an account to manage an individual gift or organize a group gift.":"Create a group or sign in to organize your people."}</p></div><div className="gc-tabs" role="tablist"><button type="button" aria-selected={tab==='register'} role="tab" onClick={()=>{setTab('register');setError('')}}>{guestGifts?'Create account':'Create Group'}</button><button type="button" aria-selected={tab==='login'} role="tab" onClick={()=>{setTab('login');setError('')}}>{guestGifts?'Sign in':'Open Group'}</button></div>{emailVerificationEnabled&&<div className="recovery-links"><a href={`/forgot-username?kind=GROUP_ADMIN&eventToken=${encodeURIComponent(token)}&returnTo=${encodeURIComponent('/e/'+token)}`}>Forgot username?</a><a href={`/forgot-password?kind=GROUP_ADMIN&returnTo=${encodeURIComponent('/e/'+token)}`}>Forgot password or PIN?</a></div>}<form className={`gc-form gc-auth-form ${tab==='register'?'gc-register-grid':''}`} onSubmit={auth}>{tab==='register'&&<><SignupEmailField value={registrationEmail} onChange={setRegistrationEmail}/><label>Mobile number<input required type="tel" autoComplete="tel" placeholder="+919876543210" value={registrationMobile} onChange={e=>setRegistrationMobile(e.target.value)}/></label><p>Use your own email and mobile for account recovery.</p></>}{tab==='register'&&<label>Full name<input required minLength={2} maxLength={120} value={fullName} onChange={e=>setFullName(e.target.value)}/></label>}{<label>Username<input required minLength={3} maxLength={40} autoComplete="username" autoCapitalize="none" spellCheck={false} value={username} onChange={e=>setUsername(e.target.value)}/></label>}{tab==='login'&&<label>Sign in with<select value={loginMethod} onChange={e=>setLoginMethod(e.target.value as 'password'\|'pin')}>{allowPin&&<option value="pin">Username and six-digit PIN</option>}<option value="password">Username and password</option></select></label>}{(tab==='register'\|\|loginMethod==='password')&&<label>Password<input required minLength={8} maxLength={128} type="password" autoComplete={tab==='register'?'new-password':'current-password'} value={password} onChange={e=>setPassword(e.target.value)}/></label>}{tab==='register'&&<label>Confirm password<input required type="password" minLength={8} value={confirm} onChange={e=>setConfirm(e.target.value)}/></label>}{tab==='register'&&allowPin&&<><label>Six-digit PIN<input required type="password" inputMode="numeric" pattern="[0-9]{6}" minLength={6} maxLength={6} value={pin} onChange={e=>setPin(e.target.value)}/></label><label>Confirm PIN<input required type="password" inputMode="numeric" pattern="[0-9]{6}" minLength={6} maxLength={6} value={confirmPin} onChange={e=>setConfirmPin(e.target.value)}/></label></>}{tab==='login'&&loginMethod==='pin'&&<label>Six-digit PIN<input required type="password" inputMode="numeric" pattern="[0-9]{6}" minLength={6} maxLength={6} value={pin} onChange={e=>setPin(e.target.value)}/></label>}<button disabled={busy\|\|allowPin===null\|\|(tab==='register'&&!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(registrationEmail.trim()))} className="gc-launch">{busy?'Please wait…':tab==='register'?'Register →':'Sign in →'}</button></form></>:group?<>{!published&&<section className="gc-panel"><h3>Group coordination fee</h3><p>A one-time fee paid by the creator. Member transfers still go directly to the creator. If the fee is switched off, publish without a purchase.</p><CommercePurchase stream="GROUP_GIFT" resourceId={group.id} groupBase={`/api/public/groups/${token}/coordination/${group.id}`} pageId={pageId.current!} onComplete={()=>void restore()}/></section>}<details className="gc-panel gc-profile"><summary>Your profile · {adminName\|\|'Organizer'}</summary>{adminName&&!editingName?<><h3>Welcome back, {adminName}!</h3><button type="button" className="gc-secondary" onClick={()=>{setAdminNameDraft(adminName);setEditingName(true)}}>Edit name</button></>:<form className="gc-form" onSubmit={async e=>{e.preventDefault();try{const x=await call('/profile-name',{fullName:adminNameDraft});setAdminName(x.fullName);setEditingName(false);setError('')}catch(x){setError(x instanceof Error?x.message:'Unable to save name')}}}><label>What should we call you?<input required minLength={2} maxLength={120} value={adminNameDraft} onChange={e=>setAdminNameDraft(e.target.value)}/></label><button className="gc-secondary">Save name</button></form>}</details><div className="gc-dash-head"><div><h2>{group.name}</h2><p>Created by {group.creatorName}</p></div><div className="gc-dash-actions"><span className="gc-status">{independent?'Group created ♡':group.status==='DRAFT'?'Private draft':group.status}</span><button type="button" className="gc-secondary" disabled={busy} onClick={()=>void logout()}>Sign out</button></div></div>{allowPin&&<details className="gc-panel" open={editingPin} onToggle={e=>setEditingPin(e.currentTarget.open)}><summary>Account security (change PIN)</summary><form className="gc-form" onSubmit={async e=>{e.preventDefault();setPinNotice('');try{await call('/pin/setup',{password:pinPassword,pin,confirmPin});setPinNotice('PIN updated successfully. You can now sign in with your new six-digit PIN.');setPinPassword('');setPin('');setConfirmPin('');setEditingPin(false)}catch(x){setPinNotice(x instanceof Error?x.message:'PIN setup failed')}}}><label>Current password<input required type="password" value={pinPassword} onChange={e=>setPinPassword(e.target.value)}/></label><label>Six-digit PIN<input required type="password" inputMode="numeric" pattern="[0-9]{6}" maxLength={6} value={pin} onChange={e=>setPin(e.target.value)}/></label><label>Confirm PIN<input required type="password" inputMode="numeric" pattern="[0-9]{6}" maxLength={6} value={confirmPin} onChange={e=>setConfirmPin(e.target.value)}/></label><button className="gc-secondary">Save PIN</button>{pinNotice&&<p role="status">{pinNotice}</p>}</form></details>}<div className="gc-metrics"><div><small>Gift goal</small><strong>{group.targetPaise!=null?`₹${(group.targetPaise/100).toLocaleString('en-IN')}`:'Not set'}</strong></div><div><small>Members</small><strong>{summary?.member_count??'—'}</strong></div><div><small>Reported transfers — unverified</small><strong>{summary?`₹${(Number(summary.reported_paise)/100).toLocaleString('en-IN')}`:'—'}</strong></div></div>{independent&&<section className="gc-panel"><h3>Your group is cooking up something special 🍳</h3><p>Your group is now listed on the invitation. Add a receiving account, enable contributions, then share your group link. Friends can give with or without signing in.</p><button type="button" className="gc-secondary" onClick={()=>setSection('payments')}>1. Receiving account</button> <button type="button" className="gc-secondary" onClick={()=>setSection('guest')}>2. Enable contributions</button> <button type="button" className="gc-secondary" onClick={()=>setSection('overview')}>3. Invite friends</button></section>}<div className="gc-nav"><button type="button" aria-pressed={section==='overview'} onClick={()=>{setSection('overview');void restore()}}>Overview</button><button type="button" aria-pressed={section==='privacy'} onClick={()=>setSection('privacy')}>Privacy & settings</button><button type="button" aria-pressed={section==='members'} onClick={()=>void loadMembers()}>Members & requests</button><button type="button" aria-pressed={section==='payments'} onClick={()=>setSection('payments')}>Payment accounts</button><button type="button" aria-pressed={section==='contribute'} onClick={()=>setSection('contribute')}>♡ My contribution</button><button type="button" aria-pressed={section==='guest'} onClick={()=>setSection('guest')}>Guest contributions</button></div>{section==='contribute'?<div className="gc-panel"><h3>Make your contribution</h3><p>Join your group’s gift and keep your transfer details in one place.</p>{ready.enabled?<GroupGuestCheckout token={token} groupId={group.id} creator creatorName={group.creatorName} pageId={pageId.current!}/>:<p>Enable the invite link from Overview, then enable a payment method in Guest contributions to start.</p>}</div>:section==='guest'?<GroupGuestCreatorSettings token={token} groupId={group.id} pageId={pageId.current!}/>:section==='payments'?<GroupCreatorPaymentSettings token={token} groupId={group.id} pageId={pageId.current!}/>:section==='overview'?<div className="gc-panel"><h3>Group overview</h3>{group.giftName&&<p>🎁 {group.giftName}</p>}{group.description&&<p>{group.description}</p>}<p>Group membership is available. Share the invite link after enabling it.</p>{ready.enabled?<><p>Group link: <a href={`/groups/${token}/${group.id}`}>Open member join page</a></p><button type="button" className="gc-secondary" onClick={()=>void navigator.clipboard.writeText(`${location.origin}/groups/${token}/${group.id}`)}>Copy invite link</button></>:<><span title={ready.reasons.join('; ')}><button disabled>Invite link unavailable</button></span><p>{ready.reasons.join('; ')}</p></>}<p><strong>Host visibility:</strong> {published?'Collection published to the host.':'Your collection stays private until you publish it.'}</p><span title={ready.reasons.join('; ')}><button type="button" className="gc-launch" disabled={busy\|\|!ready.enabled\|\|published} onClick={()=>void publishGroup()}>{published?'Collection published':'Publish collection to host'}</button></span>{independent?<IndependentGroupPlan key={group.id} group={group} call={call} onChange={refreshPlan} busy={busy} setBusy={setBusy}/>:<GroupGiftPlan groupId={group.id} giftName={group.giftName} selectedGiftId={selectedGiftId} reserved={reserved} emailVerified={creatorEmailVerified} wishlist={wishlist} call={call} onChange={refreshPlan} onNotice={setAccountNotice} busy={busy} setBusy={setBusy}/>}<GroupActivity url={base+`/activity/${group.id}`} pageId={pageId.current!} revision={historyRevision}/></div>:section==='members'?<div className="gc-panel"><h3>Member requests</h3><p>Rejected members are visible only here. Mobile numbers are private.</p>{members.length===0?<p>No requests yet.</p>:members.map(m=><div key={m.id} className="gc-member"><strong>{m.display_name}</strong> · {m.status}<small>{m.mobile}</small>{m.status==='PENDING'&&<div><button type="button" className="gc-secondary" disabled={busy} onClick={()=>void reviewMember(m.id,'APPROVE')}>Approve</button> <button type="button" className="gc-secondary" disabled={busy} onClick={()=>void reviewMember(m.id,'REJECT')}>Reject</button></div>}</div>)}</div>:<form className="gc-form gc-panel" onSubmit={savePrivacy}><h3>Who can see what?</h3><p>These settings control joining and member visibility. Manual transfers remain unverified until independently confirmed.</p><label>Who can join?<select value={privacy.join_policy} onChange={e=>setPrivacy({...privacy,join_policy:e.target.value})}><option value="APPROVAL">Anyone with invite link — Admin approval</option><option value="AUTO_JOIN">Anyone with invite link — Auto join</option><option value="INVITE_ONLY">Invitation only — individual invitation required</option></select></label><label>Who can see the member list?<select value={privacy.member_visibility} onChange={e=>setPrivacy({...privacy,member_visibility:e.target.value})}><option value="ADMIN_ONLY">Admin only</option><option value="MEMBERS">Approved members</option></select></label><label>Who can see individual contribution amounts?<select value={privacy.amount_visibility} onChange={e=>setPrivacy({...privacy,amount_visibility:e.target.value})}><option value="ADMIN_ONLY">Admin only</option><option value="OWN_ONLY">Each member sees only their own</option></select></label><p>To offer UPI or bank transfers, configure Payment accounts, then choose accounts and contribution access under Guest contributions. Reported transfers do not confirm receipt of funds.</p><p><strong>Host access:</strong> Collection details are shared only when you publish to the host.</p><button className="gc-launch" disabled={busy}>{busy?'Saving…':'Save privacy settings'}</button></form>}</>:<><details open={independent\|\|!guestGifts}><summary>Create a group for a gift together</summary><h2>Create your group</h2><p>{independent?'Tell us about your group. It will appear as Something is Cooking while you plan your special gift.':'Tell us about your group. Only you can see these details for now.'}</p><form className="gc-form" onSubmit={setup}><label>Group name *<input required maxLength={120} value={form.name} onChange={e=>setForm({...form,name:e.target.value})}/></label><label>Your name *<input required maxLength={120} value={form.creatorName} onChange={e=>setForm({...form,creatorName:e.target.value})}/></label><label>Mobile number *<input required type="tel" maxLength={20} value={form.creatorMobile} onChange={e=>setForm({...form,creatorMobile:e.target.value})}/></label><label>Group description<textarea maxLength={500} value={form.description} onChange={e=>setForm({...form,description:e.target.value})}/></label>{independent?<IndependentGroupPlanFields value={form} onChange={value=>setForm(current=>({...current,...value}))} call={call} busy={busy} setBusy={setBusy}/>:<><label>Collection plan<select value={giftId} onChange={e=>{setGiftId(e.target.value);if(!e.target.value)setForm(value=>({...value,giftName:'',giftDescription:'',giftLink:'',targetRupees:''}))}}><option value="">General collection · choose a gift later</option>{wishlist.filter(g=>g.contributionEnabled!==false).map(g=><option key={g.id} value={g.id}>{g.name}</option>)}</select></label>{giftId&&<p>Wishlist gift details are filled in below. The gift stays available until your creator email is verified.</p>}<label>Gift name (optional)<input maxLength={160} readOnly={!!giftId} value={form.giftName} onChange={e=>setForm({...form,giftName:e.target.value})}/></label><label>Gift description<textarea maxLength={500} readOnly={!!giftId} value={form.giftDescription} onChange={e=>setForm({...form,giftDescription:e.target.value})}/></label><label>Gift link<input type="url" maxLength={1000} readOnly={!!giftId} value={form.giftLink} onChange={e=>setForm({...form,giftLink:e.target.value})}/></label><label>Gift goal in ₹<input inputMode="decimal" readOnly={!!giftId} value={form.targetRupees} onChange={e=>setForm({...form,targetRupees:e.target.value})}/></label></>}<button className="gc-launch" disabled={busy}>{busy?'Creating…':'Create private group →'}</button></form></details></>} | open | [web/components/GroupContribution.tsx:80](../../../web/components/GroupContribution.tsx#L80) |
| UI-00521 | "dialog" | "UPI QR preview" | preview | [web/components/GroupCreatorPaymentSettings.tsx:22](../../../web/components/GroupCreatorPaymentSettings.tsx#L22) |
| UI-00651 | "dialog" | {/* eslint-disable-next-line @next/next/no-img-element */} | !focused&&qrOpen; selected?.method==='UPI'&&selected.qrPngBase64&&!focused; eligible.length===0; error; loading | [web/components/GuestVerifiedDestination.tsx:48](../../../web/components/GuestVerifiedDestination.tsx#L48) |
| UI-00811 | "dialog" | {step==='choice'?<><h2 id="special-gift-title">How would you like to give?</h2><p>Plan a thoughtful gift on your own or bring your favourite people together.</p><div className="special-gift-choices"><button data-autofocus type="button" className="gc-secondary" onClick={()=>void individual()}><strong>Individual Gift</strong><span>Add your details and choose how to share your gift.</span><small>No account or group needed</small></button><button type="button" className="gc-secondary" onClick={()=>{setError('');setStep('group')}}><strong>Group Gift</strong><span>Create a group and collect contributions together.</span><small>Sign in or create an account to get started</small></button></div></>:<> <button type="button" className="gc-secondary" disabled={busy} onClick={()=>setStep('choice')}>← Giving options</button><h2 id="special-gift-title">Your special gift</h2> {saved?<><p role="status">{notice\|\|`Manage ${saved.name}`}</p>{saved.giving_method==='LINK_AND_CONTRIBUTION'&&<IndividualHostContribution token={token} giftId={saved.id} call={call} onBusyChange={setBusy} onReported={()=>notifyGuestGifts(token)}/>} {saved.giving_method==='DIRECT_DELIVERY'&&<section className="gc-panel"><h3>Deliver your gift to the host</h3><p>Purchase the item from the retailer and use the host’s address at checkout.</p>{saved.product_url&&<a href={saved.product_url} target="_blank" rel="noopener noreferrer">Purchase gift from the retailer ↗</a>}{address?<address>{address.addressLine1\|\|'Please contact the host to confirm the delivery address.'}<br/>{address.city}, {address.state} {address.postalCode}<br/>{address.country}</address>:<button type="button" disabled={busy} onClick={async()=>{setBusy(true);setError('');try{await showAddress(saved.id)}catch(e){setError(e instanceof Error?e.message:'Address unavailable')}finally{setBusy(false)}}}>View host delivery address</button>}<button type="button" className="gc-secondary" disabled={busy\|\|saved.delivery_status==='TRACKING_SHARED'\|\|saved.delivery_status==='GIFT_PURCHASED'} onClick={()=>void updateDelivery('GIFT_PURCHASED')}>{saved.delivery_status==='GIFT_PURCHASED'\|\|saved.delivery_status==='TRACKING_SHARED'?'Gift purchased':'I have purchased the gift'}</button><form className="gc-form" onSubmit={e=>{e.preventDefault();void updateDelivery('TRACKING_SHARED')}}><label>Shipment tracking details<textarea required maxLength={1000} placeholder="Carrier name, tracking number or tracking link" value={tracking} onChange={e=>setTracking(e.target.value)}/></label><button className="gc-launch" disabled={busy}>Share tracking with host</button></form></section>}<button type="button" className="gc-secondary" disabled={busy} onClick={()=>{setSaved(null);setSource('');setForm(empty);setAddress(null);setTracking('');setNotice('');void individual()}}>Add another special gift</button></>:<form className="gc-form" onSubmit={save}> {gifts.length>0&&<label>Choose a gift<select disabled={busy} value={source} onChange={e=>choose(e.target.value)}><option value="">Add my own special gift</option>{gifts.map(g=><option key={g.id} value={g.id}>{g.name}</option>)}</select></label>} <fieldset disabled={busy}><legend>Your details</legend><label>Your name<input data-autofocus required autoComplete="name" maxLength={120} value={form.guestName} onChange={e=>setForm({...form,guestName:e.target.value})}/></label><label>Email address<input required type="email" autoComplete="email" maxLength={254} value={form.email} onChange={e=>setForm({...form,email:e.target.value})}/></label><label>Mobile number<input required type="tel" autoComplete="tel" pattern="\+?[0-9][0-9 \-]{7,23}" maxLength={25} value={form.mobile} onChange={e=>setForm({...form,mobile:e.target.value})}/></label></fieldset> <fieldset disabled={busy}><legend>Gift details</legend><label>Gift name<input required maxLength={160} value={form.name} onChange={e=>setForm({...form,name:e.target.value})}/></label><label>Product link{method==='DIRECT_DELIVERY'?'':' · optional if you add gift details'}<input type="url" required={method==='DIRECT_DELIVERY'} pattern="https://[^\s]+" placeholder="https://…" maxLength={1000} value={form.productUrl} onChange={e=>setForm({...form,productUrl:e.target.value})}/></label><label>Gift details<textarea maxLength={500} value={form.description} onChange={e=>setForm({...form,description:e.target.value})}/></label></fieldset> <fieldset className="special-gift-methods" disabled={busy}><legend>How would you like to share your gift?</legend><label><input type="radio" name="special-gift-method" checked={method==='LINK_AND_CONTRIBUTION'} onChange={()=>setMethod('LINK_AND_CONTRIBUTION')}/><span>Share a gift link and contribute to the host</span></label><label><input type="radio" name="special-gift-method" checked={method==='DIRECT_DELIVERY'} onChange={()=>setMethod('DIRECT_DELIVERY')}/><span>I’ll purchase and deliver the gift myself</span></label></fieldset> {method==='LINK_AND_CONTRIBUTION'?<label>Contribution amount (₹)<input required type="number" inputMode="decimal" min="1" max="1000000" step="0.01" value={form.amount} onChange={e=>setForm({...form,amount:e.target.value})}/><small>Continue to send this amount directly to the host’s UPI or bank account.</small></label>:<p>After adding your gift, you can view the host delivery address, purchase from the retailer and share tracking details here.</p>} <p className="finePrint">Your contact details and contribution amount are shared only with the host.</p><button className="gc-launch" disabled={busy}>{busy?'Saving your gift…':method==='LINK_AND_CONTRIBUTION'?'Continue to host payment':'Add my special gift'}</button> </form>} {!saved&&previous.length>0&&<section className="gc-panel"><h3>Your gifts in this browser</h3>{previous.map(g=><button key={g.id} type="button" className="gc-secondary" disabled={busy} onClick={()=>{setSaved(g);setAddress(null);setTracking('');setNotice('')}}>Manage {g.name}</button>)}</section>} </>} {error&&<p role="alert" className="gc-error">{error}</p>} | (step==='choice'\|\|step==='individual') | [web/components/SpecialGiftEntry.tsx:65](../../../web/components/SpecialGiftEntry.tsx#L65) |
| UI-00894 | dialog | {children} | Parent/source state | [web/components/focused/Invitation.tsx:16](../../../web/components/focused/Invitation.tsx#L16) |
| UI-00053 | "status" | Opening your workspace… | Parent/source state | [web/app/components/HostGlobalNav.tsx:13](../../../web/app/components/HostGlobalNav.tsx#L13) |
| UI-00112 | "status" | Opening… | Parent/source state | [web/app/events/[id]/preview/page.tsx:6](../../../web/app/events/[id]/preview/page.tsx#L6) |
| UI-00115 | "status" | Preparing your celebration… | loading | [web/app/events/[id]/preview/page.tsx:40](../../../web/app/events/[id]/preview/page.tsx#L40) |
| UI-00116 | "alert" | · | error; loading | [web/app/events/[id]/preview/page.tsx:41](../../../web/app/events/[id]/preview/page.tsx#L41) |
| UI-00121 | "alert" | Event unavailable | event; error; loading | [web/app/events/[id]/preview/page.tsx:47](../../../web/app/events/[id]/preview/page.tsx#L47) |
| UI-00411 | "alert" | {error} | error; groupBase&&error.includes('switched off') | [web/components/CommercePurchase.tsx:27](../../../web/components/CommercePurchase.tsx#L27) |
| UI-00413 | "status" | {notice} | notice | [web/components/CommercePurchase.tsx:27](../../../web/components/CommercePurchase.tsx#L27) |
| UI-00444 | "status" | Opening your celebration space… | ready | [web/components/ExperienceProvider.tsx:13](../../../web/components/ExperienceProvider.tsx#L13) |
| UI-00448 | "alert" | {error} | error | [web/components/GroupActivity.tsx:7](../../../web/components/GroupActivity.tsx#L7) |
| UI-00457 | "status" | {accountNotice} | logged&&accountNotice; open | [web/components/GroupContribution.tsx:81](../../../web/components/GroupContribution.tsx#L81) |
| UI-00458 | "alert" | {error} | error; open | [web/components/GroupContribution.tsx:81](../../../web/components/GroupContribution.tsx#L81) |
| UI-00484 | "status" | {pinNotice} | pinNotice; allowPin; group; !logged; loading; open | [web/components/GroupContribution.tsx:81](../../../web/components/GroupContribution.tsx#L81) |
| UI-00531 | "status" | ✓ Email verified for account recovery | Parent/source state | [web/components/GroupEmailVerification.tsx:14](../../../web/components/GroupEmailVerification.tsx#L14) |
| UI-00538 | "status" | {notice} | notice | [web/components/GroupEmailVerification.tsx:15](../../../web/components/GroupEmailVerification.tsx#L15) |
| UI-00545 | "status" | {notice} | notice | [web/components/GroupGiftPlan.tsx:7](../../../web/components/GroupGiftPlan.tsx#L7) |
| UI-00546 | "alert" | {error} | error | [web/components/GroupGuestCheckout.tsx:21](../../../web/components/GroupGuestCheckout.tsx#L21) |
| UI-00547 | "status" |  | stage==='thanks'; !allowed; !invite | [web/components/GroupGuestCheckout.tsx:22](../../../web/components/GroupGuestCheckout.tsx#L22) |
| UI-00568 | "status" | {notice} | notice | [web/components/GroupGuestCreatorSettings.tsx:12](../../../web/components/GroupGuestCreatorSettings.tsx#L12) |
| UI-00574 | "status" | {notice} | notice | [web/components/GuestActions.tsx:11](../../../web/components/GuestActions.tsx#L11) |
| UI-00582 | "status" | {rsvpResult} | features.RSVP | [web/components/GuestCelebration.tsx:29](../../../web/components/GuestCelebration.tsx#L29) |
| UI-00589 | "status" | {wishResult} | features.GUESTBOOK | [web/components/GuestCelebration.tsx:32](../../../web/components/GuestCelebration.tsx#L32) |
| UI-00592 | "alert" | {pricingError} | pricingError; paymentMethod==='razorpay'; paymentMethod==='razorpay'&&!razorpayAllowed | [web/components/GuestContributionCheckout.tsx:108](../../../web/components/GuestContributionCheckout.tsx#L108) |
| UI-00594 | "status" | Loading payment methods… | destinationsLoading | [web/components/GuestContributionCheckout.tsx:130](../../../web/components/GuestContributionCheckout.tsx#L130) |
| UI-00595 | "alert" | {destinationsError} | destinationsError | [web/components/GuestContributionCheckout.tsx:130](../../../web/components/GuestContributionCheckout.tsx#L130) |
| UI-00598 | "status" |  | directStatus | [web/components/GuestContributionCheckout.tsx:133](../../../web/components/GuestContributionCheckout.tsx#L133) |
| UI-00600 | "status" |  | receipt; directStatus | [web/components/GuestContributionCheckout.tsx:140](../../../web/components/GuestContributionCheckout.tsx#L140) |
| UI-00603 | "status" | Loading UPI and bank details… | destinationsLoading; receipt; directStatus | [web/components/GuestContributionCheckout.tsx:142](../../../web/components/GuestContributionCheckout.tsx#L142) |
| UI-00604 | "alert" | {destinationsError} | destinationsError; receipt; directStatus | [web/components/GuestContributionCheckout.tsx:143](../../../web/components/GuestContributionCheckout.tsx#L143) |
| UI-00615 | "status" | {directStatus} | directStatus; paymentMethod!=='razorpay'; receipt | [web/components/GuestContributionCheckout.tsx:164](../../../web/components/GuestContributionCheckout.tsx#L164) |
| UI-00617 | "alert" | {error} | error; receipt; directStatus | [web/components/GuestContributionCheckout.tsx:171](../../../web/components/GuestContributionCheckout.tsx#L171) |
| UI-00620 | "status" | Loading guest gifts… | loading | [web/components/GuestGiftBoard.tsx:10](../../../web/components/GuestGiftBoard.tsx#L10) |
| UI-00621 | "alert" | {error} | error | [web/components/GuestGiftBoard.tsx:10](../../../web/components/GuestGiftBoard.tsx#L10) |
| UI-00624 | "status" | {g.display_status\|\|g.selection_label} | Parent/source state | [web/components/GuestGiftBoard.tsx:10](../../../web/components/GuestGiftBoard.tsx#L10) |
| UI-00626 | "status" | {g.display_status} | Parent/source state | [web/components/GuestGiftBoard.tsx:10](../../../web/components/GuestGiftBoard.tsx#L10) |
| UI-00634 | "alert" | {error} | error | [web/components/GuestGiftManager.tsx:15](../../../web/components/GuestGiftManager.tsx#L15) |
| UI-00635 | "status" | {notice} | notice | [web/components/GuestGiftManager.tsx:15](../../../web/components/GuestGiftManager.tsx#L15) |
| UI-00636 | "status" | Loading gifts… | loading | [web/components/GuestGiftManager.tsx:15](../../../web/components/GuestGiftManager.tsx#L15) |
| UI-00641 | "status" | Checking eligible payment destinations… | loading | [web/components/GuestVerifiedDestination.tsx:34](../../../web/components/GuestVerifiedDestination.tsx#L34) |
| UI-00642 | "alert" | {error} | error; loading | [web/components/GuestVerifiedDestination.tsx:35](../../../web/components/GuestVerifiedDestination.tsx#L35) |
| UI-00644 | "status" | {method==='upi'? 'No eligible preferred UPI QR is currently available. Please do not make a transfer yet.': 'No eligible preferred bank account is currently available. Please do not make a transfer yet.'} | eligible.length===0; error; loading | [web/components/GuestVerifiedDestination.tsx:35](../../../web/components/GuestVerifiedDestination.tsx#L35) |
| UI-00649 | "status" | {copyNotice} | copyNotice; selected?.method==='UPI'&&selected.qrPngBase64&&focused; eligible.length===0; error; loading | [web/components/GuestVerifiedDestination.tsx:44](../../../web/components/GuestVerifiedDestination.tsx#L44) |
| UI-00666 | "alert" | {error} | error | [web/components/HostWorkspace.tsx:46](../../../web/components/HostWorkspace.tsx#L46) |
| UI-00675 | "status" | {notice} | notice | [web/components/IndependentGroupPlan.tsx:17](../../../web/components/IndependentGroupPlan.tsx#L17) |
| UI-00678 | "status" | {notice} | notice | [web/components/IndependentGroupPlan.tsx:22](../../../web/components/IndependentGroupPlan.tsx#L22) |
| UI-00679 | "status" | Loading your contribution… | loading | [web/components/IndividualHostContribution.tsx:19](../../../web/components/IndividualHostContribution.tsx#L19) |
| UI-00680 | "status" |  | payment?.claim_id; loading | [web/components/IndividualHostContribution.tsx:19](../../../web/components/IndividualHostContribution.tsx#L19) |
| UI-00687 | "alert" | {error} | error | [web/components/IndividualHostContribution.tsx:23](../../../web/components/IndividualHostContribution.tsx#L23) |
| UI-00746 | "status" | Mobile number verified | verified | [web/components/MobileVerification.tsx:13](../../../web/components/MobileVerification.tsx#L13) |
| UI-00753 | "status" | {message} | message | [web/components/MobileVerification.tsx:13](../../../web/components/MobileVerification.tsx#L13) |
| UI-00808 | "status" | {notice} | notice | [web/components/SocialSharing.tsx:13](../../../web/components/SocialSharing.tsx#L13) |
| UI-00816 | "status" | {notice\|\|`Manage ${saved.name}`} | saved; step==='choice'; (step==='choice'\|\|step==='individual') | [web/components/SpecialGiftEntry.tsx:69](../../../web/components/SpecialGiftEntry.tsx#L69) |
| UI-00840 | "alert" | {error} | error; (step==='choice'\|\|step==='individual') | [web/components/SpecialGiftEntry.tsx:78](../../../web/components/SpecialGiftEntry.tsx#L78) |
| UI-00842 | "status" | {p.receipt?<><p>Gift allocation: {inr(p.receipt.giftPaise)}</p><p>Celebration allocation: {inr(p.receipt.walletPaise)}</p><p className="finePrint">Payment ID: {p.receipt.paymentId}</p></>:<><p>{inr(amount)} · {p.method==='upi'?'UPI':'Bank transfer'}</p><p className="finePrint">Submission reference: {p.reported.split('Reference: ')[1]\|\|'See the host for an update'}</p></>} {p.close&&<button className="focusPrimary" onClick={p.close}>Back to the celebration</button>} | p.receipt\|\|p.reported | [web/components/focused/Checkout.tsx:7](../../../web/components/focused/Checkout.tsx#L7) |
| UI-00858 | "alert" | {p.error} | p.error; p.receipt\|\|p.reported | [web/components/focused/Checkout.tsx:7](../../../web/components/focused/Checkout.tsx#L7) |
| UI-00897 | "status" | Progress is temporarily unavailable. | progressState==='error' | [web/components/focused/Invitation.tsx:26](../../../web/components/focused/Invitation.tsx#L26) |


### Workflow state bindings

| Source | Binding | Initial expression |
| --- | --- | --- |
| web/app/events/[id]/preview/page.tsx | loading | true |
| web/app/events/[id]/preview/page.tsx | error | '' |
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
| Header | Shared identity/event or page title/back/actions, where declared. Composite header declarations: 3. |
| Navigation / sidebar | Host/admin navigation and event context where applicable; mobile/focused variants are conditional. nav=4, aside=3. |
| Main content | Purpose-specific forms/cards/tables/canvas/player from the composed sources. main=4, section=6, article=0. |
| Search and filters | Fields/selects listed below; exact value/handler/visibility controls are not inferred from placeholder labels. |
| Data grids and cards | Table declarations=0. Data collections and generated rows/cards are in frontend.json dynamicSources. |
| Popups / dialogs | Named/source-declared dialog control records below; opening/closing/loading/apply/cancel actions belong to those component handlers. |
| Alerts / notifications | Source controls with role alert/status are listed below. Notice strings may come from API results; do not assume a universal envelope. |
| Footer | Local/shared footer declarations=1. No undocumented legal/footer wording is invented. |
| Loading / empty / success / error | Observed state bindings and relevant action fields are listed below. Expected QA states from Document 2 do not establish a spinner/retry control where source does not implement one. |


### Composed implementation

- [web/app/events/[id]/qr/page.tsx:1](../../../web/app/events/[id]/qr/page.tsx#L1)
- [web/components/ExperienceProvider.tsx:1](../../../web/components/ExperienceProvider.tsx#L1)
- [web/components/InvitationEditor.tsx:1](../../../web/components/InvitationEditor.tsx#L1)
- [web/components/AiInvitationDesigner.tsx:1](../../../web/components/AiInvitationDesigner.tsx#L1)
- [web/app/layout.tsx:1](../../../web/app/layout.tsx#L1)
- [web/components/VerificationProvider.tsx:1](../../../web/components/VerificationProvider.tsx#L1)
- [web/app/components/PerformanceDiagnostics.tsx:1](../../../web/app/components/PerformanceDiagnostics.tsx#L1)
- [web/app/components/HostSessionRedirect.tsx:1](../../../web/app/components/HostSessionRedirect.tsx#L1)
- [web/app/components/HostGlobalNav.tsx:1](../../../web/app/components/HostGlobalNav.tsx#L1)
- [web/components/HostWorkspace.tsx:1](../../../web/components/HostWorkspace.tsx#L1)
- [web/components/focused/HostShell.tsx:1](../../../web/components/focused/HostShell.tsx#L1)

### Fields

| Control ID | Label / expression | Type | Binding | Required | Native limits / format | Detailed specification |
| --- | --- | --- | --- | --- | --- | --- |
| UI-00137 | "QR theme" | select  | {details.theme} | Not declared |  | [control](UI_CONTROLS.md#ui-00137) |
| UI-00394 | Celebration | select  | {selected} | true |  | [control](UI_CONTROLS.md#ui-00394) |
| UI-00395 | Visual style | select  | {form.visualStyle} | Not declared |  | [control](UI_CONTROLS.md#ui-00395) |
| UI-00396 | Tone | select  | {form.tone} | Not declared |  | [control](UI_CONTROLS.md#ui-00396) |
| UI-00397 | Language | input  | {form.language} | true | maxLength={40} | [control](UI_CONTROLS.md#ui-00397) |
| UI-00398 | Names shown on the invitation | input  | {form.displayNames} | Not declared | maxLength={200} | [control](UI_CONTROLS.md#ui-00398) |
| UI-00399 | Public venue | input  | {form.publicVenue} | Not declared | maxLength={160} | [control](UI_CONTROLS.md#ui-00399) |
| UI-00400 | Time | input  | {form.startTime} | Not declared | maxLength={40} | [control](UI_CONTROLS.md#ui-00400) |
| UI-00401 | A personal touch | textarea  | {form.specialMessage} | Not declared | maxLength={400} | [control](UI_CONTROLS.md#ui-00401) |
| UI-00699 | No static label; inspect component context | input "file" |  | Not declared |  | [control](UI_CONTROLS.md#ui-00699) |
| UI-00703 | Occasion | select  | {occasion} | Not declared |  | [control](UI_CONTROLS.md#ui-00703) |
| UI-00704 | Event name | input  | {eventTitle} | Not declared | maxLength={200} | [control](UI_CONTROLS.md#ui-00704) |
| UI-00714 | Collection | select  | {galleryCategory} | Not declared |  | [control](UI_CONTROLS.md#ui-00714) |
| UI-00716 | Zoom | select  | {scale} | Not declared |  | [control](UI_CONTROLS.md#ui-00716) |
| UI-00717 | Message | textarea  | {active.text} | Not declared |  | [control](UI_CONTROLS.md#ui-00717) |
| UI-00718 | Text color | input "color" | {active.color} | Not declared |  | [control](UI_CONTROLS.md#ui-00718) |
| UI-00719 | Font style | select  | {active.fontFamily\|\|"Georgia, serif"} | Not declared |  | [control](UI_CONTROLS.md#ui-00719) |
| UI-00720 | Font size | input "range" | {active.fontSize} | Not declared | min="12"; max="70" | [control](UI_CONTROLS.md#ui-00720) |
| UI-00721 | Alignment | select  | {active.align} | Not declared |  | [control](UI_CONTROLS.md#ui-00721) |
| UI-00722 | {({x:'Left',y:'Top',w:'Width',h:'Height'} as Record<string,string>)[k]} | input "number" | {active[k as 'x'\|'y'\|'w'\|'h']} | Not declared |  | [control](UI_CONTROLS.md#ui-00722) |
| UI-00725 | Background zoom: {Math.round(normalizeBackgroundTransform(design.backgroundTransform).zoom*100)} % | input "range" | {Math.round(normalizeBackgroundTransform(design.backgroundTransform).zoom*100)} | Not declared | min="100"; max="300"; step="5" | [control](UI_CONTROLS.md#ui-00725) |
| UI-00727 | Upload photo | input "file" |  | Not declared |  | [control](UI_CONTROLS.md#ui-00727) |
| UI-00728 | Background color | input "color" | {design.background} | Not declared |  | [control](UI_CONTROLS.md#ui-00728) |
| UI-00730 | Celebration | select  | {eventId} | Not declared |  | [control](UI_CONTROLS.md#ui-00730) |
| UI-00731 | No static label; inspect component context | input  | {eventId} | Not declared |  | [control](UI_CONTROLS.md#ui-00731) |


### Buttons

| ID | Label / expression | Action | Submit validation / handler | Source |
| --- | --- | --- | --- | --- |
| UI-00135 | Copy invitation link | {copyLink} | Not a native form submit or inherited from component context | [web/app/events/[id]/qr/page.tsx:171](../../../web/app/events/[id]/qr/page.tsx#L171) |
| UI-00136 | Download QR PNG | {() => void downloadQr()} | Not a native form submit or inherited from component context | [web/app/events/[id]/qr/page.tsx:175](../../../web/app/events/[id]/qr/page.tsx#L175) |
| UI-00391 |  | {onClose} | Not a native form submit or inherited from component context | [web/components/AiInvitationDesigner.tsx:27](../../../web/components/AiInvitationDesigner.tsx#L27) |
| UI-00406 | Retry QR | {()=>setQrRetry(v=>v+1)} | Not a native form submit or inherited from component context | [web/components/AiInvitationDesigner.tsx:27](../../../web/components/AiInvitationDesigner.tsx#L27) |
| UI-00407 | Use this design | {()=>{if(qrUrl){onApply(preview!,suggestion);onClose()}}} | Not a native form submit or inherited from component context | [web/components/AiInvitationDesigner.tsx:27](../../../web/components/AiInvitationDesigner.tsx#L27) |
| UI-00408 | Keep my current design | {onClose} | Not a native form submit or inherited from component context | [web/components/AiInvitationDesigner.tsx:27](../../../web/components/AiInvitationDesigner.tsx#L27) |
| UI-00409 | {busy?'Designing your invitation…':suggestion?'Generate another design':'Generate invitation'} | submit / component action | Not a native form submit or inherited from component context | [web/components/AiInvitationDesigner.tsx:27](../../../web/components/AiInvitationDesigner.tsx#L27) |
| UI-00664 | {collapsed?<PanelLeftOpen size={20}/>:<PanelLeftClose size={20}/>} | {toggle} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:43](../../../web/components/HostWorkspace.tsx#L43) |
| UI-00667 |  | {()=>void signOut()} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:46](../../../web/components/HostWorkspace.tsx#L46) |
| UI-00668 |  | {()=>setCollapsed(true)} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:47](../../../web/components/HostWorkspace.tsx#L47) |
| UI-00694 | Design with AI | {()=>setAiOpen(true)} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:176](../../../web/components/InvitationEditor.tsx#L176) |
| UI-00695 | Download PNG | {()=>void downloadArtwork()} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:177](../../../web/components/InvitationEditor.tsx#L177) |
| UI-00696 |  | {undo} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:179](../../../web/components/InvitationEditor.tsx#L179) |
| UI-00697 |  | {redo} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:179](../../../web/components/InvitationEditor.tsx#L179) |
| UI-00698 | Import draft | {()=>importInput.current?.click()} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:183](../../../web/components/InvitationEditor.tsx#L183) |
| UI-00700 | Export draft | {()=>{exportDraft();if(moreMenu.current)moreMenu.current.open=false}} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:184](../../../web/components/InvitationEditor.tsx#L184) |
| UI-00701 | Reset invitation to default | {()=>{if(moreMenu.current)moreMenu.current.open=false;resetDefault()}} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:185](../../../web/components/InvitationEditor.tsx#L185) |
| UI-00702 | {label} | {()=>setToolPanel(value)} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:190](../../../web/components/InvitationEditor.tsx#L190) |
| UI-00705 | Add text or tagline | {()=>{const next=addText(design);commit(next);setSelected(next.elements.at(-1)!.id)}} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:190](../../../web/components/InvitationEditor.tsx#L190) |
| UI-00706 | {e.type==='qr'?<Gift size={16}/>:<Type size={16}/>} | {()=>setSelected(e.id)} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:190](../../../web/components/InvitationEditor.tsx#L190) |
| UI-00707 |  | {()=>insertSticker(sticker.id)} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:195](../../../web/components/InvitationEditor.tsx#L195) |
| UI-00709 |  | {()=>insertSticker(platformDecorationId(a.id),!!linkedEventId)} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:219](../../../web/components/InvitationEditor.tsx#L219) |
| UI-00713 |  | {()=>commit({...design,backgroundImage:src})} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:262](../../../web/components/InvitationEditor.tsx#L262) |
| UI-00715 |  | {()=>commit({...design,backgroundImage:b.src})} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:318](../../../web/components/InvitationEditor.tsx#L318) |
| UI-00723 | Delete element | {()=>{commit(deleteElement(design,active.id));setSelected('qr')}} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:371](../../../web/components/InvitationEditor.tsx#L371) |
| UI-00724 | {editBackground?'Finish positioning':'Edit background — drag to position'} | {()=>setEditBackground(v=>!v)} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:372](../../../web/components/InvitationEditor.tsx#L372) |
| UI-00726 | Reset background position | {()=>backgroundChange(resetBackgroundTransform())} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:383](../../../web/components/InvitationEditor.tsx#L383) |
| UI-00729 | Remove background photo | {()=>commit({...design,backgroundImage:''})} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:388](../../../web/components/InvitationEditor.tsx#L388) |
| UI-00732 | Load authenticated QR | {()=>void loadQr()} | Not a native form submit or inherited from component context | [web/components/InvitationEditor.tsx:388](../../../web/components/InvitationEditor.tsx#L388) |


### Dialog and alert/status inventory

| ID | Role / kind | Text / label | Visibility conditions | Source |
| --- | --- | --- | --- | --- |
| UI-00132 | native-dialog | 'Invitation link copied!' | Parent/source state | [web/app/events/[id]/qr/page.tsx:110](../../../web/app/events/[id]/qr/page.tsx#L110) |
| UI-00390 | dialog |  | Parent/source state | [web/components/AiInvitationDesigner.tsx:27](../../../web/components/AiInvitationDesigner.tsx#L27) |
| UI-00688 | native-dialog | 'Choose a PNG, JPG or WebP under 3 MB.' | Parent/source state | [web/components/InvitationEditor.tsx:115](../../../web/components/InvitationEditor.tsx#L115) |
| UI-00689 | native-dialog | 'Image dimensions must be at most 4096 pixels.' | Parent/source state | [web/components/InvitationEditor.tsx:115](../../../web/components/InvitationEditor.tsx#L115) |
| UI-00690 | native-dialog | 'Reset the invitation design to its default template? This will discard your current visual changes, but will not change event details, gifts, or the event QR.' | Parent/source state | [web/components/InvitationEditor.tsx:116](../../../web/components/InvitationEditor.tsx#L116) |
| UI-00691 | native-dialog | 'Invalid invitation draft' | Parent/source state | [web/components/InvitationEditor.tsx:119](../../../web/components/InvitationEditor.tsx#L119) |
| UI-00692 | native-dialog | 'Enter a valid event UUID' | Parent/source state | [web/components/InvitationEditor.tsx:120](../../../web/components/InvitationEditor.tsx#L120) |
| UI-00693 | native-dialog | `Could not load QR: ${String(e)}. Backend authentication is required.` | Parent/source state | [web/components/InvitationEditor.tsx:120](../../../web/components/InvitationEditor.tsx#L120) |
| UI-00053 | "status" | Opening your workspace… | Parent/source state | [web/app/components/HostGlobalNav.tsx:13](../../../web/app/components/HostGlobalNav.tsx#L13) |
| UI-00134 | "alert" | {error} | error | [web/app/events/[id]/qr/page.tsx:150](../../../web/app/events/[id]/qr/page.tsx#L150) |
| UI-00402 | "status" | Checking AI availability… | checking | [web/components/AiInvitationDesigner.tsx:27](../../../web/components/AiInvitationDesigner.tsx#L27) |
| UI-00403 | "status" | {setup} | !checking&&selected&&!available&&setup | [web/components/AiInvitationDesigner.tsx:27](../../../web/components/AiInvitationDesigner.tsx#L27) |
| UI-00404 | "alert" | {error} | error | [web/components/AiInvitationDesigner.tsx:27](../../../web/components/AiInvitationDesigner.tsx#L27) |
| UI-00405 | "alert" | {qrError} | qrError; preview&&suggestion | [web/components/AiInvitationDesigner.tsx:27](../../../web/components/AiInvitationDesigner.tsx#L27) |
| UI-00444 | "status" | Opening your celebration space… | ready | [web/components/ExperienceProvider.tsx:13](../../../web/components/ExperienceProvider.tsx#L13) |
| UI-00666 | "alert" | {error} | error | [web/components/HostWorkspace.tsx:46](../../../web/components/HostWorkspace.tsx#L46) |
| UI-00711 | "alert" | {platformError} | platformError | [web/components/InvitationEditor.tsx:247](../../../web/components/InvitationEditor.tsx#L247) |


### Workflow state bindings

| Source | Binding | Initial expression |
| --- | --- | --- |
| web/app/events/[id]/qr/page.tsx | error | '' |
| web/app/events/[id]/qr/page.tsx | busy | false |
| web/components/InvitationEditor.tsx | aiOpen | false |
| web/components/InvitationEditor.tsx | platformError | '' |
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
| Header | Shared identity/event or page title/back/actions, where declared. Composite header declarations: 2. |
| Navigation / sidebar | Host/admin navigation and event context where applicable; mobile/focused variants are conditional. nav=2, aside=1. |
| Main content | Purpose-specific forms/cards/tables/canvas/player from the composed sources. main=3, section=8, article=2. |
| Search and filters | Fields/selects listed below; exact value/handler/visibility controls are not inferred from placeholder labels. |
| Data grids and cards | Table declarations=0. Data collections and generated rows/cards are in frontend.json dynamicSources. |
| Popups / dialogs | Named/source-declared dialog control records below; opening/closing/loading/apply/cancel actions belong to those component handlers. |
| Alerts / notifications | Source controls with role alert/status are listed below. Notice strings may come from API results; do not assume a universal envelope. |
| Footer | Local/shared footer declarations=0. No undocumented legal/footer wording is invented. |
| Loading / empty / success / error | Observed state bindings and relevant action fields are listed below. Expected QA states from Document 2 do not establish a spinner/retry control where source does not implement one. |


### Composed implementation

- [web/app/events/[id]/receiving-accounts/page.tsx:1](../../../web/app/events/[id]/receiving-accounts/page.tsx#L1)
- [web/app/host/payment-settings/page.tsx:1](../../../web/app/host/payment-settings/page.tsx#L1)
- [web/components/ReceivingAccountHealth.tsx:1](../../../web/components/ReceivingAccountHealth.tsx#L1)
- [web/components/ExperienceProvider.tsx:1](../../../web/components/ExperienceProvider.tsx#L1)
- [web/app/layout.tsx:1](../../../web/app/layout.tsx#L1)
- [web/components/VerificationProvider.tsx:1](../../../web/components/VerificationProvider.tsx#L1)
- [web/app/components/PerformanceDiagnostics.tsx:1](../../../web/app/components/PerformanceDiagnostics.tsx#L1)
- [web/app/components/HostSessionRedirect.tsx:1](../../../web/app/components/HostSessionRedirect.tsx#L1)
- [web/app/components/HostGlobalNav.tsx:1](../../../web/app/components/HostGlobalNav.tsx#L1)
- [web/components/HostWorkspace.tsx:1](../../../web/components/HostWorkspace.tsx#L1)
- [web/components/focused/HostShell.tsx:1](../../../web/components/focused/HostShell.tsx#L1)

### Fields

| Control ID | Label / expression | Type | Binding | Required | Native limits / format | Detailed specification |
| --- | --- | --- | --- | --- | --- | --- |
| UI-00298 | Host receiving account | select  | {existingAccount} | true |  | [control](UI_CONTROLS.md#ui-00298) |
| UI-00301 | Related host or event | select  | {upiEvent} | Not declared |  | [control](UI_CONTROLS.md#ui-00301) |
| UI-00302 | Account name / tag (optional) | input  | {upiLabel} | Not declared | maxLength={50} | [control](UI_CONTROLS.md#ui-00302) |
| UI-00303 | UPI ID | input  | {upi} | true |  | [control](UI_CONTROLS.md#ui-00303) |
| UI-00314 | "Upload UPI QR PNG" | input "file" |  | Not declared |  | [control](UI_CONTROLS.md#ui-00314) |
| UI-00320 | Related host or event | select  | {bankEvent} | Not declared |  | [control](UI_CONTROLS.md#ui-00320) |
| UI-00321 | Account name / tag (optional) | input  | {bankLabel} | Not declared | maxLength={50} | [control](UI_CONTROLS.md#ui-00321) |
| UI-00322 | Account holder name | input  | {holder} | true |  | [control](UI_CONTROLS.md#ui-00322) |
| UI-00323 | Account number | input "password" | {number} | true |  | [control](UI_CONTROLS.md#ui-00323) |
| UI-00324 | IFSC | input  | {ifsc} | true | maxLength={11} | [control](UI_CONTROLS.md#ui-00324) |


### Buttons

| ID | Label / expression | Action | Submit validation / handler | Source |
| --- | --- | --- | --- | --- |
| UI-00292 | × | {()=>setError('')} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:65](../../../web/app/host/payment-settings/page.tsx#L65) |
| UI-00294 | × | {()=>setNotice('')} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:65](../../../web/app/host/payment-settings/page.tsx#L65) |
| UI-00296 | Refresh | {()=>void load()} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:66](../../../web/app/host/payment-settings/page.tsx#L66) |
| UI-00299 | Link to this event | submit / component action | {e=>{e.preventDefault();const [kind,id]=existingAccount.split(':');if(id)void action(async()=>{await paymentProfiles.linkToEvent(authFetch,eventId,kind,id);setExistingAccount('')},'Account linked to this event. Select Preferred to make it ready for publishing.')}} | [web/app/host/payment-settings/page.tsx:67](../../../web/app/host/payment-settings/page.tsx#L67) |
| UI-00304 | Add UPI ID | submit / component action | {e=>{e.preventDefault();void action(async()=>{await paymentProfiles.addUpi(authFetch,upi,undefined,upiLabel,upiEvent);setUpi('');setUpiLabel('');},'UPI ID saved. Upload or generate its QR, then confirm your details.');}} | [web/app/host/payment-settings/page.tsx:67](../../../web/app/host/payment-settings/page.tsx#L67) |
| UI-00305 | Verify {verification?.environment==='SANDBOX'?'in sandbox':'account'} | {()=>void verifyAccount(u.id,'upi')} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:67](../../../web/app/host/payment-settings/page.tsx#L67) |
| UI-00306 | Verification details | {()=>void verificationHistory(u.id,'upi')} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:67](../../../web/app/host/payment-settings/page.tsx#L67) |
| UI-00308 | Confirm my details | {()=>void confirmDestination(u.id,'upi')} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:67](../../../web/app/host/payment-settings/page.tsx#L67) |
| UI-00309 | {u.preferred\|\|u.isPreferred?'✓ Preferred':!u.qrReady?'QR required':(u.verificationStatus==='VERIFIED'\|\|u.hostConfirmed)?'Set preferred':'Confirm details first'} | {()=>void action(()=>paymentProfiles.preferUpi(authFetch,u.id,undefined,eventId),'Preferred destination updated.')} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:67](../../../web/app/host/payment-settings/page.tsx#L67) |
| UI-00310 | Enable | {()=>void action(()=>paymentProfiles.enableUpi(authFetch,u.id,undefined,eventId),'UPI ID enabled. Independent verification is still required.')} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:67](../../../web/app/host/payment-settings/page.tsx#L67) |
| UI-00311 | Disable | {()=>void action(()=>paymentProfiles.disableUpi(authFetch,u.id,undefined,eventId),'UPI ID disabled.')} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:67](../../../web/app/host/payment-settings/page.tsx#L67) |
| UI-00312 | Generate QR | {()=>void viewQr(u.id,'generate')} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:67](../../../web/app/host/payment-settings/page.tsx#L67) |
| UI-00313 | View uploaded QR | {()=>void viewQr(u.id,'uploaded')} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:67](../../../web/app/host/payment-settings/page.tsx#L67) |
| UI-00315 | Rename | {()=>{const value=window.prompt('Account name / tag (maximum 50 characters)',u.accountLabel\|\|'');if(value!==null)void action(()=>paymentProfiles.rename(authFetch,'upi',u.id,value),'UPI account name updated.');}} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:67](../../../web/app/host/payment-settings/page.tsx#L67) |
| UI-00317 | {eventId?'Remove from event':'Delete'} | {()=>{if(window.confirm(eventId?'Remove this account from this event? Shared host accounts will remain saved.':`Archive ${u.accountLabel\|\|u.masked\|\|'this UPI ID'}? It will disappear from payment choices. Historical records will be retained.`))void action(()=>paymentProfiles.archive(authFetch,'upi',u.id,undefined,eventId),eventId?'Account removed from this event.':'UPI account archived.');}} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:67](../../../web/app/host/payment-settings/page.tsx#L67) |
| UI-00325 | Add bank account | submit / component action | {e=>{e.preventDefault();void action(async()=>{await paymentProfiles.addBank(authFetch,{accountNumber:number,ifsc,accountHolderName:holder,accountLabel:bankLabel,eventId:bankEvent\|\|null});setBankLabel('');setNumber('');setIfsc('');setHolder('');},'Bank account saved. Confirm the details; independent verification is required before guest use.');}} | [web/app/host/payment-settings/page.tsx:68](../../../web/app/host/payment-settings/page.tsx#L68) |
| UI-00326 | Verify {verification?.environment==='SANDBOX'?'in sandbox':'account'} | {()=>void verifyAccount(b.id,'bank')} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:68](../../../web/app/host/payment-settings/page.tsx#L68) |
| UI-00327 | Verification details | {()=>void verificationHistory(b.id,'bank')} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:68](../../../web/app/host/payment-settings/page.tsx#L68) |
| UI-00328 | Confirm my details | {()=>void confirmDestination(b.id,'banks')} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:68](../../../web/app/host/payment-settings/page.tsx#L68) |
| UI-00329 | {b.preferred\|\|b.isPreferred?'✓ Preferred':(b.verificationStatus==='VERIFIED'\|\|b.hostConfirmed)?'Set preferred':'Confirm details first'} | {()=>void action(()=>paymentProfiles.preferBank(authFetch,b.id,undefined,eventId),'Preferred destination updated.')} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:68](../../../web/app/host/payment-settings/page.tsx#L68) |
| UI-00330 | Enable | {()=>void action(()=>paymentProfiles.enableBank(authFetch,b.id,undefined,eventId),'Bank account enabled. Independent verification is still required.')} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:68](../../../web/app/host/payment-settings/page.tsx#L68) |
| UI-00331 | Disable | {()=>void action(()=>paymentProfiles.disableBank(authFetch,b.id,undefined,eventId),'Bank account disabled.')} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:68](../../../web/app/host/payment-settings/page.tsx#L68) |
| UI-00332 | Rename | {()=>{const value=window.prompt('Account name / tag (maximum 50 characters)',b.accountLabel\|\|'');if(value!==null)void action(()=>paymentProfiles.rename(authFetch,'banks',b.id,value),'Bank account name updated.');}} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:68](../../../web/app/host/payment-settings/page.tsx#L68) |
| UI-00334 | {eventId?'Remove from event':'Delete'} | {()=>{if(window.confirm(eventId?'Remove this account from this event? Shared host accounts will remain saved.':`Archive ${b.accountLabel\|\|maskedBank(b)}? It will disappear from payment choices. Historical records will be retained.`))void action(()=>paymentProfiles.archive(authFetch,'banks',b.id,undefined,eventId),eventId?'Account removed from this event.':'Bank account archived.');}} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:68](../../../web/app/host/payment-settings/page.tsx#L68) |
| UI-00337 | Close QR | {closeQr} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:69](../../../web/app/host/payment-settings/page.tsx#L69) |
| UI-00664 | {collapsed?<PanelLeftOpen size={20}/>:<PanelLeftClose size={20}/>} | {toggle} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:43](../../../web/components/HostWorkspace.tsx#L43) |
| UI-00667 |  | {()=>void signOut()} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:46](../../../web/components/HostWorkspace.tsx#L46) |
| UI-00668 |  | {()=>setCollapsed(true)} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:47](../../../web/components/HostWorkspace.tsx#L47) |
| UI-00784 | Refresh check | {()=>setRetry(v=>v+1)} | Not a native form submit or inherited from component context | [web/components/ReceivingAccountHealth.tsx:10](../../../web/components/ReceivingAccountHealth.tsx#L10) |


### Dialog and alert/status inventory

| ID | Role / kind | Text / label | Visibility conditions | Source |
| --- | --- | --- | --- | --- |
| UI-00286 | native-dialog | 'Account holder name exactly as registered with the bank' | Parent/source state | [web/app/host/payment-settings/page.tsx:38](../../../web/app/host/payment-settings/page.tsx#L38) |
| UI-00287 | native-dialog | `Send these account details to Cashfree for ${verification?.environment\|\|'sandbox'} verification?` | Parent/source state | [web/app/host/payment-settings/page.tsx:39](../../../web/app/host/payment-settings/page.tsx#L39) |
| UI-00288 | native-dialog | 'I have checked this destination and confirm it belongs to me. This does not independently verify ownership or enable guest transfers.' | Parent/source state | [web/app/host/payment-settings/page.tsx:56](../../../web/app/host/payment-settings/page.tsx#L56) |
| UI-00316 | native-dialog | 'Account name / tag (maximum 50 characters)' | upis.length===0; loading | [web/app/host/payment-settings/page.tsx:67](../../../web/app/host/payment-settings/page.tsx#L67) |
| UI-00318 | native-dialog | eventId?'Remove this account from this event? Shared host accounts will remain saved.':`Archive ${u.accountLabel\|\|u.masked\|\|'this UPI ID'}? It will disappear from payment choices. Historical records will be retained.` | upis.length===0; loading | [web/app/host/payment-settings/page.tsx:67](../../../web/app/host/payment-settings/page.tsx#L67) |
| UI-00333 | native-dialog | 'Account name / tag (maximum 50 characters)' | banks.length===0; loading | [web/app/host/payment-settings/page.tsx:68](../../../web/app/host/payment-settings/page.tsx#L68) |
| UI-00335 | native-dialog | eventId?'Remove this account from this event? Shared host accounts will remain saved.':`Archive ${b.accountLabel\|\|maskedBank(b)}? It will disappear from payment choices. Historical records will be retained.` | banks.length===0; loading | [web/app/host/payment-settings/page.tsx:68](../../../web/app/host/payment-settings/page.tsx#L68) |
| UI-00336 | "dialog" | "UPI QR preview" | qr | [web/app/host/payment-settings/page.tsx:69](../../../web/app/host/payment-settings/page.tsx#L69) |
| UI-00053 | "status" | Opening your workspace… | Parent/source state | [web/app/components/HostGlobalNav.tsx:13](../../../web/app/components/HostGlobalNav.tsx#L13) |
| UI-00291 | "alert" | {error} | error | [web/app/host/payment-settings/page.tsx:65](../../../web/app/host/payment-settings/page.tsx#L65) |
| UI-00293 | "status" | {notice} | notice | [web/app/host/payment-settings/page.tsx:65](../../../web/app/host/payment-settings/page.tsx#L65) |
| UI-00295 | "status" | {verificationDetails} | verificationDetails | [web/app/host/payment-settings/page.tsx:66](../../../web/app/host/payment-settings/page.tsx#L66) |
| UI-00307 | "status" | Upload or generate a valid QR before confirming or selecting Preferred. | !u.qrReady; upis.length===0; loading | [web/app/host/payment-settings/page.tsx:67](../../../web/app/host/payment-settings/page.tsx#L67) |
| UI-00444 | "status" | Opening your celebration space… | ready | [web/components/ExperienceProvider.tsx:13](../../../web/components/ExperienceProvider.tsx#L13) |
| UI-00666 | "alert" | {error} | error | [web/components/HostWorkspace.tsx:46](../../../web/components/HostWorkspace.tsx#L46) |
| UI-00785 | "status" | Checking linked accounts… | loading | [web/components/ReceivingAccountHealth.tsx:10](../../../web/components/ReceivingAccountHealth.tsx#L10) |
| UI-00786 | "alert" | {error} | error; loading | [web/components/ReceivingAccountHealth.tsx:10](../../../web/components/ReceivingAccountHealth.tsx#L10) |


### Workflow state bindings

| Source | Binding | Initial expression |
| --- | --- | --- |
| web/app/host/payment-settings/page.tsx | busy | false |
| web/app/host/payment-settings/page.tsx | loading | true |
| web/app/host/payment-settings/page.tsx | error | '' |
| web/app/host/payment-settings/page.tsx | notice | '' |
| web/app/host/payment-settings/page.tsx | uploadedQr | {} |
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
| Header | Shared identity/event or page title/back/actions, where declared. Composite header declarations: 1. |
| Navigation / sidebar | Host/admin navigation and event context where applicable; mobile/focused variants are conditional. nav=2, aside=1. |
| Main content | Purpose-specific forms/cards/tables/canvas/player from the composed sources. main=3, section=2, article=0. |
| Search and filters | Fields/selects listed below; exact value/handler/visibility controls are not inferred from placeholder labels. |
| Data grids and cards | Table declarations=0. Data collections and generated rows/cards are in frontend.json dynamicSources. |
| Popups / dialogs | Named/source-declared dialog control records below; opening/closing/loading/apply/cancel actions belong to those component handlers. |
| Alerts / notifications | Source controls with role alert/status are listed below. Notice strings may come from API results; do not assume a universal envelope. |
| Footer | Local/shared footer declarations=0. No undocumented legal/footer wording is invented. |
| Loading / empty / success / error | Observed state bindings and relevant action fields are listed below. Expected QA states from Document 2 do not establish a spinner/retry control where source does not implement one. |


### Composed implementation

- [web/app/events/[id]/thank-you/page.tsx:1](../../../web/app/events/[id]/thank-you/page.tsx#L1)
- [web/components/ExperienceProvider.tsx:1](../../../web/components/ExperienceProvider.tsx#L1)
- [web/app/layout.tsx:1](../../../web/app/layout.tsx#L1)
- [web/components/VerificationProvider.tsx:1](../../../web/components/VerificationProvider.tsx#L1)
- [web/app/components/PerformanceDiagnostics.tsx:1](../../../web/app/components/PerformanceDiagnostics.tsx#L1)
- [web/app/components/HostSessionRedirect.tsx:1](../../../web/app/components/HostSessionRedirect.tsx#L1)
- [web/app/components/HostGlobalNav.tsx:1](../../../web/app/components/HostGlobalNav.tsx#L1)
- [web/components/HostWorkspace.tsx:1](../../../web/components/HostWorkspace.tsx#L1)
- [web/components/focused/HostShell.tsx:1](../../../web/components/focused/HostShell.tsx#L1)

### Fields

| Control ID | Label / expression | Type | Binding | Required | Native limits / format | Detailed specification |
| --- | --- | --- | --- | --- | --- | --- |
| UI-00140 | Card signature / host display name | input  | {settings.hostName} | Not declared | maxLength={200} | [control](UI_CONTROLS.md#ui-00140) |
| UI-00141 | Email subject | input  | {settings.emailSubject} | Not declared | maxLength={200} | [control](UI_CONTROLS.md#ui-00141) |
| UI-00142 | Email message | textarea  | {settings.emailBody} | Not declared | maxLength={10000} | [control](UI_CONTROLS.md#ui-00142) |
| UI-00143 | SMS message | textarea  | {settings.smsBody} | Not declared | maxLength={1000} | [control](UI_CONTROLS.md#ui-00143) |
| UI-00144 | Card theme | select  | {settings.cardTemplate} | Not declared |  | [control](UI_CONTROLS.md#ui-00144) |
| UI-00145 | Queue acknowledgments automatically | input "checkbox" | {settings.autoSend} | Not declared |  | [control](UI_CONTROLS.md#ui-00145) |
| UI-00146 | Queue guest email acknowledgments | input "checkbox" | {settings.emailEnabled} | Not declared |  | [control](UI_CONTROLS.md#ui-00146) |


### Buttons

| ID | Label / expression | Action | Submit validation / handler | Source |
| --- | --- | --- | --- | --- |
| UI-00147 | {saving ? 'Saving…' : 'Save design'} | {() => void save()} | Not a native form submit or inherited from component context | [web/app/events/[id]/thank-you/page.tsx:99](../../../web/app/events/[id]/thank-you/page.tsx#L99) |
| UI-00664 | {collapsed?<PanelLeftOpen size={20}/>:<PanelLeftClose size={20}/>} | {toggle} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:43](../../../web/components/HostWorkspace.tsx#L43) |
| UI-00667 |  | {()=>void signOut()} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:46](../../../web/components/HostWorkspace.tsx#L46) |
| UI-00668 |  | {()=>setCollapsed(true)} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:47](../../../web/components/HostWorkspace.tsx#L47) |


### Dialog and alert/status inventory

| ID | Role / kind | Text / label | Visibility conditions | Source |
| --- | --- | --- | --- | --- |
| UI-00053 | "status" | Opening your workspace… | Parent/source state | [web/app/components/HostGlobalNav.tsx:13](../../../web/app/components/HostGlobalNav.tsx#L13) |
| UI-00139 | "status" | {notice} | notice | [web/app/events/[id]/thank-you/page.tsx:72](../../../web/app/events/[id]/thank-you/page.tsx#L72) |
| UI-00444 | "status" | Opening your celebration space… | ready | [web/components/ExperienceProvider.tsx:13](../../../web/components/ExperienceProvider.tsx#L13) |
| UI-00666 | "alert" | {error} | error | [web/components/HostWorkspace.tsx:46](../../../web/components/HostWorkspace.tsx#L46) |


### Workflow state bindings

| Source | Binding | Initial expression |
| --- | --- | --- |
| web/app/events/[id]/thank-you/page.tsx | notice | '' |
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
| Header | Shared identity/event or page title/back/actions, where declared. Composite header declarations: 2. |
| Navigation / sidebar | Host/admin navigation and event context where applicable; mobile/focused variants are conditional. nav=2, aside=1. |
| Main content | Purpose-specific forms/cards/tables/canvas/player from the composed sources. main=3, section=2, article=2. |
| Search and filters | Fields/selects listed below; exact value/handler/visibility controls are not inferred from placeholder labels. |
| Data grids and cards | Table declarations=0. Data collections and generated rows/cards are in frontend.json dynamicSources. |
| Popups / dialogs | Named/source-declared dialog control records below; opening/closing/loading/apply/cancel actions belong to those component handlers. |
| Alerts / notifications | Source controls with role alert/status are listed below. Notice strings may come from API results; do not assume a universal envelope. |
| Footer | Local/shared footer declarations=0. No undocumented legal/footer wording is invented. |
| Loading / empty / success / error | Observed state bindings and relevant action fields are listed below. Expected QA states from Document 2 do not establish a spinner/retry control where source does not implement one. |


### Composed implementation

- [web/app/events/[id]/transactions/page.tsx:1](../../../web/app/events/[id]/transactions/page.tsx#L1)
- [web/components/HostPaymentBreakdown.tsx:1](../../../web/components/HostPaymentBreakdown.tsx#L1)
- [web/components/CommercePurchase.tsx:1](../../../web/components/CommercePurchase.tsx#L1)
- [web/app/layout.tsx:1](../../../web/app/layout.tsx#L1)
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


### Buttons

| ID | Label / expression | Action | Submit validation / handler | Source |
| --- | --- | --- | --- | --- |
| UI-00151 | Refresh | {()=>void load()} | Not a native form submit or inherited from component context | [web/app/events/[id]/transactions/page.tsx:16](../../../web/app/events/[id]/transactions/page.tsx#L16) |
| UI-00153 | Recheck provider payment | {()=>void verify(row)} | Not a native form submit or inherited from component context | [web/app/events/[id]/transactions/page.tsx:16](../../../web/app/events/[id]/transactions/page.tsx#L16) |
| UI-00154 | Previous | {()=>setOffset(n=>Math.max(0,n-20))} | Not a native form submit or inherited from component context | [web/app/events/[id]/transactions/page.tsx:16](../../../web/app/events/[id]/transactions/page.tsx#L16) |
| UI-00155 | Next | {()=>setOffset(n=>n+20)} | Not a native form submit or inherited from component context | [web/app/events/[id]/transactions/page.tsx:16](../../../web/app/events/[id]/transactions/page.tsx#L16) |
| UI-00410 | {busy?'Processing…':offer.amountPaise===0?'Activate access':`${offer.entitled?'Renew':'Purchase'} · ${commercialMoney(offer.amountPaise)} (test mode)`} | {()=>void pay()} | Not a native form submit or inherited from component context | [web/components/CommercePurchase.tsx:27](../../../web/components/CommercePurchase.tsx#L27) |
| UI-00412 | Reload price | {()=>{setRetry(v=>v+1)}} | Not a native form submit or inherited from component context | [web/components/CommercePurchase.tsx:27](../../../web/components/CommercePurchase.tsx#L27) |
| UI-00661 | Previous breakdowns | {()=>setOffset(v=>Math.max(0,v-20))} | Not a native form submit or inherited from component context | [web/components/HostPaymentBreakdown.tsx:8](../../../web/components/HostPaymentBreakdown.tsx#L8) |
| UI-00662 | Next breakdowns | {()=>setOffset(v=>v+20)} | Not a native form submit or inherited from component context | [web/components/HostPaymentBreakdown.tsx:8](../../../web/components/HostPaymentBreakdown.tsx#L8) |
| UI-00664 | {collapsed?<PanelLeftOpen size={20}/>:<PanelLeftClose size={20}/>} | {toggle} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:43](../../../web/components/HostWorkspace.tsx#L43) |
| UI-00667 |  | {()=>void signOut()} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:46](../../../web/components/HostWorkspace.tsx#L46) |
| UI-00668 |  | {()=>setCollapsed(true)} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:47](../../../web/components/HostWorkspace.tsx#L47) |


### Dialog and alert/status inventory

| ID | Role / kind | Text / label | Visibility conditions | Source |
| --- | --- | --- | --- | --- |
| UI-00053 | "status" | Opening your workspace… | Parent/source state | [web/app/components/HostGlobalNav.tsx:13](../../../web/app/components/HostGlobalNav.tsx#L13) |
| UI-00149 | "alert" | {error} | error | [web/app/events/[id]/transactions/page.tsx:16](../../../web/app/events/[id]/transactions/page.tsx#L16) |
| UI-00150 | "status" | {message} | message | [web/app/events/[id]/transactions/page.tsx:16](../../../web/app/events/[id]/transactions/page.tsx#L16) |
| UI-00152 | "status" | Loading transactions… | loading | [web/app/events/[id]/transactions/page.tsx:16](../../../web/app/events/[id]/transactions/page.tsx#L16) |
| UI-00411 | "alert" | {error} | error; groupBase&&error.includes('switched off') | [web/components/CommercePurchase.tsx:27](../../../web/components/CommercePurchase.tsx#L27) |
| UI-00413 | "status" | {notice} | notice | [web/components/CommercePurchase.tsx:27](../../../web/components/CommercePurchase.tsx#L27) |
| UI-00444 | "status" | Opening your celebration space… | ready | [web/components/ExperienceProvider.tsx:13](../../../web/components/ExperienceProvider.tsx#L13) |
| UI-00660 | "alert" | {error} | error | [web/components/HostPaymentBreakdown.tsx:8](../../../web/components/HostPaymentBreakdown.tsx#L8) |
| UI-00666 | "alert" | {error} | error | [web/components/HostWorkspace.tsx:46](../../../web/components/HostWorkspace.tsx#L46) |


### Workflow state bindings

| Source | Binding | Initial expression |
| --- | --- | --- |
| web/app/events/[id]/transactions/page.tsx | loading | true |
| web/app/events/[id]/transactions/page.tsx | busy | false |
| web/app/events/[id]/transactions/page.tsx | error | '' |
| web/app/events/[id]/transactions/page.tsx | message | '' |
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
| Header | Shared identity/event or page title/back/actions, where declared. Composite header declarations: 2. |
| Navigation / sidebar | Host/admin navigation and event context where applicable; mobile/focused variants are conditional. nav=2, aside=1. |
| Main content | Purpose-specific forms/cards/tables/canvas/player from the composed sources. main=4, section=2, article=0. |
| Search and filters | Fields/selects listed below; exact value/handler/visibility controls are not inferred from placeholder labels. |
| Data grids and cards | Table declarations=0. Data collections and generated rows/cards are in frontend.json dynamicSources. |
| Popups / dialogs | Named/source-declared dialog control records below; opening/closing/loading/apply/cancel actions belong to those component handlers. |
| Alerts / notifications | Source controls with role alert/status are listed below. Notice strings may come from API results; do not assume a universal envelope. |
| Footer | Local/shared footer declarations=0. No undocumented legal/footer wording is invented. |
| Loading / empty / success / error | Observed state bindings and relevant action fields are listed below. Expected QA states from Document 2 do not establish a spinner/retry control where source does not implement one. |


### Composed implementation

- [web/app/events/new/page.tsx:1](../../../web/app/events/new/page.tsx#L1)
- [web/components/DeliveryAddressFields.tsx:1](../../../web/components/DeliveryAddressFields.tsx#L1)
- [web/components/focused/FocusedOnly.tsx:1](../../../web/components/focused/FocusedOnly.tsx#L1)
- [web/components/ExperienceProvider.tsx:1](../../../web/components/ExperienceProvider.tsx#L1)
- [web/app/layout.tsx:1](../../../web/app/layout.tsx#L1)
- [web/components/VerificationProvider.tsx:1](../../../web/components/VerificationProvider.tsx#L1)
- [web/app/components/PerformanceDiagnostics.tsx:1](../../../web/app/components/PerformanceDiagnostics.tsx#L1)
- [web/app/components/HostSessionRedirect.tsx:1](../../../web/app/components/HostSessionRedirect.tsx#L1)
- [web/app/components/HostGlobalNav.tsx:1](../../../web/app/components/HostGlobalNav.tsx#L1)
- [web/components/HostWorkspace.tsx:1](../../../web/components/HostWorkspace.tsx#L1)
- [web/components/focused/HostShell.tsx:1](../../../web/components/focused/HostShell.tsx#L1)

### Fields

| Control ID | Label / expression | Type | Binding | Required | Native limits / format | Detailed specification |
| --- | --- | --- | --- | --- | --- | --- |
| UI-00157 | {label} | input  | {form[key]} | Not declared |  | [control](UI_CONTROLS.md#ui-00157) |
| UI-00163 | A few words for your guests | textarea  | {form.description} | Not declared | maxLength={10000} | [control](UI_CONTROLS.md#ui-00163) |
| UI-00442 | Same as event address | input "checkbox" | {same} | Not declared |  | [control](UI_CONTROLS.md#ui-00442) |
| UI-00443 | {label} | input  | {address[key]} | true | maxLength={maxLength} | [control](UI_CONTROLS.md#ui-00443) |


### Buttons

| ID | Label / expression | Action | Submit validation / handler | Source |
| --- | --- | --- | --- | --- |
| UI-00162 |  | {()=>setForm(f=>({...f,occasionType:occasion,title:f.title\|\|`Our ${occasion.toLowerCase().replaceAll('_',' ')}`}))} | {step===4?e=>{e.preventDefault();void create()}:next} | [web/app/events/new/page.tsx:22](../../../web/app/events/new/page.tsx#L22) |
| UI-00166 |  | {()=>field('theme',value)} | {step===4?e=>{e.preventDefault();void create()}:next} | [web/app/events/new/page.tsx:22](../../../web/app/events/new/page.tsx#L22) |
| UI-00167 | Edit details | {()=>setStep(1)} | {step===4?e=>{e.preventDefault();void create()}:next} | [web/app/events/new/page.tsx:22](../../../web/app/events/new/page.tsx#L22) |
| UI-00168 | Back | {()=>setStep(s=>s-1)} | {step===4?e=>{e.preventDefault();void create()}:next} | [web/app/events/new/page.tsx:22](../../../web/app/events/new/page.tsx#L22) |
| UI-00169 | {busy?'Creating…':step===4?'Create celebration':'Continue →'} | submit / component action | {step===4?e=>{e.preventDefault();void create()}:next} | [web/app/events/new/page.tsx:22](../../../web/app/events/new/page.tsx#L22) |
| UI-00664 | {collapsed?<PanelLeftOpen size={20}/>:<PanelLeftClose size={20}/>} | {toggle} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:43](../../../web/components/HostWorkspace.tsx#L43) |
| UI-00667 |  | {()=>void signOut()} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:46](../../../web/components/HostWorkspace.tsx#L46) |
| UI-00668 |  | {()=>setCollapsed(true)} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:47](../../../web/components/HostWorkspace.tsx#L47) |


### Dialog and alert/status inventory

| ID | Role / kind | Text / label | Visibility conditions | Source |
| --- | --- | --- | --- | --- |
| UI-00053 | "status" | Opening your workspace… | Parent/source state | [web/app/components/HostGlobalNav.tsx:13](../../../web/app/components/HostGlobalNav.tsx#L13) |
| UI-00159 | "alert" | {error} | error | [web/app/events/new/page.tsx:22](../../../web/app/events/new/page.tsx#L22) |
| UI-00160 | "status" | Preparing your space… | !ready | [web/app/events/new/page.tsx:22](../../../web/app/events/new/page.tsx#L22) |
| UI-00444 | "status" | Opening your celebration space… | ready | [web/components/ExperienceProvider.tsx:13](../../../web/components/ExperienceProvider.tsx#L13) |
| UI-00666 | "alert" | {error} | error | [web/components/HostWorkspace.tsx:46](../../../web/components/HostWorkspace.tsx#L46) |
| UI-00893 | "status" | Opening your celebrations… | mode==='FOCUSED' | [web/components/focused/FocusedOnly.tsx:5](../../../web/components/focused/FocusedOnly.tsx#L5) |


### Workflow state bindings

| Source | Binding | Initial expression |
| --- | --- | --- |
| web/app/events/new/page.tsx | step | 0 |
| web/app/events/new/page.tsx | busy | false |
| web/app/events/new/page.tsx | error | '' |
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
| Header | Shared identity/event or page title/back/actions, where declared. Composite header declarations: 1. |
| Navigation / sidebar | Host/admin navigation and event context where applicable; mobile/focused variants are conditional. nav=2, aside=1. |
| Main content | Purpose-specific forms/cards/tables/canvas/player from the composed sources. main=4, section=1, article=0. |
| Search and filters | Fields/selects listed below; exact value/handler/visibility controls are not inferred from placeholder labels. |
| Data grids and cards | Table declarations=0. Data collections and generated rows/cards are in frontend.json dynamicSources. |
| Popups / dialogs | Named/source-declared dialog control records below; opening/closing/loading/apply/cancel actions belong to those component handlers. |
| Alerts / notifications | Source controls with role alert/status are listed below. Notice strings may come from API results; do not assume a universal envelope. |
| Footer | Local/shared footer declarations=0. No undocumented legal/footer wording is invented. |
| Loading / empty / success / error | Observed state bindings and relevant action fields are listed below. Expected QA states from Document 2 do not establish a spinner/retry control where source does not implement one. |


### Composed implementation

- [web/app/forgot-password/page.tsx:1](../../../web/app/forgot-password/page.tsx#L1)
- [web/components/VerificationProvider.tsx:1](../../../web/components/VerificationProvider.tsx#L1)
- [web/app/layout.tsx:1](../../../web/app/layout.tsx#L1)
- [web/components/ExperienceProvider.tsx:1](../../../web/components/ExperienceProvider.tsx#L1)
- [web/app/components/PerformanceDiagnostics.tsx:1](../../../web/app/components/PerformanceDiagnostics.tsx#L1)
- [web/app/components/HostSessionRedirect.tsx:1](../../../web/app/components/HostSessionRedirect.tsx#L1)
- [web/app/components/HostGlobalNav.tsx:1](../../../web/app/components/HostGlobalNav.tsx#L1)
- [web/components/HostWorkspace.tsx:1](../../../web/components/HostWorkspace.tsx#L1)
- [web/components/focused/HostShell.tsx:1](../../../web/components/focused/HostShell.tsx#L1)

### Fields

| Control ID | Label / expression | Type | Binding | Required | Native limits / format | Detailed specification |
| --- | --- | --- | --- | --- | --- | --- |
| UI-00207 | Account type | select  | {kind} | Not declared |  | [control](UI_CONTROLS.md#ui-00207) |
| UI-00208 | Verified email | input "email" | {identifier} | true |  | [control](UI_CONTROLS.md#ui-00208) |
| UI-00209 | Recover | select  | {purpose} | Not declared |  | [control](UI_CONTROLS.md#ui-00209) |
| UI-00210 | Six-digit email code | input  | {code} | true | pattern="[0-9]{6}" | [control](UI_CONTROLS.md#ui-00210) |
| UI-00211 | New {purpose==='RESET_PIN'?'PIN':'password'} | input "password" | {newCredential} | true |  | [control](UI_CONTROLS.md#ui-00211) |
| UI-00212 | Confirm | input "password" | {confirmCredential} | true |  | [control](UI_CONTROLS.md#ui-00212) |


### Buttons

| ID | Label / expression | Action | Submit validation / handler | Source |
| --- | --- | --- | --- | --- |
| UI-00213 | {busy?'Please wait…':challengeId?'Reset credential':'Send recovery code'} | submit / component action | {challengeId?complete:request} | [web/app/forgot-password/page.tsx:17](../../../web/app/forgot-password/page.tsx#L17) |
| UI-00664 | {collapsed?<PanelLeftOpen size={20}/>:<PanelLeftClose size={20}/>} | {toggle} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:43](../../../web/components/HostWorkspace.tsx#L43) |
| UI-00667 |  | {()=>void signOut()} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:46](../../../web/components/HostWorkspace.tsx#L46) |
| UI-00668 |  | {()=>setCollapsed(true)} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:47](../../../web/components/HostWorkspace.tsx#L47) |


### Dialog and alert/status inventory

| ID | Role / kind | Text / label | Visibility conditions | Source |
| --- | --- | --- | --- | --- |
| UI-00053 | "status" | Opening your workspace… | Parent/source state | [web/app/components/HostGlobalNav.tsx:13](../../../web/app/components/HostGlobalNav.tsx#L13) |
| UI-00214 | "status" | {message} | message | [web/app/forgot-password/page.tsx:17](../../../web/app/forgot-password/page.tsx#L17) |
| UI-00444 | "status" | Opening your celebration space… | ready | [web/components/ExperienceProvider.tsx:13](../../../web/components/ExperienceProvider.tsx#L13) |
| UI-00666 | "alert" | {error} | error | [web/components/HostWorkspace.tsx:46](../../../web/components/HostWorkspace.tsx#L46) |


### Workflow state bindings

| Source | Binding | Initial expression |
| --- | --- | --- |
| web/app/forgot-password/page.tsx | message | '' |
| web/app/forgot-password/page.tsx | busy | false |
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
| Header | Shared identity/event or page title/back/actions, where declared. Composite header declarations: 1. |
| Navigation / sidebar | Host/admin navigation and event context where applicable; mobile/focused variants are conditional. nav=2, aside=1. |
| Main content | Purpose-specific forms/cards/tables/canvas/player from the composed sources. main=4, section=2, article=0. |
| Search and filters | Fields/selects listed below; exact value/handler/visibility controls are not inferred from placeholder labels. |
| Data grids and cards | Table declarations=0. Data collections and generated rows/cards are in frontend.json dynamicSources. |
| Popups / dialogs | Named/source-declared dialog control records below; opening/closing/loading/apply/cancel actions belong to those component handlers. |
| Alerts / notifications | Source controls with role alert/status are listed below. Notice strings may come from API results; do not assume a universal envelope. |
| Footer | Local/shared footer declarations=0. No undocumented legal/footer wording is invented. |
| Loading / empty / success / error | Observed state bindings and relevant action fields are listed below. Expected QA states from Document 2 do not establish a spinner/retry control where source does not implement one. |


### Composed implementation

- [web/app/forgot-username/page.tsx:1](../../../web/app/forgot-username/page.tsx#L1)
- [web/components/VerificationProvider.tsx:1](../../../web/components/VerificationProvider.tsx#L1)
- [web/app/layout.tsx:1](../../../web/app/layout.tsx#L1)
- [web/components/ExperienceProvider.tsx:1](../../../web/components/ExperienceProvider.tsx#L1)
- [web/app/components/PerformanceDiagnostics.tsx:1](../../../web/app/components/PerformanceDiagnostics.tsx#L1)
- [web/app/components/HostSessionRedirect.tsx:1](../../../web/app/components/HostSessionRedirect.tsx#L1)
- [web/app/components/HostGlobalNav.tsx:1](../../../web/app/components/HostGlobalNav.tsx#L1)
- [web/components/HostWorkspace.tsx:1](../../../web/components/HostWorkspace.tsx#L1)
- [web/components/focused/HostShell.tsx:1](../../../web/components/focused/HostShell.tsx#L1)

### Fields

| Control ID | Label / expression | Type | Binding | Required | Native limits / format | Detailed specification |
| --- | --- | --- | --- | --- | --- | --- |
| UI-00218 | Your account | select  | {kind} | Not declared |  | [control](UI_CONTROLS.md#ui-00218) |
| UI-00219 | Verified email address | input "email" | {email} | true | maxLength={254} | [control](UI_CONTROLS.md#ui-00219) |


### Buttons

| ID | Label / expression | Action | Submit validation / handler | Source |
| --- | --- | --- | --- | --- |
| UI-00220 | {busy?'Sending request…':'Email my username'} | submit / component action | {submit} | [web/app/forgot-username/page.tsx:13](../../../web/app/forgot-username/page.tsx#L13) |
| UI-00664 | {collapsed?<PanelLeftOpen size={20}/>:<PanelLeftClose size={20}/>} | {toggle} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:43](../../../web/components/HostWorkspace.tsx#L43) |
| UI-00667 |  | {()=>void signOut()} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:46](../../../web/components/HostWorkspace.tsx#L46) |
| UI-00668 |  | {()=>setCollapsed(true)} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:47](../../../web/components/HostWorkspace.tsx#L47) |


### Dialog and alert/status inventory

| ID | Role / kind | Text / label | Visibility conditions | Source |
| --- | --- | --- | --- | --- |
| UI-00053 | "status" | Opening your workspace… | Parent/source state | [web/app/components/HostGlobalNav.tsx:13](../../../web/app/components/HostGlobalNav.tsx#L13) |
| UI-00221 | "status" |  | message | [web/app/forgot-username/page.tsx:13](../../../web/app/forgot-username/page.tsx#L13) |
| UI-00222 | "alert" | {error} | error | [web/app/forgot-username/page.tsx:13](../../../web/app/forgot-username/page.tsx#L13) |
| UI-00444 | "status" | Opening your celebration space… | ready | [web/components/ExperienceProvider.tsx:13](../../../web/components/ExperienceProvider.tsx#L13) |
| UI-00666 | "alert" | {error} | error | [web/components/HostWorkspace.tsx:46](../../../web/components/HostWorkspace.tsx#L46) |


### Workflow state bindings

| Source | Binding | Initial expression |
| --- | --- | --- |
| web/app/forgot-username/page.tsx | busy | false |
| web/app/forgot-username/page.tsx | message | '' |
| web/app/forgot-username/page.tsx | error | '' |
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
| Header | Shared identity/event or page title/back/actions, where declared. Composite header declarations: 3. |
| Navigation / sidebar | Host/admin navigation and event context where applicable; mobile/focused variants are conditional. nav=2, aside=1. |
| Main content | Purpose-specific forms/cards/tables/canvas/player from the composed sources. main=4, section=2, article=1. |
| Search and filters | Fields/selects listed below; exact value/handler/visibility controls are not inferred from placeholder labels. |
| Data grids and cards | Table declarations=0. Data collections and generated rows/cards are in frontend.json dynamicSources. |
| Popups / dialogs | Named/source-declared dialog control records below; opening/closing/loading/apply/cancel actions belong to those component handlers. |
| Alerts / notifications | Source controls with role alert/status are listed below. Notice strings may come from API results; do not assume a universal envelope. |
| Footer | Local/shared footer declarations=0. No undocumented legal/footer wording is invented. |
| Loading / empty / success / error | Observed state bindings and relevant action fields are listed below. Expected QA states from Document 2 do not establish a spinner/retry control where source does not implement one. |


### Composed implementation

- [web/app/gifts/page.tsx:1](../../../web/app/gifts/page.tsx#L1)
- [web/components/GiftDescription.tsx:1](../../../web/components/GiftDescription.tsx#L1)
- [web/app/components/HostToast.tsx:1](../../../web/app/components/HostToast.tsx#L1)
- [web/app/layout.tsx:1](../../../web/app/layout.tsx#L1)
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
| UI-00225 | Choose a celebration | select  |  | Not declared |  | [control](UI_CONTROLS.md#ui-00225) |
| UI-00230 | Retailer product HTTPS link (optional) | input "url" | {form.productUrl} | Not declared |  | [control](UI_CONTROLS.md#ui-00230) |
| UI-00232 | Gift name | input  | {form.name} | true | maxLength={160} | [control](UI_CONTROLS.md#ui-00232) |
| UI-00233 | Target amount (₹) | input "number" | {form.targetAmount} | true | min="0.01"; max="10000000"; step="0.01" | [control](UI_CONTROLS.md#ui-00233) |
| UI-00234 | Description | textarea  | {form.description} | Not declared | maxLength={2000} | [control](UI_CONTROLS.md#ui-00234) |
| UI-00235 | Gift photo (optional) | input "file" |  | Not declared |  | [control](UI_CONTROLS.md#ui-00235) |
| UI-00240 | Gift priority | select  | {form.priority} | Not declared |  | [control](UI_CONTROLS.md#ui-00240) |
| UI-00241 | Visibility | select  | {form.status} | Not declared |  | [control](UI_CONTROLS.md#ui-00241) |
| UI-00245 | "Search gifts" | input  | {search} | Not declared |  | [control](UI_CONTROLS.md#ui-00245) |
| UI-00253 | No static label; inspect component context | input "file" |  | Not declared |  | [control](UI_CONTROLS.md#ui-00253) |


### Buttons

| ID | Label / expression | Action | Submit validation / handler | Source |
| --- | --- | --- | --- | --- |
| UI-00059 | × | {()=>{setFlash(null);onDismiss?.();}} | Not a native form submit or inherited from component context | [web/app/components/HostToast.tsx:12](../../../web/app/components/HostToast.tsx#L12) |
| UI-00231 | {fetchingRetailer?'Checking product…':'Validate product link & add details'} | {()=>void fetchRetailer(true).catch(e=>setError(message(e,'Retailer lookup failed')))} | {e=>void save(e)} | [web/app/gifts/page.tsx:81](../../../web/app/gifts/page.tsx#L81) |
| UI-00238 | Remove photo | {()=>{setForm(f=>({...f,imageKey:''}));setManualPhoto(true)}} | {e=>void save(e)} | [web/app/gifts/page.tsx:82](../../../web/app/gifts/page.tsx#L82) |
| UI-00239 | Use retailer photo instead | {()=>{setForm(f=>({...f,imageKey:retailer.imageKey\|\|''}));setManualPhoto(false)}} | {e=>void save(e)} | [web/app/gifts/page.tsx:82](../../../web/app/gifts/page.tsx#L82) |
| UI-00242 | {busy?'Saving…':photoBusy?'Checking photo…':editing?'Save gift':'Create gift'} | submit / component action | {e=>void save(e)} | [web/app/gifts/page.tsx:83](../../../web/app/gifts/page.tsx#L83) |
| UI-00243 | Cancel edit | {reset} | {e=>void save(e)} | [web/app/gifts/page.tsx:83](../../../web/app/gifts/page.tsx#L83) |
| UI-00244 | Refresh | {()=>void load(eventId)} | Not a native form submit or inherited from component context | [web/app/gifts/page.tsx:84](../../../web/app/gifts/page.tsx#L84) |
| UI-00248 | Edit | {()=>edit(g)} | Not a native form submit or inherited from component context | [web/app/gifts/page.tsx:84](../../../web/app/gifts/page.tsx#L84) |
| UI-00249 | {g.status==='PUBLIC'?'Unpublish':'Publish'} | {()=>void changeVisibility(g)} | Not a native form submit or inherited from component context | [web/app/gifts/page.tsx:84](../../../web/app/gifts/page.tsx#L84) |
| UI-00250 | Cancel gift | {()=>void cancel(g)} | Not a native form submit or inherited from component context | [web/app/gifts/page.tsx:84](../../../web/app/gifts/page.tsx#L84) |
| UI-00251 | Export wishlist | {()=>download(items,'gift-wishlist-backup.json')} | Not a native form submit or inherited from component context | [web/app/gifts/page.tsx:85](../../../web/app/gifts/page.tsx#L85) |
| UI-00252 | Import backup | {()=>importInput.current?.click()} | Not a native form submit or inherited from component context | [web/app/gifts/page.tsx:85](../../../web/app/gifts/page.tsx#L85) |
| UI-00254 | Export older drafts | {()=>download(localDrafts,'gift-platform-local-drafts-backup.json')} | Not a native form submit or inherited from component context | [web/app/gifts/page.tsx:85](../../../web/app/gifts/page.tsx#L85) |
| UI-00255 | Review older drafts | {()=>setImportDrafts(localDrafts)} | Not a native form submit or inherited from component context | [web/app/gifts/page.tsx:85](../../../web/app/gifts/page.tsx#L85) |
| UI-00256 | Import gift | {()=>void importGift(g)} | Not a native form submit or inherited from component context | [web/app/gifts/page.tsx:85](../../../web/app/gifts/page.tsx#L85) |
| UI-00257 | Skip | {()=>setImportDrafts(current=>current.filter(row=>row.id!==g.id))} | Not a native form submit or inherited from component context | [web/app/gifts/page.tsx:85](../../../web/app/gifts/page.tsx#L85) |
| UI-00445 | View full description | {()=>dialog.current?.showModal()} | Not a native form submit or inherited from component context | [web/components/GiftDescription.tsx:9](../../../web/components/GiftDescription.tsx#L9) |
| UI-00447 | Close × | {()=>dialog.current?.close()} | Not a native form submit or inherited from component context | [web/components/GiftDescription.tsx:9](../../../web/components/GiftDescription.tsx#L9) |
| UI-00664 | {collapsed?<PanelLeftOpen size={20}/>:<PanelLeftClose size={20}/>} | {toggle} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:43](../../../web/components/HostWorkspace.tsx#L43) |
| UI-00667 |  | {()=>void signOut()} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:46](../../../web/components/HostWorkspace.tsx#L46) |
| UI-00668 |  | {()=>setCollapsed(true)} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:47](../../../web/components/HostWorkspace.tsx#L47) |


### Dialog and alert/status inventory

| ID | Role / kind | Text / label | Visibility conditions | Source |
| --- | --- | --- | --- | --- |
| UI-00223 | native-dialog | `Cancel ${g.name}?` | Parent/source state | [web/app/gifts/page.tsx:74](../../../web/app/gifts/page.tsx#L74) |
| UI-00446 | dialog |  | Parent/source state | [web/components/GiftDescription.tsx:9](../../../web/components/GiftDescription.tsx#L9) |
| UI-00053 | "status" | Opening your workspace… | Parent/source state | [web/app/components/HostGlobalNav.tsx:13](../../../web/app/components/HostGlobalNav.tsx#L13) |
| UI-00226 | "alert" | {error} | error | [web/app/gifts/page.tsx:80](../../../web/app/gifts/page.tsx#L80) |
| UI-00227 | "status" | {notice} | notice | [web/app/gifts/page.tsx:80](../../../web/app/gifts/page.tsx#L80) |
| UI-00236 | "alert" | {photoError} | photoError; eventId | [web/app/gifts/page.tsx:82](../../../web/app/gifts/page.tsx#L82) |
| UI-00237 | "status" | {photoStatus} | photoStatus; eventId | [web/app/gifts/page.tsx:82](../../../web/app/gifts/page.tsx#L82) |
| UI-00246 | "status" | Loading gifts… | loading | [web/app/gifts/page.tsx:84](../../../web/app/gifts/page.tsx#L84) |
| UI-00444 | "status" | Opening your celebration space… | ready | [web/components/ExperienceProvider.tsx:13](../../../web/components/ExperienceProvider.tsx#L13) |
| UI-00666 | "alert" | {error} | error | [web/components/HostWorkspace.tsx:46](../../../web/components/HostWorkspace.tsx#L46) |


### Workflow state bindings

| Source | Binding | Initial expression |
| --- | --- | --- |
| web/app/gifts/page.tsx | loading | false |
| web/app/gifts/page.tsx | busy | false |
| web/app/gifts/page.tsx | error | '' |
| web/app/gifts/page.tsx | notice | '' |
| web/app/gifts/page.tsx | photoBusy | false |
| web/app/gifts/page.tsx | photoError | '' |
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
| Header | Shared identity/event or page title/back/actions, where declared. Composite header declarations: 1. |
| Navigation / sidebar | Host/admin navigation and event context where applicable; mobile/focused variants are conditional. nav=3, aside=1. |
| Main content | Purpose-specific forms/cards/tables/canvas/player from the composed sources. main=5, section=3, article=0. |
| Search and filters | Fields/selects listed below; exact value/handler/visibility controls are not inferred from placeholder labels. |
| Data grids and cards | Table declarations=0. Data collections and generated rows/cards are in frontend.json dynamicSources. |
| Popups / dialogs | Named/source-declared dialog control records below; opening/closing/loading/apply/cancel actions belong to those component handlers. |
| Alerts / notifications | Source controls with role alert/status are listed below. Notice strings may come from API results; do not assume a universal envelope. |
| Footer | Local/shared footer declarations=0. No undocumented legal/footer wording is invented. |
| Loading / empty / success / error | Observed state bindings and relevant action fields are listed below. Expected QA states from Document 2 do not establish a spinner/retry control where source does not implement one. |


### Composed implementation

- [web/app/groups/[token]/[groupId]/page.tsx:1](../../../web/app/groups/[token]/[groupId]/page.tsx#L1)
- [web/components/VerificationProvider.tsx:1](../../../web/components/VerificationProvider.tsx#L1)
- [web/components/SignupEmailVerification.tsx:1](../../../web/components/SignupEmailVerification.tsx#L1)
- [web/components/SignupEmailField.tsx:1](../../../web/components/SignupEmailField.tsx#L1)
- [web/components/GroupEmailVerification.tsx:1](../../../web/components/GroupEmailVerification.tsx#L1)
- [web/components/GroupMobileVerification.tsx:1](../../../web/components/GroupMobileVerification.tsx#L1)
- [web/components/MobileVerification.tsx:1](../../../web/components/MobileVerification.tsx#L1)
- [web/components/ExperienceProvider.tsx:1](../../../web/components/ExperienceProvider.tsx#L1)
- [web/components/GroupGuestCheckout.tsx:1](../../../web/components/GroupGuestCheckout.tsx#L1)
- [web/app/layout.tsx:1](../../../web/app/layout.tsx#L1)
- [web/app/components/PerformanceDiagnostics.tsx:1](../../../web/app/components/PerformanceDiagnostics.tsx#L1)
- [web/app/components/HostSessionRedirect.tsx:1](../../../web/app/components/HostSessionRedirect.tsx#L1)
- [web/app/components/HostGlobalNav.tsx:1](../../../web/app/components/HostGlobalNav.tsx#L1)
- [web/components/HostWorkspace.tsx:1](../../../web/components/HostWorkspace.tsx#L1)
- [web/components/focused/HostShell.tsx:1](../../../web/components/focused/HostShell.tsx#L1)

### Fields

| Control ID | Label / expression | Type | Binding | Required | Native limits / format | Detailed specification |
| --- | --- | --- | --- | --- | --- | --- |
| UI-00268 | {mode==='join'?'Your name / username (unique within this group)':'Username (the name you joined with)'} | input  | {name} | true | minLength={2}; maxLength={120} | [control](UI_CONTROLS.md#ui-00268) |
| UI-00269 | Sign in with | select  | {loginMethod} | Not declared |  | [control](UI_CONTROLS.md#ui-00269) |
| UI-00270 | Password | input "password" | {password} | true | minLength={10} | [control](UI_CONTROLS.md#ui-00270) |
| UI-00271 | Email address (optional unless verification is required) | input "email" | {email} | {emailVerificationEnabled&&group.memberEmailVerificationRequired} |  | [control](UI_CONTROLS.md#ui-00271) |
| UI-00272 | Mobile number | input "tel" | {mobile} | true |  | [control](UI_CONTROLS.md#ui-00272) |
| UI-00273 | Confirm password | input "password" | {confirmPassword} | true | minLength={10} | [control](UI_CONTROLS.md#ui-00273) |
| UI-00274 | Six-digit PIN | input "password" | {pin} | true | maxLength={6}; pattern="[0-9]{6}" | [control](UI_CONTROLS.md#ui-00274) |
| UI-00275 | Confirm PIN | input "password" | {confirmPin} | true | maxLength={6}; pattern="[0-9]{6}" | [control](UI_CONTROLS.md#ui-00275) |
| UI-00276 | Six-digit PIN | input "password" | {pin} | true | maxLength={6}; pattern="[0-9]{6}" | [control](UI_CONTROLS.md#ui-00276) |
| UI-00533 | Email address | input "email" | {email} | true | maxLength={254} | [control](UI_CONTROLS.md#ui-00533) |
| UI-00536 | Six-digit code | input  | {code} | true | maxLength={6}; pattern="[0-9]{6}" | [control](UI_CONTROLS.md#ui-00536) |
| UI-00550 | Your contribution amount (₹) | input  | {amount} | true |  | [control](UI_CONTROLS.md#ui-00550) |
| UI-00551 | Your name * | input  | {name} | true | minLength={2}; maxLength={120} | [control](UI_CONTROLS.md#ui-00551) |
| UI-00552 | Mobile or email * | input  | {contact} | true | maxLength={120} | [control](UI_CONTROLS.md#ui-00552) |
| UI-00556 | Transaction reference / UTR * | input  | {reference} | true | minLength={6}; maxLength={120} | [control](UI_CONTROLS.md#ui-00556) |
| UI-00749 | Mobile number with country code | input "tel" | {mobile} | true | pattern="\+[1-9][0-9]{7,14}" | [control](UI_CONTROLS.md#ui-00749) |
| UI-00750 | Six-digit verification code | input  | {code} | true | maxLength={6}; pattern="[0-9]{6}" | [control](UI_CONTROLS.md#ui-00750) |
| UI-00795 | Email address | input "email" | {value} | true | maxLength={254} | [control](UI_CONTROLS.md#ui-00795) |
| UI-00798 | Six-digit email code | input  | {code} | Not declared | maxLength={6} | [control](UI_CONTROLS.md#ui-00798) |


### Buttons

| ID | Label / expression | Action | Submit validation / handler | Source |
| --- | --- | --- | --- | --- |
| UI-00259 | Contribute to the gift | {()=>setSection('contribute')} | Not a native form submit or inherited from component context | [web/app/groups/[token]/[groupId]/page.tsx:29](../../../web/app/groups/[token]/[groupId]/page.tsx#L29) |
| UI-00260 | Join / Member sign in | {()=>setSection('membership')} | Not a native form submit or inherited from component context | [web/app/groups/[token]/[groupId]/page.tsx:29](../../../web/app/groups/[token]/[groupId]/page.tsx#L29) |
| UI-00261 | Member sign out | {async()=>{try{await api(`/member-logout/${groupId}`,{});setView(null);setMode('login');setMessage('Signed out of your member account.')}catch{setMessage('Unable to sign out. Please try again.')}}} | Not a native form submit or inherited from component context | [web/app/groups/[token]/[groupId]/page.tsx:30](../../../web/app/groups/[token]/[groupId]/page.tsx#L30) |
| UI-00263 | Join group | {()=>setMode('join')} | Not a native form submit or inherited from component context | [web/app/groups/[token]/[groupId]/page.tsx:30](../../../web/app/groups/[token]/[groupId]/page.tsx#L30) |
| UI-00264 | Member sign in | {()=>setMode('login')} | Not a native form submit or inherited from component context | [web/app/groups/[token]/[groupId]/page.tsx:30](../../../web/app/groups/[token]/[groupId]/page.tsx#L30) |
| UI-00277 | {busy?'Please wait…':mode==='join'?'Request to join':'Sign in'} | submit / component action | {submit} | [web/app/groups/[token]/[groupId]/page.tsx:30](../../../web/app/groups/[token]/[groupId]/page.tsx#L30) |
| UI-00534 | Send verification code | submit / component action | {start} | [web/components/GroupEmailVerification.tsx:15](../../../web/components/GroupEmailVerification.tsx#L15) |
| UI-00537 | Verify email | submit / component action | {finish} | [web/components/GroupEmailVerification.tsx:15](../../../web/components/GroupEmailVerification.tsx#L15) |
| UI-00548 | Make another contribution | {()=>{setStage('start');setReference('');setSubmissionKey(crypto.randomUUID());setDetails(null)}} | Not a native form submit or inherited from component context | [web/components/GroupGuestCheckout.tsx:22](../../../web/components/GroupGuestCheckout.tsx#L22) |
| UI-00549 | Use suggestion | {()=>setAmount(String(invite.suggested_contribution_paise!/100))} | Not a native form submit or inherited from component context | [web/components/GroupGuestCheckout.tsx:22](../../../web/components/GroupGuestCheckout.tsx#L22) |
| UI-00553 |  | {()=>void choosePayment(d.id)} | Not a native form submit or inherited from component context | [web/components/GroupGuestCheckout.tsx:22](../../../web/components/GroupGuestCheckout.tsx#L22) |
| UI-00555 | {details.kind==='UPI'?'Show payment QR':'Show bank details'} | {()=>setModal(true)} | {submit} | [web/components/GroupGuestCheckout.tsx:22](../../../web/components/GroupGuestCheckout.tsx#L22) |
| UI-00557 | {busy?'Submitting…':'I have done the payment'} | submit / component action | {submit} | [web/components/GroupGuestCheckout.tsx:22](../../../web/components/GroupGuestCheckout.tsx#L22) |
| UI-00558 | Change payment method | {()=>{setStage('start');setDetails(null);setSelected('');setReference('')}} | {submit} | [web/components/GroupGuestCheckout.tsx:22](../../../web/components/GroupGuestCheckout.tsx#L22) |
| UI-00559 | Copy bank details | {()=>void navigator.clipboard.writeText(`${details.accountHolder}\n${details.accountNumber}\n${details.ifsc}`)} | Not a native form submit or inherited from component context | [web/components/GroupGuestCheckout.tsx:24](../../../web/components/GroupGuestCheckout.tsx#L24) |
| UI-00560 | Continue to reference details → | {()=>setModal(false)} | Not a native form submit or inherited from component context | [web/components/GroupGuestCheckout.tsx:24](../../../web/components/GroupGuestCheckout.tsx#L24) |
| UI-00664 | {collapsed?<PanelLeftOpen size={20}/>:<PanelLeftClose size={20}/>} | {toggle} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:43](../../../web/components/HostWorkspace.tsx#L43) |
| UI-00667 |  | {()=>void signOut()} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:46](../../../web/components/HostWorkspace.tsx#L46) |
| UI-00668 |  | {()=>setCollapsed(true)} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:47](../../../web/components/HostWorkspace.tsx#L47) |
| UI-00747 | Use a different mobile number | {()=>{setVerified(false);setMessage('')}} | Not a native form submit or inherited from component context | [web/components/MobileVerification.tsx:13](../../../web/components/MobileVerification.tsx#L13) |
| UI-00751 | {busy?'Please wait…':challenge?'Verify mobile':'Send verification code'} | submit / component action | {submit} | [web/components/MobileVerification.tsx:13](../../../web/components/MobileVerification.tsx#L13) |
| UI-00752 | Change number / request another code | {()=>{setChallenge('');setCode('');setMessage('Wait at least 60 seconds before requesting another code.')}} | {submit} | [web/components/MobileVerification.tsx:13](../../../web/components/MobileVerification.tsx#L13) |
| UI-00797 | {busy?'Please wait…':challenge?'Send a new code':'Send code'} | {()=>void send()} | Not a native form submit or inherited from component context | [web/components/SignupEmailVerification.tsx:17](../../../web/components/SignupEmailVerification.tsx#L17) |
| UI-00799 | Verify email | {()=>void verify()} | Not a native form submit or inherited from component context | [web/components/SignupEmailVerification.tsx:17](../../../web/components/SignupEmailVerification.tsx#L17) |


### Dialog and alert/status inventory

| ID | Role / kind | Text / label | Visibility conditions | Source |
| --- | --- | --- | --- | --- |
| UI-00053 | "status" | Opening your workspace… | Parent/source state | [web/app/components/HostGlobalNav.tsx:13](../../../web/app/components/HostGlobalNav.tsx#L13) |
| UI-00258 | "status" | Opening invitation… | Parent/source state | [web/app/groups/[token]/[groupId]/page.tsx:27](../../../web/app/groups/[token]/[groupId]/page.tsx#L27) |
| UI-00278 | "status" | {message} | message; (mode==='member'\|\|(accessMode!=='GUESTS_ONLY'&&section==='membership')) | [web/app/groups/[token]/[groupId]/page.tsx:30](../../../web/app/groups/[token]/[groupId]/page.tsx#L30) |
| UI-00444 | "status" | Opening your celebration space… | ready | [web/components/ExperienceProvider.tsx:13](../../../web/components/ExperienceProvider.tsx#L13) |
| UI-00531 | "status" | ✓ Email verified for account recovery | Parent/source state | [web/components/GroupEmailVerification.tsx:14](../../../web/components/GroupEmailVerification.tsx#L14) |
| UI-00538 | "status" | {notice} | notice | [web/components/GroupEmailVerification.tsx:15](../../../web/components/GroupEmailVerification.tsx#L15) |
| UI-00546 | "alert" | {error} | error | [web/components/GroupGuestCheckout.tsx:21](../../../web/components/GroupGuestCheckout.tsx#L21) |
| UI-00547 | "status" |  | stage==='thanks'; !allowed; !invite | [web/components/GroupGuestCheckout.tsx:22](../../../web/components/GroupGuestCheckout.tsx#L22) |
| UI-00666 | "alert" | {error} | error | [web/components/HostWorkspace.tsx:46](../../../web/components/HostWorkspace.tsx#L46) |
| UI-00746 | "status" | Mobile number verified | verified | [web/components/MobileVerification.tsx:13](../../../web/components/MobileVerification.tsx#L13) |
| UI-00753 | "status" | {message} | message | [web/components/MobileVerification.tsx:13](../../../web/components/MobileVerification.tsx#L13) |
| UI-00796 | "status" | ✓ Email verified | verified | [web/components/SignupEmailVerification.tsx:17](../../../web/components/SignupEmailVerification.tsx#L17) |
| UI-00800 | "status" | {message} | message&&!verified | [web/components/SignupEmailVerification.tsx:17](../../../web/components/SignupEmailVerification.tsx#L17) |
| UI-00801 | "alert" | {error} | error | [web/components/SignupEmailVerification.tsx:17](../../../web/components/SignupEmailVerification.tsx#L17) |


### Workflow state bindings

| Source | Binding | Initial expression |
| --- | --- | --- |
| web/app/groups/[token]/[groupId]/page.tsx | busy | false |
| web/app/groups/[token]/[groupId]/page.tsx | section | 'contribute' |
| web/app/groups/[token]/[groupId]/page.tsx | message | '' |
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
| Header | Shared identity/event or page title/back/actions, where declared. Composite header declarations: 2. |
| Navigation / sidebar | Host/admin navigation and event context where applicable; mobile/focused variants are conditional. nav=2, aside=1. |
| Main content | Purpose-specific forms/cards/tables/canvas/player from the composed sources. main=3, section=6, article=0. |
| Search and filters | Fields/selects listed below; exact value/handler/visibility controls are not inferred from placeholder labels. |
| Data grids and cards | Table declarations=0. Data collections and generated rows/cards are in frontend.json dynamicSources. |
| Popups / dialogs | Named/source-declared dialog control records below; opening/closing/loading/apply/cancel actions belong to those component handlers. |
| Alerts / notifications | Source controls with role alert/status are listed below. Notice strings may come from API results; do not assume a universal envelope. |
| Footer | Local/shared footer declarations=0. No undocumented legal/footer wording is invented. |
| Loading / empty / success / error | Observed state bindings and relevant action fields are listed below. Expected QA states from Document 2 do not establish a spinner/retry control where source does not implement one. |


### Composed implementation

- [web/app/host/account/page.tsx:1](../../../web/app/host/account/page.tsx#L1)
- [web/components/GroupMemberEmailPolicy.tsx:1](../../../web/components/GroupMemberEmailPolicy.tsx#L1)
- [web/components/VerificationProvider.tsx:1](../../../web/components/VerificationProvider.tsx#L1)
- [web/components/MobileVerification.tsx:1](../../../web/components/MobileVerification.tsx#L1)
- [web/components/AccountEmailVerification.tsx:1](../../../web/components/AccountEmailVerification.tsx#L1)
- [web/app/layout.tsx:1](../../../web/app/layout.tsx#L1)
- [web/components/ExperienceProvider.tsx:1](../../../web/components/ExperienceProvider.tsx#L1)
- [web/app/components/PerformanceDiagnostics.tsx:1](../../../web/app/components/PerformanceDiagnostics.tsx#L1)
- [web/app/components/HostSessionRedirect.tsx:1](../../../web/app/components/HostSessionRedirect.tsx#L1)
- [web/app/components/HostGlobalNav.tsx:1](../../../web/app/components/HostGlobalNav.tsx#L1)
- [web/components/HostWorkspace.tsx:1](../../../web/components/HostWorkspace.tsx#L1)
- [web/components/focused/HostShell.tsx:1](../../../web/components/focused/HostShell.tsx#L1)

### Fields

| Control ID | Label / expression | Type | Binding | Required | Native limits / format | Detailed specification |
| --- | --- | --- | --- | --- | --- | --- |
| UI-00280 | Full name | input  | {name} | true | minLength={2}; maxLength={120} | [control](UI_CONTROLS.md#ui-00280) |
| UI-00375 | Email address | input "email" | {draftEmail} | true | maxLength={254} | [control](UI_CONTROLS.md#ui-00375) |
| UI-00380 | Six-digit code | input  | {code} | true | maxLength={6}; pattern="[0-9]{6}" | [control](UI_CONTROLS.md#ui-00380) |
| UI-00569 | Require group members to verify email | input "checkbox" | {!!(hostId?policy?.admin_enabled:policy?.host_enabled)} | Not declared |  | [control](UI_CONTROLS.md#ui-00569) |
| UI-00749 | Mobile number with country code | input "tel" | {mobile} | true | pattern="\+[1-9][0-9]{7,14}" | [control](UI_CONTROLS.md#ui-00749) |
| UI-00750 | Six-digit verification code | input  | {code} | true | maxLength={6}; pattern="[0-9]{6}" | [control](UI_CONTROLS.md#ui-00750) |


### Buttons

| ID | Label / expression | Action | Submit validation / handler | Source |
| --- | --- | --- | --- | --- |
| UI-00281 | {busy?'Saving…':'Save name'} | submit / component action | {save} | [web/app/host/account/page.tsx:17](../../../web/app/host/account/page.tsx#L17) |
| UI-00284 | Sign out | {()=>void signOut()} | Not a native form submit or inherited from component context | [web/app/host/account/page.tsx:17](../../../web/app/host/account/page.tsx#L17) |
| UI-00376 | {busy?'Saving…':'Save email'} | submit / component action | {updateEmail} | [web/components/AccountEmailVerification.tsx:15](../../../web/components/AccountEmailVerification.tsx#L15) |
| UI-00377 | Cancel | {()=>setEditing(false)} | {updateEmail} | [web/components/AccountEmailVerification.tsx:15](../../../web/components/AccountEmailVerification.tsx#L15) |
| UI-00378 | Update email address | {()=>{setDraftEmail(email);setEditing(true);setError('');setMessage('')}} | Not a native form submit or inherited from component context | [web/components/AccountEmailVerification.tsx:15](../../../web/components/AccountEmailVerification.tsx#L15) |
| UI-00381 | {busy?'Verifying…':'Verify email'} | submit / component action | {finish} | [web/components/AccountEmailVerification.tsx:15](../../../web/components/AccountEmailVerification.tsx#L15) |
| UI-00382 | {busy?'Sending…':'Send verification code'} | {()=>void send()} | Not a native form submit or inherited from component context | [web/components/AccountEmailVerification.tsx:15](../../../web/components/AccountEmailVerification.tsx#L15) |
| UI-00385 | Refresh | {()=>setRetry(v=>v+1)} | Not a native form submit or inherited from component context | [web/components/AccountEmailVerification.tsx:15](../../../web/components/AccountEmailVerification.tsx#L15) |
| UI-00664 | {collapsed?<PanelLeftOpen size={20}/>:<PanelLeftClose size={20}/>} | {toggle} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:43](../../../web/components/HostWorkspace.tsx#L43) |
| UI-00667 |  | {()=>void signOut()} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:46](../../../web/components/HostWorkspace.tsx#L46) |
| UI-00668 |  | {()=>setCollapsed(true)} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:47](../../../web/components/HostWorkspace.tsx#L47) |
| UI-00747 | Use a different mobile number | {()=>{setVerified(false);setMessage('')}} | Not a native form submit or inherited from component context | [web/components/MobileVerification.tsx:13](../../../web/components/MobileVerification.tsx#L13) |
| UI-00751 | {busy?'Please wait…':challenge?'Verify mobile':'Send verification code'} | submit / component action | {submit} | [web/components/MobileVerification.tsx:13](../../../web/components/MobileVerification.tsx#L13) |
| UI-00752 | Change number / request another code | {()=>{setChallenge('');setCode('');setMessage('Wait at least 60 seconds before requesting another code.')}} | {submit} | [web/components/MobileVerification.tsx:13](../../../web/components/MobileVerification.tsx#L13) |


### Dialog and alert/status inventory

| ID | Role / kind | Text / label | Visibility conditions | Source |
| --- | --- | --- | --- | --- |
| UI-00053 | "status" | Opening your workspace… | Parent/source state | [web/app/components/HostGlobalNav.tsx:13](../../../web/app/components/HostGlobalNav.tsx#L13) |
| UI-00285 | "status" | {message} | message | [web/app/host/account/page.tsx:17](../../../web/app/host/account/page.tsx#L17) |
| UI-00372 | "status" | Checking your email… | loading | [web/components/AccountEmailVerification.tsx:15](../../../web/components/AccountEmailVerification.tsx#L15) |
| UI-00373 | "status" | ✓ Email verified for account recovery. Verified email addresses cannot be changed. | verified; loading | [web/components/AccountEmailVerification.tsx:15](../../../web/components/AccountEmailVerification.tsx#L15) |
| UI-00383 | "status" | {message} | message | [web/components/AccountEmailVerification.tsx:15](../../../web/components/AccountEmailVerification.tsx#L15) |
| UI-00384 | "alert" | {error} | error | [web/components/AccountEmailVerification.tsx:15](../../../web/components/AccountEmailVerification.tsx#L15) |
| UI-00444 | "status" | Opening your celebration space… | ready | [web/components/ExperienceProvider.tsx:13](../../../web/components/ExperienceProvider.tsx#L13) |
| UI-00570 | "alert" | {error} | error | [web/components/GroupMemberEmailPolicy.tsx:11](../../../web/components/GroupMemberEmailPolicy.tsx#L11) |
| UI-00666 | "alert" | {error} | error | [web/components/HostWorkspace.tsx:46](../../../web/components/HostWorkspace.tsx#L46) |
| UI-00746 | "status" | Mobile number verified | verified | [web/components/MobileVerification.tsx:13](../../../web/components/MobileVerification.tsx#L13) |
| UI-00753 | "status" | {message} | message | [web/components/MobileVerification.tsx:13](../../../web/components/MobileVerification.tsx#L13) |


### Workflow state bindings

| Source | Binding | Initial expression |
| --- | --- | --- |
| web/app/host/account/page.tsx | message | '' |
| web/app/host/account/page.tsx | busy | false |
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
| Header | Shared identity/event or page title/back/actions, where declared. Composite header declarations: 1. |
| Navigation / sidebar | Host/admin navigation and event context where applicable; mobile/focused variants are conditional. nav=2, aside=1. |
| Main content | Purpose-specific forms/cards/tables/canvas/player from the composed sources. main=3, section=6, article=2. |
| Search and filters | Fields/selects listed below; exact value/handler/visibility controls are not inferred from placeholder labels. |
| Data grids and cards | Table declarations=1. Data collections and generated rows/cards are in frontend.json dynamicSources. |
| Popups / dialogs | Named/source-declared dialog control records below; opening/closing/loading/apply/cancel actions belong to those component handlers. |
| Alerts / notifications | Source controls with role alert/status are listed below. Notice strings may come from API results; do not assume a universal envelope. |
| Footer | Local/shared footer declarations=0. No undocumented legal/footer wording is invented. |
| Loading / empty / success / error | Observed state bindings and relevant action fields are listed below. Expected QA states from Document 2 do not establish a spinner/retry control where source does not implement one. |


### Composed implementation

- [web/app/host/monetization/page.tsx:1](../../../web/app/host/monetization/page.tsx#L1)
- [web/components/HostCommerce.tsx:1](../../../web/components/HostCommerce.tsx#L1)
- [web/components/CommercialAnalytics.tsx:1](../../../web/components/CommercialAnalytics.tsx#L1)
- [web/components/CommercePurchase.tsx:1](../../../web/components/CommercePurchase.tsx#L1)
- [web/app/layout.tsx:1](../../../web/app/layout.tsx#L1)
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
| UI-00437 | Event | select  | {selected} | Not declared |  | [control](UI_CONTROLS.md#ui-00437) |
| UI-00657 | Placement type | select  | {form.stream} | Not declared |  | [control](UI_CONTROLS.md#ui-00657) |
| UI-00658 | {k} | input {k==='url'?'url':'text'} | {form[k]} | true | maxLength={k==='description'?1000:k==='url'?2048:k==='title'?160:k==='category'?80:50} | [control](UI_CONTROLS.md#ui-00658) |


### Buttons

| ID | Label / expression | Action | Submit validation / handler | Source |
| --- | --- | --- | --- | --- |
| UI-00410 | {busy?'Processing…':offer.amountPaise===0?'Activate access':`${offer.entitled?'Renew':'Purchase'} · ${commercialMoney(offer.amountPaise)} (test mode)`} | {()=>void pay()} | Not a native form submit or inherited from component context | [web/components/CommercePurchase.tsx:27](../../../web/components/CommercePurchase.tsx#L27) |
| UI-00412 | Reload price | {()=>{setRetry(v=>v+1)}} | Not a native form submit or inherited from component context | [web/components/CommercePurchase.tsx:27](../../../web/components/CommercePurchase.tsx#L27) |
| UI-00438 | {busy?'Loading…':'Load analytics'} | {()=>void load()} | Not a native form submit or inherited from component context | [web/components/CommercialAnalytics.tsx:10](../../../web/components/CommercialAnalytics.tsx#L10) |
| UI-00659 | Submit for approval | submit / component action | {submit} | [web/components/HostCommerce.tsx:16](../../../web/components/HostCommerce.tsx#L16) |
| UI-00664 | {collapsed?<PanelLeftOpen size={20}/>:<PanelLeftClose size={20}/>} | {toggle} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:43](../../../web/components/HostWorkspace.tsx#L43) |
| UI-00667 |  | {()=>void signOut()} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:46](../../../web/components/HostWorkspace.tsx#L46) |
| UI-00668 |  | {()=>setCollapsed(true)} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:47](../../../web/components/HostWorkspace.tsx#L47) |


### Dialog and alert/status inventory

| ID | Role / kind | Text / label | Visibility conditions | Source |
| --- | --- | --- | --- | --- |
| UI-00053 | "status" | Opening your workspace… | Parent/source state | [web/app/components/HostGlobalNav.tsx:13](../../../web/app/components/HostGlobalNav.tsx#L13) |
| UI-00411 | "alert" | {error} | error; groupBase&&error.includes('switched off') | [web/components/CommercePurchase.tsx:27](../../../web/components/CommercePurchase.tsx#L27) |
| UI-00413 | "status" | {notice} | notice | [web/components/CommercePurchase.tsx:27](../../../web/components/CommercePurchase.tsx#L27) |
| UI-00439 | "alert" | {error} | error | [web/components/CommercialAnalytics.tsx:10](../../../web/components/CommercialAnalytics.tsx#L10) |
| UI-00444 | "status" | Opening your celebration space… | ready | [web/components/ExperienceProvider.tsx:13](../../../web/components/ExperienceProvider.tsx#L13) |
| UI-00653 | "alert" | {error} | error | [web/components/HostCommerce.tsx:16](../../../web/components/HostCommerce.tsx#L16) |
| UI-00666 | "alert" | {error} | error | [web/components/HostWorkspace.tsx:46](../../../web/components/HostWorkspace.tsx#L46) |


### Workflow state bindings

| Source | Binding | Initial expression |
| --- | --- | --- |
| web/components/HostCommerce.tsx | error | '' |
| web/components/HostCommerce.tsx | busy | false |
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
| Header | Shared identity/event or page title/back/actions, where declared. Composite header declarations: 2. |
| Navigation / sidebar | Host/admin navigation and event context where applicable; mobile/focused variants are conditional. nav=2, aside=1. |
| Main content | Purpose-specific forms/cards/tables/canvas/player from the composed sources. main=3, section=8, article=2. |
| Search and filters | Fields/selects listed below; exact value/handler/visibility controls are not inferred from placeholder labels. |
| Data grids and cards | Table declarations=0. Data collections and generated rows/cards are in frontend.json dynamicSources. |
| Popups / dialogs | Named/source-declared dialog control records below; opening/closing/loading/apply/cancel actions belong to those component handlers. |
| Alerts / notifications | Source controls with role alert/status are listed below. Notice strings may come from API results; do not assume a universal envelope. |
| Footer | Local/shared footer declarations=0. No undocumented legal/footer wording is invented. |
| Loading / empty / success / error | Observed state bindings and relevant action fields are listed below. Expected QA states from Document 2 do not establish a spinner/retry control where source does not implement one. |


### Composed implementation

- [web/app/host/payment-settings/page.tsx:1](../../../web/app/host/payment-settings/page.tsx#L1)
- [web/components/ReceivingAccountHealth.tsx:1](../../../web/components/ReceivingAccountHealth.tsx#L1)
- [web/components/ExperienceProvider.tsx:1](../../../web/components/ExperienceProvider.tsx#L1)
- [web/app/layout.tsx:1](../../../web/app/layout.tsx#L1)
- [web/components/VerificationProvider.tsx:1](../../../web/components/VerificationProvider.tsx#L1)
- [web/app/components/PerformanceDiagnostics.tsx:1](../../../web/app/components/PerformanceDiagnostics.tsx#L1)
- [web/app/components/HostSessionRedirect.tsx:1](../../../web/app/components/HostSessionRedirect.tsx#L1)
- [web/app/components/HostGlobalNav.tsx:1](../../../web/app/components/HostGlobalNav.tsx#L1)
- [web/components/HostWorkspace.tsx:1](../../../web/components/HostWorkspace.tsx#L1)
- [web/components/focused/HostShell.tsx:1](../../../web/components/focused/HostShell.tsx#L1)

### Fields

| Control ID | Label / expression | Type | Binding | Required | Native limits / format | Detailed specification |
| --- | --- | --- | --- | --- | --- | --- |
| UI-00298 | Host receiving account | select  | {existingAccount} | true |  | [control](UI_CONTROLS.md#ui-00298) |
| UI-00301 | Related host or event | select  | {upiEvent} | Not declared |  | [control](UI_CONTROLS.md#ui-00301) |
| UI-00302 | Account name / tag (optional) | input  | {upiLabel} | Not declared | maxLength={50} | [control](UI_CONTROLS.md#ui-00302) |
| UI-00303 | UPI ID | input  | {upi} | true |  | [control](UI_CONTROLS.md#ui-00303) |
| UI-00314 | "Upload UPI QR PNG" | input "file" |  | Not declared |  | [control](UI_CONTROLS.md#ui-00314) |
| UI-00320 | Related host or event | select  | {bankEvent} | Not declared |  | [control](UI_CONTROLS.md#ui-00320) |
| UI-00321 | Account name / tag (optional) | input  | {bankLabel} | Not declared | maxLength={50} | [control](UI_CONTROLS.md#ui-00321) |
| UI-00322 | Account holder name | input  | {holder} | true |  | [control](UI_CONTROLS.md#ui-00322) |
| UI-00323 | Account number | input "password" | {number} | true |  | [control](UI_CONTROLS.md#ui-00323) |
| UI-00324 | IFSC | input  | {ifsc} | true | maxLength={11} | [control](UI_CONTROLS.md#ui-00324) |


### Buttons

| ID | Label / expression | Action | Submit validation / handler | Source |
| --- | --- | --- | --- | --- |
| UI-00292 | × | {()=>setError('')} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:65](../../../web/app/host/payment-settings/page.tsx#L65) |
| UI-00294 | × | {()=>setNotice('')} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:65](../../../web/app/host/payment-settings/page.tsx#L65) |
| UI-00296 | Refresh | {()=>void load()} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:66](../../../web/app/host/payment-settings/page.tsx#L66) |
| UI-00299 | Link to this event | submit / component action | {e=>{e.preventDefault();const [kind,id]=existingAccount.split(':');if(id)void action(async()=>{await paymentProfiles.linkToEvent(authFetch,eventId,kind,id);setExistingAccount('')},'Account linked to this event. Select Preferred to make it ready for publishing.')}} | [web/app/host/payment-settings/page.tsx:67](../../../web/app/host/payment-settings/page.tsx#L67) |
| UI-00304 | Add UPI ID | submit / component action | {e=>{e.preventDefault();void action(async()=>{await paymentProfiles.addUpi(authFetch,upi,undefined,upiLabel,upiEvent);setUpi('');setUpiLabel('');},'UPI ID saved. Upload or generate its QR, then confirm your details.');}} | [web/app/host/payment-settings/page.tsx:67](../../../web/app/host/payment-settings/page.tsx#L67) |
| UI-00305 | Verify {verification?.environment==='SANDBOX'?'in sandbox':'account'} | {()=>void verifyAccount(u.id,'upi')} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:67](../../../web/app/host/payment-settings/page.tsx#L67) |
| UI-00306 | Verification details | {()=>void verificationHistory(u.id,'upi')} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:67](../../../web/app/host/payment-settings/page.tsx#L67) |
| UI-00308 | Confirm my details | {()=>void confirmDestination(u.id,'upi')} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:67](../../../web/app/host/payment-settings/page.tsx#L67) |
| UI-00309 | {u.preferred\|\|u.isPreferred?'✓ Preferred':!u.qrReady?'QR required':(u.verificationStatus==='VERIFIED'\|\|u.hostConfirmed)?'Set preferred':'Confirm details first'} | {()=>void action(()=>paymentProfiles.preferUpi(authFetch,u.id,undefined,eventId),'Preferred destination updated.')} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:67](../../../web/app/host/payment-settings/page.tsx#L67) |
| UI-00310 | Enable | {()=>void action(()=>paymentProfiles.enableUpi(authFetch,u.id,undefined,eventId),'UPI ID enabled. Independent verification is still required.')} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:67](../../../web/app/host/payment-settings/page.tsx#L67) |
| UI-00311 | Disable | {()=>void action(()=>paymentProfiles.disableUpi(authFetch,u.id,undefined,eventId),'UPI ID disabled.')} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:67](../../../web/app/host/payment-settings/page.tsx#L67) |
| UI-00312 | Generate QR | {()=>void viewQr(u.id,'generate')} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:67](../../../web/app/host/payment-settings/page.tsx#L67) |
| UI-00313 | View uploaded QR | {()=>void viewQr(u.id,'uploaded')} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:67](../../../web/app/host/payment-settings/page.tsx#L67) |
| UI-00315 | Rename | {()=>{const value=window.prompt('Account name / tag (maximum 50 characters)',u.accountLabel\|\|'');if(value!==null)void action(()=>paymentProfiles.rename(authFetch,'upi',u.id,value),'UPI account name updated.');}} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:67](../../../web/app/host/payment-settings/page.tsx#L67) |
| UI-00317 | {eventId?'Remove from event':'Delete'} | {()=>{if(window.confirm(eventId?'Remove this account from this event? Shared host accounts will remain saved.':`Archive ${u.accountLabel\|\|u.masked\|\|'this UPI ID'}? It will disappear from payment choices. Historical records will be retained.`))void action(()=>paymentProfiles.archive(authFetch,'upi',u.id,undefined,eventId),eventId?'Account removed from this event.':'UPI account archived.');}} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:67](../../../web/app/host/payment-settings/page.tsx#L67) |
| UI-00325 | Add bank account | submit / component action | {e=>{e.preventDefault();void action(async()=>{await paymentProfiles.addBank(authFetch,{accountNumber:number,ifsc,accountHolderName:holder,accountLabel:bankLabel,eventId:bankEvent\|\|null});setBankLabel('');setNumber('');setIfsc('');setHolder('');},'Bank account saved. Confirm the details; independent verification is required before guest use.');}} | [web/app/host/payment-settings/page.tsx:68](../../../web/app/host/payment-settings/page.tsx#L68) |
| UI-00326 | Verify {verification?.environment==='SANDBOX'?'in sandbox':'account'} | {()=>void verifyAccount(b.id,'bank')} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:68](../../../web/app/host/payment-settings/page.tsx#L68) |
| UI-00327 | Verification details | {()=>void verificationHistory(b.id,'bank')} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:68](../../../web/app/host/payment-settings/page.tsx#L68) |
| UI-00328 | Confirm my details | {()=>void confirmDestination(b.id,'banks')} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:68](../../../web/app/host/payment-settings/page.tsx#L68) |
| UI-00329 | {b.preferred\|\|b.isPreferred?'✓ Preferred':(b.verificationStatus==='VERIFIED'\|\|b.hostConfirmed)?'Set preferred':'Confirm details first'} | {()=>void action(()=>paymentProfiles.preferBank(authFetch,b.id,undefined,eventId),'Preferred destination updated.')} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:68](../../../web/app/host/payment-settings/page.tsx#L68) |
| UI-00330 | Enable | {()=>void action(()=>paymentProfiles.enableBank(authFetch,b.id,undefined,eventId),'Bank account enabled. Independent verification is still required.')} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:68](../../../web/app/host/payment-settings/page.tsx#L68) |
| UI-00331 | Disable | {()=>void action(()=>paymentProfiles.disableBank(authFetch,b.id,undefined,eventId),'Bank account disabled.')} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:68](../../../web/app/host/payment-settings/page.tsx#L68) |
| UI-00332 | Rename | {()=>{const value=window.prompt('Account name / tag (maximum 50 characters)',b.accountLabel\|\|'');if(value!==null)void action(()=>paymentProfiles.rename(authFetch,'banks',b.id,value),'Bank account name updated.');}} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:68](../../../web/app/host/payment-settings/page.tsx#L68) |
| UI-00334 | {eventId?'Remove from event':'Delete'} | {()=>{if(window.confirm(eventId?'Remove this account from this event? Shared host accounts will remain saved.':`Archive ${b.accountLabel\|\|maskedBank(b)}? It will disappear from payment choices. Historical records will be retained.`))void action(()=>paymentProfiles.archive(authFetch,'banks',b.id,undefined,eventId),eventId?'Account removed from this event.':'Bank account archived.');}} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:68](../../../web/app/host/payment-settings/page.tsx#L68) |
| UI-00337 | Close QR | {closeQr} | Not a native form submit or inherited from component context | [web/app/host/payment-settings/page.tsx:69](../../../web/app/host/payment-settings/page.tsx#L69) |
| UI-00664 | {collapsed?<PanelLeftOpen size={20}/>:<PanelLeftClose size={20}/>} | {toggle} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:43](../../../web/components/HostWorkspace.tsx#L43) |
| UI-00667 |  | {()=>void signOut()} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:46](../../../web/components/HostWorkspace.tsx#L46) |
| UI-00668 |  | {()=>setCollapsed(true)} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:47](../../../web/components/HostWorkspace.tsx#L47) |
| UI-00784 | Refresh check | {()=>setRetry(v=>v+1)} | Not a native form submit or inherited from component context | [web/components/ReceivingAccountHealth.tsx:10](../../../web/components/ReceivingAccountHealth.tsx#L10) |


### Dialog and alert/status inventory

| ID | Role / kind | Text / label | Visibility conditions | Source |
| --- | --- | --- | --- | --- |
| UI-00286 | native-dialog | 'Account holder name exactly as registered with the bank' | Parent/source state | [web/app/host/payment-settings/page.tsx:38](../../../web/app/host/payment-settings/page.tsx#L38) |
| UI-00287 | native-dialog | `Send these account details to Cashfree for ${verification?.environment\|\|'sandbox'} verification?` | Parent/source state | [web/app/host/payment-settings/page.tsx:39](../../../web/app/host/payment-settings/page.tsx#L39) |
| UI-00288 | native-dialog | 'I have checked this destination and confirm it belongs to me. This does not independently verify ownership or enable guest transfers.' | Parent/source state | [web/app/host/payment-settings/page.tsx:56](../../../web/app/host/payment-settings/page.tsx#L56) |
| UI-00316 | native-dialog | 'Account name / tag (maximum 50 characters)' | upis.length===0; loading | [web/app/host/payment-settings/page.tsx:67](../../../web/app/host/payment-settings/page.tsx#L67) |
| UI-00318 | native-dialog | eventId?'Remove this account from this event? Shared host accounts will remain saved.':`Archive ${u.accountLabel\|\|u.masked\|\|'this UPI ID'}? It will disappear from payment choices. Historical records will be retained.` | upis.length===0; loading | [web/app/host/payment-settings/page.tsx:67](../../../web/app/host/payment-settings/page.tsx#L67) |
| UI-00333 | native-dialog | 'Account name / tag (maximum 50 characters)' | banks.length===0; loading | [web/app/host/payment-settings/page.tsx:68](../../../web/app/host/payment-settings/page.tsx#L68) |
| UI-00335 | native-dialog | eventId?'Remove this account from this event? Shared host accounts will remain saved.':`Archive ${b.accountLabel\|\|maskedBank(b)}? It will disappear from payment choices. Historical records will be retained.` | banks.length===0; loading | [web/app/host/payment-settings/page.tsx:68](../../../web/app/host/payment-settings/page.tsx#L68) |
| UI-00336 | "dialog" | "UPI QR preview" | qr | [web/app/host/payment-settings/page.tsx:69](../../../web/app/host/payment-settings/page.tsx#L69) |
| UI-00053 | "status" | Opening your workspace… | Parent/source state | [web/app/components/HostGlobalNav.tsx:13](../../../web/app/components/HostGlobalNav.tsx#L13) |
| UI-00291 | "alert" | {error} | error | [web/app/host/payment-settings/page.tsx:65](../../../web/app/host/payment-settings/page.tsx#L65) |
| UI-00293 | "status" | {notice} | notice | [web/app/host/payment-settings/page.tsx:65](../../../web/app/host/payment-settings/page.tsx#L65) |
| UI-00295 | "status" | {verificationDetails} | verificationDetails | [web/app/host/payment-settings/page.tsx:66](../../../web/app/host/payment-settings/page.tsx#L66) |
| UI-00307 | "status" | Upload or generate a valid QR before confirming or selecting Preferred. | !u.qrReady; upis.length===0; loading | [web/app/host/payment-settings/page.tsx:67](../../../web/app/host/payment-settings/page.tsx#L67) |
| UI-00444 | "status" | Opening your celebration space… | ready | [web/components/ExperienceProvider.tsx:13](../../../web/components/ExperienceProvider.tsx#L13) |
| UI-00666 | "alert" | {error} | error | [web/components/HostWorkspace.tsx:46](../../../web/components/HostWorkspace.tsx#L46) |
| UI-00785 | "status" | Checking linked accounts… | loading | [web/components/ReceivingAccountHealth.tsx:10](../../../web/components/ReceivingAccountHealth.tsx#L10) |
| UI-00786 | "alert" | {error} | error; loading | [web/components/ReceivingAccountHealth.tsx:10](../../../web/components/ReceivingAccountHealth.tsx#L10) |


### Workflow state bindings

| Source | Binding | Initial expression |
| --- | --- | --- |
| web/app/host/payment-settings/page.tsx | busy | false |
| web/app/host/payment-settings/page.tsx | loading | true |
| web/app/host/payment-settings/page.tsx | error | '' |
| web/app/host/payment-settings/page.tsx | notice | '' |
| web/app/host/payment-settings/page.tsx | uploadedQr | {} |
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
| Header | Shared identity/event or page title/back/actions, where declared. Composite header declarations: 1. |
| Navigation / sidebar | Host/admin navigation and event context where applicable; mobile/focused variants are conditional. nav=2, aside=1. |
| Main content | Purpose-specific forms/cards/tables/canvas/player from the composed sources. main=3, section=1, article=0. |
| Search and filters | Fields/selects listed below; exact value/handler/visibility controls are not inferred from placeholder labels. |
| Data grids and cards | Table declarations=0. Data collections and generated rows/cards are in frontend.json dynamicSources. |
| Popups / dialogs | Named/source-declared dialog control records below; opening/closing/loading/apply/cancel actions belong to those component handlers. |
| Alerts / notifications | Source controls with role alert/status are listed below. Notice strings may come from API results; do not assume a universal envelope. |
| Footer | Local/shared footer declarations=0. No undocumented legal/footer wording is invented. |
| Loading / empty / success / error | Observed state bindings and relevant action fields are listed below. Expected QA states from Document 2 do not establish a spinner/retry control where source does not implement one. |


### Composed implementation

- [web/app/login/page.tsx:1](../../../web/app/login/page.tsx#L1)
- [web/components/VerificationProvider.tsx:1](../../../web/components/VerificationProvider.tsx#L1)
- [web/app/components/HostToast.tsx:1](../../../web/app/components/HostToast.tsx#L1)
- [web/app/layout.tsx:1](../../../web/app/layout.tsx#L1)
- [web/components/ExperienceProvider.tsx:1](../../../web/components/ExperienceProvider.tsx#L1)
- [web/app/components/PerformanceDiagnostics.tsx:1](../../../web/app/components/PerformanceDiagnostics.tsx#L1)
- [web/app/components/HostSessionRedirect.tsx:1](../../../web/app/components/HostSessionRedirect.tsx#L1)
- [web/app/components/HostGlobalNav.tsx:1](../../../web/app/components/HostGlobalNav.tsx#L1)
- [web/components/HostWorkspace.tsx:1](../../../web/components/HostWorkspace.tsx#L1)
- [web/components/focused/HostShell.tsx:1](../../../web/components/focused/HostShell.tsx#L1)

### Fields

| Control ID | Label / expression | Type | Binding | Required | Native limits / format | Detailed specification |
| --- | --- | --- | --- | --- | --- | --- |
| UI-00342 | Username or email | input  | {identifier} | true |  | [control](UI_CONTROLS.md#ui-00342) |
| UI-00343 | Password | input "password" | {password} | true |  | [control](UI_CONTROLS.md#ui-00343) |


### Buttons

| ID | Label / expression | Action | Submit validation / handler | Source |
| --- | --- | --- | --- | --- |
| UI-00059 | × | {()=>{setFlash(null);onDismiss?.();}} | Not a native form submit or inherited from component context | [web/app/components/HostToast.tsx:12](../../../web/app/components/HostToast.tsx#L12) |
| UI-00345 | {busy?'Signing in…':'Sign in →'} | submit / component action | {submit} | [web/app/login/page.tsx:13](../../../web/app/login/page.tsx#L13) |
| UI-00664 | {collapsed?<PanelLeftOpen size={20}/>:<PanelLeftClose size={20}/>} | {toggle} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:43](../../../web/components/HostWorkspace.tsx#L43) |
| UI-00667 |  | {()=>void signOut()} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:46](../../../web/components/HostWorkspace.tsx#L46) |
| UI-00668 |  | {()=>setCollapsed(true)} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:47](../../../web/components/HostWorkspace.tsx#L47) |


### Dialog and alert/status inventory

| ID | Role / kind | Text / label | Visibility conditions | Source |
| --- | --- | --- | --- | --- |
| UI-00053 | "status" | Opening your workspace… | Parent/source state | [web/app/components/HostGlobalNav.tsx:13](../../../web/app/components/HostGlobalNav.tsx#L13) |
| UI-00340 | "status" | Your session has expired. Please sign in again to continue. | expired | [web/app/login/page.tsx:13](../../../web/app/login/page.tsx#L13) |
| UI-00344 | "alert" | {error} | error | [web/app/login/page.tsx:13](../../../web/app/login/page.tsx#L13) |
| UI-00444 | "status" | Opening your celebration space… | ready | [web/components/ExperienceProvider.tsx:13](../../../web/components/ExperienceProvider.tsx#L13) |
| UI-00666 | "alert" | {error} | error | [web/components/HostWorkspace.tsx:46](../../../web/components/HostWorkspace.tsx#L46) |


### Workflow state bindings

| Source | Binding | Initial expression |
| --- | --- | --- |
| web/app/login/page.tsx | error | '' |
| web/app/login/page.tsx | busy | false |
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
| Header | Shared identity/event or page title/back/actions, where declared. Composite header declarations: 1. |
| Navigation / sidebar | Host/admin navigation and event context where applicable; mobile/focused variants are conditional. nav=2, aside=1. |
| Main content | Purpose-specific forms/cards/tables/canvas/player from the composed sources. main=3, section=1, article=0. |
| Search and filters | Fields/selects listed below; exact value/handler/visibility controls are not inferred from placeholder labels. |
| Data grids and cards | Table declarations=0. Data collections and generated rows/cards are in frontend.json dynamicSources. |
| Popups / dialogs | Named/source-declared dialog control records below; opening/closing/loading/apply/cancel actions belong to those component handlers. |
| Alerts / notifications | Source controls with role alert/status are listed below. Notice strings may come from API results; do not assume a universal envelope. |
| Footer | Local/shared footer declarations=0. No undocumented legal/footer wording is invented. |
| Loading / empty / success / error | Observed state bindings and relevant action fields are listed below. Expected QA states from Document 2 do not establish a spinner/retry control where source does not implement one. |


### Composed implementation

- [web/app/register/page.tsx:1](../../../web/app/register/page.tsx#L1)
- [web/components/VerificationProvider.tsx:1](../../../web/components/VerificationProvider.tsx#L1)
- [web/components/SignupEmailField.tsx:1](../../../web/components/SignupEmailField.tsx#L1)
- [web/components/ExperienceProvider.tsx:1](../../../web/components/ExperienceProvider.tsx#L1)
- [web/app/components/HostToast.tsx:1](../../../web/app/components/HostToast.tsx#L1)
- [web/app/layout.tsx:1](../../../web/app/layout.tsx#L1)
- [web/app/components/PerformanceDiagnostics.tsx:1](../../../web/app/components/PerformanceDiagnostics.tsx#L1)
- [web/app/components/HostSessionRedirect.tsx:1](../../../web/app/components/HostSessionRedirect.tsx#L1)
- [web/app/components/HostGlobalNav.tsx:1](../../../web/app/components/HostGlobalNav.tsx#L1)
- [web/components/HostWorkspace.tsx:1](../../../web/components/HostWorkspace.tsx#L1)
- [web/components/focused/HostShell.tsx:1](../../../web/components/focused/HostShell.tsx#L1)

### Fields

| Control ID | Label / expression | Type | Binding | Required | Native limits / format | Detailed specification |
| --- | --- | --- | --- | --- | --- | --- |
| UI-00351 | Full name | input  | {fullName} | true | minLength={2}; maxLength={120} | [control](UI_CONTROLS.md#ui-00351) |
| UI-00352 | Username | input  | {username} | true | minLength={4}; maxLength={30}; pattern="[a-z][a-z0-9_]{3,29}" | [control](UI_CONTROLS.md#ui-00352) |
| UI-00353 | Mobile number (country code required) | input "tel" | {mobile} | true | pattern="\+[1-9][0-9]{7,14}" | [control](UI_CONTROLS.md#ui-00353) |
| UI-00354 | Password | input "password" | {password} | true | minLength={12} | [control](UI_CONTROLS.md#ui-00354) |
| UI-00795 | Email address | input "email" | {value} | true | maxLength={254} | [control](UI_CONTROLS.md#ui-00795) |


### Buttons

| ID | Label / expression | Action | Submit validation / handler | Source |
| --- | --- | --- | --- | --- |
| UI-00059 | × | {()=>{setFlash(null);onDismiss?.();}} | Not a native form submit or inherited from component context | [web/app/components/HostToast.tsx:12](../../../web/app/components/HostToast.tsx#L12) |
| UI-00355 | Edit your details | {()=>setStep(0)} | {submit} | [web/app/register/page.tsx:8](../../../web/app/register/page.tsx#L8) |
| UI-00357 | {busy?'Creating account…':focused&&step===0?'Continue →':'Create account →'} | submit / component action | {submit} | [web/app/register/page.tsx:8](../../../web/app/register/page.tsx#L8) |
| UI-00664 | {collapsed?<PanelLeftOpen size={20}/>:<PanelLeftClose size={20}/>} | {toggle} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:43](../../../web/components/HostWorkspace.tsx#L43) |
| UI-00667 |  | {()=>void signOut()} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:46](../../../web/components/HostWorkspace.tsx#L46) |
| UI-00668 |  | {()=>setCollapsed(true)} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:47](../../../web/components/HostWorkspace.tsx#L47) |


### Dialog and alert/status inventory

| ID | Role / kind | Text / label | Visibility conditions | Source |
| --- | --- | --- | --- | --- |
| UI-00053 | "status" | Opening your workspace… | Parent/source state | [web/app/components/HostGlobalNav.tsx:13](../../../web/app/components/HostGlobalNav.tsx#L13) |
| UI-00356 | "alert" | {error} | error | [web/app/register/page.tsx:8](../../../web/app/register/page.tsx#L8) |
| UI-00444 | "status" | Opening your celebration space… | ready | [web/components/ExperienceProvider.tsx:13](../../../web/components/ExperienceProvider.tsx#L13) |
| UI-00666 | "alert" | {error} | error | [web/components/HostWorkspace.tsx:46](../../../web/components/HostWorkspace.tsx#L46) |


### Workflow state bindings

| Source | Binding | Initial expression |
| --- | --- | --- |
| web/app/register/page.tsx | step | 0 |
| web/app/register/page.tsx | busy | false |
| web/app/register/page.tsx | error | '' |
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
| Header | Shared identity/event or page title/back/actions, where declared. Composite header declarations: 1. |
| Navigation / sidebar | Host/admin navigation and event context where applicable; mobile/focused variants are conditional. nav=2, aside=1. |
| Main content | Purpose-specific forms/cards/tables/canvas/player from the composed sources. main=4, section=2, article=0. |
| Search and filters | Fields/selects listed below; exact value/handler/visibility controls are not inferred from placeholder labels. |
| Data grids and cards | Table declarations=0. Data collections and generated rows/cards are in frontend.json dynamicSources. |
| Popups / dialogs | Named/source-declared dialog control records below; opening/closing/loading/apply/cancel actions belong to those component handlers. |
| Alerts / notifications | Source controls with role alert/status are listed below. Notice strings may come from API results; do not assume a universal envelope. |
| Footer | Local/shared footer declarations=0. No undocumented legal/footer wording is invented. |
| Loading / empty / success / error | Observed state bindings and relevant action fields are listed below. Expected QA states from Document 2 do not establish a spinner/retry control where source does not implement one. |


### Composed implementation

- [web/app/verify-email/page.tsx:1](../../../web/app/verify-email/page.tsx#L1)
- [web/components/VerificationProvider.tsx:1](../../../web/components/VerificationProvider.tsx#L1)
- [web/app/layout.tsx:1](../../../web/app/layout.tsx#L1)
- [web/components/ExperienceProvider.tsx:1](../../../web/components/ExperienceProvider.tsx#L1)
- [web/app/components/PerformanceDiagnostics.tsx:1](../../../web/app/components/PerformanceDiagnostics.tsx#L1)
- [web/app/components/HostSessionRedirect.tsx:1](../../../web/app/components/HostSessionRedirect.tsx#L1)
- [web/app/components/HostGlobalNav.tsx:1](../../../web/app/components/HostGlobalNav.tsx#L1)
- [web/components/HostWorkspace.tsx:1](../../../web/components/HostWorkspace.tsx#L1)
- [web/components/focused/HostShell.tsx:1](../../../web/components/focused/HostShell.tsx#L1)

### Fields

| Control ID | Label / expression | Type | Binding | Required | Native limits / format | Detailed specification |
| --- | --- | --- | --- | --- | --- | --- |
| UI-00368 | Six-digit code | input  | {code} | true | maxLength={6}; pattern="[0-9]{6}" | [control](UI_CONTROLS.md#ui-00368) |


### Buttons

| ID | Label / expression | Action | Submit validation / handler | Source |
| --- | --- | --- | --- | --- |
| UI-00363 | Check session again | {()=>setRetry(v=>v+1)} | Not a native form submit or inherited from component context | [web/app/verify-email/page.tsx:34](../../../web/app/verify-email/page.tsx#L34) |
| UI-00366 | {busy?'Sending request…':'Send email verification code'} | submit / component action | {request} | [web/app/verify-email/page.tsx:34](../../../web/app/verify-email/page.tsx#L34) |
| UI-00369 | {busy?'Verifying…':'Verify email'} | submit / component action | {verify} | [web/app/verify-email/page.tsx:34](../../../web/app/verify-email/page.tsx#L34) |
| UI-00664 | {collapsed?<PanelLeftOpen size={20}/>:<PanelLeftClose size={20}/>} | {toggle} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:43](../../../web/components/HostWorkspace.tsx#L43) |
| UI-00667 |  | {()=>void signOut()} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:46](../../../web/components/HostWorkspace.tsx#L46) |
| UI-00668 |  | {()=>setCollapsed(true)} | Not a native form submit or inherited from component context | [web/components/HostWorkspace.tsx:47](../../../web/components/HostWorkspace.tsx#L47) |


### Dialog and alert/status inventory

| ID | Role / kind | Text / label | Visibility conditions | Source |
| --- | --- | --- | --- | --- |
| UI-00053 | "status" | Opening your workspace… | Parent/source state | [web/app/components/HostGlobalNav.tsx:13](../../../web/app/components/HostGlobalNav.tsx#L13) |
| UI-00361 | "status" | Checking your sign-in session… | session==='checking' | [web/app/verify-email/page.tsx:34](../../../web/app/verify-email/page.tsx#L34) |
| UI-00370 | "alert" | {error} | error | [web/app/verify-email/page.tsx:34](../../../web/app/verify-email/page.tsx#L34) |
| UI-00371 | "status" | {message} | message | [web/app/verify-email/page.tsx:34](../../../web/app/verify-email/page.tsx#L34) |
| UI-00444 | "status" | Opening your celebration space… | ready | [web/components/ExperienceProvider.tsx:13](../../../web/components/ExperienceProvider.tsx#L13) |
| UI-00666 | "alert" | {error} | error | [web/components/HostWorkspace.tsx:46](../../../web/components/HostWorkspace.tsx#L46) |


### Workflow state bindings

| Source | Binding | Initial expression |
| --- | --- | --- |
| web/app/verify-email/page.tsx | message | '' |
| web/app/verify-email/page.tsx | error | '' |
| web/app/verify-email/page.tsx | busy | false |
| web/components/HostWorkspace.tsx | busy | false |
| web/components/HostWorkspace.tsx | error | '' |

