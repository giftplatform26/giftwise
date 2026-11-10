# GiftWise — The celebration operating system

Brand system v1 · 4 November 2026 · Proposed identity and product direction

Implementation update v0.2: the default app now uses the complete shared web application, with native navigation and platform adapters. See `../FEATURE_PARITY.md` and `../BUILD_GUIDE.md`. The original session-local brand preview is preserved as an unused design reference in `src/screens/BrandPreview.tsx`; references below to that prototype describe the earlier brand exploration.

**Every celebration, thoughtfully together.**

GiftWise brings wishes, people, contributions, and memories into one connected celebration. The emotional sequence is **joy of giving → confidence in contributing → excitement of celebrating**. Purple supplies distinction and imagination; gold marks the moments that matter. Warm neutral space keeps the experience human.

This system is a strategic proposal, accompanied by an interactive React Native mobile prototype. Product concepts, future capabilities, and operational promises require validation before being advertised as live. Brand, mascot, feature names, domains, and handles are creative candidates; availability and trademark clearance have not been established.

## 1. Brand strategy

### Mission
Help people celebrate thoughtfully by bringing wishes, gifts, shared contributions, and meaningful memories together in one easy-to-use place.

### Vision
Become the world’s most trusted celebration ecosystem: a place where every celebration, gift, contribution, and memory can be thoughtfully managed and shared.

### Positioning
For people bringing loved ones together around life’s meaningful moments, GiftWise is the celebration operating system that connects planning, giving, contributing, and remembering. Its distinct value is continuity across the whole occasion, with understandable participation and privacy choices.

Use “celebration operating system” in investor and category-building narratives. For customers, explain the benefit first: “Your wishes, gifts, and celebration, together in one place.” Category language must never make the app feel complex.

### Audience and launch focus

| Audience | Job to be done | Product and message emphasis |
| --- | --- | --- |
| Birthday hosts and family groups | Organize a thoughtful celebration without scattered messages | One celebration link, wish sharing, optional participation |
| Weddings and anniversaries | Coordinate a larger, emotionally important milestone | Elegant pages, multiple hosts, clear gift and fund choices |
| Baby showers | Let a trusted circle help with a new beginning | Practical wishes, duplicate prevention, sensitive language |
| Housewarmings | Turn a new home into a shared beginning | Group gifts, home wishes, flexible contribution options |
| Festivals and friends | Maintain recurring giving traditions | Repeat celebrations, cultural flexibility, group preferences |
| Corporate teams | Coordinate fair, appropriate gifts | Consent, budgets, organizer roles, accessible participation |
| Event organizers | Keep many details and stakeholders aligned | Permissions, checklists, exports, repeatable templates |

Start with birthdays and housewarmings among existing friends and families. They offer a repeatable, comparatively simple path to test host activation and guest participation. Extend into weddings after guest permissions, support, contribution reconciliation, and reliability are proven. Do not launch every audience as a separate product.

### Core values
1. **Thoughtfulness over transaction:** celebrate the person before the gift or amount.
2. **Clarity earns trust:** show contribution status, recipient, fees, timing, and next steps in plain language.
3. **Everyone belongs:** welcome every budget, relationship structure, culture, and ability. A message or presence is meaningful participation.
4. **Privacy with intention:** make audiences explicit; never confuse “hidden from guests” with complete anonymity.
5. **Shared joy, less effort:** reduce host administration and guest uncertainty.
6. **Memories with consent:** people control how their stories and images are shared.

### Personality
A thoughtful host: warm, composed, quietly delightful, capable, and inclusive. Premium means attention and restraint. Trust comes from clear information and reliable behavior. Family-friendly means respectful and accessible, without infantilizing adults. Use expressive illustration in discovery and calm precision in contribution flows.

### Promise
**More meaning. Less organizing.** Every interaction should help someone understand what to do and feel welcome doing it. Do not promise guaranteed delivery, absolute security, universal anonymity, or instant settlements without operational evidence.

### Brand story
The best gifts say something simple: “I know you.” Yet behind a birthday, a wedding, or a new home, thoughtful people often juggle wishlists, message threads, contribution reminders, and photos that disappear into chat history. GiftWise gives that care a place to come together. Hosts can make their wishes clear. Friends can give in a way that feels right. Everyone can spend more energy on the people and the moment. Over time, each celebration becomes part of a shared story worth keeping.

## 2. Identity system

