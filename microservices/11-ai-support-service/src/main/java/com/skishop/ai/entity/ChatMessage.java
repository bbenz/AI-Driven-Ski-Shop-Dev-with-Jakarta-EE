package com.skishop.ai.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * チャットメッセージエンティティ
 * 
 * <p>会話セッション内の個別メッセージを管理するエンティティ</p>
 * 
 * @since 1.0.0
 */
@Entity
@Table(name = "chat_messages")
public class ChatMessage extends PanacheEntityBase {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_session_id", nullable = false)
    public ConversationSession conversationSession;
    
    @Column(name = "message_id", unique = true, nullable = false)
    public String messageId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "sender_type", nullable = false)
    public MessageSenderType senderType;
    
    @Column(name = "sender_id")
    public String senderId;
    
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    public String content;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false)
    public MessageType messageType;
    
    @Column(name = "metadata", columnDefinition = "JSONB")
    public String metadata;
    
    @Embedded
    public MessageAnalysis analysis;
    
    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt;
    
    @Column(name = "processed_at")
    public LocalDateTime processedAt;
    
    // デフォルトコンストラクタ
    public ChatMessage() {
        this.createdAt = LocalDateTime.now();
        this.messageType = MessageType.TEXT;
    }
    
    // コンストラクタ
    public ChatMessage(String messageId, MessageSenderType senderType, String content) {
        this();
        this.messageId = messageId;
        this.senderType = senderType;
        this.content = content;
    }
    
    public ChatMessage(String messageId, MessageSenderType senderType, String senderId, String content, MessageType messageType) {
        this();
        this.messageId = messageId;
        this.senderType = senderType;
        this.senderId = senderId;
        this.content = content;
        this.messageType = messageType;
    }
    
    // ビジネスロジック
    public boolean isFromUser() {
        return senderType == MessageSenderType.USER;
    }
    
    public boolean isFromBot() {
        return senderType == MessageSenderType.BOT;
    }
    
    public boolean isFromAgent() {
        return senderType == MessageSenderType.AGENT;
    }
    
    public void markAsProcessed() {
        this.processedAt = LocalDateTime.now();
    }
    
    // 静的ファインダーメソッド
    public static ChatMessage findByMessageId(String messageId) {
        return find("messageId", messageId).firstResult();
    }
    
    public static List<ChatMessage> findByConversationSessionId(UUID sessionId) {
        return find("conversationSession.id = ?1 order by createdAt asc", sessionId).list();
    }
    
    public static List<ChatMessage> findRecentBySenderType(MessageSenderType senderType, int limit) {
        return find("senderType = ?1 order by createdAt desc", senderType).page(0, limit).list();
    }
    
    public static List<ChatMessage> findUnprocessedMessages() {
        return find("processedAt is null and senderType = ?1", MessageSenderType.USER).list();
    }
}

/**
 * メッセージ送信者タイプ
 */
enum MessageSenderType {
    USER,      // ユーザー
    BOT,       // AIボット
    AGENT,     // 人間のエージェント
    SYSTEM     // システム
}

/**
 * メッセージタイプ
 */
enum MessageType {
    TEXT,
    IMAGE,
    FILE,
    PRODUCT_RECOMMENDATION,
    TECHNICAL_ADVICE,
    ORDER_INQUIRY,
    SYSTEM_NOTIFICATION
}

/**
 * メッセージ分析結果
 */
@Embeddable
class MessageAnalysis {
    
    @Column(name = "intent")
    public String intent;
    
    @Column(name = "confidence_score", precision = 5, scale = 4)
    public BigDecimal confidenceScore;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "sentiment")
    public SentimentType sentiment;
    
    @Column(name = "sentiment_score", precision = 5, scale = 4)
    public BigDecimal sentimentScore;
    
    @Column(name = "entities", columnDefinition = "JSONB")
    public String entities;
    
    @Column(name = "keywords", columnDefinition = "JSONB")
    public String keywords;
    
    @Column(name = "toxicity_score", precision = 5, scale = 4)
    public BigDecimal toxicityScore;
    
    @Column(name = "language_detected", length = 5)
    public String languageDetected;
    
    public MessageAnalysis() {}
    
    public MessageAnalysis(String intent, BigDecimal confidenceScore, SentimentType sentiment) {
        this.intent = intent;
        this.confidenceScore = confidenceScore;
        this.sentiment = sentiment;
    }
}

/**
 * 感情タイプ
 */
enum SentimentType {
    POSITIVE,
    NEGATIVE,
    NEUTRAL,
    MIXED
}
