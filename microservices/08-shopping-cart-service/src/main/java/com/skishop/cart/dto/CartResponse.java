package com.skishop.cart.dto;

import com.skishop.cart.entity.ShoppingCart;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CartResponse(
    @NotNull String cartId,
    UUID customerId,
    String sessionId,
    @NotNull List<CartItemResponse> items,
    @NotNull CartTotalResponse totals,
    @NotNull String status,
    int itemCount,
    @NotNull LocalDateTime createdAt,
    LocalDateTime updatedAt,
    LocalDateTime expiresAt
) {
    public static CartResponse from(ShoppingCart cart) {
        return new CartResponse(
            cart.cartId,
            cart.customerId,
            cart.sessionId,
            cart.items.stream()
                .map(CartItemResponse::from)
                .toList(),
            CartTotalResponse.from(cart),
            cart.status.name(),
            cart.getItemCount(),
            cart.createdAt,
            cart.updatedAt,
            cart.expiresAt
        );
    }
}
