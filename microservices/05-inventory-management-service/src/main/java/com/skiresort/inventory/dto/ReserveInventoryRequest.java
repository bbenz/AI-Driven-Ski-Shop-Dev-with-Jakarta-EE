package com.skiresort.inventory.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 在庫予約リクエストDTO
 */
public record ReserveInventoryRequest(
    @NotBlank(message = "SKUは必須です")
    String sku,
    
    @NotNull(message = "数量は必須です")
    @Min(value = 1, message = "数量は1以上である必要があります")
    Integer quantity,
    
    @NotNull(message = "注文IDは必須です")
    UUID orderId,
    
    @NotNull(message = "顧客IDは必須です")
    UUID customerId,
    
    @Future(message = "有効期限は未来の日時である必要があります")
    LocalDateTime expiresAt
) {
    public ReserveInventoryRequest {
        // デフォルトの有効期限を30分後に設定
        if (expiresAt == null) {
            expiresAt = LocalDateTime.now().plusMinutes(30);
        }
    }
}