### Primary logo: the Together Ribbon
A soft purple tile holds two crossing ribbons, one ivory and one gold, meeting at a gold knot. The crossing expresses different people joining in one meaningful moment. It reads as a ribbon before a parcel, avoiding a shopping-cart or payment symbol. Pair it with a rounded, slightly tightened GiftWise wordmark. Preserve the capital G and W; use one word.

Editable assets are in `assets/brand/`. Wordmark SVGs retain editable text and system-font fallbacks; outline the approved final typeface before production distribution. The mobile mark uses native Views to remain crisp without a raster dependency.

### Secondary logo
Stack the symbol above GiftWise for invitation backs, gift tags, partner cards, and square compositions. A one-color version uses deep purple or ivory, with the center knot preserved. The wordmark alone works for narrow navigation bars after recognition is established.

### App icon and favicon
App icon: purple full-bleed square with generous space around the ribbon. Platform masks supply corner rounding; do not bake rounded outer corners into store exports. Keep the central symbol inside the middle 60% for adaptive-icon safety. Android foreground is transparent; a separate purple background supplies the field. Monochrome icon uses one solid silhouette.

Favicon: symbol only; omit wordmark and fine details. Export at 16, 32, and 48 px. Inspect the knot and ribbon separation at actual size. App PNG is 1024 × 1024; favicon source is supplied as SVG and PNG. Native splash centers the symbol on warm ivory.

### Logo rules
- Clear space: at least half the symbol width on every side.
- Minimum size: symbol 24 logical px; horizontal lockup 144 px; print mark 8 mm and lockup 30 mm. Favicon is the explicit small-size exception.
- Preferred fields: ivory, white, deep purple. Use the reverse lockup on dark fields.
- Preserve vector proportions. Use approved monochrome artwork where full color is unavailable.
- Do: check contrast, leave breathing room, use the symbol alone in compact contexts.
- Don’t: stretch, tilt, add shadows, replace the gold with a gradient, place over busy photography, enclose in a second box, or substitute a currency sign.
- Do not claim registered trademark status or display ® without registration.

## 3. Color system

| Token | HEX | Role |
| --- | --- | --- |
| Wise Purple / primary | #6336B5 | Main actions, selection, logo |
| Aubergine / secondary | #35205B | Hero panels, premium storytelling |
| Celebration Gold / signature accent | #E8BC62 | Ribbon, rare highlights, celebration detail |
| Gold Ink | #76520E | Readable gold-family text on ivory |
| Soft Lavender | #EEE7FA | Selection backgrounds and gentle grouping |
| Apricot | #F9DED0 | Occasion art and warm illustration |
| Sage Mist | #DDEFE5 | Calm secondary illustration |
| Warm Ivory | #FCFAF6 | Light canvas |
| White | #FFFFFF | Light surfaces and on-purple text |
| Ink | #292137 | Light main text |
| Muted Plum | #6F657B | Secondary light text |
| Light Border | #DED7E8 | Decorative card boundaries |
| Success | #216344 | Confirmed outcome + icon + label |
| Warning | #825509 | Attention needed + explanation |
| Error | #AC324A | Failure + recovery action |

### Dark theme

| Role | HEX |
| --- | --- |
| Canvas / surface | #171220 / #241C30 |
| Main / secondary text | #FAF6FF / #C3B7D1 |
| Primary action / on-action text | #CFB4FF / #281441 |
| Soft tint / border | #362747 / #51435F |
| Gold accent | #E8BC62 |
| Success / warning / error text | #8ED5AC / #F1CD81 / #FFA8B8 |

Use approximately 65% neutral, 25% purple-family, and 10% accents in marketing; in the product, gold should often occupy less than 5%. Gold is not a status indicator or default checkout button. Purple keeps continuity throughout the journey. Never use light gold for small text on white or white text on gold. Use deep purple on gold. Validate each actual text pair against WCAG contrast targets (4.5:1 regular text; 3:1 large text). Light borders are decorative; required input boundaries need a stronger stroke or additional persistent label and fill. Never convey status by hue alone.

The prototype uses semantic theme tokens and readable text pairs. Focus states, screen-reader order, large text, real devices, and contrast of future components still require release QA. Dark theme changes surfaces and text semantically rather than inverting artwork.

## 4. Typography and layout

