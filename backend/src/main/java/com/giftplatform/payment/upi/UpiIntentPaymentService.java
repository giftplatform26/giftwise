package com.giftplatform.payment.upi;

import com.giftplatform.audit.entity.AuditAction;
import com.giftplatform.audit.entity.AuditActorType;
import com.giftplatform.audit.service.AuditService;
import com.giftplatform.hostpayment.qr.UpiQrImageCodec;
import com.giftplatform.hostpayment.qr.UpiQrPayload;
import com.giftplatform.payment.control.PaymentControlService;
import com.giftplatform.payment.direct.GuestDirectTransferService;
import com.giftplatform.payment.direct.GuestVerifiedDestinationService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Direct UPI intent payments from a guest straight to the host's own UPI ID.
 *
 * <p>The platform never receives or routes money. It fixes the payee, amount and references for each attempt,
 * builds the upi://pay link, stores the untrusted app response when one exists, and turns the guest's report into
 * the existing direct-transfer claim (AWAITING_VERIFICATION) that the host later confirms or rejects. Nothing here
 * posts to the wallet ledger or produces a verified contribution.
 */
@Service
public class UpiIntentPaymentService {
    static final String NOTE_PREFIX = "GiftWise ";
    private static final String CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final String REF_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final Set<String> PLATFORMS = Set.of("ANDROID", "IOS", "DESKTOP", "UNKNOWN");
    private static final Set<String> OPEN = Set.of("INITIATED", "PENDING", "FAILED");

    private final JdbcTemplate jdbc;
    private final GuestDirectTransferService direct;
    private final ObjectProvider<GuestVerifiedDestinationService> destinations;
    private final PaymentStatusHistory history;
    private final Clock clock;
    private final boolean enabled;
    private final boolean includeTransactionRef;
    private final Duration ttl;
    private final SecureRandom random = new SecureRandom();
    @Autowired(required = false) private PaymentControlService controls;
    @Autowired(required = false) private AuditService audit;

    @Autowired
    public UpiIntentPaymentService(JdbcTemplate jdbc, GuestDirectTransferService direct,
                                   ObjectProvider<GuestVerifiedDestinationService> destinations, PaymentStatusHistory history,
                                   @Value("${gift.payment.direct-transfers.enabled:false}") boolean enabled,
                                   @Value("${gift.payment.upi-intent.include-transaction-ref:true}") boolean includeTransactionRef,
                                   @Value("${gift.payment.upi-intent.attempt-ttl-minutes:1440}") long ttlMinutes) {
        this(jdbc, direct, destinations, history, enabled, includeTransactionRef, Duration.ofMinutes(ttlMinutes), Clock.systemUTC());
    }

    UpiIntentPaymentService(JdbcTemplate jdbc, GuestDirectTransferService direct,
                            ObjectProvider<GuestVerifiedDestinationService> destinations, PaymentStatusHistory history,
                            boolean enabled, boolean includeTransactionRef, Duration ttl, Clock clock) {
        if (ttl.isNegative() || ttl.isZero() || ttl.toDays() > 30) throw new IllegalArgumentException("Invalid UPI attempt lifetime");
        this.jdbc = jdbc; this.direct = direct; this.destinations = destinations; this.history = history;
        this.enabled = enabled; this.includeTransactionRef = includeTransactionRef; this.ttl = ttl; this.clock = clock;
    }

    public record StartRequest(UUID destinationId, UUID giftId, long amountPaise, String idempotencyKey, String platform) {}
    public record ResponseRequest(String response, String source) {}
    public record ReportRequest(String outcome, String transactionReference, String guestName, String guestEmail, String guestPhone) {}
    public record AttemptView(UUID attemptId, String status, long amountPaise, String paymentCode, String transactionRef,
                              String payeeName, String upiId, String upiUri, String qrPngBase64, OffsetDateTime expiresAt,
                              String appResponseStatus, String suggestedReference, UUID claimId, UUID receiptToken, String message) {}

