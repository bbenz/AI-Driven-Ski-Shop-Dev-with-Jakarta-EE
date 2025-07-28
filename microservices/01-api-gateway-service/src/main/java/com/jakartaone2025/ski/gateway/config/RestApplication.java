package com.jakartaone2025.ski.gateway.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.info.License;
import org.eclipse.microprofile.openapi.annotations.servers.Server;

/**
 * JAX-RS Application Configuration for API Gateway
 */
@ApplicationPath("/api")
@ApplicationScoped
@OpenAPIDefinition(
    info = @Info(
        title = "Ski Equipment Shop API Gateway",
        version = "1.0.0",
        description = "API Gateway for Ski Equipment Shop Microservices Architecture",
        license = @License(name = "Apache 2.0", url = "https://www.apache.org/licenses/LICENSE-2.0.html")
    ),
    servers = {
        @Server(url = "http://localhost:8080/api-gateway/api", description = "Development Server"),
        @Server(url = "https://api.ski-shop.com/api", description = "Production Server")
    }
)
public class RestApplication extends Application {
}
