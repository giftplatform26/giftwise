# Production safeguard tooling

These files are implementation aids, not evidence of a deployed environment or a successful recovery. They do not provision a domain, enroll a merchant, dispatch notifications or enable live payouts/refunds.

## Production profile

Use `production` (or `prod`, an alias). It enforces secure/HttpOnly/SameSite session cookies, hides server errors, requires migration-only schema management, rejects placeholder keys, simulator/administrative bootstrap profiles, a missing canonical HTTPS origin, ambiguous private media storage and a declared instance count other than one. It leaves existing payment test-mode restrictions in force. The guard runs during application startup and aborts startup on violations. Keep ingress traffic closed until startup completes. HTTP backend traffic behind an approved TLS ingress can use secure cookies; verify forwarded-header/client-IP behavior at that ingress separately.

Provide at least these environment values through the approved secret/config store:

| Variable | Meaning |
| --- | --- |
| `SPRING_PROFILES_ACTIVE` | `production` |
| `GIFT_PRODUCTION_PUBLIC_ORIGIN` | Canonical HTTPS frontend origin, no credentials/query/path |
| `GIFT_PRODUCTION_INSTANCE_COUNT` | `1`; distributed session/abuse-limit support is still pending |
| `GIFT_GUESTBOOK_PHOTOS_DIR` | Absolute, private, durable media directory mounted on the backend |
| `GIFT_OTP_SECRET_BASE64`, `GIFT_OTP_HMAC_KEY_BASE64` | Non-placeholder 32-byte keys in base64 |
| `GIFT_PAYMENT_ENCRYPTION_KEY_BASE64`, `GIFT_PAYMENT_FINGERPRINT_KEY_BASE64` | Independent non-placeholder 32-byte keys, required when payment profiles are enabled |
| `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` | Private PostgreSQL connection/credentials |

An enabled provider still requires its documented credentials. AI defaults off in the production profile; opting in requires its key and actual project credit/access. Property layering, secret storage/rotation and ingress must be reviewed. `GET /api/admin/console/production-status` and the admin Hosts screen expose configuration issues without exposing values. The response deliberately reports external readiness as unverified; it is not a live provider/backup monitor.

The application currently requires ImageMagick **7** (`magick`) with approved HEIC/WebP/JPEG/PNG delegates for photo ingestion. Provision/scan this dependency in the target runtime; do not assume an ImageMagick 6 `convert` installation is equivalent.

## Coordinated backup

Requirements: Bash, PostgreSQL client tools matching the server, Python 3.12+, `tar`, a private media directory and a secured backup destination. Set libpq `PGHOST`, `PGPORT`, `PGUSER`, `PGDATABASE` and `.pgpass`/runtime credentials; avoid credentials in command arguments or shell history.

1. Stop **all application writes**, including background jobs and old deployment replicas. A per-database transaction does not coordinate filesystem writes.
2. Set `GIFT_WRITES_STOPPED=YES`, `GIFT_MEDIA_DIR` to the exact private media directory and `GIFT_BACKUP_DIR` to a protected destination.
3. Run `ops/backup.sh`. A new private directory contains `database.dump`, `media.tar`, `manifest.json`, `COMPLETE`. Failed partial backups have no completion marker.
4. Run `python3 ops/verify-backup.py /path/to/backup-directory`.
5. Encrypt and copy off-site using the approved KMS/storage policy; record verification and restore evidence. Resume writes only after the coordinated snapshot has completed.

The script does not delete old backups. Retention, encryption, credentials/roles, external secrets/key recovery, WAL/PITR, off-site replication and access auditing require approved operations. SHA-256 checks detect corruption, not malicious manifest modification. No OTP/payment encryption keys are included. Browser invitation drafts are not covered.

## Disposable restore drill

Use a dedicated least-privilege restore role and a trusted, authenticated backup artifact; checksums alone do not authenticate SQL archives. Create an empty database whose name ends in `_restore` and a private parent directory for media. Set `PGDATABASE`, `GIFT_RESTORE_CONFIRM=RESTORE_DISPOSABLE_DATABASE`, `GIFT_BACKUP_PATH` and a **nonexistent** `GIFT_RESTORE_MEDIA_DIR`, then run `ops/restore-drill.sh`.

The script verifies integrity and archive safety, rejects nonempty databases/existing media destinations, extracts only regular files/directories with private 0700/0600 permissions and restores transactionally without original roles/privileges. On restore failure, retain evidence and discard/recreate the disposable targets; it does not clean up arbitrary resources. After success, run the application against those targets with **all provider dispatch disabled**, verify ownership/private photos/keys, compare accounting balances/provider receipts and record timings. Production restore/failover requires a separately approved runbook; this script intentionally cannot overwrite a production database.

## Release checks

`ops/release-check.sh` runs Maven unit/integration verification, full frontend tests, TypeScript, frontend build and documentation validation. Docker, cached/downloadable dependencies and sibling `web` checkout (override `GIFT_WEB_DIR`) are required. It does not deploy. Preserve command exit codes and release-bound reports; browser UAT, load/security tests, restore timing and provider/legal approval remain manual release gates.

`ops/check-source-shadowing.py` also prevents Java files in the test tree from replacing classes with the same production source path. Two such duplicates (occasion enum and verifier configuration) were removed in this release. The default production verifier now has an explicit `@ConditionalOnMissingBean` condition so provider test doubles use normal bean overriding rather than a different production class. The release gate starts from a clean build to remove old compiled copies.

## Environment-only executable artifact

The default development Maven package can include the private local `src/main/resources/application.properties`. **Do not publish that artifact.** The `production-release` Maven profile excludes it and the example files, replaces the base configuration from `src/release/resources/application.properties`, and produces an executable Spring Boot JAR. Runtime credentials remain environment-only. The release configuration defaults to the `production` profile, AI off, external verification off, manual transfers off until explicitly configured, and existing live payment/payout restrictions remain.

After verification, build with `./mvnw -Pproduction-release -Dmaven.test.skip=true package`, then run `python3 ops/check-release-artifact.py target/production-release/backend-0.0.1-SNAPSHOT.jar`. The profile uses a separate `target/production-release` build directory and forces JAR recreation so normal development resources/test reports are not overwritten or silently reused. Keep this separate from ordinary local development packaging. The check verifies the Boot launcher/application class, configuration marker, default profile, absence of example files, and absence of literal sensitive property assignments. It does not replace artifact signing, a full secret/dependency/SBOM scan or release approval. Preserve test reports and the exact source revision/hash with the artifact. Do not deploy a development or `.original` JAR.

Minimal `/actuator/health`, `/actuator/health/liveness` and `/actuator/health/readiness` GET paths are public; production hides components/details. Metrics remain platform-admin-only. Production readiness includes DB connectivity; liveness does not depend on DB availability. Use readiness for traffic admission and keep the management/backend surface private at ingress where possible. DNS/TLS/client-IP policy, monitoring scrape/alerts and endpoint routing still require deployment validation.

Backend test commands and Docker troubleshooting: [Testing guide](../docs/TESTING.md). `./mvnw test` is Docker-free; `./mvnw verify` includes the PostgreSQL integration suite and requires Docker.
