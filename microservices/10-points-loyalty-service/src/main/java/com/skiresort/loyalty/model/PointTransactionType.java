package com.skiresort.loyalty.model;

/**
 * ポイント取引タイプ列挙型
 */
public enum PointTransactionType {
    EARN("獲得"),
    SPEND("使用"),
    EXPIRE("期限切れ"),
    ADJUST("調整"),
    REFUND("返還");
    
    private final String description;
    
    PointTransactionType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
