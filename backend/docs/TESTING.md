# Running backend tests

Use Java 21 and the Maven wrapper from the backend directory.

## Unit and HTTP slice tests (Docker not required)

```sh
./mvnw clean test
```

Surefire runs the unit/slice tests. Tests that create PostgreSQL containers use the `*IntegrationTest`, `*IT` or `*ITCase` naming convention and run through Failsafe instead. New tests using Testcontainers must follow that convention, including migration, concurrency and database-integrity tests.

The POM configures headless AWT for image tests and the Mockito Java agent for both runners; no machine-specific `-DargLine` path is needed.

## Full verification (Docker required)

Start Docker Desktop, wait until its engine is ready, then run:

```sh
docker info
./mvnw clean verify
```

`verify` runs both unit and real PostgreSQL integration tests. Docker must be reachable from the same terminal or IntelliJ process that launches Maven. PostgreSQL test images are disposable containers; do not point these tests at a live GiftWise database.

To run only one integration class after test compilation:

```sh
./mvnw test-compile failsafe:integration-test failsafe:verify -Dit.test=BackendApplicationIntegrationTest
```

## Docker errors

“Could not find a valid Docker environment” means Testcontainers could not access a running Docker engine. “Previous attempts to find a Docker environment failed” is the cached consequence of that first failure, not a separate database failure.

1. Start Docker Desktop and wait for `docker info` to succeed.
2. Confirm `docker context show` identifies the intended engine (normally `desktop-linux` for Docker Desktop on this Mac).
3. Remove stale Docker environment overrides from the terminal/IntelliJ run configuration if they point to an old engine; do not override a working context unnecessarily.
4. Restart the Maven test run. Testcontainers caches discovery failures within the test JVM, so an already-failed run will not recover when Docker starts later.
5. If running through a sandbox or remote runner, ensure that runner is allowed to access the Docker socket. Docker working in another terminal does not prove the sandbox can reach it.

Do not disable PostgreSQL tests or replace them with an in-memory database to make a release check appear green. Unit-only results do not verify migrations or PostgreSQL concurrency/integrity.

## Verification after the Docker fix (2026-11-05)

- Docker Desktop was started and engine connectivity verified.
- All 548 existing unit/slice tests passed; the added migration-version guard also passed.
- 63 targeted real-PostgreSQL tests passed across application startup, foundation migrations/integrity, allocation integrity, payment concurrency, settlement safety/audit and registration.
- Fresh PostgreSQL databases successfully applied all 106 migrations.
- The readiness consent SQL migration is V106; the existing V102 Java stationery migration is unchanged. Use a clean build once after updating to remove old compiled test classes and the stale V102 SQL resource.
- These results cover the listed targets, not a claim that every integration class has been run.

## Growth features suites (2026-11-11)

Targeted integration suites for the G1–G4 features (Docker required):

```sh
./mvnw test -Dtest='LiveWallRegisterLedgerIntegrationTest,CoHostFunctionsIntegrationTest,WhatsAppIntegrationTest,GrowthBatchFourIntegrationTest,EventManagementIntegrationTest,AcquisitionIntegrationTest,ImprovementsIntegrationTest'
```

They cover the platform/host/event switches, co-host roles, register idempotency and CSV safety, WhatsApp webhook signatures and opt-outs, referral rules, yearly re-hosting and the charity payment guard. The WhatsApp suite disables the background worker and calls it directly.

For browser testing, run the web app as a production build: `cd web && npm run preview`. Development mode (`npm run dev`) compiles each page on its first visit (0.7–3.9 s measured), which looks like slowness but isn't representative.

`./mvnw test` runs unit tests only. To run every integration class: `./mvnw test-compile failsafe:integration-test failsafe:verify` (about 25 minutes, Docker required).

## Continuous integration

`.github/workflows/ci.yml` runs on every push and pull request: backend unit tests, all backend integration tests (Testcontainers on the GitHub runner), the web app (type-check, tests, build), the website (type-check, build, tests) and the mobile shell tests. Because `application.properties` is not committed, CI copies `application.properties.example` and generates throwaway payment keys for each run.

## Load test

`backend/ops/loadtest/loadtest.mjs` simulates many guests and hosts at once. See `docs/PRODUCTION_READINESS_2026-11.md` for usage and the latest results.
