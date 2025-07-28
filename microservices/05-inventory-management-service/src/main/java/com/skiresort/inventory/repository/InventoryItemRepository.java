package com.skiresort.inventory.repository;

import com.skiresort.inventory.model.InventoryItem;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * 在庫アイテムリポジトリ
 */
@ApplicationScoped
@Transactional
public class InventoryItemRepository {
    
    private static final Logger logger = Logger.getLogger(InventoryItemRepository.class.getName());
    
    @PersistenceContext
    private EntityManager entityManager;
    
    /**
     * 在庫アイテムを保存する
     */
    public InventoryItem save(InventoryItem inventoryItem) {
        if (inventoryItem.getId() == null) {
            entityManager.persist(inventoryItem);
            logger.info("新しい在庫アイテムを作成しました: SKU=" + inventoryItem.getSku());
            return inventoryItem;
        } else {
            InventoryItem merged = entityManager.merge(inventoryItem);
            logger.info("在庫アイテムを更新しました: SKU=" + merged.getSku());
            return merged;
        }
    }
    
    /**
     * IDで在庫アイテムを検索する
     */
    public Optional<InventoryItem> findById(UUID id) {
        try {
            InventoryItem item = entityManager.find(InventoryItem.class, id);
            return Optional.ofNullable(item);
        } catch (Exception e) {
            logger.warning("在庫アイテムの検索でエラーが発生しました: ID=" + id + ", エラー=" + e.getMessage());
            return Optional.empty();
        }
    }
    
    /**
     * SKUで在庫アイテムを検索する
     */
    public Optional<InventoryItem> findBySku(String sku) {
        try {
            TypedQuery<InventoryItem> query = entityManager.createNamedQuery("InventoryItem.findBySku", InventoryItem.class);
            query.setParameter("sku", sku);
            InventoryItem item = query.getSingleResult();
            return Optional.of(item);
        } catch (NoResultException e) {
            logger.info("指定されたSKUの在庫アイテムが見つかりません: SKU=" + sku);
            return Optional.empty();
        } catch (Exception e) {
            logger.warning("SKUでの在庫アイテム検索でエラーが発生しました: SKU=" + sku + ", エラー=" + e.getMessage());
            return Optional.empty();
        }
    }
    
    /**
     * 倉庫IDで在庫アイテムを検索する
     */
    public List<InventoryItem> findByWarehouseId(UUID warehouseId) {
        try {
            TypedQuery<InventoryItem> query = entityManager.createNamedQuery("InventoryItem.findByWarehouse", InventoryItem.class);
            query.setParameter("warehouseId", warehouseId);
            return query.getResultList();
        } catch (Exception e) {
            logger.warning("倉庫IDでの在庫アイテム検索でエラーが発生しました: warehouseId=" + warehouseId + ", エラー=" + e.getMessage());
            return List.of();
        }
    }
    
    /**
     * 低在庫アイテムを検索する
     */
    public List<InventoryItem> findLowStockItems() {
        try {
            TypedQuery<InventoryItem> query = entityManager.createNamedQuery("InventoryItem.findLowStock", InventoryItem.class);
            return query.getResultList();
        } catch (Exception e) {
            logger.warning("低在庫アイテムの検索でエラーが発生しました: エラー=" + e.getMessage());
            return List.of();
        }
    }
    
    /**
     * 在庫切れアイテムを検索する
     */
    public List<InventoryItem> findOutOfStockItems() {
        try {
            TypedQuery<InventoryItem> query = entityManager.createNamedQuery("InventoryItem.findOutOfStock", InventoryItem.class);
            return query.getResultList();
        } catch (Exception e) {
            logger.warning("在庫切れアイテムの検索でエラーが発生しました: エラー=" + e.getMessage());
            return List.of();
        }
    }
    
