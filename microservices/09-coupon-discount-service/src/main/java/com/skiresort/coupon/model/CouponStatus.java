package com.skiresort.coupon.model;

/**
 * クーポンステータス列挙型
 */
public enum CouponStatus {
    ACTIVE("有効", "使用可能な状態"),
    INACTIVE("無効", "使用不可の状態"),
    EXPIRED("期限切れ", "有効期限が過ぎた状態"),
    EXHAUSTED("使用済み", "使用回数制限に達した状態"),
    SUSPENDED("停止", "一時的に停止された状態");
    
    private final String displayName;
    private final String description;
    
    CouponStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 使用可能なステータスかチェック
     */
    public boolean isUsable() {
        return this == ACTIVE;
    }
}
