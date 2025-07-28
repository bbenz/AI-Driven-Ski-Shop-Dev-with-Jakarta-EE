package com.skiresort.payment.exception;

/**
 * 決済が見つからない場合の例外
 */
public class PaymentNotFoundException extends RuntimeException {
    
    public PaymentNotFoundException(String message) {
        super(message);
    }
    
    public PaymentNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
