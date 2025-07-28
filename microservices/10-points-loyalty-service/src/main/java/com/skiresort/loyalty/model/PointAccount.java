package com.skiresort.loyalty.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * ポイントアカウントエンティティ
 */
@Entity
@Table(name = "point_accounts", indexes = {
    @Index(name = "idx_point_account_customer", columnList = "customer_id", unique = true),
    @Index(name = "idx_point_account_created", columnList = "created_at")
})
@NamedQueries({
    @NamedQuery(name = "PointAccount.findByCustomerId", 
                query = "SELECT p FROM PointAccount p WHERE p.customerId = :customerId"),
    @NamedQuery(name = "PointAccount.findByTotalPointsRange", 
                query = "SELECT p FROM PointAccount p WHERE p.totalPoints BETWEEN :minPoints AND :maxPoints"),
    @NamedQuery(name = "PointAccount.countActiveAccounts", 
                query = "SELECT COUNT(p) FROM PointAccount p WHERE p.availablePoints > 0")
})
public class PointAccount {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "customer_id", unique = true, nullable = false)
    @NotNull
    private UUID customerId;
    
    @Column(name = "total_points", nullable = false)
    @NotNull
    @Min(0)
    private Long totalPoints = 0L;
    
    @Column(name = "available_points", nullable = false)
    @NotNull
    @Min(0)
    private Long availablePoints = 0L;
    
    @Column(name = "pending_points", nullable = false)
    @NotNull
    @Min(0)
    private Long pendingPoints = 0L;
    
    @Column(name = "expired_points", nullable = false)
    @NotNull
    @Min(0)
    private Long expiredPoints = 0L;
    
    @Column(name = "lifetime_earned_points", nullable = false)
    @NotNull
    @Min(0)
    private Long lifetimeEarnedPoints = 0L;
    
    @Column(name = "lifetime_spent_points", nullable = false)
    @NotNull
    @Min(0)
    private Long lifetimeSpentPoints = 0L;
    
    @Column(name = "created_at", nullable = false)
    @NotNull
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 関連エンティティ
    @OneToMany(mappedBy = "pointAccount", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PointTransaction> transactions = new ArrayList<>();
    
    @OneToOne(mappedBy = "pointAccount", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private LoyaltyStatus loyaltyStatus;
    
    // JPA用のデフォルトコンストラクタ
    protected PointAccount() {}
    
    // ビジネスロジック用コンストラクタ
    public PointAccount(UUID customerId) {
        this.customerId = customerId;
        this.createdAt = LocalDateTime.now();
    }
    
    // ビジネスロジック
    public void earnPoints(long points, String source, String description) {
        this.totalPoints += points;
        this.availablePoints += points;
        this.lifetimeEarnedPoints += points;
        this.updatedAt = LocalDateTime.now();
        
        var transaction = new PointTransaction();
        transaction.setPointAccount(this);
        transaction.setTransactionType(PointTransactionType.EARN);
        transaction.setPoints(points);
        transaction.setSource(source);
        transaction.setDescription(description);
        transaction.setCreatedAt(LocalDateTime.now());
        
        this.transactions.add(transaction);
    }
    
    public boolean spendPoints(long points, String purpose, String description) {
        if (this.availablePoints < points) {
            return false;
        }
        
        this.availablePoints -= points;
        this.lifetimeSpentPoints += points;
        this.updatedAt = LocalDateTime.now();
        
        var transaction = new PointTransaction();
        transaction.setPointAccount(this);
        transaction.setTransactionType(PointTransactionType.SPEND);
        transaction.setPoints(-points);
        transaction.setSource(purpose);
        transaction.setDescription(description);
        transaction.setCreatedAt(LocalDateTime.now());
        
        this.transactions.add(transaction);
        return true;
    }
    
    public void expirePoints(long points) {
        this.availablePoints -= points;
        this.expiredPoints += points;
        this.updatedAt = LocalDateTime.now();
        
        var transaction = new PointTransaction();
        transaction.setPointAccount(this);
        transaction.setTransactionType(PointTransactionType.EXPIRE);
        transaction.setPoints(-points);
        transaction.setSource("SYSTEM");
        transaction.setDescription("ポイント有効期限切れ");
        transaction.setCreatedAt(LocalDateTime.now());
        
        this.transactions.add(transaction);
    }
    
    public long getPointsExpiringSoon(LocalDateTime cutoffDate) {
        return transactions.stream()
            .filter(t -> t.getTransactionType() == PointTransactionType.EARN)
            .filter(t -> t.getExpiresAt() != null)
            .filter(t -> t.getExpiresAt().isBefore(cutoffDate))
            .filter(t -> !t.getIsExpired())
            .mapToLong(PointTransaction::getPoints)
            .sum();
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public UUID getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }
    
    public Long getTotalPoints() {
        return totalPoints;
    }
    
    public void setTotalPoints(Long totalPoints) {
        this.totalPoints = totalPoints;
    }
    
    public Long getAvailablePoints() {
        return availablePoints;
    }
    
    public void setAvailablePoints(Long availablePoints) {
        this.availablePoints = availablePoints;
    }
    
    public Long getPendingPoints() {
        return pendingPoints;
    }
    
    public void setPendingPoints(Long pendingPoints) {
        this.pendingPoints = pendingPoints;
    }
    
    public Long getExpiredPoints() {
        return expiredPoints;
    }
    
    public void setExpiredPoints(Long expiredPoints) {
        this.expiredPoints = expiredPoints;
    }
    
    public Long getLifetimeEarnedPoints() {
        return lifetimeEarnedPoints;
    }
    
    public void setLifetimeEarnedPoints(Long lifetimeEarnedPoints) {
        this.lifetimeEarnedPoints = lifetimeEarnedPoints;
    }
    
    public Long getLifetimeSpentPoints() {
        return lifetimeSpentPoints;
    }
    
    public void setLifetimeSpentPoints(Long lifetimeSpentPoints) {
        this.lifetimeSpentPoints = lifetimeSpentPoints;
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
    
    public List<PointTransaction> getTransactions() {
        return transactions;
    }
    
    public void setTransactions(List<PointTransaction> transactions) {
        this.transactions = transactions;
    }
    
    public LoyaltyStatus getLoyaltyStatus() {
        return loyaltyStatus;
    }
    
    public void setLoyaltyStatus(LoyaltyStatus loyaltyStatus) {
        this.loyaltyStatus = loyaltyStatus;
    }
}
