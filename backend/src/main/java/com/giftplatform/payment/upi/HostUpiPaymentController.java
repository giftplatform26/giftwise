package com.giftplatform.payment.upi;

import com.giftplatform.security.ActorContext;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/**
 * Host view of one guest payment claim: the server-fixed attempt, the untrusted UPI app response, the screenshot
 * and the status history. Only the event owner can read it. Confirm/reject stays on
 * {@code POST /api/v1/events/{eventId}/transactions/claims/{claimId}/decision}.
 */
@RestController
@RequestMapping("/api/v1/events/{eventId}/transactions/claims/{claimId}")
public class HostUpiPaymentController {
    private final JdbcTemplate db;
    private final ActorContext actor;
    private final PaymentStatusHistory history;

    public HostUpiPaymentController(JdbcTemplate db, ActorContext actor, PaymentStatusHistory history) {
        this.db = db; this.actor = actor; this.history = history;
    }

    public record AppResponse(String status, String responseCode, String txnId, String approvalRef, Long amountPaise,
                              boolean amountMismatch, String source, OffsetDateTime receivedAt) {}
    public record Details(UUID claimId, String status, String reportedVia, long amountPaise, String paymentCode,
                          String referenceTail, String transactionRef, String platform, OffsetDateTime attemptCreatedAt,
                          AppResponse appResponse, boolean hasScreenshot, List<PaymentStatusHistory.Entry> history, String note) {}

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<Details> details(@PathVariable UUID eventId, @PathVariable UUID claimId) {
        owned(eventId);
        List<Details> rows = db.query("""
            SELECT c.id,c.status,c.reported_via,c.amount_paise,c.payment_code,c.reference_tail,a.transaction_ref,a.platform,a.created_at,
                   a.response_status,a.response_code,a.response_txn_id,a.response_approval_ref,a.response_amount_paise,
                   COALESCE(a.response_amount_mismatch,FALSE),a.response_source,a.response_received_at,
                   EXISTS(SELECT 1 FROM guest_payment_evidence ev WHERE ev.claim_id=c.id)
            FROM guest_direct_transfer_claims c LEFT JOIN guest_upi_payment_attempts a ON a.id=c.attempt_id
            WHERE c.id=? AND c.event_id=?
            """, (rs, i) -> new Details(rs.getObject(1, UUID.class), UpiPaymentStatus.fromStored(rs.getString(2)).name(), rs.getString(3),
                rs.getLong(4), rs.getString(5), rs.getString(6), rs.getString(7), rs.getString(8), rs.getObject(9, OffsetDateTime.class),
                rs.getString(10) == null ? null : new AppResponse(rs.getString(10), rs.getString(11), rs.getString(12), rs.getString(13),
                    (Long) rs.getObject(14), rs.getBoolean(15), rs.getString(16), rs.getObject(17, OffsetDateTime.class)),
                rs.getBoolean(18), List.of(),
                "Guest reports, UPI app responses and screenshots are not proof of payment. Check your bank or UPI app before confirming."),
            claimId, eventId);
        if (rows.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found");
        Details d = rows.get(0);
        Details withHistory = new Details(d.claimId(), d.status(), d.reportedVia(), d.amountPaise(), d.paymentCode(), d.referenceTail(),
            d.transactionRef(), d.platform(), d.attemptCreatedAt(), d.appResponse(), d.hasScreenshot(), history.forClaim(eventId, claimId), d.note());
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(withHistory);
    }

    @GetMapping("/evidence")
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> evidence(@PathVariable UUID eventId, @PathVariable UUID claimId) {
        owned(eventId);
        List<byte[]> rows = db.query("SELECT image FROM guest_payment_evidence WHERE claim_id=? AND event_id=?",
            (rs, i) -> rs.getBytes(1), claimId, eventId);
        if (rows.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Screenshot not found");
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).contentType(MediaType.IMAGE_JPEG)
            .header("X-Content-Type-Options", "nosniff").header("Content-Security-Policy", "default-src 'none'; sandbox")
            .header("Content-Disposition", "inline; filename=\"payment-screenshot.jpg\"").body(rows.get(0));
    }

    private void owned(UUID eventId) {
        Integer count = db.queryForObject("SELECT count(*) FROM events WHERE id=? AND owner_id=?", Integer.class,
            eventId, actor.requireAuthenticatedUserId());
        if (count == null || count != 1) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found");
    }
}
