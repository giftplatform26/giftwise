# GiftWise marketing website

Standalone Next.js 16 / React 19 / Tailwind CSS 4 source. Separate from the existing Gift Platform application and backend.

## Run locally

```sh
cd /Users/jijujones/gift-platform/website
npm install
cp .env.example .env.local
npm run dev
```

Open **http://localhost:3001**. Existing app links default to http://localhost:3000/events/new and /login. Set `NEXT_PUBLIC_APP_URL` to your app's origin. No OpenAI or payment keys are needed for this marketing site.

## Production

```sh
npm run typecheck
npm run build
npm test
npm start
```

`npm start` previews the static production export on port 3001. Deploy the **out/** directory to any static host. Environment variables are embedded at build time: rebuild after changing them. `NEXT_PUBLIC_SITE_URL` must be the public marketing origin so canonical links, sitemap, social metadata and the QR invitation point to the correct website.

## What is included

All eleven requested sections, seven celebration types, custom generated illustrations, responsive navigation, animated walkthrough, wishlist filters and contribution preview, functional QR code, interactive phone states, group reveal, dashboard tabs, footer information dialogs, SEO metadata, sitemap, robots, local fonts, reduced-motion support and keyboard-friendly dialogs.

The wishlist, balances, dashboard and contributions are **illustrative previews**. They do not collect payments or change existing app data. Event creation and login link to the actual app. The QR links to this website's wishlist showcase. Stories are labelled illustrative; replace them with approved customer testimonials when available.

Before public launch, configure contact/social URLs, publish approved pricing and legal policies, and confirm the existing app routes. Privacy/Terms dialogs currently explain the marketing preview scope rather than claiming to be final legal policies.

See [design and component structure](docs/DESIGN.md), [illustration prompts and assets](docs/ASSETS.md), and [verification](docs/QA.md).
