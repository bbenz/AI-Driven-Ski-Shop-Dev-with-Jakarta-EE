package com.skiresort.order.service;

import com.skiresort.order.exception.InvalidOrderStateException;
import com.skiresort.order.exception.OrderNotFoundException;
import com.skiresort.order.model.*;
import com.skiresort.order.repository.OrderRepository;
import com.skiresort.order.repository.OrderItemRepository;
import com.skiresort.order.repository.OrderStatusHistoryRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * 注文管理サービス
 */
@ApplicationScoped
@Transactional
public class OrderService {
    
    private static final Logger LOGGER = Logger.getLogger(OrderService.class.getName());
    
    @Inject
    private OrderRepository orderRepository;
    
    @Inject
    private OrderItemRepository orderItemRepository;
    
    @Inject
    private OrderStatusHistoryRepository orderStatusHistoryRepository;
    
    @Inject
    private OrderNumberService orderNumberService;
    
    @Inject
    private Event<OrderStatusChangedEvent> orderStatusChangedEvent;
    
    /**
     * 注文を作成
     */
    public Order createOrder(@Valid @NotNull Order order) {
        // 注文番号を生成
        if (order.getOrderNumber() == null || order.getOrderNumber().trim().isEmpty()) {
            order.setOrderNumber(orderNumberService.generateOrderNumber());
        }
        
        // 初期ステータスを設定
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentStatus(PaymentStatus.PENDING);
        
        // 金額を計算
        calculateOrderAmounts(order);
        
        // 注文を保存
        Order savedOrder = orderRepository.save(order);
        
        // 初期ステータス履歴を作成
        OrderStatusHistory initialHistory = OrderStatusHistory.createSystemChange(
            savedOrder, null, OrderStatus.PENDING, "ORDER_CREATED");
        orderStatusHistoryRepository.save(initialHistory);
        
        LOGGER.info("注文が作成されました: " + savedOrder.getOrderNumber());
        
        return savedOrder;
    }
    
    /**
     * 注文明細を追加
     */
    public Order addOrderItem(@NotNull UUID orderId, @Valid @NotNull OrderItem orderItem) {
        Order order = findOrderById(orderId);
        
        if (!order.canModifyItems()) {
            throw new InvalidOrderStateException("現在のステータスでは明細を変更できません: " + order.getStatus());
        }
        
        orderItem.setOrder(order);
        orderItem.calculateTotalAmount();
        orderItemRepository.save(orderItem);
        
        order.addOrderItem(orderItem);
        calculateOrderAmounts(order);
        
        Order updatedOrder = orderRepository.save(order);
        
        LOGGER.info("注文明細を追加しました: 注文番号=" + order.getOrderNumber() + ", 商品=" + orderItem.getProductName());
        
        return updatedOrder;
    }
    
    /**
     * 注文明細を更新
     */
    public Order updateOrderItem(@NotNull UUID orderId, @NotNull UUID itemId, @Valid @NotNull OrderItem updateData) {
        Order order = findOrderById(orderId);
        
        if (!order.canModifyItems()) {
            throw new InvalidOrderStateException("現在のステータスでは明細を変更できません: " + order.getStatus());
        }
        
        OrderItem existingItem = orderItemRepository.findById(itemId)
            .orElseThrow(() -> new OrderNotFoundException("注文明細が見つかりません: " + itemId));
        
        if (!existingItem.getOrder().getId().equals(orderId)) {
            throw new InvalidOrderStateException("指定された注文に属する明細ではありません");
        }
        
        // 明細を更新
        existingItem.setQuantity(updateData.getQuantity());
        existingItem.setUnitPrice(updateData.getUnitPrice());
        existingItem.setDiscountAmount(updateData.getDiscountAmount());
        existingItem.setNotes(updateData.getNotes());
        existingItem.calculateTotalAmount();
        
        orderItemRepository.save(existingItem);
        
        // 注文合計を再計算
        calculateOrderAmounts(order);
        Order updatedOrder = orderRepository.save(order);
        
        LOGGER.info("注文明細を更新しました: 注文番号=" + order.getOrderNumber() + ", 明細ID=" + itemId);
        
        return updatedOrder;
    }
    
    /**
     * 注文明細を削除
     */
    public Order removeOrderItem(@NotNull UUID orderId, @NotNull UUID itemId) {
        Order order = findOrderById(orderId);
        
        if (!order.canModifyItems()) {
            throw new InvalidOrderStateException("現在のステータスでは明細を変更できません: " + order.getStatus());
        }
        
        OrderItem item = orderItemRepository.findById(itemId)
            .orElseThrow(() -> new OrderNotFoundException("注文明細が見つかりません: " + itemId));
        
        if (!item.getOrder().getId().equals(orderId)) {
            throw new InvalidOrderStateException("指定された注文に属する明細ではありません");
        }
        
        order.removeOrderItem(item);
        orderItemRepository.delete(item);
        
        calculateOrderAmounts(order);
        Order updatedOrder = orderRepository.save(order);
        
        LOGGER.info("注文明細を削除しました: 注文番号=" + order.getOrderNumber() + ", 明細ID=" + itemId);
        
        return updatedOrder;
    }
    
    /**
     * 注文ステータスを変更
     */
    public Order changeOrderStatus(@NotNull UUID orderId, @NotNull OrderStatus newStatus, String changedBy, String reason) {
        Order order = findOrderById(orderId);
        OrderStatus previousStatus = order.getStatus();
        
        if (!order.canTransitionTo(newStatus)) {
            throw new InvalidOrderStateException(String.format("ステータス変更が無効です: %s -> %s", previousStatus, newStatus));
        }
        
        // ステータスを更新
        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());
        
