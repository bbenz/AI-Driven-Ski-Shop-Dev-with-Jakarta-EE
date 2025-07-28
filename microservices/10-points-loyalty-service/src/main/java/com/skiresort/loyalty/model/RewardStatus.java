package com.skiresort.loyalty.model;

/**
 * 特典ステータス列挙型
 */
public enum RewardStatus {
    GRANTED("付与済み"),
    CLAIMED("受け取り済み"),
    EXPIRED("期限切れ"),
    CANCELLED("キャンセル");
    
    private final String description;
    
    RewardStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
