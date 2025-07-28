package com.jakartaone2025.ski.gateway.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.ext.Provider;
import java.util.logging.Logger;

/**
 * CORS Filter for API Gateway
 */
@Provider
@ApplicationScoped
public class CorsFilter implements ContainerResponseFilter {
    
    private static final Logger logger = Logger.getLogger(CorsFilter.class.getName());
    
    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        
        // CORS ヘッダーを追加
        responseContext.getHeaders().add("Access-Control-Allow-Origin", "*");
        responseContext.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, PATCH, OPTIONS");
        responseContext.getHeaders().add("Access-Control-Allow-Headers", 
            "Origin, Content-Type, Accept, Authorization, X-Requested-With, X-User-ID, X-Username");
        responseContext.getHeaders().add("Access-Control-Expose-Headers",
            "X-RateLimit-Limit, X-RateLimit-Remaining, X-RateLimit-Reset");
        responseContext.getHeaders().add("Access-Control-Max-Age", "3600");
        
        // プリフライトリクエストの場合
        if ("OPTIONS".equals(requestContext.getMethod())) {
            responseContext.setStatus(200);
        }
        
        logger.fine("CORS headers added to response");
    }
}
