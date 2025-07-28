package com.skiresort.payment.model;

/**
 * 決済手段列挙型
 */
public enum PaymentMethod {
    CREDIT_CARD("クレジットカード"),
    DEBIT_CARD("デビットカード"),
    PAYPAL("PayPal"),
    BANK_TRANSFER("銀行振込"),
    CONVENIENCE_STORE("コンビニ決済"),
    ELECTRONIC_MONEY("電子マネー"),
    RAKUTEN_PAY("楽天ペイ"),
    APPLE_PAY("Apple Pay"),
    GOOGLE_PAY("Google Pay");
    
    private final String displayName;
    
    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * オンライン決済可能かどうか
     */
    public boolean isOnlinePayment() {
        return switch (this) {
            case CREDIT_CARD, DEBIT_CARD, PAYPAL, ELECTRONIC_MONEY, 
                 RAKUTEN_PAY, APPLE_PAY, GOOGLE_PAY -> true;
            case BANK_TRANSFER, CONVENIENCE_STORE -> false;
        };
    }
    
    /**
     * 即座に処理されるかどうか
     */
    public boolean isInstantPayment() {
        return switch (this) {
            case CREDIT_CARD, DEBIT_CARD, PAYPAL, ELECTRONIC_MONEY,
                 APPLE_PAY, GOOGLE_PAY -> true;
            case BANK_TRANSFER, CONVENIENCE_STORE, RAKUTEN_PAY -> false;
        };
    }
    
    /**
     * 返金サポートがあるかどうか
     */
    public boolean supportsRefund() {
        return switch (this) {
            case CREDIT_CARD, DEBIT_CARD, PAYPAL, RAKUTEN_PAY -> true;
            case BANK_TRANSFER, CONVENIENCE_STORE, ELECTRONIC_MONEY,
                 APPLE_PAY, GOOGLE_PAY -> false;
        };
    }
}
