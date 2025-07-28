package com.skiresort.loyalty.model;

/**
 * 特典タイプ列挙型
 */
public enum RewardType {
    POINTS("ポイント"),
    COUPON("クーポン"),
    FREE_SHIPPING("送料無料"),
    DISCOUNT("割引"),
    EXCLUSIVE_ACCESS("先行アクセス"),
    BIRTHDAY_BONUS("誕生日特典");
    
    private final String description;
    
    RewardType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
