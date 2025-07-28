package com.skishop.cart.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Entity
@Table(name = "shopping_carts")
public class ShoppingCart extends PanacheEntityBase {
    
    @Id
    @GeneratedValue
    public UUID id;
    
    @Column(name = "cart_id", unique = true, nullable = false, length = 100)
    public String cartId;
    
    @Column(name = "customer_id")
    public UUID customerId;
    
    @Column(name = "session_id")
    public String sessionId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    public CartStatus status = CartStatus.ACTIVE;
    
    @Column(nullable = false, length = 3)
    public String currency = "JPY";
    
    @Column(name = "subtotal_amount", precision = 12, scale = 2, nullable = false)
    public BigDecimal subtotalAmount = BigDecimal.ZERO;
    
    @Column(name = "tax_amount", precision = 12, scale = 2, nullable = false)
    public BigDecimal taxAmount = BigDecimal.ZERO;
    
    @Column(name = "shipping_amount", precision = 12, scale = 2, nullable = false)
    public BigDecimal shippingAmount = BigDecimal.ZERO;
    
    @Column(name = "discount_amount", precision = 12, scale = 2, nullable = false)
    public BigDecimal discountAmount = BigDecimal.ZERO;
    
    @Column(name = "total_amount", precision = 12, scale = 2, nullable = false)
    public BigDecimal totalAmount = BigDecimal.ZERO;
    
    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    public LocalDateTime updatedAt;
    
    @Column(name = "expires_at")
    public LocalDateTime expiresAt;
    
    @Column(name = "converted_at")
    public LocalDateTime convertedAt;
    
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    public List<CartItem> items = new ArrayList<>();
    
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    public List<AppliedCoupon> appliedCoupons = new ArrayList<>();
    
    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (cartId == null) {
            cartId = UUID.randomUUID().toString();
        }
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum CartStatus {
        ACTIVE,
        ABANDONED,
        CHECKED_OUT,
        CONVERTED,
        EXPIRED
    }
    
    // Panache queries
    public static Optional<ShoppingCart> findByCartId(String cartId) {
        return find("cartId", cartId).firstResultOptional();
    }
    
    public static Optional<ShoppingCart> findByCartIdAnyStatus(String cartId) {
        return find("cartId", cartId).firstResultOptional();
    }
    
    public static Optional<ShoppingCart> findByCustomerId(UUID customerId) {
        return find("customerId = ?1 and status = ?2", customerId, CartStatus.ACTIVE).firstResultOptional();
    }
    
    public static Optional<ShoppingCart> findBySessionId(String sessionId) {
        return find("sessionId = ?1 and status = ?2", sessionId, CartStatus.ACTIVE).firstResultOptional();
    }
    
    public static Optional<ShoppingCart> findByCartIdAndCustomerId(String cartId, String customerId) {
        if (customerId != null) {
            return find("cartId = ?1 and customerId = ?2", cartId, UUID.fromString(customerId)).firstResultOptional();
        } else {
            return find("cartId = ?1 and customerId is null", cartId).firstResultOptional();
        }
    }
    
    public static List<ShoppingCart> findByCustomerId(String customerId) {
        return find("customerId = ?1", UUID.fromString(customerId)).list();
    }
    
    
    public static List<ShoppingCart> findAbandonedCarts(LocalDateTime before) {
        return find("status = ?1 and updatedAt < ?2", CartStatus.ACTIVE, before).list();
    }
    
    public void calculateTotals() {
        subtotalAmount = items.stream()
            .map(item -> item.totalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Tax calculation (10% consumption tax)
        taxAmount = subtotalAmount.multiply(new BigDecimal("0.10"));
        
        // Shipping calculation (free shipping over 10,000 JPY)
        shippingAmount = subtotalAmount.compareTo(new BigDecimal("10000")) >= 0 
            ? BigDecimal.ZERO 
            : new BigDecimal("500");
        
        // Apply discounts
        discountAmount = appliedCoupons.stream()
            .map(coupon -> coupon.discountAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Calculate total
        totalAmount = subtotalAmount
            .add(taxAmount)
            .add(shippingAmount)
            .subtract(discountAmount);
    }
    
    public int getItemCount() {
        return items.stream()
            .mapToInt(item -> item.quantity)
            .sum();
    }
    
    public void addItem(CartItem item) {
        item.cart = this;
        items.add(item);
        calculateTotals();
    }
    
    public void removeItem(CartItem item) {
        items.remove(item);
        calculateTotals();
    }
    
    public void clearItems() {
        items.clear();
        calculateTotals();
    }
    
    public boolean isEmpty() {
        return items.isEmpty();
    }
}
