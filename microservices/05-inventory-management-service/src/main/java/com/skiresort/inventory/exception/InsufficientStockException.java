package com.skiresort.inventory.exception;

/**
 * 在庫不足例外
 */
public class InsufficientStockException extends RuntimeException {
    
    private final String sku;
    private final Integer requestedQuantity;
    private final Integer availableQuantity;
    
    public InsufficientStockException(String message) {
        super(message);
        this.sku = null;
        this.requestedQuantity = null;
        this.availableQuantity = null;
    }
    
    public InsufficientStockException(String sku, Integer requestedQuantity, Integer availableQuantity) {
        super(String.format("在庫不足です。SKU: %s, 要求数量: %d, 利用可能数量: %d", 
            sku, requestedQuantity, availableQuantity));
        this.sku = sku;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
    }
    
    public InsufficientStockException(String message, Throwable cause) {
        super(message, cause);
        this.sku = null;
        this.requestedQuantity = null;
        this.availableQuantity = null;
    }
    
    public String getSku() {
        return sku;
    }
    
    public Integer getRequestedQuantity() {
        return requestedQuantity;
    }
    
    public Integer getAvailableQuantity() {
        return availableQuantity;
    }
}
