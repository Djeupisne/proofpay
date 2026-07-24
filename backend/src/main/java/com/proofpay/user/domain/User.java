package com.proofpay.user.domain;

import com.proofpay.notification.domain.NotificationChannel;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
    @Builder.Default
    private NotificationChannel preferredChannel = NotificationChannel.SMS;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UserStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    // ===== CHAMPS VENDEUR =====
    @Column(name = "is_seller", nullable = false)
    @Builder.Default
    private boolean isSeller = false;

    @Column(name = "is_buyer", nullable = false)
    @Builder.Default
    private boolean isBuyer = true;

    @Column(name = "is_verified_seller")
    @Builder.Default
    private boolean verifiedSeller = false;

    @Column(name = "is_approved_seller")
    @Builder.Default
    private boolean approvedSeller = false;

    @Column(name = "seller_verified_at")
    private Instant sellerVerifiedAt;

    // ===== CHAMPS RÉPUTATION =====
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

    // ========== MÉTHODES ==========

    public boolean canTransact() {
        return status == UserStatus.ACTIVE;
    }

    public boolean isActiveSeller() {
        return isSeller && status == UserStatus.ACTIVE && verifiedSeller && approvedSeller;
    }

    // ✅ Getters explicites pour AuthController et SellerController
    public UUID getId() {
        return id;
    }

    public String getPhone() {
        return phone;
    }

    public UserRole getRole() {
        return role;
    }

    public NotificationChannel getPreferredChannel() {
        return preferredChannel != null ? preferredChannel : NotificationChannel.SMS;
    }

    public UserStatus getStatus() {
        return status;
    }

    public String getDisplayName() {
        return displayName;
    }

    // ✅ Setters explicites
    public void setEmail(String email) {
        this.email = email;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public void setLastLoginAt(Instant lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public void setPreferredChannel(NotificationChannel preferredChannel) {
        this.preferredChannel = preferredChannel;
    }

    // ✅ Getters avec valeurs par défaut
    public BigDecimal getRating() {
        return rating != null ? rating : BigDecimal.ZERO;
    }

    public Integer getTransactionsCount() {
        return transactionsCount != null ? transactionsCount : 0;
    }

    public Integer getDisputesOpenedCount() {
        return disputesOpenedCount != null ? disputesOpenedCount : 0;
    }

    public Integer getDisputesLostCount() {
        return disputesLostCount != null ? disputesLostCount : 0;
    }

    public boolean isSeller() {
        return isSeller;
    }

    public boolean isBuyer() {
        return isBuyer;
    }

    public boolean isVerifiedSeller() {
        return verifiedSeller;
    }

    public boolean isApprovedSeller() {
        return approvedSeller;
    }
}
