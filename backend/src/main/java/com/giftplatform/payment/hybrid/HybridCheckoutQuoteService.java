package com.giftplatform.payment.hybrid;

import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Safe quote preview. Existing Razorpay checkout remains the only verified
 * collection route. Never represent an unverified direct UPI QR as paid.
 * Surcharges are ZERO until method-specific legal/provider rules are implemented.
 */
@Service
public class HybridCheckoutQuoteService {
    private final JdbcTemplate jdbc;
    public HybridCheckoutQuoteService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public record Quote(UUID quoteId, UUID eventId, UUID giftId,
                        long contributionPaise, long processingFeePaise,
                        long totalPaise, String method,
                        String notice) {}

    @Transactional
    public Quote quote(UUID eventId, UUID giftId, long contributionPaise) {
        if (eventId == null || contributionPaise <= 0 || contributionPaise > 100_000_000L) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid contribution");
        }
        Integer valid = jdbc.queryForObject("""
            SELECT COUNT(*) FROM events e
            WHERE e.id = ? AND e.status = 'PUBLISHED'
              AND (?::uuid IS NULL OR EXISTS (
                  SELECT 1 FROM gifts g WHERE g.id = ? AND g.event_id = e.id
                    AND g.status = 'ACTIVE' AND g.visibility = 'PUBLIC'))
            """, Integer.class, eventId, giftId, giftId);
        if (valid == null || valid != 1)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invitation unavailable");
        UUID quoteId = UUID.randomUUID();
        jdbc.update("""
            INSERT INTO hybrid_checkout_quotes
            (id,event_id,gift_id,contribution_paise,processing_fee_paise,total_paise,method)
            VALUES (?,?,?,?,0,?,'RAZORPAY_CHECKOUT')
            """, quoteId,eventId,giftId,contributionPaise,contributionPaise);
        return new Quote(quoteId,eventId,giftId,contributionPaise,0,contributionPaise,
            "RAZORPAY_CHECKOUT",
            "No extra processing charge is added. Final provider fees and host settlement remain subject to provider terms.");
    }
}
