package com.skiresort.auth.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * OAuth2認証情報エンティティ
 * 外部プロバイダー（Google、Facebook等）の認証情報を管理
 */
@Entity
@Table(name = "oauth2_credentials", indexes = {
    @Index(name = "idx_oauth2_user_id", columnList = "user_id"),
    @Index(name = "idx_oauth2_provider", columnList = "provider"),
    @Index(name = "idx_oauth2_provider_user_id", columnList = "provider, provider_user_id", unique = true)
})
public class OAuth2Credential {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;
    
    @NotNull
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @NotBlank
    @Size(max = 50)
    @Column(name = "provider", nullable = false, length = 50)
    private String provider;
    
    @NotBlank
    @Size(max = 255)
    @Column(name = "provider_user_id", nullable = false, length = 255)
    private String providerUserId;
    
    @Size(max = 255)
    @Column(name = "email", length = 255)
    private String email;
    
    @Size(max = 255)
    @Column(name = "display_name", length = 255)
    private String displayName;
    
    @Size(max = 500)
    @Column(name = "profile_picture_url", length = 500)
    private String profilePictureUrl;
    
    @Size(max = 1000)
    @Column(name = "access_token", length = 1000)
    private String accessToken;
    
    @Size(max = 1000)
    @Column(name = "refresh_token", length = 1000)
    private String refreshToken;
    
    @Column(name = "access_token_expires_at")
    private LocalDateTime accessTokenExpiresAt;
    
    @ElementCollection
    @CollectionTable(name = "oauth2_scopes", joinColumns = @JoinColumn(name = "oauth2_credential_id"))
    @Column(name = "scope")
    private Set<String> scopes = new HashSet<>();
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OAuth2Status status = OAuth2Status.ACTIVE;
    
    @Column(name = "first_authenticated_at", nullable = false)
    private LocalDateTime firstAuthenticatedAt;
    
    @Column(name = "last_authenticated_at")
    private LocalDateTime lastAuthenticatedAt;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Version
    @Column(name = "version")
    private Long version;
    
    // Constructors
    public OAuth2Credential() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.firstAuthenticatedAt = LocalDateTime.now();
    }
    
    public OAuth2Credential(UUID userId, String provider, String providerUserId) {
        this();
        this.userId = userId;
        this.provider = provider;
        this.providerUserId = providerUserId;
    }
    
    // Lifecycle callbacks
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Business methods
    public boolean isActive() {
        return status == OAuth2Status.ACTIVE;
    }
    
    public boolean isAccessTokenExpired() {
        return accessTokenExpiresAt != null && accessTokenExpiresAt.isBefore(LocalDateTime.now());
    }
    
    public void updateTokens(String accessToken, String refreshToken, LocalDateTime expiresAt) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.accessTokenExpiresAt = expiresAt;
        this.lastAuthenticatedAt = LocalDateTime.now();
    }
    
    public void updateProfile(String email, String displayName, String profilePictureUrl) {
        this.email = email;
        this.displayName = displayName;
        this.profilePictureUrl = profilePictureUrl;
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public UUID getUserId() {
        return userId;
    }
    
    public void setUserId(UUID userId) {
        this.userId = userId;
    }
    
    public String getProvider() {
        return provider;
    }
    
    public void setProvider(String provider) {
        this.provider = provider;
    }
    
    public String getProviderUserId() {
        return providerUserId;
    }
    
    public void setProviderUserId(String providerUserId) {
        this.providerUserId = providerUserId;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }
    
    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }
    
    public String getAccessToken() {
        return accessToken;
    }
    
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    
    public String getRefreshToken() {
        return refreshToken;
    }
    
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    
    public LocalDateTime getAccessTokenExpiresAt() {
        return accessTokenExpiresAt;
    }
    
    public void setAccessTokenExpiresAt(LocalDateTime accessTokenExpiresAt) {
        this.accessTokenExpiresAt = accessTokenExpiresAt;
    }
    
    public Set<String> getScopes() {
        return scopes;
    }
    
    public void setScopes(Set<String> scopes) {
        this.scopes = scopes;
    }
    
    public OAuth2Status getStatus() {
        return status;
    }
    
    public void setStatus(OAuth2Status status) {
        this.status = status;
    }
    
    public LocalDateTime getFirstAuthenticatedAt() {
        return firstAuthenticatedAt;
    }
    
    public void setFirstAuthenticatedAt(LocalDateTime firstAuthenticatedAt) {
        this.firstAuthenticatedAt = firstAuthenticatedAt;
    }
    
    public LocalDateTime getLastAuthenticatedAt() {
        return lastAuthenticatedAt;
    }
    
    public void setLastAuthenticatedAt(LocalDateTime lastAuthenticatedAt) {
        this.lastAuthenticatedAt = lastAuthenticatedAt;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public Long getVersion() {
        return version;
    }
    
    public void setVersion(Long version) {
        this.version = version;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OAuth2Credential that)) return false;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "OAuth2Credential{" +
               "id=" + id +
               ", userId=" + userId +
               ", provider='" + provider + '\'' +
               ", providerUserId='" + providerUserId + '\'' +
               ", status=" + status +
               '}';
    }
}
