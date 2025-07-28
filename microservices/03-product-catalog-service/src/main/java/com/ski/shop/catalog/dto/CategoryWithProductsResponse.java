package com.ski.shop.catalog.dto;

import java.util.List;
import java.util.UUID;

/**
 * カテゴリーと商品一覧のレスポンス
 */
public class CategoryWithProductsResponse {
    public UUID id;
    public String name;
    public String description;
    public String path;
    public Integer level;
    public Integer sortOrder;
    public String imageUrl;
    public boolean active;
    public Long productCount;
    public List<ProductSummaryResponse> products;

    public CategoryWithProductsResponse() {}

    public CategoryWithProductsResponse(
            UUID id,
            String name,
            String description,
            String path,
            Integer level,
            Integer sortOrder,
            String imageUrl,
            boolean active,
            Long productCount,
            List<ProductSummaryResponse> products) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.path = path;
        this.level = level;
        this.sortOrder = sortOrder;
        this.imageUrl = imageUrl;
        this.active = active;
        this.productCount = productCount;
        this.products = products;
    }
}
