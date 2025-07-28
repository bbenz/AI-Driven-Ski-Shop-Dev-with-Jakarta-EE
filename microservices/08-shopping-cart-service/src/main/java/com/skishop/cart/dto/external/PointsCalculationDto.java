package com.skishop.cart.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * External DTO for Points Calculation from Loyalty Points Service
 */
public record PointsCalculationDto(
    @JsonProperty("cartTotal")
    BigDecimal cartTotal,
    
    @JsonProperty("earnedPoints")
    BigDecimal earnedPoints,
    
    @JsonProperty("pointsMultiplier")
    BigDecimal pointsMultiplier,
    
    @JsonProperty("bonusPoints")
    BigDecimal bonusPoints
) {}
