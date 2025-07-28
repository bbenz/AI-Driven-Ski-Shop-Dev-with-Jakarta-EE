package com.skishop.cart.exception;

/**
 * Exception thrown when there's an error processing cart operations
 */
public class CartProcessingException extends RuntimeException {
    
    public CartProcessingException(String message) {
        super(message);
    }
    
    public CartProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
