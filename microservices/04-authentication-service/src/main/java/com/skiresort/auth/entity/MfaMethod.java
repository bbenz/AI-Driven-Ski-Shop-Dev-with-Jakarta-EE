package com.skiresort.auth.entity;

/**
 * 多要素認証方法列挙型
 */
public enum MfaMethod {
    /**
     * SMS認証
     */
    SMS,
    
    /**
     * メール認証
     */
    EMAIL,
    
    /**
     * TOTP認証（Google Authenticator等）
     */
    TOTP,
    
    /**
     * バックアップコード
     */
    BACKUP_CODE,
    
    /**
     * ハードウェアキー（FIDO2）
     */
    HARDWARE_KEY
}
