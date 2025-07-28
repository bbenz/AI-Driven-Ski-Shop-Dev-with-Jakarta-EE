package com.skiresort.inventory.dto;

import com.skiresort.inventory.model.MovementType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * 在庫更新リクエストDTO
 */
public record UpdateStockRequest(
    @NotBlank(message = "SKUは必須です")
    String sku,
    
    @NotNull(message = "移動タイプは必須です")
    MovementType movementType,
    
    @NotNull(message = "数量は必須です")
    @Min(value = 1, message = "数量は1以上である必要があります")
    Integer quantity,
    
    @Size(max = 500, message = "理由は500文字以内で入力してください")
    String reason,
    
    @Size(max = 100, message = "参照番号は100文字以内で入力してください")
    String referenceNumber,
    
    UUID orderId,
    
    UUID supplierId,
    
    @DecimalMin(value = "0.0", inclusive = false, message = "単価は0より大きい値である必要があります")
    @Digits(integer = 8, fraction = 2, message = "単価は小数点以下2桁まで、整数部8桁まで入力可能です")
    BigDecimal unitCost,
    
    @Size(max = 100, message = "実行者は100文字以内で入力してください")
    String performedBy,
    
    @Size(max = 1000, message = "備考は1000文字以内で入力してください")
    String notes
) {}
