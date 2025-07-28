package com.skiresort.payment.model;

import com.skiresort.payment.exception.InvalidPaymentStateException;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 決済エンティティ
 */
@Entity
@Table(name = "payments")
@NamedQueries({
    @NamedQuery(
        name = "Payment.findByOrderId",
        query = "SELECT p FROM Payment p WHERE p.orderId = :orderId"
    ),
    @NamedQuery(
        name = "Payment.findByStatus",
        query = "SELECT p FROM Payment p WHERE p.status = :status ORDER BY p.createdAt DESC"
    ),
    @NamedQuery(
        name = "Payment.findByCustomerId",
        query = "SELECT p FROM Payment p WHERE p.customerId = :customerId ORDER BY p.createdAt DESC"
    ),
    @NamedQuery(
        name = "Payment.findByDateRange",
        query = "SELECT p FROM Payment p WHERE p.createdAt BETWEEN :startDate AND :endDate ORDER BY p.createdAt DESC"
    ),
    @NamedQuery(
        name = "Payment.findByProvider",
        query = "SELECT p FROM Payment p WHERE p.provider = :provider ORDER BY p.createdAt DESC"
    )
})
public class Payment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;
    
    @NotBlank
    @Size(max = 100)
    @Column(name = "payment_id", unique = true, nullable = false, length = 100)
    private String paymentId;
    
    @NotNull
    @Column(name = "order_id", nullable = false)
    private UUID orderId;
    
    @NotNull
    @Column(name = "customer_id", nullable = false)
    private UUID customerId;
    
    @Embedded
    @NotNull
    private PaymentAmount amount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30)
    @NotNull
    private PaymentMethod paymentMethod;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @NotNull
    private PaymentStatus status;
    
    @NotBlank
    @Size(max = 50)
    @Column(name = "provider", nullable = false, length = 50)
    private String provider;
    
    @Size(max = 200)
    @Column(name = "provider_transaction_id", length = 200)
    private String providerTransactionId;
    
    @Size(max = 200)
    @Column(name = "provider_reference", length = 200)
    private String providerReference;
    
    @Embedded
    private PaymentDetails paymentDetails;
    
    @Size(max = 500)
    @Column(name = "failure_reason", length = 500)
    private String failureReason;
    
    @Size(max = 100)
    @Column(name = "authorization_code", length = 100)
    private String authorizationCode;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "authorized_at")
    private LocalDateTime authorizedAt;
    
    @Column(name = "captured_at")
    private LocalDateTime capturedAt;
    
    @Column(name = "failed_at")
    private LocalDateTime failedAt;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @Size(max = 1000)
    @Column(name = "notes", length = 1000)
    private String notes;
    
    // 関連エンティティ
    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PaymentEvent> events = new ArrayList<>();
    
    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Refund> refunds = new ArrayList<>();
    
    // コンストラクタ
    public Payment() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = PaymentStatus.PENDING;
    }
    
    public Payment(String paymentId, UUID orderId, UUID customerId, PaymentAmount amount,
                  PaymentMethod paymentMethod, String provider) {
        this();
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.customerId = customerId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.provider = provider;
    }
    
    // ビジネスロジック
    public boolean canBeAuthorized() {
        return status == PaymentStatus.PENDING;
    }
    
    public boolean canBeCaptured() {
        return status == PaymentStatus.AUTHORIZED;
    }
    
    public boolean canBeRefunded() {
        return status == PaymentStatus.CAPTURED || status == PaymentStatus.PARTIALLY_REFUNDED;
    }
    
    public boolean canBeCancelled() {
        return status == PaymentStatus.PENDING || status == PaymentStatus.AUTHORIZED;
    }
    
    public void authorize(String authCode, String providerTransactionId) {
        if (!canBeAuthorized()) {
            throw new InvalidPaymentStateException("決済を認証できません。現在のステータス: " + status);
        }
        this.status = PaymentStatus.AUTHORIZED;
        this.authorizationCode = authCode;
        this.providerTransactionId = providerTransactionId;
        this.authorizedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public void capture() {
        if (!canBeCaptured()) {
            throw new InvalidPaymentStateException("決済を確定できません。現在のステータス: " + status);
        }
        this.status = PaymentStatus.CAPTURED;
        this.capturedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public void fail(String reason) {
        this.status = PaymentStatus.FAILED;
        this.failureReason = reason;
        this.failedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public void cancel() {
        if (!canBeCancelled()) {
            throw new InvalidPaymentStateException("決済をキャンセルできません。現在のステータス: " + status);
        }
        this.status = PaymentStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }
    
    public BigDecimal getRefundableAmount() {
        var totalRefunded = refunds.stream()
            .filter(r -> r.getStatus() == RefundStatus.COMPLETED)
            .map(Refund::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        return amount.totalAmount().subtract(totalRefunded);
    }
    
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
    
    public void addEvent(PaymentEvent event) {
        event.setPayment(this);
        this.events.add(event);
    }
    
    public void addRefund(Refund refund) {
        refund.setPayment(this);
        this.refunds.add(refund);
        
        // 返金後のステータス更新
        BigDecimal totalRefunded = refunds.stream()
            .filter(r -> r.getStatus() == RefundStatus.COMPLETED)
            .map(Refund::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        if (totalRefunded.compareTo(amount.totalAmount()) >= 0) {
            this.status = PaymentStatus.REFUNDED;
        } else if (totalRefunded.compareTo(BigDecimal.ZERO) > 0) {
            this.status = PaymentStatus.PARTIALLY_REFUNDED;
        }
        this.updatedAt = LocalDateTime.now();
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
    
    public String getPaymentId() {
        return paymentId;
    }
    
    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }
    
    public UUID getOrderId() {
        return orderId;
    }
    
    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }
    
    public UUID getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }
    
    public PaymentAmount getAmount() {
        return amount;
    }
    
    public void setAmount(PaymentAmount amount) {
        this.amount = amount;
    }
    
    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public PaymentStatus getStatus() {
        return status;
    }
    
    public void setStatus(PaymentStatus status) {
        this.status = status;
    }
    
    public String getProvider() {
        return provider;
    }
    
    public void setProvider(String provider) {
        this.provider = provider;
    }
    
    public String getProviderTransactionId() {
        return providerTransactionId;
    }
    
    public void setProviderTransactionId(String providerTransactionId) {
        this.providerTransactionId = providerTransactionId;
    }
    
    public String getProviderReference() {
        return providerReference;
    }
    
    public void setProviderReference(String providerReference) {
        this.providerReference = providerReference;
    }
    
    public PaymentDetails getPaymentDetails() {
        return paymentDetails;
    }
    
    public void setPaymentDetails(PaymentDetails paymentDetails) {
        this.paymentDetails = paymentDetails;
    }
    
    public String getFailureReason() {
        return failureReason;
    }
    
    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }
    
    public String getAuthorizationCode() {
        return authorizationCode;
    }
    
    public void setAuthorizationCode(String authorizationCode) {
        this.authorizationCode = authorizationCode;
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
    
    public LocalDateTime getAuthorizedAt() {
        return authorizedAt;
    }
    
    public void setAuthorizedAt(LocalDateTime authorizedAt) {
        this.authorizedAt = authorizedAt;
    }
    
    public LocalDateTime getCapturedAt() {
        return capturedAt;
    }
    
    public void setCapturedAt(LocalDateTime capturedAt) {
        this.capturedAt = capturedAt;
    }
    
    public LocalDateTime getFailedAt() {
        return failedAt;
    }
    
    public void setFailedAt(LocalDateTime failedAt) {
        this.failedAt = failedAt;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public List<PaymentEvent> getEvents() {
        return events;
    }
    
    public void setEvents(List<PaymentEvent> events) {
        this.events = events;
    }
    
    public List<Refund> getRefunds() {
        return refunds;
    }
    
    public void setRefunds(List<Refund> refunds) {
        this.refunds = refunds;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Payment payment)) return false;
        return id != null && id.equals(payment.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
    
    @Override
    public String toString() {
        return "Payment{" +
                "id=" + id +
                ", paymentId='" + paymentId + '\'' +
                ", orderId=" + orderId +
                ", amount=" + amount +
                ", paymentMethod=" + paymentMethod +
                ", status=" + status +
                ", provider='" + provider + '\'' +
                '}';
    }
}
