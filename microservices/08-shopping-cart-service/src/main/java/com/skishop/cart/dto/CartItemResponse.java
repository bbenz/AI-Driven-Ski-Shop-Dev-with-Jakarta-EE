package com.skishop.cart.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CartItemResponse(
    @NotNull String itemId,
    @NotNull UUID productId,
    @NotNull String sku,
    @NotNull String productName,
    String productImageUrl,
    @NotNull @Positive BigDecimal unitPrice,
    @NotNull @Positive Integer quantity,
    @NotNull BigDecimal totalPrice,
    @NotNull LocalDateTime addedAt,
    LocalDateTime updatedAt
) {
    public static CartItemResponse from(com.skishop.cart.entity.CartItem item) {
        return new CartItemResponse(
            item.id.toString(),
            item.productId,
            item.sku,
            item.productName,
            item.productImageUrl,
            item.unitPrice,
            item.quantity,
            item.totalPrice,
            item.addedAt,
            item.updatedAt
        );
    }
}
