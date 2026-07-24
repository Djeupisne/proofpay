package com.proofpay.attachment.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "attachments")
public class Attachment {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "owner_type", nullable = false, length = 30)
    private String ownerType;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "original_name", nullable = false)
    private String originalName;

    @Column(name = "stored_name", nullable = false)
    private String storedName;

    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes;

    @Column(name = "storage_path", nullable = false)
    private String storagePath;

    @Column(name = "uploaded_by")
    private UUID uploadedBy;

    @Column(name = "created_at")
    private Instant createdAt;

    // ========== CONSTRUCTEURS ==========
    public Attachment() {}

    private Attachment(Builder builder) {
        this.id = builder.id;
        this.ownerType = builder.ownerType;
        this.ownerId = builder.ownerId;
        this.originalName = builder.originalName;
        this.storedName = builder.storedName;
        this.mimeType = builder.mimeType;
        this.sizeBytes = builder.sizeBytes;
        this.storagePath = builder.storagePath;
        this.uploadedBy = builder.uploadedBy;
        this.createdAt = builder.createdAt;
    }

    // ========== GETTERS ==========
    public UUID getId() { return id; }
    public String getOwnerType() { return ownerType; }
    public UUID getOwnerId() { return ownerId; }
    public String getOriginalName() { return originalName; }
    public String getStoredName() { return storedName; }
    public String getMimeType() { return mimeType; }
    public Long getSizeBytes() { return sizeBytes; }
    public String getStoragePath() { return storagePath; }
    public UUID getUploadedBy() { return uploadedBy; }
    public Instant getCreatedAt() { return createdAt; }

    // ========== SETTERS ==========
    public void setId(UUID id) { this.id = id; }
    public void setOwnerType(String ownerType) { this.ownerType = ownerType; }
    public void setOwnerId(UUID ownerId) { this.ownerId = ownerId; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }
    public void setStoredName(String storedName) { this.storedName = storedName; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    public void setSizeBytes(Long sizeBytes) { this.sizeBytes = sizeBytes; }
    public void setStoragePath(String storagePath) { this.storagePath = storagePath; }
    public void setUploadedBy(UUID uploadedBy) { this.uploadedBy = uploadedBy; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    // ========== BUILDER ==========
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private String ownerType;
        private UUID ownerId;
        private String originalName;
        private String storedName;
        private String mimeType;
        private Long sizeBytes;
        private String storagePath;
        private UUID uploadedBy;
        private Instant createdAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder ownerType(String ownerType) { this.ownerType = ownerType; return this; }
        public Builder ownerId(UUID ownerId) { this.ownerId = ownerId; return this; }
        public Builder originalName(String originalName) { this.originalName = originalName; return this; }
        public Builder storedName(String storedName) { this.storedName = storedName; return this; }
        public Builder mimeType(String mimeType) { this.mimeType = mimeType; return this; }
        public Builder sizeBytes(Long sizeBytes) { this.sizeBytes = sizeBytes; return this; }
        public Builder storagePath(String storagePath) { this.storagePath = storagePath; return this; }
        public Builder uploadedBy(UUID uploadedBy) { this.uploadedBy = uploadedBy; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }

        public Attachment build() {
            return new Attachment(this);
        }
    }
}
