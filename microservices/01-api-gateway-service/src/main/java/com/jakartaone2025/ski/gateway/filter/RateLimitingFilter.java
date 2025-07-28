package com.jakartaone2025.ski.gateway.filter;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import java.util.logging.Logger;

/**
 * Rate Limiting Filter for API Gateway
 */
@Provider
@PreMatching
@Priority(1000)
@RequestScoped
public class RateLimitingFilter implements ContainerRequestFilter {
    
    private static final Logger logger = Logger.getLogger(RateLimitingFilter.class.getName());
    
    @Inject
    private RateLimitService rateLimitService;
    
    @Context
    private HttpServletRequest request;
    
    @Override
    public void filter(ContainerRequestContext requestContext) {
        String clientId = getClientIdentifier(requestContext);
        String path = requestContext.getUriInfo().getPath();
        
        try {
            if (!rateLimitService.isRequestAllowed(clientId, path)) {
                logger.warning(String.format("Rate limit exceeded for client: %s, path: %s", clientId, path));
                
                requestContext.abortWith(
                    Response.status(Response.Status.TOO_MANY_REQUESTS)
                        .header("X-RateLimit-Limit", rateLimitService.getLimit(path))
                        .header("X-RateLimit-Remaining", rateLimitService.getRemaining(clientId, path))
                        .header("X-RateLimit-Reset", rateLimitService.getResetTime(clientId, path))
                        .entity("{\"error\": \"Rate limit exceeded\", \"message\": \"Too many requests\"}")
                        .build()
                );
                return;
            }
            
            // レート制限情報をヘッダーに追加
            requestContext.setProperty("rateLimit.clientId", clientId);
            requestContext.setProperty("rateLimit.remaining", rateLimitService.getRemaining(clientId, path));
            
        } catch (Exception e) {
            logger.severe("Error in rate limiting: " + e.getMessage());
            // レート制限エラーの場合はリクエストを通す（可用性優先）
        }
    }
    
    private String getClientIdentifier(ContainerRequestContext requestContext) {
        // Authorization ヘッダーからユーザーIDを取得
        String authHeader = requestContext.getHeaderString("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                // JWT トークンからユーザーIDを抽出（簡略化）
                String token = authHeader.substring(7);
                String userId = extractUserIdFromToken(token);
                if (userId != null) {
                    return "user:" + userId;
                }
            } catch (Exception e) {
                logger.warning("Failed to extract user ID from token: " + e.getMessage());
            }
        }
        
        // フォールバック：IPアドレス
        String clientIp = request.getRemoteAddr();
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isEmpty()) {
            clientIp = forwardedFor.split(",")[0].trim();
        }
        
        return "ip:" + clientIp;
    }
    
    private String extractUserIdFromToken(String token) {
        // JWT トークンの解析（実装は後で詳細化）
        // 現在は簡略化
        return null;
    }
}

/**
 * Rate Limit Service Interface
 */
interface RateLimitService {
    boolean isRequestAllowed(String clientId, String path);
    int getLimit(String path);
    int getRemaining(String clientId, String path);
    long getResetTime(String clientId, String path);
}
