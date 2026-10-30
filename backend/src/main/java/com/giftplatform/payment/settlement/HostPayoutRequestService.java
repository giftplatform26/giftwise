package com.giftplatform.payment.settlement;

import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Records blocked payout requests only. Does not reserve funds, debit wallet,
 * select fallback bank, call a provider, or claim payout eligibility.
 * Call only with server-derived authenticated host identity.
 */
@Service
public class HostPayoutRequestService {
    private final JdbcTemplate jdbc;
    public HostPayoutRequestService(JdbcTemplate jdbc) { this.jdbc=jdbc; }
    public record Result(UUID requestId, String state, String reason) {}
    @Transactional
    public Result request(UUID hostId, UUID eventId, UUID bankId,
                          long amountPaise, String idempotencyKey) {
        if (hostId == null || eventId == null || bankId == null ||
            amountPaise <= 0 || idempotencyKey == null ||
            idempotencyKey.isBlank() || idempotencyKey.length() > 120)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid payout request");
        jdbc.queryForObject("SELECT pg_advisory_xact_lock(hashtextextended(?, 0))",
                            Object.class,hostId + ":" + idempotencyKey);
        List<Object[]> existing = jdbc.query("""
            SELECT id,event_id,destination_id,amount_paise
            FROM host_payout_requests WHERE host_id=? AND idempotency_key=?
            FOR UPDATE
            """, (rs,i)->new Object[]{rs.getObject(1,UUID.class),
                                      rs.getObject(2,UUID.class),
                                      rs.getObject(3,UUID.class),rs.getLong(4)},
                                      hostId,idempotencyKey);
        if (!existing.isEmpty()) {
            Object[] row=existing.get(0);
            if (!eventId.equals(row[1]) || !bankId.equals(row[2]) ||
                amountPaise != (Long)row[3])
                throw new ResponseStatusException(HttpStatus.CONFLICT,"Idempotency conflict");
            return new Result((UUID)row[0],"BLOCKED","LIVE_PAYOUTS_DISABLED");
        }
        Integer eligible = jdbc.queryForObject("""
            SELECT COUNT(*) FROM events e JOIN host_bank_accounts b
              ON b.host_id=e.owner_id AND EXISTS (SELECT 1 FROM event_payment_account_links l WHERE l.event_id=e.id AND l.bank_account_id=b.id AND l.host_id=e.owner_id AND l.enabled)
            WHERE e.id=? AND e.owner_id=? AND b.id=?
              AND b.enabled=TRUE AND b.verification_status='VERIFIED'
            """,Integer.class,eventId,hostId,bankId);
        if (eligible == null || eligible != 1)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Eligible destination not found");
        UUID id=UUID.randomUUID();
        jdbc.update("""
            INSERT INTO host_payout_requests
            (id,host_id,event_id,destination_id,amount_paise,idempotency_key)
            VALUES (?,?,?,?,?,?)
            """,id,hostId,eventId,bankId,amountPaise,idempotencyKey);
        return new Result(id,"BLOCKED","LIVE_PAYOUTS_DISABLED");
    }
}
