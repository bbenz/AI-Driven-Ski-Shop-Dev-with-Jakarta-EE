package com.skishop.cart.dto;

import com.skishop.cart.entity.ShoppingCart;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CartTotalResponse(
    @NotNull BigDecimal subtotalAmount,
    @NotNull BigDecimal taxAmount,
    @NotNull BigDecimal shippingAmount,
    @NotNull BigDecimal discountAmount,
    @NotNull BigDecimal totalAmount,
    @NotNull String currency
) {
    public static CartTotalResponse from(ShoppingCart cart) {
        return new CartTotalResponse(
            cart.subtotalAmount,
            cart.taxAmount,
            cart.shippingAmount,
            cart.discountAmount,
            cart.totalAmount,
            cart.currency
        );
    }
}
