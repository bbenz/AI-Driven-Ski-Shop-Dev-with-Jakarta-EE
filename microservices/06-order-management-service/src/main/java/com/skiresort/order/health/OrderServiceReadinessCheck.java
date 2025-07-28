package com.skiresort.order.health;

import com.skiresort.order.repository.OrderRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

/**
 * 注文管理サービス準備状況ヘルスチェック
 */
@Readiness
@ApplicationScoped
public class OrderServiceReadinessCheck implements HealthCheck {
    
    @Inject
    private OrderRepository orderRepository;
    
    @Override
    public HealthCheckResponse call() {
        try {
            // データベース接続テスト
            long orderCount = orderRepository.count();
            
            return HealthCheckResponse.named("order-service-readiness")
                .status(true)
                .withData("database", "accessible")
                .withData("order_count", orderCount)
                .build();
                
        } catch (Exception e) {
            return HealthCheckResponse.named("order-service-readiness")
                .status(false)
                .withData("database", "not accessible")
                .withData("error", e.getMessage())
                .build();
        }
    }
}
