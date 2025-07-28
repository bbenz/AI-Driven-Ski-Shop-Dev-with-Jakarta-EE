package com.jakartaone2025.ski.gateway.filter;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import com.jakartaone2025.ski.gateway.auth.JwtService;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * Authentication Filter for API Gateway - MicroProfile JWT対応
 */
@Provider
@PreMatching
@Priority(2000)
@RequestScoped
public class AuthenticationFilter implements ContainerRequestFilter {
    
    private static final Logger logger = Logger.getLogger(AuthenticationFilter.class.getName());
    
    @Inject
    private JwtService jwtService;
    
    // 認証不要なパス
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
        "/health",
        "/metrics",
        "/openapi",
        "/auth/login",
        "/auth/register",
        "/auth/refresh",
        "/products/public",
        "/static"
    );
    
    @Override
    public void filter(ContainerRequestContext requestContext) {
        String path = requestContext.getUriInfo().getPath();
        String method = requestContext.getMethod();
        
        // 公開パスはスキップ
        if (isPublicPath(path)) {
            return;
        }
        
        // OPTIONS リクエストはスキップ（CORS対応）
        if ("OPTIONS".equals(method)) {
            return;
        }
        
        String authHeader = requestContext.getHeaderString("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warning("Missing or invalid Authorization header for path: " + path);
            abortWithUnauthorized(requestContext, "Missing or invalid Authorization header");
            return;
        }
        
        String tokenString = authHeader.substring(7);
        
        try {
            // MicroProfile JWTではコンテナが自動的にトークンを検証し、
            // SecurityContextに設定するため、ここでは基本的な検証のみ行う
            // 実際の検証はMicroProfile JWTランタイムが処理
            
            // Authorizationヘッダーの形式チェック
            if (tokenString.isEmpty()) {
                logger.warning("Empty JWT token for path: " + path);
                abortWithUnauthorized(requestContext, "Empty token");
                return;
            }
            
            // Note: MicroProfile JWTが有効な場合、コンテナが自動的に
            // SecurityContextにJsonWebTokenを設定します
            // この実装では、後続のフィルターやリソースで
            // @Inject JsonWebToken jwt; でトークンを取得できます
            
            // 基本的なトークン検証（実際の署名検証はMicroProfile JWTが実行）
            String[] tokenParts = tokenString.split("\\.");
            if (tokenParts.length != 3) {
                logger.warning("Invalid JWT token format for path: " + path);
                abortWithUnauthorized(requestContext, "Invalid token format");
                return;
            }
            
            // トークンをヘッダーとして保存（後続の処理で使用）
            requestContext.setProperty("auth.token", tokenString);
            
            logger.fine(String.format("JWT token validated for path: %s", path));
            
        } catch (Exception e) {
            logger.warning("Authentication failed for path " + path + ": " + e.getMessage());
            abortWithUnauthorized(requestContext, "Authentication failed");
        }
    }
    
    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }
    
    private void abortWithUnauthorized(ContainerRequestContext requestContext, String message) {
        requestContext.abortWith(
            Response.status(Response.Status.UNAUTHORIZED)
                .header("WWW-Authenticate", "Bearer")
                .entity(String.format("{\"error\": \"Unauthorized\", \"message\": \"%s\"}", message))
                .build()
        );
    }
}
