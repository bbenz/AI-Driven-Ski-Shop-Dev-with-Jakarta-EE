package com.jakartaone2025.ski.gateway.routing;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Service Discovery and Routing Service
 */
@ApplicationScoped
public class RoutingService {
    
    private static final Logger logger = Logger.getLogger(RoutingService.class.getName());
    
    private final String userServiceUrl;
    private final String productServiceUrl;
    private final String authServiceUrl;
    private final String inventoryServiceUrl;
    private final String orderServiceUrl;
    private final String paymentServiceUrl;
    private final String cartServiceUrl;
    private final String couponServiceUrl;
    private final String pointServiceUrl;
    private final String aiServiceUrl;
    
    private final Client httpClient;
    private final Map<String, ServiceHealthStatus> serviceHealthMap = new ConcurrentHashMap<>();
    
    @Inject
    public RoutingService(
            @ConfigProperty(name = "services.user.url", defaultValue = "http://localhost:8081") String userServiceUrl,
            @ConfigProperty(name = "services.product.url", defaultValue = "http://localhost:8083") String productServiceUrl,
            @ConfigProperty(name = "services.auth.url", defaultValue = "http://localhost:8084") String authServiceUrl,
            @ConfigProperty(name = "services.inventory.url", defaultValue = "http://localhost:8085") String inventoryServiceUrl,
            @ConfigProperty(name = "services.order.url", defaultValue = "http://localhost:8086") String orderServiceUrl,
            @ConfigProperty(name = "services.payment.url", defaultValue = "http://localhost:8087") String paymentServiceUrl,
            @ConfigProperty(name = "services.cart.url", defaultValue = "http://localhost:8088") String cartServiceUrl,
            @ConfigProperty(name = "services.coupon.url", defaultValue = "http://localhost:8089") String couponServiceUrl,
            @ConfigProperty(name = "services.point.url", defaultValue = "http://localhost:8090") String pointServiceUrl,
            @ConfigProperty(name = "services.ai.url", defaultValue = "http://localhost:8091") String aiServiceUrl) {
        
        this.userServiceUrl = userServiceUrl;
        this.productServiceUrl = productServiceUrl;
        this.authServiceUrl = authServiceUrl;
        this.inventoryServiceUrl = inventoryServiceUrl;
        this.orderServiceUrl = orderServiceUrl;
        this.paymentServiceUrl = paymentServiceUrl;
        this.cartServiceUrl = cartServiceUrl;
        this.couponServiceUrl = couponServiceUrl;
        this.pointServiceUrl = pointServiceUrl;
        this.aiServiceUrl = aiServiceUrl;
        
        this.httpClient = ClientBuilder.newClient();
        
        // 初期ヘルスチェック
        initializeServiceHealth();
    }
    
    /**
     * パスに基づいてサービスURLを取得
     */
    public String resolveServiceUrl(String path) {
        if (path.startsWith("/users")) {
            return userServiceUrl;
        } else if (path.startsWith("/api/v1/products") || path.startsWith("/api/v1/categories") || path.startsWith("/products") || path.startsWith("/categories")) {
            return productServiceUrl;
        } else if (path.startsWith("/auth")) {
            return authServiceUrl;
        } else if (path.startsWith("/inventory")) {
            return inventoryServiceUrl;
        } else if (path.startsWith("/orders")) {
            return orderServiceUrl;
        } else if (path.startsWith("/payments")) {
            return paymentServiceUrl;
        } else if (path.startsWith("/cart")) {
            return cartServiceUrl;
        } else if (path.startsWith("/coupons") || path.startsWith("/discounts")) {
            return couponServiceUrl;
        } else if (path.startsWith("/points") || path.startsWith("/loyalty")) {
            return pointServiceUrl;
        } else if (path.startsWith("/ai") || path.startsWith("/support")) {
            return aiServiceUrl;
        }
        
        throw new IllegalArgumentException("Unknown service path: " + path);
    }
    
    /**
     * サービスのヘルス状態を確認
     */
    public boolean isServiceHealthy(String serviceUrl) {
        ServiceHealthStatus status = serviceHealthMap.get(serviceUrl);
        if (status == null) {
            return checkServiceHealth(serviceUrl);
        }
        
        // キャッシュされた結果が古い場合は再チェック
        long now = System.currentTimeMillis();
        if (now - status.lastChecked > 30000) { // 30秒
            return checkServiceHealth(serviceUrl);
        }
        
        return status.isHealthy;
    }
    
    /**
     * サービスのヘルスチェックを実行
     */
    private boolean checkServiceHealth(String serviceUrl) {
        try {
            WebTarget target = httpClient.target(serviceUrl).path("health");
            Response response = target.request().get();
            
            boolean isHealthy = response.getStatus() == 200;
            serviceHealthMap.put(serviceUrl, new ServiceHealthStatus(isHealthy, System.currentTimeMillis()));
            
            logger.fine(String.format("Health check for %s: %s", serviceUrl, isHealthy ? "HEALTHY" : "UNHEALTHY"));
            return isHealthy;
            
        } catch (Exception e) {
            logger.warning(String.format("Health check failed for %s: %s", serviceUrl, e.getMessage()));
            serviceHealthMap.put(serviceUrl, new ServiceHealthStatus(false, System.currentTimeMillis()));
            return false;
        }
    }
    
    /**
     * 初期化時にすべてのサービスのヘルス状態を確認
     */
    private void initializeServiceHealth() {
        String[] services = {
            userServiceUrl, productServiceUrl, authServiceUrl, inventoryServiceUrl,
            orderServiceUrl, paymentServiceUrl, cartServiceUrl, couponServiceUrl,
            pointServiceUrl, aiServiceUrl
        };
        
        for (String serviceUrl : services) {
            checkServiceHealth(serviceUrl);
        }
    }
    
    /**
     * サービスヘルス状態
     */
    private static class ServiceHealthStatus {
        final boolean isHealthy;
        final long lastChecked;
        
        ServiceHealthStatus(boolean isHealthy, long lastChecked) {
            this.isHealthy = isHealthy;
            this.lastChecked = lastChecked;
        }
    }
    
    /**
     * HTTPクライアントを取得
     */
    public Client getHttpClient() {
        return httpClient;
    }
    
    /**
     * リソースをクリーンアップ
     */
    public void destroy() {
        if (httpClient != null) {
            httpClient.close();
        }
    }
}
