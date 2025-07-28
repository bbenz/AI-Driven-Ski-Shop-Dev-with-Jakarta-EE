package com.skiresort.auth.repository;

import com.skiresort.auth.entity.OAuth2Credential;
import com.skiresort.auth.entity.OAuth2Status;
import java.util.Optional;
import java.util.UUID;
import java.util.List;
import java.time.LocalDateTime;

/**
 * OAuth2認証情報リポジトリインターフェース
 */
public interface OAuth2CredentialRepository {
    
    /**
     * IDでOAuth2認証情報を検索
     * 
     * @param id OAuth2認証情報ID
     * @return OAuth2認証情報
     */
    Optional<OAuth2Credential> findById(UUID id);
    
    /**
     * ユーザーIDでOAuth2認証情報を検索
     * 
     * @param userId ユーザーID
     * @return OAuth2認証情報のリスト
     */
    List<OAuth2Credential> findByUserId(UUID userId);
    
    /**
     * ユーザーIDとプロバイダーでOAuth2認証情報を検索
     * 
     * @param userId ユーザーID
     * @param provider プロバイダー名
     * @return OAuth2認証情報
     */
    Optional<OAuth2Credential> findByUserIdAndProvider(UUID userId, String provider);
    
    /**
     * プロバイダーユーザーIDでOAuth2認証情報を検索
     * 
     * @param providerUserId プロバイダーユーザーID
     * @param provider プロバイダー名
     * @return OAuth2認証情報
     */
    Optional<OAuth2Credential> findByProviderUserIdAndProvider(String providerUserId, String provider);
    
    /**
     * プロバイダーメールアドレスでOAuth2認証情報を検索
     * 
     * @param email プロバイダーメールアドレス
     * @param provider プロバイダー名
     * @return OAuth2認証情報
     */
    Optional<OAuth2Credential> findByProviderEmailAndProvider(String email, String provider);
    
    /**
     * プロバイダー別OAuth2認証情報を検索
     * 
     * @param provider プロバイダー名
     * @return OAuth2認証情報のリスト
     */
    List<OAuth2Credential> findByProvider(String provider);
    
    /**
     * ステータス別OAuth2認証情報を検索
     * 
     * @param status ステータス
     * @return OAuth2認証情報のリスト
     */
    List<OAuth2Credential> findByStatus(OAuth2Status status);
    
    /**
     * 指定期間内に作成されたOAuth2認証情報を検索
     * 
     * @param startDate 開始日時
     * @param endDate 終了日時
     * @return OAuth2認証情報のリスト
     */
    List<OAuth2Credential> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * 指定期間内にログインしたOAuth2認証情報を検索
     * 
     * @param startDate 開始日時
     * @param endDate 終了日時
     * @return OAuth2認証情報のリスト
     */
    List<OAuth2Credential> findByLastLoginBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * 期限切れトークンを持つOAuth2認証情報を検索
     * 
     * @param expirationTime 期限切れ時間
     * @return OAuth2認証情報のリスト
     */
    List<OAuth2Credential> findExpiredTokens(LocalDateTime expirationTime);
    
    /**
     * リフレッシュトークンが期限切れのOAuth2認証情報を検索
     * 
     * @param expirationTime 期限切れ時間
     * @return OAuth2認証情報のリスト
     */
    List<OAuth2Credential> findExpiredRefreshTokens(LocalDateTime expirationTime);
    
    /**
     * OAuth2認証情報を保存
     * 
     * @param oauth2Credential OAuth2認証情報
     * @return 保存されたOAuth2認証情報
     */
    OAuth2Credential save(OAuth2Credential oauth2Credential);
    
    /**
     * OAuth2認証情報を更新
     * 
     * @param oauth2Credential OAuth2認証情報
     * @return 更新されたOAuth2認証情報
     */
    OAuth2Credential update(OAuth2Credential oauth2Credential);
    
    /**
     * OAuth2認証情報を削除
     * 
     * @param id OAuth2認証情報ID
     */
    void deleteById(UUID id);
    
    /**
     * ユーザーIDで全OAuth2認証情報を削除
     * 
     * @param userId ユーザーID
     */
    void deleteByUserId(UUID userId);
    
    /**
     * ユーザーIDとプロバイダーでOAuth2認証情報を削除
     * 
     * @param userId ユーザーID
     * @param provider プロバイダー名
     */
    void deleteByUserIdAndProvider(UUID userId, String provider);
    
    /**
     * プロバイダーユーザーIDの存在確認
     * 
     * @param providerUserId プロバイダーユーザーID
     * @param provider プロバイダー名
     * @return 存在する場合true
     */
    boolean existsByProviderUserIdAndProvider(String providerUserId, String provider);
    
    /**
     * ユーザーIDとプロバイダーでの存在確認
     * 
     * @param userId ユーザーID
     * @param provider プロバイダー名
     * @return 存在する場合true
     */
    boolean existsByUserIdAndProvider(UUID userId, String provider);
    
    /**
     * 全件数取得
     * 
     * @return 全件数
     */
    long count();
    
    /**
     * プロバイダー別件数取得
     * 
     * @param provider プロバイダー名
     * @return 件数
     */
    long countByProvider(String provider);
    
    /**
     * ステータス別件数取得
     * 
     * @param status ステータス
     * @return 件数
     */
    long countByStatus(OAuth2Status status);
    
    /**
     * 指定日数以上ログインしていないOAuth2認証情報を検索
     * 
     * @param daysSinceLastLogin 最後のログインからの日数
     * @return OAuth2認証情報のリスト
     */
    List<OAuth2Credential> findInactiveCredentials(int daysSinceLastLogin);
    
    /**
     * ユーザーのアクティブなOAuth2認証情報を検索
     * 
     * @param userId ユーザーID
     * @return OAuth2認証情報のリスト
     */
    List<OAuth2Credential> findActiveByUserId(UUID userId);
    
    /**
     * トークンの有効性を確認
     * 
     * @param accessToken アクセストークン
     * @return 有効な場合true
     */
    boolean isTokenValid(String accessToken);
}
