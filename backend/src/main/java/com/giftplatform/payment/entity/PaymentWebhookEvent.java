package com.giftplatform.payment.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment_webhook_events")
public class PaymentWebhookEvent {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id = UUID.randomUUID();

    @Column(name = "provider")
    private String provider;

    @Column(name = "provider_event_id")
    private String providerEventId;

    @Column(name = "event_type")
    private String eventType;

    @Column(name = "status")
    private String status;

    @Column(name = "received_at", insertable = false, updatable = false)
    private Instant receivedAt;

    @Column(name = "processed_at")
    private Instant processedAt;

    protected PaymentWebhookEvent() {}

    public UUID getId() { return id; }
    public String getProvider() { return provider; }
    public String getProviderEventId() { return providerEventId; }
    public String getEventType() { return eventType; }
    public String getStatus() { return status; }
    public Instant getReceivedAt() { return receivedAt; }
    public Instant getProcessedAt() { return processedAt; }
}
