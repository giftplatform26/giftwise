PIN identity and administrative audit
=====================================

Group creator PIN checks now use invitation/event + normalized username + PIN. Member checks use group + normalized username + PIN. Different usernames may share a PIN. Existing username uniqueness remains: the same username with a different PIN is still a duplicate username. Duplicate identity/PIN attempts return “PIN already taken for this group. Choose a different PIN”. Registration, signed-in PIN changes and email PIN recovery follow the same rule. Existing credentials do not need migration.

Activity & audit uses GET `/api/admin/console/audit/page`. It requires a platform administrator. Supported query parameters are `page` (zero-based), `search`, `action`, `module`, `from` and `to` (ISO dates). Pages contain at most 20 records, sorted by timestamp and ID descending. Date bounds use UTC; the end date includes the whole day. Search covers action, module, administrator username/email, actor ID and target ID. Responses include `entries`, `total`, `page`, `pageSize`, `totalPages` and available `actions`. The original `/audit` endpoint remains for dashboard recent activity.

Apply V89 to add the audit pagination index. Frontend Activity & audit provides Apply/Clear filters, Previous/Next navigation and record counts. The main admin Refresh button reloads the current filters and page.
