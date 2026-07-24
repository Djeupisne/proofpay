package com.proofpay.subscription.domain;

import com.proofpay.user.domain.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "subscriptions")
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

    // ========== CONSTRUCTEURS ==========
    public Subscription() {}

    private Subscription(Builder builder) {
        this.id = builder.id;
        this.user = builder.user;
        this.plan = builder.plan;
        this.startDate = builder.startDate;
        this.endDate = builder.endDate;
        this.autoRenew = builder.autoRenew;
        this.active = builder.active;
        this.paymentId = builder.paymentId;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    // ========== GETTERS ==========
    public UUID getId() { return id; }
    public User getUser() { return user; }
    public SubscriptionPlan getPlan() { return plan; }
    public Instant getStartDate() { return startDate; }
    public Instant getEndDate() { return endDate; }
    public boolean isAutoRenew() { return autoRenew; }
    public boolean isActive() { return active; }
    public String getPaymentId() { return paymentId; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    // ========== SETTERS ==========
    public void setId(UUID id) { this.id = id; }
    public void setUser(User user) { this.user = user; }
    public void setPlan(SubscriptionPlan plan) { this.plan = plan; }
    public void setStartDate(Instant startDate) { this.startDate = startDate; }
    public void setEndDate(Instant endDate) { this.endDate = endDate; }
    public void setAutoRenew(boolean autoRenew) { this.autoRenew = autoRenew; }
    public void setActive(boolean active) { this.active = active; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    // ========== MÉTHODES MÉTIER ==========
    public boolean isSubscriptionActive() {
        return active && (endDate == null || Instant.now().isBefore(endDate));
    }

    public boolean isExpired() {
        return endDate != null && Instant.now().isAfter(endDate);
    }

    // ========== BUILDER ==========
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private User user;
        private SubscriptionPlan plan;
        private Instant startDate;
        private Instant endDate;
        private boolean autoRenew;
        private boolean active;
        private String paymentId;
        private Instant createdAt;
        private Instant updatedAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder user(User user) { this.user = user; return this; }
        public Builder plan(SubscriptionPlan plan) { this.plan = plan; return this; }
        public Builder startDate(Instant startDate) { this.startDate = startDate; return this; }
        public Builder endDate(Instant endDate) { this.endDate = endDate; return this; }
        public Builder autoRenew(boolean autoRenew) { this.autoRenew = autoRenew; return this; }
        public Builder active(boolean active) { this.active = active; return this; }
        public Builder paymentId(String paymentId) { this.paymentId = paymentId; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(Instant updatedAt) { this.updatedAt = updatedAt; return this; }

        public Subscription build() {
            return new Subscription(this);
        }
    }
}
