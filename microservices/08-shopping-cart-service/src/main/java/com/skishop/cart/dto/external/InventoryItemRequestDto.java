package com.skishop.cart.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * External DTO for Inventory Item Request to Inventory Management Service
 */
public record InventoryItemRequestDto(
    @JsonProperty("productId")
    String productId,
    
    @JsonProperty("quantity")
    Integer quantity
) {}
