package com.skishop.cart.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "applied_coupons")
public class AppliedCoupon extends PanacheEntityBase {
    
    @Id
    @GeneratedValue
    public UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    public ShoppingCart cart;
    
    @Column(name = "coupon_code", nullable = false, length = 50)
    public String couponCode;
    
    @Column(name = "coupon_name", nullable = false)
    public String couponName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 20)
    public DiscountType discountType;
    
    @Column(name = "discount_amount", precision = 10, scale = 2, nullable = false)
    public BigDecimal discountAmount;
    
    @Column(name = "applied_at", nullable = false)
    public LocalDateTime appliedAt;
    
    public enum DiscountType {
        PERCENTAGE,
        FIXED_AMOUNT,
        FREE_SHIPPING
    }
    
    @PrePersist
    void onCreate() {
        if (appliedAt == null) {
            appliedAt = LocalDateTime.now();
        }
    }
    
    // Panache queries
    public static AppliedCoupon findByCartAndCode(ShoppingCart cart, String couponCode) {
        return find("cart = ?1 and couponCode = ?2", cart, couponCode).firstResult();
    }
}
