package org.openpickles.policy.engine.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "service_registry")
public class ServiceRegistry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String version;
    private String description;

    @Column(name = "last_sync_hash")
    private String lastSyncHash;

    @Column(name = "last_sync_time")
    private LocalDateTime lastSyncTime;

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

    public String getLastSyncHash() {
        return lastSyncHash;
    }

    public void setLastSyncHash(String lastSyncHash) {
        this.lastSyncHash = lastSyncHash;
    }

    public LocalDateTime getLastSyncTime() {
        return lastSyncTime;
    }

    public void setLastSyncTime(LocalDateTime lastSyncTime) {
        this.lastSyncTime = lastSyncTime;
    }

    @Column(name = "public_key", columnDefinition = "TEXT")
    private String publicKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "registration_mode")
    private RegistrationMode registrationMode;

    @Column(name = "is_immutable")
    private boolean isImmutable;

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public RegistrationMode getRegistrationMode() {
        return registrationMode;
    }

    public void setRegistrationMode(RegistrationMode registrationMode) {
        this.registrationMode = registrationMode;
    }

    public boolean isImmutable() {
        return isImmutable;
    }

    public void setImmutable(boolean immutable) {
        isImmutable = immutable;
    }

    public enum RegistrationMode {
        CLIENT,
        MANUAL
    }

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "service_bundle_subscriptions", joinColumns = @JoinColumn(name = "service_id"), inverseJoinColumns = @JoinColumn(name = "bundle_id"))
    private java.util.Set<PolicyBundle> subscribedBundles = new java.util.HashSet<>();

    public java.util.Set<PolicyBundle> getSubscribedBundles() {
        return subscribedBundles;
    }

    public void setSubscribedBundles(java.util.Set<PolicyBundle> subscribedBundles) {
        this.subscribedBundles = subscribedBundles;
    }
}
