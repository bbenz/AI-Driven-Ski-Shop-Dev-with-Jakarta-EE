package com.skishop.cart.entity;

import com.skishop.cart.exception.InsufficientStockException;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Entity
@Table(name = "cart_items", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"cart_id", "sku"}))
public class CartItem extends PanacheEntityBase {
    
    @Id
    @GeneratedValue
    public UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    public ShoppingCart cart;
    
    @Column(name = "product_id", nullable = false)
    public UUID productId;
    
    @Column(nullable = false, length = 100)
    public String sku;
    
    @Column(name = "product_name", nullable = false)
    public String productName;
    
    @Column(name = "product_image_url")
    public String productImageUrl;
    
    @Column(name = "unit_price", precision = 10, scale = 2, nullable = false)
    public BigDecimal unitPrice;
    
    @Column(nullable = false)
    public Integer quantity;
    
    @Column(name = "total_price", precision = 10, scale = 2, nullable = false)
    public BigDecimal totalPrice;
    
    @Column(name = "added_at", nullable = false)
    public LocalDateTime addedAt;
    
    @Column(name = "updated_at")
    public LocalDateTime updatedAt;
    
    @PrePersist
    void onCreate() {
        if (addedAt == null) {
            addedAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
        calculateTotalPrice();
    }
    
    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
        calculateTotalPrice();
    }
    
    public void calculateTotalPrice() {
        if (unitPrice != null && quantity != null) {
            totalPrice = unitPrice.multiply(new BigDecimal(quantity));
        }
    }
    
    public void updateQuantity(int newQuantity) {
        if (newQuantity <= 0) {
            throw new InsufficientStockException("Quantity must be positive: " + newQuantity);
        }
        this.quantity = newQuantity;
        calculateTotalPrice();
    }
    
    // Panache queries
    public static Optional<CartItem> findByCartAndSku(ShoppingCart cart, String sku) {
        return find("cart = ?1 and sku = ?2", cart, sku).firstResultOptional();
    }
    
    public static Optional<CartItem> findByCartIdAndSku(UUID cartId, String sku) {
        return find("cart.id = ?1 and sku = ?2", cartId, sku).firstResultOptional();
    }
    
    public static long countByCart(ShoppingCart cart) {
        return count("cart", cart);
    }
}
