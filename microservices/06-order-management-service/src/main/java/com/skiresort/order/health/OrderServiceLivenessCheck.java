package com.skiresort.order.health;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;

/**
 * 注文管理サービス生存状況ヘルスチェック
 */
@Liveness
@ApplicationScoped
public class OrderServiceLivenessCheck implements HealthCheck {
    
    @Override
    public HealthCheckResponse call() {
        // アプリケーションが生きているかの基本チェック
        return HealthCheckResponse.named("order-service-liveness")
            .status(true)
            .withData("service", "order-management")
            .withData("version", "1.0.0")
            .withData("status", "running")
            .build();
    }
}
