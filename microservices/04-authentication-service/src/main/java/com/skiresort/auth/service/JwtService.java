package com.skiresort.auth.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;
import io.smallrye.jwt.build.Jwt;
import com.skiresort.auth.exception.JwtGenerationException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.logging.Logger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * JWT サービス - MicroProfile JWT実装
 * JWT トークンの生成、検証、管理を担当
 */
@ApplicationScoped
public class JwtService {
    
    private static final Logger logger = Logger.getLogger(JwtService.class.getName());
    
    @Inject
    @ConfigProperty(name = "mp.jwt.verify.issuer", defaultValue = "https://ski-equipment-shop.com")
    private String jwtIssuer;
    
    @Inject
    @ConfigProperty(name = "mp.jwt.verify.audiences", defaultValue = "ski-equipment-shop")
    private String jwtAudience;
    
    @Inject
    @ConfigProperty(name = "jwt.access-token.expiration", defaultValue = "PT15M")
    private Duration accessTokenExpiration;
    
    @Inject
    @ConfigProperty(name = "jwt.refresh-token.expiration", defaultValue = "P7D")
    private Duration refreshTokenExpiration;
    
    // 本番環境では外部から読み込む
    private static KeyPair keyPair;
    
    static {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            keyPair = keyPairGenerator.generateKeyPair();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate key pair", e);
        }
    }
    
    /**
     * トークンペアを生成
     * 
     * @param userId ユーザーID
     * @param roles ロール
     * @param permissions 権限
     * @return トークンペア
     */
    public AuthenticationService.TokenPair generateTokens(UUID userId, Set<String> roles, Set<String> permissions) {
        try {
            logger.info("Generating tokens for user: " + userId);
            
            String accessToken = generateAccessToken(userId, roles, permissions);
            String refreshToken = generateRefreshToken(userId, roles, permissions);
            
            return new AuthenticationService.TokenPair(
                accessToken,
                refreshToken,
                "Bearer",
                accessTokenExpiration,
                permissions
            );
            
        } catch (Exception e) {
            logger.severe("Failed to generate tokens: " + e.getMessage());
            throw new JwtGenerationException("Token generation failed", e);
        }
    }
    
    /**
     * アクセストークンを生成（MicroProfile JWT 2.1準拠、SmallRye JWT Build使用）
     * 
     * @param userId ユーザーID
     * @param roles ロール
     * @param permissions 権限
     * @return アクセストークン
     */
    private String generateAccessToken(UUID userId, Set<String> roles, Set<String> permissions) {
        try {
            Instant now = Instant.now();
            Instant expirationTime = now.plus(accessTokenExpiration);
            
            // MicroProfile JWT 2.1準拠のトークン生成
            return Jwt.issuer(jwtIssuer)
                .audience(jwtAudience)
                .subject(userId.toString())
                .issuedAt(now)
                .expiresAt(expirationTime)
                // MicroProfile JWT標準のgroupsクレーム
                .groups(roles)
                // カスタムクレーム
                .claim("permissions", String.join(",", permissions))
                .claim("token_type", "access")
                .claim("preferred_username", "user_" + userId.toString().substring(0, 8))
                .claim("typ", "JWT")
                // JWS署名
                .jws()
                .keyId("auth-service-key")
                .sign(keyPair.getPrivate());
        } catch (Exception e) {
            logger.severe("Failed to generate access token: " + e.getMessage());
            throw new JwtGenerationException("Access token generation failed", e);
        }
    }
    
    /**
     * リフレッシュトークンを生成（MicroProfile JWT 2.1準拠、SmallRye JWT Build使用）
     * 
     * @param userId ユーザーID
     * @param roles ロール
     * @param permissions 権限
     * @return リフレッシュトークン
     */
    private String generateRefreshToken(UUID userId, Set<String> roles, Set<String> permissions) {
        try {
            Instant now = Instant.now();
            Instant expirationTime = now.plus(refreshTokenExpiration);
            
            // MicroProfile JWT 2.1準拠のリフレッシュトークン生成
            return Jwt.issuer(jwtIssuer)
                .audience(jwtAudience)
                .subject(userId.toString())
                .issuedAt(now)
                .expiresAt(expirationTime)
                // MicroProfile JWT標準のgroupsクレーム
                .groups(roles)
                // カスタムクレーム
                .claim("permissions", String.join(",", permissions))
                .claim("token_type", "refresh")
                .claim("typ", "JWT")
                // JWS署名
                .jws()
                .keyId("auth-service-key")
                .sign(keyPair.getPrivate());
        } catch (Exception e) {
            logger.severe("Failed to generate refresh token: " + e.getMessage());
            throw new JwtGenerationException("Refresh token generation failed", e);
        }
    }
    
    /**
     * MicroProfile JWTトークンを検証
     * 
     * @param jwt JWT トークン
     * @return 検証結果
     */
    public boolean validateAccessToken(JsonWebToken jwt) {
        try {
            if (jwt == null) {
                logger.warning("JWT token is null");
                return false;
            }
            
            // issuerの検証
            if (!jwtIssuer.equals(jwt.getIssuer())) {
                logger.warning("JWT issuer mismatch");
                return false;
            }
            
            // audienceの検証
            Set<String> audiences = jwt.getAudience();
            if (audiences == null || !audiences.contains(jwtAudience)) {
                logger.warning("JWT audience mismatch");
                return false;
            }
            
            // token_typeの確認（accessトークンかどうか）
            Optional<String> tokenType = jwt.claim("token_type");
            if (!tokenType.isPresent() || !"access".equals(tokenType.get())) {
                logger.warning("Invalid token type");
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            logger.warning("Token validation failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 文字列トークンを検証（レガシー対応）
     */
    public boolean validateAccessToken(String token) {
        try {
            Map<String, Object> claims = parseJwtClaims(token);
            if (claims == null) {
                return false;
            }
            
            // issuerの検証
            String issuer = (String) claims.get("iss");
            if (!jwtIssuer.equals(issuer)) {
                logger.warning("JWT issuer mismatch");
                return false;
            }
            
            // token_typeの確認
            String tokenType = (String) claims.get("token_type");
            if (!"access".equals(tokenType)) {
                logger.warning("Invalid token type");
                return false;
            }
            
            // 有効期限チェック
            Long exp = (Long) claims.get("exp");
            if (exp != null && exp < System.currentTimeMillis() / 1000) {
                logger.warning("Token expired");
                return false;
            }
            
            return true;
        } catch (Exception e) {
            logger.warning("String token validation failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * リフレッシュトークンを検証
     * 
     * @param jwt JWT トークン
     * @return 検証結果
     */
    public boolean validateRefreshToken(JsonWebToken jwt) {
        try {
            if (jwt == null) {
                return false;
            }
            
            // 基本的な検証
            if (!jwtIssuer.equals(jwt.getIssuer())) {
                return false;
            }
            
            Set<String> audiences = jwt.getAudience();
            if (audiences == null || !audiences.contains(jwtAudience)) {
                return false;
            }
            
            // token_typeの確認（refreshトークンかどうか）
            Optional<String> tokenType = jwt.claim("token_type");
            return tokenType.isPresent() && "refresh".equals(tokenType.get());
            
        } catch (Exception e) {
            logger.warning("Refresh token validation failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * リフレッシュトークンを検証（文字列版）
     */
    public boolean validateRefreshToken(String token) {
        try {
            Map<String, Object> claims = parseJwtClaims(token);
            if (claims == null) {
                return false;
            }
            
            // issuerの検証
            String issuer = (String) claims.get("iss");
            if (!jwtIssuer.equals(issuer)) {
                return false;
            }
            
            // token_typeの確認
            String tokenType = (String) claims.get("token_type");
            if (!"refresh".equals(tokenType)) {
                return false;
            }
            
            // 有効期限チェック
            Long exp = (Long) claims.get("exp");
            if (exp != null && exp < System.currentTimeMillis() / 1000) {
                return false;
            }
            
            return true;
        } catch (Exception e) {
            logger.warning("String refresh token validation failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * JWTからユーザーIDを抽出
     */
    public UUID extractUserId(JsonWebToken jwt) {
        if (jwt == null) {
            return null;
        }
        
        try {
            String subject = jwt.getSubject();
            return subject != null ? UUID.fromString(subject) : null;
        } catch (IllegalArgumentException e) {
            logger.warning("Invalid UUID in JWT subject: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * JWTからロールを抽出
     */
    public Set<String> extractRoles(JsonWebToken jwt) {
        if (jwt == null) {
            return Set.of();
        }
        
        // MicroProfile JWTのgetGroups()の結果がnullの可能性がある場合の対処
        Set<String> groups = jwt.getGroups();
        if (groups != null && !groups.isEmpty()) {
            return groups;
        }
        
        // バックアップとしてclaimから取得
        Optional<String> groupsStr = jwt.claim(Claims.groups.name());
        if (groupsStr.isPresent() && !groupsStr.get().isEmpty()) {
            return new HashSet<>(Arrays.asList(groupsStr.get().split(",")));
        }
        
        return Set.of();
    }
    
    /**
     * JWTから権限を抽出
     */
    public Set<String> extractPermissions(JsonWebToken jwt) {
        if (jwt == null) {
            return Set.of();
        }
        
        Optional<String> permissionsStr = jwt.claim("permissions");
        if (permissionsStr.isPresent() && !permissionsStr.get().isEmpty()) {
            return new HashSet<>(Arrays.asList(permissionsStr.get().split(",")));
        }
        
        return Set.of();
    }
    
    /**
     * JWTの有効期限をチェック
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
            return true;
        }
    }
    
    /**
     * 公開鍵を取得（JWT検証用）
     */
    public PublicKey getPublicKey() {
        return keyPair.getPublic();
    }
    
    /**
     * リフレッシュトークンからユーザーIDを抽出（文字列版）
     */
    public UUID extractUserIdFromRefreshToken(String token) {
        try {
            Map<String, Object> claims = parseJwtClaims(token);
            if (claims == null) {
                return null;
            }
            
            String subject = (String) claims.get("sub");
            return subject != null ? UUID.fromString(subject) : null;
        } catch (Exception e) {
            logger.warning("Failed to extract user ID from refresh token: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * リフレッシュトークンからロールを抽出（文字列版）
     */
    @SuppressWarnings("unchecked")
    public Set<String> extractRolesFromRefreshToken(String token) {
        try {
            Map<String, Object> claims = parseJwtClaims(token);
            if (claims == null) {
                return Set.of();
            }
            
            Object groups = claims.get("groups");
            if (groups instanceof List) {
                return new HashSet<>((List<String>) groups);
            }
            return Set.of();
        } catch (Exception e) {
            logger.warning("Failed to extract roles from refresh token: " + e.getMessage());
            return Set.of();
        }
    }
    
    /**
     * リフレッシュトークンから権限を抽出（文字列版）
     */
    public Set<String> extractPermissionsFromRefreshToken(String token) {
        try {
            Map<String, Object> claims = parseJwtClaims(token);
            if (claims == null) {
                return Set.of();
            }
            
            Object permissions = claims.get("permissions");
            if (permissions instanceof String && !((String) permissions).isEmpty()) {
                return new HashSet<>(Arrays.asList(((String) permissions).split(",")));
            }
            return Set.of();
        } catch (Exception e) {
            logger.warning("Failed to extract permissions from refresh token: " + e.getMessage());
            return Set.of();
        }
    }
    
    /**
     * JWT文字列からクレームを抽出するヘルパーメソッド（SmallRye JWT使用）
     * シンプルなBase64デコードによる実装（署名検証は別途実施）
     */
    private Map<String, Object> parseJwtClaims(String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return null;
            }
            
            // Bearerプレフィックスを除去
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            // JWT形式チェック（3つの部分に分かれているか）
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                logger.warning("Invalid JWT format: expected 3 parts, got " + parts.length);
                return null;
            }
            
            // ペイロード部分をデコード
            String payload = parts[1];
            byte[] decodedBytes = Base64.getUrlDecoder().decode(payload);
            String decodedPayload = new String(decodedBytes, StandardCharsets.UTF_8);
            
            // JSONをMapに変換
            ObjectMapper objectMapper = new ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String, Object> claimsMap = objectMapper.readValue(decodedPayload, Map.class);
            
            return claimsMap;
            
        } catch (Exception e) {
            logger.warning("Failed to parse JWT claims: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * JWTトークンの詳細情報を取得
     */
    public TokenInfo getTokenInfo(JsonWebToken jwt) {
        if (jwt == null) {
            return null;
        }
        
        return new TokenInfo(
            extractUserId(jwt),
            jwt.getName(),
            extractRoles(jwt),
            extractPermissions(jwt),
            new Date(jwt.getIssuedAtTime() * 1000),
            new Date(jwt.getExpirationTime() * 1000),
            isTokenExpired(jwt)
        );
    }
    
    /**
     * トークン情報を保持するクラス
     */
    public static class TokenInfo {
        private final UUID userId;
        private final String username;
        private final Set<String> roles;
        private final Set<String> permissions;
        private final Date issuedAt;
        private final Date expiresAt;
        private final boolean expired;
        
        public TokenInfo(UUID userId, String username, Set<String> roles, Set<String> permissions,
                        Date issuedAt, Date expiresAt, boolean expired) {
            this.userId = userId;
            this.username = username;
            this.roles = roles;
            this.permissions = permissions;
            this.issuedAt = issuedAt;
            this.expiresAt = expiresAt;
            this.expired = expired;
        }
        
        // Getters
        public UUID getUserId() { return userId; }
        public String getUsername() { return username; }
        public Set<String> getRoles() { return roles; }
        public Set<String> getPermissions() { return permissions; }
        public Date getIssuedAt() { return issuedAt; }
        public Date getExpiresAt() { return expiresAt; }
        public boolean isExpired() { return expired; }
    }
}
