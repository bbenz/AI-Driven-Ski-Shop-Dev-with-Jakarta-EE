package com.ski.shop.catalog.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 商品画像レスポンスDTO
 */
public class ProductImageResponse {
    private UUID id;
    private String imageUrl;
    private String altText;
    private String imageType;
    private Integer sortOrder;
    private boolean isPrimary;
    private LocalDateTime createdAt;

    // デフォルトコンストラクタ
    public ProductImageResponse() {}

    // フルコンストラクタ
    public ProductImageResponse(UUID id, String imageUrl, String altText, String imageType,
                              Integer sortOrder, boolean isPrimary, LocalDateTime createdAt) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.altText = altText;
        this.imageType = imageType;
        this.sortOrder = sortOrder;
        this.isPrimary = isPrimary;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getAltText() { return altText; }
    public void setAltText(String altText) { this.altText = altText; }

    public String getImageType() { return imageType; }
    public void setImageType(String imageType) { this.imageType = imageType; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public boolean isPrimary() { return isPrimary; }
    public void setPrimary(boolean primary) { isPrimary = primary; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
