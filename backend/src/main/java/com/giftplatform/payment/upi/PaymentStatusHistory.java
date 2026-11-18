package com.giftplatform.payment.upi;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/** Writes and reads the append-only guest_payment_status_events table. Callers run inside their transaction. */
@Component
public class PaymentStatusHistory {
    private final JdbcTemplate jdbc;
    public PaymentStatusHistory(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public record Entry(String previousStatus, String newStatus, String actorType, String evidence, OffsetDateTime at) {}

    public void record(UUID eventId, UUID attemptId, UUID claimId, UpiPaymentStatus previous, UpiPaymentStatus next,
                       UpiPaymentStatus.Actor actor, UUID actorId, String evidence) {
        jdbc.update("""
            INSERT INTO guest_payment_status_events(event_id,attempt_id,claim_id,previous_status,new_status,actor_type,actor_id,evidence)
            VALUES(?,?,?,?,?,?,?,?)
            """, eventId, attemptId, claimId, previous == null ? null : previous.name(), next.name(), actor.name(), actorId, evidence);
    }

    /** History for one claim, including the attempt that led to it. Actor ids are not exposed. */
    public List<Entry> forClaim(UUID eventId, UUID claimId) {
        return jdbc.query("""
            SELECT previous_status,new_status,actor_type,evidence,created_at FROM guest_payment_status_events
            WHERE event_id=? AND (claim_id=? OR attempt_id=(SELECT attempt_id FROM guest_direct_transfer_claims WHERE id=? AND event_id=?))
            ORDER BY id
            """, (rs, i) -> new Entry(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4),
                rs.getObject(5, OffsetDateTime.class)), eventId, claimId, claimId, eventId);
    }
}
