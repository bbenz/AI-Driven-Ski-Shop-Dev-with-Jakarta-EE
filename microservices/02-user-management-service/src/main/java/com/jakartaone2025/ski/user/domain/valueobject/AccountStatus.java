package com.jakartaone2025.ski.user.domain.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * アカウント状態を表すValue Object
 * Record型を使用してイミュータブルな値オブジェクトとして実装
 */
@Embeddable
public record AccountStatus(
    @NotNull(message = "ユーザーステータスは必須です")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    UserStatus status,
    
    @Column(name = "email_verified", nullable = false)
    boolean emailVerified,
    
    @Column(name = "phone_verified", nullable = false)
    boolean phoneVerified,
    
    @Column(name = "last_login_at")
    LocalDateTime lastLoginAt,
    
    @Column(name = "login_failure_count", nullable = false)
    int loginFailureCount,
    
    @Column(name = "lockout_until")
    LocalDateTime lockoutUntil
) {
    
    /**
     * アカウントがロックされているかどうかを判定
     */
    public boolean isLocked() {
        return lockoutUntil != null && lockoutUntil.isAfter(LocalDateTime.now());
    }
    
    /**
     * アカウントがアクティブかどうかを判定
     */
    public boolean isActive() {
        return status.isActive() && !isLocked();
    }
    
    /**
     * ログイン可能かどうかを判定
     */
    public boolean canLogin() {
        return status.canLogin() && !isLocked() && emailVerified;
    }
    
    /**
     * 認証完了しているかどうかを判定
     */
    public boolean isFullyVerified() {
        return emailVerified && phoneVerified;
    }
    
    /**
     * 最後のログインからの経過時間（分）を取得
     */
    public Long getMinutesSinceLastLogin() {
        if (lastLoginAt == null) {
            return null;
        }
        return java.time.Duration.between(lastLoginAt, LocalDateTime.now()).toMinutes();
    }
    
    /**
     * ロックアウト残り時間（分）を取得
     */
    public Long getLockoutRemainingMinutes() {
        if (lockoutUntil == null || !isLocked()) {
            return 0L;
        }
        return java.time.Duration.between(LocalDateTime.now(), lockoutUntil).toMinutes();
    }
    
    // Builder pattern methods for immutability
    
    public AccountStatus withStatus(UserStatus newStatus) {
        return new AccountStatus(newStatus, emailVerified, phoneVerified, lastLoginAt, 
                                loginFailureCount, lockoutUntil);
    }
    
    public AccountStatus withEmailVerified(boolean verified) {
        return new AccountStatus(status, verified, phoneVerified, lastLoginAt, 
                                loginFailureCount, lockoutUntil);
    }
    
    public AccountStatus withPhoneVerified(boolean verified) {
        return new AccountStatus(status, emailVerified, verified, lastLoginAt, 
                                loginFailureCount, lockoutUntil);
    }
    
    public AccountStatus withLastLoginAt(LocalDateTime loginTime) {
        return new AccountStatus(status, emailVerified, phoneVerified, loginTime, 
                                loginFailureCount, lockoutUntil);
    }
    
    public AccountStatus withLoginFailureCount(int count) {
        return new AccountStatus(status, emailVerified, phoneVerified, lastLoginAt, 
                                count, lockoutUntil);
    }
    
    public AccountStatus withLockoutUntil(LocalDateTime lockoutTime) {
        return new AccountStatus(status, emailVerified, phoneVerified, lastLoginAt, 
                                loginFailureCount, lockoutTime);
    }
    
    /**
     * デフォルトの新規ユーザー状態を作成
     */
    public static AccountStatus createNew() {
        return new AccountStatus(
            UserStatus.PENDING_VERIFICATION,
            false,
            false,
            null,
            0,
            null
        );
    }
    
    /**
     * アクティブな状態を作成
     */
    public static AccountStatus createActive() {
        return new AccountStatus(
            UserStatus.ACTIVE,
            true,
            false,
            null,
            0,
            null
        );
    }
}
