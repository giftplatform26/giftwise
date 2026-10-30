package com.giftplatform.payment.settlement;

import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/** Ledger-backed informational balance; never counts HOST_PENDING as withdrawable. */
@Service
public class SettlementBalanceService {
    private final JdbcTemplate jdbc;
    public SettlementBalanceService(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    public record Balance(long availablePaise, long heldPaise, long withdrawablePaise) {}

    public Balance read(UUID eventId) {
        if (eventId == null) throw new IllegalArgumentException("eventId required");
        Long available = jdbc.queryForObject("""
            SELECT COALESCE(SUM(amount_paise),0)
            FROM wallet_ledger_entries
            WHERE event_id=? AND account_code='HOST_AVAILABLE' AND currency='INR'
            """, Long.class, eventId);
        Long held = jdbc.queryForObject("""
            SELECT COALESCE(SUM(amount_paise),0)
            FROM payout_reservations WHERE event_id=? AND state='HELD'
            """, Long.class, eventId);
        long a = available == null ? 0 : available;
        long h = held == null ? 0 : held;
        if (a < 0 || h < 0) throw new IllegalStateException("Inconsistent settlement balance");
        return new Balance(a,h,Math.max(0,Math.subtractExact(a,h)));
    }
}
