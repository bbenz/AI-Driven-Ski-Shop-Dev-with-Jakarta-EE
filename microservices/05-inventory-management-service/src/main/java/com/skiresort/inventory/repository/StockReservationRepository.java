package com.skiresort.inventory.repository;

import com.skiresort.inventory.model.StockReservation;
import com.skiresort.inventory.model.InventoryItem;
import com.skiresort.inventory.model.ReservationStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * 在庫予約リポジトリ
 */
@ApplicationScoped
@Transactional
public class StockReservationRepository {
    
    private static final Logger logger = Logger.getLogger(StockReservationRepository.class.getName());
    
    @PersistenceContext
    private EntityManager entityManager;
    
    /**
     * 在庫予約を保存する
     */
    public StockReservation save(StockReservation reservation) {
        if (reservation.getId() == null) {
            entityManager.persist(reservation);
            logger.info("新しい在庫予約を作成しました: 注文ID=" + reservation.getOrderId());
            return reservation;
        } else {
            StockReservation merged = entityManager.merge(reservation);
            logger.info("在庫予約を更新しました: ID=" + merged.getId());
            return merged;
        }
    }
    
    /**
     * IDで在庫予約を検索する
     */
    public Optional<StockReservation> findById(UUID id) {
        try {
            StockReservation reservation = entityManager.find(StockReservation.class, id);
            return Optional.ofNullable(reservation);
        } catch (Exception e) {
            logger.warning("在庫予約の検索でエラーが発生しました: ID=" + id + ", エラー=" + e.getMessage());
            return Optional.empty();
        }
    }
    
    /**
     * 注文IDで在庫予約を検索する
     */
    public List<StockReservation> findByOrderId(UUID orderId) {
        try {
            TypedQuery<StockReservation> query = entityManager.createNamedQuery("StockReservation.findByOrderId", StockReservation.class);
            query.setParameter("orderId", orderId);
            return query.getResultList();
        } catch (Exception e) {
            logger.warning("注文IDでの在庫予約検索でエラーが発生しました: orderId=" + orderId + ", エラー=" + e.getMessage());
            return List.of();
        }
    }
    
    /**
     * 顧客IDで在庫予約を検索する
     */
    public List<StockReservation> findByCustomerId(UUID customerId) {
        try {
            TypedQuery<StockReservation> query = entityManager.createNamedQuery("StockReservation.findByCustomerId", StockReservation.class);
            query.setParameter("customerId", customerId);
            return query.getResultList();
        } catch (Exception e) {
            logger.warning("顧客IDでの在庫予約検索でエラーが発生しました: customerId=" + customerId + ", エラー=" + e.getMessage());
            return List.of();
        }
    }
    
    /**
     * 期限切れの在庫予約を検索する
     */
    public List<StockReservation> findExpiredReservations() {
        try {
            TypedQuery<StockReservation> query = entityManager.createNamedQuery("StockReservation.findExpired", StockReservation.class);
            query.setParameter("currentTime", LocalDateTime.now());
            return query.getResultList();
        } catch (Exception e) {
            logger.warning("期限切れ在庫予約の検索でエラーが発生しました: エラー=" + e.getMessage());
            return List.of();
        }
    }
    
    /**
     * 在庫アイテムのアクティブな予約を検索する
     */
    public List<StockReservation> findActiveByInventoryItem(InventoryItem inventoryItem) {
        try {
            TypedQuery<StockReservation> query = entityManager.createNamedQuery("StockReservation.findActiveByInventoryItem", StockReservation.class);
            query.setParameter("inventoryItem", inventoryItem);
            return query.getResultList();
        } catch (Exception e) {
            logger.warning("在庫アイテムでのアクティブ予約検索でエラーが発生しました: SKU=" + inventoryItem.getSku() + ", エラー=" + e.getMessage());
            return List.of();
        }
    }
    
    /**
     * ステータス別で在庫予約を検索する
     */
    public List<StockReservation> findByStatus(ReservationStatus status) {
        try {
            TypedQuery<StockReservation> query = entityManager.createQuery(
                "SELECT r FROM StockReservation r WHERE r.status = :status ORDER BY r.createdAt DESC", 
                StockReservation.class);
            query.setParameter("status", status);
            return query.getResultList();
        } catch (Exception e) {
            logger.warning("ステータス別在庫予約検索でエラーが発生しました: status=" + status + ", エラー=" + e.getMessage());
            return List.of();
        }
    }
    
