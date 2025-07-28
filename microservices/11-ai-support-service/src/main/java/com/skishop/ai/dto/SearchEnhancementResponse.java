package com.skishop.ai.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 検索強化レスポンスDTO
 */
public record SearchEnhancementResponse(
        String originalQuery,
        String enhancedQuery,
        java.util.List<String> suggestions,
        java.util.List<String> relatedTerms,
        BigDecimal confidence,
        LocalDateTime timestamp
) {
    
    public static SearchEnhancementResponse create(String originalQuery, String enhancedQuery) {
        return new SearchEnhancementResponse(
            originalQuery,
            enhancedQuery,
            java.util.List.of(),
            java.util.List.of(),
            BigDecimal.valueOf(0.8),
            LocalDateTime.now()
        );
    }
}
