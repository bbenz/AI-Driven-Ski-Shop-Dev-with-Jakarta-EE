package com.skiresort.payment.dto;

import com.skiresort.payment.model.PaymentMethod;
import com.skiresort.payment.model.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 決済レスポンスDTO
 */
public record PaymentResponse(
    UUID id,
    String paymentId,
    UUID orderId,
    UUID customerId,
    BigDecimal amount,
    String currency,
    BigDecimal taxAmount,
    PaymentMethod paymentMethod,
    PaymentStatus status,
    String provider,
    String providerTransactionId,
    String authorizationCode,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    LocalDateTime authorizedAt,
    LocalDateTime capturedAt,
    LocalDateTime expiresAt,
    String failureReason,
    String maskedCardNumber
) {
    
    /**
     * 決済統計レスポンスDTO
     */
    public record PaymentStatsResponse(
        long totalCount,
        long successCount,
        long failedCount,
        long pendingCount,
        BigDecimal totalAmount,
        BigDecimal successAmount,
        BigDecimal successRate
    ) {}
}
