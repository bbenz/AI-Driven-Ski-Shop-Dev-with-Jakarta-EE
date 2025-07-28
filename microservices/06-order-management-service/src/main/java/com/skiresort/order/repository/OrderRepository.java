package com.skiresort.order.repository;

import com.skiresort.order.model.Order;
import com.skiresort.order.model.OrderStatus;
import com.skiresort.order.model.PaymentStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 注文リポジトリ
 */
@ApplicationScoped
@Transactional
public class OrderRepository {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    /**
     * 注文を保存
     */
    public Order save(Order order) {
        if (order.getId() == null) {
            entityManager.persist(order);
            return order;
        } else {
            return entityManager.merge(order);
        }
    }
    
    /**
     * IDで注文を検索
     */
    public Optional<Order> findById(UUID id) {
        Order order = entityManager.find(Order.class, id);
        return Optional.ofNullable(order);
    }
    
    /**
     * 注文番号で注文を検索
     */
    public Optional<Order> findByOrderNumber(String orderNumber) {
        TypedQuery<Order> query = entityManager.createNamedQuery("Order.findByOrderNumber", Order.class);
        query.setParameter("orderNumber", orderNumber);
        
        List<Order> results = query.getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
    
    /**
     * 顧客IDで注文を検索
     */
    public List<Order> findByCustomerId(UUID customerId) {
        TypedQuery<Order> query = entityManager.createNamedQuery("Order.findByCustomerId", Order.class);
        query.setParameter("customerId", customerId);
        return query.getResultList();
    }
    
    /**
     * 顧客IDで注文を検索（ページング付き）
     */
    public List<Order> findByCustomerId(UUID customerId, int offset, int limit) {
        TypedQuery<Order> query = entityManager.createNamedQuery("Order.findByCustomerId", Order.class);
        query.setParameter("customerId", customerId);
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        return query.getResultList();
    }
    
    /**
     * ステータスで注文を検索
     */
    public List<Order> findByStatus(OrderStatus status) {
        TypedQuery<Order> query = entityManager.createNamedQuery("Order.findByStatus", Order.class);
        query.setParameter("status", status);
        return query.getResultList();
    }
    
    /**
     * 決済ステータスで注文を検索
     */
    public List<Order> findByPaymentStatus(PaymentStatus paymentStatus) {
        TypedQuery<Order> query = entityManager.createNamedQuery("Order.findByPaymentStatus", Order.class);
        query.setParameter("paymentStatus", paymentStatus);
        return query.getResultList();
    }
    
    /**
     * 日付範囲で注文を検索
     */
    public List<Order> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        TypedQuery<Order> query = entityManager.createNamedQuery("Order.findByDateRange", Order.class);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        return query.getResultList();
    }
    
    /**
     * 期限切れの注文を検索
     */
    public List<Order> findExpiredOrders(LocalDateTime cutoffTime) {
        TypedQuery<Order> query = entityManager.createNamedQuery("Order.findExpiredOrders", Order.class);
        query.setParameter("cutoffTime", cutoffTime);
        return query.getResultList();
    }
    
    /**
     * すべての注文を検索
     */
    public List<Order> findAll() {
        TypedQuery<Order> query = entityManager.createQuery("SELECT o FROM Order o ORDER BY o.createdAt DESC", Order.class);
        return query.getResultList();
    }
    
    /**
     * すべての注文を検索（ページング付き）
     */
    public List<Order> findAll(int offset, int limit) {
        TypedQuery<Order> query = entityManager.createQuery("SELECT o FROM Order o ORDER BY o.createdAt DESC", Order.class);
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        return query.getResultList();
    }
    
    /**
     * 注文の総数を取得
     */
    public long count() {
        TypedQuery<Long> query = entityManager.createQuery("SELECT COUNT(o) FROM Order o", Long.class);
        return query.getSingleResult();
    }
    
    /**
     * 顧客の注文数を取得
     */
    public long countByCustomerId(UUID customerId) {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(o) FROM Order o WHERE o.customerId = :customerId", Long.class);
        query.setParameter("customerId", customerId);
        return query.getSingleResult();
    }
    
    /**
     * ステータス別の注文数を取得
     */
    public long countByStatus(OrderStatus status) {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(o) FROM Order o WHERE o.status = :status", Long.class);
        query.setParameter("status", status);
        return query.getSingleResult();
    }
    
    /**
     * 注文を削除
     */
    public void delete(Order order) {
        if (entityManager.contains(order)) {
            entityManager.remove(order);
        } else {
            Order managedOrder = entityManager.merge(order);
            entityManager.remove(managedOrder);
        }
    }
    
    /**
     * IDで注文を削除
     */
    public boolean deleteById(UUID id) {
        Optional<Order> order = findById(id);
        if (order.isPresent()) {
            delete(order.get());
            return true;
        }
        return false;
    }
    
    /**
     * 複数の注文をバッチ更新
     */
    public void updateStatusBatch(List<UUID> orderIds, OrderStatus newStatus) {
        entityManager.createQuery(
            "UPDATE Order o SET o.status = :status, o.updatedAt = :updatedAt WHERE o.id IN :ids")
            .setParameter("status", newStatus)
            .setParameter("updatedAt", LocalDateTime.now())
            .setParameter("ids", orderIds)
            .executeUpdate();
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
     * 注文の存在確認
     */
    public boolean existsById(UUID id) {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(o) FROM Order o WHERE o.id = :id", Long.class);
        query.setParameter("id", id);
        return query.getSingleResult() > 0;
    }
    
    /**
     * 注文番号の存在確認
     */
    public boolean existsByOrderNumber(String orderNumber) {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(o) FROM Order o WHERE o.orderNumber = :orderNumber", Long.class);
        query.setParameter("orderNumber", orderNumber);
        return query.getSingleResult() > 0;
    }
}
