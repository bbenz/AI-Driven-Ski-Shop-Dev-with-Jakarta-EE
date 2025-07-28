package com.ski.shop.catalog.service;

import com.ski.shop.catalog.domain.Category;
import com.ski.shop.catalog.domain.Product;
import com.ski.shop.catalog.dto.CategoryResponse;
import com.ski.shop.catalog.dto.CategorySummaryResponse;
import com.ski.shop.catalog.dto.CategoryWithProductCountResponse;
import com.ski.shop.catalog.dto.CategoryWithProductsResponse;
import com.ski.shop.catalog.dto.ProductSummaryResponse;
import com.ski.shop.catalog.dto.BrandSummaryResponse;
import io.quarkus.cache.CacheResult;
import io.quarkus.cache.CacheKey;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * カテゴリサービス
 */
@ApplicationScoped
public class CategoryService {

    /**
     * 全カテゴリ一覧を商品数と共に取得
     */
    @CacheResult(cacheName = "categories")
    public List<CategoryWithProductCountResponse> getAllCategoriesWithProductCount() {
        List<Category> categories = Category.list("isActive = true ORDER BY sortOrder");
        return categories.stream()
                .map(category -> toCategoryWithProductCountResponse(category, category.getProductCount()))
                .collect(Collectors.toList());
    }

    /**
     * ルートカテゴリ一覧を商品数と共に取得
     */
    @CacheResult(cacheName = "categories")
    public List<CategoryWithProductCountResponse> getRootCategoriesWithProductCount() {
        List<Category> rootCategories = Category.findRootCategories();
        return rootCategories.stream()
                .map(category -> toCategoryWithProductCountResponse(category, category.getProductCount()))
                .collect(Collectors.toList());
    }

    /**
     * 指定されたレベルのカテゴリ一覧を商品数と共に取得
     */
    @CacheResult(cacheName = "categories")
    public List<CategoryWithProductCountResponse> getCategoriesByLevel(Integer level) {
        List<Category> categories = Category.findByLevel(level);
        return categories.stream()
                .map(category -> toCategoryWithProductCountResponse(category, category.getProductCount()))
                .collect(Collectors.toList());
    }

    /**
     * 指定された親カテゴリのサブカテゴリ一覧を商品数と共に取得
     */
    @CacheResult(cacheName = "categories")
    public List<CategoryWithProductCountResponse> getSubCategories(UUID parentId) {
        List<Category> subCategories = Category.findByParent(parentId);
        return subCategories.stream()
                .map(category -> toCategoryWithProductCountResponse(category, category.getProductCount()))
                .collect(Collectors.toList());
    }

    /**
     * メインカテゴリ（レベル0）一覧を取得
     */
    @CacheResult(cacheName = "categories")
    public List<CategoryWithProductCountResponse> getMainCategories() {
        return getCategoriesByLevel(0);
    }

    /**
     * 指定されたレベルのカテゴリ一覧を商品数と共に取得
     */
    @CacheResult(cacheName = "categories")
    public List<CategoryWithProductCountResponse> getCategoriesByLevelWithProductCount(@CacheKey Integer level) {
        List<Category> categories = Category.findByLevel(level);
        return categories.stream()
                .map(category -> toCategoryWithProductCountResponse(category, category.getProductCount()))
                .collect(Collectors.toList());
    }

    /**
     * 指定されたカテゴリの子カテゴリ一覧を商品数と共に取得
     */
    @CacheResult(cacheName = "category-children")
    public List<CategoryWithProductCountResponse> getChildCategoriesWithProductCount(@CacheKey UUID parentId) {
        List<Category> children = Category.findByParent(parentId);
        return children.stream()
                .map(category -> toCategoryWithProductCountResponse(category, category.getProductCount()))
                .collect(Collectors.toList());
    }

    /**
     * カテゴリ詳細を取得
     */
    @CacheResult(cacheName = "category-details")
    public CategoryResponse getCategory(@CacheKey UUID categoryId) {
        Category category = Category.findById(categoryId);
        if (category == null) {
            throw new NotFoundException("Category not found: " + categoryId);
        }
        return toCategoryResponse(category);
    }

    /**
     * パスでカテゴリを取得
     */
    @CacheResult(cacheName = "category-details")
    public CategoryResponse getCategoryByPath(@CacheKey String path) {
        if (path == null || path.trim().isEmpty()) {
            throw new IllegalArgumentException("Path cannot be null or empty");
        }
        Category category = Category.findByPath(path)
                .orElseThrow(() -> new NotFoundException("Category not found: " + path));
        return toCategoryResponse(category);
    }

    /**
     * 指定されたカテゴリの商品一覧を取得
     */
    @CacheResult(cacheName = "categories")
    public CategoryWithProductsResponse getCategoryWithProducts(@CacheKey UUID categoryId, @CacheKey int limit) {
        Category category = Category.findById(categoryId);
        if (category == null) {
            throw new NotFoundException("Category not found: " + categoryId);
        }

        // カテゴリに属する商品を取得（公開済み・アクティブなもののみ）
        List<Product> products = Product.find(
            "category.id = ?1 AND publishStatus = 'PUBLISHED' AND isActive = true ORDER BY createdAt DESC",
            categoryId
        ).range(0, limit - 1).list();

        List<ProductSummaryResponse> productSummaries = products.stream()
                .map(this::toProductSummaryResponse)
                .collect(Collectors.toList());

        return new CategoryWithProductsResponse(
                category.id,
                category.name,
                category.description,
                category.path,
                category.level,
                category.sortOrder,
                category.imageUrl,
                category.isActive,
                category.getProductCount(),
                productSummaries
        );
    }

