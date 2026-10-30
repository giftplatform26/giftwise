package com.giftplatform.security.recovery;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Set;
import java.util.UUID;

/** Epoch storage only. Authentication paths must compare the epoch on each request. */
@Repository
public class CredentialSessionVersionStore {
    private static final Set<String> KINDS = Set.of("PLATFORM_ADMIN", "HOST", "GROUP_ADMIN", "GROUP_MEMBER");
    private final JdbcTemplate jdbc;
    public CredentialSessionVersionStore(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    private static void check(String kind, UUID id) {
        if (!KINDS.contains(kind) || id == null) throw new IllegalArgumentException("Invalid account");
    }
    public long current(String kind, UUID id) {
        check(kind, id);
        Long version = jdbc.query("SELECT version FROM credential_session_versions WHERE account_kind=? AND account_id=?",
                rs -> rs.next() ? rs.getLong(1) : 0L, kind, id);
        return version == null ? 0L : version;
    }
    @Transactional
    public long revokeAll(String kind, UUID id) {
        check(kind, id);
        Long version = jdbc.queryForObject("INSERT INTO credential_session_versions (account_kind,account_id,version) VALUES (?,?,1) " +
                "ON CONFLICT (account_kind,account_id) DO UPDATE SET version=credential_session_versions.version+1,updated_at=now() RETURNING version",
                Long.class, kind, id);
        if (version == null) throw new IllegalStateException("Could not revoke sessions");
        return version;
    }
}
