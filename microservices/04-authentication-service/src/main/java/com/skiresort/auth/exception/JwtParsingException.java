package com.skiresort.auth.exception;

/**
 * JWT解析時のエラーを表す例外
 */
public class JwtParsingException extends JwtException {
    
    /**
     * メッセージのみのコンストラクタ
     */
    public JwtParsingException(String message) {
        super(message);
    }
    
    /**
     * メッセージと原因例外のコンストラクタ
     */
    public JwtParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
