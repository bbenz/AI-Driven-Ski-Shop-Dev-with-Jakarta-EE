package com.jakartaone2025.ski.gateway.health;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;
import org.eclipse.microprofile.health.Readiness;
import com.jakartaone2025.ski.gateway.routing.RoutingService;
import java.util.HashMap;
import java.util.Map;

/**
 * API Gateway Health Checks
 */
@ApplicationScoped
public class GatewayHealthCheck {
    
    private final RoutingService routingService;
    
    @Inject
    public GatewayHealthCheck(RoutingService routingService) {
        this.routingService = routingService;
    }
    
    @Liveness
    public HealthCheck livenessCheck() {
        return () -> HealthCheckResponse.named("gateway-liveness")
            .status(true)
            .withData("timestamp", System.currentTimeMillis())
            .build();
    }
    
    @Readiness
    public HealthCheck readinessCheck() {
        return () -> {
            // 主要サービスの可用性をチェック
            boolean authServiceHealthy = checkCriticalService("http://localhost:8084");
            boolean userServiceHealthy = checkCriticalService("http://localhost:8081");
            boolean productServiceHealthy = checkCriticalService("http://localhost:8083");
            
            boolean isReady = authServiceHealthy && userServiceHealthy && productServiceHealthy;
            
            return HealthCheckResponse.named("gateway-readiness")
                .status(isReady)
                .withData("auth-service", authServiceHealthy)
                .withData("user-service", userServiceHealthy)
                .withData("product-service", productServiceHealthy)
                .withData("timestamp", System.currentTimeMillis())
                .build();
        };
    }
    
    @Path("/health/services")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllServicesHealth() {
        Map<String, Object> healthStatus = new HashMap<>();
        
        // すべてのサービスの状態を確認
        Map<String, String> services = Map.of(
            "user-service", "http://localhost:8081",
            "product-service", "http://localhost:8083", 
            "auth-service", "http://localhost:8084",
            "inventory-service", "http://localhost:8085",
            "order-service", "http://localhost:8086",
            "payment-service", "http://localhost:8087",
            "cart-service", "http://localhost:8088",
            "coupon-service", "http://localhost:8089",
            "point-service", "http://localhost:8090",
            "ai-service", "http://localhost:8091"
        );
        
        Map<String, Boolean> serviceStatuses = new HashMap<>();
        int healthyCount = 0;
        
        for (var entry : services.entrySet()) {
            boolean isHealthy = routingService.isServiceHealthy(entry.getValue());
            serviceStatuses.put(entry.getKey(), isHealthy);
            if (isHealthy) {
                healthyCount++;
            }
        }
        
        healthStatus.put("services", serviceStatuses);
        healthStatus.put("totalServices", services.size());
        healthStatus.put("healthyServices", healthyCount);
        healthStatus.put("unhealthyServices", services.size() - healthyCount);
        healthStatus.put("overallHealth", healthyCount >= services.size() * 0.7); // 70%以上が健康
        healthStatus.put("timestamp", System.currentTimeMillis());
        
        Response.Status status = healthyCount >= services.size() * 0.7 ? 
            Response.Status.OK : Response.Status.SERVICE_UNAVAILABLE;
            
        return Response.status(status)
            .entity(healthStatus)
            .build();
    }
    
    private boolean checkCriticalService(String serviceUrl) {
        try {
            return routingService.isServiceHealthy(serviceUrl);
        } catch (Exception e) {
            return false;
        }
    }
}
