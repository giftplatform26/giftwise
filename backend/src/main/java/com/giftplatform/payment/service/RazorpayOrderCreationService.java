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

    @org.springframework.beans.factory.annotation.Autowired private com.giftplatform.payment.control.PaymentControlService controls;
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
            String status,
            long platformFeePaise,
            long hostReceivesPaise
    ) {
        public CheckoutOrder(UUID id,String provider,long amount,String currency,String status) {
            this(id,provider,amount,currency,status,0,amount);
        }
    }

    @Transactional
    public CheckoutOrder create(
            UUID eventId,
            UUID giftId,
            String contributionType,
            long amountPaise,
            String idempotencyKey) {

        return create(eventId,giftId,contributionType,amountPaise,idempotencyKey,null,null,null);
    }

    @Transactional
    public CheckoutOrder create(UUID eventId, UUID giftId, String contributionType, long amountPaise,
                                String idempotencyKey, Long acceptedFeePaise, Long acceptedTotalPaise, Long acceptedHostReceivesPaise) {
        // Existing service validates event, gift, amount and
        // idempotency, using a PostgreSQL advisory transaction lock.
        PaymentOrder order = orders.create(
                eventId, giftId, contributionType,
                amountPaise, idempotencyKey);

        if ((order.getPlatformFeePaise()>0 && (acceptedFeePaise==null || acceptedTotalPaise==null || acceptedHostReceivesPaise==null)) ||
                (acceptedFeePaise!=null && acceptedFeePaise!=order.getPlatformFeePaise()) ||
                (acceptedTotalPaise!=null && !acceptedTotalPaise.equals(order.getAmountPaise())) ||
                (acceptedHostReceivesPaise!=null && !acceptedHostReceivesPaise.equals(order.getHostReceivesPaise()))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Platform fee changed. Review the fee and try again.");
        }

        List<CheckoutOrder> existing = jdbc.query("""
            SELECT provider_order_id, amount_paise,
                   currency, status, platform_fee_paise, COALESCE(host_receives_paise,amount_paise) AS host_receives_paise
            FROM payment_orders
            WHERE id = ?
            FOR UPDATE
            """, (rs, n) -> new CheckoutOrder(
                order.getId(),
                rs.getString("provider_order_id"),
                rs.getLong("amount_paise"),
                rs.getString("currency"),
                rs.getString("status"),rs.getLong("platform_fee_paise"),rs.getLong("host_receives_paise")
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

        if(controls!=null) {
            controls.require(eventId,"RAZORPAY");
            if(controls.event(eventId).get("ROUTING"))throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Complete Route account, fee and settlement setup before routed collection");
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
                order.getAmountPaise(), "INR", "PENDING",order.getPlatformFeePaise(),order.getHostReceivesPaise());
    }
}
