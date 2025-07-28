package com.jakartaone2025.ski.user.domain.valueobject;

/**
 * 性別を表すEnum
 */
public enum Gender {
    /**
     * 男性
     */
    MALE("男性"),
    
    /**
     * 女性
     */
    FEMALE("女性"),
    
    /**
     * その他
     */
    OTHER("その他"),
    
    /**
     * 未指定
     */
    NOT_SPECIFIED("未指定");
    
    private final String displayName;
    
    Gender(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * 表示名から性別を取得
     */
    public static Gender fromDisplayName(String displayName) {
        for (Gender gender : values()) {
            if (gender.displayName.equals(displayName)) {
                return gender;
            }
        }
        throw new IllegalArgumentException("不正な性別表示名: " + displayName);
    }
}
