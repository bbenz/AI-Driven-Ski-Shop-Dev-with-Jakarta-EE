package com.skiresort.auth.service;

import com.skiresort.auth.entity.*;
import com.skiresort.auth.repository.UserCredentialRepository;
import com.skiresort.auth.repository.OAuth2CredentialRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.security.enterprise.identitystore.Pbkdf2PasswordHash;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;

/**
 * 認証サービス
 * 認証・認可に関するビジネスロジックを提供
 */
@ApplicationScoped
@Transactional
public class AuthenticationService {
    
    private static final Logger logger = Logger.getLogger(AuthenticationService.class.getName());
    
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(30);
    
    @Inject
    private Pbkdf2PasswordHash passwordHash;
    
    @Inject
    private JwtService jwtService;
    
    @Inject
    private UserCredentialRepository userCredentialRepository;
    
    @Inject
    private OAuth2CredentialRepository oauth2CredentialRepository;
    
    /**
     * ユーザー認証（メール・パスワード）
     * 
     * @param email メールアドレス
     * @param password パスワード
     * @return 認証結果
     */
    public AuthenticationResult authenticate(@NotNull String email, @NotNull String password) {
        logger.info("Attempting authentication for email: " + email);
        
        Optional<UserCredential> credentialOpt = userCredentialRepository.findByEmail(email);
        if (credentialOpt.isEmpty()) {
            logger.warning("User not found: " + email);
            return new FailedAuthentication("Invalid credentials", 0, null);
        }
        
        UserCredential credential = credentialOpt.get();
        
        // アカウントロック確認
        if (credential.isAccountLocked()) {
            logger.warning("Account locked: " + email);
            return new FailedAuthentication("Account locked", credential.getFailedAttempts(), credential.getLockedUntil());
        }
        
        // アカウント状態確認
        if (!credential.isActive()) {
            logger.warning("Account inactive: " + email);
            return new FailedAuthentication("Account inactive", 0, null);
        }
        
        // パスワード検証
        if (!passwordHash.verify(password.toCharArray(), credential.getPasswordHash())) {
            logger.warning("Invalid password for: " + email);
            
            // 失敗回数を増加
            credential.incrementFailedAttempts();
            
            // 上限に達した場合はアカウントロック
            if (credential.getFailedAttempts() >= MAX_FAILED_ATTEMPTS) {
                credential.lockAccount(LOCK_DURATION);
                logger.warning("Account locked due to multiple failed attempts: " + email);
            }
            
            userCredentialRepository.save(credential);
            
            return new FailedAuthentication("Invalid credentials", credential.getFailedAttempts(), credential.getLockedUntil());
        }
        
        // 認証成功
        credential.resetFailedAttempts();
        userCredentialRepository.save(credential);
        
        // MFA確認
        if (credential.isMfaEnabled()) {
            logger.info("MFA required for: " + email);
            
            String mfaToken = generateMfaToken();
            return new RequiresMFA(mfaToken, credential.getMfaMethods(), Duration.ofMinutes(5));
        }
        
        // JWT生成
        TokenPair tokens = jwtService.generateTokens(credential.getUserId(), Set.of("user"), Set.of("read", "write"));
        
        logger.info("Authentication successful for: " + email);
        return new SuccessfulAuthentication(
            credential.getUserId().toString(),
            Set.of("user"),
            Set.of("read", "write"),
            tokens,
            LocalDateTime.now()
        );
    }
    
