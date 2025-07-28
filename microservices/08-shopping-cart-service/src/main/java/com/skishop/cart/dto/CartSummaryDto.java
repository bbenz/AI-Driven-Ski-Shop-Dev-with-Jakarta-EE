package com.skishop.cart.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for cart summary information
 */
public record CartSummaryDto(
    @JsonProperty("cartId")
    String cartId,
    
    @JsonProperty("customerId")
    String customerId,
    
    @JsonProperty("sessionId")
    String sessionId,
    
    @JsonProperty("status")
    String status,
    
    @JsonProperty("items")
    List<CartItemDto> items,
    
    @JsonProperty("itemCount")
    Integer itemCount,
    
    @JsonProperty("subtotal")
    BigDecimal subtotal,
    
    @JsonProperty("taxAmount")
    BigDecimal taxAmount,
    
    @JsonProperty("shippingAmount")
    BigDecimal shippingAmount,
    
    @JsonProperty("discountAmount")
    BigDecimal discountAmount,
    
    @JsonProperty("totalAmount")
    BigDecimal totalAmount,
    
    @JsonProperty("currency")
    String currency,
    
    @JsonProperty("appliedCoupons")
    List<AppliedCouponDto> appliedCoupons,
    
    @JsonProperty("createdAt")
    LocalDateTime createdAt,
    
    @JsonProperty("updatedAt")
    LocalDateTime updatedAt,
    
    @JsonProperty("expiresAt")
    LocalDateTime expiresAt
) {}
