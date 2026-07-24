package com.proofpay.transaction.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    // ✅ Méthodes getters explicites (déjà fournies par @Getter, mais ajoutées pour clarté)
    public UUID getId() { return id; }
    public UUID getBuyerId() { return buyerId; }
    public UUID getSellerId() { return sellerId; }
    public String getTitle() { return title; }
    public String getCurrency() { return currency; }
    public BigDecimal getAmount() { return amount; }
    public TransactionStatus getStatus() { return status; }
}
