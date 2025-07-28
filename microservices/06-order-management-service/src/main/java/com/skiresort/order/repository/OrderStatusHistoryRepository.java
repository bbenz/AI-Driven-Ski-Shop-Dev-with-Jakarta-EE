package com.skiresort.order.repository;

import com.skiresort.order.model.Order;
import com.skiresort.order.model.OrderStatus;
import com.skiresort.order.model.OrderStatusHistory;
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
 * 注文ステータス履歴リポジトリ
 */
@ApplicationScoped
@Transactional
public class OrderStatusHistoryRepository {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    /**
     * 注文ステータス履歴を保存
     */
    public OrderStatusHistory save(OrderStatusHistory orderStatusHistory) {
        if (orderStatusHistory.getId() == null) {
            entityManager.persist(orderStatusHistory);
            return orderStatusHistory;
        } else {
            return entityManager.merge(orderStatusHistory);
        }
    }
    
    /**
     * IDで注文ステータス履歴を検索
     */
    public Optional<OrderStatusHistory> findById(UUID id) {
        OrderStatusHistory history = entityManager.find(OrderStatusHistory.class, id);
        return Optional.ofNullable(history);
    }
    
    /**
     * 注文で履歴を検索（新しい順）
     */
    public List<OrderStatusHistory> findByOrder(Order order) {
        TypedQuery<OrderStatusHistory> query = entityManager.createNamedQuery(
            "OrderStatusHistory.findByOrder", OrderStatusHistory.class);
        query.setParameter("order", order);
        return query.getResultList();
    }
    
    /**
     * 注文IDで履歴を検索
     */
    public List<OrderStatusHistory> findByOrderId(UUID orderId) {
        TypedQuery<OrderStatusHistory> query = entityManager.createQuery(
            "SELECT osh FROM OrderStatusHistory osh WHERE osh.order.id = :orderId ORDER BY osh.createdAt DESC", 
            OrderStatusHistory.class);
        query.setParameter("orderId", orderId);
        return query.getResultList();
    }
    
    /**
     * 注文とステータスで履歴を検索
     */
    public List<OrderStatusHistory> findByOrderAndStatus(Order order, OrderStatus status) {
        TypedQuery<OrderStatusHistory> query = entityManager.createNamedQuery(
            "OrderStatusHistory.findByOrderAndStatus", OrderStatusHistory.class);
        query.setParameter("order", order);
        query.setParameter("status", status);
        return query.getResultList();
    }
    
    /**
     * ステータスで履歴を検索
     */
    public List<OrderStatusHistory> findByStatus(OrderStatus status) {
        TypedQuery<OrderStatusHistory> query = entityManager.createNamedQuery(
            "OrderStatusHistory.findByStatus", OrderStatusHistory.class);
        query.setParameter("status", status);
        return query.getResultList();
    }
    
