package com.giftplatform.payment.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class GuestCheckoutService {
    private final RazorpayOrderCreationService orderCreation;
    private final JdbcTemplate jdbc;
    private final String publicKey;

    public GuestCheckoutService(RazorpayOrderCreationService orderCreation, JdbcTemplate jdbc,
            @Value("${gift.payment.razorpay.key-id:}") String publicKey) {
        this.orderCreation = orderCreation;
        this.jdbc = jdbc;
        this.publicKey = publicKey;
    }

    public record Request(UUID eventId, UUID giftId, String contributionType,
                          long amountPaise, String idempotencyKey,
                          String guestName, String guestEmail, String guestPhone) {}

    public record Response(UUID localOrderId, String razorpayOrderId,
                           String razorpayKeyId, long amountPaise, String currency,
                           String status) {}

    private record Existing(String name, String email, String phone, long amount) {}

    @Transactional
    public Response create(Request request) {
        if (request == null || request.guestName() == null ||
                request.guestName().isBlank() || request.guestName().length() > 200 ||
                (request.guestEmail() != null && request.guestEmail().length() > 320) ||
                (request.guestPhone() != null && request.guestPhone().length() > 30)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid guest details");
        }
        if (!publicKey.startsWith("rzp_test_")) {
            throw new IllegalStateException("Razorpay Test Mode key required");
        }
        String name = request.guestName().trim();
        String email = normalize(request.guestEmail());
        String phone = normalize(request.guestPhone());

        // The underlying order service locks the idempotency key until commit.
        var order = orderCreation.create(request.eventId(), request.giftId(),
                request.contributionType(), request.amountPaise(), request.idempotencyKey());

        List<Existing> previous = jdbc.query("""
            SELECT guest_name, guest_email, guest_phone, amount_paise
            FROM contributions WHERE payment_order_id = ? FOR UPDATE
            """, (rs, i) -> new Existing(rs.getString(1), rs.getString(2),
                    rs.getString(3), rs.getLong(4)), order.localOrderId());

        if (previous.isEmpty()) {
            jdbc.update("""
                INSERT INTO contributions
                  (id, payment_order_id, event_id, gift_id, guest_name,
                   guest_email, guest_phone, amount_paise)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """, UUID.randomUUID(), order.localOrderId(), request.eventId(),
                    request.giftId(), name, email, phone, request.amountPaise());
        } else if (previous.size() != 1 ||
                !previous.get(0).name().equals(name) ||
                !Objects.equals(previous.get(0).email(), email) ||
                !Objects.equals(previous.get(0).phone(), phone) ||
                previous.get(0).amount() != request.amountPaise()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Idempotency key belongs to different guest details");
        }

        // Host activity is queued once per local order, not once per retry.
        jdbc.update("""
            INSERT INTO notification_outbox
              (id,event_id,notification_kind,recipient_kind,channel,source_kind,source_id,
               payment_evidence,subject,message)
            VALUES (?,?,'CONTRIBUTION','HOST','IN_APP','RAZORPAY_ORDER',?,
                    'CHECKOUT_PENDING','Razorpay checkout started',
                    'A guest started a Razorpay checkout. Payment has not been verified.')
            ON CONFLICT (event_id,notification_kind,recipient_kind,channel,source_kind,source_id)
            DO NOTHING
            """, UUID.randomUUID(), request.eventId(), order.localOrderId());

        return new Response(order.localOrderId(), order.razorpayOrderId(),
                publicKey, order.amountPaise(), order.currency(), order.status());
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
