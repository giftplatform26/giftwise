# GiftWise production deployment handoff

Read GiftWise_Production_Movement_Steps.docx first. Production_Movement_Steps.md contains the same instructions with copyable commands. Complete the owner input worksheet before starting.

Runbook version 1.1, 6 November 2026. Schema head V107. The kit directory contains the referenced companion scripts and service templates. Keep those files together and follow the runbook in order. Server setup targets a fresh dedicated Debian 13 Linux x86_64 server; never run it on a development computer or an existing shared server.

This is a nonpayment launch procedure. Live payments require a separately implemented and verified application release. The ZIP contains documentation and operational tooling, not an application binary, credentials or customer data. Build the application archive through Steps 1 to 3, or obtain it with a trusted checksum from the release engineer.

The owner must supply domain/server access, approved legal text, email configuration and offsite recovery inputs. A new database receives migration seed data. Configure real branding/provider settings and provision the first administrator as instructed.

The updated web service reads /etc/giftwise/web.env for server-only sharing-preview origins; fresh configuration creates it and encrypted configuration backup includes it. Existing installations must review the growth release's upgrade instructions before replacing their service template. The detailed growth release is in backend/docs/application/releases/2026-11-06_GROWTH_AND_PERFORMANCE.md in the source handoff.

The guide covers target-server checks that the operator must still perform. No production deployment or live provider transaction was performed while preparing it.
