package com.skishop.cart.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * External DTO for Inventory Availability from Inventory Management Service
 */
public record InventoryAvailabilityDto(
    @JsonProperty("productId")
    String productId,
    
    @JsonProperty("available")
    Boolean available,
    
    @JsonProperty("availableQuantity")
    Integer availableQuantity
) {}
