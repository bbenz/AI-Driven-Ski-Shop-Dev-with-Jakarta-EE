package com.skishop.ai.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 会話分析エンティティ
 * 
 * <p>会話セッションの分析結果を管理するエンティティ</p>
 * 
 * @since 1.0.0
 */
@Entity
@Table(name = "conversation_analyses")
public class ConversationAnalysis extends PanacheEntityBase {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_session_id", nullable = false)
    public ConversationSession conversationSession;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "analysis_type", nullable = false)
    public AnalysisType analysisType;
    
    @Column(name = "intent", length = 100)
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
    
    @Column(name = "satisfaction_score", precision = 3, scale = 2)
    public BigDecimal satisfactionScore;
    
    @Column(name = "resolution_status")
    public String resolutionStatus;
    
    @Column(name = "escalation_risk", precision = 3, scale = 2)
    public BigDecimal escalationRisk;
    
    @Column(name = "topic_categories", columnDefinition = "JSONB")
    public String topicCategories;
    
    @Column(name = "complexity_score", precision = 3, scale = 2)
    public BigDecimal complexityScore;
    
    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt;
    
    // デフォルトコンストラクタ
    public ConversationAnalysis() {
        this.createdAt = LocalDateTime.now();
    }
    
    // コンストラクタ
    public ConversationAnalysis(ConversationSession conversationSession, AnalysisType analysisType, String intent) {
        this();
        this.conversationSession = conversationSession;
        this.analysisType = analysisType;
        this.intent = intent;
    }
    
    // 静的ファインダーメソッド
    public static List<ConversationAnalysis> findByConversationSessionId(UUID sessionId) {
        return find("conversationSession.id = ?1 order by createdAt desc", sessionId).list();
    }
    
    public static List<ConversationAnalysis> findByAnalysisType(AnalysisType analysisType) {
        return find("analysisType = ?1 order by createdAt desc", analysisType).list();
    }
    
    public static List<ConversationAnalysis> findHighRiskSessions(BigDecimal riskThreshold) {
        return find("escalationRisk >= ?1 order by escalationRisk desc", riskThreshold).list();
    }
    
    public static List<ConversationAnalysis> findByIntentAndTimeRange(String intent, LocalDateTime startTime, LocalDateTime endTime) {
        return find("intent = ?1 and createdAt between ?2 and ?3 order by createdAt desc", intent, startTime, endTime).list();
    }
}

/**
 * 分析タイプ
 */
enum AnalysisType {
    INTENT_ANALYSIS,      // 意図分析
    SENTIMENT_ANALYSIS,   // 感情分析
    ENTITY_EXTRACTION,    // エンティティ抽出
    TOPIC_MODELING,       // トピックモデリング
    SATISFACTION_ANALYSIS, // 満足度分析
    ESCALATION_PREDICTION, // エスカレーション予測
    COMPLEXITY_ASSESSMENT  // 複雑度評価
}
