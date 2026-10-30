package com.giftplatform.payment.settlement;

import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Internal only. Cancellation releases holds; observations never settle or pay funds. */
@Service
public class PayoutSafetyFinalizationService {
    private final JdbcTemplate jdbc;
    public PayoutSafetyFinalizationService(JdbcTemplate jdbc) { this.jdbc=jdbc; }
    public record Cancellation(UUID requestId, boolean alreadyCancelled, int releasedHolds) {}
    public record Observation(UUID id, UUID requestId, String source, String observedState) {}

    @Transactional
    public Cancellation cancel(UUID hostId, UUID requestId) {
        if (hostId == null || requestId == null) throw new IllegalArgumentException("Host and request required");
        // Lock the request first. Reservation creation also locks this row in the
        // companion patch below, so cancellation and reservation cannot race.
        List<Object[]> rows=jdbc.query("""
            SELECT host_id,event_id,state,cancelled_at FROM host_payout_requests
            WHERE id=? FOR UPDATE
            """,(rs,i)->new Object[]{rs.getObject(1,UUID.class),rs.getObject(2,UUID.class),
                rs.getString(3),rs.getTimestamp(4)},requestId);
        if (rows.size()!=1 || !hostId.equals(rows.get(0)[0]))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Payout request not found");
        if (rows.get(0)[3]!=null) return new Cancellation(requestId,true,0);
        if (!"BLOCKED".equals(rows.get(0)[2]))
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Request not cancellable");
        UUID eventId=(UUID)rows.get(0)[1];
        jdbc.queryForObject("SELECT pg_advisory_xact_lock(hashtextextended(?, 0))",
            Object.class,"payout-reservation:"+eventId);
        int released=jdbc.update("""
            UPDATE payout_reservations SET state='RELEASED',released_at=CURRENT_TIMESTAMP
            WHERE payout_request_id=? AND event_id=? AND state='HELD'
            """,requestId,eventId);
        int changed=jdbc.update("""
            UPDATE host_payout_requests SET cancelled_at=CURRENT_TIMESTAMP,
            cancellation_reason='HOST_CANCELLED' WHERE id=? AND cancelled_at IS NULL
            """,requestId);
        if (changed!=1) throw new IllegalStateException("Cancellation race");
        return new Cancellation(requestId,false,released);
    }

    @Transactional
    public Observation recordInternalObservation(UUID requestId,String state) {
        if (requestId==null || !java.util.Set.of("UNKNOWN","PENDING","FAILED","SUCCEEDED").contains(state))
            throw new IllegalArgumentException("Invalid observation");
        Integer count=jdbc.queryForObject("SELECT COUNT(*) FROM host_payout_requests WHERE id=?",
            Integer.class,requestId);
        if (count==null || count!=1) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Request not found");
        UUID id=UUID.randomUUID();
        jdbc.update("""
            INSERT INTO payout_reconciliation_observations
            (id,payout_request_id,source,observed_state)
            VALUES (?,?,'INTERNAL_REVIEW',?)
            """,id,requestId,state);
        return new Observation(id,requestId,"INTERNAL_REVIEW",state);
    }
}
