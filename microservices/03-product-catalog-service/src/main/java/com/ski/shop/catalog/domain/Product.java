package com.ski.shop.catalog.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.panache.common.Parameters;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 商品エンティティ
 */
@Entity
@Table(name = "products")
@NamedQueries({
    @NamedQuery(
        name = "Product.findBySku",
        query = "SELECT p FROM Product p LEFT JOIN FETCH p.tags LEFT JOIN FETCH p.additionalSpecs WHERE p.sku = :sku"
    ),
    @NamedQuery(
        name = "Product.findByCategory",
        query = "SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.category LEFT JOIN FETCH p.brand LEFT JOIN FETCH p.tags LEFT JOIN FETCH p.additionalSpecs WHERE p.category.id = :categoryId AND p.publishStatus = 'PUBLISHED' AND p.isActive = true ORDER BY p.createdAt DESC"
    ),
    @NamedQuery(
        name = "Product.findFeatured",
        query = "SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.category LEFT JOIN FETCH p.brand LEFT JOIN FETCH p.tags LEFT JOIN FETCH p.additionalSpecs WHERE p.isFeatured = true AND p.publishStatus = 'PUBLISHED' AND p.isActive = true ORDER BY p.createdAt DESC"
    )
})
public class Product extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    public UUID id;

    @NotBlank
    @Size(max = 100)
    @Column(name = "sku", unique = true, nullable = false, length = 100)
    public String sku;

    @NotBlank
    @Size(max = 255)
    @Column(name = "name", nullable = false)
    public String name;

    @Size(max = 2000)
    @Column(name = "description", columnDefinition = "TEXT")
    public String description;

    @Size(max = 500)
    @Column(name = "short_description", length = 500)
    public String shortDescription;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    public Category category;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "brand_id", nullable = false)
    public Brand brand;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "material", nullable = false)
    public Material material;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "ski_type", nullable = false)
    public SkiType skiType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level", nullable = false)
    public DifficultyLevel difficultyLevel;

    @Size(max = 20)
    @Column(name = "length", length = 20)
    public String length;

    @Size(max = 20)
    @Column(name = "width", length = 20)
    public String width;

    @Size(max = 20)
    @Column(name = "weight", length = 20)
    public String weight;

    @Size(max = 20)
    @Column(name = "radius", length = 20)
    public String radius;

    @Enumerated(EnumType.STRING)
    @Column(name = "flex")
    public Flex flex;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "publish_status", nullable = false)
    public PublishStatus publishStatus = PublishStatus.DRAFT;

    @Column(name = "is_active", nullable = false)
    public boolean isActive = true;

    @Column(name = "is_featured", nullable = false)
    public boolean isFeatured = false;

    @Column(name = "is_discontinued", nullable = false)
    public boolean isDiscontinued = false;

    @Column(name = "published_at")
    public LocalDateTime publishedAt;

    @Column(name = "discontinued_at")
    public LocalDateTime discontinuedAt;

    @NotNull
    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    public BigDecimal basePrice;

    @Column(name = "sale_price", precision = 10, scale = 2)
    public BigDecimal salePrice;

    @Column(name = "cost_price", precision = 10, scale = 2)
    public BigDecimal costPrice;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    public List<ProductVariant> variants = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    public List<ProductImage> images = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "product_tags", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "tag")
    public Set<String> tags = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "product_additional_specs", joinColumns = @JoinColumn(name = "product_id"))
    @MapKeyColumn(name = "spec_key")
    @Column(name = "spec_value")
    public Map<String, String> additionalSpecs = new HashMap<>();

    @Column(name = "sales_count", nullable = false)
    public Long salesCount = 0L;

    @Column(name = "view_count", nullable = false)
    public Long viewCount = 0L;

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

    // ビジネスメソッド

    /**
     * 商品仕様をRecordとして取得
     */
    public ProductSpecification getSpecification() {
        return new ProductSpecification(
            material,
            skiType,
            difficultyLevel,
            length,
            width,
            weight,
            radius,
            flex,
            new HashMap<>(additionalSpecs)
        );
    }

    /**
     * 商品ステータスをRecordとして取得
     */
    public ProductStatus getStatus() {
        return new ProductStatus(
            publishStatus,
            isActive,
            isFeatured,
            isDiscontinued,
            publishedAt,
            discontinuedAt
        );
    }

    /**
     * 現在の価格を取得（セール価格があればセール価格、なければベース価格）
     */
    public BigDecimal getCurrentPrice() {
        return salePrice != null ? salePrice : basePrice;
    }

    /**
     * セール中かどうか
     */
    public boolean isOnSale() {
        return salePrice != null && salePrice.compareTo(basePrice) < 0;
    }

    /**
     * 割引率を計算（パーセント）
     */
    public Integer getDiscountPercentage() {
        if (!isOnSale()) {
            return 0;
        }
        BigDecimal discount = basePrice.subtract(salePrice);
        return discount.multiply(BigDecimal.valueOf(100))
                .divide(basePrice, 0, BigDecimal.ROUND_HALF_UP)
                .intValue();
    }

    /**
     * 購入可能かどうか
     */
    public boolean isAvailableForPurchase() {
        return getStatus().isAvailableForPurchase();
    }

    /**
     * プライマリ画像を取得
     */
    public Optional<ProductImage> getPrimaryImage() {
        return images.stream()
                .filter(img -> img.isPrimary)
                .findFirst();
    }

    /**
     * 販売数を増加
     */
    public void incrementSalesCount(int quantity) {
        this.salesCount += quantity;
    }

    /**
     * ビュー数を増加
     */
    public void incrementViewCount() {
        this.viewCount++;
    }

    /**
     * 商品公開
     */
    public void publish() {
        this.publishStatus = PublishStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
        this.isActive = true;
    }

    /**
     * 商品を廃番にする
     */
    public void discontinue() {
        this.isDiscontinued = true;
        this.discontinuedAt = LocalDateTime.now();
    }

    // 静的ファインダーメソッド

    public static Optional<Product> findBySku(String sku) {
        return find("#Product.findBySku", Parameters.with("sku", sku)).firstResultOptional();
    }

    public static List<Product> findByCategory(UUID categoryId) {
        return find("#Product.findByCategory", Parameters.with("categoryId", categoryId)).list();
    }

    public static List<Product> findFeatured() {
        return find("#Product.findFeatured").list();
    }

    public static List<Product> findByBrand(UUID brandId) {
        return find("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.category LEFT JOIN FETCH p.brand LEFT JOIN FETCH p.tags LEFT JOIN FETCH p.additionalSpecs WHERE p.brand.id = ?1 AND p.publishStatus = ?2 AND p.isActive = ?3", 
                   brandId, PublishStatus.PUBLISHED, true).list();
    }

    public static List<Product> findBySkiTypeAndDifficulty(SkiType skiType, DifficultyLevel difficultyLevel) {
        return find("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.category LEFT JOIN FETCH p.brand LEFT JOIN FETCH p.tags LEFT JOIN FETCH p.additionalSpecs WHERE p.skiType = ?1 AND p.difficultyLevel = ?2 AND p.publishStatus = ?3 AND p.isActive = ?4", 
                   skiType, difficultyLevel, PublishStatus.PUBLISHED, true).list();
    }
}
