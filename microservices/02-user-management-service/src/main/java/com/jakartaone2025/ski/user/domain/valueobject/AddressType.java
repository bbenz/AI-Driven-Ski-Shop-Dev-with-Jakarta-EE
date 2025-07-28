package com.jakartaone2025.ski.user.domain.valueobject;

import java.util.Arrays;

/**
 * 住所タイプ
 */
public enum AddressType {
    HOME("自宅"),
    WORK("勤務先"),
    SHIPPING("配送先"),
    BILLING("請求先"),
    OTHER("その他");
    
    private final String displayName;
    
    AddressType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * 表示名から対応するAddressTypeを取得
     * @param displayName 表示名
     * @return 対応するAddressType、見つからない場合はnull
     */
    public static AddressType fromDisplayName(String displayName) {
        return Arrays.stream(values())
                .filter(type -> type.displayName.equals(displayName))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * デフォルト住所として使用可能かチェック
     * @return デフォルト住所として使用可能な場合true
     */
    public boolean canBeDefault() {
        return this == HOME || this == SHIPPING;
    }
    
    /**
     * 配送先として使用可能かチェック
     * @return 配送先として使用可能な場合true
     */
    public boolean canBeShipping() {
        return this == HOME || this == WORK || this == SHIPPING;
    }
    
    /**
     * 請求先として使用可能かチェック
     * @return 請求先として使用可能な場合true
     */
    public boolean canBeBilling() {
        return this == HOME || this == WORK || this == BILLING;
    }
}
