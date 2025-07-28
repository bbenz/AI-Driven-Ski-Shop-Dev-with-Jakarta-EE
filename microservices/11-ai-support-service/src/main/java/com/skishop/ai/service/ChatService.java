package com.skishop.ai.service;

import com.skishop.ai.dto.ChatMessageRequest;
import com.skishop.ai.dto.ChatMessageResponse;
import com.skishop.ai.exception.AiServiceException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Chat Service Implementation
 * 
 * <p>LangChain4j 1.1.0 AIサービスを使用したチャットボット機能</p>
 * <p>統合エラーハンドリング、リトライ、フォールバック機能</p>
 * <p>最新のJava 21機能を活用</p>
 * 
 * @since 1.0.0
 */
@ApplicationScoped
public class ChatService {
    
    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);
    
    private final CustomerSupportAssistant customerSupportAssistant;
    private final ProductRecommendationAssistant recommendationAssistant;
    private final EnhancedAiServiceExecutor aiServiceExecutor;
    
    /**
     * コンストラクタ
     * 
     * @param customerSupportAssistant カスタマーサポートアシスタント
     * @param recommendationAssistant 商品推薦アシスタント
     * @param aiServiceExecutor AIサービス実行エンジン
     */
    @Inject
    public ChatService(
            CustomerSupportAssistant customerSupportAssistant,
            ProductRecommendationAssistant recommendationAssistant,
            EnhancedAiServiceExecutor aiServiceExecutor) {
        this.customerSupportAssistant = customerSupportAssistant;
        this.recommendationAssistant = recommendationAssistant;
        this.aiServiceExecutor = aiServiceExecutor;
    }
    
    /**
     * Java 21のText Blocks機能を使用したモック商品カタログ（定数として定義）
     */
    private static final String MOCK_PRODUCT_CATALOG = """
            [
              {
                "productId": "ski-001",
                "name": "Rossignol Experience 88 Ti",
                "category": "All-Mountain Ski",
                "price": 89000,
                "skillLevel": "Intermediate-Advanced",
                "features": ["Titanium reinforced", "Stability", "Carving performance"]
              },
              {
                "productId": "ski-002", 
                "name": "Salomon QST 92",
                "category": "Freeride Ski",
                "price": 76000,
                "skillLevel": "Intermediate",
                "features": ["Lightweight", "Powder compatible", "All-round"]
              }
            ]
            """;
    
    /**
     * Java 21のSealed Interfacesを使用した意図分類
     */
    public sealed interface Intent 
        permits Intent.ProductRecommendation, Intent.TechnicalAdvice, 
                Intent.OrderSupport, Intent.GeneralInquiry {
        
        record ProductRecommendation() implements Intent {}
        record TechnicalAdvice() implements Intent {}
        record OrderSupport() implements Intent {}
        record GeneralInquiry() implements Intent {}
        
        /**
         * 文字列から意図を検出
         */
        static Intent detectFromMessage(String message) {
            var lowerMessage = message.toLowerCase();
            
            if (lowerMessage.contains("recommend") || lowerMessage.contains("suggestion") || 
                lowerMessage.contains("choose") || lowerMessage.contains("which")) {
                return new ProductRecommendation();
            } else if (lowerMessage.contains("technique") || lowerMessage.contains("skill") || 
                      lowerMessage.contains("tips") || lowerMessage.contains("improvement")) {
                return new TechnicalAdvice();
            } else if (lowerMessage.contains("order") || lowerMessage.contains("shipping") || 
                      lowerMessage.contains("return") || lowerMessage.contains("exchange")) {
                return new OrderSupport();
            } else {
                return new GeneralInquiry();
            }
        }
        
        /**
         * 意図を文字列として取得
         */
        default String asString() {
            return switch (this) {
                case ProductRecommendation productRec -> "PRODUCT_RECOMMENDATION";
                case TechnicalAdvice techAdvice -> "TECHNICAL_ADVICE";
                case OrderSupport orderSupport -> "ORDER_SUPPORT";
                case GeneralInquiry generalInq -> "GENERAL_INQUIRY";
            };
        }
        
        /**
         * アクションが必要かどうかを判定
         */
        default boolean requiresAction() {
            return switch (this) {
                case ProductRecommendation productRec -> true;
                case OrderSupport orderSupport -> true;
                case TechnicalAdvice techAdvice -> false;
                case GeneralInquiry generalInq -> false;
            };
        }
        
        /**
         * アクションタイプを取得
         */
        default String getActionType() {
            return switch (this) {
                case ProductRecommendation productRec -> "SHOW_PRODUCTS";
                case OrderSupport orderSupport -> "REDIRECT_TO_SUPPORT";
                case TechnicalAdvice techAdvice -> "NONE";
                case GeneralInquiry generalInq -> "NONE";
            };
        }
    }
    
    /**
     * チャットメッセージ処理
     */
    public ChatMessageResponse processMessage(ChatMessageRequest request) {
        logger.info("Processing chat message for user: {}", request.userId());
        
        try {
            // Java 21のvar型推論とパターンマッチングを活用
            var userMessage = request.content();
            
            // コンテキストから強制的な意図を確認
            var forcedIntent = request.getForcedIntent();
            var intent = forcedIntent != null ? 
                mapForcedIntent(forcedIntent) : 
                Intent.detectFromMessage(userMessage);
            
            // 意図に基づいて適切なAIサービスを呼び出し（エラーハンドリングとリトライ付き）
            var aiResponse = generateResponseWithRetry(userMessage, intent, request);
            
            // Recordのファクトリメソッドを使用してレスポンスを構築・返却
            return ChatMessageResponse.withConfidence(
                UUID.randomUUID().toString(),
                request.conversationId(),
                aiResponse,
                intent.asString(),
                0.9 // 実際は分析結果に基づく
            );
            
        } catch (AiServiceException e) {
            // AIサービス固有のエラーはそのまま再スロー
            logger.error("AI service error processing chat message: {}", e.getMessage());
            throw e;
            
        } catch (Exception e) {
            logger.error("Unexpected error processing chat message: ", e);
            throw AiServiceException.internalError("An error occurred while processing the chat message", e);
        }
    }
    
    /**
     * 意図に基づくAIレスポンス生成（Java 21のSwitch Expressionsを活用）
     */
    private String generateResponseWithRetry(String userMessage, Intent intent, ChatMessageRequest request) {
        return switch (intent) {
            case Intent.ProductRecommendation productRec -> 
                generateProductRecommendationWithRetry(userMessage, request);
                
            case Intent.TechnicalAdvice techAdvice -> 
                aiServiceExecutor.executeWithRetry(
                    (text, context) -> customerSupportAssistant.provideTechnicalAdvice(text),
                    userMessage,
                    intent.asString()
                );
                
            case Intent.OrderSupport orderSupport -> 
                aiServiceExecutor.executeWithRetry(
                    (text, context) -> customerSupportAssistant.chat(text),
                    userMessage,
                    intent.asString()
                );
                
            case Intent.GeneralInquiry generalInq -> 
                aiServiceExecutor.executeWithRetry(
                    (text, context) -> customerSupportAssistant.chat(text),
                    userMessage,
                    intent.asString()
                );
        };
    }
    
    /**
     * 商品推薦生成（エラーハンドリングとリトライ付き）
     */
    private String generateProductRecommendationWithRetry(String userMessage, ChatMessageRequest request) {
        // ユーザープロフィールを構築
        var userProfile = buildUserProfile(request);
        
        // AIを使用して推薦生成（エラーハンドリングとリトライ付き）
        return aiServiceExecutor.executeWithRetry(
            (text, context) -> recommendationAssistant.generateRecommendations(text, userProfile, MOCK_PRODUCT_CATALOG),
            userMessage,
            "PRODUCT_RECOMMENDATION"
        );
    }
    
    /**
     * ユーザープロフィール構築（Java 21のText Blocksと文字列フォーマット）
     */
    private String buildUserProfile(ChatMessageRequest request) {
        // 実際の実装では、ユーザー管理サービスから取得
        return """
            {
              "userId": "%s",
              "skillLevel": "Intermediate",
              "preferences": {
                "budget": "50000-100000",
                "brands": ["Rossignol", "Salomon"],
                "usage": "Leisure"
              },
              "physicalAttributes": {
                "height": "170cm",
                "weight": "65kg"
              }
            }
            """.formatted(request.userId());
    }
    
    /**
     * 強制的な意図のマッピング
     */
    private Intent mapForcedIntent(String forcedIntent) {
        return switch (forcedIntent) {
            case "PRODUCT_RECOMMENDATION" -> new Intent.ProductRecommendation();
            case "TECHNICAL_ADVICE" -> new Intent.TechnicalAdvice();
            case "ORDER_SUPPORT" -> new Intent.OrderSupport();
            default -> new Intent.GeneralInquiry();
        };
    }
}
