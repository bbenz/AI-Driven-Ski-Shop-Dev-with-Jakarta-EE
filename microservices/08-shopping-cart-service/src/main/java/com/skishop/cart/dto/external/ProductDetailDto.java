package com.skishop.cart.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.Map;

/**
 * External DTO for Product Catalog Service integration
 */
public record ProductDetailDto(
    @JsonProperty("productId")
    String productId,
    
    @JsonProperty("sku")
    String sku,
    
    @JsonProperty("name")
    String name,
    
    @JsonProperty("description")
    String description,
    
    @JsonProperty("price")
    BigDecimal price,
    
    @JsonProperty("currency")
    String currency,
    
    @JsonProperty("category")
    String category,
    
    @JsonProperty("imageUrl")
    String imageUrl,
    
    @JsonProperty("inStock")
    Boolean inStock,
    
    @JsonProperty("stockQuantity")
    Integer stockQuantity,
    
    @JsonProperty("attributes")
    Map<String, String> attributes,
    
    @JsonProperty("active")
    Boolean active
) {}
