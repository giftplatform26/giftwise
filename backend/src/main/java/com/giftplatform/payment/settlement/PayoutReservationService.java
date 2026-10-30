package com.giftplatform.payment.settlement;

import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Internal reservation only. No API endpoint, provider call or ledger debit. */
@Service
public class PayoutReservationService {
    private final JdbcTemplate jdbc;
    private final SettlementBalanceService balances;
    public PayoutReservationService(JdbcTemplate jdbc, SettlementBalanceService balances) {
        this.jdbc = jdbc; this.balances = balances;
    }
    public record Reservation(UUID id, UUID requestId, long amountPaise, boolean existing) {}
    private record Request(UUID eventId, long amountPaise, String state) {}

    @Transactional
    public Reservation reserve(UUID payoutRequestId) {
        if (payoutRequestId == null) throw new IllegalArgumentException("request required");
        List<Request> requests = jdbc.query("""
            SELECT event_id,amount_paise,state FROM host_payout_requests WHERE id=? AND cancelled_at IS NULL FOR UPDATE
            """, (rs,i)->new Request(rs.getObject(1,UUID.class),rs.getLong(2),rs.getString(3)),payoutRequestId);
        if (requests.size()!=1) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Payout request not found");
        Request request = requests.get(0);
        if (!"BLOCKED".equals(request.state()))
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Payout request state invalid");
        // Serialize reservations for the same event across all application instances.
        jdbc.queryForObject("SELECT pg_advisory_xact_lock(hashtextextended(?, 0))",
            Object.class,"payout-reservation:"+request.eventId());
        List<Reservation> previous=jdbc.query("""
            SELECT id,amount_paise FROM payout_reservations
            WHERE payout_request_id=? AND state='HELD'
            """,(rs,i)->new Reservation(rs.getObject(1,UUID.class),payoutRequestId,rs.getLong(2),true),payoutRequestId);
        if (!previous.isEmpty()) {
            if (previous.size()!=1 || previous.get(0).amountPaise()!=request.amountPaise())
                throw new IllegalStateException("Inconsistent payout reservation");
            return previous.get(0);
        }
        // Released reservations cannot be recreated for the same request.
        Integer previousCount=jdbc.queryForObject(
            "SELECT COUNT(*) FROM payout_reservations WHERE payout_request_id=?",
            Integer.class,payoutRequestId);
        if (previousCount != null && previousCount > 0)
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Reservation already released");
        var balance=balances.read(request.eventId());
        if (request.amountPaise()<=0 || balance.withdrawablePaise()<request.amountPaise())
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Insufficient settled balance");
        UUID id=UUID.randomUUID();
        jdbc.update("""
            INSERT INTO payout_reservations(id,payout_request_id,event_id,amount_paise)
            VALUES (?,?,?,?)
            """,id,payoutRequestId,request.eventId(),request.amountPaise());
        return new Reservation(id,payoutRequestId,request.amountPaise(),false);
    }
}
