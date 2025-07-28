package com.skishop.cart.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * External DTO for Inventory Reservation Request to Inventory Management Service
 */
public record InventoryReservationRequestDto(
    @JsonProperty("cartId")
    String cartId,
    
    @JsonProperty("items")
    java.util.List<InventoryItemRequestDto> items
) {}
