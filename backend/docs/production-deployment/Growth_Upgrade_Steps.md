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
