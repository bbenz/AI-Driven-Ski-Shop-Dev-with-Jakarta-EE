package com.skishop.ai.dto;

import java.math.BigDecimal;

/**
 * 推薦商品情報
 */
public record RecommendedProduct(
        String productId,
        String name,
        String category,
        BigDecimal price,
        String skillLevel,
        java.util.List<String> features,
        BigDecimal recommendationScore
) {}
