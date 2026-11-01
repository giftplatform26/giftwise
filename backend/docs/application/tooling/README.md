# Documentation tooling and reproducibility

These tools export metadata and build reference Markdown; they do not change application logic or deploy services. Run from the backend checkout. They require Java/Maven, Node with the web project's installed TypeScript dependency, Python 3, Docker/Testcontainers and enough disk/permissions for an ephemeral PostgreSQL container.

## Inputs and outputs

- `DocumentationSnapshotTest.java.template`: temporary Spring/Testcontainers metadata exporter. Bootstraps a fresh database, applies Flyway migrations, reads schema metadata, registered Spring handler mappings and reflected records/enums. Reflection also inventories conditional controllers without invoking their methods. It does not export customer rows, environment values, credentials or prompts.
- `scan-frontend.cjs`: TypeScript AST traversal of App Router pages/components, native controls, dialogs/alerts/status/progress, native browser dialogs, attributes, event handlers, state initializers, imports/re-exports/dynamic imports, API call expressions and generated-control collections.
- `scan-backend.py`: source file hashes, guards, validation annotations, source endpoint declarations, record declarations, safe configuration catalogue, provider URL declarations and migration metadata. Sensitive config values are redacted. This is not a full Java semantic analyzer.
- `build_catalogues.py`: physical DB, relationship/ER, API/model, UI, source-guard, configuration, migration and dependency reference documents.
- `build_screens.py`: every page route, source composition, access/purpose, wireframe sections, field/button/dialog/status controls and workflow-state bindings.
- `validate_package.py`: structural coverage, source/model/key counts, local documentation/source links and suspicious secret-pattern checks.

The narrative Documents 1–3, coverage interpretation, defaults and gaps require human review after source changes. Regenerating an inventory does not validate product intent, production configuration, every dynamic UI state or a provider contract. After a major feature change, rerun browser/contract tests and revise the main guides/gap register.

## Refresh procedure

1. Install declared dependencies with the normal development workflow; do not use production credentials or point the exporter at a production DB.
2. Confirm the working tree/release baseline and ensure no other process is compiling/changing the same sources.
3. Create a temporary test only if that filename is absent; copy the template to `src/test/java/com/giftplatform/documentation/DocumentationSnapshotTest.java`.
4. Run `./mvnw -Dtest=DocumentationSnapshotTest test`. It overrides the data source with its own disposable PostgreSQL container. Some profile-gated controllers will not register; the separate reflection/source catalogue includes them.
5. Remove only the temporary test/class files created by this step. The source template is retained here for future exports.
6. Run:

```sh
node docs/application/tooling/scan-frontend.cjs
python3 docs/application/tooling/scan-backend.py
python3 docs/application/tooling/build_catalogues.py
python3 docs/application/tooling/build_screens.py
python3 docs/application/tooling/validate_package.py
```

7. Refresh `catalogue/dependencies.json` from the reviewed pom.xml/package-lock baseline if dependencies changed. The current dependency export includes declared Maven coordinates and all npm lockfile package entries; a release SBOM/transitive Maven resolution is a separate production gap.
8. Review safe configuration output, unknown mappings and main guides; update README/coverage figures if the inventory changed. Commit documentation with the corresponding source release. Do not silently overwrite private configuration into the package.

## Limits to preserve explicitly

The AST inventories a control declaration and its dynamic source, not every future data-driven row/option. Native form constraints are not equivalent to server validation. API examples are schema shapes; maps/polymorphism and delegated business effects require contract review. Runtime schema describes a freshly migrated empty DB, not arbitrary production drift. No infrastructure, legal policy, load benchmark, full browser E2E certification or provider enrollment can be inferred from an inventory.
