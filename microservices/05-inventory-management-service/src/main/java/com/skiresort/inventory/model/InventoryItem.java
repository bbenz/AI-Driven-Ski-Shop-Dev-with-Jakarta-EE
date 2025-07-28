package com.skiresort.inventory.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 在庫アイテムエンティティ
 */
@Entity
@Table(name = "inventory_items")
@NamedQueries({
    @NamedQuery(
        name = "InventoryItem.findBySku",
        query = "SELECT i FROM InventoryItem i WHERE i.sku = :sku"
    ),
    @NamedQuery(
        name = "InventoryItem.findByWarehouse",
        query = "SELECT i FROM InventoryItem i WHERE i.warehouseId = :warehouseId"
    ),
    @NamedQuery(
        name = "InventoryItem.findLowStock",
        query = "SELECT i FROM InventoryItem i WHERE i.availableQuantity <= i.reorderPoint"
    ),
    @NamedQuery(
        name = "InventoryItem.findOutOfStock",
        query = "SELECT i FROM InventoryItem i WHERE i.availableQuantity <= 0"
    ),
    @NamedQuery(
        name = "InventoryItem.countLowStock",
        query = "SELECT COUNT(i) FROM InventoryItem i WHERE i.availableQuantity <= i.reorderPoint"
    ),
    @NamedQuery(
        name = "InventoryItem.countOutOfStock",
        query = "SELECT COUNT(i) FROM InventoryItem i WHERE i.availableQuantity <= 0"
    )
})
public class InventoryItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;
    
    @NotNull
    @Column(name = "product_id", nullable = false)
    private UUID productId;
    
    @NotBlank
    @Size(max = 100)
    @Column(name = "sku", unique = true, nullable = false, length = 100)
    private String sku;
    
    @NotNull
    @Column(name = "warehouse_id", nullable = false)
    private UUID warehouseId;
    
    @NotNull
    @Min(0)
    @Column(name = "available_quantity", nullable = false)
    private Integer availableQuantity;
    
    @NotNull
    @Min(0)
    @Column(name = "reserved_quantity", nullable = false)
    private Integer reservedQuantity = 0;
    
    @NotNull
    @Min(0)
    @Column(name = "incoming_quantity", nullable = false)
    private Integer incomingQuantity = 0;
    
    @NotNull
    @Min(0)
    @Column(name = "minimum_stock_level", nullable = false)
    private Integer minimumStockLevel;
    
    @NotNull
    @Min(0)
    @Column(name = "maximum_stock_level", nullable = false)
    private Integer maximumStockLevel;
    
    @NotNull
    @Min(0)
    @Column(name = "reorder_point", nullable = false)
    private Integer reorderPoint;
    
    @NotNull
    @Min(1)
    @Column(name = "reorder_quantity", nullable = false)
    private Integer reorderQuantity;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InventoryStatus status = InventoryStatus.ACTIVE;
    
    @Column(name = "last_updated_at", nullable = false)
    private LocalDateTime lastUpdatedAt;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    // 関連エンティティ
    @OneToMany(mappedBy = "inventoryItem", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<StockMovement> stockMovements = new ArrayList<>();
    
    @OneToMany(mappedBy = "inventoryItem", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<StockReservation> reservations = new ArrayList<>();
    
    // コンストラクタ
    public InventoryItem() {
        this.createdAt = LocalDateTime.now();
        this.lastUpdatedAt = LocalDateTime.now();
    }
    
    public InventoryItem(UUID productId, String sku, UUID warehouseId, 
                        Integer availableQuantity, Integer minimumStockLevel, 
                        Integer maximumStockLevel, Integer reorderPoint, 
                        Integer reorderQuantity) {
        this();
        this.productId = productId;
        this.sku = sku;
        this.warehouseId = warehouseId;
        this.availableQuantity = availableQuantity;
        this.minimumStockLevel = minimumStockLevel;
        this.maximumStockLevel = maximumStockLevel;
        this.reorderPoint = reorderPoint;
        this.reorderQuantity = reorderQuantity;
    }
    
    // ビジネスロジック
    public Integer getTotalQuantity() {
        return availableQuantity + reservedQuantity;
    }
    
    public boolean isLowStock() {
        return availableQuantity <= reorderPoint;
    }
    
    public boolean isOutOfStock() {
        return availableQuantity <= 0;
    }
    
    public boolean canReserve(Integer quantity) {
        return availableQuantity >= quantity;
    }
    
    public void reserveStock(Integer quantity) {
        if (!canReserve(quantity)) {
            throw new IllegalStateException("在庫不足です。要求数量: " + quantity + ", 利用可能数量: " + availableQuantity);
        }
        this.availableQuantity -= quantity;
        this.reservedQuantity += quantity;
        this.lastUpdatedAt = LocalDateTime.now();
    }
    
    public void releaseReservation(Integer quantity) {
        if (quantity > this.reservedQuantity) {
            throw new IllegalStateException("予約数量を超えています。要求数量: " + quantity + ", 予約数量: " + reservedQuantity);
        }
        this.reservedQuantity -= quantity;
        this.availableQuantity += quantity;
        this.lastUpdatedAt = LocalDateTime.now();
    }
    
    public void confirmReservation(Integer quantity) {
        if (quantity > this.reservedQuantity) {
            throw new IllegalStateException("予約数量を超えています。要求数量: " + quantity + ", 予約数量: " + reservedQuantity);
        }
        this.reservedQuantity -= quantity;
        this.lastUpdatedAt = LocalDateTime.now();
    }
    
    public void updateQuantity(Integer newQuantity) {
        this.availableQuantity = newQuantity;
        this.lastUpdatedAt = LocalDateTime.now();
    }
    
    // ライフサイクルコールバック
    @PreUpdate
    public void preUpdate() {
        this.lastUpdatedAt = LocalDateTime.now();
    }
    
    // Getter and Setter methods
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
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
    
    public UUID getWarehouseId() {
        return warehouseId;
    }
    
    public void setWarehouseId(UUID warehouseId) {
        this.warehouseId = warehouseId;
    }
    
    public Integer getAvailableQuantity() {
        return availableQuantity;
    }
    
    public void setAvailableQuantity(Integer availableQuantity) {
        this.availableQuantity = availableQuantity;
        this.lastUpdatedAt = LocalDateTime.now();
    }
    
    public Integer getReservedQuantity() {
        return reservedQuantity;
    }
    
    public void setReservedQuantity(Integer reservedQuantity) {
        this.reservedQuantity = reservedQuantity;
        this.lastUpdatedAt = LocalDateTime.now();
    }
    
    public Integer getIncomingQuantity() {
        return incomingQuantity;
    }
    
    public void setIncomingQuantity(Integer incomingQuantity) {
        this.incomingQuantity = incomingQuantity;
        this.lastUpdatedAt = LocalDateTime.now();
    }
    
    public Integer getMinimumStockLevel() {
        return minimumStockLevel;
    }
    
    public void setMinimumStockLevel(Integer minimumStockLevel) {
        this.minimumStockLevel = minimumStockLevel;
    }
    
    public Integer getMaximumStockLevel() {
        return maximumStockLevel;
    }
    
    public void setMaximumStockLevel(Integer maximumStockLevel) {
        this.maximumStockLevel = maximumStockLevel;
    }
    
    public Integer getReorderPoint() {
        return reorderPoint;
    }
    
    public void setReorderPoint(Integer reorderPoint) {
        this.reorderPoint = reorderPoint;
    }
    
    public Integer getReorderQuantity() {
        return reorderQuantity;
    }
    
    public void setReorderQuantity(Integer reorderQuantity) {
        this.reorderQuantity = reorderQuantity;
    }
    
    public InventoryStatus getStatus() {
        return status;
    }
    
    public void setStatus(InventoryStatus status) {
        this.status = status;
    }
    
    public LocalDateTime getLastUpdatedAt() {
        return lastUpdatedAt;
    }
    
    public void setLastUpdatedAt(LocalDateTime lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public List<StockMovement> getStockMovements() {
        return stockMovements;
    }
    
    public void setStockMovements(List<StockMovement> stockMovements) {
        this.stockMovements = stockMovements;
    }
    
    public List<StockReservation> getReservations() {
        return reservations;
    }
    
    public void setReservations(List<StockReservation> reservations) {
        this.reservations = reservations;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InventoryItem that)) return false;
        return id != null && id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
    
    @Override
    public String toString() {
        return "InventoryItem{" +
                "id=" + id +
                ", sku='" + sku + '\'' +
                ", availableQuantity=" + availableQuantity +
                ", reservedQuantity=" + reservedQuantity +
                ", status=" + status +
                '}';
    }
}
