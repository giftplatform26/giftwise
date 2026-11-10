
package com.giftplatform.user.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, length = 254)
    private String email;

    @Column(name = "password_hash",
            nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UserRole role;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "created_at", nullable = false,
            insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false,
            insertable = false, updatable = false)
    private Instant updatedAt;


    @Column(name = "username", length = 50)
    private String username;


    @Column(name = "full_name", length = 120)
    private String fullName;
    public String getFullName(){return fullName;}
    public void setFullName(String value){
        if(value==null||value.strip().length()<2||value.strip().length()>120||value.chars().anyMatch(Character::isISOControl))
            throw new IllegalArgumentException("Full name must be 2–120 characters");
        fullName=value.strip();
    }
    protected User() {
    }


    public User(
            String username,
            String email,
            String passwordHash
    ) {
        this(email, passwordHash);

        if (username == null ||
                !username.matches(com.giftplatform.security.CredentialPolicy.USERNAME_PATTERN)) {
            throw new IllegalArgumentException(
                    com.giftplatform.security.CredentialPolicy.USERNAME_MESSAGE
            );
        }

        this.username = username;
    }

    public User(String email, String passwordHash) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "Email is required"
            );
        }

        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException(
                    "Password hash is required"
            );
        }

        this.id = UUID.randomUUID();
        this.email = email.trim()
                .toLowerCase(Locale.ROOT);
        this.passwordHash = passwordHash;
        this.role = UserRole.CUSTOMER;
        this.enabled = true;
    }



    @Column(name = "phone_number", length = 16)
    private String phoneNumber;

    @Column(name = "phone_verified_at")
    private Instant phoneVerifiedAt;


    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public void setPasswordHash(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Password hash required");
        this.passwordHash = value;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    /** Only the offline provisioning runner may call this method. */
    public void provisionAsPlatformAdmin() {
        if (this.role != UserRole.CUSTOMER) {
            throw new IllegalStateException("Unexpected existing role");
        }
        this.role = UserRole.PLATFORM_ADMIN;
    }

    public UserRole getRole() {
        return role;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public String getUsername() {
        return username;
    }



    public String getPhoneNumber() {
        return phoneNumber;
    }

    public Instant getPhoneVerifiedAt() {
        return phoneVerifiedAt;
    }

    /** Store a claimed mobile number without claiming OTP ownership. */
    public void setUnverifiedPhoneNumber(String mobile) {
        if (mobile == null || !mobile.matches("^\\+[1-9][0-9]{7,14}$"))
            throw new IllegalArgumentException("Mobile must use international format");
        this.phoneNumber = mobile;
        this.phoneVerifiedAt = null;
    }

    public void verifyPhoneNumber(
            String phoneNumber,
            Instant verifiedAt
    ) {
        if (phoneNumber == null ||
                !phoneNumber.matches(
                        "^\\+[1-9][0-9]{1,14}$")) {
            throw new IllegalArgumentException(
                    "Invalid phone number");
        }

        if (verifiedAt == null) {
            throw new IllegalArgumentException(
                    "Verification time is required");
        }

        if (!enabled) {
            throw new IllegalStateException(
                    "Disabled user cannot verify phone");
        }

        this.phoneNumber = phoneNumber;
        this.phoneVerifiedAt = verifiedAt;
    }

}
