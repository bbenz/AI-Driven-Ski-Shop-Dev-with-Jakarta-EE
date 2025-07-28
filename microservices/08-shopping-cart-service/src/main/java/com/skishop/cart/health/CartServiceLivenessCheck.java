package com.skishop.cart.health;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;

@Liveness
@ApplicationScoped
public class CartServiceLivenessCheck implements HealthCheck {
    
    @Override
    public HealthCheckResponse call() {
        return HealthCheckResponse.up("Shopping Cart Service is alive");
    }
}
