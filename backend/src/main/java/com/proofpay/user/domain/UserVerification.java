package com.proofpay.user.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_verifications")
public class UserVerification {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "verification_type", nullable = false, length = 30)
    private String verificationType;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(name = "verification_value")
    private String verificationValue;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @Column(name = "created_at")
    private Instant createdAt;

    // ========== CONSTRUCTEURS ==========
    public UserVerification() {}

    private UserVerification(Builder builder) {
        this.id = builder.id;
        this.userId = builder.userId;
        this.verificationType = builder.verificationType;
        this.status = builder.status;
        this.verificationValue = builder.verificationValue;
        this.expiresAt = builder.expiresAt;
        this.verifiedAt = builder.verifiedAt;
        this.createdAt = builder.createdAt;
    }

    // ========== GETTERS ==========
    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getVerificationType() { return verificationType; }
    public String getStatus() { return status; }
    public String getVerificationValue() { return verificationValue; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getVerifiedAt() { return verifiedAt; }
    public Instant getCreatedAt() { return createdAt; }

    // ========== SETTERS ==========
    public void setId(UUID id) { this.id = id; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public void setVerificationType(String verificationType) { this.verificationType = verificationType; }
    public void setStatus(String status) { this.status = status; }
    public void setVerificationValue(String verificationValue) { this.verificationValue = verificationValue; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public void setVerifiedAt(Instant verifiedAt) { this.verifiedAt = verifiedAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    // ========== BUILDER ==========
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private UUID userId;
        private String verificationType;
        private String status;
        private String verificationValue;
        private Instant expiresAt;
        private Instant verifiedAt;
        private Instant createdAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder userId(UUID userId) { this.userId = userId; return this; }
        public Builder verificationType(String verificationType) { this.verificationType = verificationType; return this; }
        public Builder status(String status) { this.status = status; return this; }
        public Builder verificationValue(String verificationValue) { this.verificationValue = verificationValue; return this; }
        public Builder expiresAt(Instant expiresAt) { this.expiresAt = expiresAt; return this; }
        public Builder verifiedAt(Instant verifiedAt) { this.verifiedAt = verifiedAt; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }

        public UserVerification build() {
            return new UserVerification(this);
        }
    }
}
