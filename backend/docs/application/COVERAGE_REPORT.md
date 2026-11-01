# Coverage, evidence and limits

## Structural coverage at the inspected baseline

| Requested dimension | Exported evidence | Structural status | Semantic / operational limit |
| --- | --- | --- | --- |
| Screens/routes | 27 App Router page routes plus composed dialogs/tabs/native dialogs | All discovered routes have screen entries | Runtime feature/data combinations and browser behavior require E2E review; 27 routes is not the number of modal/tab states. |
| Fields/buttons/UI | 905 declarations; 260 native field declarations; 293 buttons | Each has a stable UI entry and source/attributes/conditions | Data-driven instances/options and universally verified UI→API→DB semantic mapping cannot be asserted by a static AST. Unknowns are explicit. |
| APIs | 265 declared operations; 231 runtime mappings; 34 unregistered conditional operations | Source/runtime declarations reconcile and are catalogued | Dynamic Map response keys, error envelopes and delegated effects need formal contract review; examples are shape illustrations. |
| Database | 87 base tables + 3 views, 790 columns, 459 constraints, 220 indexes | All exported schema objects have references | Fresh V95 schema is not evidence that an arbitrary live DB has no drift or invalid historical data. |
| Relationships | 139 FK column relationships, triggers/routines/views | Physical matrix and full ER source included | Conceptual relationships/cardinality must also respect nullable/unique/composite rules. |
| Rules | 908 source guard sites and 113 validation annotation sites; SQL constraints | Every discovered site indexed | These are enforcement sites, not proof of every business requirement, exception, rationale or tested branch. |
| Configuration | 61 explicit property keys plus DB control tables | Each explicit key has declarations, safe values, env bindings and production concerns | Framework/deployment implicit options, external secret values, provider entitlements and production overrides remain unknown. |
| Integrations | Actual provider/client/transport/storage source and capability matrix | Implemented and unavailable/scaffolded integrations distinguished | No external enrollment, production delivery/payment/payout/credit balance or provider SLA was attested. |
| Operations | Environment/runbook/checklist/DR/scaling/retention guidance and final gaps | Requested operational topics addressed | Proposed defaults have not been deployed or validated as production SLOs/RTO/RPO. |

“100%” is supportable only for the discovered source/metadata inventories reconciled by the validator. It is **not** claimed for formal product intent, every runtime field instance, all possible errors, untested UI states or unknown production infrastructure. Claiming those would contradict the instruction not to make assumptions.

## Validation

The package validator checks route/control/API/config/schema/guard inclusion, source file hashes, local link targets and credential-like literal patterns. It writes machine-readable pass/fail/check details to [validation.json](catalogue/validation.json). The source schema/API exporter was run in an isolated disposable database, not against customer data. No business endpoint or paid provider request is invoked by that exporter.

The preceding full backend run passed 678 tests. Documentation metadata export tests passed separately. No new application implementation test was required because this task changes documentation/tooling rather than app behavior. Production deployment, real customer flow, full browser E2E, load/DR/pentest and current provider approvals remain outside the evidence collected here.

## Missing information and proposed defaults

Missing information is not concealed behind plausible values. Read the last section of [Document 3](03_PRODUCTION_OPERATIONS.md): it lists prioritized gaps, proposed decisions/defaults, suggested accountable roles and acceptance evidence. In particular, it calls out undocumented business rationale and incomplete formal dynamic API/field mappings rather than certifying them from a parser.

## Maintenance

Keep the three narrative documents and final gap register in sync with reviewed requirements. Regenerate catalogues after route/model/control/schema/config changes. Add contract/E2E identifiers to the stable UI/API/rule inventories as they become available. A future developer should use source evidence to implement a change, not assume an illustrative request or unimplemented proposed topology is already a supported production contract.
