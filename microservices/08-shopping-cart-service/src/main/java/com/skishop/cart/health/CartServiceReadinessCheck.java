package com.skishop.cart.health;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;
import io.quarkus.redis.datasource.RedisDataSource;
import io.agroal.api.AgroalDataSource;

import java.sql.Connection;

@Readiness
@ApplicationScoped 
public class CartServiceReadinessCheck implements HealthCheck {
    
    @Inject
    AgroalDataSource dataSource;
    
    @Inject
    RedisDataSource redis;
    
    @Override
    public HealthCheckResponse call() {
        try {
            // Check database connectivity
            boolean dbHealthy = checkDatabase();
            
            // Check Redis connectivity  
            boolean redisHealthy = checkRedis();
            
            if (dbHealthy && redisHealthy) {
                return HealthCheckResponse.named("Shopping Cart Service readiness check")
                    .up()
                    .withData("database", "UP")
                    .withData("redis", "UP")
                    .build();
            } else {
                return HealthCheckResponse.named("Shopping Cart Service readiness check")
                    .down()
                    .withData("database", dbHealthy ? "UP" : "DOWN")
                    .withData("redis", redisHealthy ? "UP" : "DOWN")
                    .build();
            }
            
        } catch (Exception e) {
            return HealthCheckResponse.named("Shopping Cart Service readiness check")
                .down()
                .withData("error", e.getMessage())
                .build();
        }
    }
    
    private boolean checkDatabase() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(5);
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean checkRedis() {
        try {
            redis.value(String.class).set("health-check", "ok");
            String result = redis.value(String.class).get("health-check");
            return "ok".equals(result);
        } catch (Exception e) {
            return false;
        }
    }
}
