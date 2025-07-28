package com.ski.shop.catalog.dto;

import java.util.UUID;

/**
 * カテゴリサマリーレスポンスDTO
 */
public class CategorySummaryResponse {
    private UUID id;
    private String name;
    private String path;

    public CategorySummaryResponse() {}

    public CategorySummaryResponse(UUID id, String name, String path) {
        this.id = id;
        this.name = name;
        this.path = path;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
}
