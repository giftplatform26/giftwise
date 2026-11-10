# Gift Platform — application documentation package

This is the engineering entry point for the inspected application working tree. It contains the three requested documents and source-backed reference catalogues. The documents are linked parts of one package, not three isolated summaries.

**Baseline date:** 2026-11-06. **Committed reference:** `809a8ad6f762d0038fcedcb5e3e3a078eeb33285`. **Working tree:** contains uncommitted application changes; source hashes identify the actual snapshot. **Database baseline:** fresh PostgreSQL after Flyway V107. This is not a deployed-production attestation.

## Current release

The [6 November growth and performance release](releases/2026-11-06_GROWTH_AND_PERFORMANCE.md) describes the current changes and admin activation. Source/schema catalogues are refreshed through **V107**. Earlier dated validation and inventory statements below are historical baselines; use the current generated coverage report and the release validation record for this working tree.

## Implementation updates

- [Platform localisation](releases/2026-11-02_LOCALIZATION.md) — eight language records including Malayalam/Kannada/Telugu; admin/host/event controls, guest overrides, translation publishing, RTL and communication fallback.

- [2026-11-02 controlled rollout](releases/2026-11-02_CELEBRATIONS_AND_SAFEGUARDS.md) — delivered behavior, admin activation, APIs/fields, V96 migration, compatibility, validation and remaining five-stage roadmap.
- [Operations toolkit](../../ops/README.md) — opt-in production guard, backup/integrity/disposable restore and release checks.

## Start here

1. [Document 1 — Technical Design & Specification](01_TECHNICAL_SPECIFICATION.md): architecture, roles, features, rules, field mappings, API/security/integration contracts and data lifecycle.
2. [Document 2 — Application Flow Guide](02_APPLICATION_FLOWS.md): guest/host/admin journeys, screen and state behavior, wireframes and diagrams, including explicitly unsupported refund/live payout paths.
3. [Document 3 — Production Readiness & Operations](03_PRODUCTION_OPERATIONS.md): environments, configuration, infrastructure, storage, performance, monitoring, backups/DR, hardening, go-live checklist and the **final prioritized production gap register**.

## Complete reference catalogues

| Catalogue | Contents |
| --- | --- |
| [Screen catalogue](SCREEN_CATALOGUE.md) | All page routes, route composition, access, navigation, wireframe sections, field/button/dialog/status declarations and state bindings. |
| [UI controls](UI_CONTROLS.md) | Every discovered JSX/native form control, action, navigation/dialog/progress/alert/status declaration with source, full attributes, conditions, state and request references. Use stable UI IDs to search. |
| [API catalogue](API_CATALOGUE.md) | All source-declared operations, including conditional controllers; registered/not-registered distinction, request/response shapes, models, security, effects, error/retry boundaries and source links. |
| [API models](API_MODELS.md) | Reflected request/response classes/records/enums, field constraints and undeclared-default warnings. |
| [Database catalogue](DATABASE_CATALOGUE.md) | Every table/view column, default, type, nullability, constraint, index, direct Java consumer, trigger, view/routine definition and RLS observation. |
| [Relationship matrix](RELATIONSHIP_MATRIX.md) | Exact FK column pairs, including composite relationships and delete/update definitions. |
| [Full ER diagram](complete-er.mmd) | Mermaid source for every base table/column and FK association; use the smaller diagrams in Document 1 for orientation. |
| [Business-rule catalogue](BUSINESS_RULE_CATALOGUE.md) | All source throw/guard sites, validation annotation sites and links to DB-enforced invariants. |
| [Configuration catalogue](CONFIGURATION_CATALOGUE.md) | Explicit source configuration, defaults/layers, environment names, sensitive-value redaction and DB-backed controls. |
| [Migration catalogue](MIGRATION_CATALOGUE.md) | Source/hashes/object changes through the current exported migration head. |
| [Dependency catalogue](DEPENDENCY_CATALOGUE.md) | Declared Maven dependencies and resolved npm lockfile packages, with managed-version limitations. |
| [Coverage interpretation](COVERAGE_REPORT.md) | What is structurally complete, what cannot be established from source, and validation evidence. |
| [Tooling and refresh guide](tooling/README.md) | Reproducible metadata export, AST/source scans, catalogue generation and validation. |

