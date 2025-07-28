package com.jakartaone2025.ski.user;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.info.License;
import org.eclipse.microprofile.openapi.annotations.servers.Server;

/**
 * JAX-RS Application Configuration for User Management Service
 */
@ApplicationPath("/api")
@OpenAPIDefinition(
    info = @Info(
        title = "User Management Service API",
        version = "1.0.0",
        description = "Comprehensive user management service for the ski resort management system. " +
                     "Handles user registration, profile management, preferences, and user-related operations.",
        contact = @Contact(
            name = "Development Team",
            email = "dev@skiresort.com"
        ),
        license = @License(
            name = "MIT License",
            url = "https://opensource.org/licenses/MIT"
        )
    ),
    servers = {
        @Server(url = "http://localhost:8081/user-management-service/api", description = "Development Server"),
        @Server(url = "http://gateway:8080/users", description = "Production Gateway")
    }
)
public class RestApplication extends Application {
    // JAX-RS will automatically discover and register resources
}
