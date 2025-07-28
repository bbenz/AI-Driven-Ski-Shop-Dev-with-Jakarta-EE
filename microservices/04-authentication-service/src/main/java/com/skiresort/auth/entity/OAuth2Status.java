package com.skiresort.auth.entity;

/**
 * OAuth2認証ステータス列挙型
 */
public enum OAuth2Status {
    /**
     * アクティブ - 通常使用可能
     */
    ACTIVE,
    
    /**
     * 非アクティブ - 一時的に無効
     */
    INACTIVE,
    
    /**
     * 取り消し済み - プロバイダーでの認証が取り消し
     */
    REVOKED,
    
    /**
     * 削除済み - 論理削除
     */
    DELETED
}
