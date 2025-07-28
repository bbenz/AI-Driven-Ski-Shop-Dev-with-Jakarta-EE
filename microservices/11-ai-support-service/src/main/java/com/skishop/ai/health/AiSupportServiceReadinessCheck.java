package com.skishop.ai.health;

import com.skishop.ai.service.CustomerSupportAssistant;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AI Support Service Readiness Health Check
 * 
 * <p>アプリケーションの準備状態確認</p>
 * 
 * @since 1.0.0
 */
@Readiness
@ApplicationScoped
public class AiSupportServiceReadinessCheck implements HealthCheck {
    
    private static final Logger logger = LoggerFactory.getLogger(AiSupportServiceReadinessCheck.class);
    
    @Inject
    CustomerSupportAssistant customerSupportAssistant;
    
    @Override
    public HealthCheckResponse call() {
        try {
            // AI サービスの簡単な動作確認
            String testResponse = customerSupportAssistant.answerQuestion(
                "こんにちは", 
                "一般的な挨拶", 
                "テスト"
            );
            
            if (testResponse != null && !testResponse.trim().isEmpty()) {
                return HealthCheckResponse.up("AI Support Service is ready");
            } else {
                return HealthCheckResponse.down("AI Support Service test failed");
            }
            
        } catch (Exception e) {
            logger.error("AI Support Service readiness check failed", e);
            return HealthCheckResponse.down("AI Support Service is not ready: " + e.getMessage());
        }
    }
}
