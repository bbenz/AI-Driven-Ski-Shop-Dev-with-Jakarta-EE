package com.jakartaone2025.ski.user.domain.repository;

import com.jakartaone2025.ski.user.domain.entity.Address;
import com.jakartaone2025.ski.user.domain.entity.User;
import com.jakartaone2025.ski.user.domain.valueobject.AddressType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 住所リポジトリインターフェース
 */
public interface AddressRepository {
    
    /**
     * 住所を保存
     * @param address 保存する住所
     * @return 保存された住所
     */
    Address save(Address address);
    
    /**
     * IDで住所を検索
     * @param id 住所ID
     * @return 住所（存在しない場合はEmpty）
     */
    Optional<Address> findById(UUID id);
    
    /**
     * ユーザーの全住所を検索
     * @param user ユーザー
     * @return 住所リスト
     */
    List<Address> findByUser(User user);
    
    /**
     * ユーザーIDで住所を検索
     * @param userId ユーザーID
     * @return 住所リスト
     */
    List<Address> findByUserId(UUID userId);
    
    /**
     * ユーザーの特定タイプの住所を検索
     * @param user ユーザー
     * @param type 住所タイプ
     * @return 住所リスト
     */
    List<Address> findByUserAndType(User user, AddressType type);
    
    /**
     * ユーザーのデフォルト住所を検索
     * @param user ユーザー
     * @return デフォルト住所（存在しない場合はEmpty）
     */
    Optional<Address> findDefaultByUser(User user);
    
    /**
     * ユーザーIDでデフォルト住所を検索
     * @param userId ユーザーID
     * @return デフォルト住所（存在しない場合はEmpty）
     */
    Optional<Address> findDefaultByUserId(UUID userId);
    
    /**
     * ユーザーの自宅住所を検索
     * @param user ユーザー
     * @return 自宅住所（存在しない場合はEmpty）
     */
    Optional<Address> findHomeAddressByUser(User user);
    
    /**
     * ユーザーの勤務先住所を検索
     * @param user ユーザー
     * @return 勤務先住所（存在しない場合はEmpty）
     */
    Optional<Address> findWorkAddressByUser(User user);
    
    /**
     * 都道府県で住所を検索
     * @param prefecture 都道府県
     * @return 住所リスト
     */
    List<Address> findByPrefecture(String prefecture);
    
    /**
     * 市区町村で住所を検索
     * @param city 市区町村
     * @return 住所リスト
     */
    List<Address> findByCity(String city);
    
    /**
     * 郵便番号で住所を検索
     * @param postalCode 郵便番号
     * @return 住所リスト
     */
    List<Address> findByPostalCode(String postalCode);
    
    /**
     * 住所タイプで検索
     * @param type 住所タイプ
     * @return 住所リスト
     */
    List<Address> findByType(AddressType type);
    
    /**
     * ユーザーの住所数を取得
     * @param user ユーザー
     * @return 住所数
     */
    long countByUser(User user);
    
    /**
     * ユーザーIDで住所数を取得
     * @param userId ユーザーID
     * @return 住所数
     */
    long countByUserId(UUID userId);
    
    /**
     * ユーザーの特定タイプの住所数を取得
     * @param user ユーザー
     * @param type 住所タイプ
     * @return 住所数
     */
    long countByUserAndType(User user, AddressType type);
    
    /**
     * ユーザーにデフォルト住所が存在するかチェック
     * @param user ユーザー
     * @return デフォルト住所が存在する場合true
     */
    boolean existsDefaultByUser(User user);
    
    /**
     * ユーザーに特定タイプの住所が存在するかチェック
     * @param user ユーザー
     * @param type 住所タイプ
     * @return 特定タイプの住所が存在する場合true
     */
    boolean existsByUserAndType(User user, AddressType type);
    
    /**
     * 住所を削除
     * @param address 削除する住所
     */
    void delete(Address address);
    
    /**
     * IDで住所を削除
     * @param id 住所ID
     */
    void deleteById(UUID id);
    
    /**
     * ユーザーの全住所を削除
     * @param user ユーザー
     */
    void deleteByUser(User user);
    
    /**
     * ユーザーIDで全住所を削除
     * @param userId ユーザーID
     */
    void deleteByUserId(UUID userId);
    
    /**
     * すべての住所を削除（テスト用）
     */
    void deleteAll();
}
