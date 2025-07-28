package com.skiresort.auth.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/**
 * ユーザー認証情報エンティティ
 * ユーザーの認証情報（ローカル認証用）を管理
 */
@Entity
@Table(name = "user_credentials", indexes = {
    @Index(name = "idx_user_credentials_user_id", columnList = "user_id"),
    @Index(name = "idx_user_credentials_email", columnList = "email"),
    @Index(name = "idx_user_credentials_status", columnList = "status")
})
public class UserCredential {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;
    
    @NotNull
    @Column(name = "user_id", unique = true, nullable = false)
    private UUID userId;
    
    @NotBlank
    @Size(min = 3, max = 50)
    @Column(name = "username", unique = true, nullable = false, length = 50)
    private String username;
    
    @NotBlank
    @Email
    @Size(max = 255)
    @Column(name = "email", unique = true, nullable = false, length = 255)
    private String email;
    
    @NotBlank
    @Size(max = 255)
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;
    
    @Size(max = 255)
    @Column(name = "salt", length = 255)
    private String salt;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CredentialStatus status = CredentialStatus.ACTIVE;
    
    @Column(name = "failed_attempts", nullable = false)
    private Integer failedAttempts = 0;
    
    @Column(name = "last_failed_attempt")
    private LocalDateTime lastFailedAttempt;
    
    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;
    
    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;
    
    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified = false;
    
    @Column(name = "email_verification_token")
    private String emailVerificationToken;
    
    @Column(name = "email_verification_expires_at")
    private LocalDateTime emailVerificationExpiresAt;
    
    @Column(name = "password_reset_token")
    private String passwordResetToken;
    
    @Column(name = "password_reset_expires_at")
    private LocalDateTime passwordResetExpiresAt;
    
    @Column(name = "mfa_enabled", nullable = false)
    private Boolean mfaEnabled = false;
    
    @Size(max = 100)
    @Column(name = "mfa_secret", length = 100)
    private String mfaSecret;
    
    @ElementCollection
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_mfa_methods", joinColumns = @JoinColumn(name = "user_credential_id"))
    @Column(name = "mfa_method")
    private Set<MfaMethod> mfaMethods = new HashSet<>();
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Version
    @Column(name = "version")
    private Long version;
    
