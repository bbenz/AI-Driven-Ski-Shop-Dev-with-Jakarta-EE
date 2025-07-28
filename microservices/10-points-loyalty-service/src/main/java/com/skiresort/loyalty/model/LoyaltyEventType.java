package com.skiresort.loyalty.model;

/**
 * ロイヤルティイベントタイプ列挙型
 */
public enum LoyaltyEventType {
    ENROLLMENT("入会"),
    RANK_UP("ランクアップ"),
    RANK_DOWN("ランクダウン"),
    SPECIAL_PROMOTION("特別プロモーション"),
    ANNIVERSARY("記念日"),
    BONUS_AWARD("ボーナス付与");
    
    private final String description;
    
    LoyaltyEventType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
