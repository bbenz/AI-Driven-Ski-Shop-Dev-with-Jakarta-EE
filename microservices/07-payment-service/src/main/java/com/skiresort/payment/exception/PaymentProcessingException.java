package com.skiresort.payment.exception;

/**
 * 決済処理エラーの例外
 */
public class PaymentProcessingException extends RuntimeException {
    
    private final String errorCode;
    private final String providerError;
    
    public PaymentProcessingException(String message) {
        super(message);
        this.errorCode = null;
        this.providerError = null;
    }
    
    public PaymentProcessingException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.providerError = null;
    }
    
    public PaymentProcessingException(String message, String errorCode, String providerError) {
        super(message);
        this.errorCode = errorCode;
        this.providerError = providerError;
    }
    
    public PaymentProcessingException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = null;
        this.providerError = null;
    }
    
    public PaymentProcessingException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.providerError = null;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public String getProviderError() {
        return providerError;
    }
}
