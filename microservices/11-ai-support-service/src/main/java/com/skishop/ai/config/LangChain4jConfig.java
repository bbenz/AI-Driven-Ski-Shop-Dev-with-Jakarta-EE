package com.skishop.ai.config;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.azure.AzureOpenAiChatModel;
import dev.langchain4j.model.azure.AzureOpenAiEmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
import com.skishop.ai.service.CustomerSupportAssistant;
import com.skishop.ai.service.ProductRecommendationAssistant;
import com.skishop.ai.service.SearchEnhancementAssistant;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * LangChain4j 1.1.0 Configuration for Azure OpenAI
 * 
 * <p>この設定クラスは最新のLangChain4j 1.1.0 APIを使用してAzure OpenAIへの接続を提供します。</p>
 * 
 * <h3>Azure OpenAI Service ガイドライン:</h3>
 * <ul>
 *   <li>API Key認証を使用（本番環境ではManaged Identityを推奨）</li>
 *   <li>serviceVersionパラメータを使用（apiVersionから変更）</li>
 *   <li>エラーハンドリングとリトライ機能を含める</li>
 *   <li>セキュアな設定管理</li>
 * </ul>
 * 
 * @since 1.0.0
 */
@ApplicationScoped
public class LangChain4jConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(LangChain4jConfig.class);

    @ConfigProperty(name = "azure.openai.api-key")
    String apiKey;

    @ConfigProperty(name = "azure.openai.endpoint")
    String endpoint;

    @ConfigProperty(name = "azure.openai.chat-deployment-name")
    String chatDeploymentName;

    @ConfigProperty(name = "azure.openai.embedding-deployment-name")
    String embeddingDeploymentName;

    @ConfigProperty(name = "azure.openai.api-version", defaultValue = "2024-02-15-preview")
    String serviceVersion;

    @ConfigProperty(name = "azure.openai.temperature", defaultValue = "0.7")
    Double temperature;

    @ConfigProperty(name = "azure.openai.max-tokens", defaultValue = "2000")
    Integer maxTokens;

    @ConfigProperty(name = "azure.openai.timeout", defaultValue = "60s")
    Duration timeout;

    @ConfigProperty(name = "azure.openai.max-retries", defaultValue = "3")
    Integer maxRetries;

    /**
     * Azure OpenAI Chat Model Configuration
     * 
     * <p>LangChain4j 1.1.0最新APIを使用してChatModelを設定</p>
     * <p>セキュリティのためログは無効化</p>
     * 
     * @return 設定されたChatModelインスタンス
     */
    @Produces
    @Singleton
    public AzureOpenAiChatModel chatLanguageModel() {
        logger.info("Configuring Azure OpenAI Chat Model with deployment: {}", chatDeploymentName);
        
        return AzureOpenAiChatModel.builder()
                .apiKey(apiKey)
                .endpoint(endpoint)
                .deploymentName(chatDeploymentName)
                .serviceVersion(serviceVersion)  // LangChain4j 1.1.0ではapiVersionからserviceVersionに変更
                .temperature(temperature)
                .maxTokens(maxTokens)
                .timeout(timeout)
                .maxRetries(maxRetries)
                .logRequestsAndResponses(false)  // 本番環境ではfalseを推奨
                .build();
    }

    /**
     * Azure OpenAI Embedding Model Configuration
     * 
     * <p>テキスト埋め込み生成のためのモデル設定</p>
     * 
     * @return 設定されたEmbeddingModelインスタンス
     */
    @Produces
    @Singleton
    public EmbeddingModel embeddingModel() {
        logger.info("Configuring Azure OpenAI Embedding Model with deployment: {}", embeddingDeploymentName);
        
        return AzureOpenAiEmbeddingModel.builder()
                .apiKey(apiKey)
                .endpoint(endpoint)
                .deploymentName(embeddingDeploymentName)
                .serviceVersion(serviceVersion)  // LangChain4j 1.1.0ではapiVersionからserviceVersionに変更
                .timeout(timeout)
                .maxRetries(maxRetries)
                .logRequestsAndResponses(false)  // セキュリティのため無効化
                .build();
    }

    /**
     * Chat Memory Store Configuration
     * 
     * <p>会話履歴保存のためのインメモリストア</p>
     * 
     * @return ChatMemoryStoreインスタンス
     */
    @Produces
    @Singleton
    public ChatMemoryStore chatMemoryStore() {
        return new InMemoryChatMemoryStore();
    }

    /**
     * Message Window Chat Memory Configuration
     * 
     * <p>会話履歴管理のための設定</p>
     * 
     * @return MessageWindowChatMemoryインスタンス
     */
    @Produces
    @Singleton
    public MessageWindowChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .maxMessages(10)  // 保持する最大メッセージ数
                .chatMemoryStore(chatMemoryStore())
                .build();
    }

    /**
     * Customer Support AI Service
     * 
     * <p>LangChain4j 1.1.0 AiServicesを使用してカスタマーサポートAIサービスを生成</p>
     * 
     * @return CustomerSupportAssistantインスタンス
     */
    @Produces
    @Singleton
    public CustomerSupportAssistant customerSupportAssistant() {
        logger.info("Creating Customer Support Assistant with LangChain4j 1.1.0");
        
        return AiServices.builder(CustomerSupportAssistant.class)
                .chatModel(chatLanguageModel())
                .chatMemory(chatMemory())
                .build();
    }

    /**
     * Product Recommendation AI Service
     * 
     * <p>商品推薦機能のためのAIサービス</p>
     * 
     * @return ProductRecommendationAssistantインスタンス
     */
    @Produces
    @Singleton
    public ProductRecommendationAssistant productRecommendationAssistant() {
        logger.info("Creating Product Recommendation Assistant with LangChain4j 1.1.0");
        
        return AiServices.builder(ProductRecommendationAssistant.class)
                .chatModel(chatLanguageModel())
                .build();
    }

    /**
     * Search Enhancement AI Service
     * 
     * <p>検索強化機能のためのAIサービス</p>
     * 
     * @return SearchEnhancementAssistantインスタンス
     */
    @Produces
    @Singleton
    public SearchEnhancementAssistant searchEnhancementAssistant() {
        logger.info("Creating Search Enhancement Assistant with LangChain4j 1.1.0");
        
        return AiServices.builder(SearchEnhancementAssistant.class)
                .chatModel(chatLanguageModel())
                .build();
    }
}
