package com.proofpay.payment.domain;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    // ✅ Getters explicites pour PaymentReconciliationJob
    public String getProviderCode() {
        return providerCode;
    }

    public String getProviderRef() {
        return providerRef;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public void setPaidAt(Instant paidAt) {
        this.paidAt = paidAt;
    }
}
