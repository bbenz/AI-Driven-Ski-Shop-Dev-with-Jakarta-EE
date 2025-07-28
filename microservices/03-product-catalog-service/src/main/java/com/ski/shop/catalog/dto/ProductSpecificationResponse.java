package com.ski.shop.catalog.dto;

import com.ski.shop.catalog.domain.*;

/**
 * 商品仕様レスポンスDTO
 */
public class ProductSpecificationResponse {
    private Material material;
    private SkiType skiType;
    private DifficultyLevel difficultyLevel;
    private String length;
    private String width;
    private String weight;
    private String radius;
    private Flex flex;

    // デフォルトコンストラクタ
    public ProductSpecificationResponse() {}

    // フルコンストラクタ
    public ProductSpecificationResponse(Material material, SkiType skiType, DifficultyLevel difficultyLevel,
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
    public Material getMaterial() { return material; }
    public void setMaterial(Material material) { this.material = material; }

    public SkiType getSkiType() { return skiType; }
    public void setSkiType(SkiType skiType) { this.skiType = skiType; }

    public DifficultyLevel getDifficultyLevel() { return difficultyLevel; }
    public void setDifficultyLevel(DifficultyLevel difficultyLevel) { this.difficultyLevel = difficultyLevel; }

    public String getLength() { return length; }
    public void setLength(String length) { this.length = length; }

    public String getWidth() { return width; }
    public void setWidth(String width) { this.width = width; }

    public String getWeight() { return weight; }
    public void setWeight(String weight) { this.weight = weight; }

    public String getRadius() { return radius; }
    public void setRadius(String radius) { this.radius = radius; }

    public Flex getFlex() { return flex; }
    public void setFlex(Flex flex) { this.flex = flex; }
}
