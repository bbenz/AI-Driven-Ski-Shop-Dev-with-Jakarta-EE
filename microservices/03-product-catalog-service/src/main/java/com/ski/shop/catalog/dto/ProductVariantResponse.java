package com.ski.shop.catalog.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 商品バリエーションレスポンスDTO
 */
public class ProductVariantResponse {
    private UUID id;
    private String variantType;
    private String variantValue;
    private String sku;
    private BigDecimal additionalPrice;
    private boolean isDefault;
    private Integer stockQuantity;
    private BigDecimal price;
    private boolean inStock;
    private LocalDateTime createdAt;

    // デフォルトコンストラクタ
    public ProductVariantResponse() {}

    // フルコンストラクタ
    public ProductVariantResponse(UUID id, String variantType, String variantValue, String sku,
                                BigDecimal additionalPrice, boolean isDefault, Integer stockQuantity,
                                BigDecimal price, boolean inStock, LocalDateTime createdAt) {
        this.id = id;
        this.variantType = variantType;
        this.variantValue = variantValue;
        this.sku = sku;
        this.additionalPrice = additionalPrice;
        this.isDefault = isDefault;
        this.stockQuantity = stockQuantity;
        this.price = price;
        this.inStock = inStock;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getVariantType() { return variantType; }
    public void setVariantType(String variantType) { this.variantType = variantType; }

    public String getVariantValue() { return variantValue; }
    public void setVariantValue(String variantValue) { this.variantValue = variantValue; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public BigDecimal getAdditionalPrice() { return additionalPrice; }
    public void setAdditionalPrice(BigDecimal additionalPrice) { this.additionalPrice = additionalPrice; }

    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean aDefault) { isDefault = aDefault; }

    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public boolean isInStock() { return inStock; }
    public void setInStock(boolean inStock) { this.inStock = inStock; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
