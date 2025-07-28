package com.skishop.cart.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * External DTO for Coupon Validation from Coupon Discount Service
 */
public record CouponValidationDto(
    @JsonProperty("couponCode")
    String couponCode,
    
    @JsonProperty("valid")
    Boolean valid,
    
    @JsonProperty("discountType")
    String discountType,
    
    @JsonProperty("discountValue")
    BigDecimal discountValue,
    
    @JsonProperty("minimumAmount")
    BigDecimal minimumAmount,
    
    @JsonProperty("maximumDiscount")
    BigDecimal maximumDiscount,
    
    @JsonProperty("description")
    String description,
    
    @JsonProperty("errorMessage")
    String errorMessage
) {}
