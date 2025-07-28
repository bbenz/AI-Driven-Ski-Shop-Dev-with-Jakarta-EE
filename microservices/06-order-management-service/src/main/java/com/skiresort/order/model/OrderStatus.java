package com.skiresort.order.model;

/**
 * 注文ステータス列挙型
 */
public enum OrderStatus {
    PENDING("処理中"),
    CONFIRMED("確定"),
    PAID("支払済"),
    PROCESSING("処理中"),
    SHIPPED("出荷済"),
    DELIVERED("配送完了"),
    CANCELLED("キャンセル"),
    RETURNED("返品");
    
    private final String description;
    
    OrderStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getDisplayName() {
        return description;
    }
    
    public boolean isTerminalState() {
        return this == DELIVERED || this == CANCELLED || this == RETURNED;
    }
    
    public boolean canTransitionTo(OrderStatus newStatus) {
        return switch (this) {
            case PENDING -> newStatus == CONFIRMED || newStatus == CANCELLED;
            case CONFIRMED -> newStatus == PAID || newStatus == CANCELLED;
            case PAID -> newStatus == PROCESSING || newStatus == CANCELLED;
            case PROCESSING -> newStatus == SHIPPED || newStatus == CANCELLED;
            case SHIPPED -> newStatus == DELIVERED || newStatus == RETURNED;
            case DELIVERED -> newStatus == RETURNED;
            case CANCELLED, RETURNED -> false;
        };
    }
    
    public boolean allowsItemModification() {
        return this == PENDING || this == CONFIRMED;
    }
    
    public boolean isCancellable() {
        return this != DELIVERED && this != CANCELLED && this != RETURNED;
    }
}