    /**
     * OAuth2認証（内部実装）
     * 
     * @param provider プロバイダー名
     * @param providerUserId プロバイダーユーザーID
     * @param email メールアドレス
     * @param displayName 表示名
     * @return 認証結果
     */
    private AuthenticationResult authenticateOAuth2Internal(
            @NotNull String provider,
            @NotNull String providerUserId,
            String email,
            String displayName) {
        
        logger.info("OAuth2 authentication for provider: " + provider + ", userId: " + providerUserId);
        
        Optional<OAuth2Credential> oauth2CredentialOpt = oauth2CredentialRepository.findByProviderUserIdAndProvider(providerUserId, provider);
        
        UUID userId;
        if (oauth2CredentialOpt.isPresent()) {
            // 既存ユーザー
            OAuth2Credential oauth2Credential = oauth2CredentialOpt.get();
            
            if (!oauth2Credential.isActive()) {
                logger.warning("OAuth2 credential inactive: " + provider + "/" + providerUserId);
                return new FailedAuthentication("OAuth2 credential inactive", 0, null);
            }
            
            // プロファイル情報更新
            oauth2Credential.updateProfile(email, displayName, null);
            oauth2Credential.setLastAuthenticatedAt(LocalDateTime.now());
            oauth2CredentialRepository.save(oauth2Credential);
            
            userId = oauth2Credential.getUserId();
        } else {
            // 新規ユーザー
            userId = UUID.randomUUID();
            
            OAuth2Credential newOAuth2Credential = new OAuth2Credential(userId, provider, providerUserId);
            newOAuth2Credential.updateProfile(email, displayName, null);
            oauth2CredentialRepository.save(newOAuth2Credential);
            
            logger.info("New OAuth2 user created: " + userId);
        }
        
        // JWT生成
        TokenPair tokens = jwtService.generateTokens(userId, Set.of("user"), Set.of("read", "write"));
        
        logger.info("OAuth2 authentication successful for: " + provider + "/" + providerUserId);
        return new SuccessfulAuthentication(
            userId.toString(),
            Set.of("user"),
            Set.of("read", "write"),
            tokens,
            LocalDateTime.now()
        );
    }
    
    /**
     * トークン更新
     * 
     * @param refreshToken リフレッシュトークン
     * @return 新しいトークンペア
     */
    public TokenPair refreshToken(@NotNull String refreshToken) {
        logger.info("Refreshing token");
        
        if (!jwtService.validateRefreshToken(refreshToken)) {
            throw new AuthenticationException("Invalid refresh token");
        }
        
        UUID userId = jwtService.extractUserIdFromRefreshToken(refreshToken);
        Set<String> roles = jwtService.extractRolesFromRefreshToken(refreshToken);
        Set<String> permissions = jwtService.extractPermissionsFromRefreshToken(refreshToken);
        
        return jwtService.generateTokens(userId, roles, permissions);
    }
    
    /**
     * ユーザー登録
     * 
     * @param email メールアドレス
     * @param password パスワード
     * @param userId ユーザーID
     * @return 作成された認証情報
     */
    public UserCredential registerUser(@NotNull String email, @NotNull String password, @NotNull UUID userId) {
        logger.info("Registering new user: " + email);
        
        // メール重複チェック
        if (userCredentialRepository.existsByEmail(email)) {
            throw new AuthenticationException("Email already exists");
        }
        
        // パスワードハッシュ化
        String hashedPassword = passwordHash.generate(password.toCharArray());
        
        // 認証情報作成
        UserCredential credential = new UserCredential(userId, email, hashedPassword);
        
        // メール確認トークン生成
        String verificationToken = generateEmailVerificationToken();
        credential.setEmailVerificationToken(verificationToken);
        credential.setEmailVerificationExpiresAt(LocalDateTime.now().plus(Duration.ofHours(24)));
        
        UserCredential savedCredential = userCredentialRepository.save(credential);
        
        logger.info("User registered successfully: " + email);
        return savedCredential;
    }
    
    /**
     * パスワード変更
     * 
     * @param userId ユーザーID
     * @param currentPassword 現在のパスワード
     * @param newPassword 新しいパスワード
     */
    public void changePassword(@NotNull UUID userId, @NotNull String currentPassword, @NotNull String newPassword) {
        logger.info("Changing password for user: " + userId);
        
        UserCredential credential = userCredentialRepository.findByUserId(userId)
            .orElseThrow(() -> new AuthenticationException("User not found"));
        
        // 現在のパスワード確認
        if (!passwordHash.verify(currentPassword.toCharArray(), credential.getPasswordHash())) {
            throw new AuthenticationException("Invalid current password");
        }
        
        // 新しいパスワードハッシュ化
        String hashedNewPassword = passwordHash.generate(newPassword.toCharArray());
        credential.changePassword(hashedNewPassword);
        
        userCredentialRepository.save(credential);
        
        logger.info("Password changed successfully for user: " + userId);
    }
    
