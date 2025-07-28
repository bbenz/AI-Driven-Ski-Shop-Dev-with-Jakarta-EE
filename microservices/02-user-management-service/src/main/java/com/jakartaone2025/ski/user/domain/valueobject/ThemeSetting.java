package com.jakartaone2025.ski.user.domain.valueobject;

import java.util.Arrays;

/**
 * テーマ設定
 */
public enum ThemeSetting {
    LIGHT("ライト"),
    DARK("ダーク"),
    AUTO("自動");
    
    private final String displayName;
    
    ThemeSetting(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * 表示名から対応するThemeSettingを取得
     * @param displayName 表示名
     * @return 対応するThemeSetting、見つからない場合はnull
     */
    public static ThemeSetting fromDisplayName(String displayName) {
        return Arrays.stream(values())
                .filter(theme -> theme.displayName.equals(displayName))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * システムの時間に基づいてテーマを自動決定する場合のロジック
     * @param hour 24時間表記の時間（0-23）
     * @return 推奨されるテーマ
     */
    public ThemeSetting resolveForHour(int hour) {
        if (this != AUTO) {
            return this;
        }
        
        // 6時から18時まではライト、それ以外はダーク
        return (hour >= 6 && hour < 18) ? LIGHT : DARK;
    }
    
    /**
     * デフォルトテーマかどうかをチェック
     * @return デフォルト（自動）テーマの場合true
     */
    public boolean isDefault() {
        return this == AUTO;
    }
    
    /**
     * ダークテーマ系かどうかをチェック
     * @return ダークテーマまたは自動（夜間）の場合true
     */
    public boolean isDarkTheme() {
        return this == DARK;
    }
    
    /**
     * ライトテーマ系かどうかをチェック
     * @return ライトテーマまたは自動（昼間）の場合true
     */
    public boolean isLightTheme() {
        return this == LIGHT;
    }
}
