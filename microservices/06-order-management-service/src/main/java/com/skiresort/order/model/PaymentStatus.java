package com.skiresort.order.model;

/**
 * 支払いステータス列挙型
 */
public enum PaymentStatus {
    PENDING("支払待ち"),
    PROCESSING("処理中"),
    COMPLETED("完了"),
    FAILED("失敗"),
    CANCELLED("キャンセル"),
    REFUNDED("返金済");
    
    private final String description;
    
    PaymentStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isCompleted() {
        return this == COMPLETED;
    }
    
    public boolean isFailed() {
        return this == FAILED || this == CANCELLED;
    }
}
