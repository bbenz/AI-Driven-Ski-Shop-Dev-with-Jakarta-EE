package com.skishop.cart.dto;

import jakarta.validation.constraints.NotNull;

public record CheckoutCartRequest(
    @NotNull String deliveryAddress,
    @NotNull String paymentMethod,
    String specialInstructions
) {}
