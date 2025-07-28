package com.skiresort.payment.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 決済イベントエンティティ（決済の状態変更履歴）
 */
@Entity
@Table(name = "payment_events")
@NamedQueries({
    @NamedQuery(
        name = "PaymentEvent.findByPaymentId",
        query = "SELECT pe FROM PaymentEvent pe WHERE pe.payment.id = :paymentId ORDER BY pe.createdAt ASC"
    ),
    @NamedQuery(
        name = "PaymentEvent.findByEventType",
        query = "SELECT pe FROM PaymentEvent pe WHERE pe.eventType = :eventType ORDER BY pe.createdAt DESC"
    ),
    @NamedQuery(
        name = "PaymentEvent.findByDateRange",
        query = "SELECT pe FROM PaymentEvent pe WHERE pe.createdAt BETWEEN :startDate AND :endDate ORDER BY pe.createdAt DESC"
    )
})
public class PaymentEvent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    @NotNull
    private Payment payment;
    
    @NotBlank
    @Size(max = 50)
    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;
    
    @Size(max = 30)
    @Column(name = "previous_status", length = 30)
    private String previousStatus;
    
    @Size(max = 30)
    @Column(name = "new_status", length = 30)
    private String newStatus;
    
    @Size(max = 1000)
    @Column(name = "description", length = 1000)
    private String description;
    
    @Size(max = 4000)
    @Column(name = "event_data", length = 4000)
    private String eventData;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "created_by")
    private UUID createdBy;
    
    @Size(max = 100)
    @Column(name = "source", length = 100)
    private String source;
    
    // コンストラクタ
    public PaymentEvent() {
        this.createdAt = LocalDateTime.now();
    }
    
    public PaymentEvent(Payment payment, String eventType, String description) {
        this();
        this.payment = payment;
        this.eventType = eventType;
        this.description = description;
    }
    
    public PaymentEvent(Payment payment, String eventType, String previousStatus, 
                       String newStatus, String description) {
        this(payment, eventType, description);
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
    }
    
    // 静的ファクトリーメソッド
    public static PaymentEvent createStatusChangeEvent(Payment payment, String previousStatus, 
                                                      String newStatus, String description) {
        return new PaymentEvent(payment, "STATUS_CHANGE", previousStatus, newStatus, description);
    }
    
    public static PaymentEvent createPaymentAuthorizedEvent(Payment payment, String authCode) {
        var event = new PaymentEvent(payment, "PAYMENT_AUTHORIZED", "決済が認証されました");
        event.setEventData("{\"authorizationCode\":\"" + authCode + "\"}");
        return event;
    }
    
    public static PaymentEvent createPaymentCapturedEvent(Payment payment) {
        return new PaymentEvent(payment, "PAYMENT_CAPTURED", "決済が確定されました");
    }
    
    public static PaymentEvent createPaymentFailedEvent(Payment payment, String reason) {
        var event = new PaymentEvent(payment, "PAYMENT_FAILED", "決済が失敗しました: " + reason);
        event.setEventData("{\"failureReason\":\"" + reason + "\"}");
        return event;
    }
    
    public static PaymentEvent createRefundRequestedEvent(Payment payment, String refundId) {
        var event = new PaymentEvent(payment, "REFUND_REQUESTED", "返金が要求されました");
        event.setEventData("{\"refundId\":\"" + refundId + "\"}");
        return event;
    }
    
    public static PaymentEvent createWebhookReceivedEvent(Payment payment, String provider, String eventType) {
        var event = new PaymentEvent(payment, "WEBHOOK_RECEIVED", 
                                   provider + "からのWebhookを受信しました: " + eventType);
        event.setSource(provider);
        return event;
    }
    
    // Getter and Setter methods
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public Payment getPayment() {
        return payment;
    }
    
    public void setPayment(Payment payment) {
        this.payment = payment;
    }
    
    public String getEventType() {
        return eventType;
    }
    
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
    
    public String getPreviousStatus() {
        return previousStatus;
    }
    
    public void setPreviousStatus(String previousStatus) {
        this.previousStatus = previousStatus;
    }
    
    public String getNewStatus() {
        return newStatus;
    }
    
    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getEventData() {
        return eventData;
    }
    
    public void setEventData(String eventData) {
        this.eventData = eventData;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public UUID getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(UUID createdBy) {
        this.createdBy = createdBy;
    }
    
    public String getSource() {
        return source;
    }
    
    public void setSource(String source) {
        this.source = source;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PaymentEvent that)) return false;
        return id != null && id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
    
    @Override
    public String toString() {
        return "PaymentEvent{" +
                "id=" + id +
                ", eventType='" + eventType + '\'' +
                ", previousStatus='" + previousStatus + '\'' +
                ", newStatus='" + newStatus + '\'' +
                ", description='" + description + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
