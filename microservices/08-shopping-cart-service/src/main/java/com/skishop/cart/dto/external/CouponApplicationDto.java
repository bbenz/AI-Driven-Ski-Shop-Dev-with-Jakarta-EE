package com.skishop.cart.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * External DTO for Coupon Application to Coupon Discount Service
 */
public record CouponApplicationDto(
    @JsonProperty("couponCode")
    String couponCode,
    
    @JsonProperty("cartId")
    String cartId,
    
    @JsonProperty("customerId")
    String customerId,
    
    @JsonProperty("subtotal")
    BigDecimal subtotal
) {}
