package com.skiresort.loyalty.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * ロイヤルティステータスエンティティ
 */
@Entity
@Table(name = "loyalty_status", indexes = {
    @Index(name = "idx_loyalty_status_customer", columnList = "customer_id", unique = true),
    @Index(name = "idx_loyalty_status_rank", columnList = "current_rank"),
    @Index(name = "idx_loyalty_status_created", columnList = "created_at")
})
@NamedQueries({
    @NamedQuery(name = "LoyaltyStatus.findByCustomerId", 
                query = "SELECT ls FROM LoyaltyStatus ls WHERE ls.customerId = :customerId"),
    @NamedQuery(name = "LoyaltyStatus.findByRank", 
                query = "SELECT ls FROM LoyaltyStatus ls WHERE ls.currentRank = :rank"),
    @NamedQuery(name = "LoyaltyStatus.countByRank", 
                query = "SELECT COUNT(ls) FROM LoyaltyStatus ls WHERE ls.currentRank = :rank")
})
public class LoyaltyStatus {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "point_account_id", nullable = false)
    @NotNull
    private PointAccount pointAccount;
    
    @Column(name = "customer_id", nullable = false)
    @NotNull
    private UUID customerId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "current_rank", nullable = false)
    @NotNull
    private LoyaltyRank currentRank;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "next_rank")
    private LoyaltyRank nextRank;
    
    @Column(name = "points_to_next_rank")
    @Min(0)
    private Long pointsToNextRank = 0L;
    
    @Column(name = "rank_achieved_at")
    private LocalDateTime rankAchievedAt;
    
    @Column(name = "total_purchases", nullable = false)
    @NotNull
    @Min(0)
    private Long totalPurchases = 0L;
    
    @Column(name = "total_purchase_amount", precision = 12, scale = 2, nullable = false)
    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal totalPurchaseAmount = BigDecimal.ZERO;
    
    @Column(name = "annual_purchases", nullable = false)
    @NotNull
    @Min(0)
    private Long annualPurchases = 0L;
    
    @Column(name = "annual_purchase_amount", precision = 12, scale = 2, nullable = false)
    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal annualPurchaseAmount = BigDecimal.ZERO;
    
    @Column(name = "membership_start_date", nullable = false)
    @NotNull
    private LocalDate membershipStartDate;
    
    @Column(name = "created_at", nullable = false)
    @NotNull
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 関連エンティティ
    @OneToMany(mappedBy = "loyaltyStatus", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LoyaltyEvent> loyaltyEvents = new ArrayList<>();
    
    @OneToMany(mappedBy = "loyaltyStatus", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RewardGrant> rewardGrants = new ArrayList<>();
    
    // JPA用のデフォルトコンストラクタ
    protected LoyaltyStatus() {}
    
    // ビジネスロジック用コンストラクタ
    public LoyaltyStatus(PointAccount pointAccount, UUID customerId) {
        this.pointAccount = pointAccount;
        this.customerId = customerId;
        this.currentRank = LoyaltyRank.BRONZE;
        this.nextRank = LoyaltyRank.BRONZE.getNextRank();
        this.membershipStartDate = LocalDate.now();
        this.createdAt = LocalDateTime.now();
        this.rankAchievedAt = LocalDateTime.now();
        updatePointsToNextRank();
    }
    
    // ビジネスロジック
    public void recordPurchase(BigDecimal purchaseAmount) {
        this.totalPurchases++;
        this.totalPurchaseAmount = this.totalPurchaseAmount.add(purchaseAmount);
        this.annualPurchases++;
        this.annualPurchaseAmount = this.annualPurchaseAmount.add(purchaseAmount);
        this.updatedAt = LocalDateTime.now();
    }
    
    public boolean shouldUpgradeRank() {
        if (nextRank == null) {
            return false;
        }
        
        return switch (nextRank) {
            case SILVER -> totalPurchaseAmount.compareTo(LoyaltyRank.SILVER.getMinPurchaseAmount()) >= 0
                || totalPurchases >= LoyaltyRank.SILVER.getMinPurchaseCount();
            case GOLD -> totalPurchaseAmount.compareTo(LoyaltyRank.GOLD.getMinPurchaseAmount()) >= 0
                || totalPurchases >= LoyaltyRank.GOLD.getMinPurchaseCount();
            case PLATINUM -> totalPurchaseAmount.compareTo(LoyaltyRank.PLATINUM.getMinPurchaseAmount()) >= 0
                || totalPurchases >= LoyaltyRank.PLATINUM.getMinPurchaseCount();
            case DIAMOND -> totalPurchaseAmount.compareTo(LoyaltyRank.DIAMOND.getMinPurchaseAmount()) >= 0
                || totalPurchases >= LoyaltyRank.DIAMOND.getMinPurchaseCount();
            default -> false;
        };
    }
    
    public void upgradeRank(LoyaltyRank newRank) {
        var previousRank = this.currentRank;
        this.currentRank = newRank;
        this.nextRank = newRank.getNextRank();
        this.rankAchievedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        
        // ランクアップイベント記録
        var event = new LoyaltyEvent();
        event.setLoyaltyStatus(this);
        event.setEventType(LoyaltyEventType.RANK_UP);
        event.setFromRank(previousRank);
        event.setToRank(newRank);
        event.setCreatedAt(LocalDateTime.now());
        
        this.loyaltyEvents.add(event);
        
        updatePointsToNextRank();
    }
    
    public void resetAnnualStats() {
        this.annualPurchases = 0L;
        this.annualPurchaseAmount = BigDecimal.ZERO;
        this.updatedAt = LocalDateTime.now();
    }
    
    private void updatePointsToNextRank() {
        if (nextRank != null) {
            var currentPoints = pointAccount.getLifetimeEarnedPoints();
            var requiredPoints = nextRank.getMinPoints();
            this.pointsToNextRank = Math.max(0, requiredPoints - currentPoints);
        } else {
            this.pointsToNextRank = 0L;
        }
    }
    
    public BigDecimal getDiscountRate() {
        return currentRank.getDiscountRate();
    }
    
    public Double getPointsMultiplier() {
        return currentRank.getPointsMultiplier();
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public PointAccount getPointAccount() {
        return pointAccount;
    }
    
    public void setPointAccount(PointAccount pointAccount) {
        this.pointAccount = pointAccount;
    }
    
    public UUID getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }
    
    public LoyaltyRank getCurrentRank() {
        return currentRank;
    }
    
    public void setCurrentRank(LoyaltyRank currentRank) {
        this.currentRank = currentRank;
    }
    
    public LoyaltyRank getNextRank() {
        return nextRank;
    }
    
    public void setNextRank(LoyaltyRank nextRank) {
        this.nextRank = nextRank;
    }
    
    public Long getPointsToNextRank() {
        return pointsToNextRank;
    }
    
    public void setPointsToNextRank(Long pointsToNextRank) {
        this.pointsToNextRank = pointsToNextRank;
    }
    
    public LocalDateTime getRankAchievedAt() {
        return rankAchievedAt;
    }
    
    public void setRankAchievedAt(LocalDateTime rankAchievedAt) {
        this.rankAchievedAt = rankAchievedAt;
    }
    
    public Long getTotalPurchases() {
        return totalPurchases;
    }
    
    public void setTotalPurchases(Long totalPurchases) {
        this.totalPurchases = totalPurchases;
    }
    
    public BigDecimal getTotalPurchaseAmount() {
        return totalPurchaseAmount;
    }
    
    public void setTotalPurchaseAmount(BigDecimal totalPurchaseAmount) {
        this.totalPurchaseAmount = totalPurchaseAmount;
    }
    
    public Long getAnnualPurchases() {
        return annualPurchases;
    }
    
    public void setAnnualPurchases(Long annualPurchases) {
        this.annualPurchases = annualPurchases;
    }
    
    public BigDecimal getAnnualPurchaseAmount() {
        return annualPurchaseAmount;
    }
    
    public void setAnnualPurchaseAmount(BigDecimal annualPurchaseAmount) {
        this.annualPurchaseAmount = annualPurchaseAmount;
    }
    
    public LocalDate getMembershipStartDate() {
        return membershipStartDate;
    }
    
    public void setMembershipStartDate(LocalDate membershipStartDate) {
        this.membershipStartDate = membershipStartDate;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public List<LoyaltyEvent> getLoyaltyEvents() {
        return loyaltyEvents;
    }
    
    public void setLoyaltyEvents(List<LoyaltyEvent> loyaltyEvents) {
        this.loyaltyEvents = loyaltyEvents;
    }
    
    public List<RewardGrant> getRewardGrants() {
        return rewardGrants;
    }
    
    public void setRewardGrants(List<RewardGrant> rewardGrants) {
        this.rewardGrants = rewardGrants;
    }
}
