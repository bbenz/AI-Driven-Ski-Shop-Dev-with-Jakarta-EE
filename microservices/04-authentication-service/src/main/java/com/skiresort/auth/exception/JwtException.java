package com.skiresort.auth.exception;

/**
 * JWT関連の例外の基底クラス
 */
public abstract class JwtException extends RuntimeException {
    
    /**
     * メッセージのみのコンストラクタ
     */
    public JwtException(String message) {
        super(message);
    }
    
    /**
     * メッセージと原因例外のコンストラクタ
     */
    public JwtException(String message, Throwable cause) {
        super(message, cause);
    }
}
