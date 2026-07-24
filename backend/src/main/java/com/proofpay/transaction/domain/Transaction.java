package com.proofpay.transaction.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "public_ref", nullable = false, unique = true, length = 30)
    private String publicRef;

    @Column(name = "buyer_id", nullable = false)
    private UUID buyerId;

    @Column(name = "seller_id", nullable = false)
    private UUID sellerId;

    @Column(nullable = false, length = 200)
    private String title;

    private String description;

    @Column(name = "category_code")
    private String categoryCode;

    @Column(length = 3)
    private String currency;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private BigDecimal fees;

    @Column(name = "total_amount", insertable = false, updatable = false)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TransactionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "confirmation_mode", nullable = false, length = 20)
    private ConfirmationMode confirmationMode;

    @Column(name = "confirmation_secret_hash")
    private String confirmationSecretHash;

    @Column(name = "delivery_deadline")
    private Instant deliveryDeadline;

    @Column(name = "auto_release_at")
    private Instant autoReleaseAt;

    @Column(name = "paid_at")
    private Instant paidAt;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    // ========== CONSTRUCTEURS ==========
    public Transaction() {}

    private Transaction(Builder builder) {
        this.id = builder.id;
        this.publicRef = builder.publicRef;
        this.buyerId = builder.buyerId;
        this.sellerId = builder.sellerId;
        this.title = builder.title;
        this.description = builder.description;
        this.categoryCode = builder.categoryCode;
        this.currency = builder.currency;
        this.amount = builder.amount;
        this.fees = builder.fees;
        this.totalAmount = builder.totalAmount;
        this.status = builder.status;
        this.confirmationMode = builder.confirmationMode;
        this.confirmationSecretHash = builder.confirmationSecretHash;
        this.deliveryDeadline = builder.deliveryDeadline;
        this.autoReleaseAt = builder.autoReleaseAt;
        this.paidAt = builder.paidAt;
        this.deliveredAt = builder.deliveredAt;
        this.confirmedAt = builder.confirmedAt;
        this.completedAt = builder.completedAt;
        this.cancelledAt = builder.cancelledAt;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    // ========== GETTERS ==========
    public UUID getId() { return id; }
    public String getPublicRef() { return publicRef; }
    public UUID getBuyerId() { return buyerId; }
    public UUID getSellerId() { return sellerId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getCategoryCode() { return categoryCode; }
    public String getCurrency() { return currency; }
    public BigDecimal getAmount() { return amount; }
    public BigDecimal getFees() { return fees; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public TransactionStatus getStatus() { return status; }
    public ConfirmationMode getConfirmationMode() { return confirmationMode; }
    public String getConfirmationSecretHash() { return confirmationSecretHash; }
    public Instant getDeliveryDeadline() { return deliveryDeadline; }
    public Instant getAutoReleaseAt() { return autoReleaseAt; }
    public Instant getPaidAt() { return paidAt; }
    public Instant getDeliveredAt() { return deliveredAt; }
    public Instant getConfirmedAt() { return confirmedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public Instant getCancelledAt() { return cancelledAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    // ========== SETTERS ==========
    public void setId(UUID id) { this.id = id; }
    public void setPublicRef(String publicRef) { this.publicRef = publicRef; }
    public void setBuyerId(UUID buyerId) { this.buyerId = buyerId; }
    public void setSellerId(UUID sellerId) { this.sellerId = sellerId; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setCategoryCode(String categoryCode) { this.categoryCode = categoryCode; }
    public void setCurrency(String currency) { this.currency = currency; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public void setFees(BigDecimal fees) { this.fees = fees; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public void setStatus(TransactionStatus status) { this.status = status; }
    public void setConfirmationMode(ConfirmationMode confirmationMode) { this.confirmationMode = confirmationMode; }
    public void setConfirmationSecretHash(String confirmationSecretHash) { this.confirmationSecretHash = confirmationSecretHash; }
    public void setDeliveryDeadline(Instant deliveryDeadline) { this.deliveryDeadline = deliveryDeadline; }
    public void setAutoReleaseAt(Instant autoReleaseAt) { this.autoReleaseAt = autoReleaseAt; }
    public void setPaidAt(Instant paidAt) { this.paidAt = paidAt; }
    public void setDeliveredAt(Instant deliveredAt) { this.deliveredAt = deliveredAt; }
    public void setConfirmedAt(Instant confirmedAt) { this.confirmedAt = confirmedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
    public void setCancelledAt(Instant cancelledAt) { this.cancelledAt = cancelledAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    // ========== BUILDER ==========
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private String publicRef;
        private UUID buyerId;
        private UUID sellerId;
        private String title;
        private String description;
        private String categoryCode;
        private String currency;
        private BigDecimal amount;
        private BigDecimal fees;
        private BigDecimal totalAmount;
        private TransactionStatus status;
        private ConfirmationMode confirmationMode;
        private String confirmationSecretHash;
        private Instant deliveryDeadline;
        private Instant autoReleaseAt;
        private Instant paidAt;
        private Instant deliveredAt;
        private Instant confirmedAt;
        private Instant completedAt;
        private Instant cancelledAt;
        private Instant createdAt;
        private Instant updatedAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder publicRef(String publicRef) { this.publicRef = publicRef; return this; }
        public Builder buyerId(UUID buyerId) { this.buyerId = buyerId; return this; }
        public Builder sellerId(UUID sellerId) { this.sellerId = sellerId; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder categoryCode(String categoryCode) { this.categoryCode = categoryCode; return this; }
        public Builder currency(String currency) { this.currency = currency; return this; }
        public Builder amount(BigDecimal amount) { this.amount = amount; return this; }
        public Builder fees(BigDecimal fees) { this.fees = fees; return this; }
        public Builder totalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; return this; }
        public Builder status(TransactionStatus status) { this.status = status; return this; }
        public Builder confirmationMode(ConfirmationMode confirmationMode) { this.confirmationMode = confirmationMode; return this; }
        public Builder confirmationSecretHash(String confirmationSecretHash) { this.confirmationSecretHash = confirmationSecretHash; return this; }
        public Builder deliveryDeadline(Instant deliveryDeadline) { this.deliveryDeadline = deliveryDeadline; return this; }
        public Builder autoReleaseAt(Instant autoReleaseAt) { this.autoReleaseAt = autoReleaseAt; return this; }
        public Builder paidAt(Instant paidAt) { this.paidAt = paidAt; return this; }
        public Builder deliveredAt(Instant deliveredAt) { this.deliveredAt = deliveredAt; return this; }
        public Builder confirmedAt(Instant confirmedAt) { this.confirmedAt = confirmedAt; return this; }
        public Builder completedAt(Instant completedAt) { this.completedAt = completedAt; return this; }
        public Builder cancelledAt(Instant cancelledAt) { this.cancelledAt = cancelledAt; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(Instant updatedAt) { this.updatedAt = updatedAt; return this; }

        public Transaction build() {
            return new Transaction(this);
        }
    }
}
