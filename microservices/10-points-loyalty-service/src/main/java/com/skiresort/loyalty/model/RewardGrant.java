package com.skiresort.loyalty.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 特典付与エンティティ
 */
@Entity
@Table(name = "reward_grants", indexes = {
    @Index(name = "idx_reward_grant_status", columnList = "loyalty_status_id"),
    @Index(name = "idx_reward_grant_customer", columnList = "customer_id"),
    @Index(name = "idx_reward_grant_type", columnList = "reward_type"),
    @Index(name = "idx_reward_grant_status_enum", columnList = "status"),
    @Index(name = "idx_reward_grant_granted", columnList = "granted_at"),
    @Index(name = "idx_reward_grant_expires", columnList = "expires_at")
})
@NamedQueries({
    @NamedQuery(name = "RewardGrant.findByCustomerId", 
                query = "SELECT rg FROM RewardGrant rg WHERE rg.customerId = :customerId ORDER BY rg.grantedAt DESC"),
    @NamedQuery(name = "RewardGrant.findByStatus", 
                query = "SELECT rg FROM RewardGrant rg WHERE rg.status = :status ORDER BY rg.grantedAt DESC"),
    @NamedQuery(name = "RewardGrant.findExpired", 
                query = "SELECT rg FROM RewardGrant rg WHERE rg.expiresAt < :now AND rg.status = 'GRANTED'")
})
public class RewardGrant {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loyalty_status_id", nullable = false)
    @NotNull
    private LoyaltyStatus loyaltyStatus;
    
    @Column(name = "customer_id", nullable = false)
    @NotNull
    private UUID customerId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "reward_type", nullable = false)
    @NotNull
    private RewardType rewardType;
    
    @Column(name = "reward_value", precision = 12, scale = 2)
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal rewardValue;
    
    @Column(name = "reward_reference", length = 100)
    @Size(max = 100)
    private String rewardReference;
    
    @Column(name = "description", nullable = false, length = 500)
    @NotBlank
    @Size(max = 500)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @NotNull
    private RewardStatus status;
    
    @Column(name = "granted_at", nullable = false)
    @NotNull
    private LocalDateTime grantedAt;
    
    @Column(name = "claimed_at")
    private LocalDateTime claimedAt;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    // JPA用のデフォルトコンストラクタ
    protected RewardGrant() {}
    
    // ビジネスロジック用コンストラクタ
    public RewardGrant(LoyaltyStatus loyaltyStatus, UUID customerId, RewardType rewardType, 
                      String description) {
        this.loyaltyStatus = loyaltyStatus;
        this.customerId = customerId;
        this.rewardType = rewardType;
        this.description = description;
        this.status = RewardStatus.GRANTED;
        this.grantedAt = LocalDateTime.now();
    }
    
    // ビジネスロジック
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
    
    public boolean canBeClaimed() {
        return status == RewardStatus.GRANTED && !isExpired();
    }
    
    public void claim() {
        if (!canBeClaimed()) {
            throw new IllegalStateException("Reward cannot be claimed");
        }
        this.status = RewardStatus.CLAIMED;
        this.claimedAt = LocalDateTime.now();
    }
    
    public void cancel() {
        this.status = RewardStatus.CANCELLED;
    }
    
    public void expire() {
        if (status == RewardStatus.GRANTED) {
            this.status = RewardStatus.EXPIRED;
        }
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public LoyaltyStatus getLoyaltyStatus() {
        return loyaltyStatus;
    }
    
    public void setLoyaltyStatus(LoyaltyStatus loyaltyStatus) {
        this.loyaltyStatus = loyaltyStatus;
    }
    
    public UUID getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }
    
    public RewardType getRewardType() {
        return rewardType;
    }
    
    public void setRewardType(RewardType rewardType) {
        this.rewardType = rewardType;
    }
    
    public BigDecimal getRewardValue() {
        return rewardValue;
    }
    
    public void setRewardValue(BigDecimal rewardValue) {
        this.rewardValue = rewardValue;
    }
    
    public String getRewardReference() {
        return rewardReference;
    }
    
    public void setRewardReference(String rewardReference) {
        this.rewardReference = rewardReference;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public RewardStatus getStatus() {
        return status;
    }
    
    public void setStatus(RewardStatus status) {
        this.status = status;
    }
    
    public LocalDateTime getGrantedAt() {
        return grantedAt;
    }
    
    public void setGrantedAt(LocalDateTime grantedAt) {
        this.grantedAt = grantedAt;
    }
    
    public LocalDateTime getClaimedAt() {
        return claimedAt;
    }
    
    public void setClaimedAt(LocalDateTime claimedAt) {
        this.claimedAt = claimedAt;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
}
