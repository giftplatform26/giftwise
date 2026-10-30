
package com.giftplatform.event.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "events")
public class Event {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;
    @Version
    @Column(name = "version", nullable = false)
    private long version;
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "occasion_type", nullable = false, length = 50)
    private OccasionType occasionType;

    @Column(name="address_line1",length=200) private String addressLine1;
    @Column(name="address_city",length=120) private String city;
    @Column(name="address_state",length=120) private String state;
    @Column(name="address_postal_code",length=20) private String postalCode;
    @Column(name="address_country",length=80) private String country;
    @Column(name="contact_name",length=150) private String contactName;
    @Column(name="host_display_name",nullable=false,length=200) private String hostDisplayName;
    public String getHostDisplayName(){return hostDisplayName;}
    public void setHostDisplayName(String value){this.hostDisplayName=value;}
    @Column(name="contact_phone",length=30) private String contactPhone;
    @Column(name="contact_email",length=254) private String contactEmail;
    public String getAddressLine1(){return addressLine1;}
    public String getCity(){return city;}
    public String getState(){return state;}
    public String getPostalCode(){return postalCode;}
    public String getCountry(){return country;}
    public String getContactName(){return contactName;}
    public String getContactPhone(){return contactPhone;}
    public String getContactEmail(){return contactEmail;}
    public void setEventContact(String line,String city,String state,String postal,String country,String name,String phone,String email){
      this.addressLine1=line;this.city=city;this.state=state;this.postalCode=postal;
      this.country=country;this.contactName=name;this.contactPhone=phone;this.contactEmail=email;
    }
    @Column(name = "event_date")
    private LocalDate eventDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private EventStatus status = EventStatus.DRAFT;

    @Column(name = "created_at", nullable = false,
            insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false,
            insertable = false, updatable = false)
    private Instant updatedAt;

    @Column(name = "owner_id")
    private UUID ownerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "fulfillment_default", nullable = false, length = 40)
    private com.giftplatform.gift.entity.FulfillmentPreference fulfillmentDefault =
            com.giftplatform.gift.entity.FulfillmentPreference.AFTER_EVENT_CONFIRMATION;


    @Column(name = "public_token", nullable = false, unique = true,
            columnDefinition = "uuid")
    private UUID publicToken = UUID.randomUUID();

    @Enumerated(EnumType.STRING)
    @Column(name = "qr_theme", nullable = false, length = 40)
    private com.giftplatform.qr.QrTheme qrTheme =
            com.giftplatform.qr.QrTheme.MODERN_MINIMAL;

    public com.giftplatform.gift.entity.FulfillmentPreference getFulfillmentDefault() {
        return fulfillmentDefault;
    }

    public void changeFulfillmentDefault(com.giftplatform.gift.entity.FulfillmentPreference preference) {
        this.fulfillmentDefault = java.util.Objects.requireNonNull(preference);
    }

    public UUID getOwnerId() { return ownerId; }

    public void assignOwner(UUID ownerId) {
        if (this.ownerId != null || ownerId == null) throw new IllegalStateException("Invalid owner assignment");
        this.ownerId = ownerId;
    }

    public void update(String title, String description, OccasionType occasionType, LocalDate eventDate) {
        this.title = title;
        this.description = description;
        this.occasionType = occasionType;
        this.eventDate = eventDate;
    }

    public void transition(EventStatus next) {
        boolean allowed = switch (status) {
            case DRAFT ->
                    next == EventStatus.PUBLISHED ||
                            next == EventStatus.CANCELLED;

            case PUBLISHED ->
                    next == EventStatus.DRAFT ||
                            next == EventStatus.COMPLETED ||
                            next == EventStatus.CANCELLED;

            case COMPLETED, CANCELLED -> false;
        };

        if (!allowed) {
            throw new IllegalStateException(
                    "Invalid event status transition"
            );
        }

        status = next;
    }

    protected Event() {
        // Required by JPA
    }

    public Event(String title, OccasionType occasionType) {
        this.id = UUID.randomUUID();
        this.title = title;
        this.occasionType = occasionType;
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public OccasionType getOccasionType() {
        return occasionType;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public EventStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public long getVersion() {
        return version;
    }


    public UUID getPublicToken() {
        return publicToken;
    }

    public com.giftplatform.qr.QrTheme getQrTheme() {
        return qrTheme;
    }

    public void changeQrTheme(com.giftplatform.qr.QrTheme theme) {
        this.qrTheme = java.util.Objects.requireNonNull(theme);
    }
}
