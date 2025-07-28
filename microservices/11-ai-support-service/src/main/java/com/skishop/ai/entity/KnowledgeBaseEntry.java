package com.skishop.ai.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 知識ベースエンティティ
 * 
 * <p>AIアシスタントが参照する知識ベースを管理するエンティティ</p>
 * 
 * @since 1.0.0
 */
@Entity
@Table(name = "knowledge_base")
public class KnowledgeBaseEntry extends PanacheEntityBase {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;
    
    @Column(name = "title", nullable = false)
    public String title;
    
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    public String content;
    
    @Column(name = "category", nullable = false)
    public String category;
    
    @Column(name = "subcategory")
    public String subcategory;
    
    @Column(name = "keywords", columnDefinition = "TEXT")
    public String keywords;
    
    @Column(name = "embedding_vector", columnDefinition = "vector(1536)")
    public String embeddingVector;
    
    @Column(name = "language", length = 5, nullable = false)
    public String language = "ja";
    
    @Column(name = "source_url")
    public String sourceUrl;
    
    @Column(name = "source_type")
    public String sourceType;
    
    @Column(name = "confidence_score", precision = 5, scale = 4)
    public java.math.BigDecimal confidenceScore;
    
    @Column(name = "usage_count")
    public Long usageCount = 0L;
    
    @Column(name = "last_used_at")
    public LocalDateTime lastUsedAt;
    
    @Column(name = "is_active", nullable = false)
    public Boolean isActive = true;
    
    @Column(name = "priority", nullable = false)
    public Integer priority = 1;
    
    @Column(name = "tags", columnDefinition = "JSONB")
    public String tags;
    
    @Column(name = "metadata", columnDefinition = "JSONB")
    public String metadata;
    
    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    public LocalDateTime updatedAt;
    
    @Column(name = "created_by")
    public String createdBy;
    
    @Column(name = "updated_by")
    public String updatedBy;
    
    // デフォルトコンストラクタ
    public KnowledgeBaseEntry() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // コンストラクタ
    public KnowledgeBaseEntry(String title, String content, String category, String language) {
        this();
        this.title = title;
        this.content = content;
        this.category = category;
        this.language = language;
    }
    
    // ビジネスロジック
    public void incrementUsage() {
        this.usageCount++;
        this.lastUsedAt = LocalDateTime.now();
    }
    
    public void updateContent(String newContent, String updatedBy) {
        this.content = newContent;
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = updatedBy;
    }
    
    public void deactivate() {
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void activate() {
        this.isActive = true;
        this.updatedAt = LocalDateTime.now();
    }
    
    // 静的ファインダーメソッド
    public static List<KnowledgeBaseEntry> findActiveByCategory(String category) {
        return find("isActive = true and category = ?1 order by priority desc, usageCount desc", category).list();
    }
    
    public static List<KnowledgeBaseEntry> findByKeywords(String keywords) {
        return find("isActive = true and keywords like ?1 order by priority desc", "%" + keywords + "%").list();
    }
    
    public static List<KnowledgeBaseEntry> findByLanguage(String language) {
        return find("isActive = true and language = ?1 order by priority desc, usageCount desc", language).list();
    }
    
    public static List<KnowledgeBaseEntry> findMostUsed(int limit) {
        return find("isActive = true order by usageCount desc").page(0, limit).list();
    }
    
    public static List<KnowledgeBaseEntry> findRecentlyCreated(int limit) {
        return find("isActive = true order by createdAt desc").page(0, limit).list();
    }
    
    public static List<KnowledgeBaseEntry> findByCategoryAndSubcategory(String category, String subcategory) {
        return find("isActive = true and category = ?1 and subcategory = ?2 order by priority desc", 
                    category, subcategory).list();
    }
    
    public static long countActiveEntries() {
        return count("isActive = true");
    }
    
    public static long countByCategory(String category) {
        return count("isActive = true and category = ?1", category);
    }
}
