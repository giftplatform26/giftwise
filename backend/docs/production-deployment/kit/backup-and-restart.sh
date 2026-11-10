#!/usr/bin/env bash
set -euo pipefail
[[ $(id -u) == 0 ]] || { echo 'Run as root.' >&2; exit 2; }
exec 9>/run/lock/giftwise-backup.lock
flock -n 9 || { echo 'Another backup is already running.' >&2; exit 2; }
[[ -f /etc/giftwise/offsite.env ]] || { echo 'Configure offsite backup first.' >&2; exit 2; }
# This root-owned file contains the public encryption recipient and SSH destination.
# shellcheck disable=SC1091
source /etc/giftwise/offsite.env
: "${AGE_RECIPIENT:?}" "${OFFSITE_USER:?}" "${OFFSITE_HOST:?}" "${OFFSITE_DIR:?}" "${OFFSITE_SSH_IDENTITY_FILE:?}"
[[ "$OFFSITE_DIR" =~ ^/[A-Za-z0-9_./-]+$ && "$OFFSITE_DIR" != / && "$OFFSITE_DIR" != *".."* ]] || exit 2
[[ "$OFFSITE_USER" =~ ^[A-Za-z0-9_-]+$ && "$OFFSITE_HOST" =~ ^[A-Za-z0-9.-]+$ ]] || exit 2
for required in age ssh scp tar python3; do command -v "$required" >/dev/null; done
systemctl is-active --quiet giftwise-backend
systemctl is-active --quiet giftwise-web
ssh -o BatchMode=yes -o StrictHostKeyChecking=yes -i "$OFFSITE_SSH_IDENTITY_FILE" \
 "$OFFSITE_USER@$OFFSITE_HOST" test -d "$OFFSITE_DIR"
restart_services() {
 systemctl start giftwise-backend
 for attempt in $(seq 1 120); do
  if curl -fsS http://127.0.0.1:8080/actuator/health/readiness >/dev/null; then
   systemctl start giftwise-web
   return
  fi
  sleep 2
 done
 echo 'Backend failed to become ready after backup. Investigate immediately.' >&2
 return 1
}
systemctl stop giftwise-web giftwise-backend
trap restart_services EXIT
python3 /usr/local/lib/giftwise/coordinated-backup.py
backup_path=$(find /var/backups/giftwise -maxdepth 1 -type d -name 'backup-*' | sort | tail -n 1)
[[ -n "$backup_path" && -f "$backup_path/COMPLETE" ]] || exit 2
python3 /usr/local/lib/giftwise/verify-backup.py "$backup_path"
restart_services
trap - EXIT
backup_name=$(basename "$backup_path")
# Use separate encrypted recovery artifacts for database/media and application keys.
umask 077
tar -C "$backup_path" -cf - . | age -r "$AGE_RECIPIENT" \
 -o "/var/backups/giftwise/$backup_name.data.tar.age"
tar -C /etc -cf - giftwise/backend.env giftwise/web.env caddy/Caddyfile | age -r "$AGE_RECIPIENT" \
 -o "/var/backups/giftwise/$backup_name.config.tar.age"
scp -o BatchMode=yes -o StrictHostKeyChecking=yes -i "$OFFSITE_SSH_IDENTITY_FILE" \
 "/var/backups/giftwise/$backup_name.data.tar.age" \
 "/var/backups/giftwise/$backup_name.config.tar.age" \
 "$OFFSITE_USER@$OFFSITE_HOST:$OFFSITE_DIR/"
echo 'Encrypted database/media and configuration backups copied offsite.'