**Heading font: Manrope**, weights 600 and 700. Geometric enough to feel modern, with a friendly rhythm. **Body font: Inter**, weights 400, 500, and 600. Use tabular numerals for amounts, deadlines, and progress tables. Obtain licensed font files from their official repositories and retain their license notices. Recommended fonts are not bundled; the prototype deliberately uses native system fonts for offline reliability (San Francisco on iOS, platform sans on Android). The identity SVG uses the same fallback strategy.

| Level | Mobile size / line height | Website size / line height | Weight |
| --- | --- | --- | --- |
| Display | 38 / 46 | 64 / 72; narrow 40 / 48 | 700 |
| H1 | 30 / 38 | 48 / 56 | 700 |
| H2 | 24 / 32 | 32 / 40 | 700 |
| H3 | 20 / 28 | 24 / 32 | 600 |
| Body | 16 / 24 | 18 / 28 | 400 |
| Supporting | 14 / 21 | 14 / 22 | 400 |
| Caption | 12 / 18 | 12 / 18 | 500 |
| Button | 16 / 22 | 16 / 24 | 600 |

Mobile values are logical units, not device pixels. Respect OS text scaling; avoid fixed-height text containers. Never shrink essential fee, consent, or contribution information to caption size. Buttons are at least 48 logical units high; body text inputs at least 16. Keep headlines short and use sentence case. Use an 8-point spacing rhythm with 4-point fine adjustments; 24-unit page gutters, 16-unit controls, 24-unit cards, 32-unit feature panels. Website text measures should stay around 55–75 characters; use 24 px mobile and 48–80 px desktop gutters. Do not justify body copy. Avoid thin weights and all-caps paragraphs.

### Imagery, illustration, motion
Use candid hands writing cards, friends arriving, and lived-in celebrations with varied ages, cultures, and family forms. Frame relationships and small details; avoid luxury consumption as a proxy for generosity. Obtain consent for identifiable people and special permission for children’s images. Illustration uses soft planes, ribbon curves, restrained sparkles, and imperfect symmetry. Never borrow religious symbols as generic decoration.

Motion should feel like an invitation: 180–240 ms transitions, one short success flourish, no looping confetti behind forms. Respect reduced-motion preferences. Give haptics sparingly after a deliberate action and never as evidence of a payment completing.

## 5. Voice and tone

Speak like a capable friend hosting thoughtfully. Use short sentences, specific next steps, natural contractions, and people’s names only where expected. Lead with meaning in discovery and with facts in transactional states. Avoid guilt, competitive generosity, “only ₹…”, fabricated urgency, or claims that contribution size measures care.

| Situation | Example |
| --- | --- |
| Friendly greeting | “Your people. Your plans. A little less organizing.” |
| Celebration invitation | “Maya’s next chapter deserves good company. You’re invited.” |
| Reminder notification | “Maya’s birthday is on Saturday. Her celebration page has the details.” |
| Push message | Title: “A moment worth making.” Body: “Your celebration draft is ready when you are.” |
| Contribution pending | “Your contribution is being checked. We’ll update this page when its status is confirmed. Please don’t pay again yet.” |
| Confirmed contribution | “Your ₹500 contribution is confirmed. View your receipt.” Only show this after server confirmation. |
| Error | “We couldn’t save your celebration. Your changes are still here. Try again.” Only use if changes actually remain. |
| Payment error | “We couldn’t confirm the result yet. Check contribution status before trying again.” |
| Empty wishes | “A lovely gift starts with a little hint. Add your first wish.” |
| Empty memories | “Your happy days will have a home here.” |
| Created celebration | “Your celebration is ready to make your own.” |
| Thank-you | “Your thoughtfulness is part of this story. Thank you.” |
| Cancellation | “This celebration has been cancelled. View the contribution details for next steps.” |

Push previews must not expose contribution amounts, private event names, pregnancy-related details, or children’s names on a locked screen by default. Let people select reminder channels and frequency. Use no celebratory mascot or confetti in payment disputes or bereavement contexts.

## 6. Mobile app brand experience

### Navigation and welcome
Four destinations: **Today, Wishes, Memories, You**. Today prioritizes upcoming celebrations and a single creation action. Wishes combines a Wishbook and Together Gifts. Memories is a private archive. You holds preferences, account tools, Plus, and invitations. Guest entry in a future live app should land on the invited celebration rather than force unrelated onboarding.

Welcome uses three skippable screens: “Every celebration starts with you,” “Many thoughtful people. One lovely gift,” and “The day passes. The joy stays.” Each uses Miri, a single benefit, progress dots, and one primary action. Ask for notification or photo permissions at the action that needs them. The native splash is a quiet mark on ivory, without a progress spinner pretending to load a service.

