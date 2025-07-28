package com.skishop.cart.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO for cart item information
 */
public record CartItemDto(
    @JsonProperty("itemId")
    String itemId,
    
    @JsonProperty("cartId")
    String cartId,
    
    @JsonProperty("productId")
    String productId,
    
    @JsonProperty("sku")
    String sku,
    
    @JsonProperty("productName")
    String productName,
    
    @JsonProperty("productImageUrl")
    String productImageUrl,
    
    @JsonProperty("unitPrice")
    BigDecimal unitPrice,
    
    @JsonProperty("quantity")
    Integer quantity,
    
    @JsonProperty("totalPrice")
    BigDecimal totalPrice,
    
    @JsonProperty("options")
    Map<String, String> options,
    
    @JsonProperty("addedAt")
    LocalDateTime addedAt,
    
    @JsonProperty("updatedAt")
    LocalDateTime updatedAt
) {}
