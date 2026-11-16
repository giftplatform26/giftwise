# Verification

- `npm run typecheck`: strict TypeScript validation.
- `npm run build`: Next.js production compilation and static export (needs `NEXT_PUBLIC_SITE_URL` and `NEXT_PUBLIC_APP_URL`; use `npm run build:local` for a localhost preview build).
- `npm test`: exported-page heading/navigation, SEO endpoints and local image budget smoke tests. Run after building.
- Browser checks: isolated headless Chrome at desktop 1440×1000 and mobile 390×844; responsive width, navigation, walkthrough dialog, local image loading, wishlist filtering, gift contribution preview, phone state, group reveal and dashboard controls.

Production app navigation depends on `NEXT_PUBLIC_APP_URL`; configure and verify it against the deployed app. Demo contributions intentionally never invoke a payment API. Footer pricing/legal/contact content requires launch-specific information. This source was not deployed by this task.
