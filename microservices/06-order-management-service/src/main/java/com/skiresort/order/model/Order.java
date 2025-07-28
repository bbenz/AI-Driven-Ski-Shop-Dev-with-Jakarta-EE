package com.skiresort.order.model;

import com.skiresort.order.exception.InvalidOrderStateException;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 注文エンティティ
 */
@Entity
@Table(name = "orders")
@NamedQueries({
    @NamedQuery(
        name = "Order.findByOrderNumber",
        query = "SELECT o FROM Order o WHERE o.orderNumber = :orderNumber"
    ),
    @NamedQuery(
        name = "Order.findByCustomerId",
        query = "SELECT o FROM Order o WHERE o.customerId = :customerId ORDER BY o.createdAt DESC"
    ),
    @NamedQuery(
        name = "Order.findByStatus",
        query = "SELECT o FROM Order o WHERE o.status = :status ORDER BY o.createdAt DESC"
    ),
    @NamedQuery(
        name = "Order.findByDateRange",
        query = "SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate ORDER BY o.createdAt DESC"
    )
})
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;
    
    @NotBlank
    @Size(max = 50)
    @Column(name = "order_number", unique = true, nullable = false, length = 50)
    private String orderNumber;
    
    @NotNull
    @Column(name = "customer_id", nullable = false)
    private UUID customerId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status = OrderStatus.PENDING;
    
    @Embedded
    private OrderAmount orderAmount;
    
    @Embedded
    private ShippingAddress shippingAddress;
    
    @Size(max = 1000)
    @Column(name = "notes", length = 1000)
    private String notes;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;
    
    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;
    
    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;
    
    @Column(name = "paid_at")
    private LocalDateTime paidAt;
    
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", length = 20)
    private PaymentStatus paymentStatus;
    
    @Size(max = 500)
    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;
    
    // 関連エンティティ
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> orderItems = new ArrayList<>();
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderStatusHistory> statusHistory = new ArrayList<>();
    
    // コンストラクタ
    public Order() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.orderAmount = OrderAmount.zero();
    }
    
    public Order(String orderNumber, UUID customerId, ShippingAddress shippingAddress) {
        this();
        this.orderNumber = orderNumber;
        this.customerId = customerId;
        this.shippingAddress = shippingAddress;
    }
    
    // ビジネスロジック
    public boolean canBeCancelled() {
        return status == OrderStatus.PENDING || status == OrderStatus.CONFIRMED;
    }
    
    public boolean canBeModified() {
        return status == OrderStatus.PENDING;
    }
    
    public boolean isCompleted() {
        return status == OrderStatus.DELIVERED;
    }
    
    public boolean isTerminalState() {
        return status.isTerminalState();
    }
    
    public void confirm() {
        if (status != OrderStatus.PENDING) {
            throw InvalidOrderStateException.cannotTransition(status, OrderStatus.CONFIRMED);
        }
        changeStatus(OrderStatus.CONFIRMED);
        this.confirmedAt = LocalDateTime.now();
    }
    
    public void cancel(String reason) {
        if (!canBeCancelled()) {
            throw InvalidOrderStateException.cannotCancel(status);
        }
        changeStatus(OrderStatus.CANCELLED);
        this.cancelledAt = LocalDateTime.now();
        this.cancellationReason = reason;
    }
    
    public void changeStatus(OrderStatus newStatus) {
        if (!status.canTransitionTo(newStatus)) {
            throw InvalidOrderStateException.cannotTransition(status, newStatus);
        }
        
        OrderStatus oldStatus = this.status;
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
        
        // ステータス履歴を追加
        addStatusHistory(oldStatus, newStatus, null, null);
    }
    
    public void addStatusHistory(OrderStatus fromStatus, OrderStatus toStatus, String reason, String changedBy) {
        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrder(this);
        history.setPreviousStatus(fromStatus);
        history.setNewStatus(toStatus);
        history.setReason(reason);
        history.setChangedBy(changedBy != null ? changedBy : "SYSTEM");
        history.setCreatedAt(LocalDateTime.now());
        
        this.statusHistory.add(history);
    }
    
    public void addOrderItem(OrderItem item) {
        item.setOrder(this);
        this.orderItems.add(item);
        recalculateAmount();
    }
    
    public void removeOrderItem(OrderItem item) {
        this.orderItems.remove(item);
        item.setOrder(null);
        recalculateAmount();
    }
    
    public void recalculateAmount() {
        BigDecimal subtotal = orderItems.stream()
            .map(OrderItem::calculateSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        BigDecimal discountAmount = orderItems.stream()
            .map(item -> item.getDiscountAmount() != null ? item.getDiscountAmount() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        BigDecimal taxAmount = orderItems.stream()
            .map(item -> item.getTaxAmount() != null ? item.getTaxAmount() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        // 配送料は別途計算（ここでは0とする）
        BigDecimal shippingAmount = BigDecimal.ZERO;
        
        this.orderAmount = OrderAmount.create(subtotal, discountAmount, taxAmount, shippingAmount);
    }
    
    public BigDecimal getTotalAmount() {
        return orderAmount != null ? orderAmount.totalAmount() : BigDecimal.ZERO;
    }
    
    public BigDecimal getSubtotalAmount() {
        return orderAmount != null ? orderAmount.subtotalAmount() : BigDecimal.ZERO;
    }
    
    public BigDecimal getDiscountAmount() {
        return orderAmount != null ? orderAmount.discountAmount() : BigDecimal.ZERO;
    }
    
    public BigDecimal getTaxAmount() {
        return orderAmount != null ? orderAmount.taxAmount() : BigDecimal.ZERO;
    }
    
    public BigDecimal getShippingAmount() {
        return orderAmount != null ? orderAmount.shippingAmount() : BigDecimal.ZERO;
    }
    
    public void setSubtotalAmount(BigDecimal amount) {
        if (this.orderAmount == null) {
            this.orderAmount = OrderAmount.create(amount, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        } else {
            this.orderAmount = OrderAmount.create(amount, 
                this.orderAmount.discountAmount(), 
                this.orderAmount.taxAmount(), 
                this.orderAmount.shippingAmount());
        }
    }
    
    public void setDiscountAmount(BigDecimal amount) {
        if (this.orderAmount == null) {
            this.orderAmount = OrderAmount.create(BigDecimal.ZERO, amount, BigDecimal.ZERO, BigDecimal.ZERO);
        } else {
            this.orderAmount = OrderAmount.create(
                this.orderAmount.subtotalAmount(),
                amount, 
                this.orderAmount.taxAmount(), 
                this.orderAmount.shippingAmount());
        }
    }
    
    public void setTaxAmount(BigDecimal amount) {
        if (this.orderAmount == null) {
            this.orderAmount = OrderAmount.create(BigDecimal.ZERO, BigDecimal.ZERO, amount, BigDecimal.ZERO);
        } else {
            this.orderAmount = OrderAmount.create(
                this.orderAmount.subtotalAmount(),
                this.orderAmount.discountAmount(), 
                amount, 
                this.orderAmount.shippingAmount());
        }
    }
    
    public void setTotalAmount(BigDecimal amount) {
        // Note: Total amount is calculated, not set directly
        // This method is for compatibility only
    }
    
    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }
    
    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
    
    public LocalDateTime getPaidAt() {
        return paidAt;
    }
    
    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }
    
    public LocalDateTime getShippedAt() {
        return shippedAt;
    }
    
    public void setShippedAt(LocalDateTime shippedAt) {
        this.shippedAt = shippedAt;
    }
    
    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }
    
    public void setDeliveredAt(LocalDateTime deliveredAt) {
        this.deliveredAt = deliveredAt;
    }
    
    public boolean canModifyItems() {
        return status != null && status.allowsItemModification();
    }
    
    public boolean canTransitionTo(OrderStatus newStatus) {
        return status != null && status.canTransitionTo(newStatus);
    }
    
    public boolean canCancel() {
        return status != null && status.isCancellable();
    }
    
    public int getItemCount() {
        return orderItems.size();
    }
    
    public int getTotalQuantity() {
        return orderItems.stream()
            .mapToInt(OrderItem::getQuantity)
            .sum();
    }
    
    // ライフサイクルコールバック
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getter and Setter methods
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getOrderNumber() {
        return orderNumber;
    }
    
    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }
    
    public UUID getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }
    
    public OrderStatus getStatus() {
        return status;
    }
    
    public void setStatus(OrderStatus status) {
        this.status = status;
    }
    
    public OrderAmount getOrderAmount() {
        return orderAmount;
    }
    
    public void setOrderAmount(OrderAmount orderAmount) {
        this.orderAmount = orderAmount;
    }
    
    public ShippingAddress getShippingAddress() {
        return shippingAddress;
    }
    
    public void setShippingAddress(ShippingAddress shippingAddress) {
        this.shippingAddress = shippingAddress;
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
    
    public List<OrderItem> getOrderItems() {
        return orderItems;
    }
    
    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }
    
    public List<OrderStatusHistory> getStatusHistory() {
        return statusHistory;
    }
    
    public void setStatusHistory(List<OrderStatusHistory> statusHistory) {
        this.statusHistory = statusHistory;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Order order)) return false;
        return id != null && id.equals(order.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
    
    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", orderNumber='" + orderNumber + '\'' +
                ", customerId=" + customerId +
                ", status=" + status +
                ", totalAmount=" + getTotalAmount() +
                ", createdAt=" + createdAt +
                '}';
    }
}
