package com.proofpay.notification.domain;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import org.hibernate.annotations.Type;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "transaction_id")
    private UUID transactionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationChannel channel;

    @Column(name = "template_code", nullable = false, length = 50)
    private String templateCode;

    @Column(nullable = false)
    private String destination;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> payload;

    @Column(nullable = false)
    private String status;

    @Column(name = "provider_message_id")
    private String providerMessageId;

    @Column(name = "sent_at")
    private Instant sentAt;

    @Column(name = "created_at")
    private Instant createdAt;

    // ========== CONSTRUCTEURS ==========
    public Notification() {}

    private Notification(Builder builder) {
        this.id = builder.id;
        this.userId = builder.userId;
        this.transactionId = builder.transactionId;
        this.channel = builder.channel;
        this.templateCode = builder.templateCode;
        this.destination = builder.destination;
        this.payload = builder.payload;
        this.status = builder.status;
        this.providerMessageId = builder.providerMessageId;
        this.sentAt = builder.sentAt;
        this.createdAt = builder.createdAt;
    }

    // ========== GETTERS ==========
    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public UUID getTransactionId() { return transactionId; }
    public NotificationChannel getChannel() { return channel; }
    public String getTemplateCode() { return templateCode; }
    public String getDestination() { return destination; }
    public Map<String, Object> getPayload() { return payload; }
    public String getStatus() { return status; }
    public String getProviderMessageId() { return providerMessageId; }
    public Instant getSentAt() { return sentAt; }
    public Instant getCreatedAt() { return createdAt; }

    // ========== SETTERS ==========
    public void setId(UUID id) { this.id = id; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public void setTransactionId(UUID transactionId) { this.transactionId = transactionId; }
    public void setChannel(NotificationChannel channel) { this.channel = channel; }
    public void setTemplateCode(String templateCode) { this.templateCode = templateCode; }
    public void setDestination(String destination) { this.destination = destination; }
    public void setPayload(Map<String, Object> payload) { this.payload = payload; }
    public void setStatus(String status) { this.status = status; }
    public void setProviderMessageId(String providerMessageId) { this.providerMessageId = providerMessageId; }
    public void setSentAt(Instant sentAt) { this.sentAt = sentAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    // ========== BUILDER ==========
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private UUID userId;
        private UUID transactionId;
        private NotificationChannel channel;
        private String templateCode;
        private String destination;
        private Map<String, Object> payload;
        private String status;
        private String providerMessageId;
        private Instant sentAt;
        private Instant createdAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder userId(UUID userId) { this.userId = userId; return this; }
        public Builder transactionId(UUID transactionId) { this.transactionId = transactionId; return this; }
        public Builder channel(NotificationChannel channel) { this.channel = channel; return this; }
        public Builder templateCode(String templateCode) { this.templateCode = templateCode; return this; }
        public Builder destination(String destination) { this.destination = destination; return this; }
        public Builder payload(Map<String, Object> payload) { this.payload = payload; return this; }
        public Builder status(String status) { this.status = status; return this; }
        public Builder providerMessageId(String providerMessageId) { this.providerMessageId = providerMessageId; return this; }
        public Builder sentAt(Instant sentAt) { this.sentAt = sentAt; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }

        public Notification build() {
            return new Notification(this);
        }
    }
}
