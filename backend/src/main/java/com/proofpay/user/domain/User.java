package com.proofpay.user.domain;

import com.proofpay.notification.domain.NotificationChannel;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true, length = 20)
    private String phone;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(name = "display_name", length = 150)
    private String displayName;

    @Column(length = 150)
    private String email;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(name = "preferred_language", length = 10)
    private String preferredLanguage;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_channel", nullable = false)
    private NotificationChannel preferredChannel = NotificationChannel.SMS;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UserStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Column(name = "is_seller", nullable = false)
    private boolean isSeller = false;

    @Column(name = "is_buyer", nullable = false)
    private boolean isBuyer = true;

    @Column(name = "is_verified_seller")
    private boolean verifiedSeller = false;

    @Column(name = "is_approved_seller")
    private boolean approvedSeller = false;

    @Column(name = "seller_verified_at")
    private Instant sellerVerifiedAt;

    @Column(precision = 10, scale = 2)
    private BigDecimal rating;

    @Column(name = "transactions_count")
    private Integer transactionsCount;

    @Column(name = "disputes_opened_count")
    private Integer disputesOpenedCount;

    @Column(name = "disputes_lost_count")
    private Integer disputesLostCount;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    // ========== CONSTRUCTEURS ==========
    public User() {}

    private User(Builder builder) {
        this.id = builder.id;
        this.phone = builder.phone;
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.displayName = builder.displayName;
        this.email = builder.email;
        this.photoUrl = builder.photoUrl;
        this.preferredLanguage = builder.preferredLanguage;
        this.preferredChannel = builder.preferredChannel;
        this.status = builder.status;
        this.role = builder.role;
        this.isSeller = builder.isSeller;
        this.isBuyer = builder.isBuyer;
        this.verifiedSeller = builder.verifiedSeller;
        this.approvedSeller = builder.approvedSeller;
        this.sellerVerifiedAt = builder.sellerVerifiedAt;
        this.rating = builder.rating;
        this.transactionsCount = builder.transactionsCount;
        this.disputesOpenedCount = builder.disputesOpenedCount;
        this.disputesLostCount = builder.disputesLostCount;
        this.lastLoginAt = builder.lastLoginAt;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    // ========== GETTERS ==========
    public UUID getId() { return id; }
    public String getPhone() { return phone; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getDisplayName() { return displayName; }
    public String getEmail() { return email; }
    public String getPhotoUrl() { return photoUrl; }
    public String getPreferredLanguage() { return preferredLanguage; }
    public NotificationChannel getPreferredChannel() { return preferredChannel != null ? preferredChannel : NotificationChannel.SMS; }
    public UserStatus getStatus() { return status; }
    public UserRole getRole() { return role; }
    public boolean isSeller() { return isSeller; }
    public boolean isBuyer() { return isBuyer; }
    public boolean isVerifiedSeller() { return verifiedSeller; }
    public boolean isApprovedSeller() { return approvedSeller; }
    public Instant getSellerVerifiedAt() { return sellerVerifiedAt; }
    public BigDecimal getRating() { return rating != null ? rating : BigDecimal.ZERO; }
    public Integer getTransactionsCount() { return transactionsCount != null ? transactionsCount : 0; }
    public Integer getDisputesOpenedCount() { return disputesOpenedCount != null ? disputesOpenedCount : 0; }
    public Integer getDisputesLostCount() { return disputesLostCount != null ? disputesLostCount : 0; }
    public Instant getLastLoginAt() { return lastLoginAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    // ========== SETTERS ==========
    public void setId(UUID id) { this.id = id; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public void setEmail(String email) { this.email = email; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    public void setPreferredLanguage(String preferredLanguage) { this.preferredLanguage = preferredLanguage; }
    public void setPreferredChannel(NotificationChannel preferredChannel) { this.preferredChannel = preferredChannel; }
    public void setStatus(UserStatus status) { this.status = status; }
    public void setRole(UserRole role) { this.role = role; }
    public void setSeller(boolean seller) { isSeller = seller; }
    public void setBuyer(boolean buyer) { isBuyer = buyer; }
    public void setVerifiedSeller(boolean verifiedSeller) { this.verifiedSeller = verifiedSeller; }
    public void setApprovedSeller(boolean approvedSeller) { this.approvedSeller = approvedSeller; }
    public void setSellerVerifiedAt(Instant sellerVerifiedAt) { this.sellerVerifiedAt = sellerVerifiedAt; }
    public void setRating(BigDecimal rating) { this.rating = rating; }
    public void setTransactionsCount(Integer transactionsCount) { this.transactionsCount = transactionsCount; }
    public void setDisputesOpenedCount(Integer disputesOpenedCount) { this.disputesOpenedCount = disputesOpenedCount; }
    public void setDisputesLostCount(Integer disputesLostCount) { this.disputesLostCount = disputesLostCount; }
    public void setLastLoginAt(Instant lastLoginAt) { this.lastLoginAt = lastLoginAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    // ========== MÉTHODES MÉTIER ==========
    public boolean canTransact() {
        return status == UserStatus.ACTIVE;
    }

    public boolean isActiveSeller() {
        return isSeller && status == UserStatus.ACTIVE && verifiedSeller && approvedSeller;
    }

    // ========== BUILDER ==========
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private String phone;
        private String firstName;
        private String lastName;
        private String displayName;
        private String email;
        private String photoUrl;
        private String preferredLanguage;
        private NotificationChannel preferredChannel = NotificationChannel.SMS;
        private UserStatus status;
        private UserRole role;
        private boolean isSeller = false;
        private boolean isBuyer = true;
        private boolean verifiedSeller = false;
        private boolean approvedSeller = false;
        private Instant sellerVerifiedAt;
        private BigDecimal rating;
        private Integer transactionsCount = 0;
        private Integer disputesOpenedCount = 0;
        private Integer disputesLostCount = 0;
        private Instant lastLoginAt;
        private Instant createdAt;
        private Instant updatedAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder phone(String phone) { this.phone = phone; return this; }
        public Builder firstName(String firstName) { this.firstName = firstName; return this; }
        public Builder lastName(String lastName) { this.lastName = lastName; return this; }
        public Builder displayName(String displayName) { this.displayName = displayName; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder photoUrl(String photoUrl) { this.photoUrl = photoUrl; return this; }
        public Builder preferredLanguage(String preferredLanguage) { this.preferredLanguage = preferredLanguage; return this; }
        public Builder preferredChannel(NotificationChannel preferredChannel) { this.preferredChannel = preferredChannel; return this; }
        public Builder status(UserStatus status) { this.status = status; return this; }
        public Builder role(UserRole role) { this.role = role; return this; }
        public Builder isSeller(boolean isSeller) { this.isSeller = isSeller; return this; }
        public Builder isBuyer(boolean isBuyer) { this.isBuyer = isBuyer; return this; }
        public Builder verifiedSeller(boolean verifiedSeller) { this.verifiedSeller = verifiedSeller; return this; }
        public Builder approvedSeller(boolean approvedSeller) { this.approvedSeller = approvedSeller; return this; }
        public Builder sellerVerifiedAt(Instant sellerVerifiedAt) { this.sellerVerifiedAt = sellerVerifiedAt; return this; }
        public Builder rating(BigDecimal rating) { this.rating = rating; return this; }
        public Builder transactionsCount(Integer transactionsCount) { this.transactionsCount = transactionsCount; return this; }
        public Builder disputesOpenedCount(Integer disputesOpenedCount) { this.disputesOpenedCount = disputesOpenedCount; return this; }
        public Builder disputesLostCount(Integer disputesLostCount) { this.disputesLostCount = disputesLostCount; return this; }
        public Builder lastLoginAt(Instant lastLoginAt) { this.lastLoginAt = lastLoginAt; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(Instant updatedAt) { this.updatedAt = updatedAt; return this; }

        public User build() {
            return new User(this);
        }
    }
}
