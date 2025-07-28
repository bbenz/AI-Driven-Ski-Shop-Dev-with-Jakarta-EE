package com.jakartaone2025.ski.user.domain.valueobject;

import java.util.Arrays;
import java.util.Locale;

/**
 * 言語設定
 */
public enum LanguageSetting {
    JAPANESE("日本語", Locale.JAPAN, "ja"),
    ENGLISH("English", Locale.ENGLISH, "en"),
    KOREAN("한국어", Locale.KOREA, "ko"),
    CHINESE_SIMPLIFIED("简体中文", Locale.SIMPLIFIED_CHINESE, "zh-CN"),
    CHINESE_TRADITIONAL("繁體中文", Locale.TRADITIONAL_CHINESE, "zh-TW");
    
    private final String displayName;
    private final Locale locale;
    private final String languageCode;
    
    LanguageSetting(String displayName, Locale locale, String languageCode) {
        this.displayName = displayName;
        this.locale = locale;
        this.languageCode = languageCode;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public Locale getLocale() {
        return locale;
    }
    
    public String getLanguageCode() {
        return languageCode;
    }
    
    /**
     * 表示名から対応するLanguageSettingを取得
     * @param displayName 表示名
     * @return 対応するLanguageSetting、見つからない場合はnull
     */
    public static LanguageSetting fromDisplayName(String displayName) {
        return Arrays.stream(values())
                .filter(lang -> lang.displayName.equals(displayName))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * 言語コードから対応するLanguageSettingを取得
     * @param languageCode 言語コード（例：ja, en, ko）
     * @return 対応するLanguageSetting、見つからない場合はJAPANESE
     */
    public static LanguageSetting fromLanguageCode(String languageCode) {
        return Arrays.stream(values())
                .filter(lang -> lang.languageCode.equals(languageCode))
                .findFirst()
                .orElse(JAPANESE);
    }
    
    /**
     * Localeから対応するLanguageSettingを取得
     * @param locale Locale
     * @return 対応するLanguageSetting、見つからない場合はJAPANESE
     */
    public static LanguageSetting fromLocale(Locale locale) {
        return Arrays.stream(values())
                .filter(lang -> lang.locale.equals(locale))
                .findFirst()
                .orElse(JAPANESE);
    }
    
    /**
     * デフォルト言語かどうかをチェック
     * @return デフォルト（日本語）言語の場合true
     */
    public boolean isDefault() {
        return this == JAPANESE;
    }
    
    /**
     * 右から左に書く言語かどうかをチェック
     * @return RTL言語の場合true（現在対応する言語はなし）
     */
    public boolean isRightToLeft() {
        // 現在対応する言語にRTL言語はない
        return false;
    }
    
    /**
     * アジア言語かどうかをチェック
     * @return アジア言語の場合true
     */
    public boolean isAsianLanguage() {
        return this == JAPANESE || this == KOREAN || 
               this == CHINESE_SIMPLIFIED || this == CHINESE_TRADITIONAL;
    }
    
    /**
     * 中国語系言語かどうかをチェック
     * @return 中国語系言語の場合true
     */
    public boolean isChineseLanguage() {
        return this == CHINESE_SIMPLIFIED || this == CHINESE_TRADITIONAL;
    }
}
