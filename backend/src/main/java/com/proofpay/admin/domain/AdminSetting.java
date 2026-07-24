package com.proofpay.admin.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "admin_settings")
public class AdminSetting {

    @Id
    @Column(name = "setting_key", length = 100)
    private String settingKey;

    @Column(name = "setting_value", nullable = false)
    private String settingValue;

    @Column(length = 50)
    private String scope;

    private String description;

    @Column(name = "updated_at")
    private Instant updatedAt;

    // ========== CONSTRUCTEURS ==========
    public AdminSetting() {}

    private AdminSetting(Builder builder) {
        this.settingKey = builder.settingKey;
        this.settingValue = builder.settingValue;
        this.scope = builder.scope;
        this.description = builder.description;
        this.updatedAt = builder.updatedAt;
    }

    // ========== GETTERS ==========
    public String getSettingKey() { return settingKey; }
    public String getSettingValue() { return settingValue; }
    public String getScope() { return scope; }
    public String getDescription() { return description; }
    public Instant getUpdatedAt() { return updatedAt; }

    // ========== SETTERS ==========
    public void setSettingKey(String settingKey) { this.settingKey = settingKey; }
    public void setSettingValue(String settingValue) { this.settingValue = settingValue; }
    public void setScope(String scope) { this.scope = scope; }
    public void setDescription(String description) { this.description = description; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    // ========== BUILDER ==========
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String settingKey;
        private String settingValue;
        private String scope;
        private String description;
        private Instant updatedAt;

        public Builder settingKey(String settingKey) { this.settingKey = settingKey; return this; }
        public Builder settingValue(String settingValue) { this.settingValue = settingValue; return this; }
        public Builder scope(String scope) { this.scope = scope; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder updatedAt(Instant updatedAt) { this.updatedAt = updatedAt; return this; }

        public AdminSetting build() {
            return new AdminSetting(this);
        }
    }
}
