package com.skishop.ai.dto;

import java.time.LocalDateTime;

/**
 * 会話履歴レスポンスDTO
 */
public record ConversationHistoryResponse(
        String conversationId,
        String userId,
        LocalDateTime startedAt,
        LocalDateTime lastActivityAt,
        String status,
        java.util.List<MessageHistoryItem> messages,
        long totalMessages
) {
    
    public static ConversationHistoryResponse create(String conversationId, String userId, 
                                                   java.util.List<MessageHistoryItem> messages) {
        return new ConversationHistoryResponse(
            conversationId,
            userId,
            LocalDateTime.now().minusMinutes(30),
            LocalDateTime.now(),
            "ACTIVE",
            messages,
            messages.size()
        );
    }
}
