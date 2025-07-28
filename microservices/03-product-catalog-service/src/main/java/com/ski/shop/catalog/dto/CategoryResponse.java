package com.ski.shop.catalog.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * カテゴリレスポンスDTO
 */
public class CategoryResponse {
    private UUID id;
    private String name;
    private String description;
    private String path;
    private Integer level;
    private Integer sortOrder;
    private String imageUrl;
    private boolean isActive;
    private long productCount;
    private CategorySummaryResponse parent;
    private List<CategorySummaryResponse> children;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CategoryResponse() {}

    public CategoryResponse(UUID id, String name, String description, String path, 
                           Integer level, Integer sortOrder, String imageUrl, 
                           boolean isActive, long productCount, 
                           CategorySummaryResponse parent, List<CategorySummaryResponse> children,
                           LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.path = path;
        this.level = level;
        this.sortOrder = sortOrder;
        this.imageUrl = imageUrl;
        this.isActive = isActive;
        this.productCount = productCount;
        this.parent = parent;
        this.children = children;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * カテゴリがルートカテゴリかどうかを判定
     */
    public boolean isRoot() {
        return level != null && level == 0;
    }

    /**
     * カテゴリがサブカテゴリかどうかを判定
     */
    public boolean isSubCategory() {
        return level != null && level > 0;
    }

    /**
     * 子カテゴリ（サブカテゴリ）を持つかどうかを判定
     */
    public boolean hasSubCategories() {
        return children != null && !children.isEmpty();
    }

    /**
     * サブカテゴリの数を取得
     */
    public int getSubCategoryCount() {
        return children != null ? children.size() : 0;
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

    public CategorySummaryResponse getParent() { return parent; }
    public void setParent(CategorySummaryResponse parent) { this.parent = parent; }

    public List<CategorySummaryResponse> getChildren() { return children; }
    public void setChildren(List<CategorySummaryResponse> children) { this.children = children; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
