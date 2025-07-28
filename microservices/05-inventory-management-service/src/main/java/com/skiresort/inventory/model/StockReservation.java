package com.skiresort.inventory.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 在庫予約エンティティ
 */
@Entity
@Table(name = "stock_reservations")
@NamedQueries({
    @NamedQuery(
        name = "StockReservation.findByOrderId",
        query = "SELECT r FROM StockReservation r WHERE r.orderId = :orderId"
    ),
    @NamedQuery(
        name = "StockReservation.findByCustomerId",
        query = "SELECT r FROM StockReservation r WHERE r.customerId = :customerId"
    ),
    @NamedQuery(
        name = "StockReservation.findExpired",
        query = "SELECT r FROM StockReservation r WHERE r.expiresAt < :currentTime AND r.status = 'ACTIVE'"
    ),
    @NamedQuery(
        name = "StockReservation.findActiveByInventoryItem",
        query = "SELECT r FROM StockReservation r WHERE r.inventoryItem = :inventoryItem AND r.status = 'ACTIVE'"
    )
})
public class StockReservation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_item_id", nullable = false)
    private InventoryItem inventoryItem;
    
    @Column(name = "order_id")
    private UUID orderId;
    
    @Column(name = "customer_id")
    private UUID customerId;
    
    @NotNull
    @Min(1)
    @Column(name = "reserved_quantity", nullable = false)
    private Integer reservedQuantity;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReservationStatus status = ReservationStatus.ACTIVE;
    
    @NotNull
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;
    
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
    
    @Size(max = 500)
    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;
    
    // コンストラクタ
    public StockReservation() {
        this.createdAt = LocalDateTime.now();
    }
    
    public StockReservation(InventoryItem inventoryItem, UUID orderId, UUID customerId, 
                           Integer reservedQuantity, LocalDateTime expiresAt) {
        this();
        this.inventoryItem = inventoryItem;
        this.orderId = orderId;
        this.customerId = customerId;
        this.reservedQuantity = reservedQuantity;
        this.expiresAt = expiresAt;
    }
    
    // ビジネスロジック
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
    
    public boolean canConfirm() {
        return status == ReservationStatus.ACTIVE && !isExpired();
    }
    
    public boolean canCancel() {
        return status == ReservationStatus.ACTIVE;
    }
    
    public void confirm() {
        if (!canConfirm()) {
            throw new IllegalStateException("予約を確定できません。ステータス: " + status + ", 期限切れ: " + isExpired());
        }
        this.status = ReservationStatus.CONFIRMED;
        this.confirmedAt = LocalDateTime.now();
    }
    
    public void cancel(String reason) {
        if (!canCancel()) {
            throw new IllegalStateException("予約をキャンセルできません。ステータス: " + status);
        }
        this.status = ReservationStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
        this.cancellationReason = reason;
    }
    
    public void expire() {
        if (status == ReservationStatus.ACTIVE) {
            this.status = ReservationStatus.EXPIRED;
        }
    }
    
    // Getter and Setter methods
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public InventoryItem getInventoryItem() {
        return inventoryItem;
    }
    
    public void setInventoryItem(InventoryItem inventoryItem) {
        this.inventoryItem = inventoryItem;
    }
    
    public UUID getOrderId() {
        return orderId;
    }
    
    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }
    
    public UUID getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }
    
    public Integer getReservedQuantity() {
        return reservedQuantity;
    }
    
    public void setReservedQuantity(Integer reservedQuantity) {
        this.reservedQuantity = reservedQuantity;
    }
    
    public ReservationStatus getStatus() {
        return status;
    }
    
    public void setStatus(ReservationStatus status) {
        this.status = status;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getConfirmedAt() {
        return confirmedAt;
    }
    
    public void setConfirmedAt(LocalDateTime confirmedAt) {
        this.confirmedAt = confirmedAt;
    }
    
    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }
    
    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }
    
    public String getCancellationReason() {
        return cancellationReason;
    }
    
    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StockReservation that)) return false;
        return id != null && id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
    
    @Override
    public String toString() {
        return "StockReservation{" +
                "id=" + id +
                ", orderId=" + orderId +
                ", customerId=" + customerId +
                ", reservedQuantity=" + reservedQuantity +
                ", status=" + status +
                ", expiresAt=" + expiresAt +
                '}';
    }
}
