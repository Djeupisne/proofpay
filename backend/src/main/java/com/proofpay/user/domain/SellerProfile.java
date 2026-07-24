package com.proofpay.user.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "seller_profiles")
public class SellerProfile {

    @Id
    @GeneratedValue
    private UUID id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

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

    @Column(name = "id_document_url")
    private String idDocumentUrl;

    @Column(name = "registration_document_url")
    private String registrationDocumentUrl;

    @Column(name = "rating")
    private Double rating;

    @Column(name = "total_transactions")
    private Integer totalTransactions;

    @Column(name = "completed_transactions")
    private Integer completedTransactions;

    @Column(name = "success_rate")
    private Double successRate;

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

    // ========== CONSTRUCTEURS ==========
    public SellerProfile() {}

    private SellerProfile(Builder builder) {
        this.id = builder.id;
        this.user = builder.user;
        this.businessName = builder.businessName;
        this.businessType = builder.businessType;
        this.registrationNumber = builder.registrationNumber;
        this.taxId = builder.taxId;
        this.businessAddress = builder.businessAddress;
        this.businessPhone = builder.businessPhone;
        this.businessEmail = builder.businessEmail;
        this.website = builder.website;
        this.description = builder.description;
        this.logoUrl = builder.logoUrl;
        this.idDocumentUrl = builder.idDocumentUrl;
        this.registrationDocumentUrl = builder.registrationDocumentUrl;
        this.rating = builder.rating;
        this.totalTransactions = builder.totalTransactions;
        this.completedTransactions = builder.completedTransactions;
        this.successRate = builder.successRate;
        this.verificationStatus = builder.verificationStatus;
        this.verified = builder.verified;
        this.approved = builder.approved;
        this.approvedAt = builder.approvedAt;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    // ========== GETTERS ==========
    public UUID getId() { return id; }
    public User getUser() { return user; }
    public String getBusinessName() { return businessName; }
    public String getBusinessType() { return businessType; }
    public String getRegistrationNumber() { return registrationNumber; }
    public String getTaxId() { return taxId; }
    public String getBusinessAddress() { return businessAddress; }
    public String getBusinessPhone() { return businessPhone; }
    public String getBusinessEmail() { return businessEmail; }
    public String getWebsite() { return website; }
    public String getDescription() { return description; }
    public String getLogoUrl() { return logoUrl; }
    public String getIdDocumentUrl() { return idDocumentUrl; }
    public String getRegistrationDocumentUrl() { return registrationDocumentUrl; }
    public Double getRating() { return rating; }
    public Integer getTotalTransactions() { return totalTransactions; }
    public Integer getCompletedTransactions() { return completedTransactions; }
    public Double getSuccessRate() { return successRate; }
    public VerificationStatus getVerificationStatus() { return verificationStatus; }
    public boolean isVerified() { return verified; }
    public boolean isApproved() { return approved; }
    public Instant getApprovedAt() { return approvedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    // ========== SETTERS ==========
    public void setId(UUID id) { this.id = id; }
    public void setUser(User user) { this.user = user; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }
    public void setBusinessType(String businessType) { this.businessType = businessType; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }
    public void setTaxId(String taxId) { this.taxId = taxId; }
    public void setBusinessAddress(String businessAddress) { this.businessAddress = businessAddress; }
    public void setBusinessPhone(String businessPhone) { this.businessPhone = businessPhone; }
    public void setBusinessEmail(String businessEmail) { this.businessEmail = businessEmail; }
    public void setWebsite(String website) { this.website = website; }
    public void setDescription(String description) { this.description = description; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
    public void setIdDocumentUrl(String idDocumentUrl) { this.idDocumentUrl = idDocumentUrl; }
    public void setRegistrationDocumentUrl(String registrationDocumentUrl) { this.registrationDocumentUrl = registrationDocumentUrl; }
    public void setRating(Double rating) { this.rating = rating; }
    public void setTotalTransactions(Integer totalTransactions) { this.totalTransactions = totalTransactions; }
    public void setCompletedTransactions(Integer completedTransactions) { this.completedTransactions = completedTransactions; }
    public void setSuccessRate(Double successRate) { this.successRate = successRate; }
    public void setVerificationStatus(VerificationStatus verificationStatus) { this.verificationStatus = verificationStatus; }
    public void setVerified(boolean verified) { this.verified = verified; }
    public void setApproved(boolean approved) { this.approved = approved; }
    public void setApprovedAt(Instant approvedAt) { this.approvedAt = approvedAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    // ========== BUILDER ==========
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private User user;
        private String businessName;
        private String businessType;
        private String registrationNumber;
        private String taxId;
        private String businessAddress;
        private String businessPhone;
        private String businessEmail;
        private String website;
        private String description;
        private String logoUrl;
        private String idDocumentUrl;
        private String registrationDocumentUrl;
        private Double rating;
        private Integer totalTransactions;
        private Integer completedTransactions;
        private Double successRate;
        private VerificationStatus verificationStatus;
        private boolean verified;
        private boolean approved;
        private Instant approvedAt;
        private Instant createdAt;
        private Instant updatedAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder user(User user) { this.user = user; return this; }
        public Builder businessName(String businessName) { this.businessName = businessName; return this; }
        public Builder businessType(String businessType) { this.businessType = businessType; return this; }
        public Builder registrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; return this; }
        public Builder taxId(String taxId) { this.taxId = taxId; return this; }
        public Builder businessAddress(String businessAddress) { this.businessAddress = businessAddress; return this; }
        public Builder businessPhone(String businessPhone) { this.businessPhone = businessPhone; return this; }
        public Builder businessEmail(String businessEmail) { this.businessEmail = businessEmail; return this; }
        public Builder website(String website) { this.website = website; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder logoUrl(String logoUrl) { this.logoUrl = logoUrl; return this; }
        public Builder idDocumentUrl(String idDocumentUrl) { this.idDocumentUrl = idDocumentUrl; return this; }
        public Builder registrationDocumentUrl(String registrationDocumentUrl) { this.registrationDocumentUrl = registrationDocumentUrl; return this; }
        public Builder rating(Double rating) { this.rating = rating; return this; }
        public Builder totalTransactions(Integer totalTransactions) { this.totalTransactions = totalTransactions; return this; }
        public Builder completedTransactions(Integer completedTransactions) { this.completedTransactions = completedTransactions; return this; }
        public Builder successRate(Double successRate) { this.successRate = successRate; return this; }
        public Builder verificationStatus(VerificationStatus verificationStatus) { this.verificationStatus = verificationStatus; return this; }
        public Builder verified(boolean verified) { this.verified = verified; return this; }
        public Builder approved(boolean approved) { this.approved = approved; return this; }
        public Builder approvedAt(Instant approvedAt) { this.approvedAt = approvedAt; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(Instant updatedAt) { this.updatedAt = updatedAt; return this; }

        public SellerProfile build() {
            return new SellerProfile(this);
        }
    }
}
