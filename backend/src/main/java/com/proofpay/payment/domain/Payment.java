package com.proofpay.payment.domain;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import org.hibernate.annotations.Type;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "transaction_id", nullable = false)
    private UUID transactionId;

    @Column(name = "provider_code", nullable = false, length = 50)
    private String providerCode;

    @Column(length = 30)
    private String channel;

    @Column(name = "request_ref", nullable = false, unique = true, length = 100)
    private String requestRef;

    @Column(name = "provider_ref", length = 100)
    private String providerRef;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(name = "failure_reason")
    private String failureReason;

    @Type(JsonType.class)
    @Column(name = "callback_payload", columnDefinition = "jsonb")
    private Map<String, Object> callbackPayload;

    @Column(name = "paid_at")
    private Instant paidAt;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    // ========== CONSTRUCTEURS ==========
    public Payment() {}

    private Payment(Builder builder) {
        this.id = builder.id;
        this.transactionId = builder.transactionId;
        this.providerCode = builder.providerCode;
        this.channel = builder.channel;
        this.requestRef = builder.requestRef;
        this.providerRef = builder.providerRef;
        this.amount = builder.amount;
        this.currency = builder.currency;
        this.status = builder.status;
        this.failureReason = builder.failureReason;
        this.callbackPayload = builder.callbackPayload;
        this.paidAt = builder.paidAt;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    // ========== GETTERS ==========
    public UUID getId() { return id; }
    public UUID getTransactionId() { return transactionId; }
    public String getProviderCode() { return providerCode; }
    public String getChannel() { return channel; }
    public String getRequestRef() { return requestRef; }
    public String getProviderRef() { return providerRef; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public PaymentStatus getStatus() { return status; }
    public String getFailureReason() { return failureReason; }
    public Map<String, Object> getCallbackPayload() { return callbackPayload; }
    public Instant getPaidAt() { return paidAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    // ========== SETTERS ==========
    public void setId(UUID id) { this.id = id; }
    public void setTransactionId(UUID transactionId) { this.transactionId = transactionId; }
    public void setProviderCode(String providerCode) { this.providerCode = providerCode; }
    public void setChannel(String channel) { this.channel = channel; }
    public void setRequestRef(String requestRef) { this.requestRef = requestRef; }
    public void setProviderRef(String providerRef) { this.providerRef = providerRef; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public void setCurrency(String currency) { this.currency = currency; }
    public void setStatus(PaymentStatus status) { this.status = status; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    public void setCallbackPayload(Map<String, Object> callbackPayload) { this.callbackPayload = callbackPayload; }
    public void setPaidAt(Instant paidAt) { this.paidAt = paidAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    // ========== BUILDER ==========
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private UUID transactionId;
        private String providerCode;
        private String channel;
        private String requestRef;
        private String providerRef;
        private BigDecimal amount;
        private String currency;
        private PaymentStatus status;
        private String failureReason;
        private Map<String, Object> callbackPayload;
        private Instant paidAt;
        private Instant createdAt;
        private Instant updatedAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder transactionId(UUID transactionId) { this.transactionId = transactionId; return this; }
        public Builder providerCode(String providerCode) { this.providerCode = providerCode; return this; }
        public Builder channel(String channel) { this.channel = channel; return this; }
        public Builder requestRef(String requestRef) { this.requestRef = requestRef; return this; }
        public Builder providerRef(String providerRef) { this.providerRef = providerRef; return this; }
        public Builder amount(BigDecimal amount) { this.amount = amount; return this; }
        public Builder currency(String currency) { this.currency = currency; return this; }
        public Builder status(PaymentStatus status) { this.status = status; return this; }
        public Builder failureReason(String failureReason) { this.failureReason = failureReason; return this; }
        public Builder callbackPayload(Map<String, Object> callbackPayload) { this.callbackPayload = callbackPayload; return this; }
        public Builder paidAt(Instant paidAt) { this.paidAt = paidAt; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(Instant updatedAt) { this.updatedAt = updatedAt; return this; }

        public Payment build() {
            return new Payment(this);
        }
    }
}
