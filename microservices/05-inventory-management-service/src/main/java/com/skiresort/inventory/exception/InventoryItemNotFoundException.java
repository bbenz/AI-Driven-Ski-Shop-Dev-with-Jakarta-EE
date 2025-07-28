package com.skiresort.inventory.exception;

/**
 * 在庫アイテムが見つからない例外
 */
public class InventoryItemNotFoundException extends RuntimeException {
    
    private final String sku;
    private final String id;
    
    public InventoryItemNotFoundException(String message) {
        super(message);
        this.sku = null;
        this.id = null;
    }
    
    public InventoryItemNotFoundException(String message, String sku) {
        super(message);
        this.sku = sku;
        this.id = null;
    }
    
    public InventoryItemNotFoundException(String message, String sku, String id) {
        super(message);
        this.sku = sku;
        this.id = id;
    }
    
    public InventoryItemNotFoundException(String message, Throwable cause) {
        super(message, cause);
        this.sku = null;
        this.id = null;
    }
    
    public String getSku() {
        return sku;
    }
    
    public String getId() {
        return id;
    }
    
    public static InventoryItemNotFoundException forSku(String sku) {
        return new InventoryItemNotFoundException(
            String.format("在庫アイテムが見つかりません。SKU: %s", sku), sku);
    }
    
    public static InventoryItemNotFoundException forId(String id) {
        return new InventoryItemNotFoundException(
            String.format("在庫アイテムが見つかりません。ID: %s", id), null, id);
    }
}
