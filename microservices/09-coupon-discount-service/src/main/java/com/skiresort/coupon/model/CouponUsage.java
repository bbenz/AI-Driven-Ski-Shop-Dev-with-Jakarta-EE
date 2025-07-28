package com.skiresort.coupon.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * クーポン使用履歴エンティティ
 */
@Entity
@Table(name = "coupon_usage", indexes = {
    @Index(name = "idx_usage_coupon_id", columnList = "couponId"),
    @Index(name = "idx_usage_customer_id", columnList = "customerId"),
    @Index(name = "idx_usage_order_id", columnList = "orderId"),
    @Index(name = "idx_usage_used_at", columnList = "usedAt")
})
@NamedQueries({
    @NamedQuery(name = "CouponUsage.findByCouponId", 
                query = "SELECT cu FROM CouponUsage cu WHERE cu.coupon.couponId = :couponId ORDER BY cu.usedAt DESC"),
    @NamedQuery(name = "CouponUsage.findByCustomerId", 
                query = "SELECT cu FROM CouponUsage cu WHERE cu.customerId = :customerId ORDER BY cu.usedAt DESC"),
    @NamedQuery(name = "CouponUsage.countByCustomerAndCoupon", 
                query = "SELECT COUNT(cu) FROM CouponUsage cu WHERE cu.customerId = :customerId AND cu.coupon.couponId = :couponId"),
    @NamedQuery(name = "CouponUsage.findByOrderId", 
                query = "SELECT cu FROM CouponUsage cu WHERE cu.orderId = :orderId"),
    @NamedQuery(name = "CouponUsage.findUsageStatistics", 
                query = "SELECT cu.coupon.code, COUNT(cu), SUM(cu.discountAmount) FROM CouponUsage cu GROUP BY cu.coupon.code ORDER BY COUNT(cu) DESC")
})
public class CouponUsage {
    
    @Id
    @GeneratedValue
    private UUID usageId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", nullable = false)
    @NotNull
    private Coupon coupon;
    
    @Column(name = "customer_id", nullable = false, length = 50)
    @NotNull
    @Size(min = 1, max = 50)
    private String customerId;
    
    @Column(name = "order_id", nullable = false, length = 50)
    @NotNull
    @Size(min = 1, max = 50)
    private String orderId;
    
    @Column(name = "order_amount", nullable = false, precision = 10, scale = 2)
    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal orderAmount;
    
    @Column(name = "discount_amount", nullable = false, precision = 10, scale = 2)
    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal discountAmount;
    
    @Column(name = "used_at", nullable = false)
    @NotNull
    private LocalDateTime usedAt;
    
    @Column(name = "notes", length = 500)
    @Size(max = 500)
    private String notes;
    
    // Constructors
    public CouponUsage() {}
    
    public CouponUsage(Coupon coupon, String customerId, String orderId, 
                       BigDecimal orderAmount, BigDecimal discountAmount) {
        this.coupon = coupon;
        this.customerId = customerId;
        this.orderId = orderId;
        this.orderAmount = orderAmount;
        this.discountAmount = discountAmount;
        this.usedAt = LocalDateTime.now();
    }
    
    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        if (usedAt == null) {
            usedAt = LocalDateTime.now();
        }
    }
    
    // Getters and Setters
    public UUID getUsageId() { return usageId; }
    public void setUsageId(UUID usageId) { this.usageId = usageId; }
    
    public Coupon getCoupon() { return coupon; }
    public void setCoupon(Coupon coupon) { this.coupon = coupon; }
    
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    
    public BigDecimal getOrderAmount() { return orderAmount; }
    public void setOrderAmount(BigDecimal orderAmount) { this.orderAmount = orderAmount; }
    
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    
    public LocalDateTime getUsedAt() { return usedAt; }
    public void setUsedAt(LocalDateTime usedAt) { this.usedAt = usedAt; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
