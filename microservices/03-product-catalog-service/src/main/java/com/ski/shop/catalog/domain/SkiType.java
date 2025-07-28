package com.ski.shop.catalog.domain;

/**
 * スキータイプ列挙型
 */
public enum SkiType {
    ALL_MOUNTAIN("オールマウンテン"),
    CARVING("カービング"),
    FREESTYLE("フリースタイル"),
    FREERIDE("フリーライド"),
    MAINTENANCE("メンテナンス"),
    MOGUL("モーグル"),
    POWDER("パウダー"),
    PROTECTION("プロテクション"),
    RACING("レーシング"),
    TOURING("ツーリング");

    private final String displayName;

    SkiType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
