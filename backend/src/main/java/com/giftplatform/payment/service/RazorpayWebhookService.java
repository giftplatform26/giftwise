package com.giftplatform.payment.service;

import com.giftplatform.payment.security.RazorpaySignatureVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@Service
public class RazorpayWebhookService {
    private final RazorpaySignatureVerifier signatures;
    private final RazorpayCaptureService capture;
    private final JdbcTemplate jdbc;
    private final TransactionTemplate tx;
    private final ObjectMapper mapper;
    private final String secret;

    public RazorpayWebhookService(RazorpaySignatureVerifier signatures,
                                 RazorpayCaptureService capture,
                                 JdbcTemplate jdbc,
                                 TransactionTemplate tx,
                                 ObjectMapper mapper,
                                 @Value("${gift.payment.razorpay.webhook-secret:}") String secret) {
        this.signatures = signatures;
        this.capture = capture;
        this.jdbc = jdbc;
        this.tx = tx;
        this.mapper = mapper;
        this.secret = secret;
    }

    public void receive(byte[] bytes, String signature, String eventId) {
        if (bytes == null || bytes.length == 0 || bytes.length > 262144 ||
                eventId == null || !eventId.matches("[A-Za-z0-9_:-]{1,150}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid webhook request");
        }
        String raw = new String(bytes, StandardCharsets.UTF_8);
        if (!signatures.verifyWebhook(raw, signature, secret)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid webhook signature");
        }
        JsonNode root;
        try {
            root = mapper.readTree(raw);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid webhook JSON");
        }
        String type = root.path("event").asText("");
        if (type.isBlank() || type.length() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid webhook type");
        }
        String paymentId = null;
        String orderId = null;
        if ("payment.captured".equals(type)) {
            JsonNode entity = root.path("payload").path("payment").path("entity");
            paymentId = entity.path("id").asText("");
            orderId = entity.path("order_id").asText("");
            if (!paymentId.matches("pay_[A-Za-z0-9]+") ||
                    !orderId.matches("order_[A-Za-z0-9]+")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid payment identity");
            }
        }
        final String finalPaymentId = paymentId;
        final String finalOrderId = orderId;
        // The event is durably stored before attempting provider verification.
        tx.executeWithoutResult(s -> {
            jdbc.update("""
                INSERT INTO payment_webhook_events
                    (id, provider, provider_event_id, event_type, status, raw_payload)
                VALUES (?, 'RAZORPAY', ?, ?, 'RECEIVED', ?)
                ON CONFLICT (provider, provider_event_id) DO NOTHING
                """, UUID.randomUUID(), eventId, type, raw);
        });
        try {
            tx.executeWithoutResult(s -> {
                List<EventRow> events = jdbc.query("""
                    SELECT event_type, status, raw_payload
                    FROM payment_webhook_events
                    WHERE provider = 'RAZORPAY' AND provider_event_id = ? FOR UPDATE
                    """, (rs, i) -> new EventRow(rs.getString(1), rs.getString(2), rs.getString(3)), eventId);
                if (events.size() != 1) throw new IllegalStateException("Webhook event missing");
                EventRow prior = events.get(0);
                if (!prior.type.equals(type) || !prior.payload.equals(raw)) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Webhook event ID reused");
                }
                if ("PROCESSED".equals(prior.status)) return;
                jdbc.update("""
                    UPDATE payment_webhook_events
                    SET attempt_count = attempt_count + 1, last_attempt_at = CURRENT_TIMESTAMP,
                        last_error = NULL
                    WHERE provider = 'RAZORPAY' AND provider_event_id = ?
                    """, eventId);
                if ("payment.captured".equals(type)) {
                    List<UUID> localIds = jdbc.query("""
                        SELECT id FROM payment_orders
                        WHERE provider = 'RAZORPAY' AND provider_order_id = ?
                        """, (rs, i) -> rs.getObject(1, UUID.class), finalOrderId);
                    if (localIds.size() != 1) {
                        throw new IllegalStateException("Local payment order not found");
                    }
                    // Independently fetches the provider payment, verifies amount/order/currency,
                    // and uses existing transaction and allocation idempotency protections.
                    capture.confirmVerifiedWebhook(localIds.get(0), finalOrderId, finalPaymentId);
                }
                jdbc.update("""
                    UPDATE payment_webhook_events
                    SET status = 'PROCESSED', processed_at = CURRENT_TIMESTAMP, last_error = NULL
                    WHERE provider = 'RAZORPAY' AND provider_event_id = ?
                    """, eventId);
            });
        } catch (Exception ex) {
            // Separate transaction: preserve failure status even if capture rolls back.
            tx.executeWithoutResult(s -> jdbc.update("""
                UPDATE payment_webhook_events
                SET status = 'FAILED', last_error = ?, last_attempt_at = CURRENT_TIMESTAMP,
                    attempt_count = attempt_count + 1
                WHERE provider = 'RAZORPAY' AND provider_event_id = ?
                  AND status <> 'PROCESSED'
                """, ex.getClass().getSimpleName(), eventId));
            throw ex;
        }
    }

    private record EventRow(String type, String status, String payload) {}
}
