package com.skiresort.inventory.dto;

import com.skiresort.inventory.model.InventoryStatus;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 在庫アイテム応答DTO
 */
public record InventoryItemResponse(
    UUID id,
    UUID productId,
    String sku,
    UUID warehouseId,
    Integer availableQuantity,
    Integer reservedQuantity,
    Integer incomingQuantity,
    Integer totalQuantity,
    Integer minimumStockLevel,
    Integer maximumStockLevel,
    Integer reorderPoint,
    Integer reorderQuantity,
    InventoryStatus status,
    boolean isLowStock,
    boolean isOutOfStock,
    LocalDateTime lastUpdatedAt,
    LocalDateTime createdAt
) {
    public static InventoryItemResponse from(com.skiresort.inventory.model.InventoryItem item) {
        return new InventoryItemResponse(
            item.getId(),
            item.getProductId(),
            item.getSku(),
            item.getWarehouseId(),
            item.getAvailableQuantity(),
            item.getReservedQuantity(),
            item.getIncomingQuantity(),
            item.getTotalQuantity(),
            item.getMinimumStockLevel(),
            item.getMaximumStockLevel(),
            item.getReorderPoint(),
            item.getReorderQuantity(),
            item.getStatus(),
            item.isLowStock(),
            item.isOutOfStock(),
            item.getLastUpdatedAt(),
            item.getCreatedAt()
        );
    }
}
