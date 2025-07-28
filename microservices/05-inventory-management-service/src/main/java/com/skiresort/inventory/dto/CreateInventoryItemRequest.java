package com.skiresort.inventory.dto;

import com.skiresort.inventory.model.InventoryStatus;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 在庫アイテム作成リクエストDTO
 */
public record CreateInventoryItemRequest(
    @NotNull(message = "商品IDは必須です")
    UUID productId,
    
    @NotBlank(message = "SKUは必須です")
    @Size(max = 100, message = "SKUは100文字以内で入力してください")
    String sku,
    
    @NotNull(message = "倉庫IDは必須です")
    UUID warehouseId,
    
    @NotNull(message = "利用可能数量は必須です")
    @Min(value = 0, message = "利用可能数量は0以上である必要があります")
    Integer availableQuantity,
    
    @NotNull(message = "最小在庫レベルは必須です")
    @Min(value = 0, message = "最小在庫レベルは0以上である必要があります")
    Integer minimumStockLevel,
    
    @NotNull(message = "最大在庫レベルは必須です")
    @Min(value = 1, message = "最大在庫レベルは1以上である必要があります")
    Integer maximumStockLevel,
    
    @NotNull(message = "再注文ポイントは必須です")
    @Min(value = 0, message = "再注文ポイントは0以上である必要があります")
    Integer reorderPoint,
    
    @NotNull(message = "再注文数量は必須です")
    @Min(value = 1, message = "再注文数量は1以上である必要があります")
    Integer reorderQuantity,
    
    InventoryStatus status
) {
    public CreateInventoryItemRequest {
        if (status == null) {
            status = InventoryStatus.ACTIVE;
        }
        
        // ビジネス検証
        if (maximumStockLevel <= minimumStockLevel) {
            throw new IllegalArgumentException("最大在庫レベルは最小在庫レベルより大きい必要があります");
        }
        
        if (reorderPoint > maximumStockLevel) {
            throw new IllegalArgumentException("再注文ポイントは最大在庫レベル以下である必要があります");
        }
        
        if (availableQuantity > maximumStockLevel) {
            throw new IllegalArgumentException("利用可能数量は最大在庫レベル以下である必要があります");
        }
    }
}
