# GiftWise file cleanup on 6 November 2026

Moved 321 historical entries containing 403 files (4,922,317 bytes) from `/Users/jijujones/gift-platform` to `/Users/jijujones/useless files/gift-platform-2026-11-06`.

No files were permanently deleted. Original paths, reasons and per-file SHA256 values are retained in the archive's `manifest.json`; checksums were verified after moving. Empty historical backup directories are included in the entry count. Historical database recovery exports remain recoverable in the archive.

Archived: timestamped source backup directories; `.bak`/`.backup` patch copies; old source-handoff ZIPs and build log; superseded handover v1/v2; old batch installation notes/installer; generated deployment-kit Python bytecode.

Kept: backend/web/marketing/mobile source, Git metadata, environment/configuration files, dependency folders, tests, migrations, current handover, current technical/application catalogues, operational backup scripts and deployment kit, and feature/design documentation useful for future maintenance. `website` is the separate marketing application identified by the project README, not an expendable duplicate. Dependencies and generated build artifacts were not removed during this cleanup.

The project-level [documentation index](../../DOCUMENTATION_INDEX.md) points to the maintained entry points. To restore an archived item, inspect the manifest, ensure its original destination is absent, then move it back without overwriting current work. Do not run old installers against the current application.
