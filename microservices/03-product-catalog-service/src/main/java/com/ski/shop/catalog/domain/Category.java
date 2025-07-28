package com.ski.shop.catalog.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.panache.common.Parameters;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * カテゴリエンティティ
 */
@Entity
@Table(name = "categories")
@NamedQueries({
    @NamedQuery(
        name = "Category.findRootCategories",
        query = "SELECT c FROM Category c WHERE c.parent IS NULL AND c.isActive = true ORDER BY c.sortOrder"
    ),
    @NamedQuery(
        name = "Category.findByParent",
        query = "SELECT c FROM Category c WHERE c.parent.id = :parentId AND c.isActive = true ORDER BY c.sortOrder"
    )
})
public class Category extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    public UUID id;

    @NotBlank
    @Size(max = 255)
    @Column(name = "name", nullable = false)
    public String name;

    @Size(max = 1000)
    @Column(name = "description", length = 1000)
    public String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    public Category parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    public List<Category> children = new ArrayList<>();

    @NotBlank
    @Size(max = 500)
    @Column(name = "path", nullable = false, length = 500)
    public String path;

    @NotNull
    @Column(name = "level", nullable = false)
    public Integer level;

    @NotNull
    @Column(name = "sort_order", nullable = false)
    public Integer sortOrder = 0;

    @Column(name = "is_active", nullable = false)
    public boolean isActive = true;

    @Size(max = 255)
    @Column(name = "image_url")
    public String imageUrl;

    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt;

    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        calculatePath();
        calculateLevel();
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDateTime.now();
        calculatePath();
        calculateLevel();
    }

    /**
     * パスを計算（例：/ski/alpine/carving）
     */
    private void calculatePath() {
        if (parent == null) {
            this.path = "/" + name.toLowerCase().replace(" ", "-");
        } else {
            this.path = parent.path + "/" + name.toLowerCase().replace(" ", "-");
        }
    }

    /**
     * レベルを計算
     */
    private void calculateLevel() {
        if (parent == null) {
            this.level = 0;
        } else {
            this.level = parent.level + 1;
        }
    }

    /**
     * ルートカテゴリかどうか
     */
    public boolean isRoot() {
        return parent == null;
    }

    /**
     * 子カテゴリがあるかどうか
     */
    public boolean hasChildren() {
        return !children.isEmpty();
    }

    /**
     * 祖先カテゴリのリストを取得
     */
    public List<Category> getAncestors() {
        List<Category> ancestors = new ArrayList<>();
        Category current = this.parent;
        while (current != null) {
            ancestors.add(0, current); // 先頭に追加（ルートから順番に）
            current = current.parent;
        }
        return ancestors;
    }

    /**
     * 子孫カテゴリのリストを取得（再帰的）
     */
    public List<Category> getDescendants() {
        List<Category> descendants = new ArrayList<>();
        for (Category child : children) {
            descendants.add(child);
            descendants.addAll(child.getDescendants());
        }
        return descendants;
    }

    // 静的ファインダーメソッド

    public static List<Category> findRootCategories() {
        return find("#Category.findRootCategories").list();
    }

    public static List<Category> findByParent(UUID parentId) {
        return find("#Category.findByParent", Parameters.with("parentId", parentId)).list();
    }

    public static Optional<Category> findByPath(String path) {
        return find("path = ?1 AND isActive = true", path).firstResultOptional();
    }

    public static List<Category> findByLevel(Integer level) {
        return find("level = ?1 AND isActive = true ORDER BY sortOrder", level).list();
    }

        /**
     * このカテゴリに属するアクティブで公開済みの商品数を取得
     */
    public long getProductCount() {
        return Product.count("category.id = ?1 AND isActive = true AND publishStatus = 'PUBLISHED'", this.id);
    }

    /**
     * 全カテゴリと商品数を取得
     */
    @SuppressWarnings("unchecked")
    public static List<Object[]> findAllWithProductCount() {
        return getEntityManager().createQuery(
            "SELECT c, " +
            "(SELECT COUNT(p) FROM Product p WHERE p.category.id = c.id AND p.isActive = true AND p.publishStatus = 'PUBLISHED') " +
            "FROM Category c WHERE c.isActive = true ORDER BY c.sortOrder"
        ).getResultList();
    }
}
