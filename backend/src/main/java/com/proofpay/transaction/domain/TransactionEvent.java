package com.proofpay.transaction.domain;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import org.hibernate.annotations.Type;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "transaction_events")
public class TransactionEvent {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "transaction_id", nullable = false)
    private UUID transactionId;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(name = "previous_status")
    private String previousStatus;

    @Column(name = "new_status")
    private String newStatus;

    @Column(name = "actor_user_id")
    private UUID actorUserId;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> payload;

    @Column(name = "created_at")
    private Instant createdAt;

    // ========== CONSTRUCTEURS ==========
    public TransactionEvent() {}

    private TransactionEvent(Builder builder) {
        this.id = builder.id;
        this.transactionId = builder.transactionId;
        this.eventType = builder.eventType;
        this.previousStatus = builder.previousStatus;
        this.newStatus = builder.newStatus;
        this.actorUserId = builder.actorUserId;
        this.payload = builder.payload;
        this.createdAt = builder.createdAt;
    }

    // ========== GETTERS ==========
    public UUID getId() { return id; }
    public UUID getTransactionId() { return transactionId; }
    public String getEventType() { return eventType; }
    public String getPreviousStatus() { return previousStatus; }
    public String getNewStatus() { return newStatus; }
    public UUID getActorUserId() { return actorUserId; }
    public Map<String, Object> getPayload() { return payload; }
    public Instant getCreatedAt() { return createdAt; }

    // ========== SETTERS ==========
    public void setId(UUID id) { this.id = id; }
    public void setTransactionId(UUID transactionId) { this.transactionId = transactionId; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public void setPreviousStatus(String previousStatus) { this.previousStatus = previousStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }
    public void setActorUserId(UUID actorUserId) { this.actorUserId = actorUserId; }
    public void setPayload(Map<String, Object> payload) { this.payload = payload; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    // ========== BUILDER ==========
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private UUID transactionId;
        private String eventType;
        private String previousStatus;
        private String newStatus;
        private UUID actorUserId;
        private Map<String, Object> payload;
        private Instant createdAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder transactionId(UUID transactionId) { this.transactionId = transactionId; return this; }
        public Builder eventType(String eventType) { this.eventType = eventType; return this; }
        public Builder previousStatus(String previousStatus) { this.previousStatus = previousStatus; return this; }
        public Builder newStatus(String newStatus) { this.newStatus = newStatus; return this; }
        public Builder actorUserId(UUID actorUserId) { this.actorUserId = actorUserId; return this; }
        public Builder payload(Map<String, Object> payload) { this.payload = payload; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }

        public TransactionEvent build() {
            return new TransactionEvent(this);
        }
    }
}
