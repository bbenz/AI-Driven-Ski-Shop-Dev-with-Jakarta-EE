package com.skiresort.loyalty.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ポイント取引履歴エンティティ
 */
@Entity
@Table(name = "point_transactions", indexes = {
    @Index(name = "idx_point_transaction_account", columnList = "point_account_id"),
    @Index(name = "idx_point_transaction_type", columnList = "transaction_type"),
    @Index(name = "idx_point_transaction_created", columnList = "created_at"),
    @Index(name = "idx_point_transaction_expires", columnList = "expires_at")
})
@NamedQueries({
    @NamedQuery(name = "PointTransaction.findByAccountId", 
                query = "SELECT pt FROM PointTransaction pt WHERE pt.pointAccount.id = :accountId ORDER BY pt.createdAt DESC"),
    @NamedQuery(name = "PointTransaction.findExpiring", 
                query = "SELECT pt FROM PointTransaction pt WHERE pt.expiresAt < :cutoffDate AND pt.isExpired = false"),
    @NamedQuery(name = "PointTransaction.findByType", 
                query = "SELECT pt FROM PointTransaction pt WHERE pt.transactionType = :type ORDER BY pt.createdAt DESC")
})
public class PointTransaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "point_account_id", nullable = false)
    @NotNull
    private PointAccount pointAccount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    @NotNull
    private PointTransactionType transactionType;
    
    @Column(name = "points", nullable = false)
    @NotNull
    private Long points;
    
    @Column(name = "source", nullable = false, length = 100)
    @NotBlank
    @Size(max = 100)
    private String source;
    
    @Column(name = "description", length = 500)
    @Size(max = 500)
    private String description;
    
    @Column(name = "reference_id")
    private UUID referenceId;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @Column(name = "is_expired", nullable = false)
    @NotNull
    private Boolean isExpired = false;
    
    @Column(name = "created_at", nullable = false)
    @NotNull
    private LocalDateTime createdAt;
    
    // JPA用のデフォルトコンストラクタ
    protected PointTransaction() {}
    
    // ビジネスロジック用コンストラクタ
    public PointTransaction(PointAccount pointAccount, PointTransactionType transactionType, 
                           Long points, String source, String description) {
        this.pointAccount = pointAccount;
        this.transactionType = transactionType;
        this.points = points;
        this.source = source;
        this.description = description;
        this.createdAt = LocalDateTime.now();
    }
    
    // ビジネスロジック
    public boolean isExpirable() {
        return transactionType == PointTransactionType.EARN && expiresAt != null;
    }
    
    public boolean isExpiringSoon(LocalDateTime cutoffDate) {
        return isExpirable() && !isExpired && expiresAt.isBefore(cutoffDate);
    }
    
    public void markAsExpired() {
        this.isExpired = true;
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
    
    public PointTransactionType getTransactionType() {
        return transactionType;
    }
    
    public void setTransactionType(PointTransactionType transactionType) {
        this.transactionType = transactionType;
    }
    
    public Long getPoints() {
        return points;
    }
    
    public void setPoints(Long points) {
        this.points = points;
    }
    
    public String getSource() {
        return source;
    }
    
    public void setSource(String source) {
        this.source = source;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public UUID getReferenceId() {
        return referenceId;
    }
    
    public void setReferenceId(UUID referenceId) {
        this.referenceId = referenceId;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    public Boolean getIsExpired() {
        return isExpired;
    }
    
    public void setIsExpired(Boolean isExpired) {
        this.isExpired = isExpired;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
