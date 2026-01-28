package org.openpickles.policy.engine.dto;

import java.time.LocalDateTime;

public class ServiceRegistryDTO {
    private Long id;
    private String name;
    private String version;
    private String description;
    private LocalDateTime lastSyncTime;
    private String lastSyncHash;
    private String status; // HEALTHY, DRIFTED, OFFLINE
    private long customPolicyCount;
    private long bundleCount;
    private String publicKey;
    private String registrationMode;
    private boolean isImmutable;

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getRegistrationMode() {
        return registrationMode;
    }

    public void setRegistrationMode(String registrationMode) {
        this.registrationMode = registrationMode;
    }

    public boolean isImmutable() {
        return isImmutable;
    }

    public void setImmutable(boolean immutable) {
        isImmutable = immutable;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getLastSyncTime() {
        return lastSyncTime;
    }

    public void setLastSyncTime(LocalDateTime lastSyncTime) {
        this.lastSyncTime = lastSyncTime;
    }

    public String getLastSyncHash() {
        return lastSyncHash;
    }

    public void setLastSyncHash(String lastSyncHash) {
        this.lastSyncHash = lastSyncHash;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getCustomPolicyCount() {
        return customPolicyCount;
    }

    public void setCustomPolicyCount(long customPolicyCount) {
        this.customPolicyCount = customPolicyCount;
    }

    public long getBundleCount() {
        return bundleCount;
    }

    public void setBundleCount(long bundleCount) {
        this.bundleCount = bundleCount;
    }
}
