package com.skishop.cart.exception;

/**
 * Exception thrown when a shopping cart is not found
 */
public class CartNotFoundException extends RuntimeException {
    
    public CartNotFoundException(String message) {
        super(message);
    }
    
    public CartNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
