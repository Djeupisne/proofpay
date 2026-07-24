package com.proofpay.subscription.domain;

import com.proofpay.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "subscriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription {

    @Id
    @GeneratedValue
    private UUID id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan", nullable = false)
    private SubscriptionPlan plan;

    @Column(name = "start_date")
    private Instant startDate;

    @Column(name = "end_date")
    private Instant endDate;

    @Column(name = "auto_renew")
    private boolean autoRenew;

    @Column(name = "is_active")
    private boolean active;

    @Column(name = "payment_id")
    private String paymentId;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    // ========== MÉTHODES ==========

    public boolean isActive() {
        return active && (endDate == null || Instant.now().isBefore(endDate));
    }

    public boolean isExpired() {
        return endDate != null && Instant.now().isAfter(endDate);
    }

    // ✅ Setters explicites pour les champs utilisés par SubscriptionService
    public void setEndDate(Instant endDate) {
        this.endDate = endDate;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setAutoRenew(boolean autoRenew) {
        this.autoRenew = autoRenew;
    }
}
