package com.ski.shop.catalog.dto;

import com.ski.shop.catalog.domain.PublishStatus;
import jakarta.validation.constraints.NotNull;

/**
 * 商品ステータスリクエストDTO
 */
public class ProductStatusRequest {
    
    @NotNull(message = "公開ステータスは必須です")
    private PublishStatus publishStatus;
    
    private boolean isActive;
    private boolean isFeatured;
    private boolean isDiscontinued;

    // デフォルトコンストラクタ
    public ProductStatusRequest() {}

    // フルコンストラクタ
    public ProductStatusRequest(PublishStatus publishStatus, boolean isActive, boolean isFeatured, boolean isDiscontinued) {
        this.publishStatus = publishStatus;
        this.isActive = isActive;
        this.isFeatured = isFeatured;
        this.isDiscontinued = isDiscontinued;
    }

    // Getters and Setters
    public PublishStatus publishStatus() { return publishStatus; }
    public void setPublishStatus(PublishStatus publishStatus) { this.publishStatus = publishStatus; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public boolean isFeatured() { return isFeatured; }
    public void setFeatured(boolean featured) { isFeatured = featured; }

    public boolean isDiscontinued() { return isDiscontinued; }
    public void setDiscontinued(boolean discontinued) { isDiscontinued = discontinued; }
}
