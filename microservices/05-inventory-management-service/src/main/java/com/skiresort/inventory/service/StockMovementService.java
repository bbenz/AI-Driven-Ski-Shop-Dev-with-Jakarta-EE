package com.skiresort.inventory.service;

import com.skiresort.inventory.model.*;
import com.skiresort.inventory.repository.StockMovementRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * 在庫移動サービス
 */
@ApplicationScoped
@Transactional
public class StockMovementService {
    
    private static final Logger logger = Logger.getLogger(StockMovementService.class.getName());
    
    @Inject
    private StockMovementRepository movementRepository;
    
    /**
     * 在庫移動履歴を記録する
     */
    public StockMovement recordStockMovement(
            InventoryItem inventoryItem,
            MovementType movementType,
            Integer quantity,
            Integer previousQuantity,
            Integer newQuantity,
            String reason) {
        
        return recordStockMovement(
            inventoryItem, movementType, quantity, previousQuantity, newQuantity,
            reason, null, null, null, null, null, null
        );
    }
    
    /**
     * 在庫移動履歴を記録する（詳細版）
     */
    public StockMovement recordStockMovement(
            InventoryItem inventoryItem,
            MovementType movementType,
            Integer quantity,
            Integer previousQuantity,
            Integer newQuantity,
            String reason,
            String referenceNumber,
            UUID orderId,
            UUID supplierId,
            BigDecimal unitCost,
            String performedBy,
            String notes) {
        
        logger.info("在庫移動履歴を記録中: SKU=" + inventoryItem.getSku() + 
            ", タイプ=" + movementType + ", 数量=" + quantity);
        
        StockMovement movement = new StockMovement(
            inventoryItem, movementType, quantity, previousQuantity, newQuantity, reason
        );
        
        movement.setReferenceNumber(referenceNumber);
        movement.setOrderId(orderId);
        movement.setSupplierId(supplierId);
        movement.setUnitCost(unitCost);
        movement.setPerformedBy(performedBy);
        movement.setNotes(notes);
        
        // 総コストを計算
        if (unitCost != null && quantity != null) {
            movement.setTotalCost(unitCost.multiply(BigDecimal.valueOf(quantity)));
        }
        
        StockMovement savedMovement = movementRepository.save(movement);
        
        logger.info("在庫移動履歴を記録しました: ID=" + savedMovement.getId());
        return savedMovement;
    }
    
    /**
     * 予約履歴を記録する
     */
    public StockMovement recordReservation(InventoryItem inventoryItem, Integer quantity, UUID orderId) {
        return recordStockMovement(
            inventoryItem,
            MovementType.OUTBOUND,
            quantity,
            inventoryItem.getAvailableQuantity() + quantity,
            inventoryItem.getAvailableQuantity(),
            "在庫予約",
            null,
            orderId,
            null,
            null,
            null,
            "注文ID: " + orderId + " での在庫予約"
        );
    }
    
    /**
     * 予約確定履歴を記録する
     */
    public StockMovement recordConfirmation(InventoryItem inventoryItem, Integer quantity, UUID orderId) {
        return recordStockMovement(
            inventoryItem,
            MovementType.OUTBOUND,
            quantity,
            null,
            null,
            "予約確定",
            null,
            orderId,
            null,
            null,
            null,
            "注文ID: " + orderId + " の予約確定による出荷"
        );
    }
    
    /**
     * 予約キャンセル履歴を記録する
     */
    public StockMovement recordCancellation(InventoryItem inventoryItem, Integer quantity, 
                                          UUID orderId, String cancellationReason) {
        return recordStockMovement(
            inventoryItem,
            MovementType.RETURN,
            quantity,
            inventoryItem.getAvailableQuantity() - quantity,
            inventoryItem.getAvailableQuantity(),
            "予約キャンセル",
            null,
            orderId,
            null,
            null,
            null,
            "注文ID: " + orderId + " の予約キャンセル。理由: " + cancellationReason
        );
    }
    
    /**
     * 予約期限切れ履歴を記録する
     */
    public StockMovement recordExpiration(InventoryItem inventoryItem, Integer quantity, UUID orderId) {
        return recordStockMovement(
            inventoryItem,
            MovementType.RETURN,
            quantity,
            inventoryItem.getAvailableQuantity() - quantity,
            inventoryItem.getAvailableQuantity(),
            "予約期限切れ",
            null,
            orderId,
            null,
            null,
            null,
            "注文ID: " + orderId + " の予約期限切れによる在庫復元"
        );
    }
    
    /**
     * 損傷・紛失履歴を記録する
     */
    public StockMovement recordDamageOrTheft(InventoryItem inventoryItem, MovementType movementType, 
                                           Integer quantity, String reason, String performedBy) {
        if (movementType != MovementType.DAMAGE && movementType != MovementType.THEFT) {
            throw new IllegalArgumentException("移動タイプは DAMAGE または THEFT である必要があります");
        }
        
        return recordStockMovement(
            inventoryItem,
            movementType,
            quantity,
            inventoryItem.getAvailableQuantity() + quantity,
            inventoryItem.getAvailableQuantity(),
            reason,
            null,
            null,
            null,
            null,
            performedBy,
            movementType.getDescription() + "による在庫減少"
        );
    }
    
    /**
     * 入荷履歴を記録する
     */
    public StockMovement recordInbound(InventoryItem inventoryItem, Integer quantity, 
                                     UUID supplierId, String referenceNumber, 
                                     BigDecimal unitCost, String performedBy) {
        return recordStockMovement(
            inventoryItem,
            MovementType.INBOUND,
            quantity,
            inventoryItem.getAvailableQuantity() - quantity,
            inventoryItem.getAvailableQuantity(),
            "商品入荷",
            referenceNumber,
            null,
            supplierId,
            unitCost,
            performedBy,
            "参照番号: " + referenceNumber + " での商品入荷"
        );
    }
}