### App Store screenshot storyboard
Create separate exports for current store device requirements using actual implemented product captures. These are art-direction specifications, not uploaded store listings.

| Frame | Headline | Screenshot composition |
| --- | --- | --- |
| 1 | Every celebration, together. | Today dashboard centered on ivory, large purple title, ribbon in margin |
| 2 | A little hint. A lovely gift. | Wishbook cards and add-wish interaction on lavender |
| 3 | Many people. One meaningful gift. | Group gift page, sample progress, unobscured status labels |
| 4 | Know where your contribution stands. | Confirmed, pending, and unsuccessful states with plain labels; only after backend implementation |
| 5 | Make room for the memories. | Private album and consent controls; future-release concept until built |
| 6 | Your next tradition starts here. | Recurring family celebration concept, clearly future until implemented |

Each image makes one claim. Keep headline, capture, and two small brand details; avoid dense collages and fake reviews. Do not present the memory placeholder or simulated contribution confirmation as a live capability in release screenshots.

### Premium: GiftWise Plus
Treat Plus as personalization and convenience: expanded themes, custom covers, extended memory storage, and planning tools. Do not gate essential account security, contribution status, basic receipts, or refund information. The proposed screen has a benefit list, transparent billing interval, total charge, renewal terms, trial end, restore purchases, and a clear close action when subscriptions are actually implemented. The source currently offers a **concept preview**, with no price, checkout, charge, or subscription claim. Validate pricing with host interviews and willingness-to-pay experiments before setting tiers.

### Referral: Grow the Joy
Invite the people who already celebrate together. Proposed reward: both host and friend receive one theme credit after an eligible new host publishes a first celebration. Show eligibility, region, expiry, limits, and anti-abuse terms before enrollment. Avoid rewards proportional to contribution volume. The prototype invokes the device share sheet with a clearly marked demo code; it does not create tracked referrals or award credits.

### Contribution UX principles
Before a live payment, show named recipient, amount, currency, provider, fees, total, expected timing, cancellation/refund policy, visibility audience, and support route. Separate pledged, processing, confirmed, failed, refunded, and disputed states. Never label a client tap as “paid.” Use server-confirmed provider status, deduplicated payment requests, and clear retry recovery. Monetary values belong to the backend; local prototype validation is illustrative.

## 7. Mascot: Miri

**Name:** Miri, the ribbon companion. A creative candidate pending clearance.

**Personality:** observant, encouraging, a little playful, never the center of the occasion. Miri helps you take the next small step and steps aside when facts matter.

**Visual:** a soft aubergine-purple rounded body with two asymmetric golden ribbon loops, ivory eyes, a small smile, and a gold ribbon tail. It has no gender, age, costume, currency motif, or culturally specific ceremonial role. Asymmetry keeps it from becoming a generic parcel. The silhouette remains recognizable without facial detail.

**App:** welcoming pose in onboarding; curious tilt in empty Wishbook; small wave when a celebration is created; calm still pose for reminders. Leave Miri out of payment errors, privacy decisions, and urgent notices. Native View illustration and editable SVG are included.

**Social:** “Miri’s thoughtful tip” cards, occasion checklists, short ribbon-unfolding animations, warm replies as an occasional visual signature. Do not let the mascot speak on behalf of real customers.

**Marketing:** stickers, invitation seals, thank-you cards, modest event signage, and seasonal poses that preserve the base silhouette. Keep mascot content optional for corporate and formal wedding templates. Motion storyboard: notice → lean in → loop opens → small wave, under two seconds and still-frame compatible.

## 8. Signature features and naming architecture

One master brand, descriptive names, no separate sub-brand logos. Introduce the feature name beside its plain-language purpose; avoid making guests learn internal vocabulary.

| Capability | Proposed name | UI introduction |
| --- | --- | --- |
| Group gifting | Together Gifts | “Contribute to a gift together.” |
| Anonymous gifting | Quiet Giving | “Choose who can see your name.” |
| Wishlist creation | Wishbook | “Your wishlist, thoughtfully organized.” |
| Celebration pages | Celebration Spaces | “A home for every detail of your celebration.” |
| Contribution tracking | Contribution View | “See amounts, confirmation status, and next steps.” |
| Gift recommendations | Thoughtful Finds | “Ideas based on the preferences you choose to share.” |
| Event memories | Keepsakes | “Photos, notes, and moments worth keeping.” |

