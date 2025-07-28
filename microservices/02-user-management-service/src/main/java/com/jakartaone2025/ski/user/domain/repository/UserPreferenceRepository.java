package com.jakartaone2025.ski.user.domain.repository;

import com.jakartaone2025.ski.user.domain.entity.User;
import com.jakartaone2025.ski.user.domain.entity.UserPreference;
import com.jakartaone2025.ski.user.domain.valueobject.LanguageSetting;
import com.jakartaone2025.ski.user.domain.valueobject.ThemeSetting;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * ユーザー設定リポジトリインターフェース
 */
public interface UserPreferenceRepository {
    
    /**
     * ユーザー設定を保存
     * @param preference 保存するユーザー設定
     * @return 保存されたユーザー設定
     */
    UserPreference save(UserPreference preference);
    
    /**
     * IDでユーザー設定を検索
     * @param id ユーザー設定ID
     * @return ユーザー設定（存在しない場合はEmpty）
     */
    Optional<UserPreference> findById(UUID id);
    
    /**
     * ユーザーでユーザー設定を検索
     * @param user ユーザー
     * @return ユーザー設定（存在しない場合はEmpty）
     */
    Optional<UserPreference> findByUser(User user);
    
    /**
     * ユーザーIDでユーザー設定を検索
     * @param userId ユーザーID
     * @return ユーザー設定（存在しない場合はEmpty）
     */
    Optional<UserPreference> findByUserId(UUID userId);
    
    /**
     * 言語設定で検索
     * @param language 言語設定
     * @return ユーザー設定リスト
     */
    List<UserPreference> findByLanguage(LanguageSetting language);
    
    /**
     * テーマ設定で検索
     * @param theme テーマ設定
     * @return ユーザー設定リスト
     */
    List<UserPreference> findByTheme(ThemeSetting theme);
    
    /**
     * タイムゾーンで検索
     * @param timezone タイムゾーン
     * @return ユーザー設定リスト
     */
    List<UserPreference> findByTimezone(String timezone);
    
    /**
     * メール通知が有効なユーザー設定を検索
     * @return ユーザー設定リスト
     */
    List<UserPreference> findByEmailNotificationsEnabled();
    
    /**
     * SMS通知が有効なユーザー設定を検索
     * @return ユーザー設定リスト
     */
    List<UserPreference> findBySmsNotificationsEnabled();
    
    /**
     * プッシュ通知が有効なユーザー設定を検索
     * @return ユーザー設定リスト
     */
    List<UserPreference> findByPushNotificationsEnabled();
    
    /**
     * マーケティング通知が有効なユーザー設定を検索
     * @return ユーザー設定リスト
     */
    List<UserPreference> findByMarketingNotificationsEnabled();
    
    /**
     * マーケティングメールの同意があるユーザー設定を検索
     * @return ユーザー設定リスト
     */
    List<UserPreference> findByMarketingEmailsConsent();
    
    /**
     * アナリティクスが有効なユーザー設定を検索
     * @return ユーザー設定リスト
     */
    List<UserPreference> findByAnalyticsEnabled();
    
    /**
     * チュートリアル表示が有効なユーザー設定を検索
     * @return ユーザー設定リスト
     */
    List<UserPreference> findByShowTutorialEnabled();
    
    /**
     * 全ユーザー設定数を取得
     * @return ユーザー設定数
     */
    long countAll();
    
    /**
     * 言語設定別ユーザー数を取得
     * @param language 言語設定
     * @return ユーザー数
     */
    long countByLanguage(LanguageSetting language);
    
    /**
     * テーマ設定別ユーザー数を取得
     * @param theme テーマ設定
     * @return ユーザー数
     */
    long countByTheme(ThemeSetting theme);
    
    /**
     * メール通知有効ユーザー数を取得
     * @return ユーザー数
     */
    long countByEmailNotificationsEnabled();
    
    /**
     * マーケティングメール同意ユーザー数を取得
     * @return ユーザー数
     */
    long countByMarketingEmailsConsent();
    
    /**
     * ユーザーにユーザー設定が存在するかチェック
     * @param user ユーザー
     * @return ユーザー設定が存在する場合true
     */
    boolean existsByUser(User user);
    
    /**
     * ユーザーIDでユーザー設定が存在するかチェック
     * @param userId ユーザーID
     * @return ユーザー設定が存在する場合true
     */
    boolean existsByUserId(UUID userId);
    
    /**
     * ユーザー設定を削除
     * @param preference 削除するユーザー設定
     */
    void delete(UserPreference preference);
    
    /**
     * IDでユーザー設定を削除
     * @param id ユーザー設定ID
     */
    void deleteById(UUID id);
    
    /**
     * ユーザーでユーザー設定を削除
     * @param user ユーザー
     */
    void deleteByUser(User user);
    
    /**
     * ユーザーIDでユーザー設定を削除
     * @param userId ユーザーID
     */
    void deleteByUserId(UUID userId);
    
    /**
     * すべてのユーザー設定を削除（テスト用）
     */
    void deleteAll();
}
