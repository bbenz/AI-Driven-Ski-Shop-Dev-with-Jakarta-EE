package com.ski.shop.catalog.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * 商品サマリーレスポンスDTO
 */
public class ProductSummaryResponse {
    private UUID id;
    private String sku;
    private String name;
    private String shortDescription;
    private CategorySummaryResponse category;
    private BrandSummaryResponse brand;
    private BigDecimal currentPrice;
    private BigDecimal basePrice;
    private boolean isOnSale;
    private Integer discountPercentage;
    private String primaryImageUrl;
    private boolean inStock;
    private boolean featured;
    private Double rating;
    private Integer reviewCount;
    private Set<String> tags;
    private LocalDateTime createdAt;

    // デフォルトコンストラクタ
    public ProductSummaryResponse() {}

    // フルコンストラクタ
    public ProductSummaryResponse(UUID id, String sku, String name, String shortDescription,
                                CategorySummaryResponse category, BrandSummaryResponse brand,
                                BigDecimal currentPrice, BigDecimal basePrice, boolean isOnSale,
                                Integer discountPercentage, String primaryImageUrl, boolean inStock,
                                boolean featured, Double rating, Integer reviewCount, Set<String> tags,
                                LocalDateTime createdAt) {
        this.id = id;
        this.sku = sku;
        this.name = name;
        this.shortDescription = shortDescription;
        this.category = category;
        this.brand = brand;
        this.currentPrice = currentPrice;
        this.basePrice = basePrice;
        this.isOnSale = isOnSale;
        this.discountPercentage = discountPercentage;
        this.primaryImageUrl = primaryImageUrl;
        this.inStock = inStock;
        this.featured = featured;
        this.rating = rating;
        this.reviewCount = reviewCount;
        this.tags = tags;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getShortDescription() { return shortDescription; }
    public void setShortDescription(String shortDescription) { this.shortDescription = shortDescription; }

    public CategorySummaryResponse getCategory() { return category; }
    public void setCategory(CategorySummaryResponse category) { this.category = category; }

    public BrandSummaryResponse getBrand() { return brand; }
    public void setBrand(BrandSummaryResponse brand) { this.brand = brand; }

    public BigDecimal getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; }

    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }

    public boolean isOnSale() { return isOnSale; }
    public void setOnSale(boolean onSale) { isOnSale = onSale; }

    public Integer getDiscountPercentage() { return discountPercentage; }
    public void setDiscountPercentage(Integer discountPercentage) { this.discountPercentage = discountPercentage; }

    public String getPrimaryImageUrl() { return primaryImageUrl; }
    public void setPrimaryImageUrl(String primaryImageUrl) { this.primaryImageUrl = primaryImageUrl; }

    public boolean isInStock() { return inStock; }
    public void setInStock(boolean inStock) { this.inStock = inStock; }

    public boolean isFeatured() { return featured; }
    public void setFeatured(boolean featured) { this.featured = featured; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public Integer getReviewCount() { return reviewCount; }
    public void setReviewCount(Integer reviewCount) { this.reviewCount = reviewCount; }

    public Set<String> getTags() { return tags; }
    public void setTags(Set<String> tags) { this.tags = tags; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