    /**
     * メール確認（内部実装）
     * 
     * @param token 確認トークン
     */
    private void verifyEmailInternal(@NotNull String token) {
        logger.info("Verifying email with token");
        
        UserCredential credential = userCredentialRepository.findByEmailVerificationToken(token)
            .orElseThrow(() -> new AuthenticationException("Invalid verification token"));
        
        if (credential.getEmailVerificationExpiresAt().isBefore(LocalDateTime.now())) {
            throw new AuthenticationException("Verification token expired");
        }
        
        credential.verifyEmail();
        userCredentialRepository.save(credential);
        
        logger.info("Email verified successfully for user: " + credential.getUserId());
    }
    
    /**
     * ユーザー登録（REST API用）
     * 
     * @param username ユーザー名
     * @param email メールアドレス  
     * @param password パスワード
     * @return 認証結果
     */
    public AuthenticationResult registerUser(@NotNull String username, @NotNull String email, @NotNull String password) {
        logger.info("Registering new user: " + username + " (" + email + ")");
        
        // ユーザー名重複チェック
        if (userCredentialRepository.existsByUsername(username)) {
            return new FailedAuthentication("Username already exists", 0, null);
        }
        
        // メール重複チェック
        if (userCredentialRepository.existsByEmail(email)) {
            return new FailedAuthentication("Email already exists", 0, null);
        }
        
        // パスワードハッシュ化
        String hashedPassword = passwordHash.generate(password.toCharArray());
        
        // 認証情報作成
        UUID userId = UUID.randomUUID();
        UserCredential credential = new UserCredential(userId, username, email, hashedPassword);
        
        // メール確認トークン生成
        String verificationToken = generateEmailVerificationToken();
        credential.setEmailVerificationToken(verificationToken);
        credential.setEmailVerificationExpiresAt(LocalDateTime.now().plus(Duration.ofHours(24)));
        
        userCredentialRepository.save(credential);
        
        logger.info("User registered successfully: " + username);
        return new SuccessfulAuthentication(
            userId.toString(),
            Set.of("user"),
            Set.of("read"),
            null, // トークンは登録時には生成しない
            LocalDateTime.now()
        );
    }
    
    /**
     * MFA認証
     * 
     * @param sessionId MFAセッションID
     * @param mfaCode MFAコード
     * @return 認証結果
     */
    public AuthenticationResult verifyMfa(@NotNull String sessionId, @NotNull String mfaCode) {
        logger.info("Verifying MFA for session: " + sessionId);
        
        // 実際の実装では、sessionIdからユーザー情報を取得し、MFAコードを検証
        // ここでは簡略化したダミー実装
        if ("123456".equals(mfaCode)) {
            UUID userId = UUID.randomUUID();
            TokenPair tokens = jwtService.generateTokens(userId, Set.of("user"), Set.of("read", "write"));
            
            return new SuccessfulAuthentication(
                userId.toString(),
                Set.of("user"),
                Set.of("read", "write"),
                tokens,
                LocalDateTime.now()
            );
        } else {
            return new FailedAuthentication("Invalid MFA code", 0, null);
        }
    }
    
    /**
     * OAuth2認証（REST API用）
     * 
     * @param provider プロバイダー名
     * @param authorizationCode 認証コード
     * @param redirectUri リダイレクトURI
     * @return 認証結果
     */
    public AuthenticationResult authenticateOAuth2(@NotNull String provider, @NotNull String authorizationCode, @NotNull String redirectUri) {
        return authenticateOAuth2(provider, authorizationCode, redirectUri, null);
    }
    
    /**
     * OAuth2認証（内部用）
     * 
     * @param provider プロバイダー名
     * @param authorizationCode 認証コード  
     * @param redirectUri リダイレクトURI
     * @param state ステート（CSRF防止）
     * @return 認証結果
     */
    public AuthenticationResult authenticateOAuth2(@NotNull String provider, @NotNull String authorizationCode, String redirectUri, String state) {
        logger.info("OAuth2 authentication for provider: " + provider);
        
        // OAuth2プロバイダーから情報を取得（簡略化）
        String providerUserId = "oauth_user_" + System.currentTimeMillis();
        String email = "oauth@example.com";
        String displayName = "OAuth User";
        
        return authenticateOAuth2Internal(provider, providerUserId, email, displayName);
    }
    
    /**
     * トークン取り消し
     * 
     * @param token トークン
     */
    public void revokeToken(@NotNull String token) {
        logger.info("Revoking token");
        // 実際の実装ではトークンブラックリストに追加
        // Redis等のキャッシュに取り消されたトークンを保存
    }
    
