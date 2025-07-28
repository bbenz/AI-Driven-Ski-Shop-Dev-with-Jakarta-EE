package com.skishop.ai.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.time.Duration;

/**
 * 会話セッションエンティティ
 * 
 * <p>ユーザーとAIアシスタント間の会話セッションを管理するエンティティ</p>
 * 
 * @since 1.0.0
 */
@Entity
@Table(name = "conversation_sessions")
public class ConversationSession extends PanacheEntityBase {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;
    
    @Column(name = "session_id", unique = true, nullable = false)
    public String sessionId;
    
    @Column(name = "customer_id")
    public UUID customerId;
    
    @Column(name = "visitor_id")
    public String visitorId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false)
    public ConversationChannel channel;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    public ConversationStatus status;
    
    @Column(name = "language", length = 5)
    public String language = "ja";
    
    @Column(name = "user_agent")
    public String userAgent;
    
    @Column(name = "ip_address")
    public String ipAddress;
    
    @Column(name = "started_at", nullable = false)
    public LocalDateTime startedAt;
    
    @Column(name = "ended_at")
    public LocalDateTime endedAt;
    
    @Column(name = "last_activity_at")
    public LocalDateTime lastActivityAt;
    
    @Embedded
    public ConversationContext context;
    
    // 関連エンティティ
    @OneToMany(mappedBy = "conversationSession", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("createdAt ASC")
    public List<ChatMessage> messages = new ArrayList<>();
    
    @OneToMany(mappedBy = "conversationSession", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    public List<ConversationAnalysis> analyses = new ArrayList<>();
    
    // デフォルトコンストラクタ
    public ConversationSession() {
        this.startedAt = LocalDateTime.now();
        this.lastActivityAt = LocalDateTime.now();
        this.status = ConversationStatus.ACTIVE;
        this.channel = ConversationChannel.WEB_CHAT;
    }
    
    // コンストラクタ
    public ConversationSession(String sessionId, UUID customerId, String visitorId, ConversationChannel channel) {
        this();
        this.sessionId = sessionId;
        this.customerId = customerId;
        this.visitorId = visitorId;
        this.channel = channel;
    }
    
    // ビジネスロジック
    public void addMessage(ChatMessage message) {
        message.conversationSession = this;
        this.messages.add(message);
        this.lastActivityAt = LocalDateTime.now();
    }
    
    public void endConversation() {
        this.status = ConversationStatus.ENDED;
        this.endedAt = LocalDateTime.now();
    }
    
    public boolean isActive() {
        return status == ConversationStatus.ACTIVE;
    }
    
    public boolean isTimedOut(Duration timeout) {
        return lastActivityAt.plus(timeout).isBefore(LocalDateTime.now());
    }
    
    public long getMessageCount() {
        return messages.size();
    }
    
    public Duration getDuration() {
        var endTime = endedAt != null ? endedAt : LocalDateTime.now();
        return Duration.between(startedAt, endTime);
    }
    
    public List<ChatMessage> getRecentMessages(int count) {
        return messages.stream()
            .sorted((m1, m2) -> m2.createdAt.compareTo(m1.createdAt))
            .limit(count)
            .toList();
    }
    
    // 静的ファインダーメソッド
    public static ConversationSession findBySessionId(String sessionId) {
        return find("sessionId", sessionId).firstResult();
    }
    
    public static List<ConversationSession> findActiveByCustomerId(UUID customerId) {
        return find("customerId = ?1 and status = ?2", customerId, ConversationStatus.ACTIVE).list();
    }
    
    public static List<ConversationSession> findByCustomerIdOrderByStartedAt(UUID customerId) {
        return find("customerId = ?1 order by startedAt desc", customerId).list();
    }
    
    public static List<ConversationSession> findTimedOutSessions(Duration timeout) {
        var cutoffTime = LocalDateTime.now().minus(timeout);
        return find("status = ?1 and lastActivityAt < ?2", ConversationStatus.ACTIVE, cutoffTime).list();
    }
}

/**
 * 会話チャンネル
 */
enum ConversationChannel {
    WEB_CHAT,
    MOBILE_APP,
    API,
    ADMIN_CONSOLE,
    SUPPORT_PORTAL
}

/**
 * 会話ステータス
 */
enum ConversationStatus {
    ACTIVE,
    ENDED,
    PAUSED,
    ESCALATED,
    TIMED_OUT
}

/**
 * 会話コンテキスト
 */
@Embeddable
class ConversationContext {
    
    @Column(name = "current_intent")
    public String currentIntent;
    
    @Column(name = "user_profile", columnDefinition = "JSONB")
    public String userProfile;
    
    @Column(name = "conversation_metadata", columnDefinition = "JSONB")
    public String metadata;
    
    @Column(name = "preferences", columnDefinition = "JSONB")
    public String preferences;
    
    @Column(name = "escalation_reason")
    public String escalationReason;
    
    @Column(name = "satisfaction_score")
    public Double satisfactionScore;
    
    public ConversationContext() {}
    
    public ConversationContext(String currentIntent, String userProfile) {
        this.currentIntent = currentIntent;
        this.userProfile = userProfile;
    }
}
