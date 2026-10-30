package com.giftplatform.gift.entity;

import com.giftplatform.event.entity.Event;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "gifts")
public class Gift {
    @Id @Column(nullable = false)
    private UUID id;
    @Version @Column(nullable = false)
    private long version;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;
    @Column(nullable = false, length = 200)
    private String name;
    @Column(columnDefinition = "text")
    private String description;
    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false, length = 12)
    private GiftVisibility visibility = GiftVisibility.DRAFT;
    @Column(name = "product_url", length = 2048)
    private String productUrl;
    @Column(name = "image_key", length = 512)
    private String imageKey;
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 6)
    private GiftPriority priority = GiftPriority.MEDIUM;
    @Column(name = "target_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal targetAmount;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private GiftStatus status = GiftStatus.ACTIVE;
    @Enumerated(EnumType.STRING)
    @Column(name = "fulfillment_override", length = 40)
    private FulfillmentPreference fulfillmentOverride;
    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private Instant updatedAt;

    protected Gift() {}
    public Gift(Event event, String name, BigDecimal targetAmount) {
        this.id = UUID.randomUUID();
        this.event = Objects.requireNonNull(event);
        setDetails(name, null, null, targetAmount);
    }
    public void setDetails(String name, String description, String imageKey, BigDecimal targetAmount) {
        if (name == null || name.isBlank() || name.trim().length() > 200) throw new IllegalArgumentException("Invalid gift name");
        if (targetAmount == null || targetAmount.signum() <= 0 || targetAmount.scale() > 2 || targetAmount.precision() - targetAmount.scale() > 13)
            throw new IllegalArgumentException("Invalid target amount");
        if (imageKey != null && imageKey.length() > 512) throw new IllegalArgumentException("Invalid image key");
        this.name = name.trim(); this.description = description; this.imageKey = imageKey; this.targetAmount = targetAmount;
    }
    public void setFulfillmentOverride(FulfillmentPreference override) { this.fulfillmentOverride = override; }
    public FulfillmentPreference effectiveFulfillmentPreference() {
        return fulfillmentOverride != null ? fulfillmentOverride : event.getFulfillmentDefault();
    }
    public void closeUnfunded() { if (status != GiftStatus.ACTIVE) throw new IllegalStateException("Gift not active"); status = GiftStatus.CLOSED_UNFUNDED; }
    public void cancel() { if (status != GiftStatus.ACTIVE) throw new IllegalStateException("Gift not active"); status = GiftStatus.CANCELLED; }
    public GiftVisibility getVisibility() { return visibility; }
    public String getProductUrl() { return productUrl; }
    public void setVisibility(GiftVisibility visibility) { this.visibility = Objects.requireNonNull(visibility); }
    public void setProductUrl(String productUrl) { this.productUrl = productUrl; }
    public UUID getId() { return id; }
    public long getVersion() { return version; }
    public Event getEvent() { return event; }
    public GiftPriority getPriority() { return priority; }
    public void setPriority(GiftPriority priority) { this.priority = Objects.requireNonNull(priority); }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getImageKey() { return imageKey; }
    public BigDecimal getTargetAmount() { return targetAmount; }
    public GiftStatus getStatus() { return status; }
    public FulfillmentPreference getFulfillmentOverride() { return fulfillmentOverride; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
