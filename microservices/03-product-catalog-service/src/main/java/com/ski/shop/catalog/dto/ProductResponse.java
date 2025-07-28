package com.ski.shop.catalog.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * 商品レスポンスDTO
 */
public record ProductResponse(
        UUID id,
        String sku,
        String name,
        String description,
        String shortDescription,
        CategorySummaryResponse category,
        BrandSummaryResponse brand,
        ProductSpecificationResponse specification,
        BigDecimal basePrice,
        BigDecimal salePrice,
        BigDecimal currentPrice,
        boolean onSale,
        Integer discountPercentage,
        ProductStatusResponse status,
        Set<String> tags,
        List<ProductImageResponse> images,
        List<ProductVariantResponse> variants,
        Long salesCount,
        Long viewCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
