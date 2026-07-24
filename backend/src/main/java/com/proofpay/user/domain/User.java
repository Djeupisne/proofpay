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

    // ===== CHAMPS POUR LA RÉPUTATION =====
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

    // ========== GETTERS ET SETTERS POUR LA RÉPUTATION ==========

    public BigDecimal getRating() {
        return rating != null ? rating : BigDecimal.ZERO;
    }

    public void setRating(BigDecimal rating) {
        this.rating = rating;
    }

    public Integer getTransactionsCount() {
        return transactionsCount != null ? transactionsCount : 0;
    }

    public void setTransactionsCount(Integer transactionsCount) {
        this.transactionsCount = transactionsCount;
    }

    public Integer getDisputesOpenedCount() {
        return disputesOpenedCount != null ? disputesOpenedCount : 0;
    }

    public void setDisputesOpenedCount(Integer disputesOpenedCount) {
        this.disputesOpenedCount = disputesOpenedCount;
    }

    public Integer getDisputesLostCount() {
        return disputesLostCount != null ? disputesLostCount : 0;
    }

    public void setDisputesLostCount(Integer disputesLostCount) {
        this.disputesLostCount = disputesLostCount;
    }
}
