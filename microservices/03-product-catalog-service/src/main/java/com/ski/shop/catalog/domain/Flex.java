package com.ski.shop.catalog.domain;

/**
 * フレックス列挙型
 */
public enum Flex {
    SOFT("ソフト"),
    MEDIUM("ミディアム"),
    HARD("ハード"),
    VERY_HARD("ベリーハード");

    private final String displayName;

    Flex(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