    /**
     * 全ての在庫アイテムを取得する
     */
    public List<InventoryItem> findAll() {
        try {
            TypedQuery<InventoryItem> query = entityManager.createQuery(
                "SELECT i FROM InventoryItem i ORDER BY i.createdAt DESC", InventoryItem.class);
            return query.getResultList();
        } catch (Exception e) {
            logger.warning("全在庫アイテムの取得でエラーが発生しました: エラー=" + e.getMessage());
            return List.of();
        }
    }
    
    /**
     * ページネーション対応で在庫アイテムを取得する
     */
    public List<InventoryItem> findAll(int page, int size) {
        try {
            TypedQuery<InventoryItem> query = entityManager.createQuery(
                "SELECT i FROM InventoryItem i ORDER BY i.createdAt DESC", InventoryItem.class);
            query.setFirstResult(page * size);
            query.setMaxResults(size);
            return query.getResultList();
        } catch (Exception e) {
            logger.warning("ページネーション対応在庫アイテム取得でエラーが発生しました: page=" + page + ", size=" + size + ", エラー=" + e.getMessage());
            return List.of();
        }
    }
    
    /**
     * 在庫アイテム数をカウントする
     */
    public long count() {
        try {
            TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(i) FROM InventoryItem i", Long.class);
            return query.getSingleResult();
        } catch (Exception e) {
            logger.warning("在庫アイテム数のカウントでエラーが発生しました: エラー=" + e.getMessage());
            return 0L;
        }
    }
    
    /**
     * 低在庫アイテム数をカウントする
     */
    public long countLowStockItems() {
        try {
            TypedQuery<Long> query = entityManager.createNamedQuery("InventoryItem.countLowStock", Long.class);
            return query.getSingleResult();
        } catch (Exception e) {
            logger.warning("低在庫アイテム数のカウントでエラーが発生しました: エラー=" + e.getMessage());
            return 0L;
        }
    }
    
    /**
     * 在庫切れアイテム数をカウントする
     */
    public long countOutOfStockItems() {
        try {
            TypedQuery<Long> query = entityManager.createNamedQuery("InventoryItem.countOutOfStock", Long.class);
            return query.getSingleResult();
        } catch (Exception e) {
            logger.warning("在庫切れアイテム数のカウントでエラーが発生しました: エラー=" + e.getMessage());
            return 0L;
        }
    }
    
    /**
     * 在庫アイテムを削除する
     */
    public void delete(InventoryItem inventoryItem) {
        try {
            if (entityManager.contains(inventoryItem)) {
                entityManager.remove(inventoryItem);
            } else {
                InventoryItem merged = entityManager.merge(inventoryItem);
                entityManager.remove(merged);
            }
            logger.info("在庫アイテムを削除しました: SKU=" + inventoryItem.getSku());
        } catch (Exception e) {
            logger.warning("在庫アイテムの削除でエラーが発生しました: SKU=" + inventoryItem.getSku() + ", エラー=" + e.getMessage());
            throw e;
        }
    }
    
    /**
     * IDで在庫アイテムを削除する
     */
    public void deleteById(UUID id) {
        try {
            InventoryItem item = entityManager.find(InventoryItem.class, id);
            if (item != null) {
                entityManager.remove(item);
                logger.info("在庫アイテムを削除しました: ID=" + id);
            } else {
                logger.warning("削除対象の在庫アイテムが見つかりません: ID=" + id);
            }
        } catch (Exception e) {
            logger.warning("IDでの在庫アイテム削除でエラーが発生しました: ID=" + id + ", エラー=" + e.getMessage());
            throw e;
        }
    }
    
    /**
     * SKUの存在をチェックする
     */
    public boolean existsBySku(String sku) {
        try {
            TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(i) FROM InventoryItem i WHERE i.sku = :sku", Long.class);
            query.setParameter("sku", sku);
            return query.getSingleResult() > 0;
        } catch (Exception e) {
            logger.warning("SKU存在チェックでエラーが発生しました: SKU=" + sku + ", エラー=" + e.getMessage());
            return false;
        }
    }
}
