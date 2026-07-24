package com.proofpay.user.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "seller_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerProfile {

    @Id
    @GeneratedValue
    private UUID id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Informations professionnelles
    @Column(name = "business_name", nullable = false, length = 200)
    private String businessName;

    @Column(name = "business_type", length = 100)
    private String businessType;

    @Column(name = "registration_number", length = 100)
    private String registrationNumber;

    @Column(name = "tax_id", length = 100)
    private String taxId;

    @Column(name = "business_address", length = 500)
    private String businessAddress;

    @Column(name = "business_phone", length = 20)
    private String businessPhone;

    @Column(name = "business_email", length = 150)
    private String businessEmail;

    @Column(name = "website", length = 200)
    private String website;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "logo_url")
    private String logoUrl;

    // Documents
    @Column(name = "id_document_url")
    private String idDocumentUrl;

    @Column(name = "registration_document_url")
    private String registrationDocumentUrl;

    // Statistiques
    @Column(name = "rating")
    private Double rating;

    @Column(name = "total_transactions")
    private Integer totalTransactions;

    @Column(name = "completed_transactions")
    private Integer completedTransactions;

    @Column(name = "success_rate")
    private Double successRate;

    // Statut du profil
    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status")
    private VerificationStatus verificationStatus;

    @Column(name = "is_verified")
    private boolean verified;

    @Column(name = "is_approved")
    private boolean approved;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public enum VerificationStatus {
        PENDING,
        VERIFIED,
        REJECTED
    }
}
