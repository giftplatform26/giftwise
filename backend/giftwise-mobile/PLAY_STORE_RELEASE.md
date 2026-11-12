# Publishing GiftWise on Google Play

Everything in the app is ready for a store build. What remains needs your Google Play developer account, your signing key and your final web address, so those steps are yours. Follow them in order.

## 1. One-time accounts (owner)

1. Create a Google Play developer account (one-time fee) under the business name that will appear in the store.
2. Create an Expo account and an EAS project for the app: `npx eas-cli login`, then `npx eas-cli init` in this folder. Note the project ID.
3. Decide the permanent Android package name, for example `com.giftwise.app`. It can never change after the first upload.

## 2. Release configuration

Set these for the build (EAS secrets or your shell). None of them are passwords:

| Variable | Example | Purpose |
| --- | --- | --- |
| `EXPO_PUBLIC_GIFTWISE_WEB_URL` | `https://app.giftwise.example` | The live web app the shell opens (HTTPS only) |
| `GIFTWISE_ANDROID_PACKAGE` | `com.giftwise.app` | Permanent package name |
| `GIFTWISE_IOS_BUNDLE_ID` | `com.giftwise.app` | Needed by the config even for Android-only releases |
| `GIFTWISE_EAS_PROJECT_ID` | `xxxxxxxx-…` | From `eas init` |

The config refuses release builds that use the local preview flag or plain HTTP.

## 3. Build the store bundle

```sh
npx eas-cli build -p android --profile production
```

This produces an Android App Bundle (`.aab`). Let EAS create and keep the upload key the first time (recommended), and enrol in Play App Signing when prompted in the Play Console. Version codes increase automatically.

## 4. Store listing (ready to paste; adjust to taste)

- **App name:** GiftWise — Invitations & Gifts
- **Short description (80 characters):** Invitations, RSVPs, wishlists and group gifts for every Indian celebration.
- **Full description:**

  > Plan, invite, gift and celebrate in one place.
  >
  > • Beautiful invitations with a QR code and a link you can share anywhere
  > • RSVPs for every function — haldi, mehendi, sangeet, reception — with meal choices
  > • Wishlists from any store, group gifts and experience wishes
  > • Gifts by UPI or bank straight to your account, with a receipt for every guest
  > • On the day: live wall of wishes, guest check-in with QR passes, seating plan and a digital shagun register that works on weak Wi-Fi
  > • Co-hosts for the family, a planning checklist, budget and run sheet
  > • Afterwards: memory album, printable memory book, thank-you cards and your lifelong gift ledger
  >
  > Guests don't need the app — invitations open in any browser.

- **Category:** Events. **Tags:** invitations, wedding planner, RSVP.
- **Graphics:** 512×512 icon, 1024×500 feature graphic, at least 4 phone screenshots (suggested: invitation, schedule RSVP, gift register, live wall). Capture them from the production web app on a phone.
- **Contact email and privacy policy URL:** use the live website's contact address and privacy page.

## 5. Data safety form (answers based on the current app)

| Question | Answer |
| --- | --- |
| Data collected | Name, email, phone (account and guest lists); photos (guestbook and album, optional); app activity (RSVPs, wishes); financial info: masked UPI/bank details entered by hosts, gift amounts reported by guests |
| Shared with third parties | Payment provider only when a guest pays online; WhatsApp Business only when the host sends WhatsApp messages |
| Encrypted in transit | Yes (HTTPS required for release builds) |
| Users can request deletion | Yes — Account → Delete account |
| Collected from children | No (the service is for adult hosts and guests) |

Confirm these against the final privacy policy before submitting.

## 6. Content rating and testing

1. Complete the content rating questionnaire (no violence, gambling or user-to-user chat beyond moderated guestbook wishes).
2. Upload the `.aab` to **Internal testing**, add 5–10 testers, and run the browser test plan's host and guest journeys inside the app.
3. Platform administration must stay blocked in the app (the web app returns 404 to the app for `/admin`). Confirm once on a real phone.
4. Promote to **Closed testing**, then **Production** with a staged rollout (for example 10% → 50% → 100%).

## 7. After each web release

The app opens the live web app, so most changes reach users without a new store build. Build a new `.aab` only when this folder's native code, icons, permissions or Expo version change.
