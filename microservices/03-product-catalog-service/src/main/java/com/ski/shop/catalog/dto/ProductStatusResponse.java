package com.ski.shop.catalog.dto;

import com.ski.shop.catalog.domain.PublishStatus;
import java.time.LocalDateTime;

/**
 * 商品ステータスレスポンスDTO
 */
public class ProductStatusResponse {
    private PublishStatus publishStatus;
    private boolean isActive;
    private boolean isFeatured;
    private boolean isDiscontinued;
    private LocalDateTime publishedAt;
    private LocalDateTime discontinuedAt;

    // デフォルトコンストラクタ
    public ProductStatusResponse() {}

    // フルコンストラクタ
    public ProductStatusResponse(PublishStatus publishStatus, boolean isActive, boolean isFeatured,
                               boolean isDiscontinued, LocalDateTime publishedAt, LocalDateTime discontinuedAt) {
        this.publishStatus = publishStatus;
        this.isActive = isActive;
        this.isFeatured = isFeatured;
        this.isDiscontinued = isDiscontinued;
        this.publishedAt = publishedAt;
        this.discontinuedAt = discontinuedAt;
    }

    // Getters and Setters
    public PublishStatus getPublishStatus() { return publishStatus; }
    public void setPublishStatus(PublishStatus publishStatus) { this.publishStatus = publishStatus; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public boolean isFeatured() { return isFeatured; }
    public void setFeatured(boolean featured) { isFeatured = featured; }

    public boolean isDiscontinued() { return isDiscontinued; }
    public void setDiscontinued(boolean discontinued) { isDiscontinued = discontinued; }

    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }

    public LocalDateTime getDiscontinuedAt() { return discontinuedAt; }
    public void setDiscontinuedAt(LocalDateTime discontinuedAt) { this.discontinuedAt = discontinuedAt; }
}
