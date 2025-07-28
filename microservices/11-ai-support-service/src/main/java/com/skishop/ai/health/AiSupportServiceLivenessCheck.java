package com.skishop.ai.health;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;

/**
 * AI Support Service Liveness Health Check
 * 
 * <p>アプリケーションの生存確認</p>
 * 
 * @since 1.0.0
 */
@Liveness
@ApplicationScoped
public class AiSupportServiceLivenessCheck implements HealthCheck {
    
    @Override
    public HealthCheckResponse call() {
        return HealthCheckResponse.up("AI Support Service is alive");
    }
}
