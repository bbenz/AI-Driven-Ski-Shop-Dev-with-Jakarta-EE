package com.skishop.cart.exception;

/**
 * Exception thrown when a cart item is not found
 */
public class CartItemNotFoundException extends RuntimeException {
    
    public CartItemNotFoundException(String message) {
        super(message);
    }
    
    public CartItemNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