        // 特定のステータスに応じた追加処理
        switch (newStatus) {
            case CONFIRMED -> order.setConfirmedAt(LocalDateTime.now());
            case SHIPPED -> order.setShippedAt(LocalDateTime.now());
            case DELIVERED -> order.setDeliveredAt(LocalDateTime.now());
            case CANCELLED -> order.setCancelledAt(LocalDateTime.now());
        }
        
        Order updatedOrder = orderRepository.save(order);
        
        // ステータス履歴を記録
        OrderStatusHistory history = OrderStatusHistory.createUserChange(
            updatedOrder, previousStatus, newStatus, changedBy, reason);
        orderStatusHistoryRepository.save(history);
        
        // イベントを発行
        OrderStatusChangedEvent event = new OrderStatusChangedEvent(orderId, previousStatus, newStatus, changedBy);
        orderStatusChangedEvent.fire(event);
        
        LOGGER.info(String.format("注文ステータスを変更しました: 注文番号=%s, %s -> %s", 
                   order.getOrderNumber(), previousStatus, newStatus));
        
        return updatedOrder;
    }
    
    /**
     * 決済ステータスを変更
     */
    public Order changePaymentStatus(@NotNull UUID orderId, @NotNull PaymentStatus newPaymentStatus, String reason) {
        Order order = findOrderById(orderId);
        PaymentStatus previousPaymentStatus = order.getPaymentStatus();
        
        order.setPaymentStatus(newPaymentStatus);
        order.setUpdatedAt(LocalDateTime.now());
        
        if (newPaymentStatus == PaymentStatus.COMPLETED) {
            order.setPaidAt(LocalDateTime.now());
        }
        
        Order updatedOrder = orderRepository.save(order);
        
        LOGGER.info(String.format("決済ステータスを変更しました: 注文番号=%s, %s -> %s", 
                   order.getOrderNumber(), previousPaymentStatus, newPaymentStatus));
        
        return updatedOrder;
    }
    
    /**
     * 注文をキャンセル
     */
    public Order cancelOrder(@NotNull UUID orderId, String reason, String cancelledBy) {
        Order order = findOrderById(orderId);
        
        if (!order.canCancel()) {
            throw new InvalidOrderStateException("現在のステータスではキャンセルできません: " + order.getStatus());
        }
        
        return changeOrderStatus(orderId, OrderStatus.CANCELLED, cancelledBy, reason);
    }
    
    /**
     * 注文を検索（ID）
     */
    public Order findOrderById(@NotNull UUID orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException("注文が見つかりません: " + orderId));
    }
    
    /**
     * 注文を検索（注文番号）
     */
    public Order findOrderByNumber(@NotNull String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
            .orElseThrow(() -> new OrderNotFoundException("注文が見つかりません: " + orderNumber));
    }
    
    /**
     * 顧客の注文一覧を取得
     */
    public List<Order> findOrdersByCustomerId(@NotNull UUID customerId) {
        return orderRepository.findByCustomerId(customerId);
    }
    
    /**
     * 顧客の注文一覧を取得（ページング）
     */
    public List<Order> findOrdersByCustomerId(@NotNull UUID customerId, int page, int size) {
        int offset = page * size;
        return orderRepository.findByCustomerId(customerId, offset, size);
    }
    
    /**
     * ステータス別注文一覧を取得
     */
    public List<Order> findOrdersByStatus(@NotNull OrderStatus status) {
        return orderRepository.findByStatus(status);
    }
    
    /**
     * 期限切れの注文を取得
     */
    public List<Order> findExpiredOrders(int hours) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(hours);
        return orderRepository.findExpiredOrders(cutoffTime);
    }
    
    /**
     * 注文明細一覧を取得
     */
    public List<OrderItem> getOrderItems(@NotNull UUID orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }
    
    /**
     * 注文履歴を取得
     */
    public List<OrderStatusHistory> getOrderHistory(@NotNull UUID orderId) {
        return orderStatusHistoryRepository.findByOrderId(orderId);
    }
    
    /**
     * 注文金額を計算
     */
    private void calculateOrderAmounts(Order order) {
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        
        BigDecimal subtotal = items.stream()
            .map(OrderItem::calculateSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal discountAmount = items.stream()
            .map(item -> item.getDiscountAmount() != null ? item.getDiscountAmount() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal taxAmount = items.stream()
            .map(item -> item.getTaxAmount() != null ? item.getTaxAmount() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalAmount = subtotal.subtract(discountAmount).add(taxAmount);
        
        if (order.getShippingAmount() != null) {
            totalAmount = totalAmount.add(order.getShippingAmount());
        }
        
        order.setSubtotalAmount(subtotal);
        order.setDiscountAmount(discountAmount);
        order.setTaxAmount(taxAmount);
        order.setTotalAmount(totalAmount);
    }
    
    /**
     * 注文ステータス変更イベント
     */
    public static class OrderStatusChangedEvent {
        private final UUID orderId;
        private final OrderStatus previousStatus;
        private final OrderStatus newStatus;
        private final String changedBy;
        private final LocalDateTime changedAt;
        
        public OrderStatusChangedEvent(UUID orderId, OrderStatus previousStatus, OrderStatus newStatus, String changedBy) {
            this.orderId = orderId;
            this.previousStatus = previousStatus;
            this.newStatus = newStatus;
            this.changedBy = changedBy;
            this.changedAt = LocalDateTime.now();
        }
        
        // Getters
        public UUID getOrderId() { return orderId; }
        public OrderStatus getPreviousStatus() { return previousStatus; }
        public OrderStatus getNewStatus() { return newStatus; }
        public String getChangedBy() { return changedBy; }
        public LocalDateTime getChangedAt() { return changedAt; }
    }
}
