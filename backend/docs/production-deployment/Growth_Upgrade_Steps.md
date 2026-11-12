# Upgrade an existing GiftWise deployment for V107

Use this supplement only for an existing server deployed using the main runbook. A fresh server follows the main runbook instead. Keep public access restricted, stop the backup timer, take and verify a coordinated backup, and complete the usual release checks before proceeding. Do not rerun fresh database provisioning or regenerate backend keys.

## Prepare the updated operational kit

Receive the updated ZIP and trusted runtime archive/checksum from the release engineer. Extract the ZIP to a new private directory under `/srv/giftwise/incoming` and review the changed kit files. Do not extract over an existing unreviewed directory. Install the updated helpers under `/usr/local/lib/giftwise` only after reviewing the current server's local customizations.

## Configure the Node server origins

Location: production server, any directory. The following creates the file only if it does not exist; it never truncates an existing file.

```sh
sudo sh -c 'test -e /etc/giftwise/web.env || install -o root -g root -m 0600 /dev/null /etc/giftwise/web.env'
sudoedit /etc/giftwise/web.env
```

Set these two lines in the editor, replacing the hostname with the real owner-controlled HTTPS hostname. They are public origin settings, not backend credentials:

```text
GIFTWISE_PUBLIC_ORIGIN=https://YOUR_REAL_HOSTNAME
GIFTWISE_BACKEND_ORIGIN=http://127.0.0.1:8080
```

Do not use `NEXT_PUBLIC_` prefixes. Never copy database passwords or backend encryption keys into the Node environment. Keep the existing backend environment file unchanged.

After the updated kit is in `/usr/local/lib/giftwise`, and while both application services are stopped as required by the main upgrade procedure:

```sh
sudo install -o root -g root -m 0644 /usr/local/lib/giftwise/giftwise-web.service /etc/systemd/system/giftwise-web.service
sudo systemctl daemon-reload
```

Continue the main runbook's reviewed activation and service-start steps. Backend startup applies V107 without replacing existing users, events or passwords. Verify readiness and schema head 107. Verify the Node service starts successfully and the website works before reopening.

## Verify feature rollout

In Admin → Branding & appearance, review Experience and growth controls. New sharing previews, public keepsakes and guest discovery are off by default. Existing RSVP, Guestbook, calendar, event reuse and albums still require their own grants.

On a staging event, enable rich previews globally and opt in in Design & QR. Fetch the published invitation and verify its Open Graph metadata and PNG. Disable the host preference and verify the preview endpoints return 404. Third-party caches may retain earlier previews.

For keepsakes, enable Public keepsakes, grant the host Guestbook, and opt in from Thank-you cards on a published/completed event. Confirm approved wishes appear; hidden/pending wishes, private album media, contacts and payments do not. Revoke sharing and confirm the URL becomes unavailable.

Run the coordinated backup/restore and encrypted offsite checks again. The updated configuration backup includes `giftwise/web.env`. Keep the owner-held recovery keys available. Re-enable the backup timer only after successful verification. Do not enable live payment collection as part of this upgrade.

# Upgrade to the 11 November 2026 release (V111–V119)

This release adds direct-transfer payment codes and custom receipts (V111–V112) and thirty-one switchable growth, event-management and acquisition features (V113–V119). Full details: `../application/releases/2026-11-11_GROWTH_FEATURES_G1_G4.md`.

1. Take the coordinated backup as in the main runbook. Stop both services.
2. Activate the new backend and web archives together. Backend startup applies V111–V119; all migrations are additive. Verify readiness and schema head **119**.
3. No new settings are needed to start: every new module is off, and WhatsApp defaults to practice mode (`DEV_OUTBOX`, nothing is sent).
4. Only when Meta has approved the three WhatsApp templates, add these to the existing **backend** environment file (never to `web.env`), then restart the backend:

   ```text
   GIFT_WHATSAPP_PROVIDER=META_CLOUD
   GIFT_WHATSAPP_META_PHONE_NUMBER_ID=...
   GIFT_WHATSAPP_META_ACCESS_TOKEN=...
   GIFT_WHATSAPP_META_VERIFY_TOKEN=...   # 16+ random characters
   GIFT_WHATSAPP_META_APP_SECRET=...     # from the Meta app dashboard
   ```

   In the Meta dashboard, set the webhook to `https://YOUR_BACKEND_HOST/api/webhooks/whatsapp` with the same verify token, and subscribe to `messages`. The backend refuses to start with `META_CLOUD` and missing credentials.
5. Optional: `GIFT_REFERRALS_REWARD_TEXT` (referral reward text) and `GIFT_OFFERS_FIRST_FREE_DAYS` (premium days for the guest welcome offer, default 90).
6. Roll out one feature at a time: Admin → Hosts & privileges (platform and host), then Admin → Events → Celebration feature controls (event). Confirm on a staging event before granting real hosts.
7. Optional: set `DB_POOL_SIZE` (default 20) in the backend environment. With several backend instances, keep the total below PostgreSQL `max_connections`.
8. Fastest setup for a new wedding host: Admin → Hosts & privileges → Feature presets → "Wedding pack", applied to the platform, then the host, then their event.
