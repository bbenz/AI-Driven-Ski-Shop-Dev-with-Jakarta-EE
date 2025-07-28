package com.jakartaone2025.ski.user.domain.entity;

import com.jakartaone2025.ski.user.domain.valueobject.NotificationSettings;
import com.jakartaone2025.ski.user.domain.valueobject.ThemeSetting;
import com.jakartaone2025.ski.user.domain.valueobject.LanguageSetting;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Locale;
import java.util.UUID;

/**
 * ユーザー設定エンティティ
 */
@Entity
@Table(name = "user_preferences", indexes = {
    @Index(name = "idx_user_preferences_user_id", columnList = "user_id")
})
public class UserPreference {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @NotNull(message = "ユーザーは必須です")
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "language", nullable = false, length = 10)
    @NotNull(message = "言語設定は必須です")
    private LanguageSetting language = LanguageSetting.JAPANESE;
    
    @Column(name = "timezone", nullable = false, length = 50)
    @NotNull(message = "タイムゾーンは必須です")
    private String timezone = "Asia/Tokyo";
    
    @Enumerated(EnumType.STRING)
    @Column(name = "theme", nullable = false, length = 10)
    @NotNull(message = "テーマ設定は必須です")
    private ThemeSetting theme = ThemeSetting.AUTO;
    
    @Embedded
    @Valid
    private NotificationSettings notificationSettings = new NotificationSettings();
    
    @Column(name = "auto_save_drafts", nullable = false)
    private Boolean autoSaveDrafts = true;
    
    @Column(name = "show_tutorial", nullable = false)
    private Boolean showTutorial = true;
    
    @Column(name = "enable_analytics", nullable = false)
    private Boolean enableAnalytics = true;
    
    @Column(name = "marketing_emails_consent", nullable = false)
    private Boolean marketingEmailsConsent = false;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Version
    @Column(name = "version")
    private Long version;
    
    // Constructors
    protected UserPreference() {
        // JPAのため
    }
    
    public UserPreference(User user) {
        this.user = user;
        this.language = LanguageSetting.JAPANESE;
        this.timezone = "Asia/Tokyo";
        this.theme = ThemeSetting.AUTO;
        this.notificationSettings = new NotificationSettings();
        this.autoSaveDrafts = true;
        this.showTutorial = true;
        this.enableAnalytics = true;
        this.marketingEmailsConsent = false;
    }
    
    // Lifecycle callbacks
    @PrePersist
    protected void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Business methods
    public Locale getLocale() {
        return language.getLocale();
    }
    
    public ZoneId getZoneId() {
        return ZoneId.of(timezone);
    }
    
    public void updateLanguage(LanguageSetting language) {
        this.language = language;
    }
    
    public void updateTimezone(String timezone) {
        try {
            ZoneId.of(timezone); // Validate timezone
            this.timezone = timezone;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid timezone: " + timezone);
        }
    }
    
    public void updateTheme(ThemeSetting theme) {
        this.theme = theme;
    }
    
    public void enableMarketingEmails() {
        this.marketingEmailsConsent = true;
    }
    
    public void disableMarketingEmails() {
        this.marketingEmailsConsent = false;
    }
    
    public void completeTutorial() {
        this.showTutorial = false;
    }
    
    public void resetTutorial() {
        this.showTutorial = true;
    }
    
    public void enableAnalytics() {
        this.enableAnalytics = true;
    }
    
    public void disableAnalytics() {
        this.enableAnalytics = false;
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public LanguageSetting getLanguage() {
        return language;
    }
    
    public void setLanguage(LanguageSetting language) {
        this.language = language;
    }
    
    public String getTimezone() {
        return timezone;
    }
    
    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }
    
    public ThemeSetting getTheme() {
        return theme;
    }
    
    public void setTheme(ThemeSetting theme) {
        this.theme = theme;
    }
    
    public NotificationSettings getNotificationSettings() {
        return notificationSettings;
    }
    
    public void setNotificationSettings(NotificationSettings notificationSettings) {
        this.notificationSettings = notificationSettings;
    }
    
    public Boolean getAutoSaveDrafts() {
        return autoSaveDrafts;
    }
    
    public void setAutoSaveDrafts(Boolean autoSaveDrafts) {
        this.autoSaveDrafts = autoSaveDrafts;
    }
    
    public Boolean getShowTutorial() {
        return showTutorial;
    }
    
    public void setShowTutorial(Boolean showTutorial) {
        this.showTutorial = showTutorial;
    }
    
    public Boolean getEnableAnalytics() {
        return enableAnalytics;
    }
    
    public void setEnableAnalytics(Boolean enableAnalytics) {
        this.enableAnalytics = enableAnalytics;
    }
    
    public Boolean getMarketingEmailsConsent() {
        return marketingEmailsConsent;
    }
    
    public void setMarketingEmailsConsent(Boolean marketingEmailsConsent) {
        this.marketingEmailsConsent = marketingEmailsConsent;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public Long getVersion() {
        return version;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        UserPreference that = (UserPreference) obj;
        return id != null && id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
    
    @Override
    public String toString() {
        return String.format("UserPreference{id=%s, language=%s, theme=%s, timezone='%s'}", 
            id, language, theme, timezone);
    }
}
