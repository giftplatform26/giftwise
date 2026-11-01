# Gift Platform — application documentation package

This is the engineering entry point for the inspected application working tree. It contains the three requested documents and source-backed reference catalogues. The documents are linked parts of one package, not three isolated summaries.

**Baseline date:** 2026-11-01. **Committed reference:** `79eb947b9097a6adb80197729337dd5094ec6494`. **Working tree:** contains uncommitted application changes; source hashes identify the actual snapshot. **Database baseline:** fresh PostgreSQL after Flyway V95. This is not a deployed-production attestation.

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
| [Migration catalogue](MIGRATION_CATALOGUE.md) | V1–V95 source/hashes/object changes. |
| [Dependency catalogue](DEPENDENCY_CATALOGUE.md) | Declared Maven dependencies and resolved npm lockfile packages, with managed-version limitations. |
| [Coverage interpretation](COVERAGE_REPORT.md) | What is structurally complete, what cannot be established from source, and validation evidence. |
| [Tooling and refresh guide](tooling/README.md) | Reproducible metadata export, AST/source scans, catalogue generation and validation. |

## Machine-readable handoff

JSON exports are in [catalogue/](catalogue/): `database.json`, `api-runtime.json`, `api-operations.json`, `frontend.json`, `backend-source.json`, `dependencies.json`, `coverage.json` and `validation.json`. They are intended for search, contract review, QA traceability and AI-assisted maintenance. They contain schema/code metadata, not production customer rows or plaintext credentials.

## Inventory at this baseline

Counts are explicit discovered declarations/objects, not a fabricated percentage of undocumented product intent. A generated row/option may instantiate one control declaration multiple times. The table/view column count includes view columns. A foreign-key column count differs from a foreign-key-constraint count. Runtime handler mappings differ from source-declared operations when properties disable beans.

| Inventory metric | Count |
| --- | --- |
| javaFiles | 273 |
| pageRoutes | 27 |
| tsxFiles | 92 |
| jsxControlDeclarations | 905 |
| fieldDeclarations | 260 |
| buttonDeclarations | 293 |
| runtimeHandlerMappings | 231 |
| sourceHandlerMappings | 265 |
| expandedApiOperations | 265 |
| conditionalUnregisteredOperations | 34 |
| apiModels | 165 |
| baseTables | 87 |
| views | 3 |
| columnsIncludingViews | 790 |
| constraints | 459 |
| foreignKeyColumns | 139 |
| indexes | 220 |
| triggers | 21 |
| routines | 10 |
| guardSites | 908 |
| validationAnnotationSites | 113 |
| configurationKeys | 61 |
| migrations | 95 |

## Evidence and precedence

1. Runtime schema exported from an isolated migrated database is the physical schema reference.
2. Runtime route mappings plus reflected/source-declared conditional mappings define the API inventory.
3. Source records, guards, component AST, source hashes and migration definitions are the executable behavior reference.
4. The narrative explains those facts and points out limits; older docs in the parent `docs/` directory can describe an earlier stage and are historical context.
5. Deployment, provider enrollment/credits, legal requirements, traffic forecasts and operating procedures are **unknown** unless separately attested. Proposed production defaults are labelled as proposals, not silently assumed implemented.

Last observed full application backend suite before documentation: **678 tests passed**. The documentation-only schema/route export passed on its own isolated PostgreSQL database. Current package validation is recorded in `catalogue/validation.json`. A fresh full production/browser/load/security acceptance run was not performed as part of writing documents.

## How to use this package

- New developer: read Documents 1 and 2, choose a route, inspect its control/API/model/DB/guard entries, then run the development/test workflow in Document 3.
- QA: map stable UI/API/rule IDs to happy, boundary, cross-owner, replay, session/feature-disabled, provider-failure and empty/loading cases. Source guard coverage is not branch-test coverage.
- DevOps: treat the last gap register and go-live checklist as release gates; do not copy private local settings or assume the local proxy/filesystem/session topology is production-ready.
- Product/architect: resolve absent business rationale, unsupported refunds/payouts, retention/consent and operational ownership before making promises beyond implemented behavior.
- AI maintainer: use source hashes and metadata as context; verify the actual working tree and do not invent unresolved dynamic schemas or private configuration. Regenerate after source changes.

No application source or live records were intentionally modified to create the documentation. The temporary metadata-export test was removed; its reproducible template remains under tooling.
