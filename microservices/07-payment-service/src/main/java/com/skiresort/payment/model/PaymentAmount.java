package com.skiresort.payment.model;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 決済金額を表す値オブジェクト
 */
@Embeddable
public record PaymentAmount(
    
    @NotNull
    @DecimalMin(value = "0.01", message = "金額は0.01以上である必要があります")
    @Digits(integer = 10, fraction = 2, message = "金額の桁数が正しくありません")
    BigDecimal amount,
    
    @NotNull
    String currency,
    
    @DecimalMin(value = "0.00")
    @Digits(integer = 8, fraction = 2)
    BigDecimal taxAmount,
    
    @DecimalMin(value = "0.00") 
    @Digits(integer = 8, fraction = 2)
    BigDecimal feeAmount
) {
    
    public PaymentAmount {
        if (amount == null) {
            throw new IllegalArgumentException("金額は必須です");
        }
        if (currency == null || currency.trim().isEmpty()) {
            throw new IllegalArgumentException("通貨は必須です");
        }
        if (taxAmount == null) {
            taxAmount = BigDecimal.ZERO;
        }
        if (feeAmount == null) {
            feeAmount = BigDecimal.ZERO;
        }
    }
    
    /**
     * 合計金額を計算
     */
    public BigDecimal totalAmount() {
        return amount.add(taxAmount).add(feeAmount);
    }
    
    /**
     * 基本金額のみ
     */
    public BigDecimal baseAmount() {
        return amount;
    }
    
    /**
     * JPY通貨で作成
     */
    public static PaymentAmount jpy(BigDecimal amount) {
        return new PaymentAmount(amount, "JPY", BigDecimal.ZERO, BigDecimal.ZERO);
    }
    
    /**
     * JPY通貨で税込み金額で作成
     */
    public static PaymentAmount jpyWithTax(BigDecimal amount, BigDecimal taxAmount) {
        return new PaymentAmount(amount, "JPY", taxAmount, BigDecimal.ZERO);
    }
    
    /**
     * USD通貨で作成
     */
    public static PaymentAmount usd(BigDecimal amount) {
        return new PaymentAmount(amount, "USD", BigDecimal.ZERO, BigDecimal.ZERO);
    }
    
    /**
     * 金額が0より大きいかチェック
     */
    public boolean isPositive() {
        return totalAmount().compareTo(BigDecimal.ZERO) > 0;
    }
    
    /**
     * 他の金額と比較
     */
    public int compareAmount(PaymentAmount other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("異なる通貨同士は比較できません");
        }
        return this.totalAmount().compareTo(other.totalAmount());
    }
}
