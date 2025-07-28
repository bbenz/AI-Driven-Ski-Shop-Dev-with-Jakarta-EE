package com.skishop.cart.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * External DTO for Inventory Reservation from Inventory Management Service
 */
public record InventoryReservationDto(
    @JsonProperty("reservationId")
    String reservationId,
    
    @JsonProperty("cartId")
    String cartId,
    
    @JsonProperty("status")
    String status,
    
    @JsonProperty("expiresAt")
    String expiresAt,
    
    @JsonProperty("totalReservedAmount")
    BigDecimal totalReservedAmount
) {}
