package com.skiresort.payment.service;

import com.skiresort.payment.model.Payment;
import com.skiresort.payment.model.PaymentAmount;
import com.skiresort.payment.model.PaymentDetails;
import com.skiresort.payment.model.PaymentMethod;
import com.skiresort.payment.model.PaymentStatus;
import com.skiresort.payment.model.Refund;
import com.skiresort.payment.repository.PaymentRepository.PaymentStats;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 決済サービスインターフェイス
 */
public interface PaymentService {
    
    /**
     * 決済を作成する
     */
    Payment createPayment(UUID orderId, UUID customerId, PaymentAmount amount,
                         PaymentMethod paymentMethod, String provider, PaymentDetails paymentDetails);
    
    /**
     * 決済を認証する
     */
    Payment authorizePayment(String paymentId, String authCode, String providerTransactionId);
    
    /**
     * 決済を確定する
     */
    Payment capturePayment(String paymentId);
    
    /**
     * 決済をキャンセルする
     */
    Payment cancelPayment(String paymentId);
    
    /**
     * 決済を失敗させる
     */
    Payment failPayment(String paymentId, String reason);
    
    /**
     * 返金を作成する
     */
    Refund createRefund(String paymentId, BigDecimal amount, String reason, UUID requestedBy);
    
    /**
     * 返金を処理する
     */
    Refund processRefund(String refundId, String providerRefundId);
    
    /**
     * 返金を完了する
     */
    Refund completeRefund(String refundId);
    
    /**
     * 返金を失敗させる
     */
    Refund failRefund(String refundId, String reason);
    
    /**
     * 決済IDで決済を検索する
     */
    Optional<Payment> getPaymentByPaymentId(String paymentId);
    
    /**
     * 注文IDで決済一覧を取得する
     */
    List<Payment> getPaymentsByOrderId(UUID orderId);
    
    /**
     * 顧客IDで決済一覧を取得する
     */
    List<Payment> getPaymentsByCustomerId(UUID customerId);
    
    /**
     * ステータスで決済一覧を取得する
     */
    List<Payment> getPaymentsByStatus(PaymentStatus status);
    
    /**
     * 決済IDで返金一覧を取得する
     */
    List<Refund> getRefundsByPaymentId(String paymentId);
    
    /**
     * 返金IDで返金を検索する
     */
    Optional<Refund> getRefundByRefundId(String refundId);
    
    /**
     * 期限切れ決済を処理する
     */
    void processExpiredPayments();
    
    /**
     * 失敗した決済の再試行を処理する
     */
    void retryFailedPayments();
    
    /**
     * 決済統計を取得する
     */
    PaymentStats getPaymentStats(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * 決済IDを生成する
     */
    String generatePaymentId();
    
    /**
     * 返金IDを生成する
     */
    String generateRefundId();
}
