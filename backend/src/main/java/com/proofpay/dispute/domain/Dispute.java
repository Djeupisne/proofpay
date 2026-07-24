package com.proofpay.dispute.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "disputes")
public class Dispute {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "transaction_id", nullable = false)
    private UUID transactionId;

    @Column(name = "opened_by", nullable = false)
    private UUID openedBy;

    @Column(name = "reason_code", nullable = false, length = 50)
    private String reasonCode;

    @Column(name = "reason_details")
    private String reasonDetails;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DisputeStatus status;

    @Column(name = "decision_code")
    private String decisionCode;

    @Column(name = "decision_comment")
    private String decisionComment;

    @Column(name = "resolved_by")
    private UUID resolvedBy;

    @Column(name = "opened_at")
    private Instant openedAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    // ========== CONSTRUCTEURS ==========
    public Dispute() {}

    private Dispute(Builder builder) {
        this.id = builder.id;
        this.transactionId = builder.transactionId;
        this.openedBy = builder.openedBy;
        this.reasonCode = builder.reasonCode;
        this.reasonDetails = builder.reasonDetails;
        this.status = builder.status;
        this.decisionCode = builder.decisionCode;
        this.decisionComment = builder.decisionComment;
        this.resolvedBy = builder.resolvedBy;
        this.openedAt = builder.openedAt;
        this.resolvedAt = builder.resolvedAt;
    }

    // ========== GETTERS ==========
    public UUID getId() { return id; }
    public UUID getTransactionId() { return transactionId; }
    public UUID getOpenedBy() { return openedBy; }
    public String getReasonCode() { return reasonCode; }
    public String getReasonDetails() { return reasonDetails; }
    public DisputeStatus getStatus() { return status; }
    public String getDecisionCode() { return decisionCode; }
    public String getDecisionComment() { return decisionComment; }
    public UUID getResolvedBy() { return resolvedBy; }
    public Instant getOpenedAt() { return openedAt; }
    public Instant getResolvedAt() { return resolvedAt; }

    // ========== SETTERS ==========
    public void setId(UUID id) { this.id = id; }
    public void setTransactionId(UUID transactionId) { this.transactionId = transactionId; }
    public void setOpenedBy(UUID openedBy) { this.openedBy = openedBy; }
    public void setReasonCode(String reasonCode) { this.reasonCode = reasonCode; }
    public void setReasonDetails(String reasonDetails) { this.reasonDetails = reasonDetails; }
    public void setStatus(DisputeStatus status) { this.status = status; }
    public void setDecisionCode(String decisionCode) { this.decisionCode = decisionCode; }
    public void setDecisionComment(String decisionComment) { this.decisionComment = decisionComment; }
    public void setResolvedBy(UUID resolvedBy) { this.resolvedBy = resolvedBy; }
    public void setOpenedAt(Instant openedAt) { this.openedAt = openedAt; }
    public void setResolvedAt(Instant resolvedAt) { this.resolvedAt = resolvedAt; }

    // ========== BUILDER ==========
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private UUID transactionId;
        private UUID openedBy;
        private String reasonCode;
        private String reasonDetails;
        private DisputeStatus status;
        private String decisionCode;
        private String decisionComment;
        private UUID resolvedBy;
        private Instant openedAt;
        private Instant resolvedAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder transactionId(UUID transactionId) { this.transactionId = transactionId; return this; }
        public Builder openedBy(UUID openedBy) { this.openedBy = openedBy; return this; }
        public Builder reasonCode(String reasonCode) { this.reasonCode = reasonCode; return this; }
        public Builder reasonDetails(String reasonDetails) { this.reasonDetails = reasonDetails; return this; }
        public Builder status(DisputeStatus status) { this.status = status; return this; }
        public Builder decisionCode(String decisionCode) { this.decisionCode = decisionCode; return this; }
        public Builder decisionComment(String decisionComment) { this.decisionComment = decisionComment; return this; }
        public Builder resolvedBy(UUID resolvedBy) { this.resolvedBy = resolvedBy; return this; }
        public Builder openedAt(Instant openedAt) { this.openedAt = openedAt; return this; }
        public Builder resolvedAt(Instant resolvedAt) { this.resolvedAt = resolvedAt; return this; }

        public Dispute build() {
            return new Dispute(this);
        }
    }
}
