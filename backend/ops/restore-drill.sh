#!/usr/bin/env bash
# Intentionally restricted to an existing EMPTY database ending _restore.
set -euo pipefail
umask 077
export PGCONNECT_TIMEOUT=${PGCONNECT_TIMEOUT:-10}
: "${PGDATABASE:?Set PGDATABASE}" "${GIFT_BACKUP_PATH:?Set GIFT_BACKUP_PATH}" "${GIFT_RESTORE_MEDIA_DIR:?Set GIFT_RESTORE_MEDIA_DIR}"
[[ "$PGDATABASE" =~ ^[A-Za-z_][A-Za-z0-9_]*_restore$ && "${GIFT_RESTORE_CONFIRM:-}" == RESTORE_DISPOSABLE_DATABASE ]] || { echo 'Only an explicitly confirmed *_restore database is allowed.' >&2; exit 2; }
[[ ! -e "$GIFT_RESTORE_MEDIA_DIR" ]] || { echo 'Restore media destination must not exist.' >&2; exit 2; }
python3 "$(dirname "$0")/verify-backup.py" "$GIFT_BACKUP_PATH"
objects=$(psql -X -A -t -v ON_ERROR_STOP=1 -c "SELECT count(*) FROM pg_class c JOIN pg_namespace n ON n.oid=c.relnamespace WHERE n.nspname='public' AND c.relkind IN ('r','v','m','S')")
[[ "$objects" == 0 ]] || { echo 'Restore database is not empty.' >&2; exit 2; }
# Validate every tar member before extraction: no traversal, links or device nodes.
python3 - "$GIFT_BACKUP_PATH/media.tar" "$GIFT_RESTORE_MEDIA_DIR" <<'PY'
import pathlib,sys,tarfile
archive,destination=sys.argv[1:]
with tarfile.open(archive) as source:
 for item in source.getmembers():
  p=pathlib.PurePosixPath(item.name)
  if p.is_absolute() or '..' in p.parts or not(item.isfile() or item.isdir()):raise SystemExit('Unsafe media archive')
 pathlib.Path(destination).mkdir(mode=0o700,parents=True,exist_ok=False)
 source.extractall(destination,filter='data')
 root=pathlib.Path(destination);root.chmod(0o700)
 for item in root.rglob('*'):item.chmod(0o700 if item.is_dir() else 0o600)
PY
pg_restore --exit-on-error --single-transaction --no-owner --no-privileges --dbname="$PGDATABASE" "$GIFT_BACKUP_PATH/database.dump"
psql -X -v ON_ERROR_STOP=1 -c 'SELECT version,description,success FROM flyway_schema_history ORDER BY installed_rank DESC LIMIT 5'
printf '%s\n' 'Restore completed in disposable targets. Now run private-media checks, application smoke tests and financial reconciliation. This does not prove RTO/RPO.'
