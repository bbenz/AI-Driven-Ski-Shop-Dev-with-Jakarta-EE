package com.skiresort.payment.exception;

/**
 * 無効な決済状態での操作時の例外
 */
public class InvalidPaymentStateException extends RuntimeException {
    
    public InvalidPaymentStateException(String message) {
        super(message);
    }
    
    public InvalidPaymentStateException(String message, Throwable cause) {
        super(message, cause);
    }
}
