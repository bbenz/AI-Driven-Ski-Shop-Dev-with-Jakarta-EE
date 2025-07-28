package com.skishop.cart.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * External DTO for Coupon Application Result from Coupon Discount Service
 */
public record CouponApplicationResultDto(
    @JsonProperty("applied")
    Boolean applied,
    
    @JsonProperty("discountAmount")
    BigDecimal discountAmount,
    
    @JsonProperty("finalTotal")
    BigDecimal finalTotal,
    
    @JsonProperty("couponName")
    String couponName,
    
    @JsonProperty("message")
    String message
) {}
