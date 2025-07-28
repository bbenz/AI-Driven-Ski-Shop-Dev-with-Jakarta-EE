package com.ski.shop.catalog.domain;

/**
 * 難易度レベル列挙型
 */
public enum DifficultyLevel {
    BEGINNER(1, "初心者"),
    INTERMEDIATE(2, "中級者"),
    ADVANCED(3, "上級者"),
    EXPERT(4, "エキスパート");

    private final int level;
    private final String displayName;

    DifficultyLevel(int level, String displayName) {
        this.level = level;
        this.displayName = displayName;
    }

    public int getLevel() {
        return level;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isHigherThan(DifficultyLevel other) {
        return this.level > other.level;
    }
}
