package com.skishop.cart.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for applied coupon information
 */
public record AppliedCouponDto(
    @JsonProperty("id")
    String id,
    
    @JsonProperty("cartId")
    String cartId,
    
    @JsonProperty("couponCode")
    String couponCode,
    
    @JsonProperty("couponName")
    String couponName,
    
    @JsonProperty("discountType")
    String discountType,
    
    @JsonProperty("discountAmount")
    BigDecimal discountAmount,
    
    @JsonProperty("appliedAt")
    LocalDateTime appliedAt
) {}
