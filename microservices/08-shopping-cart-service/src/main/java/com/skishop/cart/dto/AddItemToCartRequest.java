package com.skishop.cart.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public record AddItemToCartRequest(
    @NotNull UUID productId,
    @NotBlank @Size(max = 100) String sku,
    @NotBlank @Size(max = 255) String productName,
    String productImageUrl,
    @NotNull @Positive BigDecimal unitPrice,
    @NotNull @Positive Integer quantity,
    Map<String, String> options
) {}
