package com.skiresort.auth.filter;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.jwt.JsonWebToken;
import com.skiresort.auth.service.JwtService;
import java.util.logging.Logger;

/**
 * MicroProfile JWT 2.1準拠のJWT認証フィルター
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
@ApplicationScoped
public class JwtAuthenticationFilter implements ContainerRequestFilter {
    
    private static final Logger logger = Logger.getLogger(JwtAuthenticationFilter.class.getName());
    
    @Inject
    private JwtService jwtService;
    
    @Inject
    private JsonWebToken jwt;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String path = requestContext.getUriInfo().getPath();
        
        // 認証が不要なパスをスキップ
        if (isPublicPath(path)) {
            return;
        }
        
        String authHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warning("Missing or invalid Authorization header for path: " + path);
            requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\":\"Missing or invalid authorization header\"}")
                    .build()
            );
            return;
        }
        
        try {
            // MicroProfile JWT実装による自動検証
            // JsonWebTokenが注入されていることで、既に検証済み
            if (jwt != null && jwt.getSubject() != null) {
                logger.info("JWT validation successful for user: " + jwt.getSubject());
                
                // 追加の検証（必要に応じて）
                if (!jwtService.validateAccessToken(jwt)) {
                    logger.warning("JWT validation failed for token");
                    requestContext.abortWith(
                        Response.status(Response.Status.UNAUTHORIZED)
                            .entity("{\"error\":\"Invalid token\"}")
                            .build()
                    );
                }
            } else {
                logger.warning("JWT token is null or invalid");
                requestContext.abortWith(
                    Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"error\":\"Invalid token\"}")
                        .build()
                );
            }
        } catch (Exception e) {
            logger.severe("JWT validation error: " + e.getMessage());
            requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\":\"Token validation failed\"}")
                    .build()
            );
        }
    }
    
    private boolean isPublicPath(String path) {
        return path.startsWith("auth/login") || 
               path.startsWith("auth/register") ||
               path.startsWith("auth/password-reset") ||
               path.startsWith("health") ||
               path.startsWith("metrics") ||
               path.startsWith("openapi");
    }
}
