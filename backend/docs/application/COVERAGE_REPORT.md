# Coverage, evidence and limits

## Current refresh on 6 November 2026

The regenerated package passes structural validation through migration V107. It includes 34 page routes, 325 declared API operations and 120 base tables. The current source/control/schema inventories and counts are in the README and machine-readable coverage catalogue. The dated baseline table below is retained as historical context; it is superseded by the refreshed catalogues for changed items.

## Historical structural coverage at the earlier inspected baseline

| Requested dimension | Exported evidence | Structural status | Semantic / operational limit |
| --- | --- | --- | --- |
| Screens/routes | 28 App Router page routes plus composed dialogs/tabs/native dialogs | All discovered routes have screen entries | Runtime feature/data combinations and browser behavior require E2E review; 28 routes is not the number of modal/tab states. |
| Fields/buttons/UI | 972 declarations; 288 native field declarations; 306 buttons | Each has a stable UI entry and source/attributes/conditions | Data-driven instances/options and universally verified UI→API→DB semantic mapping cannot be asserted by a static AST. Unknowns are explicit. |
| APIs | 290 declared operations; 256 runtime mappings; 34 unregistered conditional operations | Source/runtime declarations reconcile and are catalogued | Dynamic Map response keys, error envelopes and delegated effects need formal contract review; examples are shape illustrations. |
| Database | 97 base tables + 3 views, 849 columns, 496 constraints, 231 indexes | All exported schema objects have references | Fresh V97 schema is not evidence that an arbitrary live DB has no drift or invalid historical data. |
| Relationships | 143 FK column relationships, triggers/routines/views | Physical matrix and full ER source included | Conceptual relationships/cardinality must also respect nullable/unique/composite rules. |
| Rules | 920 source guard sites and 126 validation annotation sites; SQL constraints | Every discovered site indexed | These are enforcement sites, not proof of every business requirement, exception, rationale or tested branch. |
| Configuration | 74 explicit property keys plus DB control tables | Each explicit key has declarations, safe values, env bindings and production concerns | Framework/deployment implicit options, external secret values, provider entitlements and production overrides remain unknown. |
| Integrations | Actual provider/client/transport/storage source and capability matrix | Implemented and unavailable/scaffolded integrations distinguished | No external enrollment, production delivery/payment/payout/credit balance or provider SLA was attested. |
| Operations | Environment/runbook/checklist/DR/scaling/retention guidance and final gaps | Requested operational topics addressed | Proposed defaults have not been deployed or validated as production SLOs/RTO/RPO. |

“100%” is supportable only for the discovered source/metadata inventories reconciled by the validator. It is **not** claimed for formal product intent, every runtime field instance, all possible errors, untested UI states or unknown production infrastructure. Claiming those would contradict the instruction not to make assumptions.

## Validation

The package validator checks route/control/API/config/schema/guard inclusion, source file hashes, local link targets and credential-like literal patterns. It writes machine-readable pass/fail/check details to [validation.json](catalogue/validation.json). The source schema/API exporter was run in an isolated disposable database, not against customer data. No business endpoint or paid provider request is invoked by that exporter.

Current 2026-11-02 evidence: 685 backend unit/HTTP tests pass; 405 frontend tests, TypeScript and the production build pass. Dedicated celebration permission/privacy/concurrency/calendar/HTML tests and isolated V97 schema/API export pass. Three backup-integrity tests pass. The full integration run completed 595 tests with one outdated host-visibility assertion; that test was corrected and passed an isolated rerun. These tests do not certify production deployment, external payment/delivery, browser/device UAT, load capacity, DR timing, penetration testing or legal approval.

## Missing information and proposed defaults

Missing information is not concealed behind plausible values. Read the last section of [Document 3](03_PRODUCTION_OPERATIONS.md): it lists prioritized gaps, proposed decisions/defaults, suggested accountable roles and acceptance evidence. In particular, it calls out undocumented business rationale and incomplete formal dynamic API/field mappings rather than certifying them from a parser.

## Maintenance

Keep the three narrative documents and final gap register in sync with reviewed requirements. Regenerate catalogues after route/model/control/schema/config changes. Add contract/E2E identifiers to the stable UI/API/rule inventories as they become available. A future developer should use source evidence to implement a change, not assume an illustrative request or unimplemented proposed topology is already a supported production contract.
