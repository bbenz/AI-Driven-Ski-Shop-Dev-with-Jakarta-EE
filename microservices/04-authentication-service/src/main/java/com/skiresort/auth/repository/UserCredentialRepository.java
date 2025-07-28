package com.skiresort.auth.repository;

import com.skiresort.auth.entity.UserCredential;
import com.skiresort.auth.entity.CredentialStatus;
import java.util.Optional;
import java.util.UUID;
import java.util.List;
import java.time.LocalDateTime;

/**
 * ユーザー認証情報リポジトリインターフェース
 */
public interface UserCredentialRepository {
    
    /**
     * IDでユーザー認証情報を検索
     * 
     * @param id ユーザーID
     * @return ユーザー認証情報
     */
    Optional<UserCredential> findById(UUID id);
    
    /**
     * ユーザーIDでユーザー認証情報を検索
     * 
     * @param userId ユーザーID
     * @return ユーザー認証情報
     */
    Optional<UserCredential> findByUserId(UUID userId);
    
    /**
     * ユーザー名でユーザー認証情報を検索
     * 
     * @param username ユーザー名
     * @return ユーザー認証情報
     */
    Optional<UserCredential> findByUsername(String username);
    
    /**
     * メールアドレスでユーザー認証情報を検索
     * 
     * @param email メールアドレス
     * @return ユーザー認証情報
     */
    Optional<UserCredential> findByEmail(String email);
    
    /**
     * メール確認トークンでユーザー認証情報を検索
     * 
     * @param token メール確認トークン
     * @return ユーザー認証情報
     */
    Optional<UserCredential> findByEmailVerificationToken(String token);
    
    /**
     * パスワードリセットトークンでユーザー認証情報を検索
     * 
     * @param token パスワードリセットトークン
     * @return ユーザー認証情報
     */
    Optional<UserCredential> findByPasswordResetToken(String token);
    
    /**
     * MFAシークレットキーでユーザー認証情報を検索
     * 
     * @param secretKey MFAシークレットキー
     * @return ユーザー認証情報
     */
    Optional<UserCredential> findByMfaSecretKey(String secretKey);
    
    /**
     * ステータス別ユーザー認証情報を検索
     * 
     * @param status ステータス
     * @return ユーザー認証情報のリスト
     */
    List<UserCredential> findByStatus(CredentialStatus status);
    
    /**
     * 指定期間内に作成されたユーザー認証情報を検索
     * 
     * @param startDate 開始日時
     * @param endDate 終了日時
     * @return ユーザー認証情報のリスト
     */
    List<UserCredential> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * 指定期間内にログインしたユーザー認証情報を検索
     * 
     * @param startDate 開始日時
     * @param endDate 終了日時
     * @return ユーザー認証情報のリスト
     */
    List<UserCredential> findByLastLoginBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * アカウントロック中のユーザー認証情報を検索
     * 
     * @return ユーザー認証情報のリスト
     */
    List<UserCredential> findLockedAccounts();
    
    /**
     * メール未確認のユーザー認証情報を検索
     * 
     * @return ユーザー認証情報のリスト
     */
    List<UserCredential> findUnverifiedEmails();
    
    /**
     * MFA有効なユーザー認証情報を検索
     * 
     * @return ユーザー認証情報のリスト
     */
    List<UserCredential> findMfaEnabled();
    
    /**
     * ユーザー認証情報を保存
     * 
     * @param userCredential ユーザー認証情報
     * @return 保存されたユーザー認証情報
     */
    UserCredential save(UserCredential userCredential);
    
    /**
     * ユーザー認証情報を更新
     * 
     * @param userCredential ユーザー認証情報
     * @return 更新されたユーザー認証情報
     */
    UserCredential update(UserCredential userCredential);
    
    /**
     * ユーザー認証情報を削除
     * 
     * @param id ユーザーID
     */
    void deleteById(UUID id);
    
    /**
     * ユーザー名の存在確認
     * 
     * @param username ユーザー名
     * @return 存在する場合true
     */
    boolean existsByUsername(String username);
    
    /**
     * メールアドレスの存在確認
     * 
     * @param email メールアドレス
     * @return 存在する場合true
     */
    boolean existsByEmail(String email);
    
    /**
     * 全件数取得
     * 
     * @return 全件数
     */
    long count();
    
    /**
     * ステータス別件数取得
     * 
     * @param status ステータス
     * @return 件数
     */
    long countByStatus(CredentialStatus status);
    
    /**
     * 期限切れメール確認トークンを持つユーザーを検索
     * 
     * @param expirationTime 期限切れ時間
     * @return ユーザー認証情報のリスト
     */
    List<UserCredential> findExpiredEmailVerificationTokens(LocalDateTime expirationTime);
    
    /**
     * 期限切れパスワードリセットトークンを持つユーザーを検索
     * 
     * @param expirationTime 期限切れ時間
     * @return ユーザー認証情報のリスト
     */
    List<UserCredential> findExpiredPasswordResetTokens(LocalDateTime expirationTime);
    
    /**
     * 指定日数以上ログインしていないユーザーを検索
     * 
     * @param daysSinceLastLogin 最後のログインからの日数
     * @return ユーザー認証情報のリスト
     */
    List<UserCredential> findInactiveUsers(int daysSinceLastLogin);
}
