package com.skiresort.auth.exception;

/**
 * JWT生成時のエラーを表す例外
 */
public class JwtGenerationException extends JwtException {
    
    /**
     * メッセージのみのコンストラクタ
     */
    public JwtGenerationException(String message) {
        super(message);
    }
    
    /**
     * メッセージと原因例外のコンストラクタ
     */
    public JwtGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
