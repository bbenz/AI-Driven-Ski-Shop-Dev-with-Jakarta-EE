package com.skiresort.loyalty.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ロイヤルティイベントエンティティ
 */
@Entity
@Table(name = "loyalty_events", indexes = {
    @Index(name = "idx_loyalty_event_status", columnList = "loyalty_status_id"),
    @Index(name = "idx_loyalty_event_type", columnList = "event_type"),
    @Index(name = "idx_loyalty_event_created", columnList = "created_at")
})
@NamedQueries({
    @NamedQuery(name = "LoyaltyEvent.findByStatusId", 
                query = "SELECT le FROM LoyaltyEvent le WHERE le.loyaltyStatus.id = :statusId ORDER BY le.createdAt DESC"),
    @NamedQuery(name = "LoyaltyEvent.findByEventType", 
                query = "SELECT le FROM LoyaltyEvent le WHERE le.eventType = :eventType ORDER BY le.createdAt DESC")
})
public class LoyaltyEvent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loyalty_status_id", nullable = false)
    @NotNull
    private LoyaltyStatus loyaltyStatus;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    @NotNull
    private LoyaltyEventType eventType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "from_rank")
    private LoyaltyRank fromRank;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "to_rank")
    private LoyaltyRank toRank;
    
    @Column(name = "description", length = 500)
    @Size(max = 500)
    private String description;
    
    @Column(name = "metadata", columnDefinition = "JSONB")
    private String metadata;
    
    @Column(name = "created_at", nullable = false)
    @NotNull
    private LocalDateTime createdAt;
    
    // JPA用のデフォルトコンストラクタ
    protected LoyaltyEvent() {}
    
    // ビジネスロジック用コンストラクタ
    public LoyaltyEvent(LoyaltyStatus loyaltyStatus, LoyaltyEventType eventType, 
                       String description) {
        this.loyaltyStatus = loyaltyStatus;
        this.eventType = eventType;
        this.description = description;
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public LoyaltyStatus getLoyaltyStatus() {
        return loyaltyStatus;
    }
    
    public void setLoyaltyStatus(LoyaltyStatus loyaltyStatus) {
        this.loyaltyStatus = loyaltyStatus;
    }
    
    public LoyaltyEventType getEventType() {
        return eventType;
    }
    
    public void setEventType(LoyaltyEventType eventType) {
        this.eventType = eventType;
    }
    
    public LoyaltyRank getFromRank() {
        return fromRank;
    }
    
    public void setFromRank(LoyaltyRank fromRank) {
        this.fromRank = fromRank;
    }
    
    public LoyaltyRank getToRank() {
        return toRank;
    }
    
    public void setToRank(LoyaltyRank toRank) {
        this.toRank = toRank;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getMetadata() {
        return metadata;
    }
    
    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
