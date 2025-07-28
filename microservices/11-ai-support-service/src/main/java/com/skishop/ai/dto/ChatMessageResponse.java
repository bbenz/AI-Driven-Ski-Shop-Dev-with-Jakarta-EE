package com.skishop.ai.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * チャットメッセージレスポンスDTO
 * 
 * <p>AIアシスタントからのレスポンスを表現するRecord</p>
 * <p>Java 21のRecordクラスを活用してイミュータブルなデータ構造を実現</p>
 * 
 * @param messageId メッセージID
 * @param conversationId 会話ID
 * @param content レスポンス内容
 * @param intent 検出された意図
 * @param confidence 信頼度スコア
 * @param timestamp タイムスタンプ
 * @param context コンテキスト情報
 * 
 * @since 1.0.0
 */
public record ChatMessageResponse(
        String messageId,
        String conversationId,
        String content,
        String intent,
        BigDecimal confidence,
        LocalDateTime timestamp,
        Map<String, Object> context
) {
    /**
     * 基本的なレスポンスを作成
     */
    public static ChatMessageResponse basic(String messageId, String conversationId, String content) {
        return new ChatMessageResponse(
            messageId,
            conversationId,
            content,
            "GENERAL_INQUIRY",
            BigDecimal.valueOf(0.8),
            LocalDateTime.now(),
            null
        );
    }
    
    /**
     * 信頼度スコア付きレスポンスを作成
     */
    public static ChatMessageResponse withConfidence(String messageId, String conversationId, String content, String intent, double confidence) {
        return new ChatMessageResponse(
            messageId,
            conversationId,
            content,
            intent,
            BigDecimal.valueOf(confidence),
            LocalDateTime.now(),
            null
        );
    }
    
    /**
     * 完全なレスポンスを作成
     */
    public static ChatMessageResponse full(String messageId, String conversationId, String content, String intent, 
                                         BigDecimal confidence, Map<String, Object> context) {
        return new ChatMessageResponse(
            messageId,
            conversationId,
            content,
            intent,
            confidence,
            LocalDateTime.now(),
            context
        );
    }
    
    /**
     * エラーレスポンスを作成
     */
    public static ChatMessageResponse error(String messageId, String conversationId, String errorMessage) {
        return new ChatMessageResponse(
            messageId,
            conversationId,
            errorMessage,
            "ERROR",
            BigDecimal.valueOf(0.0),
            LocalDateTime.now(),
            Map.of("error", true, "errorType", "AI_SERVICE_ERROR")
        );
    }
    
    /**
     * フォールバックレスポンスを作成
     */
    public static ChatMessageResponse fallback(String messageId, String conversationId) {
        String fallbackMessage = """
                申し訳ございませんが、AIアシスタントが一時的にご利用いただけません。
                しばらく時間をおいてから再度お試しください。
                
                お急ぎの場合は以下からお問い合わせください：
                - 電話: 0120-XXX-XXX（平日 9:00-18:00）
                - メール: support@skishop.com
                """;
        
        return new ChatMessageResponse(
            messageId,
            conversationId,
            fallbackMessage,
            "FALLBACK",
            BigDecimal.valueOf(0.5),
            LocalDateTime.now(),
            Map.of("fallback", true, "serviceFallback", true)
        );
    }
}
