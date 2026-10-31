package com.giftplatform.payment.service;

import com.giftplatform.payment.provider.RazorpayClient;
import com.giftplatform.payment.security.RazorpaySignatureVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class RazorpayCaptureService {

    private final RazorpayClient razorpay;
    private final RazorpaySignatureVerifier signatures;
    private final JdbcTemplate jdbc;
    private final TransactionTemplate transactions;
    private final ContributionAllocationService allocations;
    private final String keySecret;

    public RazorpayCaptureService(
            RazorpayClient razorpay,
            RazorpaySignatureVerifier signatures,
            JdbcTemplate jdbc,
            TransactionTemplate transactions,
            ContributionAllocationService allocations,
            @Value("${gift.payment.razorpay.key-secret:}")
            String keySecret) {

        this.razorpay = razorpay;
        this.signatures = signatures;
        this.jdbc = jdbc;
        this.transactions = transactions;
        this.allocations = allocations;
        this.keySecret = keySecret;
    }

    public record CaptureResult(
            UUID orderId,
            UUID contributionId,
            String paymentId,
            long giftPaise,
            long walletPaise,
            boolean alreadyProcessed
    ) {}

    private record OrderData(
            UUID id,
            String providerOrderId,
            long amountPaise,
            String currency,
            String status
    ) {}

    public CaptureResult confirmCheckout(
            UUID localOrderId,
            String providerOrderId,
            String paymentId,
            String signature) {

        if (localOrderId == null
                || providerOrderId == null
                || !providerOrderId.matches("order_[A-Za-z0-9]+")
                || paymentId == null
                || !paymentId.matches("pay_[A-Za-z0-9]+")
                || !signatures.verifyCheckout(
                        providerOrderId,
                        paymentId,
                        signature,
                        keySecret)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid payment confirmation");
        }

        return verifyAndPost(
                localOrderId, providerOrderId, paymentId);
    }

    /*
     * For trusted webhook processing only.
     * The webhook handler must authenticate the raw payload
     * before calling this method.
     */
    public CaptureResult confirmVerifiedWebhook(
            UUID localOrderId,
            String providerOrderId,
            String paymentId) {

        return verifyAndPost(
                localOrderId, providerOrderId, paymentId);
    }

    /** Caller must authorize the event and load both provider identities from its stored order/transaction. */
    public CaptureResult recheckRecordedPayment(UUID localOrderId,String providerOrderId,String paymentId) {
        return verifyAndPost(localOrderId,providerOrderId,paymentId);
    }

    private CaptureResult verifyAndPost(
            UUID localOrderId,
            String providerOrderId,
            String paymentId) {

        if (localOrderId == null
                || providerOrderId == null
                || !providerOrderId.matches("order_[A-Za-z0-9]+")
                || paymentId == null
                || !paymentId.matches("pay_[A-Za-z0-9]+")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid provider payment identity");
        }

        // Provider HTTP request happens outside the DB transaction.
        var payment = razorpay.fetchPayment(paymentId);

        if (payment == null
                || !paymentId.equals(payment.id())
                || !providerOrderId.equals(payment.orderId())
                || !"captured".equals(payment.status())
                || !"INR".equals(payment.currency())
                || payment.amount() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Verified captured payment required");
        }

        return transactions.execute(status -> {

            // Serialize all confirmations for this local order.
            List<OrderData> rows = jdbc.query("""
                SELECT id, provider_order_id, amount_paise,
                       currency, status
                FROM payment_orders
                WHERE id = ?
                FOR UPDATE
                """,
                (rs, n) -> new OrderData(
                        rs.getObject("id", UUID.class),
                        rs.getString("provider_order_id"),
                        rs.getLong("amount_paise"),
                        rs.getString("currency"),
                        rs.getString("status")),
                localOrderId);

            if (rows.isEmpty()) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Payment order not found");
            }

            OrderData order = rows.get(0);

            if (!providerOrderId.equals(order.providerOrderId())
                    || payment.amount() != order.amountPaise()
                    || !payment.currency().equals(order.currency())) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Payment does not match local order");
            }

            List<UUID> contributionIds = jdbc.query("""
                SELECT id
                FROM contributions
                WHERE payment_order_id = ?
                  AND amount_paise = ?
                FOR UPDATE
                """,
                (rs, n) -> rs.getObject(1, UUID.class),
                localOrderId, order.amountPaise());

            if (contributionIds.size() != 1) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Matching contribution required");
            }

            UUID contributionId = contributionIds.get(0);

            List<String> previousPayments = jdbc.query("""
                SELECT provider_payment_id
                FROM payment_transactions
                WHERE payment_order_id = ?
                  AND status = 'CAPTURED'
                FOR UPDATE
                """,
                (rs, n) -> rs.getString(1),
                localOrderId);

            if (!previousPayments.isEmpty()
                    && (previousPayments.size() != 1
                    || !paymentId.equals(previousPayments.get(0)))) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Order already captured by another payment");
            }

            Integer otherOwner = jdbc.queryForObject("""
                SELECT COUNT(*)
                FROM payment_transactions
                WHERE provider_payment_id = ?
                  AND payment_order_id <> ?
                """,
                Integer.class, paymentId, localOrderId);

            if (otherOwner != null && otherOwner > 0) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Provider payment already belongs to another order");
            }

            if (!"CAPTURED".equals(order.status())) {

                if (!"PENDING".equals(order.status())
                        && !"AUTHORIZED".equals(order.status())) {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Payment order is not capturable");
                }

                int inserted = jdbc.update("""
                    INSERT INTO payment_transactions
                        (id, payment_order_id,
                         provider_payment_id, amount_paise,
                         currency, status, verified_at)
                    VALUES (?, ?, ?, ?, 'INR',
                            'CAPTURED', CURRENT_TIMESTAMP)
                    """,
                    UUID.randomUUID(),
                    localOrderId,
                    paymentId,
                    order.amountPaise());

                if (inserted != 1) {
                    throw new IllegalStateException(
                            "Could not record captured payment");
                }

                int updated = jdbc.update("""
                    UPDATE payment_orders
                    SET status = 'CAPTURED',
                        updated_at = CURRENT_TIMESTAMP
                    WHERE id = ?
                      AND status IN ('PENDING', 'AUTHORIZED')
                    """, localOrderId);

                if (updated != 1) {
                    throw new IllegalStateException(
                            "Could not capture local payment order");
                }

            } else if (previousPayments.size() != 1) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Captured order has inconsistent transaction");
            }

            // Only store details from the independently fetched, matched captured payment.
            // Also backfill these fields on idempotent rechecks of older payments.
            jdbc.update("""
                UPDATE payment_transactions
                SET payment_method=?, bank_code=?, bank_transaction_id=?, rrn=?, upi_transaction_id=?
                WHERE payment_order_id=? AND provider_payment_id=? AND status='CAPTURED'
                """, payment.method(), payment.bank(), payment.bankTransactionId(), payment.rrn(),
                    payment.upiTransactionId(), localOrderId, paymentId);

            if((payment.feePaise()!=null&&payment.feePaise()<0)||(payment.taxPaise()!=null&&payment.taxPaise()<0)||
                    (payment.feePaise()!=null&&payment.taxPaise()!=null&&payment.taxPaise()>payment.feePaise()))throw new IllegalStateException("Invalid provider fee/tax breakdown");
            jdbc.update("UPDATE payment_transactions SET provider_fee_paise=COALESCE(?,provider_fee_paise),provider_tax_paise=COALESCE(?,provider_tax_paise) WHERE payment_order_id=? AND provider_payment_id=? AND status='CAPTURED'",payment.feePaise(),payment.taxPaise(),localOrderId,paymentId);
            var allocation = allocations.allocate(contributionId);

            // This runs inside the verified-capture transaction. Never emit on an unverified claim.
            jdbc.update("""
                INSERT INTO notification_outbox
                  (id,event_id,notification_kind,recipient_kind,channel,source_kind,source_id,
                   payment_evidence,subject,message)
                SELECT ?, c.event_id,'CONTRIBUTION','HOST','IN_APP','VERIFIED_CAPTURE',c.id,
                       'CAPTURED','Razorpay contribution verified',
                       'A Razorpay contribution was independently verified and allocated.'
                FROM contributions c WHERE c.id=?
                ON CONFLICT (event_id,notification_kind,recipient_kind,channel,source_kind,source_id)
                DO NOTHING
                """, UUID.randomUUID(), contributionId);

            return new CaptureResult(
                    localOrderId,
                    contributionId,
                    paymentId,
                    allocation.giftPaise(),
                    allocation.walletPaise(),
                    allocation.alreadyProcessed());
        });
    }
}