    /**
     * 全ての在庫予約を取得する
     */
    public List<StockReservation> findAll() {
        try {
            TypedQuery<StockReservation> query = entityManager.createQuery(
                "SELECT r FROM StockReservation r ORDER BY r.createdAt DESC", StockReservation.class);
            return query.getResultList();
        } catch (Exception e) {
            logger.warning("全在庫予約の取得でエラーが発生しました: エラー=" + e.getMessage());
            return List.of();
        }
    }
    
    /**
     * ページネーション対応で在庫予約を取得する
     */
    public List<StockReservation> findAll(int page, int size) {
        try {
            TypedQuery<StockReservation> query = entityManager.createQuery(
                "SELECT r FROM StockReservation r ORDER BY r.createdAt DESC", StockReservation.class);
            query.setFirstResult(page * size);
            query.setMaxResults(size);
            return query.getResultList();
        } catch (Exception e) {
            logger.warning("ページネーション対応在庫予約取得でエラーが発生しました: page=" + page + ", size=" + size + ", エラー=" + e.getMessage());
            return List.of();
        }
    }
    
    /**
     * 在庫予約数をカウントする
     */
    public long count() {
        try {
            TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(r) FROM StockReservation r", Long.class);
            return query.getSingleResult();
        } catch (Exception e) {
            logger.warning("在庫予約数のカウントでエラーが発生しました: エラー=" + e.getMessage());
            return 0L;
        }
    }
    
    /**
     * ステータス別在庫予約数をカウントする
     */
    public long countByStatus(ReservationStatus status) {
        try {
            TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(r) FROM StockReservation r WHERE r.status = :status", Long.class);
            query.setParameter("status", status);
            return query.getSingleResult();
        } catch (Exception e) {
            logger.warning("ステータス別在庫予約数のカウントでエラーが発生しました: status=" + status + ", エラー=" + e.getMessage());
            return 0L;
        }
    }
    
    /**
     * 在庫予約を削除する
     */
    public void delete(StockReservation reservation) {
        try {
            if (entityManager.contains(reservation)) {
                entityManager.remove(reservation);
            } else {
                StockReservation merged = entityManager.merge(reservation);
                entityManager.remove(merged);
            }
            logger.info("在庫予約を削除しました: ID=" + reservation.getId());
        } catch (Exception e) {
            logger.warning("在庫予約の削除でエラーが発生しました: ID=" + reservation.getId() + ", エラー=" + e.getMessage());
            throw new RuntimeException("在庫予約の削除に失敗しました", e);
        }
    }
    
    /**
     * IDで在庫予約を削除する
     */
    public void deleteById(UUID id) {
        try {
            StockReservation reservation = entityManager.find(StockReservation.class, id);
            if (reservation != null) {
                entityManager.remove(reservation);
                logger.info("在庫予約を削除しました: ID=" + id);
            } else {
                logger.warning("削除対象の在庫予約が見つかりません: ID=" + id);
            }
        } catch (Exception e) {
            logger.warning("IDでの在庫予約削除でエラーが発生しました: ID=" + id + ", エラー=" + e.getMessage());
            throw new RuntimeException("在庫予約の削除に失敗しました", e);
        }
    }
    
    /**
     * 期限切れ予約をバッチ更新する
     */
    public int updateExpiredReservations() {
        try {
            TypedQuery<StockReservation> selectQuery = entityManager.createQuery(
                "SELECT r FROM StockReservation r WHERE r.expiresAt < :currentTime AND r.status = 'ACTIVE'", 
                StockReservation.class);
            selectQuery.setParameter("currentTime", LocalDateTime.now());
            
            List<StockReservation> expiredReservations = selectQuery.getResultList();
            
            for (StockReservation reservation : expiredReservations) {
                reservation.expire();
                entityManager.merge(reservation);
            }
            
            logger.info("期限切れ予約を更新しました: 件数=" + expiredReservations.size());
            return expiredReservations.size();
        } catch (Exception e) {
            logger.warning("期限切れ予約の更新でエラーが発生しました: エラー=" + e.getMessage());
            throw new RuntimeException("期限切れ予約の更新に失敗しました", e);
        }
    }
}
