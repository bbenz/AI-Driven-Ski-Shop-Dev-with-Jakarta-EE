package com.ski.shop.catalog.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 商品バリエーションエンティティ
 */
@Entity
@Table(name = "product_variants")
public class ProductVariant extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    public UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    public Product product;

    @NotBlank
    @Size(max = 50)
    @Column(name = "variant_type", nullable = false, length = 50)
    public String variantType; // 例：SIZE, COLOR, LENGTH

    @NotBlank
    @Size(max = 100)
    @Column(name = "variant_value", nullable = false, length = 100)
    public String variantValue; // 例：165cm, Red, M

    @Size(max = 100)
    @Column(name = "sku", length = 100)
    public String sku;

    @Column(name = "additional_price", precision = 10, scale = 2)
    public BigDecimal additionalPrice = BigDecimal.ZERO;

    @Column(name = "is_default", nullable = false)
    public boolean isDefault = false;

    @Column(name = "stock_quantity")
    public Integer stockQuantity = 0;

    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt;

    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * バリエーションの価格を計算（商品価格 + 追加価格）
     */
    public BigDecimal getPrice() {
        return product.getCurrentPrice().add(additionalPrice);
    }

    /**
     * 在庫があるかどうか
     */
    public boolean isInStock() {
        return stockQuantity != null && stockQuantity > 0;
    }
}
