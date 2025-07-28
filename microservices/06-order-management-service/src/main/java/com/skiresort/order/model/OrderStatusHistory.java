package com.skiresort.order.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 注文ステータス履歴エンティティ
 */
@Entity
@Table(name = "order_status_history")
@NamedQueries({
    @NamedQuery(
        name = "OrderStatusHistory.findByOrder",
        query = "SELECT osh FROM OrderStatusHistory osh WHERE osh.order = :order ORDER BY osh.createdAt DESC"
    ),
    @NamedQuery(
        name = "OrderStatusHistory.findByOrderAndStatus",
        query = "SELECT osh FROM OrderStatusHistory osh WHERE osh.order = :order AND osh.status = :status ORDER BY osh.createdAt DESC"
    ),
    @NamedQuery(
        name = "OrderStatusHistory.findByStatus",
        query = "SELECT osh FROM OrderStatusHistory osh WHERE osh.status = :status ORDER BY osh.createdAt DESC"
    ),
    @NamedQuery(
        name = "OrderStatusHistory.findByDateRange",
        query = "SELECT osh FROM OrderStatusHistory osh WHERE osh.createdAt BETWEEN :startDate AND :endDate ORDER BY osh.createdAt DESC"
    )
})
public class OrderStatusHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", length = 50)
    private OrderStatus previousStatus;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", length = 50, nullable = false)
    private OrderStatus newStatus;
    
    @NotBlank
    @Size(max = 100)
    @Column(name = "changed_by", nullable = false, length = 100)
    private String changedBy;
    
    @Size(max = 500)
    @Column(name = "reason", length = 500)
    private String reason;
    
    @Size(max = 1000)
    @Column(name = "notes", length = 1000)
    private String notes;
    
    @Column(name = "automatic_change")
    private Boolean automaticChange = false;
    
    @Size(max = 100)
    @Column(name = "system_reference", length = 100)
    private String systemReference;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    // コンストラクタ
    public OrderStatusHistory() {
        this.createdAt = LocalDateTime.now();
        this.automaticChange = false;
    }
    
    public OrderStatusHistory(Order order, OrderStatus previousStatus, OrderStatus newStatus, 
                             String changedBy, String reason) {
        this();
        this.order = order;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.changedBy = changedBy;
        this.reason = reason;
    }
    
    public OrderStatusHistory(Order order, OrderStatus previousStatus, OrderStatus newStatus, 
                             String systemReference, Boolean automaticChange) {
        this();
        this.order = order;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.changedBy = "SYSTEM";
        this.systemReference = systemReference;
        this.automaticChange = automaticChange;
    }
    
    // ビジネスロジック
    public boolean isStatusUpgrade() {
        if (previousStatus == null || newStatus == null) {
            return false;
        }
        
        return switch (previousStatus) {
            case PENDING -> newStatus != OrderStatus.CANCELLED;
            case CONFIRMED -> newStatus == OrderStatus.PAID ||
                             newStatus == OrderStatus.PROCESSING || 
                             newStatus == OrderStatus.SHIPPED || 
                             newStatus == OrderStatus.DELIVERED;
            case PAID -> newStatus == OrderStatus.PROCESSING || 
                        newStatus == OrderStatus.SHIPPED || 
                        newStatus == OrderStatus.DELIVERED;
            case PROCESSING -> newStatus == OrderStatus.SHIPPED || 
                              newStatus == OrderStatus.DELIVERED;
            case SHIPPED -> newStatus == OrderStatus.DELIVERED;
            case DELIVERED, CANCELLED, RETURNED -> false;
        };
    }
    
    public boolean isStatusDowngrade() {
        if (previousStatus == null || newStatus == null) {
            return false;
        }
        
        return switch (newStatus) {
            case CANCELLED -> previousStatus != OrderStatus.DELIVERED && 
                             previousStatus != OrderStatus.RETURNED;
            case RETURNED -> previousStatus == OrderStatus.DELIVERED ||
                            previousStatus == OrderStatus.SHIPPED;
            case PENDING -> previousStatus != OrderStatus.PENDING;
            case CONFIRMED, PAID, PROCESSING, SHIPPED, DELIVERED -> false;
        };
    }
    
    public boolean isValidStatusTransition() {
        if (previousStatus == null) {
            return newStatus == OrderStatus.PENDING;
        }
        
        return switch (previousStatus) {
            case PENDING -> newStatus == OrderStatus.CONFIRMED || 
                           newStatus == OrderStatus.CANCELLED;
            case CONFIRMED -> newStatus == OrderStatus.PAID ||
                             newStatus == OrderStatus.PROCESSING || 
                             newStatus == OrderStatus.CANCELLED;
            case PAID -> newStatus == OrderStatus.PROCESSING || 
                        newStatus == OrderStatus.CANCELLED;
            case PROCESSING -> newStatus == OrderStatus.SHIPPED || 
                              newStatus == OrderStatus.CANCELLED;
            case SHIPPED -> newStatus == OrderStatus.DELIVERED || 
                           newStatus == OrderStatus.RETURNED ||
                           newStatus == OrderStatus.CANCELLED;
            case DELIVERED -> newStatus == OrderStatus.RETURNED; // 配送完了後は返品のみ可能
            case CANCELLED -> false; // キャンセル後は変更不可
            case RETURNED -> false; // 返品後は変更不可
        };
    }
    
    public String getStatusTransitionDescription() {
        String previous = previousStatus != null ? previousStatus.getDisplayName() : "なし";
        String current = newStatus.getDisplayName();
        
        return String.format("%s → %s", previous, current);
    }
    
    public long getTransitionDuration() {
        if (order == null || order.getCreatedAt() == null) {
            return 0;
        }
        
        return java.time.Duration.between(order.getCreatedAt(), createdAt).toMinutes();
    }
    
    // Static factory methods
    public static OrderStatusHistory createUserChange(Order order, OrderStatus previousStatus, 
                                                    OrderStatus newStatus, String changedBy, String reason) {
        OrderStatusHistory history = new OrderStatusHistory(order, previousStatus, newStatus, changedBy, reason);
        history.setAutomaticChange(false);
        return history;
    }
    
    public static OrderStatusHistory createSystemChange(Order order, OrderStatus previousStatus, 
                                                       OrderStatus newStatus, String systemReference) {
        OrderStatusHistory history = new OrderStatusHistory(order, previousStatus, newStatus, systemReference, true);
        history.setReason("システム自動変更");
        return history;
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
    
    public OrderStatus getPreviousStatus() {
        return previousStatus;
    }
    
    public void setPreviousStatus(OrderStatus previousStatus) {
        this.previousStatus = previousStatus;
    }
    
    public OrderStatus getNewStatus() {
        return newStatus;
    }
    
    public void setNewStatus(OrderStatus newStatus) {
        this.newStatus = newStatus;
    }
    
    public String getChangedBy() {
        return changedBy;
    }
    
    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public Boolean getAutomaticChange() {
        return automaticChange;
    }
    
    public void setAutomaticChange(Boolean automaticChange) {
        this.automaticChange = automaticChange;
    }
    
    public String getSystemReference() {
        return systemReference;
    }
    
    public void setSystemReference(String systemReference) {
        this.systemReference = systemReference;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderStatusHistory that)) return false;
        return id != null && id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
    
    @Override
    public String toString() {
        return "OrderStatusHistory{" +
                "id=" + id +
                ", previousStatus=" + previousStatus +
                ", newStatus=" + newStatus +
                ", changedBy='" + changedBy + '\'' +
                ", automaticChange=" + automaticChange +
                ", createdAt=" + createdAt +
                '}';
    }
}