    private record Event(UUID id, String hostName) {}
    private record Attempt(UUID id, UUID eventId, UUID destinationId, UUID giftId, long amountPaise, String paymentCode,
                           String transactionRef, String status, OffsetDateTime expiresAt, UUID claimId,
                           String responseStatus, String responseApprovalRef, String responseSha256, String hostName,
                           String claimStatus, UUID receiptToken) {}

    // ------------------------------------------------------------------ start

    @Transactional
    public AttemptView start(UUID token, StartRequest r) {
        gate();
        Event event = event(token);
        String platform = r == null || r.platform() == null ? "UNKNOWN" : r.platform().toUpperCase(Locale.ROOT);
        if (r == null || r.destinationId() == null || r.amountPaise() < 100 || r.amountPaise() > 100_000_000L ||
            r.idempotencyKey() == null || !r.idempotencyKey().matches("[A-Za-z0-9-]{8,120}") || !PLATFORMS.contains(platform))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid payment details");
        if (controls != null) controls.require(event.id(), "UPI");
        if (r.giftId() != null) requireGift(event.id(), r.giftId());
        String upiId = eligibleUpi(token, r.destinationId());

        jdbc.queryForObject("SELECT pg_advisory_xact_lock(hashtextextended(?,0))", Object.class,
            "upi-intent:" + event.id() + ":" + r.idempotencyKey());
        List<UUID> prior = jdbc.query("SELECT id FROM guest_upi_payment_attempts WHERE event_id=? AND client_key=?",
            (rs, i) -> rs.getObject(1, UUID.class), event.id(), r.idempotencyKey());
        if (!prior.isEmpty()) {
            Attempt existing = load(token, prior.get(0), false);
            if (!existing.destinationId().equals(r.destinationId()) || existing.amountPaise() != r.amountPaise() ||
                !java.util.Objects.equals(existing.giftId(), r.giftId()))
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Idempotency conflict");
            return view(existing, upiId, null);
        }
        // Codes are unique per event across attempts and manual claims; the event lock serialises allocation.
        jdbc.queryForObject("SELECT pg_advisory_xact_lock(hashtextextended(?,0))", Object.class, "upi-intent-code:" + event.id());
        UUID id = UUID.randomUUID();
        OffsetDateTime expires = now().plus(ttl);
        for (int tries = 0; ; tries++) {
            if (tries == 5) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Please try again");
            String code = "GW-" + randomString(CODE_ALPHABET, 6);
            String tr = "GWT" + randomString(REF_ALPHABET, 16);
            Boolean taken = jdbc.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM guest_direct_transfer_claims WHERE event_id=? AND payment_code=?)", Boolean.class, event.id(), code);
            if (Boolean.TRUE.equals(taken)) continue;
            int inserted = jdbc.update("""
                INSERT INTO guest_upi_payment_attempts(id,event_id,destination_id,gift_id,amount_paise,payment_code,transaction_ref,client_key,platform,expires_at)
                VALUES(?,?,?,?,?,?,?,?,?,?) ON CONFLICT DO NOTHING
                """, id, event.id(), r.destinationId(), r.giftId(), r.amountPaise(), code, tr, r.idempotencyKey(), platform, expires);
            if (inserted == 1) break;
        }
        transition(event.id(), id, null, null, UpiPaymentStatus.INITIATED, "START");
        return view(load(token, id, false), upiId, null);
    }

    // ------------------------------------------------------------------ read

    @Transactional
    public AttemptView status(UUID token, UUID attemptId) {
        gate();
        Attempt a = load(token, attemptId, true);
        return view(a, OPEN.contains(a.status()) ? eligibleUpiOrNull(token, a.destinationId()) : null, null);
    }

    // ------------------------------------------------------------------ untrusted UPI app response

    @Transactional
    public AttemptView recordResponse(UUID token, UUID attemptId, ResponseRequest r) {
        gate();
        if (r == null || r.response() == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid UPI response");
        String source = r.source() == null || r.source().isBlank() ? null : r.source().strip();
        if (source != null && !source.matches("[A-Za-z0-9._-]{1,40}"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid response source");
        UpiAppResponse response;
        try { response = UpiAppResponse.parse(r.response()); }
        catch (IllegalArgumentException ex) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid UPI response"); }
        Attempt a = load(token, attemptId, true);
        if ("EXPIRED".equals(a.status())) throw new ResponseStatusException(HttpStatus.GONE, "This payment attempt has expired");
        // A response echoing another attempt's reference is a replay or a mix-up; it is never attached here.
        if (response.txnRef() != null && response.txnRef().matches(UpiQrPayload.TRANSACTION_REF) && !response.txnRef().equals(a.transactionRef()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This response belongs to a different payment");
        String sha = sha256(r.response().strip() + "|" + (source == null ? "" : source));
        if (a.responseSha256() != null) {
            if (a.responseSha256().equals(sha)) return view(a, null, "Payment app response already recorded.");
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A payment app response was already recorded for this attempt");
        }
        boolean mismatch = response.amountPaise() != null && response.amountPaise() != a.amountPaise();
        jdbc.update("""
            UPDATE guest_upi_payment_attempts SET response_status=?,response_code=?,response_txn_id=?,response_approval_ref=?,
              response_amount_paise=?,response_amount_mismatch=?,response_source=?,response_sha256=?,response_received_at=now()
            WHERE id=?
            """, response.status(), response.responseCode(), response.txnId(), response.approvalRef(), response.amountPaise(),
            mismatch, source, sha, a.id());
        if (OPEN.contains(a.status())) {
            UpiPaymentStatus from = UpiPaymentStatus.valueOf(a.status());
            // App "success" is untrusted: at most PENDING, waiting for the guest's report and the host's check.
            UpiPaymentStatus to = "FAILURE".equals(response.status()) ? UpiPaymentStatus.FAILED : UpiPaymentStatus.PENDING;
            if (from != to && UpiPaymentStatus.allowed(from, to, UpiPaymentStatus.Actor.GUEST)) {
                jdbc.update("UPDATE guest_upi_payment_attempts SET status=?,status_changed_at=now() WHERE id=?", to.name(), a.id());
                transition(a.eventId(), a.id(), null, from, to, "UPI_APP_RESPONSE");
            }
        }
        return view(load(token, attemptId, false), null, mismatch
            ? "The payment app reported a different amount. The host will check what actually arrived."
            : "Payment app response noted. It is not proof of payment; please confirm below.");
    }

    // ------------------------------------------------------------------ guest report

    @Transactional
    public AttemptView report(UUID token, UUID attemptId, ReportRequest r) {
        gate();
        String outcome = r == null || r.outcome() == null ? "" : r.outcome().toUpperCase(Locale.ROOT);
        if (!Set.of("COMPLETED", "PENDING", "FAILED").contains(outcome))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose COMPLETED, PENDING or FAILED");
        Attempt a = load(token, attemptId, true);
        if ("EXPIRED".equals(a.status())) throw new ResponseStatusException(HttpStatus.GONE, "This payment attempt has expired. Start a new payment.");

        if (!"COMPLETED".equals(outcome)) {
            if ("PAYMENT_REPORTED".equals(a.status()))
                throw new ResponseStatusException(HttpStatus.CONFLICT, "This payment was already reported");
            UpiPaymentStatus from = UpiPaymentStatus.valueOf(a.status()), to = UpiPaymentStatus.valueOf(outcome);
            if (from != to) {
                if (!UpiPaymentStatus.allowed(from, to, UpiPaymentStatus.Actor.GUEST))
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "This status change is not allowed");
                jdbc.update("UPDATE guest_upi_payment_attempts SET status=?,status_changed_at=now() WHERE id=?", to.name(), a.id());
                transition(a.eventId(), a.id(), null, from, to, "GUEST_REPORT");
            }
            return view(load(token, attemptId, false), null, "PENDING".equals(outcome)
                ? "Noted. If the payment completes, come back and report it with the UPI reference."
                : "Noted. No payment was recorded. You can try again.");
        }

        String reference = r.transactionReference() == null || r.transactionReference().isBlank()
            ? (a.responseApprovalRef() != null && a.responseApprovalRef().matches("[0-9]{12}") ? a.responseApprovalRef() : null)
            : r.transactionReference().replaceAll("\\s+", "");
        if (reference == null || !reference.matches("[0-9]{12}"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enter the 12-digit UPI reference (UTR) from your UPI app");

        if ("PAYMENT_REPORTED".equals(a.status())) {
            // Repeated submissions (double taps, retries, replays) return the existing claim; they never add another.
            Boolean same = jdbc.queryForObject("SELECT reference_hash=? FROM guest_direct_transfer_claims WHERE id=?",
                Boolean.class, sha256("UPI:" + reference.toUpperCase(Locale.ROOT)), a.claimId());
            if (!Boolean.TRUE.equals(same))
                throw new ResponseStatusException(HttpStatus.CONFLICT, "This payment was already reported with a different reference");
            return view(a, null, "Your payment has been reported. The host will verify receipt.");
        }
        UpiPaymentStatus from = UpiPaymentStatus.valueOf(a.status());
        if (!UpiPaymentStatus.allowed(from, UpiPaymentStatus.PAYMENT_REPORTED, UpiPaymentStatus.Actor.GUEST))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This status change is not allowed");
        // Amount, payee, gift and payment code come from the server-side attempt, never from this request.
        GuestDirectTransferService.ClaimResult claim = direct.claim(token, new GuestDirectTransferService.ClaimRequest(
            a.destinationId(), "UPI", a.giftId(), a.amountPaise(), r.guestName(), blank(r.guestEmail()), blank(r.guestPhone()),
            reference, "upi-attempt-" + a.id(), a.paymentCode()));
        jdbc.update("UPDATE guest_direct_transfer_claims SET attempt_id=?,reported_via='UPI_INTENT' WHERE id=? AND event_id=?",
            a.id(), claim.claimId(), a.eventId());
        jdbc.update("UPDATE guest_upi_payment_attempts SET status='PAYMENT_REPORTED',claim_id=?,status_changed_at=now() WHERE id=?",
            claim.claimId(), a.id());
        String evidence = reference.equals(a.responseApprovalRef()) && (r.transactionReference() == null || r.transactionReference().isBlank())
            ? "UPI_APP_RESPONSE_UTR" : "GUEST_UTR";
        transition(a.eventId(), a.id(), claim.claimId(), from, UpiPaymentStatus.PAYMENT_REPORTED, evidence);
        return view(load(token, attemptId, false), null, "Your payment has been reported. The host will verify receipt.");
    }

    // ------------------------------------------------------------------ screenshot evidence

    @Transactional
    public AttemptView attachEvidence(UUID token, UUID attemptId, byte[] bytes) {
        gate();
        Attempt a = load(token, attemptId, true);
        if (!"PAYMENT_REPORTED".equals(a.status()) || a.claimId() == null)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Report the payment before adding a screenshot");
        if (!"AWAITING_VERIFICATION".equals(a.claimStatus()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The host has already reviewed this payment");
        byte[] clean;
        try { clean = PaymentEvidenceImage.sanitize(bytes); }
        catch (IllegalArgumentException ex) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage()); }
        String sha = sha256(bytes);
        Boolean reused = jdbc.queryForObject(
            "SELECT EXISTS(SELECT 1 FROM guest_payment_evidence WHERE event_id=? AND original_sha256=? AND claim_id<>?)",
            Boolean.class, a.eventId(), sha, a.claimId());
        if (Boolean.TRUE.equals(reused))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This screenshot was already submitted for another payment");
        jdbc.update("""
            INSERT INTO guest_payment_evidence(claim_id,event_id,content_type,size_bytes,original_sha256,image)
            VALUES(?,?,'image/jpeg',?,?,?)
            ON CONFLICT (claim_id) DO UPDATE SET size_bytes=EXCLUDED.size_bytes,original_sha256=EXCLUDED.original_sha256,
              image=EXCLUDED.image,created_at=now()
            """, a.claimId(), a.eventId(), clean.length, sha, clean);
        if (audit != null) audit.recordChange(null, AuditActorType.GUEST, "DIRECT_TRANSFER_EVIDENCE", a.claimId(), AuditAction.UPDATE,
            null, Map.of("screenshotSha256", sha), null);
        return view(a, null, "Screenshot added. The host will see it as supporting information.");
    }

    // ------------------------------------------------------------------ internals

    private void gate() {
        if (!enabled) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Direct UPI payments are disabled");
    }

    private Event event(UUID token) {
        if (token == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invitation unavailable");
        List<Event> rows = jdbc.query("SELECT id,host_display_name FROM events WHERE public_token=? AND status='PUBLISHED' AND gifting_enabled",
            (rs, i) -> new Event(rs.getObject(1, UUID.class), rs.getString(2)), token);
        if (rows.size() != 1) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invitation unavailable");
        return rows.get(0);
    }

    private void requireGift(UUID eventId, UUID giftId) {
        Integer count = jdbc.queryForObject("""
            SELECT COUNT(*) FROM gifts g JOIN events e ON e.id=g.event_id
            WHERE g.id=? AND g.event_id=? AND g.status='ACTIVE' AND g.visibility='PUBLIC' AND e.gift_mode='CONTRIBUTION_AND_GIFTS'
            """, Integer.class, giftId, eventId);
        if (count == null || count != 1) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Gift unavailable");
    }

    /** The host's currently active, guest-visible, host-confirmed or verified preferred UPI destination for this event. */
    private String eligibleUpi(UUID token, UUID destinationId) {
        String upi = eligibleUpiOrNull(token, destinationId);
        if (upi == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Eligible UPI destination unavailable");
        return upi;
    }

    private String eligibleUpiOrNull(UUID token, UUID destinationId) {
        GuestVerifiedDestinationService service = destinations.getIfAvailable();
        if (service == null) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Direct UPI payments are disabled");
        return service.list(token).stream()
            .filter(d -> "UPI".equals(d.method()) && d.id().equals(destinationId) && d.upiId() != null)
            .map(GuestVerifiedDestinationService.Destination::upiId).findFirst().orElse(null);
    }

    private Attempt load(UUID token, UUID attemptId, boolean lock) {
        if (token == null || attemptId == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found");
        List<Attempt> rows = jdbc.query("""
            SELECT a.id,a.event_id,a.destination_id,a.gift_id,a.amount_paise,a.payment_code,a.transaction_ref,a.status,a.expires_at,
                   a.claim_id,a.response_status,a.response_approval_ref,a.response_sha256,e.host_display_name,c.status,c.receipt_token
            FROM guest_upi_payment_attempts a JOIN events e ON e.id=a.event_id
            LEFT JOIN guest_direct_transfer_claims c ON c.id=a.claim_id
            WHERE a.id=? AND e.public_token=?
            """ + (lock ? " FOR UPDATE OF a" : ""), (rs, i) -> new Attempt(rs.getObject(1, UUID.class), rs.getObject(2, UUID.class),
                rs.getObject(3, UUID.class), rs.getObject(4, UUID.class), rs.getLong(5), rs.getString(6), rs.getString(7), rs.getString(8),
                rs.getObject(9, OffsetDateTime.class), rs.getObject(10, UUID.class), rs.getString(11), rs.getString(12), rs.getString(13),
                rs.getString(14), rs.getString(15), rs.getObject(16, UUID.class)), attemptId, token);
        if (rows.size() != 1) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found");
        Attempt a = rows.get(0);
        if (lock && OPEN.contains(a.status()) && !a.expiresAt().isAfter(now())) {
            jdbc.update("UPDATE guest_upi_payment_attempts SET status='EXPIRED',status_changed_at=now() WHERE id=?", a.id());
            transition(a.eventId(), a.id(), null, UpiPaymentStatus.valueOf(a.status()), UpiPaymentStatus.EXPIRED, "ATTEMPT_EXPIRED");
            return load(token, attemptId, false);
        }
        return a;
    }

    private void transition(UUID eventId, UUID attemptId, UUID claimId, UpiPaymentStatus from, UpiPaymentStatus to, String evidence) {
        UpiPaymentStatus.Actor actor = "ATTEMPT_EXPIRED".equals(evidence) ? UpiPaymentStatus.Actor.SYSTEM : UpiPaymentStatus.Actor.GUEST;
        history.record(eventId, attemptId, claimId, from, to, actor, null, evidence);
        if (audit != null) audit.recordChange(null, actor == UpiPaymentStatus.Actor.SYSTEM ? AuditActorType.SYSTEM : AuditActorType.GUEST,
            "UPI_PAYMENT_ATTEMPT", attemptId, from == null ? AuditAction.CREATE : AuditAction.STATUS_CHANGE,
            from == null ? null : Map.of("status", from.name()), Map.of("status", to.name(), "evidence", evidence), null);
    }

    private AttemptView view(Attempt a, String upiId, String message) {
        String status = a.claimId() != null && a.claimStatus() != null
            ? UpiPaymentStatus.fromStored(a.claimStatus()).name() : a.status();
        String uri = null, qr = null, payee = payeeName(a.hostName());
        if (upiId != null && OPEN.contains(a.status())) {
            uri = UpiQrPayload.generateIntent(upiId, payee, a.amountPaise(), a.transactionRef(), NOTE_PREFIX + a.paymentCode(), includeTransactionRef);
            qr = Base64.getEncoder().encodeToString(UpiQrImageCodec.generateIntentPng(uri));
        }
        String suggested = a.responseApprovalRef() != null && a.responseApprovalRef().matches("[0-9]{12}") ? a.responseApprovalRef() : null;
        return new AttemptView(a.id(), status, a.amountPaise(), a.paymentCode(), a.transactionRef(), payee, uri == null ? null : upiId,
            uri, qr, a.expiresAt(), a.responseStatus(), suggested, a.claimId(), a.receiptToken(), message);
    }

    /** Payee name shown in the UPI app. Uses the host's public display name; the app shows the bank-verified name anyway. */
    static String payeeName(String hostDisplayName) {
        String cleaned = hostDisplayName == null ? "" : hostDisplayName.replaceAll("[^\\p{L}\\p{N} .'-]", " ").replaceAll("\\s+", " ").strip();
        if (cleaned.length() > 50) cleaned = cleaned.substring(0, 50).strip();
        return cleaned.isEmpty() ? "Gift recipient" : cleaned;
    }

    private String randomString(String alphabet, int length) {
        StringBuilder out = new StringBuilder(length);
        for (int i = 0; i < length; i++) out.append(alphabet.charAt(random.nextInt(alphabet.length())));
        return out.toString();
    }

    private OffsetDateTime now() { return OffsetDateTime.now(clock).withOffsetSameInstant(ZoneOffset.UTC); }
    private static String blank(String value) { return value == null || value.isBlank() ? null : value.strip(); }
    private static String sha256(String value) { return sha256(value.getBytes(StandardCharsets.UTF_8)); }
    private static String sha256(byte[] value) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value)); }
        catch (NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }
}
