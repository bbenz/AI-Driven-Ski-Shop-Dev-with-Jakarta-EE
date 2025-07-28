package com.skiresort.coupon.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * クーポンエンティティ
 */
@Entity
@Table(name = "coupons", indexes = {
    @Index(name = "idx_coupon_code", columnList = "code", unique = true),
    @Index(name = "idx_coupon_status", columnList = "status"),
    @Index(name = "idx_coupon_valid_period", columnList = "validFrom, validTo"),
    @Index(name = "idx_coupon_created_at", columnList = "createdAt")
})
@NamedQueries({
    @NamedQuery(name = "Coupon.findByCode", 
                query = "SELECT c FROM Coupon c WHERE c.code = :code"),
    @NamedQuery(name = "Coupon.findActiveByCode", 
                query = "SELECT c FROM Coupon c WHERE c.code = :code AND c.status = 'ACTIVE' AND :now BETWEEN c.validFrom AND c.validTo"),
    @NamedQuery(name = "Coupon.findByStatus", 
                query = "SELECT c FROM Coupon c WHERE c.status = :status ORDER BY c.createdAt DESC"),
    @NamedQuery(name = "Coupon.findExpired", 
                query = "SELECT c FROM Coupon c WHERE c.validTo < :now AND c.status = 'ACTIVE'"),
    @NamedQuery(name = "Coupon.countByTypeAndStatus", 
                query = "SELECT COUNT(c) FROM Coupon c WHERE c.discountType = :type AND c.status = :status")
})
public class Coupon {
    
    @Id
    @GeneratedValue
    private UUID couponId;
    
    @Column(name = "code", nullable = false, unique = true, length = 50)
    @NotNull
    @Size(min = 1, max = 50)
    private String code;
    
    @Column(name = "name", nullable = false, length = 100)
    @NotNull
    @Size(min = 1, max = 100)
    private String name;
    
    @Column(name = "description", length = 500)
    @Size(max = 500)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 20)
    @NotNull
    private DiscountType discountType;
    
    @Column(name = "discount_value", nullable = false, precision = 10, scale = 2)
    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal discountValue;
    
    @Column(name = "max_discount_amount", precision = 10, scale = 2)
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal maxDiscountAmount;
    
    @Column(name = "min_order_amount", precision = 10, scale = 2)
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal minOrderAmount;
    
    @Column(name = "usage_limit")
    @Min(1)
    private Integer usageLimit;
    
    @Column(name = "usage_count", nullable = false)
    @Min(0)
    private Integer usageCount = 0;
    
    @Column(name = "customer_usage_limit")
    @Min(1)
    private Integer customerUsageLimit;
    
    @Column(name = "valid_from", nullable = false)
    @NotNull
    private LocalDateTime validFrom;
    
    @Column(name = "valid_to", nullable = false)
    @NotNull
    private LocalDateTime validTo;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @NotNull
    private CouponStatus status = CouponStatus.ACTIVE;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "created_by", length = 100)
    @Size(max = 100)
    private String createdBy;
    
    @OneToMany(mappedBy = "coupon", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CouponUsage> usageHistory;
    
    // Constructors
    public Coupon() {}
    
    public Coupon(String code, String name, DiscountType discountType, 
                  BigDecimal discountValue, LocalDateTime validFrom, LocalDateTime validTo) {
        this.code = code;
        this.name = name;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Business methods
    
    /**
     * クーポンが現在有効かチェック
     */
    public boolean isValid() {
        LocalDateTime now = LocalDateTime.now();
        return status == CouponStatus.ACTIVE 
            && !now.isBefore(validFrom) 
            && !now.isAfter(validTo)
            && (usageLimit == null || usageCount < usageLimit);
    }
    
    /**
     * クーポンが使用可能かチェック
     */
    public boolean canBeUsed(BigDecimal orderAmount) {
        if (!isValid()) {
            return false;
        }
        
        if (minOrderAmount != null && orderAmount.compareTo(minOrderAmount) < 0) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 割引額を計算
     */
    public BigDecimal calculateDiscountAmount(BigDecimal orderAmount) {
        if (!canBeUsed(orderAmount)) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal discount = switch (discountType) {
            case PERCENTAGE -> orderAmount.multiply(discountValue).divide(new BigDecimal("100"));
            case FIXED_AMOUNT -> discountValue;
            case FREE_SHIPPING -> BigDecimal.ZERO; // 送料は別途処理
        };
        
        // 最大割引額の制限
        if (maxDiscountAmount != null && discount.compareTo(maxDiscountAmount) > 0) {
            discount = maxDiscountAmount;
        }
        
        return discount;
    }
    
    /**
     * クーポン使用
     */
    public void use() {
        if (!isValid()) {
            throw new IllegalStateException("Cannot use invalid coupon: " + code);
        }
        
        usageCount++;
        
        // 使用回数制限に達した場合は無効化
        if (usageLimit != null && usageCount >= usageLimit) {
            status = CouponStatus.EXHAUSTED;
        }
        
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * クーポンを無効化
     */
    public void deactivate() {
        this.status = CouponStatus.INACTIVE;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 期限切れチェック
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(validTo);
    }
    
    // Getters and Setters
    public UUID getCouponId() { return couponId; }
    public void setCouponId(UUID couponId) { this.couponId = couponId; }
    
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public DiscountType getDiscountType() { return discountType; }
    public void setDiscountType(DiscountType discountType) { this.discountType = discountType; }
    
    public BigDecimal getDiscountValue() { return discountValue; }
    public void setDiscountValue(BigDecimal discountValue) { this.discountValue = discountValue; }
    
    public BigDecimal getMaxDiscountAmount() { return maxDiscountAmount; }
    public void setMaxDiscountAmount(BigDecimal maxDiscountAmount) { this.maxDiscountAmount = maxDiscountAmount; }
    
    public BigDecimal getMinOrderAmount() { return minOrderAmount; }
    public void setMinOrderAmount(BigDecimal minOrderAmount) { this.minOrderAmount = minOrderAmount; }
    
    public Integer getUsageLimit() { return usageLimit; }
    public void setUsageLimit(Integer usageLimit) { this.usageLimit = usageLimit; }
    
    public Integer getUsageCount() { return usageCount; }
    public void setUsageCount(Integer usageCount) { this.usageCount = usageCount; }
    
    public Integer getCustomerUsageLimit() { return customerUsageLimit; }
    public void setCustomerUsageLimit(Integer customerUsageLimit) { this.customerUsageLimit = customerUsageLimit; }
    
    public LocalDateTime getValidFrom() { return validFrom; }
    public void setValidFrom(LocalDateTime validFrom) { this.validFrom = validFrom; }
    
    public LocalDateTime getValidTo() { return validTo; }
    public void setValidTo(LocalDateTime validTo) { this.validTo = validTo; }
    
    public CouponStatus getStatus() { return status; }
    public void setStatus(CouponStatus status) { this.status = status; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    
    public List<CouponUsage> getUsageHistory() { return usageHistory; }
    public void setUsageHistory(List<CouponUsage> usageHistory) { this.usageHistory = usageHistory; }
}
