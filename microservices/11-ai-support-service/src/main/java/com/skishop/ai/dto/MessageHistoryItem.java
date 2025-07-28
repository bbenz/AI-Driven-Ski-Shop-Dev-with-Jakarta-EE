package com.skishop.ai.dto;

import java.time.LocalDateTime;

/**
 * メッセージ履歴項目
 */
public record MessageHistoryItem(
        String messageId,
        String senderType,
        String content,
        String intent,
        LocalDateTime timestamp
) {}
