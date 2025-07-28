package com.skiresort.payment.repository;

import com.skiresort.payment.model.Payment;
import com.skiresort.payment.model.PaymentStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 決済リポジトリインターフェイス
 */
public interface PaymentRepository {
    
    /**
     * 決済を保存する
     */
    Payment save(Payment payment);
    
    /**
     * IDで決済を検索する
     */
    Optional<Payment> findById(UUID id);
    
    /**
     * 決済IDで決済を検索する
     */
    Optional<Payment> findByPaymentId(String paymentId);
    
    /**
     * 注文IDで決済を検索する
     */
    List<Payment> findByOrderId(UUID orderId);
    
    /**
     * 顧客IDで決済一覧を取得する
     */
    List<Payment> findByCustomerId(UUID customerId);
    
    /**
     * ステータスで決済一覧を取得する
     */
    List<Payment> findByStatus(PaymentStatus status);
    
    /**
     * 決済プロバイダーで決済一覧を取得する
     */
    List<Payment> findByProvider(String provider);
    
    /**
     * 日付範囲で決済一覧を取得する
     */
    List<Payment> findByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * プロバイダートランザクションIDで決済を検索する
     */
    Optional<Payment> findByProviderTransactionId(String providerTransactionId);
    
    /**
     * 期限切れの決済一覧を取得する
     */
    List<Payment> findExpiredPayments();
    
    /**
     * 失敗した決済の再試行候補を取得する
     */
    List<Payment> findFailedPaymentsForRetry(LocalDateTime before);
    
    /**
     * 決済統計情報を取得する
     */
    PaymentStats getPaymentStats(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * 決済を削除する
     */
    void delete(Payment payment);
    
    /**
     * 決済統計情報クラス
     */
    record PaymentStats(
        long totalCount,
        long successCount,
        long failedCount,
        long pendingCount,
        java.math.BigDecimal totalAmount,
        java.math.BigDecimal successAmount
    ) {}
}
