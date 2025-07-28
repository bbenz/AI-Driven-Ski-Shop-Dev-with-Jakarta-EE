package com.skiresort.auth.entity;

/**
 * 認証情報ステータス列挙型
 */
public enum CredentialStatus {
    /**
     * アクティブ - 通常使用可能
     */
    ACTIVE,
    
    /**
     * 非アクティブ - 一時的に無効
     */
    INACTIVE,
    
    /**
     * 停止 - 管理者により停止
     */
    SUSPENDED,
    
    /**
     * 削除済み - 論理削除
     */
    DELETED
}
