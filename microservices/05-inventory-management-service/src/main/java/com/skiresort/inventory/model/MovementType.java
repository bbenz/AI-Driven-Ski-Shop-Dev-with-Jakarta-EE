package com.skiresort.inventory.model;

/**
 * 在庫移動タイプ列挙型
 */
public enum MovementType {
    INBOUND("入荷"),
    OUTBOUND("出荷"),
    ADJUSTMENT("調整"),
    TRANSFER("移動"),
    RETURN("返品"),
    DAMAGE("損傷"),
    THEFT("紛失");
    
    private final String description;
    
    MovementType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