    /**
     * 親カテゴリのサブカテゴリ毎の商品一覧を取得
     */
    @CacheResult(cacheName = "categories")
    public List<CategoryWithProductsResponse> getSubCategoriesWithProducts(@CacheKey UUID parentId, @CacheKey int limit) {
        List<Category> subCategories = Category.findByParent(parentId);
        
        return subCategories.stream()
                .map(subCategory -> {
                    // 各サブカテゴリの商品を取得
                    List<Product> products = Product.find(
                        "category.id = ?1 AND publishStatus = 'PUBLISHED' AND isActive = true ORDER BY createdAt DESC",
                        subCategory.id
                    ).range(0, limit - 1).list();

                    List<ProductSummaryResponse> productSummaries = products.stream()
                            .map(this::toProductSummaryResponse)
                            .collect(Collectors.toList());

                    return new CategoryWithProductsResponse(
                            subCategory.id,
                            subCategory.name,
                            subCategory.description,
                            subCategory.path,
                            subCategory.level,
                            subCategory.sortOrder,
                            subCategory.imageUrl,
                            subCategory.isActive,
                            subCategory.getProductCount(),
                            productSummaries
                    );
                })
                .collect(Collectors.toList());
    }

    // プライベートメソッド

    private CategoryWithProductCountResponse toCategoryWithProductCountResponse(Category category, Long productCount) {
        return new CategoryWithProductCountResponse(
                category.id,
                category.name,
                category.description,
                category.path,
                category.level,
                category.sortOrder,
                category.imageUrl,
                category.isActive,
                productCount != null ? productCount : 0L
        );
    }

    private CategoryResponse toCategoryResponse(Category category) {
        CategorySummaryResponse parentResponse = null;
        if (category.parent != null) {
            parentResponse = new CategorySummaryResponse(
                    category.parent.id,
                    category.parent.name,
                    category.parent.path
            );
        }

        List<CategorySummaryResponse> childrenResponse = category.children.stream()
                .filter(child -> child.isActive)
                .map(child -> new CategorySummaryResponse(child.id, child.name, child.path))
                .collect(Collectors.toList());

        return new CategoryResponse(
                category.id,
                category.name,
                category.description,
                category.path,
                category.level,
                category.sortOrder,
                category.imageUrl,
                category.isActive,
                category.getProductCount(),
                parentResponse,
                childrenResponse,
                category.createdAt,
                category.updatedAt
        );
    }

    private ProductSummaryResponse toProductSummaryResponse(Product product) {
        // 現在価格を計算（セール価格があればそれを使用、なければ基本価格）
        BigDecimal currentPrice = product.salePrice != null ? product.salePrice : product.basePrice;
        boolean isOnSale = product.salePrice != null && product.salePrice.compareTo(product.basePrice) < 0;
        
        // 割引率を計算
        Integer discountPercentage = null;
        if (isOnSale && product.basePrice.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal discount = product.basePrice.subtract(product.salePrice);
            discountPercentage = discount.multiply(BigDecimal.valueOf(100))
                    .divide(product.basePrice, 0, BigDecimal.ROUND_HALF_UP).intValue();
        }

        // カテゴリ情報
        CategorySummaryResponse categoryResponse = new CategorySummaryResponse(
                product.category.id,
                product.category.name,
                product.category.path
        );

        // ブランド情報
        BrandSummaryResponse brandResponse = new BrandSummaryResponse(
                product.brand.id,
                product.brand.name,
                product.brand.logoUrl,
                product.brand.country
        );

        // プライマリ画像URL取得
        String primaryImageUrl = product.images.stream()
                .filter(img -> img.isPrimary)
                .map(img -> img.imageUrl)
                .findFirst()
                .orElse(null);

        return new ProductSummaryResponse(
                product.id,
                product.sku,
                product.name,
                product.shortDescription,
                categoryResponse,
                brandResponse,
                currentPrice,
                product.basePrice,
                isOnSale,
                discountPercentage,
                primaryImageUrl,
                true, // inStock - 仮の値
                product.isFeatured,
                null, // rating - 仮の値
                null, // reviewCount - 仮の値
                product.tags != null ? product.tags.stream().collect(Collectors.toSet()) : Set.of(),
                product.createdAt
        );
    }

    /**
     * カテゴリとそのサブカテゴリの全商品を取得
     */
    @CacheResult(cacheName = "category-all-products")
    public CategoryWithProductsResponse getCategoryWithAllProducts(@CacheKey UUID categoryId, @CacheKey int limit) {
        Category category = Category.findById(categoryId);
        if (category == null) {
            throw new NotFoundException("Category not found: " + categoryId);
        }

        // カテゴリ自体の商品とサブカテゴリの商品をまとめて取得
        List<Product> products = Product.find(
            "SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN FETCH p.category " +
            "LEFT JOIN FETCH p.brand " +
            "LEFT JOIN FETCH p.images " +
            "LEFT JOIN FETCH p.tags " +
            "WHERE (p.category.id = ?1 OR p.category.parent.id = ?1) " +
            "AND p.isActive = true " +
            "ORDER BY p.createdAt DESC",
            categoryId
        ).page(0, limit).list();

        List<ProductSummaryResponse> productSummaries = products.stream()
                .map(this::toProductSummaryResponse)
                .collect(Collectors.toList());

        return new CategoryWithProductsResponse(
                category.id,
                category.name,
                category.description,
                category.path,
                category.level,
                category.sortOrder,
                category.imageUrl,
                category.isActive,
                (long) productSummaries.size(),
                productSummaries
        );
    }
}