    /**
     * 日付範囲で履歴を検索
     */
    public List<OrderStatusHistory> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        TypedQuery<OrderStatusHistory> query = entityManager.createNamedQuery(
            "OrderStatusHistory.findByDateRange", OrderStatusHistory.class);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        return query.getResultList();
    }
    
    /**
     * 変更者で履歴を検索
     */
    public List<OrderStatusHistory> findByChangedBy(String changedBy) {
        TypedQuery<OrderStatusHistory> query = entityManager.createQuery(
            "SELECT osh FROM OrderStatusHistory osh WHERE osh.changedBy = :changedBy ORDER BY osh.createdAt DESC", 
            OrderStatusHistory.class);
        query.setParameter("changedBy", changedBy);
        return query.getResultList();
    }
    
    /**
     * 自動変更かどうかで履歴を検索
     */
    public List<OrderStatusHistory> findByAutomaticChange(Boolean automaticChange) {
        TypedQuery<OrderStatusHistory> query = entityManager.createQuery(
            "SELECT osh FROM OrderStatusHistory osh WHERE osh.automaticChange = :automaticChange ORDER BY osh.createdAt DESC", 
            OrderStatusHistory.class);
        query.setParameter("automaticChange", automaticChange);
        return query.getResultList();
    }
    
    /**
     * 注文の最新履歴を取得
     */
    public Optional<OrderStatusHistory> findLatestByOrder(Order order) {
        TypedQuery<OrderStatusHistory> query = entityManager.createQuery(
            "SELECT osh FROM OrderStatusHistory osh WHERE osh.order = :order ORDER BY osh.createdAt DESC", 
            OrderStatusHistory.class);
        query.setParameter("order", query);
        query.setMaxResults(1);
        
        List<OrderStatusHistory> results = query.getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
    
    /**
     * 注文IDの最新履歴を取得
     */
    public Optional<OrderStatusHistory> findLatestByOrderId(UUID orderId) {
        TypedQuery<OrderStatusHistory> query = entityManager.createQuery(
            "SELECT osh FROM OrderStatusHistory osh WHERE osh.order.id = :orderId ORDER BY osh.createdAt DESC", 
            OrderStatusHistory.class);
        query.setParameter("orderId", orderId);
        query.setMaxResults(1);
        
        List<OrderStatusHistory> results = query.getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
    
    /**
     * 注文の履歴数を取得
     */
    public long countByOrderId(UUID orderId) {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(osh) FROM OrderStatusHistory osh WHERE osh.order.id = :orderId", Long.class);
        query.setParameter("orderId", orderId);
        return query.getSingleResult();
    }
    
    /**
     * ステータス別の履歴数を取得
     */
    public long countByStatus(OrderStatus status) {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(osh) FROM OrderStatusHistory osh WHERE osh.newStatus = :status", Long.class);
        query.setParameter("status", status);
        return query.getSingleResult();
    }
    
    /**
     * 変更者別の履歴数を取得
     */
    public long countByChangedBy(String changedBy) {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(osh) FROM OrderStatusHistory osh WHERE osh.changedBy = :changedBy", Long.class);
        query.setParameter("changedBy", changedBy);
        return query.getSingleResult();
    }
    
    /**
     * ステータス変遷統計を取得
     */
    public List<Object[]> getStatusTransitionStats() {
        TypedQuery<Object[]> query = entityManager.createQuery(
            "SELECT osh.previousStatus, osh.newStatus, COUNT(osh) " +
            "FROM OrderStatusHistory osh " +
            "GROUP BY osh.previousStatus, osh.newStatus " +
            "ORDER BY COUNT(osh) DESC", 
            Object[].class);
        return query.getResultList();
    }
    
    /**
     * 平均ステータス変更時間を取得
     */
    public List<Object[]> getAverageStatusTransitionTime() {
        TypedQuery<Object[]> query = entityManager.createQuery(
            "SELECT osh.newStatus, AVG(EXTRACT(EPOCH FROM (osh.createdAt - osh.order.createdAt)) / 3600) as avgHours " +
            "FROM OrderStatusHistory osh " +
            "GROUP BY osh.newStatus " +
            "ORDER BY avgHours", 
            Object[].class);
        return query.getResultList();
    }
    
    /**
     * 履歴を削除
     */
    public void delete(OrderStatusHistory orderStatusHistory) {
        if (entityManager.contains(orderStatusHistory)) {
            entityManager.remove(orderStatusHistory);
        } else {
            OrderStatusHistory managedHistory = entityManager.merge(orderStatusHistory);
            entityManager.remove(managedHistory);
        }
    }
    
    /**
     * IDで履歴を削除
     */
    public boolean deleteById(UUID id) {
        Optional<OrderStatusHistory> history = findById(id);
        if (history.isPresent()) {
            delete(history.get());
            return true;
        }
        return false;
    }
    
    /**
     * 注文に関連するすべての履歴を削除
     */
    public int deleteByOrderId(UUID orderId) {
        return entityManager.createQuery("DELETE FROM OrderStatusHistory osh WHERE osh.order.id = :orderId")
            .setParameter("orderId", orderId)
            .executeUpdate();
    }
    
    /**
     * 古い履歴を削除（指定日より前）
     */
    public int deleteOldHistory(LocalDateTime cutoffDate) {
        return entityManager.createQuery("DELETE FROM OrderStatusHistory osh WHERE osh.createdAt < :cutoffDate")
            .setParameter("cutoffDate", cutoffDate)
            .executeUpdate();
    }
    
    /**
     * 複数の履歴をバッチ保存
     */
    public List<OrderStatusHistory> saveAll(List<OrderStatusHistory> histories) {
        histories.forEach(this::save);
        entityManager.flush();
        return histories;
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
     * 履歴の存在確認
     */
    public boolean existsById(UUID id) {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(osh) FROM OrderStatusHistory osh WHERE osh.id = :id", Long.class);
        query.setParameter("id", id);
        return query.getSingleResult() > 0;
    }
}
