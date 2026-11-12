# Experience and component structure

The conversion journey moves from recognition (unwanted gifts) to possibility (wanted gifts), explains the process, provides hands-on previews, then repeats the event-creation action.

1. Hero: split before/after illustration, emotional headline, create-event and walkthrough actions.
2. Problem: four unwanted outcomes transform into four meaningful alternatives.
3. How it works: six selectable steps with a live interface illustration; optional guided, pausable tour.
4. Wishlist: six gifts, category filters, local product illustrations, contribution progress and contribution preview modal. Amazon, Flipkart, Myntra, IKEA and any product URL are presented as import examples.
5. QR invitation: scannable code with phone preview switching between wishlist, group gifting, contribution, wallet and RSVP.
6. Group gifting: friends illustration, private-to-published reveal preview and five benefits.
7. Contribution methods: four selectable visual cards.
8. Host dashboard: overview, wishlist, contributions and group gift tabs, sample analytics and wallet.
9. Benefits: six icon-led reasons to choose meaningful gifting.
10. Features journey (`#features`): five tabs — Plan, Invite, Gift, Celebrate, Remember — each with five features and a product mock-up (checklist and budget, WhatsApp RSVP and meal totals, a gift receipt, the live wall and check-in, the memory book and gift ledger). All panels are in the HTML for search engines; inactive ones use `hidden`.
11. Try band: "Try it free" (app `/try`) and "See real celebrations" (app `/showcase`).
12. For planners (`#for-planners`): partner workspace and handover, venues and decorators, office group gifts, vendor directory.
13. Trust: direct payments to the host, receipts, privacy, host control.
14. Stories: three explicitly illustrative host/guest stories; ready to replace with consented real testimonials.
15. FAQ (`#faq`): ten native `<details>` questions.
16. Final CTA: violet celebration panel leading to event creation.

Footer: company/features links, pricing, privacy, terms, contact and configurable social profiles. Informational dialogs are honest about missing launch details.

## Component hierarchy

```
RootLayout — metadata, structured data, local font
└── Page — generates real QR SVG at build time
    └── MarketingWebsite — modal state and entrance effects
        ├── Header / Hero / Occasions / ProblemSection
        ├── HowItWorks → MiniEventWindow
        ├── WishlistShowcase → ProductArt / Avatars
        ├── PhoneShowcase
        ├── GroupGifting / ContributionWays
        ├── FeatureJourney → PhaseScreen / TryBand
        ├── DashboardPreview / ForPlanners / TrustSection
        ├── Benefits / Stories / Faq / FinalCta
        ├── Footer → InfoDialog
        └── GiftPreviewDialog / WalkthroughDialog
```

`src/lib/demo-data.ts` holds illustrative product data; `src/lib/site.ts` validates configured origins. `ui.tsx` shares the logo, CTA, icons, avatars and image atlas. Styling lives in `src/app/globals.css`, with Tailwind available for utilities.

## Visual system

Violet #6C63FF, coral #FF8A65, mint #00C896, canvas #F8F9FC. Self-hosted Manrope, large expressive headings, restrained gradients, soft shadows and generous whitespace. Matte dimensional illustrations convey warmth without slowing the page with video. Product images use one compact atlas.

## Mobile and accessibility

Layouts adapt at 1100, 900, 700 and 380 pixels. Mobile navigation expands below the header; sections stack; product cards retain a compact grid; dashboard and phone previews adapt to the viewport. Native buttons, descriptive labels, progress labels, image alternatives, focus-visible styling, a skip link and native modal dialogs support keyboard use. Dialogs dismiss on Escape and restore focus. The walkthrough can pause; operating-system reduced-motion preference disables animation. Content stays visible even if an entrance observer does not run. Small details inside illustrative device mockups are decorative product context; surrounding copy explains the functionality.

## Motion and conversion

Subtle entrance animations, floating hero detail, card hover and short panel transitions guide attention. Repeated consistent create-event actions point to the existing app. Interactive demos introduce no signup barrier or live payment action. No fabricated customer counts, security certifications or pricing guarantees are used.

## Further illustration opportunities

Extend the existing visual style with celebration-specific scenes (weddings, baby showers, graduation), invitation-template thumbnails and approved customer portrait photography. Retain soft lighting, warm cream backgrounds, violet/coral accents and uncluttered crops. Use authentic, consented portraits for future testimonials.
