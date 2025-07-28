package com.skiresort.auth.resource;

import com.skiresort.auth.service.AuthenticationService;
import com.skiresort.auth.service.AuthenticationService.*;
import com.skiresort.auth.entity.MfaMethod;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;
import java.time.Duration;
import java.util.Map;

/**
 * 認証API リソースクラス
 * 
 * 認証関連のREST APIエンドポイントを提供
 * - ユーザー登録
 * - ローカル認証 (ユーザー名/パスワード)
 * - OAuth2認証 (Google, Facebook, Twitter)
 * - MFA認証
 * - トークンリフレッシュ
 * - パスワード管理
 */
@Path("/auth")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthenticationResource {
    
    private static final Logger logger = Logger.getLogger(AuthenticationResource.class.getName());
    
    @Inject
    private AuthenticationService authenticationService;
    
    @Context
    private UriInfo uriInfo;
    
    // ===== ユーザー登録 =====
    
    /**
     * 新規ユーザー登録
     * 
     * @param request 登録リクエスト
     * @return 登録結果
     */
    @POST
    @Path("/register")
    public Response registerUser(@Valid RegisterRequest request) {
        try {
            logger.info("User registration request for: " + request.username());
            
            AuthenticationResult result = authenticationService.registerUser(
                request.username(),
                request.email(),
                request.password()
            );
            
            return switch (result) {
                case AuthenticationService.SuccessfulAuthentication success -> {
                    RegisterResponse response = new RegisterResponse(
                        UUID.fromString(success.userId()),
                        "user", // usernameは別途取得
                        "user@example.com", // emailは別途取得  
                        "User registered successfully. Please check your email for verification.",
                        false // email not verified yet
                    );
                    yield Response.status(Response.Status.CREATED).entity(response).build();
                }
                case AuthenticationService.FailedAuthentication failure -> 
                    Response.status(Response.Status.BAD_REQUEST)
                           .entity(new ErrorResponse("REGISTRATION_FAILED", failure.reason()))
                           .build();
                case AuthenticationService.RequiresMFA mfa -> 
                    Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(new ErrorResponse("UNEXPECTED_MFA", "MFA should not be required during registration"))
                           .build();
            };
            
        } catch (Exception e) {
            logger.severe("Registration failed: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity(new ErrorResponse("INTERNAL_ERROR", "Registration failed"))
                          .build();
        }
    }
    
    // ===== ローカル認証 =====
    
    /**
     * ユーザー名/パスワード認証
     * 
     * @param request 認証リクエスト
     * @return 認証結果
     */
    @POST
    @Path("/login")
    public Response authenticateUser(@Valid LoginRequest request) {
        try {
            logger.info("Authentication request for: " + request.username());
            
            AuthenticationResult result = authenticationService.authenticate(
                request.username(),
                request.password()
            );
            
            return switch (result) {
                case AuthenticationService.SuccessfulAuthentication success -> {
                    if (success.tokens() != null) {
                        LoginResponse response = new LoginResponse(
                            success.tokens().accessToken(),
                            success.tokens().refreshToken(),
                            success.tokens().tokenType(),
                            success.tokens().expiresIn().toSeconds(),
                            success.permissions(),
                            UUID.fromString(success.userId()),
                            "user", // username は別途取得
                            "user@example.com" // email は別途取得
                        );
                        yield Response.ok(response).build();
                    } else {
                        yield Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                     .entity(new ErrorResponse("TOKEN_ERROR", "Failed to generate tokens"))
                                     .build();
                    }
                }
                case AuthenticationService.FailedAuthentication failure -> 
                    Response.status(Response.Status.UNAUTHORIZED)
                           .entity(new ErrorResponse("AUTHENTICATION_FAILED", failure.reason()))
                           .build();
                case AuthenticationService.RequiresMFA mfa -> {
                    MfaRequiredResponse response = new MfaRequiredResponse(
                        mfa.mfaToken(),
                        mfa.availableMethods(),
                        "MFA verification required"
                    );
                    yield Response.status(Response.Status.ACCEPTED).entity(response).build();
                }
            };
            
        } catch (Exception e) {
            logger.severe("Authentication failed: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity(new ErrorResponse("INTERNAL_ERROR", "Authentication failed"))
                          .build();
        }
    }
    
    // ===== MFA認証 =====
    
    /**
     * MFA認証コード検証
     * 
     * @param request MFA検証リクエスト
     * @return 認証結果
     */
    @POST
    @Path("/mfa/verify")
    public Response verifyMfa(@Valid MfaVerificationRequest request) {
        try {
            logger.info("MFA verification request for session: " + request.sessionId());
            
            AuthenticationResult result = authenticationService.verifyMfa(
                request.sessionId(),
                request.mfaCode()
            );
            
            return switch (result) {
                case AuthenticationService.SuccessfulAuthentication success -> {
                    if (success.tokens() != null) {
                        LoginResponse response = new LoginResponse(
                            success.tokens().accessToken(),
                            success.tokens().refreshToken(),
                            success.tokens().tokenType(),
                            success.tokens().expiresIn().toSeconds(),
                            success.permissions(),
                            UUID.fromString(success.userId()),
                            "user", // username は別途取得
                            "user@example.com" // email は別途取得
                        );
                        yield Response.ok(response).build();
                    } else {
                        yield Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                     .entity(new ErrorResponse("TOKEN_ERROR", "Failed to generate tokens"))
                                     .build();
                    }
                }
                case AuthenticationService.FailedAuthentication failure -> 
                    Response.status(Response.Status.UNAUTHORIZED)
                           .entity(new ErrorResponse("MFA_VERIFICATION_FAILED", failure.reason()))
                           .build();
                case AuthenticationService.RequiresMFA mfa -> 
                    Response.status(Response.Status.BAD_REQUEST)
                           .entity(new ErrorResponse("INVALID_MFA_CODE", "MFA code verification failed"))
                           .build();
            };
            
        } catch (Exception e) {
            logger.severe("MFA verification failed: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity(new ErrorResponse("INTERNAL_ERROR", "MFA verification failed"))
                          .build();
        }
    }
    
    // ===== OAuth2認証 =====
    
    /**
     * OAuth2認証
     * 
     * @param request OAuth2認証リクエスト
     * @return 認証結果
     */
    @POST
    @Path("/oauth2/authenticate")
    public Response authenticateOAuth2(@Valid OAuth2AuthenticationRequest request) {
        try {
            logger.info("OAuth2 authentication request for provider: " + request.provider());
            
            AuthenticationResult result = authenticationService.authenticateOAuth2(
                request.provider(),
                request.authorizationCode(),
                request.redirectUri()
            );
            
            return switch (result) {
                case AuthenticationService.SuccessfulAuthentication success -> {
                    if (success.tokens() != null) {
                        LoginResponse response = new LoginResponse(
                            success.tokens().accessToken(),
                            success.tokens().refreshToken(),
                            success.tokens().tokenType(),
                            success.tokens().expiresIn().toSeconds(),
                            success.permissions(),
                            UUID.fromString(success.userId()),
                            "user", // username は別途取得
                            "user@example.com" // email は別途取得
                        );
                        yield Response.ok(response).build();
                    } else {
                        yield Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                     .entity(new ErrorResponse("TOKEN_ERROR", "Failed to generate tokens"))
                                     .build();
                    }
                }
                case AuthenticationService.FailedAuthentication failure -> 
                    Response.status(Response.Status.UNAUTHORIZED)
                           .entity(new ErrorResponse("OAUTH2_AUTHENTICATION_FAILED", failure.reason()))
                           .build();
                case AuthenticationService.RequiresMFA mfa -> {
                    MfaRequiredResponse response = new MfaRequiredResponse(
                        mfa.mfaToken(),
                        mfa.availableMethods(),
                        "MFA verification required for OAuth2 authentication"
                    );
                    yield Response.status(Response.Status.ACCEPTED).entity(response).build();
                }
            };
            
        } catch (Exception e) {
            logger.severe("OAuth2 authentication failed: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity(new ErrorResponse("INTERNAL_ERROR", "OAuth2 authentication failed"))
                          .build();
        }
    }
    
    // ===== トークン管理 =====
    
    /**
     * トークンリフレッシュ
     * 
     * @param request リフレッシュリクエスト
     * @return 新しいトークン
     */
    @POST
    @Path("/refresh")
    public Response refreshToken(@Valid RefreshTokenRequest request) {
        try {
            logger.info("Token refresh request");
            
            TokenPair newTokens = authenticationService.refreshToken(request.refreshToken());
            
            RefreshTokenResponse response = new RefreshTokenResponse(
                newTokens.accessToken(),
                newTokens.refreshToken(),
                newTokens.tokenType(),
                newTokens.expiresIn().toSeconds(),
                newTokens.scopes()
            );
            
            return Response.ok(response).build();
            
        } catch (Exception e) {
            logger.severe("Token refresh failed: " + e.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED)
                          .entity(new ErrorResponse("TOKEN_REFRESH_FAILED", "Invalid or expired refresh token"))
                          .build();
        }
    }
    
    /**
     * トークン取り消し
     * 
     * @param request 取り消しリクエスト
     * @return 結果
     */
    @POST
    @Path("/revoke")
    public Response revokeToken(@Valid RevokeTokenRequest request) {
        try {
            logger.info("Token revocation request");
            
            authenticationService.revokeToken(request.token());
            
            return Response.ok(new MessageResponse("Token revoked successfully")).build();
            
        } catch (Exception e) {
            logger.severe("Token revocation failed: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity(new ErrorResponse("TOKEN_REVOCATION_FAILED", "Token revocation failed"))
                          .build();
        }
    }
    
    // ===== パスワード管理 =====
    
    /**
     * パスワードリセット要求
     * 
     * @param request パスワードリセット要求
     * @return 結果
     */
    @POST
    @Path("/password/reset-request")
    public Response requestPasswordReset(@Valid PasswordResetRequest request) {
        try {
            logger.info("Password reset request for: " + request.email());
            
            authenticationService.requestPasswordReset(request.email());
            
            return Response.ok(new MessageResponse("Password reset email sent if account exists")).build();
            
        } catch (Exception e) {
            logger.severe("Password reset request failed: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity(new ErrorResponse("PASSWORD_RESET_REQUEST_FAILED", "Password reset request failed"))
                          .build();
        }
    }
    
    /**
     * パスワードリセット実行
     * 
     * @param request パスワードリセット実行
     * @return 結果
     */
    @POST
    @Path("/password/reset")
    public Response resetPassword(@Valid PasswordResetConfirmRequest request) {
        try {
            logger.info("Password reset confirmation");
            
            boolean success = authenticationService.resetPassword(
                request.token(),
                request.newPassword()
            );
            
            if (success) {
                return Response.ok(new MessageResponse("Password reset successfully")).build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST)
                              .entity(new ErrorResponse("PASSWORD_RESET_FAILED", "Invalid or expired reset token"))
                              .build();
            }
            
        } catch (Exception e) {
            logger.severe("Password reset failed: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity(new ErrorResponse("PASSWORD_RESET_FAILED", "Password reset failed"))
                          .build();
        }
    }
    
    // ===== メール確認 =====
    
    /**
     * メールアドレス確認
     * 
     * @param token 確認トークン
     * @return 結果
     */
    @GET
    @Path("/email/verify")
    public Response verifyEmail(@QueryParam("token") @NotBlank String token) {
        try {
            logger.info("Email verification request");
            
            boolean success = authenticationService.verifyEmail(token);
            
            if (success) {
                return Response.ok(new MessageResponse("Email verified successfully")).build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST)
                              .entity(new ErrorResponse("EMAIL_VERIFICATION_FAILED", "Invalid or expired verification token"))
                              .build();
            }
            
        } catch (Exception e) {
            logger.severe("Email verification failed: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity(new ErrorResponse("EMAIL_VERIFICATION_FAILED", "Email verification failed"))
                          .build();
        }
    }
    
    // ===== DTOクラス =====
    
    // リクエストDTO
    public record RegisterRequest(
        @NotBlank @Size(min = 3, max = 50) String username,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, max = 100) String password
    ) {}
    
    public record LoginRequest(
        @NotBlank String username,
        @NotBlank String password
    ) {}
    
    public record MfaVerificationRequest(
        @NotBlank String sessionId,
        @NotBlank @Size(min = 6, max = 8) String mfaCode
    ) {}
    
    public record OAuth2AuthenticationRequest(
        @NotBlank String provider,
        @NotBlank String authorizationCode,
        @NotBlank String redirectUri
    ) {}
    
    public record RefreshTokenRequest(
        @NotBlank String refreshToken
    ) {}
    
    public record RevokeTokenRequest(
        @NotBlank String token
    ) {}
    
    public record PasswordResetRequest(
        @NotBlank @Email String email
    ) {}
    
    public record PasswordResetConfirmRequest(
        @NotBlank String token,
        @NotBlank @Size(min = 8, max = 100) String newPassword
    ) {}
    
    // レスポンスDTO
    public record RegisterResponse(
        UUID userId,
        String username,
        String email,
        String message,
        boolean emailVerified
    ) {}
    
    public record LoginResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        Long expiresIn,
        Set<String> permissions,
        UUID userId,
        String username,
        String email
    ) {}
    
    public record MfaRequiredResponse(
        String sessionId,
        Set<MfaMethod> availableMethods,
        String message
    ) {}
    
    public record RefreshTokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        Long expiresIn,
        Set<String> permissions
    ) {}
    
    public record ErrorResponse(
        String code,
        String message
    ) {}
    
    public record MessageResponse(
        String message
    ) {}
}
