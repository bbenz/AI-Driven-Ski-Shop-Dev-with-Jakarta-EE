package com.ski.shop.catalog.domain;

import java.time.LocalDateTime;

/**
 * 商品ステータス Value Object (Record)
 */
public record ProductStatus(
    PublishStatus publishStatus,
    boolean isActive,
    boolean isFeatured,
    boolean isDiscontinued,
    LocalDateTime publishedAt,
    LocalDateTime discontinuedAt
) {
    
    /**
     * 購入可能かどうかチェック
     */
    public boolean isAvailableForPurchase() {
        return publishStatus == PublishStatus.PUBLISHED 
            && isActive 
            && !isDiscontinued;
    }
    
    /**
     * 表示可能かどうかチェック
     */
    public boolean isDisplayable() {
        return publishStatus == PublishStatus.PUBLISHED && isActive;
    }
    
    /**
     * 管理者のみ表示可能かどうかチェック
     */
    public boolean isAdminOnly() {
        return publishStatus == PublishStatus.DRAFT || 
               publishStatus == PublishStatus.REVIEW_PENDING ||
               !isActive;
    }
}
