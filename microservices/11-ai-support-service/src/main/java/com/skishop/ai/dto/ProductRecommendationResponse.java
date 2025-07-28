package com.skishop.ai.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品推薦レスポンスDTO
 */
public record ProductRecommendationResponse(
        String messageId,
        String conversationId,
        String recommendations,
        java.util.List<RecommendedProduct> products,
        String reasoning,
        BigDecimal confidence,
        LocalDateTime timestamp
) {
    
    public static ProductRecommendationResponse create(String messageId, String conversationId, 
                                                     String recommendations, java.util.List<RecommendedProduct> products) {
        return new ProductRecommendationResponse(
            messageId,
            conversationId,
            recommendations,
            products,
            "AI analysis based on user requirements",
            BigDecimal.valueOf(0.85),
            LocalDateTime.now()
        );
    }
}
