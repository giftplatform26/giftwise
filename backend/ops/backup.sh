#!/usr/bin/env bash
# Run only after stopping app writes. Credentials use libpq env/.pgpass; never argv.
set -euo pipefail
umask 077
export PGCONNECT_TIMEOUT=${PGCONNECT_TIMEOUT:-10}
: "${PGDATABASE:?Set PGDATABASE}" "${GIFT_MEDIA_DIR:?Set GIFT_MEDIA_DIR}" "${GIFT_BACKUP_DIR:?Set GIFT_BACKUP_DIR}"
[[ "${GIFT_WRITES_STOPPED:-}" == YES ]] || { echo 'Stop application writes and set GIFT_WRITES_STOPPED=YES.' >&2; exit 2; }
[[ -d "$GIFT_MEDIA_DIR" && ! -L "$GIFT_MEDIA_DIR" && "$GIFT_MEDIA_DIR" != / ]] || { echo 'Use an existing private media directory.' >&2; exit 2; }
command -v pg_dump >/dev/null
command -v python3 >/dev/null
python3 - "$GIFT_MEDIA_DIR" "$GIFT_BACKUP_DIR" <<'PYTHON'
import pathlib,sys
media=pathlib.Path(sys.argv[1]).resolve();backup=pathlib.Path(sys.argv[2]).resolve()
if media==pathlib.Path('/') or backup==media or media in backup.parents:
 raise SystemExit('Use a private media directory and a separate backup destination outside it')
PYTHON
mkdir -p "$GIFT_BACKUP_DIR"
backup_path=$(mktemp -d "$GIFT_BACKUP_DIR/backup-$(date -u +%Y%m%dT%H%M%SZ)-XXXXXX")
# A failed run deliberately has no COMPLETE marker and cannot pass verification.
pg_dump --lock-wait-timeout=10000 --format=custom --no-owner --no-privileges --file="$backup_path/database.dump"
tar -C "$GIFT_MEDIA_DIR" -cf "$backup_path/media.tar" .
python3 "$(dirname "$0")/verify-backup.py" --create "$backup_path"
printf '%s\n' 'Coordinated database/media backup complete. Encrypt and copy off-site using your approved storage policy.'
printf '%s\n' "$backup_path"
