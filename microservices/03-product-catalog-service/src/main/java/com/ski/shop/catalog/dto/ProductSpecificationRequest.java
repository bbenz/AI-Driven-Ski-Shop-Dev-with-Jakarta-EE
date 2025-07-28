package com.ski.shop.catalog.dto;

import com.ski.shop.catalog.domain.*;
import jakarta.validation.constraints.NotNull;

/**
 * 商品仕様リクエストDTO
 */
public class ProductSpecificationRequest {
    
    @NotNull(message = "マテリアルは必須です")
    private Material material;
    
    @NotNull(message = "スキータイプは必須です")
    private SkiType skiType;
    
    @NotNull(message = "難易度レベルは必須です")
    private DifficultyLevel difficultyLevel;
    
    private String length;
    private String width;
    private String weight;
    private String radius;
    private Flex flex;

    // デフォルトコンストラクタ
    public ProductSpecificationRequest() {}

    // フルコンストラクタ
    public ProductSpecificationRequest(Material material, SkiType skiType, DifficultyLevel difficultyLevel,
                                     String length, String width, String weight, String radius, Flex flex) {
        this.material = material;
        this.skiType = skiType;
        this.difficultyLevel = difficultyLevel;
        this.length = length;
        this.width = width;
        this.weight = weight;
        this.radius = radius;
        this.flex = flex;
    }

    // Getters and Setters
    public Material material() { return material; }
    public void setMaterial(Material material) { this.material = material; }

    public SkiType skiType() { return skiType; }
    public void setSkiType(SkiType skiType) { this.skiType = skiType; }

    public DifficultyLevel difficultyLevel() { return difficultyLevel; }
    public void setDifficultyLevel(DifficultyLevel difficultyLevel) { this.difficultyLevel = difficultyLevel; }

    public String length() { return length; }
    public void setLength(String length) { this.length = length; }

    public String width() { return width; }
    public void setWidth(String width) { this.width = width; }

    public String weight() { return weight; }
    public void setWeight(String weight) { this.weight = weight; }

    public String radius() { return radius; }
    public void setRadius(String radius) { this.radius = radius; }

    public Flex flex() { return flex; }
    public void setFlex(Flex flex) { this.flex = flex; }
}
