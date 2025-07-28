package com.skiresort.payment.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 返金エンティティ
 */
@Entity
@Table(name = "refunds")
@NamedQueries({
    @NamedQuery(
        name = "Refund.findByPaymentId",
        query = "SELECT r FROM Refund r WHERE r.payment.id = :paymentId ORDER BY r.createdAt DESC"
    ),
    @NamedQuery(
        name = "Refund.findByStatus",
        query = "SELECT r FROM Refund r WHERE r.status = :status ORDER BY r.createdAt DESC"
    ),
    @NamedQuery(
        name = "Refund.findByDateRange",
        query = "SELECT r FROM Refund r WHERE r.createdAt BETWEEN :startDate AND :endDate ORDER BY r.createdAt DESC"
    ),
    @NamedQuery(
        name = "Refund.findPendingRefunds",
        query = "SELECT r FROM Refund r WHERE r.status = 'PENDING' ORDER BY r.createdAt ASC"
    )
})
public class Refund {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;
    
    @NotBlank
    @Size(max = 100)
    @Column(name = "refund_id", unique = true, nullable = false, length = 100)
    private String refundId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    @NotNull
    private Payment payment;
    
    @NotNull
    @PositiveOrZero
    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    
    @Size(max = 3)
    @Column(name = "currency", nullable = false, length = 3)
    @NotBlank
    private String currency;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @NotNull
    private RefundStatus status;
    
    @Size(max = 200)
    @Column(name = "provider_refund_id", length = 200)
    private String providerRefundId;
    
    @Size(max = 200)
    @Column(name = "provider_reference", length = 200)
    private String providerReference;
    
    @Size(max = 500)
    @Column(name = "reason", length = 500)
    private String reason;
    
    @Size(max = 500)
    @Column(name = "failure_reason", length = 500)
    private String failureReason;
    
    @NotNull
    @Column(name = "requested_by", nullable = false)
    private UUID requestedBy;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "failed_at")
    private LocalDateTime failedAt;
    
    @Size(max = 1000)
    @Column(name = "notes", length = 1000)
    private String notes;
    
    // コンストラクタ
    public Refund() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = RefundStatus.PENDING;
    }
    
    public Refund(String refundId, Payment payment, BigDecimal amount, String currency,
                  String reason, UUID requestedBy) {
        this();
        this.refundId = refundId;
        this.payment = payment;
        this.amount = amount;
        this.currency = currency;
        this.reason = reason;
        this.requestedBy = requestedBy;
    }
    
    // ビジネスロジック
    public boolean canBeProcessed() {
        return status == RefundStatus.PENDING;
    }
    
    public boolean canBeCompleted() {
        return status == RefundStatus.PROCESSING;
    }
    
    public boolean canBeFailed() {
        return status == RefundStatus.PENDING || status == RefundStatus.PROCESSING;
    }
    
    public void process(String providerRefundId) {
        if (!canBeProcessed()) {
            throw new IllegalStateException("返金を処理できません。現在のステータス: " + status);
        }
        this.status = RefundStatus.PROCESSING;
        this.providerRefundId = providerRefundId;
        this.processedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public void complete() {
        if (!canBeCompleted()) {
            throw new IllegalStateException("返金を完了できません。現在のステータス: " + status);
        }
        this.status = RefundStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public void fail(String reason) {
        if (!canBeFailed()) {
            throw new IllegalStateException("返金を失敗状態にできません。現在のステータス: " + status);
        }
        this.status = RefundStatus.FAILED;
        this.failureReason = reason;
        this.failedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public boolean isPartialRefund() {
        return amount.compareTo(payment.getAmount().totalAmount()) < 0;
    }
    
    public boolean isFullRefund() {
        return amount.compareTo(payment.getAmount().totalAmount()) == 0;
    }
    
    // ライフサイクルコールバック
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getter and Setter methods
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getRefundId() {
        return refundId;
    }
    
    public void setRefundId(String refundId) {
        this.refundId = refundId;
    }
    
    public Payment getPayment() {
        return payment;
    }
    
    public void setPayment(Payment payment) {
        this.payment = payment;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public RefundStatus getStatus() {
        return status;
    }
    
    public void setStatus(RefundStatus status) {
        this.status = status;
    }
    
    public String getProviderRefundId() {
        return providerRefundId;
    }
    
    public void setProviderRefundId(String providerRefundId) {
        this.providerRefundId = providerRefundId;
    }
    
    public String getProviderReference() {
        return providerReference;
    }
    
    public void setProviderReference(String providerReference) {
        this.providerReference = providerReference;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    public String getFailureReason() {
        return failureReason;
    }
    
    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }
    
    public UUID getRequestedBy() {
        return requestedBy;
    }
    
    public void setRequestedBy(UUID requestedBy) {
        this.requestedBy = requestedBy;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public LocalDateTime getProcessedAt() {
        return processedAt;
    }
    
    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }
    
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
    
    public LocalDateTime getFailedAt() {
        return failedAt;
    }
    
    public void setFailedAt(LocalDateTime failedAt) {
        this.failedAt = failedAt;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Refund refund)) return false;
        return id != null && id.equals(refund.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
    
    @Override
    public String toString() {
        return "Refund{" +
                "id=" + id +
                ", refundId='" + refundId + '\'' +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", status=" + status +
                ", reason='" + reason + '\'' +
                '}';
    }
}