Quiet Giving never implies anonymity from hosts, providers, or legally required records. Show precise visibility choices. Thoughtful Finds must explain why an idea appears and label sponsored placement. Contribution View is a clarity feature, never a leaderboard of people or amounts.

## 9. Marketing assets

**Primary tagline:** Every celebration, thoughtfully together.

**Alternatives:** Make room for the joy. / Good things come together. / A little thought. A lasting memory. / Give with heart. Celebrate with ease.

**Product descriptor:** Wishlists, group gifts, and celebrations in one place.

**Slogans:** Many people. One meaningful gift. / Less coordinating. More celebrating. / Your next tradition starts here. / Thoughtful looks good on everyone.

| Campaign | Creative idea | Channels | Meaningful outcome |
| --- | --- | --- | --- |
| The Thought Behind It | Real stories about why a gift mattered, without displaying its price | Short video, creator partnerships, email | Qualified host starts, not just views |
| One Gift. All Our Love. | A circle of friends building a shared surprise | Reels, invitation cards, group-share assets | Completed group-gift participation after launch |
| Make Room for Joy | Before: fragmented organizing. After: one calm celebration space | Product demos, search landing content | First celebration creation and guest visits |
| A New Tradition | Families describe a repeatable ritual | Seasonal editorial and opt-in community stories | Repeat hosts and return participation |
| The Thank-You Stays | A handwritten note becomes a Keepsake | Post-event email, printable cards | Thank-you completion and consented memory use |

Social mix: 35% thoughtful-giving tips, 25% real celebration stories, 20% product clarity, 15% occasion inspiration, 5% Miri moments. Test and adjust to qualified engagement. Publish accessible image descriptions and captions. Use natural cultural calendars rather than one global holiday default.

Hashtags: #GiftWise, #ThoughtfullyTogether, #GiveTogetherCelebrateTogether, #MakeRoomForJoy, #GiftWiseMoments. Treat as proposed tags; check existing usage and avoid hijacking unrelated community discussions. Do not use invented testimonials, referral statistics, or unsupported “world’s safest” claims.

## 10. Competitive differentiation

Research checked 4 November 2026 against official product/help pages. This is a positioning comparison, not an exhaustive feature audit; availability differs by region and registry type.

| Brand | Established strength | GiftWise’s proposed advantage |
| --- | --- | --- |
| Amazon Wishlist / registries | Familiar gifting ecosystem. Amazon describes wedding/baby registries, gift-card funds, and group gifting options. Ordinary wishlists and registry capabilities should not be treated as identical. | Build the experience around the celebration and its relationships, with cross-occasion continuity rather than a shopping destination. |
| MyRegistry | Universal registries combine stores and support cash/experience funds. | Connect invitation, gift coordination, contribution clarity, thank-yous, and private memories through one occasion record. |
| Giftster | Shared family wishlists, purchase/claim coordination, reminders, and Secret Santa groups. | Extend the family wishlist relationship into an end-to-end host workflow with clear contribution states and post-event continuity. |

