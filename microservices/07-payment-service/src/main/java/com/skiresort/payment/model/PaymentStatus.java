package com.skiresort.payment.model;

/**
 * 決済ステータス列挙型
 */
public enum PaymentStatus {
    PENDING("保留中"),
    AUTHORIZED("承認済み"),
    CAPTURED("確定済み"),
    CANCELLED("取消済み"),
    FAILED("失敗"),
    REFUNDED("返金済み"),
    PARTIALLY_REFUNDED("部分返金済み");
    
    private final String displayName;
    
    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * 次のステータスに遷移可能かチェック
     */
    public boolean canTransitionTo(PaymentStatus newStatus) {
        return switch (this) {
            case PENDING -> newStatus == AUTHORIZED || newStatus == CANCELLED || newStatus == FAILED;
            case AUTHORIZED -> newStatus == CAPTURED || newStatus == CANCELLED;
            case CAPTURED -> newStatus == REFUNDED || newStatus == PARTIALLY_REFUNDED;
            case CANCELLED, FAILED, REFUNDED, PARTIALLY_REFUNDED -> false;
        };
    }
    
    /**
     * 最終状態かどうか
     */
    public boolean isTerminalState() {
        return this == CANCELLED || this == FAILED || this == REFUNDED;
    }
    
    /**
     * 返金可能な状態かどうか
     */
    public boolean isRefundable() {
        return this == CAPTURED || this == PARTIALLY_REFUNDED;
    }
}