    /**
     * パスワードリセット要求
     * 
     * @param email メールアドレス
     */
    public void requestPasswordReset(@NotNull String email) {
        logger.info("Password reset request for: " + email);
        
        Optional<UserCredential> credentialOpt = userCredentialRepository.findByEmail(email);
        if (credentialOpt.isPresent()) {
            UserCredential credential = credentialOpt.get();
            
            // パスワードリセットトークン生成
            String resetToken = generatePasswordResetToken();
            credential.setPasswordResetToken(resetToken);
            credential.setPasswordResetExpiresAt(LocalDateTime.now().plus(Duration.ofHours(1)));
            
            userCredentialRepository.save(credential);
            
            // メール送信（実際の実装）
            logger.info("Password reset email sent to: " + email);
        }
        // セキュリティ上、存在しないメールでもエラーにしない
    }
    
    /**
     * パスワードリセット実行
     * 
     * @param token リセットトークン
     * @param newPassword 新しいパスワード
     * @return 成功フラグ
     */
    public boolean resetPassword(@NotNull String token, @NotNull String newPassword) {
        logger.info("Resetting password with token");
        
        Optional<UserCredential> credentialOpt = userCredentialRepository.findByPasswordResetToken(token);
        if (credentialOpt.isEmpty()) {
            return false;
        }
        
        UserCredential credential = credentialOpt.get();
        
        if (credential.getPasswordResetExpiresAt().isBefore(LocalDateTime.now())) {
            return false;
        }
        
        // 新しいパスワードハッシュ化
        String hashedNewPassword = passwordHash.generate(newPassword.toCharArray());
        credential.changePassword(hashedNewPassword);
        
        // リセットトークンをクリア
        credential.setPasswordResetToken(null);
        credential.setPasswordResetExpiresAt(null);
        
        userCredentialRepository.save(credential);
        
        logger.info("Password reset successful for user: " + credential.getUserId());
        return true;
    }
    
    /**
     * メール確認（REST API用）
     * 
     * @param token 確認トークン
     * @return 成功フラグ
     */
    public boolean verifyEmail(@NotNull String token) {
        logger.info("Verifying email with token");
        
        Optional<UserCredential> credentialOpt = userCredentialRepository.findByEmailVerificationToken(token);
        if (credentialOpt.isEmpty()) {
            return false;
        }
        
        UserCredential credential = credentialOpt.get();
        
        if (credential.getEmailVerificationExpiresAt().isBefore(LocalDateTime.now())) {
            return false;
        }
        
        credential.verifyEmail();
        userCredentialRepository.save(credential);
        
        logger.info("Email verified successfully for user: " + credential.getUserId());
        return true;
    }
    
    // プライベートメソッド
    private String generatePasswordResetToken() {
        return UUID.randomUUID().toString();
    }
    private String generateMfaToken() {
        // MFAトークン生成ロジック（実際の実装ではセキュアなランダム生成）
        return UUID.randomUUID().toString();
    }
    
    private String generateEmailVerificationToken() {
        // メール確認トークン生成ロジック
        return UUID.randomUUID().toString();
    }
    
    // 認証結果クラス群
    public sealed interface AuthenticationResult
        permits SuccessfulAuthentication, FailedAuthentication, RequiresMFA {
    }
    
    public record SuccessfulAuthentication(
        String userId,
        Set<String> roles,
        Set<String> permissions,
        TokenPair tokens,
        LocalDateTime authenticatedAt
    ) implements AuthenticationResult {}
    
    public record FailedAuthentication(
        String reason,
        int attemptCount,
        LocalDateTime nextAttemptAllowed
    ) implements AuthenticationResult {}
    
    public record RequiresMFA(
        String mfaToken,
        Set<MfaMethod> availableMethods,
        Duration expiresIn
    ) implements AuthenticationResult {}
    
    public record TokenPair(
        String accessToken,
        String refreshToken,
        String tokenType,
        Duration expiresIn,
        Set<String> scopes
    ) {
        public static TokenPair create(String accessToken, String refreshToken, Duration expiresIn) {
            return new TokenPair(accessToken, refreshToken, "Bearer", expiresIn, Set.of());
        }
    }
    
    // カスタム例外
    public static class AuthenticationException extends RuntimeException {
        public AuthenticationException(String message) {
            super(message);
        }
    }
}