## Machine-readable handoff

JSON exports are in [catalogue/](catalogue/): `database.json`, `api-runtime.json`, `api-operations.json`, `frontend.json`, `backend-source.json`, `dependencies.json`, `coverage.json` and `validation.json`. They are intended for search, contract review, QA traceability and AI-assisted maintenance. They contain schema/code metadata, not production customer rows or plaintext credentials.

## Current generated inventory

Counts are explicit discovered declarations/objects, not a fabricated percentage of undocumented product intent. A generated row/option may instantiate one control declaration multiple times. The table/view column count includes view columns. A foreign-key column count differs from a foreign-key-constraint count. Runtime handler mappings differ from source-declared operations when properties disable beans.

| Inventory metric | Count |
| --- | --- |
| javaFiles | 315 |
| pageRoutes | 34 |
| tsxFiles | 131 |
| jsxControlDeclarations | 1120 |
| fieldDeclarations | 318 |
| buttonDeclarations | 345 |
| runtimeHandlerMappings | 291 |
| sourceHandlerMappings | 325 |
| expandedApiOperations | 325 |
| conditionalUnregisteredOperations | 34 |
| apiModels | 194 |
| baseTables | 120 |
| views | 3 |
| columnsIncludingViews | 1021 |
| constraints | 607 |
| foreignKeyColumns | 197 |
| indexes | 277 |
| triggers | 25 |
| routines | 11 |
| guardSites | 1029 |
| validationAnnotationSites | 178 |
| configurationKeys | 79 |
| migrations | 104 |

## Evidence and precedence

1. Runtime schema exported from an isolated migrated database is the physical schema reference.
2. Runtime route mappings plus reflected/source-declared conditional mappings define the API inventory.
3. Source records, guards, component AST, source hashes and migration definitions are the executable behavior reference.
4. The narrative explains those facts and points out limits; older docs in the parent `docs/` directory can describe an earlier stage and are historical context.
5. Deployment, provider enrollment/credits, legal requirements, traffic forecasts and operating procedures are **unknown** unless separately attested. Proposed production defaults are labelled as proposals, not silently assumed implemented.

Current 2026-11-02 validation: **685 backend unit/HTTP tests pass**, **405 frontend tests pass**, frontend TypeScript and production build pass, and isolated V97 schema/API export passes. Dedicated celebration integration tests pass; the full integration run completed 595 tests with one outdated host-visibility assertion; that test was corrected and passed an isolated rerun. Documentation structural validation is in `catalogue/validation.json`. Browser/device UAT, production load/security/DR and real provider acceptance have not been performed.

## How to use this package

- New developer: read Documents 1 and 2, choose a route, inspect its control/API/model/DB/guard entries, then run the development/test workflow in Document 3.
- QA: map stable UI/API/rule IDs to happy, boundary, cross-owner, replay, session/feature-disabled, provider-failure and empty/loading cases. Source guard coverage is not branch-test coverage.
- DevOps: treat the last gap register and go-live checklist as release gates; do not copy private local settings or assume the local proxy/filesystem/session topology is production-ready.
- Product/architect: resolve absent business rationale, unsupported refunds/payouts, retention/consent and operational ownership before making promises beyond implemented behavior.
- AI maintainer: use source hashes and metadata as context; verify the actual working tree and do not invent unresolved dynamic schemas or private configuration. Regenerate after source changes.

Application code now includes the controlled celebration/safeguard release linked above. No live customer records or provider transactions were used for its tests. The temporary metadata-export test was removed; its reproducible template remains under tooling.
