package com.skiresort.payment.repository;

import com.skiresort.payment.model.Refund;
import com.skiresort.payment.model.RefundStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 返金リポジトリインターフェイス
 */
public interface RefundRepository {
    
    /**
     * 返金を保存する
     */
    Refund save(Refund refund);
    
    /**
     * IDで返金を検索する
     */
    Optional<Refund> findById(UUID id);
    
    /**
     * 返金IDで返金を検索する
     */
    Optional<Refund> findByRefundId(String refundId);
    
    /**
     * 決済IDで返金一覧を取得する
     */
    List<Refund> findByPaymentId(UUID paymentId);
    
    /**
     * ステータスで返金一覧を取得する
     */
    List<Refund> findByStatus(RefundStatus status);
    
    /**
     * 日付範囲で返金一覧を取得する
     */
    List<Refund> findByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * 処理待ちの返金一覧を取得する
     */
    List<Refund> findPendingRefunds();
    
    /**
     * プロバイダー返金IDで返金を検索する
     */
    Optional<Refund> findByProviderRefundId(String providerRefundId);
    
    /**
     * 返金統計情報を取得する
     */
    RefundStats getRefundStats(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * 返金を削除する
     */
    void delete(Refund refund);
    
    /**
     * 返金統計情報クラス
     */
    record RefundStats(
        long totalCount,
        long completedCount,
        long failedCount,
        long pendingCount,
        java.math.BigDecimal totalAmount,
        java.math.BigDecimal completedAmount
    ) {}
}