    // Constructors
    public UserCredential() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.mfaMethods = new HashSet<>();
    }
    
    public UserCredential(UUID userId, String email, String passwordHash) {
        this();
        this.userId = userId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.passwordChangedAt = LocalDateTime.now();
    }
    
    public UserCredential(UUID userId, String username, String email, String passwordHash) {
        this();
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.passwordChangedAt = LocalDateTime.now();
    }
    
    // Lifecycle callbacks
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Business methods
    public boolean isAccountLocked() {
        return lockedUntil != null && lockedUntil.isAfter(LocalDateTime.now());
    }
    
    public boolean isEmailVerified() {
        return emailVerified != null && emailVerified;
    }
    
    public boolean isMfaEnabled() {
        return mfaEnabled != null && mfaEnabled && !mfaMethods.isEmpty();
    }
    
    public boolean isActive() {
        return status == CredentialStatus.ACTIVE;
    }
    
    public void incrementFailedAttempts() {
        this.failedAttempts = (this.failedAttempts == null ? 0 : this.failedAttempts) + 1;
        this.lastFailedAttempt = LocalDateTime.now();
    }
    
    public void resetFailedAttempts() {
        this.failedAttempts = 0;
        this.lastFailedAttempt = null;
    }
    
    public void lockAccount(Duration lockDuration) {
        this.lockedUntil = LocalDateTime.now().plus(lockDuration);
    }
    
    public void unlockAccount() {
        this.lockedUntil = null;
        this.failedAttempts = 0;
        this.lastFailedAttempt = null;
    }
    
    public void verifyEmail() {
        this.emailVerified = true;
        this.emailVerificationToken = null;
        this.emailVerificationExpiresAt = null;
    }
    
    public void changePassword(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
        this.passwordChangedAt = LocalDateTime.now();
        this.passwordResetToken = null;
        this.passwordResetExpiresAt = null;
    }
    
    public void enableMfa(String secret, Set<MfaMethod> methods) {
        this.mfaEnabled = true;
        this.mfaSecret = secret;
        this.mfaMethods = new HashSet<>(methods);
    }
    
    public void disableMfa() {
        this.mfaEnabled = false;
        this.mfaSecret = null;
        this.mfaMethods.clear();
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
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPasswordHash() {
        return passwordHash;
    }
    
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    
    public String getSalt() {
        return salt;
    }
    
    public void setSalt(String salt) {
        this.salt = salt;
    }
    
    public CredentialStatus getStatus() {
        return status;
    }
    
    public void setStatus(CredentialStatus status) {
        this.status = status;
    }
    
    public Integer getFailedAttempts() {
        return failedAttempts;
    }
    
    public void setFailedAttempts(Integer failedAttempts) {
        this.failedAttempts = failedAttempts;
    }
    
    public LocalDateTime getLastFailedAttempt() {
        return lastFailedAttempt;
    }
    
    public void setLastFailedAttempt(LocalDateTime lastFailedAttempt) {
        this.lastFailedAttempt = lastFailedAttempt;
    }
    
    public LocalDateTime getLockedUntil() {
        return lockedUntil;
    }
    
    public void setLockedUntil(LocalDateTime lockedUntil) {
        this.lockedUntil = lockedUntil;
    }
    
    public LocalDateTime getPasswordChangedAt() {
        return passwordChangedAt;
    }
    
    public void setPasswordChangedAt(LocalDateTime passwordChangedAt) {
        this.passwordChangedAt = passwordChangedAt;
    }
    
    public Boolean getEmailVerified() {
        return emailVerified;
    }
    
    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }
    
    public String getEmailVerificationToken() {
        return emailVerificationToken;
    }
    
    public void setEmailVerificationToken(String emailVerificationToken) {
        this.emailVerificationToken = emailVerificationToken;
    }
    
    public LocalDateTime getEmailVerificationExpiresAt() {
        return emailVerificationExpiresAt;
    }
    
    public void setEmailVerificationExpiresAt(LocalDateTime emailVerificationExpiresAt) {
        this.emailVerificationExpiresAt = emailVerificationExpiresAt;
    }
    
    public String getPasswordResetToken() {
        return passwordResetToken;
    }
    
    public void setPasswordResetToken(String passwordResetToken) {
        this.passwordResetToken = passwordResetToken;
    }
    
    public LocalDateTime getPasswordResetExpiresAt() {
        return passwordResetExpiresAt;
    }
    
    public void setPasswordResetExpiresAt(LocalDateTime passwordResetExpiresAt) {
        this.passwordResetExpiresAt = passwordResetExpiresAt;
    }
    
    public Boolean getMfaEnabled() {
        return mfaEnabled;
    }
    
    public void setMfaEnabled(Boolean mfaEnabled) {
        this.mfaEnabled = mfaEnabled;
    }
    
    public String getMfaSecret() {
        return mfaSecret;
    }
    
    public void setMfaSecret(String mfaSecret) {
        this.mfaSecret = mfaSecret;
    }
    
    public Set<MfaMethod> getMfaMethods() {
        return mfaMethods;
    }
    
    public void setMfaMethods(Set<MfaMethod> mfaMethods) {
        this.mfaMethods = mfaMethods;
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
        if (!(o instanceof UserCredential that)) return false;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "UserCredential{" +
               "id=" + id +
               ", userId=" + userId +
               ", email='" + email + '\'' +
               ", status=" + status +
               ", emailVerified=" + emailVerified +
               ", mfaEnabled=" + mfaEnabled +
               '}';
    }
}
