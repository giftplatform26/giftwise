
package com.giftplatform.otp.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "phone_otp_challenges")
public class PhoneOtpChallenge {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "phone_number",
            nullable = false, length = 16)
    private String phoneNumber;

    @Column(name = "otp_digest",
            nullable = false, length = 128)
    private String otpDigest;

    @Column(name = "created_at",
            nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "consumed_at")
    private Instant consumedAt;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    protected PhoneOtpChallenge() {
    }


    public PhoneOtpChallenge(
            UUID userId,
            String phoneNumber,
            String otpDigest,
            Instant createdAt,
            Instant expiresAt
    ) {
        this(
                UUID.randomUUID(),
                userId,
                phoneNumber,
                otpDigest,
                createdAt,
                expiresAt
        );
    }

    public PhoneOtpChallenge(
            UUID id,
            UUID userId,
            String phoneNumber,
            String otpDigest,
            Instant createdAt,
            Instant expiresAt
    ) {
        if (id == null || userId == null) {
            throw new IllegalArgumentException(
                    "Challenge ID and user ID are required");
        }

        if (phoneNumber == null ||
                !phoneNumber.matches(
                        "^\\+[1-9][0-9]{1,14}$")) {
            throw new IllegalArgumentException(
                    "Invalid phone number");
        }

        if (otpDigest == null || otpDigest.isBlank()) {
            throw new IllegalArgumentException(
                    "OTP digest is required");
        }

        if (createdAt == null || expiresAt == null ||
                !expiresAt.isAfter(createdAt)) {
            throw new IllegalArgumentException(
                    "Invalid OTP expiration");
        }

        this.id = id;
        this.userId = userId;
        this.phoneNumber = phoneNumber;
        this.otpDigest = otpDigest;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.attemptCount = 0;

        this.deliveryStatus = OtpDeliveryStatus.PENDING;
        this.deliveryUpdatedAt = createdAt;

    }


    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_status", nullable = false,
            length = 20)
    private OtpDeliveryStatus deliveryStatus =
            OtpDeliveryStatus.PENDING;

    @Column(name = "delivery_updated_at", nullable = false)
    private Instant deliveryUpdatedAt;

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getOtpDigest() {
        return otpDigest;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getConsumedAt() {
        return consumedAt;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public boolean isExpired(Instant now) {
        return !now.isBefore(expiresAt);
    }

    public boolean isConsumed() {
        return consumedAt != null;
    }

    public boolean hasAttemptsRemaining() {
        return attemptCount < 5;
    }

    public void recordFailedAttempt() {
        if (!hasAttemptsRemaining()) {
            throw new IllegalStateException(
                    "Maximum OTP attempts reached");
        }
        attemptCount++;
    }


    public void consume(Instant now) {
        if (!isReadyForVerification()
                || isConsumed()
                || isExpired(now)
                || !hasAttemptsRemaining()) {
            throw new IllegalStateException(
                    "OTP cannot be consumed");
        }

        consumedAt = now;
    }



    public OtpDeliveryStatus getDeliveryStatus() {
        return deliveryStatus;
    }

    public Instant getDeliveryUpdatedAt() {
        return deliveryUpdatedAt;
    }

    public boolean isReadyForVerification() {
        return deliveryStatus == OtpDeliveryStatus.SENT;
    }

    public void markSent(Instant now) {
        requirePending(now);
        deliveryStatus = OtpDeliveryStatus.SENT;
        deliveryUpdatedAt = now;
    }

    public void markFailed(Instant now) {
        requirePending(now);
        deliveryStatus = OtpDeliveryStatus.FAILED;
        deliveryUpdatedAt = now;
    }

    public void invalidate(Instant now) {
        if (now == null || now.isBefore(createdAt)) {
            throw new IllegalArgumentException(
                    "Invalid transition time");
        }

        if (deliveryStatus == OtpDeliveryStatus.INVALIDATED) {
            return;
        }

        if (isConsumed()) {
            throw new IllegalStateException(
                    "Consumed challenge cannot be invalidated");
        }

        deliveryStatus = OtpDeliveryStatus.INVALIDATED;
        deliveryUpdatedAt = now;
    }

    private void requirePending(Instant now) {
        if (now == null || now.isBefore(createdAt)) {
            throw new IllegalArgumentException(
                    "Invalid transition time");
        }

        if (deliveryStatus != OtpDeliveryStatus.PENDING
                || isConsumed()) {
            throw new IllegalStateException(
                    "Challenge is not pending");
        }
    }

}
