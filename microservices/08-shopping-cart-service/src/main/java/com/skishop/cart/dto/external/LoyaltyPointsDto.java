package com.skishop.cart.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * External DTO for Loyalty Points from Loyalty Points Service
 */
public record LoyaltyPointsDto(
    @JsonProperty("customerId")
    String customerId,
    
    @JsonProperty("currentPoints")
    BigDecimal currentPoints,
    
    @JsonProperty("availablePoints")
    BigDecimal availablePoints,
    
    @JsonProperty("tierLevel")
    String tierLevel,
    
    @JsonProperty("pointsValue")
    BigDecimal pointsValue
) {}
