package com.skiresort.order.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 注文明細エンティティ
 */
@Entity
@Table(name = "order_items")
@NamedQueries({
    @NamedQuery(
        name = "OrderItem.findByOrder",
        query = "SELECT oi FROM OrderItem oi WHERE oi.order = :order"
    ),
    @NamedQuery(
        name = "OrderItem.findByProductId",
        query = "SELECT oi FROM OrderItem oi WHERE oi.productId = :productId"
    ),
    @NamedQuery(
        name = "OrderItem.findBySku",
        query = "SELECT oi FROM OrderItem oi WHERE oi.sku = :sku"
    )
})
public class OrderItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @NotNull
    @Column(name = "product_id", nullable = false)
    private UUID productId;
    
    @NotBlank
    @Size(max = 100)
    @Column(name = "sku", nullable = false, length = 100)
    private String sku;
    
    @NotBlank
    @Size(max = 200)
    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;
    
    @NotNull
    @DecimalMin(value = "0.0")
    @Digits(integer = 8, fraction = 2)
    @Column(name = "unit_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal unitPrice;
    
    @NotNull
    @Min(1)
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    
    @DecimalMin(value = "0.0")
    @Digits(integer = 8, fraction = 2)
    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;
    
    @DecimalMin(value = "0.0")
    @Digits(integer = 8, fraction = 2)
    @Column(name = "tax_amount", precision = 10, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;
    
    @NotNull
    @DecimalMin(value = "0.0")
    @Digits(integer = 10, fraction = 2)
    @Column(name = "total_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal totalAmount;
    
    @Column(name = "reservation_id")
    private UUID reservationId;
    
    @Size(max = 500)
    @Column(name = "notes", length = 500)
    private String notes;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // コンストラクタ
    public OrderItem() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public OrderItem(UUID productId, String sku, String productName, 
                    BigDecimal unitPrice, Integer quantity) {
        this();
        this.productId = productId;
        this.sku = sku;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        calculateTotalAmount();
    }
    
    // ビジネスロジック
    public BigDecimal calculateSubtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
    
    public BigDecimal calculateFinalAmount() {
        return calculateSubtotal()
            .subtract(discountAmount != null ? discountAmount : BigDecimal.ZERO)
            .add(taxAmount != null ? taxAmount : BigDecimal.ZERO);
    }
    
    public void calculateTotalAmount() {
        this.totalAmount = calculateFinalAmount();
        this.updatedAt = LocalDateTime.now();
    }
    
    public void updateQuantity(Integer newQuantity) {
        if (newQuantity == null || newQuantity < 1) {
            throw new IllegalArgumentException("数量は1以上である必要があります");
        }
        this.quantity = newQuantity;
        calculateTotalAmount();
    }
    
    public void updateUnitPrice(BigDecimal newUnitPrice) {
        if (newUnitPrice == null || newUnitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("単価は0以上である必要があります");
        }
        this.unitPrice = newUnitPrice;
        calculateTotalAmount();
    }
    
    public void applyDiscount(BigDecimal discount) {
        if (discount != null && discount.compareTo(BigDecimal.ZERO) >= 0) {
            this.discountAmount = discount;
            calculateTotalAmount();
        }
    }
    
    public void calculateTax(BigDecimal taxRate) {
        if (taxRate != null && taxRate.compareTo(BigDecimal.ZERO) >= 0) {
            BigDecimal taxableAmount = calculateSubtotal().subtract(
                discountAmount != null ? discountAmount : BigDecimal.ZERO
            );
            this.taxAmount = taxableAmount.multiply(taxRate);
            calculateTotalAmount();
        }
    }
    
    // ライフサイクルコールバック
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
        calculateTotalAmount();
    }
    
    @PrePersist
    public void prePersist() {
        calculateTotalAmount();
    }
    
    // Getter and Setter methods
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public Order getOrder() {
        return order;
    }
    
    public void setOrder(Order order) {
        this.order = order;
    }
    
    public UUID getProductId() {
        return productId;
    }
    
    public void setProductId(UUID productId) {
        this.productId = productId;
    }
    
    public String getSku() {
        return sku;
    }
    
    public void setSku(String sku) {
        this.sku = sku;
    }
    
    public String getProductName() {
        return productName;
    }
    
    public void setProductName(String productName) {
        this.productName = productName;
    }
    
    public BigDecimal getUnitPrice() {
        return unitPrice;
    }
    
    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
        calculateTotalAmount();
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
        calculateTotalAmount();
    }
    
    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }
    
    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
        calculateTotalAmount();
    }
    
    public BigDecimal getTaxAmount() {
        return taxAmount;
    }
    
    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
        calculateTotalAmount();
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public UUID getReservationId() {
        return reservationId;
    }
    
    public void setReservationId(UUID reservationId) {
        this.reservationId = reservationId;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
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
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderItem orderItem)) return false;
        return id != null && id.equals(orderItem.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
    
    @Override
    public String toString() {
        return "OrderItem{" +
                "id=" + id +
                ", productId=" + productId +
                ", sku='" + sku + '\'' +
                ", productName='" + productName + '\'' +
                ", unitPrice=" + unitPrice +
                ", quantity=" + quantity +
                ", totalAmount=" + totalAmount +
                '}';
    }
}
