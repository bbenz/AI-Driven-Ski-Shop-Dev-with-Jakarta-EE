package com.ski.shop.catalog.domain;

/**
 * スキーヤーレベル列挙型
 */
public enum SkierLevel {
    BEGINNER("初心者"),
    INTERMEDIATE("中級者"),
    ADVANCED("上級者"),
    EXPERT("エキスパート");

    private final String displayName;

    SkierLevel(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
