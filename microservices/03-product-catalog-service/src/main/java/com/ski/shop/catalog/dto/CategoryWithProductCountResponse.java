package com.ski.shop.catalog.dto;

import java.util.UUID;

/**
 * カテゴリ階層表示用のDTO
 */
public class CategoryWithProductCountResponse {
    private UUID id;
    private String name;
    private String description;
    private String path;
    private Integer level;
    private Integer sortOrder;
    private String imageUrl;
    private boolean isActive;
    private long productCount;

    public CategoryWithProductCountResponse() {}

    public CategoryWithProductCountResponse(UUID id, String name, String description, 
                                          String path, Integer level, Integer sortOrder, 
                                          String imageUrl, boolean isActive, long productCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.path = path;
        this.level = level;
        this.sortOrder = sortOrder;
        this.imageUrl = imageUrl;
        this.isActive = isActive;
        this.productCount = productCount;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public long getProductCount() { return productCount; }
    public void setProductCount(long productCount) { this.productCount = productCount; }
}
