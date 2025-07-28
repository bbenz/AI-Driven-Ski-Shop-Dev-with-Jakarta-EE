package com.skiresort.payment.model;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * 決済詳細情報（暗号化対応）
 */
@Embeddable
public record PaymentDetails(
    @Column(name = "encrypted_card_number", length = 500)
    @Size(max = 500)
    String cardNumber,
    
    @Column(name = "expiry_month")
    @Min(1)
    @Max(12)
    Integer expiryMonth,
    
    @Column(name = "expiry_year")
    @Min(2024)
    @Max(2099)
    Integer expiryYear,
    
    @Column(name = "encrypted_cvv", length = 200)
    @Size(max = 200)
    String cvv,
    
    @Column(name = "card_holder_name", length = 100)
    @Size(max = 100)
    String cardHolderName,
    
    @Column(name = "billing_address", length = 500)
    @Size(max = 500)
    String billingAddress,
    
    @Column(name = "is_stored")
    Boolean isStored
) {
    
    public PaymentDetails {
        // デフォルト値の設定
        if (isStored == null) {
            isStored = false;
        }
    }
    
    /**
     * カード番号の最後4桁を取得する（復号化後）
     */
    public String getLastFourDigits(String decryptedCardNumber) {
        if (decryptedCardNumber == null || decryptedCardNumber.length() < 4) {
            return "****";
        }
        String cleaned = decryptedCardNumber.replaceAll("\\D", "");
        return cleaned.substring(Math.max(0, cleaned.length() - 4));
    }
    
    /**
     * カードブランドを判定する（復号化後のカード番号から）
     */
    public String getCardBrand(String decryptedCardNumber) {
        if (decryptedCardNumber == null) {
            return "UNKNOWN";
        }
        
        String cleaned = decryptedCardNumber.replaceAll("\\D", "");
        if (cleaned.startsWith("4")) {
            return "VISA";
        } else if (cleaned.startsWith("5") || cleaned.startsWith("2")) {
            return "MASTERCARD";
        } else if (cleaned.startsWith("3")) {
            return "AMEX";
        } else if (cleaned.startsWith("6")) {
            return "DISCOVER";
        }
        return "UNKNOWN";
    }
    
    /**
     * 有効期限が切れているかチェックする
     */
    public boolean isExpired() {
        if (expiryMonth == null || expiryYear == null) {
            return true;
        }
        
        java.time.LocalDate now = java.time.LocalDate.now();
        java.time.LocalDate expiry = java.time.LocalDate.of(expiryYear, expiryMonth, 1)
            .plusMonths(1).minusDays(1); // 月末まで有効
        
        return now.isAfter(expiry);
    }
}
