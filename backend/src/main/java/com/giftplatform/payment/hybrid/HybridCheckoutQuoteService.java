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
 * Platform fees come from administrator-managed monetization settings.
 */
@Service
public class HybridCheckoutQuoteService {
    @org.springframework.beans.factory.annotation.Autowired private com.giftplatform.payment.control.PaymentControlService controls;
    private final JdbcTemplate jdbc;
    private final com.giftplatform.monetization.MonetizationService monetization;
    public HybridCheckoutQuoteService(JdbcTemplate jdbc, com.giftplatform.monetization.MonetizationService monetization) {
        this.jdbc=jdbc;this.monetization=monetization;
    }

    public record Quote(UUID quoteId, UUID eventId, UUID giftId,
                        long contributionPaise, long processingFeePaise,
                        long totalPaise, String method,
                        String notice, long platformFeePaise, long hostReceivesPaise, String feeBearer, long revision) {}

    @Transactional
    public Quote quote(UUID eventId, UUID giftId, long contributionPaise) {
        if (eventId == null || contributionPaise <= 0 || contributionPaise > 100_000_000L) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid contribution");
        }
        Integer valid = jdbc.queryForObject("""
            SELECT COUNT(*) FROM events e
            WHERE e.id = ? AND e.status = 'PUBLISHED' AND gifting_enabled
              AND (?::uuid IS NULL OR EXISTS (
                  SELECT 1 FROM gifts g WHERE g.id = ? AND g.event_id = e.id
                    AND g.status = 'ACTIVE' AND g.visibility = 'PUBLIC'))
            """, Integer.class, eventId, giftId, giftId);
        if (valid == null || valid != 1)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invitation unavailable");
        if(controls!=null)controls.require(eventId,"RAZORPAY");
        var pricing=monetization.quote(giftId==null?"WALLET":"CONTRIBUTION",contributionPaise);
        long surcharge=pricing.totalPaise()-contributionPaise;
        UUID quoteId = UUID.randomUUID();
        jdbc.update("""
            INSERT INTO hybrid_checkout_quotes
            (id,event_id,gift_id,contribution_paise,processing_fee_paise,total_paise,method)
            VALUES (?,?,?,?,?,?,'RAZORPAY_CHECKOUT')
            """, quoteId,eventId,giftId,contributionPaise,surcharge,pricing.totalPaise());
        return new Quote(quoteId,eventId,giftId,contributionPaise,surcharge,pricing.totalPaise(),
            "RAZORPAY_CHECKOUT", pricing.platformFeePaise()==0?
            "No extra processing charge is added. Final provider fees and host settlement remain subject to provider terms.":
            "Platform fee is "+("HOST".equals(pricing.feeBearer())?"deducted from the host receipt.":"added to your contribution."),
            pricing.platformFeePaise(),pricing.hostReceivesPaise(),pricing.feeBearer(),pricing.revision());
    }
}
