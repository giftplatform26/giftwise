package com.giftplatform.payment.service;

import com.giftplatform.payment.entity.PaymentOrder;
import com.giftplatform.payment.provider.RazorpayClient;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class RazorpayOrderCreationService {

    private final PaymentOrderService orders;
    private final RazorpayClient razorpay;
    private final JdbcTemplate jdbc;

    public RazorpayOrderCreationService(
            PaymentOrderService orders,
            RazorpayClient razorpay,
            JdbcTemplate jdbc) {
        this.orders = orders;
        this.razorpay = razorpay;
        this.jdbc = jdbc;
    }

    public record CheckoutOrder(
            UUID localOrderId,
            String razorpayOrderId,
            long amountPaise,
            String currency,
            String status
    ) {}

    @Transactional
    public CheckoutOrder create(
            UUID eventId,
            UUID giftId,
            String contributionType,
            long amountPaise,
            String idempotencyKey) {

        // Existing service validates event, gift, amount and
        // idempotency, using a PostgreSQL advisory transaction lock.
        PaymentOrder order = orders.create(
                eventId, giftId, contributionType,
                amountPaise, idempotencyKey);

        List<CheckoutOrder> existing = jdbc.query("""
            SELECT provider_order_id, amount_paise,
                   currency, status
            FROM payment_orders
            WHERE id = ?
            FOR UPDATE
            """, (rs, n) -> new CheckoutOrder(
                order.getId(),
                rs.getString("provider_order_id"),
                rs.getLong("amount_paise"),
                rs.getString("currency"),
                rs.getString("status")
            ), order.getId());

        if (existing.size() != 1) {
            throw new IllegalStateException("Payment order missing");
        }

        CheckoutOrder current = existing.get(0);

        if (current.razorpayOrderId() != null) {
            return current;
        }

        if (!"CREATED".equals(current.status())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Payment order is not eligible for provider creation");
        }

        // UUID receipt is 36 characters (Razorpay max: 40).
        var provider = razorpay.createOrder(
                order.getAmountPaise(),
                order.getId().toString());

        if (provider == null
                || provider.id() == null
                || !provider.id().matches("order_[A-Za-z0-9]+")
                || provider.amount() != order.getAmountPaise()
                || !"INR".equals(provider.currency())
                || !"created".equals(provider.status())) {
            throw new IllegalStateException(
                    "Invalid provider order response");
        }

        int updated = jdbc.update("""
            UPDATE payment_orders
            SET provider_order_id = ?,
                status = 'PENDING',
                updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
              AND provider_order_id IS NULL
              AND status = 'CREATED'
            """, provider.id(), order.getId());

        if (updated != 1) {
            throw new IllegalStateException(
                    "Could not persist Razorpay order");
        }

        return new CheckoutOrder(
                order.getId(), provider.id(),
                order.getAmountPaise(), "INR", "PENDING");
    }
}
