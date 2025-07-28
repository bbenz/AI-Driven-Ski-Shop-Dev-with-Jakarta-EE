package com.skiresort.auth.exception;

/**
 * JWT検証時のエラーを表す例外
 */
public class JwtValidationException extends JwtException {
    
    /**
     * メッセージのみのコンストラクタ
     */
    public JwtValidationException(String message) {
        super(message);
    }
    
    /**
     * メッセージと原因例外のコンストラクタ
     */
    public JwtValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
