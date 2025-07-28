package com.ski.shop.catalog.domain;

/**
 * スキーヤープロファイル Record
 */
public record SkierProfile(
    SkierLevel level,
    Integer height,
    Integer weight,
    String style
) {
}
