package com.skiresort.inventory.repository;

import com.skiresort.inventory.model.StockMovement;
import com.skiresort.inventory.model.InventoryItem;
import com.skiresort.inventory.model.MovementType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * 在庫移動履歴リポジトリ
 */
@ApplicationScoped
@Transactional
public class StockMovementRepository {
    
    private static final Logger logger = Logger.getLogger(StockMovementRepository.class.getName());
    
    @PersistenceContext
    private EntityManager entityManager;
    
    /**
     * 在庫移動履歴を保存する
     */
    public StockMovement save(StockMovement movement) {
        if (movement.getId() == null) {
            entityManager.persist(movement);
            logger.info("新しい在庫移動履歴を作成しました: タイプ=" + movement.getMovementType());
            return movement;
        } else {
            StockMovement merged = entityManager.merge(movement);
            logger.info("在庫移動履歴を更新しました: ID=" + merged.getId());
            return merged;
        }
    }
    
    /**
     * IDで在庫移動履歴を検索する
     */
    public Optional<StockMovement> findById(UUID id) {
        try {
            StockMovement movement = entityManager.find(StockMovement.class, id);
            return Optional.ofNullable(movement);
        } catch (Exception e) {
            logger.warning("在庫移動履歴の検索でエラーが発生しました: ID=" + id + ", エラー=" + e.getMessage());
            return Optional.empty();
        }
    }
    
    /**
     * 在庫アイテムの移動履歴を検索する
     */
    public List<StockMovement> findByInventoryItem(InventoryItem inventoryItem) {
        try {
            TypedQuery<StockMovement> query = entityManager.createNamedQuery("StockMovement.findByInventoryItem", StockMovement.class);
            query.setParameter("inventoryItem", inventoryItem);
            return query.getResultList();
        } catch (Exception e) {
            logger.warning("在庫アイテムでの移動履歴検索でエラーが発生しました: SKU=" + inventoryItem.getSku() + ", エラー=" + e.getMessage());
            return List.of();
        }
    }
    
    /**
     * 日付範囲で移動履歴を検索する
     */
    public List<StockMovement> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        try {
            TypedQuery<StockMovement> query = entityManager.createNamedQuery("StockMovement.findByDateRange", StockMovement.class);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            return query.getResultList();
        } catch (Exception e) {
            logger.warning("日付範囲での移動履歴検索でエラーが発生しました: 開始日=" + startDate + ", 終了日=" + endDate + ", エラー=" + e.getMessage());
            return List.of();
        }
    }
    
    /**
     * 移動タイプで履歴を検索する
     */
    public List<StockMovement> findByMovementType(MovementType movementType) {
        try {
            TypedQuery<StockMovement> query = entityManager.createNamedQuery("StockMovement.findByMovementType", StockMovement.class);
            query.setParameter("movementType", movementType);
            return query.getResultList();
        } catch (Exception e) {
            logger.warning("移動タイプでの履歴検索でエラーが発生しました: タイプ=" + movementType + ", エラー=" + e.getMessage());
            return List.of();
        }
    }
    
    /**
     * 注文IDで移動履歴を検索する
     */
    public List<StockMovement> findByOrderId(UUID orderId) {
        try {
            TypedQuery<StockMovement> query = entityManager.createQuery(
                "SELECT m FROM StockMovement m WHERE m.orderId = :orderId ORDER BY m.createdAt DESC", 
                StockMovement.class);
            query.setParameter("orderId", orderId);
            return query.getResultList();
        } catch (Exception e) {
            logger.warning("注文IDでの移動履歴検索でエラーが発生しました: orderId=" + orderId + ", エラー=" + e.getMessage());
            return List.of();
        }
    }
    
    /**
     * 全ての移動履歴を取得する
     */
    public List<StockMovement> findAll() {
        try {
            TypedQuery<StockMovement> query = entityManager.createQuery(
                "SELECT m FROM StockMovement m ORDER BY m.createdAt DESC", StockMovement.class);
            return query.getResultList();
        } catch (Exception e) {
            logger.warning("全移動履歴の取得でエラーが発生しました: エラー=" + e.getMessage());
            return List.of();
        }
    }
    
    /**
     * ページネーション対応で移動履歴を取得する
     */
    public List<StockMovement> findAll(int page, int size) {
        try {
            TypedQuery<StockMovement> query = entityManager.createQuery(
                "SELECT m FROM StockMovement m ORDER BY m.createdAt DESC", StockMovement.class);
            query.setFirstResult(page * size);
            query.setMaxResults(size);
            return query.getResultList();
        } catch (Exception e) {
            logger.warning("ページネーション対応移動履歴取得でエラーが発生しました: page=" + page + ", size=" + size + ", エラー=" + e.getMessage());
            return List.of();
        }
    }
    
    /**
     * 移動履歴数をカウントする
     */
    public long count() {
        try {
            TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(m) FROM StockMovement m", Long.class);
            return query.getSingleResult();
        } catch (Exception e) {
            logger.warning("移動履歴数のカウントでエラーが発生しました: エラー=" + e.getMessage());
            return 0L;
        }
    }
    
    /**
     * 移動タイプ別履歴数をカウントする
     */
    public long countByMovementType(MovementType movementType) {
        try {
            TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(m) FROM StockMovement m WHERE m.movementType = :movementType", Long.class);
            query.setParameter("movementType", movementType);
            return query.getSingleResult();
        } catch (Exception e) {
            logger.warning("移動タイプ別履歴数のカウントでエラーが発生しました: タイプ=" + movementType + ", エラー=" + e.getMessage());
            return 0L;
        }
    }
    
    /**
     * 移動履歴を削除する
     */
    public void delete(StockMovement movement) {
        try {
            if (entityManager.contains(movement)) {
                entityManager.remove(movement);
            } else {
                StockMovement merged = entityManager.merge(movement);
                entityManager.remove(merged);
            }
            logger.info("移動履歴を削除しました: ID=" + movement.getId());
        } catch (Exception e) {
            logger.warning("移動履歴の削除でエラーが発生しました: ID=" + movement.getId() + ", エラー=" + e.getMessage());
            throw new RuntimeException("移動履歴の削除に失敗しました", e);
        }
    }
    
    /**
     * IDで移動履歴を削除する
     */
    public void deleteById(UUID id) {
        try {
            StockMovement movement = entityManager.find(StockMovement.class, id);
            if (movement != null) {
                entityManager.remove(movement);
                logger.info("移動履歴を削除しました: ID=" + id);
            } else {
                logger.warning("削除対象の移動履歴が見つかりません: ID=" + id);
            }
        } catch (Exception e) {
            logger.warning("IDでの移動履歴削除でエラーが発生しました: ID=" + id + ", エラー=" + e.getMessage());
            throw new RuntimeException("移動履歴の削除に失敗しました", e);
        }
    }
}
