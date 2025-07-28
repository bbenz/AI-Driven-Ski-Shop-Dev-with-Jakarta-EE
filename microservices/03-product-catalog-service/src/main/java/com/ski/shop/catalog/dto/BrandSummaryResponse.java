package com.ski.shop.catalog.dto;

import java.util.UUID;

/**
 * ブランドサマリーレスポンスDTO
 */
public class BrandSummaryResponse {
    private UUID id;
    private String name;
    private String logoUrl;
    private String country;

    public BrandSummaryResponse() {}

    public BrandSummaryResponse(UUID id, String name, String logoUrl, String country) {
        this.id = id;
        this.name = name;
        this.logoUrl = logoUrl;
        this.country = country;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
}
