package com.ski.shop.catalog.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 商品画像エンティティ
 */
@Entity
@Table(name = "product_images")
public class ProductImage extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    public UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    public Product product;

    @NotBlank
    @Size(max = 500)
    @Column(name = "image_url", nullable = false, length = 500)
    public String imageUrl;

    @Size(max = 255)
    @Column(name = "alt_text")
    public String altText;

    @Size(max = 50)
    @Column(name = "image_type", length = 50)
    public String imageType; // 例：MAIN, DETAIL, LIFESTYLE

    @NotNull
    @Column(name = "sort_order", nullable = false)
    public Integer sortOrder = 0;

    @Column(name = "is_primary", nullable = false)
    public boolean isPrimary = false;

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

    // 静的ファインダーメソッド

    public static List<ProductImage> findByProduct(UUID productId) {
        return find("product.id = ?1 ORDER BY sortOrder", productId).list();
    }

    public static List<ProductImage> findPrimaryByProduct(UUID productId) {
        return find("product.id = ?1 AND isPrimary = true", productId).list();
    }
}
