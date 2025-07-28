package com.ski.shop.catalog.dto;

import com.ski.shop.catalog.domain.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 商品作成リクエストDTO
 */
public class ProductCreateRequest {
    
    @NotBlank(message = "SKUは必須です")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "SKUは英大文字、数字、ハイフンのみ使用可能です")
    @Size(max = 100, message = "SKUは100文字以内で入力してください")
    private String sku;
    
    @NotBlank(message = "商品名は必須です")
    @Size(max = 255, message = "商品名は255文字以内で入力してください")
    private String name;
    
    @Size(max = 2000, message = "商品説明は2000文字以内で入力してください")
    private String description;
    
    @Size(max = 500, message = "短縮説明は500文字以内で入力してください")
    private String shortDescription;
    
    @NotNull(message = "カテゴリIDは必須です")
    private UUID categoryId;
    
    @NotNull(message = "ブランドIDは必須です")
    private UUID brandId;
    
    @NotNull(message = "商品仕様は必須です")
    @Valid
    private ProductSpecificationRequest specification;
    
    @NotNull(message = "ベース価格は必須です")
    @DecimalMin(value = "0.0", inclusive = false, message = "ベース価格は0より大きい値を入力してください")
    private BigDecimal basePrice;
    
    @DecimalMin(value = "0.0", inclusive = false, message = "セール価格は0より大きい値を入力してください")
    private BigDecimal salePrice;
    
    @DecimalMin(value = "0.0", inclusive = false, message = "原価は0より大きい値を入力してください")
    private BigDecimal costPrice;
    
    @Size(max = 20, message = "タグは20個まで登録可能です")
    private Set<String> tags;
    
    private Map<String, String> additionalSpecs;
    
    @Valid
    private ProductStatusRequest status;

    // デフォルトコンストラクタ
    public ProductCreateRequest() {}

    // フルコンストラクタ
    public ProductCreateRequest(String sku, String name, String description, String shortDescription,
                              UUID categoryId, UUID brandId, ProductSpecificationRequest specification,
                              BigDecimal basePrice, BigDecimal salePrice, BigDecimal costPrice,
                              Set<String> tags, Map<String, String> additionalSpecs,
                              ProductStatusRequest status) {
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.shortDescription = shortDescription;
        this.categoryId = categoryId;
        this.brandId = brandId;
        this.specification = specification;
        this.basePrice = basePrice;
        this.salePrice = salePrice;
        this.costPrice = costPrice;
        this.tags = tags;
        this.additionalSpecs = additionalSpecs;
        this.status = status;
    }

    // Getters and Setters
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getShortDescription() { return shortDescription; }
    public void setShortDescription(String shortDescription) { this.shortDescription = shortDescription; }

    public UUID getCategoryId() { return categoryId; }
    public void setCategoryId(UUID categoryId) { this.categoryId = categoryId; }

    public UUID getBrandId() { return brandId; }
    public void setBrandId(UUID brandId) { this.brandId = brandId; }

    public ProductSpecificationRequest getSpecification() { return specification; }
    public void setSpecification(ProductSpecificationRequest specification) { this.specification = specification; }

    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }

    public BigDecimal getSalePrice() { return salePrice; }
    public void setSalePrice(BigDecimal salePrice) { this.salePrice = salePrice; }

    public BigDecimal getCostPrice() { return costPrice; }
    public void setCostPrice(BigDecimal costPrice) { this.costPrice = costPrice; }

    public Set<String> getTags() { return tags; }
    public void setTags(Set<String> tags) { this.tags = tags; }

    public Map<String, String> getAdditionalSpecs() { return additionalSpecs; }
    public void setAdditionalSpecs(Map<String, String> additionalSpecs) { this.additionalSpecs = additionalSpecs; }

    public ProductStatusRequest getStatus() { return status; }
    public void setStatus(ProductStatusRequest status) { this.status = status; }
}
