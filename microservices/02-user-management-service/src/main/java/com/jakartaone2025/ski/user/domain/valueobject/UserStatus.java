package com.jakartaone2025.ski.user.domain.valueobject;

/**
 * ユーザーステータスを表すEnum
 */
public enum UserStatus {
    /**
     * 認証待ち
     */
    PENDING_VERIFICATION("認証待ち"),
    
    /**
     * アクティブ
     */
    ACTIVE("アクティブ"),
    
    /**
     * 一時停止
     */
    SUSPENDED("一時停止"),
    
    /**
     * 無効化
     */
    DEACTIVATED("無効化"),
    
    /**
     * 削除
     */
    DELETED("削除");
    
    private final String displayName;
    
    UserStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * ユーザーがアクティブかどうかを判定
     */
    public boolean isActive() {
        return this == ACTIVE;
    }
    
    /**
     * ユーザーが有効かどうかを判定（削除済み以外）
     */
    public boolean isValid() {
        return this != DELETED;
    }
    
    /**
     * ログイン可能かどうかを判定
     */
    public boolean canLogin() {
        return this == ACTIVE;
    }
}
