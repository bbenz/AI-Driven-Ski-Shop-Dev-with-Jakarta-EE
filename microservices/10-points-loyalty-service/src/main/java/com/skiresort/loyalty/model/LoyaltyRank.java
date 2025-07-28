package com.skiresort.loyalty.model;

import java.math.BigDecimal;

/**
 * ロイヤルティランク列挙型
 */
public enum LoyaltyRank {
    BRONZE("ブロンズ", 0L, BigDecimal.ZERO, 0L, 1.0, BigDecimal.ZERO),
    SILVER("シルバー", 1000L, new BigDecimal("50000"), 5L, 1.2, new BigDecimal("0.03")),
    GOLD("ゴールド", 3000L, new BigDecimal("150000"), 15L, 1.5, new BigDecimal("0.05")),
    PLATINUM("プラチナ", 6000L, new BigDecimal("300000"), 30L, 2.0, new BigDecimal("0.08")),
    DIAMOND("ダイヤモンド", 12000L, new BigDecimal("600000"), 60L, 3.0, new BigDecimal("0.10"));
    
    private final String displayName;
    private final Long minPoints;
    private final BigDecimal minPurchaseAmount;
    private final Long minPurchaseCount;
    private final Double pointsMultiplier;
    private final BigDecimal discountRate;
    
    LoyaltyRank(String displayName, Long minPoints, BigDecimal minPurchaseAmount, 
                Long minPurchaseCount, Double pointsMultiplier, BigDecimal discountRate) {
        this.displayName = displayName;
        this.minPoints = minPoints;
        this.minPurchaseAmount = minPurchaseAmount;
        this.minPurchaseCount = minPurchaseCount;
        this.pointsMultiplier = pointsMultiplier;
        this.discountRate = discountRate;
    }
    
    public String getDisplayName() { 
        return displayName; 
    }
    
    public Long getMinPoints() { 
        return minPoints; 
    }
    
    public BigDecimal getMinPurchaseAmount() { 
        return minPurchaseAmount; 
    }
    
    public Long getMinPurchaseCount() { 
        return minPurchaseCount; 
    }
    
    public Double getPointsMultiplier() { 
        return pointsMultiplier; 
    }
    
    public BigDecimal getDiscountRate() { 
        return discountRate; 
    }
    
    public LoyaltyRank getNextRank() {
        var ranks = values();
        var currentIndex = this.ordinal();
        return currentIndex < ranks.length - 1 ? ranks[currentIndex + 1] : null;
    }
    
    public boolean canUpgradeTo(LoyaltyRank targetRank) {
        return targetRank.ordinal() == this.ordinal() + 1;
    }
}
