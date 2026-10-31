package com.giftplatform.payment.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment_orders")
public class PaymentOrder {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id = UUID.randomUUID();

    @Column(name = "event_id")
    private UUID eventId;

    @Column(name = "gift_id")
    private UUID giftId;

    @Column(name = "provider")
    private String provider;

    @Column(name = "provider_order_id")
    private String providerOrderId;

    @Column(name = "contribution_type")
    private String contributionType;

    @Column(name = "amount_paise")
    private Long amountPaise;

    @Column(name = "requested_amount_paise", nullable = false)
    private Long requestedAmountPaise;
    @Column(name = "platform_fee_paise", nullable = false)
    private long platformFeePaise;
    @Column(name = "host_receives_paise")
    private Long hostReceivesPaise;
    @Column(name = "monetization_revision")
    private Long monetizationRevision;

    @Column(name = "currency")
    private String currency;

    @Column(name = "status")
    private String status;

    @Column(name = "idempotency_key")
    private String idempotencyKey;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private Instant updatedAt;

    protected PaymentOrder() {}

    public PaymentOrder(
            UUID eventId,
            UUID giftId,
            String contributionType,
            long amountPaise,
            String idempotencyKey
    ) {
        this.eventId = java.util.Objects.requireNonNull(eventId);

        if (amountPaise <= 0) {
            throw new IllegalArgumentException(
                    "Payment amount must be positive");
        }

        if (idempotencyKey == null ||
                idempotencyKey.isBlank() ||
                idempotencyKey.length() > 120) {
            throw new IllegalArgumentException(
                    "Invalid idempotency key");
        }

        if (!java.util.Set.of(
                "GIFT_PARTIAL",
                "GIFT_FULL",
                "EVENT_WALLET"
        ).contains(contributionType)) {
            throw new IllegalArgumentException(
                    "Invalid contribution type");
        }

        if ("EVENT_WALLET".equals(contributionType)
                && giftId != null) {
            throw new IllegalArgumentException(
                    "Wallet contribution cannot reference a gift");
        }

        if (!"EVENT_WALLET".equals(contributionType)
                && giftId == null) {
            throw new IllegalArgumentException(
                    "Gift contribution requires a gift");
        }

        this.giftId = giftId;
        this.contributionType = contributionType;
        this.amountPaise = amountPaise;
        this.requestedAmountPaise = amountPaise;
        this.hostReceivesPaise = amountPaise;
        this.idempotencyKey = idempotencyKey;
        this.provider = "RAZORPAY";
        this.currency = "INR";
        this.status = "CREATED";
    }


    public void applyPricing(com.giftplatform.monetization.MonetizationService.Quote quote) {
        if (quote.contributionPaise() != requestedAmountPaise || monetizationRevision != null)
            throw new IllegalStateException("Invalid pricing snapshot");
        this.amountPaise=quote.totalPaise();
        this.platformFeePaise=quote.platformFeePaise();
        this.hostReceivesPaise=quote.hostReceivesPaise();
        this.monetizationRevision=quote.revision();
    }
    public Long getRequestedAmountPaise() {return requestedAmountPaise;}
    public long getPlatformFeePaise() {return platformFeePaise;}
    public Long getHostReceivesPaise() {return hostReceivesPaise==null?amountPaise:hostReceivesPaise;}
    public Long getMonetizationRevision() {return monetizationRevision;}

    public UUID getId() { return id; }
    public UUID getEventId() { return eventId; }
    public UUID getGiftId() { return giftId; }
    public String getProvider() { return provider; }
    public String getProviderOrderId() { return providerOrderId; }
    public String getContributionType() { return contributionType; }
    public Long getAmountPaise() { return amountPaise; }
    public String getCurrency() { return currency; }
    public String getStatus() { return status; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
