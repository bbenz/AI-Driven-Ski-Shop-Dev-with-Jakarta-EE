package com.skiresort.inventory.model;

/**
 * 在庫ステータス列挙型
 */
public enum InventoryStatus {
    ACTIVE("有効"),
    INACTIVE("無効"),
    DISCONTINUED("生産終了");
    
    private final String description;
    
    InventoryStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
