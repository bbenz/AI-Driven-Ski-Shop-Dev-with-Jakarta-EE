package com.skishop.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Map;

/**
 * チャットメッセージリクエストDTO
 * 
 * <p>クライアントからのチャットメッセージリクエストを表現するRecord</p>
 * <p>Java 21のRecordクラスを活用してイミュータブルなデータ構造を実現</p>
 * 
 * @param userId ユーザーID（必須）
 * @param content メッセージ内容（必須、最大8000文字）
 * @param conversationId 会話ID（任意）
 * @param sessionId セッションID（任意）
 * @param context コンテキスト情報（任意）
 * 
 * @since 1.0.0
 */
public record ChatMessageRequest(
        @NotBlank(message = "ユーザーIDは必須です")
        @Size(max = 100, message = "ユーザーIDは100文字以内で入力してください")
        String userId,
        
        @NotBlank(message = "メッセージ内容は必須です")
        @Size(max = 8000, message = "メッセージは8000文字以内で入力してください")
        String content,
        
        @Size(max = 100, message = "会話IDは100文字以内で入力してください")
        String conversationId,
        
        @Size(max = 100, message = "セッションIDは100文字以内で入力してください")
        String sessionId,
        
        Map<String, Object> context
) {
    /**
     * デフォルト値を設定したコンストラクタ
     */
    public ChatMessageRequest(String userId, String content) {
        this(userId, content, null, null, null);
    }
    
    public ChatMessageRequest(String userId, String content, String conversationId) {
        this(userId, content, conversationId, null, null);
    }
    
    /**
     * コンテキストから強制的な意図を取得
     */
    public String getForcedIntent() {
        if (context != null && context.containsKey("forcedIntent")) {
            return (String) context.get("forcedIntent");
        }
        return null;
    }
    
    /**
     * チャンネル情報を取得
     */
    public String getChannel() {
        if (context != null && context.containsKey("channel")) {
            return (String) context.get("channel");
        }
        return "web-chat";
    }
    
    /**
     * ユーザーエージェント情報を取得
     */
    public String getUserAgent() {
        if (context != null && context.containsKey("userAgent")) {
            return (String) context.get("userAgent");
        }
        return null;
    }
}
