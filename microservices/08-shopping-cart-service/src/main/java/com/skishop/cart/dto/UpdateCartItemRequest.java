package com.skishop.cart.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdateCartItemRequest(
    @NotNull @Positive Integer quantity
) {}
