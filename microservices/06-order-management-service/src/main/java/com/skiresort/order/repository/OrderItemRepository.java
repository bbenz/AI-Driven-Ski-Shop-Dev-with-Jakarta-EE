package com.skiresort.order.repository;

import com.skiresort.order.model.Order;
import com.skiresort.order.model.OrderItem;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 注文明細リポジトリ
 */
@ApplicationScoped
@Transactional
public class OrderItemRepository {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    /**
     * 注文明細を保存
     */
    public OrderItem save(OrderItem orderItem) {
        if (orderItem.getId() == null) {
            entityManager.persist(orderItem);
            return orderItem;
        } else {
            return entityManager.merge(orderItem);
        }
    }
    
    /**
     * IDで注文明細を検索
     */
    public Optional<OrderItem> findById(UUID id) {
        OrderItem orderItem = entityManager.find(OrderItem.class, id);
        return Optional.ofNullable(orderItem);
    }
    
    /**
     * 注文で注文明細を検索
     */
    public List<OrderItem> findByOrder(Order order) {
        TypedQuery<OrderItem> query = entityManager.createNamedQuery("OrderItem.findByOrder", OrderItem.class);
        query.setParameter("order", order);
        return query.getResultList();
    }
    
    /**
     * 注文IDで注文明細を検索
     */
    public List<OrderItem> findByOrderId(UUID orderId) {
        TypedQuery<OrderItem> query = entityManager.createQuery(
            "SELECT oi FROM OrderItem oi WHERE oi.order.id = :orderId", OrderItem.class);
        query.setParameter("orderId", orderId);
        return query.getResultList();
    }
    
    /**
     * 商品IDで注文明細を検索
     */
    public List<OrderItem> findByProductId(UUID productId) {
        TypedQuery<OrderItem> query = entityManager.createNamedQuery("OrderItem.findByProductId", OrderItem.class);
        query.setParameter("productId", productId);
        return query.getResultList();
    }
    
    /**
     * SKUで注文明細を検索
     */
    public List<OrderItem> findBySku(String sku) {
        TypedQuery<OrderItem> query = entityManager.createNamedQuery("OrderItem.findBySku", OrderItem.class);
        query.setParameter("sku", sku);
        return query.getResultList();
    }
    
    /**
     * 予約IDで注文明細を検索
     */
    public List<OrderItem> findByReservationId(UUID reservationId) {
        TypedQuery<OrderItem> query = entityManager.createQuery(
            "SELECT oi FROM OrderItem oi WHERE oi.reservationId = :reservationId", OrderItem.class);
        query.setParameter("reservationId", reservationId);
        return query.getResultList();
    }
    
    /**
     * 注文の合計金額を計算
     */
    public BigDecimal calculateOrderTotal(UUID orderId) {
        TypedQuery<BigDecimal> query = entityManager.createQuery(
            "SELECT COALESCE(SUM(oi.totalAmount), 0) FROM OrderItem oi WHERE oi.order.id = :orderId", 
            BigDecimal.class);
        query.setParameter("orderId", orderId);
        return query.getSingleResult();
    }
    
    /**
     * 注文の明細数を取得
     */
    public long countByOrderId(UUID orderId) {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(oi) FROM OrderItem oi WHERE oi.order.id = :orderId", Long.class);
        query.setParameter("orderId", orderId);
        return query.getSingleResult();
    }
    
    /**
     * 商品の注文回数を取得
     */
    public long countByProductId(UUID productId) {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(oi) FROM OrderItem oi WHERE oi.productId = :productId", Long.class);
        query.setParameter("productId", productId);
        return query.getSingleResult();
    }
    
    /**
     * 商品の総注文数量を取得
     */
    public Integer sumQuantityByProductId(UUID productId) {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COALESCE(SUM(oi.quantity), 0) FROM OrderItem oi WHERE oi.productId = :productId", 
            Long.class);
        query.setParameter("productId", productId);
        return query.getSingleResult().intValue();
    }
    
    /**
     * 注文明細を削除
     */
    public void delete(OrderItem orderItem) {
        if (entityManager.contains(orderItem)) {
            entityManager.remove(orderItem);
        } else {
            OrderItem managedOrderItem = entityManager.merge(orderItem);
            entityManager.remove(managedOrderItem);
        }
    }
    
    /**
     * IDで注文明細を削除
     */
    public boolean deleteById(UUID id) {
        Optional<OrderItem> orderItem = findById(id);
        if (orderItem.isPresent()) {
            delete(orderItem.get());
            return true;
        }
        return false;
    }
    
    /**
     * 注文に関連するすべての明細を削除
     */
    public int deleteByOrderId(UUID orderId) {
        return entityManager.createQuery("DELETE FROM OrderItem oi WHERE oi.order.id = :orderId")
            .setParameter("orderId", orderId)
            .executeUpdate();
    }
    
    /**
     * 複数の注文明細をバッチ保存
     */
    public List<OrderItem> saveAll(List<OrderItem> orderItems) {
        orderItems.forEach(this::save);
        entityManager.flush();
        return orderItems;
    }
    
    /**
     * 数量をバッチ更新
     */
    public void updateQuantityBatch(List<UUID> itemIds, Integer newQuantity) {
        entityManager.createQuery(
            "UPDATE OrderItem oi SET oi.quantity = :quantity WHERE oi.id IN :ids")
            .setParameter("quantity", newQuantity)
            .setParameter("ids", itemIds)
            .executeUpdate();
    }
    
    /**
     * 割引をバッチ適用
     */
    public void applyDiscountBatch(List<UUID> itemIds, BigDecimal discountAmount) {
        entityManager.createQuery(
            "UPDATE OrderItem oi SET oi.discountAmount = :discountAmount WHERE oi.id IN :ids")
            .setParameter("discountAmount", discountAmount)
            .setParameter("ids", itemIds)
            .executeUpdate();
    }
    
    /**
     * 最も売れている商品を取得
     */
    public List<Object[]> findTopSellingProducts(int limit) {
        TypedQuery<Object[]> query = entityManager.createQuery(
            "SELECT oi.productId, oi.productName, SUM(oi.quantity) as totalQuantity " +
            "FROM OrderItem oi " +
            "GROUP BY oi.productId, oi.productName " +
            "ORDER BY totalQuantity DESC", 
            Object[].class);
        query.setMaxResults(limit);
        return query.getResultList();
    }
    
    /**
     * 売上上位商品を取得
     */
    public List<Object[]> findTopRevenueProducts(int limit) {
        TypedQuery<Object[]> query = entityManager.createQuery(
            "SELECT oi.productId, oi.productName, SUM(oi.totalAmount) as totalRevenue " +
            "FROM OrderItem oi " +
            "GROUP BY oi.productId, oi.productName " +
            "ORDER BY totalRevenue DESC", 
            Object[].class);
        query.setMaxResults(limit);
        return query.getResultList();
    }
    
    /**
     * エンティティマネージャーのフラッシュ
     */
    public void flush() {
        entityManager.flush();
    }
    
    /**
     * エンティティマネージャーのクリア
     */
    public void clear() {
        entityManager.clear();
    }
    
    /**
     * 注文明細の存在確認
     */
    public boolean existsById(UUID id) {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(oi) FROM OrderItem oi WHERE oi.id = :id", Long.class);
        query.setParameter("id", id);
        return query.getSingleResult() > 0;
    }
}
