package com.jakartaone2025.ski.user.domain.repository;

import com.jakartaone2025.ski.user.domain.entity.User;
import com.jakartaone2025.ski.user.domain.valueobject.UserStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * ユーザーリポジトリインターフェース
 */
public interface UserRepository {
    
    /**
     * ユーザーを保存
     * @param user 保存するユーザー
     * @return 保存されたユーザー
     */
    User save(User user);
    
    /**
     * IDでユーザーを検索
     * @param id ユーザーID
     * @return ユーザー（存在しない場合はEmpty）
     */
    Optional<User> findById(UUID id);
    
    /**
     * メールアドレスでユーザーを検索
     * @param email メールアドレス
     * @return ユーザー（存在しない場合はEmpty）
     */
    Optional<User> findByEmail(String email);
    
    /**
     * ユーザー名でユーザーを検索
     * @param username ユーザー名
     * @return ユーザー（存在しない場合はEmpty）
     */
    Optional<User> findByUsername(String username);
    
    /**
     * ステータスでユーザーを検索
     * @param status ユーザーステータス
     * @return ユーザーリスト
     */
    List<User> findByStatus(UserStatus status);
    
    /**
     * アクティブなユーザーを検索
     * @return アクティブなユーザーリスト
     */
    List<User> findActiveUsers();
    
    /**
     * 非アクティブなユーザーを検索
     * @return 非アクティブなユーザーリスト
     */
    List<User> findInactiveUsers();
    
    /**
     * ロックされたユーザーを検索
     * @return ロックされたユーザーリスト
     */
    List<User> findLockedUsers();
    
    /**
     * 指定期間内に作成されたユーザーを検索
     * @param from 開始日時
     * @param to 終了日時
     * @return ユーザーリスト
     */
    List<User> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to);
    
    /**
     * 指定期間内に最終ログインしたユーザーを検索
     * @param from 開始日時
     * @param to 終了日時
     * @return ユーザーリスト
     */
    List<User> findByLastLoginBetween(LocalDateTime from, LocalDateTime to);
    
    /**
     * 指定期間以上ログインしていないユーザーを検索
     * @param before この日時より前に最終ログインしたユーザー
     * @return ユーザーリスト
     */
    List<User> findUsersNotLoggedInSince(LocalDateTime before);
    
    /**
     * 名前で部分一致検索
     * @param name 検索する名前（部分一致）
     * @return ユーザーリスト
     */
    List<User> findByNameContaining(String name);
    
    /**
     * メールアドレスのドメインで検索
     * @param domain ドメイン名
     * @return ユーザーリスト
     */
    List<User> findByEmailDomain(String domain);
    
    /**
     * ページネーションでユーザーを検索
     * @param offset オフセット
     * @param limit 件数制限
     * @return ユーザーリスト
     */
    List<User> findAll(int offset, int limit);
    
    /**
     * 全ユーザー数を取得
     * @return ユーザー数
     */
    long countAll();
    
    /**
     * ステータス別ユーザー数を取得
     * @param status ユーザーステータス
     * @return ユーザー数
     */
    long countByStatus(UserStatus status);
    
    /**
     * メールアドレスの重複チェック
     * @param email チェックするメールアドレス
     * @param excludeUserId 除外するユーザーID（更新時用）
     * @return 重複している場合true
     */
    boolean existsByEmailAndIdNot(String email, UUID excludeUserId);
    
    /**
     * ユーザー名の重複チェック
     * @param username チェックするユーザー名
     * @param excludeUserId 除外するユーザーID（更新時用）
     * @return 重複している場合true
     */
    boolean existsByUsernameAndIdNot(String username, UUID excludeUserId);
    
    /**
     * ユーザーを削除
     * @param user 削除するユーザー
     */
    void delete(User user);
    
    /**
     * IDでユーザーを削除
     * @param id ユーザーID
     */
    void deleteById(UUID id);
    
    /**
     * すべてのユーザーを削除（テスト用）
     */
    void deleteAll();
}
