package com.proofpay.payment.domain;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import org.hibernate.annotations.Type;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "payment_attempts")
public class PaymentAttempt {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "payment_id", nullable = false)
    private UUID paymentId;

    @Column(name = "attempt_no", nullable = false)
    private Integer attemptNo;

    @Column(name = "result_code")
    private String resultCode;

    @Column(name = "result_message")
    private String resultMessage;

    @Type(JsonType.class)
    @Column(name = "raw_payload", columnDefinition = "jsonb")
    private Map<String, Object> rawPayload;

    @Column(name = "created_at")
    private Instant createdAt;

    // ========== CONSTRUCTEURS ==========
    public PaymentAttempt() {}

    private PaymentAttempt(Builder builder) {
        this.id = builder.id;
        this.paymentId = builder.paymentId;
        this.attemptNo = builder.attemptNo;
        this.resultCode = builder.resultCode;
        this.resultMessage = builder.resultMessage;
        this.rawPayload = builder.rawPayload;
        this.createdAt = builder.createdAt;
    }

    // ========== GETTERS ==========
    public UUID getId() { return id; }
    public UUID getPaymentId() { return paymentId; }
    public Integer getAttemptNo() { return attemptNo; }
    public String getResultCode() { return resultCode; }
    public String getResultMessage() { return resultMessage; }
    public Map<String, Object> getRawPayload() { return rawPayload; }
    public Instant getCreatedAt() { return createdAt; }

    // ========== SETTERS ==========
    public void setId(UUID id) { this.id = id; }
    public void setPaymentId(UUID paymentId) { this.paymentId = paymentId; }
    public void setAttemptNo(Integer attemptNo) { this.attemptNo = attemptNo; }
    public void setResultCode(String resultCode) { this.resultCode = resultCode; }
    public void setResultMessage(String resultMessage) { this.resultMessage = resultMessage; }
    public void setRawPayload(Map<String, Object> rawPayload) { this.rawPayload = rawPayload; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    // ========== BUILDER ==========
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private UUID paymentId;
        private Integer attemptNo;
        private String resultCode;
        private String resultMessage;
        private Map<String, Object> rawPayload;
        private Instant createdAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder paymentId(UUID paymentId) { this.paymentId = paymentId; return this; }
        public Builder attemptNo(Integer attemptNo) { this.attemptNo = attemptNo; return this; }
        public Builder resultCode(String resultCode) { this.resultCode = resultCode; return this; }
        public Builder resultMessage(String resultMessage) { this.resultMessage = resultMessage; return this; }
        public Builder rawPayload(Map<String, Object> rawPayload) { this.rawPayload = rawPayload; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }

        public PaymentAttempt build() {
            return new PaymentAttempt(this);
        }
    }
}
