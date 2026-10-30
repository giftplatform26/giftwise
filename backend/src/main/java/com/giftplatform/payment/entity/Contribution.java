package com.giftplatform.payment.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "contributions")
public class Contribution {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id = UUID.randomUUID();

    @Column(name = "payment_order_id")
    private UUID paymentOrderId;

    @Column(name = "event_id")
    private UUID eventId;

    @Column(name = "gift_id")
    private UUID giftId;

    @Column(name = "guest_name")
    private String guestName;

    @Column(name = "guest_email")
    private String guestEmail;

    @Column(name = "guest_phone")
    private String guestPhone;

    @Column(name = "amount_paise")
    private Long amountPaise;

    @Column(name = "allocation_status")
    private String allocationStatus;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    protected Contribution() {}

    public UUID getId() { return id; }
    public UUID getPaymentOrderId() { return paymentOrderId; }
    public UUID getEventId() { return eventId; }
    public UUID getGiftId() { return giftId; }
    public String getGuestName() { return guestName; }
    public String getGuestEmail() { return guestEmail; }
    public String getGuestPhone() { return guestPhone; }
    public Long getAmountPaise() { return amountPaise; }
    public String getAllocationStatus() { return allocationStatus; }
    public Instant getCreatedAt() { return createdAt; }
}
