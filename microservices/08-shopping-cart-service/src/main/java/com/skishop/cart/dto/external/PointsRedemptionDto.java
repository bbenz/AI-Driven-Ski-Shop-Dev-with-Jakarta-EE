package com.skishop.cart.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * External DTO for Points Redemption from Loyalty Points Service
 */
public record PointsRedemptionDto(
    @JsonProperty("customerId")
    String customerId,
    
    @JsonProperty("pointsToRedeem")
    BigDecimal pointsToRedeem,
    
    @JsonProperty("cashValue")
    BigDecimal cashValue,
    
    @JsonProperty("redeemed")
    Boolean redeemed,
    
    @JsonProperty("transactionId")
    String transactionId
) {}
