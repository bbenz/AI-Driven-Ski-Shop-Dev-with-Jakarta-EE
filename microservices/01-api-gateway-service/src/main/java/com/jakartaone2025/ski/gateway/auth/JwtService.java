package com.jakartaone2025.ski.gateway.auth;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.jwt.Claims;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.SecurityContext;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.security.Principal;
import java.util.Set;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * JWT処理サービス - MicroProfile JWTを使用したAPI Gatewayでのトークン検証
 */
@ApplicationScoped
public class JwtService {
    
    private static final Logger logger = Logger.getLogger(JwtService.class.getName());
    
    @Inject
    @ConfigProperty(name = "mp.jwt.verify.issuer", defaultValue = "https://ski-equipment-shop.com")
    private String issuer;
    
    @Inject
    @ConfigProperty(name = "mp.jwt.verify.audiences", defaultValue = "ski-equipment-shop")
    private String audience;
    
    /**
     * JWT トークンを検証
     * MicroProfile JWTでは、フレームワークが自動的にトークンを検証します
     */
    public boolean validateToken(JsonWebToken jwt) {
        try {
            if (jwt == null) {
                logger.warning("JWT token is null");
                return false;
            }
            
            // MicroProfile JWTではフレームワークが署名と有効期限を自動検証
            // ここでは追加のビジネスロジック検証を実行
            
            // issuerの検証
            String tokenIssuer = jwt.getIssuer();
            if (!issuer.equals(tokenIssuer)) {
                logger.warning("JWT issuer mismatch: expected " + issuer + ", got " + tokenIssuer);
                return false;
            }
            
            // audienceの検証
            Set<String> audiences = jwt.getAudience();
            if (audiences == null || !audiences.contains(audience)) {
                logger.warning("JWT audience mismatch: expected " + audience + ", got " + audiences);
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            logger.warning("JWT validation error: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * トークンからユーザーIDを抽出
     */
    public String extractUserId(JsonWebToken jwt) {
        if (jwt == null) {
            return null;
        }
        
        // MicroProfile JWTでは'sub'クレームを使用
        return jwt.getSubject();
    }
    
    /**
     * トークンからユーザー名を抽出
     */
    public String extractUsername(JsonWebToken jwt) {
        if (jwt == null) {
            return null;
        }
        
        // preferredUsernameまたはupnクレームを使用
        Optional<String> preferredUsername = jwt.claim("preferred_username");
        if (preferredUsername.isPresent()) {
            return preferredUsername.get();
        }
        
        // upn (User Principal Name) を試行
        Optional<String> upn = jwt.claim(Claims.upn);
        return upn.orElse(jwt.getName());
    }
    
    /**
     * トークンからロール一覧を抽出
     */
    public Set<String> extractRoles(JsonWebToken jwt) {
        if (jwt == null) {
            return Set.of();
        }
        
        // MicroProfile JWTでは'groups'クレームがロールを表す
        return jwt.getGroups();
    }
    
    /**
     * JWTトークンの有効期限をチェック
     */
    public boolean isTokenExpired(JsonWebToken jwt) {
        if (jwt == null) {
            return true;
        }
        
        try {
            long exp = jwt.getExpirationTime();
            long currentTime = System.currentTimeMillis() / 1000;
            
            return currentTime >= exp;
        } catch (Exception e) {
            logger.warning("Failed to check token expiration: " + e.getMessage());
            return true; // エラーの場合は期限切れとして扱う
        }
    }
    
    /**
     * トークンの残り有効時間（秒）を取得
     */
    public long getTokenRemainingTime(JsonWebToken jwt) {
        if (jwt == null) {
            return 0;
        }
        
        try {
            long exp = jwt.getExpirationTime();
            long currentTime = System.currentTimeMillis() / 1000;
            
            return Math.max(0, exp - currentTime);
        } catch (Exception e) {
            logger.warning("Failed to get token remaining time: " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * SecurityContextからJWTトークンを取得
     */
    public Optional<JsonWebToken> extractJwtFromSecurityContext(SecurityContext securityContext) {
        if (securityContext == null) {
            return Optional.empty();
        }
        
        Principal principal = securityContext.getUserPrincipal();
        if (principal instanceof JsonWebToken) {
            return Optional.of((JsonWebToken) principal);
        }
        
        return Optional.empty();
    }
    
    /**
     * トークンからemailクレームを抽出
     */
    public Optional<String> extractEmail(JsonWebToken jwt) {
        if (jwt == null) {
            return Optional.empty();
        }
        
        return jwt.claim("email");
    }
    
    /**
     * カスタムクレームを抽出
     */
    public <T> Optional<T> extractClaim(JsonWebToken jwt, String claimName) {
        if (jwt == null) {
            return Optional.empty();
        }
        
        return jwt.claim(claimName);
    }
}
