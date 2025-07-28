package com.skiresort.payment.dto;

import com.skiresort.payment.model.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * 決済作成リクエストDTO
 */
public record CreatePaymentRequest(
    @NotNull(message = "注文IDは必須です")
    UUID orderId,
    
    @NotNull(message = "顧客IDは必須です")
    UUID customerId,
    
    @NotNull(message = "金額は必須です")
    @Positive(message = "金額は正の値である必要があります")
    BigDecimal amount,
    
    @NotBlank(message = "通貨は必須です")
    @Size(max = 3, message = "通貨コードは3文字以下である必要があります")
    String currency,
    
    BigDecimal taxAmount,
    
    @NotNull(message = "決済方法は必須です")
    PaymentMethod paymentMethod,
    
    @NotBlank(message = "決済プロバイダーは必須です")
    @Size(max = 50, message = "決済プロバイダー名は50文字以下である必要があります")
    String provider,
    
    PaymentDetailsDto paymentDetails
) {
    
    /**
     * 決済詳細DTO
     */
    public record PaymentDetailsDto(
        String cardNumber,
        Integer expiryMonth,
        Integer expiryYear,
        String cvv,
        String cardHolderName,
        String billingAddress,
        Boolean isStored
    ) {}
}
