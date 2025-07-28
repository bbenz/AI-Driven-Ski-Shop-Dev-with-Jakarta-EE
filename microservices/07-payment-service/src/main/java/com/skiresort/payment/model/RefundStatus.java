package com.skiresort.payment.model;

/**
 * 返金ステータス列挙型
 */
public enum RefundStatus {
    /**
     * 返金待ち
     */
    PENDING("返金待ち"),
    
    /**
     * 返金処理中
     */
    PROCESSING("返金処理中"),
    
    /**
     * 返金完了
     */
    COMPLETED("返金完了"),
    
    /**
     * 返金失敗
     */
    FAILED("返金失敗"),
    
    /**
     * 返金キャンセル
     */
    CANCELLED("返金キャンセル");
    
    private final String displayName;
    
    RefundStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * ステータス遷移可能性をチェック
     */
    public boolean canTransitionTo(RefundStatus newStatus) {
        return switch (this) {
            case PENDING -> newStatus == PROCESSING || newStatus == FAILED || newStatus == CANCELLED;
            case PROCESSING -> newStatus == COMPLETED || newStatus == FAILED;
            case COMPLETED, FAILED, CANCELLED -> false;
        };
    }
    
    /**
     * 最終ステータスかどうかを判定
     */
    public boolean isFinal() {
        return this == COMPLETED || this == FAILED || this == CANCELLED;
    }
    
    /**
     * 最終状態かどうか（旧メソッド名との互換性）
     */
    public boolean isTerminalState() {
        return isFinal();
    }
    
    /**
     * 成功状態かどうか
     */
    public boolean isSuccessful() {
        return this == COMPLETED;
    }
}
