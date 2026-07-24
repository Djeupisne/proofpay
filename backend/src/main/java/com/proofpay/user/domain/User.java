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

    // Canal préféré pour les notifications
    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_channel", nullable = false)
    @Builder.Default
    private NotificationChannel preferredChannel = NotificationChannel.SMS;

    // 🔥 NOUVEAU : Rôle utilisateur (BUYER, SELLER, ADMIN)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    // 🔥 NOUVEAU : Statut du compte utilisateur
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UserStatus status;

    // 🔥 NOUVEAU : Indique si c'est un vendeur
    @Column(name = "is_seller", nullable = false)
    @Builder.Default
    private boolean isSeller = false;

    // 🔥 NOUVEAU : Indique si c'est un acheteur
    @Column(name = "is_buyer", nullable = false)
    @Builder.Default
    private boolean isBuyer = true;

    // 🔥 NOUVEAU : Indique si le vendeur est vérifié
    @Column(name = "is_verified_seller")
    @Builder.Default
    private boolean verifiedSeller = false;

    // 🔥 NOUVEAU : Indique si le vendeur est approuvé
    @Column(name = "is_approved_seller")
    @Builder.Default
    private boolean approvedSeller = false;

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

    /** Règle métier #14 : un utilisateur suspendu ne peut plus créer ni accepter de transactions. */
    public boolean canTransact() {
        return status == UserStatus.ACTIVE;
    }

    /** Vérifie si l'utilisateur est un vendeur actif */
    public boolean isActiveSeller() {
        return isSeller && status == UserStatus.ACTIVE;
    }

    /** Vérifie si l'utilisateur est un vendeur vérifié */
    public boolean isVerifiedSeller() {
        return isSeller && verifiedSeller && approvedSeller;
    }
}