Sources: [Amazon registry overview](https://www.aboutamazon.com/news/retail/amazon-find-registry-gifts), [MyRegistry universal registry](https://www.myregistry.com/default.aspx?cloc=us&lang=en), [MyRegistry cash/experience funds](https://guides.myregistry.com/gift-list/how-cash-and-experience-funds-work-on-a-gift-list/), [Giftster family wishlists](https://www.giftster.com/), [Giftster quick start](https://www.giftster.com/quick-start-guide/).

Do not position group gifts, cash funds, or shared wishlists as inventions unique to GiftWise. The proposed distinction is **the connected journey**, supported by execution. Prove it through fewer coordination steps, understandable contribution status, opt-in privacy, and repeat celebration use. Useful measures: time to first published celebration, guest task completion, contribution-status comprehension, support contacts per confirmed payment, thank-you completion, and repeat-host retention. Set targets after a baseline study.

## 11. Technical naming, discoverability, and assets

### Domains and social
Preferred domain candidates: `giftwise.com`, `giftwise.app`, `getgiftwise.com`, `celebratewithgiftwise.com`. Regional candidate: `giftwise.in` if relevant to launch. Prefer one canonical domain; redirect legitimate alternates. No availability, purchase price, ownership, or trademark check has been performed. Do not imply these are available to register.

Handle priority across Instagram, TikTok, YouTube, Facebook, LinkedIn, and Pinterest: `@giftwise`, then consistent `@getgiftwise` or `@celebratewithgiftwise`. Avoid different spellings per channel and trailing numbers. Check platform availability before rollout.

Email structure on the domain actually owned: `hello@`, `support@`, `celebrations@`, `partners@`, `privacy@`, `security@`; person accounts `firstname.lastname@`. Use a monitored `updates@` for service messages with a working reply/support route. Domain ownership, SPF, DKIM, DMARC, and delivery configuration must be completed before use.

### App naming
Home-screen name: **GiftWise**. Store title candidate: **GiftWise: Gifts & Celebrations** (29 characters). Apple subtitle candidate: **Wishlists, gifts, shared joy** (27 characters). Google short description candidate: **Plan celebrations, share wishlists, and bring thoughtful gifts together.** Verify store field limits and rules at submission. Do not keyword-stuff the visible name. The app uses slug `giftwise-mobile`; release bundle/package identifiers must be based on a domain the organization controls. No production identifier is claimed by this prototype.

### SEO and ASO
SEO clusters: birthday wishlist app, group gifting for friends, wedding gift contributions, baby shower wishlist, housewarming gift list, anniversary gift ideas, celebration planning app, family gift organizer, corporate group gifts. Create genuinely useful occasion pages and guides, with clear intent and internal links. Launch only pages supported by working product features. Do not create thin pages for every keyword variation.

Play Store: use naturally phrased “wishlist,” “group gifting,” “celebration planner,” and occasion terms in accurate title and descriptions; Play has no dedicated keyword field. Apple keyword candidate: `wishlist,registry,birthday,wedding,baby shower,housewarming,group gift,anniversary,party` (review localized length and duplication against final title/subtitle). Do not include competitor brands or unsupported features. Localize intent, screenshots, currency, dates, and terminology with native review. Test one listing variable at a time against conversion and retained use.

Technical assets: `src/theme/tokens.ts`, `brand/tokens.css`, SVG lockups and mascot, PNG app/adaptive icons and splash mark. CSS is a handoff asset for future web consistency, not a second application.

## 12. Expansion strategy

### Stage 1 — Earn the celebration
Focus on birthdays and housewarmings. Deliver a reliable celebration page, wishes, understandable guest participation, and thank-yous. Validate host/guest flows and support readiness. Success means people finish a real celebration and choose to return.

### Stage 2 — Celebration management platform
Introduce checklists, RSVPs, schedules, co-host permissions, guest communication controls, and recurring occasions. Keep one celebration record linking all activity. Introduce organizer tools only after role/permission behavior is validated.

### Stage 3 — Digital gifting platform
Add gift cards, experiences, scheduled delivery, and additional gifting partners where commercially and operationally supported. Build accurate order states, recipient verification, recovery, and refund handling before marketing breadth. Revenue can combine optional Plus and transparent partner commissions; do not bias recommendations toward commission without disclosure.

### Stage 4 — Event planning ecosystem
Add opt-in vendor discovery, organizer workspaces, reusable templates, and integrations. Separate vendor service terms from GiftWise platform responsibilities. Rank vendors using understandable criteria and label paid placement. Avoid launching a marketplace before demand and dependable supply exist in a focused geography.

### Stage 5 — Family memory platform
Offer private family circles, consented shared albums, annual recaps, and exportable archives. Build granular audiences, storage controls, deletion/export, child-image safeguards, and ownership rules before making “forever” promises. Never sell intimate celebration context as an advertising segment.

### Category leadership
“Uber of Celebrations” and “Airbnb of Gifting” are ambition references for investor discussions, not customer-facing slogans or evidence of equivalence. Build recognition around GiftWise itself. The durable advantage can be a trusted relationship between people, occasions, and memories, reinforced by permission-aware workflows and reliable fulfillment. Expansion should make each celebration feel simpler, not crowd the home screen with unrelated services.

## Release handoff

The default mobile app now opens the shared web workspace, preserving its existing backend-backed features and gates. The archived brand preview demonstrates session-local interactions only. The mobile shell adds no new payment provider, subscription, referral service, or backend authorization. Real availability depends on backend configuration and must be verified on devices. Never use direct database access or edit the Event entity for branding. Names and product promises must match actual released behavior. Conduct trademark checks, usability studies, accessibility review, device QA, and policy review for the features actually shipping.
