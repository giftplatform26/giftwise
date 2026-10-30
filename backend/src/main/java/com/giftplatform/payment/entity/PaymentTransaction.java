package com.giftplatform.payment.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment_transactions")
public class PaymentTransaction {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id = UUID.randomUUID();

    @Column(name = "payment_order_id")
    private UUID paymentOrderId;

    @Column(name = "provider_payment_id")
    private String providerPaymentId;

    @Column(name = "amount_paise")
    private Long amountPaise;

    @Column(name = "currency")
    private String currency;

    @Column(name = "status")
    private String status;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    protected PaymentTransaction() {}

    public UUID getId() { return id; }
    public UUID getPaymentOrderId() { return paymentOrderId; }
    public String getProviderPaymentId() { return providerPaymentId; }
    public Long getAmountPaise() { return amountPaise; }
    public String getCurrency() { return currency; }
    public String getStatus() { return status; }
    public Instant getVerifiedAt() { return verifiedAt; }
    public Instant getCreatedAt() { return createdAt; }
}
