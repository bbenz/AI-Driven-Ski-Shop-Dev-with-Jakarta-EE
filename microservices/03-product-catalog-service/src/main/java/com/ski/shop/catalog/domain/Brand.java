package com.ski.shop.catalog.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * ブランドエンティティ
 */
@Entity
@Table(name = "brands")
@NamedQueries({
    @NamedQuery(
        name = "Brand.findFeatured",
        query = "SELECT b FROM Brand b WHERE b.isFeatured = true AND b.isActive = true ORDER BY b.name"
    ),
    @NamedQuery(
        name = "Brand.findByCountry",
        query = "SELECT b FROM Brand b WHERE b.country = :country AND b.isActive = true ORDER BY b.name"
    )
})
public class Brand extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    public UUID id;

    @NotBlank
    @Size(max = 255)
    @Column(name = "name", nullable = false, unique = true)
    public String name;

    @Size(max = 1000)
    @Column(name = "description", length = 1000)
    public String description;

    @Size(max = 100)
    @Column(name = "country", length = 100)
    public String country;

    @Size(max = 500)
    @Column(name = "logo_url", length = 500)
    public String logoUrl;

    @Size(max = 500)
    @Column(name = "website_url", length = 500)
    public String websiteUrl;

    @Column(name = "is_featured", nullable = false)
    public boolean isFeatured = false;

    @Column(name = "is_active", nullable = false)
    public boolean isActive = true;

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

    public static List<Brand> findFeatured() {
        return find("#Brand.findFeatured").list();
    }

    public static List<Brand> findByCountry(String country) {
        return find("#Brand.findByCountry", country).list();
    }

    public static List<Brand> findActive() {
        return find("isActive = true ORDER BY name").list();
    }
}
