package com.skishop.cart.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * External DTO for Inventory Status from Inventory Management Service
 */
public record InventoryStatusDto(
    @JsonProperty("productId")
    String productId,
    
    @JsonProperty("availableQuantity")
    Integer availableQuantity,
    
    @JsonProperty("reservedQuantity")
    Integer reservedQuantity,
    
    @JsonProperty("inStock")
    Boolean inStock,
    
    @JsonProperty("lowStockThreshold")
    Integer lowStockThreshold,
    
    @JsonProperty("lastUpdated")
    String lastUpdated
) {}
