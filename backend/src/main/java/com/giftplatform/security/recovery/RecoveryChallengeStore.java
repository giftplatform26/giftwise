package com.giftplatform.security.recovery;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.UUID;

/** Persistence only. No HTTP endpoints, identity proof, or credential changes. */
@Repository
public class RecoveryChallengeStore {
    private final JdbcTemplate jdbc;
    public RecoveryChallengeStore(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public void create(UUID id, String kind, UUID accountId, String channel, String purpose,
                       String destinationFingerprint, String secretHash, Instant expiry) {
        if (id == null || accountId == null || expiry == null || secretHash == null || !secretHash.matches("[0-9a-f]{64}"))
            throw new IllegalArgumentException("Invalid challenge");
        if (!java.util.Set.of("PLATFORM_ADMIN", "HOST", "GROUP_ADMIN", "GROUP_MEMBER").contains(kind)
                || !java.util.Set.of("EMAIL", "SMS").contains(channel)
                || !java.util.Set.of("VERIFY_CONTACT", "RESET_PASSWORD", "RESET_PIN").contains(purpose)
                || destinationFingerprint == null || !destinationFingerprint.matches("[0-9a-f]{64}"))
            throw new IllegalArgumentException("Invalid challenge binding");
        jdbc.update("INSERT INTO account_contact_challenges (id,account_kind,account_id,channel,purpose,destination_fingerprint,secret_hash,expires_at) VALUES (?,?,?,?,?,?,?,?)",
                id, kind, accountId, channel, purpose, destinationFingerprint, secretHash, java.sql.Timestamp.from(expiry));
    }

    /** Atomic attempt accounting; a wrong guess cannot consume a valid challenge. */
    @org.springframework.transaction.annotation.Transactional(propagation=org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public boolean verifyAndConsume(UUID id, String kind, UUID accountId, String purpose,
                                    String destinationFingerprint, String submittedHash) {
        if (id == null || accountId == null || kind == null || purpose == null || destinationFingerprint == null
                || submittedHash == null || !submittedHash.matches("[0-9a-f]{64}")) return false;
        return Boolean.TRUE.equals(jdbc.execute((org.springframework.jdbc.core.ConnectionCallback<Boolean>) connection -> {
            try (var lock = connection.prepareStatement(
                    "SELECT secret_hash,attempt_count FROM account_contact_challenges " +
                    "WHERE id=? AND account_kind=? AND account_id=? AND purpose=? AND destination_fingerprint=? " +
                    "AND consumed_at IS NULL AND expires_at>now() AND attempt_count<5 FOR UPDATE")) {
                lock.setObject(1,id); lock.setString(2,kind); lock.setObject(3,accountId);
                lock.setString(4,purpose); lock.setString(5,destinationFingerprint);
                try (var rows = lock.executeQuery()) {
                    if (!rows.next()) return false;
                    boolean match = RecoveryOtp.matches(rows.getString(1),submittedHash);
                    try (var update = connection.prepareStatement(match
                            ? "UPDATE account_contact_challenges SET consumed_at=now(),attempt_count=attempt_count+1 WHERE id=? AND consumed_at IS NULL"
                            : "UPDATE account_contact_challenges SET attempt_count=attempt_count+1 WHERE id=? AND consumed_at IS NULL")) {
                        update.setObject(1,id);
                        return update.executeUpdate()==1 && match;
                    }
                }
            }
        }));
    }
}
