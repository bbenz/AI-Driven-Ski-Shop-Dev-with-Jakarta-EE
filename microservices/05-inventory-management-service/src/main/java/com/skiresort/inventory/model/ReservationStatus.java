package com.skiresort.inventory.model;

/**
 * 予約ステータス列挙型
 */
public enum ReservationStatus {
    ACTIVE("有効"),
    CONFIRMED("確定"),
    CANCELLED("取消"),
    EXPIRED("期限切れ");
    
    private final String description;
    
    ReservationStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
