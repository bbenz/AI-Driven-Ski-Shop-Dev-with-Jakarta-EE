package com.jakartaone2025.ski.user.domain.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * 通知設定値オブジェクト
 */
@Embeddable
public record NotificationSettings(
    @Column(name = "email_notifications", nullable = false)
    Boolean emailNotifications,
    
    @Column(name = "sms_notifications", nullable = false)
    Boolean smsNotifications,
    
    @Column(name = "push_notifications", nullable = false)
    Boolean pushNotifications,
    
    @Column(name = "marketing_notifications", nullable = false)
    Boolean marketingNotifications,
    
    @Column(name = "system_notifications", nullable = false)
    Boolean systemNotifications,
    
    @Column(name = "security_notifications", nullable = false)
    Boolean securityNotifications
) {
    
    /**
     * デフォルトコンストラクタ（デフォルト値で初期化）
     */
    public NotificationSettings() {
        this(true, false, true, false, true, true);
    }
    
    /**
     * すべての通知を有効にする
     * @return すべての通知が有効な設定
     */
    public NotificationSettings enableAll() {
        return new NotificationSettings(true, true, true, true, true, true);
    }
    
    /**
     * すべての通知を無効にする
     * @return すべての通知が無効な設定
     */
    public NotificationSettings disableAll() {
        return new NotificationSettings(false, false, false, false, false, false);
    }
    
    /**
     * 必須通知のみ有効にする
     * @return 必須通知のみ有効な設定
     */
    public NotificationSettings essentialOnly() {
        return new NotificationSettings(true, false, true, false, true, true);
    }
    
    /**
     * メール通知の有効/無効を切り替え
     * @param enabled 有効にする場合true
     * @return 新しい設定
     */
    public NotificationSettings withEmailNotifications(boolean enabled) {
        return new NotificationSettings(enabled, smsNotifications, pushNotifications, 
            marketingNotifications, systemNotifications, securityNotifications);
    }
    
    /**
     * SMS通知の有効/無効を切り替え
     * @param enabled 有効にする場合true
     * @return 新しい設定
     */
    public NotificationSettings withSmsNotifications(boolean enabled) {
        return new NotificationSettings(emailNotifications, enabled, pushNotifications, 
            marketingNotifications, systemNotifications, securityNotifications);
    }
    
    /**
     * プッシュ通知の有効/無効を切り替え
     * @param enabled 有効にする場合true
     * @return 新しい設定
     */
    public NotificationSettings withPushNotifications(boolean enabled) {
        return new NotificationSettings(emailNotifications, smsNotifications, enabled, 
            marketingNotifications, systemNotifications, securityNotifications);
    }
    
    /**
     * マーケティング通知の有効/無効を切り替え
     * @param enabled 有効にする場合true
     * @return 新しい設定
     */
    public NotificationSettings withMarketingNotifications(boolean enabled) {
        return new NotificationSettings(emailNotifications, smsNotifications, pushNotifications, 
            enabled, systemNotifications, securityNotifications);
    }
    
    /**
     * システム通知の有効/無効を切り替え
     * @param enabled 有効にする場合true
     * @return 新しい設定
     */
    public NotificationSettings withSystemNotifications(boolean enabled) {
        return new NotificationSettings(emailNotifications, smsNotifications, pushNotifications, 
            marketingNotifications, enabled, securityNotifications);
    }
    
    /**
     * セキュリティ通知の有効/無効を切り替え
     * @param enabled 有効にする場合true
     * @return 新しい設定
     */
    public NotificationSettings withSecurityNotifications(boolean enabled) {
        return new NotificationSettings(emailNotifications, smsNotifications, pushNotifications, 
            marketingNotifications, systemNotifications, enabled);
    }
    
    /**
     * 少なくとも一つの通知が有効かチェック
     * @return 少なくとも一つの通知が有効な場合true
     */
    public boolean hasAnyEnabled() {
        return emailNotifications || smsNotifications || pushNotifications || 
               marketingNotifications || systemNotifications || securityNotifications;
    }
    
    /**
     * すべての通知が有効かチェック
     * @return すべての通知が有効な場合true
     */
    public boolean areAllEnabled() {
        return emailNotifications && smsNotifications && pushNotifications && 
               marketingNotifications && systemNotifications && securityNotifications;
    }
    
    /**
     * 必須通知が有効かチェック
     * @return 必須通知（システム・セキュリティ）が有効な場合true
     */
    public boolean areEssentialEnabled() {
        return systemNotifications && securityNotifications;
    }
}
