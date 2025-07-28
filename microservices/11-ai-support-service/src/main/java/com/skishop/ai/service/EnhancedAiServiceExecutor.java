package com.skishop.ai.service;

import com.skishop.ai.exception.AiServiceException;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * AI Service 実行プラットフォーム
 * 
 * <p>Azure OpenAI + LangChain4j のエラーハンドリング、リトライ、フォールバック機能を提供</p>
 * <p>最新のJava 21機能を活用</p>
 * 
 * @since 1.0.0
 */
@ApplicationScoped
public class EnhancedAiServiceExecutor {
    
    private static final Logger logger = LoggerFactory.getLogger(EnhancedAiServiceExecutor.class);
    private static final int MAX_INPUT_LENGTH = 8000;
    private static final Duration AI_TIMEOUT = Duration.ofSeconds(30);
    
    /**
     * Java 21のText Blocks機能を使用したフォールバックメッセージ
     */
    private static final String FALLBACK_MESSAGE = """
            申し訳ございませんが、AIサービスが一時的にご利用いただけません。
            しばらく時間をおいてから再度お試しください。
            
            お急ぎの場合は以下からお問い合わせください：
            - 電話: 0120-XXX-XXX（平日 9:00-18:00）
            - メール: support@skishop.com
            """;
    
    /**
     * リトライ付きでAIサービスを実行
     */
    public String executeWithRetry(
            AiServiceCall aiServiceCall,
            String inputText,
            String context) {
        
        validateInput(inputText);
        
        int maxAttempts = 3;
        int attempt = 0;
        long delay = 1000; // 初期遅延 1秒
        
        while (attempt < maxAttempts) {
            try {
                attempt++;
                logger.debug("AI service call attempt {} for context: {}", attempt, context);
                
                // AIサービス呼び出しをタイムアウト付きで実行
                CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                    try {
                        return aiServiceCall.call(inputText, context);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
                
                String response = future.get(AI_TIMEOUT.toSeconds(), TimeUnit.SECONDS);
                validateResponse(response);
                
                logger.debug("AI service call successful on attempt {}", attempt);
                return response;
                
            } catch (Exception e) {
                logger.warn("AI service call failed on attempt {}: {}", attempt, e.getMessage());
                
                AiServiceException aiException = mapToAiServiceException(e);
                
                // リトライ不可能なエラーの場合は即座に例外をスロー
                if (!aiException.isRetryable() || attempt >= maxAttempts) {
                    recordFailureMetrics(aiException);
                    throw aiException;
                }
                
                // 指数バックオフでリトライ
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw AiServiceException.internalError("Retry interrupted", ie);
                }
                
                delay *= 2; // 遅延を倍増
                if (delay > 5000) delay = 5000; // 最大5秒
            }
        }
        
        // すべての試行が失敗した場合
        return FALLBACK_MESSAGE;
    }
    
    /**
     * 入力検証（Java 21機能を活用）
     */
    private void validateInput(String inputText) {
        if (inputText == null || inputText.isBlank()) {
            var error = new ErrorType.ValidationError("inputText", "cannot be null or blank");
            throw error.toAiServiceException();
        }
        
        if (inputText.length() > MAX_INPUT_LENGTH) {
            var error = new ErrorType.ValidationError("inputText", 
                "exceeds maximum length of " + MAX_INPUT_LENGTH + " characters");
            throw error.toAiServiceException();
        }
    }
    
    /**
     * レスポンス検証
     */
    private void validateResponse(String response) {
        if (response == null || response.isBlank()) {
            var error = new ErrorType.ServiceError("AI Service", "EMPTY_RESPONSE");
            throw error.toAiServiceException();
        }
    }
    
    /**
     * 例外マッピング（Java 21のSwitch Expressionsを活用）
     */
    private AiServiceException mapToAiServiceException(Exception e) {
        return switch (e) {
            case java.util.concurrent.TimeoutException timeout -> 
                AiServiceException.serviceUnavailable("Request timeout: " + timeout.getMessage());
                
            case java.net.ConnectException connection -> 
                AiServiceException.connectionFailed("Connection failed", connection);
                
            case IllegalArgumentException invalidArg -> 
                AiServiceException.invalidInput("Invalid argument: " + invalidArg.getMessage());
                
            case SecurityException security -> 
                AiServiceException.authenticationFailed("Security error: " + security.getMessage());
                
            case RuntimeException runtime when runtime.getMessage() != null && 
                runtime.getMessage().contains("rate limit") -> 
                AiServiceException.rateLimitExceeded("Rate limit detected");
                
            case RuntimeException runtime when runtime.getMessage() != null && 
                runtime.getMessage().contains("quota") -> 
                AiServiceException.quotaExceeded("Quota limit detected");
                
            default -> AiServiceException.internalError("Unexpected error: " + e.getMessage(), e);
        };
    }
    
    /**
     * 失敗メトリクスの記録
     */
    private void recordFailureMetrics(AiServiceException ex) {
        logger.error("AI service failure - ErrorCode: {}, Message: {}, Retryable: {}", 
                    ex.getErrorCode(), ex.getMessage(), ex.isRetryable());
        // ここでメトリクス収集システムにデータを送信
    }
    
    /**
     * AIサービス呼び出し関数型インターフェース
     */
    @FunctionalInterface
    public interface AiServiceCall {
        String call(String inputText, String context) throws Exception;
    }
    
    /**
     * Java 21のSealed Classesを活用したエラータイプ定義
     */
    public sealed interface ErrorType 
        permits ErrorType.ValidationError, ErrorType.ServiceError, ErrorType.NetworkError {
        
        AiServiceException toAiServiceException();
        
        record ValidationError(String field, String reason) implements ErrorType {
            @Override
            public AiServiceException toAiServiceException() {
                return AiServiceException.validationFailed(
                    String.format("Field '%s': %s", field, reason)
                );
            }
        }
        
        record ServiceError(String service, String errorCode) implements ErrorType {
            @Override
            public AiServiceException toAiServiceException() {
                return AiServiceException.serviceUnavailable(
                    String.format("Service '%s' error: %s", service, errorCode)
                );
            }
        }
        
        record NetworkError(String operation, String details) implements ErrorType {
            @Override
            public AiServiceException toAiServiceException() {
                return AiServiceException.connectionFailed(
                    String.format("Network operation '%s' failed: %s", operation, details),
                    null
                );
            }
        }
    }
}
