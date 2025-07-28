package com.skiresort.coupon.model;

/**
 * 割引タイプ列挙型
 */
public enum DiscountType {
    PERCENTAGE("パーセンテージ", "注文額の指定パーセントを割引"),
    FIXED_AMOUNT("固定額", "指定金額を割引"),
    FREE_SHIPPING("送料無料", "送料を無料にする");
    
    private final String displayName;
    private final String description;
    
    DiscountType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
}
